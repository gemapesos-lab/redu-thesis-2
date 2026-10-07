import React from "react";
import { Phone, PHONE_H, PHONE_RADIUS, PHONE_W } from "../Phone";
import { Extrude } from "./Extrude";
import { rotate, shade, viewDirection, type Material, type Vec3 } from "./math";
import { useCamera } from "./Stage";

// Galaxy A35-like proportions: 8.2 mm thick on a 78 mm wide body.
const DEPTH = 0.085;
const TITANIUM: Material = { color: "#4A443E", ambient: 0.42, diffuse: 0.62, fill: 0.22, specular: 0.55, shininess: 16 };
const BUTTON: Material = { ...TITANIUM, color: "#57504A" };

type Phone3DProps = {
  readonly width: number;
  readonly position?: Vec3;
  readonly rotation?: Vec3;
  readonly scale?: number;
  readonly screenshot?: string;
  readonly statusBar?: boolean;
  readonly time?: string;
  readonly handle?: boolean;
  // Screen content at SCREEN_W x SCREEN_H dp, as for <Phone>.
  readonly children?: React.ReactNode;
};

const Back: React.FC<{ readonly k: number }> = ({ k }) => (
  <div style={{ position: "absolute", inset: 0, background: "linear-gradient(160deg, #2A2623 0%, #121010 48%, #0B0A09 70%, #1E1B19 100%)" }}>
    <div style={{ position: "absolute", left: 26 * k, top: 26 * k, width: 92 * k, height: 196 * k, borderRadius: 46 * k, background: "rgba(255,255,255,0.035)", boxShadow: "inset 0 0 0 1px rgba(255,255,255,0.08)" }} />
    {[0, 1, 2].map((i) => (
      <div
        key={i}
        style={{
          position: "absolute",
          left: 42 * k,
          top: (42 + i * 56) * k,
          width: 60 * k,
          height: 60 * k,
          borderRadius: 30 * k,
          background: "radial-gradient(circle at 40% 38%, #3A4150 0%, #0A0B0E 46%, #000 70%)",
          boxShadow: `0 0 0 ${3.5 * k}px #2B2724, 0 0 0 ${4.5 * k}px rgba(255,255,255,0.12)`,
        }}
      />
    ))}
    <div style={{ position: "absolute", left: 132 * k, top: 52 * k, width: 16 * k, height: 16 * k, borderRadius: 8 * k, background: "#E8DCC8", opacity: 0.55 }} />
  </div>
);

// The app's phone as a solid object: titanium walls, glass back with cameras,
// side keys, and a soft glare that slides across the screen as it turns.
export const Phone3D: React.FC<Phone3DProps> = ({ width, position, rotation = [0, 0, 0], scale, children, ...phone }) => {
  const k = width / PHONE_W;
  const height = PHONE_H * k;
  const depth = width * DEPTH;
  const view = viewDirection(useCamera());
  const keyColor = shade(BUTTON, rotate(rotation, [1, 0, 0]), view);
  const glare = 50 + rotation[1] * 1.4 - rotation[0] * 0.6;
  return (
    <Extrude
      width={width}
      height={height}
      depth={depth}
      radius={PHONE_RADIUS * k}
      side={TITANIUM}
      position={position}
      rotation={rotation}
      scale={scale}
      segments={7}
      front={
        <>
          <Phone width={width} three {...phone}>
            {children}
          </Phone>
          <div
            style={{
              position: "absolute",
              inset: 0,
              pointerEvents: "none",
              background: `linear-gradient(118deg, rgba(255,255,255,0) ${glare - 16}%, rgba(255,255,255,0.075) ${glare}%, rgba(255,255,255,0) ${glare + 13}%)`,
            }}
          />
        </>
      }
      back={<Back k={k} />}
    >
      {[[150, 64], [236, 40]].map(([top, length]) => (
        <div
          key={top}
          style={{
            position: "absolute",
            left: -depth * 0.22,
            top: -height / 2 + (top + length / 2) * k - (length * k) / 2,
            width: depth * 0.44,
            height: length * k,
            borderRadius: depth * 0.22,
            background: keyColor,
            transform: `translate3d(${width / 2 + 1.6 * k}px, 0, 0) rotateY(90deg)`,
            backfaceVisibility: "hidden",
          }}
        />
      ))}
    </Extrude>
  );
};
