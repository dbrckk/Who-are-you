# M775 — Local Balance Goals & Experiments Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add optional, user-defined local balance goals and seven-day experiments on top of M774 behavioral aggregates, without universal wellness thresholds or new collection permissions.

**Architecture:** Keep M775 separate from M774 collection and insight engines. A pure deterministic goal engine consumes `BehaviorGoal` plus existing `DailyBehaviorAggregate` history; a dedicated DataStore persists only goal configuration/state; Compose presentation recomputes progress from current local history. A full local My Habits reset clears both M774 history and M775 goals through an explicit integration bridge.

**Tech Stack:** Kotlin, Jetpack Compose, Android DataStore Preferences, coroutines/Flow, JUnit, Compose UI tests, repository Python contracts.

**Spec:** `docs/superpowers/specs/2026-09-25-m775-local-balance-goals-design.md`

## Global Constraints

- No new Android permissions for M775.
- No calls, SMS/messages, contacts, precise/coarse location, microphone/camera content, cloud behavioral sync, or behavioral advertising.
- No M775 data in `AppEvents`, `GlobalProfileShare`, `AdManager`, or `PersonalModelEngine`.
- Targets are always explicitly user-selected; no built-in “healthy” target.
- Missing days and missing source measurements remain unknown, never zero or failure.
- The current local day is excluded from experiment evaluation because it is partial.
- Default experiment length is 7 completed local days.
- No streak pressure, punitive copy, social comparison, notifications, or performance-linked monetization.
- EN/FR parity and compact + >=130% font-scale support are required.
- Do not merge without explicit authorization.
- Existing implementation on `m775-local-balance-goals` is candidate code only until each task below is verified against this plan.

## Review Focus

- A selected app absent from M774's retained top-app list must evaluate as unknown, not zero.
- Pausing and resuming a goal must not shift its configured experiment window or erase evidence.
- A full local My Habits reset must clear M775 goals together with M774 history, while per-source clearing must not remove goal definitions.
- Duplicate/out-of-order daily aggregates must produce stable goal progress.
- Completed/paused goals with sparse observations must report observed-day counts accurately without implying missed targets for unknown days.

---

### Task 1: Pure goal domain and deterministic progress engine

**Files:**
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalEngine.kt`
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalEngineTest.kt`
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalFactoryTest.kt`

**Interfaces:**
- Consumes: `DailyBehaviorAggregate`, `DaypartUsage`, `AppUsageAggregate` from M774.
- Produces: `BehaviorGoalMetric`, `BehaviorGoal`, `BehaviorGoalDayResult`, goal validation/factory APIs, and deterministic `BehaviorGoalEngine.evaluate(...)`.

- [ ] **Step 1: Verify failing/characterization tests** cover all four metrics, target direction, current-day exclusion, sparse history, selected-app absence, deterministic duplicate/input ordering, window clipping, pause semantics, and invalid definitions.
- [ ] **Step 2: Run focused JVM tests**: `./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.BehaviorGoalEngineTest --tests com.whoareyou.app.BehaviorGoalFactoryTest`.
- [ ] **Step 3: Adjust the engine only where tests/spec disagree**, preserving the exact rules: steps >= target; other three metrics <= target; unknown remains null; current local day excluded.
- [ ] **Step 4: Re-run focused tests and full JVM suite**: `./gradlew :app:testDebugUnitTest`.
- [ ] **Step 5: Commit** as `fix: align M775 goal engine with approved semantics` if changes are required; otherwise record the passing characterization in the PR.

### Task 2: Dedicated local goal persistence

**Files:**
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalStore.kt`
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalRepository.kt`
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalStoreCodecTest.kt`
- Contract: `tools/test_behavior_goal_storage_contract.py`

**Interfaces:**
- Consumes: validated `BehaviorGoal`.
- Produces: bounded local goal serialization plus observe/create/pause/resume/remove/reset operations. No duplicate behavioral history is stored.

- [ ] **Step 1: Verify tests** for deterministic round-trip, corruption fallback, bounded goal count, create, pause, resume, remove, and stable ordering.
- [ ] **Step 2: Run focused persistence tests** and the storage contract.
- [ ] **Step 3: Ensure the repository uses a distinct local DataStore namespace** and persists configuration/state only.
- [ ] **Step 4: Add/verify a local reset operation** that clears all goal definitions without touching unrelated profile data.
- [ ] **Step 5: Run focused + full JVM/static tests and commit** any required correction.

### Task 3: Goal presentation model and localized copy

**Files:**
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalPresentation.kt`
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalUiModel.kt`
- Verify/modify: `app/src/main/res/values/strings.xml`
- Verify/modify: `app/src/main/res/values-fr/strings.xml`
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalPresentationTest.kt`
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalUiModelTest.kt`
- Contract: `tools/test_behavior_goal_copy_contract.py`

**Interfaces:**
- Consumes: goal definition + deterministic progress.
- Produces: empty/active/paused/completed/partial-data UI states and EN/FR copy keys.

