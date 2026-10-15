# Lopikss Patches

Independent [Morphe](https://github.com/MorpheApp/morphe-patches-template) patches for Android compatibility testing and diagnostics.

> **Experimental.** The first XP Hero patch only logs the Android billing activity result code. It does **not** unlock paid items or bypass payments.

## Current patch

**XP Hero: Billing result trace** — experimental and disabled by default. Targets `io.supercent.weaponrpg` version **26.2.0**, which includes Google Play Billing **8.3.0**. When the Play billing activity returns, it logs Android's `resultCode` using the tag `LopikssBilling`. It does not log receipt contents, purchase tokens or user identifiers.

The result is **not** a billing response code: `-1` commonly means Android `RESULT_OK`, while `0` is `RESULT_CANCELED`. Neither guarantees that the game granted a purchase.

## Development

This repository was initialized from the official Morphe patches template.

1. Work on `dev`; merge the generated PR into `main` after testing.
2. In GitHub, check **Actions** for the Gradle verification run.
3. After the patch builds, test with a backed-up save and a nonproduction installation.
4. Read the trace with `adb logcat -s LopikssBilling:I '*:S'`.
5. The official Morphe release workflow creates pre-releases on `dev` and stable releases on `main` for semantic `feat:` or `fix:` commits.
6. When a release publishes a valid `.mpp` bundle, add the source to Morphe: https://morphe.software/add-source?github=lopikss/lopikss-patches

For local development, run `./gradlew buildAndroid`. GitHub Packages dependencies may require credentials described in the [Morphe development guide](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_1_setup.md).

## Read-only APK inspector

Run this on locally stored APKs or XAPKs, with Python 3.10+:

```bash
python tools/inspect_apks.py original.xapk --patched modified.apk
python -m unittest discover -s tests -v
```

The script inspects Android DEX markers and compares file CRCs without modifying either APK. It does not verify purchases or distinguish server-side checks.

See [XP Hero findings](docs/XP_HERO_FINDINGS.md) and [next steps](docs/NEXT_STEPS.md).

## Privacy and safety

Do not commit game APKs, purchase tokens, account information, or raw logs containing personal data. Back up progress before installing re-signed applications. The tracer does **not** forge transactions, skip receipt verification or award paid items.

Independent community project. Not affiliated with Morphe, Google or Supercent.

<!-- PATCHES_START EXPANDED -->

<!-- PATCHES_END -->

Licensed under GPL-3.0; see [LICENSE](LICENSE).
