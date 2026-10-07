package edu.feutech.redu.ui

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlinx.coroutines.awaitCancellation

private const val TAG = "HomeHalftone"
private const val FRAME_NANOS = 125_000_000L
private const val HARDWARE_STALL_NANOS = 500_000_000L
private const val MIN_SHORT_EDGE = 576
private const val MAX_LONG_EDGE = 1280
/**
 * Floor of the home-tab dot screen. Matches the dark margins of the reference
 * so the scaffold and the first frame sit in the same navy.
 */
internal val HomeField = Color(0xFF020710)

private var activePlayback by mutableStateOf<HalftonePlayback?>(null)

/**
 * Fine square dots on a navy floor, lit by one diagonal wash.
 *
 * The frame is rendered on a background GL thread into a bitmap, then drawn
 * with the rest of the Compose layer. A live surface would recomposite on
 * every scroll frame. New frames pause for the whole gesture. The UI thread
 * only swaps in the finished bitmap.
 *
 * The last bitmap is kept so leaving Home and coming back does not flash the
 * flat floor. That floor is only the first frame, and the gap before GL is ready.
 * The shader clock is kept with that bitmap. A new visit would otherwise start
 * at zero and snap the ribbon away from the pose still on screen.
 */
@Composable
internal fun HomeHalftoneBackdrop(modifier: Modifier = Modifier) {
    if (LocalInspectionMode.current) {
        Canvas(modifier) { drawRect(HomeField) }
        return
    }
    val appContext = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val gate = remember(appContext) {
        HalftonePlayback(
            appContext,
            active = lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED),
        )
    }
    DisposableEffect(gate) {
        activePlayback = gate
        onDispose {
            if (activePlayback === gate) activePlayback = null
        }
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
        val renderer = HalftoneRenderer(gate)
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
            .onGloballyPositioned { coords ->
                val pos = coords.positionInWindow()
                gate.setBackdropPlacement(pos.x, pos.y, coords.size.width, coords.size.height)
            }
            // Own layer so a new frame does not repaint the list above it.
            .graphicsLayer()
            .homeHalftone(gate),
    )
}

/**
 * Home panel whose fill is a blurred slice of the halftone, under a dark veil.
 * Without a backdrop playback, or before the first blur frame, it matches [ReduSection].
 */
@Composable
internal fun HomeFrostedSection(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val playback = activePlayback
    if (playback == null) {
        ReduSection(modifier, content)
        return
    }
    val solid = MaterialTheme.colorScheme.surfaceContainerLow
    val placement = remember { FrostPlacement() }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp,
    ) {
        Box {
            Box(
                Modifier
                    .matchParentSize()
                    .onGloballyPositioned { coords ->
                        val pos = coords.positionInWindow()
                        placement.move(pos.x, pos.y)
                    }
                    .graphicsLayer { clip = true }
                    .homeFrost(
                        playback = playback,
                        placement = placement,
                        veil = solid.copy(alpha = 0.46f),
                        scrim = Color.White.copy(alpha = 0.06f),
                        fallback = solid,
                    ),
            )
            Column(content = content)
        }
    }
}

private fun Modifier.homeHalftone(playback: HalftonePlayback): Modifier =
    this.then(HalftoneElement(playback))

private class HalftoneElement(
    val playback: HalftonePlayback,
) : ModifierNodeElement<HalftoneNode>() {
    override fun create(): HalftoneNode = HalftoneNode(playback)

    override fun update(node: HalftoneNode) {
        if (node.playback !== playback) {
            node.playback.detach(node)
            node.playback = playback
            if (node.isAttached) node.playback.attach(node)
        }
    }

    override fun equals(other: Any?): Boolean = other is HalftoneElement && other.playback === playback

    override fun hashCode(): Int = System.identityHashCode(playback)
}

