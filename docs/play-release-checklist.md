# Who Are You? — Google Play release checklist

## Build and version

- [x] Package name is locked to `com.whoareyou.app` in the Android project.
- [ ] Increment `versionCode` for every Play upload after the first accepted bundle.
- [ ] Set the intended public `versionName` before production promotion.
- [ ] Run Android CI and confirm repository tests, JVM tests, candidate lint, release lint, Candidate APK and Release AAB all pass.
- [ ] Download and install the CI `who-are-you-0.1.0-rc.apk` artifact on a real Android device.

The `candidate` build type is intentionally production-like: it inherits release minification/resource shrinking, keeps the production package/version, uses Google test ad IDs, leaves custom telemetry disabled by default, and uses Android's debug signing key only so the APK can be installed directly for acceptance testing. It is not a Play production artifact.

## Signing

- [ ] Create and securely back up an upload keystore; never commit it to Git.
- [ ] Configure Play App Signing in Play Console.
- [ ] Sign the upload AAB with the upload key before Play submission.
- [ ] Record the Play app-signing SHA-256 certificate fingerprint used for Android App Links.

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
- [x] Non-production builds fall back to Google's test AdMob IDs.
- [ ] Configure AdMob privacy/consent messaging as required for served regions.
- [ ] Confirm ads never appear during an active quiz and frequency caps remain enabled during device acceptance testing.

## Billing

- [ ] Create one-time in-app product `remove_ads_lifetime` in Play Console.
- [ ] Set and activate the intended localized price(s).
- [ ] Test purchase, acknowledgement and restore with Play license testers.
- [ ] Confirm the UI displays Google Play's localized formatted price.

## Privacy and Play Console declarations

- [x] Privacy policy source and web page foundation exist.
- [ ] Publish the final privacy policy at a stable public HTTPS URL with the final support contact.
- [ ] Complete Data safety based on the exact production configuration.
- [ ] Declare advertising usage accurately.
- [ ] Complete target audience/content declarations.
- [ ] Complete app access declaration (no account required in current build).
- [ ] Complete content rating questionnaire.

## Telemetry

- [ ] Decide whether production telemetry is enabled for v1.0.
- [ ] If enabled, deploy an HTTPS endpoint and document its operator, retention and deletion handling.
- [ ] Supply `WHO_ARE_YOU_TELEMETRY_ENDPOINT` only in the production build environment.
- [x] Candidate build leaves custom telemetry disabled unless explicitly configured.

## Store listing

- [x] English listing copy foundation exists.
- [x] French listing copy foundation exists.
- [x] Android adaptive launcher icon, round icon and Android 13+ monochrome icon exist.
- [ ] Produce final Play Store 512×512 icon export.
- [ ] Produce phone screenshots in EN and FR where appropriate.
- [ ] Produce final feature graphic.
- [ ] Add support email and privacy-policy URL.
- [ ] Review title/short description lengths in Play Console.

## Device acceptance before Play configuration

- [ ] Obtain a successful M56 Candidate APK build.
- [ ] Install `who-are-you-0.1.0-rc.apk` on the target phone.
- [ ] Test onboarding and cold/warm restart.
- [ ] Complete representative quizzes from every theme and verify scoring/results.
- [ ] Verify profile evolution, Discover, journeys, Daily Question and achievements.
- [ ] Verify sharing/result image generation and challenge links.
- [ ] Verify no ad appears during an active quiz; verify consent/privacy options behavior where applicable.
- [ ] Test airplane/offline behavior and recovery after network restoration.
- [ ] Test French and English device locales.
- [ ] Test back navigation, rotation/process recreation where practical and small-screen scrolling.
- [ ] Report any visual, functional or crash defect and fix all blocking issues before Play setup.

## Play testing and rollout

- [ ] Upload the signed AAB to Internal testing first.
- [ ] Test install/update and Google Play Billing with license testers.
- [ ] Check Play pre-launch report.
- [ ] Fix blocking crashes, ANRs or policy warnings.
- [ ] Promote to Closed/Open testing if required, then Production through the guarded promotion workflow.
