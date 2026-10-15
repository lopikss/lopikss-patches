# XP Hero 26.2.0 — static analysis notes

Reviewed locally (not stored in GitHub):
- Original `XP+Hero_26.2.0_APKPure.xapk`, ARM64, approximately 261.8 MB.
- Patched `XP_Hero-v26.2.0-patches-v1.1.0.apk`, approximately 216.5 MB.

In original Android DEX, detected the library and markers:
- `com.android.billingclient:billing@@8.3.0`
- `Lcom/android/billingclient/api/ProxyBillingActivity;`
- `Activity finished with resultCode `
- `INTENT_SOURCE`
- `BUY_INTENT`

Compared against the patched APK, the inspector detected modifications to these original DEX members:

`classes.dex`, `classes4.dex`, `classes5.dex`, `classes6.dex`, `classes7.dex`, `classes9.dex`, `classes11.dex`, `classes14.dex`, `classes15.dex`, `classes16.dex`.

`classes17.dex` is new in the patched APK. Shared `libil2cpp.so`, `libunity.so` and `global-metadata.dat` content had matching CRCs.

The size reduction is consistent with split merging and compression, not necessarily lost game data. A CRC match is a strong indication of identical file content, **not** runtime equivalence.

XP Hero also contains native Unity / Supercent IAP and receipt-verification logic. No conclusion about whether purchases require remote verification can be established from these marker checks alone.

The patch is an Android activity result logger, not a verified free-store modification.
