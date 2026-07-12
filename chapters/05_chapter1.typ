// ==========================================
// CHAPTER 1: INTRODUCTION
// ==========================================

#import "utils.typ": figure_arrow, figure_panel, figure_panel_items

= INTRODUCTION

Doomscrolling refers to the compulsive consumption of distressing or negative online content despite its adverse emotional and behavioral effects #cite(<sharma-2022>). Mainstream device- and platform-level digital well-being tools still emphasize screen-time summaries, app timers, or break reminders instead of session-level interpretation of fast-changing feed content #cite(<apple-screen-time-2025>) #cite(<google-digital-wellbeing-2024>) #cite(<tiktok-wellbeing-2024>) #cite(<mosseri-2021>) #cite(<rahmillah-2023>). This thesis addresses that gap through the design, development, and evaluation of the *Heuristic Risk-State Estimation System* — a privacy-preserving Android app that performs on-device risk estimation using behavioral signals, a text-first sentiment path, and an on-device Vision-Language Model (VLM) fallback for no-text items. Here, "detection" refers to computational risk estimation from observable proxies, not diagnostic classification.

The system combines threshold-based heuristics, interpretable fuzzy rule-based inference #cite(<vashishtha-2023>) #cite(<pickering-2025>), a VADER-compatible text-first sentiment pipeline for low-resource code-mixed text #cite(<hutto-2014>) #cite(<mohammed-2023>) #cite(<nazir-2026>), and an on-device no-text VLM fallback implemented with Moondream 0.5B #cite(<das-2023>) #cite(<sharshar-2025>). All processing occurs locally on the user's device to preserve privacy under real-world mobile constraints. It is evaluated as a bounded software-engineering study covering software quality, user acceptance, expert review, baseline convergent association, and short-term behavioral differences during a two-week field deployment.

== Background of the Study

In recent years, doomscrolling has emerged as a behavioral concern in algorithm-driven social media environments. Short-form platforms such as TikTok, Facebook Reels, and Instagram Reels are built around rapid, recommendation-driven streams of content, and prior studies associate heavier or more compulsive use with attention-related strain, procrastination, reduced mindfulness, anxiety-related symptoms, and related negative outcomes #cite(<qin-2022>) #cite(<zhang-tiktok-2023>) #cite(<canila-2023>) #cite(<cardoso-2024>) #cite(<taskin-2024>) #cite(<hawwa-2025>).

Existing digital well-being tools address this problem only partly. Prior work shows that the emotional effects of scrolling depend on what users are exposed to, not just how long they scroll #cite(<buchanan-2021>). Mainstream responses still center on time limits, usage summaries, or fixed break reminders, while app-level interventions reviewed in the literature remain heterogeneous and do not foreground privacy-by-design as a core requirement #cite(<apple-screen-time-2025>) #cite(<google-digital-wellbeing-2024>) #cite(<tiktok-wellbeing-2024>) #cite(<mosseri-2021>) #cite(<rahmillah-2023>) #cite(<tewari-2023>). This suggests a narrower engineering gap for the present study: whether a private mobile tool can move beyond time alone by combining observable interaction patterns with a text-first sentiment proxy while still handling no-text items through an on-device visual fallback.

Mindfulness-oriented digital interventions provide limited support for brief app-delivered self-regulation prompts #cite(<mitsea-2023>). Just-in-time smartphone interventions have also shown that smartphone-based support can be delivered in the moment, with feasibility and acceptability depending on timing, receptivity, and burden management #cite(<roffarello-2021>) #cite(<teepe-2021>) #cite(<mair-2022>) #cite(<yang-2023>). These works support in-the-moment prompting; what remains untested is a single system that continuously monitors short-form video behavior, processes extracted text locally, resolves no-text items through a visual fallback, and issues adaptive prompts entirely on-device.

In the Philippine context, local peer-reviewed studies indicate that doomscrolling, problematic social-media use, and digital well-being are relevant concerns, although the strongest available evidence remains student-focused #cite(<canila-2023>) #cite(<cardoso-2024>) #cite(<punzalan-2024>). Nearby adult Philippine contexts are more indirect, including social-media use among older adults and anxiety-related social-media exposure findings in adult workers #cite(<castillo-2022>) #cite(<zamora-2021>). The local literature therefore supports contextual relevance, but not adult prevalence, validated thresholds, or expected intervention effects. Against that backdrop, the study examines whether the system can combine behavioral monitoring, a text-first sentiment proxy, the no-text VLM fallback, and adaptive prompting within a privacy-by-design architecture, then be evaluated for usability, reliability, and practical usefulness among adult Filipino users.

