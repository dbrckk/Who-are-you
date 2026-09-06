# M32 — Secure Play candidate workflow

The repository now has a dedicated manually triggered workflow: `.github/workflows/play-candidate.yml`.

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

1. Runs only from `workflow_dispatch`.
2. Uses the `play-internal` GitHub Environment.
3. Uses read-only repository permissions.
4. Fails immediately if a required production secret is absent.
5. Materializes the keystore outside the repository checkout.
6. Calls `:app:bundlePlayRelease`, so all M30/M31 production/signing guards still apply.
7. Locates exactly one generated Play AAB.
8. Verifies the bundle signature with `jarsigner -verify -verbose -certs`.
9. Calculates SHA-256.
10. Publishes only the signed candidate plus checksum as artifact `who-are-you-play-candidate-0.1.0-1`.
11. Deletes the temporary keystore even if a previous step fails.

## GitHub Environment recommendation

Create an environment named `play-internal` and place the six required secrets there rather than as broad repository secrets. If your GitHub plan/settings permit it, add environment protection/reviewer rules so a real candidate cannot be generated accidentally.

## What is intentionally not automated here

This workflow does not upload to Google Play. That requires Play Console/API credentials and track configuration which are not currently available through the connected repository tooling. Keeping candidate generation separate also prevents a repository push from publishing anything to Play automatically.

Once the real external secrets exist, trigger **Play Candidate** manually and use its signed AAB artifact for the Internal testing upload.
