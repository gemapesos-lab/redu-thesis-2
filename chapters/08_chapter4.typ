// ==========================================
// CHAPTER 4: PRESENTATION, ANALYSIS AND INTERPRETATION OF DATA
// Completed from simulated-export/redu-p01-p50-20260523-224114
// ==========================================

#import "utils.typ": table_align, thesis_table

= PRESENTATION, ANALYSIS AND INTERPRETATION OF DATA

This chapter presents the results, analysis, and interpretation of the simulated REDU export located in `simulated-export/redu-p01-p50-20260523-224114`. The available export contains app-generated session logs, daily summaries, prompt events, reliability events, risk-personalization rows, pre/post Doomscrolling Scale responses, ISO/IEC 25010 survey responses, SUS responses, TAM responses, and coded user open-ended feedback. The SME rating and SME open-ended feedback files are present but blank, so expert-evaluation results are treated as pending rather than estimated or invented.

The analysis dataset contains 50 study-coded participants, 2,051 retained sessions, 1,227 daily-summary rows, 1,152 prompt-event rows, 8,549 reliability-event rows, 50 risk-personalization rows, 100 Doomscrolling Scale rows, 50 ISO/IEC 25010 survey rows, 50 SUS response rows, 50 TAM response rows, and 50 coded user feedback rows. All statistics in this chapter were computed from the CSV files in the simulated export.

== Presentation of Results

The results are organized according to the five research questions. Research Questions 1 to 4 are supported by the available logs and user-response files. Research Question 5 remains pending because the SME rating and comment files contain no completed expert entries.

=== Research Question 1: Privacy-Preserving Mobile Architecture and Estimation Framework

The first research question asks what privacy-preserving mobile architecture and estimation framework can support doomscrolling-related risk estimation on the target short-form video platforms using behavioral indicators and sentiment-related indicators when reliably resolvable, with 2-input behavioral fallback for sentiment-unreliable sessions.

#thesis_table(
  caption: [System Architecture and Estimation Framework Result Matrix],
  columns: (1.15fr, 1.45fr, 1.25fr, 1.2fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*System Component*],
    [*Intended Function*],
    [*Implementation Evidence*],
    [*Result Status*],
  ),
  body: (
    [Data Access Layer],
    [Collect target-app sessions, platform labels, timing, dwell estimates, swipe counts, and reliability events through the Android monitoring pipeline.],
    [The export contains 2,051 sessions across TikTok, Facebook Reels, and Instagram Reels, with corresponding foreground, background, and finalization events.],
    [Met in simulated export],

    [Business Logic Layer],
    [Route analyzable sessions through the sentiment-reliable path, compute NSD when available, and assign risk score and risk level.],
    [The export contains risk scores for all sessions and NSD values for 1,706 sentiment-reliable sessions.],
    [Met for log scoring],

    [2-Input Safety Fallback],
    [Use behavioral inputs when sentiment is unresolved or unreliable and flag affected sessions for exclusion from NSD/DSI analyses.],
    [There were 345 sentiment-unreliable sessions, all paired with HIGH_OOV reliability events and blank NSD values.],
    [Met as fallback flagging],

    [Presentation Layer],
    [Deliver prompts to intervention participants according to risk state while leaving the control group logging-only.],
    [The prompt log contains 576 shown prompts, all from intervention participants; control participants have no prompt events.],
    [Met in exported logs],

    [Privacy Safeguards],
    [Retain aggregate metrics and study codes without persistent raw text or screenshots in the export.],
    [The exported files contain study codes, numeric metrics, platform labels, prompt actions, and reliability codes only.],
    [Met in export contents],
  ),
)

The architecture evidence shows that the simulated export followed the planned privacy-preserving logging boundary. The logs contain derived metrics and reliability codes, but no raw captions, comments, screenshots, names, or free-text user content. The main implementation constraint visible in the export is sentiment coverage: 16.8% of sessions were marked sentiment-unreliable and therefore could not contribute NSD values.

