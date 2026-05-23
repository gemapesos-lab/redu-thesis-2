// ==========================================
// CHAPTER 4: PRESENTATION, ANALYSIS AND INTERPRETATION OF DATA
// Draft shell: data-dependent fields are intentionally left as placeholders.
// ==========================================

#import "utils.typ": continued_thesis_table, table_align, thesis_table

#let ph(label) = [#raw("[TBD after evaluation data: " + label + "]")]

= PRESENTATION, ANALYSIS AND INTERPRETATION OF DATA

This chapter presents the results, analysis, and interpretation of the data collected for the study. The presentation follows the statement of the problem and separates the findings into five parts: the system architecture and estimation framework, the Week 1 to Week 2 behavioral comparison, the baseline convergent association between the Doomscroll Severity Index (DSI) and self-reported doomscrolling, the user evaluation using ISO/IEC 25010 and the Technology Acceptance Model (TAM), and the subject matter expert evaluation.

All numerical entries in this draft are placeholders until the final field deployment, post-usage survey, expert evaluation, and statistical analysis are completed. No favorable or unfavorable finding is inferred in this chapter until the corresponding data are available.

== Presentation of Results

The results are organized according to the five research questions. For data-based sections, the tables use the planned metrics from Chapter 3 and retain explicit placeholders for sample size, descriptive statistics, reliability coefficients, effect sizes, confidence intervals, and p-values. The interpretations following each table should be completed only after the final dataset has been cleaned and analyzed.

=== Research Question 1: Privacy-Preserving Mobile Architecture and Estimation Framework

The first research question asks what privacy-preserving mobile architecture and estimation framework can support doomscrolling-related risk estimation on the target short-form video platforms using behavioral indicators and sentiment-related indicators when reliably resolvable, with 2-input behavioral fallback for sentiment-unreliable sessions. This question is addressed through the completed system design, implementation evidence, and pre-deployment testing results rather than through participant outcome statistics alone.

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
    [Collect target-app interaction events, visible text, app-state changes, aggregate usage signals, and no-text screenshot frames through Android Accessibility Service.],
    ph("insert implemented modules and test evidence"),
    ph("complete / partial / not met"),

    [Business Logic Layer],
    [Route analyzable text through the VADER-compatible path, route no-text items through the on-device VLM path, compute NSD, and apply fuzzy inference.],
    ph("insert implemented modules and test evidence"),
    ph("complete / partial / not met"),

    [2-Input Safety Fallback],
    [Use Video Dwell Time and Session Duration when sentiment remains unresolved or unreliable.],
    ph("insert fallback test result"),
    ph("complete / partial / not met"),

    [Presentation Layer],
    [Deliver awareness notification, pause prompt, breathing overlay, and dashboard feedback according to risk state.],
    ph("insert UI and prompt test result"),
    ph("complete / partial / not met"),

    [Privacy Safeguards],
    [Keep raw text and transient screen frames out of persistent storage and retain only aggregate metrics linked to study codes.],
    ph("insert privacy verification result"),
    ph("complete / partial / not met"),
  ),
)

As shown in the table, the system is evaluated according to whether each planned architectural component was implemented and verified. The final interpretation should explain which components were completed, which components required fallback behavior, and whether the implemented system remained aligned with the privacy-preserving design described in Chapter 3. At this draft stage, the table is retained as an interpretation shell and does not yet establish that the system met the objective.

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
    ph("pass/fail"),
    ph("insert remarks"),

    [Accessibility Extraction],
    [The service detects target app sessions, content transitions, and visible text where available.],
    ph("pass/fail"),
    ph("insert remarks"),

    [Text Sentiment Path],
    [The VADER-compatible path scores analyzable text and contributes to NSD.],
    ph("pass/fail"),
    ph("insert remarks"),

    [No-Text VLM Path],
    [No-text items are routed to the on-device VLM fallback when Accessibility Service screenshot support and device conditions allow.],
    ph("pass/fail"),
    ph("insert remarks"),

    [Fuzzy Inference],
    [Known input combinations produce expected Safe, Warning, or Critical risk states.],
    ph("pass/fail"),
    ph("insert remarks"),

    [Prompt Triggering],
    [The appropriate intervention prompt appears when risk thresholds are reached.],
    ph("pass/fail"),
    ph("insert remarks"),

    [Local Logging],
    [Aggregate metrics are stored under study codes without retaining raw text or captured frames.],
    ph("pass/fail"),
    ph("insert remarks"),
  ),
)

