@file:OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)

package edu.feutech.redu.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
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
    primary = ReduPalette.SeaGlass,
    onPrimary = ReduPalette.OnSeaGlass,
    primaryContainer = ReduPalette.SeaGlassContainer,
    onPrimaryContainer = ReduPalette.OnSeaGlassContainer,
    inversePrimary = Color(0xFF286B5B),
    secondary = ReduPalette.SeaGlass,
    onSecondary = ReduPalette.OnSeaGlass,
    secondaryContainer = ReduPalette.SeaGlassContainer,
    onSecondaryContainer = ReduPalette.OnSeaGlassContainer,
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
    surfaceTint = ReduPalette.SeaGlass,
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
)

private val ReduShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(6.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)

@Composable
fun ReduTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ReduDarkColorScheme,
        typography = ReduTypography,
        shapes = ReduShapes,
        content = content,
    )
}
