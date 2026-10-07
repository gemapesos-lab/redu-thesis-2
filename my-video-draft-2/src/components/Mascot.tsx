/*
 * Idle motion and face geometry adapted from blobatar 2.7.0 (MIT, Copyright (c) Alain),
 * via the REDU Android app's ReduBlobatar. Seed "redu", round silhouette.
 * https://github.com/Alain00/blobatar
 */
import React from "react";
import { useCurrentFrame, useVideoConfig } from "remotion";
import { COLORS } from "../brand/tokens";

export type MascotExpression =
  | "idle"
  | "sleepy"
  | "happy"
  | "wink"
  | "smug"
  | "unsure"
  | "mad"
  | "look";

type Pose = {
  esx: number;
  esy: number;
  tilt: number;
  edy: number;
  edx: number;
  esx2: number;
  esy2: number;
  tilt2: number;
  edy2: number;
  lock: number;
  shake: number;
  rock: number;
  bdy: number;
};

const IDLE: Pose = {
  esx: 1,
  esy: 1,
  tilt: 0,
  edy: 0,
  edx: 0,
  esx2: 0,
  esy2: 0,
  tilt2: 0,
  edy2: 0,
  lock: 0,
  shake: 0,
  rock: 0,
  bdy: 0,
};

const POSES: Record<MascotExpression, Pose> = {
  idle: IDLE,
  sleepy: { ...IDLE, esx: 1.14, esy: 0.22, edy: 2.4, edx: 0.3, esx2: -0.04, esy2: 0.03, tilt2: 4, lock: 1, bdy: 1.2 },
  happy: { ...IDLE, esx: 1.72, esy: 0.3, tilt: 8, edy: -1.5, edx: 1.5, esx2: 0.08, esy2: 0.05, tilt2: -16, lock: 1, bdy: -2.2 },
  wink: { ...IDLE, esx: 1.32, esy: 0.76, tilt: 5, edy: -0.6, edx: 0.8, esx2: 0.26, esy2: -0.56, tilt2: -11, lock: 1, bdy: -1.1 },
  smug: { ...IDLE, esx: 1.3, esy: 0.42, tilt: 18, edy: -0.5, edx: 0.5, esx2: 0.06, esy2: -0.06, tilt2: -36, lock: 1, bdy: -1 },
  unsure: { ...IDLE, esx: 0.95, esy: 1.02, tilt: 4, edy: -0.2, edx: 0.3, esx2: 0.24, esy2: -0.44, tilt2: -18, lock: 1 },
  mad: { ...IDLE, esx: 1.85, esy: 0.26, tilt: -33, edy: 0.4, edx: 0.6, esy2: -0.03, tilt2: 5, lock: 1, shake: 0.55, bdy: 0.8 },
  look: { ...IDLE, esy: 0.9, edy: 4.2, edx: -0.4, lock: 1, bdy: 0.6 },
};

const BODY =
  "M84.49 50.43C84.49 69.43 69.78 83.54 49.97 83.54C30.16 83.54 15.45 69.43 15.45 50.43C15.45 31.44 30.16 17.33 49.97 17.33C69.78 17.33 84.49 31.44 84.49 50.43Z";

const EYES = [
  {
    d: "M39.96 53C39.06 60.48 38.66 61.16 35.39 60.77C32.11 60.37 31.89 59.62 32.79 52.13C33.69 44.65 34.09 43.97 37.36 44.36C40.63 44.76 40.86 45.51 39.96 53Z",
    cx: 36.37132736858499,
    cy: 52.5651062406572,
    rot: 6.853573327884078,
  },
  {
    d: "M64.28 52.09C63.44 60.38 62.98 61.14 59.02 60.74C55.06 60.34 54.76 59.5 55.59 51.21C56.43 42.91 56.89 42.16 60.85 42.56C64.81 42.96 65.11 43.79 64.28 52.09Z",
    cx: 59.934747509695974,
    cy: 51.6482069878245,
    rot: 5.772371276281774,
  },
];

const SEEDS = {
  phase: 1403,
  bob: 2450,
  blink: 4474,
  blinkPhase: 2563,
  saccade: 5856,
  saccadePhase: 2938,
  lookX: 1.53,
  lookY: 1.17,
  lookMX: 1.53,
  lookMY: 1.17,
};