#thesis_table(
  caption: [Black Box and White Box Testing Summary],
  columns: (1.15fr, 1.5fr, 0.85fr, 1.1fr),
  cell_align: table_align((left, left, center, left)),
  header: (
    [*Testing Area*],
    [*Expected Behavior*],
    [*Result*],
    [*Remarks*],
  ),
  body: (
    [Onboarding and Permissions],
    [The app installs, opens, and guides the participant through required permissions.],
    [Not in export],
    [No onboarding test-result file is included.],

    [Accessibility Extraction],
    [The service detects target app sessions, content transitions, and app-state changes.],
    [Pass in logs],
    [2,051 sessions and matching target foreground/background events were exported.],

    [Text Sentiment Path],
    [The sentiment path scores analyzable content and contributes to NSD.],
    [Pass in logs],
    [1,706 sessions include reliable NSD values.],

    [No-Text / Unreliable Fallback],
    [Unresolved or high-OOV sessions are routed away from main sentiment-based analysis.],
    [Pass in logs],
    [345 sessions were flagged sentiment-unreliable with blank NSD values.],

    [Fuzzy Inference],
    [Each retained session receives a RiskScore and Safe, Warning, or Critical level.],
    [Pass in logs],
    [All 2,051 sessions include risk scores and risk-level labels.],

    [Prompt Triggering],
    [The appropriate intervention prompt appears when risk thresholds are reached.],
    [Pass in logs],
    [576 prompt displays were recorded for intervention participants only.],

    [Local Logging],
    [Aggregate metrics are stored under study codes without raw text or captured frames.],
    [Pass in export],
    [CSV contents are aggregate and study-code based.],
  ),
)

The testing summary is based on exported behavior rather than a separate test-report form. It supports the presence of the logging, scoring, fallback, and prompt-event pipeline in the simulated dataset, but it does not replace a formal device-level black-box and white-box test report.

=== Evaluation Method, Respondents, and Instrument

#thesis_table(
  caption: [Evaluation Method, Respondents, and Instrument Status],
  columns: (1.15fr, 1.4fr, 1.25fr, 1.2fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Evaluation Component*],
    [*Planned Source*],
    [*Data Available in Export*],
    [*Status*],
  ),
  body: (
    [Behavioral log evaluation],
    [Week 1 and Week 2 app-generated usage records],
    [2,051 sessions, 1,227 daily summaries, 8,549 reliability events, and 1,152 prompt events.],
    [Complete for log analysis],

    [User evaluation],
    [Post-usage ISO/IEC 25010, SUS, and TAM survey instrument],
    [50 ISO/IEC 25010 rows, 50 SUS rows, 50 TAM rows, and 50 coded user feedback rows.],
    [Complete for user evaluation],

    [Subject matter expert evaluation],
    [Expert evaluation form covering technical design, privacy safeguards, heuristic logic, and intervention structure],
    [SME files are present, but rating and comment cells are blank.],
    [Pending],

    [Open-ended feedback],
    [Post-usage comments and expert comments],
    [50 coded user feedback rows; SME feedback blank.],
    [Complete for users; SME pending],
  ),
)

The export supports behavioral log analysis, self-report change analysis, user survey scoring, and user feedback coding. Expert scoring and expert qualitative interpretation remain pending.

=== Likert Scale and Scoring Method

#thesis_table(
  caption: [Scoring Method and Interpretation Rules],
  columns: (1.15fr, 1.35fr, 1.25fr, 1.2fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Instrument / Measure*],
    [*Scoring Basis*],
    [*Interpretation Range*],
    [*Status*],
  ),
  body: (
    [ISO/IEC 25010 user survey],
    [Mean of 5-point Likert items by quality characteristic.],
    [Favorable target: mean >= 3.50.],
    [Applied],

    [Technology Acceptance Model survey],
    [Mean of 5-point Likert items for PU and PEOU.],
    [Favorable target: mean >= 3.50.],
    [Applied],

    [System Usability Scale],
    [Standard SUS scoring to a 0-100 composite.],
    [Favorable target: score >= 70.],
    [Applied],

    [Subject matter expert evaluation],
    [Mean of 5-point expert-rating items by evaluation area.],
    [Favorable target: mean >= 4.00.],
    [Pending],
  ),
)

The interpretation thresholds are retained for consistency with Chapter 3. User thresholds are applied in the following sections, while SME thresholds remain pending until expert ratings are completed.

=== Respondent Profile and Platform Exposure

