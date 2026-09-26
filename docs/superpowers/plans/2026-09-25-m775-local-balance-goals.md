# M775 — Local Balance Goals & Experiments Completion Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish and harden the already-implemented M775 local goals feature so it matches the approved v1 scope and passes Android CI plus M59 on one exact HEAD.

**Architecture:** Keep goals in a dedicated local DataStore and recompute progress from M774 `DailyBehaviorAggregate` history. The pure `BehaviorGoalEngine` remains independent from Android/UI code; Compose consumes presentation models through `BehaviorGoalsHostState`. M775 introduces no new collector, permission, telemetry, profile evidence, cloud sync, or background polling.

**Tech Stack:** Kotlin, Jetpack Compose, Preferences DataStore, JUnit4, Compose UI tests, Python source-contract tests, existing Android CI and M59 device workflows.

**Spec:** `docs/superpowers/specs/2026-09-25-m775-local-balance-goals-design.md`

## Global Constraints

- Targets are always explicitly chosen by the user; no universal healthy threshold.
- Default experiment duration is 7 completed local days.
- Missing behavioral data remains missing and is never converted to zero or target failure.
- Current local day is excluded from experiment evidence.
- Supported metrics are `STEPS_AT_LEAST`, `SCREEN_TIME_AT_MOST`, `EVENING_USAGE_AT_MOST`, and `APP_USAGE_AT_MOST`.
- Selected-app absence from M774's retained top-app list means unknown, not zero.
- v1 lifecycle is create, pause/resume, remove, completed. Editing an existing goal is out of scope.
- Full local My Habits reset clears M774 history and M775 goal definitions; clearing one source does not remove goals.
- No new Android permission, cloud sync, behavioral telemetry, advertising use, share payload, or PersonalModel confidence/certainty input.
- EN/FR parity, compact width, and >=130% font scale remain required.
- Completion requires Android CI and M59 Device Validation SUCCESS on the same commit SHA.
- Keep the M775 PR stacked on M774 and do not merge without explicit authorization.

## Review Focus

1. **M59 text semantics:** progress text must be asserted in a way compatible with the repository's Compose semantics API; current instrumentation failed even though the rendered text was correct.
2. **Full reset consistency:** one confirmed My Habits reset must clear both stores without leaving stale goal cards or stale progress.
3. **Selected-app unknown state:** a selected package absent from retained `topApps` must never be interpreted as zero use.
4. **Pause/completion precedence:** paused goals preserve evidence; completed-window behavior must remain deterministic across pause/resume.
5. **Scope creep:** editing controls, automatic target recommendations, streak/reward mechanics, notifications, or new permissions must not survive v1 hardening.

---

### Task 1: Characterize the existing M775 domain and persistence baseline

**Files:**
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalEngineTest.kt`
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalStoreCodecTest.kt`
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalPresentationTest.kt`
- Test: `app/src/test/java/com/whoareyou/app/BehaviorGoalUiModelTest.kt`
- Existing: `app/src/main/java/com/whoareyou/app/BehaviorGoalEngine.kt`
- Existing: `app/src/main/java/com/whoareyou/app/BehaviorGoalStore.kt`
- Existing: `app/src/main/java/com/whoareyou/app/BehaviorGoalRepository.kt`

**Interfaces:**
- Consumes: M774 `DailyBehaviorAggregate`, `BehaviorSnapshot`.
- Produces: stable `BehaviorGoal`, `BehaviorGoalProgress`, codec, repository and presentation contracts used by later tasks.

- [ ] **Step 1: Add/confirm failing-edge tests** for zero-valued measured data, selected-app absence, duplicate-day determinism, current-day exclusion, completed-window clipping, and pause state.
- [ ] **Step 2: Run focused JVM suite** with `./gradlew :app:testDebugUnitTest --tests 'com.whoareyou.app.BehaviorGoal*Test'`.
- [ ] **Step 3: Fix only genuine domain/persistence regressions** while preserving the approved semantics and 20-goal local retention bound.
- [ ] **Step 4: Re-run focused JVM tests** and require PASS.
- [ ] **Step 5: Commit** only if source/test changes were necessary.

### Task 2: Remove goal editing from the approved v1 surface

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalsUi.kt`
- Modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalIntegrationUi.kt`
- Modify: `app/src/main/java/com/whoareyou/app/BehaviorScreenUi.kt`
- Modify: `app/src/main/java/com/whoareyou/app/MainActivity.kt`
- Modify: `app/src/test/java/com/whoareyou/app/BehaviorGoalFactoryTest.kt`
- Modify: `app/src/androidTest/java/com/whoareyou/app/BehaviorGoalsUiTest.kt`
- Modify: EN/FR goal strings if edit-only resources exist.

**Interfaces:**
- Consumes: existing create, pause/resume, remove callbacks.
- Produces: `BehaviorGoalsSection(goals, availableApps, onCreate, onSetPaused, onDelete)` with no edit callback; `BehaviorGoalsHostState` with the same v1 lifecycle only.

- [ ] **Step 1: Change tests first** so they reject edit controls/callbacks and retain create/pause/resume/remove coverage.
- [ ] **Step 2: Run focused JVM/instrumentation compile** and verify RED against the existing edit implementation.
- [ ] **Step 3: Remove `BehaviorGoalFactory.update`, `onEdit`, edit dialog routing, edit tags and edit-only copy** without changing goal identity/create semantics.
- [ ] **Step 4: Run focused JVM tests and `./gradlew :app:compileDebugAndroidTestKotlin`** and require PASS.
- [ ] **Step 5: Commit** as `refactor: keep M775 goal lifecycle within v1 scope`.

### Task 3: Bridge full My Habits reset across M774 and M775 stores

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/BehaviorGoalRepository.kt`
- Create or modify: focused local reset coordinator/helper under `app/src/main/java/com/whoareyou/app/`
- Modify: `app/src/main/java/com/whoareyou/app/MainActivity.kt`
- Test: JVM/source contract for reset routing.
- Modify: `tools/test_behavior_goal_storage_contract.py`

