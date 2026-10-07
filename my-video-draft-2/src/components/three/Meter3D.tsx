import React from "react";
import { interpolateColors } from "remotion";
import { PAPER, PATTERN, patternForScore, type PatternTone } from "../../brand/tokens";
import { body, display } from "../../brand/type";
import { Extrude, extrudeTransform } from "./Extrude";
import { rotate, type Vec3 } from "./math";

const TONES: ReadonlyArray<PatternTone> = ["low", "elevated", "high"];
const GAP = 14;
const BAR = 30;

type MeterProps = {
  readonly width: number;
  readonly position: Vec3;
  readonly rotation: Vec3;
  readonly value: number;
  // 0–1: Elevated stays bright while Low and High fade back.
  readonly emphasis?: number;
  readonly labels?: number;
  readonly puck?: number;
};

// The 0–100 risk meter as a physical slider: three lit bars and a puck.
// Children share the meter's rotation so their walls are lit consistently.
export const Meter3D: React.FC<MeterProps> = ({ width, position, rotation, value, emphasis = 0, labels = 1, puck = 1 }) => {
  const place = (local: Vec3): Vec3 => {
    const r = rotate(rotation, local);
    return [position[0] + r[0], position[1] + r[1], position[2] + r[2]];
  };
  const segment = (width - GAP * 2) / 3;
  const tone = patternForScore(value);
  const x = -width / 2 + (value / 100) * width;
  return (
    <>
      {TONES.map((t, i) => {
        const dim = t === "elevated" ? 0 : 0.42 * emphasis;
        const color = interpolateColors(dim, [0, 1], [PATTERN[t].color, PAPER.paper]);
        return (
          <Extrude
            key={t}
            width={segment}
            height={BAR}
            depth={BAR}
            radius={BAR / 2}
            side={{ color: toHex(color), ambient: 0.7, diffuse: 0.4, fill: 0.15, specular: 0.25, shininess: 18 }}
            position={place([-width / 2 + segment / 2 + i * (segment + GAP), 0, 0])}
            rotation={rotation}
            segments={6}
            front={<div style={{ position: "absolute", inset: 0, background: color }} />}
          />
        );
      })}
      <div
        style={{
          position: "absolute",
          left: -width / 2,
          top: 40,
          width,
          display: "flex",
          opacity: labels,
          transform: extrudeTransform(place([0, 0, BAR / 2]), rotation),
          transformOrigin: `${width / 2}px -40px`,
        }}
      >
        {TONES.map((t) => (
          <div key={t} style={{ flex: 1, textAlign: "center", opacity: t === "elevated" ? 1 : 1 - 0.35 * emphasis }}>
            <div style={display(48, PATTERN[t].ink)}>{PATTERN[t].label}</div>
            <div style={{ ...body(28), marginTop: 12 }}>{PATTERN[t].range}</div>
          </div>
        ))}
      </div>
      <Extrude
        width={56}
        height={56}
        depth={22}
        radius={28}
        side={{ color: "#F3EEE7", ambient: 0.62, diffuse: 0.42, fill: 0.15, specular: 0.4, shininess: 24 }}
        position={place([x, 0, BAR / 2 + 11])}
        rotation={rotation}
        scale={puck}
        segments={6}
        front={<div style={{ position: "absolute", inset: 0, borderRadius: 28, background: "#FFFFFF", boxShadow: `inset 0 0 0 9px ${PATTERN[tone].color}` }} />}
      />
    </>
  );
};

const toHex = (rgb: string) => {
  const m = rgb.match(/\d+/g);
  if (!m) return rgb;
  return "#" + m.slice(0, 3).map((n) => Number(n).toString(16).padStart(2, "0")).join("");
};
