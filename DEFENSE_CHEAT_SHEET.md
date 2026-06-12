# REDU Defense Cheat Sheet

Use this as the defense lookup sheet. The safest defense posture is:
REDU is a privacy-preserving, non-clinical Android prototype for heuristic doomscrolling-related risk estimation and digital mindfulness prompting on short-form video platforms. It estimates risk from observable proxies. It does not diagnose, treat, prevent, or clinically validate doomscrolling.

## 1. Thirty-Second Pitch

This thesis designed, developed, and evaluated REDU, a privacy-preserving Android prototype for heuristic doomscrolling-related risk estimation on TikTok, Facebook Reels, and Instagram Reels. Instead of relying only on screen time, REDU combines session duration, video dwell time, and Negative Sentiment Density from a text-first VADER plus Filipino/Taglish MVL pipeline, with an on-device Moondream 0.5B VLM fallback for no-text items. It uses fuzzy logic to produce interpretable risk scores and adaptive non-clinical prompts. In a two-week pilot with 50 adult Filipino Android users, the system logged 10,134 sessions with 87.0% sentiment-reliable coverage, showed favorable short-term Week 1-to-Week 2 differences for the intervention group, met ISO/IEC 25010, SUS, TAM, and SME targets, and showed baseline convergence between DSI and self-reported doomscrolling while staying within pilot, non-clinical limits.

## 2. Safest Wording

Say:

- "Heuristic doomscrolling-related risk estimation."
- "Observable behavioral and sentiment-related proxies."
- "Pilot-scale software-engineering evaluation."
- "Baseline convergent plausibility."
- "Observed short-term differences under study conditions."
- "Privacy-preserving local processing and aggregate export."
- "Digital wellness prompts" or "reflective interruption."

Do not say:

- "Clinically detects doomscrolling."
- "Diagnoses mental health risk."
- "Prevents doomscrolling."
- "Validated treatment."
- "All data is anonymous." Say "study-code-linked; identifiers are separated."
- "DSI is better than duration." Say "DSI converged strongly, but duration had the highest rank association in this pilot."
- "The AI understands videos." Say "a constrained on-device VLM labels no-text items when text is unavailable."

## 3. Study At A Glance

| Item | Defense answer |
|---|---|
| General objective | Design, develop, and evaluate an on-device Android app for doomscrolling-related risk estimation on short-form video platforms. |
| Target platforms | TikTok, Facebook Reels, Instagram Reels. |
| Target users | Filipino Android users aged 18+ who use at least one target platform. |
| Design | Design-and-development study with a two-group baseline-intervention pilot field evaluation. |
| Allocation | 1:1 concealed permuted-block allocation: 25 intervention, 25 logging-only control. |
| Timeline | Week 1 baseline logging (May 25-31, 2026) with prompts off; Week 2 (Jun 1-7, 2026) intervention prompts on only for intervention group. |
| Sample | N = 50; age 18-29, mean 22.12; 39 male, 11 female. |
| Logged sessions | 10,134 total; 8,817 sentiment-reliable; 1,317 sentiment-unreliable. |
| Reliable coverage | 87.0% sentiment-reliable. |
| Key methods | Accessibility Service, VADER + MVL, Moondream VLM fallback, fuzzy inference, adaptive prompts, Room/SQLite, CSV/ZIP export. |
| Evaluation | RQ2 change-score tests, RQ3 correlations, ISO/IEC 25010, SUS, TAM, SME review, open-ended feedback. |
| Main conclusion | Feasible and acceptable pilot prototype; promising short-term results; requires larger, longer validation before broader claims. |

## 4. Research Questions And Evidence Matrix

| RQ | What it asks | Evidence | Defense interpretation |
|---|---|---|---|
| RQ1 | What architecture/framework supports private risk estimation? | Implemented Android system, 10,134 sessions, 87.0% reliable coverage, MVL concordance, reliability logs, prompt logs. | Operational feasibility of bounded local heuristic risk estimation. |
| RQ2 | What Week 1-to-Week 2 changes occurred? | Between-group change-score tests and within-intervention tests. | All four primary outcomes favored intervention after Holm correction; short-term observed differences only. |
| RQ3 | Does Week 1 DSI converge with self-report? | N = 48, Week 1 DSI vs Doomscrolling Scale, Spearman rho = 0.76, p < .001. | Strong convergent plausibility, not diagnostic validation and not superiority over duration. |
| RQ4 | How did users evaluate the system? | ISO/IEC 25010, SUS, TAM, Cronbach alpha, open-ended feedback. | All targets met; usability and acceptance favorable; UX refinements still needed. |
| RQ5 | How did SMEs evaluate it? | Two SME rubrics and narrative comments. | Favorable expert plausibility appraisal; limited by two experts and no dedicated fuzzy/data-science reviewer. |

## 5. SOP And Objectives Alignment

Defense alignment line: Each SOP question has a mirrored specific objective, a Chapter 3 method, a Chapter 4 evidence table, and a Chapter 5 conclusion. The claims are bounded to feasibility, acceptability, convergent plausibility, SME appraisal, and short-term observed differences. There is no orphan objective and no result that answers a different question.

| SOP / RQ | Matching objective | Method / evidence | Chapter 4 table(s) | What can be claimed | What not to overclaim |
|---|---|---|---|---|---|
| RQ1 architecture and estimation framework | SO1: design/develop risk estimation using behavioral indicators, sentiment indicators when reliable, and 2-input fallback. | Chapter 3 architecture, Accessibility Service monitoring, VADER + MVL, no-text VLM fallback, fuzzy inference, Room/SQLite export. | Data Quality and Session Reliability; Prompt-Response Summary; Time Complexity Summary; Summary by RQ. | REDU is technically feasible as a bounded privacy-preserving Android prototype. | Do not claim clinical detection, full content understanding, platform-proof extraction, or validated thresholds. |
| RQ2 Week 1-to-Week 2 changes | SO2: determine short-term changes in logged metrics and self-report between intervention/control and within intervention. | Two-week field deployment; change-score tests; Holm correction for four primary outcomes; within-intervention paired tests as secondary evidence. | RQ2 Primary Behavioral Comparisons; RQ2 Supplementary and Within-Intervention Comparisons. | Intervention condition showed favorable short-term observed differences in this pilot. | Do not claim long-term efficacy, causality beyond pilot conditions, habit formation, or treatment effect. |
| RQ3 baseline DSI association | SO3: determine baseline convergent association between fixed-prior Week 1 DSI and self-report among eligible participants. | Week 1 DSI and Doomscrolling Scale; N = 48; Spearman rho primary, Pearson reported for completeness. | RQ3 Baseline Convergent Association with Week 1 Doomscrolling Scale. | DSI has strong baseline convergent plausibility with self-report. | Do not claim diagnostic validity, superiority over duration, or calibrated population cutoff. |
| RQ4 user evaluation | SO4: evaluate user perspective using ISO/IEC 25010 and TAM. | Post-usage survey; ISO/IEC 25010 selected characteristics; SUS; TAM PU/PEOU; Cronbach alpha; open-ended themes. | ISO/IEC 25010, SUS, TAM, and Reliability Summary; Participant Open-Ended Feedback Theme Summary. | Users rated quality, usability, usefulness, and ease of use favorably against study targets. | Do not claim universal UX validation, externally validated researcher-made ISO items, or no usability issues. |
| RQ5 SME evaluation | SO5: obtain SME evaluation of technical design, privacy safeguards, heuristic logic, and intervention structure. | Two expert rubrics: mobile/software and digital well-being/behavioral psychology; descriptive reporting against favorable target. | Subject Matter Expert Rubric Results; Summary by RQ. | SMEs supported the design as plausible and appropriate for a non-clinical prototype. | Do not claim formal calibration, content-validity index, or full expert consensus across all domains. |

Fast oral script:

| If panel asks... | Answer |
|---|---|
| "Are the SOP and objectives aligned?" | "Yes. The five SOP questions are mirrored by five specific objectives. RQ1/SO1 covers design and implementation, RQ2/SO2 covers short-term field changes, RQ3/SO3 covers DSI convergence, RQ4/SO4 covers user evaluation, and RQ5/SO5 covers expert evaluation." |
| "Where is the general objective answered?" | "Across all five RQs: the system was designed and developed in Chapter 3, then evaluated in Chapter 4 through operational logs, short-term comparisons, convergent association, user evaluation, and SME review." |
| "What is the strongest alignment defense?" | "The study never shifts into clinical diagnosis. Every objective stays within the software-engineering pilot frame: feasibility, acceptability, estimator plausibility, expert appraisal, and short-term observed differences." |

## 6. Timeline To Memorize

