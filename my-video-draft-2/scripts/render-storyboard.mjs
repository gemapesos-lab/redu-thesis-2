import { bundle } from "@remotion/bundler";
import { enableTailwind } from "@remotion/tailwind-v4";
import { getCompositions, openBrowser, renderStill } from "@remotion/renderer";
import { mkdirSync } from "node:fs";
import { resolve } from "node:path";
import { HOOK, IMPACT, MEET, OUTRO, PRIVACY, PROBLEM, PROMPTS, SIGNALS, USERS } from "../src/choreography.ts";
import { BEATS, PANELS, sceneById } from "../src/timeline.ts";

mkdirSync("out/panels", { recursive: true });
mkdirSync("out/check-3d-polish", { recursive: true });
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
    { id: "S01-Hook", frame: 20, output: "out/check-3d-polish/opening-text-only.png", scale: 1 },
    { id: "S01-Hook", frame: HOOK.phoneAt + 22, output: "out/check-3d-polish/phone-lands.png", scale: 1 },
    { id: "S01-Hook", frame: HOOK.swipes[2][0] + 4, output: "out/check-3d-polish/swipe-motion-blur.png", scale: 1 },
    { id: "S01-Hook", frame: HOOK.rack + 12, output: "out/check-3d-polish/rack-focus.png", scale: 1 },
    { id: "S01-Hook", frame: HOOK.flip + 2, output: "out/check-3d-polish/clock-flips-to-207.png", scale: 1 },
    { id: "S01-Hook", frame: HOOK.out + 8, output: "out/check-3d-polish/push-into-glow.png", scale: 1 },
    { id: "S02-Problem", frame: BEATS.problem.that + 6, output: "out/check-3d-polish/title-swings-up.png", scale: 1 },
    { id: "S02-Problem", frame: BEATS.problem.screen + 10, output: "out/check-3d-polish/widget-counts-minutes.png", scale: 1 },
    { id: "S02-Problem", frame: PROBLEM.rack + 18, output: "out/check-3d-polish/focus-on-the-videos.png", scale: 1 },
    { id: "S02-Problem", frame: sceneById("problem").frames - 2, output: "out/check-3d-polish/sage-dot.png", scale: 1 },
    { id: "S03-Meet", frame: MEET.spin + 6, output: "out/check-3d-polish/icon-spin.png", scale: 1 },
    { id: "S03-Meet", frame: MEET.land + 4, output: "out/check-3d-polish/icon-lands.png", scale: 1 },
    { id: "S03-Meet", frame: BEATS.meet.privacy + 6, output: "out/check-3d-polish/eyebrow-after-lift.png", scale: 1 },
    { id: "S03-Meet", frame: BEATS.meet.notices + 10, output: "out/check-3d-polish/mascot-glance.png", scale: 1 },
    { id: "S04-Users", frame: 8, output: "out/check-3d-polish/phone-swings-in.png", scale: 1 },
    { id: "S04-Users", frame: USERS.chips[2] + 8, output: "out/check-3d-polish/platform-chips.png", scale: 1 },
    { id: "S05-Signals", frame: SIGNALS.swap, output: "out/check-3d-polish/phone-spin-back.png", scale: 1 },
    { id: "S05-Signals", frame: SIGNALS.cards[1] + 8, output: "out/check-3d-polish/signal-cards.png", scale: 1 },
    { id: "S05-Signals", frame: SIGNALS.closeReady + 4, output: "out/check-3d-polish/caption-close-up.png", scale: 1 },
    { id: "S06-Score", frame: 12, output: "out/check-3d-polish/cards-fly.png", scale: 1 },
    { id: "S06-Score", frame: BEATS.score.score + 4, output: "out/check-3d-polish/meter-settled.png", scale: 1 },
    { id: "S07-Prompts", frame: PROMPTS.crane[1], output: "out/check-3d-polish/meter-becomes-track.png", scale: 1 },
    { id: "S07-Prompts", frame: PROMPTS.levels[0].at + 4, output: "out/check-3d-polish/reminder-rises.png", scale: 1 },
    { id: "S07-Prompts", frame: PROMPTS.levels[2].at + 6, output: "out/check-3d-polish/breathe-rises.png", scale: 1 },
    { id: "S07-Prompts", frame: PROMPTS.pullBack[1], output: "out/check-3d-polish/you-stay-in-control.png", scale: 1 },
    { id: "S08-Privacy", frame: PRIVACY.chip + 14, output: "out/check-3d-polish/raw-caption-dissolves.png", scale: 1 },
    { id: "S08-Privacy", frame: BEATS.privacy.kept + 4, output: "out/check-3d-polish/checklist.png", scale: 1 },
    { id: "S09-Impact", frame: IMPACT.tiles[2] + 20, output: "out/check-3d-polish/stat-tiles.png", scale: 1 },
    { id: "S09-Impact", frame: IMPACT.tilesOut + 5, output: "out/check-3d-polish/tiles-flip-away.png", scale: 1 },
    { id: "S09-Impact", frame: IMPACT.footnote + 10, output: "out/check-3d-polish/results.png", scale: 1 },
    { id: "S10-Outro", frame: 6, output: "out/check-3d-polish/icon-spins-in.png", scale: 1 },
    { id: "S10-Outro", frame: OUTRO.wink + 6, output: "out/check-3d-polish/wink.png", scale: 1 },
    { id: "S05-Signals", frame: SIGNALS.closeReady + 10, output: "out/check-3d-polish/caption-640.png", scale: 1 / 3 },
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
