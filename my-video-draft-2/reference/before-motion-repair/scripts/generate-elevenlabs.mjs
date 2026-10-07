// Preserve every take and its timestamp response. Never print credentials.
import { existsSync, mkdirSync, readFileSync, writeFileSync } from "node:fs";
import { execFileSync } from "node:child_process";
import { resolve } from "node:path";

process.loadEnvFile(resolve(".env"));
const key = process.env.ELEVENLABS_API_KEY;
if (!key) throw new Error("ELEVENLABS_API_KEY is missing from the ignored .env file.");
const configFile = process.argv[2] ?? "production/voiceover/eleven-v4.json";
const take = process.argv[3] ?? "take-01";
if (!/^take-[0-9]{2}$/.test(take)) throw new Error("Use a numbered take, for example take-01.");
const config = JSON.parse(readFileSync(configFile, "utf8"));
const audioDir = resolve("public/audio/vo/eleven-v4", take);
const metaDir = resolve("production/voiceover", take);
const audioPath = resolve(audioDir, "original.mp3");
if (existsSync(audioPath) || existsSync(resolve(metaDir, "request.json"))) throw new Error("This take already exists. Use a new number so its audio and request remain preserved.");
mkdirSync(audioDir, { recursive: true });
mkdirSync(metaDir, { recursive: true });
const { voice_id, voice_name, output_format, ...body } = config;
writeFileSync(resolve(metaDir, "request.json"), JSON.stringify(config, null, 2) + "\n");
console.log(`Generating ${take}: ${config.model_id}, ${voice_name}, ${body.text.length} characters, original speed.`);
const response = await fetch(`https://api.elevenlabs.io/v1/text-to-speech/${voice_id}/with-timestamps?output_format=${output_format}`, {
  method: "POST",
  headers: { "xi-api-key": key, "Content-Type": "application/json" },
  body: JSON.stringify(body),
  signal: AbortSignal.timeout(180000),
});
if (!response.ok) {
  const error = await response.json();
  writeFileSync(resolve(metaDir, "error.json"), JSON.stringify({ status: response.status, detail: error.detail }, null, 2));
  throw new Error(`ElevenLabs returned HTTP ${response.status}: ${JSON.stringify(error.detail)}`);
}
const { audio_base64, ...timing } = await response.json();
if (!audio_base64) throw new Error("ElevenLabs returned no audio.");
writeFileSync(audioPath, Buffer.from(audio_base64, "base64"));
writeFileSync(resolve(metaDir, "alignment.json"), JSON.stringify(timing, null, 2) + "\n");
const seconds = Number(execFileSync("ffprobe", ["-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", audioPath], { encoding: "utf8" }));
const summary = { take, voice_id, voice_name, model: config.model_id, seconds, speed: body.voice_settings?.speed ?? 1, audio: audioPath, requestId: response.headers.get("request-id"), historyItemId: response.headers.get("history-item-id"), characters: body.text.length, createdAt: new Date().toISOString() };
writeFileSync(resolve(metaDir, "generation.json"), JSON.stringify(summary, null, 2) + "\n");
console.log(JSON.stringify(summary, null, 2));
