# Who Are You? — Privacy Policy

Last updated: 2026-09-08

Who Are You? is an entertainment and self-reflection app. Its quizzes are not medical or psychological diagnoses.

## Data stored on the device

The app stores quiz completion, latest and previous quiz scores, Daily Question state, streaks, onboarding completion, social comparison state and the lifetime remove-ads entitlement locally on the device using Android DataStore.

The app does not require a Who Are You? account.

## Advertising and consent

The free version can display Google AdMob interstitial advertising outside active quizzes. Google User Messaging Platform (UMP) is used to request and manage consent where required. Advertising requests are gated by the consent state returned by UMP, and the app exposes privacy options when UMP reports that they must be available.

According to Google's current Android disclosure guidance for the Google Mobile Ads SDK, the advertising SDK can automatically collect and share data such as IP address, product interactions, diagnostic information, and device or account identifiers for purposes including advertising, analytics and fraud prevention. Android advertising ID can be used when available and not disabled by the application or user/device settings.

Google and its advertising partners process data under their applicable terms, privacy documentation and the consent choices available to the user.

## Purchases

The optional lifetime remove-ads purchase is processed by Google Play Billing using the product ID `remove_ads_lifetime`. The app does not directly collect or store payment-card information. Purchase status may be queried from Google Play to grant or restore the entitlement.

Purchase tokens used by Google Play Billing are not included in the app's custom analytics events.

## Optional product telemetry

Production builds can be configured to send limited product events and minimized error reports to a developer-controlled HTTPS telemetry endpoint. If no production telemetry endpoint is configured, this custom HTTP telemetry is disabled.

When enabled, the custom analytics contract can include bounded events such as app or screen views, quiz starts/completions, shares/challenges, retention activity, ad impressions and premium purchase-funnel events.

The custom telemetry design does not include names, email addresses, free-form user text, advertising IDs, purchase tokens or deep-link query parameters. Behavioral scores used by analytics are converted into coarse ranges rather than transmitting the raw score.

Remote error telemetry excludes exception messages and stack traces. Fatal crash details may be buffered briefly on-device, while any next-launch remote fatal-crash event is minimized.

Before enabling telemetry for a public release, the operator must define the endpoint owner, retention period, legal basis and deletion process consistently with this policy and the Google Play Data Safety declaration.

## Sharing

Sharing is initiated by the user through Android's system share sheet. Shared content can include generated result/profile images, result text, compatibility information or a challenge link.

Challenge URLs can encode the quiz identifier and challenge score so another device can complete the comparison. The app does not automatically publish or upload shared content to a Who Are You? account or social network.

## App Links

Challenge links can use an HTTPS landing page and an Android custom URI fallback. Android verified App Links require a Digital Asset Links association between the production website and the final app-signing certificate. Until that association is deployed, HTTPS challenge links can still open in the browser and use the fallback handoff.

## Children

The app is not specifically designed for children. The final target-audience and age-range declaration in Google Play Console must be approved before public release and must match the advertising/consent configuration.

## Data deletion

Most Who Are You? profile data is stored locally. Users can remove local application data using Android's app-data controls or by uninstalling the app.

If custom production telemetry is enabled and server-side records are retained, the published support contact must provide the applicable deletion/request process.

## Third-party services

The app can use:

- Google Play Billing;
- Google Mobile Ads / AdMob;
- Google User Messaging Platform (UMP).

These services are governed by the applicable Google terms and privacy documentation.

## Contact

For privacy or support requests, contact: **dbrak7108@gmail.com**.
