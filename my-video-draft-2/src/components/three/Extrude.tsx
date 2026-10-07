import React from "react";
import { rotate, shade, viewDirection, type Material, type Vec3 } from "./math";
import { useCamera } from "./Stage";

type Wall = { readonly x: number; readonly y: number; readonly angle: number; readonly length: number };

// Outward-facing strips around a rounded rectangle: four straight sides and
// `segments` strips per corner. Angles follow CSS rotateZ (0 = right, 90 = down).
const walls = (width: number, height: number, radius: number, segments: number): Wall[] => {
  const r = Math.min(radius, width / 2, height / 2);
  const hx = width / 2 - r;
  const hy = height / 2 - r;
  const out: Wall[] = [];
  const corners: ReadonlyArray<readonly [number, number, number]> = [
    [hx, -hy, -90],
    [hx, hy, 0],
    [-hx, hy, 90],
    [-hx, -hy, 180],
  ];
  const sides: ReadonlyArray<readonly [number, number, number, number]> = [
    [width / 2, 0, 0, 2 * hy],
    [0, height / 2, 90, 2 * hx],
    [-width / 2, 0, 180, 2 * hy],
    [0, -height / 2, 270, 2 * hx],
  ];
  for (const [x, y, angle, length] of sides) {
    if (length > 0.5) out.push({ x, y, angle, length: length + 1 });
  }
  if (r > 0.5) {
    const step = 90 / segments;
    const chord = 2 * r * Math.sin((step * Math.PI) / 360) + 1.2;
    for (const [cx, cy, start] of corners) {
      for (let i = 0; i < segments; i++) {
        const angle = start + step * (i + 0.5);
        const a = (angle * Math.PI) / 180;
        out.push({ x: cx + r * Math.cos(a), y: cy + r * Math.sin(a), angle, length: chord });
      }
    }
  }
  return out;
};

export type ExtrudeProps = {
  readonly width: number;
  readonly height: number;
  readonly depth: number;
  readonly radius: number;
  readonly side: Material;
  readonly front?: React.ReactNode;
  readonly back?: React.ReactNode;
  // Placement in its parent's space: translate3d, then rotateX/Y/Z in degrees, then scale.
  readonly position?: Vec3;
  readonly rotation?: Vec3;
  readonly scale?: number;
  readonly segments?: number;
  // Extra object-space content, e.g. buttons on a wall.
  readonly children?: React.ReactNode;
};

export const extrudeTransform = ([x, y, z]: Vec3, [rx, ry, rz]: Vec3, scale = 1) =>
  `translate3d(${x}px, ${y}px, ${z}px) rotateX(${rx}deg) rotateY(${ry}deg) rotateZ(${rz}deg)${scale === 1 ? "" : ` scale3d(${scale}, ${scale}, ${scale})`}`;

// A rounded slab with lit side walls, a front face and a back face.
export const Extrude: React.FC<ExtrudeProps> = ({
  width,
  height,
  depth,
  radius,
  side,
  front,
  back,
  position = [0, 0, 0],
  rotation = [0, 0, 0],
  scale = 1,
  segments = 6,
  children,
}) => {
  const view = viewDirection(useCamera());
  const face: React.CSSProperties = {
    position: "absolute",
    left: -width / 2,
    top: -height / 2,
    width,
    height,
    borderRadius: radius,
    overflow: "hidden",
    backfaceVisibility: "hidden",
  };
  return (
    <div style={{ position: "absolute", left: 0, top: 0, width: 0, height: 0, transformStyle: "preserve-3d", transform: extrudeTransform(position, rotation, scale) }}>
      {walls(width, height, radius, segments).map((wall, i) => {
        const a = (wall.angle * Math.PI) / 180;
        return (
          <div
            key={i}
            style={{
              position: "absolute",
              left: -depth / 2,
              top: -wall.length / 2,
              width: depth,
              height: wall.length,
              background: shade(side, rotate(rotation, [Math.cos(a), Math.sin(a), 0]), view),
              transform: `translate3d(${wall.x}px, ${wall.y}px, 0) rotateZ(${wall.angle}deg) rotateY(90deg)`,
              backfaceVisibility: "hidden",
            }}
          />
        );
      })}
      <div style={{ ...face, background: shade(side, rotate(rotation, [0, 0, -1]), view), transform: `translateZ(${-depth / 2}px) rotateY(180deg)` }}>{back}</div>
      <div style={{ ...face, transform: `translateZ(${depth / 2}px)` }}>{front}</div>
      {children}
    </div>
  );
};
