package edu.feutech.redu.capture

data class TikTokBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
) {
    val width: Int = right - left
    val height: Int = bottom - top

    fun hasArea(): Boolean = width > 0 && height > 0

    fun intersects(other: TikTokBounds): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top
}

enum class TikTokCaptionKind {
    VIDEO_TEXT,
    POST_DESCRIPTION,
    CAPTION_LIKE_TEXT,
    COMMENT,
    AUTHOR_NAME,
}

data class TikTokCaptionMatch(
    val kind: TikTokCaptionKind,
    val normalizedText: String,
    val reason: String,
)

object TikTokCaptionRules {
    private val postDescriptionIds = setOf("desc")
    private val commentBodyIds = setOf("emz", "enn", "emb")
    private val authorNameIds = setOf("author", "author_name", "tv_name", "user_name")

    fun classify(
        resourceId: String?,
        text: String?,
        contentDescription: String? = null,
        bounds: TikTokBounds,
        rootBounds: TikTokBounds,
        visibleToUser: Boolean,
        ancestorResourceIds: List<String> = emptyList(),
    ): TikTokCaptionMatch? {
        if (!visibleToUser || !bounds.hasArea() || !bounds.intersects(rootBounds)) return null
        val normalized = normalizeCaptionText(text)
        val normalizedDescription = normalizeCaptionText(contentDescription)
        val textValue = normalized.ifBlank { normalizedDescription }
        if (textValue.isBlank()) return null

        val suffix = resourceSuffix(resourceId)
        val ancestorSuffixes = ancestorResourceIds.map(::resourceSuffix).toSet()
        return when {
            suffix in postDescriptionIds -> TikTokCaptionMatch(
                kind = TikTokCaptionKind.POST_DESCRIPTION,
                normalizedText = textValue,
                reason = "known post description resource id: $suffix",
            )

            suffix in authorNameIds -> TikTokCaptionMatch(
                kind = TikTokCaptionKind.AUTHOR_NAME,
                normalizedText = textValue,
                reason = "known author name resource id: $suffix",
            )

            suffix.isTikTokAuthorLikeId() && textValue.isPlausibleTikTokUsername() -> TikTokCaptionMatch(
                kind = TikTokCaptionKind.AUTHOR_NAME,
                normalizedText = textValue,
                reason = "author-like resource id: $suffix",
            )

            suffix in commentBodyIds && commentBodyAncestorSets.any { ancestorSuffixes.containsAll(it) } -> TikTokCaptionMatch(
                kind = TikTokCaptionKind.COMMENT,
                normalizedText = textValue,
                reason = "known comment body resource id: $suffix under comment row ancestors",
            )

            else -> null
        }
    }

