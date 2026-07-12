package edu.feutech.redu.ui.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import edu.feutech.redu.R

internal fun Context.reduDp(value: Int): Int =
    (value * resources.displayMetrics.density).toInt()

internal fun Context.reduTypeface(weight: Int = 400): Typeface {
    val base = ResourcesCompat.getFont(this, R.font.manrope_variable) ?: Typeface.DEFAULT
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Typeface.create(base, weight, false)
    } else {
        Typeface.create(base, if (weight >= 600) Typeface.BOLD else Typeface.NORMAL)
    }
}

internal fun TextView.applyReduTextStyle(
    sizeSp: Float,
    @ColorInt color: Int,
    weight: Int = 400,
    tabular: Boolean = false,
) {
    textSize = sizeSp
    setTextColor(color)
    typeface = context.reduTypeface(weight)
    letterSpacing = 0f
    includeFontPadding = false
    if (tabular) fontFeatureSettings = "tnum"
}

internal fun Context.reduShape(
    @ColorInt fillColor: Int,
    radiusDp: Int = 8,
    @ColorInt strokeColor: Int? = null,
    strokeWidthDp: Int = 1,
): GradientDrawable = GradientDrawable().apply {
    setColor(fillColor)
    cornerRadius = reduDp(radiusDp).toFloat()
    strokeColor?.let { setStroke(reduDp(strokeWidthDp), it) }
}

internal fun Context.reduRippleBackground(
    @ColorInt fillColor: Int,
    @ColorInt rippleColor: Int,
    radiusDp: Int = 8,
    @ColorInt strokeColor: Int? = null,
): RippleDrawable = RippleDrawable(
    ColorStateList.valueOf(rippleColor),
    reduShape(
        fillColor = fillColor,
        radiusDp = radiusDp,
        strokeColor = strokeColor,
    ),
    null,
)
