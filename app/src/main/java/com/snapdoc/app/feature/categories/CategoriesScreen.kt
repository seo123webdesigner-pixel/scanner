package com.snapdoc.app.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.ui.components.CategoryCard
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.feature.home.support.categoryColorsFor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    docs: DocumentRepository,
) : ViewModel() {
    val grouped: StateFlow<Map<String, List<Document>>> = docs.observeAll()
        .map { list -> list.groupBy { it.category } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}

@Composable
fun CategoriesScreen(
    onOpenCategory: (String) -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val grouped by viewModel.grouped.collectAsState()
    val colors = SnapdocTheme.colors

    Scaffold(
        containerColor = colors.bg,
        topBar = { SnapdocTopAppBar(title = "Categories") },
    ) { padding ->
        val entries = grouped.entries.sortedBy { it.key }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().background(colors.bg),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        ) {
            items(entries, key = { it.key }) { entry ->
                val accent = categoryColorsFor(entry.key, colors)
                CategoryCard(
                    label = entry.key,
                    count = entry.value.size,
                    icon = Icons.Outlined.GridView,
                    accentColor = accent.strip,
                    onClick = { onOpenCategory(entry.key) },
                )
            }
        }
    }
}
