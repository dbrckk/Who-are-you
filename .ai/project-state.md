# Project state

Status: Existing Google Play app 0.3.0 update preparation — signed build and Internal publishing not verified; do not promote to Production without explicit approval.

## Working
- App package: `com.whoareyou.app`; code version `0.3.0 (5)`.
- Release upgrade persistence validated by M777 debug→minified candidate device flow; this is not Play-to-Play signature/install validation.
- Latest substantive `main` validation: Android CI, M59, APK and M56 succeeded on `7f9ef430d31076e9b1b7682ffd5bde905fdb11b3` (21 October 2026).
- Play Candidate signed workflow ran successfully historically (11 September 2026), but not for the 0.3.0 head.
- On 21 October 2026, Play Candidate run [39590175005](https://github.com/dbrckk/Who-are-you/actions/runs/39590175005) failed at the benchmark Gradle task selector before secrets/signing; Python quality tests, JVM tests, and instrumentation-test compilation passed. Both Play workflows now specify `:baseline-profile:assembleBenchmarkRelease`, matching the successful Android CI task.
- On 21 October 2026, fresh manually dispatched [Play Candidate run 39680459218](https://github.com/dbrckk/Who-are-you/actions/runs/39680459218) passed Python/JVM/instrumentation compilations, benchmarks, Android lint, protected secrets, upload-keystore materialization and Play release lint; `:app:signPlayReleaseBundle` failed to decrypt the private key using the configured separate upload-key password. Historically successful September workflow used the upload-keystore password as the key password. The initial `keytool -certreq` preflight cannot prove the PKCS12 key password: [run 39684065842](https://github.com/dbrckk/Who-are-you/actions/runs/39684065842) reported source `explicit` but failed to decrypt the key at `:app:signPlayReleaseBundle`. The new patch verifies actual private-key signing with disposable `jarsigner` output and rejects invalid candidates before Gradle.

## 2026-10-27 Health Connect Play policy remediation (pending Play review)
- Google Play Console refused the 25 October update because Health Connect fitness/wellness use was not clearly documented in the public listing and privacy policy did not disclose Health Connect step access.
- Repository audit confirms real opt-in `READ_STEPS` tracking: daily steps, 7/30-day activity trends, and optional step goals, stored only in Android DataStore.
- A dedicated Health Connect privacy rationale activity, explicit EN/FR in-app disclosure and store descriptions, web/Markdown policy, Play data-safety worksheet, and Play Console submission copy are in the fix branch. **No new Play approval or signed-build success may be claimed until verified.**
- On merge, GitHub Pages deployment of `/Who-are-you/privacy/` and Play Console Health Connect declaration and EN/FR listing updates remain mandatory; review whether Google accepts the optional fitness/wellness use case.

## Blockers / not verified
- Google Play Console current highest accepted versionCode and original upload certificate are not accessible through this GitHub connector.
- Signed `playRelease` AAB using the existing upload keystore has not been built/verified for 0.3.0 (5).
- Play Internal receipt, actual Play-signed in-place upgrade, Vitals/pre-launch review and Production promotion are not verified.
- Protected environment signing / AdMob / Play API credentials must be checked in GitHub environment settings; never copy them into repository or chat. Actual `jarsigner` probe with the protected keystore and fallback credentials must be verified by a newly dispatched Play Candidate.

## Current priority
- M787 adds a read-only-version Play API preflight to the protected Internal publishing workflow. The workflow must be manually dispatched; its execution, credentials and Play-side result remain unverified. It checks the package, APK/AAB/track version codes and blocks a reused/obsolete code before upload.
- After the already merged Gradle task-selection fix (PR #95) and initial key preflight fix (PR #97), validate and merge the `jarsigner` probe fix for the PKCS12 keytool false-positive, then manually rerun Play Candidate on `main` for 0.3.0 (5) and confirm the signed bundle succeeds. Compare `play-upload-certificate.txt` against the existing Google Play upload certificate. Publish only to the existing app's Internal (`qa`) track **after** checking the Play version preflight and signing identity; inspect the API receipt and validate a real Play-to-Play upgrade without removing local data. Keep Production unpublished until separate explicit authorization after Vitals/pre-launch review.
- If the existing Play versionCode is >= 5, bump the Android versionCode first and revalidate (never change package name or generate a replacement upload keystore).

## Validation
- Existing Android CI, M59 device tests, APK and M56 RC on `main`: success on `7f9ef43` (21 October 2026). New `jarsigner` probe tested locally against temporary PKCS12 and JKS keystores: wrong explicit password with fallback, valid explicit passwords, and fail-closed for invalid passwords. Real protected keystore not tested with revised probe.
- Play update handoff PR #93 merged; Android CI/M59 success at `cbcfb94e6d090f4f74dec9ec6d9f296663a81e97` (12 October 2026).
- Play remote version preflight and certificate evidence: implementation in release branch, validation not yet observed.

## Last verified
- 2026-10-22 (confirmed PKCS12 keytool false-positive with local reproduction; revised signing probe prepared but real signed bundle and Google Play state still unverified)

<!-- AUTO:START -->
## Automatic repository state

Generated: 2026-10-21T22:37:56Z

### Git
- Branch: `main`
- Head: `eba321e0dc62`
- Commit date: 2026-10-21T18:37:41-04:00
- Commit: fix(play): verify private key by signing disposable JAR
- Tracked files: 697

### Recently changed files
- `docs/play-update-0.3.0.md`
- `tools/verify_play_upload_key.sh`
- `.github/workflows/play-candidate.yml`
- `.github/workflows/play-internal-publish.yml`
- `tools/test_performance_release_gate_contract.py`
- `.github/workflows/android-ci.yml`

### Project signals
- `build.gradle.kts`
- `settings.gradle.kts`

> Generated by dbrckk/repo-standards. Keep manual priorities and blockers outside the AUTO markers.
<!-- AUTO:END -->
