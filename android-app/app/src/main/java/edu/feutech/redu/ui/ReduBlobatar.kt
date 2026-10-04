/*
 * Idle motion and face geometry adapted from blobatar 2.7.0.
 * https://github.com/Alain00/blobatar
 *
 * Copyright (c) Alain
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * Seed "redu", silhouette pinned to round (shape 0.11). Body #F4EDE4, eyes #2A2420.
 */
package edu.feutech.redu.ui

import android.provider.Settings
import android.view.Choreographer
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.clearAndSetSemantics
import edu.feutech.redu.ui.theme.ReduPalette
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

internal enum class BlobatarExpression {
    Idle,
    Sleepy,
}

/**
 * The REDU mascot: one pinned blobatar, with blobatar's always-on idle motion.
 * Breathe, bob, blink, and glance run together. [BlobatarExpression.Sleepy] is
 * blobatar's sleepy pose (lower lids, body sunk) under that same motion.
 * A zero system animator scale holds the rest pose.
 */
@Composable
internal fun ReduBlobatar(
    expression: BlobatarExpression,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
) {
    val context = LocalContext.current
    val reducedMotion = remember {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f,
        ) == 0f
    }
    val scrolling = LocalReduListMotion.current.scrolling
    val motion = animate && !reducedMotion
    Spacer(
        modifier
            .clearAndSetSemantics {}
            .then(
                BlobatarElement(
                    expression = expression,
                    running = motion && !scrolling,
                    restPose = !motion,
                ),
            ),
    )
}

private class BlobatarElement(
    val expression: BlobatarExpression,
    val running: Boolean,
    val restPose: Boolean,
) : ModifierNodeElement<BlobatarNode>() {
    override fun create(): BlobatarNode = BlobatarNode(expression, running, restPose)

    override fun update(node: BlobatarNode) {
        node.expression = expression
        node.setPlayback(running, restPose)
    }

    override fun equals(other: Any?): Boolean =
        other is BlobatarElement &&
            other.expression == expression &&
            other.running == running &&
            other.restPose == restPose

    override fun hashCode(): Int = expression.hashCode() xor running.hashCode() xor restPose.hashCode()
}

private class BlobatarNode(
    var expression: BlobatarExpression,
    private var running: Boolean,
    private var restPose: Boolean,
) : Modifier.Node(), DrawModifierNode {
    private var elapsedNanos = 0L
    private var originNanos = 0L
    private var posted = false
    private val scratch = DrawScratch()
    private val callback = Choreographer.FrameCallback { now ->
        posted = false
        if (!isAttached || !running) return@FrameCallback
        if (originNanos == 0L) originNanos = now - elapsedNanos
        elapsedNanos = now - originNanos
        invalidateDraw()
        schedule()
    }

    fun setPlayback(running: Boolean, restPose: Boolean) {
        if (restPose && (elapsedNanos != 0L || originNanos != 0L)) {
            elapsedNanos = 0L
            originNanos = 0L
            if (isAttached) invalidateDraw()
        }
        this.restPose = restPose
        if (this.running == running) {
            if (isAttached) invalidateDraw()
            return
        }
        this.running = running
        if (running) {
            originNanos = 0L
            if (isAttached) schedule()
        } else {
            unschedule()
        }
    }

    override fun onAttach() {
        if (running) schedule()
    }

    override fun onDetach() {
        unschedule()
    }

    override fun ContentDrawScope.draw() {
        val pose = if (expression == BlobatarExpression.Sleepy) SLEEPY else IDLE
        scratch.frame.fill(SEEDS, elapsedNanos / 1_000_000.0, if (restPose) 0.0 else 1.0, pose.shake)
        drawBlobatar(scratch, pose)
    }

    private fun schedule() {
        if (posted || !isAttached || !running) return
        posted = true
        Choreographer.getInstance().postFrameCallback(callback)
    }

    private fun unschedule() {
        if (!posted) return
        Choreographer.getInstance().removeFrameCallback(callback)
        posted = false
    }
}

