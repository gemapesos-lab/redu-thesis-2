// ==========================================
// CHAPTER 2: REVIEW OF RELATED LITERATURE AND STUDIES
// ==========================================

#import "utils.typ": table_align, thesis_table

= REVIEW OF RELATED LITERATURE AND STUDIES

This chapter reviews peer-reviewed literature and empirical studies relevant to doomscrolling, short-form platform engagement, digital mindfulness, privacy-preserving mobile monitoring, sentiment analysis, and software evaluation. Journalistic, policy, platform-issued, and product-documentation materials are used only for contextual background or feature verification. The review is organized to clarify the study's evidence base, technical grounding, and remaining gaps.

== Foreign Literatures

This section presents selected foreign literature relevant to doomscrolling, short-form platform design, digital mindfulness, and privacy-preserving mobile systems.

=== Doomscrolling, Platform Design, and Compulsive Engagement

Recent literature treats doomscrolling in short-form feeds as a joint product of user vulnerability and platform design rather than as a simple matter of screen-time volume. #cite(<rodrigues-2022>, form: "prose") frames doomscrolling as compulsive information seeking that can intensify anxiety and helplessness, while #cite(<zenone-2021>, form: "prose") show that recommendation-driven environments can amplify harmful exposure and addictive-use patterns. #cite(<zhang-tiktok-2023>, form: "prose") complements that view by showing that TikTok is woven into users' routines as entertainment, information, and social reference, which helps explain why these feeds can sustain engagement so effectively.

These works suggest that time-only intervention logic is too coarse for the present problem. A defensible intervention system should monitor sustained interaction patterns within algorithmic short-form feeds rather than rely only on coarse daily time limits.

=== Digital Mindfulness and Technology-Assisted Self-Regulation

Recent digital well-being reviews converge on a narrow design implication: technology-assisted self-regulation support is more acceptable when it is brief, timely, and low-burden. #cite(<mitsea-2023>, form: "prose") provide limited support for digitally assisted mindfulness as one self-regulation strategy, while #cite(<antezana-2022>, form: "prose") show that engagement improves when interventions remain lightweight and relevant to the user's immediate context. Together, they support short adaptive prompts as low-burden, non-clinical interruption mechanisms.

=== Privacy, Persuasive Design, and Ethical Considerations in Digital Well-Being

Privacy-oriented digital well-being literature also constrains what a defensible system can do. #cite(<tewari-2023>, form: "prose") argues that privacy protections should be designed into mobile health architectures from the start rather than added after collection logic is already fixed. That point is central to the present design: behavioral monitoring, sentiment processing, and prompting should occur without exporting raw user content to external services. This literature therefore supports local processing and aggregate-only retention.

=== Edge Computing and On-Device Vision-Language Fallbacks

Recent literature on privacy-preserving mobile analysis converges on a clear constraint: if visual inference is used at all, it should remain inside the device's privacy boundary. Sending screenshots to cloud-based multimodal APIs would expose sensitive user data to external servers. #cite(<sharshar-2025>, form: "prose") support the feasibility of edge-deployed Vision-Language Models under network and latency constraints, while #cite(<lee-2024>, form: "prose") show why compact transformer-based vision models remain a practical design priority on mobile and edge devices. Broader multimodal sentiment and affective-computing reviews likewise show that visual channels can add information when text is absent, although interpretation remains limited #cite(<das-2023>) #cite(<cortinas-lorenzo-2024>) #cite(<johnson-2025>). This literature supports the app's on-device VLM fallback for no-text items, with Moondream 0.5B used as a small deployment-fit model.


== Local Literatures

This section presents selected local literature and contextual materials relevant to doomscrolling, excessive social media use, and digital well-being in the Philippine setting. Because adult Philippine doomscrolling evidence remains limited, peer-reviewed local studies are used as nearby evidence of attention, scrolling, and self-regulation concerns, while journalistic or policy-oriented sources serve only as contextual background.

=== Local Discourse and Platform Concern

Philippine background materials show that doomscrolling and digital well-being had already entered local public discourse by 2021 #cite(<panaligan-2021>, form: "prose") #cite(<lanuza-2021>, form: "prose"). They establish contextual relevance, not adult behavioral prevalence, intervention effectiveness, or estimator validity.

=== Limits of the Local Evidence Base

The local empirical base remains uneven and still depends mainly on student-centered studies discussed later in this chapter. Recent Philippine work outside student-only samples is useful but indirect: #cite(<cleofas-2022>, form: "prose"), #cite(<zamora-2021>, form: "prose"), and #cite(<castillo-2022>, form: "prose") broaden the adult context by showing that social-media use intersects with mental well-being, anxiety-related susceptibility, or social connectedness across different Filipino populations. Even so, the local literature does not yet provide adult doomscrolling field evidence, validated cutoffs, or intervention-effect benchmarks for a privacy-preserving mobile system.

