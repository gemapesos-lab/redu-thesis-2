// ==========================================
// CHAPTER 6: RECOMMENDATIONS
// Draft shell: data-dependent recommendations are intentionally left as placeholders.
// ==========================================

#import "utils.typ": table_align, thesis_table

#let ph(label) = [#raw("[TBD after evaluation data: " + label + "]")]

= RECOMMENDATIONS

This chapter presents recommendations for system improvement, future research, and future implementation or deployment. Because the Chapter 4 evaluation results have not yet been completed, all recommendations are retained as placeholders and should be finalized only after the weakest evaluation categories, implementation constraints, user feedback, and expert feedback are known.

== Recommendations for System Improvement

System-improvement recommendations should be tied directly to verified implementation results, ISO/IEC 25010 ratings, SUS results, TAM results, open-ended feedback, and SME evaluation findings.

#thesis_table(
  caption: [System Improvement Recommendations for Completion],
  columns: (1.25fr, 1.35fr, 1.35fr),
  cell_align: table_align((left, left, left)),
  header: (
    [*Improvement Area*],
    [*Condition to Check in Chapter 4*],
    [*Recommendation to Finalize*],
  ),
  body: (
    [Platform extraction robustness],
    ph("insert target-app extraction issues, if any"),
    ph("insert system-improvement recommendation"),

    [Android compatibility and performance],
    ph("insert device, OS, background-process, or performance issue, if any"),
    ph("insert compatibility or optimization recommendation"),

    [Sentiment and fallback handling],
    ph("insert NSD coverage, no-text fallback, or code-mixed sentiment limitation, if any"),
    ph("insert estimator refinement recommendation"),

    [Prompt timing and burden management],
    ph("insert prompt intrusiveness, timing, or cooldown feedback, if any"),
    ph("insert adaptive-prompt improvement recommendation"),

    [Dashboard and user-facing transparency],
    ph("insert usability, usefulness, or explanation weakness, if any"),
    ph("insert dashboard or explanation improvement recommendation"),
  ),
)

The final recommendations in this section should prioritize the lowest-rated or most constrained areas reported in Chapter 4. #ph("insert prioritized system-improvement recommendations after analysis of weakest evaluation categories")

== Recommendations for Future Development

Future-development recommendations should identify feature, algorithm, interface, or architecture improvements that are justified by the completed Chapter 4 results.

#thesis_table(
  caption: [Future Development Recommendations for Completion],
  columns: (1.25fr, 1.35fr, 1.35fr),
  cell_align: table_align((left, left, left)),
  header: (
    [*Development Area*],
    [*Condition to Check in Chapter 4*],
    [*Recommendation to Finalize*],
  ),
  body: (
    [Risk-estimation model refinement],
    ph("insert DSI, NSD, fuzzy-rule, or fallback weakness, if any"),
    ph("insert future model-development recommendation"),

    [Platform support expansion],
    ph("insert target-platform coverage and extraction-stability limitations"),
    ph("insert future platform-support recommendation"),

    [User interface and dashboard iteration],
    ph("insert SUS, TAM, and open-ended usability findings"),
    ph("insert future UI development recommendation"),

    [Data export and reporting workflow],
    ph("insert researcher, participant, or SME feedback on reporting needs"),
    ph("insert future reporting-feature recommendation"),
  ),
)

The final recommendations in this section should remain tied to verified system-development needs and should not assume evaluation outcomes that are not yet available. #ph("insert future-development recommendations after final Chapter 4 findings")

== Recommendations for Future Researchers

Future-research recommendations should be grounded in the final study limitations, sample coverage, measurement reliability, and statistical results.

#thesis_table(
  caption: [Future Research Recommendations for Completion],
  columns: (1.25fr, 1.35fr, 1.35fr),
  cell_align: table_align((left, left, left)),
  header: (
    [*Research Area*],
    [*Condition to Check in Chapter 4*],
    [*Recommendation to Finalize*],
  ),
  body: (
    [Sample size and group balance],
    ph("insert analyzed sample, attrition, and group-balance limitations"),
    ph("insert future sample recommendation"),

    [Deployment duration],
    ph("insert Week 1 to Week 2 result pattern and pilot-window limitation"),
    ph("insert future deployment-duration recommendation"),

    [Validation anchors],
    ph("insert DSI correlation, coverage, and self-report limitation"),
    ph("insert future validation recommendation"),

    [Survey reliability],
    ph("insert Cronbach's Alpha and item-coherence results"),
    ph("insert instrument refinement recommendation"),

    [Qualitative feedback],
    ph("insert coded user and SME feedback themes"),
    ph("insert future mixed-methods recommendation"),
  ),
)

The final recommendations in this section should avoid overstating the study's evidence and should identify what future researchers need to verify next. #ph("insert future-research recommendations after final Chapter 4 findings")

== Recommendations for Future Implementation and Deployment

Future implementation and deployment recommendations should be based on verified deployment constraints, privacy considerations, user feedback, and expert review.

#thesis_table(
  caption: [Future Implementation and Deployment Recommendations for Completion],
  columns: (1.25fr, 1.35fr, 1.35fr),
  cell_align: table_align((left, left, left)),
  header: (
    [*Deployment Area*],
    [*Condition to Check in Chapter 4*],
    [*Recommendation to Finalize*],
  ),
  body: (
    [Privacy and data governance],
    ph("insert privacy-safeguard verification and SME privacy feedback"),
    ph("insert deployment privacy recommendation"),

    [Participant onboarding],
    ph("insert onboarding, permission, or setup feedback"),
    ph("insert onboarding recommendation"),

    [Monitoring and support],
    ph("insert service reliability, support, or troubleshooting issues"),
    ph("insert monitoring/support recommendation"),

    [Institutional or classroom deployment],
    ph("insert acceptability, usefulness, and ethical constraints"),
    ph("insert implementation-context recommendation"),

    [Maintenance and platform updates],
    ph("insert target-platform change or compatibility issue"),
    ph("insert maintenance recommendation"),
  ),
)

The final recommendations in this section should describe how the system may be responsibly improved or deployed after the pilot findings are known. #ph("insert future implementation/deployment recommendations after final Chapter 4 findings")

#pagebreak()
