package edu.feutech.redu.prompt

import android.accessibilityservice.AccessibilityService
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import edu.feutech.redu.MainActivity
import edu.feutech.redu.R
import edu.feutech.redu.data.PromptAction
import edu.feutech.redu.data.PromptLevel
import edu.feutech.redu.ui.theme.ReduViewPageRadiusDp
import edu.feutech.redu.ui.theme.ReduViewPillRadiusDp
import edu.feutech.redu.ui.theme.applyReduTextStyle
import edu.feutech.redu.ui.theme.reduDp
import edu.feutech.redu.ui.theme.reduRippleBackground
import edu.feutech.redu.ui.theme.reduShape
import java.util.Locale

sealed class PromptPresentationEvent {
    data object NonBlockingShown : PromptPresentationEvent()
    data object BlockingShown : PromptPresentationEvent()
    data class Closed(val action: PromptAction) : PromptPresentationEvent()
}

object PromptPresenter {
    private const val AWARENESS_PROMPT_MILLIS = 4_000L
    private const val BREATHING_PROMPT_MILLIS = 45_000L
    private const val CONTINUE_UNLOCK_DELAY_MILLIS = 3_000L
    private val mainHandler = Handler(Looper.getMainLooper())
    private var activePrompt: ActivePromptOverlay? = null

    fun show(
        service: AccessibilityService,
        level: PromptLevel,
        score: Double,
        onEvent: (PromptPresentationEvent) -> Unit = {},
    ) {
        if (level == PromptLevel.NONE) return
        mainHandler.post {
            when (level) {
                PromptLevel.L1_AWARENESS -> showAwarenessBanner(service, onEvent)
                PromptLevel.L2_PAUSE -> showPauseOverlay(service, onEvent)
                PromptLevel.L3_BREATHING -> showBreathingOverlay(service, onEvent)
                PromptLevel.NONE -> Unit
            }
        }
    }

    fun dismissActivePrompt() {
        mainHandler.post { closeActivePrompt(emitDefaultClosed = false) }
    }

