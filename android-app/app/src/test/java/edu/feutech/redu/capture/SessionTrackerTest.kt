package edu.feutech.redu.capture

import edu.feutech.redu.sentiment.SentimentResult
import edu.feutech.redu.data.Platform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SessionTrackerTest {
    @Test
    fun commentsOnlySentimentDoesNotCreateCaptionTransition() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()

        tracker.onContentObserved("caption-one", reliableNegativeSentiment(), sentimentFingerprint = "caption-one")
        now += 5_000L
        tracker.onContentObserved(null, reliableNegativeSentiment(), sentimentFingerprint = "caption-one same visible text")
        now += 5_000L
        tracker.onContentObserved(null, reliableNegativeSentiment(), sentimentFingerprint = "caption-one same visible text")

        val snapshot = tracker.snapshot()

        assertEquals(0, snapshot?.swipeCount)
        assertEquals(10_000L, snapshot?.currentDwellMillis)
        assertEquals(10_000L, snapshot?.promptExcludedDurationMillis)
        assertEquals(10_000L, snapshot?.meanDwellMillis)
        assertEquals(2, snapshot?.resolvableUnits)
        assertEquals(2, snapshot?.negativeUnits)
        assertTrue(snapshot?.nsdPercent == 100.0)
    }

    @Test
    fun changedCaptionClosesDwellIntervalAndIncrementsTransitionCount() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()

        tracker.onContentObserved("caption-one", reliableNegativeSentiment())
        now += 7_000L
        tracker.onContentObserved(null, reliableNegativeSentiment())
        now += 5_000L
        tracker.onContentObserved("caption-two", reliableNegativeSentiment())

        val snapshot = tracker.snapshot()

        assertEquals(1, snapshot?.swipeCount)
        assertEquals(12_000L, snapshot?.meanDwellMillis)
        assertEquals(0L, snapshot?.currentDwellMillis)
        assertEquals(3, snapshot?.resolvableUnits)
    }

    @Test
    fun repeatedCaptionDoesNotCreateDuplicateTransition() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()

        tracker.onContentObserved("caption-one", reliableNegativeSentiment())
        now += 3_000L
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        val snapshot = tracker.snapshot()

        assertEquals(0, snapshot?.swipeCount)
        assertEquals(3_000L, snapshot?.currentDwellMillis)
        assertEquals(3_000L, snapshot?.meanDwellMillis)
    }

    @Test
    fun shortAppSwitchGapIsExcludedAndSessionContinues() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 10_000L
        tracker.onTargetBackground()
        now += 20_000L
        tracker.onTargetForeground()

        val snapshot = tracker.snapshot()

        assertEquals(10_000L, snapshot?.durationMillis)
        assertEquals(10_000L, snapshot?.promptExcludedDurationMillis)
        assertEquals(10_000L, snapshot?.currentDwellMillis)
        assertEquals(0, snapshot?.swipeCount)
        assertEquals(null, tracker.finalizeIfInactive())
    }

    @Test
    fun platformSwitchFinalizesPreviousSessionAndStartsNewPlatformSession() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground(Platform.TIKTOK)
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 12_000L
        val finalized = tracker.onTargetForeground(Platform.INSTAGRAM)
        val active = tracker.snapshot()

        assertEquals(Platform.TIKTOK, finalized?.snapshot?.platform)
        assertEquals(12_000L, finalized?.snapshot?.durationMillis)
        assertEquals(13_000L, finalized?.endedAtMillis)
        assertEquals(Platform.INSTAGRAM, active?.platform)
        assertEquals(0L, active?.durationMillis)
        assertEquals(0, active?.swipeCount)
    }

    @Test
    fun targetBackgroundLongerThanBridgeFinalizesAtBackgroundStart() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 12_000L
        tracker.onTargetBackground()
        now += SessionTracker.BRIDGE_WINDOW_MILLIS

        val finalized = tracker.finalizeIfInactive()

        assertEquals(12_000L, finalized?.snapshot?.durationMillis)
        assertEquals(12_000L, finalized?.snapshot?.promptExcludedDurationMillis)
        assertEquals(12_000L, finalized?.snapshot?.currentDwellMillis)
        assertEquals(13_000L, finalized?.endedAtMillis)
    }

    @Test
    fun forceFinalizeClosesAtCurrentActiveTime() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 9_000L
        val finalized = tracker.forceFinalize()

        assertEquals(9_000L, finalized?.snapshot?.durationMillis)
        assertEquals(9_000L, finalized?.snapshot?.promptExcludedDurationMillis)
        assertEquals(9_000L, finalized?.snapshot?.currentDwellMillis)
        assertEquals(10_000L, finalized?.endedAtMillis)
    }

    @Test
    fun idleWithoutMicroInteractionCapsCurrentDwellAtFortyFiveSeconds() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 60_000L
        val snapshot = tracker.snapshot()

        assertEquals(60_000L, snapshot?.durationMillis)
        assertEquals(SessionTracker.IDLE_TIMEOUT_MILLIS, snapshot?.currentDwellMillis)
    }

    @Test
    fun microInteractionExtendsCurrentDwellWindow() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 40_000L
        tracker.onUserInteraction()
        now += 20_000L
        val snapshot = tracker.snapshot()

        assertEquals(60_000L, snapshot?.durationMillis)
        assertEquals(60_000L, snapshot?.currentDwellMillis)
    }

    @Test
    fun promptExcludedDurationStopsAfterPromptClosesCurrentInterval() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 20_000L
        tracker.closeForPrompt()
        now += 10_000L
        val snapshot = tracker.snapshot()

        assertEquals(30_000L, snapshot?.durationMillis)
        assertEquals(20_000L, snapshot?.promptExcludedDurationMillis)
        assertEquals(0L, snapshot?.currentDwellMillis)
        assertEquals(20_000L, snapshot?.meanDwellMillis)
    }

    @Test
    fun nonBlockingPromptDoesNotFreezeCurrentDwellOrPromptExcludedDuration() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 20_000L
        // Non-blocking presentation events such as Toast fallbacks should not call closeForPrompt.
        now += 5_000L
        val snapshot = tracker.snapshot()

        assertEquals(25_000L, snapshot?.durationMillis)
        assertEquals(25_000L, snapshot?.promptExcludedDurationMillis)
        assertEquals(25_000L, snapshot?.currentDwellMillis)
    }

    @Test
    fun awarenessToastPromptCanResumeImmediatelyBecauseItHasNoBlockingOverlay() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 20_000L
        tracker.closeForPrompt()
        tracker.onPromptClosed()
        now += 3_000L
        tracker.onUserInteraction()
        now += 4_000L
        val snapshot = tracker.snapshot()

        assertEquals(27_000L, snapshot?.durationMillis)
        assertEquals(24_000L, snapshot?.promptExcludedDurationMillis)
        assertEquals(4_000L, snapshot?.currentDwellMillis)
    }

    @Test
    fun promptExcludedDurationResumesOnlyAfterPostPromptReturnInteraction() {
        var now = 1_000L
        val tracker = SessionTracker(clock = { now })
        tracker.onTargetForeground()
        tracker.onContentObserved("caption-one", reliableNegativeSentiment())

        now += 20_000L
        tracker.closeForPrompt()
        now += 10_000L
        tracker.onPromptClosed()
        now += 5_000L
        assertEquals(20_000L, tracker.snapshot()?.promptExcludedDurationMillis)

        tracker.onUserInteraction()
        now += 7_000L
        val snapshot = tracker.snapshot()

        assertEquals(42_000L, snapshot?.durationMillis)
        assertEquals(27_000L, snapshot?.promptExcludedDurationMillis)
        assertEquals(7_000L, snapshot?.currentDwellMillis)
    }

    private fun reliableNegativeSentiment(): SentimentResult =
        SentimentResult(
            compound = -0.6,
            recognizedTokens = 4,
            totalTokens = 4,
            oovRatio = 0.0,
        )
}
