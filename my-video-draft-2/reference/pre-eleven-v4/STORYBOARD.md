# REDU AVP — Draft 2 storyboard

**Revision scope: polish scenes 01, 05 and 06 only.** Scenes 02–04 and 07–11 retain the first draft’s content, visual treatment, motion and local timing. The original pilot-results screen is restored in full.

**Target: 59.00 seconds, including five seconds of credits. Absolute limit: 60.000 seconds.** 1920 × 1080, 16:9, 30 fps, 1,770 frames. Updated VO pending; the current animatic is silent.

![Draft 2 storyboard](out/storyboard.png)

## Storyboard

| Panel | Time | Visual and action | Working VO |
|---|---|---|---|
| 01 · Just one more | 00:00.00–00:04.50 | Keep the first-draft scrolling hook, ending on a softly illuminated seven-segment 2:07 AM clock. | Just one more video. Suddenly, it’s two-oh-seven AM. |
| 02 · The problem | 00:04.50–00:10.00 | **Retained from draft 1.** Oversized kinetic word, then a screen-time pill and a two-line contrast with the key phrase in periwinkle. | That's doomscrolling. Screen-time limits count minutes, not what you watch. |
| 03 · Meet REDU | 00:10.00–00:16.00 | **Retained from draft 1.** A sage breathing circle inhales and blooms into cream paper; the app icon and wordmark lock up, then the purpose line. | Meet REDU: a privacy-first Android app that notices doomscrolling, and helps you pause. |
| 04 · Who it's for | 00:16.00–00:20.27 | **Retained from draft 1.** Real capture: the app's platform setup screen, with the three supported apps switched on. | Built for adult Filipino Android users who watch short-form video. |
| 05A · Three signals · same layout | 00:20.27–00:23.00 | Phone on the right. Signal cards on the left. Keep this scene and phone continuously visible for the close-up. | Three signals: session length, time per video… |
| 05B · Phone grows into the close-up | 00:23.00–00:27.20 | Enlarge and reposition the entire phone on the right. Keep its bezel and feed intact beyond the frame; focus on the highlighted caption. Negative-caption card stays left. Hold ≥3 seconds. | …and negative captions. |
| 06 · Doomscrolling risk score | 00:27.20–00:30.53 | The three named inputs combine into the 0–100 Low / Elevated / High meter. Use the explicit doomscrolling risk score label and identify the marker as illustrative. | Fuzzy logic combines them into a doomscrolling risk score. |
| 07 · Gentle prompts | 00:30.53–00:39.27 | **Retained from draft 1.** Three phones: the Level 1 banner, the real Level 2 pause capture (Keep scrolling unlocks), and the Level 3 breathing circle. | When it rises, REDU steps in gently: a reminder, a pause, then a breathing break. You stay in control. |
| 08 · Private by design | 00:39.27–00:43.20 | **Retained from draft 1.** Real capture: the export screen ("Aggregate data only") beside a three-point privacy checklist. | It all runs on your phone, and raw content is never kept. |
| 09 · Pilot results | 00:43.20–00:49.93 | **Retained from draft 1.** Counting stats, then four outcome cards with down arrows, usability and expert scores, and the study fine print. | In a two-week pilot with fifty Filipino adults, prompted users scrolled less, and saw less negative content. |
| 10 · Logo & tagline | 00:49.93–00:54.00 | **Retained from draft 1.** Logo lockup returns; the tagline blurs in line by line and the mascot winks. | REDU. Notice the scroll. Choose the pause. |
| 11 · Credits | 00:54.00–00:59.00 | **Retained from draft 1.** Clean credit card on paper; fades to black. | Music only. |

## Changes in this revision

