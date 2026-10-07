import React from "react";
import { AbsoluteFill, Freeze, useCurrentFrame, useVideoConfig } from "remotion";

// Average complete, opaque scene samples with normal alpha compositing.
// Layer i uses 1/(i+1) opacity, giving all samples equal final weight without
// additive blending's color shifts or experimental canvas capture blanks.
// Children read fractional frames so the phone, its feed and bezel stay together.
const SAMPLES = 8;
const SHUTTER_FRAMES = 0.5;

export const MotionBlur: React.FC<{
  readonly children: React.ReactNode;
  readonly windows: ReadonlyArray<readonly [number, number]>;
}> = ({ children, windows }) => {
  const frame = useCurrentFrame();
  const { durationInFrames } = useVideoConfig();
  const active = windows.some(([from, to]) => frame >= from && frame < to);
  if (!active) return <>{children}</>;
  return <AbsoluteFill style={{ isolation: "isolate" }}>
    {Array.from({ length: SAMPLES }, (_, i) => {
      const sample = Math.max(0, Math.min(durationInFrames - 1, frame + ((i + 0.5) / SAMPLES - 0.5) * SHUTTER_FRAMES));
      return <AbsoluteFill key={i} style={{ opacity: 1 / (i + 1) }}>
        <Freeze frame={sample}>{children}</Freeze>
      </AbsoluteFill>;
    })}
  </AbsoluteFill>;
};
