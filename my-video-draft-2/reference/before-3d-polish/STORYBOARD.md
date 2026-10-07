# REDU AVP — Draft 2 storyboard

**Current revision:** the user-supplied October 6 Lauren recording, a text-only opening followed by “Then another,” continuous caption-to-score pacing, and updated credits. Visual redesign remains in 01, 05 and 06. Other first-draft layouts, including the complete pilot-results screen, are retained.

**59.00 seconds of picture, including five seconds of credits. Hard limit: 60.000 seconds.** 1920 × 1080, 30 fps, 1,770 frames.

![12-panel storyboard](out/storyboard.png)

| Panel | Time | Visual and action | Recorded VO |
|---|---|---|---|
| 01 · Just one more | 00:00.00–00:09.13 | Open on text only. Bring in the phone and begin scrolling on ‘Then another.’ Let the feed accelerate, then resolve into the seven-segment 2:07 AM clock on its spoken cue. | Just one more video... Then another. Suddenly, it’s two-oh-seven A.M. |
| 02 · The problem | 00:09.13–00:14.33 | Oversized kinetic word, then a screen-time pill and a two-line contrast with the key phrase in periwinkle. | That's doomscrolling. Screen-time limits count minutes, not what you watch. |
| 03 · Meet REDU | 00:14.33–00:20.33 | A sage breathing circle inhales and blooms into cream paper; the app icon and wordmark lock up, then the purpose line. | Meet REDU: a privacy-first Android app that notices doomscrolling, and helps you pause. |
| 04 · Who it's for | 00:20.33–00:24.30 | Real capture: the app's platform setup screen, with the three supported apps switched on. | Built for adult Filipino Android users who watch short-form video. |
| 05A · Three signals · same layout | 00:24.30–00:27.03 | Phone on the right. Signal cards on the left. Keep this scene and phone continuously visible for the close-up. | Three signals: session length, time per video… |
| 05B · Phone grows into the close-up | 00:27.03–00:28.93 | Enlarge and reposition the entire phone on the right. Keep its bezel and feed intact beyond the frame; focus on the highlighted caption. Negative-caption card stays left. Flow into the score with the VO’s natural break. | …and negative captions. |
| 06 · Doomscrolling risk score | 00:28.93–00:32.43 | The three named inputs combine into the 0–100 Low / Elevated / High meter. Use the explicit doomscrolling risk score label and identify the marker as illustrative. | Fuzzy logic combines them into a doomscrolling risk score. |
| 07 · Gentle prompts | 00:32.43–00:40.27 | Three phones: the Level 1 banner, the real Level 2 pause capture (Keep scrolling unlocks), and the Level 3 breathing circle. | When it rises, REDU steps in gently: a reminder, a pause, then a breathing break. You stay in control. |
| 08 · Private by design | 00:40.27–00:43.83 | Real capture: the export screen ("Aggregate data only") beside a three-point privacy checklist. | It all runs on your phone, and raw content is never kept. |
| 09 · Pilot results | 00:43.83–00:50.13 | Counting stats, then four outcome cards with down arrows, usability and expert scores, and the study fine print. | In a two-week pilot with fifty Filipino adults, prompted users scrolled less, and saw less negative content. |
| 10 · Logo & tagline | 00:50.13–00:54.00 | Logo lockup returns; the tagline blurs in line by line and the mascot winks. | REDU. Notice the scroll. Choose the pause. |
| 11 · Credits | 00:54.00–00:59.00 | Clean credit card on paper; fades to black. | Music only. |

## Opening and motion

“Just one more video…” opens on text alone. The phone enters on “Then,” with five accelerating feed swipes. “Then another.” stays beside it. The music rises during the scrolling interval, before the illuminated seven-segment 2:07 AM clock resolves on “two-oh-seven.”

The whole phone grows on the right for 05B. Its bezel, screen, username, caption and controls remain one object, including the portions outside the picture. The negative-caption card stays left. The approximately 84 px coral-highlighted “nakakalungkot” is readable at reduced playback size. The small vision fallback note remains secondary.

The inserted caption pause is removed. Its natural narration break is 0.428 seconds; the settled close-up lasts 0.70 seconds before its eased exit. The score follows immediately. Blur-and-rise type, staggered logo lettering, eased exits, the bottom-anchored phone camera and the spring-driven score marker are preserved. Movement uses twelve fractional-frame samples with a 180-degree shutter.

“Made with Remotion” is removed from visible credits. Thesis, developer, adviser, institution, narration and other asset credits remain.

## Recording and synchronization

The [117-word recording script](VOICEOVER.txt) matches the supplied take. Its original MP3 is saved byte-for-byte at public/audio/vo/opening-revision-original.mp3 (51.069 seconds). The production WAV is 53.600 seconds at 1× speed. No speech or source silence is cut. The only added time is 2.531 seconds of silence between “Then another” and “Suddenly,” filled by the animated feed, music and swipe sounds. No time is inserted after the captions line. Every aligned spoken sample is verified unchanged before loudness normalization.

Word timing was measured locally using a CTC acoustic alignment model, with phrase onsets refined against the waveform’s measured silence boundaries. The audio and transcript were not uploaded. Original alignment output and refinement notes are saved in [the audit folder](production/voiceover/opening-revision). Shared cues drive text, camera emphasis and SFX; readable text includes the entrance animation’s lead time.

Key cue frames: “Then” 70; digital clock 223; “Not” 390; session length 771; time per video 800; negative captions 835; reminder 1062; pause 1092; breathing 1131. Credits occupy frames 1,620–1,769.

## Review and preservation

- [Current MP4](out/REDU-AVP-draft-2-opening-revision.mp4) · [Full-size panels](out/panels) · [Comparison](COMPARE.html).
- [Text-only opening](out/check/opening-text-only.png) · [Then another](out/check/then-another.png) · [Reduced caption view](out/check/caption-640.png).
- The first-draft project at ../my-video is untouched; 96 source, asset and export hashes are checked. Copies and the manifest remain in reference/draft-1/.
- The previous reviewed motion revision, its source, scripts, audio and preview are preserved in reference/before-opening-revision/. Its MP4 also remains at out/REDU-AVP-draft-2-motion-polish.mp4. Earlier archives remain in reference/.
- Run npm run lint, npm run check:storyboard, npm run storyboard:panels and npm run render. Run python3 scripts/verify-motion-export.py for encoded-frame, audio-offset, loudness, duration and preservation checks. The saved [export report](production/voiceover/opening-revision/verification.json) records the measured result.

## Claim provenance

The thesis at ../acm-paper/paper.md supports the three-signal fuzzy estimate, text-first sentiment processing, vision fallback, on-device privacy and pilot claims. “Nakakalungkot” appears in the Filipino lexicon at valence −3. The 38% card and meter marker are illustrative session values. Pilot graphics and supporting study fine print retain the first-draft content. App captures and asset credits retain their [first-draft provenance](reference/draft-1/STORYBOARD.md).

**Export verified:** 59.000 seconds of video; 59.008 seconds including the audio tail. All 1,770 frames decode, twelve sampled speech points have no measured export offset, and the first draft plus previous motion export remain unchanged.

[Encoded-frame review](out/opening-export-checks/encoded-frame-review.png) · [Verification report](production/voiceover/opening-revision/verification.json).
