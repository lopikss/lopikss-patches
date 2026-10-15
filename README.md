<div align="center">

# ⚡ Lopikss Patches

**Small, focused patches. Clear diagnostics. No guesswork.**

An independent collection of [Morphe](https://github.com/MorpheApp/morphe-patches-template)-compatible patches and Android APK investigation tools.

[![Build](https://github.com/lopikss/lopikss-patches/actions/workflows/release.yml/badge.svg?branch=dev)](https://github.com/lopikss/lopikss-patches/actions/workflows/release.yml)
[![Tests](https://github.com/lopikss/lopikss-patches/actions/workflows/validate.yml/badge.svg?branch=dev)](https://github.com/lopikss/lopikss-patches/actions/workflows/validate.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-Morphe%20patches-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![License: GPL-3.0](https://img.shields.io/badge/license-GPL--3.0-40444A.svg)](LICENSE)
[![Status: Experimental](https://img.shields.io/badge/status-experimental-F3AA35)](#project-status)

[**Browse patches**](#patch-catalog) · [**Get started**](#getting-started) · [**Development**](#development) · [**Report an issue**](https://github.com/lopikss/lopikss-patches/issues)

</div>

---

> [!IMPORTANT]
> **Early development.** The first XP Hero patch traces a Google Play billing activity result for troubleshooting; it does **not** make purchases free, create receipts, disable verification, or grant paid items. This project has passed its source compilation check, but the patch has **not** been validated on a physical device.

## ✨ What is this?

Lopikss Patches is a personal open-source project for understanding Android app compatibility issues and building **small, testable Morphe patches**.

| 🔍 Investigate | 🧩 Patch | 🧪 Verify |
| :--- | :--- | :--- |
| Inspect APK/XAPK contents and version markers. | Keep changes limited to supported apps and versions. | Run build checks and tests before release. |

### Project status

| Area | Current state |
| --- | --- |
| Morphe project | ✅ Configured and compiles |
| First target | **XP Hero 26.2.0** (`io.supercent.weaponrpg`) |
| Billing library detected | Google Play Billing **8.3.0** |
| Patch availability | 🧪 Experimental; **disabled by default** |
| On-device validation | ⏳ Not completed |
| Free in-app purchases | ❌ **Not provided** |

## 📦 Patch catalog

The section below is **automatically updated by the official Morphe release workflow**. Do not edit it by hand.

<!-- PATCHES_START EXPANDED -->

_No published patch bundle yet. Build verification is separate from a released, device-tested patch._

<!-- PATCHES_END -->

### XP Hero · Billing result trace

<details open>
<summary><strong>What does the first patch actually do?</strong></summary>

<br>

- Hooks the Android `ProxyBillingActivity.onActivityResult` callback for XP Hero **26.2.0**.
- Writes the Android activity result integer to Logcat using the tag **`LopikssBilling`**.
- Leaves the checkout flow and purchase results unchanged; the patch never examines receipts or purchase tokens.

> **Reading the result:** Android result `-1` usually indicates `RESULT_OK`, and `0` indicates `RESULT_CANCELED`. These are **not** Google Play Billing response codes and do not prove a transaction succeeded.

</details>

## 🚀 Getting started

**Using Morphe?** Once a patch bundle is published in [Releases](https://github.com/lopikss/lopikss-patches/releases), you can add this repository as a patch source:

**[➕ Add Lopikss Patches to Morphe](https://morphe.software/add-source?github=lopikss/lopikss-patches)**

1. Open Morphe and add the source using the link above.
2. Select **XP Hero 26.2.0** and the patch **Billing result trace**.
3. Back up your game data before testing a modified APK.
4. To inspect the result, connect your Android device with ADB and run:

```bash
adb logcat -s LopikssBilling:I '*:S'
```

> [!CAUTION]
> A modified APK can require reinstallation and may not share the original app's signature. **Do not uninstall your only copy or wipe app data without a backup.** Never approve a real payment just to test a billing diagnostic.

## 🛠️ Development

Built with the [official Morphe patches template](https://github.com/MorpheApp/morphe-patches-template). Work happens on the **[`dev` branch](https://github.com/lopikss/lopikss-patches/tree/dev)**; `main` holds the reviewed version.

```bash
# Clone, switch to dev, run the Morphe build
git clone https://github.com/lopikss/lopikss-patches.git
cd lopikss-patches
git switch dev
./gradlew buildAndroid
```

The `.mpp` output is produced at `patches/build/libs/`. Dependencies for local builds may require [GitHub Packages credentials](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_1_setup.md).

**Read-only APK inspector:** Requires Python 3.10+; supports APK, XAPK and other split APK containers.

```bash
python tools/inspect_apks.py original.xapk --patched modified.apk
python -m unittest discover -s tests -v
```

The inspector compares DEX files and asset metadata. It **cannot** confirm server-side verification or prove a purchase bypass.

**Project resources:** [Technical findings](docs/XP_HERO_FINDINGS.md) · [Roadmap](docs/NEXT_STEPS.md) · [Contributing](CONTRIBUTING.md) · [Security](SECURITY.md) · [Actions](https://github.com/lopikss/lopikss-patches/actions)

---

<div align="center">

Made by **[Lopikss](https://lopikss.com)** · [GPL-3.0 License](LICENSE)

<sub>Independent community project. Not affiliated with Morphe, Google Play, or Supercent.</sub>

</div>
