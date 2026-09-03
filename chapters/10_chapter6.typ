// ==========================================
// CHAPTER 6: RECOMMENDATIONS
// ==========================================

= RECOMMENDATIONS

This chapter presents recommendations derived from the findings, limitations, user feedback, expert comments, and implementation constraints discussed in Chapter 4 and Chapter 5. The recommendations focus on improving the current system, strengthening future versions, and guiding future researchers who may replicate or extend the study.

== Recommendations for System Improvement

The system should include a guided onboarding flow for enabling the Android Accessibility Service and other required permissions. Onboarding and setup were among the most frequent participant feedback themes, and several respondents reported difficulty finding or understanding Android settings. A step-by-step setup guide with screen-specific instructions would reduce early friction and improve deployment consistency.

The application should explain the risk or activity score more clearly. User feedback showed recurring confusion about what the score meant and why some sessions received elevated labels. Future versions should show a simple component breakdown for session duration, video dwell time, and negative sentiment contribution. This would make the fuzzy score more transparent without exposing raw user content.

Prompt timing should be made more configurable. Since prompt timing was the most frequent open-ended feedback theme, the system should add options such as snooze, quiet hours, adjustable sensitivity, and clearer cooldown feedback. These changes would preserve the intervention function while giving users more control over interruptions.

The pause-and-reset or breathing-break feature should be shortened or made configurable. Both participant and expert feedback suggested that the breathing screen may feel too long in some contexts. A shorter default duration, or a user-selectable range, would better support autonomy while retaining the intended interruption effect.

The dashboard should include platform-monitoring status indicators for TikTok, Facebook Reels, and Instagram Reels. Some participants noticed inconsistent platform monitoring, especially around Instagram. A visible status indicator would help users understand whether each platform is actively being tracked, whether permissions are still enabled, and whether recent sessions were successfully logged.

The export feature should provide a preview or confirmation screen before sharing study data. Some participants were unsure which files were included in the export or whether the export had completed. The system should list exported file names, explain that raw text and screenshots are not included, and show a completion confirmation.

== Recommendations for Future Development

Future versions should improve platform-specific extraction reliability. The system already logs reliability events, but future development should use those events to diagnose recurring extraction failures, high-OOV cases, and unresolved no-text cases by platform. This would help reduce sentiment-unreliable sessions and improve DSI coverage.

The VLM fallback should be further optimized and documented. Since unresolved VLM events contributed to reliability issues, future development should test prompt wording, model-response mapping, screenshot timing, and fallback thresholds. The goal should be to improve no-text handling while preserving local-only processing and avoiding persistent storage of screen frames.

Future versions should explore multi-modal parallel analysis to address potential inconsistencies between visual content and textual comments. The current system uses a text-first routing architecture where the VLM fallback is invoked only for no-text items, so cases where a video's visual sentiment differs from its accompanying text are handled entirely through the text path. A future multi-modal implementation could run both text and visual analysis paths in parallel, apply weighted scoring between modalities (e.g., $S_"combined" = w_"text" dot S_"text" + w_"visual" dot S_"visual"$), and define fuzzy logic rules for conflict resolution when the two modalities diverge. Such an approach would improve coverage of sarcasm, ironic commentary, and cross-modal sentiment mismatches, though it would increase computational cost due to the additional VLM inference per item.

The terminology used in the interface should be softened. Expert feedback recommended reducing alarming labels such as "Risk Score," "Warning," and "Critical." Future versions may use terms such as "Activity Pattern Score," "Elevated Use," and "Extended Exposure," as long as the thesis or documentation clearly maps these labels to the underlying analytic bands.

The system should retain the privacy-preserving local architecture. The favorable user and expert feedback around local processing indicates that privacy is a key strength of the system. Future enhancements should avoid unnecessary cloud processing and continue storing only aggregate metrics unless a new ethics review and consent procedure justifies a different design.

== Recommendations for Future Researchers

Future researchers should conduct a longer deployment with a larger and more diverse sample. The present study used 50 purposively recruited participants over two weeks, which is appropriate for a pilot software evaluation but not enough for long-term efficacy or population-level claims. A longer study would help assess whether behavioral changes persist after novelty effects and initial prompting wear off.

Future researchers should consider expanding the SME panel to include a dedicated data science, machine learning, or fuzzy-logic expert in addition to the software engineering and behavioral psychology reviewers. While the present study's software engineering expert evaluated the algorithm's accuracy, fuzzy logic effectiveness, and scalability, a dedicated fuzzy-systems reviewer would further strengthen appraisal of membership functions, rule coherence, fallback thresholds, and defuzzification choices.

Future researchers should validate the DSI against additional external anchors. The present study found strong convergence with self-reported doomscrolling but also found that session duration had a higher rank association than the composite DSI. Future studies should compare DSI with full-scale doomscrolling instruments, ecological momentary reports, platform-independent digital well-being logs, or other validated behavioral measures.

Future researchers should evaluate whether the fuzzy composite adds explanatory value beyond simpler metrics. Because mean daily session duration showed the strongest baseline rank association with self-report, later studies should test whether DSI improves prediction, interpretability, or intervention timing after controlling for duration alone.

Future researchers should report effective sample sizes and sentiment coverage transparently. Sentiment-unreliable sessions affect NSD and DSI analysis, so future reports should continue to disclose retained sessions, excluded sessions, participant-level coverage, and platform-specific reliability patterns.

Future researchers should consider including gender as an independent variable or moderator to examine whether doomscrolling patterns, intervention responsiveness, or user acceptance differ across gender groups. The present study recorded sex in the baseline profile but did not analyze gender-based differences, which may limit the generalizability of the results.

== Recommendations for Future Implementation and Deployment

Future implementation should treat the system as a digital well-being support tool, not as a diagnostic or therapeutic application. The strongest defensible use case is self-monitoring and gentle interruption of prolonged short-form-video use. Any expansion toward mental-health claims would require a different clinical design, additional safeguards, and appropriate professional review.

Deployment should include clear consent and privacy explanations before enabling monitoring. Users should be told what the Accessibility Service reads, when transient screenshots may be used for no-text cases, what data are stored, what data are exported, and how they can stop monitoring or delete local data.

Deployment teams should prepare device-compatibility checks before field use. Android version differences, manufacturer background-process policies, and screenshot availability can affect monitoring stability. A short compatibility checklist should be completed before participants begin the baseline week.

Future deployments should include support materials for participants. A concise setup guide, troubleshooting guide, privacy FAQ, and export guide would reduce researcher support burden and help participants complete the study procedure consistently.

Finally, future implementation should keep evaluation claims proportional to the evidence collected. The current results support operational feasibility, favorable user evaluation, expert plausibility appraisal, and observed short-term differences under pilot conditions. Broader claims about long-term behavior change, diagnostic accuracy, or generalized effectiveness should be reserved for larger and longer studies with stronger validation designs.

#pagebreak()
