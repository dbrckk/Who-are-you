# Who Are You? — Google Play release checklist

## Build and version

- [ ] Confirm package name `com.whoareyou.app` is the final Play package name.
- [ ] Increment `versionCode` for every Play upload.
- [ ] Set the intended public `versionName`.
- [ ] Run Android CI and confirm lint, Debug APK, Release APK and Release AAB all pass.
- [ ] Download the CI `who-are-you-release-aab` artifact and verify the expected bundle exists.

## Signing

- [ ] Create and securely back up an upload keystore; never commit it to Git.
- [ ] Configure Play App Signing in Play Console.
- [ ] Sign the upload AAB with the upload key before Play submission.
- [ ] Record the Play app-signing SHA-256 certificate fingerprint used for Android App Links.

Google Play requires new releases to use an app bundle and Play App Signing handles the distribution signing key while the developer keeps an upload key.

## Android App Links

- [x] HTTPS challenge intent filter uses `android:autoVerify="true"`.
- [ ] Publish `https://dbrckk.github.io/.well-known/assetlinks.json` at the host root.
- [ ] Set package name to `com.whoareyou.app` in `assetlinks.json`.
- [ ] Add the production Play app-signing SHA-256 fingerprint.
- [ ] Verify the file is publicly reachable over HTTPS with no redirect/auth requirement.
- [ ] Test challenge URLs on an installed Play-signed build.

## Ads and consent

- [ ] Create the production AdMob app entry.
- [ ] Create the production interstitial ad unit.
- [ ] Supply `WHO_ARE_YOU_ADMOB_APP_ID` and `WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID` to the production build environment.
- [ ] Confirm debug/local builds continue using Google test IDs.
- [ ] Configure AdMob privacy/consent messaging as required for served regions.
- [ ] Confirm ads never appear during an active quiz and frequency caps remain enabled.

## Billing

- [ ] Create one-time in-app product `remove_ads_lifetime` in Play Console.
- [ ] Set and activate the intended localized price(s).
- [ ] Test purchase, acknowledgement and restore with Play license testers.
- [ ] Confirm the UI displays Google Play's localized formatted price.

## Privacy and Play Console declarations

- [ ] Publish the final privacy policy at a stable public HTTPS URL.
- [ ] Replace the contact placeholder in `docs/privacy-policy.md`.
- [ ] Complete Data safety based on the exact production configuration.
- [ ] Declare advertising usage accurately.
- [ ] Complete target audience/content declarations.
- [ ] Complete app access declaration (no account required in current build).
- [ ] Complete content rating questionnaire.

## Telemetry

- [ ] Decide whether production telemetry is enabled for v1.0.
- [ ] If enabled, deploy an HTTPS endpoint and document its operator, retention and deletion handling.
- [ ] Supply `WHO_ARE_YOU_TELEMETRY_ENDPOINT` only in the production build environment.
- [ ] Confirm no credentials or secret telemetry tokens are committed to the repository.

## Store listing

- [x] English listing copy foundation exists.
- [x] French listing copy foundation exists.
- [ ] Add final app icon.
- [ ] Add phone screenshots in EN and FR where appropriate.
- [ ] Add feature graphic.
- [ ] Add support email and privacy-policy URL.
- [ ] Review title/short description lengths in Play Console.

## Testing and rollout

- [ ] Upload the signed AAB to Internal testing first.
- [ ] Test install/update, onboarding, all 15 quizzes, sharing, challenges, Daily Question, ads consent and Billing.
- [ ] Test FR and EN device locales.
- [ ] Check Play pre-launch report.
- [ ] Fix blocking crashes, ANRs or policy warnings.
- [ ] Promote to Closed/Open testing if required, then Production.
