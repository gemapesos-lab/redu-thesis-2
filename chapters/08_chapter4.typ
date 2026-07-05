// ==========================================
// CHAPTER 4: PRESENTATION, ANALYSIS AND INTERPRETATION OF DATA
// ==========================================

#import "utils.typ": continued_thesis_table, table_align, thesis_table

= PRESENTATION, ANALYSIS AND INTERPRETATION OF DATA

This chapter presents the completed field-study, survey, expert-review, and workbook-based analysis results for the Heuristic Risk-State Estimation System. The final analysis dataset contains 50 participants, divided evenly between the intervention group and the logging-only control group. Both groups completed the Week 1 baseline phase and the Week 2 deployment phase. The analysis is interpreted as a pilot-scale software-engineering evaluation, not as a clinical trial or long-term behavioral-efficacy study. To keep results traceable to Chapter 1, this chapter is organized around the five Statements of the Problem (SOPs): each top-level section reports the evidence, findings, and interpretation for one SOP.

The detailed workbook sheets in `REINVENT ANALYSIS.xlsx` were used as the primary source for the tables in this chapter. The final reporting values for SOP 2 were taken from the workbook sheet `RQ2_FINAL_TESTS_PDF_ALIGNED` because it applies the methodology-selected tests after assumption checking. The formula audit sheets were used only as supporting calculation references.

== Presentation of Results

This section presents the preliminary context needed to interpret the SOP-level results that follow: the data sources used across the study, the effective sample sizes after data-quality screening, and the respondent baseline profile. To keep the discussion aligned with Chapter 3, results are organized by SOP and interpreted with attention to effective sample sizes, pilot-study scope, and known data limitations.

#thesis_table(
  caption: [Data Sources and Analysis Mapping],
  columns: (0.9fr, 1.15fr, 1.35fr, 1.25fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Research Area*],
    [*Primary Workbook Source*],
    [*Analysis or Summary Used*],
    [*Reporting Purpose*],
  ),
  body: (
    [Respondent profile], [`baseline_profile.csv`], [Counts and descriptive summaries], [Describe participant context],
    [Session coverage], [`sessions.csv`; `DATA_QUALITY_SUMMARY`], [Reliable and sentiment-unreliable session counts], [Report analyzable coverage and exclusions],
    [SOP 2 behavioral comparison], [`RQ2_FINAL_TESTS_PDF_ALIGNED`], [Assumption-selected between-group and paired tests], [Report Week 1-to-Week 2 differences],
    [SOP 3 association], [`RQ3_CORRELATION`], [Spearman's rho and Pearson's r], [Assess baseline convergent plausibility],
    [SOP 4 user evaluation], [`SURVEY_ANALYSIS`], [Means, SDs, thresholds, Cronbach's alpha], [Evaluate ISO/IEC 25010, SUS, and TAM],
    [SOP 5 expert evaluation], [`sme_evaluation.csv`; `sme_open_ended_feedback.csv`], [Per-expert rubric ratings and narrative comments], [Report expert plausibility appraisal],
  ),
)

=== Sample Disposition and Effective Sample Sizes

All 50 allocated participants completed both study weeks. The intervention and control groups each contained 25 participants. The full session log contained 10,134 sessions across the two-week period. Of these, 8,817 sessions were sentiment-reliable and 1,317 sessions were sentiment-unreliable. The sentiment-unreliable sessions were retained as operational evidence but excluded from DSI and NSD computations where the methodology required reliable sentiment coverage.

#thesis_table(
  caption: [Sample Disposition and Effective Sample Sizes],
  columns: (1.45fr, 0.75fr, 0.75fr, 0.75fr, 1.35fr),
  cell_align: table_align((left, center, center, center, left)),
  header: (
    [*Metric*],
    [*Intervention*],
    [*Control*],
    [*Total*],
    [*Interpretation*],
  ),
  body: (
    [Allocated participants], [25], [25], [50], [Balanced two-arm pilot sample],
    [Completed Week 1], [25], [25], [50], [All participants retained],
    [Completed Week 2], [25], [25], [50], [All participants retained],
    [SOP 3 eligible participants], [23], [25], [48], [Two intervention participants lacked sufficient reliable Week 1 sessions],
    [Primary duration, dwell, and self-report outcomes], [25], [25], [50], [Complete outcome pairs available],
    [NSD and DSI outcomes], [23], [25], [48], [Effective n reflects sentiment-reliability coverage],
  ),
)

