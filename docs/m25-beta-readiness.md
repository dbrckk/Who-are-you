# M25 — Beta readiness

## Repo-side status

- Android package: `com.whoareyou.app`
- Version: `0.1.0` (`versionCode = 1`)
- Target SDK: 36
- Minimum SDK: 26
- CI builds debug APK, release APK, and release AAB.
- Release AAB is uploaded as the `who-are-you-release-aab` workflow artifact on pushes to `main`.
- EN/FR quiz catalog parity is CI-validated.
- Android lint and JVM unit tests are CI gates.
- HTTPS App Links request `android:autoVerify="true"`.
- AdMob IDs and telemetry endpoint are injectable through Gradle properties.
- Ordinary CI deliberately retains Google test AdMob IDs as safe fallbacks.
- Billing product ID expected by the app: `remove_ads_lifetime`.
- Purchase restoration grants the existing entitlement without emitting a new `purchase_success` conversion event.

## External tasks required before Play internal testing

These cannot be completed or verified from the repository alone:

1. Create/configure the app in Google Play Console for package `com.whoareyou.app`.
2. Configure Play App Signing and the upload key; build/upload a properly signed bundle for the chosen release workflow.
3. Create and activate the one-time in-app product `remove_ads_lifetime` in Play Console.
4. Supply production AdMob app/interstitial IDs through `WHO_ARE_YOU_ADMOB_APP_ID` and `WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID`; do not ship the Google test IDs.
5. If production telemetry is used, supply an HTTPS `WHO_ARE_YOU_TELEMETRY_ENDPOINT` and verify the receiver/data-retention policy.
6. Publish the final privacy policy at a stable public HTTPS URL and replace any placeholder contact information.
7. Complete Play Console Data Safety and store-content declarations using `docs/play-data-safety.md` as the repo-side worksheet.
8. Publish production `/.well-known/assetlinks.json` using the Play App Signing SHA-256 certificate fingerprint.
9. Add final store icon, screenshots/phone assets, EN/FR listing copy, and release notes.
10. Test a fresh install, update path, purchase, restore, ads-consent flow, App Links, EN/FR locale switching, and offline/relaunch behavior on Play internal testing.

## Beta acceptance gate

A build is repository-ready when the final `main` Android CI run is fully green and produces a release AAB. It is Play-ready only after every external item above has also been completed and verified in the production Play/AdMob/domain environment.