Development timeline (four one-week Agile Scrum sprints, completed before the May 25, 2026 field start; the manuscript states sprint order, not calendar dates):

| Stage | Approx. window | What happened |
|---|---|---|
| Sprint 1 | late Apr 2026 | Requirements, planning, target platforms, privacy framing, data model. |
| Sprint 2 | early May 2026 | Accessibility extraction, platform adapters, local logging, session/dwell tracking. |
| Sprint 3 | early-mid May 2026 | VADER + MVL, no-text VLM fallback, fuzzy risk engine, prompt policy. |
| Sprint 4 | mid-late May 2026 | Testing, bug fixes, export, deployment preparation, documentation. |

Field evaluation timeline (dates verified from `study_periods.csv`; all participants share the same nominal week boundaries, Asia/Manila time):

| Phase | Dates (2026) | What happened |
|---|---|---|
| Phase 1 | mid-May, before May 25 | Recruitment, screening, consent, compatibility check, 1:1 allocation. |
| Phase 2 | final week before May 25 | Baseline profile and app setup; participant enabled Accessibility Service. |
| Phase 3 (Week 1) | Mon May 25 - Sun May 31 | Baseline logging; prompts disabled for both groups; Doomscrolling Scale at week end. |
| Phase 4 (Week 2) | Mon Jun 1 - Sun Jun 7 | Deployment; prompts enabled only for intervention group; control remained logging-only; Doomscrolling Scale at week end. |
| Phase 5 | Jun 7 onward | Post-usage survey, SME review, aggregate log export, data analysis (analysis package finalized Jun 13). |

If asked for exact field dates: first logged session Mon May 25, 2026 08:00; last logged session Sun Jun 7, 2026 15:45 (Asia/Manila).

## 7. System Data Flow

1. Android Accessibility Service observes target app foreground state, accessibility events, visible UI text, scroll/touch/click events, and no-text item situations.
2. Platform adapter confirms target short-form surface: TikTok, Facebook Reels, or Instagram Reels.
3. Session tracker computes session duration, item transitions, video dwell time, resolved units, negative units, and reliability events.
4. Text route: visible captions/comments/creator/post text go to VADER-compatible sentiment with Filipino/Taglish MVL support.
5. No-text route: if usable text is absent, the app uses `AccessibilityService.takeScreenshot`, resizes one frame in memory, and sends it to local Moondream 0.5B.
6. Reliability route: high OOV, VLM failure, screenshot failure, or extraction failure marks sentiment as unreliable.
7. Fuzzy engine produces RiskScore from dwell + NSD + duration, or fallback RiskScore from dwell + duration.
8. Prompt policy maps risk to no prompt, Level 1, Level 2, or Level 3, subject to live gate, cooldown, sustained-risk window, and fallback caps.
9. Room/SQLite stores aggregate session/prompt/reliability/settings data.
10. Export produces study-code-linked CSV/ZIP outputs without raw captions or screenshots.

## 8. Abbreviations And Core Terms

Abbreviation expansions (say the full form once before using the abbreviation in an answer):

| Abbreviation | Full form | One-line meaning |
|---|---|---|
| REDU | Project/app name (not an acronym) | The Android app; the manuscript calls the system the Heuristic Risk-State Estimation System. |
| API | Application Programming Interface | The interface one program exposes to another; here, Android's Accessibility API. |
| CI | Confidence Interval | The range that likely contains the true effect; 95% CI means the procedure captures the true value 95% of the time. |
| CoG | Center of Gravity | The defuzzification method that turns fired fuzzy rules into one 0-100 score. |
| DSI | Doomscroll Severity Index | Weekly mean RiskScore over sentiment-reliable sessions. |
| ISO/IEC 25010 | International Organization for Standardization / International Electrotechnical Commission standard 25010 | The software product quality model used for the user evaluation. |
| JITAI | Just-in-Time Adaptive Intervention | Intervention delivered at the right moment with adaptive rules; the literature basis for the prompt design. |
| MVL | Minimum Viable Lexicon | The small Filipino/Taglish word list added to VADER. |
| NSD | Negative Sentiment Density | Percent of resolvable session content classified negative. |
| OOV | Out-of-Vocabulary | Tokens the sentiment lexicon does not recognize. |
| PU / PEOU | Perceived Usefulness / Perceived Ease of Use | The two TAM acceptance constructs. |
| RQ | Research Question | The five thesis questions. |
| SD | Standard Deviation | Spread of scores around the mean. |
| SFV | Short-Form Video | TikTok/Reels-style feed content. |
| SME | Subject Matter Expert | The two expert reviewers (mobile/software; behavioral psychology). |
| SUS | System Usability Scale | Standard 10-item usability instrument, scored 0-100. |
| TAM | Technology Acceptance Model | User-acceptance framework (PU + PEOU). |
| UI | User Interface | The visible screens/elements of an app. |
| VADER | Valence Aware Dictionary and sEntiment Reasoner | Lexicon- and rule-based sentiment analyzer for social-media text. |
| VLM | Vision-Language Model | A model that takes an image and answers in text; here Moondream 0.5B. |
| VQA | Visual Question Answering | Asking a VLM a constrained question about an image. |

Core terms:

| Term | Meaning in defense |
|---|---|
| Accessibility Service | Android mechanism that lets an app, with explicit user activation, read on-screen content and UI events of other apps; REDU's data source, no rooting needed. |
| Moondream 0.5B | A small open vision-language model (about 0.5 billion parameters) that runs fully on the phone; labels no-text items. |
| Room / SQLite | Android's local database layer; stores only aggregate metrics on-device. |
| Edge computing / on-device processing | All inference happens on the user's phone; no raw content leaves the device. |
| Doomscrolling | Compulsive and continuous consumption of negative/distressing content; anchored on Sharma et al. and the Doomscrolling Scale. |
| Heuristic risk estimation | Proxy-based computational estimate, not diagnosis or ground-truth detection. |
| Session duration | Continuous active target-platform use; session ends on >30-second target exit, screen off, or service stop. |
| Video dwell time | Seconds spent on one short-form item before transition; idle dwell pauses after 45 seconds without micro-interaction. |
| NSD | Negative Sentiment Density: negative resolvable units divided by all resolvable units, multiplied by 100. |
| DSI | Doomscroll Severity Index: weekly mean of fixed-prior RiskScore over sentiment-reliable sessions. |
| RiskScore | 0-100 fuzzy output; Safe <33.33, Warning 33.33-66.66, Critical >=66.67. |
| Sentiment-reliable session | Session whose negativity can be resolved by text or no-text VLM path. |
| Sentiment-unreliable session | High-OOV or unresolved no-text/extraction case; excluded from NSD/DSI analyses where reliability is required. |
| VADER | Lexicon/rule-based social-media sentiment analyzer; negative if compound score < -0.05. |
| MVL | Minimum Viable Lexicon: Filipino/Taglish extension; 57 runtime entries = 56 affective + 1 neutral (`buhay`, 0.0). Built from 53 instrument terms rated by one Filipino-language expert (53/53 exact concordance) plus 4 common/formal `naka-` spelling counterparts. |
| OOV ratio | Unrecognized tokens divided by valid tokens; >=50% triggers sentiment-unreliable handling. |
| VLM fallback | On-device Moondream 0.5B route for no-text items; constrained five-label classification. |
| Fuzzy logic | Interpretable graded rule system using Low/Medium/High memberships and rules instead of hard binary classification. |
| CoG | Center of Gravity defuzzification using output centers 16.67, 50.00, 83.33. |
| JITAI | Just-in-time adaptive intervention; supports timely, low-burden, personalized prompts. |
| ISO/IEC 25010 | Software quality model used for Functional Suitability, Performance Efficiency, Usability, Reliability. |
| TAM | Technology Acceptance Model; Perceived Usefulness and Perceived Ease of Use. |
| SUS | System Usability Scale; study target >=70. |
| SME | Subject Matter Expert; one mobile/software expert and one digital well-being/behavioral psychology expert. |

## 9. Formulas And Thresholds

Negative Sentiment Density:

```text
NSD = (negative resolvable units / resolvable units in session) x 100
NSD = [sum I(y_i = 1) / N_R] x 100
```

VADER compound normalization:

```text
C = x / sqrt(x^2 + alpha), alpha = 15
Negative text unit if C < -0.05
```

Triangular membership:

```text
mu_A(x) = 0                    if x <= a
mu_A(x) = (x - a) / (b - a)    if a < x < b
mu_A(x) = (c - x) / (c - b)    if b <= x < c
mu_A(x) = 0                    if x >= c
```

Fixed analytic fuzzy priors:

| Variable | Low | Medium | High |
|---|---:|---:|---:|
| Video dwell time | tri(0,0,5) | tri(4,12,20) | tri(15,30,30) |
| NSD | tri(0,0,33) | tri(17,50,83) | tri(67,100,100) |
| Session duration | tri(0,0,10) | tri(8,15,20) | tri(15,40,40) |

Rule activation and defuzzification:

```text
w_i = min(mu_Dwell(x), mu_NSD(y), mu_Duration(z))
RiskScore = sum(w_i * c_i) / sum(w_i)

Output centers:
Safe = 16.67
Warning = 50.00
Critical = 83.33
```

Risk bands and prompt mapping:

| Score | Band/prompt |
|---:|---|
| 0.00 to 33.32 | Safe; no prompt |
| 33.33 to 49.99 | Warning lower half; Level 1 awareness |
| 50.00 to 66.66 | Warning upper half; Level 2 pause |
| 66.67 to 100.00 | Critical; Level 3 pause-and-reset/breathing, disabled in fallback |

DSI:

```text
DSI_w = (1 / m_w) * sum RiskScore_s

m_w = number of sentiment-reliable sessions in week w
```

Week 2 live prompt personalization:

```text
If participant has >=10 reliable Week 1 sessions:

Low_X    = tri(0, 0, Q50)
Medium_X = tri(Q25, Q50, Q75)
High_X   = tri(Q50, Q75, Q95)

X = Session Duration or NSD
Dwell remains fixed.
Analytic DSI always uses fixed priors.
```

SUS scoring:

```text
Odd items: response - 1
Even items: 5 - response
SUS total = sum(adjusted item scores) x 2.5
Target = >=70
```

Cronbach alpha:

```text
alpha = k/(k-1) * [1 - sum(item variances) / total score variance]
Target = >=0.70
```

Core timing thresholds:

| Threshold | Use |
|---:|---|
| 30 seconds | Brief target-app exit bridge; longer exit ends session. |
| 45 seconds | Idle dwell cap; unattended looping should not inflate active dwell. |
| 15 minutes | Live prompt gate and prompt cooldown. |
| 60 seconds | Prompt-eligible risk must persist before prompt. |
| 5 resolved units | NSD must have at least this evidence count before driving live prompts. |
| OOV >=50% | Sentiment-unreliable majority-representativeness screen. |
| >=3 reliable Week 1 sessions | Inclusion floor for RQ3 Week 1 DSI association. |
| >=10 reliable Week 1 sessions | Live prompt personalization threshold. |

## 10. Rule Bases To Memorize

Full fuzzy engine:

- 3 variables x 3 levels = 27 rules.
- Inputs: video dwell time, NSD, session duration.
- Safe rules: 1, 2, 4, 10, 19.
- Critical rules: 9, 15, 17, 18, 23, 24, 26, 27.
- All other rules are Warning.
- Main defense logic: duration and NSD are the main risk axes; dwell is an intensifier, not a standalone doomscrolling trigger.

Representative full rules:

| Rule | Dwell | NSD | Duration | Risk |
|---:|---|---|---|---|
| 1 | Low | Low | Low | Safe |
| 9 | Low | High | High | Critical |
| 14 | Medium | Medium | Medium | Warning |
| 19 | High | Low | Low | Safe |
| 25 | High | High | Low | Warning |
| 26 | High | High | Medium | Critical |
| 27 | High | High | High | Critical |

Important rule defenses:

- High dwell alone is not doomscrolling.
- Duration alone is not Critical when NSD is low.
- Low dwell does not rescue a long session dominated by negative content; rapid chaining of negative items can still be Critical.
- Rule 25 is Warning after psychology review because a short session can contain heavy content without yet showing prolonged/compulsive scrolling.

Fallback engine:

- 2 variables x 3 levels = 9 rules.
- Inputs: dwell and duration only.
- Used when sentiment is unreliable.
- Critical only when dwell High + duration Medium/High.
- Level 3 prompt is disabled in fallback.

Fallback 9 rules:

| Rule | Dwell | Duration | Risk |
|---:|---|---|---|
| 1 | Low | Low | Safe |
| 2 | Low | Medium | Safe |
| 3 | Low | High | Warning |
| 4 | Medium | Low | Safe |
| 5 | Medium | Medium | Warning |
| 6 | Medium | High | Warning |
| 7 | High | Low | Warning |
| 8 | High | Medium | Critical |
| 9 | High | High | Critical |

## 11. Results To Memorize

Participant profile:

| Item | Value |
|---|---:|
| Participants | 50 |
| Intervention | 25 |
| Control | 25 |
| Completed both weeks | 50 |
| Age | 18-29, mean 22.12 |
| Sex | 39 male, 11 female |
| Facebook observed | 50/50 |
| Instagram observed | 50/50 |
| TikTok observed | 49/50 |
| Estimated daily SFV use | 15-883 min, mean 392.70 min |

Session data:

| Item | Value |
|---|---:|
| Total sessions | 10,134 |
| Sentiment-reliable sessions | 8,817 (87.0%) |
| Sentiment-unreliable sessions | 1,317 (13.0%) |
| Intervention sessions | 4,820 |
| Control sessions | 5,314 |
| Instagram sessions | 3,433 |
| Facebook sessions | 3,389 |
| TikTok sessions | 3,312 |
| Week 1 sessions | 5,162 |
| Week 2 sessions | 4,972 |

Reliability events:

| Event | Count | Share |
|---|---:|---:|
| High OOV | 924 | 69.5% of reliability events |
| VLM unresolved | 241 | 18.1% |
| Extraction failure | 152 | 11.4% |
| Service lifecycle | 12 | 0.9% |
| Total reliability events | 1,329 | 100.0% |

Prompt events in Week 2 intervention:

| Event | Count | Share |
|---|---:|---:|
| TAKE_BREAK | 191 | 51.6% |
| SHOWN | 68 | 18.4% |
| SUPPRESSED | 49 | 13.2% |
| CONTINUE | 45 | 12.2% |
| DISMISSED | 17 | 4.6% |
| VIEW_DASHBOARD | 0 | 0.0% |

RQ2 primary between-group changes:

| Outcome | n I/C | Test | Statistic | p | Effect | 95% CI |
|---|---:|---|---:|---:|---:|---|
| Session duration | 25/25 | Welch t | t = -4.17 | <.001 | d = -1.18 | [-1.78, -0.58] |
| Video dwell time | 25/25 | Mann-Whitney U | U = 124.0 | <.001 | d = -1.33 | [-1.94, -0.72] |
| NSD | 23/25 | Mann-Whitney U | U = 68.0 | <.001 | d = -1.82 | [-2.50, -1.15] |
| Doomscrolling Scale | 25/25 | Mann-Whitney U | U = 105.0 | <.001 | d = -1.50 | [-2.13, -0.87] |

Mean changes:

| Outcome | Intervention change | Control change |
|---|---:|---:|
| Session duration | -64.81 min | +0.44 min |
| Video dwell time | -0.43 sec | +0.01 sec |
| NSD | -3.33 points | +0.20 points |
| Doomscrolling Scale | -1.28 | 0.00 |

RQ2 supplementary / within intervention:

| Outcome | Test | Statistic | p | Effect |
|---|---|---:|---:|---:|
| Mean sessions/day between groups | Mann-Whitney U | U = 153.5 | .002 | d = -0.82 |
| DSI between groups | Mann-Whitney U | U = 81.0 | <.001 | d = -1.84 |
| Session duration within intervention | Paired t | t = -4.41 | <.001 | dz = -0.88 |
| Dwell within intervention | Paired t | t = -5.45 | <.001 | dz = -1.09 |
| NSD within intervention | Wilcoxon | W = 16.0 | <.001 | dz = -1.28 |
| Doomscrolling Scale within intervention | Wilcoxon | W = 7.0 | <.001 | dz = -1.25 |

RQ3 baseline convergent association:

| Predictor vs Week 1 Doomscrolling Scale | N | Spearman rho | p | Pearson r | Interpretation |
|---|---:|---:|---:|---:|---|
| Week 1 mean daily duration | 48 | 0.92 | <.001 | 0.82 | Strongest rank association |
| Week 1 mean dwell time | 48 | 0.75 | <.001 | 0.82 | Strong positive association |
| Week 1 mean NSD | 48 | 0.76 | <.001 | 0.83 | Strong positive association |
| Week 1 DSI | 48 | 0.76 | <.001 | 0.82 | Strong convergent association |

RQ4 user evaluation:

| Construct | n | Items | Mean | SD | Alpha | Target |
|---|---:|---:|---:|---:|---:|---|
| Functional Suitability | 50 | 5 | 3.93 | 0.50 | .852 | Met |
| Performance Efficiency | 50 | 5 | 4.09 | 0.55 | .859 | Met |
| Reliability | 50 | 5 | 4.05 | 0.50 | .717 | Met |
| SUS Usability | 50 | 10 | 80.95 | 14.83 | .957 | Met |
| TAM Perceived Usefulness | 50 | 6 | 4.12 | 0.40 | .750 | Met |
| TAM Perceived Ease of Use | 50 | 6 | 4.09 | 0.53 | .855 | Met |

Open-ended feedback themes:

| Theme | Count | Share |
|---|---:|---:|
| Prompt timing | 8 | 16% |
| Onboarding | 6 | 12% |
| Score clarity | 6 | 12% |
| Platform monitoring | 5 | 10% |
| Dashboard | 5 | 10% |
| Breathing break | 4 | 8% |
| Export/study data | 4 | 8% |
| Privacy/trust | 4 | 8% |
| Performance/battery | 3 | 6% |
| No major issue | 5 | 10% |

RQ5 SME evaluation:

| SME | Expertise | Overall mean | Defense interpretation |
|---|---|---:|---|
| SME-MOB-01 | Software/mobile application development | 5.00 | Fully favorable technical appraisal. |
| SME-PSY-01 | Digital well-being / behavioral psychology | 4.33 | Favorable non-clinical and intervention appraisal, with wording/configuration recommendations. |

## 12. Statistical Logic

Plain-language test guide (what each test is, and why this study used it):

| Term | Plain meaning | Why used here |
|---|---|---|
| p-value | Probability of seeing a difference this large if there were truly no difference. Small p = unlikely to be chance. | Decision aid at alpha = .05; never reported alone. |
| Change score | Week 2 value minus Week 1 value per participant. | Adjusts for individual baselines and chance baseline imbalance. |
| Welch's t-test | Compares two group means without assuming equal variances. | Session duration: change scores normal but variances unequal. |
| Mann-Whitney U | Compares two groups by ranks instead of raw values; no normality assumption. | Dwell, NSD, Doomscrolling Scale: change scores non-normal. |
| Paired t-test | Compares the same people at two time points (mean of differences). | Within-intervention Week 1 vs Week 2, when differences normal. |
| Wilcoxon signed-rank | Rank-based version of the paired t-test. | Within-intervention comparisons with non-normal differences. |
| Shapiro-Wilk | Checks whether data look normally distributed. | Gatekeeper that decided t-test vs rank test. |
| Levene's test | Checks whether two groups have equal variances. | Gatekeeper that decided Student vs Welch t-test. |
| Holm-Bonferroni | Correction that keeps the overall false-positive rate at 5% across multiple tests. | Applied across the four primary RQ2 outcomes. |
| Cohen's d / dz | Effect size: difference in standard-deviation units (d between groups, dz within subjects). Rough guide: 0.2 small, 0.5 medium, 0.8 large. | All primary effects exceeded 1.0 = large, but pilot-sized sample. |
| 95% CI | Range of plausible true effect sizes given the data. | Reported with every primary effect so size, not just p, drives interpretation. |
| Spearman's rho | Correlation of ranks; robust to non-normality and outliers. | Primary RQ3 statistic (assumptions not met for Pearson). |
| Pearson's r | Correlation of raw values; assumes linearity/normality. | Reported alongside rho for completeness. |
| Cronbach's alpha | Internal consistency: do items in a scale move together (0-1; >=0.70 acceptable). | Reliability check for each survey subscale. |
| Permuted-block allocation | Randomization in small shuffled blocks (sizes 2 and 4) so groups stay balanced and the next assignment is hard to predict. | How the 25/25 split was assigned, concealed until eligibility confirmed. |
| Purposive-convenience sampling | Recruiting reachable people who meet criteria, not a random population sample. | Honest scope limit; no prevalence claims. |
| Hawthorne effect | People change behavior because they know they are observed. | Acknowledged limitation for both arms. |

RQ2:

- Change score = Week 2 minus Week 1.
- Between-group tests compare intervention change vs control change.
- Welch t-test used where normality held but variance was unequal.
- Mann-Whitney U used where non-normality made rank test more appropriate.
- Within-intervention tests used paired t-test or Wilcoxon signed-rank depending on assumptions.
- Holm-Bonferroni correction controlled family-wise Type I error across four primary outcomes.
- Alpha = .05.
- Effect sizes reported so interpretation does not rely only on p-values.

RQ3:

- Week 1 only.
- Spearman rho is primary when assumptions are not normal/linear enough.
- Pearson r is also reported.
- Eligible N = 48 because two intervention participants lacked enough reliable Week 1 sessions.
- Interpretation: convergent plausibility, not validation, not ground-truth accuracy.

RQ4:

- ISO/TAM favorable target = >=3.50 out of 5.
- SUS target = >=70.
- Cronbach alpha target = >=0.70.
- Alpha is internal-consistency evidence, not proof that researcher-developed items are externally validated.

RQ5:

- SME favorable target = >=4.00 out of 5.
- Two SMEs means descriptive reporting, not inferential expert-panel statistics.

## 13. Algorithmic Complexity

| Component | Time complexity | What drives cost |
|---|---|---|
| VADER scoring per item | O(n) | Number of extracted tokens; dictionary lookup average O(1). |
| Per-item routing | O(1) | Constant check for usable text or no-text route. |
| VLM fallback per no-text item | O(C_VLM) | Fixed-size Moondream inference; dominant constant cost. |
| Fuzzy inference per session | O(R x V) = O(1) | R and V are fixed: 27 rules/3 variables or 9 rules/2 variables. |
| NSD aggregation per session | O(I) | Number of resolved items in session. |
| Week-level DSI | O(S) | Number of reliable sessions in the week. |
| Week 1 personalization | O(S log S) | Sorting baseline sessions for quantiles. |
| Worst-case session | O(sum n_i) + O(I x C_VLM) + O(1) | Text tokens plus no-text VLM calls plus fixed fuzzy inference. |

Defense line: the app is efficient because most scoring is linear or constant-time; the expensive operation is VLM inference, which is only used for no-text items instead of every item.

## 14. Tables From Chapters 4, 5, And 6

Use this section when a panelist points to a table and asks, "What does this mean?" Chapter 4 contains the formal tables. Chapters 5 and 6 are prose chapters in the current manuscript, so they do not contain formal `#thesis_table` blocks; their key findings and recommendations are summarized after the Chapter 4 table guide.

### Chapter 4 Formal Tables

| Table | What it contains | What it says | Defense line |
|---|---|---|---|
| Data Sources and Analysis Mapping | Maps each research area to workbook/source files and analysis type. | The chapter is traceable to specific sources: baseline profile, sessions/data quality, RQ2 tests, RQ3 correlations, survey analysis, and SME files. | "This table shows that each result comes from a named analysis source, not from unsupported interpretation." |
| Sample Disposition and Effective Sample Sizes | Participants retained, RQ3 eligibility, complete outcomes, and NSD/DSI effective n. | All 50 completed both weeks; sentiment-dependent analyses used 48 because two intervention participants lacked enough reliable Week 1 sessions. | "The study had full participant retention, but it reports reduced effective n where sentiment reliability required it." |
| Respondent Baseline Profile Summary | Age, sex, platform exposure, and estimated daily SFV use. | Participants were adult Filipino Android users, mostly young adults; all used Facebook/Instagram Reels and almost all used TikTok. | "This supports sample suitability for a short-form-video pilot, but not population generalizability." |
| Data Quality and Session Reliability Summary | Reliable/unreliable session counts and reliability-event causes. | 10,134 sessions were logged; 8,817 were sentiment-reliable and 1,317 were sentiment-unreliable; high OOV was the main reliability driver. | "The system was transparent about unusable sentiment cases instead of hiding them." |
| Prompt-Response Summary During Week 2 Intervention | Logged prompt actions in the intervention arm. | TAKE_BREAK was the most frequent logged prompt response; prompt events are descriptive because there was no Week 1 prompt baseline. | "This describes how users interacted with prompts; it is not treated as a separate outcome." |
| RQ2 Primary Behavioral Comparisons Between Groups | Four primary Week 1-to-Week 2 between-group change-score tests. | Session duration, dwell, NSD, and Doomscrolling Scale all favored intervention with p < .001 and large effects after Holm correction. | "This is the strongest short-term outcome table, but it supports pilot differences, not long-term efficacy." |
| RQ2 Supplementary and Within-Intervention Comparisons | Sessions/day, DSI, and within-intervention paired comparisons. | Supplementary outcomes moved in the same direction; intervention participants improved from Week 1 to Week 2. | "This strengthens the pattern, but the supplementary rows are exploratory." |
| RQ3 Baseline Convergent Association with Week 1 Doomscrolling Scale | Spearman/Pearson associations between self-report and duration, dwell, NSD, DSI. | DSI correlated strongly with self-report (rho = 0.76), but duration had the highest rho (0.92). | "DSI has convergent plausibility, but the study does not claim DSI superiority over duration." |
| ISO/IEC 25010, SUS, TAM, and Reliability Summary | User evaluation means, SDs, item counts, Cronbach alpha, and targets. | All targets were met; SUS was 80.95 and alpha ranged from .717 to .957. | "Users rated the system favorably, and the survey scales showed acceptable internal consistency in this sample." |
| Participant Open-Ended Feedback Theme Summary | Coded feedback themes, counts, percentages, and implications. | Main issues were prompt timing, onboarding, score clarity, platform monitoring, and dashboard/breathing/export/privacy feedback. | "Users did not reject the concept; they asked for clearer setup, score explanations, and prompt controls." |
| Subject Matter Expert Rubric Results | Two SME ratings across technical soundness, ranges, rules, privacy, intervention, ISO quality. | Mobile expert overall = 5.00; behavioral psychology expert overall = 4.33; both met the favorable target. | "SMEs supported the design as plausible, but this is expert appraisal, not formal calibration." |
| Time Complexity Summary of Core Runtime Algorithms | Big-O for VADER, routing, VLM fallback, fuzzy inference, NSD, DSI, personalization. | Most components are linear or constant-time; VLM fallback is the dominant fixed-cost operation. | "The app is computationally bounded; the expensive path is used only for no-text items." |
| Summary of Findings by Research Question | RQ evidence, main finding, interpretation, and limitation. | RQ1-RQ5 were answered within pilot scope; every finding is paired with its limitation. | "This table is the defense map: evidence supports feasibility, acceptability, plausibility, and short-term differences only." |

