# M772 — Personal Model & Confidence Engine — Design

**Date:** 2026-09-23  
**Branch:** `m772-personal-model-confidence`  
**Depends on:** M771 Result Intelligence 3.0 (`dc42f8aa72c4870b65e0f9ca5f039007ea198f5a`)  
**Status:** Design approved in chat; implementation not yet authorized.

## 1. Objective

Transform the existing collection of profile engines into a single, explainable personal model that can answer: “What does the app currently know about me, how certain is it, how stable is it, where is it contradictory, and what remains unknown?”

The model must not diagnose, infer medical or psychiatric conditions, estimate IQ, or present weak evidence as fact. It must remain deterministic, offline-first, explainable, and compatible with the existing profile stack.

## 2. Product principles

1. **Evidence before conclusion.** Every derived personal statement must trace back to quiz evidence and/or existing longitudinal evidence.
2. **Confidence is not stability.** Confidence measures how well supported a trait is; stability measures how consistent it is over time.
3. **Uncertainty is first-class.** The model must be able to say “unknown”, “still exploring”, or “contradictory”.
4. **No fixed labels from sparse data.** One quiz cannot establish a trait as definitive.
5. **No pseudo-clinical framing.** Wording must stay behavioral and descriptive.
6. **No duplicate source of truth.** Existing engines remain responsible for their current domains; the Personal Model orchestrates them.
7. **Backward compatible.** Historic users with sparse or partial data receive graceful degradation.

## 3. Existing foundations to reuse

The implementation must reuse rather than duplicate:

- `TraitGraph`: current trait score, confidence, evidence, contradiction count.
- `ProfileCoverage`: known/unknown/developing/strong coverage.
- `TraitEvolution`: previous/current score and evidence changes.
- `TraitTimeline`: trait-level history.
- `LongitudinalTrend`: rising/falling/stable/volatile/outlier trends.
- `ProfileNarrative`: meaningful change narratives.
- `ProfileKnowledgeMap`: domain-level known/unknown coverage.
- `NextQuizRecommendation`: information-gain-driven next quiz.

## 4. Proposed architecture

```
Quiz results + score history + timed history
                 |
                 v
             TraitGraph
                 |
        +--------+--------+
        |        |        |
        v        v        v
   Coverage   Evolution  Timelines
        \        |        /
         \       |       /
          v      v      v
         PersonalModelEngine
                 |
                 v
          PersonalModel
                 |
       +---------+---------+
       |         |         |
       v         v         v
  Who Am I?  Insights   Recommendations
```

`PersonalModelEngine` is an orchestration layer. It does not recalculate quiz scores or trait mappings.

## 5. Core data model

### 5.1 PersonalTrait

```kotlin
data class PersonalTrait(
    val traitId: String,
    val score: Int,
    val confidence: Int,
    val certainty: PersonalCertainty,
    val stability: PersonalStability,
    val contradictionLevel: ContradictionLevel,
    val evidenceCount: Int,
    val sourceQuizIds: List<String>,
    val trend: PersonalTrend,
    val isDistinctive: Boolean
)
```

### 5.2 Certainty

```kotlin
enum class PersonalCertainty {
    UNKNOWN,
    EXPLORING,
    LIKELY,
    ESTABLISHED
}
```

Rules must be deterministic and conservative.

Initial thresholds:

- `UNKNOWN`: no evidence.
- `EXPLORING`: one source, or low confidence, or substantial contradiction.
- `LIKELY`: at least two independent evidence sources and confidence >= 45, with no severe contradiction.
- `ESTABLISHED`: at least three evidence sources, confidence >= 65, acceptable stability, and low contradiction.

A score near neutral must not become `ESTABLISHED` merely because evidence is abundant. “Well documented as balanced” is valid, but “strong trait” is not.

### 5.3 Stability

```kotlin
enum class PersonalStability {
    UNKNOWN,
    VARIABLE,
    MODERATE,
    STABLE
}
```

Stability is derived from trait timelines and longitudinal behavior.

Suggested rules:

- no repeated data: `UNKNOWN`
- volatile/outlier-heavy timeline: `VARIABLE`
- repeated observations with modest movement: `MODERATE`
- repeated observations with low volatility and no meaningful movement: `STABLE`

The exact thresholds must reuse existing timeline/trend semantics where possible instead of introducing conflicting definitions.

### 5.4 Contradiction level

```kotlin
enum class ContradictionLevel {
    NONE,
    LOW,
    MODERATE,
    HIGH
}
```

This should combine:
- contradictory evidence count,
- number of evidence sources,
- opposing contribution strength.

The engine must avoid treating one weak opposing signal as a major contradiction.

### 5.5 Trend

```kotlin
enum class PersonalTrend {
    UNKNOWN,
    STABLE,
    RISING,
    FALLING,
    VARIABLE
}
```

This maps from existing `LongitudinalTrendKind` / trait timeline behavior.

### 5.6 PersonalModel

```kotlin
data class PersonalModel(
    val traits: List<PersonalTrait>,
    val establishedTraits: List<PersonalTrait>,
    val likelyTraits: List<PersonalTrait>,
    val exploringTraits: List<PersonalTrait>,
    val unknownTraitIds: List<String>,
    val stableTraits: List<PersonalTrait>,
    val variableTraits: List<PersonalTrait>,
    val contradictoryTraits: List<PersonalTrait>,
    val strongestKnowledgeDomains: List<TraitDomainCoverage>,
    val knowledgeGaps: List<TraitDomainCoverage>
)
```

The aggregate lists are derived views, not separate sources of truth.

## 6. Distinctiveness

