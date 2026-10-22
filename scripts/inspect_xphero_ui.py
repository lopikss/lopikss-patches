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

# Decode type and method addresses with a read-only IL2CPP metadata dumper.
# Everything stays on the CI runner. Only the small, selected text summaries
# are printed, and no proprietary binary/metadata is committed or published.
with tempfile.TemporaryDirectory(prefix="xphero-meta-") as td:
    so=os.path.join(td,"libil2cpp.so")
    meta=os.path.join(td,"global-metadata.dat")
    with open(so,"wb") as fp: fp.write(files["lib/arm64-v8a/libil2cpp.so"])
    with open(meta,"wb") as fp: fp.write(m)
    print("DOTNET VERSION",subprocess.run(["dotnet","--version"],capture_output=True,text=True).stdout,flush=True)
    source=os.path.join(td,"dumper")
    subprocess.run(["git","clone","--depth","1","-q","https://github.com/ChanGu54/Il2CppDumper.git",source],check=True)
    build=subprocess.run(["dotnet","build",os.path.join(source,"Il2CppDumper.sln"),"-c","Release","-v","quiet"],capture_output=True,text=True,timeout=300)
    print("DUMPER BUILD",build.returncode,build.stdout[-2000:],build.stderr[-2000:],flush=True)
    if build.returncode==0:
        artifacts=[]
        for root,dirs,names in os.walk(source):
            for name in names:
                if name=="Il2CppDumper.dll" and "/bin/" in root: artifacts.append(os.path.join(root,name))
        if artifacts:
            out=os.path.join(td,"out");os.makedirs(out)
            run=subprocess.run(["dotnet",artifacts[0],so,meta,out],input="\n\n",capture_output=True,text=True,timeout=180,cwd=out)
            print("DUMPER RUN",run.returncode,run.stdout[-5000:],run.stderr[-1800:],flush=True)
            dump=os.path.join(out,"dump.cs")
            if os.path.exists(dump):
                txt=open(dump,encoding="utf8",errors="ignore").read().splitlines()
                import re
                key=re.compile(r"(?:class|void|bool|public|private).*(?:Costume.*(?:Grade|Upgrade|Star)|Star.*Costume|GradeUp|HomeReturnPortal|ReturnPortal|RefreshCostume|UpdateStar|OnClickUpgrade|UpgradeCostume)",re.I)
                rows=[i for i,v in enumerate(txt) if key.search(v)]
                print("DUMPER MATCHES",len(rows),flush=True)
                for i in rows[:180]:
                    print("DUMP",i+1," || ".join(txt[max(0,i-1):min(len(txt),i+3)]),flush=True)
