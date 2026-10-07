import React from "react";
import { AbsoluteFill, interpolate, useCurrentFrame } from "remotion";
import { COLORS, PAPER } from "../brand/tokens";
import { body, card, display } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { FeedScreen } from "../components/FeedScreen";
import { Icon } from "../components/Icon";
import { exitStyle, ramp, revealStyle, Words } from "../components/Kinetic";
import { Phone } from "../components/Phone";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.signals;
const Signal: React.FC<{ readonly n: number; readonly label: string; readonly value: string; readonly at: number; readonly top: number; readonly coral?: boolean }> = ({ n, label, value, at, top, coral = false }) => {
  const frame = useCurrentFrame();
  const color = coral ? PAPER.coralInk : PAPER.accent;
  return <div style={{ ...card, position: "absolute", left: 130, top, width: 590, padding: "26px 34px", display: "flex", alignItems: "center", gap: 28, ...revealStyle(frame, { at: at - 8, dur: 8, rise: 16, blur: 0 }) }}>
    <div style={{ width: 64, height: 64, borderRadius: 32, flexShrink: 0, display: "grid", placeItems: "center", background: coral ? "#FAE6E1" : "#E9EDF8", ...display(34, color) }}>{n}</div>
    <div><div style={body(32, PAPER.ink, 800)}>{label}</div><div style={{ ...display(56, color), marginTop: 8, fontVariantNumeric: "tabular-nums", display: "flex", alignItems: "center", gap: 15 }}><Icon name={n === 1 ? "clock" : n === 2 ? "hourglass" : "captions"} size={36} color={color} />{value}</div></div>
  </div>;
};

export const Signals: React.FC = () => {
  const frame = useCurrentFrame();
  const zoom = ramp(frame, T.captionStart, T.captionReady - T.captionStart);
  const back = ramp(frame, T.returnAt, T.returnDuration);
  const focus = zoom * (1 - back);
  const showCaption = frame >= T.captionStart && frame < T.returnAt + T.returnDuration;
  return <AbsoluteFill>
    <Paper offset={sceneStart("signals")} />
    <AbsoluteFill style={exitStyle(frame, 322, 8)}>
      <div style={{ position: "absolute", top: 82, left: 130, opacity: 1 - focus }}>
        <Words text="Three signals." times={T.heading} style={display(112)} />
      </div>
      <div style={{ position: "absolute", inset: 0, opacity: 1 - focus * 0.94, filter: focus > 0 ? "blur(" + focus * 8 + "px)" : undefined }}>
        <Signal n={1} label="Session length" value="23 min" at={T.session} top={280} />
        <Signal n={2} label="Time per video" value="41 s" at={T.video} top={490} />
        <Signal n={3} label="Negative captions" value="Negativity" at={T.negative} top={700} coral />
        <div style={{ position: "absolute", left: 1110, top: 175, ...revealStyle(frame, { at: 0, dur: 15, rise: 50 }) }}>
          <Phone width={355} onPaper>
            <FeedScreen art="rain" user="balita.now" caption="Grabe, nakakalungkot naman." likes="65.3K" comments="2,733" shares="8,904" progress={0.2} gloom={0.15} />
          </Phone>
        </div>
        <div style={{ position: "absolute", left: 139, top: 977, ...body(27, PAPER.inkSoft), opacity: frame >= T.session ? 1 : 0 }}>Illustrative session</div>
      </div>
      {showCaption ? <div style={{ position: "absolute", left: 170, top: 204, width: 1580, opacity: zoom * (1 - back), scale: String(0.45 + 0.55 * focus), translate: (1 - focus) * 420 + "px " + (1 - focus) * 390 + "px", transformOrigin: "70% 70%" }}>
        <div style={{ ...display(68, PAPER.coralInk), display: "flex", alignItems: "center", gap: 24 }}>
          <span style={{ background: COLORS.coral, color: COLORS.coralContainer, borderRadius: 40, width: 80, height: 80, display: "grid", placeItems: "center", ...display(42, COLORS.coralContainer) }}>3</span>
          Negative captions
        </div>
        <div style={{ ...card, marginTop: 34, padding: "64px 70px 54px", border: "2px solid #EDD9D4", boxShadow: "0 35px 90px rgba(60,42,25,0.14)" }}>
          <div style={{ display: "flex", alignItems: "center", gap: 14, ...body(30, PAPER.inkSoft), marginBottom: 36 }}>
            <Icon name="captions" size={34} color={PAPER.coralInk} /> Caption from the video
          </div>
          <div style={{ ...display(84), lineHeight: 1.45, whiteSpace: "nowrap" }}>
            Grabe, <span style={{ padding: "2px 15px 8px", borderRadius: 14, background: frame >= T.highlight ? COLORS.coral : "transparent", color: frame >= T.highlight ? COLORS.coralContainer : PAPER.ink }}>nakakalungkot</span> naman.
          </div>
          <div style={{ display: "flex", alignItems: "center", gap: 28, marginTop: 40, opacity: frame >= T.highlight ? 1 : 0 }}>
            <div style={{ borderRadius: 999, background: COLORS.coralContainer, padding: "13px 30px", ...body(40, COLORS.onCoralContainer, 800) }}>Negative</div>
            <div style={body(34)}>Filipino + English text analysis</div>
          </div>
        </div>
        <div style={{ ...body(28, PAPER.inkSoft, 500), marginTop: 28, marginLeft: 12 }}>
          No usable text → on-device vision fallback
        </div>
      </div> : null}
      <div style={{ position: "absolute", left: 1160, top: 955, ...body(28), opacity: interpolate(frame, [306, 313], [0, 1], { extrapolateLeft: "clamp", extrapolateRight: "clamp" }) }}>Three inputs. One risk estimate.</div>
    </AbsoluteFill>
  </AbsoluteFill>;
};