== Interim Synthesis of Literature
The reviewed foreign and local literature establishes three points. First, doomscrolling is a recognizable behavioral construct linked to distress-related and attention-related concerns, and platform design appears to help sustain engagement #cite(<taskin-2024>) #cite(<hawwa-2025>) #cite(<qin-2022>). Second, existing digital well-being responses remain fragmented: mainstream device- and platform-level tools still emphasize summaries, timers, or break reminders rather than user-controlled, locally processed, content-sensitive estimation #cite(<apple-screen-time-2025>) #cite(<google-digital-wellbeing-2024>) #cite(<tiktok-wellbeing-2024>) #cite(<mosseri-2021>) #cite(<rahmillah-2023>) #cite(<tewari-2023>). Third, Philippine literature supports local relevance and provides some adult context, but not enough adult field evidence to justify prevalence claims, threshold values, or expected intervention effects.

Together, these sources justify an engineering problem: combining behavioral signals, visible text, and a no-text VLM fallback in one privacy-preserving mobile system. They do not support text sentiment as a complete proxy for multimodal short-form feeds or provide strong local adult evidence for universal estimator parameters. Accordingly, the study uses a hybrid exposure signal in which analyzable text is scored through the text path and no-text items are routed through the VLM fallback.

== Foreign Studies

This section presents a review of related studies that provide empirical evidence on doomscrolling, compulsive social media use, and their psychological and behavioral impacts.

=== Psychometric Validation of Doomscrolling as a Measurable Construct

#cite(<sharma-2022>, form: "prose") establish doomscrolling as a measurable self-report construct, while #cite(<satici-2023>, form: "prose") extend that psychometric work and support a shorter 4-item form. Together, these studies justify using self-reported doomscrolling as a comparison anchor, not as session-level ground truth. Chapter 3 therefore treats the scale as a baseline convergent-association reference rather than as a direct label for individual scrolling sessions.

=== Psychological Mechanisms and Consequences of Doomscrolling

#cite(<taskin-2024>, form: "prose") and #cite(<hawwa-2025>, form: "prose") both position doomscrolling as part of a wider pathway linking platform engagement to poorer well-being outcomes. #cite(<taskin-2024>, form: "prose") highlight reduced mindfulness and secondary traumatic stress as mediating mechanisms, while #cite(<hawwa-2025>, form: "prose") connect doomscrolling to the relationship between social media addiction and anxiety. Their shared implication is that a pure time-only measure is insufficient, yet neither study validates a single behavioral proxy or mobile risk-estimation rule by itself.

=== Platform Addiction and Algorithmic Engagement

#cite(<qin-2022>, form: "prose") show that TikTok's platform characteristics can intensify flow experience and addictive use. This supports the thesis's focus on short-form mechanics and sustained engagement rather than on user weakness alone.

=== Computational Approaches to Detecting Harmful Digital Behavior

#cite(<zannettou-2024>, form: "prose") show that engagement with TikTok recommendation streams can be studied from donated behavioral traces, while #cite(<lokeshkumar-2021>, form: "prose") show that text and activity patterns can be processed computationally as risk-relevant indicators. These studies serve as methodological analogies: they support technical plausibility for trace-based analysis, but not direct precedent for doomscrolling estimation on short-form platforms or validation of the specific proxies and privacy constraints used here.

=== Fuzzy Logic as an Inference Engine for Mobile Behavioral Classification

Recent reviews show that fuzzy rule-based systems are useful when the target construct is gradual rather than binary and when the inference process must remain interpretable to human reviewers #cite(<vashishtha-2023>) #cite(<pickering-2025>). Recent work on interpretable fuzzy-model design also continues to treat simple triangular or shoulder-style membership sets as practical, auditable choices when labeled session-level training data are not available #cite(<porebski-2022>) #cite(<khairuddin-2021>). Recent fuzzy-design work also describes symmetric grid partitions and strong fuzzy partitions as interpretable starting structures when domain-specific numeric cutoffs are unavailable #cite(<azam-2021>) #cite(<khairuddin-2021>) #cite(<casalino-2022>). Together, this literature supports using fuzzy logic and an interpretable starting structure for Negative Sentiment Density, where no universal percentage threshold set is established.

=== Data Extraction and Privacy-Preserving Edge Computing

