import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { PAPER } from "../brand/tokens";
import { body, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { Icon } from "../components/Icon";
import { exitStyle, Reveal, revealStyle, Words } from "../components/Kinetic";
import { Phone, PhoneHighlight, phoneHeightFor } from "../components/Phone";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.privacy;
const EXIT = T.exit;
const PHONE_W = 420;

const POINTS: ReadonlyArray<readonly [string, number]> = [
  ["Processed on-device", T.points[0]],
  ["Raw text and screen frames discarded", T.points[1]],
  ["Exports hold aggregate data only", T.points[2]],
];

export const Privacy: React.FC = () => {
  const frame = useCurrentFrame();
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("privacy")} />
      <AbsoluteFill style={exitStyle(frame, EXIT, 8)}>
        <div style={{ position: "absolute", left: 330, top: (1080 - phoneHeightFor(PHONE_W)) / 2, ...revealStyle(frame, { at: 0, dur: 22, blur: 16, rise: 140 }) }}>
          <Phone width={PHONE_W} screenshot="screens/export.png" onPaper />
          <PhoneHighlight width={PHONE_W} rect={{ x: 12, y: 116, w: 336, h: 100 }} at={T.points[2] - 10} />
        </div>
        <div style={{ position: "absolute", left: 960, top: 0, bottom: 0, width: 840, display: "flex", flexDirection: "column", justifyContent: "center" }}>
          <Reveal at={2} style={eyebrow()}>
            Private by design
          </Reveal>
          <Words readableOnset text={"Runs on\nyour phone."} times={T.heading} style={{ ...display(124), marginTop: 22 }} />
          <div style={{ display: "flex", flexDirection: "column", gap: 22, marginTop: 48 }}>
            {POINTS.map(([text, at]) => (
              <Reveal key={text} at={at - 14} dur={14} rise={18} style={{ display: "flex", alignItems: "center", gap: 20 }}>
                <div style={{ width: 52, height: 52, borderRadius: 26, background: "rgba(143,181,154,0.24)", display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
                  <Icon name="check" size={30} color={PAPER.sageInk} strokeWidth={3} />
                </div>
                <span style={body(36, PAPER.ink, 700)}>{text}</span>
              </Reveal>
            ))}
          </div>
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
