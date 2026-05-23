// ==========================================
// APPENDICES
// ==========================================

#import "utils.typ": continued_thesis_table, table_align, thesis_table

#pagebreak()

#heading(level: 1, numbering: none)[APPENDICES]

#v(2em)

#align(center)[*APPENDIX A*]
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
  caption: [Table 6. Full 27-Rule Fuzzy Inference Base (continued)],
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
    [25], [High], [High], [Low], [Critical],
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
*Safe* because at least two axes stay low. Rules 3, 5-8, 11-14, 16, and 20-22
remain *Warning* because they show mixed evidence without strong convergence
between duration and negativity. Rules 9, 15, 17-18, and 23-27 become
*Critical* because prolonged use and negative exposure rise together, or because
High NSD is reinforced by High Dwell Time even before the session becomes long.

Within that Critical layer, the strongest challenge cases are still explainable.
Rule 15 becomes *Critical* because a High-duration session with both other axes
already at Medium no longer reflects time alone. Rule 23 becomes *Critical*
because High Dwell Time, Medium NSD, and Medium Session Duration already show
three non-low signals in the same direction. Rule 25 also remains *Critical*
because a short session can still contain concentrated negative immersion when
the viewed items are mostly negative and each item holds attention for longer.

#pagebreak()

#align(center)[*APPENDIX B*]
#v(1em)
#align(center)[*Fallback Rules and Trigger Thresholds*]

#v(1em)

Unless otherwise noted, the following thresholds are fixed study priors selected for reproducibility and conservative intervention behavior. They are design parameters, not empirically validated clinical or population cutoffs. Because the fallback engine retains two inputs with three linguistic levels each, exhaustive coverage requires $3^2 = 9$ rules.

In the current field-tested artifact, the two-input fallback rule base below is reserved for *Sentiment-Unreliable* sessions, specifically sessions where negativity cannot be resolved reliably after the available text or no-text path is attempted. This includes sessions whose extracted text is at least half unresolved vocabulary (study-defined unreliability trigger: session OOV ratio >= 50%) and sessions where the no-text VLM path cannot produce a stable item label because `AccessibilityService.takeScreenshot` is unavailable, screenshot capture fails, or VLM inference fails. The `>= 50%` value is not treated as a validated linguistic cutoff. It is a mathematically derived majority-representativeness screen: once unresolved tokens reach half of the valid tokens in a session, the recognized tokens no longer form the majority of the lexical content available to the text path. This is therefore used as a practical safeguard for noisy code-mixed text with spelling variation, lexical borrowing, and other textual variation rather than as a claimed language norm (Mohammed & Prasad, 2023; Perera & Caldera, 2024; Nazir et al., 2026; Khan et al., 2025). In those cases, the system degrades to a two-input fallback rule base (Video Dwell Time + Session Duration):

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
  caption: [Table 7. Fallback 9-Rule Base (Sentiment-Unreliable Sessions) (continued)],
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
- Level 3: RiskScore 66.67 to 100 (disabled in fallback mode)
- The Warning band is split at its midpoint 50.00 so lower- and upper-Warning states receive different prompt intensities without introducing extra off-band cutoffs; Level 3 is reserved for the Critical band.
- Cooldown: fixed 15-minute prototype cooldown between prompts, intentionally reusing the same literature-bounded 15-minute live gate as the minimum re-eligibility interval so the system does not introduce a second unsupported minute constant; a repeat prompt therefore requires another prolonged interval of continued use. Recent JITAI work supports managing timing, receptivity, and dose, but not this exact interval (Teepe et al., 2021; Wang et al., 2023; Fiedler et al., 2024; Hsu et al., 2025; van Genugten et al., 2025)

#pagebreak()

#align(center)[*APPENDIX C*]
#v(1em)
#align(center)[*Core Algorithmic Logic and Reproducibility Specifications*]

#v(1em)

The field-tested artifact separates fixed analytic priors from Week 2 live-prompt personalization. Fixed global memberships are retained for week-level DSI computation across both study weeks so baseline and intervention summaries remain comparable. A separate live prompt engine is then allowed to replace only the Session Duration and NSD memberships after Week 1; Video Dwell Time remains fixed.

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

These quantiles are not treated as validated behavioral cutoffs. They are nonparametric order-statistic anchors chosen to map a three-term Low/Medium/High partition onto each participant's Week 1 distribution with the smallest transparent summary set: $Q_(25)/Q_(50)/Q_(75)$ summarize the interquartile core, while $Q_(95)$ caps the High set without letting one raw maximum set the upper endpoint. Video Dwell Time remains fixed to the global priors above. If $n_u < 10$ sentiment-reliable Week 1 sessions, the default priors are retained for that participant rather than locking personalized bounds. This 10-session minimum remains a numerical sufficiency safeguard rather than a validated population cutoff: below $n_u = 10$, the empirical $Q_(95)$ is governed too heavily by the single largest observation or a nearly maximal order statistic, so the personalized High endpoint becomes too sensitive to one extreme session. Recent personalized intervention-criteria work supports deriving live criteria from a first-week baseline, but recent JITAI reviews also note that empirical decision rules and points remain underdeveloped, which is why the fielded artifact prefers participant-specific Week 2 criteria only when enough baseline data exist (Ikegaya et al., 2025; Hsu et al., 2025; van Genugten et al., 2025; Elmer et al., 2025). Because fallback prompting uses the same live prompt engine, personalized Session Duration bounds also carry into fallback mode when personalization is available; only the rule base and prompt cap differ.

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
- Level 3 (Breathing Exercise): $66.67 <= "RiskScore" <= 100$
- The Warning band is split at its midpoint 50.00 so lower- and upper-Warning states receive different prompt intensities without introducing extra off-band cutoffs; Level 3 is reserved for the Critical band.
- Fixed cooldown: minimum 15-minute prototype interval between any two prompts, intentionally reusing the same literature-bounded 15-minute live gate as the minimum re-eligibility interval so the system does not introduce a second unsupported minute constant; a repeat prompt therefore requires another prolonged interval of continued use. Recent JITAI work supports managing timing, receptivity, and dose, but not this exact interval (Teepe et al., 2021; Wang et al., 2023; Fiedler et al., 2024; Hsu et al., 2025; van Genugten et al., 2025)

When the 2-input behavioral fallback mode is active (Sentiment-Unreliable), Level 3 is disabled by safety lock and the maximum permitted prompt is Level 2.

#pagebreak()

#align(center)[*APPENDIX D*]
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
