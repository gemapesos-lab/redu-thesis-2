import React from "react";
import { Img, staticFile, useCurrentFrame } from "remotion";
import { COLORS, FONT } from "../brand/tokens";
import { ramp } from "./Kinetic";

// Screens are laid out in Android dp at the Galaxy A35's 1080x2340 px / 3x density.
export const SCREEN_W = 360;
export const SCREEN_H = 780;
export const STATUS_BAR_H = 37.33;

const BEZEL = 10;
const PHONE_W = SCREEN_W + BEZEL * 2;
const PHONE_H = SCREEN_H + BEZEL * 2;

export const phoneHeightFor = (width: number) => (width / PHONE_W) * PHONE_H;

export const StatusBar: React.FC<{ readonly color?: string; readonly time?: string }> = ({ color = COLORS.textPrimary, time = "9:41" }) => (
  <div
    style={{
      position: "absolute",
      top: 0,
      left: 0,
      right: 0,
      height: STATUS_BAR_H,
      display: "flex",
      alignItems: "center",
      justifyContent: "space-between",
      padding: "2px 22px 0 24px",
      color,
      fontFamily: FONT,
      fontSize: 14,
      fontWeight: 600,
      zIndex: 5,
    }}
  >
    <span>{time}</span>
    <svg width="58" height="14" viewBox="0 0 58 14" fill="none">
      <path d="M1 5.2a10 10 0 0 1 14 0M3.6 7.9a6.2 6.2 0 0 1 8.8 0M6.2 10.6a2.4 2.4 0 0 1 3.6 0" stroke={color} strokeWidth="1.7" strokeLinecap="round" />
      <rect x="20" y="9" width="2.6" height="4" rx="0.8" fill={color} />
      <rect x="24" y="6.5" width="2.6" height="6.5" rx="0.8" fill={color} />
      <rect x="28" y="4" width="2.6" height="9" rx="0.8" fill={color} />
      <rect x="32" y="1.5" width="2.6" height="11.5" rx="0.8" fill={color} opacity="0.4" />
      <rect x="38.5" y="1.5" width="16" height="11" rx="3.2" stroke={color} strokeOpacity="0.5" strokeWidth="1.2" />
      <rect x="40.3" y="3.3" width="10.5" height="7.4" rx="1.8" fill={color} />
      <path d="M56 5.5v3" stroke={color} strokeOpacity="0.5" strokeWidth="1.4" strokeLinecap="round" />
    </svg>
  </div>
);

type PhoneProps = {
  readonly width: number;
  // Screen content, laid out at SCREEN_W x SCREEN_H dp.
  readonly children?: React.ReactNode;
  // A 1080x2340 capture from app_showcase/ (copied to public/screens).
  readonly screenshot?: string;
  readonly statusBar?: boolean;
  readonly time?: string;
  // Gesture bar; captures already include their own.
  readonly handle?: boolean;
  // Softer, warmer drop shadow for light backgrounds.
  readonly onPaper?: boolean;
  readonly style?: React.CSSProperties;
};

const SHADOW_DARK = "0 36px 90px rgba(0,0,0,0.55), 0 8px 24px rgba(0,0,0,0.45)";
const SHADOW_PAPER = "0 50px 90px rgba(60,42,25,0.26), 0 18px 36px rgba(60,42,25,0.18)";

export const Phone: React.FC<PhoneProps> = ({ width, children, screenshot, statusBar = true, time, handle = !screenshot, onPaper = false, style }) => {
  const scale = width / PHONE_W;
  return (
    <div style={{ width, height: PHONE_H * scale, position: "relative", flexShrink: 0, ...style }}>
      <div
        style={{
          position: "absolute",
          left: 0,
          top: 0,
          width: PHONE_W,
          height: PHONE_H,
          scale: String(scale),
          transformOrigin: "0 0",
          borderRadius: 46,
          background: "linear-gradient(145deg, #2B2724 0%, #0B0A09 38%, #050505 70%, #1F1C1A 100%)",
          boxShadow: `0 0 0 1.2px #3E3833, inset 0 0 0 1px rgba(255,255,255,0.07), ${onPaper ? SHADOW_PAPER : SHADOW_DARK}`,
        }}
      >
        <div style={{ position: "absolute", right: -2.5, top: 150, width: 3, height: 64, borderRadius: 2, background: "#2A2522" }} />
        <div style={{ position: "absolute", right: -2.5, top: 236, width: 3, height: 40, borderRadius: 2, background: "#2A2522" }} />
        <div
          style={{
            position: "absolute",
            left: BEZEL,
            top: BEZEL,
            width: SCREEN_W,
            height: SCREEN_H,
            borderRadius: 36,
            overflow: "hidden",
            background: COLORS.background,
          }}
        >
          {screenshot ? (
            <>
              <Img src={staticFile(screenshot)} style={{ position: "absolute", inset: 0, width: SCREEN_W, height: SCREEN_H }} />
              {statusBar ? (
                <div
                  style={{
                    position: "absolute",
                    top: 0,
                    left: 0,
                    right: 0,
                    height: STATUS_BAR_H + 0.5,
                    background:
                      "linear-gradient(to bottom, #0F0D0C 0%, #100E0D 4.5%, #1B1817 35.7%, #282624 89.3%, #282523 100%)",
                  }}
                />
              ) : null}
            </>
          ) : null}
          {children}
          {statusBar ? <StatusBar time={time} /> : null}
          {handle ? (
            <div
              style={{
                position: "absolute",
                bottom: 8,
                left: SCREEN_W / 2 - 54,
                width: 108,
                height: 4,
                borderRadius: 2,
                background: "rgba(255,255,255,0.55)",
                zIndex: 5,
              }}
            />
          ) : null}
          <div
            style={{
              position: "absolute",
              top: 12,
              left: SCREEN_W / 2 - 5.5,
              width: 11,
              height: 11,
              borderRadius: 6,
              background: "#000",
              boxShadow: "inset 0 0 0 1.5px #1A1A1A",
              zIndex: 6,
            }}
          />
        </div>
      </div>
    </div>
  );
};

type HighlightProps = {
  // Width of the <Phone> it sits on; place both in the same positioned parent.
  readonly width: number;
  // In screen dp.
  readonly rect: { readonly x: number; readonly y: number; readonly w: number; readonly h: number };
  readonly radius?: number;
  readonly at: number;
  readonly color?: string;
};

// A soft pulsing ring that points at part of a phone screen.
export const PhoneHighlight: React.FC<HighlightProps> = ({ width, rect, radius = 20, at, color = COLORS.action }) => {
  const frame = useCurrentFrame();
  const scale = width / PHONE_W;
  const p = ramp(frame, at, 14);
  const pulse = 0.5 + 0.5 * Math.sin(Math.max(0, frame - at) / 5);
  const pad = 6;
  return (
    <div
      style={{
        position: "absolute",
        left: (BEZEL + rect.x) * scale - pad,
        top: (BEZEL + rect.y) * scale - pad,
        width: rect.w * scale + pad * 2,
        height: rect.h * scale + pad * 2,
        borderRadius: radius * scale + pad,
        border: `3px solid ${color}`,
        boxShadow: `0 0 0 ${4 + 6 * pulse}px rgba(99,130,197,0.22)`,
        opacity: p,
        scale: String(1.06 - 0.06 * p),
        pointerEvents: "none",
      }}
    />
  );
};
