package edu.feutech.redu.ui.theme

import androidx.compose.ui.graphics.Color

object ReduPalette {
    val Background = Color(0xFF0B0E0D)
    val SurfaceLowest = Color(0xFF0E1210)
    val SurfaceLow = Color(0xFF101412)
    val Surface = Color(0xFF121715)
    val SurfaceHigh = Color(0xFF1A211E)
    val SurfaceHighest = Color(0xFF222B27)

    val SeaGlass = Color(0xFF76CDB8)
    val OnSeaGlass = Color(0xFF08231C)
    val SeaGlassContainer = Color(0xFF143B32)
    val OnSeaGlassContainer = Color(0xFFB5EBDC)

    val Warning = Color(0xFFE3B76F)
    val OnWarning = Color(0xFF2B1D05)
    val WarningContainer = Color(0xFF3D2F17)
    val OnWarningContainer = Color(0xFFFFDEA3)

    val High = Color(0xFFE5968C)
    val OnHigh = Color(0xFF2B0B08)
    val HighContainer = Color(0xFF42211E)
    val OnHighContainer = Color(0xFFFFDAD5)

    val TextPrimary = Color(0xFFEEF3F0)
    val TextSecondary = Color(0xFFA7B2AC)
    val Outline = Color(0xFF68766F)
    val OutlineVariant = Color(0xFF34423C)

    val Error = High
    val ErrorContainer = HighContainer
    val OnError = OnHigh
    val OnErrorContainer = OnHighContainer
}

object ReduStatusPalette {
    val Normal = ReduPalette.SeaGlass
    val NormalContainer = ReduPalette.SeaGlassContainer
    val OnNormalContainer = ReduPalette.OnSeaGlassContainer

    val Elevated = ReduPalette.Warning
    val ElevatedContainer = ReduPalette.WarningContainer
    val OnElevatedContainer = ReduPalette.OnWarningContainer

    val Extended = ReduPalette.High
    val ExtendedContainer = ReduPalette.HighContainer
    val OnExtendedContainer = ReduPalette.OnHighContainer

    val Attention = ReduPalette.Warning
    val AttentionContainer = ReduPalette.WarningContainer
    val OnAttentionContainer = ReduPalette.OnWarningContainer
}
