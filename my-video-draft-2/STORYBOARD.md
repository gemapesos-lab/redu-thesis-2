# REDU AVP — Draft 2 · 3D polish storyboard

**Status: built** around the October 7 take of [VOICEOVER.txt](VOICEOVER.txt). Every time below is measured from that take, aligned locally. Export: [out/REDU-AVP-draft-2-3d-polish-v2.mp4](out/REDU-AVP-draft-2-3d-polish-v2.mp4). The first 3D-polish export, before the word-by-word lines and the red clock, is unchanged at out/REDU-AVP-draft-2-3d-polish.mp4. Panels: [out/storyboard.png](out/storyboard.png).

The previous reviewed cut (opening revision), with its storyboard, script, source and audio, is preserved in [reference/before-3d-polish](reference/before-3d-polish). Its MP4 is unchanged at out/REDU-AVP-draft-2-opening-revision.mp4.

**Format:** 1920 × 1080, 30 fps, 59.0 s (1,770 frames; 60.000 s hard limit including the audio tail). Narration runs 0–53.27 s and credits 54–59 s.

## What this pass changes

1. **Real depth.** The phones, the bedside clock, the app icon, the stat tiles, the cards and the risk meter are 3D objects with thickness, lit edges and screen glare. They turn, spin, flip and land instead of sliding flat.
2. **A camera.** The night scenes have handheld drift, an orbit and a rack focus. The day scenes have smooth dolly, truck and crane moves. Scenes hand off through shared objects instead of blur-out, blur-in cuts.
3. **Text on the voice, no overlaps.** Key lines keep the word-by-word blur-and-rise, each word on its own spoken frame: “Just one more video…”, “That's doomscrolling.”, “Screen-time limits count minutes. Not what you watch.”, “Notices doomscrolling. Helps you pause.”, “You stay in control.” and the closing tagline. Every other line appears whole on its cue. A text block is gone before the next one takes its space; scripts/check-storyboard.mjs verifies this and that each line starts on its word.
4. **Sound that follows the motion.** 44 original, synthesized effects. Every turn, landing, swipe and switch has its own sound, varied instead of repeated identical ticks.

**Kept:** the warm paper and night palette, Manrope, the real app captures, the prompt UI rebuilt from the app source, every claim and illustrative label, the breath bloom into REDU, the caption close-up on “nakakalungkot,” the credits and the 59-second runtime.

**Removed:**

- The on-screen “Then another.” The swipes carry it.
- The App-limit pill colliding with “That’s.”
- The stats crowding the results in 09.
- The three small pills between 05 and 06.
- The prompt-step subtitles, including “A 45-second reset.” The app build uses 45 s, but the paper’s study used 60 s, so the label is dropped rather than contradict either.

## Shot list

| # | Time | Narration | Picture |
|---|---|---|---|
| 01 | 0:00.0–0:09.1 | Just one more video… Then another. And another… Suddenly, it’s two-oh-seven A.M. | Text alone, word by word, then a 3D phone rises. The camera orbits as the feed accelerates, then racks focus to a red LED alarm clock that ticks from 2:06 to 2:07. |
| 02 | 0:09.1–0:14.4 | That's doomscrolling. Screen-time limits count minutes, NOT what you watch. | The title swings up out of the dark. A screen-time widget counts minutes; on “NOT,” focus pulls to the videos behind it. |
| 03 | 0:14.4–0:20.3 | Meet REDU: a privacy-first Android app that notices doomscrolling, and helps you pause. | Breath bloom into paper. The 3D app icon spins in and lands; the mascot glances at “Notices doomscrolling.” |
| 04 | 0:20.3–0:24.3 | Built for adult Filipino Android users who watch short-form video. | A 3D phone with the real setup screen swings in; three platform chips click into place. |
| 05 | 0:24.3–0:28.8 | Three signals: session length, time per video, and negative captions. | The same phone spins around to the feed, three signal cards land, and the camera pushes into “nakakalungkot.” |
| 06 | 0:28.8–0:32.4 | Fuzzy logic combines them into a doomscrolling risk score. | The cards fly together into a 3D risk meter; the puck springs into Elevated. |
| 07 | 0:32.4–0:40.1 | When it rises, REDU steps in gently: a reminder, a pause, then a breathing break. You stay in control. | The meter tips into a track. As the puck climbs, each prompt phone rises at its risk band, then the camera pulls back to all three. |
| 08 | 0:40.1–0:43.7 | It all runs on your phone, and raw content is never kept. | The export-screen phone turns on a turntable; a raw caption lifts off and dissolves. |
| 09 | 0:43.7–0:50.1 | In a two-week pilot with fifty Filipino adults, prompted users scrolled less, and saw less negative content. | Stat tiles flip up, then flip away before the results arrive. |
| 10 | 0:50.1–0:54.0 | REDU. Notice the scroll. Choose the pause. | The icon spins in once more, then the tagline and the wink. |
| 11 | 0:54.0–0:59.0 | Music only | Credits, then a fade to black. |

