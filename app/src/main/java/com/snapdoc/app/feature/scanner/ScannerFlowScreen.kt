package com.snapdoc.app.feature.scanner

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.snapdoc.app.core.ui.components.GhostButton
import com.snapdoc.app.core.ui.components.SecondaryButton
import com.snapdoc.app.core.ui.components.SnapdocSpinner
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme

/**
 * Hosts the entire scan → review → save flow.
 *
 * ML Kit's Document Scanner provides its own crop/multi-page/filter UI,
 * so screens 9–12 from the spec are surfaced through the system overlay
 * rather than recreated in-app. We pick up after the scanner closes and
 * show the Save screen (spec 13).
 */
@Composable
fun ScannerFlowScreen(
    onSaved: (Long) -> Unit,
    onCancel: () -> Unit,
    viewModel: SaveDocumentViewModel = hiltViewModel(),
) {
    var launched by remember { mutableStateOf(false) }
    var triggered by remember { mutableStateOf(false) }
    val state by viewModel.state.collectAsState()
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

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                state.processing -> {
                    SnapdocSpinner()
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Saving your document…",
                        style = SnapdocText.bodyLg,
                        color = colors.textSecondary,
                    )
                }
                state.error != null -> {
                    Text(state.error.orEmpty(), style = SnapdocText.bodyLg, color = colors.error)
                    Spacer(Modifier.height(16.dp))
                    SecondaryButton(text = "Go back", onClick = onCancel)
                }
                else -> {
                    Text(
                        "Opening scanner…",
                        style = SnapdocText.bodyLg,
                        color = colors.textSecondary,
                    )
                    Spacer(Modifier.height(16.dp))
                    GhostButton(text = "Cancel", onClick = onCancel)
                }
            }
        }
    }
}
