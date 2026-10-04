package edu.feutech.redu.ui

import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.graphics.PixelFormat
import android.hardware.HardwareBuffer
import android.media.Image
import android.media.ImageReader
import android.opengl.EGL14
import android.opengl.EGLConfig
import android.opengl.EGLContext
import android.opengl.EGLDisplay
import android.opengl.EGLSurface
import android.opengl.GLES30
import android.os.Handler
import android.util.Log
import android.view.Choreographer
import android.view.Surface
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.ArrayDeque
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private const val EGL_OPENGL_ES3_BIT = 0x40

/**
 * EGL context shared by the home backdrops. The pbuffer keeps the context
 * current for offscreen passes. On API 29+ a window surface can present
 * straight into an [ImageReader] so the UI thread never copies pixels.
 */
internal class BackdropEgl {
    private var display: EGLDisplay = EGL14.EGL_NO_DISPLAY
    private var context: EGLContext = EGL14.EGL_NO_CONTEXT
    private var config: EGLConfig? = null
    private var pbuffer: EGLSurface = EGL14.EGL_NO_SURFACE
    private var current: EGLSurface = EGL14.EGL_NO_SURFACE
    private var width = 0
    private var height = 0

    var supportsWindow: Boolean = false
        private set

    fun init(): Boolean {
        display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY)
        if (display == EGL14.EGL_NO_DISPLAY) return false
        val version = IntArray(2)
        if (!EGL14.eglInitialize(display, version, 0, version, 1)) return false
        val chosen = chooseConfig(EGL14.EGL_PBUFFER_BIT or EGL14.EGL_WINDOW_BIT)
            ?: chooseConfig(EGL14.EGL_PBUFFER_BIT)
            ?: return false
        config = chosen
        val surfaceType = IntArray(1)
        EGL14.eglGetConfigAttrib(display, chosen, EGL14.EGL_SURFACE_TYPE, surfaceType, 0)
        supportsWindow = surfaceType[0] and EGL14.EGL_WINDOW_BIT != 0
        val contextAttribs = intArrayOf(EGL14.EGL_CONTEXT_CLIENT_VERSION, 3, EGL14.EGL_NONE)
        context = EGL14.eglCreateContext(display, chosen, EGL14.EGL_NO_CONTEXT, contextAttribs, 0)
        return context != EGL14.EGL_NO_CONTEXT
    }

    fun resize(nextWidth: Int, nextHeight: Int): Boolean {
        if (pbuffer != EGL14.EGL_NO_SURFACE && nextWidth == width && nextHeight == height) {
            if (current == pbuffer) return true
            return makeCurrent(pbuffer)
        }
        if (pbuffer != EGL14.EGL_NO_SURFACE) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            EGL14.eglDestroySurface(display, pbuffer)
            pbuffer = EGL14.EGL_NO_SURFACE
            current = EGL14.EGL_NO_SURFACE
        }
        val surfaceAttribs = intArrayOf(
            EGL14.EGL_WIDTH, nextWidth,
            EGL14.EGL_HEIGHT, nextHeight,
            EGL14.EGL_NONE,
        )
        pbuffer = EGL14.eglCreatePbufferSurface(display, config, surfaceAttribs, 0)
        if (pbuffer == EGL14.EGL_NO_SURFACE) return false
        if (!makeCurrent(pbuffer)) return false
        width = nextWidth
        height = nextHeight
        return true
    }

    fun isCurrent(): Boolean = current != EGL14.EGL_NO_SURFACE

    fun createWindow(surface: Surface): EGLSurface {
        val chosen = config ?: return EGL14.EGL_NO_SURFACE
        val attribs = intArrayOf(EGL14.EGL_NONE)
        return EGL14.eglCreateWindowSurface(display, chosen, surface, attribs, 0)
    }

    fun destroyWindow(surface: EGLSurface) {
        if (surface == EGL14.EGL_NO_SURFACE) return
        if (current == surface) {
            EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
            current = EGL14.EGL_NO_SURFACE
        }
        EGL14.eglDestroySurface(display, surface)
    }

    fun makeCurrent(surface: EGLSurface): Boolean {
        if (surface == EGL14.EGL_NO_SURFACE) return false
        val ok = EGL14.eglMakeCurrent(display, surface, surface, context)
        if (!ok) return false
        current = surface
        return true
    }

    fun swap(surface: EGLSurface): Boolean {
        if (surface == EGL14.EGL_NO_SURFACE) return false
        return EGL14.eglSwapBuffers(display, surface)
    }

    fun release() {
        if (display == EGL14.EGL_NO_DISPLAY) return
        EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)
        if (pbuffer != EGL14.EGL_NO_SURFACE) EGL14.eglDestroySurface(display, pbuffer)
        if (context != EGL14.EGL_NO_CONTEXT) EGL14.eglDestroyContext(display, context)
        EGL14.eglTerminate(display)
        display = EGL14.EGL_NO_DISPLAY
        context = EGL14.EGL_NO_CONTEXT
        config = null
        pbuffer = EGL14.EGL_NO_SURFACE
        current = EGL14.EGL_NO_SURFACE
        supportsWindow = false
    }

    private fun chooseConfig(surfaceType: Int): EGLConfig? {
        val attribs = intArrayOf(
            EGL14.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
            EGL14.EGL_SURFACE_TYPE, surfaceType,
            EGL14.EGL_RED_SIZE, 8,
            EGL14.EGL_GREEN_SIZE, 8,
            EGL14.EGL_BLUE_SIZE, 8,
            EGL14.EGL_ALPHA_SIZE, 8,
            EGL14.EGL_NONE,
        )
        val configs = arrayOfNulls<EGLConfig>(1)
        val count = IntArray(1)
        if (!EGL14.eglChooseConfig(display, attribs, 0, configs, 0, 1, count, 0) || count[0] == 0) {
            return null
        }
        return configs[0]
    }
}

