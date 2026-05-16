package com.snapdoc.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val documents: List<Document> = emptyList(),
    val loading: Boolean = true,
    val removeAdsPurchased: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val documents: DocumentRepository,
    prefs: UserPreferences,
) : ViewModel() {

    private val selection = MutableStateFlow<Set<Long>>(emptySet())

    val state: StateFlow<HomeUiState> = combine(
        documents.observeAll(),
        prefs.removeAdsPurchased,
        selection,
    ) { docs, removeAds, selected ->
        HomeUiState(
            documents = docs,
            loading = false,
            removeAdsPurchased = removeAds,
            selectionMode = selected.isNotEmpty(),
            selectedIds = selected,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun toggleSelection(id: Long) {
        selection.value = selection.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
    }

    fun clearSelection() {
        selection.value = emptySet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            documents.deleteMany(selection.value.toList())
            selection.value = emptySet()
        }
    }
}
