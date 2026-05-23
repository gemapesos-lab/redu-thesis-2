// ==========================================
// CHAPTER 1: INTRODUCTION
// ==========================================

#import "utils.typ": figure_arrow, figure_panel, figure_panel_items

= INTRODUCTION

Doomscrolling refers to the compulsive consumption of distressing or negative online content despite its adverse emotional and behavioral effects #cite(<sharma-2022>). Mainstream device- and platform-level digital well-being tools still emphasize screen-time summaries, app timers, or break reminders instead of session-level interpretation of fast-changing feed content #cite(<apple-screen-time-2025>) #cite(<google-digital-wellbeing-2024>) #cite(<tiktok-wellbeing-2024>) #cite(<mosseri-2021>) #cite(<rahmillah-2023>). This thesis addresses that gap through the design, development, and evaluation of the *Heuristic Risk-State Estimation System* — a privacy-preserving Android app that performs on-device risk estimation using behavioral signals, a text-first sentiment path, and an on-device Vision-Language Model (VLM) fallback for no-text items. Here, "detection" refers to computational risk estimation from observable proxies, not diagnostic classification.

The system combines threshold-based heuristics, interpretable fuzzy rule-based inference #cite(<vashishtha-2023>) #cite(<pickering-2025>), a VADER-compatible text-first sentiment pipeline for low-resource code-mixed text #cite(<hutto-2014>) #cite(<mohammed-2023>) #cite(<nazir-2026>), and an on-device no-text VLM fallback implemented with Moondream 0.5B #cite(<das-2023>) #cite(<sharshar-2025>). All processing occurs locally on the user's device to preserve privacy under real-world mobile constraints. It is evaluated as a bounded software-engineering study, not a clinical trial, and focuses on practical usefulness, software quality, user acceptance, expert review, baseline convergent association, and short-term behavioral differences during a two-week field deployment.

== Background of the Study

In recent years, doomscrolling has emerged as a behavioral concern in algorithm-driven social media environments. Short-form platforms such as TikTok, Facebook Reels, and Instagram Reels are built around rapid, recommendation-driven streams of content, and prior studies associate heavier or more compulsive use with attention-related strain, procrastination, reduced mindfulness, anxiety-related symptoms, and related negative outcomes #cite(<qin-2022>) #cite(<zhang-tiktok-2023>) #cite(<canila-2023>) #cite(<cardoso-2024>) #cite(<taskin-2024>) #cite(<hawwa-2025>).

Existing digital well-being tools address this problem only partly. Prior work shows that the emotional effects of scrolling depend on what users are exposed to, not just how long they scroll #cite(<buchanan-2021>). Mainstream responses still center on time limits, usage summaries, or fixed break reminders, while app-level interventions reviewed in the literature remain heterogeneous and do not foreground privacy-by-design as a core requirement #cite(<apple-screen-time-2025>) #cite(<google-digital-wellbeing-2024>) #cite(<tiktok-wellbeing-2024>) #cite(<mosseri-2021>) #cite(<rahmillah-2023>) #cite(<tewari-2023>). This suggests a narrower engineering gap for the present study: whether a private mobile tool can move beyond time alone by combining observable interaction patterns with a text-first sentiment proxy while still handling no-text items through an on-device visual fallback.

Mindfulness-oriented digital interventions provide limited support for brief app-delivered self-regulation prompts #cite(<mitsea-2023>). Just-in-time smartphone interventions have also shown that smartphone-based support can be delivered in the moment, with feasibility and acceptability depending on timing, receptivity, and burden management #cite(<roffarello-2021>) #cite(<teepe-2021>) #cite(<mair-2022>) #cite(<yang-2023>). These works support in-the-moment prompting, but they do not show whether one system can continuously monitor short-form video behavior, process extracted text locally, resolve no-text items through the VLM fallback, and issue adaptive prompts without cloud transmission or remote dashboards.

In the Philippine context, local peer-reviewed studies indicate that doomscrolling, problematic social-media use, and digital well-being are relevant concerns, although the strongest available evidence remains student-focused #cite(<canila-2023>) #cite(<cardoso-2024>) #cite(<punzalan-2024>). Nearby adult Philippine contexts are more indirect, including social-media use among older adults and anxiety-related social-media exposure findings in adult workers #cite(<castillo-2022>) #cite(<zamora-2021>). The local literature therefore supports contextual relevance, but not adult prevalence, validated thresholds, or expected intervention effects. Against that backdrop, the study examines whether the system can combine behavioral monitoring, a text-first sentiment proxy, the no-text VLM fallback, and adaptive prompting within a privacy-by-design architecture, then be evaluated for usability, reliability, and practical usefulness among adult Filipino users.

