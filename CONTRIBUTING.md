# Contributing to Lopikss Patches

1. Branch from `dev`, not `main`.
2. Target precise supported app versions, with clear failure conditions for mismatches.
3. Document what you changed and distinguish build tests from on-device behavior.
4. Run `./gradlew :patches:buildAndroid` before submitting a PR to `dev`.
5. Do not upload proprietary APKs, Unity assets, private save files, or account credentials.

Use conventional commit messages: `feat:`, `fix:`, `docs:`, or `chore:`.
The official release workflow publishes signed bundle releases when changes are ready.

Report issues with the game version, Morphe version, patch selection, and a redacted error log.
