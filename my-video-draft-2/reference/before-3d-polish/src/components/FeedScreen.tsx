import React from "react";
import { AbsoluteFill } from "remotion";
import { FONT } from "../brand/tokens";
import { Icon } from "./Icon";
import { SCREEN_H, SCREEN_W, STATUS_BAR_H } from "./Phone";

export type FeedArt = "sunset" | "ocean" | "city" | "rain" | "food" | "neon" | "storm";

const ArtLayer: React.FC<{ readonly art: FeedArt }> = ({ art }) => {
  switch (art) {
    case "sunset":
      return (
        <AbsoluteFill style={{ background: "linear-gradient(180deg, #FFB676 0%, #F27A7D 38%, #8E4C9E 72%, #3A2A63 100%)" }}>
          <div style={{ position: "absolute", left: 95, top: 270, width: 170, height: 170, borderRadius: 85, background: "radial-gradient(circle, #FFF1C2 0%, #FFC46B 55%, rgba(255,196,107,0) 72%)" }} />
          <svg width={SCREEN_W} height={SCREEN_H} style={{ position: "absolute", inset: 0 }}>
            <path d="M0 470 C 80 420, 150 450, 210 430 S 320 400, 360 440 L360 780 L0 780 Z" fill="#4B2E6E" />
            <path d="M0 540 C 90 500, 170 540, 250 515 S 330 500, 360 520 L360 780 L0 780 Z" fill="#2A1C45" />
          </svg>
        </AbsoluteFill>
      );
    case "ocean":
      return (
        <AbsoluteFill style={{ background: "linear-gradient(180deg, #B5ECF5 0%, #63BFE0 38%, #1F6FA8 72%, #0D3D66 100%)" }}>
          <svg width={SCREEN_W} height={SCREEN_H} style={{ position: "absolute", inset: 0 }}>
            <path d="M0 400 Q 45 385 90 400 T 180 400 T 270 400 T 360 400 L360 780 L0 780 Z" fill="rgba(255,255,255,0.18)" />
            <path d="M0 450 Q 45 435 90 450 T 180 450 T 270 450 T 360 450 L360 780 L0 780 Z" fill="rgba(13,61,102,0.45)" />
            <path d="M0 520 Q 60 500 120 520 T 240 520 T 360 520 L360 780 L0 780 Z" fill="rgba(6,34,64,0.55)" />
          </svg>
        </AbsoluteFill>
      );
    case "city":
      return (
        <AbsoluteFill style={{ background: "linear-gradient(180deg, #1B1440 0%, #3A1F5C 50%, #120D24 100%)" }}>
          <div style={{ position: "absolute", left: 30, top: 160, width: 90, height: 90, borderRadius: 45, background: "radial-gradient(circle, rgba(255,170,90,0.75), transparent 70%)" }} />
          <div style={{ position: "absolute", left: 200, top: 220, width: 120, height: 120, borderRadius: 60, background: "radial-gradient(circle, rgba(120,170,255,0.6), transparent 70%)" }} />
          <div style={{ position: "absolute", left: 120, top: 320, width: 70, height: 70, borderRadius: 35, background: "radial-gradient(circle, rgba(255,110,180,0.65), transparent 70%)" }} />
          <svg width={SCREEN_W} height={SCREEN_H} style={{ position: "absolute", inset: 0 }}>
            <path d="M0 520 V420 H40 V380 H80 V450 H120 V350 H170 V430 H210 V390 H260 V460 H300 V370 H360 V780 H0 Z" fill="#0B0818" />
          </svg>
        </AbsoluteFill>
      );
    case "rain":
      return (
        <AbsoluteFill style={{ background: "linear-gradient(180deg, #3A4455 0%, #222935 55%, #12161D 100%)" }}>
          <AbsoluteFill style={{ backgroundImage: "repeating-linear-gradient(105deg, rgba(255,255,255,0.07) 0 1.5px, transparent 1.5px 14px)" }} />
          <div style={{ position: "absolute", left: 110, top: 250, width: 140, height: 200, borderRadius: 70, background: "radial-gradient(ellipse, rgba(170,190,220,0.25), transparent 70%)" }} />
        </AbsoluteFill>
      );
    case "storm":
      return (
        <AbsoluteFill style={{ background: "linear-gradient(180deg, #2B2F3A 0%, #1B1E26 50%, #0E1015 100%)" }}>
          <div style={{ position: "absolute", left: -40, top: 120, width: 440, height: 220, borderRadius: 220, background: "radial-gradient(ellipse, rgba(90,98,120,0.6), transparent 70%)" }} />
          <svg width={SCREEN_W} height={SCREEN_H} style={{ position: "absolute", inset: 0 }}>
            <path d="M196 230 L170 330 L200 330 L176 430" stroke="rgba(230,235,255,0.85)" strokeWidth="4" fill="none" strokeLinejoin="round" />
          </svg>
        </AbsoluteFill>
      );
    case "food":
      return (
        <AbsoluteFill style={{ background: "radial-gradient(circle at 50% 45%, #F6C177 0%, #D9822B 45%, #6B3410 100%)" }}>
          <div style={{ position: "absolute", left: 55, top: 230, width: 250, height: 250, borderRadius: 125, background: "radial-gradient(circle, #FFF8EE 0%, #F2E6D6 62%, #D9C7B0 64%, rgba(0,0,0,0) 66%)" }} />
          <div style={{ position: "absolute", left: 120, top: 300, width: 120, height: 100, borderRadius: 60, background: "radial-gradient(circle, #C2451E, #8C2A10)" }} />
        </AbsoluteFill>
      );
    case "neon":
      return (
        <AbsoluteFill style={{ background: "linear-gradient(160deg, #FF4FA3 0%, #7B2FF7 50%, #1BC9E4 100%)" }}>
          <AbsoluteFill style={{ background: "conic-gradient(from 200deg at 50% 0%, transparent 0deg, rgba(255,255,255,0.25) 12deg, transparent 24deg, transparent 40deg, rgba(255,255,255,0.18) 52deg, transparent 64deg)" }} />
        </AbsoluteFill>
      );
    default:
      return null;
  }
};

