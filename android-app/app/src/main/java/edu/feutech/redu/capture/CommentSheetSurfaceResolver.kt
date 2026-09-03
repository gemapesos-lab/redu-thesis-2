package edu.feutech.redu.capture

class CommentSheetSurfaceResolver(
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val transitionGraceMillis: Long = COMMENT_SHEET_TRANSITION_GRACE_MILLIS,
) {
    private var lastCommentSheetWindowId: Int = -1
    private var lastCommentSheetPlatformName: String? = null
    private var lastCommentSheetWasReels: Boolean = false
    private var canInheritReelsSurface: Boolean = false
    private var lastDirectSupportedPlatformName: String? = null
    private var lastDirectSupportedWindowId: Int = -1
    private var lastUnsupportedAtMillis: Long = NO_TIMESTAMP

    fun resolve(
        rootWindowId: Int,
        directSupported: Boolean,
        commentSheet: Boolean,
        platformName: String,
        multiWindowSupported: () -> Boolean,
    ): Boolean {
        val now = clock()
        if (directSupported) {
            resetCommentSheet()
            canInheritReelsSurface = true
            lastDirectSupportedPlatformName = platformName
            lastDirectSupportedWindowId = rootWindowId
            lastUnsupportedAtMillis = NO_TIMESTAMP
            return true
        }

        if (!commentSheet) {
            resetCommentSheet()
            if (canInheritReelsSurface &&
                platformName == lastDirectSupportedPlatformName &&
                rootWindowId == lastDirectSupportedWindowId
            ) {
                lastUnsupportedAtMillis = now
            } else {
                clearInheritance()
            }
            return false
        }

        val supported = if (
            rootWindowId == lastCommentSheetWindowId &&
            platformName == lastCommentSheetPlatformName
        ) {
            lastCommentSheetWasReels
        } else {
            val resolved = multiWindowSupported() ||
                canInheritFromRecentTargetSurface(platformName, rootWindowId, now)
            lastCommentSheetWindowId = rootWindowId
            lastCommentSheetPlatformName = platformName
            lastCommentSheetWasReels = resolved
            resolved
        }
        if (supported) {
            canInheritReelsSurface = true
            lastDirectSupportedPlatformName = platformName
            lastUnsupportedAtMillis = NO_TIMESTAMP
        } else {
            clearInheritance()
        }
        return supported
    }

    fun onTargetExit() {
        resetCommentSheet()
        clearInheritance()
    }

    private fun canInheritFromRecentTargetSurface(platformName: String, rootWindowId: Int, now: Long): Boolean {
        if (!canInheritReelsSurface || platformName != lastDirectSupportedPlatformName) return false
        if (rootWindowId != lastDirectSupportedWindowId) return false
        return lastUnsupportedAtMillis == NO_TIMESTAMP ||
            now - lastUnsupportedAtMillis <= transitionGraceMillis
    }

    private fun clearInheritance() {
        canInheritReelsSurface = false
        lastDirectSupportedPlatformName = null
        lastDirectSupportedWindowId = -1
        lastUnsupportedAtMillis = NO_TIMESTAMP
    }

    private fun resetCommentSheet() {
        lastCommentSheetWindowId = -1
        lastCommentSheetPlatformName = null
        lastCommentSheetWasReels = false
    }

    private companion object {
        const val COMMENT_SHEET_TRANSITION_GRACE_MILLIS = 2_000L
        const val NO_TIMESTAMP = -1L
    }
}