== Statement of the Problem

This study addresses the gap left by time-based or platform-controlled digital well-being tools by asking whether a privacy-preserving Android app can be designed, developed, and evaluated for doomscrolling-related risk estimation on short-form video platforms. Current responses remain fragmented: platform-native tools are usually time-based, while related research analogues rarely combine local monitoring, content-sensitive estimation, and adaptive prompting in one deployable adult short-form video system. Because the available local literature does not yet support calibrated estimator thresholds or clinical interpretation, the problem is framed as a software-engineering question. Specifically, it seeks to answer the following questions:

1. What privacy-preserving mobile architecture and estimation framework can support doomscrolling-related risk estimation on the target short-form video platforms using behavioral indicators and sentiment-related indicators when reliably resolvable, with 2-input behavioral fallback for sentiment-unreliable sessions?
2. What short-term Week 1-to-Week 2 changes are observed in selected logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group, and within the intervention group across the same period?
3. What baseline convergent association exists between the fixed-prior Week 1 Doomscroll Severity Index (DSI) and participants' self-reported doomscrolling scores among participants with at least three sentiment-reliable Week 1 sessions?
4. How do users evaluate the system using the ISO/IEC 25010 software quality model and the Technology Acceptance Model (TAM)?
5. How do subject matter experts evaluate the system's technical design, privacy safeguards, heuristic logic, and intervention structure?

== Significance of the Study

*Contribution to the Body of Knowledge:* This study contributes a mobile-systems design and pilot-scale field evaluation of one app that integrates behavioral monitoring, a text-first sentiment proxy, a no-text VLM fallback, adaptive prompting, and on-device privacy. Its contribution is an engineering evaluation framework for feasibility, acceptability, and estimator plausibility. The *Minimum Viable Lexicon (MVL)* workflow supports low-resource, code-mixed text handling within the system.

Beyond its academic contribution, the study may offer value to the following stakeholders:

*Adult users of short-form video platforms* may benefit from a consent-based self-monitoring application that supports reflective interruption of problematic scrolling behavior without sending raw content to external servers.

*Software developers and mobile-computing researchers* may reuse the app's architecture, hybrid text/VLM proxy design, and fallback logic when building privacy-preserving digital well-being systems for code-mixed social media environments.

*Digital well-being researchers* may use the study as an example of how behavioral logging, lightweight sentiment analysis, and adaptive prompting can be combined in a mobile field evaluation.

*Educational institutions and digital well-being advocates* may use the completed study output to support responsible discussions about privacy-preserving, user-directed self-regulation tools.

== Objectives of the Study

*General Objective:*

To design, develop, and evaluate the proposed system as a privacy-preserving, on-device Android application for doomscrolling-related risk estimation on short-form video platforms.

*Specific Objectives:*

1. To design and develop the system to estimate doomscrolling-related risk using behavioral indicators and sentiment-related indicators when reliably resolvable, with 2-input behavioral fallback for sentiment-unreliable sessions.
2. To determine the short-term Week 1-to-Week 2 changes in selected logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group, and within the intervention group across the same period.
3. To determine the baseline convergent association between the fixed-prior Week 1 Doomscroll Severity Index (DSI) and participants' self-reported doomscrolling scores among participants with at least three sentiment-reliable Week 1 sessions.
4. To evaluate the computational efficiency of the system's core runtime algorithms through analytic time-complexity analysis of VADER scoring, per-item routing, VLM fallback, fuzzy inference, NSD aggregation, week-level DSI computation, and quantile personalization, and to evaluate the system from the users' perspective using the ISO/IEC 25010 software quality model and TAM.
5. To obtain subject matter expert evaluation of the system's technical design, privacy safeguards, heuristic logic, and intervention structure.

== Scope, Delimitations, and Limitations

This study covers the design, development, and evaluation of the system. The scope centers on the software artifact, its risk-estimation variables, and the two-week field evaluation.

