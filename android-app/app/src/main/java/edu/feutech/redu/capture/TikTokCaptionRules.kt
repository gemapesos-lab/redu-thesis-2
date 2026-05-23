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
            .replace(Regex("\\s+more\\s*$", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\s*[.…]+\\s*more\\s*$", RegexOption.IGNORE_CASE), "")
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

    fun isUnsupportedSurfaceMarker(text: String?, contentDescription: String?, resourceId: String?, isSelected: Boolean): Boolean {
        val valText = (text.takeUnless { it.isNullOrBlank() } ?: contentDescription).normalizeWhitespace().lowercase()
        val suffix = resourceSuffix(resourceId)
        
        return (valText.contains("profile") && valText.contains("tab") && isSelected) ||
               (valText.contains("inbox") && valText.contains("tab") && isSelected) ||
               (valText.contains("friends") && valText.contains("tab") && isSelected) ||
               (valText == "messages") ||
               (suffix == "bottom_tab_profile" && isSelected) ||
               (suffix == "bottom_tab_inbox" && isSelected) ||
               (suffix == "bottom_tab_friends" && isSelected)
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
