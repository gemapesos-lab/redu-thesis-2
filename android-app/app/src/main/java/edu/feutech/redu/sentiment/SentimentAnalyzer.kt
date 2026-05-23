package edu.feutech.redu.sentiment

import android.content.Context
import kotlin.math.sqrt

data class SentimentResult(
    val compound: Double,
    val recognizedTokens: Int,
    val totalTokens: Int,
    val oovRatio: Double,
) {
    val isNegative: Boolean = compound < -0.05
    val reliable: Boolean = oovRatio < OovCalculator.UNRELIABLE_THRESHOLD
}

// DEBUG_OVERLAY_REMOVE: delete this display-only model with classifyTokensForDebug when removing the overlay.
data class DebugTokenBreakdown(
    val snippet: String,
    val negativeTokens: List<String>,
    val positiveTokens: List<String>,
    val neutralTokens: List<String>,
    val unscoredTokens: List<String>,
    val oovRatio: Double,
) {
    companion object {
        const val SNIPPET_LIMIT = 300
        const val TOKEN_GROUP_LIMIT = 18
    }
}

class VADERCompatibleAnalyzer internal constructor(
    baseLexicon: Map<String, Double>,
    private val extensionLexicon: Map<String, Double>,
) {
    constructor(extensionLexicon: Map<String, Double> = emptyMap()) : this(FALLBACK_LEXICON, extensionLexicon)

    private val lexicon = baseLexicon.mapKeys { it.key.lowercase() } +
        extensionLexicon.mapKeys { it.key.lowercase() }

    private val boosterDict = mapOf(
        "absolutely" to 0.293, "amazingly" to 0.293, "awfully" to 0.293,
        "completely" to 0.293, "considerably" to 0.293, "decidedly" to 0.293,
        "deeply" to 0.293, "effing" to 0.293, "enormously" to 0.293,
        "entirely" to 0.293, "especially" to 0.293, "exceptionally" to 0.293,
        "extremely" to 0.293, "fabulously" to 0.293, "flipping" to 0.293,
        "flippin" to 0.293, "fricking" to 0.293, "frickin" to 0.293,
        "frigging" to 0.293, "friggin" to 0.293, "fully" to 0.293,
        "fucking" to 0.293, "greatly" to 0.293, "hella" to 0.293,
        "highly" to 0.293, "hugely" to 0.293, "incredibly" to 0.293,
        "intensely" to 0.293, "majorly" to 0.293, "more" to 0.293,
        "most" to 0.293, "particularly" to 0.293, "purely" to 0.293,
        "quite" to 0.293, "really" to 0.293, "remarkably" to 0.293,
        "so" to 0.293, "substantially" to 0.293, "thoroughly" to 0.293,
        "totally" to 0.293, "tremendously" to 0.293, "uber" to 0.293,
        "unbelievably" to 0.293, "unusually" to 0.293, "utterly" to 0.293,
        "very" to 0.293,
        "almost" to -0.293, "barely" to -0.293, "hardly" to -0.293,
        "just enough" to -0.293, "kinda" to -0.293, "kindof" to -0.293,
        "less" to -0.293, "little" to -0.293, "marginally" to -0.293,
        "occasionally" to -0.293, "partly" to -0.293, "scarcely" to -0.293,
        "slightly" to -0.293, "somewhat" to -0.293, "sorta" to -0.293,
        "sortof" to -0.293,
        
        // Tagalog intensifiers / boosters
        "sobrang" to 0.293, "napaka" to 0.293, "talaga" to 0.293,
        "tunay" to 0.293, "mas" to 0.293, "medyo" to -0.293,
        "bahagya" to -0.293
    )

    private val negators = setOf(
        "aint", "arent", "cannot", "cant", "couldnt", "darent", "didnt", "doesnt",
        "ain't", "aren't", "can't", "couldn't", "daren't", "didn't", "doesn't",
        "dont", "hadnt", "hasnt", "havent", "isnt", "mightnt", "mustnt", "neither",
        "don't", "hadn't", "hasn't", "haven't", "isn't", "mightn't", "mustn't",
        "neednt", "needn't", "never", "none", "nope", "nor", "not", "nothing", "nowhere",
        "oughtnt", "shant", "shouldnt", "uhuh", "wasnt", "werent",
        "oughtn't", "shan't", "shouldn't", "uh-uh", "wasn't", "weren't",
        "without", "wont", "wouldnt", "won't", "wouldn't", "rarely", "seldom", "despite",
        
        // Tagalog negators
        "hindi", "di", "wala", "wag"
    )

    private val contrastConjunctions = setOf("but", "pero", "ngunit", "subalit")

    fun analyze(text: String): SentimentResult {
        val tokens = tokenize(text.withoutUserMentions())
        if (tokens.isEmpty()) return SentimentResult(0.0, 0, 0, 1.0)

        val isCapDiff = allcapDifferential(tokens)
        val sentiments = DoubleArray(tokens.size) { 0.0 }
        var recognized = 0

        tokens.forEachIndexed { index, token ->
            val tokenLower = token.lowercase()
            val laughterValence = laughterValence(tokenLower)
            
            // Check if recognized
            val inLexicon = lexicon.containsKey(tokenLower)
            val isStopWord = tokenLower in extensionLexicon
            val isBooster = boosterDict.containsKey(tokenLower)
            val isNegator = tokenLower in negators
            
            if (laughterValence != null || inLexicon || isStopWord || isBooster || isNegator) {
                recognized++
            }

            // Booster words don't carry valence themselves
            if (isBooster) {
                return@forEachIndexed
            }
            
            // Special check: "kind of" or "sort of" bi-grams
            if (tokenLower == "kind" && index < tokens.size - 1 && tokens[index + 1].lowercase() == "of") {
                return@forEachIndexed
            }
            if (tokenLower == "sort" && index < tokens.size - 1 && tokens[index + 1].lowercase() == "of") {
                return@forEachIndexed
            }

            if (laughterValence != null || inLexicon) {
                var valence = laughterValence ?: lexicon.getValue(tokenLower)

                // ALL CAPS differential modifier
                if (isAllCaps(token) && isCapDiff) {
                    if (valence > 0) {
                        valence += 0.733
                    } else {
                        valence -= 0.733
                    }
                }

                // Lookback up to 3 words for modifiers
                for (startI in 0 until 3) {
                    val prevIndex = index - (startI + 1)
                    if (prevIndex >= 0) {
                        val prevToken = tokens[prevIndex]
                        val prevTokenLower = prevToken.lowercase()
                        
                        // Check booster
                        if (boosterDict.containsKey(prevTokenLower)) {
                            var s = boosterDict.getValue(prevTokenLower)
                            if (valence < 0) {
                                s *= -1.0
                            }
                            if (isAllCaps(prevToken) && isCapDiff) {
                                if (valence > 0) {
                                    s += 0.733
                                } else {
                                    s -= 0.733
                                }
                            }
                            
                            if (startI == 1) s *= 0.95
                            if (startI == 2) s *= 0.9
                            
                            valence += s
                        }
                        
                        // Check negation
                        if (prevTokenLower in negators) {
                            var isNegated = true
                            if (startI == 1) {
                                val intermediateTokenLower = tokens[index - 1].lowercase()
                                if (prevTokenLower == "never" && (intermediateTokenLower == "so" || intermediateTokenLower == "this")) {
                                    valence *= 1.25
                                    isNegated = false
                                } else if (prevTokenLower == "without" && intermediateTokenLower == "doubt") {
                                    isNegated = false
                                }
                            } else if (startI == 2) {
                                val intermediate1 = tokens[index - 2].lowercase()
                                val intermediate2 = tokens[index - 1].lowercase()
                                if (prevTokenLower == "never" && 
                                    ((intermediate1 == "so" || intermediate1 == "this") || (intermediate2 == "so" || intermediate2 == "this"))) {
                                    valence *= 1.25
                                    isNegated = false
                                } else if (prevTokenLower == "without" && (intermediate1 == "doubt" || intermediate2 == "doubt")) {
                                    isNegated = false
                                }
                            }
                            
                            if (isNegated) {
                                valence *= -0.74
                            }
                        }
                    }
                }
                
                sentiments[index] = valence
            }
        }

        // Contrastive conjunction check
        var butIndex = -1
        for (i in tokens.indices) {
            if (tokens[i].lowercase() in contrastConjunctions) {
                butIndex = i
                break
            }
        }
        
        if (butIndex != -1) {
            for (i in sentiments.indices) {
                if (i < butIndex) {
                    sentiments[i] = sentiments[i] * 0.5
                } else if (i > butIndex) {
                    sentiments[i] = sentiments[i] * 1.5
                }
            }
        }

        var sum = sentiments.sum()

        // Punctuation emphasis
        val epCount = text.count { it == '!' }
        val epAmplifier = minOf(4, epCount) * 0.292
        
        val qmCount = text.count { it == '?' }
        var qmAmplifier = 0.0
        if (qmCount > 1) {
            qmAmplifier = if (qmCount <= 3) {
                qmCount * 0.18
            } else {
                0.96
            }
        }
        val punctEmphAmplifier = epAmplifier + qmAmplifier
        
        if (sum > 0) {
            sum += punctEmphAmplifier
        } else if (sum < 0) {
            sum -= punctEmphAmplifier
        }

        val compound = normalize(sum)
        val oovRatio = OovCalculator.ratio(total = tokens.size, recognized = recognized)
        return SentimentResult(compound, recognized, tokens.size, oovRatio)
    }

    // DEBUG_OVERLAY_REMOVE: display-only token grouping; analyze() remains the scoring source of truth.
    fun classifyTokensForDebug(text: String): DebugTokenBreakdown {
        val normalizedText = text.withoutUserMentions()
        val tokens = tokenize(normalizedText)
        if (tokens.isEmpty()) {
            return DebugTokenBreakdown(
                snippet = normalizedText.normalizeSnippet(DebugTokenBreakdown.SNIPPET_LIMIT),
                negativeTokens = emptyList(),
                positiveTokens = emptyList(),
                neutralTokens = emptyList(),
                unscoredTokens = emptyList(),
                oovRatio = 1.0,
            )
        }

        var recognized = 0
        val negative = mutableListOf<String>()
        val positive = mutableListOf<String>()
        val neutral = mutableListOf<String>()
        val unscored = mutableListOf<String>()

        tokens.forEach { token ->
            val tokenLower = token.lowercase()
            val laughterValence = laughterValence(tokenLower)
            val inLexicon = lexicon.containsKey(tokenLower)
            val isStopWord = tokenLower in extensionLexicon
            val isBooster = boosterDict.containsKey(tokenLower)
            val isNegator = tokenLower in negators

            if (laughterValence != null || inLexicon || isStopWord || isBooster || isNegator) {
                recognized++
                val valence = laughterValence ?: lexicon[tokenLower] ?: 0.0
                when {
                    isBooster || isNegator || valence == 0.0 -> neutral += token
                    valence < 0.0 -> negative += token
                    else -> positive += token
                }
            } else {
                unscored += token
            }
        }

        val oovRatio = OovCalculator.ratio(total = tokens.size, recognized = recognized)

        return DebugTokenBreakdown(
            snippet = normalizedText.normalizeSnippet(DebugTokenBreakdown.SNIPPET_LIMIT),
            negativeTokens = negative.distinct().take(DebugTokenBreakdown.TOKEN_GROUP_LIMIT),
            positiveTokens = positive.distinct().take(DebugTokenBreakdown.TOKEN_GROUP_LIMIT),
            neutralTokens = neutral.distinct().take(DebugTokenBreakdown.TOKEN_GROUP_LIMIT),
            unscoredTokens = unscored.distinct().take(DebugTokenBreakdown.TOKEN_GROUP_LIMIT),
            oovRatio = oovRatio,
        )
    }

    private fun isAllCaps(token: String): Boolean {
        val letters = token.filter { it.isLetter() }
        return letters.isNotEmpty() && letters.all { it.isUpperCase() }
    }

    private fun allcapDifferential(tokens: List<String>): Boolean {
        var allCapWords = 0
        var totalWords = 0
        for (token in tokens) {
            val hasLetter = token.any { it.isLetter() }
            if (hasLetter) {
                totalWords++
                if (isAllCaps(token)) {
                    allCapWords++
                }
            }
        }
        return allCapWords > 0 && allCapWords < totalWords
    }

    private fun tokenize(text: String): List<String> =
        Regex("[\\p{L}\\p{N}_']+(?:-[\\p{L}\\p{N}_']+)*|[\\p{So}\\p{Sk}\\p{Sm}\\uFE0F\\u200D]+|[:;=8B][-~]?[)D(P/\\\\|oO3D]+")
            .findAll(text)
            .map { it.value }
            .toList()

    private fun String.withoutUserMentions(): String =
        replace(USER_MENTION_REGEX, " ")

    private fun laughterValence(token: String): Double? =
        if (LAUGHTER_REGEX.matches(token)) LAUGHTER_VALENCE else null

    private fun normalize(score: Double): Double = score / sqrt(score * score + 15.0)

    private fun String.normalizeSnippet(limit: Int): String {
        val normalized = replace(Regex("\\s+"), " ").trim()
        return if (normalized.length <= limit) normalized else normalized.take(limit - 3).trimEnd() + "..."
    }

    companion object {
        private val USER_MENTION_REGEX = Regex("(?<![\\p{L}\\p{N}_])@[\\p{L}\\p{N}._]{1,32}")
        private val LAUGHTER_REGEX = Regex("(?=.{4,}$)(?=(?:.*h){2,})(?=(?:.*a){2,})[ha]*ha[ha]*")
        private const val LAUGHTER_VALENCE = 4.0

        fun fromAsset(
            context: Context,
            vaderAssetName: String = VADER_LEXICON_ASSET,
            nrcAssetName: String = NRC_VAD_LEXICON_ASSET,
            extensionLexicon: Map<String, Double> = emptyMap(),
        ): VADERCompatibleAnalyzer {
            val vaderLexicon = context.assets.open(vaderAssetName).use { input ->
                parseLexicon(input.bufferedReader().readLines())
            }
            val nrcLexicon = try {
                context.assets.open(nrcAssetName).use { input ->
                    parseLexicon(input.bufferedReader().readLines())
                }
            } catch (e: Exception) {
                emptyMap()
            }
            val mergedBase = FALLBACK_LEXICON + nrcLexicon + vaderLexicon
            return VADERCompatibleAnalyzer(mergedBase, extensionLexicon)
        }

        internal fun parseLexicon(lines: List<String>): Map<String, Double> =
            lines.asSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val parts = line.split('\t')
                    val token = parts.getOrNull(0)?.trim().orEmpty()
                    val valence = parts.getOrNull(1)?.trim()?.toDoubleOrNull()
                    if (token.isBlank() || valence == null) null else token to valence
                }
                .toMap()

        const val VADER_LEXICON_ASSET = "vader_lexicon.txt"
        const val NRC_VAD_LEXICON_ASSET = "nrc_vad_lexicon.txt"

        private val FALLBACK_LEXICON = mapOf(
            "heartbreaking" to -3.2,
            "sad" to -2.1,
            "angry" to -2.7,
            "fear" to -2.2,
            "scared" to -2.0,
            "death" to -2.8,
            "crisis" to -2.4,
            "disaster" to -2.8,
            "war" to -2.9,
            "hate" to -2.7,
            "bad" to -2.1,
            "good" to 1.9,
            "happy" to 2.7,
            "calm" to 1.7,
            "safe" to 1.5,
            "love" to 3.2,
            "like" to 1.5,
            "share" to 1.2,
            "😭" to -2.6,
            "💀" to -1.8,
            "😢" to -2.2,
            "😡" to -2.7,
            "❤️" to 2.8,
            "❤" to 2.8,
            "😂" to 1.8,
            "😊" to 2.0,
            "☺️" to 2.0,
            "☺" to 2.0,
            "😍" to 2.5,
            "😘" to 2.4,
            "👍" to 2.0,
            "👎" to -2.0,
            "✨" to 1.5,
            "🔥" to 2.0,
            "💔" to -2.5,
            "🥺" to 1.0,
            "🤡" to -1.5,
            "🤮" to -2.5,
            "🤢" to -2.0,
            "😠" to -2.5,
            "💖" to 2.5,
            "💕" to 2.5,
            "🤩" to 2.5,
            "🥳" to 2.5,
            "👏" to 1.5,
            "😀" to 2.0,
            "😃" to 2.0,
            "😄" to 2.0,
            "😁" to 2.0,
            "😆" to 2.0,
            "☹️" to -1.5,
            "☹" to -1.5,
            "🙁" to -1.5,
            "😟" to -1.5,
            "😞" to -1.5,
            "😔" to -1.5
        )
    }
}