    private fun showAwarenessBanner(
        service: AccessibilityService,
        onEvent: (PromptPresentationEvent) -> Unit,
    ) {
        val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val surface = service.getColor(R.color.redu_surface_high)
        val textPrimary = service.getColor(R.color.redu_text_primary)
        val textSecondary = service.getColor(R.color.redu_text_secondary)
        val accent = service.getColor(R.color.redu_action)

        val root = FrameLayout(service).apply {
            setPadding(
                service.reduDp(16),
                systemBarInset(service, "status_bar_height") + service.reduDp(12),
                service.reduDp(16),
                service.reduDp(8),
            )
        }
        val content = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            this.background = service.reduShape(surface, radiusDp = ReduViewPageRadiusDp)
            elevation = service.reduDp(10).toFloat()
            setPadding(service.reduDp(16), service.reduDp(14), service.reduDp(16), service.reduDp(12))
            addView(TextView(service).apply {
                text = "REDU / PAUSE"
                applyReduTextStyle(sizeSp = 11f, color = textSecondary, weight = 700)
            })
            addView(TextView(service).apply {
                text = "You've been scrolling for a while. A short pause may help."
                applyReduTextStyle(sizeSp = 15f, color = textPrimary, weight = 500)
                setLineSpacing(service.reduDp(2).toFloat(), 1f)
                setPadding(0, service.reduDp(5), 0, service.reduDp(10))
            })
        }
        root.addView(
            content,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP,
            ),
        )
        val progress = View(service).apply {
            setBackgroundColor(accent)
            pivotX = 0f
        }
        content.addView(
            progress,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, service.reduDp(2)).apply {
                topMargin = service.reduDp(10)
            },
        )

        val timeout = Runnable { closeActivePrompt(emitDefaultClosed = false) }
        runCatching {
            closeActivePrompt(emitDefaultClosed = true)
            windowManager.addView(
                root,
                overlayParams(
                    width = WindowManager.LayoutParams.MATCH_PARENT,
                    height = WindowManager.LayoutParams.WRAP_CONTENT,
                    gravity = Gravity.TOP,
                    interactive = false,
                ),
            )
            activePrompt = ActivePromptOverlay(
                windowManager = windowManager,
                view = root,
                timeout = timeout,
                onClosed = { progress.animate().cancel() },
            )
            if (ValueAnimator.areAnimatorsEnabled()) {
                root.alpha = 0f
                root.translationY = -service.reduDp(12).toFloat()
                root.animate().alpha(1f).translationY(0f).setDuration(180L).start()
                progress.animate().scaleX(0f).setDuration(AWARENESS_PROMPT_MILLIS).start()
            }
            mainHandler.postDelayed(timeout, AWARENESS_PROMPT_MILLIS)
            onEvent(PromptPresentationEvent.NonBlockingShown)
        }.onFailure {
            Toast.makeText(service, "You've been scrolling for a while. A short pause may help.", Toast.LENGTH_LONG).show()
            onEvent(PromptPresentationEvent.NonBlockingShown)
        }
    }

    private fun showPauseOverlay(
        service: AccessibilityService,
        onEvent: (PromptPresentationEvent) -> Unit,
    ) {
        val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val background = service.getColor(R.color.redu_background)
        val textPrimary = service.getColor(R.color.redu_text_primary)
        val textSecondary = service.getColor(R.color.redu_text_secondary)
        fun close(action: PromptAction) {
            closeActivePrompt(emitDefaultClosed = false)
            onEvent(PromptPresentationEvent.Closed(action))
        }

        val root = FrameLayout(service).apply {
            setBackgroundColor(background)
            isClickable = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        val sheetContent = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(
                service.reduDp(24),
                systemBarInset(service, "status_bar_height") + service.reduDp(24),
                service.reduDp(24),
                systemBarInset(service, "navigation_bar_height") + service.reduDp(18),
            )
            addView(TextView(service).apply {
                text = "REDU / PAUSE"
                applyReduTextStyle(sizeSp = 11f, color = textSecondary, weight = 700)
                gravity = Gravity.CENTER
            })
            addView(TextView(service).apply {
                text = "Pause for a moment"
                applyReduTextStyle(sizeSp = 28f, color = textPrimary, weight = 700)
                gravity = Gravity.CENTER
                setPadding(0, service.reduDp(8), 0, service.reduDp(8))
            })
            addView(TextView(service).apply {
                text = "This session has been going for a while."
                applyReduTextStyle(sizeSp = 16f, color = textSecondary)
                gravity = Gravity.CENTER
                setLineSpacing(service.reduDp(3).toFloat(), 1f)
                setPadding(0, 0, 0, service.reduDp(20))
            })
        }

        val takeBreakButton = promptButton(service, "Take a break", filled = true) {
            close(PromptAction.TAKE_BREAK)
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
        }
        sheetContent.addView(
            takeBreakButton,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, service.reduDp(52)).apply {
                bottomMargin = service.reduDp(10)
            },
        )

        val continueButton = promptButton(service, "Keep scrolling", filled = false) {
            close(PromptAction.CONTINUE)
        }.apply {
            isEnabled = false
            alpha = 0.42f
            contentDescription = "Keep scrolling, available after a short pause"
        }
        sheetContent.addView(
            continueButton,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, service.reduDp(52)).apply {
                bottomMargin = service.reduDp(4)
            },
        )

        val openRedu = textAction(service, "Open REDU", textPrimary) {
            close(PromptAction.VIEW_DASHBOARD)
            service.startActivity(
                Intent(service, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        }
        sheetContent.addView(
            openRedu,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, service.reduDp(52)),
        )

        val sheet = ScrollView(service).apply {
            isFillViewport = true
            clipToPadding = false
            addView(
                sheetContent,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT),
            )
        }
        root.addView(
            sheet,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
        )

        val unlockContinue = Runnable {
            continueButton.isEnabled = true
            continueButton.alpha = 1f
            continueButton.contentDescription = "Keep scrolling"
            continueButton.announceForAccessibility("Keep scrolling is now available")
        }
        runCatching {
            closeActivePrompt(emitDefaultClosed = true)
            windowManager.addView(
                root,
                overlayParams(
                    width = WindowManager.LayoutParams.MATCH_PARENT,
                    height = WindowManager.LayoutParams.MATCH_PARENT,
                    gravity = Gravity.CENTER,
                    interactive = true,
                ),
            )
            activePrompt = ActivePromptOverlay(
                windowManager = windowManager,
                view = root,
                onClosed = { mainHandler.removeCallbacks(unlockContinue) },
                onDefaultClosed = { onEvent(PromptPresentationEvent.Closed(PromptAction.DISMISSED)) },
            )
            if (ValueAnimator.areAnimatorsEnabled()) {
                root.alpha = 0f
                root.animate().alpha(1f).setDuration(220L).start()
            }
            mainHandler.postDelayed(unlockContinue, CONTINUE_UNLOCK_DELAY_MILLIS)
            onEvent(PromptPresentationEvent.BlockingShown)
        }.onFailure {
            Toast.makeText(service, "Consider taking a short pause.", Toast.LENGTH_LONG).show()
            onEvent(PromptPresentationEvent.NonBlockingShown)
        }
    }

    private fun showBreathingOverlay(
        service: AccessibilityService,
        onEvent: (PromptPresentationEvent) -> Unit,
    ) {
        val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val background = service.getColor(R.color.redu_background)
        val textPrimary = service.getColor(R.color.redu_text_primary)
        val textSecondary = service.getColor(R.color.redu_text_secondary)
        fun close(action: PromptAction) {
            closeActivePrompt(emitDefaultClosed = false)
            onEvent(PromptPresentationEvent.Closed(action))
        }

        val root = FrameLayout(service).apply {
            setBackgroundColor(background)
            isFocusable = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
        }
        val content = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(
                service.reduDp(24),
                systemBarInset(service, "status_bar_height") + service.reduDp(24),
                service.reduDp(24),
                systemBarInset(service, "navigation_bar_height") + service.reduDp(18),
            )
        }
        val scroll = ScrollView(service).apply {
            isFillViewport = true
            clipToPadding = false
            setBackgroundColor(Color.TRANSPARENT)
            addView(
                content,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT),
            )
        }
        root.addView(
            scroll,
            FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT),
        )

        content.addView(TextView(service).apply {
            text = "REDU / RESET"
            applyReduTextStyle(sizeSp = 11f, color = textSecondary, weight = 700)
            gravity = Gravity.CENTER
        })
        content.addView(TextView(service).apply {
            text = "Take a breathing break"
            applyReduTextStyle(sizeSp = 28f, color = textPrimary, weight = 700)
            gravity = Gravity.CENTER
            setPadding(0, service.reduDp(10), 0, service.reduDp(6))
        })

        val breathingView = BreathingCircleView(service)
        val phaseLabel = TextView(service).apply {
            applyReduTextStyle(sizeSp = 30f, color = textPrimary, weight = 700)
            gravity = Gravity.CENTER
            maxLines = 2
            setAutoSizeTextTypeUniformWithConfiguration(20, 32, 1, TypedValue.COMPLEX_UNIT_SP)
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        val phaseInstruction = TextView(service).apply {
            applyReduTextStyle(sizeSp = 15f, color = textSecondary, weight = 500)
            gravity = Gravity.CENTER
        }
        val phaseGroup = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
            setPadding(service.reduDp(28), service.reduDp(8), service.reduDp(28), service.reduDp(12))
            addView(phaseLabel, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
            addView(phaseInstruction, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        }
        breathingView.setOnPhaseChanged { phase ->
            phaseLabel.text = phase.label
            val instruction = phase.instruction
            phaseInstruction.text = instruction
            phaseInstruction.visibility = if (instruction.isBlank()) View.GONE else View.VISIBLE
            val announcement = if (instruction.isBlank()) phase.label else "${phase.label}. $instruction"
            phaseGroup.contentDescription = announcement
            if (phaseGroup.isAttachedToWindow) phaseGroup.announceForAccessibility(announcement)
        }
        val geometryHeightDp = if (service.resources.configuration.fontScale >= 1.5f) 168 else 248
        content.addView(
            breathingView,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, service.reduDp(geometryHeightDp)).apply {
                topMargin = service.reduDp(4)
            },
        )
        content.addView(
            phaseGroup,
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT),
        )

        val totalSeconds = (BREATHING_PROMPT_MILLIS / 1_000L).toInt()
        val countdown = TextView(service).apply {
            text = formatCountdown(totalSeconds)
            applyReduTextStyle(sizeSp = 18f, color = textSecondary, weight = 500, tabular = true)
            gravity = Gravity.CENTER
            contentDescription = "$totalSeconds seconds remaining"
            setPadding(0, service.reduDp(4), 0, service.reduDp(12))
        }
        content.addView(countdown, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))

        content.addView(TextView(service).apply {
            text = "This is a digital wellness pause, not a clinical exercise."
            applyReduTextStyle(sizeSp = 12f, color = textSecondary)
            gravity = Gravity.CENTER
            setPadding(service.reduDp(8), service.reduDp(4), service.reduDp(8), service.reduDp(8))
        }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
            bottomMargin = service.reduDp(14)
        })

        content.addView(
            promptButton(service, "Finish early", filled = false) {
                close(PromptAction.TAKE_BREAK)
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, service.reduDp(52)).apply {
                bottomMargin = service.reduDp(4)
            },
        )
        content.addView(
            textAction(service, "Return to scrolling", textSecondary) { close(PromptAction.DISMISSED) },
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, service.reduDp(52)),
        )

        val timeout = Runnable { close(PromptAction.DISMISSED) }
        var remaining = totalSeconds
        val tick = object : Runnable {
            override fun run() {
                remaining -= 1
                if (remaining >= 0) {
                    countdown.text = formatCountdown(remaining)
                    countdown.contentDescription = "$remaining seconds remaining"
                    mainHandler.postDelayed(this, 1_000L)
                }
            }
        }

        runCatching {
            closeActivePrompt(emitDefaultClosed = true)
            windowManager.addView(
                root,
                overlayParams(
                    width = WindowManager.LayoutParams.MATCH_PARENT,
                    height = WindowManager.LayoutParams.MATCH_PARENT,
                    gravity = Gravity.CENTER,
                    interactive = true,
                ),
            )
            activePrompt = ActivePromptOverlay(
                windowManager = windowManager,
                view = root,
                timeout = timeout,
                onClosed = {
                    breathingView.stop()
                    mainHandler.removeCallbacks(tick)
                },
            )
            if (ValueAnimator.areAnimatorsEnabled()) {
                root.alpha = 0f
                root.animate().alpha(1f).setDuration(220L).start()
            }
            breathingView.start()
            mainHandler.postDelayed(tick, 1_000L)
            mainHandler.postDelayed(timeout, BREATHING_PROMPT_MILLIS)
            onEvent(PromptPresentationEvent.BlockingShown)
        }.onFailure {
            Toast.makeText(service, "Pause and try a short breathing break.", Toast.LENGTH_LONG).show()
            onEvent(PromptPresentationEvent.NonBlockingShown)
        }
    }

    private fun promptButton(
        context: Context,
        label: String,
        filled: Boolean,
        onClick: () -> Unit,
    ): Button {
        val fill = if (filled) context.getColor(R.color.redu_action) else context.getColor(R.color.redu_surface_high)
        val textColor = if (filled) context.getColor(R.color.redu_on_action) else context.getColor(R.color.redu_text_primary)
        val ripple = if (filled) Color.argb(40, 14, 22, 36) else Color.argb(42, 255, 255, 255)
        return Button(context).apply {
            text = label
            isAllCaps = false
            applyReduTextStyle(sizeSp = 14f, color = textColor, weight = 600)
            gravity = Gravity.CENTER
            minimumHeight = context.reduDp(52)
            minHeight = context.reduDp(52)
            setPadding(context.reduDp(22), context.reduDp(14), context.reduDp(22), context.reduDp(14))
            background = context.reduRippleBackground(fill, ripple, radiusDp = ReduViewPillRadiusDp)
            stateListAnimator = null
            isFocusable = true
            setOnClickListener { onClick() }
        }
    }

    private fun textAction(
        context: Context,
        label: String,
        color: Int,
        onClick: () -> Unit,
    ): TextView = TextView(context).apply {
        text = label
        applyReduTextStyle(sizeSp = 14f, color = color, weight = 600)
        gravity = Gravity.CENTER
        minimumHeight = context.reduDp(52)
        background = context.reduRippleBackground(
            fillColor = Color.TRANSPARENT,
            rippleColor = Color.argb(34, 238, 243, 240),
            radiusDp = ReduViewPillRadiusDp,
        )
        isClickable = true
        isFocusable = true
        setOnClickListener { onClick() }
    }

    private fun formatCountdown(seconds: Int): String =
        String.format(Locale.US, "0:%02d", seconds.coerceAtLeast(0))

    private fun overlayParams(
        width: Int,
        height: Int,
        gravity: Int,
        interactive: Boolean,
    ): WindowManager.LayoutParams {
        val nonInteractiveFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        return WindowManager.LayoutParams(
            width,
            height,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                (if (interactive) 0 else nonInteractiveFlags),
            android.graphics.PixelFormat.TRANSLUCENT,
        ).apply {
            this.gravity = gravity
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }
    }

    private fun systemBarInset(context: Context, resourceName: String): Int {
        val resourceId = context.resources.getIdentifier(resourceName, "dimen", "android")
        return if (resourceId == 0) 0 else context.resources.getDimensionPixelSize(resourceId)
    }

    private fun closeActivePrompt(emitDefaultClosed: Boolean = true) {
        val prompt = activePrompt ?: return
        activePrompt = null
        prompt.timeout?.let(mainHandler::removeCallbacks)
        prompt.view.animate().cancel()
        runCatching { prompt.windowManager.removeViewImmediate(prompt.view) }
        prompt.onClosed()
        if (emitDefaultClosed) prompt.onDefaultClosed()
    }

    private data class ActivePromptOverlay(
        val windowManager: WindowManager,
        val view: View,
        val timeout: Runnable? = null,
        val onClosed: () -> Unit,
        val onDefaultClosed: () -> Unit = {},
    )
}
