package edu.feutech.redu.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import edu.feutech.redu.R
import edu.feutech.redu.ui.theme.ReduPalette
import edu.feutech.redu.ui.theme.ReduTheme

private const val ReducedMotionSplashMillis = 400L

/** Covers the first blink (~1.9s) without holding the full glance. */
internal const val SplashBeatMillis = 2_000L

/**
 * How long the splash should stay up once content is ready.
 * A fresh launch waits for [SplashBeatMillis]. If loading already ran past that,
 * leave immediately.
 */
internal fun splashHoldMillis(
    elapsedMillis: Long,
    reducedMotion: Boolean,
): Long {
    val elapsed = elapsedMillis.coerceAtLeast(0L)
    val minimum = if (reducedMotion) ReducedMotionSplashMillis else SplashBeatMillis
    return (minimum - elapsed).coerceAtLeast(0L)
}

/**
 * Launch screen for the idle mascot. It stays up through the first blink,
 * then fades into the app. Sleepy stays on empty states.
 */
@Composable
internal fun ReduSplashScreen(
    dismiss: Boolean,
    reducedMotion: Boolean,
    onDismissed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val alpha = remember { Animatable(1f) }
    LaunchedEffect(dismiss, reducedMotion) {
        if (!dismiss) return@LaunchedEffect
        if (reducedMotion) {
            alpha.snapTo(0f)
        } else {
            alpha.animateTo(0f, tween(durationMillis = 320))
        }
        onDismissed()
    }
    Column(
        modifier
            .fillMaxSize()
            .graphicsLayer { this.alpha = alpha.value }
            .background(ReduPalette.Background)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { it.consume() }
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ReduBlobatar(
            expression = BlobatarExpression.Idle,
            modifier = Modifier.size(220.dp),
            animate = !reducedMotion,
        )
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.offset(y = (-8).dp),
            style = MaterialTheme.typography.titleLarge,
            color = ReduPalette.Figure,
        )
        Spacer(Modifier.height(28.dp))
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0E0C0B, widthDp = 360, heightDp = 800)
@Composable
private fun ReduSplashPreview() {
    ReduTheme {
        ReduSplashScreen(dismiss = false, reducedMotion = true, onDismissed = {})
    }
}
