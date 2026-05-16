package com.snapdoc.app.feature.document

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.snapdoc.app.core.ui.components.GhostButton
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.PrimaryButton
import com.snapdoc.app.core.ui.components.SnapdocSpinner
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.core.util.formatMetadata
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun DocumentDetailScreen(
    onBack: () -> Unit,
    onDeleted: () -> Unit,
    viewModel: DocumentDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val colors = SnapdocTheme.colors
    val context = LocalContext.current
    val activity = context as? Activity
    var summarySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            SnapdocTopAppBar(
                title = state.document?.filename ?: "Document",
                leading = {
                    IconOnlyButton(
                        icon = Icons.Outlined.ArrowBack,
                        contentDescription = "Back",
                        onClick = onBack,
                    )
                },
                actions = {
                    val doc = state.document
                    if (doc != null) {
                        IconOnlyButton(
                            icon = if (doc.favorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            onClick = viewModel::toggleFavorite,
                        )
                        IconOnlyButton(
                            icon = Icons.Outlined.Share,
                            contentDescription = "Share",
                            onClick = { sharePdf(context, doc.pdfPath) },
                        )
                        IconOnlyButton(
                            icon = Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            onClick = { viewModel.delete(onDeleted) },
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(padding),
        ) {
            val doc = state.document
            if (doc == null) {
                Box(modifier = Modifier.fillMaxSize())
                return@Column
            }
            Text(
                formatMetadata(doc.pageCount, doc.fileSizeBytes, doc.createdAt),
                style = SnapdocText.caption,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(state.pages, key = { it.id }) { page ->
                    AsyncImage(
                        model = File(page.imagePath),
                        contentDescription = "Page ${page.pageIndex + 1}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(colors.surfaceVariant),
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PrimaryButton(
                    text = "AI Summary",
                    leadingIcon = Icons.Outlined.AutoAwesome,
                    onClick = {
                        summarySheet = true
                        viewModel.requestSummary(activity)
                    },
                    modifier = Modifier.weight(1f),
                )
                GhostButton(text = "Close", onClick = onBack)
            }
        }
    }

    if (summarySheet) {
        ModalBottomSheet(
            onDismissRequest = { summarySheet = false },
            sheetState = sheetState,
            containerColor = colors.surface,
        ) {
            AiSummarySheetContent(
                state = state,
                onRegenerate = { viewModel.regenerateSummary(activity) },
                onClose = {
                    summarySheet = false
                    scope.launch { sheetState.hide() }
                },
            )
        }
    }
}

@Composable
private fun AiSummarySheetContent(
    state: DocumentDetailUiState,
    onRegenerate: () -> Unit,
    onClose: () -> Unit,
) {
    val colors = SnapdocTheme.colors
    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp).fillMaxWidth()) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("AI Summary", style = SnapdocText.headlineMd, color = colors.textPrimary)
            Spacer(modifier = Modifier.weight(1f))
            GhostButton(text = "Regenerate", onClick = onRegenerate)
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Generated from your document text. Not stored on a server.",
            style = SnapdocText.labelSm,
            color = colors.textTertiary,
        )
        Spacer(Modifier.height(16.dp))
        when {
            state.summarizing -> {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    SnapdocSpinner(accent = true)
                    Spacer(Modifier.padding(start = 12.dp))
                    Text("Reading your document…", style = SnapdocText.bodyLg, color = colors.textSecondary)
                }
            }
            state.error != null -> {
                Text(state.error.orEmpty(), style = SnapdocText.bodyLg, color = colors.error)
            }
            state.summary != null -> {
                Text("TL;DR", style = SnapdocText.titleMd, color = colors.textSecondary)
                Spacer(Modifier.height(4.dp))
                Text(state.summary!!.tldr, style = SnapdocText.bodyLg, color = colors.textPrimary)
                Spacer(Modifier.height(16.dp))
                state.summary!!.bullets.forEach { bullet ->
                    Row(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text("•  ", style = SnapdocText.bodyLg, color = colors.textPrimary)
                        Text(bullet, style = SnapdocText.bodyLg, color = colors.textPrimary)
                    }
                }
            }
            else -> {
                Text("Tap regenerate to create a summary.", style = SnapdocText.bodyLg, color = colors.textSecondary)
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun sharePdf(context: android.content.Context, pdfPath: String) {
    val file = File(pdfPath)
    if (!file.exists()) return
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share document"))
}
