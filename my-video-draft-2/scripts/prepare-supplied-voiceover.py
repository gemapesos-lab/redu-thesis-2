"""Tighten measured silence only; preserve every spoken sample at 1x speed."""
from pathlib import Path
from array import array
import hashlib
import json
import math
import re
import subprocess
import wave

ROOT = Path(__file__).resolve().parent.parent
META = ROOT / 'production/voiceover/supplied-lauren-v4'
SOURCE = ROOT / 'public/audio/vo/draft-2-original.mp3'
SR = 48000
TARGET = 53.6
INSERT_AT = 27.2
INSERT_DURATION = 2.1

aligned = json.loads((META / 'alignment.json').read_text())
words = [w for w in aligned['words'] if w['text'].strip()]
canonical = lambda s: re.sub(r'[^a-z0-9]', '', s.lower())
paragraphs = [p.strip() for p in (ROOT / 'VOICEOVER.txt').read_text().split('\n\n') if p.strip()]
assert canonical(' '.join(paragraphs)) == canonical(' '.join(w['text'] for w in words)), 'Transcript differs from approved copy.'
raw = subprocess.check_output(['ffmpeg','-v','error','-i',str(SOURCE),'-ar',str(SR),'-ac','1','-f','f32le','-'])
samples = array('f'); samples.frombytes(raw)
duration = len(samples) / SR
probe = subprocess.run(['ffmpeg','-hide_banner','-i',str(SOURCE),'-af','silencedetect=noise=-45dB:d=0.06','-f','null','-'],capture_output=True,text=True,check=True)
gaps = []
start = None
for line in probe.stderr.splitlines():
    m = re.search(r'silence_start: ([\d.]+)', line)
    if m: start = float(m[1])
    m = re.search(r'silence_end: ([\d.]+)', line)
    if m and start is not None:
        gaps.append((start,float(m[1]))); start=None
assert any(a < INSERT_AT < b for a,b in gaps), 'Caption hold must be inserted in measured silence.'

# Intersect acoustic silence with word boundaries, leaving a 40 ms phonetic guard.
candidates = []
for a,b in gaps:
    if a <= INSERT_AT <= b: continue
    for prev,nxt in zip(words,words[1:]):
        lo,hi = max(a,prev['end']+0.04), min(b,nxt['start']-0.04)
        retain = 0.55 if lo < 3 else 0.32 if lo > 51 else 0.18
        capacity = hi-lo-retain
        if capacity > 0.035:
            candidates.append({'silenceStart':lo,'silenceEnd':hi,'capacity':capacity,'after':prev['text'],'before':nxt['text']})
remove_samples = len(samples) + round(INSERT_DURATION*SR) - round(TARGET*SR)
capacity_samples = sum(math.floor(c['capacity']*SR) for c in candidates)
assert 0 <= remove_samples <= capacity_samples, f'Need {remove_samples/SR:.3f}s of safe silence; only {capacity_samples/SR:.3f}s available.'
factor = remove_samples / capacity_samples
counts = [math.floor(math.floor(c['capacity']*SR)*factor) for c in candidates]
left = remove_samples-sum(counts)
for i,c in enumerate(candidates):
    extra = min(left, math.floor(c['capacity']*SR)-counts[i])
    counts[i] += extra; left -= extra
assert left == 0
cuts = []
for candidate,n in zip(candidates,counts):
    center = round((candidate['silenceStart']+candidate['silenceEnd'])/2*SR)
    a,b = center-n//2,center-n//2+n
    assert all(b/SR <= w['start']-0.035 or a/SR >= w['end']+0.035 for w in words), 'A silence cut would touch a spoken word.'
    cuts.append({**candidate,'fromSample':a,'toSample':b,'removedSeconds':n/SR})

events = [(c['fromSample'],c['toSample'],'cut') for c in cuts]
events.append((round(INSERT_AT*SR),round(INSERT_AT*SR),'insert'))
events.sort()
pieces=[]; cursor=0
for a,b,kind in events:
    assert a >= cursor
    piece=array('f',samples[cursor:a])
    # Tiny fades are entirely inside measured silence, preventing edit clicks.
    fade=min(round(0.003*SR),len(piece))
    if cursor > 0:
        for k in range(fade): piece[k] *= k/fade
    for k in range(fade): piece[len(piece)-1-k] *= k/fade
    pieces.append(piece)
    if kind=='insert': pieces.append(array('f',[0.0])*round(INSERT_DURATION*SR))
    cursor=b
pieces.append(array('f',samples[cursor:]))
edited=array('f')
for piece in pieces: edited.extend(piece)
assert len(edited)==round(TARGET*SR)

def mapped(t):
    assert all(not(c['fromSample']/SR < t < c['toSample']/SR) for c in cuts), 'Cannot map a timestamp inside removed silence.'
    removed=sum(c['toSample']-c['fromSample'] for c in cuts if c['toSample']/SR <= t)/SR
    return t-removed+(INSERT_DURATION if t >= INSERT_AT else 0)

ids=['hook','problem','meet','users','signals','score','prompts','privacy','impact','outro']
scenes=[]; position=0
for ident,paragraph in zip(ids,paragraphs):
    group=[]
    for expected in paragraph.split():
        word=words[position];position+=1
        assert canonical(expected)==canonical(word['text']), (expected,word)
        group.append({'word':expected,'sourceStart':word['start'],'sourceEnd':word['end'],'start':mapped(word['start']),'end':mapped(word['end'])})
    scenes.append({'id':ident,'start':group[0]['start'],'end':group[-1]['end'],'words':group})
assert position==len(words)
for scene in scenes:
    for w in scene['words']:
        assert abs((w['end']-w['start'])-(w['sourceEnd']-w['sourceStart'])) < 1/SR
        a,b = round(w['sourceStart']*SR),round(w['sourceEnd']*SR)
        dest = round(w['start']*SR)
        assert samples[a:b].tobytes() == edited[dest:dest+b-a].tobytes(), f"Spoken samples changed: {w['word']}"

output=ROOT / 'public/audio/vo/draft-2-edited.wav'
subprocess.run(['ffmpeg','-y','-v','error','-f','f32le','-ar',str(SR),'-ac','1','-i','-','-c:a','pcm_s24le',str(output)],input=edited.tobytes(),check=True)
report={'sourceSha256':hashlib.sha256(SOURCE.read_bytes()).hexdigest(),'sourceDecodedSeconds':duration,'productionSeconds':TARGET,'removedSilenceSeconds':remove_samples/SR,'insertedCaptionHoldSeconds':INSERT_DURATION,'insertAtSourceSeconds':INSERT_AT,'playbackRate':1,'allSpokenWordsPreserved':True,'allSpokenSamplesPreservedBeforeNormalization':True,'cuts':cuts}
(META/'edit-map.json').write_text(json.dumps(report,indent=2)+'\n')
(META/'words.json').write_text(json.dumps(scenes,indent=2)+'\n')
print(json.dumps({k:v for k,v in report.items() if k!='cuts'},indent=2))
print(json.dumps([{k:s[k] for k in ['id','start','end']} for s in scenes],indent=2))
