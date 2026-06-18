package com.snapdoc.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.db.CategoryEntity
import com.snapdoc.app.core.data.repository.CategoryRepository
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.ui.components.GhostButton
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.PrimaryButton
import com.snapdoc.app.core.ui.components.SnapdocTextField
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.feature.home.support.categoryColorsFor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val repo: CategoryRepository,
    docs: DocumentRepository,
) : ViewModel() {
    val categories: StateFlow<List<CategoryEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Number of documents currently filed in each folder, keyed by folder name. */
    val counts: StateFlow<Map<String, Int>> = docs.observeAll()
        .map { list -> list.groupingBy { it.category }.eachCount() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun add(name: String) {
        viewModelScope.launch {
            runCatching { repo.add(name) }
                .onFailure { _error.value = it.message ?: "Couldn't add category" }
        }
    }

    fun rename(id: Long, name: String) {
        viewModelScope.launch {
            runCatching { repo.rename(id, name) }
                .onFailure { _error.value = it.message ?: "Couldn't rename category" }
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }

    fun clearError() {
        _error.value = null
    }
}

@Composable
fun ManageCategoriesScreen(
    onBack: () -> Unit,
    viewModel: ManageCategoriesViewModel = hiltViewModel(),
) {
    val list by viewModel.categories.collectAsState()
    val counts by viewModel.counts.collectAsState()
    val error by viewModel.error.collectAsState()
    val colors = SnapdocTheme.colors
    var newName by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<CategoryEntity?>(null) }
    var deleting by remember { mutableStateOf<CategoryEntity?>(null) }

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            SnapdocTopAppBar(
                title = "Categories",
                leading = {
                    IconOnlyButton(icon = Icons.Outlined.ArrowBack, contentDescription = "Back", onClick = onBack)
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(colors.bg).padding(padding),
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                SnapdocTextField(
                    value = newName,
                    onValueChange = {
                        newName = it
                        if (error != null) viewModel.clearError()
                    },
                    label = "New category name",
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.padding(4.dp))
                PrimaryButton(
                    text = "Add",
                    enabled = newName.isNotBlank(),
                    onClick = {
                        viewModel.add(newName)
                        newName = ""
                    },
                )
            }
            if (error != null) {
                Text(
                    error.orEmpty(),
                    style = SnapdocText.caption,
                    color = colors.error,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                items(list, key = { it.id }) { cat ->
                    val accent = categoryColorsFor(cat.name, colors)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(accent.strip),
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(cat.name, style = SnapdocText.bodyLg, color = colors.textPrimary)
                            val count = counts[cat.name] ?: 0
                            Text(
                                if (count == 1) "1 document" else "$count documents",
                                style = SnapdocText.caption,
                                color = colors.textTertiary,
                            )
                        }
                        if (cat.isBuiltIn) {
                            Text("Built-in", style = SnapdocText.labelSm, color = colors.textTertiary)
                        } else {
                            IconOnlyButton(
                                icon = Icons.Outlined.Edit,
                                contentDescription = "Rename ${cat.name}",
                                onClick = { editing = cat },
                            )
                            IconOnlyButton(
                                icon = Icons.Outlined.Delete,
                                contentDescription = "Delete ${cat.name}",
                                onClick = { deleting = cat },
                            )
                        }
                    }
                }
            }
        }
    }

    editing?.let { cat ->
        var name by remember(cat.id) { mutableStateOf(cat.name) }
        AlertDialog(
            onDismissRequest = { editing = null },
            containerColor = colors.surface,
            title = { Text("Rename category", style = SnapdocText.headlineMd, color = colors.textPrimary) },
            text = {
                Column {
                    SnapdocTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Category name",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.padding(4.dp))
                    Text(
                        "Documents in this category move with it.",
                        style = SnapdocText.caption,
                        color = colors.textTertiary,
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Save",
                    enabled = name.isNotBlank(),
                    onClick = {
                        viewModel.rename(cat.id, name)
                        editing = null
                    },
                )
            },
            dismissButton = { GhostButton(text = "Cancel", onClick = { editing = null }) },
        )
    }

    deleting?.let { cat ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            containerColor = colors.surface,
            title = { Text("Delete category?", style = SnapdocText.headlineMd, color = colors.textPrimary) },
            text = {
                Text(
                    "Delete \"${cat.name}\"? Documents in this category will move to Other.",
                    style = SnapdocText.bodyLg,
                    color = colors.textSecondary,
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Delete",
                    onClick = {
                        viewModel.delete(cat.id)
                        deleting = null
                    },
                )
            },
            dismissButton = { GhostButton(text = "Cancel", onClick = { deleting = null }) },
        )
    }
}
