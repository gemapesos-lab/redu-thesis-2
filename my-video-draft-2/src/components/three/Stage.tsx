import React, { createContext, useContext } from "react";
import { AbsoluteFill } from "remotion";
import { cameraTransform, PERSPECTIVE, type Camera } from "./math";

const CameraContext = createContext<Camera>({});
export const useCamera = () => useContext(CameraContext);

// A full-frame perspective stage whose world origin is the center of frame.
// Give each object its own stage with the scene camera: blur and opacity then
// apply to the projected image here, never inside the 3D context, which would flatten it.
// Clipping to the frame keeps blur and rasterization to what is visible.
export const Stage: React.FC<{
  readonly camera?: Camera;
  readonly style?: React.CSSProperties;
  readonly children: React.ReactNode;
}> = ({ camera = {}, style, children }) => (
  <AbsoluteFill style={{ perspective: camera.perspective ?? PERSPECTIVE, overflow: "hidden", ...style }}>
    <div style={{ position: "absolute", left: "50%", top: "50%", width: 0, height: 0, transformStyle: "preserve-3d", transform: cameraTransform(camera) }}>
      <CameraContext.Provider value={camera}>{children}</CameraContext.Provider>
    </div>
  </AbsoluteFill>
);

// Depth-of-field and fade for a stage, kept off the 3D context itself.
export const lens = (blur = 0, opacity = 1): React.CSSProperties => ({
  ...(blur > 0.05 ? { filter: `blur(${blur}px)` } : {}),
  ...(opacity < 1 ? { opacity } : {}),
});

// A sized box in normal layout that hosts a 3D object at its center, e.g. the icon in a lockup.
export const Inline3D: React.FC<{
  readonly width: number;
  readonly height: number;
  readonly style?: React.CSSProperties;
  readonly children: React.ReactNode;
}> = ({ width, height, style, children }) => (
  <div style={{ position: "relative", width, height, flexShrink: 0, perspective: PERSPECTIVE, ...style }}>
    <div style={{ position: "absolute", left: width / 2, top: height / 2, width: 0, height: 0, transformStyle: "preserve-3d" }}>
      <CameraContext.Provider value={{}}>{children}</CameraContext.Provider>
    </div>
  </div>
);
