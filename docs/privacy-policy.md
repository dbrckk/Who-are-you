# Who Are You? — Privacy Policy

Last updated: 2026-10-27

Who Are You? is an entertainment and self-reflection app. Its quizzes are not medical or psychological diagnoses.

## Data stored on the device

The app stores quiz completion, latest and previous quiz scores, Daily Question state, streaks, onboarding completion, social comparison state and the lifetime remove-ads entitlement locally on the device using Android DataStore.

The app does not require a Who Are You? account.

## Health Connect: optional steps, activity trends and personal wellness goals

The optional **My Habits → Activity** feature reads **step counts only** from Android Health Connect using `android.permission.health.READ_STEPS`. It does not request permission to write step records, and it does not request access to heart rate, sleep, medical records or any other Health Connect category.

**Why access is requested:** With your explicit permission, the app reads step totals to display your daily steps, show recent activity averages and patterns (last 7 and 30 days), and let you set and check your own step-count goals. This is an optional fitness/wellness activity-tracking feature. The personality quizzes do not require Health Connect access. No medical or psychological diagnosis is inferred from step counts.

**How data is accessed and stored:** After you select Enable/Authorize for Activity and grant Health Connect access in the Android permission dialog, the app aggregates step counts for the current calendar day on the device. It saves the daily totals and related locally calculated activity observations in Android DataStore. When you use the app, daily observations are limited to the current day plus the 30 preceding days (a rolling 31-day window). Older locally stored observations are pruned when the app next runs; if you stop opening the app, previously saved records can remain on your device beyond that window until your next visit or explicit deletion. Your goal settings are stored locally until you delete them or clear the app's data. No Who Are You? account or remote step-data synchronization is required.

**Collection, use and sharing:** The app reads step totals from Health Connect on-device and retains these totals locally for the stated fitness/wellness purposes. The app does **not** upload step data or step goals to its servers, its custom telemetry, Google Mobile Ads, or other third parties. Advertising SDKs may separately handle device/ad data as described under Advertising and consent, but they are not given Health Connect steps by the app. The app does not sell health data or use it for targeted advertising.

**Your choices and deletion:** You can decline permission and use all quizzes without step tracking. Use **My Habits → Activity → Disable** to erase locally stored activity/step observations and turn off collection, or **My Habits → Delete Habits Data** to delete all locally stored habits observations and goals. These in-app actions do not revoke the separate Android permission. To revoke access, open **Android Settings → Health Connect → App permissions → Who Are You?** and turn off the step permission (the exact path varies by device). Revoking the permission prevents new step reads but does not automatically erase previously stored local observations; use the in-app deletion control if you also want that history erased. You may also clear the app's storage or uninstall it to remove local app data. No health-data deletion request to a remote Who Are You? server is necessary because steps are not transmitted to one.

## Optional app-usage observations

The separately optional **My Habits → App usage** feature requests Android Usage Access so you can see on-device screen-time summaries, time-of-day patterns, and related goals. It reads usage timing and app identifiers, not the contents of other apps. These observations remain in local Android DataStore for the same rolling current-day-plus-30-previous-days window. You can disable this source to delete its locally stored observations, or delete all Habits data. Permission revocation happens separately in Android Settings.

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

Most Who Are You? profile data is stored locally. The Profile screen includes a confirmed **Reset profile data** action that removes quiz progress, scores, streaks, comparisons and onboarding state from the device. Users can also remove local application data using Android's app-data controls or by uninstalling the app.

If custom production telemetry is enabled and server-side records are retained, the published support contact must provide the applicable deletion/request process.

## Third-party services

The app can use:

- Google Play Billing;
- Google Mobile Ads / AdMob;
- Google User Messaging Platform (UMP).

These services are governed by the applicable Google terms and privacy documentation.

## Contact

For privacy or support requests, contact: **dbrak7108@gmail.com**.
