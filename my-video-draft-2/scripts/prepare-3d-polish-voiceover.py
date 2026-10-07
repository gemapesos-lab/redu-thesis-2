"""Keep the supplied 3D-polish take at 1x; add silence at two measured pauses.

  .tools/voice-alignment/bin/python scripts/align-ctc-voiceover.py 3d-polish
  python3 scripts/prepare-3d-polish-voiceover.py
"""
from pathlib import Path
from array import array
import hashlib
import json
import re
import subprocess

ROOT = Path(__file__).resolve().parent.parent
META = ROOT / 'production/voiceover/3d-polish'
SOURCE = ROOT / 'public/audio/vo/3d-polish-original.mp3'
SR = 48000
# Silence added inside existing pauses: the accelerating scroll before "Suddenly",
# and room for the stat tiles to clear between "adults," and "prompted".
INSERTS = [('another', 1, 'suddenly', 0.6), ('adults', 0, 'prompted', 0.3)]

aligned = json.loads((META / 'alignment.json').read_text())
words = [w for w in aligned['words'] if w['text'].strip()]
canonical = lambda s: re.sub(r'[^a-z0-9]', '', s.lower())
paragraphs = [p.strip() for p in (META / 'transcript.txt').read_text().split('\n\n') if p.strip()]
assert canonical(' '.join(paragraphs)) == canonical(' '.join(w['text'] for w in words))
samples = array('f')
samples.frombytes(subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(SOURCE),
    '-ar', str(SR), '-ac', '1', '-f', 'f32le', '-']))
probe = subprocess.run(['ffmpeg', '-hide_banner', '-i', str(SOURCE), '-af',
    'silencedetect=noise=-45dB:d=0.08', '-f', 'null', '-'], capture_output=True, text=True, check=True)
silences = []
start = None
for line in probe.stderr.splitlines():
    match = re.search(r'silence_start: ([\d.]+)', line)
    if match:
        start = float(match[1])
    match = re.search(r'silence_end: ([\d.]+)', line)
    if match and start is not None:
        silences.append((start, float(match[1])))
        start = None

def nth(word, n):
    return [w for w in words if canonical(w['text']) == word][n]

cuts = []
for before, n, after, seconds in INSERTS:
    left_word, right_word = nth(before, n), nth(after, 0)
    gaps = [(max(a, left_word['end'] + 0.04), min(b, right_word['start'] - 0.04)) for a, b in silences]
    gaps = [(a, b) for a, b in gaps if b - a > 0.02]
    assert gaps, f'No safe silent insertion point between {before} and {after}.'
    a, b = max(gaps, key=lambda gap: gap[1] - gap[0])
    cuts.append((round((a + b) / 2 * SR), round(seconds * SR), before, after))

edited = array('f')
previous = 0
fade = round(0.003 * SR)
for at, length, _, _ in cuts:
    piece = array('f', samples[previous:at])
    if previous:
        for i in range(fade):
            piece[i] *= i / fade
    for i in range(fade):
        piece[-1 - i] *= i / fade
    edited += piece + array('f', [0.0]) * length
    previous = at
tail = array('f', samples[previous:])
for i in range(fade):
    tail[i] *= i / fade
edited += tail
assert len(edited) == len(samples) + sum(length for _, length, _, _ in cuts)

def mapped(t):
    return t + sum(length for at, length, _, _ in cuts if round(t * SR) >= at) / SR

ids = ['hook', 'problem', 'meet', 'users', 'signals', 'score', 'prompts', 'privacy', 'impact', 'outro']
assert len(paragraphs) == len(ids)
scenes = []
position = 0
for ident, paragraph in zip(ids, paragraphs):
    group = []
    for expected in paragraph.split():
        word = words[position]
        position += 1
        assert canonical(expected) == canonical(word['text']), (expected, word)
        group.append({'word': expected, 'sourceStart': word['start'], 'sourceEnd': word['end'],
            'start': mapped(word['start']), 'end': mapped(word['end']),
            **({'parts': [{**part, 'start': mapped(part['start']), 'end': mapped(part['end'])} for part in word['parts']]}
               if 'parts' in word else {})})
    scenes.append({'id': ident, 'start': group[0]['start'], 'end': group[-1]['end'], 'words': group})
assert position == len(words)
for scene in scenes:
    for word in scene['words']:
        a, b = round(word['sourceStart'] * SR), round(word['sourceEnd'] * SR)
        dest = round(word['start'] * SR)
        assert samples[a:b].tobytes() == edited[dest:dest + b - a].tobytes(), word

output = ROOT / 'public/audio/vo/3d-polish-edited.wav'
subprocess.run(['ffmpeg', '-y', '-v', 'error', '-f', 'f32le', '-ar', str(SR), '-ac', '1',
    '-i', '-', '-c:a', 'pcm_s24le', str(output)], input=edited.tobytes(), check=True)
report = {
    'sourceSha256': hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
    'sourceDecodedSeconds': len(samples) / SR,
    'productionSeconds': len(edited) / SR,
    'removedSilenceSeconds': 0,
    'insertedSilence': [{'afterWord': before, 'beforeWord': after, 'atSourceSeconds': at / SR, 'seconds': length / SR}
        for at, length, before, after in cuts],
    'playbackRate': 1,
    'allSpokenWordsPreserved': True,
    'allSpokenSamplesPreservedBeforeNormalization': True,
}
(META / 'edit-map.json').write_text(json.dumps(report, indent=2) + '\n')
(META / 'words.json').write_text(json.dumps(scenes, indent=2) + '\n')
print(json.dumps(report, indent=2))
print(json.dumps([{k: round(s[k], 3) if k != 'id' else s[k] for k in ['id', 'start', 'end']} for s in scenes], indent=2))
