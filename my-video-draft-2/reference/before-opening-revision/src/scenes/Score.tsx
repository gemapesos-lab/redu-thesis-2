import React from "react";
import { AbsoluteFill, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { PAPER, PATTERN, patternForScore } from "../brand/tokens";
import { body, card, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { EASE_IN, exitStyle, ramp, readableStyle, Reveal, revealStyle, Words } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.score;
const LABELS = ["Session length", "Time per video", "Negative captions"];
const TONES = ["low", "elevated", "high"] as const;
const METER = { x: 340, y: 690, w: 1240 };

const ScoreVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  // Restore the first draft's spring-driven marker and staggered input flight.
  const value = 58 * spring({ frame: frame - (T.join + 8), fps, durationInFrames: T.settle - T.join - 8, config: { damping: 14, stiffness: 55, mass: 1 } });
  const tone = patternForScore(value);
  const settled = ramp(frame, T.settle - 8, 16);
  return <AbsoluteFill>
    <Paper offset={sceneStart("score")} />
    <AbsoluteFill style={exitStyle(frame, T.exit, 8)}>
      <div style={{ position: "absolute", top: 142, left: 130, right: 130, textAlign: "center" }}>
        <Words readableOnset text="Fuzzy logic" times={[T.fuzzy, T.logic]} style={eyebrow()} />
        <Words readableOnset text={"Doomscrolling\nrisk score."} times={T.title} style={{ ...display(116), marginTop: 28 }} />
      </div>

      {LABELS.map((label, i) => {
        const x0 = 960 + (i - 1) * 490;
        const fly = ramp(frame, T.join - 6 + i * 4, 24, EASE_IN);
        return <div key={label} style={{ position: "absolute", width: 420, left: x0 - 210, top: 480, opacity: 1 - fly, translate: `${(METER.x - x0) * fly}px ${190 * fly}px`, scale: String(1 - fly * 0.7) }}>
          <div style={{ ...card, padding: "24px 20px", textAlign: "center", ...body(32, i === 2 ? PAPER.coralInk : PAPER.ink, 800), ...revealStyle(frame, { at: i * 4, dur: 18, blur: 18, rise: 30 }) }}>{i + 1} · {label}</div>
        </div>;
      })}

      <Reveal at={T.meter - 10} dur={20} style={{ position: "absolute", left: METER.x, top: METER.y, width: METER.w }}>
        <div style={{ display: "flex", gap: 14 }}>
          {TONES.map((t) => <div key={t} style={{ flex: 1, height: 26, borderRadius: 13, background: PATTERN[t].color, opacity: t === "elevated" ? 1 : 1 - 0.3 * settled }} />)}
        </div>
        <div style={{ display: "flex", marginTop: 35 }}>
          {TONES.map((t) => <div key={t} style={{ flex: 1, textAlign: "center", opacity: t === "elevated" ? 1 : 1 - 0.35 * settled }}>
            <div style={display(48, PATTERN[t].ink)}>{PATTERN[t].label}</div>
            <div style={{ ...body(28), marginTop: 12 }}>{PATTERN[t].range}</div>
          </div>)}
        </div>
        <div style={{ position: "absolute", left: value / 100 * METER.w - 26, top: -13, ...readableStyle(frame, { at: T.meter, dur: 18, blur: 10, rise: 0, scaleFrom: 0.5 }) }}>
          <div style={{ width: 52, height: 52, borderRadius: 26, background: "#fff", boxShadow: `inset 0 0 0 8px ${PATTERN[tone].color}, 0 8px 22px rgba(60,42,25,0.25)` }} />
        </div>
      </Reveal>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 123, textAlign: "center", ...body(30, PAPER.inkSoft), ...readableStyle(frame, { at: T.settle, dur: 18, rise: 12, blur: 12 }) }}>0–100 scale · Illustrative risk estimate</div>
    </AbsoluteFill>
  </AbsoluteFill>;
};

export const Score: React.FC = () => <MotionBlur windows={[[T.join - 6, T.settle]]}><ScoreVisual /></MotionBlur>;