*Scope:*
- Development of a native Android application with Android 8.0 (API level 26) as the minimum supported operating-system version. The screenshot-assisted no-text VLM path requires Android 11 (API level 30) or higher; devices below this version use the sentiment-unreliable, 2-input behavioral fallback when screenshot capture is unavailable.
- Implementation and evaluation cover one deployable system and a pilot-scale two-week field study involving 50 adult Filipino Android users assigned equally to an intervention group and a logging-only control group. Both groups completed a one-week baseline phase with prompts disabled. During the second week, adaptive prompts were enabled only for the intervention group, while the control group continued logging without prompts. Aggregate usage and sentiment-related metrics were recorded throughout both weeks, followed by post-usage user evaluation.
- Use of the Android Accessibility Service API for text extraction, interaction-event monitoring, and screenshot-assisted no-text routing through `AccessibilityService.takeScreenshot`, with related interaction signals used to estimate session duration, compute video dwell time from content transitions, and process sentiment-related indicators from visible text or no-text visual items. Swipe counts are retained only as supporting logs.
- Design of a modular architecture with separation between data collection, processing, and user interface components.
- Implementation of the core estimation framework using threshold-based rules, text-first VADER sentiment analysis, an on-device no-text VLM fallback using Moondream 0.5B, and fuzzy-logic inference, with unresolved cases handled through sentiment-unreliable classification and 2-input behavioral fallback.
- The system is designed for TikTok, Facebook Reels, and Instagram Reels as target platforms, although actual platform exposure during the field deployment varied by participant and empirical claims are limited to platforms that yielded stable extraction.
- On-device processing, where raw text and temporary no-text screen frames are processed locally in RAM and discarded after scoring, while only aggregate local metrics and configuration data are retained on-device.
- A user-directed design in which risk estimates and prompts are delivered only to the consenting user on their own device, with no remote administrator dashboard.
- Evaluation uses the ISO/IEC 25010 Software Quality Model (Functional Suitability, Performance Efficiency, Usability, Reliability), the Technology Acceptance Model (Perceived Usefulness, Perceived Ease of Use), subject matter expert review, and short-term observed differences under the study conditions.

*Delimitations and Limitations:*

This study evaluates software behavior and short-term usage differences, not clinical outcomes. The following boundaries include both deliberate choices made to define the study's coverage and constraints that may affect the findings or their generalizability. Changes in logged metrics should not be interpreted as reductions in anxiety, distress, or mental health conditions.

