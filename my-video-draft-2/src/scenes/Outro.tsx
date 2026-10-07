import React from "react";
import { AbsoluteFill, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { PAPER } from "../brand/tokens";
import { centered, display } from "../brand/type";
import { OUTRO, textBlock } from "../choreography";
import { Paper } from "../components/Backdrop";
import { WordBlock } from "../components/Block";
import { EASE_IN, ramp } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { Lockup3D } from "../components/three/Lockup3D";
import { sceneStart } from "../timeline";
import { MEASURED_BEATS, SCENE_TIMINGS } from "../voiceover-cues";

const T = MEASURED_BEATS.outro;
const END = SCENE_TIMINGS.outro.frames;

const OutroVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  // A shorter version of the spin in 03: one turn, landing on "REDU".
  const spin = spring({ frame, fps, config: { damping: 15, stiffness: 95, mass: 1 } });
  const wink = ramp(frame, OUTRO.wink - 4, 8) * (1 - ramp(frame, OUTRO.out, 5));
  const out = ramp(frame, OUTRO.out, END - OUTRO.out, EASE_IN);
  const push = 1 + 0.03 * ramp(frame, 0, END, (t) => t);
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("outro")} />
      <AbsoluteFill style={{ scale: String(push), opacity: 1 - out, translate: `0 ${-14 * out}px` }}>
        <AbsoluteFill style={{ ...centered, justifyContent: "flex-start", paddingTop: 300 }}>
          <Lockup3D
            iconSize={210}
            wordSize={196}
            turn={360 * (1 - spin)}
            tilt={8 * (1 - spin)}
            iconScale={0.6 + 0.4 * Math.min(1, spin)}
            iconOpacity={Math.min(1, spin * 3)}
            sheen={ramp(frame, OUTRO.land - 4, 18, (t) => t)}
            reveal={ramp(frame, T.redu - 6, 14)}
            blendTo="wink"
            blend={wink}
          />
        </AbsoluteFill>
        <WordBlock block={textBlock("outro", "notice-the-scroll")} text="Notice the scroll." times={T.noticeWords} exit={false} align="center" style={display(100)} />
        <WordBlock block={textBlock("outro", "choose-the-pause")} text="Choose the pause." times={T.chooseWords} exit={false} align="center" wordStyle={{ 2: { color: PAPER.accent } }} style={display(100)} />
      </AbsoluteFill>
    </AbsoluteFill>
  );
};

export const Outro: React.FC = () => (
  <MotionBlur windows={[[0, OUTRO.land]]}>
    <OutroVisual />
  </MotionBlur>
);
