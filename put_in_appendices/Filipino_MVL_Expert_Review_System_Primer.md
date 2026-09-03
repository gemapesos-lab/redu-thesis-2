# Filipino MVL Expert Review System Primer

## Review Context

| Item | Details |
| :--- | :--- |
| **System** | REDU Android app sentiment module |
| **Purpose of review** | Validate Filipino and Taglish terms used to extend the app's VADER-compatible sentiment analyzer |
| **Sentiment method** | VADER-compatible lexicon and rule-based scoring |
| **Expert task 1** | Rate each MVL candidate word from -4 to +4 based on common Filipino social media usage |
| **Expert task 2** | Confirm whether each Filipino stop word can be treated as sentiment-neutral with valence 0.0 |
| **Post-review use** | Terms with acceptable agreement across three experts will be retained and integrated into the Android app's runtime lexicon |

## System Summary

| Concept | Explanation |
| :--- | :--- |
| **VADER compatible sentiment scoring** | The app analyzes caption and comment text using tokenization, lexicon lookup, and VADER-style rules such as negation, boosters, capitalization, and punctuation emphasis. |
| **Compound score** | The app converts the summed valence of recognized tokens into a bounded score from -1.0 to +1.0. Scores below -0.05 are treated as negative. |
| **MVL** | The Minimum Viable Lexicon is a small Filipino and Taglish lexicon added to improve analysis of Philippine social-media text. |
| **Expert polarity rating** | Experts independently rate candidate words using VADER's original -4 to +4 valence convention. |
| **Stop-word neutrality** | Filipino stop words are assigned 0.0 valence. They reduce out-of-vocabulary counts but should not add positive or negative sentiment. |

## Rating Scale

| Rating | Meaning | Use when the term is commonly... |
| :--- | :--- | :--- |
| -4 | Most negative | Extremely negative, hostile, insulting, distressing, or harmful |
| -3 | Strongly negative | Clearly negative with strong emotional force |
| -2 | Moderately negative | Negative, unpleasant, sad, angry, fearful, or critical |
| -1 | Slightly negative | Mildly negative or contextually unfavorable |
| 0 | Neutral | Not sentiment-bearing, unclear, or dependent on context |
| +1 | Slightly positive | Mildly favorable or pleasant |
| +2 | Moderately positive | Clearly positive, happy, approving, or favorable |
| +3 | Strongly positive | Very positive or strongly approving |
| +4 | Most positive | Extremely positive, joyful, affectionate, admiring, or celebratory |

## Expert Instructions

1. **Step 1:** Read the system context and rating scale above. (No entry required)
2. **Step 2:** For each MVL candidate word, assign a sentiment rating from -4 to +4. Decimals are allowed if needed. (Fill the **Expert rating** column)
3. **Step 3:** If a word is ambiguous, slang-dependent, offensive, or context-sensitive, write a short note. (Fill the **Notes / concern** column)
4. **Step 4:** For each Filipino stop word, decide whether it is acceptable as sentiment-neutral. (Fill **Yes**, **No**, or **Revise** under the approval column)
5. **Step 5:** For any stop word that should not be neutral, explain why and suggest a change. (Fill the **stop-word notes** column)
6. **Step 6:** Complete the final approval table at the end of this document. (Name, role, decision, date, and signature)

## MVL Candidate Word Rating Table

