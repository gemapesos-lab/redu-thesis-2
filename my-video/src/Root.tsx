import "./index.css";
import React from "react";
import { Composition, Folder } from "remotion";
import { AVP } from "./AVP";
import { VIDEO } from "./brand/tokens";
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
import { Storyboard } from "./Storyboard";
import { SCENES, sceneById, TOTAL_FRAMES } from "./timeline";

const STORYBOARD_FRAMES = Math.max(...SCENES.map((scene) => scene.keyFrame)) + 1;

export const RemotionRoot: React.FC = () => (
  <>
    <Composition id="AVP" component={AVP} durationInFrames={TOTAL_FRAMES} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
    {/* A composition rather than a <Still>: frames are clamped to the duration, and <Freeze> needs to reach each key frame. */}
    <Composition id="Storyboard" component={Storyboard} durationInFrames={STORYBOARD_FRAMES} fps={VIDEO.fps} width={2560} height={1820} />
    <Folder name="Scenes">
      <Composition id="S01-Hook" component={Hook} durationInFrames={sceneById("hook").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S02-Problem" component={Problem} durationInFrames={sceneById("problem").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S03-Meet" component={Meet} durationInFrames={sceneById("meet").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S04-Users" component={Users} durationInFrames={sceneById("users").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S05-Signals" component={Signals} durationInFrames={sceneById("signals").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S06-Score" component={Score} durationInFrames={sceneById("score").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S07-Prompts" component={PromptLevels} durationInFrames={sceneById("prompts").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S08-Privacy" component={Privacy} durationInFrames={sceneById("privacy").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S09-Impact" component={Impact} durationInFrames={sceneById("impact").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S10-Outro" component={Outro} durationInFrames={sceneById("outro").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
      <Composition id="S11-Credits" component={Credits} durationInFrames={sceneById("credits").frames} fps={VIDEO.fps} width={VIDEO.width} height={VIDEO.height} />
    </Folder>
  </>
);
