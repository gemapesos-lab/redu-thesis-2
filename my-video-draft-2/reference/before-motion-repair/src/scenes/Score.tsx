import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { PAPER, PATTERN } from "../brand/tokens";
import { body, card, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { exitStyle, ramp, Reveal, Words } from "../components/Kinetic";
import { BEATS, sceneStart } from "../timeline";

const LABELS = ["Session length", "Time per video", "Negative captions"];
const TONES = ["low", "elevated", "high"] as const;
const METER = { x: 340, y: 690, w: 1240 };
export const Score: React.FC = () => {
  const frame = useCurrentFrame();
  const merge = ramp(frame, BEATS.score.join, 24);
  const value = 58 * ramp(frame, BEATS.score.join + 12, BEATS.score.settle - BEATS.score.join - 12);
  return <AbsoluteFill>
    <Paper offset={sceneStart("score")} />
    <AbsoluteFill style={exitStyle(frame, BEATS.score.exit, 4)}>
      <div style={{ position: "absolute", top: 142, left: 130, right: 130, textAlign: "center" }}>
        <Words readableOnset text="Fuzzy logic" times={[BEATS.score.fuzzy, BEATS.score.logic]} style={eyebrow()} />
        <Words readableOnset text={"Doomscrolling\nrisk score."} times={BEATS.score.title} style={{ ...display(116), marginTop: 28 }} />
      </div>
      {LABELS.map((label, i) => <div key={label} style={{ ...card, position: "absolute", width: 420, left: 960 + (i - 1) * 490 - 210, top: 480, padding: "24px 20px", textAlign: "center", ...body(32, i === 2 ? PAPER.coralInk : PAPER.ink, 800), opacity: 1 - merge, translate: (1 - i) * 490 * merge + "px " + 160 * merge + "px", scale: String(1 - merge * 0.35) }}>{i + 1} · {label}</div>)}
      <Reveal at={BEATS.score.meter - 12} dur={12} style={{ position: "absolute", left: METER.x, top: METER.y, width: METER.w }}>
        <div style={{ display: "flex", gap: 14 }}>
          {TONES.map((tone) => <div key={tone} style={{ flex: 1, height: 26, borderRadius: 13, background: PATTERN[tone].color }} />)}
        </div>
        <div style={{ display: "flex", marginTop: 35 }}>
          {TONES.map((tone) => <div key={tone} style={{ flex: 1, textAlign: "center" }}><div style={display(48, PATTERN[tone].ink)}>{PATTERN[tone].label}</div><div style={{ ...body(28), marginTop: 12 }}>{PATTERN[tone].range}</div></div>)}
        </div>
        <div style={{ position: "absolute", left: value / 100 * METER.w - 23, top: -10, width: 46, height: 46, borderRadius: 23, background: "#fff", boxShadow: "0 0 0 7px #9A6E22, 0 6px 20px rgba(0,0,0,0.2)" }} />
      </Reveal>
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 123, textAlign: "center", ...body(30, PAPER.inkSoft), opacity: frame >= BEATS.score.settle ? 1 : 0 }}>0–100 scale · Illustrative risk estimate</div>
    </AbsoluteFill>
  </AbsoluteFill>;
};