/**
 * One presented stream. The GPU renders into an [ImageReader] buffer, and the
 * next tick wraps that buffer as a hardware bitmap. The Image stays open until
 * a later frame replaces it, so the producer cannot overwrite the bitmap on screen.
 */
@RequiresApi(29)
internal class GpuFrameStream(
    private val egl: BackdropEgl,
) {
    private var reader: ImageReader? = null
    private var window: EGLSurface = EGL14.EGL_NO_SURFACE
    private var width = 0
    private var height = 0
    private val held = ArrayDeque<HeldImage>()
    private var failed = false

    fun sameSize(nextWidth: Int, nextHeight: Int): Boolean =
        reader != null && width == nextWidth && height == nextHeight && window != EGL14.EGL_NO_SURFACE

    fun ensure(nextWidth: Int, nextHeight: Int): Boolean {
        if (failed || !egl.supportsWindow) return false
        if (reader != null && width == nextWidth && height == nextHeight && window != EGL14.EGL_NO_SURFACE) {
            return true
        }
        closeHeld()
        destroySurface()
        return try {
            val usage = HardwareBuffer.USAGE_GPU_COLOR_OUTPUT or HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE
            val created = ImageReader.newInstance(
                nextWidth,
                nextHeight,
                PixelFormat.RGBA_8888,
                IMAGE_SLOTS,
                usage,
            )
            val surface = egl.createWindow(created.surface)
            if (surface == EGL14.EGL_NO_SURFACE) {
                created.close()
                failed = true
                false
            } else {
                reader = created
                window = surface
                width = nextWidth
                height = nextHeight
                true
            }
        } catch (error: RuntimeException) {
            Log.w(TAG, "Hardware frame target unavailable", error)
            failed = true
            false
        }
    }

    fun blit(frame: FrameBlit, texture: Int, frameWidth: Int, frameHeight: Int): Boolean {
        if (window == EGL14.EGL_NO_SURFACE) return false
        if (!egl.makeCurrent(window)) return false
        for (ignored in 0 until 8) {
            if (GLES30.glGetError() == GLES30.GL_NO_ERROR) break
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0)
        GLES30.glViewport(0, 0, frameWidth, frameHeight)
        GLES30.glDisable(GLES30.GL_SCISSOR_TEST)
        frame.draw(texture, frameWidth, frameHeight)
        val swapped = egl.swap(window)
        val restored = egl.resize(frameWidth, frameHeight)
        return swapped && restored && GLES30.glGetError() == GLES30.GL_NO_ERROR
    }

    fun acquireCompleted(): Bitmap? {
        val image = reader?.acquireLatestImage() ?: return null
        val buffer = image.hardwareBuffer
        if (buffer == null) {
            image.close()
            return null
        }
        val bitmap = Bitmap.wrapHardwareBuffer(buffer, COLOR_SPACE)
        buffer.close()
        if (bitmap == null) {
            image.close()
            return null
        }
        held.addLast(HeldImage(image, bitmap))
        return bitmap
    }

    fun trimHeld() {
        while (held.size > 2) {
            held.removeFirst().image.close()
        }
    }

    /** Closes a frame the UI did not take, so the reader cannot fill and stall. */
    fun dropNewest() {
        if (held.isEmpty()) return
        held.removeLast().image.close()
    }

    fun copyDisplayed(): Bitmap? {
        val bitmap = held.lastOrNull()?.bitmap ?: return null
        if (bitmap.isRecycled) return null
        return try {
            if (bitmap.config == Bitmap.Config.HARDWARE) {
                bitmap.copy(Bitmap.Config.ARGB_8888, false)
            } else {
                bitmap
            }
        } catch (_: RuntimeException) {
            null
        }
    }

    fun release() {
        closeHeld()
        destroySurface()
        failed = false
        width = 0
        height = 0
    }

    private fun closeHeld() {
        while (held.isNotEmpty()) {
            held.removeFirst().image.close()
        }
    }

    private fun destroySurface() {
        if (window != EGL14.EGL_NO_SURFACE) {
            egl.destroyWindow(window)
            window = EGL14.EGL_NO_SURFACE
        }
        reader?.close()
        reader = null
    }

    private class HeldImage(val image: Image, val bitmap: Bitmap)

    private companion object {
        const val TAG = "BackdropFrames"
        const val IMAGE_SLOTS = 4
        val COLOR_SPACE: ColorSpace = ColorSpace.get(ColorSpace.Named.SRGB)
    }
}

