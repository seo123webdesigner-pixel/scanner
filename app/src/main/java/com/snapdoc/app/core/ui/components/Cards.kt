package com.snapdoc.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme
import com.snapdoc.app.core.ui.theme.tabular

/** Document card. Spec §8.3.1. */
@Composable
fun DocumentCard(
    filename: String,
    metadata: String,
    categoryName: String,
    categoryStripColor: Color,
    categoryChipTextColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
    thumbnail: (@Composable () -> Unit)? = null,
    selected: Boolean = false,
    selectionMode: Boolean = false,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    val borderColor = if (selected) colors.primary else colors.border
    val borderWidth = if (selected) 2.dp else 1.dp
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shapes.lg)
            .background(colors.surface)
            .border(borderWidth, borderColor, shapes.lg)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row {
            // Category strip (2dp).
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(categoryStripColor),
            )
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                // Thumbnail 56x72.
                Box(
                    modifier = Modifier
                        .size(width = 56.dp, height = 72.dp)
                        .clip(shapes.sm)
                        .background(colors.surfaceVariant)
                        .border(1.dp, colors.border, shapes.sm),
                    contentAlignment = Alignment.Center,
                ) {
                    thumbnail?.invoke()
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        filename,
                        style = SnapdocText.titleLg,
                        color = colors.textPrimary,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        metadata,
                        style = SnapdocText.caption.tabular(),
                        color = colors.textTertiary,
                    )
                    Spacer(Modifier.height(8.dp))
                    CategoryChip(label = categoryName, textColor = categoryChipTextColor)
                }
            }
        }
        if (selectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.primary else colors.surface)
                    .border(2.dp, if (selected) colors.primary else colors.border, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        tint = colors.onPrimary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }
        }
    }
}

/** Category card for the 2-column Categories View. Spec §8.3.2. */
@Composable
fun CategoryCard(
    label: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    Column(
        modifier = modifier
            .clip(shapes.lg)
            .background(colors.surface)
            .border(1.dp, colors.border, shapes.lg)
            .clickable(onClick = onClick)
            .padding(16.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = accentColor)
        Spacer(Modifier.height(12.dp))
        Text(label, style = SnapdocText.titleLg, color = colors.textPrimary)
        Text(
            text = "$count documents",
            style = SnapdocText.caption.tabular(),
            color = colors.textTertiary,
        )
    }
}

/** Settings row. Spec §8.3.3. */
@Composable
fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val colors = SnapdocTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = colors.textSecondary)
            Spacer(Modifier.width(12.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = SnapdocText.bodyLg, color = colors.textPrimary)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, style = SnapdocText.bodyMd, color = colors.textSecondary)
            }
        }
        trailing?.invoke()
    }
}