    fun normalizeCaptionText(text: String?): String {
        val normalized = text
            ?.replace(Regex("\\p{Cf}+"), "")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()
        return normalized
            .replace(Regex("\\s*[.…]+\\s*more\\s*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s+more\\s*$", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    fun resourceSuffix(resourceId: String?): String =
        resourceId?.substringAfterLast('/')?.lowercase().orEmpty()

    fun isReelsSurfaceMarker(text: String?, contentDescription: String?, resourceId: String?, isSelected: Boolean): Boolean {
        val valText = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        val suffix = resourceSuffix(resourceId)
        
        return (valText == "for you" && isSelected) || 
               (valText == "following" && isSelected) || 
               (valText.contains("home") && valText.contains("tab") && isSelected) ||
               (suffix == "bottom_tab_home" && isSelected)
    }

    fun isFeedEvidenceMarker(text: String?, contentDescription: String?, resourceId: String?): Boolean {
        val value = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        val suffix = resourceSuffix(resourceId)

        if (value.isBlank()) return false
        if (suffix in postDescriptionIds || suffix in authorNameIds || suffix.isTikTokAuthorLikeId()) return true
        return value.startsWith("@") ||
            value.startsWith("#") ||
            value.contains("#") ||
            value.startsWith("read or add comments") ||
            value.matches(Regex("""[\d,.]+\s*[km]?\s+comments?""")) ||
            value.matches(Regex("""[\d,.]+\s*[km]?\s+likes?""")) ||
            value.matches(Regex("""[\d,.]+\s*[km]?\s+shares?""")) ||
            value == "share" ||
            value.startsWith("share ") ||
            value == "like" ||
            value.startsWith("like ") ||
            value.contains("add to favorites") ||
            value.contains("profile picture")
    }

    fun isBlockingSurfaceMarker(text: String?, contentDescription: String?, resourceId: String?, isSelected: Boolean): Boolean {
        val value = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        if (isUnsupportedSurfaceMarker(text, contentDescription, resourceId, isSelected)) return true
        if (value.isBlank()) return false
        return value == "log in" ||
            value == "sign up" ||
            value == "get started" ||
            value == "loading" ||
            value == "loading..." ||
            value == "loading…" ||
            value == "tap to retry" ||
            value == "no internet connection" ||
            value == "something went wrong" ||
            value.contains("log in to") ||
            value.contains("continue with") ||
            value.contains("use phone") ||
            value.contains("use email") ||
            value.contains("welcome to tiktok") ||
            value.contains("date of birth")
    }

    fun isSupportedReelsSurface(
        hasFeedNavigationMarker: Boolean,
        hasFeedEvidence: Boolean,
        hasBlockingSurface: Boolean,
    ): Boolean = hasFeedNavigationMarker && hasFeedEvidence && !hasBlockingSurface

    fun isUnsupportedSurfaceMarker(text: String?, contentDescription: String?, resourceId: String?, isSelected: Boolean): Boolean {
        val valText = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        val suffix = resourceSuffix(resourceId)
        
        return (valText.contains("profile") && valText.contains("tab") && isSelected) ||
               (valText.contains("inbox") && valText.contains("tab") && isSelected) ||
               (valText.contains("friends") && valText.contains("tab") && isSelected) ||
               (valText.contains("search") && valText.contains("tab") && isSelected) ||
               (valText.contains("shop") && valText.contains("tab") && isSelected) ||
               (valText.contains("explore") && valText.contains("tab") && isSelected) ||
               (valText == "messages") ||
               (suffix == "bottom_tab_profile" && isSelected) ||
               (suffix == "bottom_tab_inbox" && isSelected) ||
               (suffix == "bottom_tab_friends" && isSelected) ||
               (suffix == "bottom_tab_search" && isSelected) ||
               (suffix == "bottom_tab_shop" && isSelected)
    }

    fun isCommentSheetSurfaceMarker(text: String?, contentDescription: String?, resourceId: String?, isSelected: Boolean): Boolean {
        val valText = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        val suffix = resourceSuffix(resourceId)
        
        return (valText == "comments" ||
               valText.startsWith("comments (") ||
               valText.matches(Regex("""[\d,.]+\s*[km]?\s+comments?""")) ||
               valText == "no comments yet" ||
               valText == "add comment...") ||
               (suffix == "comment_list" || suffix == "comment_container")
    }

    private fun String?.normalizeWhitespace(): String =
        this
            ?.replace(Regex("\\p{Cf}+"), "")
            ?.replace(Regex("\\s+"), " ")
            ?.trim()
            .orEmpty()

    private fun String.isTikTokAuthorLikeId(): Boolean =
        contains("author") ||
            contains("creator") ||
            contains("user") && (contains("name") || contains("title") || contains("nickname")) ||
            contains("profile") && contains("name")

    private fun String.isPlausibleTikTokUsername(): Boolean {
        val value = removePrefix("@")
        if (value.length !in 2..32) return false
        if (!value.matches(Regex("[A-Za-z0-9._]+"))) return false
        val lower = value.lowercase()
        return lower !in usernameUiLabels &&
            !lower.contains(" ") &&
            !lower.all { it.isDigit() }
    }

    private val commentBodyAncestorSets = listOf(
        setOf("sh4", "m1a"),
        setOf("e6u", "sk7", "m3q"),
        setOf("e6u", "hv5", "sk7", "m3q", "ekt"),
        setOf("sc8", "lyq", "ejh"),
    )

    private val usernameUiLabels = setOf(
        "follow",
        "following",
        "friends",
        "for you",
        "home",
        "profile",
        "search",
        "inbox",
        "messages",
        "comments",
        "reply",
    )
}
