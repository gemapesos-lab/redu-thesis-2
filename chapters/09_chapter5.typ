// ==========================================
// CHAPTER 5: CONCLUSION
// ==========================================

= CONCLUSION

This chapter presents the summary of findings and conclusions based on the completed data analysis in Chapter 4. The conclusions are written within the study's actual scope: a pilot-scale software-engineering evaluation of a privacy-preserving Android system for heuristic doomscrolling-related risk estimation.

== Summary of Findings

For the first research question, the study found that the implemented Android system could support privacy-preserving local risk estimation using observable behavioral and sentiment-related proxies. The final logs contained 10,134 sessions across TikTok, Facebook Reels, and Instagram Reels, with 8,817 sessions classified as sentiment-reliable and 1,317 sessions classified as sentiment-unreliable. The system implemented local processing, study-code-based export, text-first VADER-compatible sentiment analysis, Filipino/Taglish MVL support, a no-text VLM fallback, fuzzy inference, and conservative fallback handling. The MVL concordance data also showed exact agreement between the expert ratings and deployed runtime valences for all 57 runtime lexicon entries.

For the second research question, the intervention group showed larger Week 1-to-Week 2 reductions than the control group across the four primary outcomes. Session duration, video dwell time, NSD, and the self-reported Doomscrolling Scale all favored the intervention group after the final assumption-selected tests and Holm-Bonferroni correction. Supplementary results for sessions per day and DSI followed the same direction, and the within-intervention paired comparisons showed reductions from Week 1 to Week 2.

For the third research question, Week 1 DSI showed a strong positive baseline association with the Week 1 Doomscrolling Scale among the 48 eligible participants. The composite DSI had Spearman's $rho = 0.76$, supporting convergent plausibility, while Week 1 mean daily session duration had the highest rank association with self-report at $rho = 0.92$. The finding supports convergence between DSI and self-reported doomscrolling rather than superiority of the composite over its individual components.

For the fourth research question, the user evaluation met all pre-set software-acceptability targets. Functional Suitability, Performance Efficiency, Reliability, SUS Usability, TAM Perceived Usefulness, and TAM Perceived Ease of Use all reached the favorable thresholds. The SUS composite was 80.95, above the study target of 70. Cronbach's alpha values ranged from 0.717 to 0.957, meeting the $alpha >= 0.70$ internal-consistency benchmark. Open-ended feedback showed that users valued the dashboard, break reminders, privacy approach, and self-monitoring support, while also identifying needs around prompt timing, onboarding, score clarity, platform monitoring, breathing-break length, export transparency, and battery/performance feedback.

For the fifth research question, both subject matter experts rated the system favorably. The mobile/software expert gave an overall mean of 5.00, while the digital well-being/behavioral psychology expert gave an overall mean of 4.33. Their comments supported the non-clinical framing, privacy-conscious design, and general intervention structure, while recommending clearer fallback criteria, clearer stored-data wording, softer risk terminology, and more configurable intervention behavior.

== Conclusions Based on Research Objectives and Questions

Based on the first research objective, the study concludes that the proposed system architecture was feasible for a bounded Android-based pilot deployment. The system was able to combine local monitoring, sentiment-aware processing, fallback handling, fuzzy inference, and adaptive prompts without requiring raw content retention in the research dataset. This supports the technical feasibility of the privacy-preserving design.

Based on the second research objective, the study concludes that the intervention condition was associated with favorable observed short-term differences in logged usage metrics and self-reported doomscrolling scores. These results support the promise of adaptive prompts as a short-term behavioral support mechanism under the study conditions; establishing long-term efficacy requires a longer deployment and a larger sample.

Based on the third research objective, the study concludes that the DSI has baseline convergent plausibility because it correlated strongly with the Doomscrolling Scale. Because session duration alone showed a stronger rank association in this sample, the DSI is best characterized as a transparent composite indicator that converges with self-report and still requires further validation and calibration.

Based on the fourth research objective, the study concludes that users evaluated the system favorably in terms of selected ISO/IEC 25010 characteristics, usability, and technology acceptance. The reliability coefficients also indicate acceptable to excellent internal consistency for the evaluation instruments in this sample. The qualitative feedback shows that the system was usable enough for pilot evaluation, but several parts of the user experience need refinement before broader deployment.

Based on the fifth research objective, the study concludes that the system received favorable expert plausibility appraisal from the available SME reviewers. The expert results support the appropriateness of the privacy framing, heuristic logic, and intervention structure for a non-clinical prototype. However, the two-expert panel and the absence of a dedicated fuzzy-systems or data-science reviewer limit the strength of the expert-validation claim.

Overall, the thesis concludes that the Heuristic Risk-State Estimation System is a defensible pilot prototype for privacy-preserving doomscrolling-related risk estimation and digital mindfulness prompting on Android short-form-video platforms. Its strongest contribution is the integration of local behavioral logging, text-first sentiment analysis, no-text fallback handling, fuzzy risk estimation, and user-facing prompts in one bounded software artifact. Its strongest limitation is that the evidence remains exploratory: the sample was purposive-convenience, the study lasted two weeks, some sentiment-dependent outcomes excluded unreliable sessions, and expert review was limited to two reviewers.

#pagebreak()
