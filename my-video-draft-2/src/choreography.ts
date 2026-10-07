// Derived timings for the 3D polish, all relative to each scene's first frame and
// computed from the measured words, so a re-aligned take moves the picture with it.
// Plain TypeScript: scripts/check-storyboard.mjs imports this to verify the text rules.
import { MEASURED_BEATS as B, SCENE_TIMINGS } from "./voiceover-cues.ts";

type SceneKey = keyof typeof SCENE_TIMINGS;
const frames = (id: SceneKey) => SCENE_TIMINGS[id].frames;

// A phrase is readable on `at`; its entrance starts `dur / 2` earlier.
export const ENTRANCE = 14;
export const EXIT = 9;
// For a word at the top of a scene: the entrance starts on the scene's first frame, not before the cut.
const opening = (frame: number) => Math.max(frame, ENTRANCE / 2);

export const HOOK = {
  titleExit: B.hook.then - 12,
  phoneAt: B.hook.then - 2,
  // Six swipes, each shorter than the last; the third lands on the second "another".
  swipes: [[B.hook.another + 2, 17], [B.hook.another + 27, 15], [B.hook.another2, 13], [B.hook.another2 + 18, 11], [B.hook.another2 + 32, 9], [B.hook.another2 + 43, 8]] as ReadonlyArray<readonly [number, number]>,
  rack: B.hook.suddenly,
  rackDur: 30,
  flip: B.hook.seven - 2,
  out: frames("hook") - 14,
} as const;

export const PROBLEM = {
  titleExit: B.problem.screen - 19,
  widgetAt: B.problem.screen - 2,
  countEnd: B.problem.not - 3,
  rack: B.problem.not,
  out: frames("problem") - 12,
} as const;

export const MEET = {
  spin: 5,
  land: 22,
  lift: B.meet.privacy - 12,
  liftDur: 16,
  look: B.meet.notices,
  lookBack: B.meet.helps - 12,
  blink: B.meet.pause,
  out: frames("meet") - 12,
} as const;

export const USERS = {
  chips: [B.users.short, B.users.short + 4, B.users.short + 8],
  textExit: frames("users") - 11,
} as const;

export const SIGNALS = {
  spin: 26,
  swap: 13,
  cards: [B.signals.session, B.signals.time, B.signals.negative],
  closeStart: B.signals.negative - 22,
  closeReady: B.signals.negative + 4,
  out: frames("signals") - 12,
} as const;

export const SCORE = {
  fly: 30,
  meterAt: B.score.combines,
  meterDur: 22,
  value: 42,
  textExit: frames("score") - 11,
} as const;

// Prompt levels sit on the score: Reminder and Pause in the lower and upper
// Elevated band, the breathing break in High (paper, Fuzzy Risk Estimation and Interventions).
export const PROMPTS = {
  crane: [B.prompts.rises - 5, B.prompts.rises + 27] as const,
  levels: [
    { at: B.prompts.reminder, value: 48 },
    { at: B.prompts.pause, value: 62 },
    { at: B.prompts.breathing, value: 84 },
  ],
  titleExit: B.prompts.you - 18,
  pullBack: [B.prompts.you, B.prompts.you + 28] as const,
  unlock: B.prompts.you + 4,
  out: frames("prompts") - 14,
} as const;

export const PRIVACY = {
  points: [B.privacy.runs, B.privacy.raw, B.privacy.kept],
  chip: B.privacy.raw,
  out: frames("privacy") - 9,
} as const;

export const IMPACT = {
  tiles: [B.impact.twoWeek, B.impact.fifty, B.impact.filipino],
  tilesOut: B.impact.adultsEnd - 1,
  cards: [0, 3, 6, 9].map((d) => B.impact.scrolled + d),
  badges: B.impact.scrolled + 18,
  footnote: B.impact.scrolled + 24,
  out: frames("impact") - 9,
} as const;

export const OUTRO = {
  land: 16,
  wink: B.outro.pauseEnd,
  out: frames("outro") - 8,
} as const;

// On-screen text: the frame box each block occupies and when. Blocks that share
// screen space must never be visible on the same frame.
export type TextBlock = {
  readonly id: string;
  readonly box: readonly [number, number, number, number];
  readonly at: number;
  readonly exitAt: number;
  readonly dur?: number;
  readonly exitDur?: number;
};

