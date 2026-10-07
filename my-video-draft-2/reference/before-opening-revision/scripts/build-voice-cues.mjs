import assert from "node:assert/strict";
import { readFileSync, writeFileSync } from "node:fs";

const measured = JSON.parse(readFileSync("production/voiceover/supplied-lauren-v4/words.json", "utf8"));
const byId = Object.fromEntries(measured.map(scene => [scene.id, scene]));
const ids = measured.map(scene => scene.id);
const fps = 30;
const lead = { hook: 0, problem: 0.16, meet: 0.22, users: 0.16, signals: 0.2, score: 0.2, prompts: 0.16, privacy: 0.16, impact: 0.16, outro: 0.2 };
const starts = Object.fromEntries(measured.map(scene => [scene.id, scene.id === "hook" ? 0 : Math.round((scene.start-lead[scene.id])*fps)]));
starts.credits = 1620;
const canonical = word => word.toLowerCase().replace(/[^a-z0-9]/g, "");
const word = (id, value, nth = 0) => {
  const matches = byId[id].words.filter(w => canonical(w.word) === canonical(value));
  assert.ok(matches[nth], `Missing spoken word ${id}/${value}/${nth}`);
  return matches[nth];
};
const at = (id, value, nth = 0) => Math.round(word(id,value,nth).start*fps)-starts[id];
const end = (id, value, nth = 0) => Math.ceil(word(id,value,nth).end*fps)-starts[id];
const list = (id, values) => values.map(value => at(id,value));
const frames = Object.fromEntries(ids.map((id,i) => [id, (starts[ids[i+1]] ?? starts.credits)-starts[id]]));
frames.credits = 150;

const beats = {
  hook: { words:list("hook",["Just","one","more","video"]), introExit:end("hook","video")+2, phoneAt:end("hook","video")+2, phoneExit:at("hook","two-oh-seven")-18, clock:at("hook","two-oh-seven"), swipes:[] },
  problem: { intro:list("problem",["That's","doomscrolling"]), introExit:at("problem","Screen-time")-10, pill:at("problem","Screen-time"), limits:list("problem",["Screen-time","limits","count","minutes"]), contrast:list("problem",["not","what","you","watch"]), exit:frames.problem-10 },
  meet: { logo:at("meet","REDU"), eyebrow:list("meet",["a","privacy-first","Android","app"]), lift:at("meet","privacy-first")-12, notices:list("meet",["notices","doomscrolling"]), helps:list("meet",["helps","you","pause"]), exit:frames.meet-10 },
  users: { built:list("users",["Built","for"]), headline:list("users",["adult","Filipino","Android","users"]), supporting:[...list("users",["who","watch","short-form","video"]),end("users","video")], platforms:at("users","short-form"), exit:frames.users-8 },
  signals: { heading:list("signals",["Three","signals"]), session:at("signals","session"), sessionWords:list("signals",["session","length"]), video:at("signals","time"), videoWords:list("signals",["time","per","video"]), negative:at("signals","negative"), negativeWords:list("signals",["negative","captions"]), captionStart:at("signals","negative")-24, captionReady:at("signals","negative")+4, highlight:at("signals","negative"), holdUntil:frames.signals-8 },
  score: { fuzzy:at("score","Fuzzy"), logic:at("score","logic"), join:at("score","combines"), meter:at("score","into"), title:list("score",["doomscrolling","risk","score"]), settle:at("score","score"), exit:frames.score-8 },
  prompts: { steps:list("prompts",["steps","in","gently"]), reminder:at("prompts","reminder"), pause:at("prompts","pause"), breathing:at("prompts","breathing"), you:[at("prompts","You"),at("prompts","stay"),at("prompts","in",1),at("prompts","control")], unlocked:at("prompts","You")-8, exit:frames.prompts-10 },
  privacy: { heading:list("privacy",["runs","on","your","phone"]), points:list("privacy",["runs","raw","kept"]), exit:frames.privacy-8 },
  impact: { weeks:at("impact","two-week"), adults:at("impact","fifty"), sessions:at("impact","adults")-28, shrink:at("impact","scrolled")-22, headline:[at("impact","scrolled"),at("impact","less"),at("impact","saw"),at("impact","less",1),at("impact","negative"),at("impact","content")], cards:[at("impact","scrolled"),at("impact","less"),at("impact","negative"),at("impact","negative")+8], badges:at("impact","less")+6, footnote:at("impact","less"), exit:frames.impact-8 },
  outro: { logo:at("outro","REDU"), notice:[at("outro","Notice"),at("outro","the"),at("outro","scroll")], choose:[at("outro","Choose"),at("outro","the",1),at("outro","pause")], wink:end("outro","pause"), exit:frames.outro-8 },
};
beats.hook.swipes = [0,1,2,3].map(i=>Math.round(beats.hook.phoneAt+10+i*(beats.hook.phoneExit-beats.hook.phoneAt-18)/3));
assert.ok(beats.signals.holdUntil-beats.signals.captionReady >= 90, "Highlighted caption hold must be at least 3 seconds.");
const cues = {
 hook:[...beats.hook.swipes.map(f=>[f,"swipe"]),[beats.hook.clock,"hit"]],
 problem:[[beats.problem.pill,"whoosh"]], meet:[[0,"breath"]], users:[[0,"whoosh"]],
 signals:[[beats.signals.session,"tick"],[beats.signals.video,"tick"],[beats.signals.negative,"tick"]],
 score:[[beats.score.settle,"tick"]],
 prompts:[[beats.prompts.reminder,"tick"],[beats.prompts.pause,"tick"],[beats.prompts.breathing,"tick"]],
 privacy:beats.privacy.points.map(f=>[f,"tick"]),
 impact:[[0,"whoosh"],...beats.impact.cards.map(f=>[f,"tick"])], outro:[], credits:[[0,"whoosh"]],
};
const keyframes = { hook:beats.hook.clock+14, problem:beats.problem.contrast.at(-1)+8, meet:beats.meet.helps.at(-1)+8, users:beats.users.platforms+22, signals:Math.round((beats.signals.captionReady+beats.signals.holdUntil)/2), score:beats.score.settle+6, prompts:beats.prompts.you.at(-1)+6, privacy:beats.privacy.points.at(-1)+5, impact:Math.max(beats.impact.headline.at(-1)+5,beats.impact.cards.at(-1)+6), outro:beats.outro.choose.at(-1)+6, credits:110 };
const timings = Object.fromEntries([...ids,"credits"].map(id=>[id,{
 frames:frames[id], keyFrame:Math.min(keyframes[id],frames[id]-6), cues:cues[id],
 ...(byId[id] ? {voAt:Math.round(byId[id].start*fps)-starts[id],voFrames:Math.ceil(byId[id].end*fps)-Math.round(byId[id].start*fps)} : {}),
}]));
const file = "// Measured from the supplied Lauren v4 recording; rebuilt by scripts/build-voice-cues.mjs.\n"+
 "export const MEASURED_BEATS = "+JSON.stringify(beats,null,2)+" as const;\n"+
 "export const SCENE_TIMINGS = "+JSON.stringify(timings,null,2)+" as const;\n";
writeFileSync("src/voiceover-cues.ts",file);
writeFileSync("production/voiceover/supplied-lauren-v4/scene-timings.json",JSON.stringify({starts,timings,beats},null,2)+"\n");
console.log(JSON.stringify({starts,frames,captionHoldSeconds:(beats.signals.holdUntil-beats.signals.captionReady)/fps,notGlobalFrame:starts.problem+beats.problem.contrast[0]},null,2));
