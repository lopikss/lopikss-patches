<div align="center">

# ⚡ Lopikss Patches

**Focused Android game patches — built for Morphe.**

[![Release build](https://github.com/lopikss/lopikss-patches/actions/workflows/release.yml/badge.svg?branch=main)](https://github.com/lopikss/lopikss-patches/actions/workflows/release.yml)
[![Validation](https://github.com/lopikss/lopikss-patches/actions/workflows/validate.yml/badge.svg?branch=main)](https://github.com/lopikss/lopikss-patches/actions/workflows/validate.yml)
[![License](https://img.shields.io/badge/license-GPL--3.0-40444A.svg)](LICENSE)
[![XP Hero](https://img.shields.io/badge/XP%20Hero%2026.2.0-user--tested-20A67E)](docs/XP_HERO_EXCHANGE.md)

### [➕ Add to Morphe](https://morphe.software/add-source?github=lopikss/lopikss-patches) · [⬇ Latest release](https://github.com/lopikss/lopikss-patches/releases/latest) · [💬 DM me on Discord](https://discord.com/users/493792470778511360)

*Want support for another game? [Request a patch](https://github.com/lopikss/lopikss-patches/issues/new/choose) or message **@lopikss** on Discord.*

</div>

---

## 🎮 Supported games

| Game | Android version | Patch | Status |
| --- | --- | --- | --- |
| **XP Hero** (`io.supercent.weaponrpg`) | **26.2.0** | **Zero-gem Exchange materials** | ✅ **Confirmed working by the project owner on a device** |

### XP Hero — Zero-gem Exchange materials

- Sets the gem prices of **24 material offers** in **Exchange → Upgrade material / Enhancement material** to **0**.
- The project owner has **confirmed the patch works in-game on XP Hero 26.2.0**. This is a user report, not independent testing or a promise about future versions.
- Leaves Google Play purchases, receipts, video-ad offers, and other shop categories unchanged.
- Verifies the original Unity shop asset against its expected SHA-256 and refuses unknown asset versions.
- Remains **off by default** in Morphe so only people who choose it apply it.

**[Technical details and supported scope →](docs/XP_HERO_EXCHANGE.md)**

## 📲 Install in Morphe (Android)

1. Install [Morphe](https://morphe.software), then **[add this patch source](https://morphe.software/add-source?github=lopikss/lopikss-patches)**. Alternatively, go to **Sources → + → Remote** and enter `github.com/lopikss/lopikss-patches`.
2. In the **Lopikss Patches** source card, tap **Update**. If you need to select XP Hero 26.2.0 and the option is shown, enable **Experimental app versions** (and **Pre-release patches** first if Morphe requires it).
3. Under **Settings → Advanced → Expert settings**, enable **Expert mode**.
4. Choose **XP Hero**, select **26.2.0**, and provide the **original, unmodified XAPK** when prompted.
5. In the **Lopikss Patches** tab, enable **XP Hero: Zero-gem Exchange materials**. Tap **Proceed to patching**.
6. Export/save the patched APK when complete; install it once you've protected your existing game data.

> [!CAUTION]
> The patched APK is normally signed with a different certificate. On an unrooted device, installation may require uninstalling the original, **deleting its local data**. Verify your save is safely backed up/synced *before* uninstalling anything. A working patch can still break after game updates.

If patching fails, report the **game version**, **Morphe version**, and **redacted patcher log** in a [bug report](https://github.com/lopikss/lopikss-patches/issues/new/choose).

Morphe documentation: [Patch sources](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patch-sources.md) · [Expert mode](https://github.com/MorpheApp/morphe-manager/blob/main/docs/patching-expert-mode.md)

## 🌐 Will this work for every game?

**Not automatically.** There is no single safe patch that makes all game exchanges free. Different apps use different game engines, resource formats and server-side validation.

The reusable **[Unity cost-table editor](patches/src/main/kotlin/app/lopikss/patches/shared/UnityCostTableEditor.kt)** handles the *same type of embedded cost data* used by XP Hero. Each new game/version still needs its **own adapter**, verified original asset, shop-currency mapping, expected values, compatible package target, and device tests.

For other engines or server-controlled stores, a completely different approach may be required—or the client-side approach may not be viable.

**[How to add support for another game →](docs/ADDING_GAME_SUPPORT.md)**

## 💬 Request patches for other apps

**Want me to look at another Android game? Feel free to DM me!**

- **Discord:** [**@lopikss**](https://discord.com/users/493792470778511360)
- **GitHub:** [Open a patch request](https://github.com/lopikss/lopikss-patches/issues/new/choose)

Tell me the **game name, Android package, exact version**, what you'd like changed, and share screenshots of the relevant game screen if useful. Requests are welcome, but **compatibility and results aren't guaranteed**. Please don't post game accounts, private save data or proprietary APKs in public issues.

## 📦 Available patches

This catalog is generated automatically from published Morphe patch metadata.

<!-- PATCHES_START EXPANDED -->
> **[v1.2.0](https://github.com/lopikss/lopikss-patches/releases/tag/v1.2.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patch total
<details open>
<summary>📦 XP Hero&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 🧪&nbsp;26.2.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [XP Hero: Zero-gem Exchange materials](#xp-hero-zero-gem-exchange-materials) | Set 24 gem-priced material exchanges to zero in XP Hero 26.2.0 (user-tested; IAPs and ad limits unaffected). |  |

</details>

<!-- PATCHES_END -->

### XP Hero: Zero-gem Exchange materials

Verified in the currently supported game version by the project owner. See [implementation notes](docs/XP_HERO_EXCHANGE.md).

## 🛠️ For developers

Based on the [Morphe patches template](https://github.com/MorpheApp/morphe-patches-template).

```bash
git clone https://github.com/lopikss/lopikss-patches.git
cd lopikss-patches
./gradlew :patches:buildAndroid
```

Compiled `.mpp` files are placed in `patches/build/libs/`. **`dev`** is for new changes; **`main`** is for stable releases. For new games, start with [the adapter guide](docs/ADDING_GAME_SUPPORT.md).

[Roadmap](docs/NEXT_STEPS.md) · [Contributing](CONTRIBUTING.md) · [Security](SECURITY.md) · [Releases](https://github.com/lopikss/lopikss-patches/releases)

---

<div align="center">

Made by [**Lopikss**](https://github.com/lopikss) · [GPL-3.0 license](LICENSE)

<sub>Independent community project; not affiliated with Morphe or the game developers.</sub>

</div>
