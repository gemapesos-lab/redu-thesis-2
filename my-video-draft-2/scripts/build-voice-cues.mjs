// Scene lengths and measured word cues for the 3D polish, from the aligned take.
//   node scripts/build-voice-cues.mjs [production/voiceover/3d-polish]
import assert from "node:assert/strict";
import { readFileSync, writeFileSync } from "node:fs";

const folder = process.argv[2] ?? "production/voiceover/3d-polish";
const measured = JSON.parse(readFileSync(`${folder}/words.json`, "utf8"));
const byId = Object.fromEntries(measured.map(scene => [scene.id, scene]));
const ids = measured.map(scene => scene.id);
const fps = 30;
// Seconds each scene opens before its first word; the hook opens on frame 0.
const lead = { hook: 0, problem: 0.3, meet: 0.22, users: 0.16, signals: 0.2, score: 0.2, prompts: 0.16, privacy: 0.16, impact: 0.16, outro: 0.2 };
const starts = Object.fromEntries(measured.map(scene => [scene.id, scene.id === "hook" ? 0 : Math.round((scene.start - lead[scene.id]) * fps)]));
starts.credits = 1620;
const canonical = word => word.toLowerCase().replace(/[^a-z0-9]/g, "");
const word = (id, value, nth = 0) => {
  const matches = byId[id].words.filter(w => canonical(w.word) === canonical(value));
  assert.ok(matches[nth], `Missing spoken word ${id}/${value}/${nth}`);
  return matches[nth];
};
const at = (id, value, nth = 0) => Math.round(word(id, value, nth).start * fps) - starts[id];
const end = (id, value, nth = 0) => Math.ceil(word(id, value, nth).end * fps) - starts[id];
const list = (id, values) => values.map(value => at(id, value));
const part = (id, value, text) => {
  const found = word(id, value).parts?.find(p => canonical(p.text) === canonical(text));
  assert.ok(found, `Missing part ${id}/${value}/${text}`);
  return Math.round(found.start * fps) - starts[id];
};
const frames = Object.fromEntries(ids.map((id, i) => [id, (starts[ids[i + 1]] ?? starts.credits) - starts[id]]));
frames.credits = 150;

const beats = {
  hook: { title: list("hook", ["Just", "one", "more", "video"]), just: at("hook", "Just"), then: at("hook", "Then"), another: at("hook", "another"), and: at("hook", "And"), another2: at("hook", "another", 1), another2End: end("hook", "another", 1), suddenly: at("hook", "Suddenly"), two: at("hook", "two-oh-seven"), seven: part("hook", "two-oh-seven", "seven"), am: at("hook", "A.M."), amEnd: end("hook", "A.M.") },
  problem: { intro: list("problem", ["That's", "doomscrolling"]), limits: list("problem", ["Screen-time", "limits", "count", "minutes"]), contrast: list("problem", ["NOT", "what", "you", "watch"]), that: at("problem", "That's"), doom: at("problem", "doomscrolling"), screen: at("problem", "Screen-time"), minutesEnd: end("problem", "minutes"), not: at("problem", "NOT"), watchEnd: end("problem", "watch") },
  meet: { noticesWords: list("meet", ["notices", "doomscrolling"]), helpsWords: list("meet", ["helps", "you", "pause"]), meet: at("meet", "Meet"), redu: at("meet", "REDU"), a: at("meet", "a"), privacy: at("meet", "privacy-first"), notices: at("meet", "notices"), helps: at("meet", "helps"), pause: at("meet", "pause"), pauseEnd: end("meet", "pause") },
  users: { built: at("users", "Built"), adult: at("users", "adult"), who: at("users", "who"), short: at("users", "short-form"), videoEnd: end("users", "video") },
  signals: { three: at("signals", "Three"), session: at("signals", "session"), time: at("signals", "time"), videoEnd: end("signals", "video"), and: at("signals", "and"), negative: at("signals", "negative"), captionsEnd: end("signals", "captions") },
  score: { fuzzy: at("score", "Fuzzy"), combines: at("score", "combines"), doom: at("score", "doomscrolling"), score: at("score", "score"), scoreEnd: end("score", "score") },
  prompts: { youWords: [at("prompts", "You"), at("prompts", "stay"), at("prompts", "in", 1), at("prompts", "control")], when: at("prompts", "When"), rises: at("prompts", "rises"), redu: at("prompts", "REDU"), reminder: at("prompts", "reminder"), pause: at("prompts", "pause"), breathing: at("prompts", "breathing"), you: at("prompts", "You"), controlEnd: end("prompts", "control") },
  privacy: { it: at("privacy", "It"), runs: at("privacy", "runs"), raw: at("privacy", "raw"), kept: at("privacy", "kept"), keptEnd: end("privacy", "kept") },
  impact: { in: at("impact", "In"), twoWeek: at("impact", "two-week"), fifty: at("impact", "fifty"), filipino: at("impact", "Filipino"), adults: at("impact", "adults"), adultsEnd: end("impact", "adults"), prompted: at("impact", "prompted"), scrolled: at("impact", "scrolled"), negative: at("impact", "negative"), contentEnd: end("impact", "content") },
  outro: { noticeWords: list("outro", ["Notice", "the", "scroll"]), chooseWords: [at("outro", "Choose"), at("outro", "the", 1), at("outro", "pause")], redu: at("outro", "REDU"), notice: at("outro", "Notice"), choose: at("outro", "Choose"), pause: at("outro", "pause"), pauseEnd: end("outro", "pause") },
};
for (const [id, scene] of Object.entries(beats)) {
  for (const [key, value] of Object.entries(scene)) for (const v of [value].flat()) assert.ok(v >= 0 && v < frames[id], `${id}.${key} falls outside its scene`);
}
const timings = Object.fromEntries([...ids, "credits"].map(id => [id, {
  frames: frames[id],
  ...(byId[id] ? { voAt: Math.round(byId[id].start * fps) - starts[id], voFrames: Math.ceil(byId[id].end * fps) - Math.round(byId[id].start * fps) } : {}),
}]));
const file = "// Measured from the supplied 3D-polish take; rebuilt by scripts/build-voice-cues.mjs.\n" +
  "export const MEASURED_BEATS = " + JSON.stringify(beats, null, 2) + " as const;\n" +
  "export const SCENE_TIMINGS = " + JSON.stringify(timings, null, 2) + " as const;\n";
writeFileSync("src/voiceover-cues.ts", file);
writeFileSync(`${folder}/scene-timings.json`, JSON.stringify({ starts, timings, beats }, null, 2) + "\n");
console.log(JSON.stringify({ starts, frames }, null, 2));
