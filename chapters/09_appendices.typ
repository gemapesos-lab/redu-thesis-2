// ==========================================
// APPENDICES
// ==========================================

#import "utils.typ": continued_thesis_table, table_align, thesis_table

#pagebreak()

#heading(level: 1, numbering: none)[APPENDICES]

#v(2em)

#heading(level: 1, numbering: none)[APPENDIX A]
#v(1em)
#align(center)[*Complete 27-Rule Fuzzy Inference Base*]

#v(1em)

The following table presents the complete 27-rule inference base used by the
three-input fuzzy engine (Video Dwell Time, Negative Sentiment Density, Session Duration).
The 27 rows arise from exhaustive coverage of the $3 times 3 times 3$ input-state
combinations. Their Safe/Warning/Critical outputs are fixed study priors intended
for transparent heuristic inference rather than empirically calibrated probabilities
or clinical thresholds. The assignment logic is conservative: Safe is reserved for
low-baseline or isolated single-signal states, Warning is the default for mixed or
moderate states, and Critical is reserved for combinations where the main risk axes
converge under the fixed study priors.

#thesis_table(
  caption: [Full 27-Rule Fuzzy Inference Base],
  columns: (0.65fr, 1.1fr, 1.1fr, 1.1fr, 0.95fr),
  cell_align: table_align((center, center, center, center, center)),
  header: (
    [*No.*],
    [*Video Dwell Time*],
    [*Negative Sentiment Density*],
    [*Session Duration*],
    [*Risk Level*],
  ),
  body: (
    [1], [Low], [Low], [Low], [Safe],
    [2], [Low], [Low], [Medium], [Safe],
    [3], [Low], [Low], [High], [Warning],
    [4], [Low], [Medium], [Low], [Safe],
    [5], [Low], [Medium], [Medium], [Warning],
    [6], [Low], [Medium], [High], [Warning],
    [7], [Low], [High], [Low], [Warning],
    [8], [Low], [High], [Medium], [Warning],
    [9], [Low], [High], [High], [Critical],
    [10], [Medium], [Low], [Low], [Safe],
    [11], [Medium], [Low], [Medium], [Warning],
    [12], [Medium], [Low], [High], [Warning],
    [13], [Medium], [Medium], [Low], [Warning],
    [14], [Medium], [Medium], [Medium], [Warning],
    [15], [Medium], [Medium], [High], [Critical],
    [16], [Medium], [High], [Low], [Warning],
  ),
)

#continued_thesis_table(
  caption: [Table 19. Full 27-Rule Fuzzy Inference Base (continued)],
  columns: (0.65fr, 1.1fr, 1.1fr, 1.1fr, 0.95fr),
  cell_align: table_align((center, center, center, center, center)),
  header: (
    [*No.*],
    [*Video Dwell Time*],
    [*Negative Sentiment Density*],
    [*Session Duration*],
    [*Risk Level*],
  ),
  body: (
    [17], [Medium], [High], [Medium], [Critical],
    [18], [Medium], [High], [High], [Critical],
    [19], [High], [Low], [Low], [Safe],
    [20], [High], [Low], [Medium], [Warning],
    [21], [High], [Low], [High], [Warning],
    [22], [High], [Medium], [Low], [Warning],
    [23], [High], [Medium], [Medium], [Critical],
    [24], [High], [Medium], [High], [Critical],
    [25], [High], [High], [Low], [Warning],
    [26], [High], [High], [Medium], [Critical],
    [27], [High], [High], [High], [Critical],
  ),
)

Interpretive note: the table follows a mostly non-decreasing assignment pattern
in Session Duration and Negative Sentiment Density, while using Video Dwell Time
as a contextual modifier rather than a standalone high-risk trigger. One
deliberate exception is retained as a precautionary prototype rule for rapid
negative-content chaining. Low dwell combined with high Negative Sentiment
Density and high session duration is classified as *Critical* because rapid
movement does not reduce concern when the session is both prolonged and clearly
negative-dominant overall. By contrast, Rule 19 (High Dwell, Low NSD, Low
Session Duration) stays *Safe* because sustained attention to one item, without
prolonged use or negative-dominant exposure, is not enough by itself to imply
doomscrolling. This keeps dwell time as a modifier, not the main driver.

Operationally, the 27 consequents follow a compact assignment grammar rather
than 27 unrelated judgments: no rule is *Critical* when both Session Duration
and Negative Sentiment Density are Low; duration alone does not produce
*Critical* when NSD remains Low; High NSD reaches *Critical* only when
reinforced by another non-low axis; High Dwell Time alone is insufficient for
*Critical*; and *Warning* remains the default mixed class when the signals are
concerning but not yet strongly convergent.

Read in groups, the table has three layers. Rules 1, 2, 4, 10, and 19 remain
*Safe* because at least two axes stay low. Rules 3, 5-8, 11-14, 16, 20-22,
and 25 remain *Warning* because they show mixed evidence without strong
convergence between duration and negativity. Rules 9, 15, 17-18, 23-24, and
26-27 become *Critical* because prolonged use and negative exposure rise
together, or because High NSD is reinforced by High Dwell Time in sessions
that are no longer brief.

