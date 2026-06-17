package com.snapdoc.app.feature.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.FileCopy
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.ads.nativead.NativeAd
import com.snapdoc.app.core.ads.BannerAd
import com.snapdoc.app.core.ads.NativeAdCard
import com.snapdoc.app.core.data.model.Document
import com.snapdoc.app.core.ui.components.DocumentCard
import com.snapdoc.app.core.ui.components.EmptyState
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.PrimaryButton
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.core.util.formatMetadata
import com.snapdoc.app.feature.home.support.categoryColorsFor

private const val AD_INTERVAL = 7

private sealed interface HomeRow {
    data class Doc(val document: Document) : HomeRow
    data class Ad(val ad: NativeAd, val key: String) : HomeRow
}

@Composable
fun HomeScreen(
    onScan: () -> Unit,
    onSearch: () -> Unit,
    onOpen: (Long) -> Unit,
    onPaywall: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val paywallDue by viewModel.paywallDue.collectAsState()
    val colors = SnapdocTheme.colors
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // In selection mode, system back clears the selection instead of leaving Home.
    BackHandler(enabled = state.selectionMode) { viewModel.clearSelection() }

    // Surface the Remove Ads paywall once, after enough interstitials.
    LaunchedEffect(paywallDue) {
        if (paywallDue) {
            viewModel.markPaywallPrompted()
            onPaywall()
        }
    }

    Scaffold(
        containerColor = colors.bg,
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
                        IconOnlyButton(
                            icon = Icons.Outlined.Delete,
                            contentDescription = "Delete selected",
                            onClick = { showDeleteConfirm = true },
                        )
                    },
                )
            } else {
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
            }
        },
        floatingActionButton = {
            if (!state.selectionMode) {
                FloatingActionButton(
                    onClick = onScan,
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary,
                ) {
                    Icon(Icons.Outlined.DocumentScanner, contentDescription = "Scan", modifier = Modifier.size(24.dp))
                }
            }
        },
        bottomBar = { BannerAd() },
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
            // Interleave a native ad card after every 7th document, drawing from
            // the loaded pool. Empty for Remove Ads owners (state.nativeAds).
            val rows = remember(state.documents, state.nativeAds) {
                buildList<HomeRow> {
                    var adIndex = 0
                    state.documents.forEachIndexed { i, doc ->
                        add(HomeRow.Doc(doc))
                        if ((i + 1) % AD_INTERVAL == 0) {
                            state.nativeAds.getOrNull(adIndex)?.let { ad ->
                                add(HomeRow.Ad(ad, "ad-$adIndex"))
                                adIndex++
                            }
                        }
                    }
                }
            }
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
                items(
                    rows,
                    key = { row ->
                        when (row) {
                            is HomeRow.Doc -> row.document.id
                            is HomeRow.Ad -> row.key
                        }
                    },
                ) { row ->
                    when (row) {
                        is HomeRow.Doc -> {
                            val doc = row.document
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
                                    onLongClick = { viewModel.toggleSelection(doc.id) },
                                    selected = doc.id in state.selectedIds,
                                )
                            }
                        }
                        is HomeRow.Ad -> {
                            Box(modifier = Modifier.padding(vertical = 4.dp)) {
                                NativeAdCard(row.ad)
                            }
                        }
                    }
                }
            }
        }
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
