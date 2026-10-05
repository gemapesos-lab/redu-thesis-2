@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package edu.feutech.redu.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import edu.feutech.redu.R

val ManropeFontFamily = FontFamily(
    Font(
        R.font.manrope_regular,
        weight = FontWeight.Normal,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    ),
    Font(
        R.font.manrope_medium,
        weight = FontWeight.Medium,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    ),
    Font(
        R.font.manrope_semibold,
        weight = FontWeight.SemiBold,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    ),
    Font(
        R.font.manrope_bold,
        weight = FontWeight.Bold,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    ),
)

private val ReduDarkColorScheme = darkColorScheme(
    primary = ReduPalette.Action,
    onPrimary = ReduPalette.OnAction,
    primaryContainer = ReduPalette.ActionContainer,
    onPrimaryContainer = ReduPalette.OnActionContainer,
    inversePrimary = Color(0xFFBDBDBD),
    secondary = ReduPalette.Figure,
    onSecondary = ReduPalette.FigureInk,
    secondaryContainer = ReduPalette.Figure.copy(alpha = 0.16f),
    onSecondaryContainer = ReduPalette.Figure,
    tertiary = ReduPalette.Warning,
    onTertiary = ReduPalette.OnWarning,
    tertiaryContainer = ReduPalette.WarningContainer,
    onTertiaryContainer = ReduPalette.OnWarningContainer,
    background = ReduPalette.Background,
    onBackground = ReduPalette.TextPrimary,
    surface = ReduPalette.Background,
    onSurface = ReduPalette.TextPrimary,
    surfaceVariant = ReduPalette.SurfaceHigh,
    onSurfaceVariant = ReduPalette.TextSecondary,
    surfaceTint = Color.Transparent,
    inverseSurface = ReduPalette.TextPrimary,
    inverseOnSurface = ReduPalette.Background,
    error = ReduPalette.Error,
    onError = ReduPalette.OnError,
    errorContainer = ReduPalette.ErrorContainer,
    onErrorContainer = ReduPalette.OnErrorContainer,
    outline = ReduPalette.Outline,
    outlineVariant = ReduPalette.OutlineVariant,
    scrim = Color.Black,
    surfaceBright = ReduPalette.SurfaceHighest,
    surfaceDim = ReduPalette.Background,
    surfaceContainerLowest = ReduPalette.SurfaceLowest,
    surfaceContainerLow = ReduPalette.SurfaceLow,
    surfaceContainer = ReduPalette.Surface,
    surfaceContainerHigh = ReduPalette.SurfaceHigh,
    surfaceContainerHighest = ReduPalette.SurfaceHighest,
)

private val ReduTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = "tnum",
    ),
    headlineSmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = ManropeFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp,
    ),
)

/** In-page surfaces: sections, banners, and the today card. */
internal val ReduPageRadius = 20.dp

/** Dialogs and sheets. Rounder than [ReduPageRadius] by a clear step. */
internal val ReduOverlayRadius = 28.dp

/**
 * Inner corner of a surface inset by [padding] inside a corner of [outer].
 * Concentric corners share a center, so the inner radius is the outer radius minus the gap.
 */
internal fun nestedCornerRadius(outer: Dp, padding: Dp): Dp =
    (outer - padding).coerceAtLeast(0.dp)

private val ReduShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(ReduPageRadius),
    large = RoundedCornerShape(ReduPageRadius),
    extraLarge = RoundedCornerShape(ReduOverlayRadius),
)

internal val ReduPill = RoundedCornerShape(percent = 50)

/** Lucide icons beside text, inside buttons, and in banners. */
internal val ReduInlineIconSize = 20.dp

/** Lucide icons in the navigation pill. The artwork is the same 24px stroke. */
internal val ReduNavIconSize = 24.dp

internal val ReduButtonMinHeight = 52.dp

@Composable
fun ReduTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReduDarkColorScheme,
        typography = ReduTypography,
        shapes = ReduShapes,
    ) {
        CompositionLocalProvider(LocalRippleConfiguration provides null, content = content)
    }
}
