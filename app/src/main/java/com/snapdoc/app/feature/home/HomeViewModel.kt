package com.snapdoc.app.feature.home

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.ads.nativead.NativeAd
import com.snapdoc.app.core.ads.NativeAdManager
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.repository.CategoryRepository
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.review.AppReviewManager
import com.snapdoc.app.core.storage.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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
private const val SAVES_BEFORE_REVIEW = 3

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val documents: DocumentRepository,
    private val prefs: UserPreferences,
    nativeAdManager: NativeAdManager,
    categories: CategoryRepository,
    private val reviews: AppReviewManager,
) : ViewModel() {

    private val selection = MutableStateFlow<Set<Long>>(emptySet())

    /** Categories the selection can be moved into — the 6 built-ins plus any custom. */
    val folders: StateFlow<List<String>> = categories.observeAll()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Prior (id -> category) of the last bulk move, so it can be undone via snackbar.
    private var lastMove: Map<Long, String>? = null

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

    /**
     * True once the user has saved enough documents to be asked for a review,
     * and only until the in-app review card has been surfaced once.
     */
    val reviewDue: StateFlow<Boolean> = combine(
        prefs.saveCount,
        prefs.reviewPrompted,
    ) { saves, prompted ->
        saves >= SAVES_BEFORE_REVIEW && !prompted
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Surfaces Google's in-app review card once, after the save milestone. */
    suspend fun maybeAskForReview(activity: Activity) {
        if (!reviewDue.value) return
        prefs.setReviewPrompted(true)
        reviews.launch(activity)
    }

    fun toggleSelection(id: Long) {
        selection.value = selection.value.toMutableSet().apply {
            if (!add(id)) remove(id)
        }
    }

    fun clearSelection() {
        selection.value = emptySet()
    }

    fun selectAll(ids: List<Long>) {
        selection.value = ids.toSet()
    }

    fun deleteSelected() {
        viewModelScope.launch {
            documents.deleteMany(selection.value.toList())
            selection.value = emptySet()
        }
    }

    /** Move every selected document into [category], remembering prior folders for undo. */
    fun moveSelected(category: String) {
        val ids = selection.value
        if (ids.isEmpty()) return
        lastMove = state.value.documents
            .filter { it.id in ids }
            .associate { it.id to it.category }
        viewModelScope.launch {
            documents.setCategoryMany(ids.toList(), category)
            selection.value = emptySet()
        }
    }

    fun undoLastMove() {
        val prior = lastMove ?: return
        lastMove = null
        viewModelScope.launch {
            prior.forEach { (id, category) -> documents.setCategory(id, category) }
        }
    }
}
