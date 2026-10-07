import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { PAPER } from "../brand/tokens";
import { centered, display } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { exitStyle, ramp, Words } from "../components/Kinetic";
import { LogoLockup } from "../components/Logo";
import { sceneStart } from "../timeline";

const EXIT = 114;

export const Outro: React.FC = () => {
  const frame = useCurrentFrame();
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("outro")} />
      <AbsoluteFill style={{ ...centered, flexDirection: "column", ...exitStyle(frame, EXIT, 8) }}>
        <LogoLockup at={0} iconSize={210} wordSize={196} blendTo="wink" blend={ramp(frame, 100, 8) * (1 - ramp(frame, 113, 6))} />
        <div style={{ marginTop: 84 }}>
          <Words text="Notice the scroll." times={[35, 43, 51]} style={display(100)} />
          <Words text="Choose the pause." times={[78, 89, 95]} wordStyle={{ 2: { color: PAPER.accent } }} style={display(100)} />
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