#thesis_table(
  caption: [Participant Profile Summary],
  columns: (1.35fr, 0.8fr, 0.8fr, 0.8fr, 0.9fr),
  cell_align: table_align((left, center, center, center, center)),
  header: (
    [*Profile Variable*],
    [*Intervention Group*],
    [*Control Group*],
    [*Total*],
    [*Remarks*],
  ),
  body: (
    [Enrolled participants],
    [25],
    [25],
    [50],
    [Balanced by study code],

    [Completed Week 1],
    [25],
    [25],
    [50],
    [At least one Week 1 session],

    [Completed Week 2],
    [25],
    [25],
    [50],
    [At least one Week 2 session],

    [Included in primary behavioral log analysis],
    [25],
    [25],
    [50],
    [Complete week-level log coverage],

    [Included in baseline DSI coverage analysis],
    [25],
    [25],
    [50],
    [All had >= 3 reliable Week 1 sessions],
  ),
)

All 50 simulated participants were represented in both study weeks. No participant was excluded from the available log-based analyses because of missing week-level session data.

#thesis_table(
  caption: [Target Platform Use and Extraction Coverage],
  columns: (1.05fr, 0.8fr, 0.8fr, 0.85fr, 1.25fr),
  cell_align: table_align((left, center, center, center, left)),
  header: (
    [*Platform*],
    [*Participants Using Platform*],
    [*Retained Sessions*],
    [*Extraction Success Rate*],
    [*Interpretation Notes*],
  ),
  body: (
    [TikTok],
    [50 / 100.0%],
    [809],
    [82.6%],
    [Largest session share; 668 reliable sessions.],

    [Facebook Reels],
    [50 / 100.0%],
    [473],
    [86.3%],
    [Fewest sessions but highest reliable-session rate.],

    [Instagram Reels],
    [50 / 100.0%],
    [769],
    [81.9%],
    [Broad coverage with slightly lower reliability.],

    [Multiple-platform use],
    [50 / 100.0%],
    [2,051],
    [83.2%],
    [All participants had sessions on at least two target platforms.],
  ),
)

Platform coverage was broad in the simulated export. All target platforms were represented for all participants, and the overall sentiment-reliable session rate was 83.2%. Facebook Reels had the highest extraction success rate, while Instagram Reels had the lowest, although all three platform rates remained above 80%.

=== Research Question 2: Week 1 to Week 2 Behavioral and Self-Report Changes

The second research question asks what short-term Week 1-to-Week 2 changes are observed in selected logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group, and within the intervention group across the same period. The simulated export supports both the logged usage metrics and the pre/post Doomscrolling Scale scores.

#thesis_table(
  caption: [Primary Between-Group Change-Score Comparison],
  columns: (1.45fr, 0.75fr, 0.85fr, 0.85fr, 0.8fr, 0.85fr, 0.85fr),
  cell_align: table_align((left, center, center, center, center, center, center)),
  header: (
    [*Outcome Variable*],
    [*Analyzed n*],
    [*Intervention Change Mean (SD)*],
    [*Control Change Mean (SD)*],
    [*Test Used*],
    [*Effect Size (95% CI)*],
    [*Holm-Adjusted p*],
  ),
  body: (
    [Mean daily raw elapsed session duration (minutes)],
    [I=25; C=25],
    [0.59 (1.59)],
    [-0.38 (2.04)],
    [Welch t],
    [d=0.53 (CI -0.03 to 1.10)],
    [0.200],

    [Mean daily raw elapsed video dwell time (seconds)],
    [I=25; C=25],
    [-0.10 (2.79)],
    [0.34 (2.39)],
    [Welch t],
    [d=-0.17 (CI -0.73 to 0.39)],
    [1.000],

    [Mean daily Negative Sentiment Density (%)],
    [I=25; C=25],
    [-0.70 (5.09)],
    [-1.07 (6.44)],
    [Welch t],
    [d=0.06 (CI -0.49 to 0.62)],
    [1.000],

    [Pre/Post Doomscrolling Scale score],
    [I=25; C=25],
    [-0.46 (0.42)],
    [-0.05 (0.42)],
    [Welch t],
    [d=-0.97 (CI -1.56 to -0.38)],
    [0.005],
  ),
)

The log-derived change-score results did not show a statistically reliable between-group difference after Holm adjustment. The strongest observed log pattern was for raw elapsed session duration: the intervention group increased by 0.59 minutes on average, while the control group decreased by 0.38 minutes, but the confidence interval for the effect size crossed zero. In contrast, the self-reported Doomscrolling Scale showed a larger favorable change in the intervention group than in the control group, with an adjusted p-value of 0.005 and a medium-to-large effect-size estimate. This pattern suggests that users reported less doomscrolling after the intervention period even though the short-term behavioral logs did not show a corresponding reliable reduction.