#cite(<lee-2022>, form: "prose") show that Android Accessibility Service can expose fine-grained interaction data unavailable through standard APIs, but they also make clear that such access is privacy-sensitive. #cite(<swathi-2025>, form: "prose") complement that point by supporting edge-centric monitoring as a privacy-preserving and responsive architecture. Together, these studies justify the architectural stance that rich interaction data, if collected at all, should be processed locally and reduced to aggregate outputs rather than exported as raw content.

=== Social Media Use, Sustained Attention, and Digital Well-Being Interventions

#cite(<rajeswari-2024>, form: "prose") and #cite(<roffarello-2021>, form: "prose") support different parts of the present design. #cite(<rajeswari-2024>, form: "prose") link heavier social media engagement with poorer sustained attention, which supports the general relevance of prolonged use and viewing cadence as behavioral signals. #cite(<roffarello-2021>, form: "prose") show that smartphone habits can be monitored in real time and mitigated through proactive just-in-time reminders, which supports the feasibility of lightweight adaptive intervention on the device. Together, they justify attention to both continuous engagement patterns and in-the-moment interruption, though not direct validation of the present doomscrolling estimator.

Recent measurement studies also help bound the fuzzy time-based inputs, even though they do not validate doomscrolling-specific cutoffs. #cite(<muise-2024>, form: "prose") show that smartphone content exposure often occurs in bursts of only a few seconds, #cite(<cho-2021>, form: "prose") treat sessions shorter than 5 seconds as likely mistaken openings and use 30 seconds as a short-duration divider in feature-level social-media logging, and #cite(<tian-2021>, form: "prose") report that 93.7% of mobile app usage in their large-scale logs lasts less than 10 minutes. Recent smartphone-logging work that reconstructs sessions from app-event streams also continues to merge adjacent app uses into one session when inter-app gaps remain within about 45 seconds #cite(<ahmed-2023>) #cite(<chen-2023>). These studies justify distinguishing immediate skips, brief but nontrivial viewing, clearly sustained use, and very short app-switch interruptions instead of treating all exposure as one undifferentiated time scale.

Intervention-timing studies point in the same direction. #cite(<terzimehic-2022>, form: "prose") report that sessions shorter than 10 minutes are less prone to regret, with alternative offline activities becoming more salient in the 10-20-minute and >20-minute ranges. #cite(<rixen-2023>, form: "prose") treat 10+ minute infinite-scrolling episodes as long sessions, #cite(<meinhardt-2025>, form: "prose") deploy an infinite-scroll intervention after 15 minutes of continuous scrolling, #cite(<ismail-2022>, form: "prose") use personalized prompts after 40 minutes of continuous sitting, and #cite(<ikegaya-2025>, form: "prose") show that week-1-derived personalized intervention criteria outperform uniform criteria in the first hour after prompting. Recent JITAI reviews add the same design implication at the framework level: decision points and rules should be empirically grounded, burden-aware, and personalized when possible rather than fixed by one universal cutoff #cite(<fiedler-2024>) #cite(<hsu-2025>) #cite(<van-genugten-2025>). Together, these studies support treating sub-10-minute use as a lower-risk baseline region, recognizing 15 minutes as a defensible live-prompt entry point for continuous infinite scrolling, and deriving Week 2 prompt criteria from each participant's Week 1 baseline.

=== Technology Acceptance Frameworks

Recent mHealth acceptability reviews continue to treat TAM as a practical evaluation framework, with Perceived Usefulness and Perceived Ease of Use retained as core acceptance constructs #cite(<adnan-2025>). That supports the inclusion of TAM alongside ISO/IEC 25010 in the post-usage evaluation.

=== Computational Constraints in Code-Mixed Sentiment Analysis

Analyzing Taglish on mobile devices presents two challenges: computational load and high Out-of-Vocabulary (OOV) rates. Recent low-resource and code-mixed sentiment work shows that transformer approaches can be data-hungry or resource-intensive, while lexicon-based and hybrid methods remain relevant when reproducibility, low overhead, and resource scarcity matter #cite(<tho-2021>) #cite(<hussain-2025>) #cite(<mohammed-2023>) #cite(<nazir-2026>). Work on lexicon curation for low-resource social-media text also shows that slang, emoticons, and local usage patterns often require targeted valence tuning rather than direct reuse of generic sentiment resources #cite(<wijayanti-2021>) #cite(<perera-2024>) #cite(<hashmi-2024>) #cite(<khan-2025>). For that reason, the study adopts a limited VADER + *Minimum Viable Lexicon* (MVL) strategy for code-mixed sentiment analysis on mobile devices.

=== Addressing the Affective Gap Through an On-Device VLM Fallback

