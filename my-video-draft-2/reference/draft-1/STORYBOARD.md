# REDU AVP: storyboard and script

A 59.6-second audio-visual presentation of REDU: 1920×1080, 16:9, 30 fps, 1,788 frames. The narration is the supplied ElevenLabs Lauren recording, preserved at its original speed. The contact sheet below is rendered from the `Storyboard` composition; this file is the written script behind it.

![Storyboard](out/storyboard.png)

- **Preview:** `npm run dev`, then open `AVP` (the full cut), `Storyboard`, or a single scene under `Scenes/`.
- **Timing:** every scene length, narration offset and sound cue lives in `src/timeline.ts`. After retiming, run `npm run audio:music` so the score follows the new cut.
- **Render:** `npm run render` writes `out/REDU-AVP.mp4`.
- **Re-render the sheet:** `npx remotion still Storyboard out/storyboard.png --frame=0`.

## Look

It opens dark on a 2 AM scroll, then one breath blooms into REDU's warm paper (`#F7F2EB`) with slow periwinkle, honey and sage glows. Type is Manrope ExtraBold with tight tracking, revealed word by word with a blur. Real app captures sit in a dark phone frame, and each beat carries one idea. Colours are taken from the app's own palette.

## Brief coverage

| Requirement | Scene |
|---|---|
| Project name and logo | 03 (icon and wordmark lockup), 10, 11 |
| Problem | 01, 02 |
| Purpose | 03 |
| Intended users | 04 |
| Major features | 05 signals, 06 fuzzy score, 07 three prompt levels, 08 on-device processing and aggregate export |
| Benefits and impact | 09 pilot results, usability and expert ratings |
| Actual system screens | 04, 07 (Level 2) and 08 are real captures; 07 Levels 1 and 3 are rebuilt from the app's code |
| Developers and credits | 11 |

