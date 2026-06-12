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
| Timeline | Week 1 baseline logging with prompts off; Week 2 intervention prompts on only for intervention group. |
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

## 5. Timeline To Memorize

Development timeline:

| Stage | What happened |
|---|---|
| Sprint 1 | Requirements, planning, target platforms, privacy framing, data model. |
| Sprint 2 | Accessibility extraction, platform adapters, local logging, session/dwell tracking. |
| Sprint 3 | VADER + MVL, no-text VLM fallback, fuzzy risk engine, prompt policy. |
| Sprint 4 | Testing, bug fixes, export, deployment preparation, documentation. |

Field evaluation timeline:

| Phase | What happened |
|---|---|
| Phase 1 | Recruitment, screening, consent, compatibility check, 1:1 allocation. |
| Phase 2 | Baseline profile and app setup; participant enabled Accessibility Service. |
| Phase 3 | Week 1 baseline logging; prompts disabled for both groups. |
| Phase 4 | Week 2 deployment; intervention prompts enabled only for intervention group; control remained logging-only. |
| Phase 5 | Post-usage survey, SME review, aggregate log export, data analysis. |

## 6. System Data Flow

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

## 7. Core Terms

| Term | Meaning in defense |
|---|---|
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
| MVL | Minimum Viable Lexicon: Filipino/Taglish extension; 57 runtime entries, single-expert concordance. |
| OOV ratio | Unrecognized tokens divided by valid tokens; >=50% triggers sentiment-unreliable handling. |
| VLM fallback | On-device Moondream 0.5B route for no-text items; constrained five-label classification. |
| Fuzzy logic | Interpretable graded rule system using Low/Medium/High memberships and rules instead of hard binary classification. |
| CoG | Center of Gravity defuzzification using output centers 16.67, 50.00, 83.33. |
| JITAI | Just-in-time adaptive intervention; supports timely, low-burden, personalized prompts. |
| ISO/IEC 25010 | Software quality model used for Functional Suitability, Performance Efficiency, Usability, Reliability. |
| TAM | Technology Acceptance Model; Perceived Usefulness and Perceived Ease of Use. |
| SUS | System Usability Scale; study target >=70. |
| SME | Subject Matter Expert; one mobile/software expert and one digital well-being/behavioral psychology expert. |

## 8. Formulas And Thresholds

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

## 9. Rule Bases To Memorize

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

## 10. Results To Memorize

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

## 11. Statistical Logic

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

## 12. Algorithmic Complexity

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

## 13. Citation Map

Use the citation keys exactly as they appear in the manuscript. The defense should explain what each source contributes, not just name-drop it.

### Doomscrolling, Self-Report, And Theory

| Key | What it does |
|---|---|
| sharma-2022 | Core doomscrolling construct and Doomscrolling Scale; anchors feedback-loop model. |
| satici-2023 | Supports short-form Doomscrolling Scale use and psychometric link to distress/personality/social media use. |
| rodrigues-2022 | Frames doomscrolling as compulsive information seeking and mental well-being concern. |
| taskin-2024 | Links doomscrolling with mindfulness, secondary traumatic stress, and mental well-being. |
| hawwa-2025 | Links social media addiction, anxiety, and doomscrolling among young adults. |
| buchanan-2021 | Supports content-quality framing: negative scrolling differs from neutral/kindness scrolling, not just time. |

### Short-Form Video, Platform Engagement, And Attention

| Key | What it does |
|---|---|
| qin-2022 | TikTok information/system quality, flow, and addictive-use framing. |
| zhang-tiktok-2023 | TikTok integration into routines, entertainment, information, and social reference. |
| zenone-2021 | TikTok public-health research agenda and harmful exposure/addictive-pattern concerns. |
| zannettou-2024 | Trace/data-donation study of engagement with TikTok recommendations. |
| chen-2024 | Engagement-prolonging designs on very large online platforms. |
| rajeswari-2024 | Heavy social-media scrolling and sustained-attention concerns. |
| gagalang-2021 | Filipino learners' social-media use and reading/attention-related outcomes. |
| cardoso-2024 | Internet use and declining attention span among students. |

### Existing Tools And Gap

| Key | What it does |
|---|---|
| apple-screen-time-2025 | Official Screen Time feature context: summaries/timers. |
| google-digital-wellbeing-2024 | Official Android Digital Wellbeing feature context. |
| tiktok-wellbeing-2024 | Official TikTok well-being/break feature context. |
| mosseri-2021 | Instagram platform protection/well-being feature context. |
| rahmillah-2023 | Review of apps designed to reduce mobile-phone use; supports tool-gap comparison. |

### JITAI, Digital Mindfulness, And Prompts

