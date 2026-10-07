import React from "react";
import { AbsoluteFill, Easing, interpolateColors, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { COLORS, PAPER } from "../brand/tokens";
import { body, display } from "../brand/type";
import { SIGNALS, textBlock } from "../choreography";
import { Paper } from "../components/Backdrop";
import { Block } from "../components/Block";
import { FeedScreen } from "../components/FeedScreen";
import { Icon } from "../components/Icon";
import { EASE_IN, EASE_IN_OUT, ramp } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { PHONE_W as BODY_DP } from "../components/Phone";
import { Card3D, PAPER_SHADOW } from "../components/three/Card3D";
import { rotate, type Vec3 } from "../components/three/math";
import { Phone3D } from "../components/three/Phone3D";
import { lens, Stage } from "../components/three/Stage";
import { sceneStart } from "../timeline";
import { MEASURED_BEATS, SCENE_TIMINGS } from "../voiceover-cues";
import { USERS_PHONE } from "./Users";

const T = MEASURED_BEATS.signals;
const END = SCENE_TIMINGS.signals.frames;
const K = USERS_PHONE.width / BODY_DP;
// "nakakalungkot" relative to the phone body's center, in dp.
const CAPTION: Vec3 = [-108, 304, 0];
const CLOSE = { scale: 5, at: [1350 - 960, 520 - 540] as const };
const CAMERA_EASE = Easing.bezier(0.4, 0, 0.2, 1);
const mix = (from: number, to: number, p: number) => from + (to - from) * p;

// Card layout, shared with 06 where the cards fly into the meter.
export const SIGNAL_CARDS = [
  { label: "Session length", value: "23 min", icon: "clock" as const, height: 150, wide: { top: 260, scale: 1 }, focus: { top: 230, scale: 0.78 } },
  { label: "Time per video", value: "41 s", icon: "hourglass" as const, height: 150, wide: { top: 470, scale: 1 }, focus: { top: 365, scale: 0.78 } },
  { label: "Negative captions", value: "38%", icon: "captions" as const, height: 196, wide: { top: 680, scale: 1 }, focus: { top: 520, scale: 1.22 } },
];
export const CARD_LEFT = 130;
export const CARD_W = 660;
// World position (center-of-frame origin) of a card scaled about its top-left corner.
export const cardCenter = (top: number, height: number, scale: number): Vec3 => [CARD_LEFT + (CARD_W * scale) / 2 - 960, top + (height * scale) / 2 - 540, 0];

export const SignalCardFace: React.FC<{ readonly index: number }> = ({ index }) => {
  const c = SIGNAL_CARDS[index];
  const negative = index === 2;
  const color = negative ? PAPER.coralInk : PAPER.accent;
  return (
    <div style={{ position: "absolute", inset: 0, padding: "26px 30px", display: "flex", alignItems: "flex-start", gap: 22 }}>
      <div style={{ width: 68, height: 68, borderRadius: 34, flexShrink: 0, display: "grid", placeItems: "center", background: negative ? "#FAE6E1" : "#E9EDF8" }}>
        <Icon name={c.icon} size={36} color={color} />
      </div>
      <div>
        <div style={body(32, PAPER.ink, 800)}>{c.label}</div>
        <div style={{ ...display(60, color), marginTop: 8, fontVariantNumeric: "tabular-nums" }}>{c.value}</div>
        {negative ? <div style={{ ...body(22), marginTop: 10 }}>Filipino + English lexicon</div> : null}
      </div>
    </div>
  );
};

const SignalsVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const spin = ramp(frame, 0, SIGNALS.spin, EASE_IN_OUT);
  const focus = ramp(frame, SIGNALS.closeStart, SIGNALS.closeReady - SIGNALS.closeStart, CAMERA_EASE);
  const out = ramp(frame, SIGNALS.out, END - SIGNALS.out, EASE_IN);
  const highlighted = ramp(frame, T.negative - 6, 6, EASE_IN_OUT);

  // The caption travels on a straight screen path while the phone's scale grows evenly.
  const rotation: Vec3 = [mix(USERS_PHONE.rotation[0], 0, focus), USERS_PHONE.rotation[1] + 360 * spin - USERS_PHONE.rotation[1] * focus, 0];
  const scale = Math.exp(Math.log(CLOSE.scale) * focus) * (1 - 0.35 * out);
  const wideCaption = rotate([USERS_PHONE.rotation[0], USERS_PHONE.rotation[1], 0], [CAPTION[0] * K, CAPTION[1] * K, 0]);
  const caption = [mix(USERS_PHONE.position[0] + wideCaption[0], CLOSE.at[0], focus), mix(USERS_PHONE.position[1] + wideCaption[1], CLOSE.at[1], focus)];
  const offset = rotate(rotation, [CAPTION[0] * K * scale, CAPTION[1] * K * scale, 0]);
  // Until the push starts, the phone spins about its own center.
  const position: Vec3 = focus > 0 ? [caption[0] - offset[0] + 1500 * out, caption[1] - offset[1], 0] : USERS_PHONE.position;

  return (
    <AbsoluteFill style={{ overflow: "hidden" }}>
      <Paper offset={sceneStart("signals")} />
      <Stage style={{ filter: focus < 0.5 ? "drop-shadow(0 40px 50px rgba(60,42,25,0.2))" : undefined }}>
        <Phone3D width={USERS_PHONE.width} position={position} rotation={rotation} scale={scale} screenshot={spin < 0.5 ? "screens/setup-platforms.png" : undefined}>
          {spin < 0.5 ? null : (
            <FeedScreen
              art="rain"
              user="balita.now"
              likes="65.3K"
              comments="2,733"
              shares="8,904"
              progress={0.2}
              gloom={0.15}
              caption={
                <>
                  Grabe,
                  <br />
                  <span style={{ position: "relative", display: "inline-block", color: interpolateColors(highlighted, [0, 1], ["#FFFFFF", COLORS.coralContainer]), textShadow: "none", padding: "0 2px" }}>
                    <span style={{ position: "absolute", inset: 0, right: "auto", width: `${highlighted * 100}%`, background: COLORS.coral, borderRadius: 2 }} />
                    <span style={{ position: "relative" }}>nakakalungkot</span>
                  </span>
                  <br />
                  naman.
                </>
              }
            />
          )}
        </Phone3D>
      </Stage>

      <Block block={textBlock("signals", "three-signals")} style={display(112)}>
        Three signals.
      </Block>
      {SIGNAL_CARDS.map((c, i) => {
        const land = spring({ frame: frame - SIGNALS.cards[i] + 9, fps, config: { damping: 16, stiffness: 110, mass: 0.9 } });
        const top = mix(c.wide.top, c.focus.top, focus);
        const s = mix(c.wide.scale, c.focus.scale, focus);
        const [x, y] = cardCenter(top, c.height, s);
        return (
          <Stage key={c.label} style={{ ...PAPER_SHADOW, ...lens(0, Math.min(1, land * 2) * (i < 2 ? 1 - 0.5 * focus : 1)) }}>
            <Card3D width={CARD_W} height={c.height} position={[x, y + 30 * (1 - land), -260 * (1 - land)]} rotation={[-55 * (1 - land), 16 * (1 - land), 0]} scale={s}>
              <SignalCardFace index={i} />
            </Card3D>
          </Stage>
        );
      })}
      <div style={{ position: "absolute", left: 140, top: mix(918, 790, focus), width: 660, ...body(27, PAPER.inkSoft, 500), opacity: ramp(frame, SIGNALS.cards[2] - 9, 16) * (1 - out) }}>
        No usable text → on-device vision fallback
      </div>
      <div style={{ position: "absolute", left: 140, top: 1000, ...body(24, PAPER.inkSoft, 500), opacity: ramp(frame, SIGNALS.cards[0] - 6, 10) * (1 - out) }}>Illustrative session metrics</div>
    </AbsoluteFill>
  );
};

export const Signals: React.FC = () => (
  <MotionBlur windows={[[0, SIGNALS.spin], [SIGNALS.closeStart, SIGNALS.closeReady], [SIGNALS.out, END]]}>
    <SignalsVisual />
  </MotionBlur>
);
