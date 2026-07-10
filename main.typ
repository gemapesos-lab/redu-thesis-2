// ==========================================
// DOCUMENT SETUP & TEMPLATE
// FEU Institute of Technology Thesis Format
// ==========================================

#set text(
  font: "Times New Roman",
  size: 12pt,
  lang: "en",
)

#set page(
  paper: "us-letter",
  margin: (left: 1.5in, right: 1in, top: 1in, bottom: 1in),
  numbering: "i",
)

#set par(
  justify: true,
  leading: 1.5em, // Double spacing
  first-line-indent: 0.5in,
)

#import "chapters/utils.typ": centered_page, table_align, thesis_table

#set table(
  inset: (x: 4pt, y: 3.5pt),
  column-gutter: 0pt,
  row-gutter: 0pt,
  stroke: 0.5pt + black,
  fill: white,
)

// ==========================================
// APA 7 FIGURE AND TABLE CAPTION FORMATTING
// Figure/Table number in bold on its own line, italic title below,
// caption positioned above the figure/table body (flush left).
// ==========================================

#show figure.where(kind: table): set block(breakable: true, above: 0.9em, below: 1.15em)
#show figure.where(kind: table): set figure.caption(position: top)
#show figure.where(kind: table): set text(size: 9.5pt, hyphenate: false)
#show figure.where(kind: table): set par(justify: false, first-line-indent: 0pt, leading: 0.96em)
#show figure.caption.where(kind: table): set block(sticky: true, above: 0.2em, below: 0.55em)
#show figure.caption.where(kind: table): it => block(width: 100%)[
  #set align(left)
  #set par(justify: false, first-line-indent: 0pt, leading: 1.08em)
  #set text(size: 12pt)
  #strong[Table #context it.counter.display(it.numbering)]
  #linebreak()
  #emph(it.body)
]

#show figure.where(kind: image): set block(above: 1.1em, below: 1.25em)
#show figure.where(kind: image): set figure.caption(position: top)
#show figure.where(kind: image): set text(size: 9pt, hyphenate: false)
#show figure.where(kind: image): set par(justify: false, first-line-indent: 0pt, leading: 1.08em)
#show figure.caption.where(kind: image): set block(sticky: true, above: 0.5em, below: 0.55em)
#show figure.caption.where(kind: image): it => block(width: 100%)[
  #set align(left)
  #set par(justify: false, first-line-indent: 0pt, leading: 1.08em)
  #set text(size: 12pt)
  #strong[Figure #context it.counter.display(it.numbering)]
  #linebreak()
  #emph(it.body)
]

#show math.equation.where(block: true): set block(above: 0.55em, below: 0.65em)

// ==========================================
// FRONT MATTER (Roman numerals)
// ==========================================

#include "chapters/01_title_page.typ"
#include "chapters/02_copyright.typ"
#include "chapters/03_approval.typ"
// Acknowledgment page dropped (optional per template; several groups omitted it).
// To restore: #include "chapters/04_acknowledgment.typ"

// ==========================================
// TABLE OF CONTENTS (Page v onwards)
// ==========================================

#show outline.entry.where(level: 1): it => {
  v(12pt, weak: true)
  strong(it)
}

#align(center)[#heading(level: 1, numbering: none, outlined: true)[#text(size: 12pt)[TABLE OF CONTENTS]]]
#v(1em)
#outline(title: none, indent: auto, depth: 3)
#pagebreak()

#align(center)[#heading(level: 1, numbering: none, outlined: true)[#text(size: 12pt)[LIST OF TABLES]]]
#v(1em)
#outline(title: none, target: figure.where(kind: table))
#pagebreak()

#align(center)[#heading(level: 1, numbering: none, outlined: true)[#text(size: 12pt)[LIST OF FIGURES]]]
#v(1em)
#outline(title: none, target: figure.where(kind: image))
#pagebreak()

#include "chapters/04a_abbreviations.typ"
#include "chapters/04b_abstract.typ"

// ==========================================
// MAIN CONTENT (Arabic numerals starting at 1)
// ==========================================

#set page(numbering: "1")
#counter(page).update(1)
#set heading(numbering: "1.1.")

// Reset first-line-indent for headings context
#set par(first-line-indent: 0pt)

// Indent lists and increase bullet size
#set list(indent: 0.5in, marker: text(size: 1.3em)[•])
#set enum(indent: 0.5in)

// Shared heading renderers for main chapters and later front/back matter.
#let render_level1_heading(it) = {
  set align(center)
  pagebreak(weak: true)
  v(2em)
  if it.numbering != none {
    text(size: 12pt, weight: "regular")[Chapter #counter(heading).display("1")]
    linebreak()
    v(0.5em)
  }
  text(size: 12pt, weight: "bold", upper(it.body))
  v(2em)
}

#let render_level2_heading(it) = {
  set text(weight: "bold", size: 12pt)
  v(1.5em)
  [#counter(heading).display() #it.body]
  v(1em)
}

#let render_level3_heading(it) = {
  set text(weight: "bold", size: 12pt)
  v(1em)
  h(0.5in)
  [#counter(heading).display() #it.body]
  v(0.5em)
}

#show heading.where(level: 1): it => render_level1_heading(it)
#show heading.where(level: 2): it => render_level2_heading(it)
#show heading.where(level: 3): it => render_level3_heading(it)

// Configure body paragraph indentation for chapters (Chapters 1 to 6)
#{
  set par(first-line-indent: 0.5in)
  show list: set par(first-line-indent: 0pt)
  show enum: set par(first-line-indent: 0pt)
  show figure: set par(first-line-indent: 0pt)

  include "chapters/05_chapter1.typ"
  include "chapters/06_chapter2.typ"
  include "chapters/07_chapter3.typ"
  include "chapters/08_chapter4.typ"
  include "chapters/09_chapter5.typ"
  include "chapters/10_chapter6.typ"
}

#pagebreak()
#heading(level: 1, numbering: none)[REFERENCES]
#v(1em)
#bibliography("chapters/08_references.bib", style: "apa", title: none)

// Configure body paragraph indentation for appendices
#{
  set par(first-line-indent: 0.5in)
  show list: set par(first-line-indent: 0pt)
  show enum: set par(first-line-indent: 0pt)
  show figure: set par(first-line-indent: 0pt)

  include "chapters/09_appendices.typ"
}