Within that Critical layer, the strongest challenge cases are still explainable.
Rule 15 becomes *Critical* because a High-duration session with both other axes
already at Medium no longer reflects time alone. Rule 23 becomes *Critical*
because High Dwell Time, Medium NSD, and Medium Session Duration already show
three non-low signals in the same direction. Rule 25 is retained as *Warning*
after psychology expert review because high negative exposure and high dwell in
a short session can be concerning, but the brief duration is not sufficient for
the strongest intervention level.

#pagebreak()

#heading(level: 1, numbering: none)[APPENDIX B]
#v(1em)
#align(center)[*Fallback Rules and Trigger Thresholds*]

#v(1em)

Unless otherwise noted, the following thresholds are fixed study priors selected for reproducibility and conservative intervention behavior. They are design parameters, not empirically validated clinical or population cutoffs. Because the fallback engine retains two inputs with three linguistic levels each, exhaustive coverage requires $3^2 = 9$ rules.

In the implemented Android system, the two-input fallback rule base below is reserved for *Sentiment-Unreliable* sessions, specifically sessions where negativity cannot be resolved reliably after the available text or no-text path is attempted. This includes sessions whose extracted text is at least half unresolved vocabulary (study-defined unreliability trigger: session OOV ratio >= 50%) and sessions where the no-text VLM path cannot produce a stable item label because `AccessibilityService.takeScreenshot` is unavailable, screenshot capture fails, or VLM inference fails. The `>= 50%` value is not treated as a validated linguistic cutoff. It is a mathematically derived majority-representativeness screen: once unresolved tokens reach half of the valid tokens in a session, the recognized tokens no longer form the majority of the lexical content available to the text path. This is therefore used as a practical safeguard for noisy code-mixed text with spelling variation, lexical borrowing, and other textual variation rather than as a claimed language norm (Mohammed & Prasad, 2023; Perera & Caldera, 2024; Nazir et al., 2026; Khan et al., 2025). In those cases, the system degrades to a two-input fallback rule base (Video Dwell Time + Session Duration):

#thesis_table(
  caption: [Fallback 9-Rule Base (Sentiment-Unreliable Sessions)],
  columns: (0.75fr, 1.2fr, 1.2fr, 1fr),
  cell_align: table_align((center, center, center, center)),
  header: (
    [*No.*],
    [*Video Dwell Time*],
    [*Session Duration*],
    [*Risk Level*],
  ),
  body: (
    [1], [Low], [Low], [Safe],
    [2], [Low], [Medium], [Safe],
    [3], [Low], [High], [Warning],
    [4], [Medium], [Low], [Safe],
    [5], [Medium], [Medium], [Warning],
    [6], [Medium], [High], [Warning],
    [7], [High], [Low], [Warning],
  ),
)

#continued_thesis_table(
  caption: [Table 20. Fallback 9-Rule Base (Sentiment-Unreliable Sessions) (continued)],
  columns: (0.75fr, 1.2fr, 1.2fr, 1fr),
  cell_align: table_align((center, center, center, center)),
  header: (
    [*No.*],
    [*Video Dwell Time*],
    [*Session Duration*],
    [*Risk Level*],
  ),
  body: (
    [8], [High], [Medium], [Critical],
    [9], [High], [High], [Critical],
  ),
)

In Week 1 and in any participant without locked personalization, both fallback inputs use the global memberships reported in Appendix C. Once Week 2 personalization is locked, the fallback engine retains the same 9-rule consequents and RiskScore cutoffs but replaces only the Session Duration memberships with participant-specific Week 1 duration quantiles; Video Dwell Time remains fixed. This is because the fallback mode reuses the same live prompt engine's duration memberships while dropping only the sentiment input.

RiskScore cutoffs used across both full and fallback modes (equal-width reporting categories on the normalized 0-100 scale):
- Safe: 0.00 to 33.32
- Warning: 33.33 to 66.66
- Critical: 66.67 to 100

Prompt mapping (study-specified defaults derived from the reporting bands):
- Level 1: RiskScore 33.33 to 49.99
- Level 2: RiskScore 50.00 to 66.66
- Level 3 (Pause and Reset / Short Breathing Break): RiskScore 66.67 to 100 (disabled in fallback mode)
- The Warning band is split at its midpoint 50.00 so lower- and upper-Warning states receive different prompt intensities without introducing extra off-band cutoffs; Level 3 is reserved for the Critical band.
- Cooldown: fixed 15-minute prototype cooldown between prompts, intentionally reusing the same literature-bounded 15-minute live gate as the minimum re-eligibility interval so the system does not introduce a second unsupported minute constant; a repeat prompt therefore requires another prolonged interval of continued use. Recent JITAI work supports managing timing, receptivity, and dose (Teepe et al., 2021; Wang et al., 2023; Fiedler et al., 2024; Hsu et al., 2025; van Genugten et al., 2025)

#pagebreak()

#heading(level: 1, numbering: none)[APPENDIX C]
#v(1em)
#align(center)[*Core Algorithmic Logic and Reproducibility Specifications*]

#v(1em)

The Android implementation separates fixed analytic priors from Week 2 live-prompt personalization. Fixed global memberships are retained for week-level DSI computation across both study weeks so baseline and intervention summaries remain comparable. A separate live prompt engine is then allowed to replace only the Session Duration and NSD memberships after Week 1; Video Dwell Time remains fixed.

