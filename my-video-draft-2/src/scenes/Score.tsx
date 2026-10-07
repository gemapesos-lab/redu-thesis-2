import React from "react";
import { AbsoluteFill, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { PAPER } from "../brand/tokens";
import { body, display, eyebrow } from "../brand/type";
import { SCORE, textBlock } from "../choreography";
import { Paper } from "../components/Backdrop";
import { Block } from "../components/Block";
import { EASE_IN, EASE_IN_OUT, ramp } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { Card3D, PAPER_SHADOW } from "../components/three/Card3D";
import { Meter3D } from "../components/three/Meter3D";
import { lens, Stage } from "../components/three/Stage";
import { sceneStart } from "../timeline";
import { MEASURED_BEATS, SCENE_TIMINGS } from "../voiceover-cues";
import { CARD_W, cardCenter, SIGNAL_CARDS, SignalCardFace } from "./Signals";

const T = MEASURED_BEATS.score;
const END = SCENE_TIMINGS.score.frames;
// Shared with the start of 07, where the meter tips flat into a track.
export const SCORE_METER = { width: 1240, position: [0, 90, 0] as const, tilt: 20 };
const CHIP = { y: -70, scale: 0.42, xs: [-420, 0, 420] };
const mix = (from: number, to: number, p: number) => from + (to - from) * p;

const ScoreVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const swing = ramp(frame, SCORE.meterAt, SCORE.meterDur, EASE_IN_OUT);
  const value = SCORE.value * spring({ frame: frame - T.doom, fps, durationInFrames: 30, config: { damping: 14, stiffness: 60, mass: 1 } });
  const emphasis = ramp(frame, T.doom + 20, 16);
  const camera = { dolly: 60 * ramp(frame, 0, END, (t) => t) };
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("score")} />
      <Block block={textBlock("score", "fuzzy-logic")} align="center" style={eyebrow()}>
        Fuzzy logic
      </Block>
      <Block block={textBlock("score", "risk-score")} align="center" style={display(100)}>
        Doomscrolling risk score.
      </Block>

      {SIGNAL_CARDS.map((c, i) => {
        // From where 05 left each card, arcing through depth to a chip, then down into the meter.
        const fly = ramp(frame, i * 3, SCORE.fly - 6, EASE_IN_OUT);
        const dive = ramp(frame, SCORE.meterAt - 3 + i * 2, 14, EASE_IN);
        const [x0, y0] = cardCenter(c.focus.top, c.height, c.focus.scale);
        const scale = mix(mix(c.focus.scale, CHIP.scale, fly), 0.12, dive);
        const x = mix(mix(x0, CHIP.xs[i], fly), (CHIP.xs[i] * 0.4), dive);
        const y = mix(mix(y0, CHIP.y, fly), SCORE_METER.position[1], dive);
        const z = 220 * Math.sin(Math.PI * fly) - 120 * dive;
        const opacity = mix(i < 2 ? 0.5 : 1, 1, fly) * (1 - dive);
        return opacity > 0.01 ? (
          <Stage key={c.label} camera={camera} style={{ ...PAPER_SHADOW, ...lens(0, opacity) }}>
            <Card3D width={CARD_W} height={c.height} position={[x, y, z]} rotation={[-18 * Math.sin(Math.PI * fly) + 40 * dive, (i - 1) * 24 * Math.sin(Math.PI * fly), 0]} scale={scale}>
              <SignalCardFace index={i} />
            </Card3D>
          </Stage>
        ) : null;
      })}

      <Stage camera={camera} style={{ filter: "drop-shadow(0 18px 24px rgba(60,42,25,0.12))", ...lens(0, Math.min(1, swing * 2.5)) }}>
        <Meter3D
          width={SCORE_METER.width}
          position={[0, SCORE_METER.position[1] + 120 * (1 - swing), -200 * (1 - swing)]}
          rotation={[SCORE_METER.tilt + 55 * (1 - swing), 0, 0]}
          value={value}
          emphasis={emphasis}
          labels={ramp(frame, SCORE.meterAt + 10, 14)}
          puck={ramp(frame, T.doom - 6, 10)}
        />
      </Stage>
      <Block block={textBlock("score", "illustrative")} align="center" style={body(30, PAPER.inkSoft)}>
        0–100 scale · Illustrative risk estimate
      </Block>
    </AbsoluteFill>
  );
};

export const Score: React.FC = () => (
  <MotionBlur windows={[[0, SCORE.fly], [SCORE.meterAt - 3, SCORE.meterAt + 16]]}>
    <ScoreVisual />
  </MotionBlur>
);