| No. | Candidate ID | Filipino / Taglish term | Expert rating (-4 to +4) | Notes / concern |
| :--- | :--- | :--- | :--- | :--- |
| 1 | MVL001 | aliw | +2 | Conveys amusement or entertainment. |
| 2 | MVL002 | ayaw | -2 | Indicates refusal or dislike. |
| 3 | MVL003 | ayos | +2 | Means "okay," "good," or "fixed." |
| 4 | MVL004 | bilib | +3 | Strongly implies being impressed or in awe. |
| 5 | MVL005 | bobo | -4 | Highly offensive insult (stupid/dumb). |
| 6 | MVL006 | buhay | 0 | We can add that it's a heteronym. Neutral on its own. Developers must account for stress/context: búhay (noun: life) vs. buháy (adjective: alive, which leans positive). |
| 7 | MVL007 | bwisit | -3 | Slang for annoying, frustrating, or bad luck. |
| 8 | MVL008 | dusa | -3 | Denotes suffering or hardship. |
| 9 | MVL009 | gago | -4 | Severe insult; highly negative. |
| 10 | MVL010 | galing | +3 | Expresses excellence or skill. |
| 11 | MVL011 | galit | -3 | Denotes anger or rage. |
| 12 | MVL012 | gusto | +2 | Means "like" or "want." |
| 13 | MVL013 | hirap | -2 | Means difficult, hard, or poor. |
| 14 | MVL014 | inis | -2 | Conveys annoyance or irritation. |
| 15 | MVL015 | iyak | -2 | Root word. In social media, often used as a command/mockery ('iyak na lang') rather than literal sadness. |
| 16 | MVL016 | kasalanan | -2 | Means sin, fault, or blame. |
| 17 | MVL017 | kilig | +3 | Romantic excitement; unique cultural term. |
| 18 | MVL018 | kupal | -4 | Highly offensive slang (arrogant/jerk). |
| 19 | MVL019 | ligaya | +3 | Deep joy or happiness. |
| 20 | MVL020 | lodi | +3 | Slang ("idol" reversed); expresses admiration. |
| 21 | MVL021 | luha | -2 | Associated with tears and sadness. |
| 22 | MVL022 | lungkot | -3 | Direct translation of sadness. |
| 23 | MVL023 | mabait | +3 | Means kind or good-natured. |
| 24 | MVL024 | maganda | +3 | Means beautiful or good. |
| 25 | MVL025 | mahal | +3 | Highly context-dependent. Positive (+3) when used as a noun/verb (love). Slightly negative (-1) when used as an adjective for commerce (expensive). |
| 26 | MVL026 | mahirap | -2 | Means poor or difficult. |
| 27 | MVL027 | masama | -3 | Means bad or evil. |
| 28 | MVL028 | masaya | +3 | Means happy or joyful. |
| 29 | MVL029 | nakagagalit | -3 | Infuriating or rage-inducing. |
| 30 | MVL030 | nakaiinis | -2 | Annoying or frustrating. |
| 31 | MVL031 | nakalulungkot | -3 | Saddening or depressing. |
| 32 | MVL032 | nakatatakot | -2 | Scary or frightening. |
| 33 | MVL033 | ngiti | +2 | Means smile. |
| 34 | MVL034 | paborito | +3 | Means favorite. |
| 35 | MVL035 | pag-asa | +3 | Means hope. |
| 36 | MVL036 | pangit | -3 | Means ugly or bad-quality. |
| 37 | MVL037 | payapa | +2 | Means peaceful. |
| 38 | MVL038 | petmalu | +3 | Slang ("malupit" reversed); means awesome. |
| 39 | MVL039 | pighati | -3 | Deep sorrow or grief. |
| 40 | MVL040 | poot | -4 | Extreme hatred or wrath. |
| 41 | MVL041 | sakit | -3 | Means pain, sickness, or hurt. |
| 42 | MVL042 | salamat | +3 | Means thank you or gratitude. |
| 43 | MVL043 | selos | -2 | Means jealousy. |
| 44 | MVL044 | sigla | +3 | Vitality, energy, or enthusiasm. |
| 45 | MVL045 | sisi | -2 | Blame or regret. |
| 46 | MVL046 | swerte | +3 | Means lucky. |
| 47 | MVL047 | takot | -2 | Means fear or afraid. |
| 48 | MVL048 | talo | -2 | Means to lose; often used negatively ("loser"). |
| 49 | MVL049 | tanga | -4 | Highly offensive insult (idiot/stupid). |
| 50 | MVL050 | tawa | +2 | Means laugh or laughter. |
| 51 | MVL051 | tiwala | +3 | Means trust or confidence. |
| 52 | MVL052 | tuwa | +3 | Joy or delight. |
| 53 | MVL053 | wagi | +3 | Means victorious or to win. |

## Filipino Stop-Word Neutrality Review Table

