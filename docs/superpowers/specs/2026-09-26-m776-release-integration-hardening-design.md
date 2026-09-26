# M776 — Release Integration & Migration Hardening Design

## Goal

Prepare the validated M774 → M775 stack for real release conditions without expanding product scope or permissions.

M776 is a hardening milestone, not a feature milestone. It validates that local profile, habits, and goals remain safe and coherent across fresh install, upgrade, permission changes, process recreation, locale changes, and destructive local reset.

## Non-goals

M776 does not:
- add new behavioral signals;
- add new Android permissions;
- merge M774 or M775;
- add cloud sync, behavioral telemetry, or behavioral advertising;
- change PersonalModel semantics;
- add notifications, streaks, recommendations, or automatic goal targets.

## Compatibility principles

1. Existing local profile data must remain readable.
2. Existing M774 behavior history must remain readable.
3. Existing M775 goal payloads must remain readable.
4. Corrupt or unknown local payloads must fail closed to safe empty state, never fabricate measurements.
5. Missing data remains unknown, never zero.
6. Upgrade paths must not silently enable behavioral sources.
7. Permission loss or provider unavailability must degrade source state without deleting unrelated data.
8. A full local My Habits reset clears M774 history and M775 goals but leaves questionnaire/profile data untouched.
9. Profile reset leaves habits/goals untouched unless explicitly selected through their own control.
10. Locale changes must not alter stored identifiers or numeric values.

## Release scenarios

### Fresh install

Verify:
- app starts with no crash;
- onboarding and catalog load normally;
- Habits sources are disabled by default;
- Goals section has no synthetic progress;
- no behavioral permission prompt is shown without user action;
- EN and FR critical paths render.

### Upgrade from pre-M774 state

Simulate local state containing only profile/questionnaire data.

Verify:
- profile remains available;
- behavior repository starts empty/disabled;
- goal repository starts empty;
- no migration invents behavior data;
- no new permission is requested automatically.

### Upgrade from M774-only state

Simulate M774 history and source states without M775 goals.

Verify:
- behavior history remains readable;
- source states remain consistent;
- M775 goal store initializes empty;
- creating a goal evaluates against existing completed M774 history only.

### Upgrade from M775 state

Verify current goal codec round-trip and repository behavior across app restart/process recreation.

### Permission transitions

Cover:
- Health Connect unavailable;
- Health Connect permission denied;
- Health Connect permission revoked after previously available;
- Usage Access denied;
- Usage Access revoked;
- empty UsageStats event history;
- provider/data-source failure.

Each transition must keep unrelated source/history/goal state intact.

### Locale transitions

Switch EN ↔ FR after local data already exists.

Verify:
- stored IDs/package names remain unchanged;
- copy changes only at presentation layer;
- no duplicate goals/history created;
- destructive controls still describe their exact scope.

### Process/lifecycle transitions

Verify:
- process recreation does not duplicate local state;
- resume refresh remains serialized;
- opening Habits after recreation recomputes progress from current local stores;
- no background polling/service is introduced.

## Migration architecture

No schema migration framework is introduced unless current serialized formats require it.

Existing versioned codecs remain authoritative:
- M774 behavior store codec;
- M775 goal store codec.

M776 adds compatibility characterization tests around those formats and, only if needed, narrowly-scoped migration helpers.

## Testing strategy

### JVM/static
- golden/legacy payload decode tests;
- corrupt payload fallback;
- locale-independent serialization;
- reset-boundary contracts;
- permission default-state contracts;
- no-new-permission manifest contract;
- no external-services coupling.

### Instrumentation
- fresh install critical-path smoke;
- Habits/Goals empty state;
- compact/large-font regression;
- process recreation where deterministic and supported by test harness;
- EN/FR critical screen reachability;
- permission-return/revocation flows where Android test environment supports them.

### Exact-head release gate
One SHA must have:
- Python/static contracts green;
- JVM unit tests green;
- Compose instrumentation compile green;
- benchmark variants green;
- lint green;
- debug/release-compatible APK build green;
- M59 visual validation green;
- M59 connected instrumentation/runtime stress green.

## Definition of done

M776 is complete when:
- compatibility/migration tests exist for pre-M774, M774-only, and M775 states;
- permission/default-state regressions are covered;
- critical EN/FR flows are covered;
- no new permissions or behavioral scope are introduced;
- Android CI and M59 both report SUCCESS on the same exact HEAD;
- PR remains unmerged pending explicit authorization.