/**
 * Copies a finished frame into a bitmap the UI thread is not drawing, then
 * asks the UI thread only to swap the reference.
 */
internal class BitmapSlot {
    private var spare: Bitmap? = null
    private var shown: Bitmap? = null

    fun obtain(width: Int, height: Int): Bitmap {
        val candidate = spare
        spare = null
        if (
            candidate != null &&
            !candidate.isRecycled &&
            candidate.width == width &&
            candidate.height == height &&
            candidate.config == Bitmap.Config.ARGB_8888
        ) {
            return candidate
        }
        if (candidate != null && !candidate.isRecycled) candidate.recycle()
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    }

    fun commit(bitmap: Bitmap) {
        val previous = shown
        shown = bitmap
        if (previous != null && previous !== bitmap) {
            if (spare != null && spare !== previous && spare?.isRecycled == false) spare?.recycle()
            spare = previous
        }
    }

    fun releaseSpare() {
        if (spare?.isRecycled == false) spare?.recycle()
        spare = null
        shown = null
    }
}

internal class MainThreadFramePublish(
    private val handler: Handler,
) {
    private val ticket = AtomicInteger(0)

    fun publish(running: () -> Boolean, apply: () -> Unit): Boolean {
        val mine = ticket.incrementAndGet()
        val latch = CountDownLatch(1)
        val applied = AtomicBoolean(false)
        handler.post {
            try {
                if (mine != ticket.get() || !running()) return@post
                apply()
                applied.set(true)
                Choreographer.getInstance().postFrameCallback { latch.countDown() }
            } finally {
                if (!applied.get()) latch.countDown()
            }
        }
        val completed = latch.await(750, TimeUnit.MILLISECONDS)
        if (!completed && ticket.get() == mine) ticket.incrementAndGet()
        return applied.get()
    }

    /** Publishes a shutdown snapshot even after playback has been closed. */
    fun replace(apply: () -> Unit) {
        val latch = CountDownLatch(1)
        handler.post {
            try {
                apply()
                Choreographer.getInstance().postFrameCallback {
                    Choreographer.getInstance().postFrameCallback { latch.countDown() }
                }
            } catch (_: RuntimeException) {
                latch.countDown()
            }
        }
        latch.await(750, TimeUnit.MILLISECONDS)
    }
}

