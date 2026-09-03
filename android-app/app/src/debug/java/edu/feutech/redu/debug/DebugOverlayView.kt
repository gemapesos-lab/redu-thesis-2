package edu.feutech.redu.debug

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import edu.feutech.redu.R
import edu.feutech.redu.ui.theme.applyReduTextStyle
import edu.feutech.redu.ui.theme.reduDp
import edu.feutech.redu.ui.theme.reduRippleBackground
import edu.feutech.redu.ui.theme.reduShape

class DebugOverlayView(
    private val context: Context,
    private val onCapture: () -> Unit,
    private val onMinimize: () -> Unit,
    private val onTouch: (View, MotionEvent) -> Boolean,
) {
    private val surface = context.getColor(R.color.redu_surface)
    private val surfaceHigh = context.getColor(R.color.redu_surface_high)
    private val textPrimary = context.getColor(R.color.redu_text_primary)
    private val outline = context.getColor(R.color.redu_outline_variant)
    private val seaGlass = context.getColor(R.color.redu_sea_glass)
    private val chipBackgrounds = mutableMapOf<RiskBand, GradientDrawable>()
    private var minimized = true
    private var lastState: DebugOverlayState? = null

    val root: FrameLayout = OverlayRootFrameLayout(context).apply {
        isClickable = true
        setOnTouchListener(onTouch)
    }

    private val chipView: TextView = TextView(context).apply {
        gravity = Gravity.CENTER
        applyReduTextStyle(sizeSp = 13f, color = textPrimary, weight = 700, tabular = true)
        minWidth = CHIP_SIZE_DP.dp
        minHeight = CHIP_SIZE_DP.dp
        background = chipBackground(RiskBand.SAFE)
        setOnTouchListener(onTouch)
        contentDescription = "REDU extraction metrics"
    }

    private val riskTextView = labelText(sizeSp = 16f, bold = true)
    private val sessionTextView = labelText()
    private val sentimentTextView = labelText()
    private val vlmTextView = labelText()
    private val negativeTextView = labelText()
    private val positiveTextView = labelText()
    private val unscoredTextView = labelText()
    private val snippetTextView = labelText(color = textPrimary, maxLines = 2)
    private val statusTextView = labelText(color = seaGlass, sizeSp = 10f, maxLines = 1)
    private val captureButton = smallButton("Capture", onCapture)

    private val panelView: LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setOnTouchListener(onTouch)
        background = context.reduShape(surface, radiusDp = 8, strokeColor = outline)
        setPadding(12.dp, 10.dp, 12.dp, 10.dp)
        addView(
            LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(riskTextView, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                addView(smallButton("Min", onMinimize))
            },
        )
        addView(sessionTextView)
        addView(sentimentTextView)
        addView(vlmTextView)
        addView(separator())
        addView(negativeTextView)
        addView(positiveTextView)
        addView(unscoredTextView)
        addView(snippetTextView)
        addView(separator())
        addView(
            LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                addView(captureButton)
                addView(
                    statusTextView,
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                        leftMargin = 8.dp
                    },
                )
            },
        )
    }

    init {
        root.addView(
            chipView,
            FrameLayout.LayoutParams(CHIP_SIZE_DP.dp, CHIP_SIZE_DP.dp, Gravity.TOP or Gravity.START),
        )
        root.addView(
            panelView,
            FrameLayout.LayoutParams(PANEL_WIDTH_DP.dp, FrameLayout.LayoutParams.WRAP_CONTENT),
        )
        renderDisplayMode()
    }

    fun bind(state: DebugOverlayState) {
        if (lastState == state) return
        lastState = state
        val riskBand = state.riskBand()
        chipView.text = state.chipText()
        chipView.background = chipBackground(riskBand)
        chipView.setTextColor(riskBand.color)
        chipView.contentDescription = "${state.chipText()} activity score. Double tap and drag to move."
        riskTextView.text = state.riskBadgeText()
        riskTextView.setTextColor(riskBand.color)
        sessionTextView.text = state.sessionLine()
        sentimentTextView.text = state.sentimentLine()
        vlmTextView.text = state.vlmLine()
        negativeTextView.text = state.tokenLine("NEG", state.negativeTokens)
        positiveTextView.text = state.tokenLine("POS", state.positiveTokens)
        unscoredTextView.text = state.tokenLine("OOV", state.unscoredTokens)
        snippetTextView.text = state.compactSnippet()
    }

    fun setMinimized(value: Boolean) {
        minimized = value
        renderDisplayMode()
    }

    fun toggleMinimized() {
        setMinimized(!minimized)
    }

    fun resetCaptureStatus() {
        statusTextView.text = ""
        captureButton.isEnabled = true
        captureButton.alpha = 1f
        captureButton.text = "Capture"
    }

    fun setCapturing() {
        captureButton.isEnabled = false
        captureButton.alpha = 0.5f
        captureButton.text = "Saving"
        statusTextView.text = "Capturing..."
    }

    fun setCaptureResult(text: String) {
        statusTextView.text = text
        captureButton.isEnabled = true
        captureButton.alpha = 1f
        captureButton.text = "Capture"
    }

    fun clearState() {
        lastState = null
    }

    private fun renderDisplayMode() {
        chipView.visibility = if (minimized) View.VISIBLE else View.GONE
        panelView.visibility = if (minimized) View.GONE else View.VISIBLE
    }

    private fun labelText(
        color: Int = textPrimary,
        sizeSp: Float = 11f,
        bold: Boolean = false,
        maxLines: Int = 1,
    ): TextView =
        TextView(context).apply {
            applyReduTextStyle(
                sizeSp = sizeSp,
                color = color,
                weight = if (bold) 700 else 500,
                tabular = true,
            )
            this.maxLines = maxLines
            includeFontPadding = false
            setPadding(0, 3.dp, 0, 3.dp)
            ellipsize = TextUtils.TruncateAt.END
        }

    private fun smallButton(label: String, onClick: () -> Unit): Button =
        Button(context).apply {
            text = label
            isAllCaps = false
            textSize = 10f
            minHeight = 0
            minWidth = 0
            minimumHeight = 30.dp
            minimumWidth = 52.dp
            setPadding(8.dp, 0, 8.dp, 0)
            applyReduTextStyle(sizeSp = 10f, color = seaGlass, weight = 600)
            background = context.reduRippleBackground(
                fillColor = surfaceHigh,
                rippleColor = Color.argb(42, 118, 205, 184),
                radiusDp = 6,
                strokeColor = outline,
            )
            stateListAnimator = null
            setOnClickListener { onClick() }
            contentDescription = label
        }

    private fun separator(): View =
        View(context).apply {
            setBackgroundColor(outline)
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1.dp).apply {
                topMargin = 5.dp
                bottomMargin = 5.dp
            }
        }

    private fun chipBackground(riskBand: RiskBand): GradientDrawable =
        chipBackgrounds.getOrPut(riskBand) {
            GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 6.dp.toFloat()
                setColor(riskBand.containerColor)
                setStroke(1.dp, riskBand.color)
            }
        }

    private fun DebugOverlayState.riskBand(): RiskBand =
        when {
            riskScore >= 80.0 -> RiskBand.CRITICAL
            riskScore >= 50.0 -> RiskBand.WARNING
            else -> RiskBand.SAFE
        }

    private enum class RiskBand(val color: Int, val containerColor: Int) {
        SAFE(0xFF76CDB8.toInt(), 0xFF143B32.toInt()),
        WARNING(0xFFE3B76F.toInt(), 0xFF3D2F17.toInt()),
        CRITICAL(0xFFE5968C.toInt(), 0xFF42211E.toInt()),
    }

    private val Int.dp: Int
        get() = context.reduDp(this)

    companion object {
        const val CHIP_SIZE_DP = 52
        const val PANEL_WIDTH_DP = 328
    }
}

private class OverlayRootFrameLayout(context: Context) : FrameLayout(context) {
    override fun checkLayoutParams(params: ViewGroup.LayoutParams?): Boolean =
        params is FrameLayout.LayoutParams

    override fun generateLayoutParams(params: ViewGroup.LayoutParams?): FrameLayout.LayoutParams =
        when (params) {
            is FrameLayout.LayoutParams -> params
            else -> FrameLayout.LayoutParams(params?.width ?: WRAP_CONTENT, params?.height ?: WRAP_CONTENT)
        }

    override fun generateDefaultLayoutParams(): FrameLayout.LayoutParams =
        FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    private companion object {
        const val WRAP_CONTENT = ViewGroup.LayoutParams.WRAP_CONTENT
    }
}
