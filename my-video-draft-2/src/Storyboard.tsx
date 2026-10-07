import React from "react";
import { AbsoluteFill, Freeze } from "remotion";
import { FONT, PAPER } from "./brand/tokens";
import { Credits } from "./scenes/Credits";
import { Hook } from "./scenes/Hook";
import { Impact } from "./scenes/Impact";
import { Meet } from "./scenes/Meet";
import { Outro } from "./scenes/Outro";
import { Privacy } from "./scenes/Privacy";
import { Problem } from "./scenes/Problem";
import { PromptLevels } from "./scenes/PromptLevels";
import { Score } from "./scenes/Score";
import { Signals } from "./scenes/Signals";
import { Users } from "./scenes/Users";
import { FPS, PANELS, type SceneId } from "./timeline";

const COMPONENTS: Record<SceneId, React.FC> = { hook: Hook, problem: Problem, meet: Meet, users: Users, signals: Signals, score: Score, prompts: PromptLevels, privacy: Privacy, impact: Impact, outro: Outro, credits: Credits };
const THUMB_W = 580;
const time = (frame: number) => (frame / FPS).toFixed(2).padStart(5, "0");
const smallLabel: React.CSSProperties = { fontSize: 13, fontWeight: 800, letterSpacing: "0.12em", textTransform: "uppercase", color: PAPER.inkSoft };

export const Storyboard: React.FC = () => (
  <AbsoluteFill style={{ background: "#EFE9E1", fontFamily: FONT, color: PAPER.ink, padding: "48px 60px" }}>
    <div style={{ display: "flex", justifyContent: "space-between", alignItems: "baseline" }}>
      <div style={{ fontSize: 48, fontWeight: 800, letterSpacing: "-0.035em" }}>REDU · Draft 2 · 3D polish storyboard</div>
      <div style={{ fontSize: 26, fontWeight: 800, color: PAPER.accent }}>59.00 s target · 60.000 s absolute limit</div>
    </div>
    <div style={{ fontSize: 22, color: PAPER.inkSoft, marginTop: 12, marginBottom: 32 }}>
      1920 × 1080 · 30 fps · 1,770 frames · 12 panels · 3D-polish take aligned locally · Every scene rebuilt with 3D objects and a camera
    </div>
    <div style={{ display: "grid", gridTemplateColumns: "repeat(4, 580px)", gridAutoRows: 640, gap: "28px 40px" }}>
      {PANELS.map((panel) => {
        const Scene = COMPONENTS[panel.scene];
        return <div key={panel.label} style={{ display: "flex", flexDirection: "column", gap: 9 }}>
          <div style={{ position: "relative", width: THUMB_W, height: THUMB_W * 1080 / 1920, borderRadius: 16, overflow: "hidden", boxShadow: "0 10px 30px rgba(60,42,25,0.15)" }}>
            <div style={{ position: "absolute", width: 1920, height: 1080, scale: String(THUMB_W / 1920), transformOrigin: "0 0" }}>
              <Freeze frame={panel.keyFrame}><Scene /></Freeze>
            </div>
          </div>
          <div style={{ fontSize: 24, fontWeight: 800, letterSpacing: "-0.025em", marginTop: 4 }}>{panel.label} · {panel.title}</div>
          <div style={{ fontSize: 19, fontWeight: 800, color: PAPER.accent, fontVariantNumeric: "tabular-nums" }}>00:{time(panel.from)}–00:{time(panel.to)}</div>
          <div style={smallLabel}>Recorded VO</div>
          <div style={{ fontSize: 18, lineHeight: 1.4, fontWeight: 600 }}>{panel.voice ? "“" + panel.voice + "”" : "Music only · five-second credit hold"}</div>
          <div style={smallLabel}>Action / sync direction</div>
          <div style={{ fontSize: 17, lineHeight: 1.4, color: PAPER.inkSoft }}>{panel.direction}</div>
        </div>;
      })}
    </div>
    <div style={{ display: "flex", gap: 65, borderTop: "2px solid rgba(26,21,18,0.12)", marginTop: 30, paddingTop: 24 }}>
      <div style={{ flex: 1, fontSize: 22, lineHeight: 1.45 }}><strong>Real depth and a camera.</strong> Solid phones, clock, icon, tiles and meter; scenes hand off by orbit, rack focus and dolly.</div>
      <div style={{ flex: 1, fontSize: 22, lineHeight: 1.45 }}><strong>Whole phrases, no overlaps.</strong> Each line appears whole on its spoken cue, and a text block is gone before the next takes its space.</div>
      <div style={{ flex: 1, fontSize: 22, lineHeight: 1.45 }}><strong>59 seconds including credits.</strong> Original speaking speed; 0.9 s of silence added inside two pauses. Credits run 54–59.</div>
    </div>
  </AbsoluteFill>
);
