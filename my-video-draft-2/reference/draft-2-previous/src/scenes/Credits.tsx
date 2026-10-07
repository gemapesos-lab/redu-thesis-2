import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { PAPER } from "../brand/tokens";
import { body, display, eyebrow } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { ramp, Reveal } from "../components/Kinetic";
import { AppIcon, Wordmark } from "../components/Logo";
import { sceneStart, VOICEOVER } from "../timeline";

const DEVELOPERS = ["Genesis A. Cadigal", "Rayan Kennard O. Chuayap", "Luigi Karl B. Limos", "Gean Dhylan E. Mapesos"];

const THIRD_PARTY = [
  "Built with VADER sentiment analysis (Hutto & Gilbert, 2014; MIT License), Moondream 0.5B (Apache License 2.0) and llama.cpp (MIT License).",
  "Manrope typeface (SIL Open Font License 1.1) · Lucide icons (ISC License) · Mascot motion adapted from blobatar by Alain (MIT License).",
  `Original music and sound effects created for this video · ${VOICEOVER.credit} · Made with Remotion.`,
  "App screens are captures of REDU on Android; the reminder and breathing animations are rebuilt from the app's source code.",
  "TikTok, Instagram and Facebook are trademarks of their respective owners, shown for identification only.",
];

export const Credits: React.FC = () => {
  const frame = useCurrentFrame();
  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("credits")} />
      <div style={{ position: "absolute", inset: 0, display: "flex", flexDirection: "column", alignItems: "center", textAlign: "center", paddingTop: 62 }}>
        <Reveal at={-30} style={{ display: "flex", alignItems: "center", gap: 22 }}>
          <AppIcon size={78} />
          <Wordmark size={64} />
        </Reveal>
        <Reveal at={-30} style={{ ...body(30, PAPER.ink, 700), maxWidth: 1240, marginTop: 30, lineHeight: 1.35 }}>
          Doomscrolling Detection and Digital Mindfulness Mobile Application for Short-Form Video Platforms Using VADER and Fuzzy Logic
        </Reveal>

        <Reveal at={-30} style={{ ...eyebrow(PAPER.accent, 22), marginTop: 46 }}>
          Developed by
        </Reveal>
        <div style={{ display: "flex", flexWrap: "wrap", justifyContent: "center", width: 1300, columnGap: 64, rowGap: 6, marginTop: 14 }}>
          {DEVELOPERS.map((name) => (
            <Reveal key={name} at={-30} style={{ ...display(40), letterSpacing: "-0.02em", width: 600 }}>
              {name}
            </Reveal>
          ))}
        </div>

        <Reveal at={-30} style={{ ...eyebrow(PAPER.accent, 22), marginTop: 38 }}>
          Thesis adviser
        </Reveal>
        <Reveal at={-30} style={{ ...display(38), letterSpacing: "-0.02em", marginTop: 12 }}>
          Ms. Marilyn M. Sanchez
        </Reveal>
        <Reveal at={-30} style={{ ...body(26), marginTop: 30 }}>
          Bachelor of Science in Computer Science with Specialization in Software Engineering
          <br />
          FEU Institute of Technology · June 2026
        </Reveal>

        <Reveal at={-30} style={{ width: 1500, height: 1.5, background: PAPER.hairline, marginTop: 34 }}>
          {null}
        </Reveal>
        <Reveal at={-30} style={{ ...body(21, PAPER.inkSoft, 500), maxWidth: 1560, marginTop: 26, lineHeight: 1.6 }}>
          {THIRD_PARTY.map((line) => (
            <div key={line}>{line}</div>
          ))}
        </Reveal>
      </div>
      <AbsoluteFill style={{ background: PAPER.dark, opacity: ramp(frame, 134, 15) }} />
    </AbsoluteFill>
  );
};
