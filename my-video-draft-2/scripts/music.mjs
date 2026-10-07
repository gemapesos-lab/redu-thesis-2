// Original score and sound effects for the AVP, synthesized from scratch so the video needs
// no third-party audio license. Cue points come from src/timeline.ts; rerun after retiming.
//   node scripts/music.mjs
import { spawnSync } from "node:child_process";
import { mkdirSync, rmSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { HOOK, IMPACT } from "../src/choreography.ts";
import { BEATS, FPS, TOTAL_FRAMES, sceneStart } from "../src/timeline.ts";

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
  then: at("hook", BEATS.hook.then),
  rack: at("hook", HOOK.rack),
  flip: at("hook", HOOK.flip + 1),
  problem: at("problem"),
  breath: at("meet"),
  bloom: at("meet", 12),
  users: at("users"),
  signals: at("signals"),
  score: at("score"),
  prompts: at("prompts"),
  privacy: at("privacy"),
  impact: at("impact"),
  results: at("impact", IMPACT.cards[0]),
  outro: at("outro", 2),
  credits: at("credits"),
};

const music = new Bus(END + 1);
sendBus = new Bus(END + 1);

// Night: a low drone under the title; a pulse that quickens with the swipes; then
// the score drops out just before "Suddenly" and returns, darker, on the clock.
const hush = T.rack - 0.32;
pad(music, 0, hush, notes("A1 E2 A2"), { gain: 0.05, attack: 1.6, release: 0.25, harmonics: 4, send: 0.35, spread: 0.4 });
air(music, 0, hush, { gain: 0.06, cutoff: 480, attack: 1.4, release: 0.25 });
air(music, hush, T.flip, { gain: 0.012, cutoff: 300, attack: 0.2, release: 0.1 });
for (let t = T.then + 0.05, gap = 0.62; t < hush - 0.12; t += gap, gap = Math.max(0.27, gap * 0.86)) {
  kick(music, t, { gain: 0.05 + 0.05 * ((t - T.then) / (hush - T.then)), from: 90, to: 40, tau: 0.2 });
}
pad(music, T.flip, T.breath - 0.35, notes("A1 E2 A2"), { gain: 0.045, attack: 0.9, release: 0.8, harmonics: 4, send: 0.4, spread: 0.4 });
pad(music, T.problem, T.breath - 0.35, notes("E4 F4"), { gain: 0.006, attack: 2, release: 0.8, harmonics: 2, send: 0.9 });
air(music, T.flip, T.breath - 0.3, { gain: 0.05, cutoff: 480, attack: 0.8, release: 0.6 });
for (let t = T.problem + 0.25; t < T.breath - 0.9; t += 0.72) {
  kick(music, t, { gain: 0.05 + 0.03 * ((t - T.problem) / (T.breath - T.problem)), from: 80, to: 38, tau: 0.22 });
}
sweep(music, 0.6, T.breath - 1.35, 1.3, {
  gain: 0.05,
  fc: (k) => 250 * 14 ** k,
  env: (k) => k * k * (k > 0.93 ? (1 - k) / 0.07 : 1),
});

// Day: Fmaj9 → Cmaj9 → Am9 → G6/9, two bars each, from the logo bloom to the outro.
const beat = 60 / 92;
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

// Blooms: bell strums on the two logo reveals, and a warm lift as the pilot results land.
notes("F5 A5 C6 E6").forEach((m, i) => bell(music, T.bloom + i * 0.07, m, { gain: 0.035 - i * 0.005, pan: -0.4 + i * 0.27 }));
notes("C5 E5 G5 C6").forEach((m, i) => bell(music, T.results + i * 0.1, m, { gain: 0.022 - i * 0.003, pan: -0.3 + i * 0.2, tau: 1.6 }));
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

const written = [];
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
  written.push(name);
};

