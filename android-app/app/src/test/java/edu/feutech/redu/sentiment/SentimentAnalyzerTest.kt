package edu.feutech.redu.sentiment

import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SentimentAnalyzerTest {
    @Test
    fun mvlLexiconContainsCuratedEntries() {
        assertTrue(MvlLexicon.defaultEntries.isNotEmpty())
        assertEquals(3.0, MvlLexicon.defaultEntries.getValue("maganda"), 0.0)
        assertEquals(-3.0, MvlLexicon.defaultEntries.getValue("nakalulungkot"), 0.0)
        assertEquals(-3.0, MvlLexicon.defaultEntries.getValue("nakakalungkot"), 0.0)
        assertEquals(0.0, MvlLexicon.defaultEntries.getValue("buhay"), 0.0)
        assertEquals(3.0, MvlLexicon.defaultEntries.getValue("mahal"), 0.0)
        assertEquals(-4.0, MvlLexicon.defaultEntries.getValue("bobo"), 0.0)
        
        // Verify stop words are separate and map to 0.0
        assertTrue(MvlLexicon.tagalogStopWords.isNotEmpty())
        assertEquals(0.0, MvlLexicon.tagalogStopWords.getValue("ang"), 0.0)
        assertEquals(0.0, MvlLexicon.tagalogStopWords.getValue("mga"), 0.0)
    }

    @Test
    fun analyzesFilipinoSentiment() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        val resultPositive = analyzer.analyze("sobrang maganda ang galing")
        assertTrue(resultPositive.compound > 0.05)
        assertTrue(resultPositive.recognizedTokens >= 2)

        val resultNegative = analyzer.analyze("nakakalungkot at nakakainis naman")
        assertTrue(resultNegative.compound < -0.05)
        assertTrue(resultNegative.recognizedTokens >= 2)
    }

    @Test
    fun recognizesExpertValidatedFormalAndCommonNakaSpellings() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)

        listOf(
            "nakagagalit",
            "nakakagalit",
            "nakaiinis",
            "nakakainis",
            "nakalulungkot",
            "nakakalungkot",
            "nakatatakot",
            "nakakatakot",
        ).forEach { term ->
            val result = analyzer.analyze(term)
            assertTrue("$term should be negative", result.compound < -0.05)
            assertEquals("$term should be recognized", 1, result.recognizedTokens)
        }
    }

    @Test
    fun expertValidatedNeutralMvlTermsDoNotAddSentiment() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        val result = analyzer.analyze("buhay")

        assertEquals(0.0, result.compound, 0.0)
        assertEquals(1, result.recognizedTokens)
        assertEquals(0.0, result.oovRatio, 0.0)
    }

    @Test
    fun handlesFilipinoNegation() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        
        // "hindi masaya" -> negated positive sentiment should yield negative compound score
        val result1 = analyzer.analyze("hindi masaya")
        assertTrue(result1.compound < -0.05)
        
        // "di maganda" -> negated positive sentiment should yield negative compound score
        val result2 = analyzer.analyze("di maganda")
        assertTrue(result2.compound < -0.05)

        // "wala talo" -> double negative/negated negative sentiment -> positive compound score
        val result3 = analyzer.analyze("wala talo")
        assertTrue(result3.compound > 0.05)
    }

    @Test
    fun tagalogStopWordsAreNeutral() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        val result = analyzer.analyze("ang mga bata sa kalsada")
        
        // Compound score should be exactly 0.0 because all recognized tokens are stop words (neutral 0.0 score)
        assertEquals(0.0, result.compound, 0.0)
        
        // Tokens "ang", "mga", "sa" are recognized, so recognizedTokens should be at least 3
        assertTrue(result.recognizedTokens >= 3)
        
        // Since recognized tokens exist, OOV ratio should be less than 1.0
        assertTrue(result.oovRatio < 1.0)
    }

    @Test
    fun parsesOriginalVaderLexiconRows() {
        val lexicon = VADERCompatibleAnalyzer.parseLexicon(
            listOf(
                "good\t1.9\t0.9434\t[2, 1, 1, 3, 2, 4, 2, 2, 1, 1]",
                "bad\t-2.5\t0.67082\t[-3, -2, -4, -3, -2, -2, -3, -2, -2, -2]",
            ),
        )

        assertEquals(1.9, lexicon.getValue("good"), 0.0)
        assertEquals(-2.5, lexicon.getValue("bad"), 0.0)
    }

    @Test
    fun recognizesEnglishVaderNegativeToken() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        val result = analyzer.analyze("sad")

        assertTrue(result.compound < -0.05)
        assertTrue(result.recognizedTokens >= 1)
    }

    @Test
    fun handlesSimpleFilipinoNegation() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        val result = analyzer.analyze("hindi good")

        assertTrue(result.compound < -0.05)
    }

    @Test
    fun highOovTextIsUnreliable() {
        val analyzer = VADERCompatibleAnalyzer()
        val result = analyzer.analyze("zzzz qqqq unknownterm bad")

        assertFalse(result.reliable)
        assertTrue(result.oovRatio >= OovCalculator.UNRELIABLE_THRESHOLD)
    }

    @Test
    fun userMentionsAreIgnoredForOovAndSentiment() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        val result = analyzer.analyze("@creator.name @another_user maganda")

        assertTrue(result.compound > 0.05)
        assertEquals(1, result.recognizedTokens)
        assertEquals(1, result.totalTokens)
        assertEquals(0.0, result.oovRatio, 0.0)
    }

    @Test
    fun recognizesHahaVariationsAsStrongPositive() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)

        listOf(
            "HAHA",
            "HAHAHA",
            "HAHAHHHAHA",
            "AHAHAHAHAHA",
            "HHHHAHAHAHA",
        ).forEach { laughter ->
            val result = analyzer.analyze(laughter)

            assertTrue("$laughter should be strongly positive", result.compound > 0.7)
            assertEquals("$laughter should be recognized", 1, result.recognizedTokens)
            assertEquals("$laughter should be the only token", 1, result.totalTokens)
            assertEquals("$laughter should not count as OOV", 0.0, result.oovRatio, 0.0)
        }
    }

    @Test
    fun englishStopWordsAreNeutral() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        
        // "the and to this under" contains only neutral English stop words
        val resultNeutral = analyzer.analyze("the and to this under")
        assertEquals(0.0, resultNeutral.compound, 0.0)
        assertTrue(resultNeutral.recognizedTokens >= 4)
        assertTrue(resultNeutral.oovRatio < 0.50)

        // "like" should retain its positive sentiment from the base VADER lexicon
        val resultLike = analyzer.analyze("like")
        assertTrue(resultLike.compound > 0.05)

        // "share" should retain its positive sentiment from the base VADER lexicon
        val resultShare = analyzer.analyze("share")
        assertTrue(resultShare.compound > 0.05)
    }

    @Test
    fun testEmojiSentiment() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        val result1 = analyzer.analyze("😭")
        assertTrue("Compound score for 😭 should be negative", result1.compound < -0.05)
        assertEquals("Should recognize 1 emoji token", 1, result1.recognizedTokens)

        val result2 = analyzer.analyze("maganda ❤️")
        assertTrue("❤️ should boost positive sentiment", result2.compound > 0.4)
        assertEquals("Should recognize 2 tokens", 2, result2.recognizedTokens)
    }

    @Test
    fun handlesCapitalizationAmplification() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        
        // When mixed with lowercase words, ALL CAPS words get amplified
        val normal = analyzer.analyze("this is good")
        val amplified = analyzer.analyze("this is GOOD")
        
        assertTrue("Amplified capital sentiment should be stronger", amplified.compound > normal.compound)
    }

    @Test
    fun handlesPunctuationEmphasis() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        
        val normal = analyzer.analyze("this is good")
        val punctuated = analyzer.analyze("this is good!!!")
        
        assertTrue("Exclamation points should amplify sentiment", punctuated.compound > normal.compound)
    }

    @Test
    fun handlesBoosterWords() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        
        val normalEn = analyzer.analyze("this is good")
        val boostedEn = analyzer.analyze("this is very good")
        assertTrue("Booster 'very' should amplify sentiment", boostedEn.compound > normalEn.compound)

        val normalTl = analyzer.analyze("maganda ito")
        val boostedTl = analyzer.analyze("sobrang maganda ito")
        assertTrue("Tagalog booster 'sobrang' should amplify sentiment", boostedTl.compound > normalTl.compound)
    }

    @Test
    fun handlesContrastiveConjunctions() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        
        // "good but bad" -> VADER rules weight the second half (bad) higher (1.5x) and first half lower (0.5x)
        val resultEn = analyzer.analyze("good but bad")
        assertTrue("Contrastive 'but' should make compound negative", resultEn.compound < -0.05)

        val resultTl = analyzer.analyze("maganda pero pangit")
        assertTrue("Contrastive 'pero' should make compound negative", resultTl.compound < -0.05)
    }

    @Test
    fun handlesEmoticons() {
        val analyzer = VADERCompatibleAnalyzer(MvlLexicon.extensionLexicon)
        
        val happy = analyzer.analyze("happy :)")
        val sad = analyzer.analyze("sad :(")
        
        assertTrue("Emoticons should be tokenized and contribute to positive compound", happy.compound > 0.1)
        assertTrue("Sad emoticons should contribute to negative compound", sad.compound < -0.1)
    }

    @Test
    fun loadsNrcVadLexiconFromFile() {
        val file = java.io.File("src/main/assets/nrc_vad_lexicon.txt")
        assertTrue("nrc_vad_lexicon.txt should exist in assets", file.exists())
        
        val lines = file.readLines()
        assertTrue("nrc_vad_lexicon.txt should not be empty", lines.isNotEmpty())
        
        val parsed = VADERCompatibleAnalyzer.parseLexicon(lines)
        assertTrue("Should contain parsed words", parsed.isNotEmpty())
        
        // Assert specific words mapped from NRC-VAD
        // "aback": (0.385 - 0.5) * 8.0 = -0.92
        // "ability": (0.875 - 0.5) * 8.0 = 3.0
        assertEquals(-0.92, parsed.getValue("aback"), 0.001)
        assertEquals(3.0, parsed.getValue("ability"), 0.001)
        
        // Verify it works in VADERCompatibleAnalyzer
        val analyzer = VADERCompatibleAnalyzer(parsed, MvlLexicon.extensionLexicon)
        val result = analyzer.analyze("aback ability")
        assertTrue(result.recognizedTokens >= 2)
        assertEquals(0.0, result.oovRatio, 0.0) // 100% recognized, OOV is 0.0
    }
}
