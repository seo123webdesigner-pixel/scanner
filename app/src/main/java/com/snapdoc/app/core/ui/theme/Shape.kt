package com.snapdoc.app.core.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/** `02_design_system.md` §5.1 — radius scale. */
@Immutable
data class SnapdocShapes(
    val none: RoundedCornerShape = RoundedCornerShape(0.dp),
    val sm: RoundedCornerShape = RoundedCornerShape(4.dp),
    val md: RoundedCornerShape = RoundedCornerShape(8.dp),
    val lg: RoundedCornerShape = RoundedCornerShape(12.dp),
    val xl: RoundedCornerShape = RoundedCornerShape(20.dp),
    val xl2: RoundedCornerShape = RoundedCornerShape(28.dp),
    val full: RoundedCornerShape = RoundedCornerShape(CornerSize(50)),
    /** Bottom sheet: only top corners rounded. */
    val bottomSheet: RoundedCornerShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
)

val DefaultShapes = SnapdocShapes()

val LocalShapes = staticCompositionLocalOf { DefaultShapes }

val MaterialShapes = Shapes(
    extraSmall = DefaultShapes.sm,
    small = DefaultShapes.md,
    medium = DefaultShapes.lg,
    large = DefaultShapes.xl,
    extraLarge = DefaultShapes.xl2,
)
