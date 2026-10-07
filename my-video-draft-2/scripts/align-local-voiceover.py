"""Offline forced alignment. Only public model weights are downloaded."""
from pathlib import Path
import json
import re
import stable_whisper
import torch

ROOT = Path(__file__).resolve().parent.parent
META = ROOT / 'production/voiceover/opening-revision'
output = META / 'alignment.json'
assert not output.exists(), 'Alignment already exists; inspect it before rerunning.'
torch.set_num_threads(4)
text = (META / 'transcript.txt').read_text()
model = stable_whisper.load_model('small.en', device='cpu', download_root=str(ROOT / '.tools/models'))
result = model.align(str(ROOT / 'public/audio/vo/opening-revision-original.mp3'), text,
    language='en', original_split=True, verbose=False, token_step=100, dynamic_heads=True)
assert result is not None
raw = result.to_dict()
(META / 'alignment-local-raw.json').write_text(json.dumps(raw, indent=2) + '\n')
raw_words = [w for segment in raw['segments'] for w in segment['words']]
canonical = lambda s: re.sub(r'[^a-z0-9]', '', s.lower())
assert canonical(' '.join(w['word'] for w in raw_words)) == canonical(text)
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
data = {'method': 'Local Stable-ts forced alignment', 'model': 'OpenAI Whisper small.en',
    'audioUploaded': False, 'words': words}
output.write_text(json.dumps(data, indent=2) + '\n')
print(json.dumps({'output': str(output), 'words': len(words), 'first': words[0], 'last': words[-1], 'audioUploaded': False}, indent=2))
