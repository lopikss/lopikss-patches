#!/usr/bin/env python3
"""Read-only inspect original XP Hero 26.2.0 for grade-up-scoped shard credit."""
import io, zipfile, hashlib, os, re, subprocess, tempfile
from capstone import Cs, CS_ARCH_ARM64, CS_MODE_ARM

with zipfile.ZipFile("/tmp/xp-hero-262.xapk") as z:
    for x in z.namelist():
        print("OUTER",x,flush=True)
    with zipfile.ZipFile(io.BytesIO(z.read("config.arm64_v8a.apk"))) as b:
        code=b.read("lib/arm64-v8a/libil2cpp.so")
    meta=None
    for n in z.namelist():
        if n.endswith(".apk"):
            with zipfile.ZipFile(io.BytesIO(z.read(n))) as b:
                if "assets/bin/Data/Managed/Metadata/global-metadata.dat" in b.namelist():
                    meta=b.read("assets/bin/Data/Managed/Metadata/global-metadata.dat")
assert hashlib.sha256(code).hexdigest()=="88b591e6099c55b1147b865847a14651b108f9bd6837707371766a054c8df443"
assert meta
with tempfile.TemporaryDirectory() as td:
    lib=os.path.join(td,"libil2cpp.so");md=os.path.join(td,"global-metadata.dat")
    open(lib,"wb").write(code);open(md,"wb").write(meta)
    repo=os.path.join(td,"dumper")
    subprocess.run(["git","clone","-q","--depth","1","https://github.com/ChanGu54/Il2CppDumper.git",repo],check=True)
    p=subprocess.run(["dotnet","build",os.path.join(repo,"Il2CppDumper.sln"),"-c","Release","-v","quiet"],capture_output=True,text=True,timeout=300)
    print("BUILD DUMPER",p.returncode,p.stdout[-1200:],p.stderr[-500:],flush=True)
    assert p.returncode==0
    dlls=[]
    for root,dirs,files in os.walk(repo):
        if "Il2CppDumper.dll" in files and "/bin/" in root:
            dlls.append(os.path.join(root,"Il2CppDumper.dll"))
    out=os.path.join(td,"output");os.makedirs(out)
    p=subprocess.run(["dotnet",dlls[0],lib,md,out],input="\n\n",capture_output=True,text=True,timeout=180,cwd=out)
    print("DUMPER",p.returncode,p.stdout[-1000:],p.stderr[-500:],flush=True)
    assert p.returncode==0
    dump=open(os.path.join(out,"dump.cs"),errors="replace").read().splitlines()
    for i,line in enumerate(dump):
        if re.search(r'^public (?:static )?class CostumeController\b|^public (?:static )?class CostumeManager\b',line):
            print("CLASS",i,line,flush=True)
            end=next((j for j in range(i+1,len(dump)) if dump[j].startswith("// Namespace:")),min(i+280,len(dump)))
            for l in dump[i:min(end,i+160)]:
                if re.search(r'\b(Add|Acquire|Increase|Give|Set|Get|Grant|Change|TryConsume|TryGradeUp|Piece|Shard|Count|Costume)\w*\s*\(',l) or " RVA: " in l:
                    print("METHOD",l,flush=True)
    names=re.compile(r"\b(AddCostume|AddPiece|AddHero|AcquirePiece|AcquireCostume|GiveCostume|IncreasePiece|SetPieceCount|ChangePieceCount|CostumePiece|Shard)\w*\s*\(",re.I)
    for i,line in enumerate(dump):
        if names.search(line) and "RVA:" not in line:
            print("MATCH",i," || ".join(dump[max(0,i-1):i+1]),flush=True)
    m=Cs(CS_ARCH_ARM64,CS_MODE_ARM)
    for va,n in [(0x2848440,0x85),(0x2854ed4,0x60)]:
        print("ASM",hex(va),flush=True)
        for inst in m.disasm(code[va-0x4000:va-0x4000+n],va):
            print(f"{inst.address:08x} {inst.bytes.hex():10s} {inst.mnemonic:8s} {inst.op_str}",flush=True)
