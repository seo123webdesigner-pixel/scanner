package com.snapdoc.app.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.snapdoc.app.core.ui.theme.SnapdocText
import com.snapdoc.app.core.ui.theme.SnapdocTheme

/** Category chip. Spec §8.3.1 / §2.4 — chip bg neutral, color carried by text. */
@Composable
fun CategoryChip(
    label: String,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    Text(
        text = label,
        style = SnapdocText.labelSm,
        color = textColor,
        modifier = modifier
            .clip(shapes.sm)
            .background(colors.surfaceVariant)
            .border(1.dp, textColor.copy(alpha = 0.06f), shapes.sm)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}
