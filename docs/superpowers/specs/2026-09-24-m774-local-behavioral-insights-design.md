# M774 — Local Behavioral Insights — Design

Date: 2026-09-24
Base: validated M773 branch `m773-who-am-i` at `65488ecd9ff7a5bb47ddd4ce00709d42f9ef3dad`

## Objective

Add an opt-in, local-only Android section that helps a person learn from measurable day-to-day behavior without confusing device observations with personality conclusions.

M774 v1 covers physical activity and digital usage: daily steps/activity, total app usage, per-app usage, usage frequency and time-of-day patterns. Call logs, message metadata, contacts, content, location and cloud synchronization are explicitly excluded from v1.

## Product principles

1. **Local by default and by design.** Behavioral data and derived aggregates stay on the device in M774 v1.
2. **Explicit opt-in.** No behavioral source is collected before the user deliberately enables it and grants the required Android access.
3. **Observation before inference.** The app first states measurable facts, then may offer clearly separated, cautious interpretations when enough history exists.
4. **No moral scoring.** More screen time, fewer steps or late usage are not presented as character flaws.
5. **No diagnosis or addiction claim.** The system may identify sustained or unusual patterns and suggest balance-oriented actions, but does not diagnose dependency, depression, sleep disorders or other conditions.
6. **Missing permission means missing data, not failure.** The experience remains usable with any subset of sources enabled.
7. **Minimal collection.** Store only data needed for the user-facing insight; avoid raw event retention when a daily aggregate is sufficient.
8. **User control.** Each source can be disabled independently and all M774 data can be deleted locally.

## Scope

### Included in M774 v1

- daily step count / physical-activity aggregate when supported and authorized;
- daily total foreground app-use duration;
- per-app foreground-use duration for the most-used apps;
- app-use frequency/session-like counts where Android data quality permits a defensible aggregate;
- hourly/daypart usage distribution;
- 7-day and 30-day baselines;
- deterministic observations such as sustained increase/decrease, concentration in a small set of apps, late-day usage concentration and activity consistency;
- neutral balance suggestions based on sustained patterns;
- a dedicated `My habits / Mes habitudes` section;
- a small, explicitly labeled behavioral summary that can later be surfaced in `Who am I?` without being merged into M772 personality confidence.

### Excluded from M774 v1

- call logs and call duration;
- SMS/message metadata or content;
- contacts;
- notification content;
- browser history;
- precise/coarse location;
- microphone/camera collection;
- cloud sync, account sync or remote behavioral analytics;
- advertising/personalization based on behavioral data;
- clinical inference;
- direct modification of `PersonalModel` certainty/confidence from device behavior.

## Architecture

M774 is a separate behavioral-observation subsystem. It must not be implemented inside `PersonalModelEngine`.

```text
Android authorized sources
        ↓
Behavior collectors
        ↓
Normalization / daily aggregation
        ↓
Local BehavioralRepository
        ↓
BehaviorInsightEngine (pure/deterministic)
        ↓
BehaviorSnapshot + BehaviorInsight[]
        ↓
My Habits UI
        ↓
optional read-only summary for Who Am I UI
```

### Boundary with M772/M773

`PersonalModel` describes questionnaire/profile evidence. `BehaviorSnapshot` describes measured device behavior. They remain separate domain models.

The `Who am I?` UI may later show a section explicitly titled along the lines of “What your habits show”, but M774 observations do not silently raise/lower trait confidence and are not converted into personality labels.

## Domain model

Introduce focused immutable models, for example:

```kotlin
data class DailyBehaviorAggregate(
    val epochDay: Long,
    val steps: Long?,
    val totalForegroundMillis: Long?,
    val topApps: List<AppUsageAggregate>,
    val launchesOrSessions: Int?,
    val daypartUsage: DaypartUsage
)

data class AppUsageAggregate(
    val packageName: String,
    val foregroundMillis: Long,
    val launchesOrSessions: Int?
)

data class DaypartUsage(
    val morningMillis: Long,
    val afternoonMillis: Long,
    val eveningMillis: Long,
    val nightMillis: Long
)

data class BehaviorSnapshot(
    val today: DailyBehaviorAggregate?,
    val last7Days: List<DailyBehaviorAggregate>,
    val last30Days: List<DailyBehaviorAggregate>,
    val sourceStates: Map<BehaviorSource, BehaviorSourceState>,
    val insights: List<BehaviorInsight>
)

enum class BehaviorSource { ACTIVITY, APP_USAGE }
enum class BehaviorSourceState { DISABLED, PERMISSION_REQUIRED, AVAILABLE, UNSUPPORTED, ERROR }
```

