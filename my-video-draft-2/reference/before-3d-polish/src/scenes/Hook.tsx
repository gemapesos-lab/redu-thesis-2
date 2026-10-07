import React from "react";
import { AbsoluteFill, useCurrentFrame } from "remotion";
import { centered, display } from "../brand/type";
import { Night } from "../components/Backdrop";
import { DigitalClock } from "../components/DigitalClock";
import { BEATS, sceneById } from "../timeline";
import { FeedScreen, type FeedArt } from "../components/FeedScreen";
import { EASE_IN_OUT, ramp, readableStyle, revealStyle, Words } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { Phone, SCREEN_H, SCREEN_W } from "../components/Phone";

type FeedItem = {
  readonly art: FeedArt;
  readonly user: string;
  readonly caption: string;
  readonly likes: string;
  readonly comments: string;
  readonly shares: string;
};

const FEED: ReadonlyArray<FeedItem> = [
  { art: "sunset", user: "mika.travels", caption: "golden hour sa Batangas, sulit ang byahe", likes: "84.2K", comments: "1,204", shares: "3,310" },
  { art: "food", user: "kain.tayo", caption: "3-ingredient leche flan, no oven needed", likes: "212K", comments: "4,882", shares: "18.1K" },
  { art: "neon", user: "dance.daily", caption: "trend na 'to, sino gagawa?", likes: "1.1M", comments: "9,420", shares: "52.7K" },
  { art: "ocean", user: "surf.siargao", caption: "cloud 9 this morning", likes: "47.9K", comments: "611", shares: "1,980" },
  { art: "city", user: "late.night.edits", caption: "one more episode recap... part 7", likes: "390K", comments: "7,015", shares: "22.4K" },
  { art: "storm", user: "balita.now", caption: "grabe ang ulan sa Maynila ngayon", likes: "65.3K", comments: "2,733", shares: "8,904" },
];

// Each swipe is shorter than the last: the feed speeds up.
const SWIPES = BEATS.hook.swipes.map((at, i) => [at, 18 - i * 2] as const);

const PHONE_W = 430;

const HookVisual: React.FC = () => {
  const frame = useCurrentFrame();
  let offset = 0;
  let speed = 0;
  for (const [start, dur] of SWIPES) {
    offset += ramp(frame, start, dur, EASE_IN_OUT);
    if (frame > start && frame < start + dur) {
      speed = Math.sin(((frame - start) / dur) * Math.PI) / dur;
    }
  }
  const phone = revealStyle(frame, { at: BEATS.hook.phoneAt, dur: 20, blur: 22, rise: 170, exitAt: BEATS.hook.phoneExit, exitDur: 10 });

  return (
    <AbsoluteFill>
      <Night />
      <AbsoluteFill style={centered}>
        <Words readableOnset text="Just one more video…" times={BEATS.hook.words} exitAt={BEATS.hook.introExit} style={display(140, "#FFFFFF")} />
      </AbsoluteFill>
      <div style={{ position: "absolute", left: 145, top: 402, width: 540 }}>
        <Words readableOnset text={"Then\nanother."} times={BEATS.hook.another} exitAt={BEATS.hook.anotherExit} style={display(104, "#FFFFFF")} />
      </div>
      <AbsoluteFill style={centered}>
        <div style={{ position: "relative", ...phone }}>
          <div
            style={{
              position: "absolute",
              inset: -220,
              background: "radial-gradient(closest-side, rgba(150,175,255,0.22), transparent)",
            }}
          />
          <Phone width={PHONE_W} time="2:06">
            <div
              style={{
                position: "absolute",
                left: 0,
                top: 0,
                width: SCREEN_W,
                height: SCREEN_H * FEED.length,
                translate: `0 ${-offset * SCREEN_H}px`,
                filter: speed > 0.01 ? `blur(${speed * 40}px)` : undefined,
              }}
            >
              {FEED.map((item, i) => (
                <div key={item.user} style={{ position: "absolute", top: i * SCREEN_H, left: 0, width: SCREEN_W, height: SCREEN_H }}>
                  <FeedScreen {...item} progress={i === 0 ? 0.2 + frame / 200 : 0.12} />
                </div>
              ))}
            </div>
          </Phone>
        </div>
      </AbsoluteFill>
      <AbsoluteFill style={centered}>
        <div style={readableStyle(frame, { at: BEATS.hook.clock, dur: 22, blur: 34, rise: 0, scaleFrom: 1.12, exitAt: sceneById("hook").frames - 10 })}>
          <DigitalClock />
        </div>
      </AbsoluteFill>
    </AbsoluteFill>
  );
};

export const Hook: React.FC = () => <MotionBlur windows={[
  [BEATS.hook.phoneAt, BEATS.hook.phoneAt + 20],
  ...SWIPES.map(([at, dur]) => [at, at + dur] as const),
  [BEATS.hook.phoneExit, BEATS.hook.clock + 12],
]}><HookVisual /></MotionBlur>;