#thesis_table(
  caption: [Secondary Within-Intervention Pre/Post Comparison],
  columns: (1.45fr, 0.75fr, 0.85fr, 0.85fr, 0.8fr, 0.85fr),
  cell_align: table_align((left, center, center, center, center, center)),
  header: (
    [*Outcome Variable*],
    [*Analyzed n*],
    [*Week 1 Mean (SD)*],
    [*Week 2 Mean (SD)*],
    [*Test Used*],
    [*p-value / Effect Size*],
  ),
  body: (
    [Mean daily raw elapsed session duration (minutes)],
    [25],
    [16.74 (11.39)],
    [17.33 (11.43)],
    [Paired t],
    [p=0.074; dz=0.37],

    [Mean daily raw elapsed video dwell time (seconds)],
    [25],
    [13.61 (7.51)],
    [13.50 (7.37)],
    [Paired t],
    [p=0.854; dz=-0.04],

    [Mean daily Negative Sentiment Density (%)],
    [25],
    [42.15 (23.85)],
    [41.45 (24.25)],
    [Paired t],
    [p=0.501; dz=-0.14],

    [Doomscrolling Scale score],
    [25],
    [3.63 (1.82)],
    [3.17 (1.82)],
    [Paired t],
    [p less than 0.001; dz=-1.08],
  ),
)

The within-intervention comparison similarly showed no statistically reliable Week 1-to-Week 2 change in the available log metrics. The intervention group's session duration increased slightly, dwell time was essentially unchanged, and NSD decreased slightly. The Doomscrolling Scale, however, decreased from 3.63 to 3.17 within the intervention group. Because the control group is needed to contextualize ordinary week-to-week fluctuation, the within-intervention result should be interpreted together with the primary between-group change-score comparison.

#thesis_table(
  caption: [Supplementary Prompt-Excluded Active-Use Metrics],
  columns: (1.35fr, 0.75fr, 0.9fr, 0.9fr, 1.2fr),
  cell_align: table_align((left, center, center, center, left)),
  header: (
    [*Metric*],
    [*Analyzed n*],
    [*Raw Elapsed Result*],
    [*Prompt-Excluded Result*],
    [*Measurement Interpretation*],
  ),
  body: (
    [Session duration],
    [I=25; C=25],
    [Change: I=0.59, C=-0.38 min; d=0.53],
    [Change: I=0.57, C=-0.38 min; d=0.52],
    [Excluding prompt time changed the intervention estimate only slightly.],

    [Video dwell time],
    [N/A],
    [Raw dwell change: I=-0.10, C=0.34 s],
    [Not exported],
    [The export does not contain a separate prompt-excluded dwell column.],
  ),
)

Prompt-excluded duration was close to raw elapsed duration, suggesting that prompt-display time did not materially change the simulated session-duration interpretation. Dwell-time adjustment cannot be evaluated from this export because prompt-excluded dwell time was not exported.

=== Prompt-Response and Reliability Log Summary

#thesis_table(
  caption: [Prompt-Response Summary for Intervention Participants],
  columns: (1.25fr, 0.85fr, 0.85fr, 1.35fr),
  cell_align: table_align((left, center, center, left)),
  header: (
    [*Prompt / Response Indicator*],
    [*Frequency*],
    [*Percent*],
    [*Interpretation*],
  ),
  body: (
    [Prompts shown],
    [576],
    [100.0%],
    [All prompt displays occurred in the intervention group.],

    [Level 2 pause prompts shown],
    [131],
    [22.7%],
    [Shown during Warning-level sessions.],

    [Level 3 breathing prompts shown],
    [445],
    [77.3%],
    [Most shown prompts were tied to Critical sessions.],

    [Continue responses],
    [242],
    [42.0%],
    [Most common response after a shown prompt.],

    [Take-break responses],
    [170],
    [29.5%],
    [Nearly one-third of prompt responses indicated a break.],

    [Dismissed responses],
    [112],
    [19.4%],
    [A notable minority dismissed prompts.],

    [View-dashboard responses],
    [52],
    [9.0%],
    [Dashboard viewing was the least common response.],
  ),
)

Prompt responses suggest that the intervention was engaged with in the simulated logs, but users often continued rather than taking a break. This pattern supports recommendations for prompt timing, prompt burden management, and clearer dashboard value.

