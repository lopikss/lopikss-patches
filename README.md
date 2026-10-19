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
| **XP Hero** (`io.supercent.weaponrpg`) | **26.2.0** | **Modular shops, hero Level Up, Evolve and experimental Upgrade** | 🧪 **Community-tested, but some modules still experimental** |

### XP Hero — Modular patches (v1.3.1)

- The **XP Hero 1.3: Exchange Shop** module replaces the old Exchange-only patch and zeroes the same 24 material offers.
- The project owner has reported successful in-game results for the new shop modules, **Free Hero Level Up**, and **Free Hero Evolve**.
- **Free Hero Upgrade** currently displays zero but has not completed an upgrade. The separate **Hero Upgrade (1-piece test)** is a non-free diagnostic fallback; never select both upgrade patches together.
- The project owner reported v1.3.0 testing results on XP Hero 26.2.0; these are user reports, not independent certification.
- Paid Google Play purchases remain outside patch scope. The 5000 PvP Video Tickets option is experimental and was reported not working in-game.
- Uses guarded Unity asset edits to avoid applying changes to unexpected game data.
- Remains **off by default** in Morphe so only people who choose it apply it.

**[Technical details and supported scope →](docs/XP_HERO_EXCHANGE.md)**

## 📲 Install in Morphe (Android)

1. Install [Morphe](https://morphe.software), then **[add this patch source](https://morphe.software/add-source?github=lopikss/lopikss-patches)**. Alternatively, go to **Sources → + → Remote** and enter `github.com/lopikss/lopikss-patches`.
2. In the **Lopikss Patches** source card, tap **Update**. If you need to select XP Hero 26.2.0 and the option is shown, enable **Experimental app versions** (and **Pre-release patches** first if Morphe requires it).
3. Under **Settings → Advanced → Expert settings**, enable **Expert mode**.
4. Choose **XP Hero**, select **26.2.0**, and provide the **original, unmodified XAPK** when prompted.
5. In the **Lopikss Patches** tab, enable the **XP Hero 1.3** modules you want. Do not enable both hero Upgrade variants. Tap **Proceed to patching**.
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
> **[v1.3.4](https://github.com/lopikss/lopikss-patches/releases/tag/v1.3.4)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;14 patches total
<details open>
<summary>📦 XP Hero&nbsp;&nbsp;•&nbsp;&nbsp;14 patches</summary>
<br>

**🎯 Supported versions:**

| 🧪&nbsp;26.2.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [XP Hero 1.3: Chest Shops](#xp-hero-1-3-chest-shops) | Experimental: zero locally stored non-IAP weapon and soul chest costs. |  |
| [XP Hero 1.3: Daily Gem Shop](#xp-hero-1-3-daily-gem-shop) | Experimental: zero local Daily Gem Shop item prices. |  |
| [XP Hero 1.3: Exchange Shop](#xp-hero-1-3-exchange-shop) | Stackable version of the tested 24 zero-gem material exchanges. |  |
| [XP Hero 1.3: Free Hero Evolve](#xp-hero-1-3-free-hero-evolve) | Experimental: zero hero evolution materials and pieces. |  |
| [XP Hero 1.3: Free Hero Level Up](#xp-hero-1-3-free-hero-level-up) | Experimental: zero hero level-up resource costs. |  |
| [XP Hero 1.3: Free Rune Upgrades](#xp-hero-1-3-free-rune-upgrades) | Experimental: zero both resource costs for all 1,440 rune upgrade levels. |  |
| [XP Hero 1.3: Free S-Ticket Rolls](#xp-hero-1-3-free-s-ticket-rolls) | Experimental: make S-Ticket rolls cost 0 in XP Hero 26.2.0. |  |
| [XP Hero 1.3: Free Ticket Refills (400/day)](#xp-hero-1-3-free-ticket-refills-400-day) | Experimental: 0-gem ticket recharges plus 400 daily refills in PvP, Boss Raid, Heroes' Tomb and Challenge Tower. |  |
| [XP Hero 1.3: Free Trait Presets](#xp-hero-1-3-free-trait-presets) | Experimental: zero save/load costs for four trait presets (not rerolls). |  |
| [XP Hero 1.3: Hero Upgrade (1-piece test)](#xp-hero-1-3-hero-upgrade-1-piece-test) | Diagnostic only: hero star upgrades cost one piece, not free. Select instead of Free Hero Upgrade. |  |
| [XP Hero 1.3: Invasion Shop](#xp-hero-1-3-invasion-shop) | Experimental: zero local Invasion Shop coin prices. |  |
| [XP Hero 1.3: Other Shops](#xp-hero-1-3-other-shops) | Experimental: other verified local non-IAP currency prices. |  |
| [XP Hero 1.3: Token Shop](#xp-hero-1-3-token-shop) | Experimental: zero red-token shop prices. |  |
| [XP Hero 1.3: Zero Trait Reroll Coins](#xp-hero-1-3-zero-trait-reroll-coins) | Experimental: change the ARM64 trait reroll cost calculator to return 0, including for locked traits. |  |

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
