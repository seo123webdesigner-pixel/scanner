package com.snapdoc.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.db.CategoryEntity
import com.snapdoc.app.core.data.repository.CategoryRepository
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.PrimaryButton
import com.snapdoc.app.core.ui.components.SnapdocTextField
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ManageCategoriesViewModel @Inject constructor(
    private val repo: CategoryRepository,
) : ViewModel() {
    val categories: StateFlow<List<CategoryEntity>> = repo.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String) {
        viewModelScope.launch { runCatching { repo.add(name) } }
    }
    fun delete(id: Long) {
        viewModelScope.launch { repo.delete(id) }
    }
}

@Composable
fun ManageCategoriesScreen(
    onBack: () -> Unit,
    viewModel: ManageCategoriesViewModel = hiltViewModel(),
) {
    val list by viewModel.categories.collectAsState()
    val colors = SnapdocTheme.colors
    var newName by remember { mutableStateOf("") }

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
            Row(modifier = Modifier.padding(16.dp)) {
                SnapdocTextField(
                    value = newName,
                    onValueChange = { newName = it },
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
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                items(list, key = { it.id }) { cat ->
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    ) {
                        Text(cat.name, style = SnapdocText.bodyLg, color = colors.textPrimary, modifier = Modifier.weight(1f))
                        if (cat.isBuiltIn) {
                            Text("Built-in", style = SnapdocText.labelSm, color = colors.textTertiary)
                        } else {
                            IconOnlyButton(
                                icon = Icons.Outlined.Delete,
                                contentDescription = "Delete ${cat.name}",
                                onClick = { viewModel.delete(cat.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
