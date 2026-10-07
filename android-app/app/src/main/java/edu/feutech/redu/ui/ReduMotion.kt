package edu.feutech.redu.ui

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import edu.feutech.redu.R
import edu.feutech.redu.ui.theme.ReduInlineIconSize

/** Large controls. A 24dp glyph needs [ReduIconPressedScale] to read as a press. */
internal const val ReduPressedScale = 0.97f

/** Icon buttons and nav icons. 0.97 on a 24dp stroke is about two pixels. */
internal const val ReduIconPressedScale = 0.92f

private const val PressedOverlayAlpha = 0.10f

/** Material fast effects. Press down, and the row overlay appearing. */
private const val FastEffectsStiffness = 3800f

/** Material default effects. Release, color, chevrons, and the overlay fading out. */
private const val DefaultEffectsStiffness = 1600f

/** Material standard spatial. The nav capsule. Damping 1 is no overshoot. */
private const val SpatialStiffness = 700f

/** True when the system animator duration scale is off, matching the splash. */
@Composable
internal fun reduReducedMotion(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
}

internal fun <T> reduFastEffectsSpec(): FiniteAnimationSpec<T> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = FastEffectsStiffness,
)

internal fun <T> reduDefaultEffectsSpec(): FiniteAnimationSpec<T> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = DefaultEffectsStiffness,
)

internal fun <T> reduSpatialSpec(): FiniteAnimationSpec<T> = spring(
    dampingRatio = Spring.DampingRatioNoBouncy,
    stiffness = SpatialStiffness,
)

internal fun <T> reduSettleSpec(reducedMotion: Boolean): FiniteAnimationSpec<T> =
    if (reducedMotion) snap() else reduDefaultEffectsSpec()

@Composable
internal fun Modifier.reduPressScale(
    interactionSource: InteractionSource,
    pressedScale: Float = ReduPressedScale,
): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = reduReducedMotion()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed, reducedMotion, pressedScale) {
        val target = if (pressed) pressedScale else 1f
        if (reducedMotion) {
            scale.snapTo(target)
        } else {
            scale.animateTo(
                targetValue = target,
                animationSpec = if (pressed) reduFastEffectsSpec() else reduDefaultEffectsSpec(),
            )
        }
    }
    return graphicsLayer {
        scaleX = scale.value
        scaleY = scale.value
    }
}

/**
 * A 10% content-color wash. Fading the row itself reads as disabled.
 * The parent card clips this to the section shape.
 */
@Composable
internal fun Modifier.reduPressOverlay(interactionSource: InteractionSource): Modifier {
    val pressed by interactionSource.collectIsPressedAsState()
    val reducedMotion = reduReducedMotion()
    val overlay = MaterialTheme.colorScheme.onSurface
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(pressed, reducedMotion) {
        val target = if (pressed) PressedOverlayAlpha else 0f
        if (reducedMotion) {
            alpha.snapTo(target)
        } else {
            alpha.animateTo(
                targetValue = target,
                animationSpec = if (pressed) reduFastEffectsSpec() else reduDefaultEffectsSpec(),
            )
        }
    }
    val overlayAlpha = alpha.value
    return drawBehind {
        if (overlayAlpha > 0f) {
            drawRect(overlay.copy(alpha = overlayAlpha))
        }
    }
}

@Composable
internal fun ReduExpandIcon(
    expanded: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = reduSettleSpec(reduReducedMotion()),
        label = "expand rotation",
    )
    Icon(
        painter = painterResource(R.drawable.ic_chevron_down),
        contentDescription = contentDescription,
        modifier = modifier
            .size(ReduInlineIconSize)
            .graphicsLayer { rotationZ = rotation },
        tint = tint,
    )
}
