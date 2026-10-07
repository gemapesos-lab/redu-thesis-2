import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { PAPER } from "../brand/tokens";
import { centered, display } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { exitStyle, ramp, Words } from "../components/Kinetic";
import { LogoLockup } from "../components/Logo";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.outro;
const EXIT = T.exit;

export const Outro: React.FC = () => {
  const frame = useCurrentFrame();
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("outro")} />
      <AbsoluteFill style={{ ...centered, flexDirection: "column", ...exitStyle(frame, EXIT, 8) }}>
        <LogoLockup at={T.logo - 15} readableAt={T.logo} iconSize={210} wordSize={196} blendTo="wink" blend={ramp(frame, T.wink - 6, 8) * (1 - ramp(frame, EXIT, 5))} />
        <div style={{ marginTop: 84 }}>
          <Words readableOnset text="Notice the scroll." times={T.notice} style={display(100)} />
          <Words readableOnset text="Choose the pause." times={T.choose} wordStyle={{ 2: { color: PAPER.accent } }} style={display(100)} />
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
