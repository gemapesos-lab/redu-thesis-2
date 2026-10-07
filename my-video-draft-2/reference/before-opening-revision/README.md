# REDU AVP — Draft 2

Independent copy of the first draft. All edits, assets, source files, dependencies and outputs for this revision live here. The original project at `../my-video` remains intact.

**Status:** supplied Lauren v4 narration integrated and aligned. **59.00 seconds**, including five seconds of credits; **1,770 frames at 30 fps**. Absolute limit **60.000 seconds**.

**Export verified:** 59.000-second video; 59.008-second audio/container including the AAC tail. Lint, timing, original-file integrity and export checks passed. [Motion revision verification](production/voiceover/supplied-lauren-v4/motion-verification.json).

## Review

- [12-panel storyboard](out/storyboard.png)
- [Draft-2 MP4](out/REDU-AVP-draft-2-motion-polish.mp4)
- [Written storyboard and timing notes](STORYBOARD.md)
- [Clean recording script](VOICEOVER.txt)
- [Side-by-side comparison](COMPARE.html)
- [Full-resolution panels](out/panels)
- [First-draft storyboard](reference/draft-1/out/storyboard.png)
- [First-draft MP4](reference/draft-1/out/REDU-AVP.mp4)

Only scenes **01, 05 and 06** have redesigned visuals: digital clock, continuous enlargement of the entire phone on the right to focus on its caption with the signal card on the left, and the explicit “Doomscrolling risk score” label. The phone's bezel and all feed elements remain intact as parts extend beyond the video frame. The other scenes retain their first-draft content and layout, including the full original pilot-results screen. Cues throughout the cut follow the new recording.

## Motion repair

The visible blur-and-rise entrances and letter-by-letter logo animation are restored. Text becomes readable around the recorded cue, with a short settling tail; there is no hard opacity gate. Scene exits regain their original 8–10-frame easing. Fast scrolls, the complete-phone zoom and score-input movement use sampled motion blur. Twelve fractional-frame samples use normal alpha averaging to retain the warm palette. The camera scales perceptually while preserving the phone’s bottom anchor, and the score marker uses the first draft’s spring motion.

The previous delivered video remains at `out/REDU-AVP-draft-2.mp4`. Its source, scripts, previews and verification are archived in `reference/before-motion-repair/`. The new file has a distinct name to avoid playback caching and make comparison easy.

## Preview and rebuild

```sh
npm run dev
npm run lint
npm run check:storyboard
npm run storyboard
npm run storyboard:panels
```

Studio uses port 3002, keeping this project separate from the first draft. Choose `Storyboard`, `AVP` with narration/music/SFX, or an individual scene. The stills render the same components used in the video.

`storyboard` rebuilds the contact sheet. `storyboard:panels` rebuilds it plus all twelve full-resolution panels and focused camera-move checks. If reinstalling elsewhere, run `npm ci` first.

## Voiceover and export

The user-supplied Lauren v4 MP3 is preserved byte-for-byte as `public/audio/vo/draft-2-original.mp3`. It is 55.066 seconds long. The production copy is 53.600 seconds: 3.566 seconds of measured silence were removed and 2.100 seconds were added after “negative captions” to support the close-up. Every spoken word remains at its original speed, and the preparation script verifies that every aligned spoken sample is unchanged before loudness normalization. The final WAV is normalized for the stereo mix.

`production/voiceover/supplied-lauren-v4/` contains the original source hash, ElevenLabs forced alignment, silence edit map, production word timings and scene timings. `src/voiceover-cues.ts` supplies shared visual and sound cues. The caption close-up holds fully readable for 3.000 seconds. Credits run from 54.000 to 59.000 seconds.

To rebuild from the supplied recording:

```sh
python3 scripts/prepare-supplied-voiceover.py
node --disable-warning=MODULE_TYPELESS_PACKAGE_JSON scripts/build-voice-cues.mjs
npm run audio:vo
npm run audio:music
npm run storyboard:panels
npm run render -- out/REDU-AVP-draft-2-motion-polish.mp4 --concurrency=2 --timeout=120000
python3 scripts/verify-motion-export.py
```

`npm run render` checks the storyboard, exports the MP4, then checks actual video/audio/container durations, frame rate and frame count. `verify-motion-export.py` decodes all frames, checks for the score's black-frame regression, measures narration offsets and loudness, and saves the motion export report. No TTS generation or API request is needed to rebuild from the saved recording and alignment.

## Preservation

`reference/draft-1/` contains the copied original storyboard, outputs and an SHA-256 manifest of the original source/assets/outputs. `check:storyboard` verifies that the source project still matches those saved hashes. No source or asset links point back to draft 1. Dependencies were copied, too.

To compare or return to draft 1, open `../my-video` or its preserved MP4. Draft-2 renders always go to this folder’s own `out/`.

The previous broader draft-2 treatment is archived in `reference/draft-2-previous/`.

`reference/pre-eleven-v4/` preserves the draft-2 source, scripts, documentation, preview and music from before this narration integration. `reference/05b-before-whole-phone/` preserves the earlier cropped-phone treatment.