| No. | Stop word | Proposed sentiment value | Approved as neutral? | Notes / suggested change |
| :--- | :--- | :--- | :--- | :--- |
| 1 | ang | 0.0 | Yes | Article marker. Neutral. |
| 2 | ng | 0.0 | Yes | Preposition/marker. Neutral. |
| 3 | sa | 0.0 | Yes | Preposition. Neutral. |
| 4 | mga | 0.0 | Yes | Plural marker. Neutral. |
| 5 | na | 0.0 | Yes | Adverb/linker. Neutral. |
| 6 | ay | 0.0 | Yes | Inversion marker. Neutral. |
| 7 | at | 0.0 | Yes | Conjunction ("and"). Neutral. |
| 8 | pa | 0.0 | Yes | Enclitic particle. Neutral. |
| 9 | si | 0.0 | Yes | Personal marker. Neutral. |
| 10 | ni | 0.0 | Yes | Personal marker. Neutral. |
| 11 | kay | 0.0 | Yes | Personal marker. Neutral. |
| 12 | lang | 0.0 | Yes | Enclitic ("only"). Neutral. |
| 13 | naman | 0.0 | Yes | Enclitic. Neutral. |
| 14 | ba | 0.0 | Yes | Question marker. Neutral. |
| 15 | ito | 0.0 | Yes | Pronoun. Neutral. |
| 16 | iyon | 0.0 | Yes | Pronoun. Neutral. |
| 17 | doon | 0.0 | Yes | Pronoun. Neutral. |
| 18 | dito | 0.0 | Yes | Pronoun. Neutral. |
| 19 | nito | 0.0 | Yes | Pronoun. Neutral. |
| 20 | niyan | 0.0 | Yes | Pronoun. Neutral. |
| 21 | nila | 0.0 | Yes | Pronoun. Neutral. |
| 22 | namin | 0.0 | Yes | Pronoun. Neutral. |
| 23 | ninyo | 0.0 | Yes | Pronoun. Neutral. |
| 24 | ako | 0.0 | Yes | Pronoun. Neutral. |
| 25 | ikaw | 0.0 | Yes | Pronoun. Neutral. |
| 26 | siya | 0.0 | Yes | Pronoun. Neutral. |
| 27 | kami | 0.0 | Yes | Pronoun. Neutral. |
| 28 | tayo | 0.0 | Yes | Pronoun. Neutral. |
| 29 | kayo | 0.0 | Yes | Pronoun. Neutral. |
| 30 | sila | 0.0 | Yes | Pronoun. Neutral. |
| 31 | ko | 0.0 | Yes | Pronoun. Neutral. |
| 32 | mo | 0.0 | Yes | Pronoun. Neutral. |
| 33 | niya | 0.0 | Yes | Pronoun. Neutral. |
| 34 | akin | 0.0 | Yes | Pronoun. Neutral. |
| 35 | iyo | 0.0 | Yes | Pronoun. Neutral. |
| 36 | kanila | 0.0 | Yes | Pronoun. Neutral. |
| 37 | kanya | 0.0 | Yes | Pronoun. Neutral. |
| 38 | ating | 0.0 | Yes | Pronoun. Neutral. |
| 39 | aming | 0.0 | Yes | Pronoun. Neutral. |
| 40 | inyong | 0.0 | Yes | Pronoun. Neutral. |
| 41 | kapag | 0.0 | Yes | Conjunction. Neutral. |
| 42 | dahil | 0.0 | Yes | Conjunction. Neutral. |
| 43 | pero | 0.0 | Yes | Conjunction ("but"). Modifies context, but neutral alone. |
| 44 | kasi | 0.0 | Yes | Conjunction. Neutral. |
| 45 | upang | 0.0 | Yes | Conjunction. Neutral. |
| 46 | habang | 0.0 | Yes | Conjunction. Neutral. |
| 47 | bago | 0.0 | Yes | Stop word meaning "before". Neutral alone. |
| 48 | kaya | 0.0 | Yes | Conjunction. Neutral. |
| 49 | sana | 0.0 | Yes | Optative particle. Neutral baseline for VADER. |
| 50 | nga | 0.0 | Yes | Enclitic particle. Neutral. |
| 51 | din | 0.0 | Yes | Enclitic particle. Neutral. |
| 52 | rin | 0.0 | Yes | Enclitic particle. Neutral. |
| 53 | raw | 0.0 | Yes | Enclitic particle. Neutral. |
| 54 | daw | 0.0 | Yes | Enclitic particle. Neutral. |
| 55 | kung | 0.0 | Yes | Conjunction. Neutral. |
| 56 | para | 0.0 | Yes | Preposition/Conjunction. Neutral. |
| 57 | kapwa | 0.0 | Yes | Pronoun/Adjective. Neutral. |
| 58 | lamang | 0.0 | Yes | Adverb. Neutral. |

## Expert Approval Table

| Field | Response |
| :--- | :--- |
| **Overall decision** | Approved with revisions |
| **Expert name** | Dr. Joshua Urrete, LPT |
| **Affiliation / role** | Associate Professor 1, National University, Philippines / Professional Translator and Validator |
| **Signature** | *(Signed: Joshua Urrete)* |
| **General comments** | The Minimum Viable Lexicon effectively captures contemporary Filipino and Taglish social media semantics. Developers **should implement context-aware rules for heteronyms and dual-meaning words** (e.g., mahal as "love" vs. "expensive"). The selected stop-words function correctly as 0.0 valence structural markers. |