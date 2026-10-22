#!/usr/bin/env python3
"""Read-only XP Hero 26.2.0 UI method/metadata inspection. No binary export."""
import io, zipfile, hashlib, re, os, subprocess, tempfile
outer="/tmp/xp-hero-262.xapk"
if not os.path.isfile(outer): raise SystemExit("No XAPK")
files={}
with zipfile.ZipFile(outer) as z:
    for n in z.namelist():
        if not n.endswith(".apk"): continue
        print("EXAMINING APK:",n,flush=True)
        with zipfile.ZipFile(io.BytesIO(z.read(n))) as x:
            for f in x.namelist():
                if f in ("assets/bin/Data/Managed/Metadata/global-metadata.dat","lib/arm64-v8a/libil2cpp.so"):
                    files[f]=x.read(f)
print("FILES",[(k,len(v),hashlib.sha256(v).hexdigest()) for k,v in files.items()])
m=files.get("assets/bin/Data/Managed/Metadata/global-metadata.dat")
if m is None: raise SystemExit("missing metadata")
terms=re.compile(rb"(?:grade.?up|star.?grade|costume|hero.?upgrade|return.?ticket|return.?portal|refresh.*hero|upgrade.*view|upgrade.*button)",re.I)
print("METADATA MAGIC",m[:16].hex())
matches=list(terms.finditer(m))
print("STRING-MATCH TOTAL",len(matches))
done=set()
for h in matches:
    p=h.start()
    low=p
    while low>0 and 32<=m[low-1]<127 and p-low<120:low-=1
    hi=h.end()
    while hi<len(m) and 32<=m[hi]<127 and hi-p<150:hi+=1
    s=m[low:hi].decode("ascii","ignore").strip()
    if not s or s in done:continue
    done.add(s)
    if len(done)>650:break
    print(f" META {p:08x} {s}",flush=True)