The final session distribution was 4,820 intervention-group sessions and 5,314 control-group sessions. By platform, the logs contained 3,433 Instagram sessions, 3,389 Facebook sessions, and 3,312 TikTok sessions. Week 1 contained 5,162 sessions, while Week 2 contained 4,972 sessions. This distribution shows that the application captured data from all target platforms and both study phases.

=== Respondent Baseline Profile

The respondent profile provides descriptive context for the behavioral analyses. The baseline profile sheet shows that the participants were adult Android users aged 18 to 29 years, with a mean age of 22.12 years. There were 39 male and 11 female participants. All 50 participants had observed use of Facebook Reels and Instagram Reels during Week 1, while 49 had observed TikTok use. Estimated daily short-form-video use ranged from 15 minutes to 883 minutes, with a mean of 392.70 minutes based on the active-day derivation used in the completed baseline profile sheet.

#thesis_table(
  caption: [Respondent Baseline Profile Summary],
  columns: (1.15fr, 0.95fr, 1.85fr),
  cell_align: table_align((left, center, left)),
  header: (
    [*Profile Variable*],
    [*Result*],
    [*Interpretation*],
  ),
  body: (
    [Total respondents], [50], [Balanced sample with 25 intervention and 25 control participants],
    [Age], [18-29 years; mean 22.12], [Adult participant group, mostly young adults],
    [Sex], [39 male; 11 female], [Male respondents formed the larger share of the sample],
    [Observed Facebook Reels use], [50 of 50], [All participants had Facebook Reels activity],
    [Observed Instagram Reels use], [50 of 50], [All participants had Instagram Reels activity],
    [Observed TikTok use], [49 of 50], [Nearly all participants had TikTok activity],
    [Estimated daily SFV use], [15-883 min; mean 392.70], [Wide variation in baseline short-form-video exposure],
  ),
)

In terms of general platform-use habits, 25 participants were categorized as having multiple sessions daily, 15 as having many sessions through the day, and 10 as having a few sessions daily. Dominant-platform descriptors were distributed across Facebook Reels, Instagram Reels, and TikTok. These profile results support the suitability of the sample for a pilot evaluation of a short-form-video monitoring application, while the purposive-convenience sampling still limits generalizability.

== SOP 1: Privacy-Preserving Mobile Architecture and Estimation Framework with 2-Input Behavioral Fallback for Sentiment-Unreliable Sessions

Statement of the Problem 1 asked what privacy-preserving mobile architecture and estimation framework can support doomscrolling-related risk estimation on the target short-form video platforms using behavioral indicators and sentiment-related indicators when reliably resolvable, with 2-input behavioral fallback for sentiment-unreliable sessions. This section reports the implemented architecture, its operational data-quality evidence, and the analytic time complexity of the runtime pipeline.

=== System Implementation and Data-Quality Evidence

The completed system implements the architecture described in Chapter 3: Android Accessibility Service monitoring, local session tracking, text-first VADER-compatible sentiment scoring, Filipino/Taglish MVL extension, no-text visual fallback through transient on-device screenshot processing, fuzzy inference, adaptive prompting, local Room storage, and CSV export using participant study codes.

The implementation evidence supports the intended privacy boundary. Raw text and no-text screen frames are processed locally and are not retained as research outputs. The exported study data contain aggregate behavioral and sentiment-related metrics rather than raw captions, comments, or images.

The Filipino MVL concordance sheet contained 61 rows, including the 57 runtime lexicon entries (56 affective entries and one neutral candidate entry) with exact expert-to-runtime valence agreement. All 57 runtime entries were within the acceptable plus-or-minus-one valence window. This supports the internal consistency of the deployed Filipino/Taglish lexicon values against the completed single-expert review. Because the review used one Filipino language expert, this result is treated as a concordance check, not as inter-rater reliability.

The deployment also logged 1,329 reliability events: 1,317 content-related events (high-OOV, VLM-unresolved, and extraction-failure), each linked to one sentiment-unreliable session, plus 12 service start/stop notices. The event percentages below use this 1,329-event total as the denominator.