const SACCADE_STOPS = [
  [0, 0, 0],
  [0.15, 0, 0],
  [0.165, -0.8, -0.9],
  [0.31, -0.8, -0.9],
  [0.325, 1, 0.1],
  [0.47, 1, 0.1],
  [0.485, -0.15, 0.85],
  [0.63, -0.15, 0.85],
  [0.645, 0.75, -0.8],
  [0.79, 0.75, -0.8],
  [0.805, -1, -0.15],
  [0.985, -1, -0.15],
  [1, 0, 0],
];

const WRAP_STOPS = [
  [0, 0, 0, 0, 0],
  [0.15, 0, 0, 0, 0],
  [0.165, -0.0176, 0.008, -0.027, 0.648],
  [0.31, -0.0176, 0.008, -0.027, 0.648],
  [0.325, -0.022, -0.01, -0.003, 0.09],
  [0.47, -0.022, -0.01, -0.003, 0.09],
  [0.485, -0.0033, 0.0015, -0.0255, -0.115],
  [0.63, -0.0033, 0.0015, -0.0255, -0.115],
  [0.645, -0.0165, -0.0075, -0.024, -0.54],
  [0.79, -0.0165, -0.0075, -0.024, -0.54],
  [0.805, -0.022, 0.01, -0.0045, 0.135],
  [0.985, -0.022, 0.01, -0.0045, 0.135],
  [1, 0, 0, 0, 0],
];

const SHAKE_STOPS = [
  [0, 0.62, -0.34],
  [0.25, -0.7, 0.22],
  [0.5, 0.38, 0.66],
  [0.75, -0.44, -0.6],
  [1, 0.62, -0.34],
];

const bezier = (x1: number, y1: number, x2: number, y2: number) => {
  const cx = 3 * x1;
  const bx = 3 * (x2 - x1) - cx;
  const ax = 1 - cx - bx;
  const cy = 3 * y1;
  const by = 3 * (y2 - y1) - cy;
  const ay = 1 - cy - by;
  return (x: number) => {
    let t = x;
    for (let step = 0; step < 8; step++) {
      const err = ((ax * t + bx) * t + cx) * t - x;
      if (Math.abs(err) < 1e-5) break;
      const derivative = (3 * ax * t + 2 * bx) * t + cx;
      if (Math.abs(derivative) < 1e-6) break;
      t -= err / derivative;
    }
    return ((ay * t + by) * t + cy) * t;
  };
};

const easeInOut = bezier(0.42, 0, 0.58, 1);
const easeIn = bezier(0.42, 0, 1, 1);
const easeOut = bezier(0, 0, 0.58, 1);

const cycle = (timeMs: number, phase: number, period: number) => {
  const u = (timeMs + phase) / period;
  return u - Math.floor(u);
};

const alternate = (timeMs: number, phase: number, period: number) => {
  const u = (timeMs + phase) / period;
  const n = Math.floor(u);
  const fraction = u - n;
  return n % 2 !== 0 ? 1 - fraction : fraction;
};

const stops = (u: number, table: number[][], col: number) => {
  for (let i = table.length - 1; i >= 0; i--) {
    const row = table[i];
    if (u < row[0]) continue;
    const next = table[i + 1];
    if (!next) return row[col];
    const span = next[0] - row[0];
    return span <= 0 ? row[col] : row[col] + (next[col] - row[col]) * ((u - row[0]) / span);
  }
  return table[0][col];
};