Every narrated line also appears on screen, so the video reads with the sound off. The music ducks about 8 dB under the voice. Outside the phone screens, which mirror the app at phone scale, no text is smaller than 21 px (the credits' licence lines).

## Script and shot list

### 01 · Just one more (0:00.0–0:06.5)

- **Visual:** Black screen. "Just one more video." blurs in word by word. A phone rises and the feed swipes faster and faster, then a large "2:07 AM" clock lands.
- **Narration:** "Just one more video. Then another. And suddenly, it's 2 AM."
- **Sound:** Night drone and a soft pulse; five accelerating swipes; a low hit on the clock.

### 02 · The problem (0:06.5–0:12.0)

- **Visual:** "That's doomscrolling." in oversized type. An "App limit · 2h a day" pill, then "Screen-time limits count minutes." and "Not what you watch.", with the key phrase in periwinkle.
- **Narration:** "That's doomscrolling. Screen-time limits count minutes, not what you watch."
- **Sound:** The drone gains a faint, uneasy shimmer; a whoosh as the pill lands.

### 03 · Meet REDU (0:12.0–0:18.0)

- **Visual:** A sage breathing circle inhales and floods the frame with paper. The app icon and REDU wordmark lock up, then "A privacy-first Android app" and "Notices doomscrolling. Helps you pause."
- **Narration:** "Meet REDU: a privacy-first Android app that notices doomscrolling, and helps you pause."
- **Sound:** A riser cuts to a single breath; the first chord blooms with a bell strum under the logo.

### 04 · Who it's for (0:18.0–0:22.3)

- **Visual:** "Built for adult Filipino Android users who watch short-form video on", with TikTok, Instagram Reels and Facebook Reels chips. Beside it, the real platform-setup screen with all three apps switched on.
- **Narration:** "Built for adult Filipino Android users who watch short-form video."
- **Sound:** A whoosh; an electric-piano arpeggio enters.

### 05 · Three signals (0:22.3–0:27.6)

- **Visual:** A stand-in short-video feed. A card pops out of the phone as each signal is named: Session length (23 min), Time per video (41 s) and Negative captions (38%, Filipino + English lexicon). A fourth card reads "No caption? Vision model (Moondream 0.5B, on-device)". In the Taglish caption, "nakakalungkot" is highlighted and tagged −3.
- **Narration:** "It reads three signals: session length, time per video, and negative captions."
- **Sound:** The beat enters; a tick for each card.
- **Note:** the card values are illustrative.

### 06 · One gentle score (0:27.6–0:31.1)

- **Visual:** The three signals fly into the app's activity-pattern meter (Low / Elevated / High) and the marker springs to Elevated. Footnote: "A gentle read on your pattern, not a diagnosis."
- **Narration:** "Fuzzy logic blends them into one gentle score."
- **Sound:** Shakers enter; a tick as the marker settles.

### 07 · Gentle prompts (0:31.1–0:39.9)

- **Visual:** Three phones, each lighting up as it is named:
  - 1 Reminder: the Level 1 banner slides in.
  - 2 Pause: the real Level 2 capture; "Keep scrolling" unlocks.
  - 3 Breathe: the 45-second breathing circle.

  "Steps in gently." then gives way to "You stay in control.", with "Keep scrolling" highlighted.
- **Narration:** "When it rises, REDU steps in gently: a reminder, a pause, then a breathing break. You stay in control."
- **Sound:** The keys gain an octave; a tick for each level.

### 08 · Private by design (0:39.9–0:43.8)

- **Visual:** The real export screen, with "Aggregate data only" highlighted, beside "Runs on your phone." and three checks: Processed on-device · Raw text and screen frames discarded · Exports hold aggregate data only.
- **Narration:** "It all runs on your phone, and raw content is never kept."
- **Sound:** The beat drops out; three ticks.

### 09 · Pilot results (0:43.8–0:50.5)

- **Visual:**
  - Counting stats: 2 weeks · 50 Filipino adults · 10,134 sessions logged.
  - "Scrolled less. Saw less negative content." over four outcome cards with effect sizes.
  - Usability (SUS) 80.95 against a target of 70, and expert ratings of 5.00 and 4.33 out of 5.
  - Fine print on the study design.
- **Narration:** "In a two-week pilot with fifty Filipino adults, prompted users scrolled less, and saw less negative content."
- **Sound:** A whoosh, the beat returns, and ticks as the cards land.

### 10 · Logo and tagline (0:50.5–0:54.6)

- **Visual:** The logo lockup returns, then "Notice the scroll. Choose the pause." The mascot winks.
- **Narration:** "REDU. Notice the scroll. Choose the pause."
- **Sound:** A second bell strum on the home chord.

### 11 · Credits (0:54.6–0:59.6)

- **Visual:**
  - The formal thesis title, the four developers and the adviser.
  - The degree, FEU Institute of Technology, June 2026.
  - Third-party credits, then a fade to black.
- **Narration:** none.
- **Sound:** The home chord and a few piano notes, fading with the picture.

## Sound

| Layer | Source | Level |
|---|---|---|
| Narration | Supplied ElevenLabs recording, Lauren — Friendly, Comforting and Soft. `npm run audio:vo` normalizes the retained original MP3. | −16 LUFS target in stereo, −3 dBTP ceiling |
| Music | Original score synthesized by `scripts/music.mjs` (no licence needed) | −19 LUFS, ducked about 8 dB under narration |
| Sound effects | Swipe, whoosh, tick, breath and hit, synthesized by the same script | Set per cue in `src/AVP.tsx` |

## Current narration

The active take is `ElevenLabs_2026-10-05T10_19_37_Lauren - Friendly, Comforting and Soft_pvc_sp100_s50_sb75_v4.mp3` supplied on 5 October 2026.

- `public/audio/vo/lauren-original.mp3` is an unchanged copy of the supplied file.
- `public/audio/vo/lauren.wav` is the normalized, 48 kHz mono production asset. It runs continuously from 0:00 to 0:54.6 with the original pauses and speed.
- Scene cuts sit in the recording's natural pauses. Headlines, signal cards, prompt highlights, sound cues and the original score follow the new timing.
- `voAt` and `voFrames` in `src/timeline.ts` describe speech spans for music ducking; they do not cut or restart the recording.
- The credits begin at 0:54.6 and run for five seconds. The narration credit is **ElevenLabs (Lauren)**.

To rebuild the audio, run `npm run audio:vo` and `npm run audio:music`, then `npm run render`. The old per-scene scratch WAV files and `scripts/scratch-voiceover.mjs` are retained for reference; the AVP does not play them.

## Sources

### Screens

| In the video | Source |
|---|---|
| Platform setup (04) | `app_showcase/04_setup_platforms_selected.png`, captured on Android |
| Level 2 pause, locked and unlocked (07) | `app_showcase/24_demo_l2_locked.png`, `25_demo_l2_unlocked.png` |
| Export screen (08) | `app_showcase/20_export_datasets.png` |
| Level 1 banner, Level 3 breathing (07) | Rebuilt from `PromptPresenter.kt` and `BreathingCircleView.kt`: same copy, colours and 45 s duration |
| App icon and mascot | Rebuilt from `ReduBlobatar.kt` and the launcher icon |
| Short-video feed (01, 05, 07) | Stand-in drawn for the video; handles and captions are invented, and no platform UI is copied |
| Platform logos (04) | The app's own `drawable-nodpi/ic_{tiktok,instagram,facebook}.png` |

The Level 1 and Level 3 captures in `app_showcase/` show developer tools or an older build, so the video rebuilds those two screens instead; the credits say so.

### Claims

| Claim on screen | Source |
|---|---|
| 50 adult Filipino Android users, two weeks, intervention vs. logging-only control | Abstract; Chapter 4 |
| 10,134 sessions logged | Chapter 4 (participant flow and data-quality tables) |
| Effect sizes: session duration −1.18, video dwell −1.33, negative sentiment density −1.82, doomscrolling scale −1.50; all p < .001 | Chapter 4, primary-outcome table |
| SUS 80.95 against a target of 70 | Chapter 4, usability table |
| Expert ratings 5.00 (software engineering) and 4.33 (psychology) | Chapter 4, SME rubric table |
| Processed on-device; raw text and screen frames are not kept; aggregate-only export | Chapter 5 conclusions; the export screen |
| Filipino + English lexicon; Moondream 0.5B vision fallback | Abstract; Chapter 5 |
| 45-second breathing break | `PromptPresenter.kt` (`BREATHING_PROMPT_MILLIS = 45_000L`) |

The fine print in scene 09 states that these are short-term, non-clinical findings from a pilot.

## Third-party credits

| Material | Licence | Credited in |
|---|---|---|
| VADER sentiment analysis (Hutto & Gilbert, 2014) | MIT | Credits |
| Moondream 0.5B | Apache 2.0 | Credits |
| llama.cpp | MIT | Credits |
| Manrope typeface (via `@remotion/google-fonts`) | SIL Open Font License 1.1 | Credits |
| Lucide icons (the app's and this video's icon set) | ISC | Credits |
| Mascot motion adapted from blobatar by Alain | MIT | Credits |
| Remotion | Remotion License (free for individuals, non-profits and companies of up to three people) | Credits |
| TikTok, Instagram and Facebook logos | Trademarks, shown for identification only | Credits |
| Music, sound effects | Original, made for this video | Credits |
| Narration | User-supplied ElevenLabs Lauren recording. | Credits |

## Open decisions

- **Narration:** the supplied ElevenLabs Lauren recording is in the cut; see Current narration above.
- **Name:** the supplied recording determines the pronunciation; on-screen text remains REDU.
- **Music:** keep the original synthesized score or swap in a licensed track. A swap means updating the Credits line.
- **Render:** run `npm run render` to rebuild `out/REDU-AVP.mp4`.
