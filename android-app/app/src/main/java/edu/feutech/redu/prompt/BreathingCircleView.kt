package edu.feutech.redu.prompt

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import edu.feutech.redu.R

internal data class BreathingPhase(
    val label: String,
    val instruction: String,
)

class BreathingCircleView(context: Context) : View(context) {
    companion object {
        private const val INHALE_MS = 4_000L
        private const val HOLD_MS = 3_000L
        private const val EXHALE_MS = 4_000L
        private const val CYCLE_MS = INHALE_MS + HOLD_MS + EXHALE_MS
        private const val MIN_RADIUS_FRACTION = 0.18f
        private const val MAX_RADIUS_FRACTION = 0.40f
        private const val CIRCLE_ALPHA_MIN = 72
        private const val CIRCLE_ALPHA_MAX = 188
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.redu_sea_glass)
        style = Paint.Style.FILL
    }
    private val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.redu_sea_glass)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.displayMetrics.density * 2f
        alpha = 92
    }
    private val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = context.getColor(R.color.redu_sea_glass)
        style = Paint.Style.FILL
        alpha = 34
    }
    private val phaseInterpolator = AccelerateDecelerateInterpolator()
    private var radiusFraction = MIN_RADIUS_FRACTION
    private var currentPhase = BreathingPhase("Breathe in", "Slowly")
    private var phaseListener: ((BreathingPhase) -> Unit)? = null
    private var animator: ValueAnimator? = null

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    internal fun setOnPhaseChanged(listener: (BreathingPhase) -> Unit) {
        phaseListener = listener
        listener(currentPhase)
    }

    fun start() {
        animator?.cancel()
        if (!ValueAnimator.areAnimatorsEnabled()) {
            radiusFraction = 0.3f
            setPhase(BreathingPhase("Breathe slowly", "At your own pace"))
            invalidate()
            return
        }
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = CYCLE_MS
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                updatePhase((animation.animatedValue as Float) * CYCLE_MS)
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
            progressMs < INHALE_MS -> {
                val fraction = phaseInterpolator.getInterpolation(progressMs / INHALE_MS)
                radiusFraction = lerp(MIN_RADIUS_FRACTION, MAX_RADIUS_FRACTION, fraction)
                circlePaint.alpha = lerp(CIRCLE_ALPHA_MIN.toFloat(), CIRCLE_ALPHA_MAX.toFloat(), fraction).toInt()
                setPhase(BreathingPhase("Breathe in", "Slowly"))
            }
            progressMs < INHALE_MS + HOLD_MS -> {
                radiusFraction = MAX_RADIUS_FRACTION
                circlePaint.alpha = CIRCLE_ALPHA_MAX
                setPhase(BreathingPhase("Hold", "Stay comfortable"))
            }
            else -> {
                val fraction = phaseInterpolator.getInterpolation(
                    (progressMs - INHALE_MS - HOLD_MS) / EXHALE_MS,
                )
                radiusFraction = lerp(MAX_RADIUS_FRACTION, MIN_RADIUS_FRACTION, fraction)
                circlePaint.alpha = lerp(CIRCLE_ALPHA_MAX.toFloat(), CIRCLE_ALPHA_MIN.toFloat(), fraction).toInt()
                setPhase(BreathingPhase("Breathe out", "Slowly"))
            }
        }
    }

    private fun setPhase(phase: BreathingPhase) {
        if (phase == currentPhase) return
        currentPhase = phase
        phaseListener?.invoke(phase)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val centerX = width / 2f
        val centerY = height / 2f
        val maxRadius = minOf(centerX, centerY)
        canvas.drawCircle(centerX, centerY, maxRadius * MAX_RADIUS_FRACTION, ringPaint)
        canvas.drawCircle(centerX, centerY, maxRadius * 0.48f, corePaint)
        canvas.drawCircle(centerX, centerY, maxRadius * radiusFraction, circlePaint)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun lerp(start: Float, end: Float, fraction: Float): Float =
        start + (end - start) * fraction.coerceIn(0f, 1f)
}
