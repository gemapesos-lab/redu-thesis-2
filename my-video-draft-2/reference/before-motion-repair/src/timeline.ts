// Draft 2: designs revised in 01, 05 and 06; all scenes now use measured VO cues.
import { MEASURED_BEATS, SCENE_TIMINGS } from "./voiceover-cues.ts";
export const FPS = 30;
export const HARD_LIMIT_FRAMES = 1800;
export const NARRATION_BUDGET_FRAMES = 1620;
export const VOICEOVER = {
 status: "ready" as "pending" | "ready",
 source: "audio/vo/draft-2-original.mp3", audio: "audio/vo/draft-2.wav",
 productionSource: "audio/vo/draft-2-edited.wav",
 frames: 1608, credit: "Narration: ElevenLabs v4 (Lauren)",
};
export type Cue = "swipe" | "whoosh" | "tick" | "breath" | "hit";
export type SceneId = "hook" | "problem" | "meet" | "users" | "signals" | "score" | "prompts" | "privacy" | "impact" | "outro" | "credits";
export type Scene = {
 readonly id: SceneId; readonly title: string; readonly frames: number;
 readonly voAt?: number; readonly voFrames?: number; readonly voice?: string;
 readonly onScreen: string; readonly visual: string; readonly keyFrame: number;
 readonly cues: ReadonlyArray<readonly [number, Cue]>;
};

export const BEATS = MEASURED_BEATS;

const STORYBOARD_SCENES: ReadonlyArray<Scene> = [
  {
    "id": "hook",
    "title": "Just one more",
    "frames": 135,
    "voice": "Just one more video. Suddenly, it’s two-oh-seven AM.",
    "onScreen": "Just one more video. → 2:07 AM",
    "visual": "Keep the first-draft scrolling hook, ending on a softly illuminated seven-segment 2:07 AM clock.",
    "keyFrame": 119,
    "cues": [
      [
        48,
        "swipe"
      ],
      [
        60,
        "swipe"
      ],
      [
        71,
        "swipe"
      ],
      [
        81,
        "swipe"
      ],
      [
        96,
        "hit"
      ]
    ]
  },
  {
    "id": "problem",
    "title": "The problem",
    "frames": 165,
    "voAt": 8,
    "voFrames": 152,
    "voice": "That's doomscrolling. Screen-time limits count minutes, not what you watch.",
    "onScreen": "That's doomscrolling. / Screen-time limits count minutes. Not what you watch.",
    "visual": "Oversized kinetic word, then a screen-time pill and a two-line contrast with the key phrase in periwinkle.",
    "keyFrame": 146,
    "cues": [
      [
        61,
        "whoosh"
      ]
    ]
  },
  {
    "id": "meet",
    "title": "Meet REDU",
    "frames": 180,
    "voAt": 7,
    "voFrames": 170,
    "voice": "Meet REDU: a privacy-first Android app that notices doomscrolling, and helps you pause.",
    "onScreen": "REDU · A privacy-first Android app · Notices doomscrolling. Helps you pause.",
    "visual": "A sage breathing circle inhales and blooms into cream paper; the app icon and wordmark lock up, then the purpose line.",
    "keyFrame": 168,
    "cues": [
      [
        0,
        "breath"
      ]
    ]
  },
  {
    "id": "users",
    "title": "Who it's for",
    "frames": 128,
    "voAt": 6,
    "voFrames": 118,
    "voice": "Built for adult Filipino Android users who watch short-form video.",
    "onScreen": "Built for adult Filipino Android users · TikTok · Instagram Reels · Facebook Reels",
    "visual": "Real capture: the app's platform setup screen, with the three supported apps switched on.",
    "keyFrame": 100,
    "cues": [
      [
        0,
        "whoosh"
      ]
    ]
  },
  {
    "id": "signals",
    "title": "Three signals",
    "frames": 208,
    "voice": "Three signals: session length, time per video, and negative captions.",
    "onScreen": "Session length · Time per video · Negative captions · Small on-device vision fallback note",
    "visual": "Keep the phone on the right and the three signal cards on the left. Scale and reposition the complete phone as one object to focus on its caption. Its bezel and all feed controls remain intact, extending beyond the video frame. The negative-caption card stays on the left.",
    "keyFrame": 160,
    "cues": [
      [
        30,
        "tick"
      ],
      [
        61,
        "tick"
      ],
      [
        82,
        "tick"
      ]
    ]
  },
  {
    "id": "score",
    "title": "Doomscrolling risk score",
    "frames": 100,
    "voice": "Fuzzy logic combines them into a doomscrolling risk score.",
    "onScreen": "Fuzzy logic · Doomscrolling risk score · Low / Elevated / High",
    "visual": "The three named inputs combine into the 0–100 Low / Elevated / High meter. Use the explicit doomscrolling risk score label and identify the marker as illustrative.",
    "keyFrame": 90,
    "cues": [
      [
        80,
        "tick"
      ]
    ]
  },
  {
    "id": "prompts",
    "title": "Gentle prompts",
    "frames": 262,
    "voAt": 6,
    "voFrames": 251,
    "voice": "When it rises, REDU steps in gently: a reminder, a pause, then a breathing break. You stay in control.",
    "onScreen": "1 Reminder · 2 Pause · 3 Breathe · You stay in control.",
    "visual": "Three phones: the Level 1 banner, the real Level 2 pause capture (Keep scrolling unlocks), and the Level 3 breathing circle.",
    "keyFrame": 248,
    "cues": [
      [
        96,
        "tick"
      ],
      [
        126,
        "tick"
      ],
      [
        172,
        "tick"
      ]
    ]
  },
  {
    "id": "privacy",
    "title": "Private by design",
    "frames": 118,
    "voAt": 6,
    "voFrames": 106,
    "voice": "It all runs on your phone, and raw content is never kept.",
    "onScreen": "Runs on your phone · Processed on-device · Raw text and frames discarded · Aggregate-only export",
    "visual": "Real capture: the export screen (\"Aggregate data only\") beside a three-point privacy checklist.",
    "keyFrame": 106,
    "cues": [
      [
        44,
        "tick"
      ],
      [
        56,
        "tick"
      ],
      [
        68,
        "tick"
      ]
    ]
  },
  {
    "id": "impact",
    "title": "Pilot results",
    "frames": 202,
    "voAt": 5,
    "voFrames": 190,
    "voice": "In a two-week pilot with fifty Filipino adults, prompted users scrolled less, and saw less negative content.",
    "onScreen": "2 weeks · 50 adults · 10,134 sessions · four outcomes down vs. control · SUS 80.95",
    "visual": "Counting stats, then four outcome cards with down arrows, usability and expert scores, and the study fine print.",
    "keyFrame": 190,
    "cues": [
      [
        0,
        "whoosh"
      ],
      [
        108,
        "tick"
      ],
      [
        112,
        "tick"
      ],
      [
        147,
        "tick"
      ],
      [
        160,
        "tick"
      ]
    ]
  },
  {
    "id": "outro",
    "title": "Logo & tagline",
    "frames": 122,
    "voAt": 6,
    "voFrames": 116,
    "voice": "REDU. Notice the scroll. Choose the pause.",
    "onScreen": "REDU · Notice the scroll. Choose the pause.",
    "visual": "Logo lockup returns; the tagline blurs in line by line and the mascot winks.",
    "keyFrame": 108,
    "cues": []
  },
  {
    "id": "credits",
    "title": "Credits",
    "frames": 150,
    "onScreen": "Developers, adviser, institution, thesis title, third-party credits and licenses",
    "visual": "Clean credit card on paper; fades to black.",
    "keyFrame": 110,
    "cues": [
      [
        0,
        "whoosh"
      ]
    ]
  }
];

