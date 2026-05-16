package com.snapdoc.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme

/** Top app bar. Spec §8.4.2. */
@Composable
fun SnapdocTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    actions: (@Composable () -> Unit)? = null,
    showBottomDivider: Boolean = false,
) {
    val colors = SnapdocTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.bg)) {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(start = 16.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(8.dp))
            }
            Text(
                title,
                style = SnapdocText.titleXl,
                color = colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            if (actions != null) actions()
        }
        if (showBottomDivider) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider),
            )
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
)

/** Bottom nav. Spec §8.4.1 — three destinations: Home, Categories, Settings. */
@Composable
fun SnapdocBottomNav(
    items: List<BottomNavItem>,
    selectedRoute: String,
    onSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SnapdocTheme.colors
    Column(modifier = modifier.fillMaxWidth().background(colors.surface)) {
        if (!colors.isDark) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.divider),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                val active = item.route == selectedRoute
                val color = if (active) colors.textPrimary else colors.textSecondary
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(role = Role.Tab) { onSelect(item) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 32.dp, height = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (active) {
                            Box(
                                modifier = Modifier
                                    .size(width = 32.dp, height = 24.dp)
                                    .clip(SnapdocTheme.shapes.full)
                                    .background(colors.surfaceVariant),
                            )
                        }
                        Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp), tint = color)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(item.label, style = SnapdocText.labelMd, color = color)
                }
            }
        }
        Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}