private class DrawScratch {
    val bodyPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = ReduPalette.Figure.toArgb()
    }
    val eyePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        style = android.graphics.Paint.Style.FILL
        color = ReduPalette.FigureInk.toArgb()
    }
    val matrix = android.graphics.Matrix()
    val values = FloatArray(9)
    val frame = IdleFrame()
    val outer = Affine()
    val op = Affine()
    val eyes = Affine()
    val posed = Affine()
    val glance = Affine()
    val combined = Affine()
    val body: Path = pathOf(BODY)
    val eyePaths: List<Path> = EYE_PATHS.map { pathOf(it) }
}

private fun pathOf(data: String): Path = PathParser().parsePathString(data).toPath()

private fun ContentDrawScope.drawBlobatar(scratch: DrawScratch, pose: Pose) {
    val scale = min(size.width, size.height) / VIEWBOX
    if (scale <= 0f) return
    val frame = scratch.frame
    val op = scratch.op
    val left = (size.width - VIEWBOX * scale) / 2f
    val top = (size.height - VIEWBOX * scale) / 2f
    val outer = scratch.outer
    outer.setTranslate(left.toDouble(), top.toDouble())
    outer.post(op.setScale(scale.toDouble(), scale.toDouble()))
    outer.post(op.setTranslate(frame.shake[0], frame.shake[1]))
    outer.post(op.setTranslate(50.0, 50.0))
    outer.post(op.setScale(frame.breathe[0], frame.breathe[1]))
    outer.post(op.setTranslate(-50.0, -50.0))
    outer.post(op.setTranslate(0.0, pose.bdy + frame.bob))
    drawPath(scratch.body, outer, scratch)
    val eyesGroup = scratch.eyes.set(outer).post(op.setTranslate(frame.saccade[0], frame.saccade[1]))
    scratch.eyePaths.forEachIndexed { index, eye ->
        poseInto(scratch.posed, op, EYE_FRAMES[index], pose, frame.rockp, index)
        glanceInto(scratch.glance, op, EYE_FRAMES[index], frame, index)
        val combined = scratch.combined.set(eyesGroup).post(scratch.posed).post(scratch.glance)
        drawPath(eye, combined, scratch, eyes = true)
    }
}

private fun ContentDrawScope.drawPath(path: Path, transform: Affine, scratch: DrawScratch, eyes: Boolean = false) {
    val values = scratch.values
    values[0] = transform.a.toFloat()
    values[1] = transform.c.toFloat()
    values[2] = transform.e.toFloat()
    values[3] = transform.b.toFloat()
    values[4] = transform.d.toFloat()
    values[5] = transform.f.toFloat()
    values[6] = 0f
    values[7] = 0f
    values[8] = 1f
    scratch.matrix.setValues(values)
    val paint = if (eyes) scratch.eyePaint else scratch.bodyPaint
    drawIntoCanvas { canvas ->
        canvas.nativeCanvas.save()
        canvas.nativeCanvas.concat(scratch.matrix)
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), paint)
        canvas.nativeCanvas.restore()
    }
}

private fun glanceInto(dest: Affine, op: Affine, eye: EyeFrame, frame: IdleFrame, index: Int) {
    val side = if (index == 0) -1.0 else 1.0
    dest.setTranslate(eye.cx, eye.cy)
    dest.post(op.setRotate(frame.wrap.rot * side))
    dest.post(op.setScale(1 + frame.wrap.mx + frame.wrap.side * side, 1 + frame.wrap.sy))
    dest.post(op.setRotate(eye.rot))
    dest.post(op.setScale(1.0, frame.blink))
    dest.post(op.setRotate(-eye.rot))
    dest.post(op.setTranslate(-eye.cx, -eye.cy))
}

