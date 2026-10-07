import type { CSSProperties } from "react";
import { FONT, PAPER } from "./tokens";

export const display = (size: number, color: string = PAPER.ink): CSSProperties => ({
  fontFamily: FONT,
  fontSize: size,
  fontWeight: 800,
  lineHeight: 1.04,
  letterSpacing: "-0.035em",
  color,
});

export const eyebrow = (color: string = PAPER.accent, size = 28): CSSProperties => ({
  fontFamily: FONT,
  fontSize: size,
  fontWeight: 700,
  letterSpacing: "0.18em",
  textTransform: "uppercase",
  color,
});

export const body = (size: number, color: string = PAPER.inkSoft, weight = 600): CSSProperties => ({
  fontFamily: FONT,
  fontSize: size,
  fontWeight: weight,
  lineHeight: 1.3,
  letterSpacing: "-0.01em",
  color,
});

export const card: CSSProperties = {
  background: PAPER.card,
  borderRadius: 30,
  boxShadow: "0 24px 60px rgba(60,42,25,0.12), 0 4px 14px rgba(60,42,25,0.08)",
};

export const centered: CSSProperties = {
  justifyContent: "center",
  alignItems: "center",
  textAlign: "center",
};
