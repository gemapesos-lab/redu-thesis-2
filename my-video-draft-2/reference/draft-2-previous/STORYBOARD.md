# REDU AVP — Draft 2 storyboard

**Status:** Storyboard and silent animatic. Updated VO pending; all word and action cues are provisional.

**Target: 59.00 seconds, including five seconds of credits. Absolute limit: 60.000 seconds.** 1920 × 1080, 16:9, 30 fps, 1,770 frames.

![Draft 2 storyboard](out/storyboard.png)

## Storyboard

| Panel | Time | Visual and action | Working VO |
|---|---|---|---|
| 01 · Just one more | 00:00–00:05 | A short scroll accelerates into a softly illuminated seven-segment digital clock. The full 2:07 AM display lands on the spoken time. | Just one more video. Suddenly, it’s two-oh-seven AM. |
| 02 · The problem | 00:05–00:10 | Every word lands sharply on its spoken onset. Give Not its own cue; the contrast line becomes the focus. | That’s doomscrolling. Screen-time limits count minutes, not what you watch. |
| 03 · Meet REDU | 00:10–00:14 | A sage breath blooms into warm paper. The REDU logo, Android identity and purpose appear in sequence. | Meet REDU, an Android app that helps you pause. |
| 04 · Who it’s for | 00:14–00:17 | The audience headline sits beside the real platform-setup capture and the three platform identifiers. | Built for adult Filipino Android users. |
| 05A · Session length + time per video | 00:17–00:21 | Introduce the two numbered timers on their spoken cues. Leave the third signal for the caption reveal. | Three signals: session length, time per video… |
| 05B · Negative captions take focus | 00:21–00:28 | 84 px caption. Coral word highlight. Readable Negative label. Hold ≥3 seconds. Vision is a small supporting note. | …and negative captions. It checks Filipino and English text for negative language. |
| 06 · Doomscrolling risk score | 00:28–00:32 | Exactly three named inputs converge into the risk meter. The marker is illustrative, not a calculated result for the demonstration caption. | Fuzzy logic combines them into a doomscrolling risk score. |
| 07 · You stay in control | 00:32–00:40 | Each phone lights up on its spoken cue. End with the unlocked choice visible. The breathing animation is an excerpt of the actual 45-second exercise. | When needed: a reminder, a pause, or a breathing break. You stay in control. |
| 08 · Private by design | 00:40–00:43 | The real export screen supports two large, immediately readable privacy statements. | Processed on-device. Raw content isn’t saved. |
| 09 · Pilot results | 00:43–00:50 | Two clear outcome cards replace the dense statistics grid. Supporting text identifies the logging-only comparison and short-term pilot context. | In a two-week pilot with fifty Filipino adults, prompted users scrolled less and saw less negative content. |
| 10 · Choose the pause | 00:50–00:54 | The logo returns. Two tagline lines land with the VO and the mascot gives a small wink. | REDU. Notice the scroll. Choose the pause. |
| 11 · Credits | 00:54–00:59 | A five-second hold. All credits are present from the start and the final fade completes within the runtime. | Music only. |

## Visual hierarchy

- Retain the dark opening, warm paper, Manrope typography, REDU palette, logo and app captures.
- Render 2:07 AM as a seven-segment digital clock, with a dark casing and softly illuminated segments.
- Give negative captions seven seconds, from 00:21 to 00:28. Pull “Grabe, nakakalungkot naman.” out of the phone into the main frame at 84 px. Highlight “nakakalungkot” in coral and show a large “Negative” label. The fully readable example holds for 4.4 seconds before returning to the three-signal view.
- Keep “No usable text → on-device vision fallback” as a small, unnumbered note beneath caption processing, with no separate card, entrance, narration or score connection.
- The three inputs are session length, time per video and negativity, demonstrated through negative captions. Visual emphasis does not change the estimator’s input weights.
- Name the output “Doomscrolling risk score.” The 0–100 meter uses the app-facing Low / Elevated / High labels. Its marker is illustrative; it does not assert a computed score for the demo caption.
- Show the two-week pilot, 50 Filipino adults and two main outcomes. Omit the effect-size grid, logged-session total, SUS badge and expert-rating badges from this cut.
- Keep developer, adviser, institution and production credits. All credit text is present from 00:54; the fade completes by 00:59.

## Updated VO handoff

Use [VOICEOVER.txt](VOICEOVER.txt) as the recording copy. Supply one recording at its natural speed, with narration ending by 00:54. A longer take needs shortened copy or a revised recording. Do not time-stretch the voice or cut words to meet the cap.

When the new recording arrives:

1. Retain the supplied source in this draft’s public/audio/vo folder and normalize a separate production copy.
2. Measure word onsets and update the shared BEATS cues in src/timeline.ts. Match each readable word, signal card, caption highlight, prompt emphasis and sound cue to the relevant spoken phrase. Give “Not” its own measured cue.
3. Integrate the updated recording and shared sound cues into AVP.tsx; regenerate the original music for this timeline. Preserve the source recording’s speed and pauses.
4. Inspect synchronization frame by frame. Words render sharply at their onset so blur does not make them appear late. Update the narration credit if the final voice changes.
5. Export only after that pass. Verify the actual container, video stream and audio stream durations are all ≤60.000 seconds, including tails. The planned picture length is 1,770 frames.

## Review and comparison

- Open [out/storyboard.png](out/storyboard.png) for all 12 panels, including separate signal-overview and caption-close-up panels.
- Run `npm run dev` and choose `Storyboard`, `AVP` (silent), or a scene under `Scenes`.
- Run `npm run storyboard` to rebuild the contact sheet.
- Run `npm run lint` and `npm run check:storyboard` to validate source, the 59-second budget, the caption hold and draft-1 integrity.
- The original project remains at `../my-video`. A copied first-draft storyboard, MP4 and production references are in [reference/draft-1](reference/draft-1). The saved manifest records hashes of its source, assets and outputs before this work.
- No draft-2 audio/video export is represented as final while the updated VO is pending. The copied first-draft audio is reference material and is not played by the silent draft-2 animatic.

## Claim and asset sources

- `../acm-paper/paper.md`: three fuzzy inputs, 0–100 risk estimate, text-first analysis, no-text vision fallback, two-week pilot with 50 adults, comparison outcomes, and on-device processing.
- `../android-app/app/src/main/java/edu/feutech/redu/sentiment/MvlLexicon.kt`: “nakakalungkot” is an actual Filipino lexicon entry (−3). The video uses the plain-language “Negative” label.
- App screenshots, icon, mascot, prompt reconstructions and production credits are copied from draft 1. See [the first-draft storyboard](reference/draft-1/STORYBOARD.md) for their detailed provenance.

## Verification status

- Source lint/type checks and storyboard timing checks are run for this delivery.
- Contact sheet and key frames are visually reviewed before delivery.
- Final word-to-audio synchronization and final MP4/container duration checks await the updated recording and export.
