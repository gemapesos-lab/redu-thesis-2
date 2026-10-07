import React from "react";
import { AbsoluteFill, Series } from "remotion";
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
import { sceneById } from "./timeline";

// Scene lengths live in timeline.ts because scripts/music.mjs scores to the same cue points.
const len = (id: Parameters<typeof sceneById>[0]) => sceneById(id).frames;

// Silent animatic: final audio integration awaits the updated draft-2 VO.
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

  </AbsoluteFill>
);