private fun poseInto(dest: Affine, op: Affine, eye: EyeFrame, pose: Pose, rockp: Double, index: Int) {
    val wrap = if (index == 0) -1.0 else 1.0
    val sel = if (index == 0) 0.0 else 1.0
    val phase = sel * (1 - pose.rock) + pose.rock * ((1 + wrap * rockp) / 2)
    dest.setTranslate(eye.cx + pose.edx * wrap, eye.cy + pose.edy + phase * pose.edy2)
    dest.post(op.setRotate((pose.tilt + sel * pose.tilt2) * wrap + eye.rot * (1 - pose.lock)))
    dest.post(op.setScale(pose.esx + sel * pose.esx2, pose.esy + sel * pose.esy2))
    dest.post(op.setRotate(-eye.rot))
    dest.post(op.setTranslate(-eye.cx, -eye.cy))
}

private const val VIEWBOX = 100f

private const val BODY =
    "M84.49 50.43C84.49 69.43 69.78 83.54 49.97 83.54C30.16 83.54 15.45 69.43 15.45 50.43C15.45 31.44 30.16 17.33 49.97 17.33C69.78 17.33 84.49 31.44 84.49 50.43Z"

private val EYE_PATHS = listOf(
    "M39.96 53C39.06 60.48 38.66 61.16 35.39 60.77C32.11 60.37 31.89 59.62 32.79 52.13C33.69 44.65 34.09 43.97 37.36 44.36C40.63 44.76 40.86 45.51 39.96 53Z",
    "M64.28 52.09C63.44 60.38 62.98 61.14 59.02 60.74C55.06 60.34 54.76 59.5 55.59 51.21C56.43 42.91 56.89 42.16 60.85 42.56C64.81 42.96 65.11 43.79 64.28 52.09Z",
)

private val EYE_FRAMES = listOf(
    EyeFrame(36.37132736858499, 52.5651062406572, 6.853573327884078),
    EyeFrame(59.934747509695974, 51.6482069878245, 5.772371276281774),
)

/**
 * One glance for seed "redu": the saccade period from blobatar `idleSeeds`.
 * The eyes visit every look and return. Breathe, bob, and the blink fit inside it.
 */
internal const val BlobatarGlanceCycleMillis = 5_856L

/** Timings for seed "redu" with the round silhouette pin. From blobatar `idleSeeds`. */
private val SEEDS = IdleSeeds(
    phase = 1403.0,
    bob = 2450.0,
    blink = 4474.0,
    blinkPhase = 2563.0,
    saccade = BlobatarGlanceCycleMillis.toDouble(),
    saccadePhase = 2938.0,
    lookX = 1.53,
    lookY = 1.17,
    lookMX = 1.53,
    lookMY = 1.17,
)

private val IDLE = Pose()

/** blobatar's `sleepy` pose. */
private val SLEEPY = Pose(
    esx = 1.14,
    esy = 0.22,
    tilt = 0.0,
    edy = 2.4,
    edx = 0.3,
    esx2 = -0.04,
    esy2 = 0.03,
    tilt2 = 4.0,
    edy2 = 0.0,
    lock = 1.0,
    shake = 0.0,
    rock = 0.0,
    bdy = 1.2,
)

private data class EyeFrame(val cx: Double, val cy: Double, val rot: Double)

private data class Pose(
    val esx: Double = 1.0,
    val esy: Double = 1.0,
    val tilt: Double = 0.0,
    val edy: Double = 0.0,
    val edx: Double = 0.0,
    val esx2: Double = 0.0,
    val esy2: Double = 0.0,
    val tilt2: Double = 0.0,
    val edy2: Double = 0.0,
    val lock: Double = 0.0,
    val shake: Double = 0.0,
    val rock: Double = 0.0,
    val bdy: Double = 0.0,
)

private data class IdleSeeds(
    val phase: Double,
    val bob: Double,
    val blink: Double,
    val blinkPhase: Double,
    val saccade: Double,
    val saccadePhase: Double,
    val lookX: Double,
    val lookY: Double,
    val lookMX: Double,
    val lookMY: Double,
)

private class IdleFrame {
    val shake = DoubleArray(2)
    val breathe = DoubleArray(2)
    var bob = 0.0
    val saccade = DoubleArray(2)
    var rockp = 0.0
    var blink = 1.0
    val wrap = Wrap()