#thesis_table(
  caption: [Data Quality and Session Reliability Summary],
  columns: (1.45fr, 0.75fr, 0.8fr, 1.3fr),
  cell_align: table_align((left, center, center, left)),
  header: (
    [*Category*],
    [*Count*],
    [*Percent*],
    [*Interpretation*],
  ),
  body: (
    [Reliable sessions], [8,817], [87.0%], [Included in sentiment-dependent analysis],
    [Sentiment-unreliable sessions], [1,317], [13.0%], [Excluded from DSI or NSD where required],
    [High-OOV reliability events], [924], [69.5% of reliability events], [Main reliability-event driver],
    [VLM-unresolved reliability events], [241], [18.1% of reliability events], [No-text visual path did not resolve some cases],
    [Extraction-failure events], [152], [11.4% of reliability events], [Platform or accessibility extraction issue],
    [Service lifecycle events], [12], [0.9% of reliability events], [Service start/stop notices not tied to sessions],
    [Total logged sessions], [10,134], [100.0%], [Operational data captured across both weeks],
  ),
)

Prompt logs were available for the Week 2 intervention arm. The largest prompt-related action was `TAKE_BREAK`, with 191 logged events or 51.6% of prompt events. `SHOWN` accounted for 68 events, `SUPPRESSED` for 49, `CONTINUE` for 45, `DISMISSED` for 17, and `VIEW_DASHBOARD` for 0. These logs are descriptive evidence of intervention interaction. They are not interpreted as a separate behavioral outcome because there is no Week 1 prompt baseline.

#thesis_table(
  caption: [Prompt-Response Summary During Week 2 Intervention],
  columns: (1.2fr, 0.75fr, 0.75fr, 1.75fr),
  cell_align: table_align((left, center, center, left)),
  header: (
    [*Prompt Action*],
    [*Count*],
    [*Percent*],
    [*Interpretation*],
  ),
  body: (
    [`TAKE_BREAK`], [191], [51.6%], [Most frequent logged prompt response],
    [`SHOWN`], [68], [18.4%], [Prompt display engine state],
    [`SUPPRESSED`], [49], [13.2%], [Cooldown or suppression state],
    [`CONTINUE`], [45], [12.2%], [User continued after prompt],
    [`DISMISSED`], [17], [4.6%], [User dismissed the prompt],
    [`VIEW_DASHBOARD`], [0], [0.0%], [No dashboard-view event was logged from prompts],
  ),
)

These findings answer SOP 1 by showing that the implemented system can collect privacy-bounded local logs, estimate session-level risk from observable proxies, degrade to conservative fallback handling when sentiment is unreliable, and record intervention events for descriptive analysis.

=== Algorithm Complexity

This subsection presents the time complexity of the core runtime algorithms described in Chapter 3, closing out SOP 1 by characterizing the computational cost of the implemented architecture. The analysis is analytic and data-independent: it characterizes the computational cost of the implemented pipeline as a function of its input sizes and does not depend on field-study data. The following variables are used:

- $n$ - number of tokens in the text payload extracted from a single viewed item
- $I$ - number of items viewed in a session
- $V$ - number of fuzzy input variables ($V = 3$; $V = 2$ in the degraded fallback mode)
- $L$ - number of linguistic terms per variable ($L = 3$)
- $R$ - number of rules in the fuzzy inference base ($R = L^V = 27$; $R = 9$ in fallback mode)
- $S$ - number of recorded sessions in a study week
- $C_("VLM")$ - the bounded per-invocation cost of one Moondream 0.5B inference

*VADER Sentiment Scoring.* Tokenization visits each token once. Lexicon membership is resolved through hash-based dictionary lookup with constant average cost per token, and the Filipino MVL extension only enlarges the dictionary without changing the lookup mechanism. The booster and negation heuristics inspect a fixed lookback window of at most three preceding tokens per scored token, which is a bounded constant amount of work. Per-item text scoring is therefore $O(n)$ time, and the compound-score normalization $C = x\/sqrt(x^2 + alpha)$ is $O(1)$.

*Per-Item Routing Heuristic.* Deciding whether a viewed item carries usable text is a constant-time check, so routing contributes $O(1)$ per item.

*No-Text VLM Fallback (Moondream 0.5B).* Each no-text item triggers at most one screenshot capture and one constrained VQA inference. Because the model size, the input resolution, and the constrained five-label output are all fixed, every invocation has a bounded constant cost $C_("VLM")$ that does not grow with session length. $C_("VLM")$ is the dominant constant in the pipeline, which is why the routing heuristic reserves it for items with no usable text instead of applying it universally.