// A pitched transient: `f` falls from f * drop, decaying over `tau` seconds.
const ping = (bus, t0, f, { gain = 1, drop = 1.5, glide = 0.02, tau = 0.08, pan = 0, send = 0.25, harmonic = 0 } = {}) => {
  let phase = 0;
  render(bus, send, t0, tau * 6, pan, (t) => {
    const fk = f * (1 + (drop - 1) * Math.exp(-t / glide));
    phase += (2 * Math.PI * fk) / SR;
    return (Math.sin(phase) + harmonic * Math.sin(3 * phase)) * Math.min(1, t / 0.0015) * Math.exp(-t / tau) * gain;
  });
};

// A short, bright noise burst for clicks and taps.
const click = (bus, t0, { gain = 1, tau = 0.004, pan = 0 } = {}) => {
  let lp = 0;
  render(bus, 0.1, t0, tau * 8, pan, (t) => {
    const x = noise();
    lp += 0.45 * (x - lp);
    return (x - lp) * Math.exp(-t / tau) * gain;
  });
};

const whoosh = (bus, seconds, { from, peak, to, pan = [-0.3, 0.3], q = 0.9, shape = 1.6, mode = "band" }) =>
  sweep(bus, 0.4, 0, seconds, {
    gain: 1,
    fc: (k) => (k < 0.5 ? from + (peak - from) * Math.sin(Math.PI * k) : to + (peak - to) * Math.sin(Math.PI * k)),
    env: (k) => Math.sin(Math.PI * k) ** shape,
    pan: (k) => pan[0] + (pan[1] - pan[0]) * k,
    q,
    mode,
  });

// Hook: the phone, six swipes that rise in pitch, the rack focus and the clock.
effect("rise", 0.8, 0.3, (bus) => whoosh(bus, 0.7, { from: 180, peak: 900, to: 600, pan: [0, 0], mode: "low", shape: 2 }));
effect("land", 0.5, 0.26, (bus) => {
  kick(bus, 0, { gain: 0.5, from: 120, to: 60, tau: 0.05 });
  ping(bus, 0.004, 2600, { gain: 0.35, drop: 1.05, tau: 0.018 });
});
[0, 1, 2, 3, 4, 5].forEach((i) =>
  effect(`swipe${i + 1}`, 0.5, 0.42, (bus) => {
    const seconds = 0.32 - i * 0.025;
    sweep(bus, 0.3, 0, seconds, {
      gain: 1,
      fc: (k) => 800 * 1.12 ** i * 5.5 ** k,
      env: (k) => Math.sin(Math.PI * k) ** 2,
      pan: (k) => 0.3 - 0.6 * k,
      q: 0.8,
    });
    ping(bus, 0, 92, { gain: 0.5, drop: 1.4, glide: 0.01, tau: 0.03, send: 0 });
  }),
);
effect("rack", 1.1, 0.3, (bus) =>
  sweep(bus, 0.5, 0, 0.95, { gain: 1, fc: (k) => 300 + 1500 * k * k, env: (k) => (k < 0.8 ? (k / 0.8) ** 1.4 : (1 - k) / 0.2), q: 1.1 }),
);
effect("blip", 0.4, 0.3, (bus) => ping(bus, 0, 1760, { gain: 1, drop: 1, tau: 0.055, harmonic: 0.18, send: 0.4 }));
effect("sub", 2.6, 0.72, (bus) => {
  kick(bus, 0, { gain: 1, from: 72, to: 31, tau: 0.75 });
  air(bus, 0, 0.06, { gain: 0.4, cutoff: 260, attack: 0.002, release: 0.3 });
  bell(bus, 0.01, note("E6"), { gain: 0.07, tau: 1.1, send: 0.95 });
});