type FeedScreenProps = {
  readonly art: FeedArt;
  readonly user: string;
  readonly caption: React.ReactNode;
  readonly likes: string;
  readonly comments: string;
  readonly shares: string;
  readonly progress?: number;
  // 0..1. Desaturates and darkens the video to suggest negative content.
  readonly gloom?: number;
};

export const FeedScreen: React.FC<FeedScreenProps> = ({ art, user, caption, likes, comments, shares, progress = 0.35, gloom = 0 }) => {
  const label: React.CSSProperties = {
    fontFamily: FONT,
    color: "#fff",
    textShadow: "0 1px 3px rgba(0,0,0,0.45)",
  };
  return (
    <AbsoluteFill style={{ width: SCREEN_W, height: SCREEN_H }}>
      <AbsoluteFill style={{ filter: gloom > 0 ? `saturate(${1 - gloom * 0.7}) brightness(${1 - gloom * 0.35})` : undefined }}>
        <ArtLayer art={art} />
      </AbsoluteFill>
      <AbsoluteFill style={{ background: "linear-gradient(to top, rgba(0,0,0,0.6) 0%, rgba(0,0,0,0) 38%), linear-gradient(to bottom, rgba(0,0,0,0.35) 0%, rgba(0,0,0,0) 14%)" }} />
      <div style={{ ...label, position: "absolute", top: STATUS_BAR_H + 8, left: 0, right: 0, display: "flex", justifyContent: "center", gap: 22, fontSize: 15.5, fontWeight: 700 }}>
        <span style={{ opacity: 0.65 }}>Following</span>
        <span style={{ position: "relative" }}>
          For You
          <span style={{ position: "absolute", left: "50%", bottom: -7, width: 24, height: 2.5, marginLeft: -12, borderRadius: 2, background: "#fff" }} />
        </span>
      </div>
      <div style={{ position: "absolute", right: 10, top: 400, width: 52, display: "flex", flexDirection: "column", alignItems: "center", gap: 16 }}>
        <div style={{ width: 46, height: 46, borderRadius: 23, border: "2px solid #fff", background: "linear-gradient(135deg, #F4EDE4, #C4B5A8)", marginBottom: 6 }} />
        <RailItem icon="heart" count={likes} />
        <RailItem icon="message" count={comments} />
        <RailItem icon="bookmark" count="1.2K" />
        <RailItem icon="share" count={shares} filled={false} />
      </div>
      <div style={{ position: "absolute", left: 14, right: 78, bottom: 34, display: "flex", flexDirection: "column", gap: 6 }}>
        <div style={{ ...label, fontSize: 16, fontWeight: 700 }}>@{user}</div>
        <div style={{ ...label, fontSize: 14.5, fontWeight: 500, lineHeight: 1.35 }}>{caption}</div>
        <div style={{ ...label, display: "flex", alignItems: "center", gap: 6, fontSize: 13, fontWeight: 500, opacity: 0.9 }}>
          <Icon name="music" size={13} color="#fff" />
          original sound · {user}
        </div>
      </div>
      <div style={{ position: "absolute", right: 14, bottom: 34, width: 40, height: 40, borderRadius: 20, background: "radial-gradient(circle, #555 0 22%, #111 24% 100%)", boxShadow: "0 0 0 6px rgba(20,20,20,0.6)" }} />
      <div style={{ position: "absolute", left: 0, right: 0, bottom: 0, height: 2.5, background: "rgba(255,255,255,0.25)" }}>
        <div style={{ width: `${progress * 100}%`, height: "100%", background: "rgba(255,255,255,0.9)" }} />
      </div>
    </AbsoluteFill>
  );
};

const RailItem: React.FC<{ readonly icon: "heart" | "message" | "bookmark" | "share"; readonly count: string; readonly filled?: boolean }> = ({ icon, count, filled = true }) => (
  <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 3 }}>
    <Icon name={icon} size={32} color="#fff" fill={filled ? "#fff" : "none"} strokeWidth={filled ? 1.2 : 2.4} style={{ filter: "drop-shadow(0 1px 2px rgba(0,0,0,0.4))" }} />
    <span style={{ fontFamily: FONT, color: "#fff", fontSize: 12, fontWeight: 600, textShadow: "0 1px 2px rgba(0,0,0,0.45)" }}>{count}</span>
  </div>
);