*Fuzzy Inference Engine.* One session-level inference consists of: (1) fuzzification, which evaluates $V dot L = 9$ triangular membership functions at $O(1)$ each; (2) rule evaluation, which computes a MIN over $V$ antecedent memberships for each of the $R$ rules, i.e., $O(R dot V)$; and (3) CoG defuzzification, a weighted average over at most $R$ activation weights, i.e., $O(R)$. Since $V$, $L$, and $R$ are fixed at runtime, a complete inference runs in constant time, $O(R dot V) = O(1)$, in both modes.

*Session Aggregation and Week-Level DSI.* Negative Sentiment Density is a ratio over resolved items maintained incrementally in $O(I)$ per session. Week-level DSI computation aggregates session records in $O(S)$. Week 1 personalization derives the $Q_(25)\/Q_(50)\/Q_(75)\/Q_(95)$ duration and NSD quantiles from at least 10 sentiment-reliable baseline sessions, which is $O(S log S)$ due to sorting.

#thesis_table(
  caption: [Time Complexity Summary of Core Runtime Algorithms],
  columns: (1.4fr, 0.8fr, 1.3fr),
  cell_align: table_align((left, center, left)),
  header: (
    [*Component*],
    [*Time Complexity*],
    [*Dominant Cost Driver*],
  ),
  body: (
    [VADER scoring (per item)], [$O(n)$], [Token count of extracted text],
    [Per-item routing heuristic], [$O(1)$], [Constant-time payload check],
    [VLM fallback (per no-text item)], [$O(C_("VLM"))$], [Fixed-size Moondream 0.5B inference],
    [Fuzzy inference (per session)], [$O(R dot V) = O(1)$], [Fixed 27-rule or 9-rule base],
    [NSD aggregation (per session)], [$O(I)$], [Items viewed in the session],
    [Week-level DSI], [$O(S)$], [Sessions recorded in the week],
    [Week 1 quantile personalization], [$O(S log S)$], [Sorting baseline sessions],
  ),
)

*Worst-Case Analysis.* For a single session, the total cost is $O(sum_(i=1)^I n_i)$ for text-path items plus $O(I dot C_("VLM"))$ for VLM-routed items, followed by one $O(1)$ fuzzy inference. No component is super-linear in the number of viewed items or tokens. The worst case occurs in a feed where every viewed item lacks usable text, so every item incurs the screenshot-plus-VLM cost, giving $O(I dot C_("VLM"))$ for the session. If screenshot capture or VLM inference fails, the safety lock degrades inference to the 2-input, 9-rule engine without adding asymptotic cost, and the session is marked Sentiment-Unreliable as described in Chapter 3.

== SOP 2: Short-Term Week 1-to-Week 2 Changes in Logged Usage Metrics and Self-Reported Doomscrolling Scores

Statement of the Problem 2 asked what short-term Week 1-to-Week 2 changes are observed in selected logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group, and within the intervention group across the same period. The final reporting table uses assumption-selected tests: Welch's t-test for session duration where normality held with unequal variance, Mann-Whitney U tests for non-normal between-group change scores, paired t-tests for normally distributed within-intervention differences, and Wilcoxon signed-rank tests for non-normal paired differences.

#thesis_table(
  caption: [SOP 2 Primary Behavioral Comparisons Between Groups],
  columns: (1.2fr, 0.7fr, 1fr, 0.8fr, 0.75fr, 0.9fr, 1.05fr),
  cell_align: table_align((left, center, center, center, center, center, center)),
  header: (
    [*Outcome*],
    [*$n$ (I/C)*],
    [*Final Test*],
    [*Statistic*],
    [*$p$ / Holm*],
    [*Effect Size*],
    [*95% CI*],
  ),
  body: (
    [Session duration (min)], [25 / 25], [Welch t-test], [$t = -4.17$], [$p < .001$], [$d = -1.18$], [[-1.78, -0.58]],
    [Video dwell time (s)], [25 / 25], [Mann-Whitney U], [$U = 124.0$], [$p < .001$], [$d = -1.33$], [[-1.94, -0.72]],
    [NSD (%)], [23 / 25], [Mann-Whitney U], [$U = 68.0$], [$p < .001$], [$d = -1.82$], [[-2.50, -1.15]],
    [Doomscrolling Scale], [25 / 25], [Mann-Whitney U], [$U = 105.0$], [$p < .001$], [$d = -1.50$], [[-2.13, -0.87]],
  ),
)

