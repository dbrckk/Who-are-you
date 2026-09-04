# Android App Links rollout

Current migration state:

- Shared challenge URLs use HTTPS: `https://dbrckk.github.io/Who-are-you/challenge/?quiz=<id>&score=<0-100>`.
- `ChallengeActivity` accepts both the HTTPS URL and the legacy `whoareyou://challenge?...` custom scheme.
- `docs/challenge/index.html` provides the browser/install fallback and preserves the challenge parameters.
- The HTTPS intent filter is intentionally not marked `android:autoVerify="true"` yet.

## Why verification is not enabled yet

Verified Android App Links require a Digital Asset Links file at:

`https://<production-domain>/.well-known/assetlinks.json`

That file must contain the SHA-256 fingerprint of the certificate that signs the installed production app. If Google Play App Signing is used, use the **App signing key certificate** fingerprint from Play Console, not a local debug/upload certificate.

The current GitHub Pages project URL is suitable as a temporary HTTPS landing page, but its project path does not provide control of `https://dbrckk.github.io/.well-known/assetlinks.json` from this repository. Production verification therefore needs either:

1. a dedicated domain controlled by the project, or
2. control of the root `dbrckk.github.io` user site.

## Final production steps

1. Choose the production HTTPS host.
2. Serve `/.well-known/assetlinks.json` over HTTPS with `Content-Type: application/json` and no redirect.
3. Insert package name `com.whoareyou.app` and the Play App Signing SHA-256 fingerprint.
4. Change the HTTPS intent filter host/path to the production host if necessary.
5. Add `android:autoVerify="true"` to that HTTPS intent filter.
6. Update `ChallengeShare.WEB_HOST` / path to the same host.
7. Validate the domain with Play Console Deep Links or Android Studio App Links Assistant.

Example `assetlinks.json` once the production fingerprint is known:

```json
[
  {
    "relation": ["delegate_permission/common.handle_all_urls"],
    "target": {
      "namespace": "android_app",
      "package_name": "com.whoareyou.app",
      "sha256_cert_fingerprints": [
        "REPLACE_WITH_PLAY_APP_SIGNING_SHA256"
      ]
    }
  }
]
```
