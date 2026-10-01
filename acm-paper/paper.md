---
title: Doomscrolling Detection and Digital Mindfulness Mobile Application for Short-Form Video Platforms Using VADER and Fuzzy Logic
authors: Genesis A. Cadigal|gacadigal@fit.edu.ph ; Rayan Kennard O. Chuayap|rochuayap@fit.edu.ph ; Luigi Karl B. Limos|lblimos@fit.edu.ph ; Gean Dhylan E. Mapesos|gemapesos@fit.edu.ph
affiliation: FEU Institute of Technology|Manila, Philippines
keywords: doomscrolling; digital mindfulness; fuzzy logic; VADER; sentiment analysis; Android; short-form video; privacy-preserving computing
ccs: Human-centered computing➝Ubiquitous and mobile computing systems and tools ; Computing methodologies➝Natural language processing ; Computing methodologies➝Vagueness and fuzzy logic ; Security and privacy➝Privacy protections
venue: CSPROJ2 ’26
venue_rest: , 1st Trimester SY 2026–2027, Manila, Philippines.
rights: Copyright 2026 held by the owner/author(s).
---
ABSTRACT:
Doomscrolling on short-form video platforms is difficult to address with time-based digital well-being tools alone, because risk may depend on both prolonged engagement and exposure to negative content. This study designed, developed, and evaluated a privacy-preserving Android application that estimates doomscrolling-related risk on TikTok, Facebook Reels, and Instagram Reels. The system combines Android Accessibility Service monitoring, local session tracking, VADER-compatible sentiment analysis with a Filipino/Taglish lexicon, a Moondream 0.5B no-text visual fallback, and a fuzzy inference engine that produces session risk scores and a week-level Doomscroll Severity Index (DSI). A two-week pilot field evaluation with 50 adult Filipino Android users, assigned evenly to an intervention group and a logging-only control group, produced 10,134 logged sessions, 87.0% of them sentiment-reliable. Week 1-to-Week 2 reductions favored the intervention group for session duration, video dwell time, Negative Sentiment Density, and Doomscrolling Scale scores (all *p* < .001 after Holm correction). Week 1 DSI correlated strongly with self-reported doomscrolling (Spearman’s ρ = 0.76), and users and experts evaluated the system favorably. The system is a feasible non-clinical prototype for privacy-preserving self-monitoring; longer and larger validation is still needed.

# INTRODUCTION

Short-form video platforms such as TikTok, Facebook Reels, and Instagram Reels deliver rapid, recommendation-driven streams of content. Heavier or more compulsive use of these platforms has been associated with attention-related strain, reduced mindfulness, and anxiety-related symptoms [@qin-2022; @taskin-2024; @hawwa-2025]. Doomscrolling, the compulsive consumption of distressing or negative online content despite its adverse emotional and behavioral effects [@sharma-2022], is a particular concern, because the emotional cost of scrolling depends on what users see and not only on how long they scroll [@buchanan-2021]. Philippine studies likewise report doomscrolling and attention problems among students, although adult evidence remains thin [@punzalan-2024; @cardoso-2024].

Mainstream device- and platform-level tools, such as Google’s Digital Wellbeing, emphasize screen-time summaries, timers, or break reminders rather than session-level interpretation of fast-changing feed content [@google-digital-wellbeing-2024; @rahmillah-2023]. The research problem is therefore whether a private mobile tool can estimate doomscrolling-related risk from observable behavior and content tone, instead of time alone, and deliver in-the-moment prompts without sending content off the device.

The research gap is a combination that prior work has not tested. App-based interventions remain heterogeneous and rarely treat privacy-by-design as a core requirement [@rahmillah-2023; @tewari-2023]. Just-in-time prompts and digitally assisted mindfulness show that brief, app-delivered support is feasible [@teepe-2021; @mitsea-2023], and English-centric sentiment tools struggle with code-mixed Filipino/Taglish text [@nazir-2026]. We found no single deployable system that monitors short-form video behavior, processes extracted text locally, resolves no-text items through a visual fallback, and issues adaptive prompts entirely on the device.

