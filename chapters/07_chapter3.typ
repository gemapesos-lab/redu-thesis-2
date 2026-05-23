// ==========================================
// CHAPTER 3: METHODOLOGY
// ==========================================

#import "utils.typ": continued_thesis_table, figure_arrow, figure_boundary, figure_layer, figure_node, table_align, thesis_table

= METHODOLOGY

This chapter presents the methodology used to design, develop, and evaluate the system. It covers the research design, architecture, development procedure, evaluation method, data collection, respondents, and statistical treatment. Here, the term "detection" refers to heuristic risk-state estimation from observable proxies rather than diagnostic classification.

== Type of Research
This study adopts a *design-and-development research* approach with a *two-group baseline-intervention pilot field evaluation* #cite(<creswell-2022>). The design-and-development component focuses on building the software artifact and its heuristic risk-estimation logic, while the field-evaluation component observes the system under real-world conditions.

The study follows a primarily quantitative orientation, with open-ended feedback used as supplementary qualitative context. Automated system logs and structured survey responses are used to assess software quality, user acceptance, expert review, baseline convergent association, and short-term behavioral differences. The study is a software evaluation rather than a clinical trial, and findings are interpreted within the study conditions.

== Research Design
The study uses a *two-group baseline-intervention field design* treated as a *pilot-scale randomized field evaluation*. After consent and study-compatibility screening, participants are assigned 1:1 to either an *intervention group* or a *logging-only control group* using a concealed permuted-block allocation list with randomly ordered block sizes of 2 and 4. The allocation list is generated before enrollment by a researcher who is not responsible for participant screening and is released only after eligibility is confirmed, which helps preserve balance without making the next assignment easily predictable. All participants then complete an identical one-week baseline logging phase with prompts disabled. During Week 2, the intervention group receives adaptive prompts while the control group continues logging-only.

The study questions are answered through related but distinct methodological parts. Research Question 1 is addressed by the architecture, routing, variable-justification, and algorithm sections. Research Question 2 is addressed by Week 1-to-Week 2 change analyses of selected logged metrics and self-reported doomscrolling scores, comparing the intervention and control groups and using a paired within-intervention comparison as secondary evidence. As a pilot-scale study, these analyses estimate short-term observed differences under the study conditions rather than definitive intervention efficacy. Research Question 3 is addressed by the baseline convergent association between Week 1 DSI and the Doomscrolling Scale. Research Questions 4 and 5 are addressed by the ISO/IEC 25010, TAM, and SME evaluations.

== System Architecture
The system uses a *modular three-layer architecture* to support maintainability, interpretability, and separation of concerns. Its design follows *Privacy by Design*, keeping processing local to the device through an edge-computing approach. The application is organized into the *Data Access Layer*, *Business Logic Layer*, and *Presentation Layer*, allowing data collection, estimation logic, and user-facing components to be developed with minimal coupling. The architecture also avoids device rooting by relying on Android Accessibility Service for text extraction, interaction-event monitoring, and screenshot-assisted VLM fallback when no usable caption or visible comments exist. The figure below shows this hybrid text/VLM pipeline.

#figure(
  kind: image,
  align(center)[
    #block(width: 95%)[
      #figure_boundary(
        [Edge-Computing Boundary],
        subtitle: [All processing stays on-device; no cloud transmission],
        [
          #figure_layer(
            [Data Access Layer],
            [
              #grid(
                columns: (1fr, 0.16fr, 1fr, 0.16fr, 1fr),
                align: center + horizon,
                gutter: 4pt,
                figure_node(
                  [Target Platforms],
                  [TikTok \ Facebook Reels \ Instagram Reels],
                ),
                text(size: 11pt, weight: "medium")[→],
                figure_node(
                  [Accessibility Capture],
                  [Event listening \ Node traversal],
                ),
                text(size: 11pt, weight: "medium")[→],
                figure_node(
                  [Text / No-Text Check],
                  [Captions and comments \ unresolved-item screening],
                ),
              )
              #v(6pt)
              #grid(
                columns: (1fr, 1fr, 1fr),
                align: center + horizon,
                gutter: 6pt,
                figure_node(
                  [Session Logger],
                  [Duration, swipe count \ App-state events],
                ),
                figure_node(
                  [`takeScreenshot` Capture],
                  [No-text path only \ Transient RAM-only frame],
                ),
                figure_node(
                  [Local Storage],
                  [Aggregate metrics only \ No raw text or frames retained],
                ),
              )
            ],
          )
          #v(7pt)
          #figure_arrow()
          #v(7pt)
          #figure_layer(
            [Business Logic Layer],
            [
              #grid(
                columns: (1fr, 0.16fr, 1fr, 0.16fr, 1fr),
                align: center + horizon,
                gutter: 4pt,
                figure_node(
                  [Threshold Gates],
                  [Session and dwell gates \ First-pass filtering],
                ),
                text(size: 11pt, weight: "medium")[→],
                figure_node(
                  [Text-First Sentiment],
                  [VADER + Filipino MVL \ Usable text only],
                ),
                text(size: 11pt, weight: "medium")[→],
                figure_node(
                  [Fuzzy Inference],
                  [27-rule engine + CoG \ Risk score (0-100)],
                ),
              )
              #v(6pt)
              #grid(
                columns: (1fr, 1fr),
                align: center + horizon,
                gutter: 6pt,
                figure_node(
                  [No-Text VLM Path],
                  [On-device VQA \ No-text items only],
                ),
                figure_node(
                  [2-Input Safety Fallback],
                  [High OOV or unresolved VLM path \ L1/L2 only],
                ),
              )
            ],
          )
          #v(7pt)
          #figure_arrow()
          #v(7pt)
          #figure_layer(
            [Presentation Layer],
            [
              #grid(
                columns: (1fr, 1fr, 1fr, 1fr),
                align: center + horizon,
                gutter: 6pt,
                figure_node(
                  [Awareness Toast],
                  [Level 1 prompt],
                ),
                figure_node(
                  [Pause Prompt],
                  [Level 2 modal],
                ),
                figure_node(
                  [Breathing Overlay],
                  [Level 3 exercise],
                ),
                figure_node(
                  [Dashboard],
                  [Usage analytics],
                ),
              )
            ],
          )
        ],
      )
    ]
  ],
  caption: "System Architecture Diagram: Three-Layer Edge Computing Design",
) <fig-architecture>

=== Data Extraction Strategy
The primary mechanism for data acquisition is the Android *Accessibility Service API*. It lets the system read captions, comments, and interaction events from short-form video apps without requiring device rooting. The same layer also captures the interaction signals needed for session duration, content-transition detection, video dwell-time computation, and supporting swipe logs. When a viewed item has no usable caption or visible comments, the enabled accessibility service uses `AccessibilityService.takeScreenshot` to capture an on-demand transient screen frame for the no-text VLM fallback.

=== Accessibility Service Implementation
The implementation entails creating a background service that extends the `AccessibilityService` class. Functionally, this allows the system to observe the UI hierarchy of other apps in real-time, effectively perceiving the screen content as a screen reader would for accessibility purposes.

The technical implementation involves the following core components:
- *Service Extension*: Development of a custom service inheriting from `AccessibilityService`.
- *Event Listening*: The service subscribes to specific system events, primarily `AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED` and `TYPE_VIEW_SCROLLED`, to detect dynamic content updates.
- *Node Traversal*: Upon capturing an event, the system traverses the hierarchy of `AccessibilityNodeInfo` objects, filtering specifically for `TextView` elements to extract creator captions and any visible user comments already present on-screen as affective reaction signals. The system does not actively open hidden comment panels or trigger additional UI states during extraction.
- *Scroll-Event Disambiguation*: Because `TYPE_VIEW_SCROLLED` events are generated by both feed-level video transitions and within-video comment-panel scrolling, the system does not treat every scroll event as a new item by default. When usable caption text is present, the system applies a content-change verification step before registering a content transition: after each feed-level scroll event, the caption text is re-extracted and compared to the previously recorded caption using exact string equality after whitespace normalization, and a transition is registered only if the caption has changed. When a viewed item has no usable caption or visible comments, however, the system cannot rely on caption comparison; in that no-text case, a feed-level scroll event is treated as a heuristic new-item boundary unless suppressed by comment-panel state or similar non-feed interaction context. As a secondary check, the system tracks comment-panel open/close state via `TYPE_WINDOW_STATE_CHANGED` to suppress transition processing during comment browsing. Comment-panel scrolling, caption expansion, and UI-only animations therefore do not reset the dwell-time interval or increment the swipe count. Comments visible during panel scrolling are still extracted for sentiment analysis and associated with the current video item.

