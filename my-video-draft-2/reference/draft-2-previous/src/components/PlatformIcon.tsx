import React from "react";
import { Img, staticFile } from "remotion";

export type Platform = "tiktok" | "instagram" | "facebook";

export const PLATFORM_NAME: Record<Platform, string> = {
  tiktok: "TikTok",
  instagram: "Instagram",
  facebook: "Facebook",
};

// The same app icons REDU shows in its own UI (drawable-nodpi), used for identification only.
export const PlatformIcon: React.FC<{ readonly platform: Platform; readonly size: number; readonly style?: React.CSSProperties }> = ({
  platform,
  size,
  style,
}) => <Img src={staticFile(`icons/${platform}.png`)} style={{ width: size, height: size, flexShrink: 0, ...style }} />;