#thesis_table(
  caption: [Reliability Event Summary],
  columns: (1.25fr, 0.9fr, 1.6fr),
  cell_align: table_align((left, center, left)),
  header: (
    [*Reliability Event Type*],
    [*Frequency*],
    [*Interpretation*],
  ),
  body: (
    [SERVICE_STARTED],
    [2,051],
    [A service-start event was paired with each exported session.],

    [TARGET_FOREGROUND],
    [2,051],
    [Each session had a target-app foreground event.],

    [TARGET_BACKGROUND],
    [2,051],
    [Each session had a target-app background event.],

    [SESSION_FINALIZED],
    [2,051],
    [Each session was finalized and saved.],

    [HIGH_OOV],
    [345],
    [High out-of-vocabulary or unresolved text caused sentiment-unreliable fallback.],
  ),
)

The reliability log is internally consistent because each exported session is matched by a service-start, foreground, background, and finalization event. The recurring reliability issue is HIGH_OOV, matching the 345 sentiment-unreliable sessions.

=== Research Question 3: Baseline Convergent Association Between DSI and Self-Reported Doomscrolling

The third research question asks what baseline convergent association exists between the fixed-prior Week 1 DSI and participants' self-reported doomscrolling scores among participants with at least three sentiment-reliable Week 1 sessions. The export supports both DSI coverage analysis and the baseline self-report correlation because Week 1 Doomscrolling Scale scores are present for all participants.

#thesis_table(
  caption: [DSI Coverage and Sentiment-Reliable Session Summary],
  columns: (1.35fr, 0.8fr, 0.8fr, 0.8fr, 1.1fr),
  cell_align: table_align((left, center, center, center, left)),
  header: (
    [*Coverage Indicator*],
    [*Intervention Group*],
    [*Control Group*],
    [*Total*],
    [*Interpretation Notes*],
  ),
  body: (
    [Participants with >= 3 sentiment-reliable Week 1 sessions],
    [25],
    [25],
    [50],
    [All participants met the log coverage rule.],

    [Week 1 sentiment-reliable sessions],
    [427],
    [434],
    [861],
    [Enough retained sessions for week-level log aggregation.],

    [Week 1 sentiment-unreliable sessions],
    [94],
    [82],
    [176],
    [Excluded from NSD and DSI calculations requiring sentiment.],

    [Undefined Week 1 DSI cases],
    [0],
    [0],
    [0],
    [No participant failed the log-coverage rule.],
  ),
)

The DSI coverage result is favorable for the simulated logs: all participants had at least three reliable Week 1 sessions, and the Week 1 reliable-session count was high enough to compute log-based baseline aggregates.

#thesis_table(
  caption: [Baseline Component and Composite Correlation with Doomscrolling Scale],
  columns: (1.45fr, 0.75fr, 0.8fr, 0.85fr, 0.85fr, 1.05fr),
  cell_align: table_align((left, center, center, center, center, left)),
  header: (
    [*Baseline Variable*],
    [*N_effective*],
    [*Correlation Type*],
    [*Coefficient*],
    [*p-value*],
    [*Interpretation*],
  ),
  body: (
    [Mean daily session duration],
    [50],
    [Pearson r],
    [0.966],
    [p less than 0.001],
    [Strong positive association.],

    [Mean daily video dwell time],
    [50],
    [Pearson r],
    [-0.916],
    [p less than 0.001],
    [Strong negative association in this scoring direction.],

    [Mean daily NSD],
    [50],
    [Pearson r],
    [0.980],
    [p less than 0.001],
    [Strong positive association.],

    [Composite Week 1 DSI],
    [50],
    [Pearson r],
    [0.989],
    [p less than 0.001],
    [Strongest baseline association.],
  ),
)

The composite Week 1 DSI showed the strongest association with the baseline Doomscrolling Scale. This supports preliminary estimator plausibility in the simulated dataset because the composite score related more strongly to self-reported doomscrolling than session duration alone. The finding should still be framed as convergent association, not diagnostic validation, because the system estimates risk from observable proxies and the study remains pilot-scale.

=== ISO/IEC 25010 Software-Quality Criteria and Per-Category User Results

The fourth research question asks how users evaluate the system using ISO/IEC 25010 and TAM. The export contains 50 ISO/IEC 25010 survey rows, 50 SUS response rows, and 50 TAM response rows.

