import React from "react";
import { AbsoluteFill, interpolate, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { COLORS, FONT, PAPER } from "../brand/tokens";
import { body, card, centered, display } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { FeedScreen } from "../components/FeedScreen";
import { Icon, type IconName } from "../components/Icon";
import { exitStyle, ramp, revealStyle, Words } from "../components/Kinetic";
import { Phone, phoneHeightFor } from "../components/Phone";
import { sceneStart } from "../timeline";

const EXIT = 153;
const PHONE_W = 360;
const PHONE_TOP = 250;
const PHONE_CENTER = { x: 960, y: PHONE_TOP + phoneHeightFor(PHONE_W) / 2 };
const CARD_W = 470;

// Signal cards fly out from behind the phone as the narration names them.
const usePop = (at: number, x: number, y: number): React.CSSProperties => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame: frame - at, fps, config: { damping: 15, stiffness: 110 } });
  const blur = (1 - Math.min(1, p)) * 14;
  return {
    position: "absolute",
    left: x,
    top: y,
    width: CARD_W,
    opacity: Math.min(1, p * 2),
    translate: `${(PHONE_CENTER.x - (x + CARD_W / 2)) * (1 - p)}px ${(PHONE_CENTER.y - (y + 80)) * (1 - p)}px`,
    scale: String(0.5 + 0.5 * p),
    filter: blur > 0.05 ? `blur(${blur}px)` : undefined,
  };
};

const SignalCard: React.FC<{
  readonly at: number;
  readonly x: number;
  readonly y: number;
  readonly icon: IconName;
  readonly tint: string;
  readonly iconColor: string;
  readonly label: string;
  readonly value: string;
  readonly valueColor?: string;
  readonly valueSize?: number;
  readonly children?: React.ReactNode;
}> = ({ at, x, y, icon, tint, iconColor, label, value, valueColor = PAPER.ink, valueSize = 60, children }) => (
  <div style={usePop(at, x, y)}>
    <div style={{ ...card, padding: "26px 30px", display: "flex", gap: 22, alignItems: "flex-start" }}>
      <div style={{ width: 68, height: 68, borderRadius: 34, background: tint, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
        <Icon name={icon} size={36} color={iconColor} strokeWidth={2.2} />
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
        <span style={body(28)}>{label}</span>
        <span style={{ ...display(valueSize, valueColor), fontVariantNumeric: "tabular-nums", whiteSpace: "nowrap" }}>{value}</span>
        {children}
      </div>
    </div>
  </div>
);

// Highlights the word the sentiment analyzer scores, with its lexicon valence.
const Scored: React.FC<{ readonly children: string; readonly at: number; readonly valence: string }> = ({ children, at, valence }) => {
  const frame = useCurrentFrame();
  const p = ramp(frame, at, 10);
  return (
    <span style={{ position: "relative", display: "inline-block" }}>
      <span
        style={{
          position: "absolute",
          left: -3,
          right: -3,
          top: 0,
          bottom: 0,
          borderRadius: 4,
          background: COLORS.coral,
          scale: `${p} 1`,
          transformOrigin: "left center",
        }}
      />
      <span style={{ position: "relative", color: p > 0.5 ? COLORS.coralContainer : "#fff", textShadow: p > 0.5 ? "none" : undefined }}>{children}</span>
      <span
        style={{
          position: "absolute",
          left: "100%",
          top: -24,
          marginLeft: -6,
          padding: "2px 8px",
          borderRadius: 8,
          background: COLORS.coralContainer,
          color: COLORS.onCoralContainer,
          fontFamily: FONT,
          fontSize: 13,
          fontWeight: 800,
          textShadow: "none",
          ...revealStyle(frame, { at: at + 6, dur: 12, blur: 6, rise: 8 }),
        }}
      >
        {valence}
      </span>
    </span>
  );
};

export const Signals: React.FC = () => {
  const frame = useCurrentFrame();
  const push = interpolate(frame, [0, 161], [1, 1.03]);
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("signals")} />
      <AbsoluteFill style={{ scale: String(push), ...exitStyle(frame, EXIT, 8) }}>
        <AbsoluteFill style={{ ...centered, justifyContent: "flex-start", paddingTop: 78 }}>
          <Words text="Three signals." times={[11, 23]} style={display(104)} />
        </AbsoluteFill>
        <SignalCard at={56} x={220} y={330} icon="clock" tint="rgba(99,130,197,0.14)" iconColor={PAPER.accent} label="Session length" value="23 min" />
        <SignalCard at={86} x={220} y={590} icon="hourglass" tint="rgba(99,130,197,0.14)" iconColor={PAPER.accent} label="Time per video" value="41 s" />
        <SignalCard
          at={119}
          x={1230}
          y={330}
          icon="captions"
          tint="rgba(229,150,140,0.2)"
          iconColor={PAPER.coralInk}
          label="Negative captions"
          value="38%"
          valueColor={PAPER.coralInk}
        >
          <span style={{ ...body(22, PAPER.inkFaint, 600), marginTop: 6 }}>Filipino + English lexicon</span>
        </SignalCard>
        <SignalCard
          at={132}
          x={1230}
          y={620}
          icon="image"
          tint="rgba(143,181,154,0.22)"
          iconColor={PAPER.sageInk}
          label="No caption?"
          value="Vision model"
          valueSize={44}
        >
          <span style={{ ...body(22, PAPER.inkFaint, 600), marginTop: 6 }}>Moondream 0.5B, on-device</span>
        </SignalCard>
        <div style={{ position: "absolute", left: PHONE_CENTER.x - PHONE_W / 2, top: PHONE_TOP, ...revealStyle(frame, { at: 0, dur: 22, blur: 16, rise: 140 }) }}>
          <Phone width={PHONE_W} onPaper>
            <FeedScreen
              art="rain"
              user="balita.now"
              caption={
                <>
                  grabe, <Scored at={123} valence="−3">nakakalungkot</Scored> naman huhu #balita
                </>
              }
              likes="65.3K"
              comments="2,733"
              shares="8,904"
              progress={0.15 + frame / 330}
              gloom={0.25}
            />
          </Phone>
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