*Fixed Analytic Priors (used for week-level DSI across both study weeks and as the Week 1 live default):*
Where $ "tri"(a, b, c) $ is the standard triangular membership function, the study uses the following fixed memberships:

$ "Low"_"Dwell" = "tri"(0, 0, 5) $
$ "Medium"_"Dwell" = "tri"(4, 12, 20) $
$ "High"_"Dwell" = "tri"(15, 30, 30) $

$ "Low"_"NSD" = "tri"(0, 0, 33) $
$ "Medium"_"NSD" = "tri"(17, 50, 83) $
$ "High"_"NSD" = "tri"(67, 100, 100) $

$ "Low"_"Duration" = "tri"(0, 0, 10) $
$ "Medium"_"Duration" = "tri"(8, 15, 20) $
$ "High"_"Duration" = "tri"(15, 40, 40) $

NSD values are clipped to [0, 100]. These fixed memberships govern week-level analytic DSI computation across both study weeks. In Week 1 they also act as the live default priors before any participant-specific personalization is available.

Where recent studies provide direct numeric anchors, only those anchors are treated as literature-bounded values: 5 s and 30 s for dwell time, 10 min, 15 min, and 40 min for session duration, and the normalized NSD/output partitions derived mathematically from the 0-100 scales. The remaining interior overlap points in the fixed priors are analytic smoothing values used to preserve overlap between adjacent triangular sets and prevent the engine from collapsing into hard step thresholds. They are therefore treated as transition parameters and included in the sensitivity analysis rather than defended as standalone validated behavioral cutoffs.

*Week 2 Prompt Personalization (implemented in the live prompt engine only):*
For participant $u$, let the Week 1 sentiment-reliable-session values for personalized variable $X$ (Session Duration or NSD) be $x_(u,1), x_(u,2), ..., x_(u,n_u)$. The prompt engine computes ordered quantiles:

$ Q_(25,u,X), Q_(50,u,X), Q_(75,u,X), Q_(95,u,X) $

The personalized Week 2 memberships are then:

$ "Low"_(X,"prompt",u) = "tri"(0, 0, Q_(50,u,X)) $
$ "Medium"_(X,"prompt",u) = "tri"(Q_(25,u,X), Q_(50,u,X), Q_(75,u,X)) $
$ "High"_(X,"prompt",u) = "tri"(Q_(50,u,X), Q_(75,u,X), Q_(95,u,X)) $

These quantiles are not treated as validated behavioral cutoffs. They are nonparametric order-statistic anchors chosen to map a three-term Low/Medium/High partition onto each participant's Week 1 distribution with the smallest transparent summary set: $Q_(25)/Q_(50)/Q_(75)$ summarize the interquartile core, while $Q_(95)$ caps the High set without letting one raw maximum set the upper endpoint. Video Dwell Time remains fixed to the global priors above. If $n_u < 10$ sentiment-reliable Week 1 sessions, the default priors are retained for that participant rather than locking personalized bounds. This 10-session minimum remains a numerical sufficiency safeguard rather than a validated population cutoff: below $n_u = 10$, the empirical $Q_(95)$ is governed too heavily by the single largest observation or a nearly maximal order statistic, so the personalized High endpoint becomes too sensitive to one extreme session. Recent personalized intervention-criteria work supports deriving live criteria from a first-week baseline, but recent JITAI reviews also note that empirical decision rules and points remain underdeveloped, which is why the implemented prompt engine uses participant-specific Week 2 criteria only when enough baseline data exist (Ikegaya et al., 2025; Hsu et al., 2025; van Genugten et al., 2025; Elmer et al., 2025). Because fallback prompting uses the same live prompt engine, personalized Session Duration bounds also carry into fallback mode when personalization is available; only the rule base and prompt cap differ.

*Risk Score Category Cutoffs (0-100 scale):*
- Safe: $0 <= "RiskScore" < 33.33$
- Warning: $33.33 <= "RiskScore" < 66.67$
- Critical: $66.67 <= "RiskScore" <= 100$

*Output Centers Used in CoG Aggregation:*
- Safe center: 16.67
- Warning center: 50
- Critical center: 83.33

These fixed centers are the band midpoints of the equal-width thirds above, so they are mathematically derived ordinal anchors rather than empirical risk probabilities. This removes arbitrary-looking output constants while preserving smooth interpolation across mixed-rule activations before the final category label is assigned.

*Intervention Mapping and Cooldown:*
- No intervention when RiskScore < 33.33
- Level 1 (Awareness Notification): $33.33 <= "RiskScore" < 50$
- Level 2 (Pause Prompt): $50 <= "RiskScore" < 66.67$
- Level 3 (Pause and Reset / Short Breathing Break): $66.67 <= "RiskScore" <= 100$
- The Warning band is split at its midpoint 50.00 so lower- and upper-Warning states receive different prompt intensities without introducing extra off-band cutoffs; Level 3 is reserved for the Critical band.
- Fixed cooldown: minimum 15-minute prototype interval between any two prompts, intentionally reusing the same literature-bounded 15-minute live gate as the minimum re-eligibility interval so the system does not introduce a second unsupported minute constant; a repeat prompt therefore requires another prolonged interval of continued use. Recent JITAI work supports managing timing, receptivity, and dose (Teepe et al., 2021; Wang et al., 2023; Fiedler et al., 2024; Hsu et al., 2025; van Genugten et al., 2025)

