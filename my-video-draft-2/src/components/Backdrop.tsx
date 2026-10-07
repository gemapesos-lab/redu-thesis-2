import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { COLORS, PAPER } from "../brand/tokens";

// `offset` is the scene's start frame in the full edit, so the slow drift carries across cuts.
type BackdropProps = {
  readonly offset?: number;
  readonly children?: React.ReactNode;
  readonly style?: React.CSSProperties;
};

const drift = (t: number, speed: number, phase: number, amount: number) => Math.sin(t * speed + phase) * amount;

// Warm paper with soft blooms of the app's periwinkle, honey and sage.
export const Paper: React.FC<BackdropProps> = ({ offset = 0, children, style }) => {
  const t = (useCurrentFrame() + offset) / 30;
  const blooms = [
    { color: "rgba(99,130,197,0.20)", x: 8 + drift(t, 0.21, 0, 4), y: 92 + drift(t, 0.17, 1, 3), w: 1100, h: 820 },
    { color: "rgba(227,183,111,0.20)", x: 94 + drift(t, 0.19, 2, 3), y: 6 + drift(t, 0.23, 3, 4), w: 1000, h: 760 },
    { color: "rgba(143,181,154,0.16)", x: 96 + drift(t, 0.15, 4, 3), y: 96 + drift(t, 0.2, 5, 3), w: 900, h: 700 },
  ];
  return (
    <AbsoluteFill style={{ backgroundColor: PAPER.paper, overflow: "hidden", ...style }}>
      <AbsoluteFill
        style={{
          background: blooms.map((b) => `radial-gradient(${b.w}px ${b.h}px at ${b.x}% ${b.y}%, ${b.color}, transparent 70%)`).join(", "),
        }}
      />
      {children}
    </AbsoluteFill>
  );
};

// The late-night scroll: near-black with a faint cold screen glow.
export const Night: React.FC<BackdropProps & { readonly glow?: number }> = ({ offset = 0, glow = 1, children, style }) => {
  const t = (useCurrentFrame() + offset) / 30;
  return (
    <AbsoluteFill style={{ backgroundColor: PAPER.dark, overflow: "hidden", ...style }}>
      <AbsoluteFill
        style={{
          opacity: glow,
          background: `radial-gradient(900px 700px at ${50 + drift(t, 0.2, 0, 3)}% ${46 + drift(t, 0.16, 1, 3)}%, rgba(99,130,197,0.16), transparent 70%)`,
        }}
      />
      <AbsoluteFill style={{ background: `radial-gradient(ellipse at 50% 50%, transparent 50%, ${COLORS.night} 100%)`, opacity: 0.7 }} />
      {children}
    </AbsoluteFill>
  );
};
