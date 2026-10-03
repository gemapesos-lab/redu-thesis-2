#!/usr/bin/env python3
"""Build the ACM-formatted paper by editing the CS0039 ACM Word template.

Design rule: never create new styles or formatting. Every paragraph, table,
section break and caption is a deep copy of an element that already exists in
the template, with only its text replaced.  Unchanged package parts (styles,
numbering, settings, theme, fonts, footer) are copied byte-for-byte.

Usage:  build_acm_docx.py [--template T.docx] [--out OUT.docx]
"""
import argparse
import copy
import datetime as dt
import re
import sys
import zipfile
from pathlib import Path

from lxml import etree

HERE = Path(__file__).resolve().parent
REPO = HERE.parent

W = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
M = "http://schemas.openxmlformats.org/officeDocument/2006/math"
R = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
WP = "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing"
A = "http://schemas.openxmlformats.org/drawingml/2006/main"
PIC = "http://schemas.openxmlformats.org/drawingml/2006/picture"
NS = {"w": W, "m": M}


def q(tag, ns=W):
    return "{%s}%s" % (ns, tag)


# ----------------------------------------------------------------------------
# Markup parsing
# ----------------------------------------------------------------------------

def parse_paper(path):
    text = Path(path).read_text(encoding="utf-8")
    _, fm, rest = text.split("---\n", 2)
    meta = {}
    for line in fm.strip().splitlines():
        k, v = line.split(":", 1)
        meta[k.strip()] = v.strip()
    lines = rest.splitlines()
    blocks, i = [], 0
    while i < len(lines):
        ln = lines[i]
        if not ln.strip():
            i += 1
            continue
        if ln.startswith("ABSTRACT:"):
            i += 1
            while not lines[i].strip():
                i += 1
            blocks.append(("abstract", lines[i].strip()))
            i += 1
        elif ln.startswith(":::"):
            kind = ln[3:].strip()
            i += 1
            props = {}
            rows = []
            while not lines[i].startswith(":::"):
                k, v = lines[i].split(":", 1)
                if k == "row":
                    rows.append([c.strip() for c in v.split("|")])
                else:
                    props[k.strip()] = v.strip()
                i += 1
            i += 1
            props["rows"] = rows
            blocks.append((kind, props))
        elif ln.startswith("### "):
            blocks.append(("h3", ln[4:].strip())); i += 1
        elif ln.startswith("## "):
            blocks.append(("h2", ln[3:].strip())); i += 1
        elif ln.startswith("# "):
            blocks.append(("h1", ln[2:].strip())); i += 1
        elif ln.startswith("$$"):
            m = re.match(r"\$\$(.*)\$\$\s*\{#(\w+)\}", ln)
            blocks.append(("eq", (m.group(1).strip(), m.group(2)))); i += 1
        else:
            blocks.append(("p", ln.strip())); i += 1
    return meta, blocks


# ----------------------------------------------------------------------------
# Bibliography -> ACM Reference Format
# ----------------------------------------------------------------------------

def strip_braces(s):
    return re.sub(r"[{}]", "", s or "").strip()


def initials(given):
    parts = []
    for tok in given.split():
        tok = tok.strip(".")
        if not tok:
            continue
        sub = [t for t in tok.split("-") if t]
        parts.append("-".join(t[0] + "." for t in sub).replace(".-", ".-"))
    return " ".join(parts)


def fmt_author(a):
    corporate = a.strip().startswith("{")
    a = strip_braces(a)
    if corporate:
        return a, a
    if "," in a:
        last, given = [x.strip() for x in a.split(",", 1)]
    else:
        toks = a.split()
        if len(toks) == 1:
            return toks[0], toks[0]
        last, given = toks[-1], " ".join(toks[:-1])
    ini = initials(given)
    return last, (f"{last}, {ini}" if ini else last)


def split_authors(s):
    # corporate authors are wrapped in {...}
    s = s.strip()
    if s.startswith("{") and s.endswith("}") and " and " not in s:
        return [s]
    return [x.strip() for x in re.split(r"\s+and\s+", s) if x.strip()]