/**
 * Asynchronous pixel readback. [queue] returns immediately; [take] maps the
 * buffer on a later tick so the GPU queue Compose is using is not stalled.
 */
internal class ColorReadback {
    private var pbo = 0
    private var capacity = 0
    private var fbo = 0
    private var width = 0
    private var height = 0
    private var pending = false
    private var scratch: ByteBuffer? = null

    fun queue(framebuffer: Int, frameWidth: Int, frameHeight: Int) {
        val bytes = frameWidth * frameHeight * 4
        if (bytes <= 0) return
        ensure(bytes)
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, framebuffer)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbo)
        GLES30.glReadPixels(0, 0, frameWidth, frameHeight, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, 0)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        fbo = framebuffer
        width = frameWidth
        height = frameHeight
        pending = true
    }

    fun take(existing: IntArray?, store: (IntArray) -> Unit): IntArray? {
        if (!pending) return null
        val mapped = map()
        val source = mapped ?: readSync()
        pending = false
        if (source == null) return null
        return packGlRgba(source, width, height, existing, store)
    }

    fun delete() {
        pending = false
        if (pbo != 0) {
            GLES30.glDeleteBuffers(1, intArrayOf(pbo), 0)
            pbo = 0
            capacity = 0
        }
        scratch = null
    }

    private fun ensure(bytes: Int) {
        if (pbo == 0) {
            val ids = IntArray(1)
            GLES30.glGenBuffers(1, ids, 0)
            pbo = ids[0]
        }
        if (bytes == capacity) return
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbo)
        GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, bytes, null, GLES30.GL_STREAM_READ)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        capacity = bytes
    }

    private fun map(): ByteBuffer? {
        if (pbo == 0) return null
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pbo)
        val mapped = GLES30.glMapBufferRange(
            GLES30.GL_PIXEL_PACK_BUFFER,
            0,
            width * height * 4,
            GLES30.GL_MAP_READ_BIT,
        ) as? ByteBuffer
        if (mapped == null) {
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
            return null
        }
        mapped.order(ByteOrder.nativeOrder())
        val bytes = width * height * 4
        mapped.position(0)
        if (mapped.capacity() < bytes) {
            GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
            return null
        }
        mapped.limit(bytes)
        val copy = scratch(bytes)
        copy.clear()
        copy.put(mapped)
        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        copy.flip()
        return copy
    }

    private fun readSync(): ByteBuffer? {
        if (width < 2 || height < 2) return null
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
        GLES30.glBindFramebuffer(GLES30.GL_READ_FRAMEBUFFER, fbo)
        val bytes = width * height * 4
        val buffer = scratch(bytes)
        buffer.clear()
        GLES30.glPixelStorei(GLES30.GL_PACK_ALIGNMENT, 4)
        GLES30.glReadPixels(0, 0, width, height, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, buffer)
        if (GLES30.glGetError() != GLES30.GL_NO_ERROR) return null
        buffer.rewind()
        return buffer
    }

    private fun scratch(bytes: Int): ByteBuffer {
        val existing = scratch
        if (existing != null && existing.capacity() >= bytes) return existing
        return ByteBuffer.allocateDirect(bytes).order(ByteOrder.nativeOrder()).also { scratch = it }
    }
}

internal class ReadbackFence {
    private var fence = 0L

    fun arm() {
        if (fence != 0L) GLES30.glDeleteSync(fence)
        fence = GLES30.glFenceSync(GLES30.GL_SYNC_GPU_COMMANDS_COMPLETE, 0)
        GLES30.glFlush()
    }

    fun signaled(): Boolean {
        if (fence == 0L) return false
        val status = GLES30.glClientWaitSync(fence, GLES30.GL_SYNC_FLUSH_COMMANDS_BIT, 0)
        return status == GLES30.GL_ALREADY_SIGNALED || status == GLES30.GL_CONDITION_SATISFIED
    }

