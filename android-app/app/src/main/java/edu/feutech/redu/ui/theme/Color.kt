package edu.feutech.redu.ui.theme

import androidx.compose.ui.graphics.Color

object ReduPalette {
    val Background = Color(0xFF0E0C0B)
    val SurfaceLowest = Color(0xFF141210)
    val SurfaceLow = Color(0xFF1C1916)
    val Surface = Color(0xFF24201C)
    val SurfaceHigh = Color(0xFF2C2824)
    val SurfaceHighest = Color(0xFF36312C)

    // Center of the home-card sky, between kHorizon #6483C6 and kZenith #6180C3.
    val Action = Color(0xFF6382C5)
    val OnAction = Color(0xFF0E1624)
    val ActionContainer = Color(0x336382C5)
    val OnActionContainer = Color(0xFFF4EDE4)

    val Persimmon = Action

    val Sage = Color(0xFF8FB59A)
    val SageContainer = Color(0xFF1E3328)
    val OnSageContainer = Color(0xFFD5EDE0)

    val Figure = Color(0xFFF4EDE4)
    val FigureInk = Color(0xFF2A2420)

    val Warning = Color(0xFFE3B76F)
    val OnWarning = Color(0xFF2B1D05)
    val WarningContainer = Color(0xFF3D2F17)
    val OnWarningContainer = Color(0xFFFFDEA3)

    val High = Color(0xFFE5968C)
    val OnHigh = Color(0xFF2B0B08)
    val HighContainer = Color(0xFF42211E)
    val OnHighContainer = Color(0xFFFFDAD5)

    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFC4B5A8)
    val Outline = Color(0xFF8A7E74)
    val OutlineVariant = Color(0xFF3A342F)

    val Error = High
    val ErrorContainer = HighContainer
    val OnError = OnHigh
    val OnErrorContainer = OnHighContainer
}

object ReduStatusPalette {
    val Normal = ReduPalette.Sage
    val NormalContainer = ReduPalette.SageContainer
    val OnNormalContainer = ReduPalette.OnSageContainer

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