def format_reference(e):
    """Return list of (text, italic) segments in ACM Reference Format."""
    auth = split_authors(e.get("author", ""))
    names = [fmt_author(a)[1] for a in auth]
    if len(names) > 6:
        names = names[:6] + ["et al"]
    if not names:
        astr = ""
    elif len(names) == 1:
        astr = names[0]
    elif names[-1] == "et al":
        astr = ", ".join(names[:-1]) + ", et al"
    elif len(names) == 2:
        astr = f"{names[0]} and {names[1]}"
    else:
        astr = ", ".join(names[:-1]) + f", and {names[-1]}"
    year = e.get("year", "")
    title = re.sub(r"\s+_.*$", "", strip_braces(e.get("title", ""))).replace("---", "\u2014")
    title = title.rstrip(".") if not title.endswith('."') else title
    typ = e["ENTRYTYPE"]
    a_dot = "" if astr.endswith(".") else "."
    t_dot = "" if title.endswith('."') else "."
    segs = [(f"{astr}{a_dot} {year}. {title}{t_dot} ", False)]
    doi = e.get("doi")
    url = e.get("url")
    tail = ""
    if typ == "article":
        segs.append((strip_braces(e.get("journal", "")), True))
        vol, no, pages = e.get("volume"), e.get("number"), e.get("pages")
        bits = ""
        if vol and vol != "None":
            bits += f" {int(vol) if vol.isdigit() else vol}"
        if no and no != "None":
            bits += f", {int(no) if no.isdigit() else no}"
        bits += f" ({year})"
        if pages and pages != "None":
            pg = pages.replace("--", "–")
            if "–" in pg and pg.split("–")[0] == pg.split("–")[1]:
                pg = pg.split("–")[0]
            bits += ", " + pg
        tail = bits + ". "
    elif typ == "inproceedings":
        segs.append(("In ", False))
        segs.append((strip_braces(e.get("booktitle", "")), True))
        if e.get("pages"):
            tail = ", " + e["pages"].replace("--", "–")
        tail += ". "
    elif typ == "inbook":
        segs.append(("In ", False))
        segs.append((strip_braces(e.get("booktitle", e.get("series", ""))), True))
        pub = strip_braces(e.get("publisher", ""))
        tail = (f". {pub}" if pub else "") + (f", {e['pages'].replace('--', '–')}" if e.get("pages") else "") + ". "
    elif typ == "book":
        pub = strip_braces(e.get("publisher", ""))
        tail = f"{pub}. " if pub else ""
    else:  # misc / report
        site = strip_braces(e.get("howpublished") or e.get("institution") or e.get("publisher") or "")
        tail = f"{site}. " if site else ""
    segs.append((tail, False))
    if doi:
        segs.append((f"DOI= https://doi.org/{doi}", False))
    elif url:
        segs.append((f"URL= {url}", False))
    else:
        segs[-1] = (segs[-1][0].rstrip(), False)
    # drop empty/dangling segments
    return [(t, i) for t, i in segs if t]


def load_bib(path):
    import bibtexparser
    with open(path, encoding="utf-8") as fh:
        db = bibtexparser.load(fh)
    return {e["ID"]: e for e in db.entries}


def first_surname(e):
    auth = split_authors(e.get("author", ""))
    return fmt_author(auth[0])[0].lower() if auth else ""


# ----------------------------------------------------------------------------
# XML helpers
# ----------------------------------------------------------------------------

def el(tag, attrs=None, ns=W, children=()):
    e = etree.Element(q(tag, ns))
    for k, v in (attrs or {}).items():
        e.set(q(k, ns) if ":" not in k else k, v)
    for c in children:
        e.append(c)
    return e


def run(text, italic=False, sub=False, base=(), extra_rpr=()):
    """base: iterable of rPr tag names applied first (e.g. 'b','bCs')."""
    r = el("r")
    order = []
    for t in base:
        order.append(el(t))
    if italic:
        order += [el("i"), el("iCs")]
    order += list(extra_rpr)
    if sub:
        order.append(el("vertAlign", {"val": "subscript"}))
    if order:
        r.append(el("rPr", children=order))
    parts = text.split("\n")
    for n, part in enumerate(parts):
        if n:
            r.append(el("br"))
        t = el("t")
        t.text = part
        t.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
        r.append(t)
    return r


INLINE_RE = re.compile(r"\*([^*]+)\*(_(\w+))?")