=== Constraints and Challenges
A significant challenge in this architecture is the requirement to reverse-engineer the UI structure of targeted applications. The extraction logic must identify specific resource IDs (e.g., `com.zhiliaoapp.musically:id/caption_text`) to distinguish meaningful content from UI clutter such as buttons or timestamps. This approach has inherent fragility: changes to the target application's UI structure or IDs by their respective developers may disrupt the scraper's functionality, representing a known limitation of this methodology.

=== Edge Computing and Privacy Architecture

The system follows edge computing principles to mitigate privacy risks:

1. *No Persistent Storage of Raw Content*: In the study version, scraped text content and temporary no-text screen frames are processed entirely in RAM and immediately discarded after scoring. Only derived numeric scores, aggregate usage metrics, and local configuration data are retained on-device.

2. *On-Device Processing*: All Fuzzy Logic inference, VADER analysis, and no-text VLM inference occur locally on the user's device. No content is transmitted to external servers.

3. *User-Initiated Consent*: The Accessibility Service requires explicit user activation through Android system settings. The same enabled service provides the no-text screenshot capability through `android:canTakeScreenshot="true"` and `AccessibilityService.takeScreenshot`, so the study's operational data-access consent is centered on the Accessibility Service setup.

4. *Aggregate-Only History*: The usage dashboard displays only aggregate statistics (e.g., "negative content exposure: 45%") rather than storing individual text samples or captured frames.

=== Extraction Failure Handling

This architecture relies on hardcoded UI resource IDs (e.g., `com.zhiliaoapp.musically:id/caption_text`) within the Accessibility Service. Mid-experiment UI updates can restructure the target application's view hierarchy and disrupt extraction. Such breakage is logged as a *reliability event*, and sessions or participant-periods that cannot yield valid extraction output are excluded from the analyses that require those measures.

=== Mindfulness Intervention System

Based on digital well-being and smartphone-habit intervention literature, including recent just-in-time adaptive intervention work on timing, feasibility, and burden management #cite(<roffarello-2021>) #cite(<teepe-2021>) #cite(<ismail-2022>) #cite(<mair-2022>) #cite(<yang-2023>) #cite(<wang-2023>), the system uses a multi-level adaptive intervention approach:

*Level 1 - Awareness Notification:* A non-intrusive toast message appears when the RiskScore enters the lower Warning band, informing the user of their session duration.

*Level 2 - Pause Prompt:* A modal dialog offers the user options to continue, take a break, or view their usage statistics when the RiskScore enters the upper Warning band.

*Level 3 - Breathing Exercise:* A full-screen overlay presents a brief guided breathing animation (study default: 60 seconds) when the RiskScore enters the Critical band. The breathing overlay is treated as a study-defined pause-and-reset prompt rather than as a therapeutic procedure #cite(<mitsea-2023>) #cite(<yang-2023>). It is intended as behavioral interruption only.



For analytic transparency, time spent inside system-generated modals or overlays is excluded from prompt-excluded active-use metrics. When an intervention appears, the ongoing content-view interval is closed before the prompt begins, and a new interval starts only if the user returns to the target platform afterward. Raw elapsed session time and raw elapsed dwell time are retained separately for the primary behavioral comparison in the evaluation phase.

*Usage Dashboard:* Shows aggregated usage and sentiment trends over time to support self-reflection and behavioral awareness. It is treated as a secondary reflective feature rather than as the primary intervention mechanism.

=== Design Justification for Risk-Estimation Variables

This section addresses Research Question 1 by mapping each risk-estimation variable to the Doomscrolling Feedback Loop Model #cite(<sharma-2022>) and to the reviewed literature. Variables were retained only if they were conceptually plausible, literature-supported, and observable through Android without rooting.

#{
  set text(size: 9pt)
  thesis_table(
    caption: [Design Justification Matrix: Theoretical Constructs to System Variables],
    columns: (1.2fr, 1.2fr, 0.9fr, 1.7fr, 0.65fr),
    cell_align: table_align((left, left, center, left, center)),
    pad_x: 0pt,
    header: (
      [*Theoretical Construct*],
      [*Observable Proxy*],
      [*System Variable*],
      [*Data Source*],
      [*Unit*],
    ),
    body: (
      [Behavior: Persistent, obsessive consumption],
      [Prolonged or rapid navigation through content without meaningful pauses],
      [Video Dwell \ Time],
      [AccessibilityService \ (active time between \ TYPE_VIEW_SCROLLED events, \ gated by micro-interaction events)],
      [seconds],

      [Behavior: Repetitive rapid feed navigation],
      [Frequency of moving through items during a session],
      [Swipe Count \ (supporting interaction log)],
      [AccessibilityService \ view-scroll event stream],
      [count],

      [Behavior: Prolonged maladaptive engagement],
      [Extended time on target apps without voluntary breaks],
      [Session \ Duration],
      [Accessibility-derived session tracker \ and app-state events],
      [minutes],

      [Antecedents/Triggers: Negative content exposure],
      [Proportion of consumed content with negative emotional tone],
      [Negative Sentiment \ Density (NSD)],
      [Negative-exposure density from analyzable \ text units and VLM-resolved no-text items],
      [% (0–100)],
    ),
  )
}

*Video Dwell Time* was selected because TikTok and Reels use a discrete, paginated swipe interface rather than continuous scrolling. Dwell time - measured as the duration in seconds between consecutive content-verified transition events — captures how quickly or slowly users move through content. Because the Accessibility Service cannot reliably read exact platform-reported video duration across all target applications, dwell time is treated as a behavioral proxy. *Swipe Count* is retained only as a supporting interaction metric that helps show whether a long session consists of rapid chaining or slower browsing.

*Session Duration* is operationalized as continuous active time on a target application, reflecting the prolonged-engagement dimension reported in doomscrolling-related literature. Together, dwell time and session duration describe how long and how continuously the user remains engaged. They are retained as observable engagement proxies, not as validated biomarkers of doomscrolling.

*Negative Sentiment Density (NSD)* captures the negative-exposure dimension of doomscrolling through a hybrid operationalization. When usable text exists, analyzable caption and visible-comment units are scored through the text path. When no usable text exists, the item is routed through the VLM fallback and contributes one resolved no-text label to the same session-level exposure estimate. High-OOV text is not rerouted into the VLM; if negativity still cannot be resolved reliably, the session enters the 2-input behavioral fallback and is marked sentiment-unreliable for the main sentiment-based analyses.

NSD is therefore an exposure proxy, not a direct measure of distress. It cannot distinguish crisis-related negativity from ordinary negative commentary or represent the full multimodal meaning of viewed media.

*Session Boundary Definition:* A session begins when a target app enters the foreground (detected via `TYPE_WINDOW_STATE_CHANGED`) and ends when: (a) the target app leaves the foreground for longer than a 30-second bridge window, (b) the screen turns off, or (c) the Accessibility Service is terminated. If the user briefly switches away and returns within 30 seconds, the gap is excluded from duration and dwell-time accumulation but the session continues. Recent smartphone-logging studies that reconstruct sessions from app-event streams continue to use 45-second inter-app gaps when direct lock/unlock boundaries are unavailable #cite(<ahmed-2023>) #cite(<chen-2023>). The present system therefore treats 30 seconds as a stricter study-defined session-continuity heuristic within that recent measurement range so only very brief app switches are merged; longer away intervals are treated as new sessions. The post-deployment sensitivity analysis examines ±15-second perturbations alongside the other fixed study priors.

