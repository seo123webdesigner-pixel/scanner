package com.snapdoc.app.core.network

import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.generationConfig
import com.snapdoc.app.core.data.model.AiSummary
import com.snapdoc.app.core.data.model.BuiltInCategory
import com.snapdoc.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException

/**
 * Sole Gemini integration point for Snapdoc.
 *
 * PRIVACY CONTRACT (spec §4.2):
 *   - Every public method on this class accepts ONLY a String (OCR text).
 *   - There is no code path on this class that handles raw image bytes,
 *     Bitmaps, Uris, or filenames. Adding one breaks the privacy contract
 *     printed on every onboarding screen and in the Play Store listing.
 *   - Do NOT log request or response bodies — they contain user document
 *     text. Only call outcome (ok/failed) is logged, never prompt or
 *     response content.
 *
 * Calls go through Firebase AI Logic rather than a raw REST call with an
 * embedded key: the Gemini API key lives server-side in the Firebase
 * project, never inside the compiled app, and App Check (Play Integrity)
 * attests that requests come from a genuine, unmodified build of this app
 * before Firebase forwards them to Gemini.
 */
@Singleton
class GeminiClient @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    // Summaries/classification don't need reasoning; disabling thinking
    // keeps gemini-2.5-flash fast and cheap (thinkingBudget 0).
    private val model = Firebase.ai(backend = GenerativeBackend.googleAI())
        .generativeModel(
            modelName = MODEL_NAME,
            generationConfig = generationConfig {
                thinkingConfig = thinkingConfig { thinkingBudget = 0 }
            },
        )

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    suspend fun summarize(ocrText: String): Result<AiSummary> = withContext(ioDispatcher) {
        if (ocrText.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("OCR text is blank"))
        }
        val prompt = """
            Summarize the following document text. Output JSON only with these exact fields:
            - "tldr": a single-line summary, no more than 120 characters
            - "bullets": an array of 3 to 5 strings, each no more than 100 characters

            Document text:
            $ocrText
        """.trimIndent()
        runCatching {
            val raw = callWithRetry(prompt)
            val payload = extractFirstJsonObject(raw)
                ?: error("Gemini returned no JSON payload")
            val parsed = json.decodeFromString<SummaryPayload>(payload)
            AiSummary(
                tldr = parsed.tldr.take(120),
                bullets = parsed.bullets.take(5).map { it.take(100) },
                generatedAt = System.currentTimeMillis(),
            )
        }
    }

    suspend fun classify(ocrText: String): BuiltInCategory = withContext(ioDispatcher) {
        if (ocrText.isBlank()) return@withContext BuiltInCategory.Other
        val prompt = """
            Classify this document into exactly one category from this list:
            Bills, IDs, Receipts, Notes, Contracts, Other.

            Output only the category name, with no explanation.

            Document text:
            $ocrText
        """.trimIndent()
        runCatching {
            val raw = callWithRetry(prompt)
            BuiltInCategory.fromResponse(raw)
        }.getOrDefault(BuiltInCategory.Other)
    }

    private suspend fun callWithRetry(prompt: String): String {
        var attempt = 0
        var lastError: Throwable? = null
        while (attempt < MAX_ATTEMPTS) {
            try {
                val text = model.generateContent(prompt).text
                    ?: error("Gemini response had no text")
                Timber.tag("Gemini").i("generateContent ok (attempt %d)", attempt + 1)
                return text
            } catch (t: Throwable) {
                lastError = t
                Timber.tag("Gemini").w(
                    "generateContent attempt %d failed: %s",
                    attempt + 1,
                    t.javaClass.simpleName,
                )
            }
            attempt++
            if (attempt < MAX_ATTEMPTS) delay(BACKOFFS_MS[attempt - 1])
        }
        throw IOException("Gemini failed after $MAX_ATTEMPTS attempts", lastError)
    }

    private fun extractFirstJsonObject(raw: String): String? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        return if (start in 0..end) raw.substring(start, end + 1) else null
    }

    @Serializable
    private data class SummaryPayload(
        val tldr: String,
        val bullets: List<String>,
    )

    companion object {
        private const val MODEL_NAME = "gemini-2.5-flash"
        private const val MAX_ATTEMPTS = 3
        private val BACKOFFS_MS = longArrayOf(1_000, 2_000, 4_000)
    }
}