- The study targets Filipino Android users aged 18 years and above who actively use at least one of the target short-form video platforms. Its external validity does not extend to minors, iOS users, or users who primarily consume long-form or non-short-form content.
- Any field deployment is limited to Android devices that can install the study build, maintain the required permissions, and keep the monitoring service sufficiently stable during the evaluation window.
- The risk-estimation framework addresses only short-form video platforms and does not cover long-form video, news websites, general web browsing, or other social media formats outside TikTok, Facebook Reels, and Instagram Reels.
- The study is not designed as a clinical diagnostic system, medical intervention, psychotherapy tool, or long-term habit-formation trial.
- The study does not claim to prevent doomscrolling. It evaluates a heuristic risk-estimation system and observes short-term behavioral differences between intervention and control groups.
- The study does not attempt multi-site rollout, large-sample validation, or extensive model optimization.
- The study excludes covert monitoring, employer surveillance, parental-control workflows, remote dashboards, and third-party alerting mechanisms.
- The use of Android Accessibility Service to read on-screen content from third-party applications may create platform-policy concerns. Ethical risk is reduced through explicit user consent, local processing, and the absence of third-party surveillance workflows, but policy changes remain outside the researchers' control.
- The Accessibility Service approach is fragile by nature. Changes in the user interface of TikTok, Facebook, or Instagram may disrupt extraction logic, and Android device manufacturers may aggressively terminate background services, affecting data continuity.
- Continuous monitoring may affect battery life, thermal behavior, and background stability on some devices, which can in turn influence both system reliability and user acceptance.
- The system does not perform continuous full-resolution video or audio analysis. It relies on behavioral logs, accessibility-extracted text, and sparse transient on-device screen-frame sampling for no-text items. The system therefore still estimates risk from observable proxies rather than from full multimodal understanding of the viewed media.
- Although the deployable app supports Android 8.0 (API 26) and higher, the screenshot-assisted no-text VLM path depends on `AccessibilityService.takeScreenshot`, which is available only on Android 11 (API 30) and higher. On older compatible devices, no-text visual resolution degrades to the sentiment-unreliable / 2-input fallback path when screenshot capture is unavailable.
- Video duration is not consistently exposed across all target applications through the Accessibility Service. For this reason, the system relies on video dwell time and related interaction patterns rather than exact platform-reported video length.
- Because Android Accessibility APIs do not expose a guaranteed content-level identifier for third-party short-form feed items, transition detection remains heuristic and may still mis-segment some consecutive items, especially when they are text-barren or visually similar.
- The baseline sentiment engine is English-centric. Although the system extends it with a limited Filipino/Taglish lexicon, unsupported dialects, slang drift, and code-mixed text may still reduce sentiment reliability.
- Sessions with high out-of-vocabulary text, or no-text items that still cannot be resolved reliably after VLM routing, are treated as sentiment-unreliable and excluded from some sentiment-dependent analyses, which may reduce the effective analyzable sample for some outcomes.
- Self-reported profile data and Doomscrolling Scale responses remain subject to recall bias and social desirability bias.
- Because recruitment is limited to consenting participants willing to install a monitoring application, the sample may overrepresent users who are already receptive to self-regulation tools.
- Although the logging-only control group was intended to isolate prompt effects from monitoring awareness and natural fluctuation, the study remains non-blinded: intervention-group participants know they are receiving prompts, which may introduce expectancy or novelty effects beyond the prompt mechanism itself.
- For the intervention group, prompt-excluded active-use metrics can be mechanically lower by design when prompts occur because prompt-display time is removed from session duration and dwell-time calculations. For this reason, Chapter 3 treats raw elapsed session duration and raw elapsed dwell time as the primary behavioral comparison, while prompt-excluded metrics are kept only as supplementary traces of prompt-interrupted use.
- The two-week deployment window captures only short-term behavior. It is insufficient for demonstrating long-term habit formation, retention, or sustained behavioral change.
- Participants know they are being monitored, which introduces Hawthorne-effect risk and may reduce the naturalism of observed behavior.

== Conceptual Framework

The study uses the *Input-Process-Output (IPO) model* to illustrate the implemented runtime architecture and data flow for heuristic risk estimation #cite(<laudon-2022>). The IPO model was selected because it provides a compact and transparent representation of how observable behavioral and content signals are transformed into a heuristic risk estimate and, in the intervention phase, into an adaptive prompt. The three stages of the model are used in this study to describe (a) the runtime signals that the mobile application collects on-device, (b) the reasoning pipeline that combines behavioral thresholds, sentiment analysis, and fuzzy logic inference to derive a graded risk state, and (c) the outputs that either inform local analytics only (Week 1 baseline) or additionally trigger an adaptive mindfulness prompt (Week 2 intervention). The addition of a *feedback loop* in Figure 1 makes explicit that the pipeline is not one-shot: outputs produced during an ongoing scrolling session are fed back into the next interval's Input, allowing the system to continuously re-estimate risk based on subsequent user behavior after a prompt has been shown or after further scrolling has occurred.

