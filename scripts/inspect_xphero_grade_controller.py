#!/usr/bin/env python3
import hashlib,io,zipfile
from capstone import Cs,CS_ARCH_ARM64,CS_MODE_ARM
with zipfile.ZipFile("/tmp/xp-hero-262.xapk") as a:
    with zipfile.ZipFile(io.BytesIO(a.read("config.arm64_v8a.apk"))) as b:
        raw=b.read("lib/arm64-v8a/libil2cpp.so")
sha=hashlib.sha256(raw).hexdigest()
print("GAME LIB",len(raw),sha,flush=True)
assert sha=="88b591e6099c55b1147b865847a14651b108f9bd6837707371766a054c8df443"
md=Cs(CS_ARCH_ARM64,CS_MODE_ARM)
for va,n,title in [
  (0x28482fc,0x5d8,"CostumeController.TryGradeUpCostume full body"),
  (0x2847e6c,0x1d0,"CostumeManager.TryGradeUp static body and success"),
  (0x24c237c,0x70,"HomeReturnPortal Free Count"),
  (0x2852dd0,0x224,"CostumeController.TryConsumePiece positive and zero flow"),
  (0x284e4bc,0x130,"CostumeController.HasEnoughPieces"),
  (0x24c4400,0x125,"HomeReturnPortal button next action"),
  (0x24c6330,0x320,"HomeReturnPortal TryUse")
]:
 print("BEGIN",title,hex(va),flush=True)
 for ins in md.disasm(raw[va-0x4000:va-0x4000+n],va):
  print(f"{ins.address:08x} {ins.bytes.hex():10s} {ins.mnemonic:7s} {ins.op_str}",flush=True)
