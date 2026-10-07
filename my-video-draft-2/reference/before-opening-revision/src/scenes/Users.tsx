import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { PAPER } from "../brand/tokens";
import { body, card, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { Reveal, revealStyle, Words } from "../components/Kinetic";
import { Phone, PhoneHighlight, phoneHeightFor } from "../components/Phone";
import { PlatformIcon, type Platform } from "../components/PlatformIcon";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.users;
const EXIT = T.exit;
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
        <Words readableOnset text="Built for" times={T.built} exitAt={EXIT} exitDur={8} style={eyebrow()} />
        <Words readableOnset text={"Adult Filipino\nAndroid users"} times={T.headline} exitAt={EXIT} exitDur={8} style={{ ...display(116), marginTop: 22 }} />
        <Words readableOnset text="who watch short-form video on" times={T.supporting} exitAt={EXIT} exitDur={8} style={{ ...body(40), marginTop: 34 }} />
        <div style={{ display: "flex", gap: 16, marginTop: 22 }}>
          {PLATFORMS.map(([platform, label], i) => (
            <Reveal key={platform} at={T.platforms - 12 + i * 6} exitAt={EXIT} exitDur={8}>
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
          ...revealStyle(frame, { at: 0, dur: 24, blur: 16, rise: 150, exitAt: EXIT, exitDur: 8 }),
        }}
      >
        <Phone width={PHONE_W} screenshot="screens/setup-platforms.png" onPaper />
        <PhoneHighlight width={PHONE_W} rect={{ x: 23, y: 152, w: 314, h: 187 }} at={T.platforms - 10} />
      </div>
    </AbsoluteFill>
  );
};