#figure(
  kind: image,
  align(center)[
    #block(width: 100%)[
      #stack(
        dir: ttb,
        spacing: 0.4em,
        grid(
          columns: (1fr, 0.1fr, 1.8fr, 0.1fr, 1fr),
          align: center + horizon,
          gutter: 5pt,
          figure_panel_items(
            [Input],
            (
              [Interaction metrics],
              [Captions and visible comments],
              [App-state events],
            ),
            body_width: 96%,
            item_align: left,
            item_size: 8.4pt,
            item_leading: 0.92em,
            item_gap: 5pt,
          ),
          text(size: 12pt, weight: "medium")[→],
          figure_panel_items(
            [Process],
            (
              [Threshold-based heuristics],
              [VADER + limited Filipino/Taglish lexicon],
              [On-device no-text VLM fallback],
              [2-input fallback if sentiment unavailable],
              [Fuzzy logic inference],
            ),
            body_width: 96%,
            item_align: left,
            item_size: 8.4pt,
            item_leading: 0.92em,
            item_gap: 5pt,
          ),
          text(size: 12pt, weight: "medium")[→],
          figure_panel_items(
            [Output],
            (
              [Risk-state estimate],
              [Adaptive mindfulness prompts],
              [Local usage analytics],
            ),
            body_width: 96%,
            item_align: left,
            item_size: 8.4pt,
            item_leading: 0.92em,
            item_gap: 5pt,
          ),
        ),
        // Feedback loop annotation: return arrow from Output back to Input.
        block(width: 100%, inset: (top: 0.3em))[
          #align(center)[
            #box(width: 92%)[
              #grid(
                columns: (auto, 1fr, auto),
                align: (left + horizon, center + horizon, right + horizon),
                gutter: 0pt,
                text(size: 11pt, weight: "medium")[←],
                line(length: 100%, stroke: (paint: black, thickness: 0.7pt, dash: "dashed")),
                text(size: 11pt, weight: "medium")[┘],
              )
            ]
          ]
          #v(0.15em)
          #align(center)[
            #text(size: 8.5pt, style: "italic")[Feedback loop: prompt response and continued behavior feed the next interval's Input for re-estimation]
          ]
        ],
      )
    ]
  ],
  caption: [Conceptual Framework of the Study Using the IPO Model with Feedback Loop],
)

The IPO model here refers to the runtime application pipeline rather than the full research workflow. The *Input* stage captures three families of signals directly on-device: (i) interaction metrics such as swipe or scroll transitions, video dwell time, and session-level engagement counters; (ii) captions and visible comments extracted from the target short-form video platforms; and (iii) app-state events that mark session start, foreground and background transitions, and prompt-response events. These signals are all collected locally through the Android Accessibility Service and never leave the device.

The *Process* stage combines four reasoning layers. Threshold-based heuristics screen the interaction metrics and produce membership degrees for Session Duration and Video Dwell Time. VADER, extended with a limited Filipino/Taglish minimum viable lexicon, computes item-level sentiment on usable text units. When a viewed item has no usable caption or visible comments, an on-device Vision-Language Model fallback (Moondream 0.5B) is invoked to estimate whether the item contributes negative exposure. If neither text nor VLM paths can resolve an item reliably, the pipeline degrades gracefully to a two-input fallback that relies only on Session Duration and Video Dwell Time. All resolved signals are then combined by a fuzzy logic inference engine that yields a graded RiskScore across low, warning, and critical bands.

The *Output* stage produces the RiskScore, the corresponding risk state, and — during Week 2 — an adaptive mindfulness prompt at level L1 (awareness), L2 (pause), or L3 (pause-and-reset short breathing break). Local usage analytics are also written to on-device storage for later export. The *feedback loop* shown in Figure 1 operates once the live duration gate is met and the live RiskScore enters the Warning or Critical bands: the app selects an intervention level, closes the current interval, and begins a new one only if the user returns to the target platform. The response to the prompt (dismissal, engagement, or platform exit) and the subsequent behavior become part of the next interval's Input, so the system continuously updates its risk estimate rather than relying on a single decision point. When sufficient reliable baseline sessions exist, the live prompt engine may personalize Session Duration and NSD memberships from Week 1 quantiles while Video Dwell Time remains fixed, further reinforcing the adaptive character of the loop.

== Theoretical Framework

The theoretical framework of this study integrates four complementary theories that jointly justify each aspect of the system: the *Doomscrolling Feedback Loop Model* #cite(<sharma-2022>), which anchors the phenomenon being estimated and grounds the specific role of Negative Sentiment Density; *Uses and Gratifications Theory* #cite(<katz-1973>) #cite(<ruggiero-2000>), which explains why users initiate and prolong exposure to distressing short-form content; *Self-Determination Theory* #cite(<ryan-deci-2000>), which grounds the design of non-coercive, autonomy-supportive mindfulness prompts; and the *Dual-Systems / Habit Loop* perspective #cite(<kahneman-2011>) #cite(<wood-neal-2007>), which explains the automatic, cue-driven character of prolonged scrolling and the timing at which interruption is expected to be most effective. The evaluation of the resulting artifact is then guided by *ISO/IEC 25010* #cite(<iso-25010-2023>) and the *Technology Acceptance Model (TAM)* #cite(<adnan-2025>), while the baseline-to-intervention design supports short-term behavioral comparison.

