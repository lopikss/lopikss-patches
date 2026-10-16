# XP Hero 26.2.0 — Experimental zero-cost shops

**Status: UNTESTED EXPERIMENT.** Use only with an original, unmodified XP Hero 26.2.0 XAPK. This does **not** guarantee every shop is free. Do not describe it as working until verified on a device.

## Scope

This patch uses the same SHA-256-verified Unity asset as the user-confirmed Exchange materials patch. Instead of changing only 24 Exchange offers, it changes every **positive `cost_value`** in that asset whose row matches the existing structured shop-cost format and whose `org_price_iap` is zero. The rewrite preserves file length by using leading-zero padding.

The patch requires all 24 previously verified Exchange price rows to match the expected values, and refuses to run if no additional positive-priced non-IAP row exists. Any unknown asset SHA, missing or changed Exchange baseline row, or size change aborts the operation.

**Not covered / unknown:**
- Paid Google Play transactions / IAP billing
- Shops whose prices live in other assets or are enforced by a server
- Ad rewards, refresh limits, stock limits, and purchase caps
- Any shop format that does not match the local serialized row structure

The user's screenshots show Daily Gem Discount, Chests, Ticket Recharge, Token Shop and Invasion Shop, but there is **no evidence yet** that all of those screens use this table. They need in-game tests.

## Morphe

Select **XP Hero: Experimental zero-cost shops** **instead of** **XP Hero: Zero-gem Exchange materials**. Never enable both in one patching run: they each expect the unchanged original resource SHA and edit the same asset.

This is opt-in (`default = false`). Install instructions and data safety notes are in the [README](../README.md).

## Reporting tests

Report whether each screenshot shop shows price zero **and** whether an actual purchase works without reducing gem or token balances. Some game clients display changed prices yet enforce payment on the server. If any shop still charges a currency, do not assume it was patched.
