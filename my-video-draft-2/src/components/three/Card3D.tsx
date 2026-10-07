import React from "react";
import { Extrude } from "./Extrude";
import type { Material, Vec3 } from "./math";

const PAPER_EDGE: Material = { color: "#EDE6DC", ambient: 0.72, diffuse: 0.32, fill: 0.12, specular: 0.12, shininess: 12 };

// A white card with a little thickness, for signal, stat and outcome cards.
export const Card3D: React.FC<{
  readonly width: number;
  readonly height: number;
  readonly depth?: number;
  readonly radius?: number;
  readonly position?: Vec3;
  readonly rotation?: Vec3;
  readonly scale?: number;
  readonly background?: string;
  readonly edge?: Material;
  readonly children: React.ReactNode;
}> = ({ width, height, depth = 14, radius = 30, position, rotation, scale, background = "#FFFFFF", edge = PAPER_EDGE, children }) => (
  <Extrude
    width={width}
    height={height}
    depth={depth}
    radius={radius}
    side={edge}
    position={position}
    rotation={rotation}
    scale={scale}
    segments={4}
    front={<div style={{ position: "absolute", inset: 0, background }}>{children}</div>}
    back={<div style={{ position: "absolute", inset: 0, background: "#F4EEE6" }} />}
  />
);

// Soft paper shadow for a stage holding cards.
export const PAPER_SHADOW: React.CSSProperties = { filter: "drop-shadow(0 26px 34px rgba(60,42,25,0.13)) drop-shadow(0 6px 10px rgba(60,42,25,0.08))" };
