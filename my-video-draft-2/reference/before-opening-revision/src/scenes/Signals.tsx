import React from "react";
import { AbsoluteFill, Easing, interpolate, interpolateColors, useCurrentFrame } from "remotion";
import { COLORS, PAPER } from "../brand/tokens";
import { body, card, display } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { FeedScreen } from "../components/FeedScreen";
import { Icon } from "../components/Icon";
import { EASE_IN_OUT, exitStyle, ramp, revealStyle, Words } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { Phone, phoneHeightFor } from "../components/Phone";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.signals;
const PHONE_W = 360;
// Transform the entire phone, including bezel and feed controls. Its top and
// right edges leave the composition; the visible bottom bezel preserves its form.
const CAMERA = { wideX: 1190, wideY: 200, zoom: 6.1, closeX: 900, closeBottom: 1070 };
const CAMERA_EASE = Easing.bezier(0.4, 0, 0.2, 1);
const mix = (from: number, to: number, p: number) => from + (to - from) * p;

const SignalCard: React.FC<{ readonly n: number; readonly label: string; readonly value: string; readonly at: number; readonly focus: number }> = ({ n, label, value, at, focus }) => {
  const frame = useCurrentFrame();
  const negative = n === 3;
  const color = negative ? PAPER.coralInk : PAPER.accent;
  const top = mix([260, 470, 680][n - 1], [230, 365, 535][n - 1], focus);
  return <div style={{ position: "absolute", left: 130, top, transformOrigin: "left top", scale: String(negative ? 1 : 1 - 0.22 * focus), opacity: negative ? 1 : 1 - 0.55 * focus }}>
    <div style={{ ...card, width: 660, padding: "26px 30px", display: "flex", alignItems: "flex-start", gap: 22, ...revealStyle(frame, { at: at - 9, dur: 18, rise: 30, blur: 18 }) }}>
      <div style={{ width: 68, height: 68, borderRadius: 34, flexShrink: 0, display: "grid", placeItems: "center", background: negative ? "#FAE6E1" : "#E9EDF8" }}>
        <Icon name={n === 1 ? "clock" : n === 2 ? "hourglass" : "captions"} size={36} color={color} />
      </div>
      <div>
        <Words readableOnset text={label} times={n === 1 ? T.sessionWords : n === 2 ? T.videoWords : T.negativeWords} style={body(negative ? mix(32, 46, focus) : 32, PAPER.ink, 800)} />
        <div style={{ ...display(negative ? mix(60, 92, focus) : 60, color), marginTop: 8, fontVariantNumeric: "tabular-nums" }}>{value}</div>
        {negative ? <div style={{ ...body(mix(22, 28, focus)), marginTop: 12 }}>Filipino + English lexicon</div> : null}
      </div>
    </div>
  </div>;
};

const SignalsVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const focus = ramp(frame, T.captionStart, T.captionReady - T.captionStart, CAMERA_EASE);
  const zoom = Math.exp(Math.log(CAMERA.zoom) * focus);
  const x = mix(CAMERA.wideX, CAMERA.closeX, focus);
  // Anchor the bottom of the phone while its perceived scale changes smoothly.
  // This keeps the caption on a continuous camera path through the enlargement.
  const bottom = mix(CAMERA.wideY + phoneHeightFor(PHONE_W), CAMERA.closeBottom, focus);
  const y = bottom - phoneHeightFor(PHONE_W) * zoom;
  const highlighted = ramp(frame, T.highlight - 6, 6, EASE_IN_OUT);
  return <AbsoluteFill style={{ overflow: "hidden" }}>
    <Paper offset={sceneStart("signals")} />
    <AbsoluteFill style={exitStyle(frame, T.holdUntil, 8)}>
      <div style={{ position: "absolute", left: x, top: y, scale: String(zoom), transformOrigin: "0 0" }}>
        <div style={revealStyle(frame, { at: 0, dur: 24, blur: 16, rise: 100 })}>
        <Phone width={PHONE_W} onPaper>
          <FeedScreen
            art="rain" user="balita.now" likes="65.3K" comments="2,733" shares="8,904" progress={0.2} gloom={0.15}
            caption={<>
              Grabe,<br />
              <span style={{ position: "relative", display: "inline-block", color: interpolateColors(highlighted, [0, 1], ["#FFFFFF", COLORS.coralContainer]), textShadow: "none", padding: "0 2px" }}>
                <span style={{ position: "absolute", inset: 0, right: "auto", width: `${highlighted * 100}%`, background: COLORS.coral, borderRadius: 2 }} />
                <span style={{ position: "relative" }}>nakakalungkot</span>
              </span>
              <br />naman.
            </>}
          />
        </Phone>
        </div>
      </div>
      <div style={{ position: "absolute", top: 78, left: 130 }}>
        <Words readableOnset text="Three signals." times={T.heading} style={display(112)} />
      </div>
      <SignalCard n={1} label="Session length" value="23 min" at={T.session} focus={focus} />
      <SignalCard n={2} label="Time per video" value="41 s" at={T.video} focus={focus} />
      <SignalCard n={3} label="Negative captions" value="38%" at={T.negative} focus={focus} />
      <div style={{ position: "absolute", left: 140, top: mix(936, 836, focus), width: 660, ...body(27, PAPER.inkSoft, 500), opacity: ramp(frame, T.negative - 9, 18) }}>
        No usable text → on-device vision fallback
      </div>
      <div style={{ position: "absolute", left: 140, top: 1000, ...body(24, PAPER.inkSoft, 500), opacity: interpolate(frame, [T.session, T.session + 8], [0, 1], { extrapolateLeft: "clamp", extrapolateRight: "clamp" }) }}>Illustrative session metrics</div>
    </AbsoluteFill>
  </AbsoluteFill>;
};

export const Signals: React.FC = () => <MotionBlur windows={[[0, 24], [T.captionStart, T.captionReady]]}><SignalsVisual /></MotionBlur>;
