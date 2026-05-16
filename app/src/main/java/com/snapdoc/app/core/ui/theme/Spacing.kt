package com.snapdoc.app.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** `02_design_system.md` §4 — strict 4dp grid. */
@Immutable
data class SnapdocSpacing(
    val s0: Dp = 0.dp,
    val s1: Dp = 4.dp,
    val s2: Dp = 8.dp,
    val s3: Dp = 12.dp,
    val s4: Dp = 16.dp,
    val s5: Dp = 24.dp,
    val s6: Dp = 32.dp,
    val s7: Dp = 48.dp,
    val s8: Dp = 64.dp,
    /** Default horizontal page padding. */
    val pageHorizontal: Dp = 16.dp,
    /** Default vertical padding between top bar and first row. */
    val pageVerticalTop: Dp = 16.dp,
)

val DefaultSpacing = SnapdocSpacing()

val LocalSpacing = staticCompositionLocalOf { DefaultSpacing }