All four primary outcomes favored the intervention group after Holm-Bonferroni correction. The negative effect sizes indicate larger reductions in the intervention group compared with the logging-only control group. Session duration, dwell time, NSD, and self-reported Doomscrolling Scale scores all showed short-term reductions under the intervention condition. Because the study is pilot-sized and lasted two weeks, these results are interpreted as observed short-term differences under study conditions, not as proof of long-term intervention efficacy.

#thesis_table(
  caption: [SOP 2 Supplementary and Within-Intervention Comparisons],
  columns: (1.25fr, 0.65fr, 1.05fr, 0.8fr, 0.7fr, 0.9fr, 1.35fr),
  cell_align: table_align((left, center, center, center, center, center, left)),
  header: (
    [*Outcome*],
    [*$n$*],
    [*Final Test*],
    [*Statistic*],
    [*$p$*],
    [*Effect Size*],
    [*Interpretation*],
  ),
  body: (
    [Mean sessions/day (between groups)], [25 / 25], [Mann-Whitney U], [$U = 153.5$], [$p = .002$], [$d = -0.82$], [Supplementary; favors intervention],
    [DSI composite risk (between groups)], [23 / 25], [Mann-Whitney U], [$U = 81.0$], [$p < .001$], [$d = -1.84$], [Supplementary; favors intervention],
    [Session duration (within intervention)], [25], [Paired t-test], [$t = -4.41$], [$p < .001$], [$d_z = -0.88$], [Reduction from Week 1 to Week 2],
    [Video dwell time (within intervention)], [25], [Paired t-test], [$t = -5.45$], [$p < .001$], [$d_z = -1.09$], [Reduction from Week 1 to Week 2],
    [NSD (within intervention)], [23], [Wilcoxon signed-rank], [$W = 16.0$], [$p < .001$], [$d_z = -1.28$], [Reduction among participants with reliable coverage],
    [Doomscrolling Scale (within intervention)], [25], [Wilcoxon signed-rank], [$W = 7.0$], [$p < .001$], [$d_z = -1.25$], [Reduction from Week 1 to Week 2],
  ),
)

The supplementary outcomes show the same direction as the primary outcomes. Mean sessions per day and DSI both favored the intervention group, although they were not part of the four-outcome Holm family. The within-intervention comparisons also showed reductions from Week 1 to Week 2. These secondary results strengthen the descriptive pattern but remain exploratory.

== SOP 3: Baseline Convergent Association Between Week 1 DSI and Self-Reported Doomscrolling Scores

Statement of the Problem 3 asked what baseline convergent association exists between the fixed-prior Week 1 Doomscroll Severity Index (DSI) and participants' self-reported doomscrolling scores among participants with at least three sentiment-reliable Week 1 sessions. The effective sample size was 48 because two intervention participants lacked sufficient reliable Week 1 sessions for DSI inclusion. Spearman's rho was treated as the primary statistic because the methodology selected a rank-based correlation when normality assumptions were not met.