## Direction

### 3D objects

Built with CSS 3D transforms rather than WebGL, so the real app captures and live UI stay pixel-sharp. Walls are shaded per face from a fixed key and fill light, so highlights travel as objects turn.

- **Phone:** the app's phone face on a titanium body 8.5% as deep as it is wide, with a glass back, camera module and side keys. A faint glare slides across the screen as it turns.
- **Bedside clock:** a red LED alarm clock — a deep, rounded matte-black body with a snooze bar on top, a recessed smoked-red lens with glass glare, and slanted red digits over faint ghost segments. Red is the warning colour of the hour; its light pools on the nightstand and fills the room once focus lands on it. The minute switches from 6 to 7 segment by segment, each with a short flicker.
- **App icon:** the launcher tile extruded into a thick squircle, with a sheen sweep on landing.
- **Cards and tiles:** the signal cards, stat tiles and outcome cards are thin slabs that tilt, flip and settle.
- **Risk meter:** three lit bars (sage, honey, coral) and a slider puck. It becomes the floor in 07.

### Camera

- **Night (01–02):** handheld drift of at most 0.3° and 5 px, a slow orbit and a rack focus. The room glow takes the color of whatever the screen shows.
- **Day (03–10):** locked off, or smooth dolly, truck and crane moves on eased curves. No shake.
- **Hand-offs:** clock glow to black; sage dot to paper bloom; lockup to phone; phone spin to feed; cards to meter; meter to track; phones sink to the export phone; tiles to results; results to icon.
- **Lens:** the existing motion blur (12 samples, 180° shutter) only on fast moves: the phone's arrival, swipes, spins, flights and the rack focus. Depth of field softens out-of-focus objects; text that is being read is never blurred.

### Text

- Word-by-word lines: each word blurs and rises in, readable on its own spoken frame, and the line leaves as one block.
- Other lines appear whole, readable on their spoken onset; the blur-and-rise starts 7 frames early. A phrase at the top of a scene starts on the scene's first frame, at most 3 frames after its word.
- Blocks that share screen space are never visible on the same frame; the gap is at least 2 frames.
- Emphasis is static color: periwinkle for key ideas, coral only for negative content.

### Motion

- Entrances ease out over 12–18 frames. Exits ease in over 8–10 frames and leave into depth, tipping back or receding, rather than as flat fades.
- Landings use springs with small overshoot, so they feel weighty rather than bouncy.
- Held objects keep slow camera drift, turntable rotation or idle mascot motion, so no frame looks frozen.

### Sound

All music and effects stay original and synthesized in scripts/music.mjs, so the credit line is unchanged.

- **Score:** the same original theme, retimed. The night pulse quickens with the swipes, from 0.62 s to 0.27 s between beats. The score drops to room tone 0.32 s before “Suddenly” and returns, darker, on the clock. A soft bell lift marks the pilot results.
- **Effects:** phone rise and landing; six swipes that rise in pitch, each with a haptic thump; rack-focus air; digital blip and sub hit; minute ticks; focus pull; recede; breath; spin; truck; toggles; three card pops; highlighter; push; fly; meter lock; puck slide and settle click; riser; lift; notification chime; lock and unlock clicks; sink; checks; dissolve shimmer; tile flaps; counter roll; four descending down-ticks; sparkle.
- **Mix:** effects sit about 3 dB lower while narration plays. Music ducks about 8 dB under narration, as before, and comes up through the scroll gap.

## Shots

Times are seconds from the start of the video.

### 01 · Just one more — 0:00.0–0:09.1

