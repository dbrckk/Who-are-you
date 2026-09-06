# M29 — Smartphone-friendly Play submission preparation

The repository now reduces the repeatable pre-submission work to one JSON file plus one command.

## 1. Fill one input file

Copy `docs/m28-external-inputs.template.json` to a private working file outside Git, for example `play-inputs.json`, and replace every placeholder with the real Play/AdMob/support values.

Required values:
- support email;
- public HTTPS privacy-policy URL;
- production AdMob app ID;
- production AdMob interstitial ID;
- Play App Signing SHA-256 fingerprint;
- active/priced `remove_ads_lifetime` confirmation;
- tester group/email label.

Never commit the filled file if it contains account-specific production identifiers you do not want in source control.

## 2. Generate the pack

From the repository root:

```bash
python tools/prepare_play_submission.py --input play-inputs.json
```

The default output directory is `build/play-submission/`.

Generated files:
- `assetlinks.json` — publish as `/.well-known/assetlinks.json` on the configured App Links host;
- `production-gradle.properties` — use its AdMob values for the production Play build;
- `privacy-policy-final.md` — publish at the HTTPS privacy-policy URL entered in Play Console;
- `submission-summary.md` — concise final handoff record.

The command stops instead of generating a pack when it detects placeholders, malformed IDs, a non-HTTPS privacy URL, malformed signing SHA-256, or an inactive/unpriced Billing product.

## 3. Smartphone-only option

On Android, this can be run from Termux, a GitHub Codespace/browser terminal, or any mobile-accessible shell with Python 3 and the repository checked out. No Android Studio is required for this preparation step.

## 4. Production build

Apply the generated AdMob properties only to the production release candidate. The generator deliberately omits `WHO_ARE_YOU_TELEMETRY_ENDPOINT`, keeping custom telemetry disabled for the first Internal testing candidate unless a production endpoint has been deliberately deployed and documented.

## 5. What still cannot be automated from this repository alone

The following remain account-bound actions in Google/AdMob infrastructure:
- enabling Play App Signing / obtaining the signing fingerprint;
- creating or activating the Play Billing product;
- creating production AdMob IDs;
- publishing the privacy-policy page and `assetlinks.json` to public HTTPS hosting;
- uploading the signed production AAB and configuring the Internal testing audience;
- completing Play Console policy/Data Safety declarations against the actual production SDK configuration.
