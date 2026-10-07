// Legacy scratch narration; these per-scene files are no longer used by the AVP.
// The active Lauren recording is processed by scripts/voiceover.mjs.
//   node scripts/scratch-voiceover.mjs [voice] [words-per-minute]
import { execFileSync } from "node:child_process";
import { mkdirSync, rmSync } from "node:fs";
import { join } from "node:path";
import { FPS, SCENES } from "../src/timeline.ts";

const VOICE = process.argv[2] ?? "Karen";
const RATE = process.argv[3] ?? "172";
const OUT = join(import.meta.dirname, "..", "public", "audio", "vo");

mkdirSync(OUT, { recursive: true });
for (const scene of SCENES) {
  if (!scene.voice) {
    continue;
  }
  const aiff = join(OUT, `${scene.id}.aiff`);
  const wav = join(OUT, `${scene.id}.wav`);
  execFileSync("say", ["-v", VOICE, "-r", RATE, "-o", aiff, scene.voice.replace(/REDU/g, "Ree-doo")]);
  execFileSync("ffmpeg", [
    "-y", "-loglevel", "error", "-i", aiff,
    "-af", "silenceremove=start_periods=1:start_threshold=-50dB,areverse,silenceremove=start_periods=1:start_threshold=-50dB,areverse,highpass=f=70,loudnorm=I=-16:TP=-1.5:LRA=7",
    "-ar", "48000", "-ac", "1", wav,
  ]);
  rmSync(aiff);
  const seconds = Number(execFileSync("ffprobe", ["-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", wav]).toString());
  const frames = Math.ceil(seconds * FPS);
  const room = scene.frames - (scene.voAt ?? 0);
  console.log(`${scene.id.padEnd(8)} ${seconds.toFixed(2)}s  voFrames ${frames}${frames > room ? `  (over the scene by ${frames - room} frames)` : ""}`);
}
