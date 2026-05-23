# Filipino MVL Expert Review System Primer

## Review Context

| Item | Details |
|---|---|
| System | REDU Android app sentiment module |
| Purpose of review | Validate Filipino and Taglish terms used to extend the app's VADER-compatible sentiment analyzer |
| Source of candidate sentiment words | `android-app/app/src/main/java/edu/feutech/redu/sentiment/MvlLexicon.kt`, `MvlLexicon.defaultEntries` |
| Source of stop words | `android-app/app/src/main/java/edu/feutech/redu/sentiment/MvlLexicon.kt`, `MvlLexicon.tagalogStopWords` |
| Sentiment method | VADER-compatible lexicon and rule-based scoring |
| Expert task 1 | Rate each MVL candidate word from `-4` to `+4` based on common Filipino social-media usage |
| Expert task 2 | Confirm whether each Filipino stop word can be treated as sentiment-neutral with valence `0.0` |
| Post-review use | Terms with acceptable agreement across three experts will be retained and integrated into the Android app's runtime lexicon |

## System Summary

| Concept | Explanation |
|---|---|
| VADER-compatible sentiment scoring | The app analyzes caption and comment text using tokenization, lexicon lookup, and VADER-style rules such as negation, boosters, capitalization, and punctuation emphasis. |
| Compound score | The app converts the summed valence of recognized tokens into a bounded score from `-1.0` to `+1.0`. Scores below `-0.05` are treated as negative. |
| MVL | The Minimum Viable Lexicon is a small Filipino and Taglish lexicon added to improve analysis of Philippine social-media text. |
| Expert polarity rating | Experts independently rate candidate words using VADER's original `-4` to `+4` valence convention. |
| Agreement screening | A candidate term is retained only if the three expert ratings are within plus or minus 1 of the mean rating. |
| Stop-word neutrality | Filipino stop words are assigned `0.0` valence. They reduce out-of-vocabulary counts but should not add positive or negative sentiment. |

## Rating Scale

| Rating | Meaning | Use when the term is commonly... |
|---:|---|---|
| `-4` | Most negative | Extremely negative, hostile, insulting, distressing, or harmful |
| `-3` | Strongly negative | Clearly negative with strong emotional force |
| `-2` | Moderately negative | Negative, unpleasant, sad, angry, fearful, or critical |
| `-1` | Slightly negative | Mildly negative or contextually unfavorable |
| `0` | Neutral | Not sentiment-bearing, unclear, or dependent on context |
| `+1` | Slightly positive | Mildly favorable or pleasant |
| `+2` | Moderately positive | Clearly positive, happy, approving, or favorable |
| `+3` | Strongly positive | Very positive or strongly approving |
| `+4` | Most positive | Extremely positive, joyful, affectionate, admiring, or celebratory |

## Expert Instructions

| Step | What to do | Output needed |
|---:|---|---|
| 1 | Read the system context and rating scale above. | No entry required |
| 2 | For each MVL candidate word, assign a sentiment rating from `-4` to `+4`. Decimals are allowed if needed. | Fill the `Expert rating (-4 to +4)` column |
| 3 | If a word is ambiguous, slang-dependent, offensive, or context-sensitive, write a short note. | Fill the `Notes / concern` column |
| 4 | For each Filipino stop word, decide whether it is acceptable as sentiment-neutral. | Fill `Yes`, `No`, or `Revise` under the approval column |
| 5 | For any stop word that should not be neutral, explain why and suggest a change. | Fill the stop-word notes column |
| 6 | Complete the final approval table at the end of this document. | Name, role, decision, date, and signature |

## MVL Candidate Word Rating Table

| No. | Candidate ID | Filipino / Taglish term | Expert rating (-4 to +4) | Notes / concern |
|---:|---|---|---:|---|
| 1 | MVL001 | aliw |  |  |
| 2 | MVL002 | ayaw |  |  |
| 3 | MVL003 | ayos |  |  |
| 4 | MVL004 | bilib |  |  |
| 5 | MVL005 | bobo |  |  |
| 6 | MVL006 | buhay |  |  |
| 7 | MVL007 | bwisit |  |  |
| 8 | MVL008 | dusa |  |  |
| 9 | MVL009 | gago |  |  |
| 10 | MVL010 | galing |  |  |
| 11 | MVL011 | galit |  |  |
| 12 | MVL012 | gusto |  |  |
| 13 | MVL013 | hirap |  |  |
| 14 | MVL014 | inis |  |  |
| 15 | MVL015 | iyak |  |  |
| 16 | MVL016 | kasalanan |  |  |
| 17 | MVL017 | kilig |  |  |
| 18 | MVL018 | kupal |  |  |
| 19 | MVL019 | ligaya |  |  |
| 20 | MVL020 | lodi |  |  |
| 21 | MVL021 | luha |  |  |
| 22 | MVL022 | lungkot |  |  |
| 23 | MVL023 | mabait |  |  |
| 24 | MVL024 | maganda |  |  |
| 25 | MVL025 | mahal |  |  |
| 26 | MVL026 | mahirap |  |  |
| 27 | MVL027 | masama |  |  |
| 28 | MVL028 | masaya |  |  |
| 29 | MVL029 | nakakagalit |  |  |
| 30 | MVL030 | nakakainis |  |  |
| 31 | MVL031 | nakakalungkot |  |  |
| 32 | MVL032 | nakakatakot |  |  |
| 33 | MVL033 | ngiti |  |  |
| 34 | MVL034 | paborito |  |  |
| 35 | MVL035 | pag-asa |  |  |
| 36 | MVL036 | pangit |  |  |
| 37 | MVL037 | payapa |  |  |
| 38 | MVL038 | petmalu |  |  |
| 39 | MVL039 | pighati |  |  |
| 40 | MVL040 | poot |  |  |
| 41 | MVL041 | sakit |  |  |
| 42 | MVL042 | salamat |  |  |
| 43 | MVL043 | selos |  |  |
| 44 | MVL044 | sigla |  |  |
| 45 | MVL045 | sisi |  |  |
| 46 | MVL046 | swerte |  |  |
| 47 | MVL047 | takot |  |  |
| 48 | MVL048 | talo |  |  |
| 49 | MVL049 | tanga |  |  |
| 50 | MVL050 | tawa |  |  |
| 51 | MVL051 | tiwala |  |  |
| 52 | MVL052 | tuwa |  |  |
| 53 | MVL053 | wagi |  |  |