- [ ] **Step 1: Verify tests** for empty, active, paused, completed, sparse-data, zero-observed-day and selected-app states.
- [ ] **Step 2: Verify copy contract** rejects clinical, addiction, personality, moralizing, streak-pressure and universal-threshold language.
- [ ] **Step 3: Ensure presentation explicitly separates measured values from the user's chosen target** and never labels unknown days as misses.
- [ ] **Step 4: Run focused JVM tests plus localization/resource contracts**.
- [ ] **Step 5: Commit** any presentation/copy corrections.

### Task 4: Goals UI inside My Habits

**Files:**
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalsUi.kt`
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorScreenUi.kt`
- Test: `app/src/androidTest/java/com/whoareyou/app/BehaviorGoalsUiTest.kt`
- Test: `app/src/androidTest/java/com/whoareyou/app/BehaviorScreenUiTest.kt`

**Interfaces:**
- Consumes: `BehaviorGoalUiModel` list and typed callbacks for create/pause/resume/remove.
- Produces: Goals/Objectifs section, local create form, progress cards and destructive removal confirmation.

- [ ] **Step 1: Verify Compose tests** for empty state, user-defined framing, four create types, partial progress, pause/resume/remove callbacks and destructive confirmation.
- [ ] **Step 2: Add/verify compact-width and `fontScale = 1.3f` coverage with all controls reachable by scrolling.**
- [ ] **Step 3: Ensure v1 has no edit-goal path**: users create, pause/resume, remove, or create a replacement.
- [ ] **Step 4: Compile/run instrumentation tests through the repository's Android gate.**
- [ ] **Step 5: Commit** any UI corrections.

### Task 5: Integration, lifecycle and full local reset bridge

**Files:**
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalIntegrationUi.kt`
- Verify/modify: `app/src/main/java/com/whoareyou/app/MainActivity.kt`
- Verify/modify: `app/src/main/java/com/whoareyou/app/BehaviorScreenUi.kt`
- Test: relevant JVM/Compose integration tests; add `tools/test_behavior_goal_integration_contract.py` if static contracts are clearer.

**Interfaces:**
- Consumes: M774 `BehaviorSnapshot`, M775 goal Flow/repository and UI callbacks.
- Produces: recomputed goal progress whenever M774 history changes; full local My Habits reset bridge clears M774 history + M775 goal definitions.

- [ ] **Step 1: Write/verify a test** proving M774 history updates recompute progress without duplicating history into M775 storage.
- [ ] **Step 2: Write/verify a test** proving per-source M774 clearing preserves goal definitions and yields unknown progress where measurements disappear.
- [ ] **Step 3: Write a failing integration contract** proving the full My Habits reset invokes both `BehaviorRepository.clearAll(...)` and the M775 goal reset.
- [ ] **Step 4: Implement the explicit reset bridge** and keep questionnaire/profile reset behavior unchanged.
- [ ] **Step 5: Run focused tests, full JVM suite and instrumentation compile/tests; commit.**

### Task 6: Privacy and permission boundary

**Files:**
- Verify/modify: `tools/test_behavior_goal_privacy_contract.py`
- Verify/modify: `tools/test_behavior_goal_storage_contract.py`
- Verify: `app/src/main/AndroidManifest.xml`
- Verify: `app/src/main/java/com/whoareyou/app/AppEvents.kt`
- Verify: `app/src/main/java/com/whoareyou/app/GlobalProfileShare.kt`
- Verify: `app/src/main/java/com/whoareyou/app/AdManager.kt`
- Verify: `app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt`

**Interfaces:**
- Consumes: final M775 implementation surface.
- Produces: executable privacy/permission contracts.

- [ ] **Step 1: Verify contracts** fail if goal types/repository/payloads enter telemetry, sharing, ads or PersonalModel.
- [ ] **Step 2: Verify the manifest diff from M774 adds zero permissions.**
- [ ] **Step 3: Verify no M775 source contains network/cloud sync code or behavioral advertising hooks.**
- [ ] **Step 4: Run all Python/static contracts.**
- [ ] **Step 5: Commit** any missing boundary tests/corrections.

### Task 7: Exact-HEAD hardening and draft PR

**Files:**
- Update only if needed: implementation/tests from Tasks 1-6.
- Update PR metadata; do not change validated source afterward unless re-running both gates.

**Interfaces:**
- Consumes: complete M775 branch.
- Produces: one exact commit SHA with both Android CI and M59 successful.

- [ ] **Step 1: Run complete JVM/static/build gates**: repository Python contracts, `:app:testDebugUnitTest`, Compose instrumentation compile, benchmark variants, lint and debug APK.
- [ ] **Step 2: Run M59 Device Validation** with connected instrumentation/runtime stress and visual validation.
- [ ] **Step 3: Fix regressions smallest-test-first**, then re-run the affected full gate.
- [ ] **Step 4: Verify Android CI and M59 are both SUCCESS on the same exact HEAD SHA.**
- [ ] **Step 5: Open/update a draft PR** from `m775-local-balance-goals` onto `m774-local-behavioral-insights`, documenting privacy exclusions, exact-HEAD validation and remaining exclusions.
- [ ] **Step 6: Leave the PR unmerged** pending explicit authorization.
