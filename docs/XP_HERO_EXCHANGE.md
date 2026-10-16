# XP Hero Exchange: experimental zero-gem material costs

**Status: locally asset-tested, not yet verified on a physical device.**
This Morphe raw resource patch targets only XP Hero Android **v26.2.0**
(package io.supercent.weaponrpg, original APK/XAPK asset SHA-256
4e36ede85e3927f9f98ad3e3ae28a5e3362f1676da4c95e1ed33f6df263e26ac).

## What is changed

The Unity asset `assets/bin/Data/c48bf6e962b374c2d829c84217571712`
stores an embedded ShopProductCostData table. Its group IDs were matched
with ShopProductData entries in material shop categories 6 and 7.
Only the **24 gem-cost rows** for upgrade/enhancement Exchange material
products are edited, in groups:

- 1011 / 1013 / 1014
- 1021 / 1023 / 1024
- 1031 / 1033 / 1034
- 1041 / 1043 / 1044
- 1051 / 1053 / 1054
- 1061 / 1063 / 1064
- 1071 / 1073 / 1074
- 1081 / 1083 / 1084

The `cost_value` JSON digits are replaced by the **same number**
of zero characters (e.g. 50 → 00, 500 → 000, 1000 → 0000). This preserves
the serialized Unity asset's byte length. Unrelated rows and daily ad offers
remain unchanged. The original 829-row cost table has 805 unchanged entries.

## Safety checks

The transformer refuses any asset with a different original SHA-256,
checks each expected row and price, checks that exactly all 24 groups
were changed, and checks that the total asset size remains unchanged.

This does not touch real-money IAPs, purchase receipts, Google Play Billing
or the ad-based daily free redemption limits.

## Important runtime limitation

Passing local transformer checks or a GitHub Actions compile **does not prove**
that the game honors the zero prices when granting materials. The client may
still check its own transaction requirements or contact a server.

## Device validation (no paid purchase necessary)

1. Back up game progress and avoid overwriting your only save.
2. In Morphe, use a **development bundle** with the
   **XP Hero: Zero-gem Exchange materials** patch enabled for XP Hero 26.2.0.
3. Open **Exchange** → **Upgrade material** / **Enhancement material**.
4. Check whether gem prices show 0 and whether choosing a material grants
   it while leaving the gem balance unchanged.
5. Record whether the UI stays at its original price, shows zero but rejects
   the exchange, or grants the material. Screenshots and any crash logs help
   determine the next step.

Keep this patch experimental until this real-device test is complete.
