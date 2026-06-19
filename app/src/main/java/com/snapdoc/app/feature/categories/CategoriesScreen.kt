package com.snapdoc.app.feature.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import com.snapdoc.app.core.data.db.CategoryEntity
import com.snapdoc.app.core.data.repository.CategoryRepository
import com.snapdoc.app.core.data.repository.DocumentRepository
import com.snapdoc.app.core.ui.components.CategoryCard
import com.snapdoc.app.core.ui.components.GhostButton
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.feature.home.support.categoryColorsFor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** A category tile: its name plus how many documents are filed in it. */
data class CategoryTile(val name: String, val count: Int)

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    docs: DocumentRepository,
    categoryRepo: CategoryRepository,
) : ViewModel() {

    /**
     * Every category — the 6 built-ins (canonical order) then custom ones
     * (alphabetical) — each with a live document count, including zero so an
     * empty category still shows a card the user can tap into (spec screen 7).
     */
    val categories: StateFlow<List<CategoryTile>> = combine(
        docs.observeAll(),
        categoryRepo.observeAll(),
    ) { documents, cats ->
        val counts = documents.groupingBy { it.category }.eachCount()
        cats.sortedWith(
            compareByDescending<CategoryEntity> { it.isBuiltIn }
                .thenBy { if (it.isBuiltIn) it.sortOrder else Int.MAX_VALUE }
                .thenBy { it.name.lowercase() },
        ).map { CategoryTile(it.name, counts[it.name] ?: 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

@Composable
fun CategoriesScreen(
    onOpenCategory: (String) -> Unit,
    onManageCategories: () -> Unit,
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsState()
    val colors = SnapdocTheme.colors

    Scaffold(
        containerColor = colors.bg,
        topBar = { SnapdocTopAppBar(title = "Categories") },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize().background(colors.bg),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(categories, key = { it.name }) { tile ->
                val accent = categoryColorsFor(tile.name, colors)
                CategoryCard(
                    label = tile.name,
                    count = tile.count,
                    icon = Icons.Outlined.GridView,
                    accentColor = accent.strip,
                    onClick = { onOpenCategory(tile.name) },
                )
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                GhostButton(
                    text = "Manage categories",
                    onClick = onManageCategories,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        }
    }
}