*Idle Timeout Heuristic (Active-vs-Idle Disambiguation):* To prevent false inflation of dwell time when the device is unattended (e.g., phone left on a looping video), the dwell accumulator is treated as *active* only when periodic micro-interactions are observed (e.g., `TYPE_VIEW_CLICKED`, `TYPE_TOUCH_INTERACTION_START/END`, or equivalent interaction signals exposed by the Accessibility layer). If no micro-interaction occurs within a rolling 45-second window, the ongoing dwell interval is paused and marked as idle exposure until interaction resumes or the next swipe occurs. Recent smartphone-session reconstruction studies continue to treat gaps of about 45 seconds as a plausible continuity delimiter when direct boundary data are unavailable #cite(<ahmed-2023>) #cite(<chen-2023>). The dwell tracker uses the same order-of-magnitude value as a study-defined inactivity cap to distinguish active viewing from unattended looping: if interaction signals disappear beyond that range, accumulation pauses until interaction resumes or the next swipe occurs.

$ "NSD" = ("Negative resolvable units" / "Resolvable units in session") times 100 $

To formally represent this calculation in mathematical notation:
$ "NSD" = (limits(sum)_(i=1)^(N_R) II(y_i = 1)) / N_R times 100 $
Where $N_R$ is the number of session units whose negativity can be resolved either from analyzable text or from the no-text VLM path, and $y_i$ is the binary negative label assigned to resolved unit $i$. For text-resolved units, negativity follows the VADER criterion $C_i < -0.05$. For no-text VLM-resolved units, negativity is assigned when the returned visual label is `SEVERE_NEG` or `MILD_NEG`. The indicator function $II$ assigns 1 only to resolved units classified as negative and 0 otherwise. This makes the reported NSD a conservative negative-item density rather than a continuous magnitude-weighted affect score.

The −0.05 threshold follows VADER's standard classification convention, where compound scores below −0.05 are treated as negative sentiment #cite(<hutto-2014>). Caption/comment text units excluded by the language-reliability screen are removed from the text-side evaluation. No-text items routed to the VLM contribute one resolved item-level label each: `SEVERE_NEG` and `MILD_NEG` count as negative exposure, while `NEUTRAL`, `MILD_POS`, and `SEVERE_POS` count as resolved non-negative exposure. High-OOV text is not rerouted into the VLM path. Sessions with very few resolvable units are flagged descriptively during analysis.

Alternative candidates such as swipe-back frequency, time-of-day patterns, and biometric signals were excluded because they either lack enough empirical support in the doomscrolling literature or are technically infeasible within the intended Android monitoring environment. Likewise, although the *internal psychological Antecedents* of the Feedback Loop Model (e.g., anxiety, FoMO) remain conceptually important, they are not measured directly because real-time capture would require intrusive experience sampling or biometric sensors. The system therefore estimates doomscrolling-related risk from one exposure proxy, NSD, together with two behavioral proxies, Video Dwell Time and Session Duration, while swipe or scroll events remain transition signals and supporting logs rather than a separate estimator input.

=== Analytic Scoring Framework
Week-level DSI is computed on one fixed scoring framework across both study weeks so the analytic scale remains constant. Swipe events serve only as transition markers for dwell-time computation, and prompt-response logs are reported descriptively because they exist only in Week 2. Live prompting may personalize Session Duration and NSD memberships after Week 1, but analytic DSI scoring remains fixed across both weeks.

== Hardware and Software Specifications
Development uses Android Studio (Ladybug 2024.2.1+) with Kotlin 1.9+ on a machine with at least 8 GB RAM. The deployable application targets Android 8.0 (API 26) or higher, while the screenshot-assisted no-text VLM path requires Android 11 (API 30) or higher because it uses `AccessibilityService.takeScreenshot`. Local storage uses Room (SQLite), sentiment analysis uses a VADER-compatible lexicon-and-rule engine re-implemented in Kotlin for on-device Android deployment, dashboard visualization uses `MPAndroidChart`, screen-content extraction uses `androidx.accessibilityservice`, and no-text routing uses the Android Accessibility Service together with the on-device Moondream 0.5B VLM/VQA module. A physical Android device is used for field testing.

== Method in Developing Software

=== Methodology Procedure

==== Agile Scrum
Development follows *Agile Scrum* in four iterative one-week sprints covering: (1) requirements finalization and planning, (2) accessibility extraction and local logging, (3) estimator and prompting integration, and (4) testing, refinement, and deployment preparation. Scrum is selected because the Accessibility Service extraction logic requires frequent adaptation as target-app UI structures change, and the fuzzy-logic rule base requires iterative tuning.

=== Algorithm
The core logic of the system combines *Threshold-Based Rules*, *VADER (Valence Aware Dictionary and sEntiment Reasoner)*, the no-text VLM fallback, and *Fuzzy Logic*. Together, these components govern live prompting, session-level scoring, and week-level DSI computation.

*1. Threshold-Based Heuristics:*
For *live prompting*, the system first applies duration-based gates before deeper inference. Recent JITAI and infinite-scroll studies support burden-aware timing and treat sub-10-minute use as relatively brief while placing sustained scrolling in the 10 to 20+ minute range #cite(<teepe-2021>) #cite(<wang-2023>) #cite(<fiedler-2024>) #cite(<hsu-2025>) #cite(<van-genugten-2025>) #cite(<terzimehic-2022>) #cite(<rixen-2023>) #cite(<meinhardt-2025>). Accordingly, the system uses 15 minutes as the study-defined live-prompt gate and cooldown interval.
- *Session Duration Gate:* deeper prompting begins after 15 minutes of continuous use.
- *Intervention Cooldown:* after a prompt is shown, the system requires another 15 minutes of qualifying use before a new prompt can be triggered.

Mean Video Dwell Time still contributes to the fuzzy engine once prompt eligibility is met, but it is not implemented as a separate hard pre-inference gate.

Logged session summaries are retained whether or not a live prompt occurs.

*2. VADER Sentiment Analysis:*
VADER is a lexicon- and rule-based sentiment analysis tool designed for social-media text and suited to on-device use #cite(<hutto-2014>).
- *Input:* Extracted caption or comment text (e.g., "This is heartbreaking 😭").
- *Process:* Tokenization and lexicon lookup.
- *Output:* A Compound Score ranging from -1.0 (Most Negative) to +1.0 (Most Positive). Mathematically, VADER computes this bounded score via the normalization scalar:
  $ C = x / sqrt(x^2 + alpha) $
  Where $x$ is the sum of the valence scores of recognized words, and $alpha$ is the standard normalization constant ($alpha=15$).

The implementation also retains VADER's native UTF-8 emoji valence parsing, so emoji-dominant comments (e.g., "😭😭" or "💀💀💀") still contribute affective signal even when lexical Taglish syntax is highly out-of-vocabulary.

*Filipino Lexicon Extension:* To handle Filipino and Taglish expressions, the system extends VADER with a limited *Minimum Viable Lexicon* (MVL). Recent low-resource sentiment work and lexicon-curation studies support this approach for code-mixed, resource-constrained settings #cite(<tho-2021>) #cite(<mohammed-2023>) #cite(<nazir-2026>) #cite(<wijayanti-2021>).

1. *Corpus Collection:* Before participant deployment, up to 50 high-frequency non-English tokens are compiled from exploratory extraction logs produced during researcher-controlled test sessions or earliest non-study dry runs. The 50-term cap is a study-defined feasibility rule that keeps the lexicon focused on dominant recurrent tokens.
2. *Expert Polarity Rating:* The collected tokens are presented to a panel of three Filipino language teachers and/or linguistics students, who independently rate each token on a sentiment polarity scale from -4 (most negative) to +4 (most positive), following VADER's valence-rating convention.
3. *Inter-Rater Agreement Screening:* A token is retained only if all 3 raters assign values within ±1 of the mean. Recent sentiment-annotation work shows that agreement can be difficult even with explicit instructions, so the ±1 screen is used as a conservative agreement rule for retaining only tokens with comparatively stable local judgments before lexicon integration #cite(<krusic-2024>) #cite(<ayravainen-2025>).
4. *Integration:* The retained tokens and their mean valence scores are appended to the VADER lexicon dictionary at runtime, enabling the sentiment engine to recognize common Filipino expressions such as "nakakalungkot" (sad), "nakakagalit" (infuriating), or "sana all" (envious aspiration).

This approach keeps the vocabulary focused on recurring tokens while preserving annotation quality and computational efficiency for edge deployment.