== Statement of the Problem

This study addresses the gap left by time-based or platform-controlled digital well-being tools by asking whether a privacy-preserving Android app can be designed, developed, and evaluated for doomscrolling-related risk estimation on short-form video platforms. Current responses remain fragmented: platform-native tools are usually time-based, while related research analogues rarely combine local monitoring, content-sensitive estimation, and adaptive prompting in one deployable adult short-form video system. Because the available local literature does not yet support calibrated estimator thresholds or clinical interpretation, the problem is framed as a software-engineering question. Specifically, it seeks to answer the following questions:

1. What privacy-preserving mobile architecture and estimation framework can support doomscrolling-related risk estimation on the target short-form video platforms using behavioral indicators and sentiment-related indicators when reliably resolvable, with 2-input behavioral fallback for sentiment-unreliable sessions?
2. What short-term Week 1-to-Week 2 changes are observed in selected logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group, and within the intervention group across the same period?
3. What baseline convergent association exists between the fixed-prior Week 1 Doomscroll Severity Index (DSI) and participants' self-reported doomscrolling scores among participants with at least three sentiment-reliable Week 1 sessions?
4. How do users evaluate the system using the ISO/IEC 25010 software quality model and the Technology Acceptance Model (TAM)?
5. How do subject matter experts evaluate the system's technical design, privacy safeguards, heuristic logic, and intervention structure?

== Significance of the Study

#block(width: 100%)[
  *Contribution to the Body of Knowledge:* This study contributes a mobile-systems design and pilot-scale field evaluation of one app that integrates behavioral monitoring, a text-first sentiment proxy, a no-text VLM fallback, adaptive prompting, and on-device privacy. Its contribution is an engineering evaluation framework for feasibility, acceptability, and estimator plausibility. The *Minimum Viable Lexicon (MVL)* workflow supports low-resource, code-mixed text handling within the system rather than functioning as a separate lexicon-development study.
]

Beyond its academic contribution, the study may offer value to the following stakeholders:

*Adult users of short-form video platforms* may benefit from a consent-based self-monitoring application that supports reflective interruption of problematic scrolling behavior without sending raw content to external servers.

*Software developers and mobile-computing researchers* may reuse the app's architecture, hybrid text/VLM proxy design, and fallback logic when building privacy-preserving digital well-being systems for code-mixed social media environments.

*Digital well-being researchers* may use the study as an example of how behavioral logging, lightweight sentiment analysis, and adaptive prompting can be combined in a mobile field evaluation.

*Educational institutions and digital well-being advocates* may use the findings to support responsible discussions about privacy-preserving, user-directed self-regulation tools rather than surveillance-oriented monitoring systems.

== Objectives of the Study

#block(width: 100%)[*General Objective:*]

#block(width: 100%)[
  To design, develop, and evaluate the proposed system as a privacy-preserving, on-device Android application for doomscrolling-related risk estimation on short-form video platforms.
]

#block(width: 100%)[*Specific Objectives:*]

1. To design and develop the system to estimate doomscrolling-related risk using behavioral indicators and sentiment-related indicators when reliably resolvable, with 2-input behavioral fallback for sentiment-unreliable sessions.
2. To determine the short-term Week 1-to-Week 2 changes in selected logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group, and within the intervention group across the same period.
3. To determine the baseline convergent association between the fixed-prior Week 1 Doomscroll Severity Index (DSI) and participants' self-reported doomscrolling scores among participants with at least three sentiment-reliable Week 1 sessions.
4. To evaluate the system from the users' perspective using the ISO/IEC 25010 software quality model and TAM.
5. To obtain subject matter expert evaluation of the system's technical design, privacy safeguards, heuristic logic, and intervention structure.

== Scope and Delimitations

This study covers the design, development, and evaluation of the system. The scope centers on the software artifact, its risk-estimation variables, and the two-week field evaluation.

