# M28 — Google Play internal-test submission handoff

This is the shortest handoff from the green repository build to a real Google Play Internal testing release.

## Locked build

- Application ID: `com.whoareyou.app`
- Version: `0.1.0` (`versionCode` 1)
- Release artifact: `who-are-you-release-aab`
- Billing product: `remove_ads_lifetime`
- App Links host: `dbrckk.github.io`
- Custom telemetry: optional; if configured, HTTPS only

The machine-readable source of truth is `docs/internal-test-release.json`.

## Repository-side status

Ready:
- Android release build, lint and JVM tests are CI-gated.
- EN/FR Play listing copy exists.
- EN/FR internal-test release notes exist.
- Privacy-policy draft exists.
- Data Safety worksheet exists.
- App Links use HTTPS with `autoVerify`.
- AdMob production IDs are injectable; CI/local fallback remains Google's test IDs.
- Billing product ID is locked in CI.

## Required external actions — do these in order

1. **Play Console app** — create/select the app using package `com.whoareyou.app` and enable Play App Signing.
2. **Upload key** — create and securely retain the upload key/keystore. Do not commit it.
3. **Production AdMob** — create the app + interstitial unit, then build the actual Play upload with production `WHO_ARE_YOU_ADMOB_APP_ID` and `WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID` values. Do not ship the Google test IDs.
4. **Billing** — create and activate the one-time product `remove_ads_lifetime`; configure localized price(s).
5. **Privacy policy** — choose the developer support/contact email, replace the placeholder in `docs/privacy-policy.md`, publish it at a stable public HTTPS URL, and enter that URL in Play Console.
6. **Data Safety / declarations** — complete them against the exact production SDK/configuration. Declare ads. Re-check current Google Mobile Ads, UMP and Play Billing disclosures at submission time.
7. **App Links** — after Play App Signing is enabled, copy the Play app-signing SHA-256 fingerprint into `docs/assetlinks.template.json`, publish it as `https://dbrckk.github.io/.well-known/assetlinks.json`, and verify the HTTPS URL without redirects/authentication.
8. **Store assets** — provide the final app icon, phone screenshots, feature graphic, support email and privacy-policy URL. Use the EN/FR listing copy already in `docs/`.
9. **Internal testing** — upload the signed production-configured AAB, add testers, publish the internal track, then install through the Play opt-in link.
10. **Smoke test** — test clean install + update, EN/FR, quizzes/results/profile, share/challenge links, Daily Question/achievements, AdMob consent/ad frequency, lifetime purchase + restore, and Play pre-launch report.

## Stop conditions

Do not promote beyond Internal testing if any of these are true:
- production build still uses Google test AdMob IDs;
- privacy-policy URL/contact is missing;
- Billing product is missing/inactive;
- Data Safety or ads declaration does not match the actual production build;
- Play App Links are expected to work but production `assetlinks.json` is not published with the Play signing fingerprint;
- blocking crash/ANR or Play policy warning remains.

## After the first Play upload

For every replacement bundle, increment `versionCode` above 1 before uploading. Keep `versionName` user-facing and update release notes when behavior changes.
