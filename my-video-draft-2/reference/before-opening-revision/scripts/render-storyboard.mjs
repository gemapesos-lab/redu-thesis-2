import { bundle } from "@remotion/bundler";
import { enableTailwind } from "@remotion/tailwind-v4";
import { getCompositions, openBrowser, renderStill } from "@remotion/renderer";
import { mkdirSync } from "node:fs";
import { resolve } from "node:path";
import { BEATS, PANELS } from "../src/timeline.ts";

mkdirSync("out/panels", { recursive: true });
mkdirSync("out/check", { recursive: true });
const serveUrl = await bundle({ entryPoint: resolve("src/index.ts"), outDir: resolve("out/.storyboard-bundle"), rspack: true, bundlerOverride: enableTailwind });
const browser = await openBrowser("chrome");
try {
  const comps = await getCompositions(serveUrl, { puppeteerInstance: browser });
  const sceneComps = {
    hook: "S01-Hook", problem: "S02-Problem", meet: "S03-Meet", users: "S04-Users",
    signals: "S05-Signals", score: "S06-Score", prompts: "S07-Prompts", privacy: "S08-Privacy",
    impact: "S09-Impact", outro: "S10-Outro", credits: "S11-Credits",
  };
  const jobs = [
    { id: "Storyboard", frame: 0, output: "out/storyboard.png", scale: 1 },
    ...PANELS.map(p => ({ id: sceneComps[p.scene], frame: p.keyFrame, output: "out/panels/" + p.label + "-" + p.scene + ".png", scale: 1 })),
    { id: "S05-Signals", frame: BEATS.signals.captionReady, output: "out/check/caption-hold-start.png", scale: 1 },
    { id: "S05-Signals", frame: BEATS.signals.holdUntil - 1, output: "out/check/caption-hold-end.png", scale: 1 },
    { id: "S05-Signals", frame: BEATS.signals.captionStart - 1, output: "out/check/camera-before.png", scale: 1 },
    { id: "S05-Signals", frame: Math.round((BEATS.signals.captionStart + BEATS.signals.captionReady) / 2), output: "out/check/camera-midpoint.png", scale: 1 },
    { id: "S05-Signals", frame: 160, output: "out/check/caption-640.png", scale: 1 / 3 },
    { id: "S02-Problem", frame: BEATS.problem.contrast[0] - 1, output: "out/check/not-before.png", scale: 1 },
    { id: "S02-Problem", frame: BEATS.problem.contrast[0] - 4, output: "out/check/not-blur-lead.png", scale: 1 },
    { id: "S02-Problem", frame: BEATS.problem.contrast[0], output: "out/check/not-onset.png", scale: 1 },
    { id: "S01-Hook", frame: BEATS.hook.clock - 7, output: "out/check/clock-blur-lead.png", scale: 1 },
    { id: "S01-Hook", frame: BEATS.hook.swipes[1] + 4, output: "out/check/scroll-motion-blur.png", scale: 1 },
    { id: "S03-Meet", frame: BEATS.meet.logo - 8, output: "out/check/logo-blur-lead.png", scale: 1 },
    { id: "S06-Score", frame: BEATS.score.join + 16, output: "out/check/score-input-flight.png", scale: 1 },
    { id: "S05-Signals", frame: BEATS.signals.session, output: "out/check/session-onset.png", scale: 1 },
    { id: "S05-Signals", frame: BEATS.signals.video, output: "out/check/time-per-video-onset.png", scale: 1 },
    { id: "S07-Prompts", frame: BEATS.prompts.reminder, output: "out/check/reminder-onset.png", scale: 1 },
    { id: "S07-Prompts", frame: BEATS.prompts.pause, output: "out/check/pause-onset.png", scale: 1 },
    { id: "S07-Prompts", frame: BEATS.prompts.breathing, output: "out/check/breathing-onset.png", scale: 1 },
  ];
  for (const job of jobs) {
    const composition = comps.find(c => c.id === job.id);
    if (!composition) throw new Error("Missing composition " + job.id);
    await renderStill({ serveUrl, composition, puppeteerInstance: browser, imageFormat: "png", output: job.output, frame: job.frame, scale: job.scale });
    console.log(job.output);
  }
} finally {
  await browser.close({ silent: true });
}
