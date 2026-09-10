# Who Are You?

Android-first social self-discovery app built around fast personality quizzes, an evolving profile, guided journeys, shareable results, retention loops, friend challenges and lightweight monetization.

## Product goal

- 60-second personality micro-tests
- Highly visual result cards
- Global profile that evolves as tests are completed
- Guided self-discovery journeys spanning multiple themes
- Personalized recommendations and next-test guidance
- Searchable, filterable bilingual quiz library
- Friend challenges and compatibility comparisons
- Daily question and achievements for retention
- Freemium: free core experience + ads, €1.99 one-time purchase to remove ads forever

## Current state

The app has moved well beyond the original prototype. The current product includes:

- Kotlin + Jetpack Compose Android application
- bilingual EN/FR quiz catalog with 30+ tests per locale
- onboarding, Discover, quiz, result and profile flows
- evolving global profile and profile-map visualizations
- signature profiles and personalized recommendations
- result interpretation, retake recommendations and profile evolution
- guided journeys across personality, relationships, inner world, values and direction
- editorial collections plus searchable/filterable quiz library
- progressive library result expansion
- daily question, achievements and retention surfaces
- result/profile/challenge sharing flows
- friend challenge and compatibility infrastructure
- AdMob interstitial support with no ad interruption during quizzes
- Google Play Billing one-time ad-removal purchase
- Play release preparation/publishing tooling
- GitHub Actions validation, device testing and release-candidate workflows

## Quality gates

GitHub Actions validates `main` with:

1. repository and bilingual quiz-catalog integrity checks
2. JVM unit tests
3. instrumentation-test compilation
4. Android candidate and release lint
5. optimized installable candidate APK and unsigned release AAB builds
6. emulator/device UI and runtime smoke validation with crash/ANR detection
7. release-candidate artifact packaging and prerelease publication

## Tech

- Kotlin
- Jetpack Compose
- Material 3
- DataStore
- Google Play Billing
- Google Mobile Ads / UMP
- minSdk 26
- targetSdk 36
- compileSdk 37
- Java 17
- Gradle 9.5

## Product principles

1. A user must understand the product in under 5 seconds.
2. First result should be reachable in under 60 seconds.
3. No ad interrupts a test.
4. Sharing and friend comparison stay free.
5. Quiz results are entertainment/self-reflection, not medical or psychological diagnoses.
6. Recommendations must broaden self-discovery rather than repeatedly surface the same theme.
7. Product progress must remain deterministic, testable and recoverable from persisted local state.
