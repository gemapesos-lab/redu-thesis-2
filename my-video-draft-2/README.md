# REDU AVP — Draft 2

Independent second-draft project. The original at `../my-video` is preserved; 96 original source, asset and output hashes are checked.

**Current cut: 3D polish.** 59 seconds of picture, 1,770 frames at 30 fps, including five seconds of credits. Every scene is rebuilt with solid 3D objects and a scene camera, key lines keep the word-by-word blur-and-rise on their spoken words, other lines appear whole, and no text overlaps, and 44 original synthesized effects follow the motion. It is timed to the user-supplied October 7 recording at its original speaking speed.

**Export verified:** 59.000 seconds of video; 59.008 seconds including the audio tail. All 1,770 frames decode, thirteen sampled speech points show no export offset, loudness is −16.2 LUFS integrated with a −1.8 dBTP true peak, and the first draft plus both previous draft-2 exports remain unchanged.

## Review

- [Current MP4](out/REDU-AVP-draft-2-3d-polish-v2.mp4) · [first 3D-polish export](out/REDU-AVP-draft-2-3d-polish.mp4)
- [12-panel storyboard](out/storyboard.png) · [full-size panels](out/panels) · [check frames](out/check-3d-polish)
- [Written storyboard](STORYBOARD.md)
- [Recording script](VOICEOVER.txt) · [recording brief](production/voiceover/3d-polish/recording-brief.md)
- [Draft comparison](COMPARE.html)
- [Export verification](production/voiceover/3d-polish/verification.json)
- [Previous cut: opening revision](out/REDU-AVP-draft-2-opening-revision.mp4)
- [First draft](reference/draft-1/out/REDU-AVP.mp4)

The opening is text alone; on “Then,” a 3D phone rises and the camera orbits it as six swipes speed up, then racks focus to a red LED alarm clock that ticks from 2:06 to 2:07 on “seven.” The same phone carries the users scene into the signals scene with a full spin, then grows into the “nakakalungkot” close-up. The signal cards fly into a 3D risk meter, which tips flat into a track where each prompt phone rises at its risk band. Stat tiles flip up and away before the pilot results arrive, and the 3D icon spins in for the tagline.

## Voiceover

The untouched supplied MP3 is `public/audio/vo/3d-polish-original.mp3` (52.689 seconds): the October 6 script plus “And another…”. The working WAV is 53.589 seconds at 1×. The edit adds 0.6 seconds inside the take's own pause after “And another…” for the scroll, and 0.3 seconds after “adults,” so the stat tiles clear before the results. No speech or silence is removed, and every aligned spoken sample is verified unchanged before loudness normalization.

Words were aligned locally with the CTC aligner; hyphenated words keep part timings, which places “seven” at 7.92 seconds. Nothing was uploaded. `production/voiceover/3d-polish/` holds the source record, alignment, edit map, word cues and verification report.

## How it is built

- `src/components/three/` holds the CSS 3D layer: the math for rotations, lighting and projection; a perspective `Stage` that shares a scene camera; `Extrude`, a rounded slab with lit walls; and the phone, clock, icon, card, meter and lockup built on it.
- `src/voiceover-cues.ts` is generated from the alignment. `src/choreography.ts` derives every timing from it, including the on-screen text blocks and the SFX cues.
- `scripts/check-storyboard.mjs` checks the runtime, panels, cue boundaries, the narration edit, that every phrase is readable on its measured word, and that no two text blocks sharing screen space are visible on the same frame.
- `scripts/music.mjs` synthesizes the score and all effects, so the video needs no third-party audio.

## Preview and rebuild

```sh
npm run dev
.tools/voice-alignment/bin/python scripts/align-ctc-voiceover.py 3d-polish
python3 scripts/prepare-3d-polish-voiceover.py
node --disable-warning=MODULE_TYPELESS_PACKAGE_JSON scripts/build-voice-cues.mjs
npm run audio:vo
npm run audio:music
npm run lint
npm run check:storyboard
npm run storyboard:panels
npm run render -- out/REDU-AVP-draft-2-3d-polish-v2.mp4 --concurrency=4 --timeout=120000
python3 scripts/verify-motion-export.py
```

Studio runs on port 3002. The saved alignment is sufficient for a rebuild. A full render takes 20–40 minutes on this machine; keep concurrency at 4, because the 3D motion-blur frames are memory-heavy.

The export guard checks actual video/audio/container duration, frame rate and frame count. The verification script decodes every frame, checks the score scene for black-frame regressions, compares speech samples for audio offsets, measures loudness, and verifies that the first draft and all previous draft-2 exports remain unchanged.

## Preservation

`reference/before-3d-polish/` preserves the opening-revision cut's storyboard, recording script, documentation, source, scripts, storyboard panels, music and effects. Its export, `out/REDU-AVP-draft-2-opening-revision.mp4`, is unchanged.

`reference/before-opening-revision/` preserves the revision before that. The earlier exports `out/REDU-AVP-draft-2-motion-polish.mp4` and `out/REDU-AVP-draft-2.mp4` remain untouched.

Other preserved revisions remain under `reference/draft-1`, `reference/draft-2-previous`, `reference/05b-before-whole-phone`, `reference/pre-eleven-v4` and `reference/before-motion-repair`. All current work and outputs live in this second-draft folder.
