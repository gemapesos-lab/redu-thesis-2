// The edit in one place: scene lengths, narration and sound cues. Shared by the Remotion
// compositions and by scripts/voiceover.mjs and scripts/music.mjs, so it must stay free of
// imports. Frames are at 30 fps.

export const FPS = 30;

// User-supplied ElevenLabs take, played continuously at its original speed.
export const VOICEOVER = {
  source: "audio/vo/lauren-original.mp3",
  audio: "audio/vo/lauren.wav",
  frames: 1638,
  credit: "Narration: ElevenLabs (Lauren)",
} as const;

export type Cue = "swipe" | "whoosh" | "tick" | "breath" | "hit";

export type SceneId =
  | "hook"
  | "problem"
  | "meet"
  | "users"
  | "signals"
  | "score"
  | "prompts"
  | "privacy"
  | "impact"
  | "outro"
  | "credits";

export type Scene = {
  readonly id: SceneId;
  readonly title: string;
  readonly frames: number;
  // Speech spans within the continuous take, used to duck the music.
  readonly voAt?: number;
  readonly voFrames?: number;
  // Transcript of the supplied take; its pauses stay in the audio.
  readonly voice?: string;
  readonly onScreen: string;
  readonly visual: string;
  // Representative frame for the storyboard sheet.
  readonly keyFrame: number;
  readonly cues: ReadonlyArray<readonly [number, Cue]>;
};

export const SCENES: ReadonlyArray<Scene> = [
  {
    id: "hook",
    title: "Just one more",
    frames: 195,
    voAt: 0,
    voFrames: 188,
    voice: "Just one more video. Then another. And suddenly, it's 2 AM.",
    onScreen: "Just one more video. → 2:07 AM",
    visual: "Dark. Words blur in one by one; a phone rises and the feed swipes faster and faster; a late-night clock lands.",
    keyFrame: 108,
    cues: [
      [80, "swipe"],
      [96, "swipe"],
      [111, "swipe"],
      [125, "swipe"],
      [138, "swipe"],
      [160, "hit"],
    ],
  },
  {
    id: "problem",
    title: "The problem",
    frames: 165,
    voAt: 8,
    voFrames: 152,
    voice: "That's doomscrolling. Screen-time limits count minutes, not what you watch.",
    onScreen: "That's doomscrolling. / Screen-time limits count minutes. Not what you watch.",
    visual: "Oversized kinetic word, then a screen-time pill and a two-line contrast with the key phrase in periwinkle.",
    keyFrame: 146,
    cues: [[61, "whoosh"]],
  },
  {
    id: "meet",
    title: "Meet REDU",
    frames: 180,
    voAt: 7,
    voFrames: 170,
    voice: "Meet REDU: a privacy-first Android app that notices doomscrolling, and helps you pause.",
    onScreen: "REDU · A privacy-first Android app · Notices doomscrolling. Helps you pause.",
    visual: "A sage breathing circle inhales and blooms into cream paper; the app icon and wordmark lock up, then the purpose line.",
    keyFrame: 168,
    cues: [[0, "breath"]],
  },
  {
    id: "users",
    title: "Who it's for",
    frames: 128,
    voAt: 6,
    voFrames: 118,
    voice: "Built for adult Filipino Android users who watch short-form video.",
    onScreen: "Built for adult Filipino Android users · TikTok · Instagram Reels · Facebook Reels",
    visual: "Real capture: the app's platform setup screen, with the three supported apps switched on.",
    keyFrame: 100,
    cues: [[0, "whoosh"]],
  },
  {
    id: "signals",
    title: "Three signals",
    frames: 161,
    voAt: 6,
    voFrames: 151,
    voice: "It reads three signals: session length, time per video, and negative captions.",
    onScreen: "Session length · Time per video · Negative captions (Filipino + English lexicon, on-device vision fallback)",
    visual: "A stand-in short-video feed; three signal cards pop out of the phone as they are named; a Taglish caption is scored.",
    keyFrame: 148,
    cues: [
      [56, "tick"],
      [86, "tick"],
      [119, "tick"],
    ],
  },
  {
    id: "score",
    title: "One gentle score",
    frames: 105,
    voAt: 5,
    voFrames: 94,
    voice: "Fuzzy logic blends them into one gentle score.",
    onScreen: "Fuzzy logic · One gentle score · Low / Elevated / High",
    visual: "The three signals merge into the app's activity-pattern meter; the marker settles on Elevated.",
    keyFrame: 92,
    cues: [[78, "tick"]],
  },
  {
    id: "prompts",
    title: "Gentle prompts",
    frames: 262,
    voAt: 6,
    voFrames: 251,
    voice: "When it rises, REDU steps in gently: a reminder, a pause, then a breathing break. You stay in control.",
    onScreen: "1 Reminder · 2 Pause · 3 Breathe · You stay in control.",
    visual: "Three phones: the Level 1 banner, the real Level 2 pause capture (Keep scrolling unlocks), and the Level 3 breathing circle.",
    keyFrame: 248,
    cues: [
      [96, "tick"],
      [126, "tick"],
      [172, "tick"],
    ],
  },
  {
    id: "privacy",
    title: "Private by design",
    frames: 118,
    voAt: 6,
    voFrames: 106,
    voice: "It all runs on your phone, and raw content is never kept.",
    onScreen: "Runs on your phone · Processed on-device · Raw text and frames discarded · Aggregate-only export",
    visual: "Real capture: the export screen (\"Aggregate data only\") beside a three-point privacy checklist.",
    keyFrame: 106,
    cues: [
      [44, "tick"],
      [56, "tick"],
      [68, "tick"],
    ],
  },
  {
    id: "impact",
    title: "Pilot results",
    frames: 202,
    voAt: 5,
    voFrames: 190,
    voice: "In a two-week pilot with fifty Filipino adults, prompted users scrolled less, and saw less negative content.",
    onScreen: "2 weeks · 50 adults · 10,134 sessions · four outcomes down vs. control · SUS 80.95",
    visual: "Counting stats, then four outcome cards with down arrows, usability and expert scores, and the study fine print.",
    keyFrame: 190,
    cues: [
      [0, "whoosh"],
      [108, "tick"],
      [112, "tick"],
      [147, "tick"],
      [160, "tick"],
    ],
  },
  {
    id: "outro",
    title: "Logo & tagline",
    frames: 122,
    voAt: 6,
    voFrames: 116,
    voice: "REDU. Notice the scroll. Choose the pause.",
    onScreen: "REDU · Notice the scroll. Choose the pause.",
    visual: "Logo lockup returns; the tagline blurs in line by line and the mascot winks.",
    keyFrame: 108,
    cues: [],
  },
  {
    id: "credits",
    title: "Credits",
    frames: 150,
    onScreen: "Developers, adviser, institution, thesis title, third-party credits and licenses",
    visual: "Clean credit card on paper; fades to black.",
    keyFrame: 110,
    cues: [[0, "whoosh"]],
  },
];

export const TOTAL_FRAMES = SCENES.reduce((sum, scene) => sum + scene.frames, 0);

export const sceneStart = (id: SceneId): number => {
  let start = 0;
  for (const scene of SCENES) {
    if (scene.id === id) {
      return start;
    }
    start += scene.frames;
  }
  throw new Error(`Unknown scene ${id}`);
};

export const sceneById = (id: SceneId): Scene => {
  for (const scene of SCENES) {
    if (scene.id === id) {
      return scene;
    }
  }
  throw new Error(`Unknown scene ${id}`);
};
