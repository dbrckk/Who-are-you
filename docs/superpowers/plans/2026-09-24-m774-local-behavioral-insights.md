# M774 Local Behavioral Insights Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an opt-in, local-only Android “My Habits / Mes habitudes” experience using real authorized app-usage and physical-activity data, with deterministic non-clinical insights and explicit separation from the questionnaire-based PersonalModel.

**Architecture:** M774 adds a separate behavioral domain (`BehaviorSnapshot`, daily aggregates, source states), pure `BehaviorInsightEngine`, bounded local repository, Android collectors behind interfaces, and a Compose screen. `PersonalModel` remains untouched by behavioral evidence; `Who Am I?` may only consume a read-only labeled behavioral summary after the standalone experience is stable.

**Tech Stack:** Kotlin/JVM, Android API 26–36, Jetpack Compose, DataStore Preferences, UsageStatsManager, Android activity/step source selected behind an interface, JUnit4, Compose UI tests, existing Android CI + M59 device validation.

**Spec:** `docs/superpowers/specs/2026-09-24-m774-local-behavioral-insights-design.md`

## Global Constraints

- Behavioral data is local-only in M774 v1.
- Explicit opt-in per source; missing permission is data absence, never zero.
- No call logs, SMS, contacts, notification content, browser history, location, microphone or camera collection.
- No cloud sync and no behavioral payload in telemetry/share/ad decisions.
- Behavioral evidence must never modify `PersonalModel` certainty/confidence.
- Persist bounded daily aggregates rather than an unlimited raw event stream.
- Same-day values are facts; trend language requires multiple comparable days.
- No diagnostic, addiction, personality, moral or causal claim from device behavior.
- EN/FR parity and accessibility are required.
- Keep minSdk 26 / targetSdk 36 compatibility.

## Review Focus

1. A permission is revoked after prior successful collection: UI must move to `PERMISSION_REQUIRED`, preserve no fabricated zero, and stop refreshing that source.
2. Android returns an empty UsageStats result: treat as unavailable/unknown for that interval, not `0 ms`.
3. Local-day/DST boundary changes: each event belongs to exactly one local epoch day/daypart and must not be double-counted.
4. A single extreme usage/activity day: robust baseline must not turn it into a persistent “usual pattern”.
5. Old/uninstalled package IDs: history remains readable without crashing; package name is safe fallback only when a label cannot be resolved.

---

### Task 1: Behavioral domain and deterministic insight engine

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/BehaviorModels.kt`
- Create: `app/src/main/java/com/whoareyou/app/BehaviorInsightEngine.kt`
- Create: `app/src/test/java/com/whoareyou/app/BehaviorInsightEngineTest.kt`

**Interfaces:**
- Produces: `DailyBehaviorAggregate`, `AppUsageAggregate`, `DaypartUsage`, `BehaviorSource`, `BehaviorSourceState`, `BehaviorSnapshot`, `BehaviorInsight`, `BehaviorInsightCategory`, `BehaviorEvidenceTier`.
- Produces: `BehaviorInsightEngine.build(days: List<DailyBehaviorAggregate>, sourceStates: Map<BehaviorSource, BehaviorSourceState>): List<BehaviorInsight>`.

- [ ] **Step 1: Write failing pure JVM tests** covering no-history, missing-day-not-zero, deterministic input ordering, stable multi-day pattern, sustained screen-time change, outlier resistance, repeated late-use evidence, app concentration, disabled source suppression, and a vocabulary guard that rejects clinical/personality labels.

- [ ] **Step 2: Run RED**

Run: `./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.BehaviorInsightEngineTest`
Expected: FAIL because M774 domain/engine types do not exist.

- [ ] **Step 3: Implement immutable domain types and minimal deterministic engine**

Use nullable measured values for missing data. Use named constants for evidence windows. Sort by `epochDay` internally. Use a robust median baseline for comparable historical days. Insights carry measured supporting values and `BehaviorEvidenceTier`, never psychological certainty.

- [ ] **Step 4: Run GREEN**

Run the exact Task 1 test command; expected PASS.

- [ ] **Step 5: Commit**

`git commit -m "feat: add deterministic behavioral insight domain"`

### Task 2: Bounded local behavioral repository

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/BehaviorRepository.kt`
- Create: `app/src/main/java/com/whoareyou/app/BehaviorStore.kt`
- Create: `app/src/test/java/com/whoareyou/app/BehaviorStoreCodecTest.kt`

**Interfaces:**
- Consumes: Task 1 domain models.
- Produces: `BehaviorRepository.observe(): Flow<BehaviorSnapshot>`, `upsert(day)`, `setSourceEnabled(source, enabled)`, `setSourceState(source, state)`, `clearSource(source)`, `clearAll()`.

