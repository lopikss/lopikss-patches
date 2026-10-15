# Contributing to Lopikss Patches

Thanks for helping improve the project.

## Before opening a PR

1. Check existing [issues](https://github.com/lopikss/lopikss-patches/issues) and open pull requests.
2. Work from the `dev` branch, not `main`.
3. Keep new patches narrow in scope and pin exact supported versions when practical.
4. Explain what is modified, why, and how it was tested. Distinguish build verification from device testing.
5. Run `python -m unittest discover -s tests -v` and `./gradlew buildAndroid` when relevant.
6. Open a pull request into `dev`.

Do not submit copyrighted proprietary game binaries, account credentials, billing tokens, or payment receipts. Attach minimal redacted logs instead.

## Commits and releases

Use conventional commits such as `fix: correct billing trace fingerprint`, `feat: add supported diagnostic patch`, or `docs: improve setup instructions`. The official Morphe release workflow uses commit types to determine releases.

## Support

Use the [bug report form](https://github.com/lopikss/lopikss-patches/issues/new/choose) and include the application package, version, Morphe version, exact patch settings, and a redacted error message.