    fun clear() {
        if (fence != 0L) GLES30.glDeleteSync(fence)
        fence = 0L
    }
}

/**
 * Copies a GL texture into a window surface.
 * [packGlRgba] puts GL's top row at bitmap row 0, and a window surface is
 * displayed the same way, so the copy is not flipped again.
 */
internal class FrameBlit {
    private var program = 0
    private var srcLoc = -1
    private var resLoc = -1

    fun draw(texture: Int, width: Int, height: Int) {
        if (program == 0 && !link()) return
        GLES30.glDisable(GLES30.GL_BLEND)
        GLES30.glDisable(GLES30.GL_DEPTH_TEST)
        GLES30.glUseProgram(program)
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0)
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texture)
        GLES30.glUniform1i(srcLoc, 0)
        GLES30.glUniform2f(resLoc, width.toFloat(), height.toFloat())
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, 3)
    }

    fun delete() {
        if (program != 0) GLES30.glDeleteProgram(program)
        program = 0
        srcLoc = -1
        resLoc = -1
    }

    private fun link(): Boolean {
        val vertex = compile(GLES30.GL_VERTEX_SHADER, VERT)
        val fragment = compile(GLES30.GL_FRAGMENT_SHADER, FRAG)
        if (vertex == 0 || fragment == 0) {
            if (vertex != 0) GLES30.glDeleteShader(vertex)
            if (fragment != 0) GLES30.glDeleteShader(fragment)
            return false
        }
        val linked = GLES30.glCreateProgram()
        GLES30.glAttachShader(linked, vertex)
        GLES30.glAttachShader(linked, fragment)
        GLES30.glLinkProgram(linked)
        val status = IntArray(1)
        GLES30.glGetProgramiv(linked, GLES30.GL_LINK_STATUS, status, 0)
        GLES30.glDeleteShader(vertex)
        GLES30.glDeleteShader(fragment)
        if (status[0] == 0) {
            GLES30.glDeleteProgram(linked)
            return false
        }
        program = linked
        srcLoc = GLES30.glGetUniformLocation(linked, "uSrc")
        resLoc = GLES30.glGetUniformLocation(linked, "uRes")
        return true
    }

    private fun compile(type: Int, source: String): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source.trim())
        GLES30.glCompileShader(shader)
        val compiled = IntArray(1)
        GLES30.glGetShaderiv(shader, GLES30.GL_COMPILE_STATUS, compiled, 0)
        if (compiled[0] == 0) {
            GLES30.glDeleteShader(shader)
            return 0
        }
        return shader
    }

    private companion object {
        const val VERT = """
            #version 300 es
            void main() {
                float x = float((gl_VertexID & 1) << 2) - 1.0;
                float y = float((gl_VertexID & 2) << 1) - 1.0;
                gl_Position = vec4(x, y, 0.0, 1.0);
            }
        """

        const val FRAG = """
            #version 300 es
            precision mediump float;
            uniform sampler2D uSrc;
            uniform vec2 uRes;
            out vec4 fragColor;
            void main() {
                fragColor = texture(uSrc, gl_FragCoord.xy / uRes);
            }
        """
    }
}

internal fun packGlRgba(
    src: ByteBuffer,
    width: Int,
    height: Int,
    existing: IntArray?,
    store: (IntArray) -> Unit,
): IntArray {
    val needed = width * height
    val pixels = existing?.takeIf { it.size == needed } ?: IntArray(needed).also(store)
    val rowBytes = width * 4
    var index = 0
    for (y in height - 1 downTo 0) {
        val row = y * rowBytes
        var x = 0
        while (x < width) {
            val offset = row + x * 4
            val r = src.get(offset).toInt() and 0xFF
            val g = src.get(offset + 1).toInt() and 0xFF
            val b = src.get(offset + 2).toInt() and 0xFF
            val a = src.get(offset + 3).toInt() and 0xFF
            pixels[index++] = (a shl 24) or (r shl 16) or (g shl 8) or b
            x++
        }
    }
    return pixels
}