=== Doomscrolling Feedback Loop Model

The system is primarily informed by the *Doomscrolling Feedback Loop Model* derived from #cite(<sharma-2022>, form: "prose"). The model conceptualizes doomscrolling as a self-reinforcing three-phase cycle:

1. *Antecedents or triggers:* Users are drawn toward distressing or uncertainty-inducing content by factors such as anxiety, fear of missing out, perceived information needs during crises, or algorithmic amplification of emotionally charged material. The salient feature of this phase is not merely that negative content is available, but that it is *densely present* within the user's immediate feed, which increases the probability that any given item consumed carries negative valence.
2. *Behavior:* Users engage in persistent, repetitive scrolling through negative or emotionally charged content. In the short-form video context this manifests as prolonged single-session engagement (long session duration) and reduced per-item dwell time as users rapidly sample many items, or, alternately, elevated per-item dwell time on distressing items.
3. *Outcome:* Sustained exposure to such content contributes to distress, negative affect, and heightened vigilance, which may in turn reinforce the antecedent phase (elevated anxiety) and the behavior phase (further compensatory scrolling), closing the loop.

The distinctive contribution of Sharma et al.'s model over generic screen-time or problematic-use accounts is precisely the loop structure: negative exposure is not treated as an incidental side-effect but as an active driver of the next iteration. This study operationalizes each phase with observable proxies. The behavior phase is estimated by *Video Dwell Time* and *Session Duration*, which are extracted from swipe/scroll transition signals and app-state events. The antecedent phase — the density of negative content in the user's immediate exposure — is estimated by *Negative Sentiment Density (NSD)*, which serves as the model's operational bridge into content-level measurement.

*Role of Negative Sentiment Density (NSD) in the model.* NSD is defined in this study as the proportion of resolvable content units within a session that are classified as negative, computed from analyzable caption and visible-comment text units that pass the text-side reliability screen, together with no-text items resolved through the on-device VLM fallback. NSD occupies the *antecedents/triggers* branch of the Doomscrolling Feedback Loop Model: it estimates how negatively-loaded the user's immediate content environment is, and therefore how strongly the environment is likely to trigger continued scrolling in the next interval. In the loop's forward direction, a rising NSD combined with elevated Session Duration and altered Video Dwell Time signals that the user is transitioning from ordinary engagement into a pattern more consistent with doomscrolling; the system responds with an adaptive prompt intended to interrupt the loop before the outcome phase is further reinforced. In the loop's return direction (Figure 1), the user's response to the prompt and the sentiment composition of items viewed after the prompt feed the next interval's NSD, so that NSD itself is continuously updated across the session rather than being computed once. NSD is therefore the theoretical framework's direct point of contact with the sentiment analysis subsystem: without an operational density measure at the antecedents phase, the loop model could only be observed at the behavior phase, and the interruption logic would collapse into a pure duration timer.

=== Uses and Gratifications Theory

*Uses and Gratifications Theory (U&G)* #cite(<katz-1973>) #cite(<ruggiero-2000>) posits that media users actively select and continue engaging with content that satisfies specific psychological and social needs — information-seeking, mood management, surveillance, and social integration — rather than being passive recipients. In the context of short-form video platforms and doomscrolling, U&G explains *why* users initiate and prolong exposure to negative or distressing content: distressing content can paradoxically gratify surveillance and uncertainty-reduction needs during crises, and rapid item turnover on short-form platforms gratifies mood-repair and stimulation-seeking needs even when individual items are aversive. U&G therefore justifies treating scrolling behavior as goal-directed and non-random, which supports the study's use of behavioral proxies (Session Duration, Video Dwell Time) as meaningful indicators of engagement intensity rather than as noise. It also justifies designing prompts that offer an alternative gratification pathway — reflective interruption and self-monitoring — rather than merely blocking access, which would leave the underlying need unaddressed.

=== Self-Determination Theory

