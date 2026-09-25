# M775 — Local Balance Goals & Experiments Design

**Date:** 2026-09-25  
**Base:** M774 Local Behavioral Insights  
**Tracking:** #75

## Goal

Let users turn M774 observations into small, explicit, user-chosen experiments without turning device data into diagnosis, personality evidence, or universal wellness scoring.

M775 remains local-first and opt-in. It reuses M774 daily aggregates; it does not add Android collection permissions or new background collection.

## Principles

- **User chooses the target.** The app does not declare a universal healthy amount of screen time, app use, evening use, or steps.
- **Measured data stays separate from interpretation.** Progress says whether a user-selected target was met on observed days.
- **Missing is not failure.** Missing permission/data is never converted to zero and never counted as a missed target.
- **Short experiments over streak pressure.** Default experiments last seven completed local days. No shame, punishment, loss aversion, or social comparison.
- **Local-only.** Goal definitions and progress stay on device and never enter telemetry, ads, sharing, or PersonalModel confidence/certainty.

## Included in v1

Goal types:
1. daily steps at least a user-selected value;
2. daily total screen/app foreground time at most a user-selected duration;
3. evening usage at most a user-selected duration;
4. selected-app foreground time at most a user-selected duration.

Goal lifecycle:
- create;
- active;
- pause/resume;
- delete;
- completed after its configured experiment window.

Progress:
- evaluate completed local days only;
- count observed days independently from expected elapsed days;
- count target-met days only when the underlying metric exists;
- expose deterministic day-by-day evidence;
- never infer failure from an absent day.

## Excluded from v1

- call/SMS/contact/location data;
- automatic normative targets;
- clinical/addiction/personality conclusions;
- PersonalModel certainty/confidence changes;
- cloud sync;
- behavioral telemetry or advertising;
- social comparison;
- streak loss mechanics;
- notifications/reminders;
- rewards or monetization based on behavioral performance.

## Domain model

Pure Kotlin domain types live outside Android/UI layers.

```kotlin
enum class BehaviorGoalMetric {
    STEPS_AT_LEAST,
    SCREEN_TIME_AT_MOST,
    EVENING_USAGE_AT_MOST,
    APP_USAGE_AT_MOST
}

data class BehaviorGoal(
    val id: String,
    val metric: BehaviorGoalMetric,
    val targetValue: Long,
    val startEpochDay: Long,
    val durationDays: Int,
    val packageName: String?,
    val paused: Boolean
)

data class BehaviorGoalDayResult(
    val epochDay: Long,
    val measuredValue: Long?,
    val met: Boolean?
)
```

`BehaviorGoalEngine` is deterministic and receives only `BehaviorGoal`, existing `DailyBehaviorAggregate` values, and the current local epoch day.

## Evaluation semantics

- Experiments evaluate days in `startEpochDay .. startEpochDay + durationDays - 1`.
- The current local day is excluded because it is partial.
- Days after the configured experiment window are ignored.
- Duplicate aggregates for a day resolve deterministically.
- A day with no relevant measurement has `measuredValue = null` and `met = null`.
- `STEPS_AT_LEAST`: met when measured steps >= target.
- `SCREEN_TIME_AT_MOST`: met when total foreground millis <= target.
- `EVENING_USAGE_AT_MOST`: met when an app-usage measurement exists and evening millis <= target.
- `APP_USAGE_AT_MOST`: evaluated only when the selected package is present in the retained per-app aggregate for that day.

M774 intentionally stores only a bounded top-app list. Therefore an absent selected package is **unknown, not zero**, even when total app usage is known. This prevents a package outside the retained top list from being misclassified as unused.

## Persistence

Add a dedicated local goal store, separate from `ProfileStore`. Persist only goal configuration/state, not duplicate behavioral history. Progress is recomputed from M774 aggregates.

Requirements:
- bounded number of goals;
- deterministic codec/serialization;
- corrupt payload safely falls back to no goals;
- deleting M774 behavioral history leaves goal definitions intact but progress becomes missing;
- deleting a goal removes only that goal.

## UI

Add a `Goals / Objectifs` section to My Habits:
- empty state explains that targets are user-defined;
- create action opens a simple local form;
- active cards show target, observed days, target-met days, and remaining experiment days;
- paused/completed states are explicit;
- no red/green moral scoring;
- compact width and >=130% font scale supported;
- EN/FR parity.

## Privacy boundaries

M775 goal data must not enter:
- `AppEvents` payloads;
- `GlobalProfileShare`;
- `AdManager`;
- `PersonalModelEngine`;
- remote sync or network payloads.

No new manifest permission is allowed for M775.

## Testing

### Pure JVM
- each metric direction;
- current partial day excluded;
- missing day != zero/failure;
- missing source measurement != zero;
- selected app absent from the retained top-app aggregate => missing;
- selected app present => evaluate its retained foreground duration;
- duplicate/input ordering deterministic;
- experiment window clipping;
- paused goal preserves evidence but reports paused;
- invalid definitions rejected.

### Persistence
- deterministic round-trip;
- corrupt payload fallback;
- bounded goal count;
- create/update/pause/delete.

### UI/integration
- goals section empty state;
- user-defined framing;
- create/edit/pause/delete routing;
- progress with partial data;
- compact + 130% font scale;
- no new permissions;
- privacy boundary contracts.

## Completion gate

M775 is complete only when Android CI and M59 Device Validation are both SUCCESS on the same exact HEAD. Keep its PR stacked on M774 and do not merge without explicit authorization.
