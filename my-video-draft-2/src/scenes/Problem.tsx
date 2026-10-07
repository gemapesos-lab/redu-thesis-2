import React from "react";
import { AbsoluteFill, interpolate, useCurrentFrame } from "remotion";
import { COLORS, FONT } from "../brand/tokens";
import { display } from "../brand/type";
import { PROBLEM, textBlock } from "../choreography";
import { Night } from "../components/Backdrop";
import { WordBlock } from "../components/Block";
import { FeedScreen, type FeedArt } from "../components/FeedScreen";
import { Icon } from "../components/Icon";
import { EASE_IN_OUT, handheld, ramp } from "../components/Kinetic";
import { SCREEN_W } from "../components/Phone";
import { Extrude } from "../components/three/Extrude";
import type { Camera, Material, Vec3 } from "../components/three/math";
import { lens, Stage } from "../components/three/Stage";
import { MEASURED_BEATS, SCENE_TIMINGS } from "../voiceover-cues";

const T = MEASURED_BEATS.problem;
const END = SCENE_TIMINGS.problem.frames;
const MUTED = "#8B8F99";
const PERIWINKLE_ON_DARK = "#93A9E2";
const WIDGET = { width: 560, height: 300, at: [470, -20, 0] as Vec3 };
const GLASS: Material = { color: "#2C2824", ambient: 0.5, diffuse: 0.55, fill: 0.2, specular: 0.4, shininess: 18 };

// The videos a timer never sees, floating behind the widget.
const THUMBS: ReadonlyArray<{ readonly art: FeedArt; readonly user: string; readonly caption: string; readonly at: Vec3; readonly rotation: Vec3 }> = [
  { art: "sunset", user: "mika.travels", caption: "golden hour sa Batangas", at: [180, -250, -420], rotation: [0, -10, -6] },
  { art: "neon", user: "dance.daily", caption: "trend na 'to, sino gagawa?", at: [520, -300, -460], rotation: [0, -16, 5] },
  { art: "storm", user: "balita.now", caption: "grabe ang ulan sa Maynila ngayon", at: [790, -90, -400], rotation: [0, -22, -4] },
  { art: "food", user: "kain.tayo", caption: "3-ingredient leche flan", at: [260, 230, -440], rotation: [0, -12, 7] },
  { art: "city", user: "late.night.edits", caption: "one more episode recap... part 7", at: [640, 250, -380], rotation: [0, -18, -3] },
];
const THUMB_W = 150;
const THUMB_H = THUMB_W * (780 / 360);

const minutesLabel = (m: number) => `${Math.floor(m / 60)}h ${String(Math.round(m) % 60).padStart(2, "0")}m`;

export const Problem: React.FC = () => {
  const frame = useCurrentFrame();
  const widgetIn = ramp(frame, PROBLEM.widgetAt - 4, 18);
  const rackFocus = ramp(frame, PROBLEM.rack, 18, EASE_IN_OUT);
  const recede = ramp(frame, PROBLEM.out, END - PROBLEM.out, (t) => t * t);
  const minutes = interpolate(frame, [PROBLEM.widgetAt + 4, PROBLEM.countEnd], [101, 118], { extrapolateLeft: "clamp", extrapolateRight: "clamp" });
  const camera: Camera = {
    x: interpolate(frame, [0, END], [40, -40]) + handheld(frame + 272, 1) * 5,
    y: handheld(frame + 272, 2) * 4,
    orbit: handheld(frame + 272, 3) * 0.4,
    tilt: handheld(frame + 272, 4) * 0.3,
    roll: handheld(frame + 272, 5) * 0.3,
    dolly: -380 * recede,
  };
  const dot = ramp(frame, END - 10, 10);
  const accent = { color: PERIWINKLE_ON_DARK };

  return (
    <AbsoluteFill>
      <Night offset={272} glow={1 - 0.6 * recede} />
      <WordBlock
        block={textBlock("problem", "thats-doomscrolling")}
        text={"That's\ndoomscrolling."}
        times={T.intro}
        align="center"
        lineStyle={{ 0: display(96, MUTED), 1: { ...display(196, "#FFFFFF"), letterSpacing: "-0.045em", marginTop: 6 } }}
      />

      {THUMBS.map((thumb, i) => (
        <Stage key={thumb.user} camera={camera} style={lens(12 * (1 - rackFocus) + 6 * recede, (0.4 + 0.6 * rackFocus) * widgetIn * (1 - recede))}>
          <Extrude
            width={THUMB_W}
            height={THUMB_H}
            depth={6}
            radius={16}
            side={GLASS}
            segments={3}
            position={[thumb.at[0] + (i % 2 ? 30 : -30) * rackFocus, thumb.at[1], thumb.at[2] + 180 * rackFocus]}
            rotation={thumb.rotation}
            front={
              <div style={{ position: "absolute", left: 0, top: 0, width: SCREEN_W, height: 780, scale: String(THUMB_W / SCREEN_W), transformOrigin: "0 0" }}>
                <FeedScreen art={thumb.art} user={thumb.user} caption={thumb.caption} likes="" comments="" shares="" progress={0.3 + 0.1 * i} />
              </div>
            }
          />
        </Stage>
      ))}

      <Stage camera={camera} style={lens(7 * rackFocus + 10 * (1 - widgetIn), widgetIn * (1 - 0.45 * rackFocus) * (1 - recede))}>
        <Extrude
          width={WIDGET.width}
          height={WIDGET.height}
          depth={22}
          radius={34}
          side={GLASS}
          position={[WIDGET.at[0] + 120 * (1 - widgetIn), WIDGET.at[1], -300 * (1 - widgetIn)]}
          rotation={[6, -18 - 40 * (1 - widgetIn), 0]}
          front={
            <div style={{ position: "absolute", inset: 0, background: COLORS.surfaceLow, padding: "34px 40px", fontFamily: FONT }}>
              <div style={{ display: "flex", alignItems: "center", gap: 14, color: COLORS.textSecondary, fontSize: 26, fontWeight: 600 }}>
                <Icon name="hourglass" size={30} color={COLORS.honey} />
                Screen time today
              </div>
              <div style={{ color: "#FFFFFF", fontSize: 96, fontWeight: 800, letterSpacing: "-0.03em", marginTop: 10, fontVariantNumeric: "tabular-nums" }}>{minutesLabel(minutes)}</div>
              <div style={{ height: 12, borderRadius: 6, background: COLORS.surfaceHighest, marginTop: 16, overflow: "hidden" }}>
                <div style={{ width: `${(minutes / 120) * 100}%`, height: "100%", borderRadius: 6, background: COLORS.honey }} />
              </div>
              <div style={{ color: COLORS.textSecondary, fontSize: 22, fontWeight: 600, marginTop: 14 }}>App limit · 2h a day</div>
            </div>
          }
        />
      </Stage>

      <WordBlock block={textBlock("problem", "count-minutes")} text={"Screen-time limits\ncount minutes."} times={T.limits} style={display(80, "#C9C2BA")} />
      <WordBlock block={textBlock("problem", "not-what-you-watch")} text={"Not what\nyou watch."} times={T.contrast} wordStyle={{ 1: accent, 2: accent, 3: accent }} style={display(120, "#FFFFFF")} />

      <AbsoluteFill style={{ justifyContent: "center", alignItems: "center", opacity: dot }}>
        <div style={{ width: 68, height: 68, borderRadius: 34, background: COLORS.sage, opacity: 0.92, boxShadow: "0 0 0 11px rgba(143,181,154,0.14), 0 0 34px rgba(143,181,154,0.35)" }} />
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