| Key | What it does |
|---|---|
| aggarwal-2024 | Digital mindfulness concept for thoughtful technology use. |
| mitsea-2023 | Digitally assisted mindfulness and self-regulation support. |
| antezana-2022 | Well-being app engagement; supports low-burden, relevant interventions. |
| roffarello-2021 | Real-time smartphone habit monitoring and just-in-time reminders. |
| teepe-2021 | JITAI mechanisms in mobile apps; supports timing/receptivity concerns. |
| ismail-2022 | JITAI to reduce sedentary behavior; supports prompt timing and behavior-change analogy. |
| mair-2022 | Smartphone-delivered JITAI feasibility and personalization context. |
| yang-2023 | JITAI feasibility/acceptability in smoking cessation; supports adaptive prompt framing. |
| wang-2023 | Prompt dosing in mobile JITAI; supports burden-aware intervention dose. |
| fiedler-2024 | JITAI review; supports empirically grounded decision points. |
| hsu-2025 | Personalized JITAI review; supports adaptive decision rules and personalization. |
| van-genugten-2025 | JITAI in mental health; supports caution around underdeveloped decision rules. |
| terzimehic-2022 | Regretful smartphone use and session-length timing; supports 10-20+ min intervention reasoning. |
| rixen-2023 | Infinite scrolling behavior; supports 10+ minute long-session framing. |
| meinhardt-2025 | Infinite-scroll intervention after 15 minutes; supports live prompt gate analogy. |
| ikegaya-2025 | Personalized intervention criteria from baseline; supports Week 1-derived prompt personalization. |

### Timing, Measurement, And Repeated Observations

| Key | What it does |
|---|---|
| cho-2021 | Feature-level smartphone use and regret; supports short-duration distinctions and 30-sec-like handling. |
| tian-2021 | Mobile app engagement duration distribution; supports sub-10-minute baseline region. |
| muise-2024 | Smartphone content exposure often occurs in short bursts; supports dwell/session boundary caution. |
| ahmed-2023 | Smartphone stream/session reconstruction analogy; supports gap handling. |
| chen-2023 | Sensor/screenshot-based time-killing detection; supports local smartphone inference analogy. |
| yao-2021 | Repeated digital measures; supports minimum observations for aggregate estimates. |
| meyer-2022 | How much data is enough for wearable/digital measures; supports week-level reliability caution. |
| ratitch-2023 | Statistical methods for digital-measure validation; supports cautious validation language. |
| buekers-2025 | Digital assessment hours/days needed; supports repeated-observation sufficiency framing. |

### Fuzzy Logic, Memberships, And Sensitivity

| Key | What it does |
|---|---|
| vashishtha-2023 | Fuzzy logic for sentiment analysis; supports interpretable graded inference. |
| pickering-2025 | Interpretability of fuzzy rule-based models; supports transparent rules over black-box classifier. |
| porebski-2022 | Membership functions for explainable/reliable fuzzy classifiers. |
| khairuddin-2021 | Structured review of triangular/trapezoidal fuzzy membership functions. |
| azam-2021 | Generating triangular/trapezoidal membership functions; supports simple partitions. |
| casalino-2022 | Strong fuzzy partitions; supports partitioned Low/Medium/High setup. |
| dogan-2021 | Fuzzy AHP sensitivity-analysis analogy. |
| vinogradova-zinkevic-2023 | Sensitivity analysis of fuzzy methods. |
| shahari-2024 | Fuzzy similarity sensitivity analysis. |
| shukla-2025 | Membership-function sensitivity analysis. |

### Sentiment, Code-Mixed Text, MVL, And Annotation

| Key | What it does |
|---|---|
| hutto-2014 | VADER method and -0.05 negative threshold. |
| tho-2021 | Lexicon vs transformer sentiment for code-mixed low-resource language; supports lightweight choice. |
| hussain-2025 | Resource-constrained MobileBERT/DistilBERT edge sentiment context; supports compute tradeoff discussion. |
| mohammed-2023 | Lexicon-based sentiment for low-resource languages; supports MVL strategy. |
| nazir-2026 | Code-mixed low-resource sentiment review; supports challenges/future work. |
| wijayanti-2021 | Sentiment lexicon curation with valence tuning; supports targeted lexicon values. |
| perera-2024 | Code-mixed sentiment review; supports OOV and language-variation limitations. |
| hashmi-2024 | Code-mixed tweets with multilingual transformers; supports alternative methods and tradeoffs. |
| khan-2025 | Textual variation challenges in social-media text processing. |
| pacol-2021 | English-Filipino sentiment lexicon approach; supports Filipino sentiment feasibility. |
| co-2022 | Bilingual Filipino/English sentiment and emotion classification; supports local text-processing feasibility. |
| cruz-2022 | VADER application in local/public concern analysis; supports local VADER use. |
| krusic-2024 | Sentiment annotation challenges; supports caution with single-expert MVL. |
| ayravainen-2025 | Annotator agreement/instructions in sentiment annotation; supports no inter-rater-claim limitation. |

### VLM, Multimodal, Visual, And Edge AI

