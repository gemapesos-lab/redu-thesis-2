package edu.feutech.redu.prompt

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import edu.feutech.redu.MainActivity
import edu.feutech.redu.data.PromptAction
import edu.feutech.redu.data.PromptLevel

sealed class PromptPresentationEvent {
    data object NonBlockingShown : PromptPresentationEvent()
    data object BlockingShown : PromptPresentationEvent()
    data class Closed(val action: PromptAction) : PromptPresentationEvent()
}

object PromptPresenter {
    private val mainHandler = Handler(Looper.getMainLooper())
    private const val BREATHING_PROMPT_MILLIS = 45_000L
    private var activePrompt: ActivePromptOverlay? = null

    fun show(
        service: AccessibilityService,
        level: PromptLevel,
        score: Double,
        onEvent: (PromptPresentationEvent) -> Unit = {},
    ) {
        val text = when (level) {
            PromptLevel.L1_AWARENESS -> "You've been scrolling for a while. Consider a short pause."
            PromptLevel.L2_PAUSE -> "Consider taking a short pause."
            PromptLevel.L3_BREATHING -> "Pause and try a short breathing break."
            PromptLevel.NONE -> return
        }
        mainHandler.post {
            when (level) {
                PromptLevel.L1_AWARENESS -> {
                    Toast.makeText(service, text, Toast.LENGTH_LONG).show()
                    onEvent(PromptPresentationEvent.NonBlockingShown)
                }
                PromptLevel.L2_PAUSE -> showPauseOverlay(service, onEvent)
                PromptLevel.L3_BREATHING -> showBreathingOverlay(service, onEvent)
                PromptLevel.NONE -> Unit
            }
        }
    }

    private fun showPauseOverlay(
        service: AccessibilityService,
        onEvent: (PromptPresentationEvent) -> Unit,
    ) {
        val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        lateinit var overlay: LinearLayout
        fun close(action: PromptAction) {
            closeActivePrompt(emitClosed = false)
            onEvent(PromptPresentationEvent.Closed(action))
        }

        val density = service.resources.displayMetrics.density
        fun dp(v: Int): Int = (v * density).toInt()

        val cardBackground = android.graphics.drawable.GradientDrawable().apply {
            setColor(0xF01E1E2E.toInt())
            cornerRadius = dp(24).toFloat()
        }

        overlay = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = cardBackground
            setPadding(dp(28), dp(28), dp(28), dp(20))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply {
                leftMargin = dp(16)
                rightMargin = dp(16)
                bottomMargin = dp(24)
            }

            // Title
            addView(TextView(service).apply {
                text = "Pause and reset"
                textSize = 22f
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(8))
            })

