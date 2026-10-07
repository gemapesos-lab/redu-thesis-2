"""Refine local word timings within the recording's measured phrase breaks."""
from pathlib import Path
import json
import re
import stable_whisper
import torch

ROOT = Path(__file__).resolve().parent.parent
META = ROOT / 'production/voiceover/opening-revision'
PHRASES = [
    (0, 2.10, 'Just one more video...'),
    (2.20, 3.35, 'Then another.'),
    (3.46, 4.42, 'Suddenly,'),
    (4.42, 6.50, 'it’s two-oh-seven A.M.'),
    (6.60, 8.30, "That's doomscrolling."),
    (8.35, 10.38, 'Screen-time limits count minutes,'),
    (10.38, 11.82, 'NOT what you watch.'),
    (11.91, 13.02, 'Meet REDU:'),
    (13.08, 17.79, 'a privacy-first Android app that notices doomscrolling, and helps you pause.'),
    (17.86, 21.83, 'Built for adult Filipino Android users who watch short-form video.'),
    (21.88, 23.06, 'Three signals:'),
    (23.07, 24.02, 'session length,'),
    (24.02, 26.47, 'time per video, and negative captions.'),
    (26.51, 29.91, 'Fuzzy logic combines them into a doomscrolling risk score.'),
    (29.96, 32.66, 'When it rises, REDU steps in gently:'),
    (32.70, 33.68, 'a reminder,'),
    (33.68, 34.58, 'a pause,'),
    (34.58, 36.00, 'then a breathing break.'),
    (36.05, 37.72, 'You stay in control.'),
    (37.78, 39.32, 'It all runs on your phone,'),
    (39.32, 41.32, 'and raw content is never kept.'),
    (41.35, 44.14, 'In a two-week pilot with fifty Filipino adults,'),
    (44.15, 47.62, 'prompted users scrolled less, and saw less negative content.'),
    (47.69, 48.43, 'REDU.'),
    (48.49, 49.77, 'Notice the scroll.'),
    (49.83, 51.069388, 'Choose the pause.'),
]
canonical = lambda s: re.sub(r'[^a-z0-9]', '', s.lower())
text = (META / 'transcript.txt').read_text()
assert canonical(' '.join(p[2] for p in PHRASES)) == canonical(text)
torch.set_num_threads(4)
model = stable_whisper.load_model('small.en', device='cpu', download_root=str(ROOT / '.tools/models'))
result = model.align_words(str(ROOT / 'public/audio/vo/opening-revision-original.mp3'),
    [{'start': a, 'end': b, 'text': t} for a, b, t in PHRASES],
    language='en', verbose=False, regroup=False, dynamic_heads=True)
raw = result.to_dict()
(META / 'alignment-local-phrases-raw.json').write_text(json.dumps(raw, indent=2) + '\n')
raw_words = [w for segment in raw['segments'] for w in segment['words']]
words = []
position = 0
for expected in text.split():
    collected = []
    while canonical(''.join(w['word'] for w in collected)) != canonical(expected):
        collected.append(raw_words[position])
        position += 1
        assert len(canonical(''.join(w['word'] for w in collected))) <= len(canonical(expected))
    words.append({'text': expected, 'start': collected[0]['start'], 'end': collected[-1]['end']})
assert position == len(raw_words)
assert all(w['end'] > w['start'] for w in words)
prior = META / 'alignment-whole-take.json'
if not prior.exists():
    prior.write_bytes((META / 'alignment.json').read_bytes())
(META / 'alignment.json').write_text(json.dumps({
    'method': 'Local Stable-ts alignment within measured phrase boundaries',
    'model': 'OpenAI Whisper small.en', 'audioUploaded': False,
    'phraseWindows': PHRASES, 'words': words}, indent=2) + '\n')
for word in words:
    print(f"{word['start']:6.2f}–{word['end']:6.2f} {word['text']}")
