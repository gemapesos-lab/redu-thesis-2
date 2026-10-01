import zipfile, hashlib, re, sys
from lxml import etree
W="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
tpl, out = sys.argv[1], sys.argv[2]
zt, zo = zipfile.ZipFile(tpl), zipfile.ZipFile(out)
print("== package parts ==")
changed=[]
for n in zo.namelist():
    if n not in zt.namelist(): print("  NEW      ", n); continue
    same = hashlib.sha256(zt.read(n)).digest()==hashlib.sha256(zo.read(n)).digest()
    if not same: changed.append(n)
print("  changed:", changed)
print("  removed:", [n for n in zt.namelist() if n not in zo.namelist()])
for n in ["word/styles.xml","word/numbering.xml","word/settings.xml","word/fontTable.xml","word/footer1.xml","word/theme/theme1.xml","[Content_Types].xml"]:
    if n in zo.namelist(): print("  identical" if zt.read(n)==zo.read(n) else "  DIFFERENT", n)
doc = etree.fromstring(zo.read("word/document.xml")); dt = etree.fromstring(zt.read("word/document.xml"))
sty = lambda d:{e.get("{%s}val"%W) for tag in ("pStyle","rStyle","tblStyle") for e in d.iter("{%s}%s"%(W,tag))}
print("== styles ==\n  used not in template body:", sty(doc)-sty(dt))
ids={s.get("{%s}styleId"%W) for s in etree.fromstring(zo.read("word/styles.xml")).iter("{%s}style"%W)}
print("  undefined styles:", sty(doc)-ids)
text="\n".join("".join(p.itertext()) for p in doc.iter("{%s}p"%W))
print("== leftover template text ==")
for w in ["1st Author","SIG","Bowman","SAMPLE","Conference'10","Conference","Lorem","Columns on Last","Permission to make","doi.org/10.1145/1122445"]:
    print("  ",w, len(re.findall(re.escape(w),text)))
i=text.index("\nREFERENCES\n")
body, refs = text[:i], text[i:]
nref=len(re.findall(r"^\[?\d*", ""))
cites=set()
for m in re.finditer(r"\[(\d+(?:, \d+)*)\]", body):
    cites |= {int(x) for x in m.group(1).split(", ")}
nrefs=len([l for l in refs.strip().split("\n")[1:] if l.strip()])
print("== citations ==\n  refs:", nrefs, " cited:", sorted(cites)==list(range(1,nrefs+1)), "max cited", max(cites))
print("== headings ==")
for p in doc.iter("{%s}p"%W):
    st=p.find("{%s}pPr/{%s}pStyle"%(W,W))
    if st is not None and st.get("{%s}val"%W).startswith("Heading"):
        print("  ",st.get("{%s}val"%W),"".join(p.itertext()))
print("  eq count:", len(list(doc.iter("{http://schemas.openxmlformats.org/officeDocument/2006/math}oMath"))))
print("  words:", len(text.split()))