- [ ] **Step 1: Write failing tests** for deterministic round-trip serialization, 30-day bounded retention, partial/null measurements, per-source clearing, delete-all, and corrupt stored payload falling back safely to empty state.
- [ ] **Step 2: Run RED** with `./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.BehaviorStoreCodecTest`.
- [ ] **Step 3: Implement DataStore-backed storage** using the app’s existing persistence style; keep Android framework collection outside repository code. Retain at most the current local day plus the previous 30 local days.
- [ ] **Step 4: Run GREEN** and then `./gradlew :app:testDebugUnitTest`.
- [ ] **Step 5: Commit** as `feat: persist bounded local behavior history`.

### Task 3: App-usage collector and authorization state

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/AppUsageCollector.kt`
- Create: `app/src/main/java/com/whoareyou/app/UsageAccess.kt`
- Create: `app/src/test/java/com/whoareyou/app/AppUsageAggregationTest.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Produces: `BehaviorCollector<T>.collect(window): BehaviorCollectionResult<T>` (shared collector result types live in `BehaviorModels.kt`).
- Produces: `AppUsageCollector.collectDay(localDate): BehaviorCollectionResult<AppUsageDay>`.
- Produces: `UsageAccess.state(context): BehaviorSourceState` and intent factory for Android usage-access settings.

- [ ] **Step 1: Write failing aggregation tests** using synthetic usage events for local-day clipping, daypart assignment, repeated sessions, empty result != zero, DST/day-boundary safety, and deterministic top-app ordering.
- [ ] **Step 2: Run RED**.
- [ ] **Step 3: Implement UsageStatsManager adapter** with special-access detection and typed denied/available/error states. Declare only the Android usage-stats permission required by this feature; do not add call/SMS/location permissions.
- [ ] **Step 4: Run GREEN**, full JVM tests, and `./gradlew :app:lintDebug`.
- [ ] **Step 5: Commit** as `feat: collect authorized local app usage`.

### Task 4: Physical-activity collector

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/ActivityCollector.kt`
- Create: `app/src/test/java/com/whoareyou/app/ActivityAggregationTest.kt`
- Modify: `app/src/main/AndroidManifest.xml`

**Interfaces:**
- Produces: `ActivityCollector.collectDay(localDate): BehaviorCollectionResult<ActivityDay>`.
- Uses the least-invasive supported Android step/activity source behind `ActivityDataSource`; unsupported devices return `UNSUPPORTED`, denial returns `PERMISSION_REQUIRED`, and true measured zero remains distinguishable from missing data.

- [ ] **Step 1: Write failing tests** for unsupported source, denied authorization, measured zero, missing history, day-boundary aggregation and source revocation.
- [ ] **Step 2: Run RED**.
- [ ] **Step 3: Implement source abstraction and Android adapter**. Add only the activity permission required by the selected API. No location permission and no persistent foreground service.
- [ ] **Step 4: Run GREEN**, full JVM tests and lint.
- [ ] **Step 5: Commit** as `feat: collect authorized physical activity`.

### Task 5: Refresh orchestration

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/BehaviorRefreshCoordinator.kt`
- Create: `app/src/test/java/com/whoareyou/app/BehaviorRefreshCoordinatorTest.kt`

**Interfaces:**
- Consumes: repository + both collectors.
- Produces: `refresh(now: Instant): BehaviorRefreshResult` and source-specific refresh functions.

- [ ] **Step 1: Write failing tests** proving disabled sources are never queried, revocation changes state without writing zero, one collector failure does not erase the other source, repeated refresh is idempotent, and missing days remain missing.
- [ ] **Step 2: Run RED**.
- [ ] **Step 3: Implement opportunistic refresh coordinator** for app-open/resume. Do not add aggressive polling or a foreground service. Add WorkManager only if later device validation demonstrates that opportunistic collection cannot preserve reliable daily aggregates.
- [ ] **Step 4: Run GREEN** and full JVM suite.
- [ ] **Step 5: Commit** as `feat: orchestrate local behavior refresh`.

### Task 6: My Habits presentation model and EN/FR copy

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/BehaviorUiModel.kt`
- Create: `app/src/test/java/com/whoareyou/app/BehaviorUiModelTest.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fr/strings.xml`

**Interfaces:**
- Produces safe presentation rows for Today / 7 days / 30 days / Patterns / Suggestions / Data controls.
- Converts typed source states to localized copy; never exposes internal enum/package IDs as primary user labels.

- [ ] **Step 1: Write failing tests** for disabled, permission-required, unsupported, partial-data and sufficient-history presentation states, plus wording guards against clinical/personality labels.
- [ ] **Step 2: Run RED**.
- [ ] **Step 3: Implement UI model and complete EN/FR resources** including explicit “stored locally on this device” copy and measured-vs-suggested labels.
- [ ] **Step 4: Run GREEN** plus repository localization/resource checks.
- [ ] **Step 5: Commit** as `feat: add localized habits presentation model`.

### Task 7: My Habits Compose screen

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/BehaviorScreenUi.kt`
- Create: `app/src/androidTest/java/com/whoareyou/app/BehaviorScreenUiTest.kt`

**Interfaces:**
- `BehaviorScreen(snapshot, onBack, onEnableUsage, onEnableActivity, onDisableSource, onDeleteAll)`.

