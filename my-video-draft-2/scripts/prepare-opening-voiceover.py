"""Keep the supplied take at 1x; allocate the spare time to the opening scroll."""
from pathlib import Path
from array import array
import hashlib
import json
import re
import subprocess

ROOT = Path(__file__).resolve().parent.parent
META = ROOT / 'production/voiceover/opening-revision'
SOURCE = ROOT / 'public/audio/vo/opening-revision-original.mp3'
SR = 48000
TARGET = 53.6

aligned = json.loads((META / 'alignment.json').read_text())
words = [w for w in aligned['words'] if w['text'].strip()]
canonical = lambda s: re.sub(r'[^a-z0-9]', '', s.lower())
paragraphs = [p.strip() for p in (META / 'transcript.txt').read_text().split('\n\n') if p.strip()]
assert canonical(' '.join(paragraphs)) == canonical(' '.join(w['text'] for w in words))
samples = array('f')
samples.frombytes(subprocess.check_output(['ffmpeg', '-v', 'error', '-i', str(SOURCE),
    '-ar', str(SR), '-ac', '1', '-f', 'f32le', '-']))
extra = round(TARGET * SR) - len(samples)
assert 0 <= extra <= 3 * SR, 'New recording needs a different timing budget.'

# The opening is the only added beat. The caption-to-score gap is untouched.
another = next(w for w in words if canonical(w['text']) == 'another')
suddenly = next(w for w in words if canonical(w['text']) == 'suddenly')
probe = subprocess.run(['ffmpeg', '-hide_banner', '-i', str(SOURCE), '-af',
    'silencedetect=noise=-45dB:d=0.08', '-f', 'null', '-'], capture_output=True, text=True, check=True)
gaps = []
start = None
for line in probe.stderr.splitlines():
    match = re.search(r'silence_start: ([\d.]+)', line)
    if match:
        start = float(match[1])
    match = re.search(r'silence_end: ([\d.]+)', line)
    if match and start is not None:
        a = max(start, another['end'] + 0.04)
        b = min(float(match[1]), suddenly['start'] - 0.04)
        if b - a > 0.02:
            gaps.append((a, b))
        start = None
assert gaps, 'No safe silent insertion point between another and Suddenly.'
a, b = max(gaps, key=lambda gap: gap[1] - gap[0])
insert_sample = round((a + b) / 2 * SR)
left, right = array('f', samples[:insert_sample]), array('f', samples[insert_sample:])
fade = round(0.003 * SR)
for i in range(fade):
    left[-1 - i] *= i / fade
    right[i] *= i / fade
edited = left + array('f', [0.0]) * extra + right
assert len(edited) == round(TARGET * SR)

def mapped(t):
    return t + (extra / SR if round(t * SR) >= insert_sample else 0)

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
            'start': mapped(word['start']), 'end': mapped(word['end'])})
    scenes.append({'id': ident, 'start': group[0]['start'], 'end': group[-1]['end'], 'words': group})
assert position == len(words)
for scene in scenes:
    for word in scene['words']:
        a, b = round(word['sourceStart'] * SR), round(word['sourceEnd'] * SR)
        dest = round(word['start'] * SR)
        assert samples[a:b].tobytes() == edited[dest:dest + b - a].tobytes(), word
by_id = {scene['id']: scene for scene in scenes}
caption_gap = by_id['score']['start'] - by_id['signals']['end']
assert 0 <= caption_gap < 0.8, 'The caption-to-score gap should remain a natural sentence break.'

output = ROOT / 'public/audio/vo/opening-revision-edited.wav'
subprocess.run(['ffmpeg', '-y', '-v', 'error', '-f', 'f32le', '-ar', str(SR), '-ac', '1',
    '-i', '-', '-c:a', 'pcm_s24le', str(output)], input=edited.tobytes(), check=True)
report = {
    'sourceSha256': hashlib.sha256(SOURCE.read_bytes()).hexdigest(),
    'sourceDecodedSeconds': len(samples) / SR,
    'productionSeconds': TARGET,
    'removedSilenceSeconds': 0,
    'insertedCaptionHoldSeconds': 0,
    'insertedOpeningScrollSeconds': extra / SR,
    'insertAtSourceSeconds': insert_sample / SR,
    'captionToScoreGapSeconds': caption_gap,
    'playbackRate': 1,
    'allSpokenWordsPreserved': True,
    'allSpokenSamplesPreservedBeforeNormalization': True,
}
(META / 'edit-map.json').write_text(json.dumps(report, indent=2) + '\n')
(META / 'words.json').write_text(json.dumps(scenes, indent=2) + '\n')
print(json.dumps(report, indent=2))
print(json.dumps([{k: s[k] for k in ['id', 'start', 'end']} for s in scenes], indent=2))