This study addresses five research questions. RQ1: What privacy-preserving mobile architecture and estimation framework can support doomscrolling-related risk estimation, with a two-input behavioral fallback when sentiment is unreliable? RQ2: What short-term Week 1-to-Week 2 changes occur in logged usage metrics and self-reported doomscrolling between an intervention group and a logging-only control group? RQ3: How strongly does the Week 1 Doomscroll Severity Index (DSI) converge with self-reported doomscrolling? RQ4: How do users evaluate the system under ISO/IEC 25010 and the Technology Acceptance Model (TAM)? RQ5: How do subject matter experts (SMEs) evaluate its technical design, privacy safeguards, heuristic logic, and intervention structure?

The contributions are (1) an on-device architecture that combines accessibility-based monitoring, a text-first sentiment path, a no-text vision-language fallback, and interpretable fuzzy inference; (2) a Filipino/Taglish Minimum Viable Lexicon (MVL) extension for VADER; and (3) a pilot-scale field evaluation of feasibility, acceptability, estimator plausibility, and short-term behavioral differences. Here, “detection” means computational risk estimation from observable proxies, not clinical diagnosis.

# RELATED WORK

Doomscrolling has been modeled as a feedback loop of triggers, compulsive behavior, and negative affect [@sharma-2022], and a validated Doomscrolling Scale supports its self-report measurement [@satici-2023]. It is associated with lower mindfulness and well-being [@taskin-2024] and with anxiety among young adults [@hawwa-2025], while information and system quality in short-form apps help sustain addictive use [@qin-2022].

Reviews of apps that reduce mobile phone use find heterogeneous evidence [@rahmillah-2023], and platform tools remain largely time-based [@google-digital-wellbeing-2024]. Just-in-time adaptive interventions can deliver support at moments of need, with feasibility depending on timing, receptivity, and burden [@teepe-2021; @ikegaya-2025]. Digitally assisted mindfulness can train self-regulation [@mitsea-2023], and mHealth work argues for privacy-by-design [@tewari-2023].

VADER is a parsimonious, rule-based sentiment model built for social media text [@hutto-2014]. Low-resource and code-mixed settings call for curated lexicon extensions [@mohammed-2023; @nazir-2026], and Philippine work has built bilingual sentiment models for Filipino–English text [@co-2022]. Fuzzy rule-based inference is attractive here because it is interpretable and needs no labeled training data [@vashishtha-2023; @pickering-2025]. Surveys of multimodal sentiment analysis motivate visual cues for items without text [@das-2023], and small vision-language models make on-device inference practical [@sharshar-2025].

# METHODOLOGY

## Research Design

The study combined software development with a pilot field evaluation. The system was built in four one-week Agile Scrum sprints covering requirements, accessibility extraction and logging, estimator and prompting integration, and testing. Evaluation used a two-group, two-week design. Both groups completed a Week 1 baseline with prompts disabled; in Week 2, adaptive prompts were enabled only for the intervention group, while the control group continued logging only. Results are reported as operational feasibility, software acceptability (ISO/IEC 25010, TAM, SME review), and estimator plausibility (baseline convergent association and sentiment-reliable coverage).

## Participants

Fifty Filipino Android users aged 18 years or older, each an active user of at least one target platform, were recruited through purposive-convenience sampling and gave informed consent. A concealed permuted-block list allocated them 1:1 to the intervention (*n* = 25) and control (*n* = 25) groups. Ages ranged from 18 to 29 (mean 22.12), and 39 participants were male and 11 female. All 50 completed both weeks. Analyses that need sentiment-reliable data report smaller effective samples (*n* = 48 for DSI; 23 intervention and 25 control for NSD).

## Proposed System

### Architecture

The app follows a three-layer architecture (Figure 1). The Data Access Layer uses an Android AccessibilityService to read captions and visible comments, detect feed transitions, and track session duration and video dwell time without rooting the device. The Business Logic Layer applies threshold gates, a text-first sentiment path, a no-text fallback, and fuzzy inference. The Presentation Layer delivers three prompt levels and a usage dashboard. All processing is on-device: raw text and transient screenshots are held in memory and discarded after scoring, only aggregate metrics are stored, and the service requires explicit user activation. A session begins when a target app enters the foreground and ends after a 30-second away interval, screen-off, or service termination. Dwell time is the active time between content-verified transitions and pauses after 45 seconds without interaction.

