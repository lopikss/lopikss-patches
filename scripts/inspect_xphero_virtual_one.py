#!/usr/bin/env python3
"""Read-only scan: find code regions for scoped star-transaction injection."""
import hashlib,io,zipfile,collections
with zipfile.ZipFile("/tmp/xp-hero-262.xapk") as outer:
    with zipfile.ZipFile(io.BytesIO(outer.read("config.arm64_v8a.apk"))) as a:
        raw=a.read("lib/arm64-v8a/libil2cpp.so")
assert hashlib.sha256(raw).hexdigest()=="88b591e6099c55b1147b865847a14651b108f9bd6837707371766a054c8df443"
# Original lib .text has virtual-address -0x4000 relationship.
start=0x1c8ab84
end=0x49bf980
print("FILE_RANGE",hex(start),hex(end),flush=True)
for pat,label in [(bytes.fromhex("00000000"),"zero"),(bytes.fromhex("1f2003d5"),"nop"),(bytes.fromhex("000020d4"),"brk")]:
    runs=[]
    i=start+(4-start%4)%4
    while i+4<=end:
        if raw[i:i+4]!=pat:i+=4;continue
        st=i
        while i+4<=end and raw[i:i+4]==pat: i+=4
        n=i-st
        if n>=20:runs.append((n,st,st+0x4000))
    print("PATTERN",label,"caves >= 20",len(runs))
    for size,offset,va in sorted(runs,reverse=True)[:40]:
        print("CAVE",label,"size",size,"file",hex(offset),"virtual",hex(va),flush=True)
# Also print aligned short return-padding (RET instruction and following unused)
pat=bytes.fromhex("c0035fd6")
count=0
for i in range(start,end-4,4):
    if raw[i:i+4]==pat and raw[i+4:i+16]==b"\\x00"*12:
        count+=1
print("RET_WITH_PADDING_12",count)