When the 2-input behavioral fallback mode is active (Sentiment-Unreliable), Level 3 is disabled by safety lock and the maximum permitted prompt is Level 2.

#pagebreak()

#heading(level: 1, numbering: none)[APPENDIX D]
#v(1em)
#align(center)[*Filipino MVL and Neutral OOV-Reduction Inventory*]

#v(1em)

The current Android implementation extends the VADER-compatible analyzer with the Filipino/Taglish Minimum Viable Lexicon (MVL) and neutral Filipino stop-word entries documented in the Filipino MVL review instrument. The Filipino MVL review was completed by one Filipino language expert and cross-checked against the deployed runtime lexicon in the workbook's `mvl_concordance` sheet. The review supports the deployed runtime inventory as a single-expert concordance check rather than as inter-rater validation. All 57 runtime entries (56 affective entries and one neutral candidate entry) matched the deployed valence values within the review window, and the neutral OOV-reduction terms were confirmed as sentiment-neutral. The neutral entries are assigned 0.0 valence only to reduce false OOV inflation from high-frequency Filipino function words; they do not add positive or negative sentiment to the compound score.

#thesis_table(
  caption: [Filipino/Taglish MVL Runtime Terms],
  columns: (1.05fr, 0.7fr, 3.25fr),
  cell_align: table_align((left, center, left)),
  header: (
    [*Runtime group*],
    [*Valence*],
    [*Terms included in the runtime lexicon*],
  ),
  body: (
    [Most negative],
    [-4.0],
    [`bobo`, `gago`, `kupal`, `poot`, `tanga`],

    [Strongly negative],
    [-3.0],
    [`bwisit`, `dusa`, `galit`, `lungkot`, `masama`, `nakagagalit`, `nakakagalit`, `nakalulungkot`, `nakakalungkot`, `pangit`, `pighati`, `sakit`],

    [Moderately negative],
    [-2.0],
    [`ayaw`, `hirap`, `inis`, `iyak`, `kasalanan`, `luha`, `mahirap`, `nakaiinis`, `nakakainis`, `nakatatakot`, `nakakatakot`, `selos`, `sisi`, `takot`, `talo`],

    [Neutral candidate],
    [0.0],
    [`buhay`],

    [Moderately positive],
    [2.0],
    [`aliw`, `ayos`, `gusto`, `ngiti`, `payapa`, `tawa`],

    [Strongly positive],
    [3.0],
    [`bilib`, `galing`, `kilig`, `ligaya`, `lodi`, `mabait`, `maganda`, `mahal`, `masaya`, `paborito`, `pag-asa`, `petmalu`, `salamat`, `sigla`, `swerte`, `tiwala`, `tuwa`, `wagi`],
  ),
)

The runtime list contains the Filipino/Taglish candidate terms used by the app and preserves common/formal spelling counterparts for four `naka-` roots. These counterparts are mapped to the same valence as their corresponding common form so that spelling variation does not unnecessarily increase the OOV ratio.

#pagebreak()

#thesis_table(
  caption: [Neutral Filipino OOV-Reduction Terms],
  columns: (1.3fr, 0.65fr, 3fr),
  cell_align: table_align((left, center, left)),
  header: (
    [*Neutral term group*],
    [*Valence*],
    [*Terms counted as recognized but sentiment-neutral*],
  ),
  body: (
    [Markers, linkers, and particles],
    [0.0],
    [`ang`, `ng`, `sa`, `mga`, `na`, `ay`, `at`, `pa`, `si`, `ni`, `kay`, `lang`, `naman`, `ba`, `nga`, `din`, `rin`, `raw`, `daw`, `lamang`],

    [Demonstratives and locatives],
    [0.0],
    [`ito`, `iyon`, `doon`, `dito`, `nito`, `niyan`],

    [Pronouns and possessives],
    [0.0],
    [`nila`, `namin`, `ninyo`, `ako`, `ikaw`, `siya`, `kami`, `tayo`, `kayo`, `sila`, `ko`, `mo`, `niya`, `akin`, `iyo`, `kanila`, `kanya`, `ating`, `aming`, `inyong`],

    [Connectors and discourse terms],
    [0.0],
    [`kapag`, `dahil`, `pero`, `kasi`, `upang`, `habang`, `bago`, `kaya`, `sana`, `kung`, `para`, `kapwa`],
  ),
)

#pagebreak()

#heading(level: 1, numbering: none)[APPENDIX E]
#v(1em)
#align(center)[*Short-Form Doomscrolling Scale Administration Sheet*]

#v(1em)

The study uses the short-form 4-item *Doomscrolling Scale*, corresponding to Items 1, 2, 10, and 12 of the original 15-item instrument developed by Sharma et al. (2022) and later confirmed in short-form use by Satici et al. (2023). To align self-report with each study week, the instrument is administered with a 7-day recall instruction stem. This modification narrows the recall window only; it does not create a separately validated weekly state instrument.

*Participant Instructions:*
"In the past week, please indicate how strongly you agree with each statement."

