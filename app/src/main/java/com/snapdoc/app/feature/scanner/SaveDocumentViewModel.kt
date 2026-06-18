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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
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
    val reviewing: Boolean = false,
    /** Category is being suggested in the background (the one network step). */
    val detecting: Boolean = false,
    val suggestedName: String = "",
    val category: String = "",
    /** True until the user manually picks a folder — drives the "Auto" badge. */
    val categoryWasAuto: Boolean = true,
    val saved: Boolean = false,
    val savedDocumentId: Long? = null,
    val error: String? = null,
)

/**
 * Orchestrates everything between "scanner closed" and "row in the documents
 * table". The Save review screen (spec 13) is shown immediately; the local
 * work (copy + on-device OCR) and the one network step (Gemini category
 * suggestion) run in the background so the user never waits on a screen that
 * looks like an upload. Nothing but extracted text ever leaves the device.
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

    // Scratch state held between "shown" and "committed".
    private var pendingPdf: File? = null
    private var pendingPages: List<File> = emptyList()
    private var pendingOcr: String = ""
    private var pendingSizeBytes: Long = 0

    // Local prep (copy + OCR) — everything commit needs. Held so commit/discard
    // can wait for it; it's all on-device and fast.
    private var prepJob: Job? = null
    private var nameEditedByUser = false
    private var categoryChosenByUser = false

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
        if (_state.value.reviewing) return

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        nameEditedByUser = false
        categoryChosenByUser = false

        // Show the Save screen straight away with a provisional name.
        _state.value = SaveUiState(
            reviewing = true,
            detecting = true,
            suggestedName = storage.sanitizeFilename("Scan - $today"),
            category = "",
            categoryWasAuto = true,
        )

        // Copy the files and run on-device OCR — all local, never uploaded.
        // Off the main thread so the just-shown Save screen stays responsive.
        prepJob = viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
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
                    pendingPdf = pdfFile
                    pendingPages = pageFiles
                    pendingSizeBytes = pdfFile?.length() ?: pageFiles.sumOf { it.length() }

                    val autoOcr = prefs.autoOcr.first()
                    pendingOcr = if (autoOcr && pageFiles.isNotEmpty()) {
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
                }
            } catch (t: Throwable) {
                Timber.e(t, "Preparing scan failed")
                _state.update { it.copy(error = "Couldn't process your scan. Please try again.") }
            }
        }

        // Suggest a category in the background (the only step that uses the
        // network). The Save screen is already visible; this just fills it in.
        viewModelScope.launch {
            prepJob?.join()
            val text = pendingOcr
            val detected = if (text.isNotBlank()) {
                runCatching { gemini.classify(text).displayName }
                    .getOrDefault(BuiltInCategory.Other.displayName)
            } else BuiltInCategory.Other.displayName
            _state.update { st ->
                if (st.saved) return@update st
                st.copy(
                    detecting = false,
                    category = if (categoryChosenByUser) st.category else detected,
                    suggestedName = if (nameEditedByUser) st.suggestedName
                        else storage.sanitizeFilename("$detected - $today"),
                )
            }
        }
    }

    fun updateName(name: String) {
        nameEditedByUser = true
        _state.update { it.copy(suggestedName = name) }
    }

    fun selectCategory(category: String) {
        categoryChosenByUser = true
        _state.update { it.copy(category = category, categoryWasAuto = false, detecting = false) }
    }

    /** Persist the document with the (possibly edited) name + folder. */
    fun commit() {
        if (_state.value.saved) return
        viewModelScope.launch {
            prepJob?.join() // ensure files are copied + OCR captured (local, fast)
            val current = _state.value
            val name = storage.sanitizeFilename(current.suggestedName)
            val category = current.category.ifBlank { BuiltInCategory.Other.displayName }
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
            prepJob?.join()
            pendingPdf?.let { storage.delete(it.absolutePath) }
            pendingPages.forEach { storage.delete(it.absolutePath) }
            onDiscarded()
        }
    }
}