*Scope:*
- Development of a native Android application using modern software engineering practices.
- Implementation and evaluation cover one deployable system and a pilot-scale two-week field study.
- Use of the Android Accessibility Service API for text extraction, interaction-event monitoring, and screenshot-assisted no-text routing through `AccessibilityService.takeScreenshot`, with related interaction signals used to estimate session duration, compute video dwell time from content transitions, and process sentiment-related indicators from visible text or no-text visual items. Swipe counts are retained only as supporting logs.
- Design of a modular architecture with separation between data collection, processing, and user interface components.
- Implementation of the core estimation framework using threshold-based rules, text-first VADER sentiment analysis, an on-device no-text VLM fallback using Moondream 0.5B, and fuzzy-logic inference, with unresolved cases handled through sentiment-unreliable classification and 2-input behavioral fallback.
- The system is designed for TikTok, Facebook Reels, and Instagram Reels as target platforms, although actual platform exposure during field deployment may vary by participant and empirical claims are limited to platforms that yield stable extraction.
- On-device processing, where raw text and temporary no-text screen frames are processed locally in RAM and discarded after scoring, while only aggregate local metrics and configuration data are retained on-device.
- A user-directed design in which risk estimates and prompts are delivered only to the consenting user on their own device, with no remote administrator dashboard.
- Evaluation uses a pilot-scale two-week deployment: all participants complete a one-week baseline logging phase, after which the intervention group receives adaptive prompts during Week 2 while the control group continues logging-only.
- Evaluation uses the ISO/IEC 25010 Software Quality Model (Functional Suitability, Performance Efficiency, Usability, Reliability), the Technology Acceptance Model (Perceived Usefulness, Perceived Ease of Use), subject matter expert review, and short-term observed differences under the study conditions.

*Delimitations:*
- The study targets Filipino Android users aged 18 years and above who actively use at least one of the target short-form video platforms. Its external validity does not extend to minors, iOS users, or users who primarily consume long-form or non-short-form content.
- Field deployment is limited to Android devices that can install the study build, maintain the required permissions, and keep the monitoring service sufficiently stable during the evaluation window.
- The risk-estimation framework addresses only short-form video platforms and does not cover long-form video, news websites, general web browsing, or other social media formats outside TikTok, Facebook Reels, and Instagram Reels.
- The study is not designed as a clinical diagnostic system, medical intervention, psychotherapy tool, or long-term habit-formation trial.
- The study does not claim to prevent doomscrolling. It evaluates a heuristic risk-estimation system and observes short-term behavioral differences between intervention and control groups.
- The study does not attempt multi-site rollout, large-sample validation, or extensive model optimization.
- The study excludes covert monitoring, employer surveillance, parental-control workflows, remote dashboards, and third-party alerting mechanisms.

== Limitations

This study evaluates software behavior and short-term usage differences — not clinical outcomes. Changes in logged metrics should not be interpreted as reductions in anxiety, distress, or mental health conditions.

The following limitations describe constraints beyond the researchers' control that may affect the study's findings and generalizability:

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
- Although a logging-only control group is included to isolate prompt effects from monitoring awareness and natural fluctuation, the study remains non-blinded: intervention-group participants know they are receiving prompts, which may introduce expectancy or novelty effects beyond the prompt mechanism itself.
- For the intervention group, prompt-excluded active-use metrics can be mechanically lower by design when prompts occur because prompt-display time is removed from session duration and dwell-time calculations. For this reason, Chapter 3 treats raw elapsed session duration and raw elapsed dwell time as the primary behavioral comparison, while prompt-excluded metrics are kept only as supplementary traces of prompt-interrupted use.
- The two-week deployment window captures only short-term behavior. It is insufficient for demonstrating long-term habit formation, retention, or sustained behavioral change.
- Participants know they are being monitored, which introduces Hawthorne-effect risk and may reduce the naturalism of observed behavior.

== Conceptual Framework

The study uses the *Input-Process-Output (IPO) model* to illustrate the implemented runtime architecture and data flow for heuristic risk estimation #cite(<laudon-2022>).

#figure(
  kind: image,
  align(center)[
    #block(width: 100%)[
      #grid(
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
      )
    ]
  ],
  caption: "Conceptual Framework of the Study using IPO Model",
)

The IPO model here refers to the runtime application pipeline rather than the full research workflow. During Week 2, a *feedback loop* operates once the live duration gate is met and the live RiskScore enters the Warning or Critical bands: the app selects an intervention level (L1 awareness, L2 pause, or L3 breathing exercise), closes the current interval, and begins a new one only if the user returns to the target platform. When sufficient reliable baseline sessions exist, the live prompt engine may personalize Session Duration and NSD memberships from Week 1 quantiles while Video Dwell Time remains fixed. Usable caption or visible-comment text stays on the text path; no-text items are routed through `AccessibilityService.takeScreenshot` and the VLM fallback; unresolved cases degrade to 2-input behavioral inference with conservative L1/L2 prompts.

== Theoretical Framework

The system is informed by the *Doomscrolling Feedback Loop Model* derived from #cite(<sharma-2022>, form: "prose"). The model is used as a design guide for content exposure, prolonged engagement, and interruption points, but the study does not directly measure the internal emotional states described by the model.

The model conceptualizes doomscrolling as a three-phase cycle:

1. *Antecedents or triggers:* Users are drawn toward distressing or uncertainty-inducing content by factors such as anxiety, fear of missing out, or crisis-related information needs.
2. *Behavior:* Users engage in persistent, repetitive scrolling through negative or emotionally charged content.
3. *Outcome:* Exposure to such content contributes to distress or negative affect, which may in turn reinforce further scrolling.

This framework informs the system's variable selection. The system estimates risk at the behavior and exposure level by observing video dwell time, session duration, and NSD as proxies for heightened maladaptive engagement. Swipe or scroll events are used only as transition signals to determine when one short-form item ends and another begins, allowing video dwell-time computation rather than serving as a separate estimator input. Its adaptive prompts are intended to interrupt potentially escalating patterns before exposure becomes more sustained.

The framework thus justifies using observable proxies and non-clinical prompts. Evaluation is guided by ISO/IEC 25010 and TAM, while the baseline-to-intervention design supports short-term behavioral comparison.

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

*Adaptive Digital Mindfulness Prompts* - Non-clinical system-generated interventions, such as awareness notifications, pause prompts, or short breathing exercises, that are triggered according to estimated risk severity to encourage reflective interruption of scrolling behavior.

*Digital Mindfulness* - A practice of intentional and self-aware technology use that promotes attention, presence, and self-regulation in digital environments #cite(<aggarwal-2024>).

*Doomscrolling* - The compulsive and continuous consumption of negative or distressing content on digital platforms, particularly in ways that may reinforce anxiety, vigilance, or maladaptive engagement #cite(<sharma-2022>).

*Doomscrolling-Related Risk Estimation* - The computational estimation of elevated scrolling risk using observable behavioral and sentiment-related proxies rather than direct measurement of internal psychological states.

*Edge Computing* - A computing approach in which data processing occurs on or near the data source - in this study, on the user's mobile device - rather than in remote cloud infrastructure, thereby reducing latency and privacy risks #cite(<swathi-2025>).

*Fuzzy Logic System* - A computational approach that uses partial membership and rule-based reasoning to model ambiguous behavioral patterns, allowing graded risk estimation instead of binary classification through interpretable linguistic rules #cite(<vashishtha-2023>) #cite(<pickering-2025>).

*Heuristic Risk-State Estimation System* - The Android application developed in this study for heuristic doomscrolling-related risk estimation and adaptive prompting.

*ISO/IEC 25010* - An international software quality model used in this study to evaluate selected quality characteristics of the system, specifically Functional Suitability, Performance Efficiency, Usability, and Reliability #cite(<iso-25010-2023>).

*Negative Sentiment Density (NSD)* - The proportion of resolvable content units within a session that are classified as negative. In this study, it is computed from analyzable caption and visible-comment text units that pass the text-side reliability screen together with no-text items resolved through the VLM fallback. It is used as a limited proxy for negative exposure.

*Technology Acceptance Model (TAM)* - A framework used to assess user acceptance of digital systems, particularly through the constructs of Perceived Usefulness and Perceived Ease of Use, and still commonly applied in mHealth acceptability evaluation #cite(<adnan-2025>).

*VADER Sentiment Analysis* - Valence Aware Dictionary and sEntiment Reasoner, a lexicon- and rule-based sentiment analysis method for social media text that is adapted in this study for limited code-mixed use #cite(<hutto-2014>).

*Vision-Language Model (VLM) Fallback* - A limited on-device visual-analysis path used in this study when a viewed short-form item has no usable caption or visible comments. It uses the Moondream 0.5B model together with `AccessibilityService.takeScreenshot` and transient RAM-only frame processing to estimate whether a no-text item contributes negative exposure.

*Video Dwell Time* - The time interval spent on a viewed short-form video before the user swipes away or otherwise transitions, computed from heuristically inferred content-transition signals derived from accessibility events and used in this study as one behavioral proxy for scrolling engagement.

*Very Large Online Platforms (VLOPs)* - Large digital platforms, such as Facebook, Instagram, and TikTok, that operate at massive user scale and strongly shape online content exposure and engagement patterns #cite(<chen-2024>).

*Session Duration* - The continuous period of active engagement with a target short-form video platform, as determined by interaction signals and the session-management rules described in Chapter 3.

*Sentiment-Unreliable Session* - A session in which item-level negativity cannot be derived reliably for the main analysis after the available text path or no-text VLM path has been attempted, such as sessions dominated by high-OOV text or by unresolved no-text items. Such sessions are excluded from the primary NSD- and DSI-based analyses.

*Swipe/Scroll Transition Signals* - Accessibility-detected navigation events used to heuristically infer when one short-form video ends and another begins, with text verification applied when usable caption text exists, enabling the computation of video dwell time and the logging of supporting interaction counts.