*Response Format:*
- 1 = Strongly disagree
- 2 = Disagree
- 3 = Somewhat disagree
- 4 = Neither agree nor disagree
- 5 = Somewhat agree
- 6 = Agree
- 7 = Strongly agree

*Items:*
1. I feel an urge to seek bad news on social media, more and more often.
2. I lose track of time when I read bad news on social media.
3. I find myself continuously browsing negative news.
4. I feel like I am addicted to negative news.

*Scoring Note:*
Item responses are summed or averaged to produce a single doomscrolling score, with higher values indicating greater self-reported doomscrolling during the recalled 7-day period. The 4-item short form contains no reverse-coded items.

#pagebreak()
#heading(level: 1, numbering: none)[APPENDIX F]
#v(1em)
#align(center)[*Filipino MVL Expert Validation Documents*]
#v(1em)
#align(center)[*Filipino MVL Expert Validation Certificate*]
#v(1em)

#table(
  columns: (auto, 1fr),
  [*NAME*], [Dr. Joshua Urrete, LPT],
  [*POSITION / TITLE*], [Associate Professor 1],
  [*PROFESSIONAL ORGANIZATION*], [NU MOA],
  [*NO. OF YEARS OF PROFESSIONAL EXPERIENCE*], [6],
  [*LICENSE NO.*], [1815498],
  [*EDUCATIONAL ATTAINMENT*], [
    Doctor of Philosophy in Education major in Filipino (2025) \
    Master of Arts in Filipino (2022) \
    Bachelor of Secondary Education major in Filipino (2019)
  ],
  [*GRADUATE COURSE(S)*], [
    - Pagsasalin sa Iba't Ibang Disiplina
    - Istruktura ng Wikang Filipino
    - Seminar sa Pagsulat ng Disertasyon
  ]
)

#v(1em)

This certifies that I have personally validated the research instrument developed by Genesis A. Cadigal, Rayan Kennard O. Chuayap, Luigi Karl B. Limos, and Gean Dhylan E. Mapesos for their study titled *"Doomscrolling Detection and Digital Mindfulness Mobile Application for Short-Form Video Platforms using Vader and Fuzzy Logic"* on May 22, 2026. I have also provided the necessary revisions for the further refinement of the tool.

#v(2em)

*Dr. Joshua Urrete, LPT* \
_(Signature)_ \
*Signature Over Printed of Validator*

#pagebreak()
#align(center)[*Filipino MVL Expert Validated Research Tool*]
#v(1em)

= Filipino MVL Expert Review System Primer

== Review Context

#table(
  columns: (auto, 1fr),
  [*Item*], [*Details*],
  [*System*], [REDU Android app sentiment module],
  [*Purpose of review*], [Validate Filipino and Taglish terms used to extend the app's VADER-compatible sentiment analyzer],
  [*Sentiment method*], [VADER-compatible lexicon and rule-based scoring],
  [*Expert task 1*], [Rate each MVL candidate word from -4 to +4 based on common Filipino social media usage],
  [*Expert task 2*], [Confirm whether each Filipino stop word can be treated as sentiment-neutral with valence 0.0],
  [*Post-review use*], [Terms with acceptable agreement across three experts will be retained and integrated into the Android app's runtime lexicon]
)

== System Summary

#table(
  columns: (auto, 1fr),
  [*Concept*], [*Explanation*],
  [*VADER compatible sentiment scoring*], [The app analyzes caption and comment text using tokenization, lexicon lookup, and VADER-style rules such as negation, boosters, capitalization, and punctuation emphasis.],
  [*Compound score*], [The app converts the summed valence of recognized tokens into a bounded score from -1.0 to +1.0. Scores below -0.05 are treated as negative.],
  [*MVL*], [The Minimum Viable Lexicon is a small Filipino and Taglish lexicon added to improve analysis of Philippine social-media text.],
  [*Expert polarity rating*], [Experts independently rate candidate words using VADER's original -4 to +4 valence convention.],
  [*Stop-word neutrality*], [Filipino stop words are assigned 0.0 valence. They reduce out-of-vocabulary counts but should not add positive or negative sentiment.]
)

== Rating Scale

#table(
  columns: (auto, auto, 1fr),
  [*Rating*], [*Meaning*], [*Use when the term is commonly...*],
  [-4], [Most negative], [Extremely negative, hostile, insulting, distressing, or harmful],
  [-3], [Strongly negative], [Clearly negative with strong emotional force],
  [-2], [Moderately negative], [Negative, unpleasant, sad, angry, fearful, or critical],
  [-1], [Slightly negative], [Mildly negative or contextually unfavorable],
  [0], [Neutral], [Not sentiment-bearing, unclear, or dependent on context],
  [+1], [Slightly positive], [Mildly favorable or pleasant],
  [+2], [Moderately positive], [Clearly positive, happy, approving, or favorable],
  [+3], [Strongly positive], [Very positive or strongly approving],
  [+4], [Most positive], [Extremely positive, joyful, affectionate, admiring, or celebratory]
)

== Expert Instructions