When a short-form item contains no usable caption or visible comments, text-only sentiment cannot characterize that item at all. Reviews of multimodal sentiment analysis show why visual channels become relevant once text is absent #cite(<das-2023>), while user-generated-video emotion studies show that keyframe-based reduction can support practical visual inference from video without requiring dense frame-by-frame processing #cite(<wei-2021>) #cite(<zhang-xu-2023>). Additional recent work shows that sparse visual sampling can remain useful under resource or privacy constraints, including sparse sampling with majority voting on short utterance clips #cite(<sharma-2023>), privacy-compliant group emotion recognition using only 5 uniformly distributed frames on 5-second videos #cite(<augusma-2023>), and sparse sampling regimes in recent VLM long-video evaluation under context-length constraints #cite(<qu-2025>). These studies justify routing no-text items through the VLM fallback, using Moondream 0.5B as a small deployment-fit model with an on-demand screenshot capture rule rather than continuous video analysis. They do not validate the exact prompt design or label set, and they do not support full multimodal affective understanding.


== Local Studies

=== Doomscrolling and Student Outcomes in the Philippines

#cite(<punzalan-2024>, form: "prose") provide the closest local study to the present topic by describing doomscrolling-related experiences among Filipino students. #cite(<bautista-2024>, form: "prose"), #cite(<canila-2023>, form: "prose"), #cite(<ababat-2024>, form: "prose"), and #cite(<cleofas-2022>, form: "prose") reinforce the same local signal: heavier or more problematic social media use is linked to boredom proneness, distraction, procrastination, attention-related strain, or poorer mental well-being among Filipino students and young adults. These studies establish Philippine relevance but remain student-centered, self-report-heavy, or qualitative, so they support cautious adult field evaluation rather than adult norms or threshold values.

=== Social Media, Reading Competence, and Attention Span

#cite(<gagalang-2021>, form: "prose") and #cite(<cardoso-2024>, form: "prose") together support the local relevance of attention-related harms associated with heavy digital-platform use, especially among students. They help explain why viewing cadence and sustained use are reasonable behavioral dimensions to observe, even though they do not validate the estimator thresholds used for an adult sample.

=== Cognitive Benefits of Digital Detox Interventions

#cite(<lim-2025>, form: "prose") show that a two-week digital detox can improve attention and memory among Filipino Grade 12 students. The study is relevant mainly because it makes a two-week observation window plausible, not because it is a direct precedent for the present prompt-based intervention.

=== Local Computational Work on Filipino Sentiment Analysis

#cite(<co-2022>, form: "prose") and #cite(<cruz-2022>, form: "prose") show that Filipino or English-Filipino public text can be analyzed with lexicon-based or lightweight sentiment pipelines, but they also make clear that domain adaptation remains necessary when language use shifts across settings. These studies support the narrower claim that Filipino-facing sentiment processing is technically workable in the local setting, provided the lexicon is treated as task-specific and incomplete rather than as a universal sentiment resource.

=== Software Quality Evaluation in Philippine Educational Technology

For software evaluation, the stronger methodological anchor is the official ISO/IEC 25010 product quality model itself rather than a single local implementation paper #cite(<iso-25010-2023>). The standard supports the choice of selected quality characteristics, while the survey wording used in Chapter 3 remains researcher-developed and subject to expert review.

== Integrated Synthesis and Research Gap

=== Comparative Analysis of Existing Digital Well-Being Tools

To contextualize the research gap identified in this review, the following table summarizes the capabilities of existing digital well-being tools and platform-level features. The comparison is based on five key requirements derived from the preceding literature.

Feature characterizations for Apple Screen Time, Google Digital Wellbeing, TikTok, and Instagram are drawn from their official product or support pages #cite(<apple-screen-time-2025>) #cite(<google-digital-wellbeing-2024>) #cite(<tiktok-wellbeing-2024>) #cite(<mosseri-2021>). The broader third-party app row is grounded in the multimethod review by #cite(<rahmillah-2023>, form: "prose").

*Operational Definitions for Comparison:*
- *Behavioral Monitoring:* [Real-time] = continuous background monitoring of specific actions; [Aggregate] = summary views or timer-based monitoring without item-level sensing; [Platform-only] = monitoring limited to one platform's own activity tools.
- *Text-Visible Sentiment Proxy:* [✓] = analyzes the emotional tone of extracted text content; [✗] = treats all viewed content equally regardless of text tone.
- *Adaptive Intervention:* [✓] = intervention type changes based on estimated severity; [Partial] = fixed caps, timers, or break nudges that do not estimate content or risk; [✗] = monitoring only or fixed reminders without severity adaptation.
- *On-Device Processing:* [✓] = core monitoring or intervention logic is user-controlled and executed locally on the device; [N/A] = feature is platform-managed and not exposed as a user-controlled local processing pipeline.
- *Platform Scope:* [System-wide] = works across many apps on the device; [Single-platform] = limited to one service; [Varies] = depends on the specific third-party app; [Specific targets] = limited to named study platforms.

