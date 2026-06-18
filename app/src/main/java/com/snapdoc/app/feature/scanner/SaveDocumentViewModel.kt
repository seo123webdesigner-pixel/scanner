package com.snapdoc.app.feature.scanner

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.ads.AdsManager
import com.snapdoc.app.core.data.model.BuiltInCategory
import com.snapdoc.app.core.data.repository.CategoryRepository
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.data.repository.NewPage
import com.snapdoc.app.core.data.repository.OcrRepository
import com.snapdoc.app.core.ml.OcrEngine
import com.snapdoc.app.core.network.GeminiClient
import com.snapdoc.app.core.storage.FileStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.snapdoc.app.core.storage.UserPreferences
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import timber.log.Timber

data class SaveUiState(
    val processing: Boolean = false,
    val reviewing: Boolean = false,
    val suggestedName: String = "",
    val category: String = BuiltInCategory.Other.displayName,
    /** True until the user manually picks a folder — drives the "Auto" badge. */
    val categoryWasAuto: Boolean = true,
    val saved: Boolean = false,
    val savedDocumentId: Long? = null,
    val error: String? = null,
)

/**
 * Orchestrates everything between "scanner closed" and "row in the documents
 * table": copy PDF/images into app storage, run OCR, call Gemini for an
 * auto-category, then hand control to the Save review screen so the user can
 * confirm or correct the folder and name before it's committed (spec 13).
 */
@HiltViewModel
class SaveDocumentViewModel @Inject constructor(
    private val storage: FileStorage,
    private val docs: DocumentRepository,
    private val ocr: OcrRepository,
    private val ocrEngine: OcrEngine,
    private val gemini: GeminiClient,
    private val prefs: UserPreferences,
    private val ads: AdsManager,
    categories: CategoryRepository,
) : ViewModel() {

    /** All folders the document can be filed into — the 6 built-ins plus any custom. */
    val folders: StateFlow<List<String>> = categories.observeAll()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow(SaveUiState())
    val state: StateFlow<SaveUiState> = _state.asStateFlow()

    // Scratch state held between "processed" and "committed".
    private var pendingPdf: File? = null
    private var pendingPages: List<File> = emptyList()
    private var pendingOcr: String = ""
    private var pendingSizeBytes: Long = 0

    /**
     * Triggers the post-save interstitial. Caller is the composable, which
     * owns the Activity reference — VMs intentionally don't hold one.
     * Short-circuits internally for Remove Ads owners.
     */
    suspend fun maybeShowInterstitial(activity: Activity) {
        ads.maybeShowInterstitial(activity)
    }

    fun onScanResult(pdfUri: Uri?, pageUris: List<Uri>) {
        if (pdfUri == null && pageUris.isEmpty()) return
        _state.update { it.copy(processing = true) }
        viewModelScope.launch {
            try {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                val provisional = storage.sanitizeFilename("Scan - $today")

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
                        val parts = mutableListOf<String>()
                        for (file in pageFiles) {
                            parts += ocrEngine.extractText(Uri.fromFile(file))
                        }
                        parts.joinToString("\n\n")
                    }.getOrElse {
                        Timber.w(it, "OCR failed; saving without text index")
                        ""
                    }
                } else ""

                // Auto-categorize via Gemini if we have text. Falls through to "Other"
                // (offline, blank text, or an API error all land here).
                val category: String = if (ocrText.isNotBlank()) {
                    runCatching { gemini.classify(ocrText).displayName }
                        .getOrDefault(BuiltInCategory.Other.displayName)
                } else BuiltInCategory.Other.displayName

                pendingPdf = pdfFile
                pendingPages = pageFiles
                pendingOcr = ocrText
                pendingSizeBytes = pdfFile?.length() ?: pageFiles.sumOf { it.length() }

                _state.update {
                    it.copy(
                        processing = false,
                        reviewing = true,
                        suggestedName = storage.sanitizeFilename("$category - $today"),
                        category = category,
                        categoryWasAuto = true,
                    )
                }
            } catch (t: Throwable) {
                Timber.e(t, "Process scan failed")
                _state.update {
                    it.copy(processing = false, error = "Couldn't process your scan. Please try again.")
                }
            }
        }
    }

    fun updateName(name: String) = _state.update { it.copy(suggestedName = name) }

    fun selectCategory(category: String) =
        _state.update { it.copy(category = category, categoryWasAuto = false) }

    /** Persist the document with the (possibly edited) name + folder. */
    fun commit() {
        val current = _state.value
        if (current.saved) return
        val name = storage.sanitizeFilename(current.suggestedName)
        val category = current.category
        viewModelScope.launch {
            try {
                val docId = docs.create(
                    filename = name,
                    pdfPath = pendingPdf?.absolutePath
                        ?: pendingPages.firstOrNull()?.absolutePath.orEmpty(),
                    category = category,
                    fileSizeBytes = pendingSizeBytes,
                    pages = pendingPages.map { NewPage(imagePath = it.absolutePath) },
                )
                if (pendingOcr.isNotBlank()) ocr.save(docId, pendingOcr)
                prefs.incrementSaveCount()
                _state.update { it.copy(saved = true, savedDocumentId = docId) }
            } catch (t: Throwable) {
                Timber.e(t, "Commit document failed")
                _state.update { it.copy(error = "Couldn't save your document. Please try again.") }
            }
        }
    }

    /** Throw away a scan the user decided not to keep, cleaning up copied files. */
    fun discard(onDiscarded: () -> Unit) {
        viewModelScope.launch {
            pendingPdf?.let { storage.delete(it.absolutePath) }
            pendingPages.forEach { storage.delete(it.absolutePath) }
            onDiscarded()
        }
    }
}
