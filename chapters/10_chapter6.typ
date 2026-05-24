// ==========================================
// CHAPTER 6: RECOMMENDATIONS
// Completed from simulated-export/redu-p01-p50-20260523-224114
// ==========================================

#import "utils.typ": table_align, thesis_table

= RECOMMENDATIONS

This chapter presents recommendations for system improvement, future research, and future implementation or deployment. The recommendations are based on the simulated export analyzed in Chapter 4. The available user data support recommendations about prompt timing, onboarding clarity, dashboard usefulness, privacy communication, survey reliability, and sentiment-reliability coverage. SME recommendations remain pending until the blank expert rating and comment files are completed.

== Recommendations for System Improvement

System-improvement recommendations are tied to verified implementation results, user survey results, and coded user feedback.

#thesis_table(
  caption: [System Improvement Recommendations],
  columns: (1.25fr, 1.35fr, 1.35fr),
  cell_align: table_align((left, left, left)),
  header: (
    [*Improvement Area*],
    [*Condition Observed in Chapter 4*],
    [*Recommendation*],
  ),
  body: (
    [Platform extraction robustness],
    [All platforms produced logs, but sentiment reliability varied: TikTok 82.6%, Facebook 86.3%, and Instagram 81.9%.],
    [Maintain platform-specific extraction tests and update selectors when target-app UI structures change.],

    [Android compatibility and performance],
    [Performance Efficiency met the target with mean 3.83, but technical reliability appeared in 6 user feedback rows.],
    [Add CPU, memory, battery, and background-service survival logs, and expose reliability status in the dashboard.],

    [Sentiment and fallback handling],
    [345 of 2,051 sessions, or 16.8%, were sentiment-unreliable and marked HIGH_OOV.],
    [Expand the Filipino/Taglish lexicon review process and improve reliability diagnostics for high-OOV sessions.],

    [Prompt timing and burden management],
    [Prompt timing and interruption was the most frequent feedback theme with 14 responses.],
    [Add quiet hours, gentler prompt-frequency controls, and clearer cooldown behavior.],

    [Dashboard and user-facing transparency],
    [Dashboard usefulness appeared in 12 feedback rows, and only 9.0% of prompt responses selected view-dashboard.],
    [Add weekly trends, plain-language Safe/Warning/Critical explanations, and clearer DSI component summaries.],
  ),
)

The highest-priority system improvements are prompt timing and dashboard clarity because these were the most frequent user-facing concerns. Sentiment-reliability handling remains technically important because unresolved or high-OOV sessions directly reduce NSD and DSI interpretability.

== Recommendations for Future Development

Future-development recommendations should address the prototype limitations shown by the logs, user survey results, and coded feedback.

#thesis_table(
  caption: [Future Development Recommendations],
  columns: (1.25fr, 1.35fr, 1.35fr),
  cell_align: table_align((left, left, left)),
  header: (
    [*Development Area*],
    [*Observed Basis*],
    [*Recommendation*],
  ),
  body: (
    [Risk-estimation model refinement],
    [DSI coverage was complete and DSI correlated strongly with baseline DSS, but 16.8% of sessions lacked reliable sentiment.],
    [Retain the interpretable fuzzy model while adding better missing-sentiment diagnostics and sensitivity summaries.],

    [Platform support expansion],
    [All three target platforms were represented, but extraction success differed by platform.],
    [Build automated regression checks for each target platform and document platform-specific failure modes.],

    [User interface and dashboard iteration],
    [SUS met the target at 76.35, but dashboard usefulness and prompt timing were recurring feedback themes.],
    [Iterate the dashboard and prompt flow around the most frequent feedback categories.],

    [Data export and reporting workflow],
    [User files are now present, but SME rating and comment files remain blank.],
    [Keep the expanded export format and complete the SME data collection fields before final defense.],
  ),
)

Future development should preserve the expanded export package because it now supports Chapter 4 user analyses. The remaining export gap is expert review completion.

== Recommendations for Future Researchers

Future-research recommendations are grounded in the final study limitations, sample coverage, statistical results, and survey reliability findings.

#thesis_table(
  caption: [Future Research Recommendations],
  columns: (1.25fr, 1.35fr, 1.35fr),
  cell_align: table_align((left, left, left)),
  header: (
    [*Research Area*],
    [*Observed Basis*],
    [*Recommendation*],
  ),
  body: (
    [Sample size and group balance],
    [The simulated export had balanced groups of 25 participants each and complete week-level log and user-response coverage.],
    [Use at least the same balanced structure in field deployment, but plan for attrition and device incompatibility.],

    [Deployment duration],
    [Log metrics did not show reliable Week 1-to-Week 2 behavioral reduction, while DSS improved.],
    [Use a longer deployment window if the goal is to evaluate sustained behavior change in logs.],

    [Validation anchors],
    [Composite Week 1 DSI correlated strongly with baseline DSS at r=0.989.],
    [Replicate the convergent association with real field data and test whether DSI predicts later self-report or intervention response.],

    [Survey reliability],
    [Most alpha coefficients were below 0.70; Perceived Usefulness was closest at 0.687.],
    [Refine item wording, add or revise weak items, and pilot the instrument before final deployment.],

    [Qualitative feedback],
    [The top themes were prompt timing (14), onboarding clarity (12), and dashboard usefulness (12).],
    [Use follow-up interviews or structured probes to explain why these issues recur.],
  ),
)

Future researchers should treat the current findings as pilot evidence. The most important next step is to verify whether the favorable self-report and user-evaluation results remain stable in field data and whether they eventually align with behavioral log changes.

== Recommendations for Future Implementation and Deployment

Future implementation and deployment recommendations are based on verified deployment constraints, privacy considerations, prompt interaction behavior, and user feedback.

#thesis_table(
  caption: [Future Implementation and Deployment Recommendations],
  columns: (1.25fr, 1.35fr, 1.35fr),
  cell_align: table_align((left, left, left)),
  header: (
    [*Deployment Area*],
    [*Observed Basis*],
    [*Recommendation*],
  ),
  body: (
    [Privacy and data governance],
    [Privacy and trust feedback was favorable but still appeared as a distinct theme in 6 responses.],
    [Keep the aggregate-only export design and explain clearly that raw captions and screenshots are not saved.],

    [Participant onboarding],
    [Permission and onboarding clarity appeared in 12 feedback rows.],
    [Add clearer setup screens, permission rationale, and a post-setup checklist.],

    [Monitoring and support],
    [Every session had service-start, foreground, background, and finalization events; 345 HIGH_OOV events were recorded.],
    [Monitor reliability-event counts during deployment and contact participants when service or extraction issues recur.],

    [Institutional or classroom deployment],
    [User acceptance means were favorable, but SME acceptability is still pending.],
    [Avoid classroom-scale rollout until SME ratings and ethics-facing deployment safeguards are completed.],

    [Maintenance and platform updates],
    [Platform extraction success differed across target apps.],
    [Schedule platform-maintenance checks before and during field deployment to catch app UI changes early.],
  ),
)

The system may be responsibly improved as a privacy-preserving prototype. Broader deployment should wait until SME review is complete and until prompt timing, onboarding clarity, dashboard explanation, and survey reliability concerns are addressed.

#pagebreak()
