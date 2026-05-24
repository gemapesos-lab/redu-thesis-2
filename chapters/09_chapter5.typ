// ==========================================
// CHAPTER 5: CONCLUSION
// Completed from simulated-export/redu-p01-p50-20260523-224114
// ==========================================

#import "utils.typ": table_align, thesis_table

= CONCLUSION

This chapter presents the summary of findings and conclusions of the study based on the simulated export analyzed in Chapter 4. The conclusions are bounded by the available data. The export supports conclusions about aggregate logging, fallback flagging, prompt-event generation, pre/post self-reported doomscrolling, baseline convergent association, ISO/IEC 25010 user ratings, SUS usability, TAM acceptance, and coded user feedback. SME ratings and SME comments remain pending because the expert files are present but blank.

== Summary of Findings

The study was guided by five research questions. The first question focused on the design and implementation of a privacy-preserving architecture and estimation framework. The second question examined short-term Week 1-to-Week 2 changes in logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group. The third question examined the baseline convergent association between the fixed-prior Week 1 DSI and participants' self-reported doomscrolling scores. The fourth question evaluated the system from the users' perspective using ISO/IEC 25010 and TAM. The fifth question obtained subject matter expert evaluation of the technical design, privacy safeguards, heuristic logic, and intervention structure.

#thesis_table(
  caption: [Summary of Findings],
  columns: (0.55fr, 1.6fr, 1.45fr, 1.2fr),
  cell_align: table_align((center, left, left, left)),
  header: (
    [*RQ*],
    [*Research Question Focus*],
    [*Finding from Chapter 4*],
    [*Conclusion Direction*],
  ),
  body: (
    [1],
    [Architecture and estimation framework],
    [The export contained 2,051 aggregate sessions, 1,706 sentiment-reliable sessions, 345 fallback cases, and 576 intervention-only prompt displays.],
    [Objective supported by logs],

    [2],
    [Week 1 to Week 2 behavioral and self-report changes],
    [Log metrics showed no reliable between-group reduction, but the Doomscrolling Scale decreased more in the intervention group than in the control group.],
    [Mixed; self-report favorable],

    [3],
    [Baseline DSI and Doomscrolling Scale association],
    [All 50 participants met the Week 1 reliable-session coverage rule, and composite DSI correlated strongly with baseline DSS.],
    [Preliminary estimator plausibility],

    [4],
    [User ISO/IEC 25010 and TAM evaluation],
    [ISO/IEC 25010, SUS, and TAM means met targets, while alpha coefficients were mostly below 0.70.],
    [User acceptance favorable with reliability caution],

    [5],
    [SME evaluation],
    [SME files were present but rating and comment fields were blank.],
    [Expert appraisal pending],
  ),
)

The strongest findings are that REDU produced a complete aggregate logging workflow, met the user-evaluation mean targets, and showed a strong baseline association between composite DSI and self-reported doomscrolling. The short-term behavioral logs did not show reliable between-group improvement, so the self-report improvement should be interpreted cautiously rather than as definitive behavioral effectiveness.

== Conclusions Based on Research Objectives and Questions

For the first research question, the simulated export supports the conclusion that the implemented architecture can produce privacy-preserving aggregate records for the target study workflow. The exported files contain study codes, platform labels, timing metrics, risk scores, reliability codes, and prompt actions without raw captions, comments, screenshots, or directly identifying content. The system also produced fallback flags for 345 sentiment-unreliable sessions and preserved analyzable NSD values for 1,706 sessions. Therefore, the architecture and estimation framework are supported at the exported-log level, although SME review is still needed for expert appraisal of the design.

For the second research question, the available log metrics do not show a reliable short-term between-group behavioral difference. The intervention group's mean daily raw elapsed session duration increased by 0.59 minutes while the control group's decreased by 0.38 minutes, but the Holm-adjusted p-value was 0.200 and the effect-size confidence interval crossed zero. Dwell time and NSD differences were smaller. However, the Doomscrolling Scale mean score decreased more in the intervention group than in the control group, with an adjusted p-value of 0.005. Thus, the simulated data support a favorable self-report change but not a corresponding reliable reduction in logged behavior within the two-week pilot window.

For the third research question, the coverage requirement for DSI computation was met because all 50 participants had at least three sentiment-reliable Week 1 sessions. The composite Week 1 DSI correlated strongly with baseline Doomscrolling Scale scores ($r=0.989$, $p < 0.001$), and it showed a stronger association than session duration, dwell time, or NSD alone. This supports preliminary estimator plausibility in the simulated dataset, but it remains a convergent association finding rather than diagnostic validation.

For the fourth research question, users evaluated the system favorably at the mean-score level. Functional Suitability (3.97), Performance Efficiency (3.83), Reliability (3.91), SUS usability (76.35), Perceived Usefulness (4.01), and Perceived Ease of Use (4.06) all met the study-defined targets. Open-ended feedback identified prompt timing, permission/onboarding clarity, and dashboard usefulness as the most frequent user concerns. Because most Cronbach's alpha values were below 0.70, the favorable means should be interpreted with caution and supported by future instrument refinement.

For the fifth research question, no conclusion can be made about subject matter expert evaluation from the current export. Expert ratings and comments are still required to judge the technical soundness of the risk-estimation framework, input ranges, fuzzy rule base, privacy safeguards, and intervention structure.

The overall conclusion is that REDU is supported as a privacy-preserving, aggregate-log-producing prototype with visible risk scoring, fallback handling, prompt-event recording, preliminary self-report convergence, and favorable user-evaluation means. It should not yet be described as behaviorally effective in logged use or expert-approved. The study remains a pilot-scale software evaluation rather than a clinical or diagnostic validation study.

#pagebreak()