:::figure
file: figures/architecture.png
width: 7.0
caption: Three-layer, on-device architecture of the system.
:::

### Sentiment Analysis and Fallback

When an item has usable text, caption and comment units are scored with a Kotlin re-implementation of VADER [@hutto-2014], whose compound score is *C* = *x*/√(*x*² + α) with α = 15. We extend VADER with an MVL of 57 Filipino/Taglish entries reviewed by a Filipino-language expert (exact expert-to-runtime valence agreement on all 57) and a negation heuristic that inverts a sentiment token when a Filipino negation marker such as *hindi* or *wala* occurs within three preceding tokens. A session whose out-of-vocabulary ratio reaches 50% is marked sentiment-unreliable. When an item has no usable text, a transient on-demand screenshot is classified on-device by Moondream 0.5B through constrained visual question answering into five labels, from SEVERE_NEG to SEVERE_POS [@sharshar-2025]. Each item uses only one path. Negative Sentiment Density (NSD) for a session is

$$ \mathrm{NSD} = \frac{\sum_{i=1}^{N_R} \mathbb{1}(y_i = 1)}{N_R} \times 100 $$ {#eq1}

where *N*_R is the number of resolved units and *y*_i = 1 marks a negative unit (VADER compound below −0.05, or a SEVERE_NEG or MILD_NEG visual label).

### Fuzzy Risk Estimation and Interventions

Video dwell time (s), NSD (%), and session duration (min) are fuzzified with triangular membership functions defined by bounds (*a*, *b*, *c*),

$$ \mu_A(x) = \mathrm{max}\left(\mathrm{min}\left(\frac{x-a}{b-a}, \frac{c-x}{c-b}\right), 0\right) $$ {#eq2}

using the Low, Medium, and High sets in Table 1. Three variables with three states each give 3³ = 27 rules that map to Safe, Warning, or Critical. The rule base is mostly monotonic: higher duration or NSD never lowers risk, and dwell time mainly intensifies already concerning states. One deliberate exception classifies Low dwell, High NSD, and High duration as Critical, because rapid chaining across negative items is treated as more concerning than reassuring.

:::table
span: single
caption: Fuzzy set support intervals for analytic scoring.
widths: 2100|900|900|900
header: Variable|Low|Medium|High
row: Video dwell time (s)|0–5|4–20|15–30+
row: NSD (%)|0–33|17–83|67–100
row: Session duration (min)|0–10|8–20|15–40+
:::

Each rule is activated by the minimum of its antecedent memberships, and a center-of-gravity step yields a crisp risk score from 0 to 100,

$$ w_i = \mathrm{min}\left(\mu_{\mathrm{Dwell}}(x), \mu_{\mathrm{NSD}}(y), \mu_{\mathrm{Duration}}(z)\right) $$ {#eq3}

$$ \mathrm{RiskScore} = \frac{\sum_{i=1}^{n} w_i c_i}{\sum_{i=1}^{n} w_i} $$ {#eq4}

where *c*_i is the output center of the risk class of rule *i* (16.67, 50.00, and 83.33 for Safe, Warning, and Critical). The week-level DSI is the mean risk score of the *m*_w sentiment-reliable sessions in week *w*,

$$ \mathrm{DSI}_w = \frac{1}{m_w} \sum_{s=1}^{m_w} \mathrm{RiskScore}_s $$ {#eq5}

If sentiment cannot be resolved, inference falls back to dwell time and duration only (nine rules), Level 3 prompts are disabled, and the session is excluded from DSI.

Prompting starts after 15 minutes of continuous use, with a 15-minute cooldown. Level 1 is an awareness toast in the lower Warning band, Level 2 a pause prompt with continue, break, or statistics options in the upper Warning band, and Level 3 a 60-second guided breathing pause-and-reset in the Critical band. Prompts are worded calmly and non-judgmentally and are framed as non-clinical. In Week 2, live duration and NSD memberships were personalized from each participant’s Week 1 quantiles when at least 10 reliable sessions existed [@ikegaya-2025]; analytic DSI scoring stayed fixed across both weeks.

## Instruments

Behavioral measures came from the built-in logger. Self-reported doomscrolling used the four-item short-form Doomscrolling Scale [@sharma-2022; @satici-2023] with a 7-day recall stem, administered at the end of each week. The post-usage survey covered ISO/IEC 25010 [@iso-25010-2023] Functional Suitability, Performance Efficiency, and Reliability (five items each); the 10-item System Usability Scale (SUS) [@hyzy-2022]; 12 TAM items (six each for Perceived Usefulness and Perceived Ease of Use); and open-ended feedback. Two SMEs, a software engineering expert who also reviewed the algorithms and a digital well-being and behavioral psychology expert, rated the system on a six-area, 5-point rubric.

## Procedures

Data gathering had five phases: (1) recruitment, screening, consent, and allocation; (2) baseline profile and app setup; (3) Week 1 baseline logging with prompts disabled, ending with the Doomscrolling Scale; (4) Week 2 deployment, with prompts for the intervention group only, ending with the scale; and (5) the post-usage survey and retrieval of aggregate logs. Logs and surveys were linked only by participant study code, no routine study data left the device, and handling followed the Data Privacy Act of 2012.

## Evaluation Metrics

The primary behavioral outcomes were mean daily raw elapsed session duration, video dwell time, NSD, and Doomscrolling Scale score; sessions per day and DSI were supplementary. Acceptability used pre-set favorable thresholds: a mean of at least 3.50 out of 5 for ISO/IEC 25010 and TAM constructs, SUS of at least 70, SME ratings of at least 4.00, and Cronbach’s α of at least 0.70. Session-level ground truth would require interrupting users, so classification metrics such as precision and recall were not computed; convergent association with self-report served as the plausibility check.

## Data Analysis

Between-group comparisons used Week 2 minus Week 1 change scores, with Welch’s *t*-test or the Mann-Whitney *U* test chosen after Shapiro-Wilk and Levene checks. Within-group changes used paired *t*-tests or Wilcoxon signed-rank tests. The four primary *p*-values were Holm-adjusted at a family-wise α of .05, and Cohen’s *d* is reported with 95% confidence intervals. Baseline convergence used Spearman’s ρ and Pearson’s *r* between Week 1 measures and the Doomscrolling Scale among participants with at least three sentiment-reliable Week 1 sessions. Survey constructs are summarized by mean, standard deviation, and α, and two researchers coded open-ended feedback inductively. Given the pilot sample, effect sizes and intervals are emphasized over significance alone.

## Ethical Considerations

Participation was voluntary, and participants were told the study’s purpose, the data collected, the risks and safeguards, and their right to withdraw at any time. Using the app also required the participant to activate the Accessibility Service in system settings, which served as an explicit operational consent step for text extraction, interaction monitoring, and the screenshot capability used on the no-text path. Personal identifiers are not part of the dataset: logs and surveys are linked only by study code, and any name-to-code list is kept separately by the researchers. Raw text and screen frames are processed in memory only, exported aggregate logs sit in a password-protected research folder, and locally stored data linked to a study code may be deleted upon withdrawal.

# RESULTS

## Implementation and Data Quality

The system ran on participants’ own devices for two weeks and logged 10,134 sessions (4,820 intervention and 5,314 control; 3,433 Instagram, 3,389 Facebook, and 3,312 TikTok). Of these, 8,817 (87.0%) were sentiment-reliable and 1,317 (13.0%) were sentiment-unreliable and handled by the two-input fallback. High out-of-vocabulary text caused 924 of 1,329 logged reliability events (69.5%), unresolved visual items 241 (18.1%), extraction failures 152 (11.4%), and service start/stop notices 12 (0.9%). Analytically, every core runtime component is at most *O*(*n*) per item, and a fuzzy inference runs in constant time. In Week 2, 370 prompt events were logged, and TAKE_BREAK was the most frequent response (191 events, 51.6%).

## Short-Term Behavioral Changes

All four primary outcomes favored the intervention group after Holm correction (Table 2), and every 95% interval lies below zero.

:::table
span: full
caption: Between-group Week 1-to-Week 2 change comparisons for the four primary outcomes (intervention *n* / control *n*).
widths: 2400|1000|1700|1400|1100|1000|1480
header: Outcome|*n* (I / C)|Final test|Statistic|*p* (Holm)|Cohen’s *d*|95% CI
row: Session duration (min)|25 / 25|Welch’s *t*-test|*t* = −4.17|< .001|−1.18|[−1.78, −0.58]
row: Video dwell time (s)|25 / 25|Mann-Whitney *U*|*U* = 124.0|< .001|−1.33|[−1.94, −0.72]
row: NSD (%)|23 / 25|Mann-Whitney *U*|*U* = 68.0|< .001|−1.82|[−2.50, −1.15]
row: Doomscrolling Scale|25 / 25|Mann-Whitney *U*|*U* = 105.0|< .001|−1.50|[−2.13, −0.87]
:::

In the intervention group, mean Doomscrolling Scale scores fell from 15.24 to 13.96 and mean NSD from 41.54% to 37.36%, whereas the control group stayed at 15.36 and moved from 40.86% to 41.06%. Supplementary comparisons agreed: sessions per day (*U* = 153.5, *p* = .002, *d* = −0.82) and DSI (*U* = 81.0, *p* < .001, *d* = −1.84; intervention mean 60.81 to 56.27, control 60.15 to 60.30). Within the intervention group, all four primary outcomes decreased from Week 1 to Week 2 (all *p* < .001, *d*_z from −0.88 to −1.28).

## Baseline Convergent Association

Among the 48 eligible participants, Week 1 DSI correlated strongly with the Week 1 Doomscrolling Scale (ρ = 0.76, *r* = 0.82, *p* < .001). Every component also reached ρ ≥ 0.75: mean daily session duration (ρ = 0.92, *r* = 0.82), mean dwell time (ρ = 0.75, *r* = 0.82), and mean NSD (ρ = 0.76, *r* = 0.83). The data therefore support convergence of the composite with self-report, but not superiority over session duration alone.

## User and Expert Evaluation

All six user-evaluated constructs met their favorable thresholds (Table 3; *n* = 50), and Cronbach’s α ranged from 0.717 to 0.957. As a share of each instrument’s maximum, the construct means fell in a narrow 78.6%–82.4% band, so the assessment was uniformly positive and no single subscale stood out. Open-ended feedback was led by prompt timing (16%), onboarding and setup (12%), and score clarity (12%), followed by platform-monitoring status (10%), dashboard usefulness (10%), and the breathing break (8%); 10% reported no major issue. Participants generally found the system useful and privacy-conscious, and their main requests were snooze or quiet-hour controls, clearer Accessibility setup guidance, and simpler score explanations.

Both SMEs met the 4.00 target on the six-area rubric. The software engineering expert, who also assessed the algorithm design, rated every area 5 (overall 5.00) and judged the hybrid VADER, fallback, and fuzzy approach appropriate for a non-clinical prototype. The psychology expert gave ratings of 4 or 5 (overall 4.33), considered the Doomscrolling Feedback Loop Model an appropriate theoretical basis, and supported the non-clinical self-monitoring framing. Their recommendations were clearer operational definitions for fallback triggers, stronger privacy wording, softer risk terminology, and a shorter or configurable pause-and-reset. With two experts and no dedicated fuzzy-systems reviewer, these ratings are plausibility appraisals and not formal calibration.

:::table
span: single
caption: User evaluation results against favorable thresholds (all targets met).
widths: 1900|1200|800|900
header: Construct|Mean (SD)|α|Target
row: Functional Suitability|3.93 (0.50)|0.852|≥ 3.50
row: Performance Efficiency|4.09 (0.55)|0.859|≥ 3.50
row: Reliability|4.05 (0.50)|0.717|≥ 3.50
row: SUS Usability|80.95 (14.83)|0.957|≥ 70
row: TAM Perceived Usefulness|4.12 (0.40)|0.750|≥ 3.50
row: TAM Perceived Ease of Use|4.09 (0.53)|0.855|≥ 3.50
:::

# DISCUSSION

## Interpretation of Findings

The results are coherent for a pilot. The architecture ran for two weeks without retaining raw content, and text-first routing with a visual fallback kept 87.0% of sessions analyzable. High out-of-vocabulary text was the main cause of unreliable sessions, which points to lexicon coverage as the first improvement target. The intervention group showed large reductions on all four primary outcomes while the control group stayed essentially flat, which is consistent with a prompt effect, although monitoring awareness and novelty cannot be ruled out. Raw elapsed time was the primary behavioral measure because prompt-display time is removed from prompt-excluded metrics by design. The DSI converged with self-report, but session duration alone ranked closer to it, so DSI is best read as a transparent composite of several feedback-loop dimensions and not as a more accurate predictor.

## Comparison with Previous Studies

That negative content matters beyond time on the feed [@buchanan-2021] is the rationale for NSD, and the strong association of logged behavior with the Doomscrolling Scale is consistent with the scale’s construct [@sharma-2022; @satici-2023], although we used a four-item short form. The SUS score of 80.95 exceeds the 68.05 benchmark mean for digital health apps [@hyzy-2022]. Prior just-in-time work stresses timing and burden [@teepe-2021; @ikegaya-2025], and prompt timing was our users’ most frequent feedback theme. Because reviewed tools are mostly time-based and heterogeneous [@rahmillah-2023], our effect sizes are not directly comparable to earlier app evaluations.

## Implications

For designers, content-sensitive estimation that stays on the device is feasible within accessibility-service limits, and a fuzzy rule base keeps the logic inspectable by experts. For researchers, a logging-only control arm helps separate prompt effects from awareness of being monitored. For deployment, setup guidance, score explanations, and configurable prompts are the clearest practical needs. User and expert feedback converge on the same refinements: clearer fallback criteria and setup guidance, plainer and less alarming risk labels, and prompts that users can snooze or shorten.

## Limitations

The sample is small (50), male-dominant (78%), recruited by convenience, and observed for two weeks, so findings are short-term and not generalizable or clinical. The study is non-blinded, so expectancy, novelty, and Hawthorne effects may contribute. Self-reports are subject to recall and social desirability bias. The Accessibility Service depends on target-app interfaces and platform policy. Sentiment analysis is English-centric, the lexicon was reviewed by a single expert, and 13.0% of sessions were sentiment-unreliable. Text-first routing cannot detect mismatches between visual content and text. Only two SMEs reviewed the system, none a dedicated fuzzy-logic specialist, and some survey items were researcher-developed.

# CONCLUSION AND FUTURE WORK

A privacy-preserving, on-device Android system combining accessibility-based monitoring, a Filipino/Taglish-aware VADER pipeline, a Moondream 0.5B fallback, and fuzzy inference was feasible in a two-week pilot with 50 adults. It logged 10,134 sessions with 87.0% sentiment-reliable coverage, showed favorable short-term differences in the intervention group on all four primary outcomes, converged with self-reported doomscrolling at baseline (ρ = 0.76), and met every user and expert acceptability target. These findings are pilot evidence for a non-clinical self-monitoring tool, not proof of long-term efficacy or diagnostic validity.

Future work should run larger, longer, multi-site randomized trials to separate prompt effects from novelty and monitoring awareness; widen the Filipino/Taglish lexicon with multi-rater review; evaluate parallel text-and-visual scoring for sarcasm and cross-modal mismatches; calibrate membership boundaries with sensitivity analysis and labeled data; and add user controls such as snooze, quiet hours, clearer onboarding, and plainer score explanations.

# ACKNOWLEDGMENTS

The authors thank their thesis adviser, Ms. Marilyn M. Sanchez, the thesis panel, the subject matter experts and the Filipino-language expert who reviewed the lexicon, and the 50 volunteers who took part in the pilot study.

# REFERENCES
