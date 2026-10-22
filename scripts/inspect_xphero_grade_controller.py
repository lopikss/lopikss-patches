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
  (0x2854ed4,0x1b0,"Core lower-level hero shard TryConsume entry and result"),
  (0x284e4bc,0x130,"CostumeController.HasEnoughPieces"),
  (0x24c4400,0x125,"HomeReturnPortal button next action"),
  (0x24c6330,0x320,"HomeReturnPortal TryUse")
]:
 print("BEGIN",title,hex(va),flush=True)
 for ins in md.disasm(raw[va-0x4000:va-0x4000+n],va):
  print(f"{ins.address:08x} {ins.bytes.hex():10s} {ins.mnemonic:7s} {ins.op_str}",flush=True)

# Assert the exact original ARM64 instructions behind the v1.3.12 edits.
def at(va,n): return raw[va-0x4000:va-0x4000+n]
assert at(0x2852e64,4).hex() == "4d090054"
assert at(0x24c237c,8).hex() == "fe0f1ef8f44f01a9"
assert at(0x24c6458,4).hex() == "e0060036"
assert at(0x2847ea8,4).hex() == "2b010054"
replacements = {
  0x2852e64:bytes.fromhex("4b090054"), # B.LT keeps negative invalid, zero proceeds
  0x24c237c:(0x52800000 | (5000 << 5)).to_bytes(4,"little"),
  0x24c2380:bytes.fromhex("c0035fd6"), # RET, no prologue stack damage
  0x24c6458:bytes.fromhex("1f2003d5"), # NOP, free-item debit need not exist
}
edited=bytearray(raw)
for va,b in replacements.items(): edited[va-0x4000:va-0x4000+len(b)]=b
for va,size,title in [
    (0x2852e60,0x10,"Zero-count native transaction"),
    (0x24c237c,8,"Instant virtual free Return allowance"),
    (0x24c6448,0x18,"Free Return consume success continuation")
]:
 print("VERIFIED EDIT",title)
 for ins in md.disasm(edited[va-0x4000:va-0x4000+size],va): print(hex(ins.address),ins.mnemonic,ins.op_str)