#thesis_table(
  caption: [SOP 3 Baseline Convergent Association with Week 1 Doomscrolling Scale],
  columns: (1.45fr, 0.65fr, 0.75fr, 0.85fr, 0.75fr, 1.15fr),
  cell_align: table_align((left, center, center, center, center, left)),
  header: (
    [*Predictor*],
    [*$n$*],
    [*Spearman's rho*],
    [*Spearman $p$*],
    [*Pearson's r*],
    [*Interpretation*],
  ),
  body: (
    [Week 1 mean daily duration], [48], [$rho = 0.92$], [$p < .001$], [$r = 0.82$], [Strongest rank association],
    [Week 1 mean dwell time], [48], [$rho = 0.75$], [$p < .001$], [$r = 0.82$], [Strong positive association],
    [Week 1 mean NSD], [48], [$rho = 0.76$], [$p < .001$], [$r = 0.83$], [Strong positive association],
    [Week 1 DSI], [48], [$rho = 0.76$], [$p < .001$], [$r = 0.82$], [Strong convergent association],
  ),
)

The Week 1 DSI had a strong positive association with the Week 1 Doomscrolling Scale. This supports baseline convergent plausibility for the composite risk estimate. However, mean daily session duration had the highest rank correlation with self-report, so the data support convergence of the composite with self-reported doomscrolling rather than superiority over session duration alone.

== SOP 4: User Evaluation Using the ISO/IEC 25010 Software Quality Model and the Technology Acceptance Model

Statement of the Problem 4 asked how users evaluated the system using the ISO/IEC 25010 software quality model and the Technology Acceptance Model (TAM). All six evaluated constructs met their pre-set favorable thresholds. The ISO/IEC 25010 Functional Suitability, Performance Efficiency, and Reliability subscales all exceeded the 3.50 favorable target. The SUS usability score exceeded the 70 target. TAM Perceived Usefulness and Perceived Ease of Use also exceeded 3.50.

#thesis_table(
  caption: [ISO/IEC 25010, SUS, TAM, and Reliability Summary],
  columns: (1.45fr, 0.45fr, 0.55fr, 0.7fr, 0.65fr, 0.8fr, 0.75fr),
  cell_align: table_align((left, center, center, center, center, center, center)),
  header: (
    [*Construct*],
    [*$n$*],
    [*Items*],
    [*Mean*],
    [*SD*],
    [*Alpha*],
    [*Target*],
  ),
  body: (
    [Functional Suitability], [50], [5], [3.93], [0.50], [0.852], [Met],
    [Performance Efficiency], [50], [5], [4.09], [0.55], [0.859], [Met],
    [Reliability], [50], [5], [4.05], [0.50], [0.717], [Met],
    [SUS Usability], [50], [10], [80.95], [14.83], [0.957], [Met],
    [TAM Perceived Usefulness], [50], [6], [4.12], [0.40], [0.750], [Met],
    [TAM Perceived Ease of Use], [50], [6], [4.09], [0.53], [0.855], [Met],
  ),
)

The Cronbach's alpha values ranged from 0.717 to 0.957, meeting the study's descriptive benchmark of $alpha >= 0.70$ for every multi-item scale. These coefficients support internal consistency of the survey constructs in this sample, while still requiring caution because some subscales were researcher-developed or adapted to the specific system context.

Participant open-ended feedback provided practical context for the quantitative ratings. The most common coded theme was prompt timing, followed by onboarding/setup and score clarity. These themes show that users generally found the system useful and privacy-conscious, but also wanted clearer setup guidance, clearer score explanations, and more control over prompts.

#thesis_table(
  caption: [Participant Open-Ended Feedback Theme Summary],
  columns: (1.25fr, 0.55fr, 0.7fr, 1.8fr),
  cell_align: table_align((left, center, center, left)),
  header: (
    [*Theme*],
    [*Count*],
    [*Percent*],
    [*Main Implication*],
  ),
  body: (
    [Prompt timing], [8], [16%], [Add snooze, quiet hours, or sensitivity controls],
    [Onboarding and setup], [6], [12%], [Improve Accessibility setup guidance],
    [Score clarity], [6], [12%], [Explain the risk or activity score in simpler terms],
    [Platform monitoring], [5], [10%], [Show platform-specific monitoring status],
    [Dashboard usefulness], [5], [10%], [Retain and improve history and dashboard summaries],
    [Breathing break], [4], [8%], [Make the pause duration shorter or configurable],
    [Export and study data], [4], [8%], [Clarify which files are exported],
    [Privacy and trust], [4], [8%], [Keep local processing and explain stored data],
    [Performance and battery], [3], [6%], [Provide in-app battery and performance feedback],
    [No major issue / general positive], [5], [10%], [Some respondents reported no major difficulty],
  ),
)

These qualitative findings support the survey results while also identifying improvement priorities. The strongest user-facing issues were not rejection of the system concept, but setup complexity, prompt tuning, score interpretation, and transparency around platform monitoring and exported data.

== SOP 5: Subject Matter Expert Evaluation of Technical Design, Privacy Safeguards, Heuristic Logic, and Intervention Structure

Statement of the Problem 5 asked how subject matter experts evaluated the system's technical design, privacy safeguards, heuristic logic, and intervention structure. Two SMEs completed the evaluation: one software engineering or mobile application development expert and one digital well-being or behavioral psychology expert. Given the two-member panel, ratings are reported per expert against the study-defined favorable target of 4.00 rather than as an inferential panel statistic. The six rubric ratings cover technical soundness, input-range plausibility, rule-base coherence, architecture and privacy quality, intervention appropriateness, and overall ISO/IEC 25010 quality; the Overall column is the mean of these six ratings.