The testing summary should be interpreted as evidence for Research Question 1 only after final implementation testing is completed. Any failures should be described as design constraints, reliability events, or unresolved implementation limitations rather than hidden from the discussion.

=== Evaluation Method, Respondents, and Instrument

The evaluation method, respondent participation, and instruments are presented here as a completion shell. This section should be finalized only after the deployment records, post-usage survey responses, and expert evaluation forms are complete.

#thesis_table(
  caption: [Evaluation Method, Respondents, and Instrument Placeholder],
  columns: (1.15fr, 1.4fr, 1.25fr, 1.2fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Evaluation Component*],
    [*Planned Source*],
    [*Data to Insert*],
    [*Status*],
  ),
  body: (
    [Behavioral log evaluation],
    [Week 1 and Week 2 app-generated usage records],
    ph("insert cleaned log availability, inclusion basis, and analysis status"),
    ph("complete after log validation"),

    [User evaluation],
    [Post-usage ISO/IEC 25010, SUS, and TAM survey instrument],
    ph("insert respondent count, response completeness, and instrument coverage"),
    ph("complete after survey encoding"),

    [Subject matter expert evaluation],
    [Expert evaluation form covering technical design, privacy safeguards, heuristic logic, and intervention structure],
    ph("insert expert count, rating completeness, and feedback coverage"),
    ph("complete after SME evaluation"),

    [Open-ended feedback],
    [Post-usage comments and expert comments],
    ph("insert coding status and theme count"),
    ph("complete after qualitative coding"),
  ),
)

The final manuscript should use this section to identify the actual respondents, instruments, and evaluative evidence used in Chapter 4. No respondent count, survey result, or expert result is interpreted until the corresponding dataset has been verified.

=== Likert Scale and Scoring Method Placeholder

The scoring method for user and expert evaluation should be stated before the per-category results are interpreted. This section should identify the final scale anchors, interpretation ranges, and scoring procedure used for ISO/IEC 25010, TAM, SUS, and SME evaluation instruments.

#thesis_table(
  caption: [Scoring Method and Interpretation Placeholder],
  columns: (1.15fr, 1.35fr, 1.25fr, 1.2fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Instrument / Measure*],
    [*Scoring Basis*],
    [*Interpretation Range to Insert*],
    [*Status*],
  ),
  body: (
    [ISO/IEC 25010 user survey],
    ph("insert final Likert scale anchors and computation procedure"),
    ph("insert interpretation ranges after instrument finalization"),
    ph("complete after survey instrument finalization"),

    [Technology Acceptance Model survey],
    ph("insert final Likert scale anchors and computation procedure"),
    ph("insert interpretation ranges after instrument finalization"),
    ph("complete after survey instrument finalization"),

    [System Usability Scale],
    ph("insert SUS scoring procedure"),
    ph("insert interpretation threshold used by the study"),
    ph("complete after scoring procedure verification"),

    [Subject matter expert evaluation],
    ph("insert expert-rating scale anchors and computation procedure"),
    ph("insert interpretation ranges after SME form finalization"),
    ph("complete after SME instrument finalization"),
  ),
)

The final manuscript should use the same interpretation ranges consistently across Chapter 4, Chapter 5, and Chapter 6. No weighted mean, rank, descriptive interpretation, or acceptability claim should be added until the evaluation data and scoring procedure are complete.

=== Respondent Profile and Platform Exposure

Before presenting the outcome results, the study reports the final participant profile and the platform exposure observed during the two-week field deployment. These summaries clarify the sample used for interpretation and show whether the target platforms produced usable data.

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
    ph("insert n"),
    ph("insert n"),
    ph("insert N"),
    ph("insert remarks"),

    [Completed Week 1],
    ph("insert n"),
    ph("insert n"),
    ph("insert N"),
    ph("insert remarks"),

    [Completed Week 2],
    ph("insert n"),
    ph("insert n"),
    ph("insert N"),
    ph("insert remarks"),

    [Included in primary behavioral analysis],
    ph("insert n"),
    ph("insert n"),
    ph("insert N"),
    ph("insert exclusion basis"),

    [Included in baseline convergent association],
    ph("insert n"),
    ph("insert n"),
    ph("insert N"),
    ph("requires >= 3 reliable Week 1 sessions"),
  ),
)

