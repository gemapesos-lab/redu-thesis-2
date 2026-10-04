package edu.feutech.redu.ui.sky

import android.graphics.Bitmap
import android.opengl.GLES30
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import edu.feutech.redu.ui.BackdropEgl
import edu.feutech.redu.ui.BitmapSlot
import edu.feutech.redu.ui.ColorReadback
import edu.feutech.redu.ui.FrameBlit
import edu.feutech.redu.ui.GpuFrameStream
import edu.feutech.redu.ui.LocalReduListMotion
import edu.feutech.redu.ui.MainThreadFramePublish
import edu.feutech.redu.ui.ReadbackFence
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.awaitCancellation

private const val TAG = "BlurredSky"
private const val FRAME_NANOS = 125_000_000L
private const val MAX_RENDER_EDGE = 480
private const val HARDWARE_STALL_NANOS = 500_000_000L

private val SkyFallbackTop = Color(0xFF86A7D3)
private val SkyFallbackMid = Color(0xFF749BD0)
private val SkyFallbackBottom = Color(0xFF5B7FAE)

private val FallbackBrush = Brush.verticalGradient(
    0f to SkyFallbackTop,
    0.55f to SkyFallbackMid,
    1f to SkyFallbackBottom,
)

/**
 * Daytime sky and one cumulus deck for the home card.
 *
 * The frame is rendered on a background GL thread into a bitmap, then drawn as
 * part of the normal Compose layer. A [android.view.TextureView] cannot do that:
 * every scroll frame has to recomposite its surface, and the previous renderer
 * kept a full-screen shader on the GPU while the list moved. New frames pause
 * for the whole gesture. The UI thread only swaps in the finished bitmap.
 *
 * The last bitmap is kept so the deck does not fall back to a flat blue fill
 * when the card leaves and re-enters the list. The placeholder below is only
 * the first frame, and the gap before GL is ready.
 */
@Composable
internal fun BlurredSkyBackdrop(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        Canvas(modifier) { drawFallbackSky() }
        return
    }
    val appContext = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val gate = remember(appContext) {
        SkyPlayback(
            appContext,
            active = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED),
        )
    }
    val scrolling = LocalReduListMotion.current.scrolling
    SideEffect { gate.setScrolling(scrolling) }
    DisposableEffect(lifecycle, gate) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> gate.setActive(true)
                Lifecycle.Event.ON_PAUSE -> gate.setActive(false)
                else -> Unit
            }
        }
        lifecycle.addObserver(observer)
        gate.setActive(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose {
            lifecycle.removeObserver(observer)
            gate.close()
        }
    }
    LaunchedEffect(gate) {
        val renderer = SkyRenderer(gate)
        renderer.start()
        try {
            awaitCancellation()
        } finally {
            renderer.stop()
        }
    }
    Box(
        modifier
            .onSizeChanged { gate.setViewSize(it.width, it.height) }
            .skyBackdrop(gate),
    )
}

private fun Modifier.skyBackdrop(playback: SkyPlayback): Modifier = this.then(SkyBackdropElement(playback))

private class SkyBackdropElement(
    val playback: SkyPlayback,
) : ModifierNodeElement<SkyBackdropNode>() {
    override fun create(): SkyBackdropNode = SkyBackdropNode(playback)

    override fun update(node: SkyBackdropNode) {
        if (node.playback !== playback) {
            node.playback.detach(node)
            node.playback = playback
            if (node.isAttached) node.playback.attach(node)
        }
    }

    override fun equals(other: Any?): Boolean = other is SkyBackdropElement && other.playback === playback

    override fun hashCode(): Int = System.identityHashCode(playback)
}