*3. Taglish Syntax Handling and Fallback Logic:*
The system uses a lightweight lexicon-based approach to keep inference on-device under mobile resource and privacy constraints. Local and low-resource studies support this choice for Philippine social-media text and code-mixed sentiment analysis #cite(<co-2022>) #cite(<cruz-2022>) #cite(<pacol-2021>) #cite(<mohammed-2023>) #cite(<nazir-2026>).

To address negations such as "hindi maganda" ("not good"), the system implements a localized negation heuristic. A predefined list of common Filipino negation markers (e.g., "hindi", "di", "wala", "wag") is integrated into VADER's pre-processing step. If a negation marker is detected within three tokens preceding a recognized sentiment token, the polarity of that token is inverted before the final compound score is computed.

Informal social-media text also contains slang, textual variation, and deliberate misspellings. Unmatched variants remain unmatched and contribute to the OOV calculation rather than being force-mapped into a sentiment label through fuzzy matching.

Dialect-heavy text and purely visual items can still fall outside the reliable text path. The system addresses that case through the no-text VLM fallback:

=== No-Text Sentiment Fallback via Moondream 0.5B

Short-form video environments can include purely visual items without accompanying text payloads. For that case, the system uses a *Per-Video Routing Heuristic* that calls a VLM fallback built on Moondream 0.5B. Moondream 0.5B is used as a small deployment-fit VLM for on-device edge inference #cite(<sharshar-2025>) #cite(<lee-2024>).

- *Dynamic Routing Logic:* When transitioning to a new item in the feed, if the Accessibility Service successfully extracts usable text payload (caption or visible comments), the item is evaluated via VADER and routed into the text calculation tier. If no usable text is present (e.g., purely visual content without captions), the item enters the Accessibility Service screenshot plus Moondream 0.5B path. High-OOV text is not rerouted into the VLM path; it is handled through the same reliability safeguard that triggers the fallback mode.
- *Data Extraction via `AccessibilityService.takeScreenshot`:* Visually routed items use an on-demand screenshot after the new item has settled. This is a resource-aware compromise supported by related affective-video and privacy-compliant video analysis work showing that sparse visual evidence can support practical inference under resource constraints #cite(<sharma-2023>) #cite(<augusma-2023>) #cite(<qu-2025>). To maintain the edge-computing privacy boundary, the captured frame exists only in volatile RAM for VLM inference, ensuring no user screen data is committed to persistent storage.
- *VQA Zero-Shot Computation:* The transient screenshot is processed entirely on-device by the Moondream 0.5B VLM/VQA module. The system uses constrained Visual Question Answering (VQA), and the no-text item is assigned one of five labels: `SEVERE_NEG`, `MILD_NEG`, `NEUTRAL`, `MILD_POS`, or `SEVERE_POS`. If the label is `SEVERE_NEG` or `MILD_NEG`, the item contributes one negative resolved unit to NSD; otherwise it contributes one resolved non-negative unit.

This route extends NSD coverage to no-text items while remaining within the device's privacy boundary. It remains an item-level visual classification path rather than a full multimodal interpretation of viewed media #cite(<das-2023>) #cite(<wei-2021>) #cite(<zhang-xu-2023>) #cite(<sharshar-2025>) #cite(<cortinas-lorenzo-2024>) #cite(<johnson-2025>).

*Safety Lock:* If the no-text VLM path fails because `AccessibilityService.takeScreenshot` is unavailable, a screenshot cannot be captured, or VLM inference does not produce a stable item label, the system drops to 2-input fuzzy inference (Video Dwell Time + Session Duration only). Sessions dominated by high-OOV text follow the same fallback path. In either case, unresolved sessions are marked *Sentiment-Unreliable* and excluded from main Doomscroll Severity Index (DSI) statistical evaluation. The degraded rule base and prompt-mapping thresholds are detailed in Appendix B.

Operationally, each viewed item stays on the text path when usable text exists and moves to the VLM fallback only when no usable text exists. If the session-level OOV ratio reaches 50% or more, or if no-text resolution fails, the session is marked *Sentiment-Unreliable*: the sentiment input is dropped, inference proceeds on Video Dwell Time and Session Duration only, Level 3 prompts are disabled, and the session is excluded from main DSI computation. The 50% OOV screen functions as a study-defined reliability rule, and the study reports the number and proportion of sentiment-unreliable sessions to show DSI coverage.

*4. Fuzzy Logic Inference System:*
The system uses fuzzy logic because the target construct is gradual, the rule base must remain inspectable for SME review, and labeled session data are not available for supervised on-device modeling #cite(<vashishtha-2023>) #cite(<pickering-2025>). The fuzzy engine operates at the per-session inference level, while the statistical tests in Chapter 4 are used later for sample-level evaluation. The membership boundaries below define the fixed scoring framework for week-level DSI across both study weeks. In Week 2 live prompting, Session Duration and NSD memberships may be personalized from Week 1 sentiment-reliable sessions, while Video Dwell Time remains fixed #cite(<ikegaya-2025>).

- *Fuzzification:* Crisp inputs (Video Dwell Time in s, Negative Sentiment Density in %, Session Duration in min) are converted into fuzzy sets using triangular membership functions. Mathematically, a triangular membership function with bounds $(a, b, c)$ is defined as:
$ mu_(A)(x) = cases(
  0 &"if" x <= a,
  (x-a)/(b-a) &"if" a < x < b,
  (c-x)/(c-b) &"if" b <= x < c,
  0 &"if" x >= c
) $
With three input variables and three linguistic states per variable (Low, Medium, High), the rule base contains $3^3 = 27$ rules. The table below presents the membership boundaries used for analytic scoring:

  #thesis_table(
  caption: [Fuzzy Set Membership Boundaries for Analytic Scoring],
  columns: (1.45fr, 1fr, 1fr, 1fr),
  cell_align: table_align((left, center, center, center)),
  header: (
    [*Variable*],
    [*Low*],
    [*Medium*],
    [*High*],
  ),
  body: (
    [Video Dwell Time (s)], [0–5], [4–20], [15–30+],
    [Negative Sentiment Density (%)], [0–33], [17–83], [67–100],
    [Session Duration (min)], [0–10], [8–20], [15–40+],
  ),
)

These boundaries combine recent app-use timing evidence with a regular three-part partition for NSD, where the literature does not provide standard per-session negativity thresholds #cite(<cho-2021>) #cite(<tian-2021>) #cite(<muise-2024>) #cite(<terzimehic-2022>) #cite(<meinhardt-2025>) #cite(<ismail-2022>) #cite(<azam-2021>) #cite(<khairuddin-2021>) #cite(<casalino-2022>). They serve as the pilot scoring structure for this estimator. Session Duration and Negative Sentiment Density act as the main risk axes, while Video Dwell Time mainly adjusts risk by showing how sustained attention is distributed across viewed items.

The table reports support intervals rather than every triangular parameter. In implementation, each Low set is a left-shoulder triangle anchored at zero, each Medium set peaks at the midpoint of its displayed support interval (12 s, 50%, and 15 min), and each High set is a right-shoulder triangle that saturates at the upper cap (30 s, 100%, and 40 min). This setup was chosen because it is the simplest piecewise-linear form that matches the declared Low/Medium/High meanings, keeps adjacent overlap without gaps, prevents overlap between non-adjacent sets, and keeps every parameter easy to check against the table.

The boundaries are interpreted as follows:

- *Video Dwell Time:* Low (0–5 s) represents immediate skip or near-skip behavior, while High (15–30+ s) represents clearly sustained attention to a single item.
- *Negative Sentiment Density:* Low, Medium, and High are defined through an evenly distributed three-term partition over the 0-100% NSD scale because the literature does not provide standard per-session negativity thresholds.
- *Session Duration:* Low (0–10 min) represents brief checking, while High (15–40+ min) represents clearly extended sessions.

The rule base is arranged so that increasing Session Duration should not lower risk when the other inputs are fixed, and increasing Negative Sentiment Density should not lower risk. Increasing Video Dwell Time generally raises risk because it reflects more sustained attention to a single item, but that effect is weaker and more context-dependent than the duration and negativity axes. The resulting rule surface is therefore mostly monotonic rather than strictly monotonic.