The model must distinguish between “well known” and “strongly directional”.

A trait is `isDistinctive = true` only when:
- certainty is `LIKELY` or `ESTABLISHED`, and
- the score is sufficiently far from neutral.

Initial threshold: `abs(score - 50) >= 15`.

This prevents statements such as “You are highly X” when the system actually knows the user is consistently near the midpoint.

## 7. Confidence semantics

Existing `ProfileTrait.confidence` remains the quantitative input.

M772 adds interpretation, not a competing confidence formula.

The model may reduce effective certainty when:
- contradictory evidence is high,
- longitudinal behavior is volatile,
- evidence count is too small,
- evidence is concentrated in one source.

It must never increase the underlying numeric confidence beyond the source graph.

## 8. Contradictions and paradoxes

M772 only detects **trait-level evidence contradiction**.

It must not yet infer cross-trait paradoxes such as “high independence + high need for approval”. That belongs to a later dedicated feature.

M772 should expose enough structured data for that future engine:
- certainty,
- contradiction level,
- source count,
- stability,
- score direction.

## 9. Knowledge gaps

Knowledge gaps are derived from `ProfileCoverage` and `ProfileKnowledgeMap`.

The model should expose:
- unknown traits,
- low-confidence traits,
- contradictory traits,
- least-covered domains.

This feeds:
- future “What we still don’t know” UI,
- `NextQuizRecommendation`,
- guided exploration.

## 10. Future “Who Am I?” integration

M772 does not redesign the entire Profile screen.

Its output is designed for a later “Who Am I?” experience with five primary blocks:

1. **Your current portrait** — most established and distinctive traits.
2. **What seems stable** — well-supported, longitudinally stable traits.
3. **Your nuances** — variable or contradictory traits.
4. **What we are still learning** — low-confidence or unknown areas.
5. **Your evolution** — meaningful recent movement.

M772 must make those blocks possible without embedding presentation strings in the domain layer.

## 11. Localization

The Personal Model domain layer contains no user-facing prose.

UI copy added later must use Android resources with EN/FR parity and existing trait localization.

## 12. Privacy

M772 introduces no new raw personal data source.

Requirements:
- no raw answer telemetry,
- no free-text personality inference,
- no remote model calls,
- no advertising use of personal model data,
- derived model remains local under current persistence rules,
- future behavioral-data integrations require separate explicit opt-in design.

## 13. Safety and wording constraints

The model may describe:
- tendencies,
- repeated behavioral patterns,
- confidence,
- stability,
- variability,
- contradictions in available evidence.

It must not conclude:
- psychiatric diagnosis,
- medical condition,
- addiction,
- intelligence/IQ,
- criminal propensity,
- employability,
- relationship destiny,
- immutable personality.

User-facing wording must distinguish “the app has evidence for” from “this is objectively true about you”.

## 14. Backward compatibility

Historic profiles with:
- no timed history,
- only one completed quiz,
- incomplete trait coverage,
- older persisted score formats

must still build a valid `PersonalModel`.

Expected graceful behavior:
- missing history => stability `UNKNOWN`
- one evidence source => at most `EXPLORING`
- missing trait => `UNKNOWN`
- no contradictions available => do not invent any

No migration should be required solely for M772 if the model can be recomputed from existing persisted profile data.

## 15. Determinism

Given identical:
- catalog,
- latest scores,
- score history,
- timed history,

the output must be identical regardless of map/list insertion order where semantic order is irrelevant.

All aggregate lists must define deterministic sorting.

Suggested ordering:
1. certainty,
2. confidence,
3. distinctiveness,
4. trait ID as stable tiebreaker.

## 16. Testing strategy

### Domain tests

Required tests:

1. no evidence => `UNKNOWN`
2. one quiz cannot produce `ESTABLISHED`
3. coherent multi-source evidence increases certainty
4. high contradiction lowers certainty
5. volatile history lowers stability
6. repeated stable history yields `STABLE`
7. strong confidence near score 50 does not mark trait as distinctive
8. sparse historic profile degrades gracefully
9. source ordering does not change result
10. unknown traits are included in knowledge gaps
11. no source list contains duplicates
12. zero-weight trait mappings do not create evidence

### Integration tests

- `GlobalProfileEngine` can produce a `PersonalModel` without changing existing outputs.
- existing profile UI remains functional before M773 UI work.
- M771 result connections still behave identically after M772 is added.

### Release gates

Reuse existing:
- Android CI
- JVM tests
- instrumentation compile
- lint
- M59 device validation

No gate may be weakened.

## 17. Proposed implementation boundaries

New domain files:

- `PersonalModel.kt`
- `PersonalModelEngine.kt` only if splitting improves file clarity

Likely modified files:

- `GlobalProfile.kt` to expose `personalModel`
- associated unit tests

Not part of M772:
- major Profile screen redesign,
- behavioral phone data,
- values/motivations,
- cross-trait paradox engine,
- contextual questionnaires,
- remote AI summaries.

## 18. Rollout

Phase 1:
- pure domain model + tests.

Phase 2:
- integrate into `GlobalProfileSummary`.

Phase 3:
- verify no regressions in existing profile/result flows.

Phase 4:
- M773 consumes M772 to build the new “Who Am I?” interface.

## 19. Definition of done

M772 is complete when:

- every current known trait maps to a deterministic `PersonalTrait`,
- certainty, stability, contradiction and trend are available,
- knowledge gaps are explicit,
- sparse users degrade gracefully,
- no existing scoring semantics are changed,
- no new sensitive data source is introduced,
- unit and integration tests pass,
- Android CI and device validation pass,
- the feature remains non-clinical and explainable.

