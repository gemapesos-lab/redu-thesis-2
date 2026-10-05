import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { PAPER } from "../brand/tokens";
import { body, card, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { Reveal, revealStyle, Words } from "../components/Kinetic";
import { Phone, PhoneHighlight, phoneHeightFor } from "../components/Phone";
import { PlatformIcon, type Platform } from "../components/PlatformIcon";
import { sceneStart } from "../timeline";

const EXIT = 120;
const PHONE_W = 420;

const PLATFORMS: ReadonlyArray<readonly [Platform, string]> = [
  ["tiktok", "TikTok"],
  ["instagram", "Instagram Reels"],
  ["facebook", "Facebook Reels"],
];

export const Users: React.FC = () => {
  const frame = useCurrentFrame();
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("users")} />
      <div style={{ position: "absolute", left: 170, top: 0, bottom: 0, width: 980, display: "flex", flexDirection: "column", justifyContent: "center" }}>
        <Reveal at={4} exitAt={EXIT} style={eyebrow()}>
          Built for
        </Reveal>
        <Words text={"Adult Filipino\nAndroid users"} times={[16, 23, 39, 52]} exitAt={EXIT} style={{ ...display(116), marginTop: 22 }} />
        <Reveal at={66} exitAt={EXIT} style={{ ...body(40), marginTop: 34 }}>
          who watch short-form video on
        </Reveal>
        <div style={{ display: "flex", gap: 16, marginTop: 22 }}>
          {PLATFORMS.map(([platform, label], i) => (
            <Reveal key={platform} at={74 + i * 6} exitAt={EXIT}>
              <div style={{ ...card, borderRadius: 999, display: "flex", alignItems: "center", gap: 14, padding: "10px 26px 10px 10px" }}>
                <PlatformIcon platform={platform} size={52} style={{ borderRadius: 14 }} />
                <span style={body(32, PAPER.ink, 700)}>{label}</span>
              </div>
            </Reveal>
          ))}
        </div>
      </div>
      <div
        style={{
          position: "absolute",
          left: 1250,
          top: (1080 - phoneHeightFor(PHONE_W)) / 2,
          ...revealStyle(frame, { at: 0, dur: 24, blur: 16, rise: 150, exitAt: EXIT }),
        }}
      >
        <Phone width={PHONE_W} screenshot="screens/setup-platforms.png" onPaper />
        <PhoneHighlight width={PHONE_W} rect={{ x: 23, y: 152, w: 314, h: 187 }} at={36} />
      </div>
    </AbsoluteFill>
  );
};
