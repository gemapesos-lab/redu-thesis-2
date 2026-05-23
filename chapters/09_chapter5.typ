// ==========================================
// CHAPTER 5: CONCLUSION
// Draft shell: data-dependent conclusions are intentionally left as placeholders.
// ==========================================

#import "utils.typ": table_align, thesis_table

#let ph(label) = [#raw("[TBD after evaluation data: " + label + "]")]

= CONCLUSION

This chapter presents the summary of findings and conclusions of the study. Because the final participant data, survey data, subject matter expert ratings, and statistical outputs have not yet been inserted in Chapter 4, the conclusion statements below are retained as completion shells. Final wording should be completed only after Chapter 4 contains verified results.

== Summary of Findings

The study was guided by five research questions. The first question focused on the design and implementation of a privacy-preserving architecture and estimation framework. The second question examined short-term Week 1-to-Week 2 changes in logged usage metrics and self-reported doomscrolling scores between the intervention group and the logging-only control group, with an additional within-intervention comparison. The third question examined the baseline convergent association between the fixed-prior Week 1 DSI and participants' self-reported doomscrolling scores. The fourth question evaluated the system from the users' perspective using ISO/IEC 25010 and TAM. The fifth question obtained subject matter expert evaluation of the technical design, privacy safeguards, heuristic logic, and intervention structure.

#thesis_table(
  caption: [Summary of Findings for Completion],
  columns: (0.55fr, 1.6fr, 1.45fr, 1.2fr),
  cell_align: table_align((center, left, left, left)),
  header: (
    [*RQ*],
    [*Research Question Focus*],
    [*Finding to Insert from Chapter 4*],
    [*Conclusion Direction*],
  ),
  body: (
    [1],
    [Architecture and estimation framework],
    ph("insert final implementation/testing finding"),
    ph("insert whether objective was met, partially met, or not met"),

    [2],
    [Week 1 to Week 2 behavioral and self-report changes],
    ph("insert final change-score and pre/post finding"),
    ph("insert observed short-term difference interpretation"),

    [3],
    [Baseline DSI and Doomscrolling Scale association],
    ph("insert final correlation and coverage finding"),
    ph("insert estimator plausibility interpretation"),

    [4],
    [User ISO/IEC 25010 and TAM evaluation],
    ph("insert final survey and SUS finding"),
    ph("insert user acceptability interpretation"),

    [5],
    [SME evaluation],
    ph("insert final expert-rating and feedback finding"),
    ph("insert expert appraisal interpretation"),
  ),
)

The completed summary should briefly synthesize the main findings rather than repeat every table from Chapter 4. It should identify whether the system met the software-development objective, whether short-term behavioral differences were observed, whether the DSI showed baseline plausibility, and how users and experts evaluated the system.

== Conclusions Based on Research Objectives and Questions

The conclusions should be finalized only after the Chapter 4 results are complete. Each conclusion must be tied to a verified finding, and no conclusion should state that the system was effective, acceptable, reliable, or statistically supported unless the completed data support that claim.

The final conclusion for the first research question should state whether the implemented system architecture, fuzzy inference design, fallback behavior, and privacy safeguards were completed and verified. The conclusion should also specify any implementation constraints discovered during testing, such as target-app UI changes, device compatibility issues, or Accessibility Service screenshot/API compatibility limitations. #ph("insert final RQ1 conclusion")

The final conclusion for the second research question should state whether the intervention group showed meaningful Week 1-to-Week 2 changes compared with the logging-only control group. The wording should distinguish between observed pilot differences and long-term intervention effectiveness. #ph("insert final RQ2 conclusion")

The final conclusion for the third research question should state whether the fixed-prior Week 1 DSI showed a theoretically coherent association with the short-form Doomscrolling Scale among participants with sufficient sentiment-reliable sessions. This conclusion should be framed as estimator plausibility only, not diagnostic validation. #ph("insert final RQ3 conclusion")

The final conclusion for the fourth research question should state how users evaluated the system across ISO/IEC 25010, SUS, and TAM constructs. If any construct falls below the study-defined threshold, the conclusion should identify it clearly and connect it to improvement needs. #ph("insert final RQ4 conclusion")

The final conclusion for the fifth research question should state how subject matter experts evaluated the technical design, privacy safeguards, heuristic logic, and intervention structure. If expert feedback identifies weaknesses in the rule base, variable thresholds, or intervention timing, the conclusion should acknowledge these as refinement priorities. #ph("insert final RQ5 conclusion")

The overall conclusion should remain bounded by the study design. The study is a pilot-scale software evaluation, not a clinical trial, and the system estimates risk from observable proxies rather than diagnosing doomscrolling or mental-health conditions. #ph("insert overall conclusion after Chapter 4 is complete")

#pagebreak()