### Chapter 5 Conclusion Guide

Chapter 5 has no formal tables in the current manuscript. It summarizes and interprets Chapter 4 in prose. Use this guide as the "what Chapter 5 says" table.

| Chapter 5 part | What it says | Defense line |
|---|---|---|
| Summary of findings for RQ1 | The Android system supported local risk estimation with 10,134 sessions, 87.0% reliable coverage, MVL concordance, VADER/MVL, VLM fallback, fuzzy inference, and fallback handling. | "The architecture worked as a bounded privacy-preserving prototype." |
| Summary of findings for RQ2 | The intervention group had larger Week 1-to-Week 2 reductions than control across duration, dwell, NSD, and Doomscrolling Scale; supplementary DSI/sessions-day followed the same direction. | "The intervention condition showed favorable short-term differences." |
| Summary of findings for RQ3 | Week 1 DSI had rho = 0.76 with self-report, but duration had rho = 0.92. | "Convergent plausibility, not superiority and not diagnostic validation." |
| Summary of findings for RQ4 | All software/user evaluation targets were met; SUS = 80.95; alpha values met >=0.70. | "Users evaluated the system favorably, but UX improvements remain." |
| Summary of findings for RQ5 | SMEs rated the system favorably: 5.00 and 4.33, with recommendations for clearer fallback/privacy wording and configurable prompts. | "Expert appraisal supports plausibility, but the panel was small." |
| Overall conclusion | REDU is a defensible pilot prototype integrating local logging, text-first sentiment, no-text fallback, fuzzy risk estimation, and prompts. | "The main contribution is integration in one privacy-preserving Android artifact." |
| Overall limitation | Evidence remains exploratory because of the two-week window, purposive-convenience sample, unreliable-session exclusions, and two-SME panel. | "The conclusion is deliberately proportional to the evidence." |

### Chapter 6 Recommendation Guide

Chapter 6 has no formal tables in the current manuscript. It is organized into recommendation sections. Use this guide to explain what the recommendations say and why they follow from the findings.

| Chapter 6 area | What it recommends | Why it matters in defense |
|---|---|---|
| System improvement - onboarding | Add guided Accessibility Service and permission setup. | Responds to onboarding/setup feedback and improves deployment consistency. |
| System improvement - score clarity | Explain RiskScore/activity score through simple component breakdowns. | Responds to score-clarity feedback and makes fuzzy output more transparent. |
| System improvement - prompt timing | Add snooze, quiet hours, adjustable sensitivity, and cooldown feedback. | Responds to the most frequent user feedback theme. |
| System improvement - breathing break | Shorten or make pause-and-reset duration configurable. | Responds to participant and psychology-expert feedback. |
| System improvement - dashboard/platform status | Show monitoring status for TikTok, Facebook Reels, and Instagram Reels. | Addresses platform-monitoring transparency and extraction reliability concerns. |
| System improvement - export preview | Show exported file names and confirm raw text/screenshots are not included. | Strengthens privacy trust and export transparency. |
| Future development - extraction reliability | Use reliability events to diagnose high-OOV, unresolved VLM, and extraction failures by platform. | Directly targets the 13.0% sentiment-unreliable coverage issue. |
| Future development - VLM fallback | Test prompt wording, model mapping, screenshot timing, and fallback thresholds. | Improves no-text coverage while preserving local processing. |
| Future development - softer terminology | Replace alarming UI labels with terms like "Activity Pattern Score" while preserving analytic mapping. | Reduces clinical-sounding language and aligns with SME advice. |
| Future development - privacy architecture | Keep local processing and aggregate storage unless a new ethics process justifies otherwise. | Protects the strongest design contribution. |
| Future researchers - larger/longer study | Use a larger, more diverse sample and longer deployment. | Needed before long-term efficacy or population-level claims. |
| Future researchers - more SME coverage | Add a data science, machine learning, or fuzzy-logic expert. | Strengthens review of membership functions, rules, fallback, and defuzzification. |
| Future researchers - DSI validation | Compare DSI against full instruments, EMA, external logs, or other validated measures. | Needed because DSI converged but did not outperform duration. |
| Future researchers - incremental validity | Test whether DSI adds value beyond duration alone. | Directly answers the strongest possible panel critique of the composite. |
| Future researchers - transparent coverage | Keep reporting effective n, excluded sessions, and platform-specific reliability. | Preserves methodological honesty. |
| Future implementation - non-clinical use | Deploy as a digital well-being support tool, not diagnosis or therapy. | Keeps claims within evidence and ethics. |
| Future implementation - consent/privacy | Explain Accessibility access, transient screenshots, stored data, export data, and deletion/withdrawal. | Addresses the biggest privacy concern. |
| Future implementation - compatibility checks | Check Android version, manufacturer background policy, and screenshot availability before deployment. | Reduces field failure risk. |
| Future implementation - support materials | Provide setup guide, troubleshooting guide, privacy FAQ, and export guide. | Reduces participant confusion and researcher support burden. |
| Future implementation - proportional claims | Keep claims to feasibility, user evaluation, expert appraisal, and short-term pilot differences. | Prevents overclaiming during defense and future publication. |

## 15. Citation Map

Use the citation keys exactly as they appear in the manuscript. The defense should explain three things: whether the source is *Local* or *Foreign/International*, the key takeaway, and exactly how it supports REDU.

Local = Philippine/Filipino context, Philippine datasets, Filipino participants, Philippine institutions, or Philippine public/policy sources. Foreign/International = non-Philippine empirical studies, reviews, standards, official platform documentation, or general technical sources.

### Doomscrolling, Self-Report, And Theory

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| sharma-2022 | Foreign/International | Doomscrolling is a measurable construct involving persistent negative feed consumption. | Main construct source; justifies the Doomscrolling Feedback Loop framing and self-report anchor. |
| satici-2023 | Foreign/International | Doomscrolling Scale scores relate to distress, social media use, and well-being, and support shorter scale use. | Justifies the 4-item short-form weekly self-report as a convergent anchor, not ground truth. |
| rodrigues-2022 | Foreign/International | Doomscrolling can be framed as compulsive information seeking with mental well-being implications. | Use to explain why doomscrolling is more than ordinary scrolling. |
| taskin-2024 | Foreign/International | Doomscrolling is linked with reduced mindfulness and secondary traumatic stress in well-being pathways. | Supports non-clinical digital mindfulness and pause-and-reset framing. |
| hawwa-2025 | Foreign/International | Doomscrolling can mediate links between social media addiction and anxiety among young adults. | Supports relevance to young adult users while avoiding diagnostic claims. |
| buchanan-2021 | Foreign/International | Negative scrolling produces different emotional consequences than positive/kindness scrolling. | Supports adding content valence/NSD instead of relying only on screen time. |