def inline_runs(text, base=()):
    """*italic* and *x*_sub markup -> list of w:r."""
    out, pos = [], 0
    for m in INLINE_RE.finditer(text):
        if m.start() > pos:
            out.append(run(text[pos:m.start()], base=base))
        out.append(run(m.group(1), italic=True, base=base))
        if m.group(3):
            out.append(run(m.group(3), sub=True, base=base))
        pos = m.end()
    if pos < len(text):
        out.append(run(text[pos:], base=base))
    return out


def fld_runs(instr, cached):
    def fc(kind):
        return el("r", children=[el("fldChar", {"fldCharType": kind})])
    ins = el("r", children=[el("instrText")])
    ins[0].text = f" {instr} "
    ins[0].set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
    cr = el("r", children=[el("rPr", children=[el("noProof")]), el("t")])
    cr[1].text = str(cached)
    return [fc("begin"), ins, fc("separate"), cr, fc("end")]


class Proto:
    """Access to template body elements by index (never mutated)."""

    def __init__(self, body):
        self.body = list(body)

    def para(self, idx, runs=(), first_line=None, after=None, before=None, drop_runs=True):
        p = copy.deepcopy(self.body[idx])
        ppr = p.find(q("pPr"))
        if drop_runs:
            for c in list(p):
                if c is not ppr:
                    p.remove(c)
        if ppr is not None:
            if first_line is not None or after is not None or before is not None:
                self._set_spacing_ind(ppr, first_line, after, before)
        for r in runs:
            p.append(r)
        return p

    @staticmethod
    def _set_spacing_ind(ppr, first_line, after, before):
        # keep schema order: ... spacing, ind ...
        sp = ppr.find(q("spacing"))
        ind = ppr.find(q("ind"))
        if after is not None or before is not None:
            if sp is None:
                sp = el("spacing")
                _insert_ordered(ppr, sp)
            if after is not None:
                sp.set(q("after"), str(after))
            if before is not None:
                sp.set(q("before"), str(before))
        if first_line is not None:
            if ind is None:
                ind = el("ind")
                _insert_ordered(ppr, ind)
            ind.set(q("firstLine"), str(first_line))


PPR_ORDER = ["pStyle", "keepNext", "keepLines", "pageBreakBefore", "framePr", "widowControl",
             "numPr", "suppressLineNumbers", "pBdr", "shd", "tabs", "suppressAutoHyphens",
             "spacing", "ind", "contextualSpacing", "mirrorIndents", "jc", "outlineLvl",
             "rPr", "sectPr", "pPrChange"]


def _insert_ordered(ppr, child):
    name = etree.QName(child).localname
    rank = PPR_ORDER.index(name)
    for i, c in enumerate(ppr):
        if PPR_ORDER.index(etree.QName(c).localname) > rank:
            ppr.insert(i, child)
            return
    ppr.append(child)


def add_ppr_child(p, child):
    ppr = p.find(q("pPr"))
    if ppr is None:
        ppr = el("pPr")
        p.insert(0, ppr)
    _insert_ordered(ppr, child)


def make_sectpr(proto_sectpr, cols_attrs):
    s = copy.deepcopy(proto_sectpr)
    for a in list(s.attrib):
        del s.attrib[a]
    for fr in s.findall(q("footerReference")):
        s.remove(fr)
    cols = s.find(q("cols"))
    for a in list(cols.attrib):
        del cols.attrib[a]
    for k, v in cols_attrs.items():
        cols.set(q(k), v)
    return s


# ----------------------------------------------------------------------------
# OMML
# ----------------------------------------------------------------------------

def tex_to_omml(tex, xslt):
    import latex2mathml.converter as conv
    mml = conv.convert(tex)
    out = xslt(etree.fromstring(mml))
    omath = out.getroot()
    omath = copy.deepcopy(omath)
    return omath


# ----------------------------------------------------------------------------
# Build
# ----------------------------------------------------------------------------