private class HalftoneNode(
    var playback: HalftonePlayback,
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
        drawHalftoneFrame()
        drawContent()
    }

    fun refresh() {
        if (isAttached) invalidateDraw()
    }

    private fun DrawScope.drawHalftoneFrame() {
        val bmp = retainedHalftone
        if (bmp == null || bmp.isRecycled) {
            drawRect(HomeField)
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

private var retainedHalftone: Bitmap? = null
private var retainedBlur: Bitmap? = null

/** Shader seconds of [retainedHalftone]. Survives leaving the Home tab. */
@Volatile
private var retainedHalftoneTime = 0f

private class FrostPlacement {
    var x: Float = 0f
    var y: Float = 0f
    var placed: Boolean = false
    var onMove: (() -> Unit)? = null

    fun move(x: Float, y: Float) {
        if (placed && this.x == x && this.y == y) return
        this.x = x
        this.y = y
        placed = true
        onMove?.invoke()
    }
}

private fun Modifier.homeFrost(
    playback: HalftonePlayback,
    placement: FrostPlacement,
    veil: Color,
    scrim: Color,
    fallback: Color,
): Modifier = this.then(FrostElement(playback, placement, veil, scrim, fallback))

private class FrostElement(
    val playback: HalftonePlayback,
    val placement: FrostPlacement,
    val veil: Color,
    val scrim: Color,
    val fallback: Color,
) : ModifierNodeElement<FrostNode>() {
    override fun create(): FrostNode = FrostNode(playback, placement, veil, scrim, fallback)

    override fun update(node: FrostNode) {
        node.veil = veil
        node.scrim = scrim
        node.fallback = fallback
        if (node.playback !== playback) {
            node.playback.detachFrost(node)
            node.playback = playback
            if (node.isAttached) node.playback.attachFrost(node)
        }
        if (node.placement !== placement) {
            node.placement.onMove = null
            node.placement = placement
            if (node.isAttached) node.placement.onMove = node::refresh
        }
    }

    override fun equals(other: Any?): Boolean =
        other is FrostElement &&
            other.playback === playback &&
            other.placement === placement &&
            other.veil == veil &&
            other.scrim == scrim &&
            other.fallback == fallback

    override fun hashCode(): Int =
        System.identityHashCode(playback) xor
            System.identityHashCode(placement) xor
            veil.hashCode() xor
            scrim.hashCode() xor
            fallback.hashCode()
}

private class FrostNode(
    var playback: HalftonePlayback,
    var placement: FrostPlacement,
    var veil: Color,
    var scrim: Color,
    var fallback: Color,
) : Modifier.Node(), DrawModifierNode {
    private var shadeBitmap: Bitmap? = null
    private var shade: ImageBitmap? = null

    override fun onAttach() {
        placement.onMove = ::refresh
        playback.attachFrost(this)
    }

    override fun onDetach() {
        placement.onMove = null
        playback.detachFrost(this)
    }

    override fun ContentDrawScope.draw() {
        drawFrost()
        drawContent()
    }

    fun refresh() {
        if (isAttached) invalidateDraw()
    }

    private fun DrawScope.drawFrost() {
        val bmp = retainedBlur
        val backdropWidth = playback.backdropWidth
        val backdropHeight = playback.backdropHeight
        if (
            bmp == null ||
            bmp.isRecycled ||
            !placement.placed ||
            backdropWidth < 2 ||
            backdropHeight < 2
        ) {
            drawRect(fallback)
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
        val dx = placement.x - playback.backdropX
        val dy = placement.y - playback.backdropY
        val window = frostWindow(
            dx = dx,
            dy = dy,
            panelWidth = size.width,
            panelHeight = size.height,
            backdropWidth = backdropWidth,
            backdropHeight = backdropHeight,
            bitmapWidth = image.width,
            bitmapHeight = image.height,
        )
        if (window != null) {
            drawImage(
                image = image,
                srcOffset = IntOffset(window.srcOffsetX, window.srcOffsetY),
                srcSize = IntSize(window.srcWidth, window.srcHeight),
                dstOffset = IntOffset(window.dstOffsetX, window.dstOffsetY),
                dstSize = IntSize(window.dstWidth, window.dstHeight),
                filterQuality = FilterQuality.Low,
            )
        }
        drawRect(veil)
        drawRect(scrim)
    }
}

private class HalftonePlayback(
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

    var painter: HalftoneNode? = null
        private set

    var backdropX: Float = 0f
        private set

    var backdropY: Float = 0f
        private set

    var backdropWidth: Int = 0
        private set

    var backdropHeight: Int = 0
        private set

    private val frostPainters = ArrayList<FrostNode>()

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

    fun attach(node: HalftoneNode) {
        painter = node
        if (retainedHalftone != null) node.refresh()
    }

    fun detach(node: HalftoneNode) {
        if (painter === node) painter = null
    }

    fun attachFrost(node: FrostNode) {
        if (!frostPainters.contains(node)) frostPainters.add(node)
        node.refresh()
    }

    fun detachFrost(node: FrostNode) {
        frostPainters.remove(node)
    }

    fun setBackdropPlacement(x: Float, y: Float, width: Int, height: Int) {
        if (backdropX == x && backdropY == y && backdropWidth == width && backdropHeight == height) return
        backdropX = x
        backdropY = y
        backdropWidth = width
        backdropHeight = height
        refreshFrost()
    }

    fun refreshFrames() {
        painter?.refresh()
        refreshFrost()
    }

    private fun refreshFrost() {
        for (index in frostPainters.indices) {
            frostPainters[index].refresh()
        }
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
     * Waits while Home is paused, mid-scroll, or already painted for reduced motion.
     * [DrawableSize.resumedFromHold] is set when this call blocked, so the caller
     * can drop the wall-clock gap and keep the pose already on screen.
     */
    fun awaitDrawableSize(hasFrame: Boolean, stillKey: Long): DrawableSize? {
        var waited = false
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
                if (!paused) return DrawableSize(viewWidth, viewHeight, waited)
                waited = true
                wake.await(400, TimeUnit.MILLISECONDS)
            }
        }
        return null
    }

    fun sizeKey(): Long = (viewWidth.toLong() shl 32) or (viewHeight.toLong() and 0xffffffffL)
}

private class DrawableSize(
    val width: Int,
    val height: Int,
    val resumedFromHold: Boolean,
)

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

private class HalftoneRenderer(
    private val gate: HalftonePlayback,
) {
    private val publish = MainThreadFramePublish(Handler(Looper.getMainLooper()))
    private var worker: Thread? = null
    private val sharpSlot = BitmapSlot()
    private val blurSlot = BitmapSlot()
    private val sharpRead = ColorReadback()
    private val blurRead = ColorReadback()
    private val readFence = ReadbackFence()
    private val frameBlit = FrameBlit()
    private var packedSharp: IntArray? = null
    private var packedBlur: IntArray? = null
    private var sharpTarget: ColorTarget? = null
    private var tempTarget: ColorTarget? = null
    private var blurTarget: ColorTarget? = null
    private var sharpStream: GpuFrameStream? = null
    private var blurStream: GpuFrameStream? = null
    private var stagedSharp: Bitmap? = null
    private var stagedBlur: Bitmap? = null
    private var transport = FrameTransport.Unset
    private var pending = false
    private var pendingAt = 0L
    private var pendingTime = 0f
    private var readWidth = 0
    private var readHeight = 0

    @Volatile private var frameReady = false

    fun start() {
        if (worker != null) return
        worker = Thread(::loop, "home-halftone").also {
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
            var program = 0
            var blur = 0
            var timeLoc = -1
            var resLoc = -1
            var blurSrc = -1
            var blurInvRes = -1
            var blurDirection = -1
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
                var time = retainedHalftoneTime
                while (gate.running) {
                    val drawable = gate.awaitDrawableSize(hasFrame, stillKey) ?: break
                    if (drawable.resumedFromHold) lastNanos = 0L
                    val (bufferWidth, bufferHeight) = halftoneBufferSize(drawable.width, drawable.height)
                    if (bufferWidth < 2 || bufferHeight < 2) continue
                    if (!egl.resize(bufferWidth, bufferHeight)) {
                        throw IllegalStateException("Halftone surface resize failed")
                    }
                    if (program == 0 || blur == 0) {
                        program = linkProgram(FULLSCREEN_VERT, HALFTONE_FRAG)
                        blur = linkProgram(FULLSCREEN_VERT, BLUR_FRAG)
                        if (program == 0 || blur == 0) throw IllegalStateException("Halftone shader failed to link")
                        timeLoc = GLES30.glGetUniformLocation(program, "uTime")
                        resLoc = GLES30.glGetUniformLocation(program, "uRes")
                        blurSrc = GLES30.glGetUniformLocation(blur, "uSrc")
                        blurInvRes = GLES30.glGetUniformLocation(blur, "uInvRes")
                        blurDirection = GLES30.glGetUniformLocation(blur, "uDirection")
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
                                    releaseHardwareStreams()
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
                    val dt = if (lastNanos == 0L) {
                        0f
                    } else {
                        ((now - lastNanos) / 1_000_000_000f).coerceIn(0f, 0.5f)
                    }
                    lastNanos = now
                    if (!gate.reducedMotion) time += dt
                    val drawTime = if (gate.reducedMotion) 0f else time
                    val sharp = sharpTarget ?: ColorTarget().also { sharpTarget = it }
                    val temp = tempTarget ?: ColorTarget().also { tempTarget = it }
                    val blurred = blurTarget ?: ColorTarget().also { blurTarget = it }
                    if (!drawScene(
                            sharp,
                            temp,
                            blurred,
                            program,
                            blur,
                            timeLoc,
                            resLoc,
                            blurSrc,
                            blurInvRes,
                            blurDirection,
                            bufferWidth,
                            bufferHeight,
                            drawTime,
                        )
                    ) {
                        throw IllegalStateException("Halftone frame was incomplete")
                    }
                    if (gate.scrolling && hasFrame) {
                        if (!gate.reducedMotion) time -= dt
                        lastNanos = 0L
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
                        releaseHardwareStreams()
                        transport = FrameTransport.Software
                    }
                    if (transport == FrameTransport.Hardware &&
                        !blitHardware(sharp.texture, blurred.texture, bufferWidth, bufferHeight)
                    ) {
                        Log.w(TAG, "Hardware frame publish failed; using bitmaps")
                        releaseHardwareStreams()
                        transport = FrameTransport.Software
                    }
                    if (transport == FrameTransport.Software) {
                        sharpRead.queue(sharp.fbo, bufferWidth, bufferHeight)
                        blurRead.queue(blurred.fbo, bufferWidth, bufferHeight)
                        readFence.arm()
                        readWidth = bufferWidth
                        readHeight = bufferHeight
                    }
                    pending = true
                    pendingTime = drawTime
                    pendingAt = System.nanoTime()
                    gate.pace(frameStart)
                }
            } catch (_: InterruptedException) {
                break
            } catch (error: Exception) {
                if (gate.running) {
                    failures++
                    Log.e(TAG, "Halftone backdrop failed", error)
                    gate.await(400)
                }
            } finally {
                if (egl.isCurrent()) {
                    frameBlit.delete()
                    sharpTarget?.delete()
                    tempTarget?.delete()
                    blurTarget?.delete()
                    sharpRead.delete()
                    blurRead.delete()
                    readFence.clear()
                    if (program != 0) GLES30.glDeleteProgram(program)
                    if (blur != 0) GLES30.glDeleteProgram(blur)
                }
                releaseHardwareStreams()
                sharpTarget = null
                tempTarget = null
                blurTarget = null
                transport = FrameTransport.Unset
                pending = false
                sharpSlot.releaseSpare()
                blurSlot.releaseSpare()
                egl.release()
            }
        }
    }

    private fun drawScene(
        sharp: ColorTarget,
        temp: ColorTarget,
        blurred: ColorTarget,
        program: Int,
        blur: Int,
        timeLoc: Int,
        resLoc: Int,
        blurSrc: Int,
        blurInvRes: Int,
        blurDirection: Int,
        width: Int,
        height: Int,
        time: Float,
    ): Boolean {
        drainGlErrors()
        sharp.ensure(width, height)
        temp.ensure(width, height)
        blurred.ensure(width, height)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, sharp.fbo)
        GLES30.glViewport(0, 0, width, height)
        GLES30.glUseProgram(program)
        GLES30.glUniform1f(timeLoc, time)
        GLES30.glUniform2f(resLoc, width.toFloat(), height.toFloat())
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)

        blurPass(temp.fbo, sharp.texture, blur, blurSrc, blurInvRes, blurDirection, 1f, 0f, width, height)
        blurPass(blurred.fbo, temp.texture, blur, blurSrc, blurInvRes, blurDirection, 0f, 1f, width, height)
        return drainGlErrors() == GLES30.GL_NO_ERROR
    }

    private fun blurPass(
        targetFbo: Int,
        source: Int,
        program: Int,
        srcLoc: Int,
        invResLoc: Int,
        directionLoc: Int,
        directionX: Float,
        directionY: Float,
        width: Int,
        height: Int,
    ) {
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, targetFbo)
        GLES30.glViewport(0, 0, width, height)
        GLES30.glUseProgram(program)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, source)
        GLES30.glUniform1i(srcLoc, 0)
        GLES30.glUniform2f(invResLoc, 1f / width, 1f / height)
        GLES30.glUniform2f(directionLoc, directionX, directionY)
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
    }

    private fun drain(width: Int, height: Int): FrameDrain {
        return if (transport == FrameTransport.Hardware) drainHardware() else drainSoftware(width, height)
    }

    private fun drainSoftware(width: Int, height: Int): FrameDrain {
        if (!readFence.signaled()) return FrameDrain.NotReady
        val dots = sharpRead.take(packedSharp) { packedSharp = it }
        val frost = blurRead.take(packedBlur) { packedBlur = it }
        readFence.clear()
        if (dots == null || frost == null || readWidth != width || readHeight != height) return FrameDrain.Dropped
        return if (presentSoftware(dots, frost, readWidth, readHeight)) FrameDrain.Published else FrameDrain.Dropped
    }

    private fun presentSoftware(dots: IntArray, frost: IntArray, width: Int, height: Int): Boolean {
        val sharpBitmap = sharpSlot.obtain(width, height)
        val blurBitmap = blurSlot.obtain(width, height)
        sharpBitmap.setPixels(dots, 0, width, 0, 0, width, height)
        blurBitmap.setPixels(frost, 0, width, 0, 0, width, height)
        val frameTime = pendingTime
        val delivered = publish.publish({ gate.running }) {
            retainedHalftone = sharpBitmap
            retainedBlur = blurBitmap
            retainedHalftoneTime = frameTime
            gate.refreshFrames()
        }
        if (!delivered) {
            Log.w(TAG, "Dropped a halftone frame because the UI thread was busy")
            return false
        }
        sharpSlot.commit(sharpBitmap)
        blurSlot.commit(blurBitmap)
        return true
    }

    private fun drainHardware(): FrameDrain {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return FrameDrain.Dropped
        return drainHardware29()
    }

    @RequiresApi(29)
    private fun drainHardware29(): FrameDrain {
        if (stagedSharp == null) stagedSharp = sharpStream?.acquireCompleted()
        if (stagedBlur == null) stagedBlur = blurStream?.acquireCompleted()
        val dots = stagedSharp ?: return FrameDrain.NotReady
        val frost = stagedBlur ?: return FrameDrain.NotReady
        val frameTime = pendingTime
        val delivered = publish.publish({ gate.running }) {
            retainedHalftone = dots
            retainedBlur = frost
            retainedHalftoneTime = frameTime
            gate.refreshFrames()
        }
        stagedSharp = null
        stagedBlur = null
        if (!delivered) {
            sharpStream?.dropNewest()
            blurStream?.dropNewest()
            Log.w(TAG, "Dropped a halftone frame because the UI thread was busy")
            return FrameDrain.Dropped
        }
        sharpStream?.trimHeld()
        blurStream?.trimHeld()
        return FrameDrain.Published
    }

    private fun openHardware(egl: BackdropEgl, width: Int, height: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !egl.supportsWindow) return false
        return openHardware29(egl, width, height)
    }

    @RequiresApi(29)
    private fun openHardware29(egl: BackdropEgl, width: Int, height: Int): Boolean {
        if (sharpStream?.sameSize(width, height) == false) releaseHardwareStreams()
        val sharp = sharpStream ?: GpuFrameStream(egl).also { sharpStream = it }
        val frost = blurStream ?: GpuFrameStream(egl).also { blurStream = it }
        if (!sharp.ensure(width, height) || !frost.ensure(width, height)) {
            releaseHardwareStreams()
            return false
        }
        return true
    }

    private fun blitHardware(sharpTexture: Int, blurTexture: Int, width: Int, height: Int): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return false
        return blitHardware29(sharpTexture, blurTexture, width, height)
    }

    @RequiresApi(29)
    private fun blitHardware29(sharpTexture: Int, blurTexture: Int, width: Int, height: Int): Boolean {
        val sharp = sharpStream ?: return false
        val frost = blurStream ?: return false
        return sharp.blit(frameBlit, sharpTexture, width, height) &&
            frost.blit(frameBlit, blurTexture, width, height)
    }

    private fun releaseHardwareStreams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) releaseHardwareStreams29()
        sharpStream = null
        blurStream = null
        stagedSharp = null
        stagedBlur = null
    }

    @RequiresApi(29)
    private fun releaseHardwareStreams29() {
        val sharpCopy = sharpStream?.copyDisplayed()
        val blurCopy = blurStream?.copyDisplayed()
        if (sharpCopy != null && blurCopy != null) {
            publish.replace {
                retainedHalftone = sharpCopy
                retainedBlur = blurCopy
                gate.refreshFrames()
            }
        }
        sharpStream?.release()
        blurStream?.release()
    }
}