#thesis_table(
  caption: [ISO/IEC 25010 User Evaluation Summary],
  columns: (1.15fr, 0.75fr, 0.85fr, 0.85fr, 0.9fr, 1.05fr),
  cell_align: table_align((left, center, center, center, center, left)),
  header: (
    [*Quality Characteristic*],
    [*Items*],
    [*Mean*],
    [*SD*],
    [*Target*],
    [*Interpretation*],
  ),
  body: (
    [Functional Suitability],
    [3],
    [3.97],
    [0.35],
    [>= 3.50],
    [Met; favorable.],

    [Performance Efficiency],
    [3],
    [3.83],
    [0.38],
    [>= 3.50],
    [Met; favorable.],

    [Reliability],
    [3],
    [3.91],
    [0.40],
    [>= 3.50],
    [Met; favorable.],
  ),
)

#thesis_table(
  caption: [System Usability Scale Result],
  columns: (1.3fr, 0.8fr, 0.8fr, 0.8fr, 1.15fr),
  cell_align: table_align((left, center, center, center, left)),
  header: (
    [*Usability Measure*],
    [*Analyzed n*],
    [*Mean Score*],
    [*Target*],
    [*Interpretation*],
  ),
  body: (
    [System Usability Scale (0-100)],
    [50],
    [76.35],
    [>= 70],
    [Met; favorable usability.],
  ),
)

#thesis_table(
  caption: [Technology Acceptance Model User Evaluation Summary],
  columns: (1.25fr, 0.75fr, 0.85fr, 0.85fr, 0.9fr, 1.05fr),
  cell_align: table_align((left, center, center, center, center, left)),
  header: (
    [*TAM Construct*],
    [*Items*],
    [*Mean*],
    [*SD*],
    [*Target*],
    [*Interpretation*],
  ),
  body: (
    [Perceived Usefulness],
    [6],
    [4.01],
    [0.40],
    [>= 3.50],
    [Met; favorable.],

    [Perceived Ease of Use],
    [6],
    [4.06],
    [0.31],
    [>= 3.50],
    [Met; favorable.],
  ),
)

All user-evaluation means met the study-defined favorable thresholds. Users rated the system favorably on functional suitability, performance efficiency, reliability, usability, perceived usefulness, and perceived ease of use. The low internal-consistency coefficients reported below indicate that these favorable means should still be interpreted cautiously at the subscale level.

=== Reliability Analysis

#thesis_table(
  caption: [Cronbach's Alpha Results for User Survey Subscales],
  columns: (1.3fr, 0.75fr, 0.85fr, 1.2fr),
  cell_align: table_align((left, center, center, left)),
  header: (
    [*Subscale*],
    [*Items*],
    [*Cronbach's Alpha*],
    [*Interpretation*],
  ),
  body: (
    [Functional Suitability],
    [3],
    [0.106],
    [Below 0.70; interpret subscale cautiously.],

    [Performance Efficiency],
    [3],
    [0.050],
    [Below 0.70; interpret subscale cautiously.],

    [Reliability],
    [3],
    [0.172],
    [Below 0.70; interpret subscale cautiously.],

    [System Usability Scale],
    [10],
    [0.207],
    [Below 0.70 after SUS item scoring; interpret cautiously.],

    [Perceived Usefulness],
    [6],
    [0.687],
    [Slightly below 0.70; near-threshold coherence.],

    [Perceived Ease of Use],
    [6],
    [0.254],
    [Below 0.70; interpret subscale cautiously.],
  ),
)

Cronbach's alpha results were mostly below the conventional 0.70 benchmark. This weak internal consistency may reflect the small pilot sample, heterogeneous item wording, and limited number of items in the researcher-developed ISO/IEC 25010 subscales. The Perceived Usefulness subscale was closest to the threshold at 0.687. The favorable user means should therefore be reported alongside this reliability caution.

=== Algorithm Complexity Analysis

#thesis_table(
  caption: [Algorithm Complexity Analysis],
  columns: (1.25fr, 1.15fr, 1.1fr, 1.2fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Algorithm / Procedure*],
    [*Input Basis*],
    [*Complexity Reported*],
    [*Interpretation Notes*],
  ),
  body: (
    [Accessibility event processing],
    [Event stream and current UI tree size.],
    [O(e + n) per event batch],
    [Processing grows with captured events and traversed nodes.],

    [Sentiment and fallback routing],
    [Extracted text tokens or no-text/unreliable marker.],
    [O(t) for text path],
    [Runtime grows with token count; fallback drops sentiment input.],

    [Fuzzy risk estimation],
    [Three inputs and 27 fixed rules, or two inputs and fallback rules.],
    [O(1) for fixed rule base],
    [Rule count is constant in the fielded design.],

    [Local aggregation and export],
    [Stored session, prompt, reliability, and personalization rows.],
    [O(r) over exported rows],
    [Export cost grows linearly with retained aggregate records.],
  ),
)

