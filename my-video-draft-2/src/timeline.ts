// Draft 2 · 3D polish: every scene redesigned around the supplied 3D-polish take.
import { HOOK, SFX, type Sfx } from "./choreography.ts";
import { MEASURED_BEATS, SCENE_TIMINGS } from "./voiceover-cues.ts";
export const FPS = 30;
export const HARD_LIMIT_FRAMES = 1800;
export const NARRATION_BUDGET_FRAMES = 1620;
export const VOICEOVER = {
 status: "ready" as "pending" | "ready",
 source: "audio/vo/3d-polish-original.mp3", audio: "audio/vo/3d-polish.wav",
 productionSource: "audio/vo/3d-polish-edited.wav",
 auditFolder: "production/voiceover/3d-polish",
 frames: 1608, credit: "Narration: ElevenLabs v4 (Lauren)",
};
export type Cue = Sfx;
export type SceneId = "hook" | "problem" | "meet" | "users" | "signals" | "score" | "prompts" | "privacy" | "impact" | "outro" | "credits";
export type Scene = {
 readonly id: SceneId; readonly title: string; readonly frames: number;
 readonly voAt?: number; readonly voFrames?: number; readonly voice?: string;
 readonly onScreen: string; readonly visual: string; readonly keyFrame: number;
 readonly cues: ReadonlyArray<readonly [number, Cue, number]>;
};

export const BEATS = MEASURED_BEATS;

const STORYBOARD_SCENES: ReadonlyArray<Omit<Scene, "frames" | "cues" | "voAt" | "voFrames">> = [
  {
    id: "hook",
    title: "Just one more",
    voice: "Just one more video... Then another. And another... Suddenly, it’s two-oh-seven A.M.",
    onScreen: "Just one more video… → 2:07 AM",
    visual: "Text alone, then a 3D phone rises. The camera orbits as the feed accelerates, then racks focus to a red LED alarm clock that ticks from 2:06 to 2:07.",
    keyFrame: MEASURED_BEATS.hook.amEnd - 2,
  },
  {
    id: "problem",
    title: "The problem",
    voice: "That's doomscrolling. Screen-time limits count minutes, NOT what you watch.",
    onScreen: "That's doomscrolling. · Screen-time limits count minutes. · Not what you watch.",
    visual: "The title swings up out of the dark. A screen-time widget counts minutes; on “NOT,” focus pulls to the videos behind it.",
    keyFrame: MEASURED_BEATS.problem.watchEnd - 8,
  },
  {
    id: "meet",
    title: "Meet REDU",
    voice: "Meet REDU: a privacy-first Android app that notices doomscrolling, and helps you pause.",
    onScreen: "REDU · A privacy-first Android app · Notices doomscrolling. Helps you pause.",
    visual: "Breath bloom into paper. The 3D app icon spins in and lands; the mascot glances at “Notices doomscrolling.”",
    keyFrame: MEASURED_BEATS.meet.pauseEnd - 2,
  },
  {
    id: "users",
    title: "Who it's for",
    voice: "Built for adult Filipino Android users who watch short-form video.",
    onScreen: "Built for adult Filipino Android users · TikTok · Instagram Reels · Facebook Reels",
    visual: "A 3D phone with the real setup screen swings in; three platform chips click into place.",
    keyFrame: MEASURED_BEATS.users.videoEnd - 6,
  },
  {
    id: "signals",
    title: "Three signals",
    voice: "Three signals: session length, time per video, and negative captions.",
    onScreen: "Session length · Time per video · Negative captions · On-device vision fallback note",
    visual: "The same phone spins around to the feed, three signal cards land, and the camera pushes into “nakakalungkot.”",
    keyFrame: MEASURED_BEATS.signals.captionsEnd - 8,
  },
  {
    id: "score",
    title: "Doomscrolling risk score",
    voice: "Fuzzy logic combines them into a doomscrolling risk score.",
    onScreen: "Fuzzy logic · Doomscrolling risk score · Low / Elevated / High · Illustrative",
    visual: "The cards fly together into a 3D risk meter; the puck springs into Elevated.",
    keyFrame: MEASURED_BEATS.score.scoreEnd - 4,
  },
  {
    id: "prompts",
    title: "Gentle prompts",
    voice: "When it rises, REDU steps in gently: a reminder, a pause, then a breathing break. You stay in control.",
    onScreen: "REDU steps in gently. · 1 Reminder · 2 Pause · 3 Breathe · You stay in control.",
    visual: "The meter tips into a track. As the puck climbs, each prompt phone rises at its risk band, then the camera pulls back to all three.",
    keyFrame: MEASURED_BEATS.prompts.controlEnd - 10,
  },
  {
    id: "privacy",
    title: "Private by design",
    voice: "It all runs on your phone, and raw content is never kept.",
    onScreen: "Private by design · Runs on your phone. · Processed on-device · Raw text and screen frames discarded · Exports hold aggregate data only",
    visual: "The export-screen phone turns on a turntable; a raw caption lifts off and dissolves.",
    keyFrame: MEASURED_BEATS.privacy.keptEnd - 4,
  },
  {
    id: "impact",
    title: "Pilot results",
    voice: "In a two-week pilot with fifty Filipino adults, prompted users scrolled less, and saw less negative content.",
    onScreen: "2 weeks · 50 Filipino adults · 10,134 sessions → Prompted users scrolled less and saw less negative content · four outcomes · SUS 80.95",
    visual: "Stat tiles flip up, then flip away before the results arrive.",
    keyFrame: MEASURED_BEATS.impact.contentEnd - 8,
  },
  {
    id: "outro",
    title: "Logo & tagline",
    voice: "REDU. Notice the scroll. Choose the pause.",
    onScreen: "REDU · Notice the scroll. Choose the pause.",
    visual: "The icon spins in once more, then the tagline and the wink.",
    keyFrame: MEASURED_BEATS.outro.pauseEnd + 10,
  },
  {
    id: "credits",
    title: "Credits",
    onScreen: "Developers, adviser, institution, thesis title, third-party credits and licenses",
    visual: "Clean credit card on paper; fades to black.",
    keyFrame: 110,
  },
];

export const SCENES: ReadonlyArray<Scene> = STORYBOARD_SCENES.map((scene) => ({ ...scene, ...SCENE_TIMINGS[scene.id], cues: SFX[scene.id] }));

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
 if (s.id === "hook") return [
  { scene: "hook", label: "01A", title: "Just one more", from, to: from + HOOK.rack, keyFrame: MEASURED_BEATS.hook.another2 + 8,
    voice: "Just one more video... Then another. And another...",
    direction: "Text alone. On “Then,” a 3D phone rises; six swipes speed up while the camera orbits and the room takes each video's color." },
  { scene: "hook", label: "01B", title: "2:07 AM", from: from + HOOK.rack, to: from + s.frames, keyFrame: s.keyFrame,
    voice: "Suddenly, it’s two-oh-seven A.M.",
    direction: "Rack focus: the phone slides out, the red LED clock sharpens, and 2:06 switches to 2:07 on “seven.” Push into the glow." },
 ];
 return [{ scene: s.id, label: String(i + 1).padStart(2, "0"), title: s.title, from, to: from + s.frames, keyFrame: s.keyFrame, voice: s.voice ?? "", direction: s.visual }];
});
