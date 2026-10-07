# REDU AVP — Draft 2

Independent copy of the first draft. All edits, assets, source files, dependencies and outputs for this revision live here. The original project at `../my-video` remains intact.

**Status:** updated storyboard and silent animatic; awaiting the new VO. Target **59.00 seconds**, including credits. Absolute limit **60.000 seconds**.

## Review

- [12-panel storyboard](out/storyboard.png)
- [Written storyboard and timing notes](STORYBOARD.md)
- [Clean recording script](VOICEOVER.txt)
- [Side-by-side comparison](COMPARE.html)
- [Full-resolution panels](out/panels)
- [First-draft storyboard](reference/draft-1/out/storyboard.png)
- [First-draft MP4](reference/draft-1/out/REDU-AVP.mp4)

Only scenes **01, 05 and 06** are polished: digital clock, a continuous camera move into the phone caption on the right with its signal card on the left, and the explicit “Doomscrolling risk score” label. All other scene components and their local timing are restored from draft 1, including the full original pilot-results screen.

## Preview and rebuild

```sh
npm run dev
npm run lint
npm run check:storyboard
npm run storyboard
npm run storyboard:panels
```

Studio uses port 3002, keeping this project separate from the first draft. Choose `Storyboard`, `AVP` (silent), or an individual scene. The stills render the same components used in the 59-second animatic.

`storyboard` rebuilds the contact sheet. `storyboard:panels` rebuilds it plus all twelve full-resolution panels and focused camera-move checks. If reinstalling elsewhere, run `npm ci` first.

## Voiceover and final export

The copied Lauren files are reference assets and are not played in draft 2. The new recording belongs in this folder’s `public/audio/vo/`. See [STORYBOARD.md](STORYBOARD.md) for the integration steps; the scene boundaries and cue timings remain provisional.

`npm run render` intentionally requires the updated VO to be integrated and marked ready. After export, `npm run check:storyboard -- out/REDU-AVP-draft-2.mp4` checks video frames, frame rate and the actual video/audio/container durations.

## Preservation

`reference/draft-1/` contains the copied original storyboard, outputs and an SHA-256 manifest of the original source/assets/outputs. `check:storyboard` verifies that the source project still matches those saved hashes. No source or asset links point back to draft 1. Dependencies were copied, too.

To compare or return to draft 1, open `../my-video` or its preserved MP4. Draft-2 renders always go to this folder’s own `out/`.

The previous broader draft-2 treatment is archived in `reference/draft-2-previous/`.
