// Convert ElevenLabs character alignment into word and scene cues without
// estimating timings from reading speed. Keep the original response alongside it.
import assert from "node:assert/strict";
import { readFileSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";
import { SCENES } from "../reference/pre-eleven-v4/src/timeline.ts";

const take = process.argv[2] ?? "take-02";
const folder = resolve("production/voiceover", take);
const response = JSON.parse(readFileSync(resolve(folder, "alignment.json"), "utf8"));
const alignment = response.normalized_alignment ?? response.alignment;
assert.ok(alignment?.characters?.length, "The generation did not return character timings.");
const letters = [];
let inTag = false;
alignment.characters.forEach((character, i) => {
  for (const c of character) {
    if (c === "[") inTag = true;
    else if (c === "]") inTag = false;
    else if (!inTag && /[a-z0-9]/i.test(c)) letters.push({ letter: c.toLowerCase(), start: alignment.character_start_times_seconds[i], end: alignment.character_end_times_seconds[i] });
  }
});
const canonical = text => text.toLowerCase().replace(/[^a-z0-9]/g, "");
const scenes = SCENES.filter(scene => scene.voice);
const expected = canonical(scenes.map(scene => scene.voice).join(" "));
const actual = letters.map(item => item.letter).join("");
if (actual !== expected) {
  const mismatch = [...expected].findIndex((letter, i) => letter !== actual[i]);
  throw new Error(`Alignment text differs at ${mismatch}: expected ${expected.slice(Math.max(0,mismatch-20),mismatch+50)}, got ${actual.slice(Math.max(0,mismatch-20),mismatch+50)}. Inspect the response before retiming.`);
}
let offset = 0;
const result = scenes.map(scene => {
  const words = scene.voice.split(/\s+/).map(word => {
    const size = canonical(word).length;
    const start = letters[offset].start;
    const end = letters[offset + size - 1].end;
    offset += size;
    return { word, start, end, onsetFrame: Math.round(start * 30), endFrame: Math.ceil(end * 30) };
  });
  return { id: scene.id, start: words[0].start, end: words.at(-1).end, words };
});
writeFileSync(resolve(folder, "words.json"), JSON.stringify(result, null, 2) + "\n");
console.log(JSON.stringify(result.map(scene => ({ id: scene.id, start: scene.start, end: scene.end, seconds: Number((scene.end-scene.start).toFixed(3)) })), null, 2));
