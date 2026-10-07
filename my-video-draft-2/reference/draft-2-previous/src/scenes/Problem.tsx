import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { FONT } from "../brand/tokens";
import { centered, display } from "../brand/type";
import { Night } from "../components/Backdrop";
import { Icon } from "../components/Icon";
import { revealStyle, Words } from "../components/Kinetic";
import { BEATS, sceneStart } from "../timeline";

const MUTED = "#8B8F99";
const PERIWINKLE_ON_DARK = "#93A9E2";

export const Problem: React.FC = () => {
  const frame = useCurrentFrame();
  const accent = { color: PERIWINKLE_ON_DARK };
  return (
    <AbsoluteFill>
      <Night offset={sceneStart("problem")} />
      <AbsoluteFill style={{ ...centered, flexDirection: "column" }}>
        <Words text="That's" times={BEATS.problem.thats} exitAt={41} style={display(96, MUTED)} />
        <Words
          text="doomscrolling."
          times={BEATS.problem.doomscrolling}
          dur={22}
          blur={34}
          exitAt={41}
          style={{ ...display(196, "#FFFFFF"), letterSpacing: "-0.045em", marginTop: 6 }}
        />
      </AbsoluteFill>
      <AbsoluteFill style={{ ...centered, flexDirection: "column" }}>
        <div
          style={{
            display: "flex",
            alignItems: "center",
            gap: 16,
            padding: "16px 30px 16px 24px",
            borderRadius: 999,
            background: "rgba(255,255,255,0.07)",
            boxShadow: "inset 0 0 0 1.5px rgba(255,255,255,0.1)",
            fontFamily: FONT,
            fontSize: 34,
            marginBottom: 54,
            ...revealStyle(frame, { at: 44, dur: 8, exitAt: 145, exitDur: 5 }),
          }}
        >
          <Icon name="hourglass" size={36} color="#C9C2BA" />
          <span style={{ color: "#C9C2BA", fontWeight: 600 }}>App limit</span>
          <span style={{ color: "#FFFFFF", fontWeight: 800 }}>2h a day</span>
        </div>
        <Words text="Screen-time limits count minutes." times={BEATS.problem.limits} exitAt={145} exitDur={5} style={display(84, "#C9C2BA")} />
        <Words
          text="Not what you watch."
          times={BEATS.problem.contrast}
          dur={12}
          exitAt={145} exitDur={5}
          wordStyle={{ 1: accent, 2: accent, 3: accent }}
          style={{ ...display(128, "#FFFFFF"), marginTop: 18 }}
        />
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