### Short-Form Video, Platform Engagement, And Attention

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| qin-2022 | Foreign/International | TikTok design and information/system quality can intensify flow and addictive use. | Supports short-form-video focus and sustained engagement variables. |
| zhang-tiktok-2023 | Foreign/International | TikTok is embedded in entertainment, information, and social routines. | Explains why short-form feeds can sustain repeated engagement. |
| zenone-2021 | Foreign/International | TikTok deserves public-health attention because recommendation systems can amplify exposure and use patterns. | Supports treating short-form feeds as a relevant well-being setting. |
| zannettou-2024 | Foreign/International | Donated traces can be used to analyze engagement with TikTok recommendation streams. | Supports the trace/logging approach as a methodological analogy. |
| chen-2024 | Foreign/International | VLOPs expose users to engagement-prolonging design patterns. | Supports why app/platform design can sustain scrolling beyond user choice alone. |
| rajeswari-2024 | Foreign/International | Heavy social-media scrolling is associated with sustained-attention concerns. | Supports attention to prolonged engagement and cadence. |
| gagalang-2021 | Local | Filipino learners' social media use can affect reading attitudes and competence. | Local attention-related support; do not treat as adult doomscrolling prevalence. |
| cardoso-2024 | Local | Internet use is discussed as contributing to declining attention span among Filipino students. | Local attention-risk support; keep as student-context evidence. |

### Existing Tools And Gap

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| apple-screen-time-2025 | Foreign/International | Mainstream tools emphasize summaries, limits, and screen-time controls. | Helps show the gap: existing tools are mostly time-based. |
| google-digital-wellbeing-2024 | Foreign/International | Android Digital Wellbeing provides time summaries and app controls. | Shows REDU differs by adding session-level local risk estimation. |
| tiktok-wellbeing-2024 | Foreign/International | TikTok provides well-being and break-related features. | Shows platform tools exist but are platform-controlled and not cross-platform/local composite estimators. |
| mosseri-2021 | Foreign/International | Instagram discusses safety and support features. | Supports the comparison with platform-native well-being tools. |
| rahmillah-2023 | Foreign/International | Apps to reduce maladaptive phone use often vary in design and evidence strength. | Supports the gap for an integrated, privacy-preserving, content-sensitive artifact. |

### JITAI, Digital Mindfulness, And Prompts

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| aggarwal-2024 | Foreign/International | Digital mindfulness means more intentional, self-aware technology use. | Supports wording prompts as reflection/self-monitoring, not therapy. |
| mitsea-2023 | Foreign/International | Digitally assisted mindfulness can support self-regulation skills. | Supports short breathing/pause prompt as wellness support only. |
| antezana-2022 | Foreign/International | Well-being app engagement improves when interventions are relevant and low-burden. | Supports brief, non-intrusive prompts and avoiding constant interruption. |
| roffarello-2021 | Foreign/International | Habitual smartphone use can be monitored and mitigated through real-time reminders. | Supports feasibility of local monitoring plus just-in-time reminders. |
| teepe-2021 | Foreign/International | JITAI mechanisms require attention to timing and receptivity. | Supports cooldowns and burden-aware prompt timing. |
| ismail-2022 | Foreign/International | JITAI can be designed for behavior interruption in another health-adjacent domain. | Analogy for adaptive, timely prompts; not direct doomscrolling proof. |
| mair-2022 | Foreign/International | Smartphone JITAI can be feasible and personalized in field contexts. | Supports mobile delivery and personalization as feasible. |
| yang-2023 | Foreign/International | JITAI can be feasible/acceptable for behavior-change support. | Supports acceptability of adaptive prompt logic. |
| wang-2023 | Foreign/International | Prompt dose matters; too many prompts can create burden. | Supports cooldown and suppressed prompt logic. |
| fiedler-2024 | Foreign/International | JITAI decision points should be empirically grounded and context-sensitive. | Supports cautious wording around study-defined timing thresholds. |
| hsu-2025 | Foreign/International | Personalization is a major JITAI direction, but decision rules need stronger evidence. | Supports Week 1 personalization while admitting it is exploratory. |
| van-genugten-2025 | Foreign/International | Mental-health JITAIs still need stronger evidence on timing and decision rules. | Supports conservative, non-clinical prompt framing. |
| terzimehic-2022 | Foreign/International | Regretful smartphone use becomes more salient in longer use sessions. | Supports sub-10-minute vs 10-20+ minute timing logic. |
| rixen-2023 | Foreign/International | Infinite scrolling can form loops, and users have reasons to break them. | Supports reflective interruption design. |
| meinhardt-2025 | Foreign/International | Infinite-scroll intervention timing can use a 15-minute trigger in related work. | Supports REDU's 15-minute gate as literature-bounded, not clinical. |
| ikegaya-2025 | Foreign/International | Personalized intervention criteria based on prior behavior can improve prompt relevance. | Supports Week 1-derived prompt personalization. |

### Timing, Measurement, And Repeated Observations

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| cho-2021 | Foreign/International | Feature-level smartphone logs distinguish very brief, intentional, and regretful use patterns. | Supports using session/dwell timing rather than total screen time only. |
| tian-2021 | Foreign/International | Many mobile app sessions are short; sustained sessions need separate treatment. | Supports sub-10-minute low-duration region and sustained-use thresholds. |
| muise-2024 | Foreign/International | Smartphone content exposure is easy to mismeasure and often happens in bursts. | Supports cautious dwell/session inference wording. |
| ahmed-2023 | Foreign/International | Smartphone streams can be reconstructed for behavioral inference using explainable features. | Supports session reconstruction and gap-handling analogy. |
| chen-2023 | Foreign/International | Smartphone sensors/screenshots can infer time-killing contexts. | Supports local sensing plus screenshot-based inference as an analogy. |
| yao-2021 | Foreign/International | Repeated digital measures need enough observations for stable aggregation. | Supports minimum-session rules for week-level DSI. |
| meyer-2022 | Foreign/International | Data sufficiency is a practical problem in digital/wearable measurement. | Supports caution about sparse reliable sessions. |
| ratitch-2023 | Foreign/International | Digital measures require reliability/validation methods before strong claims. | Supports "convergent plausibility, not validation." |
| buekers-2025 | Foreign/International | Digital assessment needs enough hours/days to stabilize estimates. | Supports week-level aggregation and future longer validation. |

### Fuzzy Logic, Memberships, And Sensitivity

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| vashishtha-2023 | Foreign/International | Fuzzy logic can support sentiment-related reasoning where boundaries are gradual. | Justifies graded Low/Medium/High risk estimation. |
| pickering-2025 | Foreign/International | Fuzzy rule-based models are interpretable compared with many black-box models. | Main defense for fuzzy logic over supervised opaque models. |
| porebski-2022 | Foreign/International | Membership-function choice affects explainability and reliability. | Supports explicitly documenting membership functions. |
| khairuddin-2021 | Foreign/International | Triangular/trapezoidal fuzzy memberships are common structured choices. | Supports simple triangular membership sets. |
| azam-2021 | Foreign/International | Generated triangular/trapezoidal memberships can be useful classification structures. | Supports using structured Low/Medium/High partitions. |
| casalino-2022 | Foreign/International | Strong fuzzy partitions support interpretable classifier behavior. | Supports overlap between adjacent fuzzy sets. |
| dogan-2021 | Foreign/International | Sensitivity analysis is used to test stability in fuzzy decision methods. | Supports the documented boundary-sensitivity protocol. |
| vinogradova-zinkevic-2023 | Foreign/International | Fuzzy methods can be compared through sensitivity analysis. | Supports future audit of membership parameters. |
| shahari-2024 | Foreign/International | Fuzzy similarity measures require sensitivity checks. | Supports reporting possible output instability. |
| shukla-2025 | Foreign/International | Membership functions can affect model outputs and should be tested. | Supports sensitivity analysis as future/replication work. |

