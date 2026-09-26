# M776 — Release Integration & Migration Hardening Implementation Plan

**Goal:** Prove that the M774/M775 stack behaves safely across fresh install, upgrades, locale changes, permission transitions and process/lifecycle boundaries.

**Base:** M775 validated HEAD `227eefcf9ef66ed3ebc15332ca4d18bbcc0eaabd`

**Spec:** `docs/superpowers/specs/2026-09-26-m776-release-integration-hardening-design.md`

## Constraints
- No new product permissions or behavioral signals.
- No merge of M774/M775 without explicit authorization.
- Missing/corrupt data fails to empty/unknown state, never synthetic zero.
- Existing profile, behavior and goal stores remain separately scoped.
- All changes must preserve EN/FR parity.

### Task 1 — Compatibility characterization
- [ ] Add legacy/pre-feature payload tests for ProfileStore/behavior/goals.
- [ ] Verify versioned codecs decode known valid payloads deterministically.
- [ ] Verify corrupt/unknown payloads fail safely.
- [ ] Verify serialization is locale-independent.

### Task 2 — Fresh-install/default-state contracts
- [ ] Verify behavior sources default disabled.
- [ ] Verify goals empty.
- [ ] Verify no behavioral permission launcher is triggered automatically.
- [ ] Add static/instrumentation coverage for fresh Habits screen.

### Task 3 — Upgrade boundaries
- [ ] Characterize pre-M774 profile-only state.
- [ ] Characterize M774-only behavior state with empty M775 goal store.
- [ ] Characterize M775 goal state after process/repository restart.
- [ ] Verify no duplicate or synthetic data appears.

### Task 4 — Permission/revocation hardening
- [ ] Verify Health Connect unsupported/denied/revoked paths.
- [ ] Verify Usage Access denied/revoked/empty-history paths.
- [ ] Verify unrelated source/history/goal data is preserved.

### Task 5 — Reset and locale boundaries
- [ ] Verify Profile reset leaves Habits/Goals untouched.
- [ ] Verify full Habits reset clears behavior + goals only.
- [ ] Verify EN↔FR changes presentation only, not stored identifiers/values.

### Task 6 — Process/lifecycle smoke
- [ ] Verify resume refresh serialization remains intact.
- [ ] Verify process recreation does not duplicate goals/history.
- [ ] Verify Habits progress recomputes from repositories after recreation.

### Task 7 — Exact-head release gate
- [ ] Run Python/static contracts.
- [ ] Run full JVM suite.
- [ ] Compile/run Compose instrumentation.
- [ ] Run benchmark variants + lint + APK build.
- [ ] Run M59 visual + connected instrumentation/runtime stress.
- [ ] Require Android CI and M59 SUCCESS on the same SHA.
- [ ] Update draft PR and leave unmerged.
