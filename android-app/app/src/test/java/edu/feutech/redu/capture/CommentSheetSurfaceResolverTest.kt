package edu.feutech.redu.capture

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommentSheetSurfaceResolverTest {
    @Test
    fun directSupportedSurfaceAllowsLaterCommentSheetInheritance() {
        val resolver = CommentSheetSurfaceResolver()

        assertTrue(resolver.resolve(1, directSupported = true, commentSheet = false, platformName = "INSTAGRAM") { false })
        assertTrue(resolver.resolve(2, directSupported = false, commentSheet = true, platformName = "INSTAGRAM") { false })
    }

    @Test
    fun unsupportedSurfaceClearsCommentSheetInheritance() {
        val resolver = CommentSheetSurfaceResolver()

        assertTrue(resolver.resolve(1, directSupported = true, commentSheet = false, platformName = "INSTAGRAM") { false })
        assertFalse(resolver.resolve(2, directSupported = false, commentSheet = false, platformName = "INSTAGRAM") { false })
        resolver.onTargetExit()
        assertFalse(resolver.resolve(3, directSupported = false, commentSheet = true, platformName = "INSTAGRAM") { false })
    }

    @Test
    fun sameCommentSheetWindowUsesCachedDecision() {
        val resolver = CommentSheetSurfaceResolver()
        var checks = 0

        assertTrue(resolver.resolve(4, directSupported = false, commentSheet = true, platformName = "INSTAGRAM") {
            checks += 1
            true
        })
        assertTrue(resolver.resolve(4, directSupported = false, commentSheet = true, platformName = "INSTAGRAM") {
            checks += 1
            false
        })
        assertEquals(1, checks)
    }

    @Test
    fun targetExitStopsInheritanceFallback() {
        val resolver = CommentSheetSurfaceResolver()

        assertTrue(resolver.resolve(1, directSupported = true, commentSheet = false, platformName = "INSTAGRAM") { false })
        resolver.onTargetExit()

        assertFalse(resolver.resolve(2, directSupported = false, commentSheet = true, platformName = "INSTAGRAM") { false })
    }

    @Test
    fun trustedPlatformCommentSheetIsRejectedWithoutVisibleOrInheritedReelsWindow() {
        val resolver = CommentSheetSurfaceResolver()

        assertFalse(
            resolver.resolve(
                rootWindowId = 8,
                directSupported = false,
                commentSheet = true,
                platformName = "INSTAGRAM",
            ) { false },
        )
    }

    @Test
    fun targetExitClearsCachedCommentSheetWindowDecision() {
        val resolver = CommentSheetSurfaceResolver()

        assertTrue(resolver.resolve(4, directSupported = false, commentSheet = true, platformName = "INSTAGRAM") { true })
        resolver.onTargetExit()

        assertFalse(resolver.resolve(4, directSupported = false, commentSheet = true, platformName = "INSTAGRAM") { false })
    }

    @Test
    fun commentSheetInheritanceStillWorksUntilExplicitTargetExit() {
        val resolver = CommentSheetSurfaceResolver()

        assertTrue(resolver.resolve(1, directSupported = true, commentSheet = false, platformName = "INSTAGRAM") { false })
        assertTrue(resolver.resolve(2, directSupported = false, commentSheet = true, platformName = "INSTAGRAM") { false })
    }

    @Test
    fun transientUnsupportedSurfaceDoesNotClearCommentSheetInheritance() {
        var now = 1_000L
        val resolver = CommentSheetSurfaceResolver(clock = { now }, transitionGraceMillis = 2_000L)

        assertTrue(resolver.resolve(1, directSupported = true, commentSheet = false, platformName = "TIKTOK") { false })
        now += 300L
        assertFalse(resolver.resolve(1, directSupported = false, commentSheet = false, platformName = "TIKTOK") { false })
        now += 300L

        assertTrue(resolver.resolve(2, directSupported = false, commentSheet = true, platformName = "TIKTOK") { false })
    }

    @Test
    fun oldUnsupportedSurfaceClearsCommentSheetInheritance() {
        var now = 1_000L
        val resolver = CommentSheetSurfaceResolver(clock = { now }, transitionGraceMillis = 2_000L)

        assertTrue(resolver.resolve(1, directSupported = true, commentSheet = false, platformName = "TIKTOK") { false })
        assertFalse(resolver.resolve(1, directSupported = false, commentSheet = false, platformName = "TIKTOK") { false })
        now += 2_001L

        assertFalse(resolver.resolve(2, directSupported = false, commentSheet = true, platformName = "TIKTOK") { false })
    }
}
