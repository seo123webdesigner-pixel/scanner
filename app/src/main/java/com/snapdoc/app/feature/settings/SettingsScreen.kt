package com.snapdoc.app.feature.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.snapdoc.app.BuildConfig
import com.snapdoc.app.core.storage.UserPreferences
import com.snapdoc.app.core.ui.components.SettingsRow
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
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferences,
) : ViewModel() {
    val autoOcr: StateFlow<Boolean> =
        prefs.autoOcr.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val autoSuggest: StateFlow<Boolean> =
        prefs.autoAiSuggestion.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)
    val removeAdsPurchased: StateFlow<Boolean> =
        prefs.removeAdsPurchased.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun toggleAutoOcr(value: Boolean) { viewModelScope.launch { prefs.setAutoOcr(value) } }
    fun toggleAutoSuggest(value: Boolean) { viewModelScope.launch { prefs.setAutoAiSuggestion(value) } }
}

@Composable
fun SettingsScreen(
    onManageCategories: () -> Unit,
    onPaywall: () -> Unit,
    onAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val colors = SnapdocTheme.colors
    val context = LocalContext.current
    val autoOcr by viewModel.autoOcr.collectAsState()
    val autoSuggest by viewModel.autoSuggest.collectAsState()
    val removeAds by viewModel.removeAdsPurchased.collectAsState()

    Scaffold(
        containerColor = colors.bg,
        topBar = { SnapdocTopAppBar(title = "Settings") },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            SettingsRow(
                title = "Auto-OCR after scan",
                subtitle = if (autoOcr) "On — extracts text from every scan" else "Off",
                icon = Icons.Outlined.TextFields,
                onClick = { viewModel.toggleAutoOcr(!autoOcr) },
                trailing = { Switch(checked = autoOcr, onCheckedChange = viewModel::toggleAutoOcr) },
            )
            SettingsRow(
                title = "Suggest AI summary",
                subtitle = if (autoSuggest) "On" else "Off — open AI Summary manually",
                icon = Icons.Outlined.AutoAwesome,
                onClick = { viewModel.toggleAutoSuggest(!autoSuggest) },
                trailing = { Switch(checked = autoSuggest, onCheckedChange = viewModel::toggleAutoSuggest) },
            )
            SettingsRow(
                title = "Manage categories",
                icon = Icons.Outlined.GridView,
                onClick = onManageCategories,
            )
            SettingsRow(
                title = if (removeAds) "Ads removed — thank you" else "Remove ads",
                subtitle = if (removeAds) null else "₹99 / \$1.49 once — unlocks unlimited AI summaries",
                icon = Icons.Outlined.Lock,
                onClick = onPaywall,
            )
            SettingsRow(
                title = "Rate SnapDoc",
                subtitle = "Enjoying it? A quick review really helps.",
                icon = Icons.Outlined.StarBorder,
                onClick = { openPlayStoreListing(context) },
            )
            SettingsRow(
                title = "Share SnapDoc",
                icon = Icons.Outlined.Share,
                onClick = { shareApp(context) },
            )
            SettingsRow(
                title = "About SnapDoc",
                icon = Icons.Outlined.Info,
                onClick = onAbout,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "SnapDoc v${BuildConfig.VERSION_NAME}",
                style = SnapdocText.caption,
                color = colors.textTertiary,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** Opens the app's Play Store listing, falling back to the web URL if Play isn't installed. */
private fun openPlayStoreListing(context: Context) {
    val pkg = context.packageName
    val market = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
    try {
        context.startActivity(market)
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")),
        )
    }
}

/** Shares a link to the app via the system share sheet. */
private fun shareApp(context: Context) {
    val pkg = context.packageName
    val message =
        "Scan documents privately with SnapDoc — your scans stay on your phone. " +
            "https://play.google.com/store/apps/details?id=$pkg"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Share SnapDoc"))
}
