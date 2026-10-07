import React from "react";
import { AbsoluteFill, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { FONT, PAPER } from "../brand/tokens";
import { body, display, eyebrow } from "../brand/type";
import { IMPACT, textBlock } from "../choreography";
import { Paper } from "../components/Backdrop";
import { Block } from "../components/Block";
import { Icon } from "../components/Icon";
import { CountUp, EASE_IN, EASE_IN_OUT, Phrase, ramp } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { Card3D, PAPER_SHADOW } from "../components/three/Card3D";
import { lens, Stage } from "../components/three/Stage";
import { sceneStart } from "../timeline";
import { SCENE_TIMINGS } from "../voiceover-cues";

const END = SCENE_TIMINGS.impact.frames;
const TILE = { width: 440, height: 300, y: 530 - 540, xs: [-520, 0, 520] };
const CARD = { width: 380, height: 236, y: 438 - 540, gap: 28 };

// Chapter 4, between-group Week 1 → Week 2 comparisons (all p < .001, Holm–Bonferroni).
const OUTCOMES: ReadonlyArray<readonly [string, string]> = [
  ["Session length", "d = −1.18"],
  ["Time per video", "d = −1.33"],
  ["Negative content", "d = −1.82"],
  ["Self-reported doomscrolling", "d = −1.50"],
];

const STATS: ReadonlyArray<{ readonly label: string; readonly value: React.ReactNode }> = [
  { label: "weeks", value: "2" },
  { label: "Filipino adults", value: "50" },
  { label: "sessions logged", value: <CountUp to={10134} at={IMPACT.tiles[2]} dur={26} /> },
];

const Badge: React.FC<{ readonly at: number; readonly label: string; readonly value: string; readonly note: string }> = ({ at, label, value, note }) => (
  <Phrase at={at} dur={12} exitAt={IMPACT.out} exitDur={8}>
    <div style={{ display: "flex", alignItems: "baseline", gap: 14, padding: "14px 28px", borderRadius: 999, background: "rgba(26,21,18,0.06)", fontFamily: FONT }}>
      <span style={body(28)}>{label}</span>
      <span style={{ ...display(36), letterSpacing: "-0.01em", fontVariantNumeric: "tabular-nums" }}>{value}</span>
      <span style={body(24, PAPER.inkFaint)}>{note}</span>
    </div>
  </Phrase>
);

const ImpactVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const tilesOut = ramp(frame, IMPACT.tilesOut, 10, EASE_IN);
  const camera = { dolly: 40 * ramp(frame, IMPACT.cards[0], END - IMPACT.cards[0], EASE_IN_OUT) };
  const less = { color: PAPER.accent };
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("impact")} />
      <Block block={textBlock("impact", "pilot-study")} align="center" style={eyebrow()}>
        Two-week pilot study
      </Block>
      {STATS.map((stat, i) => {
        // Hinged at the bottom edge: lying back, face up, it stands and later falls back.
        const up = spring({ frame: frame - IMPACT.tiles[i] + 8, fps, config: { damping: 15, stiffness: 120, mass: 0.9 } });
        const angle = 90 * (1 - up) + 90 * tilesOut;
        const a = (angle * Math.PI) / 180;
        const h = TILE.height / 2;
        return up > 0.001 && tilesOut < 1 ? (
          <Stage key={stat.label} camera={camera} style={{ ...PAPER_SHADOW, ...lens(0, Math.min(1, up * 3) * (1 - tilesOut)) }}>
            <Card3D width={TILE.width} height={TILE.height} depth={28} radius={34} position={[TILE.xs[i], TILE.y + h - h * Math.cos(a), -h * Math.sin(a)]} rotation={[angle, 0, 0]}>
              <div style={{ position: "absolute", inset: 0, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center" }}>
                <div style={{ ...display(150), fontVariantNumeric: "tabular-nums" }}>{stat.value}</div>
                <div style={{ ...body(36), marginTop: 6 }}>{stat.label}</div>
              </div>
            </Card3D>
          </Stage>
        ) : null;
      })}

      <Block block={textBlock("impact", "results-headline")} align="center" style={display(76)}>
        Prompted users scrolled <span style={less}>less</span>
        <br />
        and saw <span style={less}>less</span> negative content.
      </Block>
      {OUTCOMES.map(([title, effect], i) => {
        const land = spring({ frame: frame - IMPACT.cards[i] + 7, fps, config: { damping: 16, stiffness: 120, mass: 0.9 } });
        const leave = ramp(frame, IMPACT.out, 8, EASE_IN);
        const x = (i - 1.5) * (CARD.width + CARD.gap);
        const nudge = Math.sin(Math.PI * ramp(frame, IMPACT.cards[i] - 1, 10, (t) => t)) * 8;
        return land > 0.001 ? (
          <Stage key={title} camera={camera} style={{ ...PAPER_SHADOW, ...lens(0, Math.min(1, land * 2) * (1 - leave)) }}>
            <Card3D
              width={CARD.width}
              height={CARD.height}
              depth={16}
              position={[x, CARD.y + 60 * (1 - land) - 20 * leave, Math.abs(i - 1.5) * 30 - 160 * (1 - land)]}
              rotation={[-34 * (1 - land) + 20 * leave, (1.5 - i) * 6, 0]}
            >
              <div style={{ position: "absolute", inset: 0, padding: "30px 32px", display: "flex", flexDirection: "column" }}>
                <div style={{ width: 60, height: 60, borderRadius: 30, background: "rgba(143,181,154,0.24)", display: "flex", alignItems: "center", justifyContent: "center", translate: `0 ${nudge}px` }}>
                  <Icon name="arrowDown" size={34} color={PAPER.sageInk} strokeWidth={2.8} />
                </div>
                <div style={{ ...display(36), lineHeight: 1.12, letterSpacing: "-0.02em", marginTop: 20, flex: 1 }}>{title}</div>
                <div style={{ ...body(24, PAPER.inkFaint), fontVariantNumeric: "tabular-nums" }}>{effect}</div>
              </div>
            </Card3D>
          </Stage>
        ) : null;
      })}
      <div style={{ position: "absolute", left: 0, right: 0, top: textBlock("impact", "badges").box[1], display: "flex", justifyContent: "center", gap: 22 }}>
        <Badge at={IMPACT.badges} label="Usability (SUS)" value="80.95" note="target 70" />
        <Badge at={IMPACT.badges + 3} label="Expert ratings" value="5.00 · 4.33" note="out of 5" />
      </div>
      <Block block={textBlock("impact", "footnote")} align="center" style={{ ...body(23, PAPER.inkFaint, 500), lineHeight: 1.45 }}>
        Pilot field study, 50 adult Filipino Android users, two weeks, intervention vs. logging-only control. All four primary outcomes favored the intervention
        group (p &lt; .001, Holm–Bonferroni). Short-term, non-clinical findings.
      </Block>
    </AbsoluteFill>
  );
};

export const Impact: React.FC = () => (
  <MotionBlur windows={[[IMPACT.tilesOut, IMPACT.tilesOut + 10]]}>
    <ImpactVisual />
  </MotionBlur>
);
