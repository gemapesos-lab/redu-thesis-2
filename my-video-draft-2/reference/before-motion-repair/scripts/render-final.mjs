import { execFileSync } from "node:child_process";
import { VOICEOVER } from "../src/timeline.ts";
if (VOICEOVER.status !== "ready") {
  throw new Error("Draft 2 is a storyboard / silent animatic. Supply and align the updated VO, integrate its audio, regenerate music, then mark VOICEOVER ready before final export.");
}
execFileSync(process.execPath, ["--disable-warning=MODULE_TYPELESS_PACKAGE_JSON", "scripts/check-storyboard.mjs"], { stdio: "inherit" });
execFileSync("node_modules/.bin/remotion", ["render", "AVP", "out/REDU-AVP-draft-2.mp4"], { stdio: "inherit" });
execFileSync(process.execPath, ["--disable-warning=MODULE_TYPELESS_PACKAGE_JSON", "scripts/check-storyboard.mjs", "out/REDU-AVP-draft-2.mp4"], { stdio: "inherit" });
