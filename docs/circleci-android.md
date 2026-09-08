# CircleCI Android CI

GitHub remains the source repository. Android validation and debug APK generation are handled by CircleCI to avoid consuming GitHub Actions minutes on every commit.

## One-time setup

1. Sign in to CircleCI with GitHub.
2. Add/select the private repository `dbrckk/Who-are-you`.
3. Choose **Use existing config**. CircleCI will detect `.circleci/config.yml`.
4. Run the first pipeline on `main`.

No Android signing secret is required for the current debug APK pipeline.

## Pipeline behavior

### `validate`
Runs on pushes and validates:
- Android SDK 37 availability
- bilingual quiz catalog integrity
- Python Play submission generator tests
- JVM unit tests
- Android lint

### `debug_apk`
Runs after `validate` on `main` only and:
- builds `:app:assembleDebug`
- creates `dist/who-are-you-0.1.0-debug.apk`
- creates its SHA-256 checksum
- publishes the `dist` directory in CircleCI **Artifacts**

## GitHub Actions fallback

The following GitHub workflows are now manual-only (`workflow_dispatch`) and no longer consume Actions minutes on every push:
- `.github/workflows/android-ci.yml`
- `.github/workflows/android-apk.yml`
- `.github/workflows/apk-test.yml`

Use them only as an emergency fallback.

## Cost-control strategy

- Validation is separated from APK generation.
- APK generation occurs only after validation succeeds and only on `main`.
- Gradle caches are restored/saved between CircleCI jobs.
- Release/Play publishing remains separate from routine development builds.
