#!/usr/bin/env python3
"""Read-only original XP Hero 26.2.0 native shard credit/grade-up inspection."""
import io,zipfile,hashlib,struct
from capstone import Cs,CS_ARCH_ARM64,CS_MODE_ARM
with zipfile.ZipFile("/tmp/xp-hero-262.xapk") as z:
    with zipfile.ZipFile(io.BytesIO(z.read("config.arm64_v8a.apk"))) as f:
        lib=f.read("lib/arm64-v8a/libil2cpp.so")
sha=hashlib.sha256(lib).hexdigest()
print("ORIGINAL",len(lib),sha,flush=True)
assert len(lib)==83724712 and sha=="88b591e6099c55b1147b865847a14651b108f9bd6837707371766a054c8df443"
def data(va,n):
    return lib[va-0x4000:va-0x4000+n]
md=Cs(CS_ARCH_ARM64,CS_MODE_ARM)
for va,size,name in [
  (0x2854af4,0x19c,"CostumeManager.AddCostumePiece"),
  (0x2854e7c,0x58,"CostumePiece.AddPieces"),
  (0x2848438,0x84,"CostumeController.TryGradeUpCostume"),
  (0x2852dd0,0x170,"CostumeController.TryConsumeCostumePiece")]:
    print("====",name,hex(va),"====",flush=True)
    for ins in md.disasm(data(va,size),va):
        print(f"{ins.address:08x} {ins.bytes.hex():10s} {ins.mnemonic:7s} {ins.op_str}",flush=True)
for va,orig in [
    (0x2848460,"d71240f9"),
    (0x2848478,"11180094"),
    (0x284847c,"201a0036"),
    (0x2848484,"d31240f9"),
]:
    print("VERIFY",hex(va),data(va,4).hex(),flush=True)
    assert data(va,4).hex()==orig
mods={0x2848460:(0xd2800037).to_bytes(4,"little"),
      0x2848478:(0x9400319f).to_bytes(4,"little"),
      0x284847c:bytes.fromhex("1f2003d5"),
      0x2848484:(0xd2800033).to_bytes(4,"little")}
modified=bytearray(lib)
for va,ins in mods.items(): modified[va-0x4000:va-0x4000+4]=ins
print("==== PROPOSED ====",flush=True)
for ins in md.disasm(modified[0x2848440-0x4000:0x284849c-0x4000],0x2848440):
    print(f"{ins.address:08x} {ins.bytes.hex():10s} {ins.mnemonic:7s} {ins.op_str}",flush=True)
