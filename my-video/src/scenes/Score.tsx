import React from "react";
import { AbsoluteFill, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { PAPER, PATTERN, patternForScore, type PatternTone } from "../brand/tokens";
import { body, card, centered, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { Icon, type IconName } from "../components/Icon";
import { EASE_IN, exitStyle, ramp, Reveal, revealStyle, Words } from "../components/Kinetic";
import { sceneStart } from "../timeline";

const EXIT = 97;
const METER = { x: 410, y: 690, w: 1100, h: 24, gap: 14 };
const SEGMENT = (METER.w - METER.gap * 2) / 3;
const TONES: ReadonlyArray<PatternTone> = ["low", "elevated", "high"];
const SCORE = 58;

const INPUTS: ReadonlyArray<readonly [IconName, string]> = [
  ["clock", "23 min"],
  ["hourglass", "41 s"],
  ["captions", "38%"],
];

export const Score: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const score = SCORE * spring({ frame: frame - 36, fps, config: { damping: 14, stiffness: 55 } });
  const tone = patternForScore(score);
  const markerX = METER.x + (score / 100) * METER.w;
  const settled = spring({ frame: frame - 80, fps, config: { damping: 10, stiffness: 160 } });

  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("score")} />
      <AbsoluteFill style={exitStyle(frame, EXIT, 8)}>
        <div style={{ position: "absolute", top: 222, left: 0, right: 0, display: "flex", flexDirection: "column", alignItems: "center" }}>
          <Reveal at={2} style={eyebrow()}>
            Fuzzy logic
          </Reveal>
          <Words text="One gentle score." times={[51, 62, 77]} style={{ ...display(120), marginTop: 22 }} />
        </div>

        {INPUTS.map(([icon, value], i) => {
          const x0 = 960 + (i - 1) * 300;
          const fly = ramp(frame, 22 + i * 4, 14, EASE_IN);
          return (
            <div
              key={value}
              style={{
                position: "absolute",
                left: x0 - 120,
                top: 470,
                width: 240,
                display: "flex",
                justifyContent: "center",
                translate: `${(METER.x - x0) * fly}px ${(METER.y - 505) * fly}px`,
                scale: String(1 - 0.7 * fly),
                opacity: 1 - fly,
              }}
            >
              <div style={{ ...card, ...revealStyle(frame, { at: 4 + i * 4, dur: 14 }), borderRadius: 999, display: "flex", alignItems: "center", gap: 12, padding: "14px 26px" }}>
                <Icon name={icon} size={30} color={PAPER.inkSoft} />
                <span style={{ ...body(32, PAPER.ink, 800), fontVariantNumeric: "tabular-nums" }}>{value}</span>
              </div>
            </div>
          );
        })}

        <svg width={1920} height={1080} style={{ position: "absolute", inset: 0, ...revealStyle(frame, { at: 26, dur: 16, blur: 10, rise: 20 }) }}>
          {TONES.map((t, i) => (
            <rect
              key={t}
              x={METER.x + i * (SEGMENT + METER.gap)}
              y={METER.y - METER.h / 2}
              width={SEGMENT}
              height={METER.h}
              rx={METER.h / 2}
              fill={PATTERN[t].color}
              opacity={t === tone ? 1 : 0.55}
            />
          ))}
        </svg>

        {TONES.map((t, i) => {
          const active = t === "elevated" ? 1 : 0;
          const dim = settled * (1 - active);
          return (
            <div
              key={t}
              style={{
                position: "absolute",
                left: METER.x + i * (SEGMENT + METER.gap),
                width: SEGMENT,
                top: METER.y + 40,
                textAlign: "center",
                ...revealStyle(frame, { at: 32 + i * 3, dur: 14 }),
              }}
            >
              <div style={{ opacity: 1 - dim * 0.6, scale: String(1 + active * 0.1 * Math.sin(Math.min(1, settled) * Math.PI)) }}>
                <div style={display(44, PATTERN[t].ink)}>{PATTERN[t].label}</div>
                <div style={{ ...body(26, PAPER.inkFaint), marginTop: 6 }}>{PATTERN[t].range}</div>
              </div>
            </div>
          );
        })}

        <div style={{ position: "absolute", left: markerX - 28, top: METER.y - 28, ...revealStyle(frame, { at: 34, dur: 10, blur: 8, rise: 0, scaleFrom: 0.4 }) }}>
          <div
            style={{
              position: "absolute",
              left: 28 - 40,
              top: -78,
              width: 80,
              height: 54,
              borderRadius: 27,
              background: PAPER.ink,
              color: "#fff",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              ...display(32, "#fff"),
              letterSpacing: "0",
              fontVariantNumeric: "tabular-nums",
            }}
          >
            {Math.round(score)}
          </div>
          <div
            style={{
              width: 56,
              height: 56,
              borderRadius: 28,
              background: "#fff",
              boxShadow: `inset 0 0 0 9px ${PATTERN[tone].color}, 0 10px 24px rgba(60,42,25,0.25)`,
            }}
          />
        </div>

        <AbsoluteFill style={{ ...centered, justifyContent: "flex-end", paddingBottom: 130 }}>
          <Reveal at={82} style={body(34)}>
            A gentle read on your pattern, not a diagnosis.
          </Reveal>
        </AbsoluteFill>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