// Problem and Meet: swings, the minute ticks, the focus pull, the spin and the truck.
effect("swing", 0.8, 0.3, (bus) => whoosh(bus, 0.6, { from: 250, peak: 1300, to: 500, pan: [-0.2, 0.2] }));
effect("tick", 0.3, 0.32, (bus) => {
  click(bus, 0, { gain: 0.6, tau: 0.003 });
  ping(bus, 0.001, 3200, { gain: 0.5, drop: 1, tau: 0.012 });
});
effect("focus", 0.9, 0.24, (bus) =>
  sweep(bus, 0.5, 0, 0.75, { gain: 1, fc: (k) => 600 + 2000 * k, env: (k) => Math.sin(Math.PI * k) ** 1.4, q: 1 }),
);
effect("recede", 0.8, 0.26, (bus) => whoosh(bus, 0.6, { from: 1500, peak: 1200, to: 280, pan: [0, 0], shape: 1.2, mode: "low" }));
effect("breath", 1.6, 0.3, (bus) => {
  sweep(bus, 0.4, 0, 1.25, {
    gain: 1,
    fc: (k) => 700 + 900 * k,
    env: (k) => (k < 0.75 ? (k / 0.75) ** 1.5 : 1 - (k - 0.75) / 0.25),
    q: 1.1,
  });
});
effect("spin", 0.9, 0.32, (bus) =>
  sweep(bus, 0.4, 0, 0.75, {
    gain: 1,
    fc: (k) => 500 + 2000 * Math.abs(Math.sin(Math.PI * 2.5 * k)) * (1 - 0.6 * k),
    env: (k) => Math.sin(Math.PI * k) ** 1.2 * (1 - 0.5 * k),
    pan: (k) => 0.5 * Math.sin(Math.PI * 2.5 * k),
    q: 0.9,
  }),
);
effect("truck", 0.7, 0.26, (bus) => whoosh(bus, 0.5, { from: 400, peak: 1600, to: 500, pan: [0.6, -0.6] }));

// Users, Signals and Score: toggles, card pops, the marker, the push and the meter.
effect("toggle", 0.3, 0.3, (bus) => {
  click(bus, 0, { gain: 0.5 });
  ping(bus, 0, 1400, { gain: 0.5, drop: 1.1, tau: 0.02 });
  ping(bus, 0.026, 2100, { gain: 0.35, drop: 1.05, tau: 0.018 });
});
[["pop1", "E5"], ["pop2", "G5"], ["pop3", "C5"]].forEach(([name, n], i) =>
  effect(name, 0.5, 0.32, (bus) => {
    ping(bus, 0, hz(note(n)), { gain: 1, drop: 1.6, glide: 0.012, tau: i === 2 ? 0.16 : 0.11, harmonic: i === 2 ? 0.08 : 0.04, send: 0.35 });
    click(bus, 0, { gain: 0.25 });
  }),
);
effect("marker", 0.45, 0.24, (bus) =>
  sweep(bus, 0.2, 0, 0.28, { gain: 1, fc: (k) => 2200 + 2800 * k, env: (k) => Math.min(1, k / 0.1) * (1 - k) ** 0.8, pan: (k) => -0.2 + 0.4 * k, q: 0.7 }),
);
effect("push", 1.1, 0.3, (bus) =>
  sweep(bus, 0.4, 0, 0.95, { gain: 1, fc: (k) => 150 + 600 * k, env: (k) => (k < 0.7 ? k / 0.7 : (1 - k) / 0.3) ** 1.3, q: 0.9, mode: "low" }),
);
effect("whoosh", 1.1, 0.32, (bus) => whoosh(bus, 0.85, { from: 300, peak: 2900, to: 300, pan: [-0.5, 0.5], mode: "low" }));
effect("fly", 0.6, 0.26, (bus) => whoosh(bus, 0.45, { from: 900, peak: 2600, to: 900, pan: [-0.5, 0.5], shape: 1.4 }));
effect("lock", 0.7, 0.36, (bus) => {
  kick(bus, 0, { gain: 0.8, from: 120, to: 58, tau: 0.08 });
  [820, 1310, 2100].forEach((f, i) => ping(bus, 0.003, f, { gain: 0.18 - i * 0.04, drop: 1, tau: 0.15 - i * 0.03, send: 0.4 }));
});
effect("slide", 1, 0.24, (bus) =>
  sweep(bus, 0.25, 0, 0.8, { gain: 1, fc: (k) => 500 + 900 * k, env: (k) => Math.sin(Math.PI * k) * (0.75 + 0.25 * Math.sin(2 * Math.PI * 15 * k * 0.8)), q: 1.2 }),
);
effect("click", 0.3, 0.3, (bus) => {
  click(bus, 0, { gain: 0.6 });
  ping(bus, 0, 1900, { gain: 0.6, drop: 1.05, tau: 0.02 });
});

