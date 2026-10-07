# REDU app showcase

These screenshots are from the debug build on a connected Samsung Galaxy A35
(`SM-A356E`, Android 16, 1080 x 2340), recaptured after the warm-dark polish.
The device had a fresh empty database, so the capture starts at setup. The
participant code used for the flow is `P001`, which assigns the intervention
group. No sessions were inserted. Design frames from the Sleek pass are in
`sleek/`. The live editor is [the REDU polish project](https://sleek.design/project/7pZ8yuDCNd0).

## Setup and Android permission flow

- `01_setup_participant.png` — participant-code step, empty state
- `02_setup_participant_filled.png` — participant code and assigned group
- `03_setup_platforms.png` — platform-selection step, nothing selected
- `04_setup_platforms_selected.png` — TikTok, Instagram, and Facebook selected
- `05_setup_monitoring.png` — monitoring permission still required
- `06_android_accessibility_settings.png` — Android accessibility settings opened from setup

Once the monitoring service is enabled, the app opens Home directly. The
"Setup ready" step is not shown on that path.

## Home and history

- `07_home.png` — home with no recorded activity
- `08_home_signal_details.png` — expanded research diagnostics
- `09_history.png` — empty history
- `10_history_tiktok_filter.png` — TikTok platform filter
- `11_history_elevated_filter.png` — Elevated activity-pattern filter

## Settings, export, and dialogs

- `12_settings_monitoring.png` — monitoring, platforms, and pause prompts
- `13_settings_study_collapsed.png` — collapsed study configuration and image scanning
- `14_settings_study_expanded.png` — participant code, group, and timezone
- `15_settings_study_period.png` — study-period date fields
- `16_edit_participant_dialog.png` — participant-code edit dialog
- `17_study_date_picker.png` — study-period date picker
- `18_settings_privacy.png` — image scanning, export, and local processing
- `19_export.png` — export overview
- `20_export_datasets.png` — the six included CSV datasets
- `21_settings_advanced.png` — reset and developer demo controls
- `22_reset_study_data_dialog.png` — destructive reset confirmation

## Intervention overlays

- `23_demo_l1.png` — non-blocking awareness reminder
- `24_demo_l2_locked.png` — pause prompt before continue unlocks
- `25_demo_l2_unlocked.png` — pause prompt after continue unlocks
- `26_demo_l3.png` — guided breathing break

History with saved sessions, the clear-history confirmation, and the activity-score
sheet are not included. Those screens appear only after REDU has saved a session.
