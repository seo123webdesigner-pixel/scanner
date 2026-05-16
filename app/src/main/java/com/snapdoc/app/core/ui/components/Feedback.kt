package com.snapdoc.app.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.snapdoc.app.core.ui.theme.SnapdocTheme

@Composable
fun SnapdocSpinner(modifier: Modifier = Modifier, accent: Boolean = false) {
    val colors = SnapdocTheme.colors
    CircularProgressIndicator(
        modifier = modifier.size(24.dp),
        color = if (accent) colors.accent else colors.textPrimary,
        strokeWidth = 1.5.dp,
    )
}

@Composable
fun SkeletonBox(modifier: Modifier = Modifier) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    val transition = rememberInfiniteTransition(label = "skeleton")
    val offset by transition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "skeleton-offset",
    )
    val brush = Brush.horizontalGradient(
        0f to colors.surfaceVariant,
        0.5f to colors.surface,
        1f to colors.surfaceVariant,
        startX = offset,
        endX = offset + 300f,
    )
    Box(
        modifier = modifier
            .clip(shapes.md)
            .background(brush),
    )
}

@Composable
fun SnapdocLinearProgress(progress: Float, modifier: Modifier = Modifier) {
    val colors = SnapdocTheme.colors
    val shapes = SnapdocTheme.shapes
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(shapes.full)
            .background(colors.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(shapes.full)
                .background(colors.primary),
        )
    }
}