private class SkyBackdropNode(
    var playback: SkyPlayback,
) : Modifier.Node(), DrawModifierNode {
    private var shadeBitmap: Bitmap? = null
    private var shade: ImageBitmap? = null

    override fun onAttach() {
        playback.attach(this)
    }

    override fun onDetach() {
        playback.detach(this)
    }

    override fun ContentDrawScope.draw() {
        drawSkyFrame()
        drawContent()
    }

    fun refresh() {
        if (isAttached) invalidateDraw()
    }

    private fun DrawScope.drawSkyFrame() {
        val bmp = retainedSky
        if (bmp == null || bmp.isRecycled) {
            drawFallbackSky()
            return
        }
        val image = if (shadeBitmap === bmp) {
            shade ?: bmp.asImageBitmap().also { shade = it }
        } else {
            bmp.asImageBitmap().also {
                shade = it
                shadeBitmap = bmp
            }
        }
        val width = size.width.roundToInt()
        val height = size.height.roundToInt()
        if (width <= 0 || height <= 0) return
        drawImage(
            image = image,
            dstSize = IntSize(width, height),
            filterQuality = FilterQuality.Medium,
        )
    }
}

private fun DrawScope.drawFallbackSky() {
    drawRect(FallbackBrush)
    val sun = Offset(size.width * 0.78f, size.height * 0.22f)
    val sunRadius = size.minDimension * 0.45f
    drawCircle(
        brush = Brush.radialGradient(
            0f to Color(0xFFFFE9C4).copy(alpha = 0.50f),
            1f to Color.Transparent,
            center = sun,
            radius = sunRadius,
        ),
        radius = sunRadius,
        center = sun,
    )
    fun puff(x: Float, y: Float, radiusFraction: Float, alpha: Float) {
        val center = Offset(size.width * x, size.height * y)
        val radius = size.width * radiusFraction
        drawCircle(
            brush = Brush.radialGradient(
                0f to Color.White.copy(alpha = alpha),
                0.62f to Color.White.copy(alpha = alpha * 0.42f),
                1f to Color.Transparent,
                center = center,
                radius = radius,
            ),
            radius = radius,
            center = center,
        )
    }
    puff(0.30f, 0.62f, 0.28f, 0.80f)
    puff(0.52f, 0.52f, 0.34f, 0.88f)
    puff(0.74f, 0.64f, 0.26f, 0.72f)
    puff(0.44f, 0.74f, 0.24f, 0.50f)
}

private var retainedSky: Bitmap? = null

private class SkyPlayback(
    private val appContext: android.content.Context,
    active: Boolean,
) {
    private val lock = ReentrantLock()
    private val wake = lock.newCondition()

    @Volatile var running: Boolean = true
        private set

    @Volatile var active: Boolean = active
        private set

    @Volatile var scrolling: Boolean = false
        private set

    @Volatile var viewWidth: Int = 0
        private set

    @Volatile var viewHeight: Int = 0
        private set

    @Volatile var reducedMotion: Boolean = animatorScaleIsZero(appContext)
        private set

    var painter: SkyBackdropNode? = null
        private set

    fun setScrolling(value: Boolean) {
        lock.withLock {
            if (scrolling == value) return
            scrolling = value
            wake.signalAll()
        }
    }

    fun setActive(value: Boolean) {
        lock.withLock {
            active = value
            reducedMotion = animatorScaleIsZero(appContext)
            wake.signalAll()
        }
    }

    fun setViewSize(width: Int, height: Int) {
        lock.withLock {
            if (viewWidth == width && viewHeight == height) return
            viewWidth = width
            viewHeight = height
            wake.signalAll()
        }
    }

    fun attach(node: SkyBackdropNode) {
        painter = node
        if (retainedSky != null) node.refresh()
    }

    fun detach(node: SkyBackdropNode) {
        if (painter === node) painter = null
    }

    fun close() {
        lock.withLock {
            running = false
            wake.signalAll()
        }
    }

    fun await(millis: Long) {
        lock.withLock {
            if (running) wake.await(millis, TimeUnit.MILLISECONDS)
        }
    }

    fun pace(frameStart: Long) {
        val spare = FRAME_NANOS - (System.nanoTime() - frameStart)
        if (spare <= 1_000_000L) return
        await(spare / 1_000_000L)
    }

    /**
     * Returns the view size to draw, or null when playback has been closed.
     * Waits while the card is off-screen, mid-scroll, or already painted for reduced motion.
     */
    fun awaitDrawableSize(hasFrame: Boolean, stillKey: Long): Pair<Int, Int>? {
        while (running) {
            val motionOff = animatorScaleIsZero(appContext)
            lock.withLock {
                reducedMotion = motionOff
                val holdStill = hasFrame && reducedMotion && stillKey == sizeKey()
                val paused = !active ||
                    viewWidth < 2 ||
                    viewHeight < 2 ||
                    (scrolling && hasFrame) ||
                    holdStill
                if (!paused) return viewWidth to viewHeight
                wake.await(400, TimeUnit.MILLISECONDS)
            }
        }
        return null
    }

    fun sizeKey(): Long = (viewWidth.toLong() shl 32) or (viewHeight.toLong() and 0xffffffffL)
}