+ *Step 1:* Read the system context and rating scale above. (No entry required)
+ *Step 2:* For each MVL candidate word, assign a sentiment rating from -4 to +4. Decimals are allowed if needed. (Fill the *Expert rating* column)
+ *Step 3:* If a word is ambiguous, slang-dependent, offensive, or context-sensitive, write a short note. (Fill the *Notes / concern* column)
+ *Step 4:* For each Filipino stop word, decide whether it is acceptable as sentiment-neutral. (Fill *Yes*, *No*, or *Revise* under the approval column)
+ *Step 5:* For any stop word that should not be neutral, explain why and suggest a change. (Fill the *stop-word notes* column)
+ *Step 6:* Complete the final approval table at the end of this document. (Name, role, decision, date, and signature)

== MVL Candidate Word Rating Table

#table(
  columns: (auto, auto, auto, auto, 1fr),
  [*No.*], [*Candidate ID*], [*Filipino / Taglish term*], [*Expert rating (-4 to +4)*], [*Notes / concern*],
  [1], [MVL001], [aliw], [+2], [Conveys amusement or entertainment.],
  [2], [MVL002], [ayaw], [-2], [Indicates refusal or dislike.],
  [3], [MVL003], [ayos], [+2], [Means "okay," "good," or "fixed."],
  [4], [MVL004], [bilib], [+3], [Strongly implies being impressed or in awe.],
  [5], [MVL005], [bobo], [-4], [Highly offensive insult (stupid/dumb).],
  [6], [MVL006], [buhay], [0], [We can add that it's a heteronym. Neutral on its own. Developers must account for stress/context: búhay (noun: life) vs. buháy (adjective: alive, which leans positive).],
  [7], [MVL007], [bwisit], [-3], [Slang for annoying, frustrating, or bad luck.],
  [8], [MVL008], [dusa], [-3], [Denotes suffering or hardship.],
  [9], [MVL009], [gago], [-4], [Severe insult; highly negative.],
  [10], [MVL010], [galing], [+3], [Expresses excellence or skill.],
  [11], [MVL011], [galit], [-3], [Denotes anger or rage.],
  [12], [MVL012], [gusto], [+2], [Means "like" or "want."],
  [13], [MVL013], [hirap], [-2], [Means difficult, hard, or poor.],
  [14], [MVL014], [inis], [-2], [Conveys annoyance or irritation.],
  [15], [MVL015], [iyak], [-2], [Root word. In social media, often used as a command/mockery ('iyak na lang') rather than literal sadness.],
  [16], [MVL016], [kasalanan], [-2], [Means sin, fault, or blame.],
  [17], [MVL017], [kilig], [+3], [Romantic excitement; unique cultural term.],
  [18], [MVL018], [kupal], [-4], [Highly offensive slang (arrogant/jerk).],
  [19], [MVL019], [ligaya], [+3], [Deep joy or happiness.],
  [20], [MVL020], [lodi], [+3], [Slang ("idol" reversed); expresses admiration.],
  [21], [MVL021], [luha], [-2], [Associated with tears and sadness.],
  [22], [MVL022], [lungkot], [-3], [Direct translation of sadness.],
  [23], [MVL023], [mabait], [+3], [Means kind or good-natured.],
  [24], [MVL024], [maganda], [+3], [Means beautiful or good.],
  [25], [MVL025], [mahal], [+3], [Highly context-dependent. Positive (+3) when used as a noun/verb (love). Slightly negative (-1) when used as an adjective for commerce (expensive).],
  [26], [MVL026], [mahirap], [-2], [Means poor or difficult.],
  [27], [MVL027], [masama], [-3], [Means bad or evil.],
  [28], [MVL028], [masaya], [+3], [Means happy or joyful.],
  [29], [MVL029], [nakagagalit], [-3], [Infuriating or rage-inducing.],
  [30], [MVL030], [nakaiinis], [-2], [Annoying or frustrating.],
  [31], [MVL031], [nakalulungkot], [-3], [Saddening or depressing.],
  [32], [MVL032], [nakatatakot], [-2], [Scary or frightening.],
  [33], [MVL033], [ngiti], [+2], [Means smile.],
  [34], [MVL034], [paborito], [+3], [Means favorite.],
  [35], [MVL035], [pag-asa], [+3], [Means hope.],
  [36], [MVL036], [pangit], [-3], [Means ugly or bad-quality.],
  [37], [MVL037], [payapa], [+2], [Means peaceful.],
  [38], [MVL038], [petmalu], [+3], [Slang ("malupit" reversed); means awesome.],
  [39], [MVL039], [pighati], [-3], [Deep sorrow or grief.],
  [40], [MVL040], [poot], [-4], [Extreme hatred or wrath.],
  [41], [MVL041], [sakit], [-3], [Means pain, sickness, or hurt.],
  [42], [MVL042], [salamat], [+3], [Means thank you or gratitude.],
  [43], [MVL043], [selos], [-2], [Means jealousy.],
  [44], [MVL044], [sigla], [+3], [Vitality, energy, or enthusiasm.],
  [45], [MVL045], [sisi], [-2], [Blame or regret.],
  [46], [MVL046], [swerte], [+3], [Means lucky.],
  [47], [MVL047], [takot], [-2], [Means fear or afraid.],
  [48], [MVL048], [talo], [-2], [Means to lose; often used negatively ("loser").],
  [49], [MVL049], [tanga], [-4], [Highly offensive insult (idiot/stupid).],
  [50], [MVL050], [tawa], [+2], [Means laugh or laughter.],
  [51], [MVL051], [tiwala], [+3], [Means trust or confidence.],
  [52], [MVL052], [tuwa], [+3], [Joy or delight.],
  [53], [MVL053], [wagi], [+3], [Means victorious or to win.]
)