This table should be used to explain participant flow from enrollment to analysis. If the final analyzed sample is lower than the recruited sample, the interpretation should specify whether attrition, missing survey responses, device incompatibility, service interruption, or sentiment-unreliable sessions caused the reduction.

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
    ph("insert n / %"),
    ph("insert count"),
    ph("insert %"),
    ph("insert stability note"),

    [Facebook Reels],
    ph("insert n / %"),
    ph("insert count"),
    ph("insert %"),
    ph("insert stability note"),

    [Instagram Reels],
    ph("insert n / %"),
    ph("insert count"),
    ph("insert %"),
    ph("insert stability note"),

    [Multiple-platform use],
    ph("insert n / %"),
    ph("insert count"),
    ph("insert %"),
    ph("insert cross-platform note"),
  ),
)

The platform summary should be used to limit claims to the platforms that produced stable extraction and analyzable logs. If one target platform yields low extraction success, the final discussion should avoid generalizing equally across all target platforms.

=== Research Question 2: Week 1 to Week 2 Behavioral and Self-Report Changes

The second research question asks what short-term Week 1-to-Week 2 changes are observed in selected logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group, and within the intervention group across the same period. The primary analysis uses change scores, computed as Week 2 minus Week 1, with the control group used to contextualize natural fluctuation and monitoring awareness.

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
    ph("insert n"),
    ph("insert mean (SD)"),
    ph("insert mean (SD)"),
    ph("t/Welch/Mann-Whitney"),
    ph("insert d and CI"),
    ph("insert p"),

    [Mean daily raw elapsed video dwell time (seconds)],
    ph("insert n"),
    ph("insert mean (SD)"),
    ph("insert mean (SD)"),
    ph("t/Welch/Mann-Whitney"),
    ph("insert d and CI"),
    ph("insert p"),

    [Mean daily Negative Sentiment Density (%)],
    ph("insert n"),
    ph("insert mean (SD)"),
    ph("insert mean (SD)"),
    ph("t/Welch/Mann-Whitney"),
    ph("insert d and CI"),
    ph("insert p"),

    [Pre/Post Doomscrolling Scale score],
    ph("insert n"),
    ph("insert mean (SD)"),
    ph("insert mean (SD)"),
    ph("t/Welch/Mann-Whitney"),
    ph("insert d and CI"),
    ph("insert p"),
  ),
)

The final interpretation should identify the direction, size, and uncertainty of each observed change. A favorable result would be discussed only if the intervention group shows a stronger desirable change than the control group, with attention to effect size and confidence interval. A weak or mixed result should be reported as evidence of limited short-term difference under the pilot conditions rather than as a failure of the broader concept.

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
    ph("insert n"),
    ph("insert mean (SD)"),
    ph("insert mean (SD)"),
    ph("paired t/Wilcoxon"),
    ph("insert p and d"),

    [Mean daily raw elapsed video dwell time (seconds)],
    ph("insert n"),
    ph("insert mean (SD)"),
    ph("insert mean (SD)"),
    ph("paired t/Wilcoxon"),
    ph("insert p and d"),

    [Mean daily Negative Sentiment Density (%)],
    ph("insert n"),
    ph("insert mean (SD)"),
    ph("insert mean (SD)"),
    ph("paired t/Wilcoxon"),
    ph("insert p and d"),

    [Doomscrolling Scale score],
    ph("insert n"),
    ph("insert mean (SD)"),
    ph("insert mean (SD)"),
    ph("paired t/Wilcoxon"),
    ph("insert p and d"),
  ),
)

This secondary comparison should be interpreted cautiously because it does not by itself separate the intervention effect from monitoring awareness, time variation, or other external factors. It is retained to describe the intervention group's observed pre/post pattern and to support comparison with the primary between-group analysis.

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
    ph("insert n"),
    ph("insert result"),
    ph("insert result"),
    ph("explain whether prompts changed measured active-use time"),

    [Video dwell time],
    ph("insert n"),
    ph("insert result"),
    ph("insert result"),
    ph("explain whether prompt time affected dwell estimates"),
  ),
)