export const TEXT: Readonly<Record<SceneKey | "credits", ReadonlyArray<TextBlock>>> = {
  hook: [{ id: "just-one-more", box: [260, 450, 1400, 180], at: 7, exitAt: HOOK.titleExit, dur: 14, exitDur: 10 }],
  problem: [
    { id: "thats-doomscrolling", box: [300, 330, 1320, 380], at: B.problem.that, exitAt: PROBLEM.titleExit, exitDur: 10 },
    { id: "count-minutes", box: [150, 290, 880, 200], at: B.problem.screen, exitAt: PROBLEM.out },
    { id: "not-what-you-watch", box: [150, 520, 880, 260], at: B.problem.not, exitAt: PROBLEM.out },
  ],
  meet: [
    // After the lockup has lifted clear of this line.
    { id: "privacy-first", box: [460, 545, 1000, 50], at: B.meet.privacy + 4, exitAt: MEET.out },
    { id: "notices", box: [360, 615, 1200, 120], at: B.meet.notices, exitAt: MEET.out },
    { id: "helps", box: [360, 735, 1200, 120], at: B.meet.helps, exitAt: MEET.out },
  ],
  users: [
    { id: "built-for", box: [150, 300, 960, 330], at: opening(B.users.built), exitAt: USERS.textExit, exitDur: 8 },
    { id: "who-watch", box: [150, 655, 960, 60], at: B.users.who, exitAt: USERS.textExit, exitDur: 8 },
  ],
  signals: [{ id: "three-signals", box: [130, 70, 820, 130], at: opening(B.signals.three), exitAt: SIGNALS.out, exitDur: 10 }],
  score: [
    { id: "fuzzy-logic", box: [560, 130, 800, 50], at: opening(B.score.fuzzy), exitAt: SCORE.textExit },
    { id: "risk-score", box: [360, 205, 1200, 130], at: B.score.doom, exitAt: SCORE.textExit },
    { id: "illustrative", box: [460, 830, 1000, 50], at: B.score.score, exitAt: SCORE.textExit },
  ],
  prompts: [
    { id: "steps-in-gently", box: [360, 40, 1200, 110], at: B.prompts.redu, exitAt: PROMPTS.titleExit, exitDur: 8 },
    { id: "you-stay-in-control", box: [360, 40, 1200, 110], at: B.prompts.you, exitAt: PROMPTS.out },
  ],
  privacy: [
    { id: "runs-on-your-phone", box: [960, 270, 860, 300], at: opening(B.privacy.it), exitAt: PRIVACY.out, exitDur: 8 },
    { id: "checklist", box: [960, 600, 860, 220], at: B.privacy.runs, exitAt: PRIVACY.out, exitDur: 8 },
  ],
  impact: [
    { id: "pilot-study", box: [560, 280, 800, 50], at: opening(B.impact.in), exitAt: IMPACT.tilesOut - 3, exitDur: 9 },
    { id: "stat-tiles", box: [220, 360, 1480, 340], at: IMPACT.tiles[0], exitAt: IMPACT.tilesOut, exitDur: 10 },
    { id: "results-headline", box: [160, 100, 1600, 170], at: B.impact.prompted, exitAt: IMPACT.out, exitDur: 8 },
    { id: "outcome-cards", box: [158, 320, 1604, 240], at: IMPACT.cards[0], exitAt: IMPACT.out, exitDur: 8 },
    { id: "badges", box: [440, 610, 1040, 80], at: IMPACT.badges, exitAt: IMPACT.out, exitDur: 8 },
    { id: "footnote", box: [220, 950, 1480, 80], at: IMPACT.footnote, exitAt: IMPACT.out, exitDur: 8 },
  ],
  outro: [
    { id: "notice-the-scroll", box: [460, 640, 1000, 110], at: B.outro.notice, exitAt: OUTRO.out, exitDur: 8 },
    { id: "choose-the-pause", box: [460, 750, 1000, 110], at: B.outro.choose, exitAt: OUTRO.out, exitDur: 8 },
  ],
  credits: [],
};

