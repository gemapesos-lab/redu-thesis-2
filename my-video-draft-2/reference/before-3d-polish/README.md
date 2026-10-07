# REDU AVP — Draft 2

Independent second-draft project. The original at `../my-video` is preserved; 96 original source, asset and output hashes are checked.

**Current cut:** 59 seconds of picture, 1,770 frames at 30 fps, including five seconds of credits. Uses the user-supplied October 6 Lauren recording at its original speaking speed.

**Export verified:** 59.000 seconds of video; 59.008 seconds including the audio tail. All 1,770 frames decode, twelve sampled speech points have no measured export offset, and the first draft plus previous motion export remain unchanged.

## Review

- [Current MP4](out/REDU-AVP-draft-2-opening-revision.mp4)
- [12-panel storyboard](out/storyboard.png)
- [Written storyboard](STORYBOARD.md)
- [Recording script](VOICEOVER.txt)
- [Draft comparison](COMPARE.html)
- [Export verification](production/voiceover/opening-revision/verification.json)
- [Previous motion revision](out/REDU-AVP-draft-2-motion-polish.mp4)
- [First draft](reference/draft-1/out/REDU-AVP.mp4)

The opening starts with “Just one more video…” as text alone. The phone enters on “Then another,” and five accelerating swipes lead to the seven-segment 2:07 AM clock. The inserted caption pause is removed. The complete phone still enlarges on the right, with the negative-caption card on the left. “Made with Remotion” is removed from visible credits. The original pilot-results layout and restored motion blur remain.

Only scenes 01, 05 and 06 have redesigned visuals. All scenes use cues measured from the new recording. The warm palette, typography, app captures, blur-and-rise text, staggered logo, eased exits and spring-driven score marker remain.

## Voiceover

The untouched supplied MP3 is `public/audio/vo/opening-revision-original.mp3` (51.069 seconds). The working WAV is 53.600 seconds: 2.531 seconds are added in a silent opening interval after “Then another,” filled by scrolling, music and SFX. No speech or source silence is removed. Nothing is inserted after “negative captions”; its natural break before “Fuzzy logic” is 0.428 seconds.

Word timings were aligned locally with an acoustic model and refined against measured phrase boundaries. No recording or transcript upload was needed. `production/voiceover/opening-revision/` retains the source hash, raw alignments, refinement information, word cues, edit map and verification report. Every aligned spoken sample is verified unchanged before loudness normalization. `src/voiceover-cues.ts` shares visual and SFX timing. Credits run 54–59 seconds.

## Preview and rebuild

```sh
npm run dev
python3 scripts/prepare-opening-voiceover.py
node --disable-warning=MODULE_TYPELESS_PACKAGE_JSON scripts/build-voice-cues.mjs
npm run audio:vo
npm run audio:music
npm run lint
npm run check:storyboard
npm run storyboard:panels
npm run render -- out/REDU-AVP-draft-2-opening-revision.mp4 --concurrency=2 --timeout=120000
python3 scripts/verify-motion-export.py
```

Studio runs on port 3002. The saved alignment is sufficient for a rebuild; no API request or model download is required. If a fresh alignment is needed, the local CTC workflow is `scripts/align-ctc-voiceover.py`, using the pinned environment and model weights under `.tools/`.

The export guard checks actual video/audio/container duration, frame rate and frame count. The verification script decodes every frame, checks the score for black-frame regressions, compares twelve speech samples for audio offsets, measures loudness, and verifies that the first draft and previous motion export remain unchanged.

## Preservation

`reference/before-opening-revision/` preserves the previous reviewed source, scripts, audio, storyboard, documentation and MP4. The earlier exports `out/REDU-AVP-draft-2-motion-polish.mp4` and `out/REDU-AVP-draft-2.mp4` remain untouched. The new cut uses a distinct filename.

Other preserved revisions remain under `reference/draft-1`, `reference/draft-2-previous`, `reference/05b-before-whole-phone`, `reference/pre-eleven-v4` and `reference/before-motion-repair`. All current work and outputs live in this second-draft folder.
