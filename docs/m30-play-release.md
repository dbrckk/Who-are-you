# M30 — Strict Google Play build path

`release` and `playRelease` now have different purposes.

## `release`

Used by ordinary CI and local technical validation. It deliberately keeps Google's official test AdMob IDs as safe fallbacks when production IDs are absent. This artifact proves the app compiles/tests, but it is **not the build type to upload to Google Play**.

## `playRelease`

Used only for a Google Play candidate. Any task whose name contains `playRelease` fails during Gradle configuration unless both production AdMob properties are present, structurally valid, and different from Google's test IDs:

- `WHO_ARE_YOU_ADMOB_APP_ID`
- `WHO_ARE_YOU_ADMOB_INTERSTITIAL_ID`

Custom telemetry remains optional. If `WHO_ARE_YOU_TELEMETRY_ENDPOINT` is supplied, it must use HTTPS.

## Production command

After running the M29 generator and obtaining `release.properties`, supply those values as Gradle properties and run:

```text
gradle :app:bundlePlayRelease \
  -PWHO_ARE_YOU_ADMOB_APP_ID=<production-app-id> \
  -PWHO_ARE_YOU_ADMOB_INTERSTITIAL_ID=<production-interstitial-id>
```

If custom production telemetry is intentionally enabled, also supply:

```text
-PWHO_ARE_YOU_TELEMETRY_ENDPOINT=https://...
```

For the first internal candidate, keeping custom telemetry disabled remains the simpler configuration unless the production endpoint and privacy handling are already deployed.

## What this guard does and does not prove

It prevents the Play-specific build path from silently using missing, malformed, or Google's known test AdMob IDs. It does not validate ownership/existence of an AdMob unit; Google/AdMob must still recognize the IDs at runtime.

It also does not replace Play upload signing. The final bundle still needs the upload-key/App Signing workflow configured before Play Console accepts it.

## CI behavior

CI intentionally continues building the normal `release` AAB with safe test fallbacks. Separately, CI probes the `playRelease` guard with missing, malformed, test, and synthetic production-shaped IDs so the safety rule remains deterministic without storing real account credentials.
