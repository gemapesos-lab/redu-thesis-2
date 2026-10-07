import React from "react";
import { AbsoluteFill, Easing, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { COLORS, PAPER } from "../brand/tokens";
import { centered, display, eyebrow } from "../brand/type";
import { MEET, textBlock } from "../choreography";
import { Night, Paper } from "../components/Backdrop";
import { Block, WordBlock } from "../components/Block";
import { EASE_IN, EASE_IN_OUT, ramp } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { Lockup3D } from "../components/three/Lockup3D";
import { MEASURED_BEATS, SCENE_TIMINGS } from "../voiceover-cues";
import { sceneStart } from "../timeline";

const T = MEASURED_BEATS.meet;
const END = SCENE_TIMINGS.meet.frames;

const MeetVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const offset = sceneStart("meet");

  // One breath: the sage circle inhales, then cream paper blooms out of it.
  const inhale = ramp(frame, 0, 10, EASE_IN_OUT);
  const r = 34 + 96 * inhale;
  const flood = ramp(frame, 4, 14, Easing.bezier(0.7, 0, 0.25, 1));
  const clip = frame < 4 ? 0 : r * 0.8 + flood * 1300;

  // About one and a half turns, landing on a spring with a small overshoot.
  const spin = spring({ frame: frame - MEET.spin, fps, config: { damping: 15, stiffness: 72, mass: 1 } });
  const lift = ramp(frame, MEET.lift, MEET.liftDur, EASE_IN_OUT);
  const look = ramp(frame, MEET.look - 2, 8) * (1 - ramp(frame, MEET.lookBack, 8));
  const blink = Math.sin(Math.PI * ramp(frame, MEET.blink, 12, (t) => t));
  const out = ramp(frame, MEET.out, END - MEET.out, EASE_IN);

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
        <AbsoluteFill style={{ translate: `${-480 * out}px 0`, opacity: 1 - out }}>
          <AbsoluteFill style={{ ...centered, translate: `0 ${-lift * 178}px`, scale: String(1 - lift * 0.22) }}>
            <Lockup3D
              iconSize={220}
              wordSize={200}
              turn={540 * (1 - spin)}
              tilt={10 * (1 - spin)}
              iconScale={0.55 + 0.45 * Math.min(1, spin)}
              iconOpacity={Math.min(1, spin * 3)}
              sheen={ramp(frame, MEET.land - 4, 18, (t) => t)}
              reveal={ramp(frame, T.redu - 4, 14)}
              blendTo={frame < MEET.blink ? "look" : "sleepy"}
              blend={frame < MEET.blink ? look : blink}
            />
          </AbsoluteFill>
          <Block block={textBlock("meet", "privacy-first")} exit={false} align="center" style={eyebrow(PAPER.accent, 30)}>
            A privacy-first Android app
          </Block>
          <WordBlock block={textBlock("meet", "notices")} text="Notices doomscrolling." times={T.noticesWords} exit={false} align="center" style={display(100)} />
          <WordBlock block={textBlock("meet", "helps")} text="Helps you pause." times={T.helpsWords} exit={false} align="center" wordStyle={{ 2: { color: PAPER.accent } }} style={display(100)} />
        </AbsoluteFill>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};

export const Meet: React.FC = () => (
  <MotionBlur windows={[[MEET.spin, MEET.land], [MEET.out, END]]}>
    <MeetVisual />
  </MotionBlur>
);