The prompt-excluded metrics should be used as a transparency check. If raw elapsed and prompt-excluded results differ, the final discussion should explain whether the difference reflects prompt interruption time rather than ordinary target-platform use.

=== Research Question 3: Baseline Convergent Association Between DSI and Self-Reported Doomscrolling

The third research question asks what baseline convergent association exists between the fixed-prior Week 1 DSI and participants' self-reported doomscrolling scores among participants with at least three sentiment-reliable Week 1 sessions. This analysis does not validate the system as a diagnostic classifier; it checks whether the baseline composite estimator relates plausibly to self-reported doomscrolling under the pilot conditions.

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
    ph("insert n"),
    ph("insert n"),
    ph("insert N_effective"),
    ph("insert coverage interpretation"),

    [Week 1 sentiment-reliable sessions],
    ph("insert count"),
    ph("insert count"),
    ph("insert count"),
    ph("insert retained-session interpretation"),

    [Week 1 sentiment-unreliable sessions],
    ph("insert count"),
    ph("insert count"),
    ph("insert count"),
    ph("insert exclusion interpretation"),

    [Undefined Week 1 DSI cases],
    ph("insert count"),
    ph("insert count"),
    ph("insert count"),
    ph("insert reason"),
  ),
)

The DSI coverage table should be interpreted before the correlation result. If many participants or sessions are excluded because of sentiment-unreliable data, the final discussion should state that estimator plausibility is based on the retained analyzable subset.

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
    ph("insert n"),
    ph("Pearson r / Spearman rho"),
    ph("insert coefficient"),
    ph("insert p"),
    ph("interpret direction and size"),

    [Mean daily video dwell time],
    ph("insert n"),
    ph("Pearson r / Spearman rho"),
    ph("insert coefficient"),
    ph("insert p"),
    ph("interpret direction and size"),

    [Mean daily NSD],
    ph("insert n"),
    ph("Pearson r / Spearman rho"),
    ph("insert coefficient"),
    ph("insert p"),
    ph("interpret direction and size"),

    [Composite Week 1 DSI],
    ph("insert n"),
    ph("Pearson r / Spearman rho"),
    ph("insert coefficient"),
    ph("insert p"),
    ph("interpret plausibility"),
  ),
)

The final interpretation should compare the composite DSI association against its individual components. If DSI shows a stronger and theoretically coherent association than session duration alone, it may be discussed as preliminary estimator plausibility. If the association is weak, inconsistent, or based on low coverage, the finding should be framed as limited pilot evidence rather than as evidence of invalidity or validity.

=== ISO/IEC 25010 Software-Quality Criteria and Per-Category User Results

The fourth research question asks how users evaluate the system using ISO/IEC 25010 and TAM. The user evaluation is reported through per-category descriptive statistics, interpretation thresholds, and internal consistency estimates. The System Usability Scale is reported separately from the researcher-developed ISO/IEC 25010 items because it uses a 0 to 100 scoring procedure.

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
    ph("insert item count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 3.50],
    ph("met / not met / interpret"),

    [Performance Efficiency],
    ph("insert item count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 3.50],
    ph("met / not met / interpret"),

    [Reliability],
    ph("insert item count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 3.50],
    ph("met / not met / interpret"),
  ),
)

The final interpretation should identify which ISO/IEC 25010 characteristics met the study-defined favorable threshold and which did not. Results below the threshold should be discussed as areas requiring improvement, not omitted.

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
    ph("insert n"),
    ph("insert SUS mean"),
    [>= 70],
    ph("met / not met / interpret against target"),
  ),
)

The SUS result should be interpreted using the study target of 70. If the result is below the target, the discussion should identify interface, onboarding, permission, prompt, or dashboard issues raised by the survey and open-ended feedback.

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
    ph("insert item count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 3.50],
    ph("met / not met / interpret"),

    [Perceived Ease of Use],
    ph("insert item count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 3.50],
    ph("met / not met / interpret"),
  ),
)

