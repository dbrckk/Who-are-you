# Who Are You? — Production Data Safety decision sheet

This sheet intentionally does not guess Google Play answers that depend on the production SDK behavior or external configuration. Complete it immediately before the Internal testing submission.

## Fixed app facts from the repository

- No Who Are You? account is required.
- Quiz/profile/history/Daily Question/streak/onboarding state is primarily local Android DataStore data.
- The free app contains Google Mobile Ads / AdMob integration.
- Google UMP is used for consent handling where required.
- Google Play Billing handles the optional one-time `remove_ads_lifetime` entitlement.
- Custom product/error telemetry is optional and is disabled when no endpoint is configured.
- If custom telemetry is enabled, its endpoint must use HTTPS.

## Recommended v0.1.0 simplification

For the first Internal testing candidate, leave custom telemetry **disabled** unless you have already deployed and documented a production HTTPS endpoint, operator, retention period and deletion/support process. This reduces the number of custom server-side data flows that must be declared and validated.

This recommendation does **not** remove the need to declare data practices of Google Mobile Ads, UMP or Play Billing.

## Play Console decisions to verify from current Google documentation

Before submitting, review the current disclosures for the exact SDK versions in the release build and decide each Play Data Safety field for:
- device or other identifiers used by advertising/consent SDKs;
- app interactions/activity used for advertising, analytics, fraud prevention or SDK operation;
- diagnostics/performance data, if any SDK transmits it;
- purchase history/entitlement processing through Play Billing;
- any custom telemetry categories if you enable the endpoint.

For every applicable category record:
- collected: yes/no;
- shared: yes/no under Google's Data Safety definition;
- required/optional;
- purpose(s);
- ephemeral/retained where applicable;
- encryption in transit;
- deletion mechanism/handling.

## Do not claim

Do not select “no data collected” solely because profile scores are stored locally. The release contains advertising/consent/Billing SDKs whose current Google disclosures must be included where applicable.

## Final consistency check

The Play form, published privacy policy, AdMob/UMP configuration and actual release build must describe the same production behavior. If one changes, re-review the other three before promotion beyond Internal testing.
