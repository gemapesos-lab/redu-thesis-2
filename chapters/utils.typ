// Utility functions for the thesis

#import "@preview/cetz:0.3.2" as cetz

// ==========================================
// CHART HELPERS (cetz-based)
// ==========================================
// All charts use a shared monochrome palette so they remain readable in
// grayscale thesis printouts.

#let _chart_dark = rgb("#2f2f2f")
#let _chart_mid = rgb("#7a7a7a")
#let _chart_grid = rgb("#b8b8b8")
#let _chart_ref = rgb("#404040")

// Horizontal bar chart. Data: array of (label: str, value: number, note: str-or-none).
#let bar_chart_h(
  data,
  chart_width: 4.2,
  bar_thickness: 0.32,
  bar_gap: 0.28,
  max_value: none,
  x_ticks: 5,
  bar_color: none,
  value_formatter: v => str(calc.round(v, digits: 0)),
  x_label: none,
) = {
  let color = if bar_color == none { _chart_dark } else { bar_color }
  let n = data.len()
  let max_v = if max_value == none {
    calc.max(..data.map(d => d.at(1))) * 1.08
  } else { max_value }
  let y_len = n * (bar_thickness + bar_gap) + bar_gap
  cetz.canvas(length: 1cm, {
    import cetz.draw: *
    line((0, 0), (chart_width, 0), stroke: 0.7pt + black)
    line((0, 0), (0, y_len), stroke: 0.7pt + black)
    for i in range(1, x_ticks + 1) {
      let x = i * chart_width / x_ticks
      let v = i * max_v / x_ticks
      line((x, 0), (x, y_len), stroke: 0.3pt + _chart_grid)
      line((x, 0), (x, -0.08), stroke: 0.5pt + black)
      content((x, -0.32), text(size: 7pt)[#value_formatter(v)])
    }
    line((0, 0), (0, -0.08), stroke: 0.5pt + black)
    content((0, -0.32), text(size: 7pt)[0])
    if x_label != none {
      content((chart_width / 2, -0.72), text(size: 7.5pt, style: "italic")[#x_label])
    }
    for (i, entry) in data.enumerate() {
      let label = entry.at(0)
      let value = entry.at(1)
      let note = if entry.len() > 2 { entry.at(2) } else { none }
      let y_top = y_len - bar_gap - i * (bar_thickness + bar_gap)
      let y_bot = y_top - bar_thickness
      let bar_len = value * chart_width / max_v
      content(
        (-0.18, y_top - bar_thickness / 2),
        anchor: "east",
        text(size: 7.5pt)[#label],
      )
      rect((0, y_bot), (bar_len, y_top), fill: color, stroke: none)
      let value_txt = if note != none {
        [#value_formatter(value) #text(size: 6.6pt)[(#note)]]
      } else {
        [#value_formatter(value)]
      }
      content(
        (bar_len + 0.15, y_top - bar_thickness / 2),
        anchor: "west",
        text(size: 7.2pt)[#value_txt],
      )
    }
  })
}

// Grouped horizontal bar chart with two series per category.
// Data: array of (label: str, value_a: number, value_b: number).
#let bar_chart_grouped_h(
  data,
  series_labels: ("A", "B"),
  chart_width: 4.5,
  bar_thickness: 0.22,
  bar_gap: 0.08,
  group_gap: 0.32,
  max_value: 1.0,
  x_ticks: 5,
  colors: none,
  x_label: none,
  value_formatter: v => str(calc.round(v, digits: 2)),
) = {
  let cols = if colors == none { (_chart_dark, _chart_mid) } else { colors }
  let n = data.len()
  let group_height = 2 * bar_thickness + bar_gap
  let y_len = n * (group_height + group_gap) + group_gap
  cetz.canvas(length: 1cm, {
    import cetz.draw: *
    line((0, 0), (chart_width, 0), stroke: 0.7pt + black)
    line((0, 0), (0, y_len), stroke: 0.7pt + black)
    for i in range(1, x_ticks + 1) {
      let x = i * chart_width / x_ticks
      let v = i * max_value / x_ticks
      line((x, 0), (x, y_len), stroke: 0.3pt + _chart_grid)
      line((x, 0), (x, -0.08), stroke: 0.5pt + black)
      content((x, -0.32), text(size: 7pt)[#value_formatter(v)])
    }
    line((0, 0), (0, -0.08), stroke: 0.5pt + black)
    content((0, -0.32), text(size: 7pt)[0])
    if x_label != none {
      content((chart_width / 2, -0.72), text(size: 7.5pt, style: "italic")[#x_label])
    }
    for (i, entry) in data.enumerate() {
      let label = entry.at(0)
      let val_a = entry.at(1)
      let val_b = entry.at(2)
      let group_top = y_len - group_gap - i * (group_height + group_gap)
      let a_top = group_top
      let a_bot = a_top - bar_thickness
      let b_top = a_bot - bar_gap
      let b_bot = b_top - bar_thickness
      let a_len = val_a * chart_width / max_value
      let b_len = val_b * chart_width / max_value
      content(
        (-0.18, group_top - group_height / 2),
        anchor: "east",
        text(size: 7.5pt)[#label],
      )
      rect((0, a_bot), (a_len, a_top), fill: cols.at(0), stroke: none)
      content(
        (a_len + 0.12, a_top - bar_thickness / 2),
        anchor: "west",
        text(size: 6.8pt)[#value_formatter(val_a)],
      )
      rect((0, b_bot), (b_len, b_top), fill: cols.at(1), stroke: none)
      content(
        (b_len + 0.12, b_top - bar_thickness / 2),
        anchor: "west",
        text(size: 6.8pt)[#value_formatter(val_b)],
      )
    }
    // legend
    let leg_y = y_len + 0.35
    rect((0, leg_y), (0.3, leg_y + 0.18), fill: cols.at(0), stroke: none)
    content((0.4, leg_y + 0.09), anchor: "west", text(size: 7pt)[#series_labels.at(0)])
    rect((1.6, leg_y), (1.9, leg_y + 0.18), fill: cols.at(1), stroke: none)
    content((2.0, leg_y + 0.09), anchor: "west", text(size: 7pt)[#series_labels.at(1)])
  })
}

// Forest plot for effect sizes with 95% CI.
// Data: array of (label: str, point: number, lo: number, hi: number).
#let forest_plot(
  data,
  chart_width: 4.6,
  row_gap: 0.42,
  x_min: -3.0,
  x_max: 0.5,
  x_ticks: 7,
  point_color: none,
  x_label: none,
) = {
  let color = if point_color == none { _chart_dark } else { point_color }
  let n = data.len()
  let y_len = n * row_gap + row_gap
  let x_span = x_max - x_min
  let to_x(v) = (v - x_min) * chart_width / x_span
  cetz.canvas(length: 1cm, {
    import cetz.draw: *
    // gridlines and x-axis
    for i in range(0, x_ticks + 1) {
      let x = i * chart_width / x_ticks
      let v = x_min + i * x_span / x_ticks
      line((x, 0), (x, y_len), stroke: 0.3pt + _chart_grid)
      line((x, 0), (x, -0.08), stroke: 0.5pt + black)
      content((x, -0.32), text(size: 7pt)[#calc.round(v, digits: 1)])
    }
    line((0, 0), (chart_width, 0), stroke: 0.7pt + black)
    // reference line at 0 (null effect)
    let zero_x = to_x(0)
    line((zero_x, 0), (zero_x, y_len), stroke: (paint: black, thickness: 0.7pt, dash: "dashed"))
    if x_label != none {
      content((chart_width / 2, -0.72), text(size: 7.5pt, style: "italic")[#x_label])
    }
    // rows
    for (i, entry) in data.enumerate() {
      let label = entry.at(0)
      let point = entry.at(1)
      let lo = entry.at(2)
      let hi = entry.at(3)
      let y = y_len - row_gap - i * row_gap - row_gap / 2
      content(
        (-0.18, y),
        anchor: "east",
        text(size: 7.5pt)[#label],
      )
      // CI whisker
      line((to_x(lo), y), (to_x(hi), y), stroke: 0.9pt + color)
      line((to_x(lo), y - 0.09), (to_x(lo), y + 0.09), stroke: 0.9pt + color)
      line((to_x(hi), y - 0.09), (to_x(hi), y + 0.09), stroke: 0.9pt + color)
      // point estimate
      circle((to_x(point), y), radius: 0.11, fill: color, stroke: none)
      // value label to right of CI
      let lbl_x = calc.max(to_x(hi), to_x(point)) + 0.18
      content(
        (lbl_x, y),
        anchor: "west",
        text(size: 6.8pt)[$d = #calc.round(point, digits: 2)$ [#calc.round(lo, digits: 2), #calc.round(hi, digits: 2)]],
      )
    }
  })
}

// Vertical bar chart with an optional horizontal threshold reference line.
// Data: array of (label: str, value: number, target: number).
#let bar_chart_v_threshold(
  data,
  chart_width: 5.2,
  chart_height: 3.2,
  bar_gap: 0.14,
  max_value: 5.0,
  y_ticks: 5,
  bar_color: none,
  target_label: none,
  y_label: none,
  value_formatter: v => str(calc.round(v, digits: 2)),
) = {
  let color = if bar_color == none { _chart_dark } else { bar_color }
  let n = data.len()
  let bar_width = (chart_width - (n + 1) * bar_gap) / n
  cetz.canvas(length: 1cm, {
    import cetz.draw: *
    line((0, 0), (chart_width, 0), stroke: 0.7pt + black)
    line((0, 0), (0, chart_height), stroke: 0.7pt + black)
    // y ticks and gridlines
    for i in range(0, y_ticks + 1) {
      let y = i * chart_height / y_ticks
      let v = i * max_value / y_ticks
      line((0, y), (chart_width, y), stroke: 0.3pt + _chart_grid)
      line((-0.08, y), (0, y), stroke: 0.5pt + black)
      content((-0.15, y), anchor: "east", text(size: 7pt)[#value_formatter(v)])
    }
    if y_label != none {
      content(
        (-0.9, chart_height / 2),
        rotate(90deg, reflow: true, text(size: 7.5pt, style: "italic")[#y_label]),
      )
    }
    // bars
    for (i, entry) in data.enumerate() {
      let label = entry.at(0)
      let value = entry.at(1)
      let target = entry.at(2)
      let x_left = bar_gap + i * (bar_width + bar_gap)
      let x_right = x_left + bar_width
      let bar_h = value * chart_height / max_value
      rect((x_left, 0), (x_right, bar_h), fill: color, stroke: none)
      content(
        ((x_left + x_right) / 2, bar_h + 0.18),
        text(size: 7pt)[#value_formatter(value)],
      )
      content(
        ((x_left + x_right) / 2, -0.25),
        anchor: "north",
        text(size: 7pt)[#label],
      )
      // per-bar target tick
      let tgt_h = target * chart_height / max_value
      line(
        (x_left - 0.05, tgt_h),
        (x_right + 0.05, tgt_h),
        stroke: (paint: _chart_ref, thickness: 0.9pt, dash: "dashed"),
      )
    }
    if target_label != none {
      content(
        (chart_width - 0.05, chart_height - 0.2),
        anchor: "east",
        text(size: 6.8pt, style: "italic")[#target_label],
      )
    }
  })
}

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
