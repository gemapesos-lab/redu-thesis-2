import React from "react";
import { useCurrentFrame } from "remotion";
import { COLORS, FONT, PAPER } from "../brand/tokens";
import { revealStyle } from "./Kinetic";
import { Mascot, type MascotExpression } from "./Mascot";

type IconProps = {
  readonly size: number;
  readonly expression?: MascotExpression;
  readonly blendTo?: MascotExpression;
  readonly blend?: number;
  readonly style?: React.CSSProperties;
};

// The launcher icon: the mascot fills the visible 72 dp of the adaptive icon on #0A0A0A.
export const AppIcon: React.FC<IconProps> = ({ size, expression = "idle", blendTo, blend, style }) => (
  <div
    style={{
      width: size,
      height: size,
      borderRadius: size * 0.3,
      background: `radial-gradient(120% 90% at 30% 0%, #262422 0%, ${COLORS.launcherTile} 60%)`,
      boxShadow: `0 ${size * 0.14}px ${size * 0.3}px rgba(26,21,18,0.22), 0 ${size * 0.03}px ${size * 0.08}px rgba(26,21,18,0.2), inset 0 1px 0 rgba(255,255,255,0.08)`,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      flexShrink: 0,
      ...style,
    }}
  >
    <Mascot size={size} expression={expression} blendTo={blendTo} blend={blend} />
  </div>
);

export const Wordmark: React.FC<{ readonly size: number; readonly color?: string; readonly at?: number; readonly readableAt?: number }> = ({
  size,
  color = PAPER.ink,
  at,
  readableAt,
}) => {
  const frame = useCurrentFrame();
  return (
    <div style={{ fontFamily: FONT, fontWeight: 800, fontSize: size, lineHeight: 1, letterSpacing: "0.02em", color, display: "flex" }}>
      {"REDU".split("").map((letter, i) => (
        <span key={i} style={readableAt !== undefined
          ? revealStyle(frame, { at: readableAt - 14 + i * 2, dur: 20, blur: 24, rise: size * 0.18 })
          : at === undefined ? undefined : revealStyle(frame, { at: at + i * 3, dur: 18, blur: 24, rise: size * 0.18 })}>
          {letter}
        </span>
      ))}
    </div>
  );
};