- **01 — Digital clock:** keep the scrolling hook and end on an illuminated seven-segment 2:07 AM display. This scene’s budget is 4.5 seconds.
- **05A — Three signals:** use one continuous layout, with the phone on the right and session length, time per video, and negative-caption cards on the left. The illustrative values are 23 min, 41 s and 38%.
- **05B — The entire phone grows:** scale and reposition the complete phone on the right, keeping its bezel, screen, username, caption, sound metadata and controls together as one intact object. Its top and right edges move beyond the video frame; the visible left and bottom bezel keep the phone recognizable. Only the outer video frame clips it. The negative-caption card stays on the left and gains emphasis while the other two cards recede.
- **Caption treatment:** “Grabe, / nakakalungkot / naman.” remains inside the actual feed component. Scaling the entire phone enlarges the 14.5 dp caption to approximately 84 px. Highlight “nakakalungkot” in coral at the close-up cue. All other feed elements retain their original appearance and positions within the phone, including the parts beyond the frame. The readable close-up holds for 3.13 seconds.
- **Vision model:** retain the small unnumbered “No usable text → on-device vision fallback” note under the left-side signal card. It is supporting processing, without a fourth card or connection to the score.
- **06 — Score wording:** use “Doomscrolling risk score” over the three-input, 0–100 Low / Elevated / High meter. Its marker is illustrative, not a computed claim about this single example caption.
- **09 — Original results restored:** show the original two-week / 50-person / 10,134-session sequence, four effect-size cards, SUS 80.95, expert ratings 5.00 and 4.33, and original study footnote.

Only scenes 01, 05 and 06 have revised durations. The other scenes keep their original durations and local animation timing; their placement shifts as the modified scenes are retimed. Narration still ends by 00:54, followed by five seconds of credits.

## Updated VO handoff

[VOICEOVER.txt](VOICEOVER.txt) retains the original narration for all unchanged scenes and supplies working lines for 01, 05 and 06. Use one natural-speed recording with narration complete by 00:54.

1. Retain the new supplied source and normalize a separate production copy within this draft’s public/audio/vo folder.
2. Measure word onsets and align the revised-scene BEATS cues, camera move, highlight, score and SFX. Preserve the first-draft visual design in the other scenes while aligning their cues to the new recording; recheck “Not” explicitly.
3. Integrate the new recording into AVP.tsx and regenerate music for the final timeline. Do not speed up the voice or truncate words. A take longer than the budget needs revised copy or a new take.
4. Verify the rendered container, video and audio durations are each ≤60.000 seconds, including fades and tails. Planned video length: 1,770 frames.

## Review, comparison and preservation

- [12-panel preview](out/storyboard.png) · [Full-size panels](out/panels) · [Side-by-side comparison](COMPARE.html).
- Run `npm run dev` and open `Storyboard`, `AVP` (silent), or a scene under `Scenes`.
- Run `npm run storyboard:panels` to render the contact sheet, twelve panels and camera-move checks.
- Run `npm run lint` and `npm run check:storyboard`. The checks enforce the runtime, caption hold, byte-identical unchanged scene components, unchanged scene metadata, and integrity of the first-draft source/assets/outputs.
- The original project stays at `../my-video`; [reference/draft-1](reference/draft-1) contains copied original outputs and an integrity manifest. The previous broader draft-2 treatment is saved in [reference/draft-2-previous](reference/draft-2-previous).
- The previous cropped 05B source and preview are saved in [reference/05b-before-whole-phone](reference/05b-before-whole-phone) for comparison.

## Sources and verification

- `../acm-paper/paper.md` supports the three-input fuzzy estimate, the text-first sentiment path, vision fallback, on-device privacy and pilot claims.
- “Nakakalungkot” is present in the app’s Filipino lexicon at valence −3. The 38% card is an illustrative session metric, not the valence of that word.
- App captures and production credits retain their first-draft provenance; see [the original storyboard](reference/draft-1/STORYBOARD.md).
- Source/type checks, timing checks and rendered key-frame inspection are performed for this delivery. Final audio synchronization and final MP4 duration verification await the new VO and export.
