import { Audio } from "@remotion/media";
import React from "react";
import { AbsoluteFill, interpolate, Series, staticFile } from "remotion";
import { PAPER } from "./brand/tokens";
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
import { BEATS, SCENES, TOTAL_FRAMES, sceneById, sceneStart, VOICEOVER, type Cue } from "./timeline";

// Scene lengths live in timeline.ts because scripts/music.mjs scores to the same cue points.
const len = (id: Parameters<typeof sceneById>[0]) => sceneById(id).frames;

const CUE_VOLUME: Record<Cue, number> = {
  swipe: 0.55,
  whoosh: 0.4,
  tick: 0.3,
  breath: 0.7,
  hit: 0.75,
};

const VO_SPANS = SCENES.filter((scene) => scene.voice).flatMap((scene) => {
  const start = sceneStart(scene.id) + (scene.voAt ?? 0);
  const end = start + (scene.voFrames ?? 0);
  if (scene.id === "hook") return [[start, BEATS.hook.scrollBreak[0]], [BEATS.hook.scrollBreak[1], end]] as const;
  return [[start, end]] as const;
});

// The score sits about 8 dB under the narration and comes back up between lines.
const musicVolume = (frame: number) => {
  let duck = 0;
  for (const [start, end] of VO_SPANS) {
    duck = Math.max(duck, interpolate(frame, [start - 10, start, end, end + 20], [0, 1, 1, 0], { extrapolateLeft: "clamp", extrapolateRight: "clamp" }));
  }
  return 1 - 0.6 * duck;
};

export const AVP: React.FC = () => (
  <AbsoluteFill style={{ backgroundColor: PAPER.dark }}>
    <Series>
      <Series.Sequence name="Just one more" durationInFrames={len("hook")} premountFor={30}>
        <Hook />
      </Series.Sequence>
      <Series.Sequence name="The problem" durationInFrames={len("problem")} premountFor={30}>
        <Problem />
      </Series.Sequence>
      <Series.Sequence name="Meet REDU" durationInFrames={len("meet")} premountFor={30}>
        <Meet />
      </Series.Sequence>
      <Series.Sequence name="Who it's for" durationInFrames={len("users")} premountFor={30}>
        <Users />
      </Series.Sequence>
      <Series.Sequence name="Three signals" durationInFrames={len("signals")} premountFor={30}>
        <Signals />
      </Series.Sequence>
      <Series.Sequence name="Doomscrolling risk score" durationInFrames={len("score")} premountFor={30}>
        <Score />
      </Series.Sequence>
      <Series.Sequence name="Gentle prompts" durationInFrames={len("prompts")} premountFor={30}>
        <PromptLevels />
      </Series.Sequence>
      <Series.Sequence name="Private by design" durationInFrames={len("privacy")} premountFor={30}>
        <Privacy />
      </Series.Sequence>
      <Series.Sequence name="Pilot results" durationInFrames={len("impact")} premountFor={30}>
        <Impact />
      </Series.Sequence>
      <Series.Sequence name="Logo & tagline" durationInFrames={len("outro")} premountFor={30}>
        <Outro />
      </Series.Sequence>
      <Series.Sequence name="Credits" durationInFrames={len("credits")} premountFor={30}>
        <Credits />
      </Series.Sequence>
    </Series>

    <Audio name="Music" src={staticFile("audio/music.mp3")} volume={musicVolume} durationInFrames={TOTAL_FRAMES} premountFor={30} />
    <Audio name="Voiceover · Lauren (ElevenLabs v4)" src={staticFile(VOICEOVER.audio)} durationInFrames={VOICEOVER.frames} premountFor={30} />
    {SCENES.map((scene) =>
      scene.cues.map(([frame, cue]) => (
        <Audio
          key={`${scene.id}-${frame}-${cue}`}
          name={`SFX ${cue}`}
          from={sceneStart(scene.id) + frame}
          src={staticFile(`audio/sfx/${cue}.wav`)}
          volume={CUE_VOLUME[cue]}
          premountFor={30}
        />
      )),
    )}
  </AbsoluteFill>
);
