package com.snapdoc.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme

/**
 * Snapdoc button primitives. See `02_design_system.md` §8.1.
 *
 * Material 3's [Button] family is avoided because its container tokens
 * drift from ours; these primitives bind directly to Snapdoc tokens.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    val spacing = SnapdocTheme.spacing
    val bg = if (!enabled) colors.surfaceVariant else colors.primary
    val fg = if (enabled) colors.onPrimary else colors.textDisabled
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .heightIn(min = 48.dp)
            .clip(shapes.md)
            .background(bg)
            .clickable(
                enabled = enabled && !loading,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = spacing.s4),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = fg,
                    strokeWidth = 1.5.dp,
                )
                Box(Modifier.size(8.dp))
            } else if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(16.dp), tint = fg)
                Box(Modifier.size(8.dp))
            }
            Text(text, style = SnapdocText.labelLg, color = fg)
        }
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    val spacing = SnapdocTheme.spacing
    val bg = if (enabled) colors.surface else colors.surfaceVariant
    val borderColor = if (enabled) colors.border else colors.divider
    val fg = if (enabled) colors.textPrimary else colors.textDisabled
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .heightIn(min = 48.dp)
            .clip(shapes.md)
            .background(bg)
            .border(1.dp, borderColor, shapes.md)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = spacing.s4),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(16.dp), tint = fg)
                Box(Modifier.size(8.dp))
            }
            Text(text, style = SnapdocText.labelLg, color = fg)
        }
    }
}

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    val fg = if (enabled) colors.textPrimary else colors.textDisabled
    Box(
        modifier = modifier
            .heightIn(min = 36.dp)
            .clip(shapes.md)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = SnapdocText.labelLg, color = fg)
    }
}

@Composable
fun DestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    val spacing = SnapdocTheme.spacing
    val dark = colors.isDark
    val bg = if (dark) colors.surface else colors.error
    val fg = if (dark) colors.error else Color.White
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .heightIn(min = 48.dp)
            .clip(shapes.md)
            .background(bg)
            .border(if (dark) 1.dp else 0.dp, colors.error, shapes.md)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = spacing.s4),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = SnapdocText.labelLg, color = fg)
    }
}

@Composable
fun IconOnlyButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color? = null,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    val resolvedTint = tint ?: if (enabled) colors.textPrimary else colors.textDisabled
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(shapes.full)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(24.dp), tint = resolvedTint)
    }
}
