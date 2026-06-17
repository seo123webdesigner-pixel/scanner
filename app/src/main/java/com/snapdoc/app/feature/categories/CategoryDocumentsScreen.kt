package com.snapdoc.app.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.ui.components.DocumentCard
import com.snapdoc.app.core.ui.components.EmptyState
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.core.util.formatMetadata
import com.snapdoc.app.feature.home.support.categoryColorsFor
import com.snapdoc.app.navigation.SnapdocRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CategoryDocumentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    docs: DocumentRepository,
) : ViewModel() {

    val category: String =
        savedStateHandle.get<String>(SnapdocRoute.CategoryDocuments.ARG_NAME).orEmpty()

    val documents: StateFlow<List<Document>> = docs.observeByCategory(category)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun CategoryDocumentsScreen(
    onBack: () -> Unit,
    onOpen: (Long) -> Unit,
    viewModel: CategoryDocumentsViewModel = hiltViewModel(),
) {
    val documents by viewModel.documents.collectAsState()
    val colors = SnapdocTheme.colors

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            SnapdocTopAppBar(
                title = viewModel.category,
                leading = {
                    IconOnlyButton(
                        icon = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                    )
                },
            )
        },
    ) { padding ->
        if (documents.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.FileCopy,
                title = "Nothing here yet.",
                body = "Documents in \"${viewModel.category}\" will appear here.",
                modifier = Modifier.padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.bg),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 16.dp,
                ),
            ) {
                items(documents, key = { it.id }) { doc ->
                    val accent = categoryColorsFor(doc.category, colors)
                    Box(modifier = Modifier.padding(vertical = 4.dp)) {
                        DocumentCard(
                            filename = doc.filename,
                            metadata = formatMetadata(doc.pageCount, doc.fileSizeBytes, doc.createdAt),
                            categoryName = doc.category,
                            categoryStripColor = accent.strip,
                            categoryChipTextColor = accent.chipText,
                            onClick = { onOpen(doc.id) },
                        )
                    }
                }
            }
        }
    }
}
