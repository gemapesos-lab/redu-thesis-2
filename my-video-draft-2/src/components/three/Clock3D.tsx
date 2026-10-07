import React from "react";
import { FONT } from "../../brand/tokens";
import { Extrude } from "./Extrude";
import { rotate, type Material, type Vec3 } from "./math";

const SEGMENTS = [
  "18,5 78,5 87,14 78,23 18,23 9,14",
  "82,27 91,18 100,27 100,91 91,100 82,91",
  "82,109 91,100 100,109 100,173 91,182 82,173",
  "18,177 78,177 87,186 78,195 18,195 9,186",
  "0,109 9,100 18,109 18,173 9,182 0,173",
  "0,27 9,18 18,27 18,91 9,100 0,91",
  "18,91 78,91 87,100 78,109 18,109 9,100",
];
const DIGITS: Record<string, readonly number[]> = { "2": [0, 1, 6, 4, 3], "0": [0, 1, 2, 3, 4, 5], "6": [0, 2, 3, 4, 5, 6], "7": [0, 1, 2] };
// The red of a bedside LED clock: the warning colour of the hour.
const LED = "#FF3322";
const GHOST = "rgba(255,60,40,0.075)";

export const CLOCK = { width: 1200, height: 520, depth: 420, radius: 110 } as const;
const PLASTIC: Material = { color: "#1B1A1C", ambient: 0.5, diffuse: 0.62, fill: 0.2, specular: 0.28, shininess: 16 };
const SNOOZE: Material = { color: "#2A292C", ambient: 0.5, diffuse: 0.65, fill: 0.2, specular: 0.4, shininess: 22 };

// 6 → 7: segment b lights, then g, f, e and d go out, each with a short flicker.
const FLIP: ReadonlyArray<readonly [number, number, ReadonlyArray<number>]> = [
  [1, 0, [0, 0.6, 0.25, 1]],
  [6, 1, [1, 0.35, 0.7, 0]],
  [5, 1, [1, 0.3, 0.6, 0]],
  [4, 2, [1, 0.4, 0.65, 0]],
  [3, 2, [1, 0.3, 0.5, 0]],
];

const minuteLevels = (sinceFlip: number) => {
  const levels: number[] = SEGMENTS.map((_, i) => (DIGITS["6"].includes(i) ? 1 : 0));
  for (const [segment, delay, steps] of FLIP) {
    const t = Math.floor(sinceFlip) - delay;
    if (t >= 0) levels[segment] = steps[Math.min(t, steps.length - 1)];
  }
  return levels;
};

