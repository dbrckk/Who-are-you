# Who Are You? — Google Play Data Safety production worksheet

This document is a release-preparation worksheet, not a substitute for the declarations shown in Play Console. Re-check it against the exact production configuration immediately before submission.

## Current app architecture

- No Who Are You? account is required.
- Quiz/profile/streak/onboarding data is stored locally with Android DataStore.
- Google Play Billing handles the optional lifetime remove-ads purchase.
- Google Mobile Ads / AdMob `25.4.0` may serve interstitial ads in the free version.
- Google UMP manages consent state where required.
- Optional HTTPS telemetry can send bounded product events and minimized error reports when configured.
- User-initiated sharing uses the Android system share sheet.

## Data handled by Google Mobile Ads

Google's current Android disclosure guidance for the Google Mobile Ads SDK states that the SDK automatically collects and shares data including:

- IP address;
- user product interactions;
- diagnostic information;
- device and account identifiers, including Android advertising ID when available and not disabled by the developer.

The purposes described by Google include advertising, analytics and fraud prevention. Google states that data collected by the SDK is encrypted in transit.

Because Who Are You? uses AdMob, the production Play Data Safety declaration must account for this SDK behavior even when the app's own custom telemetry does not identify users.

The project currently does **not** explicitly remove the Advertising ID capability from the merged manifest. This is intentional for the current ad-supported product design; therefore the Play Console Advertising ID declaration and Data Safety answers must be completed consistently with the final merged manifest and AdMob configuration.

## App-owned telemetry

When `WHO_ARE_YOU_TELEMETRY_ENDPOINT` is blank, custom HTTP telemetry is disabled.

When it is configured, the app can send bounded events such as:

- app/screen lifecycle events;
- quiz start/completion/abandon events;
- result/share/challenge actions;
- Daily Question and streak events;
- premium funnel events;
- ad impression events;
- minimized error metadata.

Behavioral scores are reduced to coarse score buckets before telemetry. Deep-link query parameters, purchase tokens, free-form user text, email addresses and account identifiers are not part of the custom analytics contract.

Remote error telemetry excludes exception messages and stack traces. A local fatal-crash buffer may temporarily retain diagnostic details on-device, but the next-launch remote crash event is minimized.

## Data categories to review in Play Console

### App activity

Potentially collected by the app when custom telemetry is enabled, and independently processed by Google Mobile Ads where applicable:

- app interactions;
- quiz/test activity;
- shares/challenges;
- retention events;
- ad interactions.

### App info and performance

Potentially applicable through:

- minimized custom error telemetry when enabled;
- Google Mobile Ads diagnostics.

### Device or other identifiers

Applicable to the advertising SDK according to Google's current disclosure guidance. Advertising ID collection is optional at the SDK/platform level, but this project currently does not opt out of it.

### Purchase history

Google Play Billing queries/restores the one-time `remove_ads_lifetime` entitlement. The app does not receive or store payment-card details. Purchase tokens are used locally with Play Billing and are not sent through the custom analytics contract.

## Local-only profile data

Quiz scores, completed tests, Daily Question state, streaks and onboarding state are stored locally. They must not be declared as server-collected by the app merely because they exist on-device. Re-evaluate this if future telemetry begins transmitting raw profile values.

## Advertising / consent

- The Play Console must declare that the app contains ads.
- UMP requests fresh consent information on launch.
- Ad requests are gated by `canRequestAds()`.
- A privacy-options entry point is exposed when UMP reports that one is required.
- The AdMob Privacy & messaging configuration must still be completed in the publisher account before release.

## Collection / sharing decisions to verify

For every applicable category in the final Play Console form, determine:

- collected vs not collected;
- shared vs not shared;
- required vs optional;
- ephemeral vs retained;
- purpose: app functionality, analytics, advertising/marketing, fraud/security, personalization, etc.;
- encrypted in transit;
- deletion options.

## Deletion

Local app data is removed through normal Android app-data deletion/uninstall behavior. If custom production telemetry is enabled and retained server-side, the production operator must publish a support/deletion contact and define the server retention/deletion process.

## Before submission

- [ ] Confirm the exact Google Mobile Ads SDK version in `app/build.gradle.kts`.
- [ ] Re-check Google's current Mobile Ads Data Safety disclosure guidance.
- [ ] Inspect the final merged manifest for Advertising ID behavior.
- [ ] Decide whether custom production telemetry is enabled in v1.0.
- [ ] If telemetry is enabled, document endpoint owner, retention period and deletion process.
- [ ] Confirm `remove_ads_lifetime` exists and is active in Play Console.
- [ ] Complete the Play Console Data Safety form from the production configuration, not debug defaults.
- [ ] Ensure the published privacy policy says the same thing as the Play Console declaration.
