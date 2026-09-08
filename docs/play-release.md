# Google Play release contract

This document defines the repository-side release contract for **Who Are You?**.

## Release variants

- `debug`: development APK with Google test ad identifiers when production identifiers are not supplied.
- `release`: unsigned optimized verification bundle used by CI. R8 and resource shrinking are enabled.
- `playRelease`: optimized production bundle. It refuses to evaluate unless production AdMob identifiers and upload-signing material are supplied.

The Play workflows must use `playRelease`, not the generic `release` variant.

## Required protected secrets

The `play-internal` environment is expected to provide:

- `WHO_ARE_YOU_ADMOB_APP_ID`
- `WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID`
- `WHO_ARE_YOU_UPLOAD_KEYSTORE_B64`
- `WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD`
- `WHO_ARE_YOU_UPLOAD_KEY_ALIAS`
- `WHO_ARE_YOU_UPLOAD_KEY_PASSWORD`
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64` for publishing
- `WHO_ARE_YOU_TELEMETRY_ENDPOINT` only when production telemetry is intentionally enabled

Signing files and credentials must never be committed. The repository ignores common keystore formats.

## Automated release gates

The automatic CircleCI job on `main` must pass all of the following before a Play candidate is considered technically ready:

1. bilingual quiz catalog validation;
2. repository Python verification suite;
3. JVM unit tests;
4. Compose instrumentation-test compilation;
5. debug lint;
6. release lint;
7. debug APK assembly;
8. optimized `bundleRelease` assembly;
9. SHA-256 generation for the APK and unsigned release AAB.

The unsigned CI AAB proves that the optimized release variant compiles. It is **not** a Play upload artifact.

## Signed candidate gate

`.github/workflows/play-candidate.yml` is a manual protected workflow. It:

1. validates required production secrets;
2. materializes the upload keystore only in the runner temporary directory;
3. builds `:app:bundlePlayRelease`;
4. verifies the resulting AAB with `jarsigner`;
5. emits a SHA-256 digest;
6. uploads the signed candidate as a temporary workflow artifact;
7. deletes the temporary keystore.

## Internal-track publishing gate

`.github/workflows/play-internal-publish.yml` defaults to validation only. Upload requires the explicit `publish_to_play=true` input. Publishing is limited to the Internal testing track by the locked repository publisher implementation.

Production promotion must not be added as an implicit side effect of a `main` branch push.

## Manifest/privacy defaults

The production manifest intentionally:

- declares only the Internet permission at application level;
- disables Android backup for locally stored profile data;
- rejects cleartext HTTP traffic;
- exports only the launcher/deep-link activities that need external entry points;
- keeps the FileProvider non-exported;
- uses HTTPS App Links with verification for challenge links.

Telemetry is optional and only accepts HTTPS endpoints. The analytics contract is documented in `docs/analytics.md`.

## Google Play Console checklist

Repository automation cannot replace Play Console account configuration. Before the first public release, verify in Play Console:

- app name, default language and category;
- contact email and privacy-policy URL;
- app icon, feature graphic, phone screenshots and store descriptions;
- content rating questionnaire;
- target audience and app-content declarations;
- ads declaration;
- Data safety form consistent with AdMob/UMP, billing and optional telemetry behavior;
- pricing/countries for the one-time `remove_ads_lifetime` product;
- Play App Signing enrollment and upload certificate;
- Internal testing install and purchase test with licensed testers;
- App Link verification on the production domain;
- pre-launch report review before production rollout.

## Release rule

A release is not considered ready merely because source code exists. Minimum evidence is:

- automatic CI release bundle passes;
- signed `playRelease` candidate builds and verifies;
- Internal track install works;
- billing and ads use production configuration;
- Play Console policy/declaration tasks are complete.
