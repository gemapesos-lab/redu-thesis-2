// Normalize the edited working copy at its original speed.
// The supplied MP3 stays untouched at public/audio/vo/draft-2-original.mp3.
//   npm run audio:vo
import { execFileSync, spawnSync } from "node:child_process";
import { join } from "node:path";
import { FPS, VOICEOVER } from "../src/timeline.ts";

if (VOICEOVER.status !== "ready") throw new Error("Updated draft-2 VO is pending. Import the new recording and measure its cues first.");

const PUBLIC = join(import.meta.dirname, "..", "public");
const source = join(PUBLIC, VOICEOVER.productionSource ?? VOICEOVER.source);
const output = join(PUBLIC, VOICEOVER.audio);
// Compensate for the mono narration being played through both stereo channels.
const filter = "loudnorm=I=-16:TP=-3:LRA=7:dual_mono=true";
const probe = spawnSync("ffmpeg", [
  "-hide_banner", "-i", source, "-af", `${filter}:print_format=json`, "-f", "null", "-",
], { encoding: "utf8" });
if (probe.status !== 0) {
  throw new Error(probe.stderr || probe.error?.message || "Could not measure narration");
}
const measured = JSON.parse(probe.stderr.slice(probe.stderr.lastIndexOf("{"), probe.stderr.lastIndexOf("}") + 1));
const pass2 = `${filter}:measured_I=${measured.input_i}:measured_TP=${measured.input_tp}:measured_LRA=${measured.input_lra}:measured_thresh=${measured.input_thresh}:offset=${measured.target_offset}:linear=false`;
execFileSync("ffmpeg", [
  "-y", "-loglevel", "error", "-i", source, "-af", pass2,
  "-ar", "48000", "-ac", "1", "-c:a", "pcm_s24le", output,
]);
const seconds = Number(execFileSync("ffprobe", [
  "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", output,
]).toString());
const frames = Math.ceil(seconds * FPS);
if (frames !== VOICEOVER.frames) {
  throw new Error(`Narration is ${frames} frames; retime VOICEOVER.frames and the scenes before rendering.`);
}
console.log(`Lauren v4: ${seconds.toFixed(3)} s, ${frames} frames, original pace, normalized for the stereo mix.`);
