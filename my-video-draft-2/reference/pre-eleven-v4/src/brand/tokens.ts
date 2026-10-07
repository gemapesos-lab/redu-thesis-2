import { loadFont } from "@remotion/google-fonts/Manrope";

// Keep in sync with ReduPalette in the Android app so the video matches the shipped UI.
export const COLORS = {
  background: "#0E0C0B",
  surfaceLowest: "#141210",
  surfaceLow: "#1C1916",
  surface: "#24201C",
  surfaceHigh: "#2C2824",
  surfaceHighest: "#36312C",
  action: "#6382C5",
  onAction: "#0E1624",
  sage: "#8FB59A",
  sageContainer: "#1E3328",
  onSageContainer: "#D5EDE0",
  figure: "#F4EDE4",
  figureInk: "#2A2420",
  honey: "#E3B76F",
  honeyContainer: "#3D2F17",
  onHoneyContainer: "#FFDEA3",
  coral: "#E5968C",
  coralContainer: "#42211E",
  onCoralContainer: "#FFDAD5",
  textPrimary: "#FFFFFF",
  textSecondary: "#C4B5A8",
  outline: "#8A7E74",
  outlineVariant: "#3A342F",
  night: "#020710",
  launcherTile: "#0A0A0A",
} as const;

// Video-only light palette: the mascot's cream as paper, its eye ink as type.
export const PAPER = {
  paper: "#F7F2EB",
  card: "#FFFFFF",
  ink: "#1A1512",
  inkSoft: "#6E645B",
  inkFaint: "#A0968C",
  hairline: "rgba(26,21,18,0.10)",
  accent: "#5672B8",
  sageInk: "#4E7A5C",
  honeyInk: "#9A6E22",
  coralInk: "#B65A4E",
  dark: "#0B0908",
} as const;

export type PatternTone = "low" | "elevated" | "high";

export const PATTERN = {
  low: {
    label: "Low",
    range: "0–33",
    color: COLORS.sage,
    container: COLORS.sageContainer,
    onContainer: COLORS.onSageContainer,
    ink: PAPER.sageInk,
  },
  elevated: {
    label: "Elevated",
    range: "34–66",
    color: COLORS.honey,
    container: COLORS.honeyContainer,
    onContainer: COLORS.onHoneyContainer,
    ink: PAPER.honeyInk,
  },
  high: {
    label: "High",
    range: "67–100",
    color: COLORS.coral,
    container: COLORS.coralContainer,
    onContainer: COLORS.onCoralContainer,
    ink: PAPER.coralInk,
  },
} as const;

export const patternForScore = (score: number): PatternTone =>
  score < 33.33 ? "low" : score < 66.67 ? "elevated" : "high";

export const { fontFamily: FONT } = loadFont("normal", {
  weights: ["400", "500", "600", "700", "800"],
  subsets: ["latin"],
});

export const VIDEO = {
  width: 1920,
  height: 1080,
  fps: 30,
} as const;