    fun fill(seeds: IdleSeeds, timeMs: Double, amp: Double, shakeAmp: Double) {
        val breatheU = easeInOut(alternate(timeMs, seeds.phase, 2800.0))
        val bobU = easeInOut(alternate(timeMs, seeds.bob, 3400.0))
        val sac = cycle(timeMs, seeds.saccadePhase, seeds.saccade)
        val shaken = cycle(timeMs, 0.0, 112.0)
        val rock = cycle(timeMs, 0.0, 900.0)
        rockp = if (rock < 0.5) {
            1 - 2 * easeInOut(rock * 2)
        } else {
            -1 + 2 * easeInOut(rock * 2 - 1)
        }
        val blinkU = cycle(timeMs, seeds.blinkPhase, seeds.blink)
        blink = when {
            blinkU < 0.972 -> 1.0
            blinkU < 0.986 -> 1 - 0.92 * amp * easeIn((blinkU - 0.972) / 0.014)
            else -> 1 - 0.92 * amp * (1 - easeOut((blinkU - 0.986) / 0.014))
        }
        shake[0] = stops(shaken, shakeStops, 1) * shakeAmp
        shake[1] = stops(shaken, shakeStops, 2) * shakeAmp
        breathe[0] = 1 + 0.022 * amp * breatheU
        breathe[1] = 1 - 0.018 * amp * breatheU
        bob = -1.1 * amp * bobU
        saccade[0] = stops(sac, saccadeStops, 1) * seeds.lookX * amp
        saccade[1] = stops(sac, saccadeStops, 2) * seeds.lookY * amp
        wrap.mx = stops(sac, wrapStops, 1) * seeds.lookMX * amp
        wrap.side = stops(sac, wrapStops, 2) * seeds.lookX * amp
        wrap.sy = stops(sac, wrapStops, 3) * seeds.lookMY * amp
        wrap.rot = stops(sac, wrapStops, 4) * seeds.lookX * seeds.lookY * amp
    }
}

private class Wrap {
    var mx = 0.0
    var side = 0.0
    var sy = 0.0
    var rot = 0.0
}

private val easeInOut = bezier(0.42, 0.0, 0.58, 1.0)
private val easeIn = bezier(0.42, 0.0, 1.0, 1.0)
private val easeOut = bezier(0.0, 0.0, 0.58, 1.0)

private val saccadeStops = arrayOf(
    doubleArrayOf(0.0, 0.0, 0.0),
    doubleArrayOf(0.15, 0.0, 0.0),
    doubleArrayOf(0.165, -0.8, -0.9),
    doubleArrayOf(0.31, -0.8, -0.9),
    doubleArrayOf(0.325, 1.0, 0.1),
    doubleArrayOf(0.47, 1.0, 0.1),
    doubleArrayOf(0.485, -0.15, 0.85),
    doubleArrayOf(0.63, -0.15, 0.85),
    doubleArrayOf(0.645, 0.75, -0.8),
    doubleArrayOf(0.79, 0.75, -0.8),
    doubleArrayOf(0.805, -1.0, -0.15),
    doubleArrayOf(0.985, -1.0, -0.15),
    doubleArrayOf(1.0, 0.0, 0.0),
)

private val wrapStops = arrayOf(
    doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0),
    doubleArrayOf(0.15, 0.0, 0.0, 0.0, 0.0),
    doubleArrayOf(0.165, -0.0176, 0.008, -0.027, 0.648),
    doubleArrayOf(0.31, -0.0176, 0.008, -0.027, 0.648),
    doubleArrayOf(0.325, -0.022, -0.01, -0.003, 0.09),
    doubleArrayOf(0.47, -0.022, -0.01, -0.003, 0.09),
    doubleArrayOf(0.485, -0.0033, 0.0015, -0.0255, -0.115),
    doubleArrayOf(0.63, -0.0033, 0.0015, -0.0255, -0.115),
    doubleArrayOf(0.645, -0.0165, -0.0075, -0.024, -0.54),
    doubleArrayOf(0.79, -0.0165, -0.0075, -0.024, -0.54),
    doubleArrayOf(0.805, -0.022, 0.01, -0.0045, 0.135),
    doubleArrayOf(0.985, -0.022, 0.01, -0.0045, 0.135),
    doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0),
)