These ranges are retained as the fixed analytic scoring framework for week-level DSI across both study weeks and as the Week 1 live default. After the 7-day baseline window, if at least 10 sentiment-reliable sessions are available, the live prompt engine updates Session Duration and NSD memberships from that participant's Week 1 $Q_(25)/Q_(50)/Q_(75)/Q_(95)$ quantiles; otherwise the default memberships are retained. The >= 10 threshold is used to avoid unstable personalization from sparse baseline data. Video Dwell Time remains fixed in both analytic and live modes.

*Sensitivity Analysis of Fuzzy Boundaries:* Post-deployment, the study conducts a one-at-a-time local sensitivity analysis of the nine boundary parameters by varying each parameter across a pre-specified plausibility range while the others are held constant. For each perturbation, the analysis reports: (1) mean absolute score change, (2) percentage of sessions reclassified (Safe/Warning/Critical), and (3) which parameters produce the largest output instability under the tested range #cite(<dogan-2021>) #cite(<vinogradova-zinkevic-2023>) #cite(<shahari-2024>) #cite(<shukla-2025>).

- *Rule Evaluation:* The system evaluates a rule base of 27 rules (3 variables × 3 levels each = 3³ combinations). *Safe* is assigned to clearly low-risk states and isolated mild signals. *Warning* is assigned to mixed or moderate states without strong multi-axis convergence. *Critical* is assigned when prolonged use and negative exposure converge, with Video Dwell Time acting mainly as an intensifier. The rule assignments follow these principles:

  - every Low/Medium/High combination is assigned exactly once, so the engine has no undefined corner cases
  - no rule is *Critical* when Session Duration and Negative Sentiment Density are both Low
  - prolonged duration alone does not produce *Critical* when Negative Sentiment Density remains Low
  - High NSD is escalated most strongly when at least one other axis is also non-low, especially Session Duration
  - High Dwell Time alone is insufficient for *Critical* and mainly intensifies already concerning states
  - *Warning* remains the default mixed class whenever the evidence is concerning but not yet strongly convergent

Read in groups, Rules 1, 2, 4, 10, and 19 are assigned *Safe* because at least two axes remain low. Rules 3, 5-8, 11-14, 16, and 20-22 are assigned *Warning* because they show one elevated axis or mixed evidence without strong duration-negativity convergence. Rules 9, 15, 17-18, and 23-27 are assigned *Critical* because prolonged use and negative exposure converge, or because High NSD is reinforced by High Dwell Time even when the total session is still short.

Rule 15 (Medium Dwell, Medium NSD, High Duration) is classified as *Critical* because the session is already prolonged and both other axes are non-low. Rule 23 (High Dwell, Medium NSD, Medium Duration) is also classified as *Critical* because sustained attention and non-low negative exposure already converge in a session that is no longer brief. Rule 25 (High Dwell, High NSD, Low Duration) reflects the same logic in concentrated form: even a short session can show concentrated negative immersion when viewed items are mostly negative and each item holds attention for a relatively long time.

Representative rules include:

#thesis_table(
  caption: [Representative Appendix A Rules (6 of 27)],
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
    [14], [Medium], [Medium], [Medium], [Warning],
    [27], [High], [High], [High], [Critical],
    [26], [High], [High], [Medium], [Critical],
    [9], [Low], [High], [High], [Critical],
    [19], [High], [Low], [Low], [Safe],
  ),
)

The complete 27-rule inference base is presented in Appendix A.

*Exception Logic:* Most rules are arranged so that higher Session Duration or higher Negative Sentiment Density does not reduce risk, but one deliberate exception is retained for interpretive accuracy. Appendix A Rule 9 (Low Dwell, High NSD, High Session Duration) is classified as *Critical* because low dwell should not automatically downgrade a session that is both prolonged and dominated by negative items; in that edge case, rapid chaining across many negative items is treated as more concerning than reassuring. By contrast, Appendix A Rule 19 (High Dwell, Low NSD, Low Session Duration) is not treated as an exception but as a boundary clarification: sustained attention to a single item, without prolonged session time or negative-dominant exposure, is not enough by itself to indicate doomscrolling. This is why the table treats dwell time as a modifier, not the main driver.

- *Defuzzification:* The aggregated fuzzy output is converted back into a crisp "Risk Score" (0–100) using the Center of Gravity (CoG) method. The three output classes occupy equal thirds of the reporting scale, with category cutoffs at 33.33 and 66.67 and output centers at 16.67, 50.00, and 83.33 for Safe, Warning, and Critical, respectively. Formally, this computation involves rule activation weight ($w_i$) following the MIN T-norm, and output defuzzification:

$ w_i = min(mu_("Dwell")(x), mu_("NSD")(y), mu_("Duration")(z)) $
$ "Risk Score" = (limits(sum)_(i=1)^n w_i dot c_i) / (limits(sum)_(i=1)^n w_i) $

Where $w_i$ is the activation weight of rule $i$, and $c_i$ is the crisp output center for the mapped risk class. For each fired rule, the smallest membership value determines the rule's activation weight. This prevents one strong input from fully dominating when the other conditions required by that rule remain weak.

*Dynamic Weighting vs. Static Percentages:* The adopted MIN-based fuzzy inference does not assign flat percentage weights to individual variables. Each fired rule is limited by its weakest required antecedent, so a variable's influence depends on the full combination of inputs rather than on a fixed linear contribution.

*Illustrative Worked Example:* Given the input tuple (VideoDwellTime = 45 s, NegativeSentimentDensity = 62%, SessionDuration = 35 min), the system computes Video Dwell Time High membership = 1.0, Negative Sentiment Density Medium membership ≈ 0.64, and Session Duration High membership = 0.80. The dominant firing pattern is Appendix A Rule 24 (High Dwell Time, Medium NSD, High Duration → Critical), so the defuzzified Risk Score is 83.33/100 and the resulting intervention level is Level 3 (Critical).

== Method in Evaluation of the Study
The study uses four complementary evaluation methods: the *ISO/IEC 25010 Software Quality Model* for technical quality assessment, TAM for user acceptance evaluation, an SME panel for expert review, and a baseline-intervention comparison of selected logged metrics and self-reported doomscrolling scores. Results are reported separately as *operational feasibility* (successful deployment completion and absence of critical system failures), *software acceptability* (ISO/IEC 25010, TAM, and SME results), and *estimator plausibility* (baseline convergent association together with sentiment-reliable coverage).

=== ISO/IEC 25010 Software Quality Evaluation

The study specifically focuses on four key characteristics of the ISO/IEC 25010 model #cite(<iso-25010-2023>) relevant to privacy-sensitive mobile well-being applications:

1. *Functional Suitability:* Assessing whether the heuristic risk-state estimation and intervention features function as intended and cover the specified requirements.
2. *Performance Efficiency:* Evaluating the application's resource usage through both user survey responses and objective profiling metrics such as CPU usage, RAM consumption, and battery drain observed during controlled testing on a small reference-device set, with field-deployment logs used only as descriptive context.
3. *Usability:* Measuring the ease of use, learnability, and user satisfaction with the interface and intervention mechanisms.
4. *Reliability:* Assessing the system's stability and availability during extended periods of monitoring without crashing or being killed by the Android OS.

=== Technology Acceptance Model (TAM) Evaluation

To assess user acceptance, the study uses the Technology Acceptance Model (TAM). Recent mHealth acceptability reviews continue to treat TAM as a practical user-acceptance framework, with Perceived Usefulness and Perceived Ease of Use retained as core constructs #cite(<adnan-2025>). Two TAM constructs are measured:

1. *Perceived Usefulness (PU):* The degree to which users believe the system enhances their ability to manage doomscrolling behavior.
2. *Perceived Ease of Use (PEOU):* The degree to which users believe the system is free of effort to learn and operate.

Each construct is measured using 6 Likert-scale items aligned to these two core TAM constructs and adapted to the doomscrolling risk-estimation context. The TAM instrument is administered in the Phase 5 post-usage survey.

=== Subject Matter Expert (SME) Evaluation

A panel of *three (3) subject matter experts* evaluates the system's technical quality and risk-estimation logic. The SME panel is composed of:
- One (1) expert in software engineering or mobile application development
- One (1) expert in data science, machine learning, or fuzzy logic systems
- One (1) expert in digital well-being, behavioral psychology, or educational technology

