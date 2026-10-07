package edu.feutech.redu.ui

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * The slice of the blurred halftone that a frosted panel should draw.
 * Coordinates match the full-backdrop blit: bitmap pixels map linearly onto
 * the backdrop, and [dx]/[dy] is the panel origin in backdrop space.
 */
internal class FrostWindow(
    val srcOffsetX: Int,
    val srcOffsetY: Int,
    val srcWidth: Int,
    val srcHeight: Int,
    val dstOffsetX: Int,
    val dstOffsetY: Int,
    val dstWidth: Int,
    val dstHeight: Int,
)

internal fun frostWindow(
    dx: Float,
    dy: Float,
    panelWidth: Float,
    panelHeight: Float,
    backdropWidth: Int,
    backdropHeight: Int,
    bitmapWidth: Int,
    bitmapHeight: Int,
): FrostWindow? {
    if (backdropWidth < 2 || backdropHeight < 2 || bitmapWidth < 2 || bitmapHeight < 2) return null
    if (panelWidth < 1f || panelHeight < 1f) return null
    val visLeft = max(0f, -dx)
    val visTop = max(0f, -dy)
    val visRight = min(panelWidth, backdropWidth - dx)
    val visBottom = min(panelHeight, backdropHeight - dy)
    if (visRight - visLeft < 1f || visBottom - visTop < 1f) return null

    val scaleX = bitmapWidth.toFloat() / backdropWidth.toFloat()
    val scaleY = bitmapHeight.toFloat() / backdropHeight.toFloat()
    val srcLeft = (visLeft + dx) * scaleX
    val srcTop = (visTop + dy) * scaleY
    val srcRight = (visRight + dx) * scaleX
    val srcBottom = (visBottom + dy) * scaleY
    val left = srcLeft.roundToInt().coerceIn(0, bitmapWidth - 1)
    val top = srcTop.roundToInt().coerceIn(0, bitmapHeight - 1)
    val right = srcRight.roundToInt().coerceIn(left + 1, bitmapWidth)
    val bottom = srcBottom.roundToInt().coerceIn(top + 1, bitmapHeight)
    val dstLeft = visLeft.roundToInt()
    val dstTop = visTop.roundToInt()
    val dstRight = visRight.roundToInt().coerceAtLeast(dstLeft + 1)
    val dstBottom = visBottom.roundToInt().coerceAtLeast(dstTop + 1)
    return FrostWindow(
        srcOffsetX = left,
        srcOffsetY = top,
        srcWidth = right - left,
        srcHeight = bottom - top,
        dstOffsetX = dstLeft,
        dstOffsetY = dstTop,
        dstWidth = dstRight - dstLeft,
        dstHeight = dstBottom - dstTop,
    )
}
