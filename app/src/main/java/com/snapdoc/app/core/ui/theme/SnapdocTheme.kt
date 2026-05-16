package com.snapdoc.app.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Accessor for Snapdoc design tokens from any composable. */
object SnapdocTheme {
    val colors: SnapdocColors
        @Composable @ReadOnlyComposable get() = LocalSnapdocColors.current
    val spacing: SnapdocSpacing
        @Composable @ReadOnlyComposable get() = LocalSpacing.current
    val shapes: SnapdocShapes
        @Composable @ReadOnlyComposable get() = LocalShapes.current
    val motion: SnapdocMotion
        @Composable @ReadOnlyComposable get() = LocalMotion.current
}


/**
 * Root theme entry point. Wires Snapdoc color tokens, typography, shape,
 * spacing, and motion into Compose's [MaterialTheme] plus our own
 * [CompositionLocal]s for tokens that do not map cleanly to Material.
 *
 * Full design system rationale lives in `02_design_system.md`.
 */
@Composable
fun SnapdocTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkSnapdocColors else LightSnapdocColors
    val material = if (darkTheme) darkMaterialColors() else lightMaterialColors()

    CompositionLocalProvider(
        LocalSnapdocColors provides colors,
        LocalSpacing provides DefaultSpacing,
        LocalShapes provides DefaultShapes,
        LocalMotion provides DefaultMotion,
    ) {
        MaterialTheme(
            colorScheme = material,
            typography = SnapdocTypography,
            shapes = MaterialShapes,
            content = content,
        )
    }
}
