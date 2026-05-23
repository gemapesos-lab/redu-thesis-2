package edu.feutech.redu.capture

import android.view.accessibility.AccessibilityNodeInfo
import edu.feutech.redu.data.Platform

object PlatformAdapterRegistry {
    val packageNames: Set<String> = TikTokAdapter.PACKAGE_NAMES + InstagramAdapter.PACKAGE_NAMES + FacebookAdapter.PACKAGE_NAMES

    fun platformFor(packageName: CharSequence?): Platform? =
        when {
            TikTokAdapter.supports(packageName) -> Platform.TIKTOK
            InstagramAdapter.supports(packageName) -> Platform.INSTAGRAM
            FacebookAdapter.supports(packageName) -> Platform.FACEBOOK
            else -> null
        }

    fun supports(packageName: CharSequence?): Boolean = platformFor(packageName) != null

    fun isSupportedSurface(platform: Platform, root: AccessibilityNodeInfo?): Boolean =
        when (platform) {
            Platform.TIKTOK -> TikTokAdapter.isReelsSurface(root)
            Platform.INSTAGRAM -> InstagramAdapter.isReelsSurface(root)
            Platform.FACEBOOK -> FacebookAdapter.isReelsSurface(root)
        }

    fun isSupportedSurface(platform: Platform, roots: List<AccessibilityNodeInfo>): Boolean =
        when (platform) {
            Platform.TIKTOK -> TikTokAdapter.isReelsSurface(roots)
            Platform.INSTAGRAM -> InstagramAdapter.isReelsSurface(roots)
            Platform.FACEBOOK -> FacebookAdapter.isReelsSurface(roots)
        }

    fun isCommentSheetSurface(platform: Platform, root: AccessibilityNodeInfo?): Boolean =
        when (platform) {
            Platform.TIKTOK -> TikTokAdapter.isCommentSheetSurface(root)
            Platform.INSTAGRAM -> InstagramAdapter.isCommentSheetSurface(root)
            Platform.FACEBOOK -> FacebookAdapter.isCommentSheetSurface(root)
        }

    fun extract(platform: Platform, root: AccessibilityNodeInfo?, reelsSurfaceConfirmed: Boolean = false): PlatformTextExtraction =
        when (platform) {
            Platform.TIKTOK -> TikTokAdapter.extract(root)
            Platform.INSTAGRAM -> InstagramAdapter.extract(root)
            Platform.FACEBOOK -> FacebookAdapter.extract(root, reelsSurfaceConfirmed)
        }
}
