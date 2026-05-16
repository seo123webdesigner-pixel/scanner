package com.snapdoc.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp

/**
 * Type scale per `02_design_system.md` §3.
 *
 * Geist + Geist Mono are the brand fonts. To enable them:
 *   1. Drop `Geist-Regular.ttf`, `Geist-Medium.ttf`, `Geist-SemiBold.ttf`,
 *      `GeistMono-Regular.ttf`, `GeistMono-Medium.ttf` into `app/src/main/res/font/`.
 *   2. Replace [SnapdocFontFamily] / [SnapdocMonoFamily] below with the
 *      `FontFamily(Font(R.font.geist_regular, FontWeight.Normal), …)` form.
 *
 * Until then we use the system sans-serif; metrics are close enough that
 * layout does not need to change when fonts are swapped in.
 */
val SnapdocFontFamily: FontFamily = FontFamily.SansSerif
val SnapdocMonoFamily: FontFamily = FontFamily.Monospace

private val sans = SnapdocFontFamily
private val mono = SnapdocMonoFamily

object SnapdocText {
    val display = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp, lineHeight = 48.sp, letterSpacing = (-0.02).em,
    )
    val headlineXl = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = (-0.02).em,
    )
    val headlineLg = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = (-0.015).em,
    )
    val headlineMd = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 28.sp, letterSpacing = (-0.01).em,
    )
    val titleXl = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium,
        fontSize = 18.sp, lineHeight = 26.sp, letterSpacing = (-0.01).em,
    )
    val titleLg = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = (-0.005).em,
    )
    val titleMd = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val bodyLg = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp,
    )
    val bodyMd = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp,
    )
    val bodySm = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, lineHeight = 18.sp,
    )
    val labelLg = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.005.em,
    )
    val labelMd = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.01.em,
    )
    val labelSm = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.02.em,
    )
    val caption = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp,
    )
    val overline = TextStyle(
        fontFamily = sans, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.08.em,
    )
}

/** Returns a tabular-numbers variant. Used for sizes, counts, prices. */
fun TextStyle.tabular(): TextStyle = copy(fontFamily = mono)

val SnapdocTypography = Typography(
    displayLarge = SnapdocText.display,
    displayMedium = SnapdocText.headlineXl,
    displaySmall = SnapdocText.headlineLg,
    headlineLarge = SnapdocText.headlineLg,
    headlineMedium = SnapdocText.headlineMd,
    headlineSmall = SnapdocText.titleXl,
    titleLarge = SnapdocText.titleXl,
    titleMedium = SnapdocText.titleLg,
    titleSmall = SnapdocText.titleMd,
    bodyLarge = SnapdocText.bodyLg,
    bodyMedium = SnapdocText.bodyMd,
    bodySmall = SnapdocText.bodySm,
    labelLarge = SnapdocText.labelLg,
    labelMedium = SnapdocText.labelMd,
    labelSmall = SnapdocText.labelSm,
)