*Self-Determination Theory (SDT)* #cite(<ryan-deci-2000>) distinguishes between autonomous and controlled forms of motivation and identifies autonomy, competence, and relatedness as the three basic psychological needs whose support facilitates self-regulation and well-being. SDT grounds the design of the mindfulness prompts along two dimensions. First, the prompts are *non-coercive*: they present awareness, pause, or short breathing-break invitations at graded levels (L1, L2, L3) rather than forcing app closure, preserving user autonomy in deciding whether to continue. Second, the prompts are *informational rather than controlling*: they surface behavioral signals (elapsed session duration, current risk band) so that users can exercise competence in interpreting and acting on their own usage, which is consistent with SDT's finding that autonomy-supportive interventions are more likely to be internalized and sustained than externally imposed restrictions. SDT thereby justifies the study's choice of adaptive mindfulness prompts over hard blocks or punitive time limits.

=== Dual-Systems Reasoning and the Habit Loop

The *Dual-Systems* perspective #cite(<kahneman-2011>) distinguishes between System 1 — fast, automatic, cue-driven processing — and System 2 — slow, effortful, reflective processing. The *Habit Loop* view of habitual behavior #cite(<wood-neal-2007>) complements this by describing habits as cue-routine-reward sequences in which contextual cues automatically activate learned motor and attentional routines with minimal deliberative control. Prolonged scrolling on short-form video platforms exemplifies System-1, cue-driven behavior: opening the app functions as the cue, the swipe-and-consume sequence is the routine, and brief affective spikes from novel content function as intermittent reward. This perspective grounds two design decisions in the present study. First, it justifies *timing* interruption around the behavioral signals themselves (a duration gate combined with the fuzzy risk band) rather than at fixed clock times, because a well-timed cue must arrive while the automatic routine is active in order to shift processing toward System 2. Second, it explains *why brief reflective prompts are expected to be effective at all*: by inserting a small deliberative moment into the loop, the prompt aims to convert an otherwise System-1 continuation decision into a System-2 evaluation, which is precisely the mechanism the mindfulness literature invokes for attentional reset.

=== Integration and Evaluation

Taken together, the four theories map onto the framework as follows: the Doomscrolling Feedback Loop Model defines *what* the system estimates and how NSD, Session Duration, and Video Dwell Time correspond to its phases; Uses and Gratifications explains *why* users engage with the behavior in the first place and why behavioral proxies are meaningful; Self-Determination Theory grounds the *form* of the intervention as autonomy-supportive rather than restrictive; and the Dual-Systems/Habit-Loop perspective grounds the *timing and mechanism* by which a brief prompt is expected to interrupt automatic scrolling. The framework thus justifies using observable proxies and non-clinical prompts. The evaluation of the resulting artifact is then guided by ISO/IEC 25010 #cite(<iso-25010-2023>) and TAM #cite(<adnan-2025>), while the baseline-to-intervention design supports short-term behavioral comparison.

#figure(
  kind: image,
  align(center)[
    #block(width: 78%, inset: (bottom: 0.35em))[
      #stack(
        dir: ttb,
        spacing: 0.7em,
        figure_panel(
          [Antecedents / Triggers],
          [Negative Content Exposure],
          note: [Operational proxy: Negative Sentiment Density (NSD)],
        ),
        figure_arrow(),
        figure_panel(
          [Behavior],
          [Persistent Scrolling Engagement],
          note: [Operational proxies: dwell time and session duration; transitions are inferred from swipe/scroll events],
        ),
        figure_arrow(),
        figure_panel(
          [Outcome],
          [Increased Distress or Negative Affect],
          note: [Conceptual outcome; not directly measured by the system],
        ),
        block(inset: (bottom: 0.45em))[
          #figure_arrow(symbol: "↑", note: [Feedback loop: outcomes may reinforce triggers])
        ],
      )
    ]
  ],
  caption: [Doomscrolling Feedback Loop Diagrammatic Interception Model, adapted from #cite(<sharma-2022>, form: "prose")],
)

The system is therefore a *heuristic risk-estimation tool* that uses behavioral and exposure proxies rather than direct measures of internal psychological states.

== Definition of Terms

*Adaptive Digital Wellness Prompts* - Non-clinical system-generated prompts, such as awareness notifications, pause prompts, or short pause-and-reset breathing breaks, that are triggered according to estimated activity pattern severity to encourage reflective interruption of scrolling behavior.

