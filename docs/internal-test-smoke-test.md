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

## Local habits and goals

- [ ] My Habits shows the local-only disclosure before any behavioral source is enabled.
- [ ] Health Connect can be enabled only from the user action in My Habits.
- [ ] Usage Access can be enabled only from the user action in My Habits.
- [ ] Permission denial leaves the affected source unavailable without fabricating zero activity or usage.
- [ ] Permission revocation after prior use is handled without a crash and without inventing a measurement.
- [ ] Today / 7-day / 30-day views remain distinct; incomplete or missing history is presented as missing/building rather than as zero.
- [ ] Create, pause, resume and remove a local goal; the chosen target remains clearly user-defined.
- [ ] A goal persists after relaunch and Activity/process recreation, with progress recomputed from retained local observations.
- [ ] Full Habits reset clears local behavioral history and local goals, while the questionnaire profile remains intact.
- [ ] Verify My Habits and goal creation in EN and FR, on compact width, and at 130% font scale with all controls reachable by scrolling.

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