The final TAM interpretation should distinguish perceived usefulness from perceived ease of use. A high usefulness score with lower ease of use would suggest perceived value but onboarding or interface friction. A low usefulness score would require discussion of whether prompts, dashboard feedback, or estimator explanations were meaningful to participants.

=== Reliability Analysis Placeholder

Cronbach's alpha is used to estimate the internal consistency of the multi-item survey subscales. It is interpreted as a reliability check for the survey responses, not as a stand-alone validation of the system or the constructs. Because the study uses a pilot-sized sample and includes researcher-developed items, each coefficient should be interpreted cautiously and alongside item content.

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
    ph("insert k"),
    ph("insert alpha"),
    ph(">= 0.70 / below threshold"),

    [Performance Efficiency],
    ph("insert k"),
    ph("insert alpha"),
    ph(">= 0.70 / below threshold"),

    [Reliability],
    ph("insert k"),
    ph("insert alpha"),
    ph(">= 0.70 / below threshold"),

    [Perceived Usefulness],
    ph("insert k"),
    ph("insert alpha"),
    ph(">= 0.70 / below threshold"),

    [Perceived Ease of Use],
    ph("insert k"),
    ph("insert alpha"),
    ph(">= 0.70 / below threshold"),
  ),
)

The final interpretation should state which subscales reached the conventional $alpha >= 0.70$ benchmark. If any subscale falls below the benchmark, the discussion should note whether the issue may reflect a small pilot sample, heterogeneous item wording, or weak item coherence.

=== Algorithm Complexity Analysis Placeholder

The algorithm complexity analysis should document the final implementation path after the Android application and analysis scripts are frozen. This placeholder is retained so that computational claims are not made before the implemented routines are reviewed.

#thesis_table(
  caption: [Algorithm Complexity Analysis Placeholder],
  columns: (1.25fr, 1.15fr, 1.1fr, 1.2fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Algorithm / Procedure*],
    [*Input Basis*],
    [*Complexity to Report*],
    [*Interpretation Notes*],
  ),
  body: (
    [Accessibility event processing],
    ph("insert final event-processing input definition"),
    ph("insert time and space complexity after implementation review"),
    ph("insert performance implication"),

    [Sentiment and fallback routing],
    ph("insert final text/no-text routing basis"),
    ph("insert time and space complexity after implementation review"),
    ph("insert device constraint implication"),

    [Fuzzy risk estimation],
    ph("insert final variable and rule-count basis"),
    ph("insert time and space complexity after implementation review"),
    ph("insert scalability implication"),

    [Local aggregation and export],
    ph("insert final stored-record and export basis"),
    ph("insert time and space complexity after implementation review"),
    ph("insert storage and privacy implication"),
  ),
)

The final discussion should report only the complexity supported by the reviewed source code and should avoid claiming efficiency beyond the tested implementation.

=== Research Question 5: Subject Matter Expert Evaluation