== Filipino Stop-Word Neutrality Review Table

#table(
  columns: (auto, auto, auto, auto, 1fr),
  [*No.*], [*Stop word*], [*Proposed sentiment value*], [*Approved as neutral?*], [*Notes / suggested change*],
  [1], [ang], [0.0], [Yes], [Article marker. Neutral.],
  [2], [ng], [0.0], [Yes], [Preposition/marker. Neutral.],
  [3], [sa], [0.0], [Yes], [Preposition. Neutral.],
  [4], [mga], [0.0], [Yes], [Plural marker. Neutral.],
  [5], [na], [0.0], [Yes], [Adverb/linker. Neutral.],
  [6], [ay], [0.0], [Yes], [Inversion marker. Neutral.],
  [7], [at], [0.0], [Yes], [Conjunction ("and"). Neutral.],
  [8], [pa], [0.0], [Yes], [Enclitic particle. Neutral.],
  [9], [si], [0.0], [Yes], [Personal marker. Neutral.],
  [10], [ni], [0.0], [Yes], [Personal marker. Neutral.],
  [11], [kay], [0.0], [Yes], [Personal marker. Neutral.],
  [12], [lang], [0.0], [Yes], [Enclitic ("only"). Neutral.],
  [13], [naman], [0.0], [Yes], [Enclitic. Neutral.],
  [14], [ba], [0.0], [Yes], [Question marker. Neutral.],
  [15], [ito], [0.0], [Yes], [Pronoun. Neutral.],
  [16], [iyon], [0.0], [Yes], [Pronoun. Neutral.],
  [17], [doon], [0.0], [Yes], [Pronoun. Neutral.],
  [18], [dito], [0.0], [Yes], [Pronoun. Neutral.],
  [19], [nito], [0.0], [Yes], [Pronoun. Neutral.],
  [20], [niyan], [0.0], [Yes], [Pronoun. Neutral.],
  [21], [nila], [0.0], [Yes], [Pronoun. Neutral.],
  [22], [namin], [0.0], [Yes], [Pronoun. Neutral.],
  [23], [ninyo], [0.0], [Yes], [Pronoun. Neutral.],
  [24], [ako], [0.0], [Yes], [Pronoun. Neutral.],
  [25], [ikaw], [0.0], [Yes], [Pronoun. Neutral.],
  [26], [siya], [0.0], [Yes], [Pronoun. Neutral.],
  [27], [kami], [0.0], [Yes], [Pronoun. Neutral.],
  [28], [tayo], [0.0], [Yes], [Pronoun. Neutral.],
  [29], [kayo], [0.0], [Yes], [Pronoun. Neutral.],
  [30], [sila], [0.0], [Yes], [Pronoun. Neutral.],
  [31], [ko], [0.0], [Yes], [Pronoun. Neutral.],
  [32], [mo], [0.0], [Yes], [Pronoun. Neutral.],
  [33], [niya], [0.0], [Yes], [Pronoun. Neutral.],
  [34], [akin], [0.0], [Yes], [Pronoun. Neutral.],
  [35], [iyo], [0.0], [Yes], [Pronoun. Neutral.],
  [36], [kanila], [0.0], [Yes], [Pronoun. Neutral.],
  [37], [kanya], [0.0], [Yes], [Pronoun. Neutral.],
  [38], [ating], [0.0], [Yes], [Pronoun. Neutral.],
  [39], [aming], [0.0], [Yes], [Pronoun. Neutral.],
  [40], [inyong], [0.0], [Yes], [Pronoun. Neutral.],
  [41], [kapag], [0.0], [Yes], [Conjunction. Neutral.],
  [42], [dahil], [0.0], [Yes], [Conjunction. Neutral.],
  [43], [pero], [0.0], [Yes], [Conjunction ("but"). Modifies context, but neutral alone.],
  [44], [kasi], [0.0], [Yes], [Conjunction. Neutral.],
  [45], [upang], [0.0], [Yes], [Conjunction. Neutral.],
  [46], [habang], [0.0], [Yes], [Conjunction. Neutral.],
  [47], [bago], [0.0], [Yes], [Stop word meaning "before". Neutral alone.],
  [48], [kaya], [0.0], [Yes], [Conjunction. Neutral.],
  [49], [sana], [0.0], [Yes], [Optative particle. Neutral baseline for VADER.],
  [50], [nga], [0.0], [Yes], [Enclitic particle. Neutral.],
  [51], [din], [0.0], [Yes], [Enclitic particle. Neutral.],
  [52], [rin], [0.0], [Yes], [Enclitic particle. Neutral.],
  [53], [raw], [0.0], [Yes], [Enclitic particle. Neutral.],
  [54], [daw], [0.0], [Yes], [Enclitic particle. Neutral.],
  [55], [kung], [0.0], [Yes], [Conjunction. Neutral.],
  [56], [para], [0.0], [Yes], [Preposition/Conjunction. Neutral.],
  [57], [kapwa], [0.0], [Yes], [Pronoun/Adjective. Neutral.],
  [58], [lamang], [0.0], [Yes], [Adverb. Neutral.]
)

