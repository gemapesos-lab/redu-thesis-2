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
    private const val BREATHING_PROMPT_MILLIS = 60_000L
    private var activePrompt: ActivePromptOverlay? = null

    fun show(
        service: AccessibilityService,
        level: PromptLevel,
        score: Double,
        onEvent: (PromptPresentationEvent) -> Unit = {},
    ) {
        val text = when (level) {
            PromptLevel.L1_AWARENESS -> "REDU: You have been scrolling for a while."
            PromptLevel.L2_PAUSE -> "REDU: Consider taking a short pause."
            PromptLevel.L3_BREATHING -> "REDU: Pause and try a 60-second breathing reset."
            PromptLevel.NONE -> return
        }
        mainHandler.post {
            when (level) {
                PromptLevel.L1_AWARENESS -> {
                    Toast.makeText(service, "$text Risk score: ${score.toInt()}", Toast.LENGTH_LONG).show()
                    onEvent(PromptPresentationEvent.NonBlockingShown)
                }
                PromptLevel.L2_PAUSE -> showPauseOverlay(service, score, onEvent)
                PromptLevel.L3_BREATHING -> showBreathingOverlay(service, score, onEvent)
                PromptLevel.NONE -> Unit
            }
        }
    }

    private fun showPauseOverlay(
        service: AccessibilityService,
        score: Double,
        onEvent: (PromptPresentationEvent) -> Unit,
    ) {
        val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        lateinit var overlay: LinearLayout
        fun close(action: PromptAction) {
            closeActivePrompt(emitClosed = false)
            onEvent(PromptPresentationEvent.Closed(action))
        }

        overlay = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 28, 36, 28)
            setBackgroundColor(0xEE202124.toInt())
            addView(promptText(service, "REDU pause prompt", 20f, true))
            addView(promptText(service, "Risk score: ${score.toInt()}. Consider taking a short pause before continuing.", 16f))
            addView(
                LinearLayout(service).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.END
                    addView(promptButton(service, "Continue") { close(PromptAction.CONTINUE) })
                    addView(promptButton(service, "Take break") { close(PromptAction.TAKE_BREAK) })
                    addView(promptButton(service, "Dashboard") {
                        close(PromptAction.VIEW_DASHBOARD)
                        service.startActivity(
                            Intent(service, MainActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                        )
                    })
                },
            )
        }

        runCatching {
            closeActivePrompt(emitClosed = true)
            windowManager.addView(overlay, overlayParams(WindowManager.LayoutParams.WRAP_CONTENT))
            activePrompt = ActivePromptOverlay(
                windowManager = windowManager,
                view = overlay,
                onClosed = { onEvent(PromptPresentationEvent.Closed(PromptAction.DISMISSED)) },
            )
            onEvent(PromptPresentationEvent.BlockingShown)
        }.onFailure {
            Toast.makeText(service, "REDU: Consider taking a short pause.", Toast.LENGTH_LONG).show()
            onEvent(PromptPresentationEvent.NonBlockingShown)
        }
    }

    private fun showBreathingOverlay(
        service: AccessibilityService,
        score: Double,
        onEvent: (PromptPresentationEvent) -> Unit,
    ) {
        val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        lateinit var overlay: LinearLayout
        fun close(action: PromptAction) {
            closeActivePrompt(emitClosed = false)
            onEvent(PromptPresentationEvent.Closed(action))
        }

        overlay = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
            setBackgroundColor(0xF2172028.toInt())
            addView(promptText(service, "REDU breathing reset", 24f, true))
            addView(promptText(service, "Risk score: ${score.toInt()}", 16f))
            addView(promptText(service, "Breathe in slowly. Hold. Breathe out slowly. Repeat for 60 seconds.", 18f))
            addView(promptButton(service, "Done") { close(PromptAction.DISMISSED) })
            addView(promptButton(service, "Take break") { close(PromptAction.TAKE_BREAK) })
        }

        runCatching {
            closeActivePrompt(emitClosed = true)
            windowManager.addView(overlay, overlayParams(WindowManager.LayoutParams.MATCH_PARENT))
            val timeout = Runnable { close(PromptAction.DISMISSED) }
            activePrompt = ActivePromptOverlay(
                windowManager = windowManager,
                view = overlay,
                timeout = timeout,
                onClosed = { onEvent(PromptPresentationEvent.Closed(PromptAction.DISMISSED)) },
            )
            onEvent(PromptPresentationEvent.BlockingShown)
            mainHandler.postDelayed(timeout, BREATHING_PROMPT_MILLIS)
        }.onFailure {
            Toast.makeText(service, "REDU: Pause and try a 60-second breathing reset.", Toast.LENGTH_LONG).show()
            onEvent(PromptPresentationEvent.NonBlockingShown)
        }
    }

    private fun promptText(context: Context, value: String, sizeSp: Float, bold: Boolean = false): TextView =
        TextView(context).apply {
            text = value
            textSize = sizeSp
            setTextColor(Color.WHITE)
            if (bold) typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 18)
        }

    private fun promptButton(context: Context, label: String, onClick: () -> Unit): Button =
        Button(context).apply {
            text = label
            isAllCaps = false
            setOnClickListener { onClick() }
        }

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