#{
  set text(size: 9pt)
  thesis_table(
    caption: [Comparative Analysis of Digital Well-Being Tools and the Proposed System],
    columns: (1.9fr, 0.9fr, 1fr, 0.95fr, 0.9fr, 1.65fr),
    cell_align: table_align((left, center, center, center, center, center)),
    pad_x: 0pt,
    header: (
      [*Tool / System*],
      [*Behavioral* \ *Monitoring*],
      [*Text-Visible* \ *Sentiment Proxy*],
      [*Adaptive* \ *Intervention*],
      [*On-Device* \ *Processing*],
      [*Platform* \ *Scope*],
    ),
    body: (
      [Apple Screen Time], [Aggregate], [✗], [Partial], [✓], [System-wide],
      [Google Digital Wellbeing], [Aggregate], [✗], [Partial], [✓], [System-wide],
      [TikTok screen-time \ tools], [Platform-only], [✗], [Partial], [N/A], [Single-platform: \ TikTok],
      [Instagram daily limit \ / break reminders], [Platform-only], [✗], [✗], [N/A], [Single-platform: \ Instagram],
      [#cite(<rahmillah-2023>, form: "prose") \ apps], [Varies], [✗], [Partial], [Varies], [Varies],
      [*Proposed System*], [*Real-time*], [*✓*], [*✓*], [*✓*], [*Specific targets:* \ *TikTok,* \ *Facebook Reels,* \ *Instagram Reels*],
    ),
  )
} <tab-comparison>

As shown in the table, the reviewed literature and official product pages identify useful summaries, timers, and break mechanisms, but not a single adult short-form video system that combines real-time behavioral monitoring, a text-first sentiment proxy, an implemented no-text VLM fallback, adaptive prompting, and strict local processing. The gap therefore lies in integration and deployment scope: the ingredients appear separately in prior work, but not as one privacy-preserving field-tested artifact under the conditions targeted here. #cite(<buchanan-2021>, form: "prose") help motivate this search by showing that scrolling outcomes depend on exposure quality rather than time alone, while #cite(<roffarello-2021>, form: "prose") show that just-in-time mobile interventions are feasible even without the specific doomscrolling construct or item-level negativity proxy used here.

The reviewed studies converge on three themes. First, doomscrolling can be measured through self-report and discussed as a distinct construct, which supports a baseline convergent association check #cite(<sharma-2022>) #cite(<satici-2023>). Second, technical and intervention precedents exist for mixed mobile interventions, interpretable fuzzy inference, privacy-preserving on-device processing, Filipino-facing sentiment analysis, and narrow no-text visual inference on edge devices, although the closest studies remain analogies rather than direct implementations of the app evaluated here #cite(<roffarello-2021>) #cite(<vashishtha-2023>) #cite(<pickering-2025>) #cite(<lee-2022>) #cite(<co-2022>) #cite(<cruz-2022>) #cite(<das-2023>) #cite(<wei-2021>) #cite(<sharshar-2025>). Third, the local Philippine literature indicates that attention, self-regulation, and problematic-use concerns are locally relevant, that some adult social-media contexts have been studied, that short-term digital well-being observation windows are workable, and that Filipino-facing sentiment processing is technically feasible in local text settings #cite(<punzalan-2024>) #cite(<cleofas-2022>) #cite(<zamora-2021>) #cite(<castillo-2022>) #cite(<lim-2025>) #cite(<co-2022>) #cite(<cruz-2022>). It does not yet establish adult prevalence, intervention effects, or calibrated threshold values for the present estimator.

A clear *research gap* therefore remains. No reviewed study integrates behavioral logging, a text-first sentiment proxy, an implemented no-text VLM fallback, adaptive prompts, and strict local processing in one privacy-preserving adult short-form video system while also reporting a field evaluation aligned with this thesis. The local adult evidence base is also too limited to justify calibrated estimator thresholds, strong expected-effect claims, or clinical interpretation. Hybrid text-plus-visual sentiment routing is therefore treated as partial coverage of negative exposure, not as a full representation of multimodal short-form video content. In this study, NSD is defined as a session-level negative-exposure metric computed from resolvable text units and VLM-resolved no-text items. The study addresses these gaps through a two-week pilot field evaluation focused on software behavior, short-term behavioral comparison, user acceptance, expert review, baseline convergent association, and sentiment-reliable coverage reporting.
