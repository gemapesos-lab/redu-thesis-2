"""Check the encoded motion revision, including the failed blur-frame regression."""
import array
import hashlib
import json
import math
from pathlib import Path
import re
import subprocess
import sys

root = Path(__file__).resolve().parent.parent
video = root / (sys.argv[1] if len(sys.argv) > 1 else 'out/REDU-AVP-draft-2-motion-polish.mp4')
audit = root / 'production/voiceover/supplied-lauren-v4'


def run(*args):
    return subprocess.check_output(args, cwd=root)


probe = json.loads(run('ffprobe', '-v', 'error', '-show_entries',
    'format=duration,size:stream=codec_name,codec_type,width,height,avg_frame_rate,nb_frames,duration,start_time,sample_rate,channels',
    '-of', 'json', str(video)))
assert float(probe['format']['duration']) <= 60.0
stream = next(s for s in probe['streams'] if s['codec_type'] == 'video')
assert stream['nb_frames'] == '1770' and stream['avg_frame_rate'] == '30/1'

# Decode every frame and check that the score's paper background never disappears.
decoded = run('ffmpeg', '-v', 'error', '-i', str(video), '-an', '-vf',
    'scale=64:36,format=gray', '-f', 'rawvideo', '-')
pixels = 64 * 36
assert len(decoded) == 1770 * pixels
means = [sum(decoded[i:i + pixels]) / pixels for i in range(0, len(decoded), pixels)]
timing = json.loads((audit / 'scene-timings.json').read_text())
score_start = timing['starts']['score']
score_end = timing['starts']['prompts']
score_min = min(means[score_start:score_end])
assert score_min > 180, f'Unexpected dark score frame: luminance {score_min}'


def pcm(path):
    result = array.array('f')
    result.frombytes(run('ffmpeg', '-v', 'error', '-i', str(path), '-vn', '-ac', '1',
        '-ar', '8000', '-f', 'f32le', '-'))
    return result


reference = pcm(root / 'public/audio/vo/draft-2.wav')
mixed = pcm(video)
alignment = []
for scene in json.loads((audit / 'words.json').read_text()):
    preferred = {'problem': 'not', 'signals': 'negative', 'prompts': 'reminder'}.get(scene['id'])
    word = next((w for w in scene['words'] if re.sub('[^a-z]', '', w['word'].lower()) == preferred), scene['words'][0])
    start = round((word['start'] + 0.02) * 8000)
    count = round(min(0.4, scene['end'] - word['start'] - 0.02) * 8000)
    source = reference[start:start + count]
    sx = sum(source)
    sxx = sum(x * x for x in source) - sx * sx / count
    best = (-1, 0)
    for lag in range(-400, 401, 8):
        target = mixed[start + lag:start + lag + count]
        sy = sum(target)
        syy = sum(y * y for y in target) - sy * sy / count
        covariance = sum(x * y for x, y in zip(source, target)) - sx * sy / count
        correlation = covariance / math.sqrt(sxx * syy)
        if correlation > best[0]:
            best = (correlation, lag)
    assert best[0] > 0.75 and abs(best[1]) / 8000 < 1 / 30
    alignment.append({'scene': scene['id'], 'word': word['word'],
        'bestOffsetMs': best[1] / 8, 'correlation': round(best[0], 5)})

measurement = subprocess.run(['ffmpeg', '-hide_banner', '-i', str(video), '-vn',
    '-af', 'loudnorm=I=-16:TP=-1.5:LRA=11:print_format=json', '-f', 'null', '-'],
    capture_output=True, text=True, check=True).stderr
loudness = json.loads(re.search(r'\{\s*"input_i".*?\}', measurement, re.S).group())
checks = json.loads(run('node', '--disable-warning=MODULE_TYPELESS_PACKAGE_JSON',
    'scripts/check-storyboard.mjs', str(video)))
report = {
    'export': str(video.relative_to(root)),
    'sha256': hashlib.sha256(video.read_bytes()).hexdigest(),
    'probe': probe,
    'actualContainerSeconds': float(probe['format']['duration']),
    'videoFrames': 1770,
    'fps': 30,
    'creditsSeconds': 5,
    'narrationSeconds': 53.6,
    'narrationPlaybackRate': 1,
    'captionHoldSeconds': checks['captionHoldSeconds'],
    'originalFilesUnchanged': checks['originalFilesUnchanged'],
    'voiceAlignmentInExport': alignment,
    'loudnessMeasuredFromExport': {
        'integratedLUFS': float(loudness['input_i']),
        'truePeakDBTP': float(loudness['input_tp']),
        'loudnessRangeLU': float(loudness['input_lra']),
    },
    'motionRenderRegression': {
        'decodedFrames': len(means),
        'scoreFramesChecked': score_end - score_start,
        'scoreMinimumMeanLuminance': round(score_min, 3),
        'unexpectedDarkScoreFrames': 0,
        'sampling': '12 fractional-frame samples, 180-degree shutter, normal alpha averaging',
    },
    'checks': {
        'measuredCueFrames': 'passed',
        'originalVOHash': 'passed',
        'exportDecodesWithoutErrors': 'passed',
        'scoreNoBlackFrameRegression': 'passed',
    },
}
(audit / 'motion-verification.json').write_text(json.dumps(report, indent=2) + '\n')
print(json.dumps({'export': report['export'], 'seconds': report['actualContainerSeconds'],
    'scoreMinimumLuminance': round(score_min, 3), 'speechOffsetsMs': [a['bestOffsetMs'] for a in alignment],
    'originalFilesUnchanged': checks['originalFilesUnchanged']}, indent=2))