export const textBlock = (scene: SceneKey, id: string) => {
  const block = TEXT[scene].find((b) => b.id === id);
  if (!block) throw new Error(`Unknown text block ${scene}/${id}`);
  return block;
};

export type Sfx =
  | "rise" | "land" | "swipe1" | "swipe2" | "swipe3" | "swipe4" | "swipe5" | "swipe6" | "rack" | "blip" | "sub"
  | "swing" | "tick" | "focus" | "recede" | "breath" | "spin" | "truck" | "toggle" | "pop1" | "pop2" | "pop3"
  | "marker" | "push" | "fly" | "lock" | "slide" | "click" | "riser" | "lift" | "chime" | "lockclick" | "unlock"
  | "sink" | "check" | "shimmer" | "flap" | "roll" | "down1" | "down2" | "down3" | "down4" | "sparkle" | "whoosh";

// [frame, effect, volume]
export type SfxCue = readonly [number, Sfx, number];

export const SFX: Readonly<Record<SceneKey | "credits", ReadonlyArray<SfxCue>>> = {
  hook: [
    [HOOK.phoneAt, "rise", 0.5],
    [HOOK.phoneAt + 20, "land", 0.35],
    ...HOOK.swipes.map(([at], i) => [at, `swipe${i + 1}` as Sfx, 0.5 + 0.04 * i] as const),
    [HOOK.rack, "rack", 0.55],
    [HOOK.flip + 1, "blip", 0.42],
    [HOOK.flip + 1, "sub", 0.8],
  ],
  problem: [
    [B.problem.that - 6, "swing", 0.42],
    [PROBLEM.widgetAt, "swing", 0.3],
    ...[0, 1, 2, 3, 4].map((i) => [B.problem.screen + 4 + i * 11, "tick", 0.24 - i * 0.015] as const),
    [PROBLEM.rack, "focus", 0.3],
    [PROBLEM.out - 2, "recede", 0.35],
  ],
  meet: [
    [0, "breath", 0.7],
    // Under "REDU": the spin stays beneath the brand name.
    [MEET.spin, "spin", 0.32],
    [MEET.out - 2, "truck", 0.35],
  ],
  users: [
    [0, "swing", 0.4],
    ...USERS.chips.map((at) => [at, "toggle", 0.38] as const),
  ],
  signals: [
    [0, "spin", 0.45],
    [SIGNALS.cards[0] - 6, "pop1", 0.36],
    [SIGNALS.cards[1] - 6, "pop2", 0.36],
    [SIGNALS.cards[2] - 6, "pop3", 0.38],
    [SIGNALS.closeStart, "push", 0.4],
    [B.signals.negative - 3, "marker", 0.32],
    [SIGNALS.out, "whoosh", 0.32],
  ],
  score: [
    [1, "fly", 0.32],
    [5, "fly", 0.3],
    [9, "fly", 0.28],
    [SCORE.meterAt + 14, "lock", 0.45],
    [B.score.doom, "slide", 0.22],
    [B.score.doom + 26, "click", 0.34],
  ],
  prompts: [
    [PROMPTS.crane[0], "riser", 0.4],
    ...PROMPTS.levels.map(({ at }) => [at - 6, "lift", 0.34] as const),
    [PROMPTS.levels[0].at + 2, "chime", 0.34],
    [PROMPTS.levels[1].at - 2, "lockclick", 0.36],
    [PROMPTS.levels[2].at, "breath", 0.45],
    [PROMPTS.unlock, "unlock", 0.38],
    [PROMPTS.out, "sink", 0.32],
  ],
  privacy: [
    [0, "rise", 0.4],
    ...PRIVACY.points.map((at, i) => [at - 4, "check", 0.3 + 0.02 * i] as const),
    [PRIVACY.chip + 6, "shimmer", 0.38],
  ],
  impact: [
    ...IMPACT.tiles.map((at) => [at - 6, "flap", 0.36] as const),
    [IMPACT.tiles[2] - 2, "roll", 0.3],
    [IMPACT.tilesOut, "whoosh", 0.3],
    ...IMPACT.cards.map((at, i) => [at - 4, `down${i + 1}` as Sfx, 0.3] as const),
  ],
  outro: [
    [0, "spin", 0.42],
    [OUTRO.wink, "sparkle", 0.3],
  ],
  credits: [],
};