== Expert Approval Table

#table(
  columns: (auto, 1fr),
  [*Field*], [*Response*],
  [*Overall decision*], [Approved with revisions],
  [*Expert name*], [Dr. Joshua Urrete, LPT],
  [*Affiliation / role*], [Associate Professor 1, National University, Philippines / Professional Translator and Validator],
  [*Signature*], [_(Signed: Joshua Urrete)_],
  [*General comments*], [The Minimum Viable Lexicon effectively captures contemporary Filipino and Taglish social media semantics. Developers *should implement context-aware rules for heteronyms and dual-meaning words* (e.g., mahal as "love" vs. "expensive"). The selected stop-words function correctly as 0.0 valence structural markers.]
)

#pagebreak()
#align(center)[*Curriculum Vitae — Filipino Language Expert*]
#v(1em)

#image("../put_in_appendices/Joshua_Urrete_CV.png", width: 100%)

#pagebreak()
#heading(level: 1, numbering: none)[APPENDIX G]
#v(1em)
#align(center)[*Psychology Expert Validation Feedback*]
#v(1em)

#image("../put_in_appendices/Psychology_Expert_Validation_1.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_Validation_2.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_Validation_3.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_Validation_4.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_Validation_5.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_Validation_6.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_Validation_7.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_Validation_8.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_Validation_9.png", width: 100%)

#pagebreak()
#heading(level: 1, numbering: none)[APPENDIX H]
#v(1em)
#align(center)[*Curriculum Vitae — Psychology Expert*]
#v(1em)

#image("../put_in_appendices/Psychology_Expert_CV_1.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_CV_2.png", width: 100%)
#image("../put_in_appendices/Psychology_Expert_CV_3.png", width: 100%)

#pagebreak()

#heading(level: 1, numbering: none)[APPENDIX I]
#v(1em)
#align(center)[*Curriculum Vitae — Mobile/Software Engineering Expert*]
#v(1em)

#image("../put_in_appendices/Jim_Carlo_Pajendo_CV.png", width: 100%)

#pagebreak()

#heading(level: 1, numbering: none)[APPENDIX J]
#v(1em)
#align(center)[*System Evaluation Survey Instrument*]
#v(1em)

#image("../put_in_appendices/Survey_1.png", width: 100%)
#image("../put_in_appendices/Survey_2.png", width: 100%)
#image("../put_in_appendices/Survey_3.png", width: 100%)
#image("../put_in_appendices/Survey_4.png", width: 100%)
#image("../put_in_appendices/Survey_5.png", width: 100%)
#image("../put_in_appendices/Survey_6.png", width: 100%)
#image("../put_in_appendices/Survey_7.png", width: 100%)
#image("../put_in_appendices/Survey_8.png", width: 100%)
#image("../put_in_appendices/Survey_9.png", width: 100%)
#image("../put_in_appendices/Survey_10.png", width: 100%)

#pagebreak()

#heading(level: 1, numbering: none)[APPENDIX K]
#v(1em)
#align(center)[*Signed Progress Reports*]
#v(1em)

#image("../put_in_appendices/Progress_Report_1.png", width: 100%)
#image("../put_in_appendices/Progress_Report_2.jpg", width: 100%)
#image("../put_in_appendices/Progress_Report_3.png", width: 100%)
#image("../put_in_appendices/Progress_Report_4.png", width: 100%)
#image("../put_in_appendices/Progress_Report_5.png", width: 100%)

#pagebreak()

#set page(fill: white)
#heading(level: 1, numbering: none)[APPENDIX L]
#v(0.5em)
#align(center)[*Representative REDU Mobile Application Screens*]
#v(0.75em)

The following screens document the principal participant flow and intervention
surfaces of the implemented REDU Android application. Intermediate menu states,
confirmation dialogs, developer-only diagnostics, and repetitive variants are
omitted to keep the appendix concise.

#let app_screen(path, label) = block(width: 100%)[
  #align(center)[#image(path, width: 1.72in)]
  #v(0.2em)
  #align(center)[#text(size: 8pt)[#label]]
]

#v(0.75em)
#grid(
  columns: (1fr, 1fr, 1fr),
  gutter: 0.18in,
  app_screen("../app_showcase/01_setup_participant.png", [*L1.* Participant setup]),
  app_screen("../app_showcase/03_setup_platforms.png", [*L2.* Platform selection]),
  app_screen("../app_showcase/10_dashboard_empty.png", [*L3.* Participant dashboard]),
)

#pagebreak()

#grid(
  columns: (1fr, 1fr, 1fr),
  gutter: 0.18in,
  app_screen("../app_showcase/12_history_empty.png", [*L4.* Session history]),
  app_screen("../app_showcase/17_settings_image_scanning.png", [*L5.* Image scanning settings]),
  app_screen("../app_showcase/21_export_study_data.png", [*L6.* Aggregate-data export]),
)

#pagebreak()

#grid(
  columns: (1fr, 1fr),
  gutter: 0.35in,
  app_screen("../app_showcase/26_demo_intervention_l2_unlocked.png", [*L7.* Level 2 pause intervention]),
  app_screen("../app_showcase/27_demo_intervention_l3_breathing.png", [*L8.* Level 3 breathing intervention]),
)