| Key | What it does |
|---|---|
| das-2023 | Multimodal sentiment analysis survey; supports visual channel relevance when text is absent. |
| cortinas-lorenzo-2024 | Explainable affective computing review; supports caution around visual affect inference. |
| johnson-2025 | Audio/visual affective computing XAI review; supports interpretability/limits. |
| wei-2021 | User-generated video emotion recognition with key frames; supports sparse visual inference analogy. |
| zhang-xu-2023 | Frame-level video emotion recognition; supports per-frame visual evidence. |
| sharma-2023 | Sparse multimodal emotional-health detection; supports limited sampling analogy. |
| augusma-2023 | Privacy-compliant group emotion recognition using limited visual features. |
| qu-2025 | VLM long-video sampling dilemma; supports sparse sampling under constraints. |
| sharshar-2025 | VLMs for edge networks; supports on-device/edge feasibility. |
| lee-2024 | Mobile/edge vision transformers; supports compact model choice. |

### Privacy, Accessibility, Edge, And Architecture

| Key | What it does |
|---|---|
| lee-2022 | Android API/accessibility data availability and privacy sensitivity. |
| swathi-2025 | Edge-centric monitoring for responsiveness/privacy; supports local processing. |
| tewari-2023 | Privacy-by-design in mHealth; supports raw-content non-export stance. |
| laudon-2022 | IPO model and information-system architecture framing. |

### Evaluation, Research Design, Standards

| Key | What it does |
|---|---|
| creswell-2022 | Research design basis for design-and-development / pilot field evaluation. |
| iso-25010-2023 | Official product-quality model for Functional Suitability, Performance Efficiency, Usability, Reliability. |
| adnan-2025 | TAM acceptability in mHealth apps; supports PU/PEOU evaluation. |
| hyzy-2022 | SUS benchmark for digital health apps; supports SUS target. |

### Philippine / Local Relevance

| Key | What it does |
|---|---|
| panaligan-2021 | Philippine public discourse on doomscrolling. |
| lanuza-2021 | Understudied digital platforms in the Philippines; supports local digital-platform context. |
| punzalan-2024 | Closest local doomscrolling study, student lived experiences. |
| bautista-2024 | Boredom proneness and social media among college students. |
| canila-2023 | TikTok consumption and attention span among Filipino students. |
| ababat-2024 | Excessive internet use among selected senior high school students. |
| cleofas-2022 | Problematic vs reflective social-media use and mental health among Filipino undergraduates. |
| zamora-2021 | Daily social-media exposure and anxiety/depression among Filipino seafarers; adult context. |
| castillo-2022 | Filipino older adults, social media, and social connectedness during COVID-19; adult context. |
| lim-2025 | Two-week digital detox among Filipino adolescents; supports plausibility of short observation window but not adult proof. |

### Contextual Or Orphan/Peripheral Bibliography Entries

These keys appear in the bibliography or were flagged as not central in the current chapters. Do not lean on them unless a panel asks about broader context.

| Key | What it does / status |
|---|---|
| eva-2025 | Public/policy context on TikTok and children; peripheral. |
| icamina-2025 | Public context on TikTok and youth brains; peripheral. |
| mangaluz-2025 | Public/policy context on social-media restrictions for minors; peripheral. |
| quijano-2026 | Digital detox public-context source; peripheral. |
| sutrisno-2025 | Campaign/public-context source about social media and well-being; peripheral. |
| lokeshkumar-2021 | Social-media data and mental-state prediction analogy; methodological background, not direct REDU validation. |

## 14. Weaknesses And Defense Answers

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

## 15. Likely Panel Questions

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
| What is the biggest limitation? | Pilot evidence: two weeks, N = 50, purposive-convenience sample, 13% sentiment-unreliable sessions, and limited expert panel. |
| What is the strongest result? | All primary RQ2 outcomes favored intervention, all RQ4 user-evaluation targets were met, and system logging achieved 87.0% reliable coverage. |
| What is the strongest technical feature? | Privacy-preserving local integration of behavioral proxies, sentiment proxy, no-text fallback, fuzzy inference, and adaptive prompting. |

## 16. Peer Comparison / Manuscript Quality

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

## 17. Remaining Technical Notes From App Review

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

## 18. One-Minute Closing Answer

The thesis should be defended as a software-engineering and pilot field-evaluation contribution. The evidence supports that REDU can run locally, collect analyzable short-form-video usage logs, estimate risk through transparent behavioral and sentiment-related proxies, degrade conservatively when sentiment is unreliable, and receive favorable user and SME evaluation. The Week 1-to-Week 2 results are promising, but they are short-term pilot findings, not proof of long-term efficacy or clinical validity. The next step is larger, longer, more diverse validation with stronger sentiment annotation, platform-specific reliability reporting, additional SMEs, and incremental-validity testing of DSI beyond duration alone.

## 19. Last-Minute Memorization List

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
