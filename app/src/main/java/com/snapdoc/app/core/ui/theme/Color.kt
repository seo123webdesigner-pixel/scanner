package com.snapdoc.app.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Full Snapdoc color token set, sourced 1:1 from `02_design_system.md` §2.
 *
 * Material 3's [ColorScheme] is too narrow for this product — categories,
 * sunken surfaces, tabular accents, etc. live alongside it via
 * [LocalSnapdocColors]. Never use raw hex outside this file.
 */
@Immutable
data class SnapdocColors(
    val bg: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val surfaceSunken: Color,
    val primary: Color,
    val primaryContainer: Color,
    val onPrimary: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val accent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textDisabled: Color,
    val border: Color,
    val divider: Color,
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color,
    val scrim: Color,
    val categories: CategoryColors,
    val isDark: Boolean,
)

@Immutable
data class CategoryColors(
    val billsStrip: Color,
    val billsChipText: Color,
    val idsStrip: Color,
    val idsChipText: Color,
    val receiptsStrip: Color,
    val receiptsChipText: Color,
    val notesStrip: Color,
    val notesChipText: Color,
    val contractsStrip: Color,
    val contractsChipText: Color,
    val otherStrip: Color,
    val otherChipText: Color,
)

val LightSnapdocColors = SnapdocColors(
    bg = Color(0xFFFAFAF8),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF2F1ED),
    surfaceSunken = Color(0xFFF5F4F0),
    primary = Color(0xFF111111),
    primaryContainer = Color(0xFF1F1F1F),
    onPrimary = Color(0xFFFFFFFF),
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF5A5A5A),
    accent = Color(0xFFB45309),
    textPrimary = Color(0xFF111111),
    textSecondary = Color(0xFF5A5A5A),
    textTertiary = Color(0xFF8B8A85),
    textDisabled = Color(0xFFA3A29E),
    border = Color(0xFFE5E4DF),
    divider = Color(0xFFEDECE7),
    success = Color(0xFF15803D),
    warning = Color(0xFFB45309),
    error = Color(0xFFB42318),
    info = Color(0xFF1F6FEB),
    scrim = Color(0x73111111), // 0.45 alpha
    categories = CategoryColors(
        billsStrip = Color(0xFFB45309),
        billsChipText = Color(0xFF92400E),
        idsStrip = Color(0xFF1F6FEB),
        idsChipText = Color(0xFF1E40AF),
        receiptsStrip = Color(0xFF15803D),
        receiptsChipText = Color(0xFF166534),
        notesStrip = Color(0xFF6D28D9),
        notesChipText = Color(0xFF5B21B6),
        contractsStrip = Color(0xFFB42318),
        contractsChipText = Color(0xFF991B1B),
        otherStrip = Color(0xFF57534E),
        otherChipText = Color(0xFF44403C),
    ),
    isDark = false,
)

val DarkSnapdocColors = SnapdocColors(
    bg = Color(0xFF0C0C0D),
    surface = Color(0xFF141416),
    surfaceVariant = Color(0xFF1C1C1F),
    surfaceSunken = Color(0xFF08080A),
    primary = Color(0xFFF4F4F1),
    primaryContainer = Color(0xFFE0DFDA),
    onPrimary = Color(0xFF111111),
    onPrimaryContainer = Color(0xFF111111),
    secondary = Color(0xFFA3A29E),
    accent = Color(0xFFD97706),
    textPrimary = Color(0xFFF4F4F1),
    textSecondary = Color(0xFF9A9A95),
    textTertiary = Color(0xFF6E6E69),
    textDisabled = Color(0xFF56565A),
    border = Color(0xFF26262A),
    divider = Color(0xFF1F1F22),
    success = Color(0xFF4ADE80),
    warning = Color(0xFFFBBF24),
    error = Color(0xFFF87171),
    info = Color(0xFF60A5FA),
    scrim = Color(0x99000000), // 0.6 alpha
    categories = CategoryColors(
        billsStrip = Color(0xFFD97706),
        billsChipText = Color(0xFFFBBF24),
        idsStrip = Color(0xFF60A5FA),
        idsChipText = Color(0xFF93C5FD),
        receiptsStrip = Color(0xFF4ADE80),
        receiptsChipText = Color(0xFF86EFAC),
        notesStrip = Color(0xFFA78BFA),
        notesChipText = Color(0xFFC4B5FD),
        contractsStrip = Color(0xFFF87171),
        contractsChipText = Color(0xFFFCA5A5),
        otherStrip = Color(0xFFA8A29E),
        otherChipText = Color(0xFFD6D3D1),
    ),
    isDark = true,
)

val LocalSnapdocColors = staticCompositionLocalOf { LightSnapdocColors }

/**
 * Bridge to Material 3 [ColorScheme] so stock M3 components inherit our
 * tokens. Always prefer reading from [LocalSnapdocColors] for app code.
 */
internal fun lightMaterialColors(): ColorScheme = lightColorScheme(
    primary = LightSnapdocColors.primary,
    onPrimary = LightSnapdocColors.onPrimary,
    primaryContainer = LightSnapdocColors.primaryContainer,
    onPrimaryContainer = LightSnapdocColors.onPrimaryContainer,
    secondary = LightSnapdocColors.secondary,
    onSecondary = LightSnapdocColors.onPrimary,
    background = LightSnapdocColors.bg,
    onBackground = LightSnapdocColors.textPrimary,
    surface = LightSnapdocColors.surface,
    onSurface = LightSnapdocColors.textPrimary,
    surfaceVariant = LightSnapdocColors.surfaceVariant,
    onSurfaceVariant = LightSnapdocColors.textSecondary,
    error = LightSnapdocColors.error,
    onError = Color.White,
    outline = LightSnapdocColors.border,
    outlineVariant = LightSnapdocColors.divider,
    scrim = LightSnapdocColors.scrim,
)

internal fun darkMaterialColors(): ColorScheme = darkColorScheme(
    primary = DarkSnapdocColors.primary,
    onPrimary = DarkSnapdocColors.onPrimary,
    primaryContainer = DarkSnapdocColors.primaryContainer,
    onPrimaryContainer = DarkSnapdocColors.onPrimaryContainer,
    secondary = DarkSnapdocColors.secondary,
    onSecondary = DarkSnapdocColors.onPrimary,
    background = DarkSnapdocColors.bg,
    onBackground = DarkSnapdocColors.textPrimary,
    surface = DarkSnapdocColors.surface,
    onSurface = DarkSnapdocColors.textPrimary,
    surfaceVariant = DarkSnapdocColors.surfaceVariant,
    onSurfaceVariant = DarkSnapdocColors.textSecondary,
    error = DarkSnapdocColors.error,
    onError = Color.Black,
    outline = DarkSnapdocColors.border,
    outlineVariant = DarkSnapdocColors.divider,
    scrim = DarkSnapdocColors.scrim,
)
