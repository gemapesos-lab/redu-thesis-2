import React from "react";
import { AbsoluteFill, Img, interpolateColors, spring, staticFile, useCurrentFrame, useVideoConfig } from "remotion";
import { PAPER } from "../brand/tokens";
import { display } from "../brand/type";
import { PROMPTS, SCORE, textBlock } from "../choreography";
import { Paper } from "../components/Backdrop";
import { Block, WordBlock } from "../components/Block";
import { FeedScreen } from "../components/FeedScreen";
import { EASE_IN, EASE_IN_OUT, ramp, revealStyle, track } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { PHONE_H, PHONE_W as BODY_DP, SCREEN_H, SCREEN_W, ScreenHighlight, StatusBar } from "../components/Phone";
import { AwarenessBanner, BreathingScreen } from "../components/Prompts";
import { project, type Camera } from "../components/three/math";
import { Meter3D } from "../components/three/Meter3D";
import { Phone3D } from "../components/three/Phone3D";
import { lens, Stage } from "../components/three/Stage";
import { sceneStart } from "../timeline";
import { MEASURED_BEATS, SCENE_TIMINGS } from "../voiceover-cues";
import { SCORE_METER } from "./Score";

const T = MEASURED_BEATS.prompts;
const END = SCENE_TIMINGS.prompts.frames;
const TRACK = { width: 3200, y: 370, tilt: 74 };
const PHONE_W = 300;
const PHONE_HALF = (PHONE_H * PHONE_W) / BODY_DP / 2;
const STEPS = ["Reminder", "Pause", "Breathe"];
const fill: React.CSSProperties = { position: "absolute", left: 0, top: 0, width: SCREEN_W, height: SCREEN_H };
const mix = (from: number, to: number, p: number) => from + (to - from) * p;

const PromptsVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const crane = ramp(frame, PROMPTS.crane[0], PROMPTS.crane[1] - PROMPTS.crane[0], EASE_IN_OUT);
  const pull = ramp(frame, PROMPTS.pullBack[0], PROMPTS.pullBack[1] - PROMPTS.pullBack[0], EASE_IN_OUT);
  const out = ramp(frame, PROMPTS.out, END - PROMPTS.out, EASE_IN);

  const width = mix(SCORE_METER.width, TRACK.width, crane);
  const value = track(frame, [[0, SCORE.value], [PROMPTS.crane[1], SCORE.value + 2], ...PROMPTS.levels.map(({ at, value: v }) => [at, v] as const)]);
  const puckX = -width / 2 + (value / 100) * width;
  const markX = (v: number) => -TRACK.width / 2 + (v / 100) * TRACK.width;
  const middle = (markX(PROMPTS.levels[0].value) + markX(PROMPTS.levels[2].value)) / 2;

  const camera: Camera = {
    x: mix(puckX * crane, middle, pull),
    y: mix(0, 40, crane),
    tilt: mix(0, 10, crane) - 3 * pull,
    dolly: mix(mix(60, 90, crane), -70, pull) - 160 * out,
  };

  const breathMs = (Math.max(0, frame - T.breathing) / 30) * 1000;
  const pause = ramp(frame, PROMPTS.levels[1].at - 6, 12);
  const breathe = ramp(frame, PROMPTS.levels[2].at - 6, 12);
  const screens = [
    <>
      <FeedScreen art="city" user="late.night.edits" caption="one more episode recap... part 7" likes="390K" comments="7,015" shares="22.4K" progress={0.55} />
      <AwarenessBanner remaining={1 - ramp(frame, PROMPTS.levels[0].at + 2, 120, (t) => t)} style={revealStyle(frame, { at: PROMPTS.levels[0].at - 8, dur: 14, blur: 6, rise: -34 })} />
    </>,
    <>
      <FeedScreen art="neon" user="dance.daily" caption="trend na 'to, sino gagawa?" likes="1.1M" comments="9,420" shares="52.7K" progress={0.4} />
      <div style={{ ...fill, opacity: 1 - pause }}>
        <StatusBar />
      </div>
      <Img src={staticFile("screens/pause-locked.png")} style={{ ...fill, opacity: pause }} />
      <Img src={staticFile("screens/pause-unlocked.png")} style={{ ...fill, opacity: ramp(frame, PROMPTS.unlock - 6, 6) }} />
      <ScreenHighlight rect={{ x: 23, y: 434, w: 314, h: 48 }} radius={24} at={PROMPTS.unlock + 2} />
    </>,
    <>
      <FeedScreen art="storm" user="balita.now" caption="grabe ang ulan sa Maynila ngayon" likes="65.3K" comments="2,733" shares="8,904" progress={0.7} />
      <div style={{ ...fill, opacity: 1 - breathe }}>
        <StatusBar />
      </div>
      <div style={{ ...fill, opacity: breathe }}>
        <BreathingScreen timeMs={breathMs} secondsLeft={45 - Math.floor(breathMs / 1000)} />
      </div>
    </>,
  ];

  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("prompts")} />
      <Stage camera={camera} style={{ filter: "drop-shadow(0 18px 24px rgba(60,42,25,0.12))", ...lens(0, 1 - out) }}>
        <Meter3D
          width={width}
          position={[0, mix(SCORE_METER.position[1], TRACK.y, crane) + 80 * out, 0]}
          rotation={[mix(SCORE_METER.tilt, TRACK.tilt, crane) + 14 * out, 0, 0]}
          value={value}
          emphasis={1 - crane}
          labels={1 - ramp(frame, PROMPTS.crane[0], 10)}
        />
      </Stage>
      {PROMPTS.levels.map((level, k) => {
        const rise = spring({ frame: frame - level.at + 8, fps, config: { damping: 15, stiffness: 95, mass: 1 } });
        const later = PROMPTS.levels[k + 1];
        const soften = later ? ramp(frame, later.at - 6, 12) * (1 - pull) : 0;
        const x = markX(level.value);
        const sink = 140 * out;
        const label = project([x, TRACK.y + 58, 0], camera);
        // The label arrives once its phone has risen clear of it.
        const active = ramp(frame, level.at + 1, 10);
        return (
          <React.Fragment key={STEPS[k]}>
            <Stage camera={camera} style={{ filter: "drop-shadow(0 30px 36px rgba(60,42,25,0.18))", ...lens(2.2 * soften, Math.min(1, rise * 2) * (1 - out)) }}>
              <Phone3D width={PHONE_W} position={[x, TRACK.y - PHONE_HALF + 160 * (1 - rise) + sink, 0]} rotation={[24 * (1 - rise), -8 * (1 - pull) * (k - 1), 0]}>
                {screens[k]}
              </Phone3D>
            </Stage>
            <div
              style={{
                position: "absolute",
                left: label.x - 200,
                top: label.y,
                width: 400,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                gap: 14,
                opacity: active * (1 - 0.5 * soften) * (1 - out),
                translate: `0 ${(1 - active) * 16}px`,
              }}
            >
              <div style={{ width: 46, height: 46, borderRadius: 23, background: interpolateColors(active - soften, [0, 1], [PAPER.ink, PAPER.accent]), display: "grid", placeItems: "center", ...display(26, "#fff"), letterSpacing: "0" }}>
                {k + 1}
              </div>
              <span style={display(46)}>{STEPS[k]}</span>
            </div>
          </React.Fragment>
        );
      })}
      <Block block={textBlock("prompts", "steps-in-gently")} align="center" style={display(84)}>
        REDU steps in gently.
      </Block>
      <WordBlock block={textBlock("prompts", "you-stay-in-control")} text="You stay in control." times={T.youWords} align="center" style={display(84)} />
    </AbsoluteFill>
  );
};

export const PromptLevels: React.FC = () => (
  <MotionBlur windows={[[PROMPTS.out, END]]}>
    <PromptsVisual />
  </MotionBlur>
);