The fifth research question asks how subject matter experts evaluate the system's technical design, privacy safeguards, heuristic logic, and intervention structure. The SME results are presented separately from user survey results because the SME panel evaluates technical plausibility and design coherence rather than ordinary user acceptance.

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
    ph("insert expert count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 4.00],
    ph("met / not met / interpret"),

    [Plausibility of input ranges],
    ph("insert expert count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 4.00],
    ph("met / not met / interpret"),

    [Internal coherence of fuzzy rule base],
    ph("insert expert count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 4.00],
    ph("met / not met / interpret"),

    [Privacy and architecture quality],
    ph("insert expert count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 4.00],
    ph("met / not met / interpret"),

    [Intervention design appropriateness],
    ph("insert expert count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 4.00],
    ph("met / not met / interpret"),

    [Overall expert evaluation],
    ph("insert expert count"),
    ph("insert mean"),
    ph("insert SD"),
    [>= 4.00],
    ph("met / not met / interpret"),
  ),
)

The final interpretation should report both the numerical SME ratings and the expert comments. If experts disagree, the discussion should identify whether disagreement concerns the estimator variables, fuzzy rule base, Android extraction assumptions, privacy safeguards, or intervention design.

=== Open-Ended Feedback

Open-ended responses from the post-usage survey are analyzed through light inductive coding. The purpose is to identify recurring usability concerns, perceived intervention value, privacy concerns, device or permission issues, and implementation constraints that are not fully captured by Likert-scale responses.

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
    ph("insert count"),
    ph("insert summarized feedback, not direct identifying quote"),
    ph("insert implication"),

    [Permission and onboarding clarity],
    ph("insert count"),
    ph("insert summarized feedback, not direct identifying quote"),
    ph("insert implication"),

    [Dashboard usefulness],
    ph("insert count"),
    ph("insert summarized feedback, not direct identifying quote"),
    ph("insert implication"),

    [Privacy and trust],
    ph("insert count"),
    ph("insert summarized feedback, not direct identifying quote"),
    ph("insert implication"),

    [Technical reliability],
    ph("insert count"),
    ph("insert summarized feedback, not direct identifying quote"),
    ph("insert implication"),
  ),
)

The final interpretation should connect these themes to the quantitative results only after the coded feedback and survey results are available. For example, any explanation of ease-of-use, onboarding, permission, reliability, or platform-extraction concerns should be tied to verified Chapter 4 evidence.

=== Overall Evaluation Results

The overall results table consolidates the major evaluation areas without replacing the per-category tables. It should be completed only after the preceding result sections have been populated.

#thesis_table(
  caption: [Overall Results Placeholder],
  columns: (1.2fr, 1.35fr, 1.35fr, 1.1fr),
  cell_align: table_align((left, left, left, left)),
  header: (
    [*Evaluation Area*],
    [*Evidence Source*],
    [*Overall Result to Insert*],
    [*Interpretation Status*],
  ),
  body: (
    [Implementation and testing],
    [Architecture matrix, black box testing, and white box testing],
    ph("insert overall implementation/testing result"),
    ph("complete after system testing"),

    [Behavioral and self-report outcomes],
    [Week 1 to Week 2 logs and Doomscrolling Scale scores],
    ph("insert overall change pattern"),
    ph("complete after statistical analysis"),

    [Estimator plausibility],
    [Week 1 DSI coverage and correlation analysis],
    ph("insert overall DSI plausibility result"),
    ph("complete after correlation analysis"),

    [User evaluation],
    [ISO/IEC 25010, SUS, TAM, and open-ended feedback],
    ph("insert overall user evaluation result"),
    ph("complete after survey analysis"),

    [Expert evaluation],
    [SME rating form and SME comments],
    ph("insert overall expert evaluation result"),
    ph("complete after SME review"),
  ),
)

== Narrative Interpretation of Results

The interpretation of results should connect the statistical and descriptive findings back to the five research questions. This section should be completed after all tables above have been populated.

The final narrative should explain the direction, magnitude, and limitations of the verified findings without introducing conclusions that are not supported by the completed tables. #ph("insert narrative interpretation after all Chapter 4 results are available")

== Summary of Findings

#thesis_table(
  caption: [Summary of Findings by Research Question],
  columns: (0.55fr, 1.55fr, 1.35fr, 1.15fr),
  cell_align: table_align((center, left, left, left)),
  header: (
    [*RQ*],
    [*Focus*],
    [*Key Result to Report*],
    [*Interpretation Status*],
  ),
  body: (
    [1],
    [Privacy-preserving architecture and estimation framework],
    ph("insert implementation and testing finding"),
    ph("complete after testing"),

    [2],
    [Week 1 to Week 2 behavioral and self-report changes],
    ph("insert change-score finding"),
    ph("complete after analysis"),

    [3],
    [Baseline DSI and Doomscrolling Scale association],
    ph("insert correlation finding"),
    ph("complete after analysis"),

    [4],
    [User evaluation through ISO/IEC 25010 and TAM],
    ph("insert user evaluation finding"),
    ph("complete after survey analysis"),

    [5],
    [Subject matter expert evaluation],
    ph("insert SME evaluation finding"),
    ph("complete after SME review"),
  ),
)

For the final manuscript, this section should avoid broad claims beyond the data. If results are favorable, the interpretation should still identify the pilot scope and proxy-based nature of the estimator. If results are mixed or below threshold, the interpretation should specify which objective was unmet and what design or deployment condition may explain the result.

#pagebreak()
