import React from "react";
import { AbsoluteFill } from "remotion";
import { COLORS, FONT } from "../brand/tokens";
import { SCREEN_H, SCREEN_W, STATUS_BAR_H } from "./Phone";

// Timings and copy match PromptPresenter.kt and BreathingCircleView.kt in the Android app.
const INHALE_MS = 4000;
const HOLD_MS = 3000;
const EXHALE_MS = 4000;
export const BREATH_CYCLE_MS = INHALE_MS + HOLD_MS + EXHALE_MS;
const MIN_RADIUS = 0.18;
const MAX_RADIUS = 0.46;

const accelerateDecelerate = (t: number) => Math.cos((t + 1) * Math.PI) / 2 + 0.5;

export const breathingState = (timeMs: number) => {
  const t = ((timeMs % BREATH_CYCLE_MS) + BREATH_CYCLE_MS) % BREATH_CYCLE_MS;
  if (t < INHALE_MS) {
    const f = accelerateDecelerate(t / INHALE_MS);
    return { radius: MIN_RADIUS + (MAX_RADIUS - MIN_RADIUS) * f, alpha: (72 + (188 - 72) * f) / 255, label: "Breathe in" };
  }
  if (t < INHALE_MS + HOLD_MS) {
    return { radius: MAX_RADIUS, alpha: 188 / 255, label: "Hold" };
  }
  const f = accelerateDecelerate((t - INHALE_MS - HOLD_MS) / EXHALE_MS);
  return { radius: MAX_RADIUS + (MIN_RADIUS - MAX_RADIUS) * f, alpha: (188 + (72 - 188) * f) / 255, label: "Breathe out" };
};

export const BreathingCircle: React.FC<{ readonly size: number; readonly timeMs: number; readonly stroke?: number }> = ({ size, timeMs, stroke = 2 }) => {
  const { radius, alpha } = breathingState(timeMs);
  const max = size / 2;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} style={{ overflow: "visible" }}>
      <circle cx={max} cy={max} r={max * 0.48} fill={COLORS.sage} fillOpacity={34 / 255} />
      <circle cx={max} cy={max} r={max * MAX_RADIUS} fill="none" stroke={COLORS.sage} strokeOpacity={92 / 255} strokeWidth={stroke} />
      <circle cx={max} cy={max} r={max * radius} fill={COLORS.sage} fillOpacity={alpha} />
    </svg>
  );
};

const eyebrow: React.CSSProperties = {
  fontFamily: FONT,
  fontSize: 11,
  fontWeight: 700,
  color: COLORS.textSecondary,
};

const PromptButton: React.FC<{ readonly label: string; readonly filled: boolean; readonly opacity?: number }> = ({ label, filled, opacity = 1 }) => (
  <div
    style={{
      height: 52,
      borderRadius: 26,
      background: filled ? COLORS.action : COLORS.surfaceHigh,
      color: filled ? COLORS.onAction : COLORS.textPrimary,
      fontFamily: FONT,
      fontSize: 14,
      fontWeight: 600,
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      opacity,
    }}
  >
    {label}
  </div>
);

const TextAction: React.FC<{ readonly label: string; readonly color: string }> = ({ label, color }) => (
  <div style={{ height: 52, display: "flex", alignItems: "center", justifyContent: "center", fontFamily: FONT, fontSize: 14, fontWeight: 600, color }}>
    {label}
  </div>
);

// Level 1: non-blocking banner. `remaining` is the 4 s timer bar, 1 = full.
export const AwarenessBanner: React.FC<{ readonly remaining?: number; readonly style?: React.CSSProperties }> = ({ remaining = 0.7, style }) => (
  <div
    style={{
      position: "absolute",
      top: STATUS_BAR_H + 12,
      left: 16,
      right: 16,
      padding: "14px 16px 12px",
      borderRadius: 20,
      background: COLORS.surfaceHigh,
      boxShadow: "0 10px 28px rgba(0,0,0,0.45)",
      zIndex: 4,
      ...style,
    }}
  >
    <div style={eyebrow}>REDU / PAUSE</div>
    <div style={{ fontFamily: FONT, fontSize: 15, fontWeight: 500, lineHeight: "22px", color: COLORS.textPrimary, padding: "5px 0 10px" }}>
      You’ve been scrolling for a while. A short pause may help.
    </div>
    <div style={{ height: 2, marginTop: 10, background: COLORS.action, width: `${remaining * 100}%` }} />
  </div>
);

// Level 2: full-screen pause. "Keep scrolling" unlocks after a short wait.
export const PauseScreen: React.FC<{ readonly unlocked?: boolean }> = ({ unlocked = false }) => (
  <AbsoluteFill style={{ width: SCREEN_W, height: SCREEN_H, background: COLORS.background, justifyContent: "center", padding: "0 24px" }}>
    <div style={{ ...eyebrow, textAlign: "center" }}>REDU / PAUSE</div>
    <div style={{ fontFamily: FONT, fontSize: 28, fontWeight: 700, color: COLORS.textPrimary, textAlign: "center", padding: "8px 0" }}>
      Pause for a moment
    </div>
    <div style={{ fontFamily: FONT, fontSize: 16, color: COLORS.textSecondary, textAlign: "center", lineHeight: "25px", paddingBottom: 20 }}>
      This session has been going for a while.
    </div>
    <PromptButton label="Take a break" filled />
    <div style={{ height: 10 }} />
    <PromptButton label="Keep scrolling" filled={false} opacity={unlocked ? 1 : 0.42} />
    <div style={{ height: 4 }} />
    <TextAction label="Open REDU" color={COLORS.textPrimary} />
  </AbsoluteFill>
);

// Level 3: guided breathing break (45 s in the current build).
export const BreathingScreen: React.FC<{ readonly timeMs: number; readonly secondsLeft: number }> = ({ timeMs, secondsLeft }) => {
  const { label } = breathingState(timeMs);
  return (
    <AbsoluteFill style={{ width: SCREEN_W, height: SCREEN_H, background: COLORS.background, justifyContent: "center", padding: "0 24px" }}>
      <div style={{ ...eyebrow, textAlign: "center" }}>REDU / RESET</div>
      <div style={{ fontFamily: FONT, fontSize: 28, fontWeight: 700, color: COLORS.textPrimary, textAlign: "center", padding: "10px 0 6px" }}>
        Take a breathing break
      </div>
      <div style={{ height: 248, marginTop: 4, display: "flex", justifyContent: "center" }}>
        <BreathingCircle size={248} timeMs={timeMs} />
      </div>
      <div style={{ fontFamily: FONT, fontSize: 30, fontWeight: 700, color: COLORS.textPrimary, textAlign: "center", padding: "8px 0 4px" }}>{label}</div>
      <div style={{ fontFamily: FONT, fontSize: 18, fontWeight: 500, color: COLORS.textSecondary, textAlign: "center", fontVariantNumeric: "tabular-nums", padding: "4px 0 12px" }}>
        0:{secondsLeft < 10 ? `0${secondsLeft}` : secondsLeft}
      </div>
      <div style={{ fontFamily: FONT, fontSize: 12, color: COLORS.textSecondary, textAlign: "center", padding: "4px 8px 8px", marginBottom: 14 }}>
        This is a digital wellness pause, not a clinical exercise.
      </div>
      <PromptButton label="Finish early" filled={false} />
      <div style={{ height: 4 }} />
      <TextAction label="Return to scrolling" color={COLORS.textSecondary} />
    </AbsoluteFill>
  );
};
