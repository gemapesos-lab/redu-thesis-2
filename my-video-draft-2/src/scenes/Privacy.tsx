import React from "react";
import { AbsoluteFill, random, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { COLORS, FONT, PAPER } from "../brand/tokens";
import { body, display, eyebrow } from "../brand/type";
import { PRIVACY, textBlock } from "../choreography";
import { Paper } from "../components/Backdrop";
import { Block } from "../components/Block";
import { Icon } from "../components/Icon";
import { EASE_IN, ramp } from "../components/Kinetic";
import { ScreenHighlight } from "../components/Phone";
import { project } from "../components/three/math";
import { Phone3D } from "../components/three/Phone3D";
import { lens, Stage } from "../components/three/Stage";
import { sceneStart } from "../timeline";
import { SCENE_TIMINGS } from "../voiceover-cues";

const END = SCENE_TIMINGS.privacy.frames;
const PHONE = { width: 420, at: [-430, 0, 0] as const };
const POINTS = ["Processed on-device", "Raw text and screen frames discarded", "Exports hold aggregate data only"];
const CHIP_TEXT = "Grabe, nakakalungkot naman.";
const DUST = Array.from({ length: 42 }, (_, i) => ({
  x: random(`dust-x-${i}`) - 0.5,
  y: random(`dust-y-${i}`) - 0.5,
  drift: random(`dust-d-${i}`),
  size: 3 + random(`dust-s-${i}`) * 6,
}));

export const Privacy: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const rise = spring({ frame, fps, config: { damping: 17, stiffness: 85, mass: 1 } });
  const out = ramp(frame, PRIVACY.out, END - PRIVACY.out, EASE_IN);
  const turn = -32 + 24 * ramp(frame, 0, END, (t) => t);
  const position = [PHONE.at[0], PHONE.at[1] + 520 * (1 - rise) + 160 * out, PHONE.at[2]] as const;

  // The raw caption leaves the screen and turns to dust before it clears the phone's light.
  const lift = ramp(frame, PRIVACY.chip - 2, 20);
  const dissolve = ramp(frame, PRIVACY.chip + 8, 22, (t) => t);
  const anchor = project([position[0], position[1] + 60, 120], {});
  const chipX = anchor.x - 330 * lift;
  const chipY = anchor.y - 120 * lift;

  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("privacy")} />
      <Stage style={{ filter: "drop-shadow(0 40px 50px rgba(60,42,25,0.2))", ...lens(0, Math.min(1, rise * 2) * (1 - out)) }}>
        <Phone3D width={PHONE.width} position={position} rotation={[4, turn, 0]} screenshot="screens/export.png">
          <ScreenHighlight rect={{ x: 12, y: 116, w: 336, h: 100 }} at={PRIVACY.points[2] - 8} />
        </Phone3D>
      </Stage>

      {lift > 0 && dissolve < 1 ? (
        <div style={{ position: "absolute", left: chipX, top: chipY, translate: "-50% -50%" }}>
          <div
            style={{
              padding: "14px 22px",
              borderRadius: 18,
              background: COLORS.surfaceHigh,
              color: COLORS.textPrimary,
              fontFamily: FONT,
              fontSize: 22,
              fontWeight: 600,
              whiteSpace: "nowrap",
              boxShadow: "0 18px 40px rgba(26,21,18,0.25)",
              opacity: lift * (1 - dissolve) ** 1.5,
              filter: `blur(${dissolve * 6}px)`,
              scale: String(1 - 0.12 * dissolve),
            }}
          >
            {CHIP_TEXT}
          </div>
          {DUST.map((d, i) => {
            const t = Math.max(0, dissolve - d.drift * 0.25);
            return (
              <div
                key={i}
                style={{
                  position: "absolute",
                  left: d.x * 380,
                  top: d.y * 60,
                  width: d.size,
                  height: d.size,
                  borderRadius: d.size,
                  background: i % 3 ? COLORS.surfaceHighest : PAPER.inkFaint,
                  opacity: (t > 0 ? 1 - t : 0) * 0.9,
                  translate: `${t * (40 + d.drift * 140)}px ${-t * (60 + d.drift * 120)}px`,
                }}
              />
            );
          })}
        </div>
      ) : null}

      <Block block={textBlock("privacy", "runs-on-your-phone")}>
        <div style={eyebrow()}>Private by design</div>
        <div style={{ ...display(124), marginTop: 22 }}>
          Runs on
          <br />
          your phone.
        </div>
      </Block>
      <div style={{ position: "absolute", left: textBlock("privacy", "checklist").box[0], top: textBlock("privacy", "checklist").box[1], display: "flex", flexDirection: "column", gap: 22 }}>
        {POINTS.map((text, i) => {
          const p = ramp(frame, PRIVACY.points[i] - 7, 14);
          const q = ramp(frame, PRIVACY.out, 8, EASE_IN);
          return (
            <div key={text} style={{ display: "flex", alignItems: "center", gap: 20, opacity: p * (1 - q), translate: `${(1 - p) * -24}px ${-q * 14}px`, filter: p < 1 ? `blur(${(1 - p) * 8}px)` : undefined }}>
              <div style={{ width: 52, height: 52, borderRadius: 26, background: "rgba(143,181,154,0.24)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0, scale: String(0.6 + 0.4 * p) }}>
                <Icon name="check" size={30} color={PAPER.sageInk} strokeWidth={3} />
              </div>
              <span style={body(36, PAPER.ink, 700)}>{text}</span>
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};