def build(template, out_path, paper_md, bib_path, figure_png):
    meta, blocks = parse_paper(paper_md)
    bib = load_bib(bib_path)

    # ----- citation numbering (alphabetical, as the template requires) -----
    cited = []
    for kind, val in blocks:
        txt = val if isinstance(val, str) else ""
        if kind == "abstract":
            txt = val
        for m in re.finditer(r"\[((?:@[\w-]+(?:;\s*)?)+)\]", txt):
            for k in re.findall(r"@([\w-]+)", m.group(1)):
                if k not in cited:
                    cited.append(k)
    missing = [k for k in cited if k not in bib]
    if missing:
        sys.exit(f"missing bib keys: {missing}")
    ordered = sorted(cited, key=lambda k: (first_surname(bib[k]), bib[k].get("year", ""), k))
    num = {k: i + 1 for i, k in enumerate(ordered)}

    def sub_cites(text):
        def rep(m):
            nums = sorted(num[k] for k in re.findall(r"@([\w-]+)", m.group(1)))
            return "[" + ", ".join(str(n) for n in nums) + "]"
        return re.sub(r"\[((?:@[\w-]+(?:;\s*)?)+)\]", rep, text)

    zin = zipfile.ZipFile(template)
    doc = etree.fromstring(zin.read("word/document.xml"))
    body = doc.find(q("body"))
    proto = Proto(body)
    final_sectpr_proto = body.find(q("sectPr"))
    # prototype body indexes (see analysis of the template)
    P_TITLE, P_SECT0, P_AUTH1, P_AFF, P_MAIL, P_AUTH_BRK = 0, 1, 2, 3, 4, 5
    P_SECT_AUTH, P_ABS_LBL, P_ABS, P_CCS_LBL, P_CCS, P_KW_LBL, P_KW = 13, 14, 15, 16, 17, 19, 20
    P_H1, P_BODY, P_H2_FIRST, P_H2, P_H3 = 21, 31, 26, 32, 58
    P_FRAME = (27, 28, 29)
    P_CAP, P_TBL, P_EMPTY_BTI, P_REF = 39, 40, 41, 67
    P_FIG = 55
    sectpr_tmpl = body[P_SECT_AUTH].find(q("pPr")).find(q("sectPr"))

    new = []  # new body children

    # ---------------- title block ----------------
    new.append(proto.para(P_TITLE, [run(meta["title"])]))
    new.append(copy.deepcopy(body[P_SECT0]))  # section 0 break, untouched

    sp = [el("spacing", {"val": "-2"})]
    authors = [a.strip().split("|") for a in meta["authors"].split(";")]
    aff_lines = meta["affiliation"].split("|")

    def author_block(name, mail, brk):
        pa = proto.para(P_AUTH_BRK if brk else P_AUTH1, [])
        if brk:
            pa.append(el("r", children=[el("rPr", children=[el("spacing", {"val": "-2"})]),
                                        el("br", {"type": "column"})]))
        pa.append(run(name, extra_rpr=sp))
        pf = proto.para(P_AFF, [])
        for n, line in enumerate(aff_lines):
            r = run(line, extra_rpr=sp)
            if n:
                r.insert(1 if r.find(q("rPr")) is not None else 0, el("br"))
            pf.append(r)
        pm = proto.para(P_MAIL, [run(mail, extra_rpr=sp)])
        return [pa, pf, pm]

    for row in (authors[0:2], authors[2:4]):
        blk = []
        for j, (name, mail) in enumerate(row):
            blk += author_block(name.strip(), mail.strip(), brk=(j == 1))
        if row is authors[0:2]:
            add_ppr_child(blk[-1], make_sectpr(sectpr_tmpl, {"num": "2", "space": "0"}))
            new += blk
        else:
            new += blk
            holder = copy.deepcopy(body[P_SECT_AUTH])
            hs = holder.find(q("pPr")).find(q("sectPr"))
            holder.find(q("pPr")).replace(hs, make_sectpr(sectpr_tmpl, {"num": "2", "space": "0"}))
            new.append(holder)

    # ---------------- abstract, CCS, keywords ----------------
    abstract = [b for b in blocks if b[0] == "abstract"][0][1]
    new.append(proto.para(P_ABS_LBL, [run("ABSTRACT", base=("b",), extra_rpr=[el("sz", {"val": "24"})])]))
    new.append(proto.para(P_ABS, inline_runs(sub_cites(abstract))))
    new.append(proto.para(P_CCS_LBL, [run("CCS Concepts", base=("b",), extra_rpr=[el("sz", {"val": "24"})])]))

    ccs = [c.strip() for c in meta["ccs"].split(";")]
    szcs = lambda: el("szCs", {"val": "18"})
    ccs_runs = []
    for n, item in enumerate(ccs):
        cat, concept = item.split("➝")
        lead = "•\u00a0" if n == 0 else " \u00a0 •\u00a0"
        ccs_runs.append(el("r", children=[el("rPr", children=[szcs()]), _t(lead)]))
        ccs_runs.append(el("r", children=[el("rPr", children=[el("rStyle", {"val": "Strong"}), szcs()]), _t(cat)]))
        ccs_runs.append(el("r", children=[el("rPr", children=[
            el("rFonts", {"ascii": "MS Mincho", "eastAsia": "MS Mincho", "hAnsi": "MS Mincho", "cs": "MS Mincho"}),
            el("b"), el("bCs"), el("sz", {"val": "16"}), el("szCs", {"val": "16"})]), _t("➝")]))
        ccs_runs.append(el("r", children=[el("rPr", children=[el("rStyle", {"val": "Strong"}), szcs()]), _t(concept)]))
    new.append(proto.para(P_CCS, ccs_runs))
    new.append(proto.para(P_KW_LBL, [run("Keywords", base=("b",), extra_rpr=[el("sz", {"val": "24"})])]))
    new.append(proto.para(P_KW, [run(meta["keywords"])]))

    # ---------------- first-page copyright frame ----------------
    permission = body[P_FRAME[0]]
    perm_text = "".join(permission.itertext()).replace("SAMPLE: ", "")
    f0 = proto.para(P_FRAME[0], [run(perm_text)])
    f1 = proto.para(P_FRAME[1], [])
    f1.append(el("r", children=[el("rPr", children=[el("i"), el("iCs"), el("sz", {"val": "16"})]), _t(meta["venue"])]))
    f1.append(el("r", children=[el("rPr", children=[el("sz", {"val": "16"})]), _t(meta["venue_rest"])]))
    f2 = proto.para(P_FRAME[2], [])
    f2.append(el("r", children=[el("rPr", children=[el("sz", {"val": "16"})]), _t(meta["rights"])]))
    new += [f0, f1, f2]

    # ---------------- body content ----------------
    seq = {"Table": 0, "Figure": 0}
    sect_counter = {"n": 0}
    two_col = lambda: make_sectpr(sectpr_tmpl, {"num": "2", "space": "475"})
    one_col = lambda: make_sectpr(sectpr_tmpl, {"space": "720"})

    def close_section(kind):
        """Attach a section break (ending a section) to the last paragraph."""
        last = new[-1]
        if last.tag != q("p") or last.find(q("pPr")) is None or last.find(q("pPr")).find(q("sectPr")) is not None:
            holder = proto.para(P_EMPTY_BTI, [])
            new.append(holder)
            last = holder
        add_ppr_child(last, two_col() if kind == "two" else one_col())

    def tc_proto(row_idx, col_idx=0):
        tbl = body[P_TBL]
        return tbl.findall(q("tr"))[row_idx].findall(q("tc"))[col_idx]

    def caption(kind, text):
        seq[kind] += 1
        p = proto.para(P_CAP, [])
        p.append(run(f"{kind} "))
        for r in fld_runs(f"SEQ {kind} \\* ARABIC", seq[kind]):
            p.append(r)
        p.append(run(". "))
        for r in inline_runs(sub_cites(text)):
            p.append(r)
        return p

    def table(props):
        span_full = props["span"] == "full"
        widths = [int(x) for x in props["widths"].split("|")]
        header = [c.strip() for c in props["header"].split("|")]
        tbl_proto = body[P_TBL]
        tbl = copy.deepcopy(tbl_proto)
        grid = tbl.find(q("tblGrid"))
        for g in list(grid):
            grid.remove(g)
        for wd in widths:
            grid.append(el("gridCol", {"w": str(wd)}))
        trs = tbl.findall(q("tr"))
        hdr_proto, row_proto = trs[0], trs[1]
        for tr in trs:
            tbl.remove(tr)

        def mk_row(proto_tr, cells, bold):
            tr = copy.deepcopy(proto_tr)
            tcs = tr.findall(q("tc"))
            tc0 = tcs[0]
            for tc in tcs:
                tr.remove(tc)
            for wd, txt in zip(widths, cells):
                txt = txt.replace("<br>", "\n")
                tc = copy.deepcopy(tc0)
                tc.find(q("tcPr")).find(q("tcW")).set(q("w"), str(wd))
                p = tc.find(q("p"))
                for c in list(p):
                    if c.tag != q("pPr"):
                        p.remove(c)
                for r in inline_runs(txt, base=("b", "bCs") if bold else ()):
                    p.append(r)
                tr.append(tc)
            return tr

        tblW = tbl.find(q("tblPr")).find(q("tblW"))
        tblW.set(q("w"), str(sum(widths)))
        tblW.set(q("type"), "dxa")
        tbl.append(mk_row(hdr_proto, header, True))
        for row in props["rows"]:
            tbl.append(mk_row(row_proto, row, False))
        return tbl

    def eq_table(eqs, xslt):
        """One borderless table (display equation | number) per group of equations."""
        tbl = copy.deepcopy(body[P_TBL])
        tblpr = tbl.find(q("tblPr"))
        tblpr.remove(tblpr.find(q("tblBorders")))
        tblpr.remove(tblpr.find(q("tblLook")))
        for c in list(tbl.find(q("tblGrid"))):
            tbl.find(q("tblGrid")).remove(c)
        # widths: equation | number
        wl, wr = 4200, 600
        for wd in (wl, wr):
            tbl.find(q("tblGrid")).append(el("gridCol", {"w": str(wd)}))
        trs = tbl.findall(q("tr"))
        row_proto = trs[1]
        for tr in trs:
            tbl.remove(tr)
        for tex, label in eqs:
            tr = copy.deepcopy(row_proto)
            tcs = tr.findall(q("tc"))
            for tc in tcs[2:]:
                tr.remove(tc)
            tcs = tr.findall(q("tc"))
            for tc, wd in zip(tcs, (wl, wr)):
                tc.find(q("tcPr")).find(q("tcW")).set(q("w"), str(wd))
            # equation cell
            p = tcs[0].find(q("p"))
            for c in list(p):
                if c.tag != q("pPr"):
                    p.remove(c)
            omp = etree.SubElement(p, q("oMathPara", M))
            omp.append(tex_to_omml(tex, xslt))
            # number cell
            p2 = tcs[1].find(q("p"))
            for c in list(p2):
                if c.tag != q("pPr"):
                    p2.remove(c)
            jc = p2.find(q("pPr")).find(q("jc"))
            jc.set(q("val"), "right")
            eq_n = eq_table.counter = getattr(eq_table, "counter", 0) + 1
            p2.append(run(f"({eq_n})"))
            tbl.append(tr)
        return tbl

    xslt = etree.XSLT(etree.parse("/Applications/Microsoft Word.app/Contents/Resources/mathml2omml.xsl"))

    figure_rid = "rId12"

    def figure(props):
        width_in = float(props["width"])
        from PIL import Image
        with Image.open(figure_png) as im:
            ratio = im.height / im.width
        cx = int(width_in * 914400)
        cy = int(cx * ratio)
        p = proto.para(P_FIG, [])
        # template figure paragraph is BodyTextIndent, after=120, firstLine=0: keep, centre + keep with caption
        add_ppr_child(p, el("keepNext"))
        add_ppr_child(p, el("jc", {"val": "center"}))
        drawing = etree.fromstring(f"""
<w:r xmlns:w="{W}" xmlns:wp="{WP}" xmlns:a="{A}" xmlns:pic="{PIC}" xmlns:r="{R}">
  <w:rPr><w:noProof/></w:rPr>
  <w:drawing>
    <wp:inline distT="0" distB="0" distL="0" distR="0">
      <wp:extent cx="{cx}" cy="{cy}"/>
      <wp:effectExtent l="0" t="0" r="0" b="0"/>
      <wp:docPr id="1" name="Figure 1" descr="Diagram of the three-layer on-device architecture: a Data Access Layer (accessibility capture, text/no-text check, session logger, screenshot capture, local storage), a Business Logic Layer (threshold gates, VADER with Filipino lexicon, Moondream no-text path, fuzzy inference, two-input fallback), and a Presentation Layer (awareness toast, pause prompt, pause and reset, dashboard), all inside an edge-computing boundary."/>
      <wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect="1"/></wp:cNvGraphicFramePr>
      <a:graphic>
        <a:graphicData uri="http://schemas.openxmlformats.org/drawingml/2006/picture">
          <pic:pic>
            <pic:nvPicPr><pic:cNvPr id="0" name="architecture.png"/><pic:cNvPicPr/></pic:nvPicPr>
            <pic:blipFill><a:blip r:embed="{figure_rid}"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill>
            <pic:spPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="{cx}" cy="{cy}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom></pic:spPr>
          </pic:pic>
        </a:graphicData>
      </a:graphic>
    </wp:inline>
  </w:drawing>
</w:r>""")
        p.append(drawing)
        return p

    # run through blocks
    items = [b for b in blocks if b[0] != "abstract"]
    # group consecutive equations
    grouped = []
    for b in items:
        if b[0] == "eq" and grouped and grouped[-1][0] == "eqs":
            grouped[-1][1].append(b[1])
        elif b[0] == "eq":
            grouped.append(("eqs", [b[1]]))
        else:
            grouped.append(b)
    items = grouped

    UNNUM = {"ACKNOWLEDGMENTS", "REFERENCES"}
    prev_kind = "h1"  # after the frame, the first heading follows block-wise
    last_h2_after_h1 = False
    n_items = len(items)
    refs_pending = False
    # A full-width figure should close its page. The next paragraph starts
    # on a new page so body text does not continue underneath the figure.
    break_before_next = False

    def next_kind(i):
        return items[i + 1][0] if i + 1 < n_items else "end"

    for i, (kind, val) in enumerate(items):
        pk = items[i - 1][0] if i else "frame"
        nk = next_kind(i)
        if kind == "h1":
            p = proto.para(P_H1, [run(val)])
            if val in UNNUM:
                np_ = el("numPr", children=[el("ilvl", {"val": "0"}), el("numId", {"val": "0"})])
                add_ppr_child(p, np_)
            if break_before_next:
                add_ppr_child(p, el("pageBreakBefore"))
                break_before_next = False
            new.append(p)
            if val == "REFERENCES":
                refs_pending = True
        elif kind == "h2":
            idx = P_H2_FIRST if pk == "h1" else P_H2
            p = proto.para(idx, [run(val)])
            if break_before_next:
                add_ppr_child(p, el("pageBreakBefore"))
                break_before_next = False
            new.append(p)
        elif kind == "h3":
            p = proto.para(P_H3, [run(val)])
            if break_before_next:
                add_ppr_child(p, el("pageBreakBefore"))
                break_before_next = False
            new.append(p)
        elif kind == "p":
            first_after_heading = pk in ("h1", "h2", "h3")
            after_eq = pk == "eqs"
            last_in_block = nk in ("h1", "h2", "h3", "table", "figure", "end")
            fl = 0 if (first_after_heading or after_eq) else None
            af = 120 if last_in_block else None
            if not first_after_heading and not after_eq and not last_in_block:
                p = proto.para(P_BODY, inline_runs(sub_cites(val)))
                # template middle paragraph: indent 0.25in, no space after
                p.find(q("pPr")).remove(p.find(q("pPr")).find(q("spacing"))) if p.find(q("pPr")).find(q("spacing")) is not None else None
                p.find(q("pPr")).remove(p.find(q("pPr")).find(q("ind"))) if p.find(q("pPr")).find(q("ind")) is not None else None
            else:
                p = proto.para(P_BODY, inline_runs(sub_cites(val)), first_line=fl if fl is not None else 360, after=af if af is not None else 0)
                if fl is None:
                    pass
            if break_before_next:
                add_ppr_child(p, el("pageBreakBefore"))
                break_before_next = False
            new.append(p)
        elif kind == "eqs":
            new.append(eq_table(val, xslt))
            new.append(proto.para(P_EMPTY_BTI, []))
        elif kind == "figure":
            if new and new[-1].tag == q("p"):
                pass
            close_section("two")
            new.append(figure(val))
            cap = caption("Figure", val["caption"])
            add_ppr_child(cap, one_col())
            new.append(cap)
            break_before_next = True
        elif kind == "table":
            cap = caption("Table", val["caption"])
            if val["span"] == "full":
                close_section("two")
                new.append(cap)
                new.append(table(val))
                holder = proto.para(P_EMPTY_BTI, [])
                add_ppr_child(holder, one_col())
                new.append(holder)
            else:
                new.append(cap)
                new.append(table(val))
                new.append(proto.para(P_EMPTY_BTI, []))
        prev_kind = kind

    # ---------------- references ----------------
    ref_paras = []
    for k in ordered:
        segs = format_reference(bib[k])
        p = proto.para(P_REF, [])
        for text, ital in segs:
            p.append(run(text, italic=ital))
        ref_paras.append(p)
    new += ref_paras
    add_ppr_child(ref_paras[-1], two_col())

    # final empty paragraph + final section (template keeps a last continuous
    # 1-column section so Word balances the columns on the last page)
    new.append(proto.para(P_EMPTY_BTI, []))
    new.append(copy.deepcopy(final_sectpr_proto))

    for c in list(body):
        body.remove(c)
    for c in new:
        body.append(c)

    # ---------------- package parts ----------------
    doc_xml = etree.tostring(doc, xml_declaration=True, encoding="UTF-8", standalone=True)

    rels = etree.fromstring(zin.read("word/_rels/document.xml.rels"))
    RELNS = "http://schemas.openxmlformats.org/package/2006/relationships"
    for rel in list(rels):
        t = rel.get("Type", "")
        if t.endswith("/hyperlink") or rel.get("Id") in ("rId12", "rId13"):
            rels.remove(rel)
    rels.append(etree.Element("{%s}Relationship" % RELNS, Id="rId12",
                              Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image",
                              Target="media/image1.png"))
    rels_xml = etree.tostring(rels, xml_declaration=True, encoding="UTF-8", standalone=True)

    fn = etree.fromstring(zin.read("word/footnotes.xml"))
    for f in fn.findall(q("footnote")):
        if f.get(q("id")) == "1":
            fn.remove(f)
    fn_xml = etree.tostring(fn, xml_declaration=True, encoding="UTF-8", standalone=True)

    core = zin.read("docProps/core.xml").decode("utf-8")
    now = dt.datetime.now(dt.timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    core = re.sub(r"<dc:title>.*?</dc:title>", "<dc:title>%s</dc:title>" % _esc(meta["title"]), core, flags=re.S)
    core = re.sub(r"<dc:creator>.*?</dc:creator>", "<dc:creator>%s</dc:creator>" % _esc(
        ", ".join(a.strip().split("|")[0].strip() for a in meta["authors"].split(";"))), core, flags=re.S)
    core = re.sub(r"<dc:description>.*?</dc:description>", "<dc:description></dc:description>", core, flags=re.S)
    core = re.sub(r"<cp:lastModifiedBy>.*?</cp:lastModifiedBy>", "<cp:lastModifiedBy>%s</cp:lastModifiedBy>" %
                  _esc(meta["authors"].split(";")[0].split("|")[0].strip()), core, flags=re.S)
    core = re.sub(r"(<dcterms:modified[^>]*>).*?(</dcterms:modified>)", r"\g<1>%s\g<2>" % now, core, flags=re.S)

    replaced = {
        "word/document.xml": doc_xml,
        "word/_rels/document.xml.rels": rels_xml,
        "word/footnotes.xml": fn_xml,
        "docProps/core.xml": core.encode("utf-8"),
        "word/media/image1.png": Path(figure_png).read_bytes(),
    }
    skip = {"word/media/image2.png"}
    written = set()
    with zipfile.ZipFile(out_path, "w", zipfile.ZIP_DEFLATED) as zout:
        for info in zin.infolist():
            if info.filename in skip:
                continue
            data = replaced.get(info.filename, zin.read(info.filename))
            zi = zipfile.ZipInfo(info.filename, date_time=info.date_time)
            zi.compress_type = zipfile.ZIP_DEFLATED
            zi.external_attr = info.external_attr
            zout.writestr(zi, data)
            written.add(info.filename)
        for name, data in replaced.items():
            if name not in written:
                zout.writestr(name, data)
    print(f"wrote {out_path}: {len(ordered)} references, {seq['Table']} tables, {seq['Figure']} figure(s)")
    return ordered, num


def _t(text):
    t = el("t")
    t.text = text
    t.set("{http://www.w3.org/XML/1998/namespace}space", "preserve")
    return t


def _esc(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


if __name__ == "__main__":
    ap = argparse.ArgumentParser()
    ap.add_argument("--template", default=str(HERE / "build" / "template.docx"))
    ap.add_argument("--out", default=str(HERE / "build" / "paper.docx"))
    ap.add_argument("--md", default=str(HERE / "paper.md"))
    ap.add_argument("--bib", default=str(REPO / "chapters" / "08_references.bib"))
    ap.add_argument("--figure", default=str(HERE / "figures" / "architecture.png"))
    a = ap.parse_args()
    build(a.template, a.out, a.md, a.bib, a.figure)
