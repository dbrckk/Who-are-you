# Versioning policy

Who Are You? uses Android `versionCode` plus a human-readable semantic `versionName`.

## Current track

- `versionCode = 1`
- `versionName = 0.1.0`
- Intended channel: Google Play internal testing / closed testing.

The first production launch should not be renamed to `1.0.0` until the Play Console configuration, production signing, AdMob IDs, Billing product, public privacy policy, verified App Links and store assets are complete and an internal/closed test has passed.

## Rules

1. `versionCode` must increase for every AAB uploaded to Google Play. Never reuse a versionCode already accepted by Play.
2. `versionName` follows `MAJOR.MINOR.PATCH`.
3. PATCH: fixes with no meaningful product-scope change.
4. MINOR: new tests, retention features, social/profile features or substantial UX improvements while compatibility is preserved.
5. MAJOR: major product/platform changes or the first deliberately declared stable production release.

## Release sequence

Recommended initial sequence:

- `0.1.0 (1)` — first internal-test bundle.
- `0.1.1 (2)` — fixes discovered during internal testing, if needed.
- `0.2.0 (3+)` — larger pre-production feature/content iteration, if needed.
- `1.0.0 (next unused versionCode)` — first stable public launch once the release checklist is complete.

Before each Play upload, update both fields in `app/build.gradle.kts`, update release notes, run Android CI and use the generated release AAB artifact from the matching commit.