### Sentiment, Code-Mixed Text, MVL, And Annotation

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| hutto-2014 | Foreign/International | VADER is a parsimonious rule-based model designed for social-media sentiment. | Main source for VADER and the compound < -0.05 negative threshold. |
| tho-2021 | Foreign/International | Lexicon methods remain relevant beside transformers in low-resource/code-mixed settings. | Supports lightweight VADER + MVL instead of heavy transformer deployment. |
| hussain-2025 | Foreign/International | Efficient transformer approaches exist for edge devices but still involve compute tradeoffs. | Use when asked why not transformers: future alternative, not necessary for pilot. |
| mohammed-2023 | Foreign/International | Lexicon-based sentiment can be built for low-resource languages. | Supports the MVL strategy for Taglish/Filipino gaps. |
| nazir-2026 | Foreign/International | Code-mixed low-resource sentiment remains challenging and underdeveloped. | Supports high-OOV screen and conservative reliability exclusions. |
| wijayanti-2021 | Foreign/International | Social-media sentiment lexicons need domain-specific valence tuning. | Supports expert-reviewed MVL values. |
| perera-2024 | Foreign/International | Code-mixed text has variation, OOV, and ambiguity problems. | Supports limitations around slang, spelling, and dialects. |
| hashmi-2024 | Foreign/International | Multilingual transformers can improve code-mixed sentiment but require heavier modeling. | Use as future work / alternative method. |
| khan-2025 | Foreign/International | Textual variation in social media complicates NLP systems. | Supports why unmatched variants are marked OOV instead of guessed. |
| pacol-2021 | Local | English-Filipino sentiment can be approached with a bilingual lexicon. | Local support that Filipino-facing lexicon sentiment is feasible. |
| co-2022 | Local | Filipino/English social-media text can be classified for sentiment/emotion in local contexts. | Local support for bilingual sentiment processing, not direct doomscrolling validation. |
| cruz-2022 | Local | VADER has been applied to Philippine public-concern sentiment analysis. | Local support for VADER-style analysis in Philippine text settings. |
| krusic-2024 | Foreign/International | Sentiment annotation is difficult and shaped by annotator experience. | Supports caution: MVL review is concordance, not full validation. |
| ayravainen-2025 | Foreign/International | Instructions and annotator experience affect sentiment agreement. | Supports why no inter-rater reliability is claimed with one expert. |

### VLM, Multimodal, Visual, And Edge AI

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| das-2023 | Foreign/International | Multimodal sentiment can use visual information when text is absent. | Supports the no-text VLM fallback concept. |
| cortinas-lorenzo-2024 | Foreign/International | Affective computing needs explainability and careful interpretation. | Supports conservative visual-label wording. |
| johnson-2025 | Foreign/International | Audio/visual affective computing still faces explainability limits. | Supports not claiming the VLM truly understands user emotion. |
| wei-2021 | Foreign/International | Key frames can support user-generated video emotion recognition. | Supports sparse one-frame inference analogy. |
| zhang-xu-2023 | Foreign/International | Frame-level adaptation can support video emotion recognition. | Supports using a frame-level cue for no-text items. |
| sharma-2023 | Foreign/International | Sparse multimodal fusion can be used in emotional-health detection settings. | Supports limited sampling, while keeping it as analogy only. |
| augusma-2023 | Foreign/International | Privacy-compliant visual features can support emotion recognition without full video retention. | Supports RAM-only, no persistent screenshot storage. |
| qu-2025 | Foreign/International | VLMs face sampling tradeoffs in long videos. | Supports not doing continuous video analysis. |
| sharshar-2025 | Foreign/International | VLMs can be studied for edge-network deployment. | Supports feasibility of on-device/edge VLM route. |
| lee-2024 | Foreign/International | Mobile/edge vision models require compact and efficient design. | Supports choosing Moondream 0.5B as a deployment-fit model. |

### Privacy, Accessibility, Edge, And Architecture

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| lee-2022 | Foreign/International | Android APIs can expose rich smartphone analytics but create privacy sensitivity. | Supports explicit consent, local processing, and aggregate export. |
| swathi-2025 | Foreign/International | Edge-centric monitoring supports responsiveness and privacy. | Supports on-device processing rather than cloud inference. |
| tewari-2023 | Foreign/International | mHealth systems should build privacy protections into the design. | Supports privacy-by-design and no raw-content export. |
| laudon-2022 | Foreign/International | Information systems can be represented through input-process-output structure. | Supports the IPO architecture explanation. |

### Evaluation, Research Design, Standards

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| creswell-2022 | Foreign/International | Mixed/quantitative research designs can combine structured measures and supplementary qualitative feedback. | Supports design-and-development plus pilot field evaluation structure. |
| iso-25010-2023 | Foreign/International | ISO/IEC 25010 defines software product quality characteristics. | Supports Functional Suitability, Performance Efficiency, Usability, and Reliability evaluation. |
| adnan-2025 | Foreign/International | TAM remains useful for assessing mHealth app acceptability. | Supports measuring PU and PEOU. |
| hyzy-2022 | Foreign/International | SUS has benchmark evidence for digital health apps. | Supports the SUS target of >=70 and interpretation of 80.95. |

### Philippine / Local Relevance

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| panaligan-2021 | Local | Doomscrolling had entered Philippine public discourse. | Establishes local relevance, not empirical prevalence. |
| lanuza-2021 | Local | Some digital platforms and online behaviors in the Philippines remain understudied. | Supports the local research gap. |
| punzalan-2024 | Local | Filipino students describe lived experiences of being lost in feeds/doomscrolling. | Closest local doomscrolling support; student-centered, so keep adult claims cautious. |
| bautista-2024 | Local | Boredom proneness and social media use are linked among Filipino college students. | Supports local self-regulation/scrolling concern. |
| canila-2023 | Local | Filipino student TikTok consumption is perceived to affect attention span. | Supports TikTok/attention relevance locally; not direct adult field evidence. |
| ababat-2024 | Local | Excessive internet use is reported among Filipino senior high school students. | Local excessive-use context; not adult doomscrolling proof. |
| cleofas-2022 | Local | Problematic social-media use relates to mental health among Filipino undergraduates. | Stronger local empirical support for problematic-use concern. |
| zamora-2021 | Local | Social-media exposure relates to anxiety/depression among Filipino seafarers. | Provides adult Filipino context, but not short-form doomscrolling specifically. |
| castillo-2022 | Local | Filipino older adults used social media for connection during COVID-19. | Shows social media has adult Filipino well-being context, including beneficial use. |
| lim-2025 | Local | A two-week digital detox showed cognitive/social effects among Filipino adolescents. | Supports plausibility of a two-week window; not adult REDU efficacy proof. |

### Contextual Or Orphan/Peripheral Bibliography Entries

These keys appear in the bibliography or were flagged as not central in the current chapters. Do not lean on them unless a panel asks about broader context.

| Key | Local/Foreign | Key takeaway | Use in defense |
|---|---|---|---|
| eva-2025 | Local | Philippine public/policy discussion around TikTok and children's digital habits. | Peripheral background only; REDU does not study minors. |
| icamina-2025 | Local | Philippine public article on TikTok and youth brain concerns. | Peripheral background only; avoid using as primary evidence. |
| mangaluz-2025 | Local | Philippine policy discussion on social-media restriction for minors. | Peripheral public-policy context; REDU targets adults. |
| quijano-2026 | Local | Philippine public article on digital detox. | Peripheral public-discourse context. |
| sutrisno-2025 | Foreign/International | Campaign/industry discussion on rethinking social media for well-being. | Peripheral context only; not empirical support. |
| lokeshkumar-2021 | Foreign/International | Social-media data can be used for mental-state prediction with machine learning. | Methodological analogy only; not REDU validation. |

## 16. Weaknesses And Defense Answers

| Weakness | Honest defense |
|---|---|
| Not clinical or diagnostic | Correct. The system estimates doomscrolling-related risk from proxies and gives wellness prompts; it does not measure mental health outcomes. |
| Two-week pilot | Appropriate for feasibility and short-term observed differences; long-term efficacy requires longer deployment. |
| N = 50 purposive-convenience sample | Acceptable for pilot software evaluation; not enough for population calibration or prevalence claims. |
| Mostly young adults and 39/11 male-female split | Limits generalizability; results are scoped to adult Filipino Android users in this pilot. |
| DSI did not beat duration | Admitted. DSI converged strongly with self-report, but duration had the highest rank association; future work should test incremental validity. |
| 13% sentiment-unreliable sessions | Transparent reliability screen; better to exclude unreliable sentiment than overclaim noisy NSD. |
| NSD/DSI n = 23/25 in intervention for RQ2 | Coverage limitation due to reliability criteria; report exact n and keep interpretation cautious. |
| VADER limitations | VADER can miss sarcasm, slang, dialects, ambiguity; MVL improves recurring Taglish terms but is not complete Filipino sentiment understanding. |
| MVL single-expert review | Defended as concordance, not inter-rater reliability or full lexicon validation. |
| VLM fallback limitations | One local frame and five labels; improves no-text coverage but is not full video understanding or affective truth. |
| Accessibility API fragility | Known tradeoff for no-root, user-consented local monitoring; mitigated through adapters, reliability logs, fallback, and exclusions. |
| No precision/recall/F1 | No session-level ground truth; labeling active scrolling would be intrusive and behavior-changing. Convergent association is the correct pilot plausibility check. |
| Two SMEs only | Useful expert plausibility appraisal but not formal calibration; future work should add data science/fuzzy expert and larger SME panel. |
| Hawthorne/novelty effects | Possible; two-group baseline-intervention design helps, but longer and more blinded/delayed-treatment designs are needed. |
| Ethics and privacy concerns | Consent, local processing, study-code export, no raw content retention, withdrawal/delete option. Avoid saying anonymous; say study-code-linked. |
| Title/word "detection" | Define detection as computational risk-state estimation from observable proxies, not clinical diagnosis. |

