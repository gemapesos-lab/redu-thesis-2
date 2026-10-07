import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { PAPER } from "../brand/tokens";
import { body, card, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { Icon } from "../components/Icon";
import { exitStyle, Reveal } from "../components/Kinetic";
import { BEATS, sceneStart } from "../timeline";

export const Impact: React.FC = () => {
  const frame = useCurrentFrame();
  return <AbsoluteFill>
    <Paper offset={sceneStart("impact")} />
    <AbsoluteFill style={exitStyle(frame, 204, 6)}>
      <div style={{ position: "absolute", top: 150, left: 145 }}>
        <div style={eyebrow()}>Pilot results</div>
        <div style={{ display: "flex", alignItems: "baseline", gap: 42, marginTop: 30 }}>
          <Reveal at={BEATS.impact.weeks - 8} dur={8} blur={0} style={display(76)}>2-week pilot</Reveal>
          <Reveal at={BEATS.impact.adults - 8} dur={8} blur={0} style={display(76)}>50 Filipino adults</Reveal>
        </div>
      </div>
      {[{ title: "Less scrolling", at: BEATS.impact.scrolling, color: PAPER.accent }, { title: "Less negative-content\nexposure", at: BEATS.impact.negative, color: PAPER.coralInk }].map(({ title, at, color }, i) => <Reveal key={title} at={at - 8} dur={8} blur={0} style={{ ...card, position: "absolute", left: 145 + i * 838, top: 400, width: 792, height: 365, padding: "45px 48px" }}>
        <Icon name="arrowDown" size={68} color={color} strokeWidth={2.7} />
        <div style={{ ...display(62, color), marginTop: 28, lineHeight: 1.12, whiteSpace: "pre-line" }}>{title}</div>
      </Reveal>)}
      <Reveal at={BEATS.impact.scrolling - 8} dur={8} blur={0} style={{ position: "absolute", left: 145, right: 145, top: 835, ...body(34) }}>
        Prompted users compared with a logging-only control group.
        <div style={{ ...body(28, PAPER.inkSoft, 500), marginTop: 20 }}>Short-term findings from a non-clinical pilot.</div>
      </Reveal>
    </AbsoluteFill>
  </AbsoluteFill>;
};