            // Divider
            addView(View(service).apply {
                setBackgroundColor(0x33FFFFFF)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1,
                ).apply {
                    bottomMargin = dp(14)
                }
            })

            // Body
            addView(TextView(service).apply {
                text = "You\u2019ve been scrolling for an extended period.\nConsider taking a short pause before continuing."
                textSize = 15f
                setTextColor(0xCCFFFFFF.toInt())
                gravity = Gravity.CENTER
                setPadding(dp(4), 0, dp(4), dp(20))
                setLineSpacing(dp(3).toFloat(), 1f)
            })

            // Primary button — Take break
            addView(styledButton(service, "Take break", filled = true, dp = density) {
                close(PromptAction.TAKE_BREAK)
                service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
            }.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { bottomMargin = dp(8) }
            })

            // Secondary button — View dashboard
            addView(styledButton(service, "View dashboard", filled = false, dp = density) {
                close(PromptAction.VIEW_DASHBOARD)
                service.startActivity(
                    Intent(service, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                )
            }.apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { bottomMargin = dp(6) }
            })

            // Tertiary — Continue scrolling (text-only)
            addView(TextView(service).apply {
                text = "Continue scrolling"
                textSize = 14f
                setTextColor(0x99FFFFFF.toInt())
                gravity = Gravity.CENTER
                setPadding(0, dp(8), 0, dp(4))
                setOnClickListener { close(PromptAction.CONTINUE) }
            })
        }

        // Wrap in a full-width frame to position the card at bottom
        val frame = android.widget.FrameLayout(service).apply {
            addView(overlay, android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM,
            ))
        }

        runCatching {
            closeActivePrompt(emitClosed = true)
            windowManager.addView(frame, overlayParams(WindowManager.LayoutParams.MATCH_PARENT))
            activePrompt = ActivePromptOverlay(
                windowManager = windowManager,
                view = frame,
                onClosed = { onEvent(PromptPresentationEvent.Closed(PromptAction.DISMISSED)) },
            )
            onEvent(PromptPresentationEvent.BlockingShown)
        }.onFailure {
            Toast.makeText(service, "Consider taking a short pause.", Toast.LENGTH_LONG).show()
            onEvent(PromptPresentationEvent.NonBlockingShown)
        }
    }

    private fun styledButton(
        context: Context,
        label: String,
        filled: Boolean,
        dp: Float,
        onClick: () -> Unit,
    ): Button {
        val bg = android.graphics.drawable.GradientDrawable().apply {
            cornerRadius = (12 * dp)
            if (filled) {
                setColor(0xFF6C63FF.toInt())
            } else {
                setColor(Color.TRANSPARENT)
                setStroke((1.5f * dp).toInt(), 0x66FFFFFF)
            }
        }
        return Button(context).apply {
            text = label
            isAllCaps = false
            textSize = 15f
            setTextColor(if (filled) Color.WHITE else 0xDDFFFFFF.toInt())
            background = bg
            setPadding((16 * dp).toInt(), (12 * dp).toInt(), (16 * dp).toInt(), (12 * dp).toInt())
            stateListAnimator = null
            setOnClickListener { onClick() }
        }
    }

    private fun showBreathingOverlay(
        service: AccessibilityService,
        onEvent: (PromptPresentationEvent) -> Unit,
    ) {
        val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        lateinit var overlay: LinearLayout
        fun close(action: PromptAction) {
            closeActivePrompt(emitClosed = false)
            onEvent(PromptPresentationEvent.Closed(action))
        }

        val density = service.resources.displayMetrics.density
        fun dp(v: Int): Int = (v * density).toInt()

        val breathingView = BreathingCircleView(service)
        val totalSeconds = (BREATHING_PROMPT_MILLIS / 1_000).toInt()
        val countdownText = TextView(service).apply {
            text = formatCountdown(totalSeconds)
            textSize = 18f
            setTextColor(0x99FFFFFF.toInt())
            gravity = Gravity.CENTER
            typeface = android.graphics.Typeface.MONOSPACE
        }

        // Full-screen gradient background
        val bgGradient = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xF0101020.toInt(), 0xF01A1A2E.toInt(), 0xF0141428.toInt()),
        )

        overlay = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            background = bgGradient
            setPadding(dp(32), dp(48), dp(32), dp(32))

            // Title
            addView(TextView(service).apply {
                text = "Take a short breathing break"
                textSize = 24f
                setTextColor(Color.WHITE)
                typeface = android.graphics.Typeface.DEFAULT_BOLD
                gravity = Gravity.CENTER
                setPadding(0, 0, 0, dp(6))
            })

            // Subtitle
            addView(TextView(service).apply {
                text = "Follow the circle. Breathe in as it grows, out as it shrinks."
                textSize = 14f
                setTextColor(0xAAFFFFFF.toInt())
                gravity = Gravity.CENTER
                setPadding(dp(16), 0, dp(16), dp(20))
            })

            // Breathing circle — weighted to fill available space
            addView(breathingView, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f,
            ).apply {
                topMargin = dp(8)
                bottomMargin = dp(8)
            })

            // Countdown
            addView(countdownText.apply {
                setPadding(0, dp(4), 0, dp(16))
            })

            // Disclaimer in a subtle card
            val disclaimerBg = android.graphics.drawable.GradientDrawable().apply {
                setColor(0x1AFFFFFF.toInt())
                cornerRadius = dp(12).toFloat()
            }
            addView(TextView(service).apply {
                text = "This is a digital wellness pause, not a clinical exercise."
                textSize = 12f
                setTextColor(0x88FFFFFF.toInt())
                gravity = Gravity.CENTER
                background = disclaimerBg
                setPadding(dp(16), dp(8), dp(16), dp(8))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { bottomMargin = dp(20) }
            })

            // Buttons row
            addView(LinearLayout(service).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER

                // Take break — outlined button
                addView(styledButton(service, "Take break", filled = false, dp = density) {
                    close(PromptAction.TAKE_BREAK)
                    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME)
                }.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply { rightMargin = dp(16) }
                })

                // Skip — text link
                addView(TextView(service).apply {
                    text = "Skip"
                    textSize = 15f
                    setTextColor(0x99FFFFFF.toInt())
                    gravity = Gravity.CENTER
                    setPadding(dp(20), dp(12), dp(20), dp(12))
                    setOnClickListener { close(PromptAction.DISMISSED) }
                })
            })
        }

        runCatching {
            closeActivePrompt(emitClosed = true)
            windowManager.addView(overlay, overlayParams(WindowManager.LayoutParams.MATCH_PARENT))
            val timeout = Runnable { close(PromptAction.DISMISSED) }

            // Countdown tick
            var remaining = totalSeconds
            val tick = object : Runnable {
                override fun run() {
                    remaining--
                    if (remaining >= 0) {
                        countdownText.text = formatCountdown(remaining)
                        mainHandler.postDelayed(this, 1_000L)
                    }
                }
            }
            mainHandler.postDelayed(tick, 1_000L)

            activePrompt = ActivePromptOverlay(
                windowManager = windowManager,
                view = overlay,
                timeout = timeout,
                onClosed = {
                    breathingView.stop()
                    mainHandler.removeCallbacks(tick)
                    onEvent(PromptPresentationEvent.Closed(PromptAction.DISMISSED))
                },
            )
            onEvent(PromptPresentationEvent.BlockingShown)
            breathingView.start()
            mainHandler.postDelayed(timeout, BREATHING_PROMPT_MILLIS)
        }.onFailure {
            Toast.makeText(service, "Pause and try a short breathing break.", Toast.LENGTH_LONG).show()
            onEvent(PromptPresentationEvent.NonBlockingShown)
        }
    }

    private fun formatCountdown(seconds: Int): String =
        String.format(java.util.Locale.US, "0:%02d", seconds.coerceAtLeast(0))

    private fun overlayParams(height: Int): WindowManager.LayoutParams =
        WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            height,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            android.graphics.PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = if (height == WindowManager.LayoutParams.MATCH_PARENT) {
                Gravity.CENTER
            } else {
                Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            }
        }

    private fun closeActivePrompt(emitClosed: Boolean) {
        val prompt = activePrompt ?: return
        activePrompt = null
        prompt.timeout?.let(mainHandler::removeCallbacks)
        runCatching { prompt.windowManager.removeViewImmediate(prompt.view) }
        if (emitClosed) prompt.onClosed()
    }

    private data class ActivePromptOverlay(
        val windowManager: WindowManager,
        val view: View,
        val timeout: Runnable? = null,
        val onClosed: () -> Unit,
    )
}
