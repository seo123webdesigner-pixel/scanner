package com.snapdoc.app.core.ml

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Thin wrapper around ML Kit Text Recognition v2. Runs entirely on-device.
 *
 * Spec §2.2 calls for Latin + Devanagari + Tamil + Telugu + Kannada +
 * Malayalam + Bengali + Gujarati. We ship Latin + Devanagari today
 * because they cover the dominant Indian English/Hindi use case at
 * minimum APK cost; remaining scripts are loaded as separate ML Kit
 * recognizer modules — add them per [recognizers] when releases include
 * those locales.
 */
@Singleton
class OcrEngine @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val recognizers: List<TextRecognizer> = listOf(
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS),
        TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build()),
    )

    /** Runs each enabled recognizer on the image and returns the longest result. */
    suspend fun extractText(uri: Uri): String {
        val image = InputImage.fromFilePath(context, uri)
        val results = recognizers.map { rec ->
            suspendCancellableCoroutine<String> { cont ->
                rec.process(image)
                    .addOnSuccessListener { cont.resume(it.text.orEmpty()) }
                    .addOnFailureListener { cont.resumeWithException(it) }
            }
        }
        return results.maxByOrNull { it.length }.orEmpty()
    }
}
