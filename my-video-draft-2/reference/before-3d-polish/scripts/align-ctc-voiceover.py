"""Align this recording locally with a frame-level acoustic model; no uploads."""
from pathlib import Path
import json
import re
import subprocess
import numpy as np
import torch
import torchaudio

ROOT = Path(__file__).resolve().parent.parent
META = ROOT / 'production/voiceover/opening-revision'
torch.set_num_threads(4)
bundle = torchaudio.pipelines.WAV2VEC2_ASR_BASE_960H
model = bundle.get_model(dl_kwargs={'model_dir': str(ROOT / '.tools/models')}).eval()
labels = bundle.get_labels()
dictionary = {c: i for i, c in enumerate(labels)}
audio = np.frombuffer(subprocess.check_output(['ffmpeg', '-v', 'error', '-i',
    str(ROOT / 'public/audio/vo/opening-revision-original.mp3'), '-ar', '16000', '-ac', '1', '-f', 'f32le', '-']), dtype=np.float32).copy()
original = (META / 'transcript.txt').read_text().split()
chars = []
ranges = []
for word in original:
    normalized = re.sub('[^A-Z\'|]', '', word.upper().replace('’', "'").replace('-', '|').replace('.', '|')).strip('|')
    if chars:
        chars.append('|')
    start = len(chars)
    chars.extend(normalized)
    ranges.append((start, len(chars)))
targets = torch.tensor([[dictionary[c] for c in chars]], dtype=torch.int32)
with torch.inference_mode():
    emissions, _ = model(torch.from_numpy(audio).unsqueeze(0))
    log_probs = emissions.log_softmax(-1)
    path, score = torchaudio.functional.forced_align(log_probs, targets, blank=0)
    spans = torchaudio.functional.merge_tokens(path[0], score[0].exp())
assert len(spans) == len(chars)
assert [span.token for span in spans] == targets[0].tolist()
ratio = len(audio) / 16000 / emissions.shape[1]
words = []
for word, (a, b) in zip(original, ranges):
    group = spans[a:b]
    words.append({'text': word, 'start': round(group[0].start * ratio, 5),
        'end': round(group[-1].end * ratio, 5),
        'acousticConfidence': round(sum(s.score for s in group) / len(group), 4)})
assert all(w['end'] > w['start'] for w in words)
prior = META / 'alignment-whole-take.json'
if not prior.exists():
    prior.write_bytes((META / 'alignment.json').read_bytes())
data = {'method': 'Local CTC forced alignment', 'model': 'PyTorch Wav2Vec2 ASR Base 960h',
    'audioUploaded': False, 'frameSeconds': ratio, 'words': words}
(META / 'alignment-ctc-raw.json').write_text(json.dumps(data, indent=2) + '\n')
# CTC letters peak after the initial consonant. Place readable cues near its
# attack, and snap phrase starts to measured silence ends where available.
probe = subprocess.run(['ffmpeg', '-hide_banner', '-i',
    str(ROOT / 'public/audio/vo/opening-revision-original.mp3'), '-af',
    'silencedetect=noise=-45dB:d=0.08', '-f', 'null', '-'], capture_output=True, text=True, check=True)
silence_ends = [float(t) for t in re.findall(r'silence_end: ([\d.]+)', probe.stderr)]
for index, word in enumerate(words):
    raw_start = word['start']
    near = [t for t in silence_ends if raw_start - 0.18 <= t <= raw_start + 0.03 and t < word['end']]
    adjusted = min(near, key=lambda t: abs(raw_start - t)) if near else raw_start - 0.03
    word['ctcStart'] = raw_start
    word['start'] = round(max(0, words[index - 1]['end'] if index else 0, adjusted), 5)
    word['onsetRefinement'] = 'measured silence end' if near else '30ms consonant attack allowance'
data['method'] += ' with acoustic phrase-start refinement'
(META / 'alignment.json').write_text(json.dumps(data, indent=2) + '\n')
for word in words:
    print(f"{word['start']:6.2f}–{word['end']:6.2f} {word['text']}")
