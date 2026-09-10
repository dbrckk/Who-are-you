# Who Are You? 0.2.0

## What changed

- Refreshed visual system with a richer violet / blue / aqua palette and improved contrast.
- Added a layered app backdrop and a more expressive Discover hero surface.
- Added subtle ambient motion and stronger pressed-state feedback while preserving existing navigation semantics.
- Improved spacing, typography hierarchy, metrics and card presentation.
- Hardened the minified Android candidate after the WorkManager/Room startup crash found on a physical phone.
- Device validation now installs and launches the minified candidate APK in addition to the debug test build.
- Strengthened persisted quiz-state normalization and replay protection.

## Play Store candidate

- Application ID: `com.whoareyou.app`
- Version code: `2`
- Version name: `0.2.0`
- Target SDK: `36`
- Min SDK: `26`
- Production build type: `playRelease`
- Release bundle task: `:app:bundlePlayRelease`

The production build remains fail-closed: it requires a real upload keystore and production AdMob IDs and rejects Google's test AdMob IDs.

## Before public production

The remaining external inputs are intentionally not committed to the repository: Play upload keystore, signing passwords/alias, production AdMob application/interstitial IDs, Play Console service-account credentials where automation is used, final store screenshots/feature graphic, and final console declarations/review.
