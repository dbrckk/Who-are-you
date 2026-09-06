# M33 — Google Play Developer API internal publishing

This milestone adds a guarded path from the signed `playRelease` bundle to the Google Play **Internal testing** track.

## Workflow

Use `.github/workflows/play-internal-publish.yml` (`Play Internal Publish`).

The workflow is manual only. By default, `publish_to_play` is `false`, so it only validates the locked API payload and does not call Google Play.

When `publish_to_play=true`, the workflow:
1. validates required GitHub Environment secrets;
2. materializes the upload keystore and Google service-account JSON only under `$RUNNER_TEMP`;
3. builds the strict signed `playRelease` bundle;
4. verifies its signature and records SHA-256;
5. creates a Google Play edit;
6. uploads the AAB;
7. assigns versionCode `1` to the `internal` track;
8. validates the edit;
9. commits the edit;
10. stores a non-secret publishing receipt and removes temporary credentials.

## Required `play-internal` environment secrets

Existing M32/M31 secrets:
- `WHO_ARE_YOU_ADMOB_APP_ID`
- `WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID`
- `WHO_ARE_YOU_UPLOAD_KEYSTORE_B64`
- `WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD`
- `WHO_ARE_YOU_UPLOAD_KEY_ALIAS`
- `WHO_ARE_YOU_UPLOAD_KEY_PASSWORD`

New M33 secret:
- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64`

`GOOGLE_PLAY_SERVICE_ACCOUNT_JSON_B64` must be the base64 encoding of the complete Google service-account JSON file. Never commit the JSON file or its base64 value.

## Google-side prerequisites

Before the first real publish run:
- create/select the Google Cloud service account used for publishing;
- enable/use the Google Play Android Developer API for the linked project as required by the account setup;
- grant that service account access in Play Console **Users and permissions**;
- grant only permissions needed to upload/manage releases for `com.whoareyou.app`;
- ensure the Play Console app already exists and package name is exactly `com.whoareyou.app`;
- configure Play App Signing and the upload key;
- configure `remove_ads_lifetime` and the production AdMob IDs separately as already documented.

## Release status

The workflow exposes only:
- `draft`
- `completed`

Use `draft` for the first API-driven test until the Play Console configuration and tester setup are confirmed. `completed` commits the internal release in a completed state.

## Locked safety properties

- Package: `com.whoareyou.app`
- Track: `internal`
- Version: `0.1.0` / versionCode `1`
- Actual publishing is opt-in (`publish_to_play=false` by default).
- The Python helper refuses another package, track, version code or version name.
- The AAB is built through M30/M31 `playRelease` production/signing guards.
- The edit is validated before it is committed.
- Temporary service-account and keystore files are deleted with `if: always()`.

## After a successful first upload

Google Play does not allow reusing the same versionCode for a replacement bundle. Increment `versionCode` before the next upload and update the locked release/API contracts and tests together.