- **“Just” (0.10):** a near-black room. “Just one more video…” fades up whole, centered and white.
- **“Then” (2.13):** the line tips back into the dark and is gone before the phone reaches it. A 3D phone rises from below frame at a three-quarter angle and lands on a soft spring. Its screen light washes the room in the video's color.
- **“another,” “And another…” (3.67) and the scroll gap:** six swipes, each faster than the last, with motion blur on the feed; the third lands on the second “another.” The camera orbits about 34° around the phone and drifts closer, so its edge and thickness read. The status bar shows 2:06. A red glow, out of focus, sits far behind the phone.
- **“Suddenly” (6.19):** rack focus. The phone slides out of frame left and goes soft, while the clock sharpens as the camera travels to it. It reads 2:06.
- **“seven” (7.92):** the last red digit switches from 6 to 7, segment by segment, and the red glow pulses through the room. Hold, then push into the glow and fall to black.
- **Sound:** room tone and a low drone; rise and landing for the phone; pitched swipes over a quickening pulse; room tone only, just before “Suddenly”; rack air; blip and sub hit on “seven.”

### 02 · The problem — 0:09.1–0:14.4

- **“That's” (9.36):** “That's doomscrolling.” swings up out of the dark as one block.
- **“Screen-time” (11.10):** the title tips back and is gone before anything replaces it. A generic 3D screen-time widget tilts in on the right and counts from 1h 41m to 1h 58m. “Screen-time limits count minutes.” appears whole on the left.
- **“NOT” (13.09):** “Not what you watch.” appears whole beneath it. Focus racks from the widget to the short videos floating behind it, the videos the timer never sees, and they drift forward.
- **Hand-off:** everything recedes and dims to a small sage dot.
- **Sound:** swings for the title and widget; five minute ticks that stop on “NOT”; a focus pull; a recede.

### 03 · Meet REDU — 0:14.4–0:20.3

- **Scene start:** the sage circle inhales and cream paper blooms out of it.
- **“REDU” (14.99):** the 3D icon spins in, one and a half turns, and lands on a spring as a sheen sweeps the tile. The wordmark slides out beside it in one piece.
- **“privacy-first” (15.78):** the lockup lifts. “A privacy-first Android app” appears whole once the lockup has cleared its line.
- **“notices” (17.59):** “Notices doomscrolling.” appears whole, and the mascot's eyes glance down at it.
- **“helps” (19.25):** “Helps you pause.” appears whole; the mascot blinks slowly on “pause.”
- **Hand-off:** the camera trucks right and the lockup slides off left.
- **Sound:** breath into the bloom; spin; the bell strum in the score; truck.

### 04 · Who it's for — 0:20.3–0:24.3

- **“Built” (20.50):** “Built for / Adult Filipino Android users” appears as one block. The 3D phone with the real setup capture swings in from edge-on and lands at a three-quarter angle.
- **“who” (22.73):** “who watch short-form video on” appears whole.
- **“short-form” (23.19):** the TikTok, Instagram Reels and Facebook Reels chips click in, 4 frames apart; the phone's switches get the highlight ring and the phone turns slightly toward the text.
- **Sound:** a swing for the phone; three toggles.

### 05 · Three signals — 0:24.3–0:28.8

- **Line start:** the same phone spins a full turn; its back and cameras flash past, and it comes round on the feed: @balita.now, “Grabe, nakakalungkot naman.”
- **“Three” (24.49):** “Three signals.” appears whole.
- **“session” (25.63), “time” (26.60), “negative” (27.75):** one card lands on each cue: Session length 23 min; Time per video 41 s; Negative captions 38%.
- **Close-up (27.03):** the phone squares up and grows as one object, bezel intact and running off frame, until the caption fills the right half. A coral marker sweeps “nakakalungkot” on “negative.” Cards 1 and 2 fall back; card 3 grows, with the vision-fallback note beneath. Fine print: “Illustrative session metrics.”
- **Hand-off:** the close-up pulls back and exits right.
- **Sound:** spin; three card pops; push; highlighter; whoosh.

### 06 · Doomscrolling risk score — 0:28.8–0:32.4

- **“Fuzzy logic” (29.05):** the three cards arc through depth to the center and shrink into chips. “Fuzzy logic” appears whole.
- **“combines” (29.82):** the chips dive into a 3D risk meter as it swings up into place, with Low 0–33, Elevated 34–66 and High 67–100 beneath.
- **“doomscrolling” (30.86):** “Doomscrolling risk score.” appears whole. The puck springs to 42, just inside Elevated; Low and High dim.
- **“score” (31.86):** “0–100 scale · Illustrative risk estimate.”
- **Sound:** three flies; the meter lock; the puck slide and settle click.

### 07 · Gentle prompts — 0:32.4–0:40.1

