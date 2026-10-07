import React from "react";
import { AbsoluteFill } from "remotion";
import { PAPER } from "../brand/tokens";
import { display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { Words } from "../components/Kinetic";
import { Phone, PhoneHighlight } from "../components/Phone";
import { BEATS, sceneStart } from "../timeline";

export const Privacy: React.FC = () => <AbsoluteFill>
  <Paper offset={sceneStart("privacy")} />
  <div style={{ position: "absolute", left: 230, top: 116 }}>
    <Phone width={400} screenshot="screens/export.png" onPaper />
    <PhoneHighlight width={400} rect={{ x: 12, y: 116, w: 336, h: 100 }} at={BEATS.privacy.raw[0]} />
  </div>
  <div style={{ position: "absolute", left: 810, top: 230, width: 1000 }}>
    <div style={eyebrow()}>Private by design</div>
    <Words text={"Processed\non-device."} times={BEATS.privacy.processed} style={{ ...display(108), marginTop: 35 }} />
    <Words text={"Raw content\nisn’t saved."} times={BEATS.privacy.raw} style={{ ...display(88, PAPER.accent), marginTop: 66 }} />
  </div>
</AbsoluteFill>;