export const SCENES: ReadonlyArray<Scene> = STORYBOARD_SCENES.map(scene => ({ ...scene, ...SCENE_TIMINGS[scene.id] }));

export const TOTAL_FRAMES = SCENES.reduce((sum, s) => sum + s.frames, 0);
if (TOTAL_FRAMES > HARD_LIMIT_FRAMES) throw new Error("AVP exceeds the absolute 60.000-second limit.");
export const sceneStart = (id: SceneId): number => {
 let start = 0;
 for (const s of SCENES) { if (s.id === id) return start; start += s.frames; }
 throw new Error("Unknown scene " + id);
};
export const sceneById = (id: SceneId): Scene => {
 const s = SCENES.find(item => item.id === id);
 if (!s) throw new Error("Unknown scene " + id);
 return s;
};
export type StoryPanel = {
 readonly scene: SceneId; readonly label: string; readonly title: string;
 readonly from: number; readonly to: number; readonly keyFrame: number;
 readonly voice: string; readonly direction: string;
};
export const PANELS: ReadonlyArray<StoryPanel> = SCENES.flatMap((s, i): StoryPanel[] => {
 const from = sceneStart(s.id);
 if (s.id === "signals") return [
  { scene: "signals", label: "05A", title: "Three signals · same layout", from, to: from + BEATS.signals.captionStart, keyFrame: BEATS.signals.captionStart - 1,
    voice: "Three signals: session length, time per video…",
    direction: "Phone on the right. Signal cards on the left. Keep this scene and phone continuously visible for the close-up." },
  { scene: "signals", label: "05B", title: "Phone grows into the close-up", from: from + BEATS.signals.captionStart, to: from + s.frames, keyFrame: s.keyFrame,
    voice: "…and negative captions.",
    direction: "Enlarge and reposition the entire phone on the right. Keep its bezel and feed intact beyond the frame; focus on the highlighted caption. Negative-caption card stays left. Hold ≥3 seconds." },
 ];
 return [{scene:s.id,label:String(i+1).padStart(2,"0"),title:s.title,from,to:from+s.frames,keyFrame:s.keyFrame,voice:s.voice??"",direction:s.visual}];
});
