package com.snapdoc.app.core.network

import com.snapdoc.app.BuildConfig
import com.snapdoc.app.core.data.model.AiSummary
import com.snapdoc.app.core.data.model.BuiltInCategory
import com.snapdoc.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
 *     text. The [redactingLogger] truncates everything to URL + status.
 */
@Singleton
class GeminiClient @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
        .addInterceptor(redactingLogger)
        .build()

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
            BuiltInCategory.fromName(raw.trim().lineSequence().firstOrNull().orEmpty())
        }.getOrDefault(BuiltInCategory.Other)
    }

    private suspend fun callWithRetry(prompt: String): String {
        val key = BuildConfig.GEMINI_API_KEY
        require(key.isNotBlank()) { "GEMINI_API_KEY missing. Add it to local.properties." }
        val url = "$ENDPOINT?key=$key"
        val body = buildJsonObject {
            put(
                "contents",
                buildJsonArray {
                    add(
                        buildJsonObject {
                            put("role", JsonPrimitive("user"))
                            put(
                                "parts",
                                buildJsonArray {
                                    add(buildJsonObject { put("text", JsonPrimitive(prompt)) })
                                },
                            )
                        },
                    )
                },
            )
        }.toString().toRequestBody(JSON_MEDIA)
        val request = Request.Builder().url(url).post(body).build()

        var attempt = 0
        var lastError: Throwable? = null
        while (attempt < MAX_ATTEMPTS) {
            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        error("HTTP ${response.code}")
                    }
                    val responseJson = response.body?.string().orEmpty()
                    val root = Json.parseToJsonElement(responseJson) as? JsonObject
                        ?: error("Gemini response was not a JSON object")
                    return extractTextFromGeminiResponse(root)
                        ?: error("Gemini response missing candidates[0].content.parts[0].text")
                }
            } catch (t: IOException) {
                lastError = t
            } catch (t: Throwable) {
                lastError = t
            }
            attempt++
            if (attempt < MAX_ATTEMPTS) delay(BACKOFFS_MS[attempt - 1])
        }
        throw IOException("Gemini failed after $MAX_ATTEMPTS attempts", lastError)
    }

    private fun extractTextFromGeminiResponse(root: JsonObject): String? {
        val candidates = root["candidates"] as? kotlinx.serialization.json.JsonArray ?: return null
        val first = candidates.firstOrNull() as? JsonObject ?: return null
        val content = first["content"] as? JsonObject ?: return null
        val parts = content["parts"] as? kotlinx.serialization.json.JsonArray ?: return null
        val text = (parts.firstOrNull() as? JsonObject)?.get("text") as? JsonPrimitive ?: return null
        return text.content
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
        private const val ENDPOINT =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent"
        private val JSON_MEDIA = "application/json".toMediaType()
        private const val MAX_ATTEMPTS = 3
        private val BACKOFFS_MS = longArrayOf(1_000, 2_000, 4_000)
    }
}

/** Logs URL + status only. Bodies contain user OCR text and must NEVER be logged. */
private val redactingLogger = okhttp3.Interceptor { chain ->
    val request = chain.request()
    val started = System.nanoTime()
    val response = chain.proceed(request)
    val tookMs = (System.nanoTime() - started) / 1_000_000
    Timber.tag("Gemini").i("%s → %d (%dms)", request.url, response.code, tookMs)
    response
}