Each SME evaluates the system using a structured rubric covering: (1) the technical soundness of the hybrid risk-estimation framework, including VADER integration and fuzzy inference behavior, (2) the plausibility of the selected input ranges for Video Dwell Time, Negative Sentiment Density, and Session Duration, (3) the internal coherence of the 27-rule base, including whether nearby rules change in a sensible direction across duration and negative exposure and whether exception rules such as rapid negative-content chaining are reasonable, (4) the quality of the system architecture and privacy implementation, (5) the appropriateness of the intervention design, and (6) overall software quality using the same ISO/IEC 25010 characteristics evaluated by end users. SME responses are collected using a 5-point Likert scale and analyzed descriptively (mean, standard deviation) alongside qualitative feedback. This SME review serves as narrative expert appraisal of the rule logic and as an expert plausibility check, not as formal empirical calibration or a content-validity index.

=== Testing Method
The study uses both *black box* and *white box* testing. Black-box testing checks whether visible functions behave as expected under realistic use, while white-box testing checks whether the internal scoring, routing, and data-flow logic behave correctly at code and module level.

==== Black Box Testing Procedure
The black-box procedure evaluates the application through expected inputs, user actions, and observed outputs without relying on internal code inspection. The full application is deployed on a small set of physical devices representing the screen sizes and Android versions available to the researchers to verify that usage patterns (e.g., prolonged session with high negative content) produce the expected risk classification and intervention, including correct routing of no-text cases into the VLM fallback and correct degradation to the 2-input fallback when sentiment remains unresolved. The same procedure checks user-visible flows such as onboarding, permission handling, dashboard display, logging continuity, and prompt presentation.

==== White Box Testing Procedure
The white-box procedure evaluates the internal logic and data flow of the system. The Kotlin-implemented VADER-compatible scoring engine and fuzzy-logic rules are tested in isolation using JUnit to verify that known inputs produce expected outputs. The Accessibility Service-to-database pipeline is also tested to confirm correct data flow and that interventions trigger from logged state changes. This same layer verifies text-path routing, no-text VLM routing, and expected fallback to the 2-input safety mode when sentiment remains unresolved.

== Data Collection Scheduling
Data collection follows five sequential phases. Phase 1 covers recruitment, screening, consent, and group assignment. Phase 2 covers baseline profile completion and application setup. Phase 3 covers the one-week baseline logging period with prompts disabled. Phase 4 covers the one-week deployment period in which only the intervention group receives adaptive prompts while the control group continues logging-only. Phase 5 covers the post-usage survey and final retrieval of aggregate local logs.

== Data Gathering
Data gathering is conducted through five phases:

*Phase 1: Recruitment, Screening, and Consent*
Potential participants are recruited through non-probability purposive-convenience sampling using accessible channels such as academic networks, peer referrals, and online groups. Interested individuals undergo screening to confirm eligibility based on age, active use of at least one target short-form video platform, and study-device compatibility, including whether the phone can install the study build, maintain the required permissions, and keep the monitoring service active under ordinary use. Eligible participants are then provided with an informed consent form explaining the study's purpose, procedures, data to be collected, risks, safeguards, and right to withdraw. After consent and eligibility confirmation, participants are assigned to either the intervention group or the logging-only control group using the concealed permuted-block allocation list described earlier, with an initial 1:1 allocation target of 25 per group. Both groups receive the same baseline setup and Week 1 logging procedure; the distinction becomes operational only in Week 2 when prompts are enabled for the intervention group.

*Phase 2: Baseline Profile Form and Application Setup*
Prior to deployment, each participant completes a baseline profile form capturing demographic information, estimated short-form video use, and general platform-use habits. The participant is then assisted in installing the application and enabling the necessary Android permissions. Each participant is assigned a unique study code used to link logs and survey forms; the name-to-code list is retained separately by the researchers. This form provides contextual information but is not treated as the primary source of behavioral measurement.

Week 1 provides the fixed-prior baseline DSI and self-report anchor. At the end of the 7-day baseline, the live prompt engine may lock participant-specific Session Duration and NSD memberships from Week 1 sentiment-reliable sessions while leaving Video Dwell Time fixed. This follows recent JITAI evidence favoring personalized intervention criteria over uniform ones for live prompting #cite(<ikegaya-2025>). If reliable baseline coverage is insufficient, the default priors are retained.

*Phase 3: Week 1 Baseline Logging Phase*
During Week 1, the app's built-in logger records quantitative usage metrics locally on the participant's device while adaptive intervention prompts remain inactive. This baseline period establishes the participant's observed usage pattern under the study conditions and provides the baseline DSI and self-report anchor used for convergent association testing.

The Week 1 logged data include:
- *Usage Metrics:* Session duration, video dwell time, and supporting interaction counts such as swipe totals.
- *Sentiment-Related Indicators:* Session-level sentiment scores and Negative Sentiment Density estimates.
- *Reliability Events:* Application crashes, service interruptions, and related system-stability notes.
- *Performance Indicators:* CPU, RAM, and battery observations gathered through limited in-app logging and controlled profiling on a small reference-device set, with heterogeneous participant-device traces used descriptively.

At the conclusion of Week 1, participants complete the short-form 4-item *Doomscrolling Scale* #cite(<sharma-2022>) #cite(<satici-2023>), using Items 1, 2, 10, and 12 of the original instrument and a 7-day recall instruction stem (e.g., "In the past week…"; see Appendix D), to provide the baseline self-report anchor for convergent association. The original instrument was validated as a general self-report scale, so changes between administrations are interpreted as short-horizon self-report differences.

*Phase 4: Week 2 Deployment Phase*
During Week 2, *intervention-group* participants receive adaptive digital mindfulness prompts through a live prompt engine that retains the fixed rule base and fixed Video Dwell Time memberships but, when available, replaces the global Session Duration and NSD memberships with participant-specific Week 1 quantile-derived bounds. *Control-group* participants continue logging-only operation with prompts disabled via a configuration flag. Both groups continue to have usage and sentiment-related metrics logged, while prompt-response data for the intervention group are recorded descriptively.

For behavioral comparison, raw elapsed session duration and raw elapsed video dwell time are treated as the primary time-based metrics. *Prompt-excluded active-use metrics* are also retained: when a prompt appears, the active content-view interval is closed, and time spent inside the system-generated intervention is excluded until the participant returns to the target platform. These prompt-excluded metrics are reported as supplementary traces of prompt-interrupted use. Observed Week 2 differences are interpreted as short-term behavioral differences under the study conditions.

Potential confounds include time-of-day and day-of-week variation, external events during the two-week window, and device-specific differences in processing speed or background-process management. The randomized two-group design helps balance these factors across study arms, while the within-subject Week 1 to Week 2 comparison in the intervention group accounts for stable individual differences.

At the conclusion of Week 2, participants complete the same short-form 4-item *Doomscrolling Scale* under the same 7-day recall instruction stem (Appendix D) to provide the post-intervention self-report anchor for paired comparison.

*Phase 5: Post-Usage Survey and Final Data Retrieval*
To align data structures for baseline convergent association and descriptive week-level comparison, logged session summaries over each 7-day period are aggregated into a composite *Doomscroll Severity Index* per user per week. This analytic DSI uses the same fixed-prior scoring framework across both study weeks. Unlike the live Week 2 prompt engine, the DSI engine is not personalized after Week 1, so week-level summaries remain on one fixed scale:

$ "DSI"_w = (1 / m_w) sum_(s=1)^(m_w) "RiskScore"_s $

where $m_w$ is the number of week-$w$ sessions classified as sentiment-reliable and therefore doomscrolling-evaluable. Sessions flagged as *Sentiment-Unreliable* are excluded from DSI computation. DSI is a mean of retained session scores rather than a cumulative sum, so these exclusions affect coverage more than scale. Week-specific DSI values are retained for descriptive comparison, convergent testing is anchored to the baseline week only, and DSI coverage is reported by week and study arm in terms of retained participants, retained sessions, and excluded sentiment-unreliable sessions.

*Interpretation of Classification Metrics:* Traditional supervised classification metrics (e.g., precision, recall, F1, AUROC) are not estimated because session-level ground-truth labeling would require interrupting active scrolling. The study therefore uses convergent association as the main plausibility check and interprets the system as a heuristic behavioral risk estimator.

