import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { FONT, PAPER } from "../brand/tokens";
import { body, card, centered, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { Icon } from "../components/Icon";
import { CountUp, EASE_IN_OUT, exitStyle, ramp, Reveal, Words } from "../components/Kinetic";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.impact;
const EXIT = T.exit;
const SHRINK = T.shrink;

// Chapter 4, between-group Week 1 → Week 2 comparisons (all p < .001, Holm–Bonferroni).
const OUTCOMES: ReadonlyArray<readonly [string, string, number]> = [
  ["Session length", "d = −1.18", T.cards[0]],
  ["Time per video", "d = −1.33", T.cards[1]],
  ["Negative content", "d = −1.82", T.cards[2]],
  ["Self-reported doomscrolling", "d = −1.50", T.cards[3]],
];

const Stat: React.FC<{ readonly at: number; readonly label: string; readonly children: React.ReactNode }> = ({ at, label, children }) => (
  <Reveal at={at} dur={18} blur={22} style={{ width: 520, textAlign: "center" }}>
    <div style={{ ...display(170), fontVariantNumeric: "tabular-nums" }}>{children}</div>
    <div style={{ ...body(38), marginTop: 6 }}>{label}</div>
  </Reveal>
);

const Badge: React.FC<{ readonly at: number; readonly label: string; readonly value: string; readonly note: string }> = ({ at, label, value, note }) => (
  <Reveal at={at} dur={14} rise={16}>
    <div style={{ display: "flex", alignItems: "baseline", gap: 14, padding: "14px 28px", borderRadius: 999, background: "rgba(26,21,18,0.06)", fontFamily: FONT }}>
      <span style={body(28)}>{label}</span>
      <span style={{ ...display(36), letterSpacing: "-0.01em", fontVariantNumeric: "tabular-nums" }}>{value}</span>
      <span style={body(24, PAPER.inkFaint)}>{note}</span>
    </div>
  </Reveal>
);

export const Impact: React.FC = () => {
  const frame = useCurrentFrame();
  const shrink = ramp(frame, SHRINK, 22, EASE_IN_OUT);
  const less = { color: PAPER.accent };
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("impact")} />
      <AbsoluteFill style={exitStyle(frame, EXIT, 8)}>
        <div style={{ position: "absolute", top: 318, left: 0, right: 0, display: "flex", justifyContent: "center", opacity: 1 - shrink }}>
          <Reveal at={2} style={eyebrow()}>
            Two-week pilot study
          </Reveal>
        </div>
        <div
          style={{
            position: "absolute",
            top: 398,
            left: 0,
            right: 0,
            display: "flex",
            justifyContent: "center",
            translate: `0 ${-330 * shrink}px`,
            scale: String(1 - 0.5 * shrink),
          }}
        >
          <Stat at={T.weeks - 18} label="weeks">
            2
          </Stat>
          <Stat at={T.adults - 18} label="Filipino adults">
            50
          </Stat>
          <Stat at={T.sessions} label="sessions logged">
            <CountUp to={10134} at={T.sessions} dur={28} />
          </Stat>
        </div>

        <AbsoluteFill style={{ ...centered, justifyContent: "flex-start", paddingTop: 300 }}>
          <Words
            text="Scrolled less. Saw less negative content."
            readableOnset
            times={T.headline}
            dur={12}
            wordStyle={{ 1: less, 3: less }}
            style={display(80)}
          />
        </AbsoluteFill>

        <div style={{ position: "absolute", top: 450, left: 0, right: 0, display: "flex", justifyContent: "center", gap: 28 }}>
          {OUTCOMES.map(([title, effect, at]) => (
            <Reveal key={title} at={at - 16} dur={16} rise={30} style={{ ...card, width: 380, height: 236, padding: "30px 32px", display: "flex", flexDirection: "column" }}>
              <div style={{ width: 60, height: 60, borderRadius: 30, background: "rgba(143,181,154,0.24)", display: "flex", alignItems: "center", justifyContent: "center" }}>
                <Icon name="arrowDown" size={34} color={PAPER.sageInk} strokeWidth={2.8} />
              </div>
              <div style={{ ...display(36), lineHeight: 1.12, letterSpacing: "-0.02em", marginTop: 20, flex: 1 }}>{title}</div>
              <div style={{ ...body(24, PAPER.inkFaint), fontVariantNumeric: "tabular-nums" }}>{effect}</div>
            </Reveal>
          ))}
        </div>

        <div style={{ position: "absolute", top: 752, left: 0, right: 0, display: "flex", justifyContent: "center", gap: 22 }}>
          <Badge at={T.badges} label="Usability (SUS)" value="80.95" note="target 70" />
          <Badge at={T.badges + 4} label="Expert ratings" value="5.00 · 4.33" note="out of 5" />
        </div>

        <AbsoluteFill style={{ ...centered, justifyContent: "flex-end", paddingBottom: 54 }}>
          <Reveal at={T.footnote} dur={16} rise={10} style={{ ...body(23, PAPER.inkFaint, 500), maxWidth: 1480, lineHeight: 1.45 }}>
            Pilot field study, 50 adult Filipino Android users, two weeks, intervention vs. logging-only control. All four primary
            outcomes favored the intervention group (p &lt; .001, Holm–Bonferroni). Short-term, non-clinical findings.
          </Reveal>
        </AbsoluteFill>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
