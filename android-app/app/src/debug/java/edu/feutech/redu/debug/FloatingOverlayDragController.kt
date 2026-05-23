package edu.feutech.redu.debug

import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import kotlin.math.abs
import kotlin.math.roundToInt

data class OverlayBounds(
    val displayWidth: Int,
    val displayHeight: Int,
    val minVisibleWidth: Int,
    val minVisibleHeight: Int,
)

object FloatingOverlayPositioning {
    fun clampX(value: Int, width: Int, bounds: OverlayBounds): Int {
        val overlayWidth = width.coerceAtLeast(bounds.minVisibleWidth)
        val min = -overlayWidth + bounds.minVisibleWidth
        val max = bounds.displayWidth - bounds.minVisibleWidth
        return value.coerceIn(min.coerceAtMost(max), max.coerceAtLeast(min))
    }

    fun clampY(value: Int, height: Int, bounds: OverlayBounds): Int {
        val max = bounds.displayHeight - bounds.minVisibleHeight
        return value.coerceIn(0, max.coerceAtLeast(0))
    }
}

class FloatingOverlayDragController(
    private val windowManager: WindowManager,
    private val touchSlop: Int,
    private val boundsProvider: () -> OverlayBounds,
    private val layoutParamsProvider: () -> WindowManager.LayoutParams?,
    private val rootViewProvider: () -> View?,
    private val onTap: () -> Unit,
    private val onPositionChanged: (x: Int, y: Int) -> Unit,
) {
    private var dragStartRawX = 0f
    private var dragStartRawY = 0f
    private var dragStartX = 0
    private var dragStartY = 0
    private var movedDuringTouch = false

    fun handleTouch(view: View, event: MotionEvent): Boolean {
        val params = layoutParamsProvider() ?: return false
        val overlayRoot = rootViewProvider() ?: view
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                dragStartRawX = event.rawX
                dragStartRawY = event.rawY
                dragStartX = params.x
                dragStartY = params.y
                movedDuringTouch = false
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.rawX - dragStartRawX
                val dy = event.rawY - dragStartRawY
                if (abs(dx) > touchSlop || abs(dy) > touchSlop) movedDuringTouch = true
                params.x = FloatingOverlayPositioning.clampX(
                    dragStartX + dx.roundToInt(),
                    overlayRoot.width,
                    boundsProvider(),
                )
                params.y = FloatingOverlayPositioning.clampY(
                    dragStartY + dy.roundToInt(),
                    overlayRoot.height,
                    boundsProvider(),
                )
                onPositionChanged(params.x, params.y)
                runCatching { windowManager.updateViewLayout(overlayRoot, params) }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (movedDuringTouch) {
                    clampCurrentPosition()
                } else {
                    onTap()
                }
                return true
            }

            MotionEvent.ACTION_CANCEL -> return true
        }
        return false
    }

    fun clampCurrentPosition() {
        val view = rootViewProvider() ?: return
        val params = layoutParamsProvider() ?: return
        params.x = FloatingOverlayPositioning.clampX(params.x, view.width, boundsProvider())
        params.y = FloatingOverlayPositioning.clampY(params.y, view.height, boundsProvider())
        onPositionChanged(params.x, params.y)
        runCatching { windowManager.updateViewLayout(view, params) }
    }
}
