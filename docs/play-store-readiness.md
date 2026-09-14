# Google Play release readiness

This document lists release conditions that cannot be proven from the Android source tree alone.

## Android App Links

The manifest declares the verified HTTPS route:

`https://dbrckk.github.io/Who-are-you/challenge/`

For Android App Links verification to succeed in production, the site owner must publish:

`https://dbrckk.github.io/.well-known/assetlinks.json`

The file must contain the package name `com.whoareyou.app` and the **Google Play App Signing SHA-256 certificate fingerprint**. The upload-key fingerprint is not sufficient after Play App Signing is enabled.

A template exists at `docs/assetlinks.template.json`. Replace the placeholder only in the deployed website configuration; do not commit a guessed fingerprint.

Before promoting beyond internal testing:

1. Obtain the App Signing certificate SHA-256 fingerprint from Play Console.
2. Publish the final Digital Asset Links JSON at the root `.well-known` URL above.
3. Verify the URL is publicly reachable over HTTPS with status 200 and `application/json` or compatible JSON content.
4. Install the Play-signed build and confirm the HTTPS challenge URL opens directly in the app without the chooser.
5. Keep the custom `whoareyou://challenge` route only as the legacy fallback.

## Release gates already automated

The Play candidate and Play Internal workflows must pass:

- Python contract tests.
- JVM unit tests.
- Android instrumentation-test compilation.
- benchmark variant compilation.
- `lintDebug`.
- `lintPlayRelease` using production AdMob IDs and the upload keystore.
- signed AAB verification.
- Play bundle budget check.
- non-empty R8 `mapping.txt`.

## Data and privacy

The app currently sets `android:allowBackup="false"` and `android:usesCleartextTraffic="false"`. Profile history remains local unless an explicitly configured external telemetry path records an allowed event. Do not enable backup without first defining data extraction/backup rules for profile and crash-buffer data.


## Android 15/16 edge-to-edge

The app targets API 36. Android enforces edge-to-edge for apps targeting API 35 or newer, so top-level Compose surfaces must apply system-bar insets.

Current release contract requires `statusBarsPadding()` on Discover, Profile, Quiz, Result, and Challenge surfaces, while the persistent shell already applies `navigationBarsPadding()`.

Before production rollout, visually smoke-test at least:

- gesture navigation;
- three-button navigation;
- display cutout / hole-punch device;
- large font and display scaling;
- Android 15 and Android 16.

## CI status providers

Both GitHub Actions and CircleCI are configured. CircleCI uses `cimg/android:2026.08.1`, installs pinned Gradle 9.5.0, and installs SDK Platform `android-37` plus Build Tools `37.0.0`.

If branch protection requires CircleCI, do not remove `.circleci/config.yml` without also removing that required status from repository settings.
