import React from "react";
import { AbsoluteFill, Img, staticFile, useCurrentFrame } from "remotion";
import { PAPER } from "../brand/tokens";
import { body, centered, display } from "../brand/type";
import { Paper } from "../components/Backdrop";
import { FeedScreen } from "../components/FeedScreen";
import { EASE_IN_OUT, exitStyle, ramp, revealStyle, Words } from "../components/Kinetic";
import { Phone, PhoneHighlight, phoneHeightFor, SCREEN_H, SCREEN_W, StatusBar } from "../components/Phone";
import { AwarenessBanner, BreathingScreen } from "../components/Prompts";
import { BEATS, sceneStart } from "../timeline";

const T = BEATS.prompts;
const EXIT = T.exit;
const PHONE_W = 330;
const PHONE_TOP = 194;
const PHONE_H = phoneHeightFor(PHONE_W);
const CENTERS = [520, 960, 1400];

// Frames where each level is the one being named in the narration.
const ACTIVE: ReadonlyArray<readonly [number, number]> = [
  [T.reminder, T.pause],
  [T.pause, T.breathing],
  [T.breathing, T.you[0]],
];

const STEPS = [
  { title: "Reminder", desc: "A quiet banner" },
  { title: "Pause", desc: "Take a break or keep going" },
  { title: "Breathe", desc: "A 45-second reset" },
];

const fill: React.CSSProperties = { position: "absolute", left: 0, top: 0, width: SCREEN_W, height: SCREEN_H };

export const PromptLevels: React.FC = () => {
  const frame = useCurrentFrame();
  const focus = ramp(frame, ACTIVE[0][0] - 8, 8, EASE_IN_OUT) * (1 - ramp(frame, ACTIVE[2][1] - 8, 8, EASE_IN_OUT));
  const pause = ramp(frame, ACTIVE[1][0] - 8, 8);
  const breathe = ramp(frame, ACTIVE[2][0] - 8, 8);
  const breathMs = (Math.max(0, frame - ACTIVE[2][0]) / 30) * 1000;

  const screens = [
    <Phone key="reminder" width={PHONE_W} onPaper>
      <FeedScreen art="city" user="late.night.edits" caption="one more episode recap... part 7" likes="390K" comments="7,015" shares="22.4K" progress={0.55} />
      <AwarenessBanner
        remaining={1 - ramp(frame, ACTIVE[0][0] + 2, 120, (t) => t)}
        style={revealStyle(frame, { at: ACTIVE[0][0] - 14, dur: 14, blur: 6, rise: -34 })}
      />
    </Phone>,
    <Phone key="pause" width={PHONE_W} onPaper statusBar={false} handle={false}>
      <FeedScreen art="neon" user="dance.daily" caption="trend na 'to, sino gagawa?" likes="1.1M" comments="9,420" shares="52.7K" progress={0.4} />
      <div style={{ ...fill, opacity: 1 - pause }}>
        <StatusBar />
      </div>
      <Img src={staticFile("screens/pause-locked.png")} style={{ ...fill, opacity: pause }} />
      <Img src={staticFile("screens/pause-unlocked.png")} style={{ ...fill, opacity: ramp(frame, T.unlocked - 6, 6) }} />
    </Phone>,
    <Phone key="breathe" width={PHONE_W} onPaper statusBar={false}>
      <FeedScreen art="storm" user="balita.now" caption="grabe ang ulan sa Maynila ngayon" likes="65.3K" comments="2,733" shares="8,904" progress={0.7} />
      <div style={{ ...fill, opacity: 1 - breathe }}>
        <StatusBar />
      </div>
      <div style={{ ...fill, opacity: breathe }}>
        <BreathingScreen timeMs={breathMs} secondsLeft={45 - Math.floor(breathMs / 1000)} />
      </div>
    </Phone>,
  ];

  return (
    <AbsoluteFill>
      <Paper offset={sceneStart("prompts")} />
      <AbsoluteFill style={exitStyle(frame, EXIT, 4)}>
        <AbsoluteFill style={{ ...centered, justifyContent: "flex-start", paddingTop: 52 }}>
          <Words readableOnset text="Steps in gently." times={T.steps} exitAt={T.you[0] - 8} exitDur={8} style={display(84)} />
        </AbsoluteFill>
        <AbsoluteFill style={{ ...centered, justifyContent: "flex-start", paddingTop: 52 }}>
          <Words readableOnset text="You stay in control." times={T.you} style={display(84)} />
        </AbsoluteFill>

        {screens.map((screen, k) => {
          const [start, end] = ACTIVE[k];
          const active = ramp(frame, start - 8, 8, EASE_IN_OUT) * (1 - ramp(frame, end - 8, 8, EASE_IN_OUT));
          const dim = Math.max(0, focus - active);
          return (
            <React.Fragment key={k}>
              <div
                style={{
                  position: "absolute",
                  left: CENTERS[k] - PHONE_W / 2,
                  top: PHONE_TOP,
                  ...revealStyle(frame, { at: 4 + k * 6, dur: 22, blur: 16, rise: 140 }),
                }}
              >
                <div style={{ position: "relative", opacity: 1 - 0.45 * dim, scale: String(1 + 0.05 * active), translate: `0 ${-6 * active}px` }}>
                  {screen}
                  {k === 1 ? <PhoneHighlight width={PHONE_W} rect={{ x: 23, y: 434, w: 314, h: 48 }} radius={24} at={T.you[0] - 10} /> : null}
                </div>
              </div>
              <div
                style={{
                  position: "absolute",
                  left: CENTERS[k] - 220,
                  width: 440,
                  top: PHONE_TOP + PHONE_H + 36,
                  textAlign: "center",
                  ...revealStyle(frame, { at: start - 12, dur: 12 }),
                }}
              >
                <div style={{ opacity: 1 - 0.6 * dim }}>
                  <div style={{ display: "flex", alignItems: "center", justifyContent: "center", gap: 14 }}>
                    <div
                      style={{
                        width: 46,
                        height: 46,
                        borderRadius: 23,
                        background: active > 0.5 ? PAPER.accent : PAPER.ink,
                        color: "#fff",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        ...display(26, "#fff"),
                        letterSpacing: "0",
                      }}
                    >
                      {k + 1}
                    </div>
                    <span style={display(46)}>{STEPS[k].title}</span>
                  </div>
                  <div style={{ ...body(26), marginTop: 8 }}>{STEPS[k].desc}</div>
                </div>
              </div>
            </React.Fragment>
          );
        })}
      </AbsoluteFill>
    </AbsoluteFill>
  );
};
