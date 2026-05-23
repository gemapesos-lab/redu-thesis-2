// Utility functions for the thesis

#let centered_page(body) = {
  set align(center)
  set page(numbering: none)
  body
}

#let figure_chip(label) = rect(
  fill: black,
  radius: 3pt,
  inset: (x: 9pt, y: 4pt),
)[
  #text(size: 8pt, weight: "bold", fill: white)[#upper(label)]
]

#let figure_note(body) = text(size: 7.2pt, style: "italic")[#body]

#let figure_arrow(symbol: "↓", note: none) = align(center)[
  #stack(
    dir: ttb,
    spacing: 2pt,
    text(size: 12pt, weight: "medium")[#symbol],
    if note != none {
      figure_note(note)
    },
  )
]

#let figure_panel(
  title,
  body,
  note: none,
  width: 100%,
  body_width: 100%,
  body_align: center,
  body_leading: 1.04em,
  item_gap: 0pt,
) = block(width: width)[
  #rect(
    width: 100%,
    stroke: 0.85pt + black,
    radius: 5pt,
    inset: 10pt,
  )[
    #align(center)[#text(size: 8.8pt, weight: "bold")[#upper(title)]]
    #v(5pt)
    #align(center)[
      #block(width: body_width)[
        #{
          set par(justify: false, first-line-indent: 0pt, leading: body_leading, spacing: item_gap)
          align(body_align, body)
        }
      ]
    ]
    #if note != none {
      v(6pt)
      align(center)[#figure_note(note)]
    }
  ]
]

#let figure_panel_items(
  title,
  items,
  note: none,
  width: 100%,
  body_width: 100%,
  item_align: left,
  item_size: 8.6pt,
  item_leading: 0.94em,
  item_gap: 5pt,
) = block(width: width)[
  #rect(
    width: 100%,
    stroke: 0.85pt + black,
    radius: 5pt,
    inset: 10pt,
  )[
    #align(center)[#text(size: 8.8pt, weight: "bold")[#upper(title)]]
    #v(5pt)
    #align(center)[
      #block(width: body_width)[
        #stack(
          dir: ttb,
          spacing: 0pt,
          ..items.enumerate().map(((index, item)) => [
            #block(width: 100%, inset: (x: 0pt, y: 2pt))[
              #{
                set text(size: item_size)
                set par(justify: false, first-line-indent: 0pt, leading: item_leading, spacing: 0pt)
                align(item_align)[#item]
              }
            ]
            #if index < items.len() - 1 {
              line(length: 100%, stroke: 0.35pt + rgb("#A0A0A0"))
              v(item_gap)
            }
          ]),
        )
      ]
    ]
    #if note != none {
      v(6pt)
      align(center)[#figure_note(note)]
    }
  ]
]

#let figure_layer(
  title,
  body,
) = rect(
  width: 100%,
  stroke: 1pt + black,
  radius: 7pt,
  inset: 10pt,
)[
  #align(center)[#figure_chip(title)]
  #v(9pt)
  #body
]

#let figure_node(
  title,
  body,
) = rect(
  width: 100%,
  stroke: 0.75pt + black,
  radius: 4pt,
  inset: 7pt,
)[
  #set par(justify: false, first-line-indent: 0pt, leading: 1.02em)
  #align(center)[#text(size: 7.8pt, weight: "bold")[#title]]
  #v(3pt)
  #align(center)[#text(size: 7pt)[#body]]
]

#let figure_boundary(
  title,
  subtitle: none,
  body,
) = rect(
  width: 100%,
  stroke: (paint: black, thickness: 1pt, dash: "dashed"),
  radius: 8pt,
  inset: 12pt,
)[
  #align(center)[#figure_chip(title)]
  #if subtitle != none {
    v(5pt)
    align(center)[#text(size: 7.2pt)[#subtitle]]
  }
  #v(10pt)
  #body
]

#let table_align(spec) = (x, y) => {
  if y == 0 {
    center
  } else {
    spec.at(x, default: left)
  }
}

#let thesis_table_body(
  columns: (),
  header: (),
  cell_align: left,
  body: (),
  pad_x: 0.035in,
) = block(width: 100%)[
  #align(center, pad(x: pad_x, table(
    columns: columns,
    align: cell_align,
    table.header(..header),
    ..body,
  )))
]

#let thesis_table(
  caption: [],
  columns: (),
  header: (),
  cell_align: left,
  body: (),
  pad_x: 0.035in,
) = figure(
  kind: table,
  thesis_table_body(
    columns: columns,
    header: header,
    cell_align: cell_align,
    body: body,
    pad_x: pad_x,
  ),
  caption: caption,
)

#let continued_thesis_table(
  caption: [],
  columns: (),
  header: (),
  cell_align: left,
  body: (),
  pad_x: 0.035in,
) = {
  set text(size: 9.5pt, hyphenate: false)
  set par(justify: false, first-line-indent: 0pt, leading: 0.96em)
  block(breakable: false, above: 0.9em, below: 1.15em)[
    #align(center, text(size: 10pt)[#caption])
    #v(0.45em)
    #thesis_table_body(
      columns: columns,
      header: header,
      cell_align: cell_align,
      body: body,
      pad_x: pad_x,
    )
  ]
}