After the deployment period, participants complete a structured post-usage survey instrument with three components: (a) the ISO/IEC 25010 software quality evaluation, (b) the TAM assessment, and (c) open-ended feedback items. At the same stage, aggregate local logs are exported using the participant study code only and transferred to a password-protected research folder accessible only to the researchers.

For the ISO/IEC 25010 component, the survey uses a 5-point Likert Scale to evaluate the four target quality characteristics: Functional Suitability, Performance Efficiency, Usability, and Reliability. For the Usability sub-characteristic, all 10 items of the System Usability Scale (SUS) are adopted verbatim, and the standard SUS scoring formula is applied to yield a composite usability score on a 0–100 scale. The SUS is a standardized usability measurement tool whose validity for digital health applications has been supported by a meta-analysis of 114 apps yielding a benchmark mean of 68.05 (SD 14.05) #cite(<hyzy-2022>). For the remaining three sub-characteristics (Functional Suitability, Performance Efficiency, and Reliability), the study uses researcher-developed items derived from the ISO/IEC 25010 sub-characteristic definitions #cite(<iso-25010-2023>) and refined through pre-deployment expert review.

For the TAM component, 12 Likert-scale items (6 for Perceived Usefulness, 6 for Perceived Ease of Use) aligned to the study's contextualized TAM constructs are included to assess user acceptance of the system.

=== Instrument Review and Reliability Safeguards

Before deployment, the combined survey instrument undergoes structured expert content review to check item relevance, clarity, redundancy, and alignment with the study constructs. Reviewer comments are consolidated, and unclear or overlapping items are revised before administration.

After data collection, internal consistency for each multi-item sub-scale is estimated through Cronbach's alpha and interpreted as an index of item coherence, with appropriate caution given the pilot sample size and the mix of standardized and adapted items.

Open-ended feedback items are used as descriptive support for the survey findings. Two researchers independently review the responses, reconcile coding differences through discussion, and report recurring issue categories.

The alignment between the study's research variables, data sources, and instruments is summarized in the following table.

#thesis_table(
  caption: [Variable–Instrument Alignment Matrix],
  columns: (1.2fr, 1.15fr, 1.55fr, 0.8fr),
  cell_align: table_align((left, left, left, center)),
  header: (
    [*Research Variable*],
    [*Data Source*],
    [*Instrument / Tool*],
    [*Phase*],
  ),
  body: (
    [Baseline usage profile], [Self-report / Logs], [Baseline profile form + Week 1 logs], [Phase 2 & 3],
    [Session Duration], [Automated system logs], [Built-in application logger], [Phase 3 & 4],
    [Video Dwell Time (s)], [Automated system logs], [Built-in application logger], [Phase 3 & 4],
    [Negative Sentiment Density (%)], [Automated system logs], [VADER text-path + VLM no-text path + Built-in logger], [Phase 3 & 4],
    [Prompt-response profile (descriptive)], [Automated system logs], [Built-in application logger], [Phase 4],
    [Pre/Post Doomscrolling Scale Score], [Self-report], [Short-form 4-item Doomscrolling Scale (Appendix D)], [End of Phase 3 & 4],
    [Baseline convergent association],
    [Week 1 DSI + Week 1 Doomscrolling Scale],
    [Bivariate Correlation (Pearson's r / Spearman's rho)],
    [End of Phase 3],
    [Functional Suitability], [Likert-scale survey], [Researcher-developed ISO/IEC 25010 items + expert review], [Phase 5],
    [Performance Efficiency (subjective)],
    [Likert-scale survey],
    [Researcher-developed ISO/IEC 25010 items + expert review],
    [Phase 5],
    [Performance Efficiency support (objective profiling)],
    [Profiling records / logs],
    [Controlled profiling + field-deployment metrics],
    [Phase 3 & 4],
  ),
)

#continued_thesis_table(
  caption: [Table 5. Variable–Instrument Alignment Matrix (continued)],
  columns: (1.2fr, 1.15fr, 1.55fr, 0.8fr),
  cell_align: table_align((left, left, left, center)),
  header: (
    [*Research Variable*],
    [*Data Source*],
    [*Instrument / Tool*],
    [*Phase*],
  ),
  body: (
    [Usability], [Likert-scale survey], [System Usability Scale (SUS), benchmarked in #cite(<hyzy-2022>, form: "prose")], [Phase 5],
    [Reliability], [Likert-scale survey], [Researcher-developed ISO/IEC 25010 items + expert review], [Phase 5],
    [Perceived Usefulness (TAM)], [Likert-scale survey], [Contextualized TAM PU items], [Phase 5],
    [Perceived Ease of Use (TAM)], [Likert-scale survey], [Contextualized TAM PEOU items], [Phase 5],
    [Expert quality assessment], [SME evaluation], [Structured rubric (ISO 25010 + risk-estimation logic)], [SME],
  ),
)

== Respondents of the Study and Sampling Technique
The respondents of this study are Filipino Android users aged 18 years and above who actively use at least one of the target short-form video platforms. The findings apply to this target population and to the non-probability nature of the sample. The sampling technique employed is *purposive-convenience sampling* because participants must meet specific technical and behavioral eligibility criteria and be reachable through channels accessible to the researchers.

The study reports how many participants used TikTok, Facebook Reels, and Instagram Reels during the field evaluation, including platform mix by study arm. Per-platform extraction success rates and analyzable-session counts are also reported, and cross-platform interpretation is limited to platforms that yielded stable extraction throughout the deployment window.

*Inclusion Criteria:*
- Must be aged 18 years or older to provide independent informed consent.
- Must own an Android smartphone (Version 8.0 or higher) that passes study-compatibility screening for installation, permissions, and background-service stability.
- Must be an active user of at least one target short-form video platform (TikTok, Facebook Reels, or Instagram Reels).
- Must consent to the installation of the application and the collection of usage data.

*Sample Size Justification:*
A minimum of *N = 50* participants is targeted, split as evenly as possible into an intervention group and a control group, with an initial allocation target of *n = 25* per group. This target supports a balanced pilot deployment and post-usage evaluation within an undergraduate thesis scope; it is not intended as a fully powered efficacy sample.

Analyses that exclude sentiment-unreliable sessions or participants with insufficient baseline data report their effective sample sizes separately. With a minimum sample of about 50 participants, between-group estimates are interpreted with emphasis on effect sizes and 95% confidence intervals alongside p-values.

*Selection Bias Acknowledgment:*
Purposive-convenience sampling may overrepresent users who are already receptive to self-regulation tools. This self-selection tendency can affect perceived acceptance and compliance estimates.

== Statistical Treatment
Research Question 1 is addressed descriptively through the design-and-development sections earlier in this chapter and is not treated as an inferential hypothesis test. The evaluation data are analyzed using the following statistical methods, each selected for a specific data source and objective.

*1. Descriptive Summaries (Survey Data — ISO 25010 & TAM):*
The Likert-scale responses from the ISO/IEC 25010 software quality evaluation survey and the TAM instrument (Perceived Usefulness and Perceived Ease of Use subscales) are summarized using *mean scores* and standard deviation.

Mean scores are described on a 5-point Likert scale, where 3.00 represents the neutral midpoint. For evaluative decisions, the study uses pre-set favorable thresholds of >= 3.50 for ISO/IEC 25010 and TAM constructs and >= 4.00 for SME ratings. These are study-defined favorable benchmarks rather than universal cutoffs.

*2. Objective Acceptance Thresholds for Evaluation:*
To reduce post-hoc rationalization of evaluation results, the following pre-set interpretation thresholds are established as decision aids for Chapter 4 rather than as stand-alone proof of software success:
- *System Usability Scale (SUS):* A composite score of >= 70 is treated as the study's usability target, set slightly above the 68.05 digital-health benchmark mean reported by #cite(<hyzy-2022>, form: "prose").
- *ISO/IEC 25010 Subscales:* A mean score of >= 3.50 out of 5 is treated as the study-defined favorable-evaluation target for Functional Suitability, Performance Efficiency, and Reliability.
- *TAM Constructs:* A mean score of >= 3.50 is treated as the study-defined favorable-evaluation target for Perceived Usefulness and Perceived Ease of Use.
- *SME Evaluation:* A mean rating of >= 4.00 out of 5 is treated as the study-defined favorable-evaluation target for expert review.

