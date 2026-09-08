# External release inputs

This file lists the production information that cannot be invented safely by the repository and must come from the owner accounts.

## Google Play Console

Required before a real Play candidate can be published:

- Create the one-time in-app product with the exact ID `remove_ads_lifetime`.
- Set the intended production price (product target is €1.99 lifetime, localized by Google Play where appropriate).
- Create or confirm the application entry for package `com.whoareyou.app`.
- Enable Play App Signing.
- Provide the upload keystore credentials to CI as protected secrets.
- Provide a Google Play service account with the minimum permissions needed by the internal publishing workflow.
- Complete the Advertising ID declaration according to the final merged manifest and Google Mobile Ads SDK behavior.
- Complete Data safety based on actual production telemetry, ads and billing configuration.

## AdMob / Privacy & messaging

Required before `playRelease`:

- Production AdMob application ID (`WHO_ARE_YOU_ADMOB_APP_ID`).
- Production interstitial ad unit ID (`WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID`).
- A Privacy & messaging consent message configured for the app in the Google publisher account.
- Review geography / consent settings and verify the UMP flow on a test device before publication.

The app requests fresh UMP consent information on launch, gates ad requests with `canRequestAds()`, and exposes the privacy-options form when UMP reports that an entry point is required.

## HTTPS App Links

The manifest currently declares verified challenge URLs under:

`https://dbrckk.github.io/Who-are-you/challenge/...`

Android App Links verification requires an `assetlinks.json` file at the domain root:

`https://dbrckk.github.io/.well-known/assetlinks.json`

The file must contain the final signing certificate SHA-256 fingerprint(s) for `com.whoareyou.app`. A project-page path inside this repository is not a substitute for the domain-root `.well-known` location.

Information needed from the owner before this can be finalized:

- whether `dbrckk.github.io` is the permanent production challenge-link domain;
- the final Play App Signing certificate SHA-256 fingerprint once Play App Signing is configured.

The custom URI scheme (`whoareyou://challenge/...`) remains available as a non-verified fallback.

## Privacy policy / Play listing

Before production submission, provide or approve:

- public privacy-policy URL;
- developer/support email shown in Play Console;
- final developer/company display name;
- confirmation of target audience / age range;
- whether production telemetry is enabled and, if so, the production HTTPS endpoint and retention policy.

These values affect the Play Data safety and policy declarations and therefore must not be guessed by CI or source code.
