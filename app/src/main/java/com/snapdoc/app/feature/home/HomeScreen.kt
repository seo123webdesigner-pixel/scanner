package com.snapdoc.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snapdoc.app.core.ui.components.DocumentCard
import com.snapdoc.app.core.ui.components.EmptyState
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.PrimaryButton
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.core.util.formatMetadata
import com.snapdoc.app.feature.home.support.categoryColorsFor

@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onSearch: () -> Unit,
    onOpen: (Long) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val colors = SnapdocTheme.colors

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            SnapdocTopAppBar(
                title = "Snapdoc",
                actions = {
                    IconOnlyButton(
                        icon = Icons.Outlined.Search,
                        contentDescription = "Search",
                        onClick = onSearch,
                    )
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onScan,
                containerColor = colors.primary,
                contentColor = colors.onPrimary,
            ) {
                Icon(Icons.Outlined.DocumentScanner, contentDescription = "Scan", modifier = Modifier.size(24.dp))
            }
        },
    ) { padding ->
        if (state.loading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding))
        } else if (state.documents.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.FileCopy,
                title = "Scan your first document.",
                body = "Tap the scan button to capture a bill, receipt, ID, or note. It'll be saved on your phone — nowhere else.",
                modifier = Modifier.padding(padding),
                action = {
                    PrimaryButton(text = "Scan now", onClick = onScan)
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.bg),
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 96.dp,
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
                            onClick = { onOpen(doc.id) },
                            selected = doc.id in state.selectedIds,
                        )
                    }
                }
            }
        }
    }
}
