package com.snapdoc.app.feature.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.ui.components.DocumentCard
import com.snapdoc.app.core.ui.components.SearchField
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.core.util.formatMetadata
import com.snapdoc.app.feature.home.support.categoryColorsFor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(FlowPreview::class, kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val docs: DocumentRepository,
) : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val results: StateFlow<List<Document>> = _query
        .debounce(180)
        .distinctUntilChanged()
        .mapLatest { q -> if (q.isBlank()) emptyList() else docs.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun onQueryChange(value: String) { _query.value = value }
}

@Composable
fun SearchScreen(
    onOpen: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsState()
    val results by viewModel.results.collectAsState()
    val colors = SnapdocTheme.colors

    Scaffold(
        containerColor = colors.bg,
        topBar = { SnapdocTopAppBar(title = "Search") },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(colors.bg).padding(padding)) {
            SearchField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                modifier = Modifier.padding(16.dp),
                placeholder = "Search documents and text inside them",
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                items(results, key = { it.id }) { doc ->
                    val accent = categoryColorsFor(doc.category, colors)
                    DocumentCard(
                        filename = doc.filename,
                        metadata = formatMetadata(doc.pageCount, doc.fileSizeBytes, doc.createdAt),
                        categoryName = doc.category,
                        categoryStripColor = accent.strip,
                        categoryChipTextColor = accent.chipText,
                        onClick = { onOpen(doc.id) },
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
            }
        }
    }
}
