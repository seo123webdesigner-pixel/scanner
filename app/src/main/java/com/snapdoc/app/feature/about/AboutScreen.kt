package com.snapdoc.app.feature.about

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.snapdoc.app.BuildConfig
import com.snapdoc.app.core.ui.components.IconOnlyButton
import com.snapdoc.app.core.ui.components.SnapdocTopAppBar
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val colors = SnapdocTheme.colors
    Scaffold(
        containerColor = colors.bg,
        topBar = {
            SnapdocTopAppBar(
                title = "About",
                leading = {
                    IconOnlyButton(icon = Icons.Outlined.ArrowBack, contentDescription = "Back", onClick = onBack)
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bg)
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
        ) {
            Spacer(Modifier.height(24.dp))
            Text("snapdoc", style = SnapdocText.headlineXl, color = colors.textPrimary)
            Text("v${BuildConfig.VERSION_NAME}", style = SnapdocText.caption, color = colors.textTertiary)
            Spacer(Modifier.height(32.dp))
            Text(
                "We don't have a server. Your scans never leave your phone unless you ask AI to summarize them — and even then, only the typed-out text is sent, never the image.",
                style = SnapdocText.bodyLg,
                color = colors.textPrimary,
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Snapdoc is built privacy-first. There are no accounts, no required signups, and no analytics tied to your identity. Anonymous crash reports help us fix bugs.",
                style = SnapdocText.bodyLg,
                color = colors.textSecondary,
            )
        }
    }
}
