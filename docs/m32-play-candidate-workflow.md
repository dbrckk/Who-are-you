# M32 — Secure Play candidate workflow

The repository provides `.github/workflows/play-candidate.yml` for manual signed candidate builds and a narrowly scoped `main` request trigger in `.github/workflows/play-candidate-request.yml`.

## Purpose

`Android CI` remains the normal development/repository verification workflow. `Play Candidate` is the only workflow intended to assemble the real upload candidate with account-bound production values.

The workflow does **not** contain the upload key or production service values. It reads them from GitHub Actions secrets attached to the `play-internal` environment.

## Required GitHub Actions secrets

Configure these in the repository/environment before triggering the workflow:

- `WHO_ARE_YOU_ADMOB_APP_ID`
- `WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID`
- `WHO_ARE_YOU_UPLOAD_KEYSTORE_B64`
- `WHO_ARE_YOU_UPLOAD_KEYSTORE_PASSWORD`
- `WHO_ARE_YOU_UPLOAD_KEY_ALIAS`
- `WHO_ARE_YOU_UPLOAD_KEY_PASSWORD`

Optional:

- `WHO_ARE_YOU_TELEMETRY_ENDPOINT` — only consumed when the manual `telemetry_enabled` input is set to true. It remains subject to the app's HTTPS-only guard.

## Keystore encoding

Store the upload keystore as base64 in `WHO_ARE_YOU_UPLOAD_KEYSTORE_B64`. The workflow decodes it into `$RUNNER_TEMP`, sets restrictive file permissions, uses it only for the `playRelease` build and removes the temporary file in an `always()` cleanup step.

The repository must never contain the real `.jks`/`.keystore`/`.p12`/`.pfx` file or any of its passwords.

## Workflow behavior

1. Runs from `workflow_dispatch` or as a reusable workflow called by the trusted main-branch request workflow. Pull requests and generic pushes never run the signing workflow directly.
2. Uses the `play-internal` GitHub Environment.
3. Uses read-only repository permissions.
4. Fails immediately if a required production secret is absent.
5. Materializes the keystore outside the repository checkout.
6. Calls `:app:bundlePlayRelease`, so all M30/M31 production/signing guards still apply.
7. Locates exactly one generated Play AAB.
8. Verifies the bundle signature with `jarsigner -verify -verbose -certs`.
9. Calculates SHA-256.
10. Publishes a versioned, signed candidate AAB, SHA-256, native-library inventory, bundle size report, upload certificate and R8 mapping as a temporary artifact `who-are-you-play-candidate-{versionName}-{versionCode}`.
11. Deletes the temporary keystore even if a previous step fails.

## GitHub Environment recommendation

Create an environment named `play-internal` and place the six required secrets there rather than as broad repository secrets. If your GitHub plan/settings permit it, add environment protection/reviewer rules so a real candidate cannot be generated accidentally.

## What is intentionally not automated here

This workflow does not upload to Google Play. Internal publishing remains manual and explicitly opt-in in `play-internal-publish.yml`, and requires a separate Google Play API service-account credential. A `main` push can **only build the signed AAB** when it modifies `docs/play-candidate-request.json`; it cannot upload or publish to Play. The request workflow checks the version against Gradle and enforces `buildOnly=true` and `telemetryEnabled=false` before invoking the protected candidate job. The `play-internal` GitHub Environment continues to guard signing secrets.

Once the real external secrets exist, trigger **Play Candidate** manually and use its signed AAB artifact for the Internal testing upload.

## Rebuild a current signed AAB without browser automation

Update `docs/play-candidate-request.json` in a branch and merge to `main`. The only automatic trigger is that exact file path, on `main`. The request must match the authoritative Gradle `versionName` and `versionCode`, set `buildOnly=true`, and leave telemetry disabled. The reusable job requires the existing `play-internal` environment and its upload-key/production AdMob secrets; it never uploads to Google Play.

After the run, download its short-lived, versioned signed-candidate artifact. Verify the AAB signature, SHA-256, bundle report and **upload certificate against the existing Play Console application**. This is not an update or release until separately accepted by Google Play.