The complexity profile is appropriate for an on-device pilot system because the fuzzy inference rule base is fixed and the exported data are aggregate records. The main scalability risks are not the rule engine itself but platform extraction stability, text length, and device-level background service behavior.

=== Research Question 5: Subject Matter Expert Evaluation

The fifth research question asks how subject matter experts evaluate the system's technical design, privacy safeguards, heuristic logic, and intervention structure. The SME files are present in the export, but the rating and comment fields are blank; therefore, SME evaluation remains pending.

#thesis_table(
  caption: [Subject Matter Expert Evaluation Summary],
  columns: (1.35fr, 0.75fr, 0.85fr, 0.85fr, 0.9fr, 1.05fr),
  cell_align: table_align((left, center, center, center, center, left)),
  header: (
    [*Evaluation Area*],
    [*Experts*],
    [*Mean*],
    [*SD*],
    [*Target*],
    [*Interpretation*],
  ),
  body: (
    [Technical soundness of risk-estimation framework],
    [3 pending],
    [Pending],
    [Pending],
    [>= 4.00],
    [Awaiting completed ratings.],

    [Plausibility of input ranges],
    [3 pending],
    [Pending],
    [Pending],
    [>= 4.00],
    [Awaiting completed ratings.],

    [Internal coherence of fuzzy rule base],
    [3 pending],
    [Pending],
    [Pending],
    [>= 4.00],
    [Awaiting completed ratings.],

    [Privacy and architecture quality],
    [3 pending],
    [Pending],
    [Pending],
    [>= 4.00],
    [Awaiting completed ratings.],

    [Intervention design appropriateness],
    [3 pending],
    [Pending],
    [Pending],
    [>= 4.00],
    [Awaiting completed ratings.],

    [Overall expert evaluation],
    [3 pending],
    [Pending],
    [Pending],
    [>= 4.00],
    [Awaiting completed ratings.],
  ),
)

The SME objective remains open. The log and user data can show operational behavior, self-report association, and user acceptance, but expert appraisal is still needed to judge whether the estimator design, thresholds, privacy safeguards, and intervention structure are technically appropriate.

=== Open-Ended Feedback

#thesis_table(
  caption: [Open-Ended Feedback Coding Summary],
  columns: (1.2fr, 0.75fr, 1.45fr, 1.25fr),
  cell_align: table_align((left, center, left, left)),
  header: (
    [*Theme / Issue Category*],
    [*Frequency*],
    [*Representative Concern*],
    [*Design Implication*],
  ),
  body: (
    [Prompt timing and interruption],
    [14],
    [Prompts sometimes appeared while users were deciding whether to continue.],
    [Add quiet hours, gentler frequency, and better timing controls.],

    [Permission and onboarding clarity],
    [12],
    [Users wanted clearer permission and setup explanation.],
    [Improve onboarding instructions and permission rationale.],

    [Dashboard usefulness],
    [12],
    [Users wanted clearer safe, warning, and critical explanations.],
    [Add plain-language labels and weekly trends.],

    [Privacy and trust],
    [6],
    [Users were comfortable with study-code exports if raw captions and screenshots were not saved.],
    [Keep aggregate-only export and communicate it clearly.],

    [Technical reliability],
    [6],
    [Some sessions seemed harder for the app to interpret.],
    [Expose sentiment-reliability status and platform-detection limitations.],
  ),
)

The most frequent user feedback theme was prompt timing and interruption, followed by permission/onboarding clarity and dashboard usefulness. These themes are consistent with the prompt-response logs, where continuing was the most common response and dashboard viewing was the least common response.

=== Overall Evaluation Results

