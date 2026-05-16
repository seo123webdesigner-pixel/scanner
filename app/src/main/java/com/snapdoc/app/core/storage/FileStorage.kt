package com.snapdoc.app.core.storage

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scoped Storage-compliant file storage. Documents are saved into
 * app-specific external storage (no permission required on API 26+),
 * with a private `cache` for temporary scratch files.
 */
@Singleton
class FileStorage @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val documentsDir: File
        get() = File(context.filesDir, "documents").apply { if (!exists()) mkdirs() }

    val pagesDir: File
        get() = File(context.filesDir, "pages").apply { if (!exists()) mkdirs() }

    val thumbsDir: File
        get() = File(context.filesDir, "thumbs").apply { if (!exists()) mkdirs() }

    val exportDir: File
        get() = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }

    fun newPdfFile(safeName: String): File =
        File(documentsDir, "$safeName.pdf")

    fun newPageImageFile(documentName: String, pageIndex: Int): File =
        File(pagesDir, "${documentName}_p${pageIndex}.jpg")

    fun newExportFile(safeName: String, extension: String): File =
        File(exportDir, "$safeName.$extension")

    fun copyInto(source: InputStream, destination: File) {
        destination.outputStream().use { source.copyTo(it) }
    }

    fun copyUriIntoStorage(uri: Uri, destination: File) {
        context.contentResolver.openInputStream(uri)?.use { input ->
            copyInto(input, destination)
        } ?: error("Could not open URI for read: $uri")
    }

    /** Returns a content:// URI suitable for sharing via Intent.ACTION_SEND. */
    fun shareUriFor(file: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun delete(path: String): Boolean = runCatching { File(path).delete() }.getOrDefault(false)

    /** Strip filesystem-hostile characters per `02_design_system.md` rules. */
    fun sanitizeFilename(name: String): String =
        name.replace(Regex("""[/\\:*?"<>|]"""), "").trim().ifEmpty { "Untitled" }
}