// The front: matte bezel around a recessed, smoked-red acrylic lens with slanted LED digits.
export const ClockFace: React.FC<{ readonly sinceFlip: number; readonly glow: number }> = ({ sinceFlip, glow }) => {
  const minute = minuteLevels(sinceFlip);
  const digits: ReadonlyArray<readonly [string, number, number[] | undefined]> = [["2", 0, undefined], ["0", 164, undefined], ["6", 280, minute]];
  const level = (d: string, i: number, levels?: number[]) => (levels ? levels[i] : DIGITS[d].includes(i) ? 1 : 0);
  const segments = (lit: boolean) =>
    digits.map(([d, x, levels]) => (
      <g key={x} transform={`translate(${x} 0)`}>
        {SEGMENTS.map((points, i) => {
          const l = level(d, i, levels);
          return lit ? (l > 0.02 ? <polygon key={i} points={points} fill={LED} opacity={l} /> : null) : <polygon key={i} points={points} fill={GHOST} />;
        })}
      </g>
    ));
  return (
    <div style={{ position: "absolute", inset: 0, background: "linear-gradient(180deg, #262528 0%, #161517 55%, #0F0E10 100%)", boxShadow: "inset 0 3px 0 rgba(255,255,255,0.07), inset 0 -2px 0 rgba(0,0,0,0.6)" }}>
      <div
        style={{
          position: "absolute",
          left: 58,
          right: 58,
          top: 56,
          bottom: 56,
          borderRadius: 70,
          overflow: "hidden",
          background: "radial-gradient(ellipse 70% 80% at 50% 50%, #1E0605 0%, #120303 60%, #080202 100%)",
          boxShadow: "inset 0 14px 34px rgba(0,0,0,0.95), inset 0 -2px 0 rgba(255,255,255,0.06), 0 1px 0 rgba(255,255,255,0.05)",
          display: "grid",
          placeItems: "center",
        }}
      >
        <svg width={900} height={333} viewBox="0 0 540 200" role="img" aria-label="LED clock reading 2:07 AM" style={{ overflow: "visible" }}>
          <g transform="translate(18 0) skewX(-6)">
            {segments(false)}
            <g style={{ filter: `drop-shadow(0 0 ${3 + 3 * glow}px rgba(255,60,40,0.9)) drop-shadow(0 0 ${12 + 16 * glow}px rgba(255,40,20,${0.45 + 0.35 * glow}))` }}>
              {segments(true)}
              <rect x={126} y={56} width={17} height={17} rx={3} fill={LED} />
              <rect x={126} y={124} width={17} height={17} rx={3} fill={LED} />
              <text x={420} y={56} fill={LED} fontFamily={FONT} fontSize={30} fontWeight={800} letterSpacing={2}>
                AM
              </text>
            </g>
            <circle cx={436} cy={176} r={7} fill={GHOST} />
          </g>
        </svg>
        <div style={{ position: "absolute", inset: 0, background: "linear-gradient(160deg, rgba(255,255,255,0.10) 0%, rgba(255,255,255,0.03) 28%, rgba(255,255,255,0) 42%), linear-gradient(0deg, rgba(255,255,255,0.025), rgba(255,255,255,0) 30%)" }} />
      </div>
    </div>
  );
};

// A bedside alarm clock: deep rounded body, snooze bar on top, red LED face,
// standing on a nightstand that catches its red light.
export const Clock3D: React.FC<{
  readonly position?: Vec3;
  readonly rotation?: Vec3;
  readonly scale?: number;
  readonly sinceFlip?: number;
  readonly glow?: number;
}> = ({ position = [0, 0, 0], rotation = [0, 0, 0], scale = 1, sinceFlip = -1, glow = 0 }) => {
  const { width, height, depth, radius } = CLOCK;
  return (
    <div style={{ position: "absolute", transformStyle: "preserve-3d", transform: `translate3d(${position[0]}px, ${position[1]}px, ${position[2]}px) scale3d(${scale}, ${scale}, ${scale})` }}>
      <div
        style={{
          position: "absolute",
          left: -1500,
          top: -900,
          width: 3000,
          height: 1800,
          background: `radial-gradient(900px 520px at 50% 64%, rgba(255,50,30,${0.2 + 0.2 * glow}), rgba(255,50,30,0) 70%), radial-gradient(1400px 800px at 50% 50%, #120D0C, #070606 70%)`,
          transform: `translate3d(0, ${height / 2 + 2}px, 0) rotateX(90deg)`,
        }}
      />
      <Extrude width={width} height={height} depth={depth} radius={radius} side={PLASTIC} rotation={rotation} segments={8} front={<ClockFace sinceFlip={sinceFlip} glow={glow} />}>
        <div
          style={{
            position: "absolute",
            left: -width * 0.9,
            top: -height,
            width: width * 1.8,
            height: height * 2,
            background: `radial-gradient(closest-side, rgba(255,50,30,${0.08 + 0.14 * glow}), rgba(255,50,30,0))`,
            transform: `translateZ(${depth / 2 + 6}px)`,
            pointerEvents: "none",
          }}
        />
      </Extrude>
      <Extrude
        width={width * 0.5}
        height={44}
        depth={depth * 0.38}
        radius={22}
        side={SNOOZE}
        rotation={rotation}
        position={rotate(rotation, [0, -height / 2 - 20, -depth * 0.12])}
        segments={4}
        front={<div style={{ position: "absolute", inset: 0, background: "linear-gradient(180deg, #3A383C, #232225)" }} />}
      />
    </div>
  );
};