#thesis_table(
  caption: [Subject Matter Expert Rubric Results],
  columns: (1.05fr, 1.35fr, 0.5fr, 0.55fr, 0.5fr, 0.6fr, 0.55fr, 0.45fr, 0.6fr),
  cell_align: table_align((left, left, center, center, center, center, center, center, center)),
  header: (
    [*Expert*],
    [*Role*],
    [*Tech.*],
    [*Range*],
    [*Rules*],
    [*Privacy*],
    [*Interv.*],
    [*ISO*],
    [*Overall*],
  ),
  body: (
    [`SME-MOB-01`], [Software engineering / mobile application development], [5], [5], [5], [5], [5], [5], [5.00],
    [`SME-PSY-01`], [Digital well-being / behavioral psychology], [4], [5], [4], [4], [5], [4], [4.33],
  ),
)

Both SME ratings met the favorable target. The mobile/software expert rated all rubric areas as 5.00 and considered the hybrid VADER, fallback, and fuzzy inference approach appropriate for a non-clinical prototype, while recommending clearer operational definitions for aggregate-only handling and fallback triggers. The behavioral-psychology expert considered the Doomscrolling Feedback Loop Model an appropriate theoretical basis and supported the non-clinical self-monitoring framing. This expert also recommended softer terminology, clearer presentation of risk labels, and a shorter or configurable pause-and-reset duration.

The SME results support expert plausibility appraisal, not formal empirical calibration. The absence of a dedicated data science, machine learning, or fuzzy-logic reviewer remains a limitation. The expert comments identify concrete refinements: clearer fallback criteria, stronger privacy wording, less alarming labels, and more user control over interventions.

== Analysis and Interpretation

The completed data show that the system met the study's software-acceptability thresholds, produced analyzable usage and sentiment logs across the target platforms, and showed favorable short-term differences in the intervention arm compared with the logging-only control arm. The results also show important boundaries. The pilot sample, two-week deployment window, purposive-convenience sampling, two-member SME panel, and sentiment-unreliable exclusions prevent broader claims about long-term efficacy, diagnostic validity, or population-level calibration.

#thesis_table(
  caption: [Summary of Findings by Statement of the Problem],
  columns: (0.55fr, 1.25fr, 1.45fr, 1.45fr),
  cell_align: table_align((center, left, left, left)),
  header: (
    [*SOP*],
    [*Evidence*],
    [*Main Finding*],
    [*Interpretation and Limitation*],
  ),
  body: (
    [1], [Implementation, session logs, MVL concordance, data-quality summary, prompt logs, algorithm complexity], [The privacy-preserving local architecture operated across the two-week deployment and produced 10,134 logged sessions with 87.0% sentiment-reliable coverage, with all core runtime components bounded to at most $O(n)$ per item and $O(1)$ per fuzzy inference.], [Supports operational feasibility and bounded local risk estimation.],
    [2], [Final SOP 2 tests on Week 2 minus Week 1 change scores], [All four primary outcomes favored intervention after Holm correction, with large effect sizes.], [Supports observed short-term differences under study conditions, not long-term efficacy.],
    [3], [Week 1 DSI and Doomscrolling Scale correlation], [DSI showed strong baseline convergence with self-report ($rho = 0.76$), while duration had the highest rank association ($rho = 0.92$).], [Supports plausibility of DSI but not composite superiority or diagnostic validation.],
    [4], [ISO/IEC 25010, SUS, TAM, Cronbach alpha, user feedback], [All six software-acceptability targets were met; alpha values were all at least 0.70.], [Supports favorable user evaluation, with improvement needs in onboarding, prompt timing, score clarity, and monitoring transparency.],
    [5], [SME rubric and narrative feedback], [Both SMEs met the favorable target, with overall means of 5.00 and 4.33.], [Supports expert plausibility appraisal; limited by two experts and no dedicated fuzzy-systems reviewer.],
  ),
)

Overall, the Chapter 4 evidence supports the completion of the thesis evaluation under the scope defined in Chapter 3. The system stands as a privacy-preserving, heuristic, non-clinical mobile tool that combines local behavioral logging, sentiment-aware risk estimation, and adaptive prompts, and the findings constitute promising pilot evidence within clear methodological limits.

#pagebreak()
