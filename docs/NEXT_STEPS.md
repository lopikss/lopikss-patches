# Development and testing checklist

- [x] Create `lopikss/lopikss-patches` using the official Morphe template.
- [x] Remove the starter/example Kotlin patches.
- [x] Add a narrowly scoped, off-by-default XP Hero billing activity diagnostic.
- [ ] Verify the Gradle build in GitHub Actions.
- [ ] Confirm that Morphe recognizes the patch bundle.
- [ ] Back up XP Hero progress before any experimental installation.
- [ ] Check that the unmodified purchase UI continues to behave normally.
- [ ] Read the `LopikssBilling` Android activity result codes from Logcat.
- [ ] Compare with the official application's behavior.

Build: `./gradlew buildAndroid`.

The diagnostic patch is **not** intended to make any in-app purchase free. It does not fabricate receipts, modify transaction outcomes or bypass remote purchase verification.

If a build fails, use the exact GitHub Actions error messages to adjust the source.
