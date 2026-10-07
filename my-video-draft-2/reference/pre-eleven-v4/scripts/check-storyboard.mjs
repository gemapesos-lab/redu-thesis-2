import assert from "node:assert/strict";
import { readFileSync, existsSync } from "node:fs";
import { createHash } from "node:crypto";
import { execFileSync } from "node:child_process";
import { resolve } from "node:path";
import { SCENES as ORIGINAL_SCENES } from "../reference/draft-1/timeline.ts";
import { BEATS, FPS, HARD_LIMIT_FRAMES, NARRATION_BUDGET_FRAMES, SCENES, PANELS, TOTAL_FRAMES, VOICEOVER, sceneStart, sceneById } from "../src/timeline.ts";

assert.equal(TOTAL_FRAMES, 1770, "Approved storyboard must total 59 seconds");
assert.ok(TOTAL_FRAMES <= HARD_LIMIT_FRAMES, "Absolute runtime exceeds 60 seconds");
assert.equal(sceneStart("credits"), NARRATION_BUDGET_FRAMES);
assert.equal(sceneById("credits").frames, 5 * FPS);
assert.equal(PANELS.length, 12);
assert.equal(new Set(SCENES.map((s) => s.id)).size, SCENES.length);
assert.equal(BEATS.signals.highlight, BEATS.signals.captionReady);
assert.ok(BEATS.signals.holdUntil - BEATS.signals.captionReady >= 3 * FPS);
assert.ok(BEATS.signals.holdUntil < sceneById("signals").frames);
let previous = 0;
for (const panel of PANELS) {
  const scene = sceneById(panel.scene);
  const start = sceneStart(panel.scene);
  assert.equal(panel.from, previous, "Storyboard panels must be continuous");
  assert.ok(panel.to > panel.from && panel.from >= start && panel.to <= start + scene.frames);
  assert.ok(panel.keyFrame >= panel.from - start && panel.keyFrame < panel.to - start);
  previous = panel.to;
}
assert.equal(previous, TOTAL_FRAMES);
for (const scene of SCENES) {
  assert.ok(scene.frames > 0 && Number.isInteger(scene.frames));
  for (const [frame] of scene.cues) assert.ok(frame >= 0 && frame < scene.frames);
}
if (VOICEOVER.status === "ready") assert.ok(VOICEOVER.frames > 0 && VOICEOVER.frames <= NARRATION_BUDGET_FRAMES);

// Verify isolation using the saved pre-edit hashes, including the first-draft MP4.
const baseline = JSON.parse(readFileSync(new URL("../reference/draft-1/original-manifest.json", import.meta.url)));
const unchangedScenes = { problem: "Problem", meet: "Meet", users: "Users", prompts: "PromptLevels", privacy: "Privacy", impact: "Impact", outro: "Outro", credits: "Credits" };
for (const [id, component] of Object.entries(unchangedScenes)) {
  const file = "src/scenes/" + component + ".tsx";
  const hash = createHash("sha256").update(readFileSync(new URL("../" + file, import.meta.url))).digest("hex");
  assert.equal(hash, baseline[file], "Unrequested scene changed: " + id);
  assert.deepEqual(sceneById(id), ORIGINAL_SCENES.find(s => s.id === id), "Unrequested scene metadata changed: " + id);
}
const firstDraft = new URL("../../my-video/", import.meta.url);
let originalsChecked = 0;
if (existsSync(firstDraft)) {
  for (const [name, expected] of Object.entries(baseline)) {
    const actual = createHash("sha256").update(readFileSync(new URL(name, firstDraft))).digest("hex");
    assert.equal(actual, expected, "First draft changed: " + name);
    originalsChecked++;
  }
}

const exportPath = process.argv[2];
if (exportPath) {
  const probe = JSON.parse(execFileSync("ffprobe", ["-v", "error", "-show_entries", "format=duration:stream=codec_type,duration,nb_frames,avg_frame_rate", "-of", "json", resolve(exportPath)], { encoding: "utf8" }));
  assert.ok(Number(probe.format.duration) <= 60.000, "Container/audio tail exceeds 60.000 seconds");
  assert.ok(probe.streams.some((stream) => stream.codec_type === "audio"), "Final AVP must contain audio");
  for (const stream of probe.streams) {
    if (stream.duration) assert.ok(Number(stream.duration) <= 60.000, "Stream exceeds 60.000 seconds");
    if (stream.codec_type === "video") {
      assert.equal(Number(stream.nb_frames), TOTAL_FRAMES);
      assert.equal(stream.avg_frame_rate, "30/1");
    }
  }
}
console.log(JSON.stringify({ seconds: TOTAL_FRAMES / FPS, frames: TOTAL_FRAMES, panels: PANELS.length, captionHoldSeconds: (BEATS.signals.holdUntil - BEATS.signals.captionReady) / FPS, originalFilesUnchanged: originalsChecked, preservedScenes: Object.keys(unchangedScenes), voiceover: VOICEOVER.status, exportVerified: exportPath ?? null }, null, 2));
