# REDU AVP — Draft 2 storyboard

**Visual revision scope: polish scenes 01, 05 and 06.** Scenes 02–04 and 07–11 retain the first draft’s content and layout, including the full original pilot-results screen. Cues and scene boundaries throughout the cut now follow the supplied Lauren v4 narration.

**59.00 seconds, including five seconds of credits. Absolute limit: 60.000 seconds.** 1920 × 1080, 16:9, 30 fps, 1,770 frames. Lauren v4 VO: 53.600 seconds, original speaking speed, silence-only edits. Music and sound effects are integrated.

![Draft 2 storyboard](out/storyboard.png)

## Storyboard

| Panel | Time | Visual and action | Recorded VO |
|---|---|---|---|
| 01 · Just one more | 00:00.00–00:05.57 | Keep the first-draft scrolling hook, ending on a softly illuminated seven-segment 2:07 AM clock. | Just one more video. Suddenly, it’s two-oh-seven AM. |
| 02 · The problem | 00:05.57–00:10.67 | **Draft-1 layout.** Oversized kinetic word, screen-time pill and two-line contrast. Each word is readable on its spoken cue, including “Not.” | That's doomscrolling. Screen-time limits count minutes, not what you watch. |
| 03 · Meet REDU | 00:10.67–00:16.63 | **Draft-1 layout.** Sage breathing circle blooms into cream paper; logo and purpose lines follow their spoken cues. | Meet REDU: a privacy-first Android app that notices doomscrolling, and helps you pause. |
| 04 · Who it's for | 00:16.63–00:20.67 | **Draft-1 layout.** Platform-setup capture with TikTok, Instagram Reels and Facebook Reels identifiers. | Built for adult Filipino Android users who watch short-form video. |
| 05A · Three signals · same layout | 00:20.67–00:23.53 | Phone on the right, signal cards on the left. Keep this scene and phone continuously visible for the close-up. | Three signals: session length, time per video… |
| 05B · Phone grows into the close-up | 00:23.53–00:27.73 | Enlarge and reposition the entire phone on the right. Keep bezel and feed intact beyond the frame. Highlight the caption on “negative”; the negative-caption card stays left. Fully readable hold: 3.000 seconds. | …and negative captions. |
| 06 · Doomscrolling risk score | 00:27.73–00:31.40 | The three named inputs combine into the 0–100 Low / Elevated / High meter. Marker settles on “score” and is labeled illustrative. | Fuzzy logic combines them into a doomscrolling risk score. |
| 07 · Gentle prompts | 00:31.40–00:39.27 | **Draft-1 layout.** Each of the three phones gains emphasis on its spoken prompt. Keep-scrolling choice is highlighted for “You stay in control.” | When it rises, REDU steps in gently: a reminder, a pause, then a breathing break. You stay in control. |
| 08 · Private by design | 00:39.27–00:43.13 | **Draft-1 layout.** Export capture and three-point privacy checklist follow the narration. | It all runs on your phone, and raw content is never kept. |
| 09 · Pilot results | 00:43.13–00:50.13 | **Full draft-1 results screen.** Counting stats, four outcome cards, usability and expert scores, and original study fine print. | In a two-week pilot with fifty Filipino adults, prompted users scrolled less, and saw less negative content. |
| 10 · Logo & tagline | 00:50.13–00:54.00 | **Draft-1 layout.** Logo, two spoken tagline beats and mascot wink. | REDU. Notice the scroll. Choose the pause. |
| 11 · Credits | 00:54.00–00:59.00 | **Retained from draft 1.** Clean credit card on paper; fades to black. | Music only. |

## Changes in this revision

- **01 — Digital clock:** keep the scrolling hook and end on an illuminated seven-segment 2:07 AM display, cued to “two-oh-seven.” The recorded opening occupies 5.567 seconds.
- **05A — Three signals:** use one continuous layout, with the phone on the right and session length, time per video, and negative-caption cards on the left. The illustrative values are 23 min, 41 s and 38%.
- **05B — The entire phone grows:** scale and reposition the complete phone on the right, keeping its bezel, screen, username, caption, sound metadata and controls together as one intact object. Its top and right edges move beyond the video frame; the visible left and bottom bezel keep the phone recognizable. Only the outer video frame clips it. The negative-caption card stays on the left and gains emphasis while the other two cards recede.
- **Caption treatment:** “Grabe, / nakakalungkot / naman.” remains inside the actual feed component. Scaling the entire phone enlarges the 14.5 dp caption to approximately 84 px. Highlight “nakakalungkot” in coral at the close-up cue. All other feed elements retain their original appearance and positions within the phone, including the parts beyond the frame. The readable close-up holds for 3.00 seconds.
- **Vision model:** retain the small unnumbered “No usable text → on-device vision fallback” note under the left-side signal card. It is supporting processing, without a fourth card or connection to the score.
- **Motion treatment:** restore the first draft’s blur-and-rise text, staggered logo letters, 8–10-frame exits and spring-based score marker. Add sampled motion blur during the fast phone/scroll and score-input movement. The complete-phone camera path uses perceptual scale and a bottom anchor, with a sharp, stationary three-second caption hold.
- **06 — Score wording:** use “Doomscrolling risk score” over the three-input, 0–100 Low / Elevated / High meter. Its marker is illustrative, not a computed claim about this single example caption.
- **09 — Original results restored:** show the original two-week / 50-person / 10,134-session sequence, four effect-size cards, SUS 80.95, expert ratings 5.00 and 4.33, and original study footnote.