private enum class FrameTransport {
    Unset,
    Hardware,
    Software,
}

private enum class FrameDrain {
    NotReady,
    Published,
    Dropped,
}

private class SkyRenderer(
    private val gate: SkyPlayback,
) {
    private val publish = MainThreadFramePublish(Handler(Looper.getMainLooper()))
    private var worker: Thread? = null
    private val slot = BitmapSlot()
    private val readback = ColorReadback()
    private val readFence = ReadbackFence()
    private val frameBlit = FrameBlit()
    private var packed: IntArray? = null
    private var output: HalfResTarget? = null
    private var stream: GpuFrameStream? = null
    private var staged: Bitmap? = null
    private var transport = FrameTransport.Unset
    private var pending = false
    private var pendingAt = 0L
    private var readWidth = 0
    private var readHeight = 0
    private var skyTimeLoc = -1
    private var skyResLoc = -1
    private var blurSrcLoc = -1
    private var blurTexelLoc = -1
    private var blurRadiusLoc = -1
    private var blurResLoc = -1

    @Volatile private var frameReady = false

    fun start() {
        if (worker != null) return
        worker = Thread(::loop, "blurred-sky").also {
            it.priority = Thread.NORM_PRIORITY - 1
            it.start()
        }
    }

    fun stop() {
        gate.close()
    }

    private fun loop() {
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
        var failures = 0
        while (gate.running && failures < 3) {
            val egl = BackdropEgl()
            var sky = 0
            var blur = 0
            var target: HalfResTarget? = null
            var hasFrame = frameReady
            var stillKey = Long.MIN_VALUE
            transport = FrameTransport.Unset
            pending = false
            try {
                if (!egl.init()) {
                    Log.e(TAG, "EGL init failed: 0x${Integer.toHexString(android.opengl.EGL14.eglGetError())}")
                    failures++
                    gate.await(500)
                    continue
                }
                var lastNanos = 0L
                var time = 0f
                while (gate.running) {
                    val size = gate.awaitDrawableSize(hasFrame, stillKey) ?: break
                    val (bufferWidth, bufferHeight) = skyRenderSize(size.first, size.second)
                    if (bufferWidth < 2 || bufferHeight < 2) continue
                    if (!egl.resize(bufferWidth, bufferHeight)) {
                        throw IllegalStateException("Sky surface resize failed")
                    }
                    if (sky == 0 || blur == 0) {
                        sky = linkProgram(FULLSCREEN_VERT, SKY_FRAG)
                        blur = linkProgram(FULLSCREEN_VERT, BLUR_FRAG)
                        if (sky == 0 || blur == 0) throw IllegalStateException("Sky shaders failed to link")
                        skyTimeLoc = GLES30.glGetUniformLocation(sky, "uTime")
                        skyResLoc = GLES30.glGetUniformLocation(sky, "uRes")
                        blurSrcLoc = GLES30.glGetUniformLocation(blur, "uSrc")
                        blurTexelLoc = GLES30.glGetUniformLocation(blur, "uTexel")
                        blurRadiusLoc = GLES30.glGetUniformLocation(blur, "uRadius")
                        blurResLoc = GLES30.glGetUniformLocation(blur, "uRes")
                        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
                        GLES30.glDisable(GLES30.GL_BLEND)
                    }
                    val frameStart = System.nanoTime()
                    if (pending) {
                        when (drain(bufferWidth, bufferHeight)) {
                            FrameDrain.NotReady -> {
                                if (
                                    transport == FrameTransport.Hardware &&
                                    System.nanoTime() - pendingAt > HARDWARE_STALL_NANOS
                                ) {
                                    Log.w(TAG, "Hardware frame publish stalled; using bitmaps")
                                    releaseHardwareStream()
                                    transport = FrameTransport.Software
                                    pending = false
                                } else {
                                    gate.pace(frameStart)
                                    continue
                                }
                            }
                            FrameDrain.Published -> {
                                hasFrame = true
                                frameReady = true
                                if (gate.reducedMotion) stillKey = gate.sizeKey()
                                failures = 0
                                pending = false
                            }
                            FrameDrain.Dropped -> pending = false
                        }
                    }
                    if (gate.reducedMotion && hasFrame) {
                        gate.pace(frameStart)
                        continue
                    }
                    val now = System.nanoTime()
                    val dt = if (lastNanos == 0L) 0f else ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                    lastNanos = now
                    time += dt
                    val frameTarget = target ?: HalfResTarget().also { target = it }
                    val frameOutput = output ?: HalfResTarget().also { output = it }
                    if (!drawScene(frameTarget, frameOutput, sky, blur, bufferWidth, bufferHeight, time)) {
                        throw IllegalStateException("Sky frame was incomplete")
                    }
                    if (gate.scrolling && hasFrame) {
                        gate.pace(frameStart)
                        continue
                    }
                    if (transport == FrameTransport.Unset) {
                        transport = if (openHardware(egl, bufferWidth, bufferHeight)) {
                            FrameTransport.Hardware
                        } else {
                            FrameTransport.Software
                        }
                    } else if (
                        transport == FrameTransport.Hardware &&
                        !openHardware(egl, bufferWidth, bufferHeight)
                    ) {
                        releaseHardwareStream()
                        transport = FrameTransport.Software
                    }
                    if (transport == FrameTransport.Hardware &&
                        !blitHardware(frameOutput.texture, bufferWidth, bufferHeight)
                    ) {
                        Log.w(TAG, "Hardware frame publish failed; using bitmaps")
                        releaseHardwareStream()
                        transport = FrameTransport.Software
                    }
                    if (transport == FrameTransport.Software) {
                        readback.queue(frameOutput.fbo, bufferWidth, bufferHeight)
                        readFence.arm()
                        readWidth = bufferWidth
                        readHeight = bufferHeight
                    }
                    pending = true
                    pendingAt = System.nanoTime()
                    gate.pace(frameStart)
                }
            } catch (_: InterruptedException) {
                break
            } catch (error: Exception) {
                if (gate.running) {
                    failures++
                    Log.e(TAG, "Sky backdrop failed", error)
                    gate.await(400)
                }
            } finally {
                if (egl.isCurrent()) {
                    frameBlit.delete()
                    target?.delete()
                    output?.delete()
                    readback.delete()
                    readFence.clear()
                    if (sky != 0) GLES30.glDeleteProgram(sky)
                    if (blur != 0) GLES30.glDeleteProgram(blur)
                }
                releaseHardwareStream()
                output = null
                transport = FrameTransport.Unset
                pending = false
                slot.releaseSpare()
                egl.release()
            }
        }
    }

    private fun drawScene(
        target: HalfResTarget,
        blurred: HalfResTarget,
        sky: Int,
        blur: Int,
        viewW: Int,
        viewH: Int,
        time: Float,
    ): Boolean {
        drainGlErrors()
        target.ensure(viewW / 2, viewH / 2)
        blurred.ensure(viewW, viewH)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, target.fbo)
        GLES30.glViewport(0, 0, target.width, target.height)
        GLES30.glUseProgram(sky)
        GLES30.glUniform1f(skyTimeLoc, time)
        GLES30.glUniform2f(skyResLoc, target.width.toFloat(), target.height.toFloat())
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, blurred.fbo)
        GLES30.glViewport(0, 0, viewW, viewH)
        GLES30.glUseProgram(blur)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, target.texture)
        GLES30.glUniform1i(blurSrcLoc, 0)
        GLES30.glUniform2f(blurTexelLoc, 1f / target.width, 1f / target.height)
        GLES30.glUniform1f(blurRadiusLoc, max(4f, viewH * 0.035f) * 0.5f)
        GLES30.glUniform2f(blurResLoc, viewW.toFloat(), viewH.toFloat())
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
        return drainGlErrors() == GLES30.GL_NO_ERROR
    }

    private fun drain(width: Int, height: Int): FrameDrain {
        return if (transport == FrameTransport.Hardware) drainHardware() else drainSoftware(width, height)
    }

    private fun drainSoftware(width: Int, height: Int): FrameDrain {
        if (!readFence.signaled()) return FrameDrain.NotReady
        val pixels = readback.take(packed) { packed = it }
        readFence.clear()
        if (pixels == null || readWidth != width || readHeight != height) return FrameDrain.Dropped
        return if (presentSoftware(pixels, readWidth, readHeight)) FrameDrain.Published else FrameDrain.Dropped
    }

    private fun presentSoftware(pixels: IntArray, width: Int, height: Int): Boolean {
        val bitmap = slot.obtain(width, height)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        val delivered = publish.publish({ gate.running }) {
            retainedSky = bitmap
            gate.painter?.refresh()
        }
        if (!delivered) {
            Log.w(TAG, "Dropped a sky frame because the UI thread was busy")
            return false
        }
        slot.commit(bitmap)
        return true
    }

    private fun drainHardware(): FrameDrain {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return FrameDrain.Dropped
        return drainHardware29()
    }

    @RequiresApi(29)
    private fun drainHardware29(): FrameDrain {
        if (staged == null) staged = stream?.acquireCompleted()
        val bitmap = staged ?: return FrameDrain.NotReady
        val delivered = publish.publish({ gate.running }) {
            retainedSky = bitmap
            gate.painter?.refresh()
        }
        staged = null
        if (!delivered) {
            stream?.dropNewest()
            Log.w(TAG, "Dropped a sky frame because the UI thread was busy")
            return FrameDrain.Dropped
        }
        stream?.trimHeld()
        return FrameDrain.Published
    }

    private fun openHardware(egl: BackdropEgl, width: Int, height: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !egl.supportsWindow) return false
        return openHardware29(egl, width, height)
    }

    @RequiresApi(29)
    private fun openHardware29(egl: BackdropEgl, width: Int, height: Int): Boolean {
        if (stream?.sameSize(width, height) == false) releaseHardwareStream()
        val target = stream ?: GpuFrameStream(egl).also { stream = it }
        if (!target.ensure(width, height)) {
            releaseHardwareStream()
            return false
        }
        return true
    }

    private fun blitHardware(texture: Int, width: Int, height: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return stream?.blit(frameBlit, texture, width, height) == true
    }

    private fun releaseHardwareStream() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) releaseHardwareStream29()
        stream = null
        staged = null
    }

    @RequiresApi(29)
    private fun releaseHardwareStream29() {
        val copy = stream?.copyDisplayed()
        if (copy != null) {
            publish.replace {
                retainedSky = copy
                gate.painter?.refresh()
            }
        }
        stream?.release()
    }
}

