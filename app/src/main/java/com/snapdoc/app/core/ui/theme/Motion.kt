package com.snapdoc.app.core.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

/** `02_design_system.md` §6. */
@Immutable
data class SnapdocMotion(
    val durInstant: Int = 80,
    val durFast: Int = 160,
    val durDefault: Int = 240,
    val durSlow: Int = 320,
    val durExtraSlow: Int = 480,
    val standard: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f),
    val emphasized: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f),
    val decelerate: Easing = CubicBezierEasing(0f, 0f, 0f, 1f),
    val accelerate: Easing = CubicBezierEasing(0.3f, 0f, 1f, 1f),
    val linear: Easing = LinearEasing,
)

val DefaultMotion = SnapdocMotion()

val LocalMotion = staticCompositionLocalOf { DefaultMotion }
