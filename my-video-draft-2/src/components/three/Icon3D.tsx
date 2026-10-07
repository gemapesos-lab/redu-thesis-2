import React from "react";
import { COLORS } from "../../brand/tokens";
import { AppIcon } from "../Logo";
import type { MascotExpression } from "../Mascot";
import { Extrude } from "./Extrude";
import type { Material, Vec3 } from "./math";

const TILE: Material = { color: "#24211E", ambient: 0.5, diffuse: 0.55, fill: 0.2, specular: 0.5, shininess: 22 };

// The launcher icon as a thick tile; `sheen` sweeps a highlight across the glass (0–1).
export const Icon3D: React.FC<{
  readonly size: number;
  readonly position?: Vec3;
  readonly rotation?: Vec3;
  readonly scale?: number;
  readonly sheen?: number;
  readonly expression?: MascotExpression;
  readonly blendTo?: MascotExpression;
  readonly blend?: number;
}> = ({ size, position, rotation, scale, sheen = 0, expression, blendTo, blend }) => {
  const sweep = -40 + 180 * sheen;
  return (
    <Extrude
      width={size}
      height={size}
      depth={size * 0.17}
      radius={size * 0.3}
      side={TILE}
      position={position}
      rotation={rotation}
      scale={scale}
      segments={8}
      front={
        <>
          <AppIcon size={size} expression={expression} blendTo={blendTo} blend={blend} style={{ boxShadow: "inset 0 1px 0 rgba(255,255,255,0.08)" }} />
          {sheen > 0 && sheen < 1 ? (
            <div style={{ position: "absolute", inset: 0, background: `linear-gradient(120deg, rgba(255,255,255,0) ${sweep - 18}%, rgba(255,255,255,0.22) ${sweep}%, rgba(255,255,255,0) ${sweep + 18}%)` }} />
          ) : null}
        </>
      }
      back={<div style={{ position: "absolute", inset: 0, background: `radial-gradient(120% 90% at 30% 0%, #2A2725 0%, ${COLORS.launcherTile} 60%)` }} />}
    />
  );
};