private fun skyRenderSize(viewWidth: Int, viewHeight: Int): Pair<Int, Int> {
    if (viewWidth < 2 || viewHeight < 2) return 0 to 0
    val longSide = max(viewWidth, viewHeight)
    val scale = min(1f, MAX_RENDER_EDGE.toFloat() / longSide.toFloat())
    return even((viewWidth * scale).toInt()) to even((viewHeight * scale).toInt())
}

private fun even(value: Int): Int {
    val clamped = value.coerceAtLeast(2)
    return if (clamped % 2 == 0) clamped else clamped - 1
}

private fun drainGlErrors(): Int {
    var last = GLES30.GL_NO_ERROR
    repeat(8) {
        val error = GLES30.glGetError()
        if (error == GLES30.GL_NO_ERROR) return last
        last = error
    }
    return last
}

private class HalfResTarget {
    var fbo: Int = 0
        private set
    var texture: Int = 0
        private set
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    fun ensure(requestedWidth: Int, requestedHeight: Int) {
        val nextWidth = requestedWidth.coerceAtLeast(1)
        val nextHeight = requestedHeight.coerceAtLeast(1)
        if (fbo != 0 && nextWidth == width && nextHeight == height) return
        delete()
        val textures = IntArray(1)
        GLES30.glGenTextures(1, textures, 0)
        texture = textures[0]
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE)
        GLES30.glTexImage2D(
            GLES30.GL_TEXTURE_2D,
            0,
            GLES30.GL_RGBA8,
            nextWidth,
            nextHeight,
            0,
            GLES30.GL_RGBA,
            GLES30.GL_UNSIGNED_BYTE,
            null,
        )
        val framebuffers = IntArray(1)
        GLES30.glGenFramebuffers(1, framebuffers, 0)
        fbo = framebuffers[0]
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, fbo)
        GLES30.glFramebufferTexture2D(
            GLES30.GL_FRAMEBUFFER,
            GLES30.GL_COLOR_ATTACHMENT0,
            GLES30.GL_TEXTURE_2D,
            texture,
            0,
        )
        val status = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)
        if (status != GLES30.GL_FRAMEBUFFER_COMPLETE) {
            throw IllegalStateException("Sky framebuffer incomplete: $status")
        }
        width = nextWidth
        height = nextHeight
    }

    fun delete() {
        if (fbo != 0) {
            GLES30.glDeleteFramebuffers(1, intArrayOf(fbo), 0)
            fbo = 0
        }
        if (texture != 0) {
            GLES30.glDeleteTextures(1, intArrayOf(texture), 0)
            texture = 0
        }
        width = 0
        height = 0
    }
}