private val shakeStops = arrayOf(
    doubleArrayOf(0.0, 0.62, -0.34),
    doubleArrayOf(0.25, -0.7, 0.22),
    doubleArrayOf(0.5, 0.38, 0.66),
    doubleArrayOf(0.75, -0.44, -0.6),
    doubleArrayOf(1.0, 0.62, -0.34),
)

private fun cycle(timeMs: Double, phase: Double, period: Double): Double {
    val u = (timeMs + phase) / period
    return u - floor(u)
}

private fun alternate(timeMs: Double, phase: Double, period: Double): Double {
    val u = (timeMs + phase) / period
    val n = floor(u)
    val fraction = u - n
    return if (n.toInt() % 2 != 0) 1 - fraction else fraction
}

private fun stops(u: Double, table: Array<DoubleArray>, col: Int): Double {
    for (i in table.lastIndex downTo 0) {
        val row = table[i]
        if (u < row[0]) continue
        val next = table.getOrNull(i + 1) ?: return row[col]
        val span = next[0] - row[0]
        return if (span <= 0) row[col] else row[col] + (next[col] - row[col]) * ((u - row[0]) / span)
    }
    return table[0][col]
}

private fun bezier(x1: Double, y1: Double, x2: Double, y2: Double): (Double) -> Double {
    val cx = 3 * x1
    val bx = 3 * (x2 - x1) - cx
    val ax = 1 - cx - bx
    val cy = 3 * y1
    val by = 3 * (y2 - y1) - cy
    val ay = 1 - cy - by
    return { x ->
        var t = x
        for (step in 0 until 8) {
            val err = ((ax * t + bx) * t + cx) * t - x
            if (abs(err) < 1e-5) break
            val derivative = (3 * ax * t + 2 * bx) * t + cx
            if (abs(derivative) < 1e-6) break
            t -= err / derivative
        }
        ((ay * t + by) * t + cy) * t
    }
}

/**
 * SVG-style affine: [then] post-multiplies, so the argument is applied to the
 * point first. A left-to-right SVG transform list is a chain of [then] calls.
 */
private class Affine {
    var a: Double = 1.0
    var b: Double = 0.0
    var c: Double = 0.0
    var d: Double = 1.0
    var e: Double = 0.0
    var f: Double = 0.0

    fun set(other: Affine): Affine {
        a = other.a
        b = other.b
        c = other.c
        d = other.d
        e = other.e
        f = other.f
        return this
    }

    fun setTranslate(x: Double, y: Double): Affine {
        a = 1.0
        b = 0.0
        c = 0.0
        d = 1.0
        e = x
        f = y
        return this
    }

    fun setScale(x: Double, y: Double): Affine {
        a = x
        b = 0.0
        c = 0.0
        d = y
        e = 0.0
        f = 0.0
        return this
    }

    fun setRotate(degrees: Double): Affine {
        val radians = Math.toRadians(degrees)
        val cos = cos(radians)
        val sin = sin(radians)
        a = cos
        b = sin
        c = -sin
        d = cos
        e = 0.0
        f = 0.0
        return this
    }

    /** Post-multiply, matching the old `then`: [right] is applied to the point first. */
    fun post(right: Affine): Affine {
        val na = a * right.a + c * right.b
        val nb = b * right.a + d * right.b
        val nc = a * right.c + c * right.d
        val nd = b * right.c + d * right.d
        val ne = a * right.e + c * right.f + e
        val nf = b * right.e + d * right.f + f
        a = na
        b = nb
        c = nc
        d = nd
        e = ne
        f = nf
        return this
    }
}