private class ColorTarget {
    var fbo: Int = 0
        private set
    var texture: Int = 0
        private set
    private var width: Int = 0
    private var height: Int = 0

    fun ensure(nextWidth: Int, nextHeight: Int) {
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
            throw IllegalStateException("Halftone framebuffer incomplete: $status")
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

private fun halftoneBufferSize(viewWidth: Int, viewHeight: Int): Pair<Int, Int> {
    if (viewWidth < 2 || viewHeight < 2) return 0 to 0
    val longSide = max(viewWidth, viewHeight).toFloat()
    val shortSide = min(viewWidth, viewHeight).toFloat()
    var scale = 1f
    if (shortSide < MIN_SHORT_EDGE) scale = MIN_SHORT_EDGE / shortSide
    if (longSide * scale > MAX_LONG_EDGE) scale = MAX_LONG_EDGE / longSide
    return (viewWidth * scale).roundToInt().coerceAtLeast(2) to
        (viewHeight * scale).roundToInt().coerceAtLeast(2)
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

private const val HALFTONE_FRAG = """
#version 300 es
precision mediump float;
uniform float uTime;
uniform vec2 uRes;
out vec4 fragColor;

const vec3 kFloor = vec3(0.008, 0.027, 0.063);
const vec3 kWash = vec3(0.239, 0.231, 0.262);
const vec3 kDot = vec3(0.210, 0.210, 0.210);
const float kColumns = 115.0;
const float kRadius = 0.30;

float wave(vec2 uv, float t) {
    // Horizontal sine ribbon. The curve scrolls sideways and repeats, so the
    // shape leaving the right edge is the one entering the left.
    float along = uv.x - t * 0.035;
    float ridge = 0.50 + 0.035 * sin(along * 6.2831853 * 1.5);
    float d = uv.y - ridge;
    return exp(-d * d / (2.0 * 0.10 * 0.10));
}

void main() {
    vec2 frag = gl_FragCoord.xy;
    vec2 uv = vec2(frag.x / uRes.x, 1.0 - frag.y / uRes.y);
    float cell = max(uRes.x / kColumns, 1.0);
    vec2 gv = fract(frag / cell) - 0.5;
    float dist = length(gv);
    float aa = min(fwidth(dist), 1.0 / cell);
    float dotMask = 1.0 - smoothstep(kRadius - aa, kRadius + aa, dist);
    float light = wave(uv, uTime);
    fragColor = vec4(kFloor + (kWash + kDot * dotMask) * light, 1.0);
}
"""

private const val BLUR_FRAG = """
#version 300 es
precision mediump float;
uniform sampler2D uSrc;
uniform vec2 uInvRes;
uniform vec2 uDirection;
out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy * uInvRes;
    // Six steps of 4px cover ±24px. Weights are a gaussian with sigma 8.
    vec2 step = uDirection * uInvRes * 4.0;
    vec3 acc = texture(uSrc, uv).rgb * 0.200;
    acc += (texture(uSrc, uv + step).rgb + texture(uSrc, uv - step).rgb) * 0.176;
    acc += (texture(uSrc, uv + step * 2.0).rgb + texture(uSrc, uv - step * 2.0).rgb) * 0.121;
    acc += (texture(uSrc, uv + step * 3.0).rgb + texture(uSrc, uv - step * 3.0).rgb) * 0.065;
    acc += (texture(uSrc, uv + step * 4.0).rgb + texture(uSrc, uv - step * 4.0).rgb) * 0.027;
    acc += (texture(uSrc, uv + step * 5.0).rgb + texture(uSrc, uv - step * 5.0).rgb) * 0.009;
    acc += (texture(uSrc, uv + step * 6.0).rgb + texture(uSrc, uv - step * 6.0).rgb) * 0.002;
    fragColor = vec4(acc, 1.0);
}
"""
