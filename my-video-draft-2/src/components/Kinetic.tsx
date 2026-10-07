import React from "react";
import { Easing, interpolate, useCurrentFrame } from "remotion";

export const EASE_OUT = Easing.bezier(0.16, 1, 0.3, 1);
export const EASE_IN = Easing.bezier(0.7, 0, 0.84, 0);
export const EASE_IN_OUT = Easing.bezier(0.65, 0, 0.35, 1);

export const ramp = (frame: number, at: number, dur: number, easing = EASE_OUT) =>
  interpolate(frame, [at, at + dur], [0, 1], { extrapolateLeft: "clamp", extrapolateRight: "clamp", easing });

// Keyframed value: [frame, value] points, eased between each pair, held outside.
export const track = (frame: number, points: ReadonlyArray<readonly [number, number]>, easing = EASE_IN_OUT) => {
  if (frame <= points[0][0]) return points[0][1];
  for (let i = 1; i < points.length; i++) {
    const [f1, v1] = points[i];
    if (frame <= f1) {
      const [f0, v0] = points[i - 1];
      return v0 + (v1 - v0) * easing((frame - f0) / Math.max(1e-6, f1 - f0));
    }
  }
  return points[points.length - 1][1];
};

// Slow, irregular handheld drift in -1..1, from three detuned sines.
export const handheld = (frame: number, seed: number) => {
  const t = frame / 30;
  return Math.sin(t * 0.83 + seed) * 0.6 + Math.sin(t * 1.71 + seed * 2.3) * 0.3 + Math.sin(t * 2.9 + seed * 0.7) * 0.1;
};

export type RevealTiming = {
  readonly at: number;
  readonly dur?: number;
  readonly blur?: number;
  readonly rise?: number;
  readonly scaleFrom?: number;
  readonly exitAt?: number;
  readonly exitDur?: number;
};

// Blur-and-rise entrance with an optional blur-out exit.
export const revealStyle = (
  frame: number,
  { at, dur = 18, blur = 18, rise = 28, scaleFrom = 1, exitAt, exitDur = 10 }: RevealTiming,
): React.CSSProperties => {
  const p = ramp(frame, at, dur);
  const q = exitAt === undefined ? 0 : ramp(frame, exitAt, exitDur, EASE_IN);
  const b = (1 - p) * blur + q * 14;
  const s = scaleFrom + (1 - scaleFrom) * p;
  const style: React.CSSProperties = {
    opacity: p * (1 - q),
    translate: `0 ${(1 - p) * rise - q * 18}px`,
  };
  if (b > 0.05) {
    style.filter = `blur(${b}px)`;
  }
  if (s !== 1) {
    style.scale = String(s);
  }
  return style;
};

export const Reveal: React.FC<
  RevealTiming & { readonly children: React.ReactNode; readonly style?: React.CSSProperties }
> = ({ children, style, ...timing }) => {
  const frame = useCurrentFrame();
  return <div style={{ ...style, ...revealStyle(frame, timing) }}>{children}</div>;
};

export type PhraseTiming = {
  // The spoken onset: the phrase is readable here; its entrance starts `dur / 2` earlier.
  readonly at: number;
  readonly dur?: number;
  readonly exitAt?: number;
  readonly exitDur?: number;
  // Stand up from lying back, instead of blur-and-rise.
  readonly swing?: boolean;
};

// A whole phrase in one move: blur-and-rise in, then tip back into depth on exit.
export const phraseStyle = (frame: number, { at, dur = 14, exitAt, exitDur = 9, swing = false }: PhraseTiming): React.CSSProperties => {
  const p = ramp(frame, at - dur / 2, dur);
  const q = exitAt === undefined ? 0 : ramp(frame, exitAt, exitDur, EASE_IN);
  const b = (1 - p) * (swing ? 10 : 16) + q * 10;
  const tip = swing ? (1 - p) * 72 : 0;
  const style: React.CSSProperties = {
    opacity: Math.min(1, p * 1.4) * (1 - q),
    transform: `perspective(1400px) translate3d(0, ${(1 - p) * (swing ? 40 : 24)}px, ${-q * 220}px) rotateX(${tip + q * 32}deg)`,
    transformOrigin: "50% 100%",
  };
  if (b > 0.05) {
    style.filter = `blur(${b}px)`;
  }
  return style;
};

export const Phrase: React.FC<PhraseTiming & { readonly children: React.ReactNode; readonly style?: React.CSSProperties }> = ({ children, style, ...timing }) => {
  const frame = useCurrentFrame();
  return <div style={{ ...style, ...phraseStyle(frame, timing) }}>{children}</div>;
};

// Word by word: each word blurs and rises in, readable on its own spoken frame
// (`times`, one per word across lines). The line still leaves as one block.
export const Words: React.FC<{
  readonly text: string;
  readonly times: ReadonlyArray<number>;
  readonly dur?: number;
  readonly exitAt?: number;
  readonly exitDur?: number;
  // Style overrides by word index, counted across lines; "\n" starts a line.
  readonly wordStyle?: Readonly<Record<number, React.CSSProperties>>;
  readonly lineStyle?: Readonly<Record<number, React.CSSProperties>>;
  readonly style?: React.CSSProperties;
}> = ({ text, times, dur = 16, exitAt, exitDur, wordStyle, lineStyle, style }) => {
  const frame = useCurrentFrame();
  let index = 0;
  return (
    <div style={{ ...style, ...phraseStyle(frame, { at: -1000, exitAt, exitDur }) }}>
      {text.split("\n").map((line, l) => (
        <div key={l} style={lineStyle?.[l]}>
          {line.split(" ").map((word, w) => {
            const i = index++;
            return (
              <React.Fragment key={w}>
                {w > 0 ? " " : null}
                <span style={{ display: "inline-block", whiteSpace: "pre", ...wordStyle?.[i], ...revealStyle(frame, { at: times[i] - dur / 2, dur, blur: 20, rise: 22 }) }}>{word}</span>
              </React.Fragment>
            );
          })}
        </div>
      ))}
    </div>
  );
};

export const CountUp: React.FC<{
  readonly to: number;
  readonly at: number;
  readonly dur?: number;
  readonly from?: number;
}> = ({ to, at, dur = 30, from = 0 }) => {
  const frame = useCurrentFrame();
  const value = from + (to - from) * ramp(frame, at, dur, EASE_IN_OUT);
  return <>{Math.round(value).toLocaleString("en-US")}</>;
};