Persist daily aggregates, not an unlimited raw event stream. Per-app history should be bounded to what is required for the 30-day experience.

## Android data sources

### App usage

Use Android `UsageStatsManager`/usage access. This is special access controlled by Android settings rather than pretending it is a normal runtime permission.

Requirements:
- detect whether usage access is actually granted;
- explain why it is requested before opening system settings;
- return a typed `PERMISSION_REQUIRED` state when absent;
- aggregate foreground usage into local day/daypart buckets;
- handle OEM/API differences and empty usage results without inventing zero usage;
- package labels/icons are presentation metadata only; package name remains the stable internal key.

### Steps / physical activity

Use the least invasive Android-supported source available to the project/API targets. The collector is behind an interface so the source can evolve without changing the insight engine.

Requirements:
- explicit authorization where Android requires it;
- distinguish unsupported hardware/API from denied permission and from genuinely zero steps;
- store daily aggregates only;
- no location collection is required for steps;
- no background behavior should be added unless it is necessary for reliable daily aggregation and complies with current Android/Play requirements.

If a reliable historical step source is unavailable on a device, M774 degrades gracefully to app-usage insights rather than fabricating history.

## Repository and privacy

Introduce `BehaviorRepository` with an implementation consistent with the app's existing local persistence patterns.

Responsibilities:
- read/write bounded daily aggregates;
- source enable/disable preferences;
- last successful refresh timestamps;
- delete all behavioral data;
- expose snapshots to UI/domain code without exposing Android framework collectors.

Privacy rules:
- no M774 behavioral payload in telemetry;
- no raw usage-event upload;
- no app-usage history in share-profile payloads;
- no behavioral data in advertising decisions;
- reset is explicit and independently available from broader profile reset where practical.

## Insight engine

`BehaviorInsightEngine` is pure Kotlin and deterministic. It receives normalized aggregates and emits observations with evidence windows.

Example categories:
- `ACTIVITY_CONSISTENCY`
- `ACTIVITY_CHANGE`
- `SCREEN_TIME_CHANGE`
- `APP_CONCENTRATION`
- `LATE_USAGE_PATTERN`
- `USAGE_REGULARITY`
- `INSUFFICIENT_HISTORY`

Each insight includes:
- category;
- observation period;
- supporting aggregate values;
- confidence/evidence tier based on history completeness, not psychological certainty;
- optional neutral action suggestion.

### Minimum evidence

- same-day values can be displayed as facts but not as trends;
- trend language requires multiple comparable days;
- stronger “usual pattern” wording requires at least a meaningful multi-day baseline;
- missing days reduce evidence rather than being interpreted as zeros;
- comparisons use robust aggregates (for example median/bounded averages where appropriate) so one extreme day does not dominate the profile.

Exact thresholds belong in named constants and are pinned by tests.

## Advice and excess prevention

Advice is framed around balance and user-defined awareness, not moral judgment.

Allowed examples:
- “Your evening app use has been higher than your recent baseline on several days.”
- “Your activity has been more consistent this week.”
- “Most of your app time is concentrated in two apps.”
- “If you want to reduce evening use, a small app timer or a screen-free period may help.”

Not allowed:
- “You are addicted.”
- “You are lazy.”
- “This proves you are introverted/depressed/anxious.”
- causal claims unsupported by the data.

The app should prefer user-controlled thresholds/goals later rather than universal claims that a specific amount of usage is inherently excessive.

## UI — My Habits / Mes habitudes

Add a dedicated destination/section reachable from the app's existing navigation without replacing `Who am I?`.

Information hierarchy:

1. **Data control header** — local-only explanation and enabled source status.
2. **Today** — steps/activity and total app usage when available.
3. **7 days** — compact trend cards and most-used apps.
4. **30 days** — baseline/regularity and meaningful longer-term changes.
5. **Patterns noticed** — deterministic insights with evidence wording.
6. **Balance suggestions** — optional, concrete, non-clinical actions.
7. **Data controls** — source permissions, disable collection, delete behavioral data.

Empty states are actionable:
- source disabled → explain and enable;
- permission required → explain and open appropriate Android setting/request;
- unsupported → say so without repeated prompts;
- insufficient history → show today's facts and state how history will improve insights.

## Background refresh

Prefer opportunistic refresh when the app is opened/resumed plus a conservative scheduled refresh only if required for reliable daily aggregation.

Do not introduce aggressive polling, persistent foreground services or battery-heavy collection for v1.

Any WorkManager job must be idempotent, respect disabled sources, tolerate missed runs and never turn missing data into zero-valued observations.

## Localization and accessibility

- complete EN/FR resource parity;
- no raw package names when a safe installed-app label is available, with package name used only as a fallback when necessary;
- headings expose semantic heading roles;
- cards remain readable at >=130% font scale and compact width;
- charts, if used, have textual summaries and do not encode meaning by color alone;
- permission explanations are readable before launching Android settings.

## Error handling

Collectors return typed states/results rather than throwing through UI layers.

Expected conditions include:
- access revoked after previously being enabled;
- OEM returns empty/incomplete usage stats;
- activity sensor/source unavailable;
- partial 7/30-day history;
- clock/day boundary changes;
- app uninstall/rename for historical package IDs;
- local database/preferences corruption handled with safe fallback where existing persistence supports it.

No error condition should produce a fabricated behavioral conclusion.

## Testing

### Pure JVM tests

Required:
1. no history => facts/insufficient-history only;
2. missing days are not zeros;
3. stable multi-day pattern is deterministic;
4. sustained increase/decrease requires enough comparable evidence;
5. one outlier does not dominate baseline;
6. late usage pattern requires repeated evidence;
7. app concentration calculation is deterministic;
8. disabled/unavailable sources do not generate conclusions;
9. input ordering does not alter semantic output;
10. no insight emits clinical/personality labels.

### Collector/repository tests

- usage access denied/granted states;
- empty UsageStats result != zero usage;
- day boundary aggregation;
- bounded retention;
- source disable stops refresh and can remove its stored data;
- delete-all removes M774 data;
- no M774 data enters existing share-profile serialization.

### Compose/UI tests

- source-state empty screens;
- Today/7-day/30-day sections;
- permission CTA wiring;
- local-only privacy copy;
- patterns and advice remain visually distinct from measured facts;
- large font/compact width;
- EN/FR resource coverage;
- accessibility semantics.

### Regression gates

Retain existing repository gates, Android CI and exact-HEAD M59 device validation. Do not weaken existing M772/M773 tests.

## Rollout order

1. pure domain models + deterministic insight engine;
2. local repository and bounded retention;
3. app-usage collector and authorization flow;
4. physical-activity collector and authorization flow;
5. My Habits UI;
6. refresh orchestration;
7. optional read-only behavioral summary in Who Am I;
8. privacy/accessibility/regression hardening;
9. exact-HEAD CI/device validation and draft PR.

This order gives useful app-usage functionality even on devices where step history is unavailable.

## Definition of done

M774 v1 is complete when:
- activity and app-usage sources are independently opt-in;
- data stays local and has bounded retention;
- My Habits provides Today/7-day/30-day information from real authorized sources;
- insights distinguish measured facts from interpretations;
- sustained-pattern thresholds are deterministic and tested;
- no call/message/location collection exists;
- no behavioral data silently modifies `PersonalModel` confidence;
- source revocation/unsupported states degrade gracefully;
- deletion controls work;
- EN/FR and accessibility coverage are present;
- full JVM/instrumentation/lint/build gates pass;
- Android CI and M59 device validation pass at the exact reviewed HEAD;
- work remains draft/open until explicit merge authorization.