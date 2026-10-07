// Measured from the supplied Lauren v4 recording; rebuilt by scripts/build-voice-cues.mjs.
export const MEASURED_BEATS = {
  "hook": {
    "words": [
      5,
      20,
      30,
      37
    ],
    "introExit": 54,
    "phoneAt": 54,
    "phoneExit": 99,
    "clock": 117,
    "swipes": [
      64,
      73,
      82,
      91
    ]
  },
  "problem": {
    "intro": [
      5,
      20
    ],
    "introExit": 42,
    "pill": 52,
    "limits": [
      52,
      70,
      80,
      91
    ],
    "contrast": [
      117,
      128,
      133,
      137
    ],
    "exit": 143
  },
  "meet": {
    "logo": 17,
    "eyebrow": [
      38,
      42,
      69,
      81
    ],
    "lift": 30,
    "notices": [
      98,
      114
    ],
    "helps": [
      146,
      155,
      159
    ],
    "exit": 169
  },
  "users": {
    "built": [
      4,
      13
    ],
    "headline": [
      18,
      30,
      48,
      61
    ],
    "supporting": [
      74,
      78,
      88,
      105,
      116
    ],
    "platforms": 88,
    "exit": 113
  },
  "signals": {
    "heading": [
      6,
      16
    ],
    "session": 41,
    "sessionWords": [
      41,
      53
    ],
    "video": 71,
    "videoWords": [
      71,
      79,
      84
    ],
    "negative": 110,
    "negativeWords": [
      110,
      122
    ],
    "captionStart": 86,
    "captionReady": 114,
    "highlight": 110,
    "holdUntil": 204
  },
  "score": {
    "fuzzy": 6,
    "logic": 16,
    "join": 30,
    "meter": 54,
    "title": [
      66,
      87,
      96
    ],
    "settle": 96,
    "exit": 102
  },
  "prompts": {
    "steps": [
      53,
      63,
      72
    ],
    "reminder": 96,
    "pause": 130,
    "breathing": 163,
    "you": [
      197,
      206,
      212,
      217
    ],
    "unlocked": 189,
    "exit": 226
  },
  "privacy": {
    "heading": [
      20,
      27,
      31,
      36
    ],
    "points": [
      20,
      59,
      102
    ],
    "exit": 108
  },
  "impact": {
    "weeks": 13,
    "adults": 47,
    "sessions": 45,
    "shrink": 100,
    "headline": [
      122,
      134,
      156,
      167,
      177,
      188
    ],
    "cards": [
      122,
      134,
      177,
      185
    ],
    "badges": 140,
    "footnote": 134,
    "exit": 202
  },
  "outro": {
    "logo": 6,
    "notice": [
      32,
      43,
      47
    ],
    "choose": [
      74,
      85,
      89
    ],
    "wink": 103,
    "exit": 108
  }
} as const;
export const SCENE_TIMINGS = {
  "hook": {
    "frames": 167,
    "keyFrame": 131,
    "cues": [
      [
        64,
        "swipe"
      ],
      [
        73,
        "swipe"
      ],
      [
        82,
        "swipe"
      ],
      [
        91,
        "swipe"
      ],
      [
        117,
        "hit"
      ]
    ],
    "voAt": 5,
    "voFrames": 152
  },
  "problem": {
    "frames": 153,
    "keyFrame": 145,
    "cues": [
      [
        52,
        "whoosh"
      ]
    ],
    "voAt": 5,
    "voFrames": 144
  },
  "meet": {
    "frames": 179,
    "keyFrame": 167,
    "cues": [
      [
        0,
        "breath"
      ]
    ],
    "voAt": 6,
    "voFrames": 167
  },
  "users": {
    "frames": 121,
    "keyFrame": 110,
    "cues": [
      [
        0,
        "whoosh"
      ]
    ],
    "voAt": 4,
    "voFrames": 112
  },
  "signals": {
    "frames": 212,
    "keyFrame": 159,
    "cues": [
      [
        41,
        "tick"
      ],
      [
        71,
        "tick"
      ],
      [
        110,
        "tick"
      ]
    ],
    "voAt": 6,
    "voFrames": 134
  },
  "score": {
    "frames": 110,
    "keyFrame": 102,
    "cues": [
      [
        96,
        "tick"
      ]
    ],
    "voAt": 6,
    "voFrames": 100
  },
  "prompts": {
    "frames": 236,
    "keyFrame": 223,
    "cues": [
      [
        96,
        "tick"
      ],
      [
        130,
        "tick"
      ],
      [
        163,
        "tick"
      ]
    ],
    "voAt": 5,
    "voFrames": 227
  },
  "privacy": {
    "frames": 116,
    "keyFrame": 107,
    "cues": [
      [
        20,
        "tick"
      ],
      [
        59,
        "tick"
      ],
      [
        102,
        "tick"
      ]
    ],
    "voAt": 5,
    "voFrames": 107
  },
  "impact": {
    "frames": 210,
    "keyFrame": 193,
    "cues": [
      [
        0,
        "whoosh"
      ],
      [
        122,
        "tick"
      ],
      [
        134,
        "tick"
      ],
      [
        177,
        "tick"
      ],
      [
        185,
        "tick"
      ]
    ],
    "voAt": 5,
    "voFrames": 199
  },
  "outro": {
    "frames": 116,
    "keyFrame": 95,
    "cues": [],
    "voAt": 6,
    "voFrames": 97
  },
  "credits": {
    "frames": 150,
    "keyFrame": 110,
    "cues": [
      [
        0,
        "whoosh"
      ]
    ]
  }
} as const;