- **“When it rises” (32.60):** the camera cranes up and the meter tips flat into a long track. The puck starts to climb.
- **“REDU” (33.59):** “REDU steps in gently.” appears whole.
- **“reminder” (35.35):** the puck reaches 48, in the lower Elevated band, and phone 1 rises there with the reminder banner. Label: 1 · Reminder. The camera trucks with the puck.
- **“pause” (36.31):** at 62, the upper Elevated band, phone 2 rises with the real pause capture.
- **“breathing” (37.54):** at 84, in High, phone 3 rises with the breathing circle running. Earlier phones soften with depth.
- **“You” (38.58):** “You stay in control.” replaces the title. The camera pulls back to all three phones; “Keep scrolling” unlocks and gets the ring.
- **Sound:** riser; three lifts; the notification chime, the lock click and a breath; unlock; sink.

### 08 · Private by design — 0:40.1–0:43.7

- **“It” (40.25):** the export-screen phone rises at left and turns slowly. “Private by design / Runs on your phone.” appears as one block.
- **“runs” (40.70), “raw” (41.88), “kept” (43.14):** one checklist line appears on each cue.
- **“raw content”:** a raw caption chip, “Grabe, nakakalungkot naman.,” lifts off the screen, drifts left and dissolves into dust. On “kept,” the “Aggregate data only” card gets the ring.
- **Sound:** rise; three checks; dissolve shimmer.

### 09 · Pilot results — 0:43.7–0:50.1

- **“two-week” (44.08), “fifty” (45.04), “Filipino” (45.38):** three thick stat tiles flip up, hinged at their bottom edges: 2 weeks; 50 Filipino adults; 10,134 sessions logged, counting up.
- **After “adults,” (46.31):** the tiles flip back and away together, inside the 0.55 s pause.
- **“prompted” (46.86):** “Prompted users scrolled less and saw less negative content.” appears whole. On “scrolled” (47.64), four outcome cards rise in one wave on a shallow arc, each arrow nudging down as it lands.
- **Then:** the SUS 80.95 and expert-rating badges, and the study fine print.
- **Sound:** three tile flaps; the counter roll; a whoosh; four descending down-ticks; a soft bell lift in the score.

### 10 · Logo & tagline — 0:50.1–0:54.0

- **“REDU” (50.30):** the icon spins in, one turn, and lands with a sheen; the wordmark slides out.
- **“Notice” (51.13):** “Notice the scroll.” appears whole.
- **“Choose” (52.42):** “Choose the pause.” appears whole. After “pause” (ends 53.27), the mascot winks.
- **Sound:** spin; the bell strum and home chord; a sparkle on the wink.

### 11 · Credits — 0:54.0–0:59.0

Unchanged: the content and layout, with “Made with Remotion” still removed. Music only; the frame fades to black.

## Narration and timing

- The take adds one line to the October 6 script, “And another…”. Its file name omits the voice; its pitch and timbre match the October 6 Lauren take, so the credit remains “ElevenLabs v4 (Lauren).”
- Played at 1×. No speech or silence is removed. The edit adds 0.6 s after “And another…,” extending the take's own 1.07 s pause to 1.67 s for the scroll, and 0.3 s after “adults,” for the stat tiles. Every aligned spoken sample is verified unchanged before loudness normalization.
- Word timings come from the local CTC aligner, with sub-word timing for hyphenated words, which places “seven” at 7.92 s. The audio was not uploaded.

## As built, compared with the plan

- The scroll gap needed only 0.6 s of added silence rather than 1.0–1.5 s, because the take already pauses after “And another…”.
- The lockup eyebrow appears 4 frames after “privacy-first” begins, once the lifting logo has cleared its line.
- The illustrative score settles at 42 rather than the previous 58, so the puck climbs to the Reminder mark at 48 in 07.

## Claim provenance

The thesis at ../acm-paper/paper.md supports the three-signal fuzzy estimate, text-first sentiment processing with a vision fallback, on-device processing with raw text and screenshots discarded after scoring, aggregate-only storage, and the pilot results.

The prompt ladder in 07 follows the paper's mapping: Level 1 in the lower Warning band, Level 2 in the upper Warning band and Level 3 in the Critical band. The app labels these bands Elevated and High.

The puck positions, the 23 min, 41 s and 38% card values, and the screen-time widget are illustrative. “Nakakalungkot” appears in the Filipino lexicon at valence −3. The pilot figures, effect sizes and fine print are unchanged from draft 1.
