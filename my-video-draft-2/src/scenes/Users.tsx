import React from "react";
import { AbsoluteFill, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { PAPER } from "../brand/tokens";
import { body, card, display, eyebrow } from "../brand/type";
import { textBlock, USERS } from "../choreography";
import { Paper } from "../components/Backdrop";
import { Block } from "../components/Block";
import { ramp } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { ScreenHighlight } from "../components/Phone";
import { PlatformIcon, type Platform } from "../components/PlatformIcon";
import { Phone3D } from "../components/three/Phone3D";
import { Stage } from "../components/three/Stage";
import { sceneStart } from "../timeline";
import { MEASURED_BEATS } from "../voiceover-cues";

const T = MEASURED_BEATS.users;
// Shared with the start of 05, where the same phone spins around to the feed.
// It ends turned slightly toward the text, as the chips land.
export const USERS_PHONE = { width: 420, position: [500, 0, 0] as const, rotation: [4, -11, 0] as const };

const PLATFORMS: ReadonlyArray<readonly [Platform, string]> = [
  ["tiktok", "TikTok"],
  ["instagram", "Instagram Reels"],
  ["facebook", "Facebook Reels"],
];

const UsersVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const swing = spring({ frame, fps, config: { damping: 18, stiffness: 80, mass: 1 } });
  const [x, y, z] = USERS_PHONE.position;
  const [rx, ry] = USERS_PHONE.rotation;
  const settle = ramp(frame, T.short, 20);
  const textOut = ramp(frame, USERS.textExit, 8);
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("users")} />
      <Stage style={{ filter: "drop-shadow(0 40px 50px rgba(60,42,25,0.2))" }}>
        <Phone3D
          width={USERS_PHONE.width}
          position={[x + 900 * (1 - swing), y, z - 200 * (1 - swing)]}
          rotation={[rx, ry - 70 * (1 - swing) - 5 * (1 - settle), 0]}
          screenshot="screens/setup-platforms.png"
        >
          <ScreenHighlight rect={{ x: 23, y: 152, w: 314, h: 187 }} at={T.short - 2} />
        </Phone3D>
      </Stage>
      <Block block={textBlock("users", "built-for")}>
        <div style={eyebrow()}>Built for</div>
        <div style={{ ...display(116), marginTop: 22 }}>
          Adult Filipino
          <br />
          Android users
        </div>
      </Block>
      <Block block={textBlock("users", "who-watch")} style={body(40)}>
        who watch short-form video on
      </Block>
      <div style={{ position: "absolute", left: 150, top: 735, display: "flex", gap: 16, opacity: 1 - textOut, translate: `0 ${-18 * textOut}px` }}>
        {PLATFORMS.map(([platform, label], i) => {
          const pop = spring({ frame: frame - USERS.chips[i] + 4, fps, config: { damping: 14, stiffness: 160, mass: 0.8 } });
          return (
            <div
              key={platform}
              style={{
                ...card,
                borderRadius: 999,
                display: "flex",
                alignItems: "center",
                gap: 14,
                padding: "10px 26px 10px 10px",
                opacity: Math.min(1, pop * 2),
                scale: String(0.8 + 0.2 * pop),
                translate: `0 ${(1 - pop) * 26}px`,
              }}
            >
              <PlatformIcon platform={platform} size={52} style={{ borderRadius: 14 }} />
              <span style={body(32, PAPER.ink, 700)}>{label}</span>
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

export const Users: React.FC = () => (
  <MotionBlur windows={[[0, 16]]}>
    <UsersVisual />
  </MotionBlur>
);
