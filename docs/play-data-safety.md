# Who Are You? — Play Data Safety foundation

This document is a release-preparation worksheet, not a substitute for the declarations shown in Play Console. Re-check it against the exact production configuration immediately before submission.

## Current app architecture

- No Who Are You? account is required.
- Quiz/profile/streak/onboarding data is stored locally with Android DataStore.
- Google Play Billing handles the optional lifetime remove-ads purchase.
- Google Mobile Ads / AdMob may serve interstitial ads in the free version.
- Google UMP manages consent state where required.
- Optional HTTPS telemetry can send product events and error/crash reports when configured.
- User-initiated sharing uses the Android system share sheet.

## Data categories to review in Play Console

### App activity
Potentially applicable when production telemetry is enabled:
- quiz/test starts and completions;
- share/challenge actions;
- Daily Question votes/streak events;
- purchase-success events;
- ad-impression events.

Current custom telemetry is designed without a Who Are You? account identifier, but Play declarations must reflect what the production endpoint and third-party SDKs actually transmit.

### App info and performance
Potentially applicable:
- non-fatal errors;
- fatal crash reports stored on-device and forwarded on a later launch when telemetry is configured.

### Device or other identifiers
Potentially applicable through Google Mobile Ads and related Google SDK behavior. Confirm the current Google Mobile Ads/UMP Data Safety guidance at submission time rather than assuming no identifier processing.

### Purchase history
Google Play Billing is used to query/restore the one-time remove-ads entitlement. Confirm the Play Billing SDK's current disclosure requirements in Play Console.

## Collection / sharing decisions to verify

For every applicable category, determine in the final Play Console form:
- collected vs not collected;
- shared vs not shared;
- required vs optional;
- ephemeral vs retained;
- purpose: app functionality, analytics, advertising/marketing, fraud/security, personalization, etc.;
- whether data is encrypted in transit;
- whether deletion can be requested.

## Local-only profile data

The app's quiz scores, completed tests, Daily Question state, streaks and onboarding state are stored locally. They should not be represented as server-collected data unless a future production telemetry payload begins transmitting those values beyond the currently described event fields.

## Advertising

Because the free build includes AdMob, the Play Console must declare that the app contains ads. Consent configuration and region-specific ad behavior must be finalized in AdMob/UMP before production rollout.

## Deletion

Local app data is removed with normal app-data deletion/uninstall behavior. If custom production telemetry is enabled and retained server-side, publish a support/deletion contact and document the server retention/deletion process.

## Before submission

- [ ] Compare this worksheet against current Google Mobile Ads Data Safety documentation.
- [ ] Compare against current Google Play Billing disclosures.
- [ ] Decide whether custom production telemetry is enabled in v1.0.
- [ ] Inspect actual telemetry payload fields.
- [ ] Complete the Play Console Data safety form from the production configuration, not from debug defaults.