// Prompts: the riser, phones lifting, the reminder chime, the lock and unlock, the sink.
effect("riser", 1.2, 0.26, (bus) =>
  sweep(bus, 0.5, 0, 1.05, { gain: 1, fc: (k) => 300 * 8 ** k, env: (k) => k * k * (k > 0.9 ? (1 - k) / 0.1 : 1), q: 1 }),
);
effect("lift", 0.7, 0.26, (bus) => whoosh(bus, 0.5, { from: 300, peak: 1200, to: 900, pan: [0, 0], shape: 1.8 }));
effect("chime", 1.2, 0.3, (bus) => {
  bell(bus, 0, note("E6"), { gain: 0.5, tau: 0.45, send: 0.6 });
  bell(bus, 0.09, note("B5"), { gain: 0.45, tau: 0.55, send: 0.6 });
});
effect("lockclick", 0.4, 0.3, (bus) => {
  kick(bus, 0, { gain: 0.35, from: 140, to: 80, tau: 0.03 });
  ping(bus, 0.002, 900, { gain: 0.6, drop: 1.1, tau: 0.03 });
});
effect("unlock", 0.4, 0.3, (bus) => {
  ping(bus, 0, 1200, { gain: 0.6, drop: 1.05, tau: 0.025 });
  ping(bus, 0.05, 1800, { gain: 0.6, drop: 1.05, tau: 0.03 });
});
effect("sink", 0.8, 0.26, (bus) => whoosh(bus, 0.6, { from: 900, peak: 800, to: 200, pan: [0, 0], shape: 1.2, mode: "low" }));

// Privacy, Impact and Outro: checks, the dissolve, tile flaps, the count, the results, the wink.
effect("check", 0.4, 0.28, (bus) => {
  ping(bus, 0, 1480, { gain: 0.8, drop: 0.8, glide: 0.015, tau: 0.06, send: 0.35 });
  click(bus, 0, { gain: 0.2 });
});
effect("shimmer", 1.2, 0.24, (bus) => {
  for (let i = 0; i < 60; i++) {
    const t = Math.pow(rand(), 0.8) * 0.85;
    ping(bus, t, 2000 + rand() * 4500, { gain: 0.25 * (1 - t / 0.9), drop: 1, tau: 0.03 + rand() * 0.04, pan: rand() - 0.5, send: 0.6 });
  }
});
effect("flap", 0.5, 0.32, (bus) => {
  air(bus, 0, 0.03, { gain: 0.9, cutoff: 420, attack: 0.002, release: 0.05 });
  ping(bus, 0, 140, { gain: 0.7, drop: 1.3, glide: 0.01, tau: 0.06, send: 0.15 });
});
effect("roll", 1.1, 0.24, (bus) => {
  for (let i = 0, t = 0; i < 18; i++) {
    const u = i / 17;
    t += 0.025 + 0.045 * (1 - Math.sin(Math.PI * u));
    click(bus, t, { gain: 0.5, tau: 0.002 });
    ping(bus, t, 2500 + 400 * u, { gain: 0.3, drop: 1, tau: 0.008 });
  }
});
["G5", "E5", "C5", "A4"].forEach((n, i) =>
  effect(`down${i + 1}`, 0.5, 0.26, (bus) => ping(bus, 0, hz(note(n)), { gain: 1, drop: 1.25, glide: 0.02, tau: 0.1, harmonic: 0.05, send: 0.4 })),
);
effect("sparkle", 1, 0.24, (bus) => {
  notes("C7 G6 E7").forEach((m, i) => bell(bus, i * 0.07, m, { gain: 0.3 - i * 0.05, tau: 0.5, pan: -0.3 + i * 0.3, send: 0.8 }));
});

console.log(`music.mp3 ${END.toFixed(2)}s and ${written.length} effects in sfx/ written to public/audio: ${written.join(", ")}`);