## 17. Likely Panel Questions

| Question | Short answer |
|---|---|
| What is your main contribution? | A bounded Android artifact that integrates local behavioral logging, VADER + Filipino/Taglish MVL, on-device no-text VLM fallback, fuzzy risk estimation, adaptive prompts, and aggregate export for short-form doomscrolling-related risk estimation. |
| Why not just screen time? | Doomscrolling is not only time. Literature and theory suggest exposure quality matters, so REDU adds negative exposure and dwell/context. |
| Why did duration correlate higher than DSI? | Self-report may be heavily time-based in this pilot. DSI still converged strongly but needs future incremental-validity testing. |
| Why fuzzy logic? | No labeled ground-truth sessions, gradual construct, low compute, and interpretable rules that SMEs can inspect. |
| Why not train a supervised model? | The study lacks session-level labels and prioritizes privacy, explainability, and deployability over black-box prediction. |
| Why VADER? | Lightweight, reproducible, on-device, and suited for social-media text; MVL addresses recurring Filipino/Taglish gaps. |
| Why not transformers? | More compute/data demand and harder local deployment; useful future work, but not necessary for this bounded prototype. |
| Why one Filipino expert for MVL? | Scope limitation. It supports single-expert concordance, not full inter-rater lexicon validation. |
| Why VLM screenshots if privacy is central? | Screenshots are transient RAM-only inputs to local Moondream, not stored/exported or sent to cloud. |
| What if VLM fails? | The item/session becomes unresolved; reliability event is logged; the engine falls back to dwell + duration; Level 3 disabled. |
| Why OOV >=50%? | Majority-representativeness rule: when unresolved tokens are at least half, recognized text no longer represents most lexical content. It is not a linguistic universal. |
| Why 15 minutes? | It is a burden-aware prototype gate supported by infinite-scroll/JITAI timing literature, not a clinical cutoff. |
| Why 30 seconds for session bridging? | It merges only very brief app switches and is stricter than some smartphone reconstruction windows around 45 seconds. |
| Why 45 seconds idle cap? | Prevents unattended looping from inflating active dwell when no micro-interactions occur. |
| Why exclude unreliable sessions? | NSD/DSI require sentiment reliability. Keeping unreliable sentiment would be less honest. |
| Can REDU prevent doomscrolling? | No. It supports self-monitoring and reflective interruption; prevention/long-term behavior change needs longer studies. |
| Is REDU spying? | No. User installs it, enables Accessibility explicitly, processing is local, export is aggregate/study-code-linked, and raw captions/screenshots are not exported. |
| Why adult Filipino Android users only? | Android Accessibility and local Filipino/Taglish context are the target scope; no claims for minors, iOS, or other populations. |
| Why no AUROC/F1? | No ground-truth class labels for sessions; those metrics would imply a supervised validation the study did not collect. |
| Did you measure actual CPU, RAM, or battery use? | No objective device profiling was collected. Performance Efficiency was evaluated through user survey responses and SME review, supported by the algorithmic complexity analysis. Objective profiling is future work. |
| What does REDU stand for? | It is the app's project name, not an acronym. The manuscript refers to the system as the Heuristic Risk-State Estimation System. |
| When exactly did the field study run? | Week 1 baseline: May 25-31, 2026. Week 2 deployment: June 1-7, 2026. Post-usage survey and export followed from June 7. |
| What is the biggest limitation? | Pilot evidence: two weeks, N = 50, purposive-convenience sample, 13% sentiment-unreliable sessions, and limited expert panel. |
| What is the strongest result? | All primary RQ2 outcomes favored intervention, all RQ4 user-evaluation targets were met, and system logging achieved 87.0% reliable coverage. |
| What is the strongest technical feature? | Privacy-preserving local integration of behavioral proxies, sentiment proxy, no-text fallback, fuzzy inference, and adaptive prompting. |

## 18. Peer Comparison / Manuscript Quality

Based on the local peer-manuscript critique in `/Users/geanm/Documents/Codex/2026-05-16/papers of other groups`, the common high-risk gaps among peers were missing Cronbach alpha, missing algorithm complexity, incomplete Chapter 5 recommendations, weak RQ alignment, and limited discussion of limitations.

REDU is strong against peers because:

- It has complete Chapters 1-5 plus Chapter 6/recommendations material.
- Chapter 4 is organized by research questions and evidence sources.
- It includes Cronbach alpha for user-evaluation constructs.
- It includes explicit algorithmic complexity with Big-O.
- It reports sample sizes, effect sizes, confidence intervals, and correction logic.
- It reports operational reliability events instead of hiding failed sentiment cases.
- It explicitly states limitations and avoids clinical overclaiming.
- Chapter 5 conclusions are aligned with the actual RQs and evidence boundaries.

REDU is most comparable to the stronger peer manuscripts, such as those with clear results interpretation and limitation discussion, but it is ahead of most reviewed peers on reliability analysis and complexity analysis. The main defense risk is not manuscript completeness; it is wording discipline. If the researchers overclaim "detection accuracy," "clinical validation," or "prevention," the panel can attack. If the researchers keep the pilot/non-clinical framing, the manuscript is defense-ready and above-average in methodological transparency.

## 19. Remaining Technical Notes From App Review

Fixed items to know if asked:

- Release build CMake issue fixed.
- API 33 receiver flag issue fixed with `ContextCompat.registerReceiver`.
- Prompt risk must persist 60 seconds before showing.
- NSD drives live prompts only when resolvable units >=5.
- Prompt outcomes now adapt policy: repeated disregards can escalate Level 1 to Level 2; break resets streak.
- L2 continue unlocks after 3 seconds.
- Trigger reason is persisted/exported.
- Overlay dismissal on service teardown fixed.
- VLM native failure returns unresolved instead of crashing.
- Export filename reuse fixed.
- Export now includes `resolvable_units` and `negative_units`.
- Daily summary/export timezone fixed to Asia/Manila.
- ARM64-only ABI is intentional for field devices but limits emulator/ChromeOS coverage.

Be honest about what still needed live confirmation:

- Connected ARM64 device validation for runtime behaviors.
- Release app-exit detection confirmation on device.
- VLM warm-up timing confirmation on device.
- Social-app extraction behavior can drift as TikTok/Facebook/Instagram update their UI.

## 20. One-Minute Closing Answer

The thesis should be defended as a software-engineering and pilot field-evaluation contribution. The evidence supports that REDU can run locally, collect analyzable short-form-video usage logs, estimate risk through transparent behavioral and sentiment-related proxies, degrade conservatively when sentiment is unreliable, and receive favorable user and SME evaluation. The Week 1-to-Week 2 results are promising, but they are short-term pilot findings, not proof of long-term efficacy or clinical validity. The next step is larger, longer, more diverse validation with stronger sentiment annotation, platform-specific reliability reporting, additional SMEs, and incremental-validity testing of DSI beyond duration alone.

## 21. Last-Minute Memorization List

- N = 50, 25 intervention, 25 control.
- Two weeks: Week 1 prompts off, Week 2 prompts on only for intervention.
- 10,134 sessions total.
- 8,817 reliable, 1,317 unreliable, 87.0% reliable.
- Main variables: duration, dwell, NSD.
- DSI = weekly mean fixed-prior RiskScore over reliable sessions.
- VADER negative threshold = compound < -0.05.
- OOV high threshold = >=50%.
- Risk bands: Safe <33.33, Warning 33.33-66.66, Critical >=66.67.
- Prompt gate/cooldown = 15 minutes.
- Sustained risk = 60 seconds.
- Minimum NSD evidence for live prompt = 5 resolved units.
- RQ2 all four primary outcomes p < .001 favor intervention.
- RQ3 DSI rho = 0.76, duration rho = 0.92.
- SUS = 80.95.
- Cronbach alpha range = .717 to .957.
- SMEs = 5.00 and 4.33.
- Biggest limitation = pilot/non-clinical/short-term/generalizability.
- Best defense phrase = "convergent plausibility, not diagnostic validation."
