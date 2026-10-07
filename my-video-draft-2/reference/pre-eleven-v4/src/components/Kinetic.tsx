import React from "react";
import { Easing, interpolate, useCurrentFrame } from "remotion";

export const EASE_OUT = Easing.bezier(0.16, 1, 0.3, 1);
export const EASE_IN = Easing.bezier(0.7, 0, 0.84, 0);
export const EASE_IN_OUT = Easing.bezier(0.65, 0, 0.35, 1);

export const ramp = (frame: number, at: number, dur: number, easing = EASE_OUT) =>
  interpolate(frame, [at, at + dur], [0, 1], { extrapolateLeft: "clamp", extrapolateRight: "clamp", easing });

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

// Blur-out only, for elements that are already on screen.
export const exitStyle = (frame: number, at: number, dur = 10): React.CSSProperties => revealStyle(frame, { at: -dur * 4, exitAt: at, exitDur: dur });

export const Reveal: React.FC<
  RevealTiming & { readonly children: React.ReactNode; readonly style?: React.CSSProperties }
> = ({ children, style, ...timing }) => {
  const frame = useCurrentFrame();
  return <div style={{ ...style, ...revealStyle(frame, timing) }}>{children}</div>;
};

type WordsProps = {
  // Opt in only for the revised scenes. Other scenes retain draft-1 motion.
  readonly readableOnset?: boolean;
  // "\n" starts a new line.
  readonly text: string;
  readonly at?: number;
  readonly stagger?: number;
  // Per-word start frames, e.g. synced to the narration. Overrides `at` + `stagger`.
  readonly times?: ReadonlyArray<number>;
  readonly dur?: number;
  readonly blur?: number;
  readonly rise?: number;
  readonly exitAt?: number;
  readonly exitDur?: number;
  // Style overrides by word index, counted across lines.
  readonly wordStyle?: Readonly<Record<number, React.CSSProperties>>;
  readonly style?: React.CSSProperties;
};

// Word-by-word blur reveal for kinetic headlines.
export const Words: React.FC<WordsProps> = ({
  readableOnset = false,
  text,
  at = 0,
  stagger = 4,
  times,
  dur = 16,
  blur = 20,
  rise = 22,
  exitAt,
  exitDur,
  wordStyle,
  style,
}) => {
  const frame = useCurrentFrame();
  let index = 0;
  return (
    <div style={style}>
      {text.split("\n").map((line, l) => (
        <div key={l}>
          {line.split(" ").map((word, w) => {
            const i = index++;
            const start = times && times[i] !== undefined ? times[i] : at + i * stagger;
            return (
              <React.Fragment key={w}>
                {w > 0 ? " " : null}
                <span
                  style={{
                    display: "inline-block",
                    whiteSpace: "pre",
                    ...(wordStyle ? wordStyle[i] : undefined),
                    ...revealStyle(frame, { at: readableOnset ? start - dur : start, dur, blur, rise, exitAt, exitDur }),
                    ...(readableOnset && frame < start ? { opacity: 0 } : {}),
                  }}
                >
                  {word}
                </span>
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
