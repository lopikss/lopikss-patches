#!/usr/bin/env python3
"""Read-only disassembly of unmodified XP Hero 26.2.0. No APK distribution."""
import hashlib
import io
import os
import struct
import sys
import zipfile
from capstone import Cs, CS_ARCH_ARM64, CS_MODE_ARM

APK="/tmp/xp-hero-262.xapk"
if not os.path.isfile(APK): raise SystemExit("XAPK download missing")
with zipfile.ZipFile(APK) as archive:
    entries=archive.namelist()
    print("Outer ZIP list:", [(n, archive.getinfo(n).file_size) for n in entries if n.lower().endswith(".apk") or "il2cpp" in n.lower()], flush=True)
    direct=[n for n in entries if n.endswith("lib/arm64-v8a/libil2cpp.so")]
    if direct:
        data=archive.read(direct[0])
    else:
        candidates=[n for n in entries if n.lower().endswith(".apk") and ("arm64" in n.lower() or "config" in n.lower())]
        candidates+= [n for n in entries if n.lower().endswith(".apk") and n not in candidates]
        data=None
        for name in candidates:
            print("Checking nested", name, flush=True)
            raw=archive.read(name)
            with zipfile.ZipFile(io.BytesIO(raw)) as inner:
                matches=[n for n in inner.namelist() if n.endswith("lib/arm64-v8a/libil2cpp.so")]
                if matches:
                    data=inner.read(matches[0]);break
if data is None: raise SystemExit("No arm64 lib found in XAPK")
sha=hashlib.sha256(data).hexdigest()
print("LIB size",len(data),"SHA256",sha,flush=True)
if sha!="88b591e6099c55b1147b865847a14651b108f9bd6837707371766a054c8df443":
    raise SystemExit("Wrong game binary: source does not match pinned patch")
md=Cs(CS_ARCH_ARM64,CS_MODE_ARM)
def show(va, size, title):
    offset=va-0x4000
    print("\n====",title,"VA",hex(va),"offset",hex(offset),"====",flush=True)
    for insn in md.disasm(data[offset:offset+size],va):
        print(f"  {insn.address:08x}  {insn.bytes.hex():10s} {insn.mnemonic:8s} {insn.op_str}")
show(0x2847d70,0x2f0,"TryGradeUp near failed check, debit and exit")
show(0x24c4be0,0x490,"ResetDailyTickets relevant logic")
show(0x2a34ea0,0x200,"Star-upgrade zero-count approval/calculation method 0x2a34f04")
show(0x2a34a80,0x220,"Star-upgrade inventory spending method 0x2a34b14")
show(0x2a33f10,0x1e0,"Return inventory set-count method 0x2a33f70")
show(0x24c6290,0x540,"Return portal activation and free-ticket verification")
show(0x24c6e80,0x380,"Return portal ticket consumption method")
show(0x24c22f0,0x120,"Current free/paid Return ticket counts")

print("\n==== 7001/7002 MOVZ uses near RETURN methods ====",flush=True)
hits=[]
for i,(inst,) in enumerate(struct.iter_unpack("<I",data[:len(data)//4*4])):
    if (inst & 0xFFE00000) in (0x52800000,0xD2800000):
        imm=(inst>>5)&65535
        if imm in (7001,7002):
            off=i*4
            if off<0x5000000: hits.append((off+0x4000,imm))
print("ALL locations count",len(hits))
for va,imm in hits[:180]:
    print("\n=Ticket immediate",imm,"at",hex(va))
    show(va-0x24,0x70,f"Immediate {imm}")
