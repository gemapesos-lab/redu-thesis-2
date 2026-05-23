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

#show figure.where(kind: table): set block(breakable: true, above: 0.9em, below: 1.15em)
#show figure.where(kind: table): set figure.caption(position: top, separator: [. ])
#show figure.where(kind: table): set text(size: 9.5pt, hyphenate: false)
#show figure.where(kind: table): set par(justify: false, first-line-indent: 0pt, leading: 0.96em)
#show figure.caption.where(kind: table): set block(sticky: true, above: 0.2em, below: 0.45em)
#show figure.caption.where(kind: table): it => align(center, text(size: 10pt)[#it])

#show figure.where(kind: image): set block(above: 1.1em, below: 1.25em)
#show figure.where(kind: image): set text(size: 9pt, hyphenate: false)
#show figure.where(kind: image): set par(justify: false, first-line-indent: 0pt, leading: 1.08em)
#show figure.caption.where(kind: image): set block(above: 0.5em, below: 0.25em)
#show figure.caption.where(kind: image): it => align(center, text(size: 10pt)[#it])

#show math.equation.where(block: true): set block(above: 0.55em, below: 0.65em)

// ==========================================
// FRONT MATTER (Roman numerals)
// ==========================================

#include "chapters/01_title_page.typ"
#include "chapters/02_copyright.typ"
#include "chapters/03_approval.typ"
#include "chapters/04_acknowledgment.typ"

// ==========================================
// TABLE OF CONTENTS (Page v onwards)
// ==========================================

#show outline.entry.where(level: 1): it => {
  v(12pt, weak: true)
  strong(it)
}

#align(center)[#heading(level: 1, numbering: none, outlined: true)[#text(size: 12pt)[TABLE OF CONTENTS]]]
#v(1em)
#outline(title: none, indent: auto, depth: 2)
#pagebreak()

#align(center)[#heading(level: 1, numbering: none, outlined: true)[#text(size: 12pt)[LIST OF TABLES]]]
#v(1em)
#outline(title: none, target: figure.where(kind: table))
#pagebreak()

#align(center)[#heading(level: 1, numbering: none, outlined: true)[#text(size: 12pt)[LIST OF FIGURES]]]
#v(1em)
#outline(title: none, target: figure.where(kind: image))
#pagebreak()

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

// Chapters 1-3 use a house style where only the first prose paragraph after
// each heading gets a first-line indent. Later paragraphs stay flush-left.
#{
  let first-par-indent-pending = state("chapter-first-par-indent-pending", false)

  show heading.where(level: 1): it => {
    render_level1_heading(it)
    first-par-indent-pending.update(true)
  }

  show heading.where(level: 2): it => {
    render_level2_heading(it)
    first-par-indent-pending.update(true)
  }

  show heading.where(level: 3): it => {
    render_level3_heading(it)
    first-par-indent-pending.update(true)
  }

  show par: it => context {
    if first-par-indent-pending.get() {
      first-par-indent-pending.update(false)
      // Pad the paragraph and use a negative hanging indent so only the first
      // line shifts right while later lines return to the normal left edge.
      block(width: 100%)[#h(0.5in)#it.body]
    } else {
      it
    }
  }

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
#include "chapters/09_appendices.typ"
