# CircleCI Android CI

GitHub remains the source repository. Routine Android validation and debug APK generation are handled by CircleCI so normal development no longer consumes GitHub Actions minutes.

## One-time setup

1. Sign in to CircleCI with GitHub.
2. Add/select the private repository `dbrckk/Who-are-you`.
3. Choose **Use existing config**. CircleCI will detect `.circleci/config.yml`.
4. Run the first pipeline on `main`.

No Android signing secret is required for the current debug APK pipeline.

## Pipeline behavior

The `android_ci` job runs on `main` and performs the complete routine validation in one container:

- installs Android SDK 37/build-tools 37 when required;
- pins Gradle `9.5.0` to match the previous GitHub CI environment;
- validates bilingual quiz catalog integrity;
- runs Python Play submission generator tests;
- runs JVM unit tests;
- runs Android lint;
- builds `:app:assembleDebug`;
- creates `dist/who-are-you-0.1.0-debug.apk`;
- creates its SHA-256 checksum;
- publishes `dist` under CircleCI **Artifacts**.

## Cost-control strategy

- Validation and debug APK generation share one CircleCI job instead of two separate containers.
- The workflow only runs on `main`.
- Gradle 9.5.0 binaries and Gradle dependency caches are persisted.
- No emulator is launched for routine CI.
- Release signing and Google Play publishing remain separate from routine development CI.

## GitHub Actions fallback

These GitHub workflows are manual-only (`workflow_dispatch`) and therefore do not consume Actions minutes for ordinary pushes:

- `.github/workflows/android-ci.yml`
- `.github/workflows/android-apk.yml`
- `.github/workflows/apk-test.yml`
- `.github/workflows/play-candidate.yml`
- `.github/workflows/play-internal-publish.yml`

The Play workflows intentionally remain on GitHub as guarded, manual release operations because their existing environments/secrets and release protections are already configured there. Routine development should use CircleCI.

## Provider note

CircleCI's official Android convenience image `cimg/android` supports Docker-executor Android builds and includes the Android command-line tooling used by this project. The repository currently pins `cimg/android:2026.08`.
