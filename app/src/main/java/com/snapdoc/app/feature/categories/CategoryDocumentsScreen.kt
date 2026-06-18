package com.snapdoc.app.feature.categories

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.repository.CategoryRepository
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.ui.components.DocumentCard
import com.snapdoc.app.core.ui.components.EmptyState
import com.snapdoc.app.core.ui.components.FolderPickerSheet
import com.snapdoc.app.core.ui.components.GhostButton
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.core.util.formatMetadata
import com.snapdoc.app.feature.home.support.categoryColorsFor
import com.snapdoc.app.navigation.SnapdocRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryDocsUiState(
    val documents: List<Document> = emptyList(),
    val selectedIds: Set<Long> = emptySet(),
    val selectionMode: Boolean = false,
)

@HiltViewModel
class CategoryDocumentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val docs: DocumentRepository,
    categories: CategoryRepository,
) : ViewModel() {

    val category: String =
        savedStateHandle.get<String>(SnapdocRoute.CategoryDocuments.ARG_NAME).orEmpty()

    private val selection = MutableStateFlow<Set<Long>>(emptySet())

    // Prior (id -> category) of the last bulk move, so it can be undone.
    private var lastMove: Map<Long, String>? = null

    val state: StateFlow<CategoryDocsUiState> = combine(
        docs.observeByCategory(category),
        selection,
    ) { documents, selected ->
        CategoryDocsUiState(
            documents = documents,
            selectedIds = selected,
            selectionMode = selected.isNotEmpty(),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CategoryDocsUiState())

    /** Categories the selection can be moved into — the 6 built-ins plus any custom. */
    val folders: StateFlow<List<String>> = categories.observeAll()
        .map { list -> list.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

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

    /** Move every selected document into [target], remembering prior folders for undo. */
    fun moveSelected(target: String) {
        val ids = selection.value
        if (ids.isEmpty()) return
        lastMove = state.value.documents
            .filter { it.id in ids }
            .associate { it.id to it.category }
        viewModelScope.launch {
            docs.setCategoryMany(ids.toList(), target)
            selection.value = emptySet()
        }
    }

    fun undoLastMove() {
        val prior = lastMove ?: return
        lastMove = null
        viewModelScope.launch {
            prior.forEach { (id, category) -> docs.setCategory(id, category) }
        }
    }

    fun deleteSelected() {
        viewModelScope.launch {
            docs.deleteMany(selection.value.toList())
            selection.value = emptySet()
        }
    }
}

@Composable
fun CategoryDocumentsScreen(
    onBack: () -> Unit,
    onOpen: (Long) -> Unit,
    viewModel: CategoryDocumentsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val colors = SnapdocTheme.colors
    var showMovePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    // In selection mode, system back clears the selection instead of leaving the screen.
    BackHandler(enabled = state.selectionMode) { viewModel.clearSelection() }

    Scaffold(
        containerColor = colors.bg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (state.selectionMode) {
                SnapdocTopAppBar(
                    title = "${state.selectedIds.size} selected",
                    leading = {
                        IconOnlyButton(
                            icon = Icons.Outlined.Close,
                            contentDescription = "Clear selection",
                            onClick = viewModel::clearSelection,
                        )
                    },
                    actions = {
                        val allSelected = state.documents.isNotEmpty() &&
                            state.selectedIds.size == state.documents.size
                        GhostButton(
                            text = if (allSelected) "Deselect all" else "Select all",
                            onClick = {
                                if (allSelected) viewModel.clearSelection()
                                else viewModel.selectAll(state.documents.map { it.id })
                            },
                        )
                        IconOnlyButton(
                            icon = Icons.Outlined.Folder,
                            contentDescription = "Move selected",
                            onClick = { showMovePicker = true },
                        )
                        IconOnlyButton(
                            icon = Icons.Outlined.Delete,
                            contentDescription = "Delete selected",
                            onClick = { showDeleteConfirm = true },
                        )
                    },
                )
            } else {
                SnapdocTopAppBar(
                    title = if (state.documents.isEmpty()) {
                        viewModel.category
                    } else {
                        "${viewModel.category} · ${state.documents.size}"
                    },
                    leading = {
                        IconOnlyButton(
                            icon = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            onClick = onBack,
                        )
                    },
                )
            }
        },
    ) { padding ->
        if (state.documents.isEmpty()) {
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
                items(state.documents, key = { it.id }) { doc ->
                    val accent = categoryColorsFor(doc.category, colors)
                    Box(modifier = Modifier.padding(vertical = 4.dp)) {
                        DocumentCard(
                            filename = doc.filename,
                            metadata = formatMetadata(doc.pageCount, doc.fileSizeBytes, doc.createdAt),
                            categoryName = doc.category,
                            categoryStripColor = accent.strip,
                            categoryChipTextColor = accent.chipText,
                            onClick = {
                                if (state.selectionMode) viewModel.toggleSelection(doc.id) else onOpen(doc.id)
                            },
                            onLongClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.toggleSelection(doc.id)
                            },
                            selectionMode = state.selectionMode,
                            selected = doc.id in state.selectedIds,
                        )
                    }
                }
            }
        }
    }

    if (showMovePicker) {
        FolderPickerSheet(
            title = "Move to category",
            folders = folders,
            selected = null,
            onSelect = { target ->
                val count = state.selectedIds.size
                viewModel.moveSelected(target)
                showMovePicker = false
                scope.launch {
                    val noun = if (count == 1) "document" else "documents"
                    val result = snackbarHostState.showSnackbar(
                        message = "Moved $count $noun to $target",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) viewModel.undoLastMove()
                }
            },
            onDismiss = { showMovePicker = false },
        )
    }

    if (showDeleteConfirm) {
        val count = state.selectedIds.size
        val noun = if (count == 1) "document" else "documents"
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete $count $noun?") },
            text = { Text("This permanently removes the selected $noun from your device. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected()
                    showDeleteConfirm = false
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }
}
