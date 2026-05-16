package com.snapdoc.app.feature.document

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.model.AiSummary
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.model.Page
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.data.repository.OcrRepository
import com.snapdoc.app.core.data.repository.SummaryRepository
import com.snapdoc.app.core.network.GeminiClient
import com.snapdoc.app.navigation.SnapdocRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

data class DocumentDetailUiState(
    val document: Document? = null,
    val pages: List<Page> = emptyList(),
    val ocrText: String? = null,
    val summary: AiSummary? = null,
    val summarizing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DocumentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val docs: DocumentRepository,
    private val ocr: OcrRepository,
    private val summaries: SummaryRepository,
    private val gemini: GeminiClient,
) : ViewModel() {

    private val documentId: Long = checkNotNull(
        savedStateHandle.get<String>(SnapdocRoute.Document.ARG_ID)?.toLongOrNull(),
    ) { "Missing document id arg" }

    private val ephemeral = MutableStateFlow(DocumentDetailUiState())

    val state: StateFlow<DocumentDetailUiState> = combine(
        docs.observeById(documentId),
        docs.observePages(documentId),
        summaries.observeSummaryFor(documentId),
        ephemeral,
    ) { doc, pages, summary, transient ->
        transient.copy(document = doc, pages = pages, summary = summary)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentDetailUiState())

    init {
        viewModelScope.launch {
            ephemeral.update { it.copy(ocrText = ocr.textFor(documentId)) }
        }
    }

    fun toggleFavorite() {
        val doc = state.value.document ?: return
        viewModelScope.launch { docs.setFavorite(doc.id, !doc.favorite) }
    }

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            docs.delete(documentId)
            onDeleted()
        }
    }

    fun requestSummary() {
        val cached = state.value.summary
        if (cached != null) return
        val text = state.value.ocrText
        if (text.isNullOrBlank()) {
            ephemeral.update { it.copy(error = "No text extracted yet — try opening this document again in a moment.") }
            return
        }
        ephemeral.update { it.copy(summarizing = true, error = null) }
        viewModelScope.launch {
            gemini.summarize(text).fold(
                onSuccess = { summary ->
                    summaries.save(documentId, summary)
                    ephemeral.update { it.copy(summarizing = false) }
                },
                onFailure = { error ->
                    Timber.w(error, "Summary failed")
                    ephemeral.update {
                        it.copy(
                            summarizing = false,
                            error = "Could not reach AI service. Please check your connection.",
                        )
                    }
                },
            )
        }
    }

    fun regenerateSummary() {
        viewModelScope.launch {
            summaries.deleteFor(documentId)
            requestSummary()
        }
    }

    fun dismissError() = ephemeral.update { it.copy(error = null) }
}
