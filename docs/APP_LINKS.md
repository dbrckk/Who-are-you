# Android App Links rollout

Current migration state:

- Shared challenge URLs use HTTPS: `https://dbrckk.github.io/Who-are-you/challenge/?quiz=<id>&score=<0-100>`.
- `ChallengeActivity` accepts both the HTTPS URL and the legacy `whoareyou://challenge?...` custom scheme.
- `docs/challenge/index.html` provides the browser/install fallback and preserves the challenge parameters.
- The HTTPS intent filter is marked `android:autoVerify="true"` so production verification can activate automatically once the host publishes a valid Digital Asset Links file.
- `docs/assetlinks.template.json` contains the production-ready Digital Asset Links template for package `com.whoareyou.app`.

## Verification dependency

Verified Android App Links require a Digital Asset Links file at:

`https://<production-domain>/.well-known/assetlinks.json`

That file must contain the SHA-256 fingerprint of the certificate that signs the installed production app. If Google Play App Signing is used, use the **App signing key certificate** fingerprint from Play Console, not a local debug/upload certificate.

The current GitHub Pages project URL is suitable as the HTTPS landing page, but Android verifies the host at the root path `https://dbrckk.github.io/.well-known/assetlinks.json`. Publishing that file therefore requires control of the root `dbrckk.github.io` user site (or moving challenge links to another controlled production host).

Until the correct file is published at the host root, Android can still open the HTTPS challenge in the browser and the landing page can hand off to the custom `whoareyou://challenge` scheme or Play Store fallback. The missing Digital Asset Links file affects verified direct-open behavior only; it does not break challenge sharing.

## Final production steps

1. Configure Google Play App Signing.
2. Copy the **App signing key certificate SHA-256 fingerprint** from Play Console.
3. Copy `docs/assetlinks.template.json`, replace `REPLACE_WITH_PLAY_APP_SIGNING_SHA256`, and publish it as `/.well-known/assetlinks.json` on the production host.
4. Serve it over HTTPS with `Content-Type: application/json` and no redirect.
5. If the production host changes, update both the manifest HTTPS intent filter and `ChallengeShare.WEB_HOST` / path.
6. Validate the domain with Play Console Deep Links or Android Studio App Links Assistant.

Expected production file:

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