## Filipino Stop-Word Neutrality Review Table

| No. | Stop word | Proposed sentiment value | Approved as neutral? | Notes / suggested change |
|---:|---|---:|---|---|
| 1 | ang | `0.0` |  |  |
| 2 | ng | `0.0` |  |  |
| 3 | sa | `0.0` |  |  |
| 4 | mga | `0.0` |  |  |
| 5 | na | `0.0` |  |  |
| 6 | ay | `0.0` |  |  |
| 7 | at | `0.0` |  |  |
| 8 | pa | `0.0` |  |  |
| 9 | si | `0.0` |  |  |
| 10 | ni | `0.0` |  |  |
| 11 | kay | `0.0` |  |  |
| 12 | lang | `0.0` |  |  |
| 13 | naman | `0.0` |  |  |
| 14 | ba | `0.0` |  |  |
| 15 | ito | `0.0` |  |  |
| 16 | iyon | `0.0` |  |  |
| 17 | doon | `0.0` |  |  |
| 18 | dito | `0.0` |  |  |
| 19 | nito | `0.0` |  |  |
| 20 | niyan | `0.0` |  |  |
| 21 | nila | `0.0` |  |  |
| 22 | namin | `0.0` |  |  |
| 23 | ninyo | `0.0` |  |  |
| 24 | ako | `0.0` |  |  |
| 25 | ikaw | `0.0` |  |  |
| 26 | siya | `0.0` |  |  |
| 27 | kami | `0.0` |  |  |
| 28 | tayo | `0.0` |  |  |
| 29 | kayo | `0.0` |  |  |
| 30 | sila | `0.0` |  |  |
| 31 | ko | `0.0` |  |  |
| 32 | mo | `0.0` |  |  |
| 33 | niya | `0.0` |  |  |
| 34 | akin | `0.0` |  |  |
| 35 | iyo | `0.0` |  |  |
| 36 | kanila | `0.0` |  |  |
| 37 | kanya | `0.0` |  |  |
| 38 | ating | `0.0` |  |  |
| 39 | aming | `0.0` |  |  |
| 40 | inyong | `0.0` |  |  |
| 41 | kapag | `0.0` |  |  |
| 42 | dahil | `0.0` |  |  |
| 43 | pero | `0.0` |  |  |
| 44 | kasi | `0.0` |  |  |
| 45 | upang | `0.0` |  |  |
| 46 | habang | `0.0` |  |  |
| 47 | bago | `0.0` |  |  |
| 48 | kaya | `0.0` |  |  |
| 49 | sana | `0.0` |  |  |
| 50 | nga | `0.0` |  |  |
| 51 | din | `0.0` |  |  |
| 52 | rin | `0.0` |  |  |
| 53 | raw | `0.0` |  |  |
| 54 | daw | `0.0` |  |  |
| 55 | kung | `0.0` |  |  |
| 56 | para | `0.0` |  |  |
| 57 | kapwa | `0.0` |  |  |
| 58 | lamang | `0.0` |  |  |

## Expert Approval Table

| Field | Response |
|---|---|
| Overall decision | Approved / Approved with revisions / Not approved |
| Expert name |  |
| Affiliation / role |  |
| Date reviewed |  |
| Signature |  |
| General comments |  |

## Researcher Processing After Expert Review

| Step | Researcher action |
|---:|---|
| 1 | Collect completed primers from three Filipino language experts. |
| 2 | Compute the mean valence rating for each MVL candidate term. |
| 3 | Retain only terms where all three ratings are within plus or minus 1 of the mean. |
| 4 | Use the retained term mean as the final MVL valence score. |
| 5 | Review stop-word approvals and revise the neutral stop-word list if any expert flags a term. |
| 6 | Update the Android app lexicon and document the final retained terms in the thesis. |