private fun linkProgram(vertexSource: String, fragmentSource: String): Int {
    val vertex = compileShader(GLES30.GL_VERTEX_SHADER, vertexSource)
    val fragment = compileShader(GLES30.GL_FRAGMENT_SHADER, fragmentSource)
    if (vertex == 0 || fragment == 0) {
        if (vertex != 0) GLES30.glDeleteShader(vertex)
        if (fragment != 0) GLES30.glDeleteShader(fragment)
        return 0
    }
    val program = GLES30.glCreateProgram()
    GLES30.glAttachShader(program, vertex)
    GLES30.glAttachShader(program, fragment)
    GLES30.glLinkProgram(program)
    val linked = IntArray(1)
    GLES30.glGetProgramiv(program, GLES30.GL_LINK_STATUS, linked, 0)
    GLES30.glDeleteShader(vertex)
    GLES30.glDeleteShader(fragment)
    if (linked[0] == 0) {
        Log.e(TAG, "Program link failed: ${GLES30.glGetProgramInfoLog(program)}")
        GLES30.glDeleteProgram(program)
        return 0
    }
    return program
}

private fun compileShader(type: Int, source: String): Int {
    val shader = GLES30.glCreateShader(type)
    GLES30.glShaderSource(shader, source.trim())
    GLES30.glCompileShader(shader)
    val compiled = IntArray(1)
    GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
    if (compiled[0] == 0) {
        Log.e(TAG, "Shader compile failed: ${GLES30.glGetShaderInfoLog(shader)}")
        GLES30.glDeleteShader(shader)
        return 0
    }
    return shader
}

