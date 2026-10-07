import assert from "node:assert/strict";
import { readFileSync, existsSync } from "node:fs";
import { createHash } from "node:crypto";
import { execFileSync } from "node:child_process";
import { resolve } from "node:path";
import { ENTRANCE, EXIT, TEXT } from "../src/choreography.ts";
import { BEATS, FPS, HARD_LIMIT_FRAMES, NARRATION_BUDGET_FRAMES, SCENES, PANELS, TOTAL_FRAMES, VOICEOVER, sceneStart, sceneById } from "../src/timeline.ts";

assert.equal(TOTAL_FRAMES, 1770, "Approved storyboard must total 59 seconds");
assert.ok(TOTAL_FRAMES <= HARD_LIMIT_FRAMES, "Absolute runtime exceeds 60 seconds");
assert.equal(sceneStart("credits"), NARRATION_BUDGET_FRAMES);
assert.equal(sceneById("credits").frames, 5 * FPS);
assert.equal(PANELS.length, 12);
assert.equal(new Set(SCENES.map((s) => s.id)).size, SCENES.length);
let previous = 0;
for (const panel of PANELS) {
  const scene = sceneById(panel.scene);
  const start = sceneStart(panel.scene);
  assert.equal(panel.from, previous, "Storyboard panels must be continuous");
  assert.ok(panel.to > panel.from && panel.from >= start && panel.to <= start + scene.frames);
  assert.ok(panel.keyFrame >= panel.from - start && panel.keyFrame < panel.to - start, "Key frame outside its panel: " + panel.label);
  previous = panel.to;
}
assert.equal(previous, TOTAL_FRAMES);
for (const scene of SCENES) {
  assert.ok(scene.frames > 0 && Number.isInteger(scene.frames));
  for (const [frame, , volume] of scene.cues) assert.ok(frame >= 0 && frame < scene.frames && volume > 0 && volume <= 1, "SFX cue outside its scene: " + scene.id);
}
if (VOICEOVER.status === "ready") assert.ok(VOICEOVER.frames > 0 && VOICEOVER.frames <= NARRATION_BUDGET_FRAMES);

// Text rules: whole phrases only, and blocks that share screen space are never visible together.
const intersects = (a, b) => a[0] < b[0] + b[2] && b[0] < a[0] + a[2] && a[1] < b[1] + b[3] && b[1] < a[1] + a[3];
let textPairs = 0;
for (const [scene, blocks] of Object.entries(TEXT)) {
  const frames = sceneById(scene).frames;
  const span = (b) => [b.at - (b.dur ?? ENTRANCE) / 2, b.exitAt + (b.exitDur ?? EXIT)];
  for (const block of blocks) {
    const [from, to] = span(block);
    assert.ok(from >= 0 && from < to && block.exitAt <= frames, `Text block outside its scene: ${scene}/${block.id}`);
  }
  for (let i = 0; i < blocks.length; i++) {
    for (let j = i + 1; j < blocks.length; j++) {
      if (!intersects(blocks[i].box, blocks[j].box)) continue;
      const [a0, a1] = span(blocks[i]);
      const [b0, b1] = span(blocks[j]);
      assert.ok(a1 + 2 <= b0 || b1 + 2 <= a0, `Overlapping text: ${scene}/${blocks[i].id} and ${blocks[j].id}`);
      textPairs++;
    }
  }
}

let wordsChecked = 0;
if (VOICEOVER.status === "ready") {
  const aligned = JSON.parse(readFileSync(resolve(VOICEOVER.auditFolder, "words.json")));
  const edit = JSON.parse(readFileSync(resolve(VOICEOVER.auditFolder, "edit-map.json")));
  assert.equal(edit.playbackRate, 1);
  assert.equal(edit.removedSilenceSeconds, 0);
  assert.equal(edit.allSpokenWordsPreserved, true);
  assert.equal(edit.allSpokenSamplesPreservedBeforeNormalization, true);
  assert.ok(edit.insertedSilence.reduce((sum, gap) => sum + gap.seconds, 0) <= 1, "More than a second of silence added");
  assert.ok(edit.productionSeconds <= 54);
  const sourceHash = createHash("sha256").update(readFileSync(new URL("../public/" + VOICEOVER.source, import.meta.url))).digest("hex");
  assert.equal(sourceHash, edit.sourceSha256, "Supplied original VO changed");
  for (const line of aligned) {
    const scene = sceneById(line.id);
    const start = sceneStart(line.id);
    assert.ok(line.start * FPS >= start && line.end * FPS <= start + scene.frames, "Scene boundary crosses spoken words: " + line.id);
  }
  const key = (text) => text.toLowerCase().replace(/[^a-z0-9]/g, "");
  const onset = (id, word, nth = 0) => {
    const spoken = aligned.find((s) => s.id === id).words.filter((w) => key(w.word) === key(word))[nth];
    assert.ok(spoken, `Missing word ${id}/${word}`);
    return Math.round(spoken.start * FPS) - sceneStart(id);
  };
  // Each phrase is readable on its word; the lockup eyebrow waits for the logo to clear,
  // and the cold-open title fades up from black.
  const READABLE = [
    ["hook", "just-one-more", "Just", 0, 4], ["problem", "thats-doomscrolling", "That's"], ["problem", "count-minutes", "Screen-time"], ["problem", "not-what-you-watch", "NOT"],
    ["meet", "privacy-first", "privacy-first", 0, 4], ["meet", "notices", "notices"], ["meet", "helps", "helps"],
    ["users", "built-for", "Built"], ["users", "who-watch", "who"], ["signals", "three-signals", "Three"],
    ["score", "fuzzy-logic", "Fuzzy"], ["score", "risk-score", "doomscrolling"], ["prompts", "steps-in-gently", "REDU"], ["prompts", "you-stay-in-control", "You"],
    ["privacy", "runs-on-your-phone", "It"], ["impact", "results-headline", "prompted"], ["outro", "notice-the-scroll", "Notice"], ["outro", "choose-the-pause", "Choose"],
  ];
  for (const [scene, id, word, nth = 0, late = 2] of READABLE) {
    const block = TEXT[scene].find((b) => b.id === id);
    const measured = onset(scene, word, nth);
    const clamp = Math.max(0, ENTRANCE / 2 - measured);
    assert.ok(block.at >= measured - 2 && block.at <= measured + Math.max(late, clamp), `Not readable on its word: ${scene}/${id} (${block.at} vs ${measured})`);
    wordsChecked++;
  }
  assert.equal(BEATS.hook.suddenly, onset("hook", "Suddenly"));
  assert.equal(BEATS.signals.negative, onset("signals", "negative"));
  for (const [i, word] of ["reminder", "pause", "breathing"].entries()) assert.equal([BEATS.prompts.reminder, BEATS.prompts.pause, BEATS.prompts.breathing][i], onset("prompts", word));
}
assert.ok(!readFileSync(new URL("../src/scenes/Credits.tsx", import.meta.url), "utf8").includes("Made with Remotion"));
const baseline = JSON.parse(readFileSync(new URL("../reference/draft-1/original-manifest.json", import.meta.url)));
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
console.log(JSON.stringify({ seconds: TOTAL_FRAMES / FPS, frames: TOTAL_FRAMES, panels: PANELS.length, sfxCues: SCENES.reduce((n, s) => n + s.cues.length, 0), sharedTextRegionsChecked: textPairs, phrasesOnTheirWord: wordsChecked, originalFilesUnchanged: originalsChecked, voiceover: VOICEOVER.status, exportVerified: exportPath ?? null }, null, 2));