**Interfaces:**
- Consumes: `BehaviorRepository.clearAll(context)`, `BehaviorGoalRepository.clearAll(context)`.
- Produces: one explicit suspend local reset entry point used by the existing confirmed My Habits delete action.

- [ ] **Step 1: Write a failing contract/test** proving the full local reset invokes both repositories while per-source clear leaves goals untouched.
- [ ] **Step 2: Run the smallest affected test/contract** and verify RED.
- [ ] **Step 3: Implement the reset bridge** and route `BehaviorScreen.onDeleteAll` through it; do not revoke Android permissions.
- [ ] **Step 4: Run focused contracts/JVM tests** and require PASS.
- [ ] **Step 5: Commit** as `feat: reset local habits goals with behavior history`.

### Task 4: Fix the current M59 Compose regression and harden goal UI semantics

**Files:**
- Modify: `app/src/androidTest/java/com/whoareyou/app/BehaviorGoalsUiTest.kt`
- Modify only if needed: `app/src/main/java/com/whoareyou/app/BehaviorGoalsUi.kt`

**Interfaces:**
- Consumes: goal-card stable test tags and localized progress copy.
- Produces: device-stable tests for progress, pause/resume, create, remove confirmation and compact/large-font layout.

- [ ] **Step 1: Reproduce the known failure** `activeGoalShowsMeasuredProgressAndRoutesPause`; current M59 #1126 rendered `3 observed days · 2 days matching your target` but the chained `assertTextContains` failed.
- [ ] **Step 2: Replace brittle text assertions with a repository-compatible semantics assertion** that proves both observed=3 and met=2 without relying on unsupported/chained behavior.
- [ ] **Step 3: Ensure pause/resume and remove confirmation tests still exercise callbacks**, and retain 360dp + fontScale 1.3 coverage.
- [ ] **Step 4: Run `./gradlew :app:compileDebugAndroidTestKotlin`** and the smallest connected test available; require PASS.
- [ ] **Step 5: Commit** as `fix: stabilize M775 goal instrumentation semantics`.

### Task 5: Re-verify privacy, copy and permission boundaries

**Files:**
- Modify as needed: `tools/test_behavior_goal_privacy_contract.py`
- Modify as needed: `tools/test_behavior_goal_storage_contract.py`
- Modify as needed: `tools/test_behavior_goal_copy_contract.py`
- Inspect: `app/src/main/AndroidManifest.xml`
- Inspect: `AppEvents.kt`, `GlobalProfileShare.kt`, `AdManager.kt`, `PersonalModelEngine.kt`

**Interfaces:**
- Consumes: final M775 file set after Tasks 2–4.
- Produces: source-level regression barriers for privacy, local-only persistence and non-clinical copy.

- [ ] **Step 1: Update file/token lists** for any new reset helper and ensure no goal file references external-service/profile APIs.
- [ ] **Step 2: Add a contract assertion** that manifest permissions remain exactly INTERNET, PACKAGE_USAGE_STATS and health.READ_STEPS.
- [ ] **Step 3: Run all `tools/test_behavior_goal_*.py` tests** and require PASS.
- [ ] **Step 4: Run the repository-wide Python quality suite** and require PASS.
- [ ] **Step 5: Commit** only if contracts changed.

### Task 6: Exact-HEAD validation and stacked draft PR

**Files:**
- No product-code changes after the final validation SHA.
- Update PR metadata only after validation.

**Interfaces:**
- Consumes: final branch HEAD.
- Produces: a reviewable M775 draft PR stacked on `m774-local-behavioral-insights`.

- [ ] **Step 1: Run/observe Android CI**: localized catalogs, Python contracts, JVM tests, Compose instrumentation compile, benchmark variants, lint, debug APK/checksum must all succeed.
- [ ] **Step 2: Run/observe M59 Device Validation**: visual validation and connected instrumentation/runtime stress must both succeed.
- [ ] **Step 3: If either gate fails, fix smallest-test-first** and repeat until both workflows are SUCCESS on the same SHA.
- [ ] **Step 4: Verify branch diff** contains no new permission, telemetry/share/ad/profile coupling, edit flow, notification/streak/reward mechanic, or unrelated refactor.
- [ ] **Step 5: Open or update a draft M775 PR** with base `m774-local-behavioral-insights`, documenting exact validated SHA and privacy boundaries; do not merge without explicit authorization.
