# REDU Final Data Collection and Export Plan

## Purpose

This document lists the final data needed for the REDU thesis analysis package and identifies where each dataset should come from. The analysis package combines app-generated logs, participant survey responses, participant qualitative feedback, and expert/SME review forms. All records must use the participant study code, not names or personally identifying information.

## 1. App-Generated Data

These files come from the REDU Android app export. They are produced from local app storage after the field deployment. The app must export only aggregate metrics, study codes, platform labels, timing metrics, risk scores, reliability codes, and prompt events. Raw captions, comments, screenshots, usernames, and free-text platform content must not be stored or exported.

| File | Source | Expected Use | Notes |
|---|---|---|---|
| `sessions.csv` | REDU app local logs | Main session-level behavioral analysis | Includes study code, group, platform, session timing, duration, dwell time, swipe count, risk score, risk level, sentiment reliability, NSD when reliable, OOV ratio, and session ID. |
| `daily_summaries.csv` | REDU app local logs or post-export aggregation from app logs | Week-level and day-level descriptive summaries | Should summarize session count, mean duration, mean dwell, mean NSD, mean risk score, and reliable-session count by participant/date/platform. |
| `prompt_events.csv` | REDU app local logs | Prompt exposure and response summary | Intervention-group participants should have prompt events; control participants should not. Include prompt level, action, cooldown state, risk level, and linked session ID. |
| `reliability_events.csv` | REDU app local audit log | Data-quality and extraction-reliability audit | Includes service start, target foreground/background, session finalization, HIGH_OOV, screenshot/VLM failures if applicable, and affected session ID. |
| `risk_personalization.csv` | REDU app personalization records | Documents Week 1 baseline personalization status | Includes participant-level quantiles and reliable baseline session counts used for Week 2 live prompt personalization. |
| `redu-export.zip` | REDU app export bundle | Portable copy of core app-generated CSVs | Should contain the app-generated CSV files above. This is optional if the CSVs are already stored separately, but useful for traceability. |

## 2. Participant Self-Report Data

These files come from participant forms, not automatically from the app unless the app directly includes the survey form. Each row must be linked using the same study code used in the app logs.

| File | Source | Expected Use | Notes |
|---|---|---|---|
| `doomscrolling_scale.csv` | Participant survey at end of Week 1 and Week 2 | Baseline/post self-reported doomscrolling comparison and Week 1 DSI convergent association | Uses the 4-item short-form Doomscrolling Scale with 7-day recall. Expected two rows per participant: baseline and post. |
| `survey_iso25010.csv` | Participant post-usage survey | User software-quality evaluation | Participants rate Functional Suitability, Performance Efficiency, and Reliability through researcher-developed ISO/IEC 25010-aligned items. Usability is handled separately through SUS. |
| `sus_responses.csv` | Participant post-usage survey | User usability evaluation | Contains all 10 SUS items and the computed SUS score on a 0-100 scale. Even-numbered SUS items must be reverse-scored in analysis. |
| `tam_responses.csv` | Participant post-usage survey | User acceptance evaluation | Contains Perceived Usefulness and Perceived Ease of Use items, 6 items each, plus construct means. |
| `open_ended_feedback.csv` | Participant post-usage written feedback | Qualitative context for user experience findings | Includes what participants liked, main difficulty, privacy comment, suggested improvement, and researcher-coded theme. |

## 3. Expert / SME Evaluation Data

These files come from subject matter experts, not ordinary participants. The paper currently treats SME evaluation as pending unless these forms are completed.

| File | Source | Expected Use | Notes |
|---|---|---|---|
| `sme_evaluation.csv` | Three subject matter experts | Expert rating of technical soundness and design plausibility | Should include one expert in software/mobile development, one in data science/fuzzy logic, and one in digital well-being/behavioral psychology/educational technology. |
| `sme_open_ended_feedback.csv` | Same SME panel | Expert qualitative appraisal | Covers comments on technical design, privacy safeguards, heuristic logic, intervention structure, and recommended revisions. |

## 4. Researcher-Coded / Analysis-Derived Data

These are not raw collection instruments. They are values computed or coded by the researchers after collecting app logs and survey responses.

| Data | Source | Expected Use | Notes |
|---|---|---|---|
| `coded_theme` in `open_ended_feedback.csv` | Researcher coding of participant feedback | Summarizes recurring feedback themes | Coding must be based on actual participant comments, not invented themes. |
| Cronbach's alpha values | Computed from survey item responses | Internal-consistency reporting | Used for ISO/IEC 25010 subscales, SUS after scoring, and TAM constructs. Low alpha should be reported transparently. |
| Means and standard deviations | Computed from app logs and survey responses | Chapter 4 descriptive results | Must be reproducible from exported CSVs. |
| Week 1 / Week 2 change scores | Computed from logs and Doomscrolling Scale | RQ2 analysis | Used for behavioral and self-report comparison. |
| DSI correlations | Computed from Week 1 app logs and baseline Doomscrolling Scale | RQ3 convergent association | Uses participants with sufficient sentiment-reliable Week 1 sessions. |

## 5. Important Source Notes

- The REDU app should provide only the behavioral log files and app-derived audit records.
- Participant surveys provide Doomscrolling Scale, ISO/IEC 25010 user ratings, SUS, TAM, and open-ended feedback.
- SME files are separate expert forms and should not be mixed with participant survey files.
- ISO/IEC 25010 is used twice in the paper: participants provide user-facing software-quality ratings, while SMEs provide expert appraisal that includes overall software quality.
- A separate `current_device_snapshot/` folder is not required for the final analysis package unless the researchers want an internal audit backup. The app-generated export files are sufficient for the main thesis analysis if they contain complete logs and reliability events.
- All files must use study codes only. The name-to-code key must be stored separately and must not be placed in the export package.
- Raw captions, comments, screenshots, visible usernames, or captured frames must not be included in the export package.

## 6. Final Expected Export Package

The final `export/` directory should contain one analysis folder with:

```text
export/
  redu-final-analysis/
    sessions.csv
    daily_summaries.csv
    prompt_events.csv
    reliability_events.csv
    risk_personalization.csv
    redu-export.zip
    doomscrolling_scale.csv
    survey_iso25010.csv
    sus_responses.csv
    tam_responses.csv
    open_ended_feedback.csv
    sme_evaluation.csv
    sme_open_ended_feedback.csv
```

If SME evaluation remains pending, `sme_evaluation.csv` and `sme_open_ended_feedback.csv` may contain expert identifiers/roles with blank rating and comment fields, but the thesis must continue to state that SME results are pending.