Objective CPU, RAM, and battery observations are summarized descriptively across the deployment and controlled profiling sessions. Given device differences in hardware and manufacturer background-process behavior, these profiling results are interpreted as supporting evidence for Performance Efficiency instead of as a uniform decision criterion.

Results below these thresholds are reported as unmet software-acceptability targets in Chapter 4. Operational feasibility and estimator plausibility are reported separately. Given the pilot-sized sample and the possibility of analyzable-sample reduction from sentiment-unreliable sessions, inferential results are interpreted primarily through effect sizes, confidence intervals, and coverage rather than pass-fail significance alone.

*3. Cronbach's Alpha (Instrument Reliability):*
To assess internal consistency, *Cronbach's alpha* ($alpha$) is computed for each sub-scale (Functional Suitability, Performance Efficiency, Usability, Reliability, PU, PEOU). A pre-set coefficient threshold of $alpha >= 0.70$ is adopted as a conventional descriptive benchmark for this study. Given the pilot sample and the mix of standardized and adapted items, alpha is treated as a reliability check rather than as a stand-alone validation claim.

*4. Primary Behavioral Analysis:*
The main quantitative comparison for Research Question 2 uses an *Independent Samples t-test on change scores* (Week 2 minus Week 1) to compare observed short-term behavioral differences between the intervention group and the control group. Using change scores rather than raw Week 2 values adjusts for chance baseline imbalance and individual differences. As a secondary within-subject analysis, a *Paired Samples t-test* compares Week 1 and Week 2 within the intervention group. Given the pilot sample, these tests are treated as exploratory analyses that estimate short-term observed differences under the study conditions rather than precise intervention-effect magnitudes.
Separate primary tests are conducted for:
- Mean daily *raw elapsed* session duration (minutes)
- Mean daily *raw elapsed* video dwell time (s)
- Mean daily Negative Sentiment Density (%)
- Pre/Post Doomscrolling Scale Score (Self-Reported)

For the Negative Sentiment Density outcome, mean daily NSD is computed only from sentiment-reliable sessions. Days or participant-weeks with no sentiment-reliable sessions contribute missing data for that specific outcome and are excluded only from that specific NSD comparison, with the analyzed $n$ reported.

As a supplementary transparency check, the same tests are also conducted on *prompt-excluded active-use metrics* for session duration and video dwell time. The difference between raw elapsed and prompt-excluded results is reported to show the measurement-rule effect.

To control family-wise Type I error across the four change-score comparisons, p-values are adjusted using the *Holm-Bonferroni sequential correction* at an initial family-wise $alpha = 0.05$. For each comparison, *Cohen's d* and its 95% confidence interval are reported alongside the Holm-adjusted p-value to support interpretation of effect size, not statistical significance alone.

*Handling of Assumption Violations:*
For the primary independent-samples t-test, normality of change scores within each group is checked with Shapiro-Wilk and equality of variances with Levene's test. If normality is severely violated, the *Mann-Whitney U test* is used as fallback. If variances are unequal but normality holds, Welch's t-test is applied. For the secondary paired t-test, normality of within-subject differences is checked with Shapiro-Wilk; if violated, the *Wilcoxon Signed-Rank Test* is used.
If analyzable sample sizes become too small for stable inferential interpretation after outcome-specific exclusions, the affected comparison is reported descriptively with its effective $n$ and directional pattern rather than being overstated as confirmatory evidence.

*5. Descriptive Analysis of Prompt-Response Logs:*
Week 2 prompt-response data (e.g., continue, dismiss, take-break) are summarized using frequency counts and percentages. These logs are treated as descriptive evidence of intervention interaction only and are not analyzed as a baseline-comparable outcome variable.

*6. Baseline Convergent Association Analysis:*
Baseline convergent association is assessed on *Week 1 data only*. First, a component-wise correlation table reports the bivariate association between each individual input variable (mean daily session duration, mean daily video dwell time, mean daily NSD) and the baseline Doomscrolling Scale score. Second, the bivariate correlation between the full composite DSI (computed with fixed priors) and the Doomscrolling Scale is computed. The pattern of component-wise versus composite correlations is discussed descriptively to assess whether the multi-variable composite associates more strongly with self-reported doomscrolling than session duration alone.

Only participants with at least three sentiment-reliable sessions in Week 1 are included; the effective sample size ($N_"effective"$) is reported. This coverage rule follows recent digital-measure practice for forming week-level aggregates from repeated observations, with three observations commonly used as a pragmatic weekly minimum while stronger reliability claims require more data #cite(<yao-2021>) #cite(<meyer-2022>) #cite(<ratitch-2023>) #cite(<buekers-2025>). Here, the >= 3 rule functions as a pragmatic floor for the baseline aggregate. *Pearson's $r$* is used when linearity and normality assumptions hold; otherwise *Spearman's $rho$*. Results are interpreted as a baseline plausibility check rather than as diagnostic validation.

*7. Interpretation:*
For these exploratory pilot analyses, the nominal level of significance is set at $alpha = 0.05$. Statistically significant differences between Week 1 and Week 2 are interpreted as observed short-term behavioral differences under the study conditions. The two-group randomized design supports stronger inference than a one-group pre/post comparison, but findings are not interpreted as long-term intervention effects or calibrated validation. Prompt-excluded active-use metrics are interpreted separately because they incorporate the prompt-display rule.

*8. Descriptive Coding of Open-Ended Feedback (Phase 5):*
Open-ended responses from the post-usage survey are analyzed through a light inductive coding procedure. Two researchers read the responses in full, group them into recurring issue categories, and reconcile coding differences through discussion before the categories are summarized. The goal is to identify usability issues, perceived intervention value, and implementation constraints not captured by the Likert-scale measures. This step supports interpretation of the survey results and surfaces recurring concerns for Chapter 4 discussion.

*9. Missing Data and Exclusion Criteria:*
For the convergent correlation analysis, participants need at least three sentiment-reliable sessions in Week 1 ($m_1 >= 3$) for Week 1 DSI to enter the correlation dataset. This rule includes the edge case $m_1 = 0$, where DSI is undefined by construction. The effective sample size ($N_"effective"$) after this exclusion is reported explicitly. For the NSD outcome, participants require at least one sentiment-reliable day in each compared week to contribute a valid week-level comparison; otherwise that specific outcome is treated as missing for that participant. For paired pre/post analyses, complete paired observations are required per outcome variable, and the analyzed $n$ for each test is reported.

#pagebreak()

== Ethical Considerations

This study involves the monitoring of human participants' social media usage behavior, and as such, strict ethical protocols are observed to protect respondent welfare, privacy, and autonomy.

*Institutional Review:* Prior to data collection, the research protocol, instruments, and informed consent forms are submitted for review and approval by the university's Research Ethics Committee (or Institutional Review Board). No data collection activities commence until formal ethical clearance is obtained.

*Informed Consent:* All participants are provided with a detailed informed consent form explaining the study's purpose, procedures, data collected, risks, and benefits. Participation is entirely voluntary. The study version requires explicit user activation of the Android Accessibility Service through the device's system settings, providing the study's operational consent layer for text, interaction access, and the service-declared screenshot capability used for no-text visual processing.

*Data Protection and Privacy:* Consistent with the Privacy by Design principle described in the System Architecture section, the study version processes scraped textual content and temporary no-text screen frames in RAM only, with no persistent storage of raw text data or captured frames. Incidental third-party text, such as usernames or interface labels, is processed briefly and filtered where identifiable so non-participant data is not retained. Only aggregated metrics (e.g., sentiment scores, session duration) are retained. Logs and survey instruments are linked only through a participant study code, while the name-to-code key is stored separately by the researchers. Aggregate exported logs are transferred to a password-protected research folder accessible only to the researchers. The system does not transmit any data to external servers during routine operation.

*Right to Withdraw:* Participants may withdraw from the study at any time without penalty. Upon withdrawal, all locally stored usage data is deleted from the participant's device. If withdrawal occurs before final dataset consolidation, any exported participant record linked to that study code is also deleted from the research dataset.

*Minimization of Harm:* The study does not involve clinical diagnosis or treatment of mental health conditions. The mindfulness interventions (breathing exercises, break reminders) are designed as general wellness prompts, not therapeutic interventions.

The methodological and technical limitations arising from the research design and system architecture of this study are documented in Chapter 1 under *Limitations*.
