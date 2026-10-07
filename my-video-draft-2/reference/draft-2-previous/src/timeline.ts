// Draft 2 editorial budgets. All cue frames are provisional until the updated VO
// is supplied and measured. Keep this module import-free for production scripts.
export const FPS = 30;
export const HARD_LIMIT_FRAMES = 1800;
export const NARRATION_BUDGET_FRAMES = 1620;
export const VOICEOVER = {
  status: "pending" as "pending" | "ready",
  source: "audio/vo/draft-2-original.mp3",
  audio: "audio/vo/draft-2.wav",
  frames: 0,
  credit: "Narration: ElevenLabs (Lauren)",
};

export type Cue = "swipe" | "whoosh" | "tick" | "breath" | "hit";
export type SceneId = "hook" | "problem" | "meet" | "users" | "signals" | "score" | "prompts" | "privacy" | "impact" | "outro" | "credits";
export type Scene = {
  readonly id: SceneId;
  readonly title: string;
  readonly frames: number;
  readonly voice?: string;
  readonly onScreen: string;
  readonly visual: string;
  readonly keyFrame: number;
  readonly cues: ReadonlyArray<readonly [number, Cue]>;
};

// Readable word onsets, not blur-animation start times. Replace these proposed
// numbers with measured VO cues later; pictures and SFX share the same cues.
export const BEATS = {
  hook: { words: [2, 12, 21, 32], swipes: [48, 60, 71, 81], clock: 96 },
  problem: { thats: [3], doomscrolling: [17], limits: [52, 66, 78, 90], contrast: [106, 117, 125, 134] },
  meet: { identity: 34, helps: [72, 85, 97] },
  users: { heading: [17, 28, 42, 56], platforms: 18 },
  signals: { heading: [5, 18], session: 36, video: 80, negative: 120, captionStart: 132, captionReady: 150, highlight: 150, holdUntil: 282, returnAt: 282, returnDuration: 24 },
  score: { fuzzy: 3, join: 25, title: [58, 80, 95], settle: 99 },
  prompts: { introduction: [5, 17], reminder: 45, pause: 78, breathe: 114, unlock: 168, control: [179, 191, 204, 218] },
  privacy: { processed: [6, 23], raw: [45, 55, 66, 77] },
  impact: { weeks: 12, adults: 55, scrolling: 114, negative: 157 },
  outro: { notice: [29, 40, 51], choose: [73, 86, 98] },
} as const;

