import React from "react";
import { AbsoluteFill, Easing, useCurrentFrame } from "remotion";
import { COLORS, PAPER } from "../brand/tokens";
import { centered, display, eyebrow } from "../brand/type";
import { Night, Paper } from "../components/Backdrop";
import { EASE_IN_OUT, exitStyle, ramp, Words } from "../components/Kinetic";
import { LogoLockup } from "../components/Logo";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.meet;
const EXIT = T.exit;

export const Meet: React.FC = () => {
  const frame = useCurrentFrame();
  const offset = sceneStart("meet");

  // One breath: the sage circle inhales, then cream paper blooms out of it.
  const inhale = ramp(frame, 0, 10, EASE_IN_OUT);
  const r = 34 + 96 * inhale;
  const flood = ramp(frame, 4, 14, Easing.bezier(0.7, 0, 0.25, 1));
  const clip = frame < 4 ? 0 : r * 0.8 + flood * 1300;
  const lift = ramp(frame, T.lift, 20, EASE_IN_OUT);
  const exit = exitStyle(frame, EXIT, 10);

  return (
    <AbsoluteFill>
      <Night offset={offset} />
      <AbsoluteFill style={centered}>
        <div
          style={{
            width: r * 2,
            height: r * 2,
            borderRadius: r,
            background: COLORS.sage,
            boxShadow: `0 0 0 ${r * 0.32}px rgba(143,181,154,0.14), 0 0 ${r}px rgba(143,181,154,0.35)`,
            opacity: 0.92,
          }}
        />
      </AbsoluteFill>
      <AbsoluteFill style={{ clipPath: `circle(${clip}px at 50% 50%)` }}>
        <Paper offset={offset} />
        <AbsoluteFill style={{ ...centered, translate: `0 ${-lift * 178}px`, scale: String(1 - lift * 0.22) }}>
          <div style={exit}>
            <LogoLockup at={T.logo - 15} readableAt={T.logo} iconSize={220} wordSize={200} />
          </div>
        </AbsoluteFill>
        <div style={{ position: "absolute", top: 560, left: 0, right: 0, display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center" }}>
          <Words readableOnset text="A privacy-first Android app" times={T.eyebrow} exitAt={EXIT} exitDur={10} style={eyebrow(PAPER.accent, 30)} />
          <Words readableOnset text="Notices doomscrolling." times={T.notices} exitAt={EXIT} exitDur={10} style={{ ...display(100), marginTop: 30 }} />
          <Words readableOnset text="Helps you pause." times={T.helps} dur={12} exitAt={EXIT} exitDur={10} wordStyle={{ 2: { color: PAPER.accent } }} style={display(100)} />
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
