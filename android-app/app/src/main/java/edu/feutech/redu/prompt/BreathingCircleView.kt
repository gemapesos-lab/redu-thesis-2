package edu.feutech.redu.prompt

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

/**
 * Animated breathing guide circle for the L3 intervention overlay.
 *
 * Cycles through three phases:
 * 1. **Breathe in** (4 s) — circle expands from small to large
 * 2. **Hold** (3 s) — circle stays at max size
 * 3. **Breathe out** (4 s) — circle shrinks back to small
 *
 * The total cycle is 11 seconds, repeating until [stop] is called
 * or the overlay times out.
 */
class BreathingCircleView(context: Context) : View(context) {

    companion object {
        private const val INHALE_MS = 4_000L
        private const val HOLD_MS = 3_000L
        private const val EXHALE_MS = 4_000L
        private const val CYCLE_MS = INHALE_MS + HOLD_MS + EXHALE_MS

        private const val MIN_RADIUS_FRACTION = 0.18f
        private const val MAX_RADIUS_FRACTION = 0.42f

        private const val CIRCLE_COLOR = 0xFF4FC3F7.toInt()   // light blue
        private const val CIRCLE_ALPHA_MIN = 60
        private const val CIRCLE_ALPHA_MAX = 180
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = CIRCLE_COLOR
        style = Paint.Style.FILL
    }

    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = CIRCLE_COLOR
        style = Paint.Style.STROKE
        strokeWidth = 3f
        alpha = 100
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 48f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
        alpha = 180
    }

    /** Normalized progress 0..1 mapping to radius fraction. */
    private var radiusFraction = MIN_RADIUS_FRACTION
    private var phaseLabel = "Breathe in"
    private var phaseSubLabel = ""

    private var animator: ValueAnimator? = null

    fun start() {
        animator?.cancel()
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = CYCLE_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { anim ->
                val t = anim.animatedValue as Float
                val cycleProgress = t * CYCLE_MS
                updatePhase(cycleProgress)
                invalidate()
            }
            start()
        }
    }

    fun stop() {
        animator?.cancel()
        animator = null
    }

    private fun updatePhase(progressMs: Float) {
        when {
            // Inhale phase: 0 → INHALE_MS
            progressMs < INHALE_MS -> {
                val t = progressMs / INHALE_MS
                radiusFraction = lerp(MIN_RADIUS_FRACTION, MAX_RADIUS_FRACTION, t)
                circlePaint.alpha = lerp(CIRCLE_ALPHA_MIN.toFloat(), CIRCLE_ALPHA_MAX.toFloat(), t).toInt()
                phaseLabel = "Breathe in"
                phaseSubLabel = "slowly…"
            }
            // Hold phase: INHALE_MS → INHALE_MS + HOLD_MS
            progressMs < INHALE_MS + HOLD_MS -> {
                radiusFraction = MAX_RADIUS_FRACTION
                circlePaint.alpha = CIRCLE_ALPHA_MAX
                phaseLabel = "Hold"
                phaseSubLabel = ""
            }
            // Exhale phase: INHALE_MS + HOLD_MS → CYCLE_MS
            else -> {
                val t = (progressMs - INHALE_MS - HOLD_MS) / EXHALE_MS
                radiusFraction = lerp(MAX_RADIUS_FRACTION, MIN_RADIUS_FRACTION, t)
                circlePaint.alpha = lerp(CIRCLE_ALPHA_MAX.toFloat(), CIRCLE_ALPHA_MIN.toFloat(), t).toInt()
                phaseLabel = "Breathe out"
                phaseSubLabel = "slowly…"
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = minOf(cx, cy)

        // Outer reference ring at max size
        canvas.drawCircle(cx, cy, maxRadius * MAX_RADIUS_FRACTION, ringPaint)

        // Animated circle
        val radius = maxRadius * radiusFraction
        canvas.drawCircle(cx, cy, radius, circlePaint)

        // Phase label
        canvas.drawText(phaseLabel, cx, cy + labelPaint.textSize * 0.35f, labelPaint)
        if (phaseSubLabel.isNotEmpty()) {
            canvas.drawText(phaseSubLabel, cx, cy + labelPaint.textSize * 0.35f + subPaint.textSize * 1.3f, subPaint)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    private fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t.coerceIn(0f, 1f)
}
