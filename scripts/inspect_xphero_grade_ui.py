#!/usr/bin/env python3
"""Read-only disassembly of verified XP Hero 26.2.0 UI callbacks."""
import zipfile,io,hashlib
from capstone import Cs,CS_ARCH_ARM64,CS_MODE_ARM
archive="/tmp/xp-hero-262.xapk"
with zipfile.ZipFile(archive) as outer:
    with zipfile.ZipFile(io.BytesIO(outer.read("config.arm64_v8a.apk"))) as apk:
        data=apk.read("lib/arm64-v8a/libil2cpp.so")
sha=hashlib.sha256(data).hexdigest()
print("ORIGINAL SHA",sha,flush=True)
assert sha=="88b591e6099c55b1147b865847a14651b108f9bd6837707371766a054c8df443"
md=Cs(CS_ARCH_ARM64,CS_MODE_ARM)
for va,size,desc in [
    (0x28691ac,0x1f0,"CostumeManagementView.OnGradeUp UI callback"),
    (0x2874e9c,0xf0,"CostumeViewModel.TryGradeUp UI initiated model"),
    (0x28690bc,0xf0,"CostumeManagementView.ReturnToMainView"),
    (0x286863c,0x1d0,"CostumeManagementView.DeactivateCurrentView"),
    (0x286880c,0x180,"CostumeManagementView.ActivateView"),
    (0x2847e6c,0x1ac,"CostumeManager.TryGradeUp from validation through OnGradeUp notification")
]:
    print("====",desc,hex(va),"====",flush=True)
    for ins in md.disasm(data[va-0x4000:va-0x4000+size],va):
        print(f"{ins.address:08x} {ins.mnemonic:7s} {ins.op_str}",flush=True)
