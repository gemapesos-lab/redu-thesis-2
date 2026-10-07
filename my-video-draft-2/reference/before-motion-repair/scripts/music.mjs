// Original score and sound effects for the AVP, synthesized from scratch so the video needs
// no third-party audio license. Cue points come from src/timeline.ts; rerun after retiming.
//   node scripts/music.mjs
import { spawnSync } from "node:child_process";
import { mkdirSync, rmSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { FPS, TOTAL_FRAMES, sceneStart } from "../src/timeline.ts";

const SR = 48000;
const OUT = join(import.meta.dirname, "..", "public", "audio");
const END = TOTAL_FRAMES / FPS;
const at = (id, frame = 0) => (sceneStart(id) + frame) / FPS;

let seed = 20261005;
const rand = () => {
  seed = (Math.imul(seed, 1664525) + 1013904223) >>> 0;
  return seed / 4294967296;
};
const noise = () => rand() * 2 - 1;
const hz = (midi) => 440 * 2 ** ((midi - 69) / 12);
const STEPS = { C: 0, D: 2, E: 4, F: 5, G: 7, A: 9, B: 11 };
const note = (name) => {
  const [, letter, accidental, octave] = name.match(/^([A-G])([#b]?)(-?\d)$/);
  return 12 * (Number(octave) + 1) + STEPS[letter] + (accidental === "#" ? 1 : accidental === "b" ? -1 : 0);
};
const notes = (names) => names.split(" ").map(note);
const panGains = (pan) => {
  const a = ((pan + 1) * Math.PI) / 4;
  return [Math.cos(a), Math.sin(a)];
};

class Bus {
  constructor(seconds) {
    this.n = Math.ceil(seconds * SR);
    this.l = new Float32Array(this.n);
    this.r = new Float32Array(this.n);
  }
  add(i, v, gl, gr) {
    if (i >= 0 && i < this.n) {
      this.l[i] += v * gl;
      this.r[i] += v * gr;
    }
  }
}

// Writes `fn(t)` into `dry` (and the reverb send) from t0 for `seconds`.
const render = (bus, send, t0, seconds, pan, fn) => {
  const start = Math.round(t0 * SR);
  const n = Math.round(seconds * SR);
  const [gl, gr] = panGains(pan);
  const fade = Math.min(n, Math.round(0.05 * SR));
  for (let i = 0; i < n; i++) {
    const tail = i > n - fade ? (n - i) / fade : 1;
    const v = fn(i / SR) * tail;
    bus.add(start + i, v, gl, gr);
    if (send && sendBus) {
      sendBus.add(start + i, v * send, gl, gr);
    }
  }
};

let sendBus = null;

// ---------------------------------------------------------------- instruments

const pad = (bus, t0, t1, chord, { gain = 0.03, attack = 1.5, release = 2.5, harmonics = 7, send = 0.7, spread = 0.8 } = {}) => {
  const hold = t1 - t0;
  chord.forEach((midi, k) => {
    const pan = chord.length > 1 ? (-0.5 + k / (chord.length - 1)) * spread : 0;
    for (const cents of [-6, 0, 6]) {
      const f = hz(midi) * 2 ** (cents / 1200);
      const phase = rand() * Math.PI * 2;
      const lfoRate = 0.09 + rand() * 0.12;
      const lfoPhase = rand() * Math.PI * 2;
      const amps = [];
      for (let h = 1; h <= harmonics; h++) {
        amps.push(1 / h ** 1.7);
      }
      render(bus, send, t0, hold + release, pan, (t) => {
        let env = t < attack ? 0.5 - 0.5 * Math.cos((Math.PI * t) / attack) : 1;
        if (t > hold) {
          env *= Math.exp((-(t - hold) * 4.5) / release);
        }
        env *= 0.82 + 0.18 * Math.sin(2 * Math.PI * lfoRate * t + lfoPhase);
        const w = 2 * Math.PI * f * t + phase;
        let s = 0;
        for (let h = 0; h < amps.length; h++) {
          s += amps[h] * Math.sin(w * (h + 1));
        }
        return s * env * gain;
      });
    }
  });
};

// FM electric piano: soft tine attack, warm body.
const keys = (bus, t0, midi, { gain = 0.07, pan = 0, send = 0.35, decay = 1 } = {}) => {
  const f = hz(midi);
  const tau = decay * (0.25 + 0.9 * Math.sqrt(440 / f));
  render(bus, send, t0, Math.min(6, tau * 5), pan, (t) => {
    const index = 1.5 * Math.exp(-t / 0.16) + 0.22;
    const env = Math.min(1, t / 0.004) * Math.exp(-t / tau);
    const tine = 0.05 * Math.sin(2 * Math.PI * f * 7 * t) * Math.exp(-t / 0.035);
    return (Math.sin(2 * Math.PI * f * t + index * Math.sin(2 * Math.PI * f * t)) + tine) * env * gain;
  });
};

const bell = (bus, t0, midi, { gain = 0.04, pan = 0, send = 0.85, tau = 2.2 } = {}) => {
  const f = hz(midi);
  render(bus, send, t0, tau * 5, pan, (t) => {
    const index = 2 * Math.exp(-t / 0.5) + 0.12;
    const env = Math.min(1, t / 0.003) * Math.exp(-t / tau);
    return Math.sin(2 * Math.PI * f * t + index * Math.sin(2 * Math.PI * f * 3.5 * t)) * env * gain;
  });
};

const bass = (bus, t0, t1, midi, { gain = 0.11, release = 0.9 } = {}) => {
  const f = hz(midi);
  const hold = t1 - t0;
  render(bus, 0.05, t0, hold + release, 0, (t) => {
    let env = Math.min(1, t / 0.08);
    if (t > hold) {
      env *= Math.exp((-(t - hold) * 5) / release);
    }
    return (Math.sin(2 * Math.PI * f * t) + 0.22 * Math.sin(4 * Math.PI * f * t) + 0.06 * Math.sin(6 * Math.PI * f * t)) * env * gain;
  });
};

const kick = (bus, t0, { gain = 0.2, from = 115, to = 46, tau = 0.16 } = {}) => {
  let phase = 0;
  render(bus, 0.02, t0, 0.7, 0, (t) => {
    const f = to + (from - to) * Math.exp(-t / 0.035);
    phase += (2 * Math.PI * f) / SR;
    return Math.sin(phase) * Math.exp(-t / tau) * Math.min(1, t / 0.002) * gain;
  });
};

const shaker = (bus, t0, { gain = 0.02, pan = 0.3 } = {}) => {
  let lp = 0;
  render(bus, 0.15, t0, 0.14, pan, (t) => {
    const x = noise();
    lp += 0.3 * (x - lp);
    return (x - lp) * Math.min(1, t / 0.006) * Math.exp(-t / 0.03) * gain;
  });
};

const air = (bus, t0, t1, { gain = 0.03, cutoff = 600, attack = 2, release = 1 } = {}) => {
  const a = 1 - Math.exp((-2 * Math.PI * cutoff) / SR);
  const hold = t1 - t0;
  let lp1 = 0;
  let lp2 = 0;
  render(bus, 0.3, t0, hold + release, 0, (t) => {
    lp1 += a * (noise() - lp1);
    lp2 += a * (lp1 - lp2);
    let env = Math.min(1, t / attack);
    if (t > hold) {
      env *= Math.max(0, 1 - (t - hold) / release);
    }
    return lp2 * env * gain;
  });
};

// Chamberlin state-variable filter over noise, sweeping `fc(k)` for k in 0..1.
const sweep = (bus, send, t0, seconds, { gain, fc, env, pan = () => 0, q = 0.6, mode = "band" }) => {
  let low = 0;
  let band = 0;
  const start = Math.round(t0 * SR);
  const n = Math.round(seconds * SR);
  for (let i = 0; i < n; i++) {
    const k = i / n;
    const f = 2 * Math.sin((Math.PI * Math.min(fc(k), 9000)) / SR);
    const x = noise();
    low += f * band;
    const high = x - low - q * band;
    band += f * high;
    const v = (mode === "band" ? band : low) * env(k) * gain;
    const [gl, gr] = panGains(pan(k));
    bus.add(start + i, v, gl, gr);
    if (send && sendBus) {
      sendBus.add(start + i, v * send, gl, gr);
    }
  }
};

// ---------------------------------------------------------------- reverb (Freeverb)

const freeverb = (bus, { room = 0.86, damp = 0.3 } = {}) => {
  const scale = SR / 44100;
  const combs = [1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617];
  const allpasses = [556, 441, 341, 225];
  const run = (input, offset) => {
    const out = new Float32Array(input.length);
    const cs = combs.map((len) => ({ buf: new Float32Array(Math.round((len + offset) * scale)), i: 0, store: 0 }));
    const as = allpasses.map((len) => ({ buf: new Float32Array(Math.round((len + offset) * scale)), i: 0 }));
    for (let n = 0; n < input.length; n++) {
      const x = input[n] * 0.015;
      let acc = 0;
      for (const c of cs) {
        const y = c.buf[c.i];
        c.store = y * (1 - damp) + c.store * damp;
        c.buf[c.i] = x + c.store * room;
        c.i = (c.i + 1) % c.buf.length;
        acc += y;
      }
      for (const a of as) {
        const b = a.buf[a.i];
        a.buf[a.i] = acc + b * 0.5;
        a.i = (a.i + 1) % a.buf.length;
        acc = b - acc;
      }
      out[n] = acc * 3;
    }
    return out;
  };
  return [run(bus.l, 0), run(bus.r, 23)];
};

// ---------------------------------------------------------------- output

const writeWav = (path, channels) => {
  const n = channels[0].length;
  const ch = channels.length;
  const b = Buffer.alloc(44 + n * ch * 2);
  b.write("RIFF", 0);
  b.writeUInt32LE(36 + n * ch * 2, 4);
  b.write("WAVE", 8);
  b.write("fmt ", 12);
  b.writeUInt32LE(16, 16);
  b.writeUInt16LE(1, 20);
  b.writeUInt16LE(ch, 22);
  b.writeUInt32LE(SR, 24);
  b.writeUInt32LE(SR * ch * 2, 28);
  b.writeUInt16LE(ch * 2, 32);
  b.writeUInt16LE(16, 34);
  b.write("data", 36);
  b.writeUInt32LE(n * ch * 2, 40);
  let o = 44;
  for (let i = 0; i < n; i++) {
    for (const c of channels) {
      b.writeInt16LE(Math.round(Math.max(-1, Math.min(1, c[i])) * 32767), o);
      o += 2;
    }
  }
  writeFileSync(path, b);
};

const normalize = (channels, peak) => {
  let max = 1e-9;
  for (const c of channels) {
    for (let i = 0; i < c.length; i++) {
      max = Math.max(max, Math.abs(c[i]));
    }
  }
  for (const c of channels) {
    for (let i = 0; i < c.length; i++) {
      c[i] *= peak / max;
    }
  }
  return channels;
};

const loudnorm = (input, output, target) => {
  const filter = `loudnorm=I=${target}:TP=-1.5:LRA=14`;
  const probe = spawnSync("ffmpeg", ["-hide_banner", "-i", input, "-af", `${filter}:print_format=json`, "-f", "null", "-"], { encoding: "utf8" });
  const m = JSON.parse(probe.stderr.slice(probe.stderr.lastIndexOf("{"), probe.stderr.lastIndexOf("}") + 1));
  const pass2 = `${filter}:measured_I=${m.input_i}:measured_TP=${m.input_tp}:measured_LRA=${m.input_lra}:measured_thresh=${m.input_thresh}:offset=${m.target_offset}:linear=true`;
  const res = spawnSync("ffmpeg", ["-y", "-loglevel", "error", "-i", input, "-af", pass2, "-ar", String(SR), "-c:a", "libmp3lame", "-b:a", "192k", output], { encoding: "utf8" });
  if (res.status !== 0) {
    throw new Error(res.stderr);
  }
};

// ---------------------------------------------------------------- the score

const T = {
  problem: at("problem"),
  breath: at("meet"),
  bloom: at("meet", 12),
  users: at("users"),
  signals: at("signals"),
  score: at("score"),
  prompts: at("prompts"),
  privacy: at("privacy"),
  impact: at("impact"),
  outro: at("outro", 2),
  credits: at("credits"),
};

const music = new Bus(END + 1);
sendBus = new Bus(END + 1);

// Night: a low drone, a slow pulse and a riser into the breath.
pad(music, 0, T.breath - 0.35, notes("A1 E2 A2"), { gain: 0.05, attack: 1.6, release: 0.8, harmonics: 4, send: 0.35, spread: 0.4 });
pad(music, T.problem, T.breath - 0.35, notes("E4 F4"), { gain: 0.006, attack: 2, release: 0.8, harmonics: 2, send: 0.9 });
air(music, 0, T.breath - 0.3, { gain: 0.06, cutoff: 480, attack: 1.4, release: 0.6 });
const beat = 60 / 92;
for (let t = 0.3; t < T.breath - 0.7; t += beat) {
  kick(music, t, { gain: 0.06 + 0.05 * (t / T.breath), from: 90, to: 40, tau: 0.2 });
}
sweep(music, 0.6, T.breath - 1.35, 1.3, {
  gain: 0.05,
  fc: (k) => 250 * 14 ** k,
  env: (k) => k * k * (k > 0.93 ? (1 - k) / 0.07 : 1),
});

// Day: Fmaj9 → Cmaj9 → Am9 → G6/9, two bars each, from the logo bloom to the outro.
const chordLen = beat * 8;
const PROGRESSION = [
  { pad: "A3 C4 E4 G4", bass: "F2", arp: "F4 A4 C5 E5 G5" },
  { pad: "G3 B3 D4 E4", bass: "C3", arp: "E4 G4 B4 C5 D5" },
  { pad: "G3 B3 C4 E4", bass: "A2", arp: "C4 E4 G4 A4 B4" },
  { pad: "A3 B3 D4 E4", bass: "G2", arp: "D4 E4 G4 A4 B4" },
];
const PATTERN = [0, 2, 4, 3, 1, 2, 4, 2];
for (let c = 0, t = T.bloom; t < T.outro - 0.2; c++, t += chordLen) {
  const chord = PROGRESSION[c % PROGRESSION.length];
  const t1 = Math.min(t + chordLen, T.outro - 0.1);
  pad(music, t, t1, notes(chord.pad), { gain: 0.022, attack: c === 0 ? 0.9 : 0.6, release: 1.6 });
  bass(music, t, t1 - 0.05, note(chord.bass), { gain: c === 0 ? 0.05 : 0.065 });
  const arp = notes(chord.arp);
  for (let k = 0; k < 16; k++) {
    const tk = t + (k * beat) / 2 + (rand() - 0.5) * 0.012;
    if (tk < T.users || tk > T.outro - 0.35) {
      continue;
    }
    const lift = tk >= T.prompts ? 1.15 : 1;
    keys(music, tk, arp[PATTERN[k % 8]], { gain: (k % 4 === 0 ? 0.06 : 0.042) * lift * (0.9 + rand() * 0.2), pan: k % 2 ? 0.32 : -0.32 });
    if (tk >= T.prompts && k % 2 === 1) {
      keys(music, tk, arp[PATTERN[(k + 3) % 8]] + 12, { gain: 0.018, pan: 0.55, send: 0.6 });
    }
    const drums = tk >= T.signals && !(tk >= T.privacy && tk < T.impact);
    if (drums && k % 4 === 0) {
      kick(music, t + (k * beat) / 2, { gain: 0.085 });
    }
    if (tk >= T.score && drums && k % 2 === 1) {
      shaker(music, t + (k * beat) / 2, { gain: 0.016, pan: k % 4 === 1 ? 0.35 : -0.25 });
    }
  }
}

// Blooms: bell strums on the two logo reveals.
notes("F5 A5 C6 E6").forEach((m, i) => bell(music, T.bloom + i * 0.07, m, { gain: 0.035 - i * 0.005, pan: -0.4 + i * 0.27 }));
notes("C5 E5 G5 B5 D6").forEach((m, i) => bell(music, T.outro + i * 0.08, m, { gain: 0.035 - i * 0.004, pan: -0.45 + i * 0.22 }));

// Home: Cmaj9 under the tagline and credits, fading out at the end.
pad(music, T.outro, END - 2.2, notes("G3 B3 D4 E4"), { gain: 0.024, attack: 0.5, release: 2 });
bass(music, T.outro, END - 2.4, note("C3"), { gain: 0.06, release: 1.8 });
[
  [T.credits + 0.6, "G4"],
  [T.credits + 1.9, "E5"],
  [T.credits + 3.2, "D5"],
].forEach(([t, n]) => keys(music, t, note(n), { gain: 0.04, send: 0.6, decay: 1.6 }));

const [wl, wr] = freeverb(sendBus);
const mix = [new Float32Array(music.n), new Float32Array(music.n)];
for (let i = 0; i < music.n; i++) {
  const t = i / SR;
  const fade = Math.min(1, t / 0.25) * Math.max(0, Math.min(1, (END - 0.1 - t) / 2.6));
  mix[0][i] = Math.tanh((music.l[i] + wl[i] * 0.32) * 1.2) * fade;
  mix[1][i] = Math.tanh((music.r[i] + wr[i] * 0.32) * 1.2) * fade;
}

mkdirSync(join(OUT, "sfx"), { recursive: true });
const tmp = join(OUT, "music.tmp.wav");
writeWav(tmp, normalize(mix, 0.9));
loudnorm(tmp, join(OUT, "music.mp3"), -19);
rmSync(tmp);

// ---------------------------------------------------------------- sound effects

const effect = (name, seconds, peak, build) => {
  const bus = new Bus(seconds);
  sendBus = new Bus(seconds);
  build(bus);
  const [rl, rr] = freeverb(sendBus, { room: 0.7, damp: 0.4 });
  for (let i = 0; i < bus.n; i++) {
    bus.l[i] += rl[i] * 0.25;
    bus.r[i] += rr[i] * 0.25;
  }
  writeWav(join(OUT, "sfx", `${name}.wav`), normalize([bus.l, bus.r], peak));
};

effect("swipe", 0.45, 0.42, (bus) =>
  sweep(bus, 0.3, 0, 0.3, {
    gain: 1,
    fc: (k) => 900 * 6 ** k,
    env: (k) => Math.sin(Math.PI * k) ** 2,
    pan: (k) => -0.35 + 0.7 * k,
    q: 0.8,
  }),
);

effect("whoosh", 1.1, 0.32, (bus) =>
  sweep(bus, 0.5, 0, 0.85, {
    gain: 1,
    fc: (k) => 300 + 2600 * Math.sin(Math.PI * k) ** 2,
    env: (k) => Math.sin(Math.PI * k) ** 1.6,
    pan: (k) => -0.5 + k,
    q: 0.9,
    mode: "low",
  }),
);

effect("tick", 0.3, 0.32, (bus) => {
  let phase = 0;
  render(bus, 0.25, 0, 0.15, 0, (t) => {
    const f = 1250 + 900 * Math.exp(-t / 0.015);
    phase += (2 * Math.PI * f) / SR;
    return Math.sin(phase) * Math.min(1, t / 0.0015) * Math.exp(-t / 0.024);
  });
});

effect("breath", 1.6, 0.3, (bus) => {
  sweep(bus, 0.4, 0, 1.25, {
    gain: 1,
    fc: (k) => 700 + 900 * k,
    env: (k) => (k < 0.75 ? (k / 0.75) ** 1.5 : 1 - (k - 0.75) / 0.25),
    q: 1.1,
  });
});

effect("hit", 2.4, 0.7, (bus) => {
  kick(bus, 0, { gain: 1, from: 95, to: 38, tau: 0.55 });
  air(bus, 0, 0.05, { gain: 0.5, cutoff: 320, attack: 0.002, release: 0.12 });
  bell(bus, 0.01, note("C6"), { gain: 0.12, tau: 0.8, send: 0.9 });
});

console.log(`music.mp3 ${END.toFixed(2)}s and sfx/{swipe,whoosh,tick,breath,hit}.wav written to public/audio`);