export const SCENES: ReadonlyArray<Scene> = [
  { id: "hook", title: "Just one more", frames: 150, keyFrame: 124,
    voice: "Just one more video. Suddenly, it’s two-oh-seven AM.",
    onScreen: "Just one more video. → 2:07 AM",
    visual: "A short scroll accelerates into a softly illuminated seven-segment digital clock. The full 2:07 AM display lands on the spoken time.",
    cues: [...BEATS.hook.swipes.map((at): readonly [number, Cue] => [at, "swipe"]), [BEATS.hook.clock, "hit"]] },
  { id: "problem", title: "The problem", frames: 150, keyFrame: 141,
    voice: "That’s doomscrolling. Screen-time limits count minutes, not what you watch.",
    onScreen: "That’s doomscrolling. / Screen-time limits count minutes. / Not what you watch.",
    visual: "Every word lands sharply on its spoken onset. Give Not its own cue; the contrast line becomes the focus.",
    cues: [[BEATS.problem.limits[0], "whoosh"]] },
  { id: "meet", title: "Meet REDU", frames: 120, keyFrame: 108,
    voice: "Meet REDU, an Android app that helps you pause.",
    onScreen: "REDU · An Android app · Helps you pause.",
    visual: "A sage breath blooms into warm paper. The REDU logo, Android identity and purpose appear in sequence.",
    cues: [[0, "breath"]] },
  { id: "users", title: "Who it’s for", frames: 90, keyFrame: 78,
    voice: "Built for adult Filipino Android users.",
    onScreen: "Adult Filipino Android users · TikTok · Instagram Reels · Facebook Reels",
    visual: "The audience headline sits beside the real platform-setup capture and the three platform identifiers.",
    cues: [[0, "whoosh"]] },
  { id: "signals", title: "Three signals", frames: 330, keyFrame: 220,
    voice: "Three signals: session length, time per video, and negative captions. It checks Filipino and English text for negative language.",
    onScreen: "1 · Session length / 2 · Time per video / 3 · Negative captions",
    visual: "Two timer cards lead into a seven-second caption demonstration. An 84 px caption fills the frame; nakakalungkot is coral with a Negative label. Hold for 4.4 seconds, then return to three signals. Vision stays a small note.",
    cues: [[BEATS.signals.session, "tick"], [BEATS.signals.video, "tick"], [BEATS.signals.negative, "tick"]] },
  { id: "score", title: "Doomscrolling risk score", frames: 120, keyFrame: 110,
    voice: "Fuzzy logic combines them into a doomscrolling risk score.",
    onScreen: "Fuzzy logic · Doomscrolling risk score · 0–100 · Low / Elevated / High",
    visual: "Exactly three named inputs converge into the risk meter. The marker is illustrative, not a calculated result for the demonstration caption.",
    cues: [[BEATS.score.settle, "tick"]] },
  { id: "prompts", title: "You stay in control", frames: 240, keyFrame: 230,
    voice: "When needed: a reminder, a pause, or a breathing break. You stay in control.",
    onScreen: "Reminder → Pause → Breathing break · You stay in control.",
    visual: "Each phone lights up on its spoken cue. End with the unlocked choice visible. The breathing animation is an excerpt of the actual 45-second exercise.",
    cues: [[BEATS.prompts.reminder, "tick"], [BEATS.prompts.pause, "tick"], [BEATS.prompts.breathe, "tick"]] },
  { id: "privacy", title: "Private by design", frames: 90, keyFrame: 83,
    voice: "Processed on-device. Raw content isn’t saved.",
    onScreen: "Processed on-device. / Raw content isn’t saved.",
    visual: "The real export screen supports two large, immediately readable privacy statements.",
    cues: [[BEATS.privacy.processed[0], "tick"], [BEATS.privacy.raw[0], "tick"]] },
  { id: "impact", title: "Pilot results", frames: 210, keyFrame: 194,
    voice: "In a two-week pilot with fifty Filipino adults, prompted users scrolled less and saw less negative content.",
    onScreen: "2-week pilot · 50 Filipino adults / Less scrolling / Less negative-content exposure",
    visual: "Two clear outcome cards replace the dense statistics grid. Supporting text identifies the logging-only comparison and short-term pilot context.",
    cues: [[BEATS.impact.scrolling, "tick"], [BEATS.impact.negative, "tick"]] },
  { id: "outro", title: "Choose the pause", frames: 120, keyFrame: 108,
    voice: "REDU. Notice the scroll. Choose the pause.",
    onScreen: "REDU / Notice the scroll. / Choose the pause.",
    visual: "The logo returns. Two tagline lines land with the VO and the mascot gives a small wink.", cues: [] },
  { id: "credits", title: "Credits", frames: 150, keyFrame: 72,
    onScreen: "Thesis title · Developers · Adviser · Institution · Production credits",
    visual: "A five-second hold. All credits are present from the start and the final fade completes within the runtime.", cues: [] },
];

export const TOTAL_FRAMES = SCENES.reduce((sum, scene) => sum + scene.frames, 0);
if (TOTAL_FRAMES > HARD_LIMIT_FRAMES) throw new Error("AVP exceeds the absolute 60.000-second limit.");
export const sceneStart = (id: SceneId): number => {
  let start = 0;
  for (const scene of SCENES) { if (scene.id === id) return start; start += scene.frames; }
  throw new Error("Unknown scene " + id);
};
export const sceneById = (id: SceneId): Scene => {
  const scene = SCENES.find((item) => item.id === id);
  if (!scene) throw new Error("Unknown scene " + id);
  return scene;
};

export type StoryPanel = {
  readonly scene: SceneId; readonly label: string; readonly title: string;
  readonly from: number; readonly to: number; readonly keyFrame: number;
  readonly voice: string; readonly direction: string;
};
export const PANELS: ReadonlyArray<StoryPanel> = SCENES.flatMap((scene, i): StoryPanel[] => {
  if (scene.id === "signals") return [
    { scene: "signals", label: "05A", title: "Session length + time per video", from: 510, to: 630, keyFrame: 108,
      voice: "Three signals: session length, time per video…",
      direction: "Introduce the two numbered timers on their spoken cues. Leave the third signal for the caption reveal." },
    { scene: "signals", label: "05B", title: "Negative captions take focus", from: 630, to: 840, keyFrame: 220,
      voice: "…and negative captions. It checks Filipino and English text for negative language.",
      direction: "84 px caption. Coral word highlight. Readable Negative label. Hold ≥3 seconds. Vision is a small supporting note." },
  ];
  const from = sceneStart(scene.id);
  return [{ scene: scene.id, label: String(i + 1).padStart(2, "0"), title: scene.title, from, to: from + scene.frames,
    keyFrame: scene.keyFrame, voice: scene.voice ?? "", direction: scene.visual }];
});
