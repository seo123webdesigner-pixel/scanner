package com.snapdoc.app.feature.scanner

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.model.BuiltInCategory
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.data.repository.NewPage
import com.snapdoc.app.core.data.repository.OcrRepository
import com.snapdoc.app.core.ml.OcrEngine
import com.snapdoc.app.core.network.GeminiClient
import com.snapdoc.app.core.storage.FileStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.snapdoc.app.core.storage.UserPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

data class SaveUiState(
    val suggestedName: String = "",
    val category: String = BuiltInCategory.Other.displayName,
    val processing: Boolean = false,
    val saved: Boolean = false,
    val savedDocumentId: Long? = null,
    val error: String? = null,
)

/**
 * Orchestrates everything that has to happen between "scanner closed" and
 * "row in the documents table": copy PDF/images into app storage, run OCR,
 * call Gemini for auto-category, persist.
 */
@HiltViewModel
class SaveDocumentViewModel @Inject constructor(
    private val storage: FileStorage,
    private val docs: DocumentRepository,
    private val ocr: OcrRepository,
    private val ocrEngine: OcrEngine,
    private val gemini: GeminiClient,
    private val prefs: UserPreferences,
) : ViewModel() {

    private val _state = MutableStateFlow(SaveUiState())
    val state: StateFlow<SaveUiState> = _state.asStateFlow()

    fun onScanResult(pdfUri: Uri?, pageUris: List<Uri>) {
        if (pdfUri == null && pageUris.isEmpty()) return
        _state.update { it.copy(processing = true) }
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val provisional = storage.sanitizeFilename("Scan - $today")
                _state.update { it.copy(suggestedName = provisional) }

                val pdfFile = pdfUri?.let {
                    val dest = storage.newPdfFile(provisional)
                    storage.copyUriIntoStorage(it, dest)
                    dest
                }
                val pageFiles = pageUris.mapIndexed { index, uri ->
                    val dest = storage.newPageImageFile(provisional, index)
                    storage.copyUriIntoStorage(uri, dest)
                    dest
                }

                // Run OCR. Pages from the scanner are JPEGs on local storage.
                val autoOcr = prefs.autoOcr.first()
                val ocrText: String = if (autoOcr && pageFiles.isNotEmpty()) {
                    runCatching {
                        pageFiles.joinToString("\n\n") { file ->
                            ocrEngine.extractText(Uri.fromFile(file))
                        }
                    }.getOrElse {
                        Timber.w(it, "OCR failed; saving without text index")
                        ""
                    }
                } else ""

                // Auto-categorize via Gemini if we have text. Falls through to "Other".
                val category: String = if (ocrText.isNotBlank()) {
                    runCatching { gemini.classify(ocrText).displayName }
                        .getOrDefault(BuiltInCategory.Other.displayName)
                } else BuiltInCategory.Other.displayName

                val finalName = storage.sanitizeFilename(
                    "${category} - $today - ${ocrText.take(4).hashCode().toString(16).take(4)}",
                )

                val docId = docs.create(
                    filename = finalName,
                    pdfPath = pdfFile?.absolutePath ?: pageFiles.firstOrNull()?.absolutePath.orEmpty(),
                    category = category,
                    fileSizeBytes = pdfFile?.length() ?: pageFiles.sumOf { it.length() },
                    pages = pageFiles.map { NewPage(imagePath = it.absolutePath) },
                )

                if (ocrText.isNotBlank()) ocr.save(docId, ocrText)
                prefs.incrementSaveCount()

                _state.update {
                    it.copy(
                        processing = false,
                        saved = true,
                        savedDocumentId = docId,
                        suggestedName = finalName,
                        category = category,
                    )
                }
            } catch (t: Throwable) {
                Timber.e(t, "Save document failed")
                _state.update {
                    it.copy(processing = false, error = "Couldn't save your document. Please try again.")
                }
            }
        }
    }

    fun rename(newName: String) {
        val id = _state.value.savedDocumentId ?: return
        viewModelScope.launch {
            val safe = storage.sanitizeFilename(newName)
            docs.rename(id, safe)
            _state.update { it.copy(suggestedName = safe) }
        }
    }

    fun setCategory(category: String) {
        val id = _state.value.savedDocumentId ?: return
        viewModelScope.launch {
            docs.setCategory(id, category)
            _state.update { it.copy(category = category) }
        }
    }
}
