# M775 — Local Balance Goals & Experiments Implementation Plan

**Date:** 2026-09-25  
**Base branch:** `m774-local-behavioral-insights`  
**Working branch:** `m775-local-balance-goals`

## Task 1 — Pure goal domain and engine
- [ ] Write JVM tests for all four goal metrics, missing-data semantics, partial-day exclusion, deterministic ordering/window clipping, paused goals, and invalid definitions.
- [ ] Implement immutable goal domain types and `BehaviorGoalEngine`.
- [ ] Run focused JVM tests, then full JVM suite.
- [ ] Commit.

## Task 2 — Local goal persistence
- [ ] Write codec/store tests for deterministic round-trip, corruption fallback, bounded goal count, create/update/pause/delete.
- [ ] Add local goal persistence separate from ProfileStore and behavioral history.
- [ ] Run focused + full JVM tests.
- [ ] Commit.

## Task 3 — Presentation model and EN/FR copy
- [ ] Add tests for empty, active, paused, completed and partial-data states.
- [ ] Add non-clinical/user-defined copy guards.
- [ ] Implement localized presentation model.
- [ ] Run localization/resource gates.
- [ ] Commit.

## Task 4 — Goals UI inside My Habits
- [ ] Add Compose tests for empty state, cards, controls, create flow, compact width and >=130% font scale.
- [ ] Implement goal section and local create/edit form using existing V2 design system.
- [ ] Ensure destructive delete confirmation.
- [ ] Run instrumentation compile/tests.
- [ ] Commit.

## Task 5 — Integration and lifecycle
- [ ] Wire goal persistence + recomputed progress to My Habits.
- [ ] Ensure M774 source refresh automatically recomputes displayed progress without duplicate behavioral storage.
- [ ] Ensure deleting behavioral history produces missing progress, not failures.
- [ ] Commit.

## Task 6 — Privacy and permission boundary
- [ ] Add tests proving no M775 data enters telemetry, profile share, ads or PersonalModel.
- [ ] Add manifest contract proving M775 adds no permission.
- [ ] Run full affected gates.
- [ ] Commit.

## Task 7 — Exact-HEAD hardening
- [ ] Run complete JVM suite.
- [ ] Run Python/static contracts, instrumentation compile, benchmark variants, lint and debug APK.
- [ ] Run M59 visual + connected instrumentation.
- [ ] Fix regressions smallest-test-first.
- [ ] Verify Android CI + M59 SUCCESS on same SHA.
- [ ] Open/update draft PR stacked on M774; do not merge without explicit authorization.