*Digital Mindfulness* - A practice of intentional and self-aware technology use that promotes attention, presence, and self-regulation in digital environments #cite(<aggarwal-2024>).

*Doomscrolling* - The compulsive and continuous consumption of negative or distressing content on digital platforms, particularly in ways that may reinforce anxiety, vigilance, or maladaptive engagement #cite(<sharma-2022>).

*Doomscrolling-Related Risk Estimation* - The computational estimation of elevated scrolling risk using observable behavioral and sentiment-related proxies rather than direct measurement of internal psychological states.

*Edge Computing* - A computing approach in which data processing occurs on or near the data source - in this study, on the user's mobile device - rather than in remote cloud infrastructure, thereby reducing latency and privacy risks #cite(<swathi-2025>).

*Fuzzy Logic System* - A computational approach that uses partial membership and rule-based reasoning to model ambiguous behavioral patterns, allowing graded risk estimation instead of binary classification through interpretable linguistic rules #cite(<vashishtha-2023>) #cite(<pickering-2025>).

*Heuristic Risk-State Estimation System* - The Android application developed in this study for heuristic doomscrolling-related risk estimation and adaptive prompting.

*ISO/IEC 25010* - An international software quality model used in this study to evaluate selected quality characteristics of the system, specifically Functional Suitability, Performance Efficiency, Usability, and Reliability #cite(<iso-25010-2023>).

*Minimum Viable Lexicon (MVL)* - The limited Filipino/Taglish lexicon extension used by the system to supplement VADER for recurring code-mixed social-media terms. It includes Filipino review-instrument candidate terms with implementation runtime valence values and separate neutral Filipino function-word entries used only to reduce false OOV inflation.

*Negative Sentiment Density (NSD)* - The proportion of resolvable content units within a session that are classified as negative. In this study, it is computed from analyzable caption and visible-comment text units that pass the text-side reliability screen together with no-text items resolved through the VLM fallback. It is used as a limited proxy for negative exposure.

*Out-of-Vocabulary (OOV) Ratio* - The proportion of extracted text tokens that are not recognized by the base VADER lexicon, emoji/emoticon handling, Filipino MVL extension, booster/negation rules, or neutral OOV-reduction list. High OOV values are used as a reliability screen rather than as a sentiment score.

*Technology Acceptance Model (TAM)* - A framework used to assess user acceptance of digital systems, particularly through the constructs of Perceived Usefulness and Perceived Ease of Use, and still commonly applied in mHealth acceptability evaluation #cite(<adnan-2025>).

*VADER Sentiment Analysis* - Valence Aware Dictionary and sEntiment Reasoner, a lexicon- and rule-based sentiment analysis method for social media text that is adapted in this study for limited code-mixed use #cite(<hutto-2014>).

*Vision-Language Model (VLM) Fallback* - A limited on-device visual-analysis path used in this study when a viewed short-form item has no usable caption or visible comments. It uses the Moondream 0.5B model together with `AccessibilityService.takeScreenshot` and transient RAM-only frame processing to estimate whether a no-text item contributes negative exposure.

*Video Dwell Time* - The time interval spent on a viewed short-form video before the user swipes away or otherwise transitions, computed from heuristically inferred content-transition signals derived from accessibility events and used in this study as one behavioral proxy for scrolling engagement.

*Very Large Online Platforms (VLOPs)* - Large digital platforms, such as Facebook, Instagram, and TikTok, that operate at massive user scale and strongly shape online content exposure and engagement patterns #cite(<chen-2024>).

*Session Duration* - The continuous period of active engagement with a target short-form video platform, as determined by interaction signals and the session-management rules described in Chapter 3.

*Sentiment-Unreliable Session* - A session in which item-level negativity cannot be derived reliably for the main analysis after the available text path or no-text VLM path has been attempted, such as sessions dominated by high-OOV text or by unresolved no-text items. Such sessions are excluded from the primary NSD- and DSI-based analyses.

*Swipe/Scroll Transition Signals* - Accessibility-detected navigation events used to heuristically infer when one short-form video ends and another begins, with text verification applied when usable caption text exists, enabling the computation of video dwell time and the logging of supporting interaction counts.