const idleFrame = (timeMs: number, amp: number, shakeAmp: number) => {
  const breatheU = easeInOut(alternate(timeMs, SEEDS.phase, 2800));
  const bobU = easeInOut(alternate(timeMs, SEEDS.bob, 3400));
  const sac = cycle(timeMs, SEEDS.saccadePhase, SEEDS.saccade);
  const shaken = cycle(timeMs, 0, 112);
  const rock = cycle(timeMs, 0, 900);
  const rockp = rock < 0.5 ? 1 - 2 * easeInOut(rock * 2) : -1 + 2 * easeInOut(rock * 2 - 1);
  const blinkU = cycle(timeMs, SEEDS.blinkPhase, SEEDS.blink);
  const blink =
    blinkU < 0.972
      ? 1
      : blinkU < 0.986
        ? 1 - 0.92 * amp * easeIn((blinkU - 0.972) / 0.014)
        : 1 - 0.92 * amp * (1 - easeOut((blinkU - 0.986) / 0.014));
  return {
    shake: [stops(shaken, SHAKE_STOPS, 1) * shakeAmp, stops(shaken, SHAKE_STOPS, 2) * shakeAmp],
    breathe: [1 + 0.022 * amp * breatheU, 1 - 0.018 * amp * breatheU],
    bob: -1.1 * amp * bobU,
    saccade: [
      stops(sac, SACCADE_STOPS, 1) * SEEDS.lookX * amp,
      stops(sac, SACCADE_STOPS, 2) * SEEDS.lookY * amp,
    ],
    rockp,
    blink,
    wrap: {
      mx: stops(sac, WRAP_STOPS, 1) * SEEDS.lookMX * amp,
      side: stops(sac, WRAP_STOPS, 2) * SEEDS.lookX * amp,
      sy: stops(sac, WRAP_STOPS, 3) * SEEDS.lookMY * amp,
      rot: stops(sac, WRAP_STOPS, 4) * SEEDS.lookX * SEEDS.lookY * amp,
    },
  };
};

const lerpPose = (from: Pose, to: Pose, t: number): Pose => {
  const out = { ...from };
  for (const key of Object.keys(from) as (keyof Pose)[]) {
    out[key] = from[key] + (to[key] - from[key]) * t;
  }
  return out;
};

type MascotProps = {
  readonly size: number;
  readonly expression?: MascotExpression;
  // Morph toward a second expression; 0 = `expression`, 1 = `blendTo`.
  readonly blendTo?: MascotExpression;
  readonly blend?: number;
  readonly animate?: boolean;
  readonly color?: string;
  readonly style?: React.CSSProperties;
};

export const Mascot: React.FC<MascotProps> = ({
  size,
  expression = "idle",
  blendTo,
  blend = 0,
  animate = true,
  color = COLORS.figure,
  style,
}) => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const pose = blendTo ? lerpPose(POSES[expression], POSES[blendTo], blend) : POSES[expression];
  const f = idleFrame((frame / fps) * 1000, animate ? 1 : 0, pose.shake);

  return (
    <svg viewBox="0 0 100 100" width={size} height={size} style={{ overflow: "visible", ...style }}>
      <g
        transform={`translate(${f.shake[0]} ${f.shake[1]}) translate(50 50) scale(${f.breathe[0]} ${f.breathe[1]}) translate(-50 -50) translate(0 ${pose.bdy + f.bob})`}
      >
        <path d={BODY} fill={color} />
        <g transform={`translate(${f.saccade[0]} ${f.saccade[1]})`}>
          {EYES.map((eye, index) => {
            const wrap = index === 0 ? -1 : 1;
            const sel = index === 0 ? 0 : 1;
            const phase = sel * (1 - pose.rock) + pose.rock * ((1 + wrap * f.rockp) / 2);
            const posed = `translate(${eye.cx + pose.edx * wrap} ${eye.cy + pose.edy + phase * pose.edy2}) rotate(${(pose.tilt + sel * pose.tilt2) * wrap + eye.rot * (1 - pose.lock)}) scale(${pose.esx + sel * pose.esx2} ${pose.esy + sel * pose.esy2}) rotate(${-eye.rot}) translate(${-eye.cx} ${-eye.cy})`;
            const glance = `translate(${eye.cx} ${eye.cy}) rotate(${f.wrap.rot * wrap}) scale(${1 + f.wrap.mx + f.wrap.side * wrap} ${1 + f.wrap.sy}) rotate(${eye.rot}) scale(1 ${f.blink}) rotate(${-eye.rot}) translate(${-eye.cx} ${-eye.cy})`;
            return <path key={eye.d} d={eye.d} fill={COLORS.figureInk} transform={`${posed} ${glance}`} />;
          })}
        </g>
      </g>
    </svg>
  );
};