- [ ] **Step 1: Write failing Compose tests** for local-only header, source-state CTAs, Today/7-day/30-day sections, patterns separated from suggestions, delete-data control, compact width, and semantic headings.
- [ ] **Step 2: Run instrumentation compile/test RED** using the repository’s existing Android test command/gate.
- [ ] **Step 3: Implement screen** using existing `V2Colors`, `V2Type`, spacing/radius conventions. Avoid charts in v1 unless they add information beyond accessible text; if a chart is introduced, provide an equivalent textual summary.
- [ ] **Step 4: Run GREEN** and `./gradlew :app:assembleDebug :app:lintDebug`.
- [ ] **Step 5: Commit** as `feat: add My Habits screen`.

### Task 8: Navigation, permissions and lifecycle integration

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/AppNavigation.kt`
- Modify: `app/src/main/java/com/whoareyou/app/MainActivity.kt`
- Modify: the existing Discover/Profile entry UI that owns secondary destinations
- Create/Modify: navigation instrumentation tests under `app/src/androidTest/java/com/whoareyou/app/`

**Interfaces:**
- Add `AppScreen.BEHAVIOR` with back destination `DISCOVER` (or the exact existing secondary-destination convention found during implementation).
- Main app observes `BehaviorRepository`, triggers opportunistic refresh on resume/open, and launches Android authorization surfaces only after explicit CTA.

- [ ] **Step 1: Write failing navigation tests** for opening My Habits, system back, permission CTA routing, permission revocation after return, and source-independent operation.
- [ ] **Step 2: Run RED**.
- [ ] **Step 3: Integrate navigation/lifecycle** without changing existing quiz/profile back behavior. Permission requests/settings launches must be user initiated.
- [ ] **Step 4: Run GREEN**, full JVM tests, instrumentation compile/tests, lint and debug build.
- [ ] **Step 5: Commit** as `feat: integrate local habits navigation and permissions`.

### Task 9: Privacy boundaries and profile bridge

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/BehaviorProfileSummary.kt`
- Create: `app/src/test/java/com/whoareyou/app/BehaviorPrivacyBoundaryTest.kt`
- Modify: `app/src/main/java/com/whoareyou/app/ProfileScreenUi.kt` only if a clearly labeled read-only habits summary is useful and does not crowd M773.
- Inspect and test existing share/telemetry/ad code paths.

**Interfaces:**
- Produces a read-only `BehaviorProfileSummary` containing only user-facing aggregate observations suitable for local display.
- It has no API that mutates `PersonalModel`.

- [ ] **Step 1: Write failing boundary tests** proving behavior data is absent from profile-share payloads, telemetry payloads and ad configuration, and that no M774 function changes `PersonalModel` confidence/certainty.
- [ ] **Step 2: Run RED** where a boundary needs implementation; if existing code already satisfies a boundary, record the passing characterization test rather than forcing an artificial failure.
- [ ] **Step 3: Implement only necessary bridge/boundary code**. Any `Who Am I?` insertion must be labeled as observed habits, not personality evidence.
- [ ] **Step 4: Run GREEN and full test suite**.
- [ ] **Step 5: Commit** as `feat: enforce behavioral privacy boundaries`.

### Task 10: Hardening and exact-HEAD verification

**Files:**
- Modify only files required by failures discovered in verification.
- Update M774 docs/PR description with exact verified HEAD and known limitations.

**Interfaces:**
- No new product interface; this task proves the implementation.

- [ ] **Step 1: Run complete JVM suite**: `./gradlew :app:testDebugUnitTest`.
- [ ] **Step 2: Run static/build gates**: `./gradlew :app:lintDebug :app:assembleDebug` plus repository-specific candidate/release verification already used by CI.
- [ ] **Step 3: Run instrumentation/device gates** including M59 exact-HEAD validation. Verify compact screen, >=130% font scale, EN/FR, denied permissions, unsupported activity source, empty UsageStats, old package IDs and source revocation.
- [ ] **Step 4: Fix each discovered regression RED→GREEN**, rerunning the smallest relevant test then the full affected gate.
- [ ] **Step 5: Verify exact HEAD**: Android CI and M59 Device Validation must both be SUCCESS on the same commit SHA.
- [ ] **Step 6: Open/update a draft M774 PR** stacked on M773; do not merge without explicit authorization. Document that calls/messages/location/cloud sync remain excluded.

## Self-review result

- Spec coverage: all scope, privacy, collection, repository, insights, UI, refresh, localization/accessibility, error handling, testing and rollout requirements map to Tasks 1–10.
- Placeholder scan: no implementation TODO/TBD steps remain; API-selection flexibility for physical activity is deliberately encapsulated by `ActivityDataSource` because device/API support must be validated rather than guessed.
- Type consistency: behavioral source/state, daily aggregate, snapshot and repository interfaces are defined before collector/UI consumers.
- Review-focus coverage: revocation (Tasks 4/5/8), empty UsageStats (Task 3), DST/day boundary (Tasks 3/4), outlier baseline (Task 1), old package IDs (Tasks 6/10).