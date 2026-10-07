import { readFileSync, writeFileSync, existsSync } from "node:fs";
import { resolve } from "node:path";

process.loadEnvFile(".env");
const output = resolve("production/voiceover/supplied-lauren-v4/alignment.json");
if (existsSync(output)) throw new Error("Alignment is already saved; inspect it instead of repeating the API request.");
const form = new FormData();
form.append("file", new Blob([readFileSync("public/audio/vo/draft-2-original.mp3")], { type: "audio/mpeg" }), "draft-2-original.mp3");
form.append("text", readFileSync("VOICEOVER.txt", "utf8"));
const response = await fetch("https://api.elevenlabs.io/v1/forced-alignment", {
  method: "POST", headers: { "xi-api-key": process.env.ELEVENLABS_API_KEY }, body: form, signal: AbortSignal.timeout(180000),
});
if (!response.ok) throw new Error(`Alignment HTTP ${response.status}: ${JSON.stringify((await response.json()).detail)}`);
const alignment = await response.json();
writeFileSync(output, JSON.stringify(alignment, null, 2) + "\n");
console.log(JSON.stringify({ output, words: alignment.words?.length, first: alignment.words?.[0], last: alignment.words?.at(-1), loss: alignment.loss }, null, 2));
