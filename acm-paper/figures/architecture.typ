// Figure 1 for the ACM paper: three-layer, on-device architecture.
// Re-laid out from the thesis figure (chapters/07_chapter3.typ) so that it
// fits the full 7.0in ACM text width at about 3in tall with text >= 6.5pt.
#import "../../chapters/utils.typ": figure_chip

#set page(width: 7in, height: auto, margin: 0pt)
#set text(font: "Times New Roman", size: 7pt)
#set par(justify: false, first-line-indent: 0pt, leading: 0.6em, spacing: 0pt)

#let node(title, body) = rect(
  width: 100%,
  stroke: 0.7pt + black,
  radius: 3pt,
  inset: (x: 4pt, y: 3.2pt),
)[
  #align(center)[#text(size: 7.4pt, weight: "bold")[#title] \ #text(size: 6.8pt)[#body]]
]

#let layer(title, ..nodes) = rect(
  width: 100%,
  stroke: 1pt + black,
  radius: 6pt,
  inset: (x: 6pt, y: 5pt),
)[
  #align(center)[#figure_chip(title)]
  #v(5pt)
  #stack(dir: ttb, spacing: 3.2pt, ..nodes.pos())
]

#let arrow = align(center + horizon)[#text(size: 13pt, weight: "bold")[→]]

#rect(
  width: 100%,
  stroke: (paint: black, thickness: 1pt, dash: "dashed"),
  radius: 8pt,
  inset: (x: 7pt, y: 6pt),
)[
  #align(center)[#figure_chip("Edge-Computing Boundary")]
  #v(3pt)
  #align(center)[#text(size: 6.8pt)[All processing stays on-device; no cloud transmission]]
  #v(5pt)
  #grid(
    columns: (1fr, 0.08fr, 1fr, 0.08fr, 1fr),
    align: top,
    layer(
      [Data Access Layer],
      node([Target Platforms], [TikTok · Facebook Reels · Instagram Reels]),
      node([Accessibility Capture], [Event listening · Node traversal]),
      node([Text / No-Text Check], [Captions and comments screening]),
      node([Session Logger], [Duration, swipe count, app-state events]),
      node([`takeScreenshot` Capture], [No-text path only · transient RAM-only frame]),
      node([Local Storage], [Aggregate metrics only · no raw text or frames]),
    ),
    pad(top: 80pt, arrow),
    layer(
      [Business Logic Layer],
      node([Threshold Gates], [Session and dwell gates · first-pass filtering]),
      node([Text-First Sentiment], [VADER + Filipino MVL · usable text only]),
      node([No-Text VLM Path], [On-device VQA (Moondream 0.5B) · no-text items only]),
      node([Fuzzy Inference], [27-rule engine + center of gravity · risk score 0–100]),
      node([2-Input Safety Fallback], [High OOV or unresolved VLM path · dwell and duration only]),
    ),
    pad(top: 80pt, arrow),
    layer(
      [Presentation Layer],
      node([Awareness Toast], [Level 1 prompt]),
      node([Pause Prompt], [Level 2 modal]),
      node([Pause and Reset], [Level 3 · 60-second guided breathing break]),
      node([Dashboard], [Aggregate usage and sentiment trends]),
    ),
  )
]
