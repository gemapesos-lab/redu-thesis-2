# Psychology Expert Validation Feedback

**Project Title:** Doomscrolling Detection and Digital Mindfulness Mobile Application for Short-Form Video Platforms

## Reviewer’s General Impression

After reviewing the submitted scoping brief and validation compilation, the proposed application appears to have a strong foundation as a non-clinical digital wellness tool intended to help users become more aware of prolonged and potentially unhealthy scrolling behavior.

The project clearly avoids presenting itself as a psychological assessment or mental health treatment application, which is an important ethical consideration. The overall structure of the interventions, the use of behavioral indicators, and the privacy-focused design are generally appropriate for an undergraduate research project.

At the same time, there are several areas where adjustments in wording, framing, and intervention intensity would help reduce the possibility of users misunderstanding the system as a clinical or diagnostic tool.

## Validation Findings

### 1. Intervention Structure and Psychological Appropriateness
The three-level intervention system (Awareness Notification → Pause Prompt → Breathing Overlay) is psychologically reasonable as a digital mindfulness strategy.

The gradual escalation works well because it starts with low-intensity reminders before moving into more noticeable interruptions. This approach helps avoid overwhelming users while still encouraging reflection on their scrolling habits.

One of the strengths of the design is that the system does not force users to stop using the application. Instead, it encourages self-awareness and gives users the ability to decide what to do next. From a psychological perspective, preserving user autonomy is important because overly controlling interventions may lead to resistance or frustration.

**Recommendation:**
The interventions should continue to use supportive and neutral language. The prompts should feel like reminders or wellness nudges rather than warnings about problematic behavior.

### 2. Use of the Doomscrolling Feedback Loop Model
The Doomscrolling Feedback Loop Model is an appropriate theoretical basis for the application. The system appropriately limits itself to observable behavior such as:
*   session duration,
*   dwell time,
*   and exposure to negative content.

Importantly, the application does not claim to measure emotional states, psychological distress, or psychiatric conditions directly. This distinction is clearly stated throughout the documentation and should remain heavily emphasized in the final implementation.

The current framing of the system as a behavioral estimation and self-monitoring tool is psychologically appropriate.

**Recommendation:**
The researchers should consistently remind users that:
*   the app only estimates scrolling patterns,
*   the scores are heuristic rather than diagnostic,
*   and the application is not intended to evaluate mental health status.

This clarification should appear:
*   during onboarding,
*   in the settings page,
*   and near the dashboard or score displays.

### 3. Evaluation of the Fuzzy Logic Rule Base
The overall logic behind the 27-rule fuzzy inference system is conceptually acceptable for a non-clinical application. The prioritization of prolonged exposure and high negative-content engagement makes psychological sense within the context of doomscrolling behavior.

The decision to treat dwell time as a supporting factor rather than the main indicator is also reasonable. Spending a long time on a single piece of content does not automatically indicate unhealthy engagement.

The rule structure generally demonstrates:
*   logical escalation,
*   consistency,
*   and reasonable behavioral interpretation.

**Concern Regarding Rule 25**
Rule 25 classifies: High Dwell Time, High Negative Sentiment Density, and Low Session Duration as “Critical.” This classification may be too strong for a short session. Labeling this as “Critical” could potentially feel exaggerated or alarming.

**Recommendation:**
It may be more proportionate to classify this condition under a Warning-level response instead of triggering the strongest intervention.

### 4. Timing and Prompt Frequency
The 15-minute threshold before interventions begin is psychologically reasonable and supported by literature. The threshold appears balanced enough to:
*   avoid excessive interruption,
*   reduce prompt fatigue,
*   and still capture sustained scrolling behavior.

Similarly, the 15-minute cooldown period between prompts is appropriate for reducing user burden.

**Recommendation:**
Future versions of the application may benefit from: adjustable sensitivity settings, personalized thresholds, or adaptive timing.

### 5. Review of the User-Facing Prompts

#### Level 1 — Awareness Notification
The Level 1 notification is appropriately lightweight and minimally intrusive.

**Recommendation:**
Wording should remain calm and non-judgmental.
*   **Appropriate:** "You've been scrolling for a while," "Consider taking a short break," "You may want to pause and reset."
*   **Avoid:** "Warning," "Risk detected," "You are doomscrolling," "Critical behavior detected."

#### Level 2 — Pause Prompt
The Level 2 modal is appropriate because it introduces friction while preserving user choice.

**Recommendation:**
The interface should avoid visually favoring one option over another (e.g., "Continue" vs. "Take a break").

#### Level 3 — Breathing Overlay
This is the part most likely to be interpreted as a mental health intervention.

**Recommendations:**
1.  Reframe using non-clinical language: "Pause and Reset" or "Take a Short Breathing Break" instead of "Breathing Intervention."
2.  Include a brief disclaimer that the feature is a digital wellness pause, not treatment.
3.  Allow users to skip, shorten, or disable this feature.
4.  A shorter default duration (30–45 seconds) is recommended.

### 6. Risk of Harm and Ethical Considerations
One concern involves terms like "Risk," "Warning," "Critical," and "Detection." These may unintentionally sound clinical.

**Recommendation:**
Use softer, more neutral terminology.

| Current Term | Suggested Alternative |
| :--- | :--- |
| Risk Score | Activity Pattern Score |
| Warning | Elevated Use |
| Critical | Extended Exposure |
| Detection | Pattern Monitoring |

**Recommended Safeguards:**
The application should include clear non-clinical disclaimers, opt-out settings, adjustable notifications, and mental health referral information.

### 7. Cultural Considerations for Filipino Users
The non-confrontational and reflective style of prompts fits well with Filipino communication styles. However, limitations may exist in sentiment analysis when interpreting: sarcasm, humor, slang, Taglish, and culturally specific emotional language.

**Recommendation:**
Acknowledge these limitations in the study discussion.

### 8. Evaluation Methodology
The two-week field deployment is acceptable for usability/feasibility, but not sufficient for long-term habit change or sustained psychological effects. The thesis appropriately acknowledges this.

## Final Validation Conclusion
Overall, the proposed application is psychologically defensible as a non-clinical digital wellness and behavioral awareness tool.

**Strongest aspects:**
*   privacy-conscious design,
*   transparent limitations,
*   adaptive intervention structure,
*   and careful avoidance of direct mental health claims.

**Overall Assessment:**
*   **Conceptual Foundation:** Acceptable with minor revisions.
*   **Intervention Design:** Psychologically appropriate for non-clinical use.
*   **Ethical Framing:** Needs softer terminology and stronger disclaimers.
*   **Risk Management:** Adequate, though additional user controls are recommended.

**Validated By:**
**CHRISTINE ANNE MORENO VILLAMARZO, RPsy**
Chief Psychologist, KZen Psychological Services
License No.: 0001858