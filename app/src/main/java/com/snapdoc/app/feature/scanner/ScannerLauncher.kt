package com.snapdoc.app.feature.scanner

import android.app.Activity.RESULT_OK
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_JPEG
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.RESULT_FORMAT_PDF
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions.SCANNER_MODE_FULL
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import timber.log.Timber

data class ScanResult(
    val pdfUri: Uri?,
    val pageImageUris: List<Uri>,
)

/**
 * Hosts the ML Kit Document Scanner activity result contract. Renders no
 * UI of its own — caller passes [trigger] = true to launch the scanner.
 */
@Composable
fun ScannerLauncher(
    trigger: Boolean,
    onResult: (ScanResult?) -> Unit,
) {
    val context = LocalContext.current
    val scanner = remember {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(true)
            .setPageLimit(20)
            .setResultFormats(RESULT_FORMAT_JPEG, RESULT_FORMAT_PDF)
            .setScannerMode(SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options)
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            val data = GmsDocumentScanningResult.fromActivityResultIntent(activityResult.data)
            val pages = data?.pages?.mapNotNull { it.imageUri } ?: emptyList()
            val pdf = data?.pdf?.uri
            onResult(ScanResult(pdfUri = pdf, pageImageUris = pages))
        } else {
            onResult(null)
        }
    }

    LaunchedEffect(trigger) {
        if (!trigger) return@LaunchedEffect
        scanner.getStartScanIntent(context as androidx.activity.ComponentActivity)
            .addOnSuccessListener { intentSender ->
                launcher.launch(IntentSenderRequest.Builder(intentSender).build())
            }
            .addOnFailureListener { error ->
                Timber.e(error, "Failed to start document scanner")
                onResult(null)
            }
    }
}