Scene durations and entrance cues are adjusted to the measured recording. The visual redesign remains confined to 01, 05 and 06. Narration ends within 00:54, followed by five seconds of credits.

## Supplied VO and synchronization

[VOICEOVER.txt](VOICEOVER.txt) is the 115-word script spoken in the supplied Lauren v4 recording. No replacement TTS was generated. The original MP3 is preserved byte-for-byte at `public/audio/vo/draft-2-original.mp3` (55.066 seconds).

- **Production recording:** 53.600 seconds. Tighten 3.566 seconds of measured silence across the take; insert 2.100 seconds in the silent gap after “negative captions” for caption reading. Every spoken word remains at 1× speed. All aligned spoken samples are verified unchanged before loudness normalization. The original and the edited WAV are both retained.
- **Measured cues:** ElevenLabs forced alignment provides word onsets. The edit map carries those onsets into the production recording. `src/voiceover-cues.ts` supplies shared visual and SFX cues; each narrated word stays inside its scene. Text enters through the first draft’s visible blur-and-rise easing, resolves to readable type around the spoken cue, then finishes settling. No opacity gate hides the entrance. The clock and logo also retain animated entrances.
- **Key checks:** “Not” at global frame 284; session length at 661; time per video at 691; negative-caption emphasis at 730; reminder at 1,038; pause at 1,072; breathing at 1,105. The caption close-up is fully readable for frames 734–823, a 90-frame / 3.000-second hold.
- **Mix and end:** normalize the narration separately, duck music under speech, align SFX to the same scene cues, and finish music/fades inside 59 seconds. Credits occupy frames 1,620–1,769.
- **Audit files:** `production/voiceover/supplied-lauren-v4/` contains source provenance, raw alignment, silence edits, production word timings and scene boundaries. The preparation script checks original audio integrity and spoken-sample preservation.

## Review, comparison and preservation

- [Draft-2 MP4](out/REDU-AVP-draft-2-motion-polish.mp4) · [12-panel preview](out/storyboard.png) · [Full-size panels](out/panels) · [Side-by-side comparison](COMPARE.html).
- Run `npm run dev` and open `Storyboard`, `AVP` with narration/music/SFX, or a scene under `Scenes`.
- Run `npm run storyboard:panels` to render the contact sheet, twelve panels, camera checks, reduced-size caption and exact cue-frame checks.
- Run `npm run lint` and `npm run check:storyboard`. These enforce the runtime, caption hold, measured key word cues, preserved scene content, original VO integrity and 96 first-draft source/asset/output hashes.
- Run `npm run render` to export, then automatically verify actual container/video/audio durations ≤60.000 seconds, 1,770 video frames and 30 fps.
- The original project stays at `../my-video`; [reference/draft-1](reference/draft-1) contains copied original outputs and an integrity manifest. The previous broader draft-2 treatment is saved in [reference/draft-2-previous](reference/draft-2-previous).
- The previous cropped 05B source and preview are saved in [reference/05b-before-whole-phone](reference/05b-before-whole-phone) for comparison.
- The previous delivered revision is archived in [reference/before-motion-repair](reference/before-motion-repair), including its MP4 and source.
- The draft-2 source and preview from before this narration integration are saved in [reference/pre-eleven-v4](reference/pre-eleven-v4).

## Sources and verification

- `../acm-paper/paper.md` supports the three-input fuzzy estimate, the text-first sentiment path, vision fallback, on-device privacy and pilot claims.
- “Nakakalungkot” is present in the app’s Filipino lexicon at valence −3. The 38% card is an illustrative session metric, not the valence of that word.
- App captures and production credits retain their first-draft provenance; see [the original storyboard](reference/draft-1/STORYBOARD.md).
- Source/type checks, measured-cue checks and rendered key-frame inspection passed for “Not,” the signal names, whole-phone camera move, caption hold at full and reduced size, and the three prompt labels.
- **Export verified:** video is 59.000 seconds / 1,770 frames at 30 fps; audio and container are 59.008 seconds including the AAC tail, below the 60.000-second limit. The decoded mix matches the production narration with a measured 0 ms offset at a speech sample from each scene. The final frame has faded to the dark background. The prior audio integration report is [verification.json](production/voiceover/supplied-lauren-v4/verification.json); the motion revision has a separate [export report](production/voiceover/supplied-lauren-v4/motion-verification.json).
