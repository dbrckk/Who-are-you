# Who Are You? — Internal-test smoke test

Run this on the build installed from Google Play Internal testing, not only an Android Studio APK.

## Install and update

- [ ] Fresh install opens without crash.
- [ ] Existing local profile survives an update from the previous internal build.
- [ ] App launches in both EN and FR device locales.
- [ ] System Back behavior is correct from quiz, result and profile screens.

## Core profile flow

- [ ] Start and complete representative quizzes.
- [ ] Score/result is persisted after relaunch.
- [ ] Full profile updates from latest scores.
- [ ] Previous-score evolution appears only after a real retake.
- [ ] Signature/profile recommendation behavior is coherent.
- [ ] Post-completion retake recommendation works.

## Sharing and challenges

- [ ] Result/profile share sheet opens.
- [ ] Generated share card is readable.
- [ ] HTTPS challenge link opens the installed app.
- [ ] Challenge fallback remains usable when the app is not installed.
- [ ] Friend match/compatibility flow completes and can be shared.

## Retention

- [ ] Daily Question can be answered.
- [ ] Streak state persists.
- [ ] Newly unlocked achievement celebration appears once and does not replay after consumption.

## Ads / consent

- [ ] UMP consent flow behaves correctly for the tester/region.
- [ ] Production build uses the intended production AdMob IDs, not Google's CI fallback IDs.
- [ ] Interstitial does not interrupt an active quiz.
- [ ] Frequency cap behavior remains acceptable.

## Billing

- [ ] Play shows product `remove_ads_lifetime` and its localized formatted price.
- [ ] License tester can complete the purchase flow.
- [ ] Successful entitlement removes ads.
- [ ] Purchase is acknowledged.
- [ ] Entitlement restores after relaunch/reinstall as supported by Play Billing.

## Play checks

- [ ] Play pre-launch report reviewed.
- [ ] No blocking crash/ANR.
- [ ] No unresolved policy warning.
- [ ] Store listing, privacy policy and Data Safety answers match the tested production configuration.