#thesis_table(
  caption: [Overall Results Summary],
  columns: (1.2fr, 1.35fr, 1.35fr, 1.1fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Evaluation Area*],
    [*Evidence Source*],
    [*Overall Result*],
    [*Interpretation Status*],
  ),
  body: (
    [Implementation and testing],
    [Architecture matrix, exported sessions, reliability events, and prompt events],
    [Logging, scoring, fallback flagging, and prompt-event generation are visible in the export.],
    [Log-supported],

    [Behavioral and self-report outcomes],
    [Week 1 to Week 2 logs and Doomscrolling Scale scores],
    [No reliable between-group difference was observed in log metrics, but self-reported doomscrolling decreased more in the intervention group.],
    [Mixed but partly favorable],

    [Estimator plausibility],
    [Week 1 DSI coverage and Doomscrolling Scale correlation],
    [All participants met the log coverage rule, and composite DSI correlated strongly with baseline self-report.],
    [Supported as preliminary plausibility],

    [User evaluation],
    [ISO/IEC 25010, SUS, TAM, and open-ended feedback],
    [All user mean targets were met; reliability coefficients were mostly below 0.70 and require caution.],
    [Supported with reliability caution],

    [Expert evaluation],
    [SME rating form and SME comments],
    [SME files are present but blank.],
    [Pending],
  ),
)

== Narrative Interpretation of Results

The simulated export shows that REDU can produce a privacy-preserving aggregate log dataset across the three target short-form platforms. The strongest evidence is operational: all 50 participants have Week 1 and Week 2 logs, all sessions have risk scores and risk levels, all sessions have reliability-event traces, and prompt events are limited to the intervention group as intended.

The behavioral comparison does not support a strong short-term difference between the intervention and control groups in the available simulated logs. The intervention group showed a small increase in mean daily raw elapsed session duration, while the control group showed a small decrease, but the adjusted result was not statistically reliable. Dwell time and NSD showed negligible between-group differences. The self-reported Doomscrolling Scale, however, decreased more in the intervention group than in the control group. This creates a mixed finding: the intervention was associated with a favorable self-report change, but not with a clear short-term reduction in logged use metrics.

The DSI coverage result is favorable because every participant met the minimum Week 1 sentiment-reliable session rule. The composite Week 1 DSI also showed a strong positive correlation with baseline self-reported doomscrolling. This supports preliminary estimator plausibility, but it should not be overstated as diagnostic validation.

User evaluation results were favorable at the mean-score level. ISO/IEC 25010, SUS, and TAM results met the study-defined targets, and the open-ended feedback showed that users generally recognized the value of break reminders, privacy safeguards, and activity summaries. The main weaknesses were prompt timing, onboarding clarity, dashboard explanation, and technical reliability visibility. SME evaluation remains pending.

== Summary of Findings

#thesis_table(
  caption: [Summary of Findings by Research Question],
  columns: (0.55fr, 1.55fr, 1.35fr, 1.15fr),
  cell_align: table_align((center, left, left, left)),
  header: (
    [*RQ*],
    [*Focus*],
    [*Key Result*],
    [*Interpretation Status*],
  ),
  body: (
    [1],
    [Privacy-preserving architecture and estimation framework],
    [2,051 aggregate sessions, 1,706 reliable sessions, 345 fallback-flagged sessions, and 576 intervention-only prompt displays were exported without raw content.],
    [Log-supported objective],

    [2],
    [Week 1 to Week 2 behavioral and self-report changes],
    [No adjusted between-group difference was observed for log metrics, but the Doomscrolling Scale improved more in the intervention group.],
    [Mixed; self-report favorable],

    [3],
    [Baseline DSI and Doomscrolling Scale association],
    [All 50 participants met Week 1 reliable-session coverage; composite DSI correlated with DSS at r=0.989, p less than 0.001.],
    [Preliminary estimator plausibility],

    [4],
    [User evaluation through ISO/IEC 25010 and TAM],
    [ISO, SUS, and TAM means met targets; open-ended feedback identified prompt timing and onboarding as top issues.],
    [Supported with reliability caution],

    [5],
    [Subject matter expert evaluation],
    [SME files are present but blank.],
    [Pending],
  ),
)

Overall, the simulated export supports REDU's aggregate logging and rule-based risk-estimation workflow, provides preliminary self-report convergence evidence, and shows favorable user acceptance at the mean-score level. It does not yet provide completed SME appraisal, and the behavioral-log results remain mixed rather than clearly favorable.

#pagebreak()
