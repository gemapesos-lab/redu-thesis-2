import React from "react";
import { AbsoluteFill, interpolate, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { display } from "../brand/type";
import { HOOK, textBlock } from "../choreography";
import { Night } from "../components/Backdrop";
import { WordBlock } from "../components/Block";
import { FeedScreen, type FeedArt } from "../components/FeedScreen";
import { EASE_IN_OUT, handheld, ramp, track } from "../components/Kinetic";
import { MotionBlur } from "../components/MotionBlur";
import { SCREEN_H, SCREEN_W } from "../components/Phone";
import { Clock3D } from "../components/three/Clock3D";
import { Phone3D } from "../components/three/Phone3D";
import { lens, Stage } from "../components/three/Stage";
import type { Camera } from "../components/three/math";
import { MEASURED_BEATS } from "../voiceover-cues";

type FeedItem = {
  readonly art: FeedArt;
  readonly user: string;
  readonly caption: string;
  readonly likes: string;
  readonly comments: string;
  readonly shares: string;
  // The light the video throws into the dark room, as r,g,b.
  readonly light: string;
};

const FEED: ReadonlyArray<FeedItem> = [
  { art: "sunset", user: "mika.travels", caption: "golden hour sa Batangas, sulit ang byahe", likes: "84.2K", comments: "1,204", shares: "3,310", light: "255,150,100" },
  { art: "food", user: "kain.tayo", caption: "3-ingredient leche flan, no oven needed", likes: "212K", comments: "4,882", shares: "18.1K", light: "240,160,80" },
  { art: "neon", user: "dance.daily", caption: "trend na 'to, sino gagawa?", likes: "1.1M", comments: "9,420", shares: "52.7K", light: "200,90,240" },
  { art: "ocean", user: "surf.siargao", caption: "cloud 9 this morning", likes: "47.9K", comments: "611", shares: "1,980", light: "90,185,230" },
  { art: "city", user: "late.night.edits", caption: "one more episode recap... part 7", likes: "390K", comments: "7,015", shares: "22.4K", light: "120,90,210" },
  { art: "storm", user: "balita.now", caption: "grabe ang ulan sa Maynila ngayon", likes: "65.3K", comments: "2,733", shares: "8,904", light: "130,140,165" },
  { art: "rain", user: "ulan.vibes", caption: "isa pa, tapos tulog na", likes: "28.6K", comments: "402", shares: "1,115", light: "110,125,160" },
];

const PHONE_W = 400;
const CLOCK_AT = [430, 70, -1300] as const;
const T = MEASURED_BEATS.hook;
const END = HOOK.out + 14;
const TITLE = textBlock("hook", "just-one-more");

const mixLight = (a: string, b: string, t: number) => {
  const x = a.split(",").map(Number);
  const y = b.split(",").map(Number);
  return x.map((v, i) => Math.round(v + (y[i] - v) * t)).join(",");
};

const HookVisual: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  let offset = 0;
  let speed = 0;
  for (const [start, dur] of HOOK.swipes) {
    offset += ramp(frame, start, dur, EASE_IN_OUT);
    if (frame > start && frame < start + dur) {
      speed = Math.sin(((frame - start) / dur) * Math.PI) / dur;
    }
  }
  const index = Math.min(FEED.length - 2, Math.floor(offset));
  const light = mixLight(FEED[index].light, FEED[index + 1].light, offset - index);

  const rise = spring({ frame: frame - HOOK.phoneAt, fps, config: { damping: 17, stiffness: 95, mass: 1 } });
  const rack = ramp(frame, HOOK.rack, HOOK.rackDur, EASE_IN_OUT);
  const out = ramp(frame, HOOK.out, END - HOOK.out, (t) => t * t);
  const sinceFlip = frame - HOOK.flip;
  const glow = sinceFlip < 0 ? 0.2 : 0.35 + 0.65 * Math.exp(-sinceFlip / 9);

  const shake = 1 - rack * 0.5;
  const camera: Camera = {
    x: CLOCK_AT[0] * rack + handheld(frame, 1) * 5 * shake,
    y: CLOCK_AT[1] * rack + handheld(frame, 2) * 4 * shake,
    z: CLOCK_AT[2] * rack,
    orbit: track(frame, [[HOOK.phoneAt, -8], [HOOK.rack, 26], [HOOK.rack + HOOK.rackDur, -6], [END, -9]]) + handheld(frame, 3) * 0.3 * shake,
    tilt: track(frame, [[HOOK.phoneAt, -2], [HOOK.rack, 4], [HOOK.rack + HOOK.rackDur, 13], [END, 15]]) + handheld(frame, 4) * 0.25 * shake,
    roll: handheld(frame, 5) * 0.3 * shake,
    dolly: track(frame, [[HOOK.phoneAt, 0], [HOOK.rack, 200], [HOOK.rack + HOOK.rackDur, -170], [HOOK.out, -90], [END, 700]]),
  };

  const phone = {
    position: [-1500 * rack, 950 * (1 - rise), 260 * rack] as const,
    rotation: [6 + 32 * (1 - rise) + 4 * rack, -24 - 22 * (1 - rise) - 46 * rack, -3 - 6 * (1 - rise)] as const,
  };
  const visible = Math.floor(offset);

  return (
    <AbsoluteFill>
      <Night glow={0.6 * (1 - rack)} />
      <AbsoluteFill style={{ opacity: rack, background: `radial-gradient(1100px 760px at 50% 48%, rgba(255,45,30,${0.1 + 0.08 * glow}), rgba(255,45,30,0) 72%)` }} />
      <AbsoluteFill
        style={{
          opacity: rise * (1 - rack),
          background: `radial-gradient(900px 760px at 50% 52%, rgba(${light},0.17), rgba(${light},0) 70%)`,
        }}
      />
      <Stage camera={camera} style={lens(30 * (1 - rack) + 8 * out, 0.25 * ramp(frame, HOOK.phoneAt + 6, 30) * (1 - rack) + rack)}>
        <Clock3D position={CLOCK_AT} rotation={[0, -26, 0]} sinceFlip={sinceFlip} glow={glow + out} />
      </Stage>
      {frame >= HOOK.phoneAt - 1 && rack < 0.55 ? (
        <Stage camera={camera} style={lens(16 * rack)}>
          <Phone3D width={PHONE_W} position={phone.position} rotation={phone.rotation} time="2:06">
            <div
              style={{
                position: "absolute",
                left: 0,
                top: 0,
                width: SCREEN_W,
                height: SCREEN_H * FEED.length,
                translate: `0 ${-offset * SCREEN_H}px`,
                filter: speed > 0.01 ? `blur(${speed * 34}px)` : undefined,
              }}
            >
              {FEED.map((item, i) =>
                Math.abs(i - offset) < 1.5 || i === visible ? (
                  <div key={item.user} style={{ position: "absolute", top: i * SCREEN_H, left: 0, width: SCREEN_W, height: SCREEN_H }}>
                    <FeedScreen {...item} progress={i === 0 ? 0.2 + frame / 220 : 0.12} />
                  </div>
                ) : null,
              )}
            </div>
          </Phone3D>
        </Stage>
      ) : null}
      <WordBlock block={TITLE} text="Just one more video…" times={[TITLE.at, ...T.title.slice(1)]} align="center" style={display(140, "#FFFFFF")} />
      <AbsoluteFill style={{ background: "#000", opacity: interpolate(frame, [END - 8, END], [0, 1], { extrapolateLeft: "clamp", extrapolateRight: "clamp" }) }} />
    </AbsoluteFill>
  );
};

// The feed carries its own speed blur during swipes; sampled motion blur is kept
// for the moves that carry whole objects: the phone's arrival, the rack and the push.
export const Hook: React.FC = () => (
  <MotionBlur
    windows={[
      [HOOK.phoneAt, HOOK.phoneAt + 22],
      [HOOK.rack, HOOK.rack + HOOK.rackDur],
      [HOOK.out, END],
    ]}
  >
    <HookVisual />
  </MotionBlur>
);

// For the storyboard: the montage and the clock as separate key frames.
export const HOOK_KEYS = { montage: T.another2 + 8, clock: T.amEnd - 2 } as const;
