package com.snapdoc.app.feature.scanner

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snapdoc.app.core.ui.components.FolderPickerSheet
import com.snapdoc.app.core.ui.components.GhostButton
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.PrimaryButton
import com.snapdoc.app.core.ui.components.SecondaryButton
import com.snapdoc.app.core.ui.components.SnapdocSpinner
import com.snapdoc.app.core.ui.components.SnapdocTextField
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme

/**
 * Hosts the entire scan → review → save flow.
 *
 * ML Kit's Document Scanner provides its own crop/multi-page/filter UI,
 * so screens 9–12 from the spec are surfaced through the system overlay
 * rather than recreated in-app. We pick up after the scanner closes and
 * show the Save screen (spec 13), where the user confirms the folder + name.
 */
@Composable
fun ScannerFlowScreen(
    onSaved: (Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: SaveDocumentViewModel = hiltViewModel(),
) {
    var launched by remember { mutableStateOf(false) }
    var triggered by remember { mutableStateOf(false) }
    var showFolderPicker by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val colors = SnapdocTheme.colors
    val activity = LocalContext.current as? Activity

    LaunchedEffect(Unit) {
        if (!launched) {
            triggered = true
            launched = true
        }
    }

    ScannerLauncher(
        trigger = triggered,
        onResult = { result ->
            triggered = false
            if (result == null) {
                onCancel()
            } else {
                viewModel.onScanResult(result.pdfUri, result.pageImageUris)
            }
        },
    )

    LaunchedEffect(state.saved) {
        if (state.saved) {
            val id = state.savedDocumentId ?: return@LaunchedEffect
            activity?.let { viewModel.maybeShowInterstitial(it) }
            onSaved(id)
        }
    }

    when {
        state.reviewing && !state.saved -> {
            SaveReviewScreen(
                state = state,
                onNameChange = viewModel::updateName,
                onPickFolder = { showFolderPicker = true },
                onSave = viewModel::commit,
                onCancel = { viewModel.discard(onCancel) },
            )
        }
        state.error != null -> {
            CenteredStatus {
                Text(state.error.orEmpty(), style = SnapdocText.bodyLg, color = colors.error)
                Spacer(Modifier.height(16.dp))
                SecondaryButton(text = "Go back", onClick = onCancel)
            }
        }
        else -> {
            CenteredStatus {
                SnapdocSpinner()
                Spacer(Modifier.height(16.dp))
                Text(
                    if (state.saved) "Saving…" else "Opening scanner…",
                    style = SnapdocText.bodyLg,
                    color = colors.textSecondary,
                )
                if (!state.saved) {
                    Spacer(Modifier.height(16.dp))
                    GhostButton(text = "Cancel", onClick = onCancel)
                }
            }
        }
    }

    if (showFolderPicker) {
        FolderPickerSheet(
            title = "Choose a category",
            folders = folders,
            selected = state.category,
            onSelect = {
                viewModel.selectCategory(it)
                showFolderPicker = false
            },
            onDismiss = { showFolderPicker = false },
        )
    }
}

@Composable
private fun SaveReviewScreen(
    state: SaveUiState,
    onNameChange: (String) -> Unit,
    onPickFolder: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes

    Scaffold(
        containerColor = colors.bg,
        topBar = {
            SnapdocTopAppBar(
                title = "Save",
                leading = {
                    IconOnlyButton(
                        icon = Icons.Outlined.ArrowBack,
                        contentDescription = "Cancel",
                        onClick = onCancel,
                    )
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(padding)
                .padding(horizontal = 24.dp),
        ) {
            Spacer(Modifier.height(8.dp))
            Text("File name", style = SnapdocText.labelMd, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))
            SnapdocTextField(
                value = state.suggestedName,
                onValueChange = onNameChange,
                label = "File name",
                isError = state.suggestedName.isBlank(),
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
            Text("Category", style = SnapdocText.labelMd, color = colors.textSecondary)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shapes.md)
                    .background(colors.surfaceVariant)
                    .clickable(onClick = onPickFolder)
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    Icons.Outlined.Folder,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(20.dp),
                )
                if (state.detecting && state.category.isBlank()) {
                    Text(
                        "Finding the best category…",
                        style = SnapdocText.bodyLg,
                        color = colors.textSecondary,
                        modifier = Modifier.weight(1f),
                    )
                    SnapdocSpinner(accent = true)
                } else {
                    Text(
                        state.category.ifBlank { "Other" },
                        style = SnapdocText.bodyLg,
                        color = colors.textPrimary,
                        modifier = Modifier.weight(1f),
                    )
                    if (state.categoryWasAuto) {
                        Text("Auto", style = SnapdocText.labelSm, color = colors.accent)
                    }
                    Text("Change", style = SnapdocText.labelMd, color = colors.accent)
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = colors.textTertiary,
                    modifier = Modifier.size(14.dp),
                )
                Text(
                    "Your scan stays on your phone.",
                    style = SnapdocText.caption,
                    color = colors.textTertiary,
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(12.dp))
                Text(state.error, style = SnapdocText.bodyMd, color = colors.error)
            }

            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GhostButton(text = "Cancel", onClick = onCancel)
                PrimaryButton(
                    text = "Save",
                    onClick = onSave,
                    enabled = state.suggestedName.isNotBlank(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CenteredStatus(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}
