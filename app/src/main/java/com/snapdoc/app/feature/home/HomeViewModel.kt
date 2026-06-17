package com.snapdoc.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.snapdoc.app.core.ads.NativeAdManager
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeUiState(
    val documents: List<Document> = emptyList(),
    val loading: Boolean = true,
    val removeAdsPurchased: Boolean = false,
    val selectionMode: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val nativeAds: List<NativeAd> = emptyList(),
)

private const val INTERSTITIALS_BEFORE_PAYWALL = 5

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val documents: DocumentRepository,
    private val prefs: UserPreferences,
    nativeAdManager: NativeAdManager,
) : ViewModel() {

    private val selection = MutableStateFlow<Set<Long>>(emptySet())

    val state: StateFlow<HomeUiState> = combine(
        documents.observeAll(),
        prefs.removeAdsPurchased,
        selection,
        nativeAdManager.ads,
    ) { docs, removeAds, selected, ads ->
        HomeUiState(
            documents = docs,
            loading = false,
            removeAdsPurchased = removeAds,
            selectionMode = selected.isNotEmpty(),
            selectedIds = selected,
            nativeAds = if (removeAds) emptyList() else ads,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /**
     * True once the user has seen enough interstitials to be offered Remove Ads,
     * and only until the paywall has been surfaced once (or they purchase).
     */
    val paywallDue: StateFlow<Boolean> = combine(
        prefs.interstitialShownCount,
        prefs.paywallPrompted,
        prefs.removeAdsPurchased,
    ) { shown, prompted, owned ->
        shown >= INTERSTITIALS_BEFORE_PAYWALL && !prompted && !owned
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun markPaywallPrompted() {
        viewModelScope.launch { prefs.setPaywallPrompted(true) }
    }

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