private fun animatorScaleIsZero(context: android.content.Context): Boolean =
    try {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    } catch (_: Exception) {
        false
    }

private const val FULLSCREEN_VERT = """
#version 300 es
void main() {
    float x = float((gl_VertexID & 1) << 2) - 1.0;
    float y = float((gl_VertexID & 2) << 1) - 1.0;
    gl_Position = vec4(x, y, 0.0, 1.0);
}
"""

private const val SKY_FRAG = """
#version 300 es
precision highp float;
uniform float uTime;
uniform vec2 uRes;
out vec4 fragColor;

const vec3 kZenith = vec3(0.3804, 0.5020, 0.7647);
const vec3 kHorizon = vec3(0.3922, 0.5137, 0.7765);
const vec3 kSunGlow = vec3(1.0, 0.9137, 0.7686);
const vec3 kSunDir = vec3(0.5235, 0.6980, 0.4886);
const float kFov = 0.1853;

float hash2(vec2 p) {
    // sin(dot) * 43758 collapses once coordinates grow, and the deck disappears.
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float vn(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash2(i), hash2(i + vec2(1.0, 0.0)), f.x),
               mix(hash2(i + vec2(0.0, 1.0)), hash2(i + vec2(1.0, 1.0)), f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 r = mat2(0.8, 0.6, -0.6, 0.8);
    for (int i = 0; i < 5; i++) {
        v += a * vn(p);
        p = r * p * 2.07 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

float fbm3(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    mat2 r = mat2(0.8, 0.6, -0.6, 0.8);
    for (int i = 0; i < 3; i++) {
        v += a * vn(p);
        p = r * p * 2.07 + vec2(1.7, 9.2);
        a *= 0.5;
    }
    return v;
}

float cloudField(vec2 p, vec2 drift, float seed) {
    float base = fbm(p * 3.2 + drift + seed);
    float fine = 0.16 * (fbm(p * 5.5 - drift * 1.4 + 4.0 + seed) - 0.5);
    float billow = 1.0 - abs(2.0 * fbm3(p * 11.0 + drift * 0.6 + 9.0 + seed) - 1.0);
    float grain = 0.06 * (fbm3(p * 26.0 + drift * 0.3 + 23.0 + seed) - 0.5);
    return base + fine + 0.14 * (billow - 0.5) + grain;
}

vec3 aces(vec3 x) {
    return clamp((x * (2.51 * x + 0.03)) / (x * (2.43 * x + 0.59) + 0.14), 0.0, 1.0);
}

vec3 skyColor(vec3 dir) {
    float h = smoothstep(-0.06, 0.6, dir.y);
    vec3 col = mix(kHorizon, kZenith, pow(h, 0.85));
    float sd = max(dot(dir, kSunDir), 0.0);
    col += kSunGlow * pow(sd, 14.0) * 0.22;
    col *= 1.0 + (0.011 * sin(dir.x * 4.1 + dir.y * 6.3) * sin(dir.y * 3.7 - dir.x * 2.3)
                + 0.006 * sin(dir.x * 11.0) * sin(dir.y * 9.0));
    return col;
}

vec4 deck(vec3 dir, vec2 p0, vec2 drift) {
    float wx = fbm3(p0 * 0.9 + drift * 0.5 + 31.0);
    vec2 warp = (vec2(fbm3(p0 * 1.4 + drift + 3.0), fbm3(p0 * 1.4 + drift + 17.0)) - 0.5) * 0.25;
    vec2 p = p0 + warp;
    float th = 0.562 + (0.5 - wx) * 0.08;
    float field = cloudField(p, drift, 0.0);
    float dens = smoothstep(th, th + 0.14, field);
    float fringe = smoothstep(th - 0.08, th, field) * (1.0 - dens);
    float thick = smoothstep(th, th + 0.40, field);
    vec2 lightDir = normalize(kSunDir.xz + vec2(0.0001, 0.0));
    float towardBig = cloudField(p + lightDir * 0.045, drift, 0.0) - field;
    float lobeHere = fbm3(p * 11.0 + drift * 0.6 + 9.0);
    float towardLobe = fbm3((p + lightDir * 0.012) * 11.0 + drift * 0.6 + 9.0) - lobeHere;
    float bigShade = smoothstep(0.08, -0.08, towardBig);
    float lobeShade = smoothstep(0.10, -0.10, towardLobe);
    float lam = 0.10 + 0.90 * (0.45 * bigShade + 0.55 * lobeShade);
    float above = cloudField(p * 0.96, drift, 0.0);
    float belly = exp(-max(above - th, 0.0) * 7.0);
    float light = lam * belly;
    float topEdge = smoothstep(0.0, 0.10, th - above) * dens;
    float hue = smoothstep(0.36, 0.64, fbm3(p0 * 0.55 + drift * 0.3 + 57.0));
    vec3 lit = mix(vec3(1.28, 1.28, 1.32), vec3(1.18, 1.20, 1.26), hue);
    vec3 mid = mix(vec3(0.96, 0.97, 1.02), vec3(0.90, 0.93, 1.00), hue);
    vec3 shade = vec3(0.68, 0.72, 0.80);
    float lk = mix(0.5, clamp(light, 0.0, 1.0), 0.55);
    vec3 col = lk < 0.5 ? mix(shade, mid, lk * 2.0) : mix(mid, lit, lk * 2.0 - 1.0);
    col += lit * topEdge * 0.12;
    col *= mix(1.0, 0.94, thick);
    float glow = pow(max(dot(dir, kSunDir), 0.0), 24.0);
    col += vec3(1.0) * glow * 0.18 * (1.0 - thick * 0.8);
    return vec4(aces(col), clamp(dens + fringe, 0.0, 1.0));
}

vec3 rayDir(vec2 frag) {
    vec2 uv = frag / uRes;
    vec2 ndc = uv * 2.0 - 1.0;
    vec3 forward = normalize(vec3(0.0, 0.55, -3.0));
    vec3 right = normalize(cross(forward, vec3(0.0, 1.0, 0.0)));
    vec3 up = cross(right, forward);
    float aspect = uRes.x / uRes.y;
    return normalize(forward + right * ndc.x * kFov * aspect + up * ndc.y * kFov);
}

void main() {
    vec3 dir = rayDir(gl_FragCoord.xy);
    vec2 p0 = dir.xz / (max(dir.y, 0.0) + 0.5) * 0.26;
    vec2 drift = uTime * vec2(0.005, 0.002);
    vec4 cloud = deck(dir, p0, drift);
    float alpha = cloud.a * smoothstep(-0.03, 0.10, dir.y);
    fragColor = vec4(mix(skyColor(dir), cloud.rgb, alpha), 1.0);
}
"""

private const val BLUR_FRAG = """
#version 300 es
precision highp float;
uniform sampler2D uSrc;
uniform vec2 uTexel;
uniform float uRadius;
uniform vec2 uRes;
out vec4 fragColor;

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

void main() {
    vec2 uv = gl_FragCoord.xy / uRes;
    float angle = hash12(uv * 517.3) * 6.2831853;
    float c = cos(angle);
    float s = sin(angle);
    mat2 rot = mat2(c, s, -s, c);
    vec3 accum = vec3(0.0);
    const int taps = 24;
    for (int i = 0; i < taps; i++) {
        float n = float(i) + 0.5;
        float ang = n * 2.399963;
        float rad = sqrt(n / float(taps));
        vec2 offset = (rot * vec2(cos(ang), sin(ang))) * rad * uRadius;
        accum += texture(uSrc, uv + offset * uTexel).rgb;
    }
    fragColor = vec4(accum / float(taps), 1.0);
}
"""
