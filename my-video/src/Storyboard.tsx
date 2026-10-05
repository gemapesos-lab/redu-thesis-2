import React from "react";
import { AbsoluteFill, Freeze } from "remotion";
import { COLORS, FONT, PAPER } from "./brand/tokens";
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
import { FPS, SCENES, sceneStart, TOTAL_FRAMES, type SceneId } from "./timeline";

const SCENE_COMPONENTS: Record<SceneId, React.FC> = {
  hook: Hook,
  problem: Problem,
  meet: Meet,
  users: Users,
  signals: Signals,
  score: Score,
  prompts: PromptLevels,
  privacy: Privacy,
  impact: Impact,
  outro: Outro,
  credits: Credits,
};

const THUMB_W = 560;
const THUMB_SCALE = THUMB_W / 1920;

const timecode = (frames: number) => {
  const seconds = frames / FPS;
  const m = Math.floor(seconds / 60);
  const s = seconds - m * 60;
  return `${m}:${s < 10 ? "0" : ""}${s.toFixed(1)}`;
};

const spoken = (voice: string) => voice.replace(/\[\[[^\]]*\]\]\s*/g, "").replace(/Ree-doo/g, "REDU");

const label: React.CSSProperties = {
  fontFamily: FONT,
  fontSize: 13,
  fontWeight: 800,
  letterSpacing: "0.14em",
  textTransform: "uppercase",
  color: PAPER.inkFaint,
};

// A contact sheet of every scene, frozen on its key frame, with the script beneath.
export const Storyboard: React.FC = () => (
  <AbsoluteFill style={{ backgroundColor: "#EFE9E1", fontFamily: FONT, color: PAPER.ink, padding: "48px 60px" }}>
    <div style={{ display: "flex", alignItems: "baseline", justifyContent: "space-between", marginBottom: 30 }}>
      <div style={{ fontSize: 44, fontWeight: 800, letterSpacing: "-0.03em" }}>REDU · AVP storyboard</div>
      <div style={{ fontSize: 20, fontWeight: 600, color: PAPER.inkSoft }}>
        1920×1080 · 30 fps · {timecode(TOTAL_FRAMES)} total · Lauren narration + original score
      </div>
    </div>
    <div style={{ display: "flex", flexWrap: "wrap", gap: "28px 40px" }}>
      {SCENES.map((scene, i) => {
        const Scene = SCENE_COMPONENTS[scene.id];
        const start = sceneStart(scene.id);
        return (
          <div key={scene.id} style={{ width: THUMB_W + 20, display: "flex", flexDirection: "column", gap: 8 }}>
            <div style={{ position: "relative", width: THUMB_W, height: 1080 * THUMB_SCALE, borderRadius: 14, overflow: "hidden", boxShadow: "0 10px 30px rgba(60,42,25,0.18)" }}>
              <div style={{ position: "absolute", left: 0, top: 0, width: 1920, height: 1080, scale: String(THUMB_SCALE), transformOrigin: "0 0" }}>
                <Freeze frame={scene.keyFrame}>
                  <Scene />
                </Freeze>
              </div>
            </div>
            <div style={{ display: "flex", alignItems: "baseline", gap: 10, marginTop: 4 }}>
              <span style={{ fontSize: 22, fontWeight: 800 }}>
                {i + 1 < 10 ? `0${i + 1}` : i + 1} · {scene.title}
              </span>
              <span style={{ fontSize: 15, fontWeight: 700, color: COLORS.action, fontVariantNumeric: "tabular-nums" }}>
                {timecode(start)}–{timecode(start + scene.frames)}
              </span>
            </div>
            <div style={label}>Narration</div>
            <div style={{ fontSize: 16, fontWeight: 600, lineHeight: 1.35 }}>{scene.voice ? `“${spoken(scene.voice)}”` : "— (music only)"}</div>
            <div style={label}>On screen</div>
            <div style={{ fontSize: 15, fontWeight: 500, lineHeight: 1.35, color: PAPER.inkSoft }}>{scene.onScreen}</div>
          </div>
        );
      })}
      <div style={{ width: THUMB_W + 20, display: "flex", flexDirection: "column", gap: 10, fontSize: 16, lineHeight: 1.45, color: PAPER.inkSoft }}>
        <div style={{ fontSize: 22, fontWeight: 800, color: PAPER.ink }}>Look & sound</div>
        <div>
          Dark cold open → one breath blooms into REDU’s warm paper. Manrope ExtraBold kinetic type with word-by-word blur reveals; real
          app captures in a phone frame; one idea per beat.
        </div>
        <div style={{ display: "flex", gap: 10 }}>
          {[PAPER.dark, PAPER.paper, PAPER.ink, COLORS.action, COLORS.sage, COLORS.honey, COLORS.coral].map((c) => (
            <div key={c} style={{ width: 44, height: 44, borderRadius: 12, background: c, boxShadow: "inset 0 0 0 1px rgba(0,0,0,0.12)" }} />
          ))}
        </div>
        <div>
          Audio: narration at −16 LUFS; original synthesized score ducked ~8 dB under it; swipe, tick, breath and hit effects on the
          cuts. Every line is also on screen, so it reads with sound off.
        </div>
      </div>
    </div>
  </AbsoluteFill>
);
