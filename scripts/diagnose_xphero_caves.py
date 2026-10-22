#!/usr/bin/env python3
import zipfile,io,hashlib,struct,os
from elftools.elf.elffile import ELFFile
with zipfile.ZipFile("/tmp/xp-hero-262.xapk") as z:
    with zipfile.ZipFile(io.BytesIO(z.read("config.arm64_v8a.apk"))) as b:
        raw=b.read("lib/arm64-v8a/libil2cpp.so")
assert hashlib.sha256(raw).hexdigest()=="88b591e6099c55b1147b865847a14651b108f9bd6837707371766a054c8df443"
fp=io.BytesIO(raw)
elf=ELFFile(fp)
regions=[]
for p in elf.iter_segments():
    if p["p_type"]=="PT_LOAD":
        print("SEGMENT",hex(p["p_offset"]),hex(p["p_vaddr"]),hex(p["p_filesz"]),p["p_flags"],flush=True)
        if p["p_flags"] & 1:
            regions.append((int(p["p_offset"]),int(p["p_filesz"]),int(p["p_vaddr"])))
for offset,size,virt in regions:
    # An executable code cave must be at least 192 aligned bytes long.
    for pattern,name in ((b"\\x00"*4,"zero"),(b"\\x1f\\x20\\x03\\xd5","nop"),(b"\\x00\\x00\\x20\\xd4","brk")):
        runs=[]
        p=offset
        end=offset+size
        while p+4<=end:
            if raw[p:p+4]!=pattern:p+=4;continue
            st=p
            while p+4<=end and raw[p:p+4]==pattern:p+=4
            n=p-st
            if n>=128:runs.append((n,st,virt+(st-offset)))
        for n,at,va in sorted(runs,reverse=True)[:10]:
            print("CAVE",name,"size",n,"offset",hex(at),"va",hex(va),"before",raw[at-16:at].hex(),"after",raw[at+n:at+n+16].hex(),flush=True)
