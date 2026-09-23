# M772 — Personal Model & Confidence Engine Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a deterministic Personal Model that interprets existing trait evidence into certainty, stability, contradiction, trend, distinctiveness, and explicit knowledge gaps without changing current scoring semantics.

**Architecture:** Reuse `TraitGraph`, `ProfileCoverage`, `TraitEvolution`, `TraitTimeline`, `LongitudinalTrend`, and `ProfileKnowledgeMap` as source engines. Add a pure `PersonalModelEngine` orchestration layer and expose its result through `GlobalProfileSummary`; no UI redesign is included in M772.

**Tech Stack:** Kotlin, Android/JVM unit tests, Jetpack Compose project, existing deterministic profile engines.

**Spec:** `docs/superpowers/specs/2026-09-23-m772-personal-model-confidence-design.md`

## Global Constraints

- M772 depends on M771 HEAD `dc42f8aa72c4870b65e0f9ca5f039007ea198f5a`.
- Reuse existing engines; do not duplicate scoring, confidence, coverage, evolution, or trend calculations.
- No remote model calls.
- No raw answer telemetry.
- No free-text personality inference.
- No psychiatric, medical, addiction, IQ, criminal-propensity, employability, relationship-destiny, or immutable-personality conclusions.
- One quiz cannot produce `ESTABLISHED`.
- Missing history must degrade to stability `UNKNOWN`.
- Zero-weight trait mappings must not create evidence.
- All outputs must be deterministic for semantically identical inputs.
- No UI redesign in M772.
- No quality gate may be weakened.

## Review Focus

1. A trait with three sources but all scores close to neutral must not be marked distinctive; Task 2 pins this.
2. A trait with enough evidence for high confidence but volatile history must not become `ESTABLISHED`; Task 3 pins this.
3. A weak single opposing signal among several coherent sources must not become a high contradiction; Task 4 pins this.
4. Input ordering differences in graph, coverage, and timeline collections must not change the semantic Personal Model; Task 5 pins this.
5. Historic profiles with no timed history and sparse scores must still build a valid model without crashes or invented stability; Task 6 pins this.

---

### Task 1: Define the Personal Model domain contract

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/PersonalModel.kt`
- Create: `app/src/test/java/com/whoareyou/app/PersonalModelContractTest.kt`

**Interfaces:**
- Consumes: existing trait/domain identifiers and no engine behavior yet.
- Produces:
  - `enum class PersonalCertainty { UNKNOWN, EXPLORING, LIKELY, ESTABLISHED }`
  - `enum class PersonalStability { UNKNOWN, VARIABLE, MODERATE, STABLE }`
  - `enum class ContradictionLevel { NONE, LOW, MODERATE, HIGH }`
  - `enum class PersonalTrend { UNKNOWN, STABLE, RISING, FALLING, VARIABLE }`
  - `data class PersonalTrait(...)`
  - `data class PersonalModel(...)`

- [ ] **Step 1: Write the failing contract test**

```kotlin
package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PersonalModelContractTest {
    @Test
    fun `personal trait exposes certainty stability contradiction and trend`() {
        val trait = PersonalTrait(
            traitId = "curiosity",
            score = 78,
            confidence = 72,
            certainty = PersonalCertainty.LIKELY,
            stability = PersonalStability.STABLE,
            contradictionLevel = ContradictionLevel.LOW,
            evidenceCount = 3,
            sourceQuizIds = listOf("a", "b", "c"),
            trend = PersonalTrend.STABLE,
            isDistinctive = true
        )

        assertEquals(PersonalCertainty.LIKELY, trait.certainty)
        assertEquals(PersonalStability.STABLE, trait.stability)
        assertEquals(ContradictionLevel.LOW, trait.contradictionLevel)
        assertEquals(PersonalTrend.STABLE, trait.trend)
        assertFalse(trait.sourceQuizIds.isEmpty())
    }
}
```

- [ ] **Step 2: Run the test and verify RED**

Run:

```bash
gradle :app:testDebugUnitTest --tests com.whoareyou.app.PersonalModelContractTest --stacktrace
```

Expected: compilation failure because `PersonalTrait` and related enums do not exist.

- [ ] **Step 3: Add the minimal domain types**

```kotlin
package com.whoareyou.app

enum class PersonalCertainty {
    UNKNOWN,
    EXPLORING,
    LIKELY,
    ESTABLISHED
}

enum class PersonalStability {
    UNKNOWN,
    VARIABLE,
    MODERATE,
    STABLE
}

enum class ContradictionLevel {
    NONE,
    LOW,
    MODERATE,
    HIGH
}

enum class PersonalTrend {
    UNKNOWN,
    STABLE,
    RISING,
    FALLING,
    VARIABLE
}

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
) {
    companion object {
        val EMPTY = PersonalModel(
            traits = emptyList(),
            establishedTraits = emptyList(),
            likelyTraits = emptyList(),
            exploringTraits = emptyList(),
            unknownTraitIds = emptyList(),
            stableTraits = emptyList(),
            variableTraits = emptyList(),
            contradictoryTraits = emptyList(),
            strongestKnowledgeDomains = emptyList(),
            knowledgeGaps = emptyList()
        )
    }
}
```

- [ ] **Step 4: Run the focused test and verify GREEN**

Run the same Gradle command.

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/whoareyou/app/PersonalModel.kt app/src/test/java/com/whoareyou/app/PersonalModelContractTest.kt
git commit -m "feat: define personal model domain contract"
```

---

### Task 2: Derive certainty and distinctiveness from existing trait evidence

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt`
- Create: `app/src/test/java/com/whoareyou/app/PersonalModelCertaintyTest.kt`

**Interfaces:**
- Consumes:
  - `TraitGraph`
  - `ProfileCoverage`
  - empty/default timelines and knowledge map in this task
- Produces:
  - `object PersonalModelEngine`
  - `fun build(graph: TraitGraph, coverage: ProfileCoverage, timelines: List<TraitTimeline> = emptyList(), knowledgeMap: ProfileKnowledgeMap = ProfileKnowledgeMap(emptyList(), emptyList(), emptyList(), emptyList())): PersonalModel`

- [ ] **Step 1: Write failing certainty tests**

```kotlin
package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalModelCertaintyTest {
    @Test
    fun `one evidence source can never become established`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("curiosity", 80, 90, evidenceCount = 1)),
            evidenceCount = 1
        )
        val model = PersonalModelEngine.build(graph, coverageFor("curiosity", 1, 90))

        assertEquals(PersonalCertainty.EXPLORING, model.traits.single().certainty)
    }

    @Test
    fun `coherent multi source evidence can become established`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("curiosity", 80, 75, evidenceCount = 3)),
            evidenceCount = 3
        )
        val model = PersonalModelEngine.build(graph, coverageFor("curiosity", 3, 75))

        assertEquals(PersonalCertainty.ESTABLISHED, model.traits.single().certainty)
    }

    @Test
    fun `well known neutral trait is not distinctive`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("curiosity", 52, 85, evidenceCount = 4)),
            evidenceCount = 4
        )
        val model = PersonalModelEngine.build(graph, coverageFor("curiosity", 4, 85))

        assertFalse(model.traits.single().isDistinctive)
    }

    @Test
    fun `directional likely trait is distinctive`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("curiosity", 72, 55, evidenceCount = 2)),
            evidenceCount = 2
        )
        val model = PersonalModelEngine.build(graph, coverageFor("curiosity", 2, 55))

        assertTrue(model.traits.single().isDistinctive)
    }

    private fun profileTrait(
        id: String,
        score: Int,
        confidence: Int,
        evidenceCount: Int
    ): ProfileTrait = ProfileTrait(
        id = id,
        score = score,
        confidence = confidence,
        evidence = List(evidenceCount) { index ->
            TraitEvidence(
                quizId = "q$index",
                quizTitle = "Q$index",
                sourceScore = score,
                weight = 1.0,
                contribution = score,
                signalStrength = 0.8
            )
        },
        contradictoryEvidenceCount = 0
    )

    private fun coverageFor(
        id: String,
        evidenceCount: Int,
        confidence: Int
    ): ProfileCoverage = ProfileCoverage(
        knownTraitCount = 1,
        totalTraitCount = 1,
        coveragePercent = 100,
        averageConfidence = confidence,
        strongTraitCount = 1,
        uncertainTraitCount = 0,
        traits = listOf(
            TraitCoverage(
                traitId = id,
                evidenceCount = evidenceCount,
                confidence = confidence,
                contradictoryEvidenceCount = 0,
                status = CoverageStatus.STRONG
            )
        )
    )
}
```

- [ ] **Step 2: Run focused tests and verify RED**

Run:

```bash
gradle :app:testDebugUnitTest --tests com.whoareyou.app.PersonalModelCertaintyTest --stacktrace
```

Expected: failure because `PersonalModelEngine` does not exist.

- [ ] **Step 3: Implement minimal certainty/distinctiveness logic**

```kotlin
package com.whoareyou.app

import kotlin.math.abs

object PersonalModelEngine {
    fun build(
        graph: TraitGraph,
        coverage: ProfileCoverage,
        timelines: List<TraitTimeline> = emptyList(),
        knowledgeMap: ProfileKnowledgeMap = ProfileKnowledgeMap(
            strong = emptyList(),
            developing = emptyList(),
            unknown = emptyList(),
            domains = emptyList()
        )
    ): PersonalModel {
        val traits = graph.traits
            .map { trait ->
                val contradiction = contradictionLevel(trait)
                val certainty = certaintyFor(trait, contradiction, PersonalStability.UNKNOWN)
                PersonalTrait(
                    traitId = trait.id,
                    score = trait.score,
                    confidence = trait.confidence,
                    certainty = certainty,
                    stability = PersonalStability.UNKNOWN,
                    contradictionLevel = contradiction,
                    evidenceCount = trait.evidenceCount,
                    sourceQuizIds = trait.evidence.map { it.quizId }.distinct().sorted(),
                    trend = PersonalTrend.UNKNOWN,
                    isDistinctive = certainty >= PersonalCertainty.LIKELY &&
                        abs(trait.score - 50) >= 15
                )
            }
            .sortedWith(
                compareByDescending<PersonalTrait> { it.certainty.ordinal }
                    .thenByDescending { it.confidence }
                    .thenByDescending { abs(it.score - 50) }
                    .thenBy { it.traitId }
            )

        return aggregate(traits, coverage, knowledgeMap)
    }

    private fun certaintyFor(
        trait: ProfileTrait,
        contradiction: ContradictionLevel,
        stability: PersonalStability
    ): PersonalCertainty = when {
        trait.evidenceCount == 0 -> PersonalCertainty.UNKNOWN
        trait.evidenceCount == 1 -> PersonalCertainty.EXPLORING
        contradiction == ContradictionLevel.HIGH -> PersonalCertainty.EXPLORING
        trait.evidenceCount >= 3 &&
            trait.confidence >= 65 &&
            stability != PersonalStability.VARIABLE -> PersonalCertainty.ESTABLISHED
        trait.evidenceCount >= 2 && trait.confidence >= 45 ->
            PersonalCertainty.LIKELY
        else -> PersonalCertainty.EXPLORING
    }

    private fun contradictionLevel(trait: ProfileTrait): ContradictionLevel = when {
        trait.contradictoryEvidenceCount <= 0 -> ContradictionLevel.NONE
        trait.evidenceCount <= 1 -> ContradictionLevel.LOW
        trait.contradictoryEvidenceCount * 2 >= trait.evidenceCount -> ContradictionLevel.HIGH
        trait.contradictoryEvidenceCount * 3 >= trait.evidenceCount -> ContradictionLevel.MODERATE
        else -> ContradictionLevel.LOW
    }

    private fun aggregate(
        traits: List<PersonalTrait>,
        coverage: ProfileCoverage,
        knowledgeMap: ProfileKnowledgeMap
    ): PersonalModel = PersonalModel(
        traits = traits,
        establishedTraits = traits.filter { it.certainty == PersonalCertainty.ESTABLISHED },
        likelyTraits = traits.filter { it.certainty == PersonalCertainty.LIKELY },
        exploringTraits = traits.filter { it.certainty == PersonalCertainty.EXPLORING },
        unknownTraitIds = coverage.traits
            .filter { it.status == CoverageStatus.UNKNOWN }
            .map { it.traitId }
            .sorted(),
        stableTraits = traits.filter { it.stability == PersonalStability.STABLE },
        variableTraits = traits.filter { it.stability == PersonalStability.VARIABLE },
        contradictoryTraits = traits.filter { it.contradictionLevel >= ContradictionLevel.MODERATE },
        strongestKnowledgeDomains = knowledgeMap.domains
            .sortedWith(compareByDescending<TraitDomainCoverage> { it.coveragePercent }.thenBy { it.domain.name })
            .take(3),
        knowledgeGaps = knowledgeMap.domains
            .sortedWith(compareBy<TraitDomainCoverage> { it.coveragePercent }.thenBy { it.domain.name })
            .take(3)
    )
}
```

- [ ] **Step 4: Run focused tests and verify GREEN**

Run the same test command.

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt app/src/test/java/com/whoareyou/app/PersonalModelCertaintyTest.kt
git commit -m "feat: derive personal model certainty"
```

---

### Task 3: Derive longitudinal stability and trend

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt`
- Create: `app/src/test/java/com/whoareyou/app/PersonalModelStabilityTest.kt`

**Interfaces:**
- Consumes: `List<TraitTimeline>` and existing `LongitudinalTrendKind`.
- Produces: populated `PersonalTrait.stability` and `PersonalTrait.trend`.

- [ ] **Step 1: Write failing stability tests**

```kotlin
package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalModelStabilityTest {
    @Test
    fun `missing timeline keeps stability unknown`() {
        val model = PersonalModelEngine.build(
            graph = graphFor("planning", 78, 72, 3),
            coverage = coverageFor("planning", 3, 72),
            timelines = emptyList()
        )

        assertEquals(PersonalStability.UNKNOWN, model.traits.single().stability)
    }

    @Test
    fun `stable repeated timeline yields stable stability`() {
        val timeline = timelineFor(
            traitId = "planning",
            kind = LongitudinalTrendKind.STABLE,
            pointCount = 4
        )
        val model = PersonalModelEngine.build(
            graph = graphFor("planning", 78, 72, 3),
            coverage = coverageFor("planning", 3, 72),
            timelines = listOf(timeline)
        )

        assertEquals(PersonalStability.STABLE, model.traits.single().stability)
        assertEquals(PersonalTrend.STABLE, model.traits.single().trend)
    }

    @Test
    fun `volatile history prevents established certainty`() {
        val timeline = timelineFor(
            traitId = "planning",
            kind = LongitudinalTrendKind.VOLATILE,
            pointCount = 4
        )
        val model = PersonalModelEngine.build(
            graph = graphFor("planning", 78, 80, 4),
            coverage = coverageFor("planning", 4, 80),
            timelines = listOf(timeline)
        )

        assertEquals(PersonalStability.VARIABLE, model.traits.single().stability)
        assertEquals(PersonalCertainty.LIKELY, model.traits.single().certainty)
    }
}
```

Test helpers must construct real `TraitTimeline` and its existing trend/point types, using exact constructors from `TraitTimeline.kt`.

- [ ] **Step 2: Run focused tests and verify RED**

Run:

```bash
gradle :app:testDebugUnitTest --tests com.whoareyou.app.PersonalModelStabilityTest --stacktrace
```

Expected: assertions fail because stability/trend are currently always `UNKNOWN`.

- [ ] **Step 3: Add timeline mapping**

Add helpers in `PersonalModelEngine`:

```kotlin
private fun stabilityFor(timeline: TraitTimeline?): PersonalStability = when {
    timeline == null || timeline.points.size < 2 -> PersonalStability.UNKNOWN
    timeline.trend.kind == LongitudinalTrendKind.VOLATILE ||
        timeline.trend.kind == LongitudinalTrendKind.OUTLIER -> PersonalStability.VARIABLE
    timeline.trend.kind == LongitudinalTrendKind.STABLE &&
        timeline.points.size >= 3 -> PersonalStability.STABLE
    else -> PersonalStability.MODERATE
}

private fun trendFor(timeline: TraitTimeline?): PersonalTrend = when (timeline?.trend?.kind) {
    null, LongitudinalTrendKind.INSUFFICIENT -> PersonalTrend.UNKNOWN
    LongitudinalTrendKind.RISING -> PersonalTrend.RISING
    LongitudinalTrendKind.FALLING -> PersonalTrend.FALLING
    LongitudinalTrendKind.VOLATILE,
    LongitudinalTrendKind.OUTLIER -> PersonalTrend.VARIABLE
    LongitudinalTrendKind.STABLE -> PersonalTrend.STABLE
}
```

Build a `timelineByTraitId` map and calculate stability before certainty so `VARIABLE` can cap certainty below `ESTABLISHED`.

- [ ] **Step 4: Run focused tests and verify GREEN**

Expected: PASS.

- [ ] **Step 5: Run Task 2 regression tests**

```bash
gradle :app:testDebugUnitTest --tests com.whoareyou.app.PersonalModelCertaintyTest --stacktrace
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt app/src/test/java/com/whoareyou/app/PersonalModelStabilityTest.kt
git commit -m "feat: derive personal trait stability"
```

---

### Task 4: Make contradiction severity evidence-aware

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt`
- Create: `app/src/test/java/com/whoareyou/app/PersonalModelContradictionTest.kt`

**Interfaces:**
- Consumes: `ProfileTrait.evidence`, `contradictoryEvidenceCount`, and contribution direction/strength.
- Produces: robust `ContradictionLevel` that does not overreact to weak opposition.

- [ ] **Step 1: Write failing contradiction tests**

```kotlin
package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalModelContradictionTest {
    @Test
    fun `one weak opposing signal among strong coherent sources stays low contradiction`() {
        val trait = ProfileTrait(
            id = "independence",
            score = 76,
            confidence = 78,
            evidence = listOf(
                evidence("a", contribution = 82, strength = 0.9),
                evidence("b", contribution = 77, strength = 0.8),
                evidence("c", contribution = 71, strength = 0.75),
                evidence("d", contribution = 48, strength = 0.1)
            ),
            contradictoryEvidenceCount = 1
        )

        val model = PersonalModelEngine.build(
            TraitGraph(listOf(trait), 4),
            coverageFor("independence", 4, 78)
        )

        assertEquals(ContradictionLevel.LOW, model.traits.single().contradictionLevel)
    }

    @Test
    fun `strong opposing signals can produce high contradiction`() {
        val trait = ProfileTrait(
            id = "independence",
            score = 56,
            confidence = 70,
            evidence = listOf(
                evidence("a", contribution = 85, strength = 0.9),
                evidence("b", contribution = 80, strength = 0.85),
                evidence("c", contribution = 20, strength = 0.9),
                evidence("d", contribution = 18, strength = 0.85)
            ),
            contradictoryEvidenceCount = 2
        )

        val model = PersonalModelEngine.build(
            TraitGraph(listOf(trait), 4),
            coverageFor("independence", 4, 70)
        )

        assertEquals(ContradictionLevel.HIGH, model.traits.single().contradictionLevel)
        assertEquals(PersonalCertainty.EXPLORING, model.traits.single().certainty)
    }
}
```

- [ ] **Step 2: Run focused tests and verify RED**

Expected: first or second contradiction assertion fails under count-only logic.

- [ ] **Step 3: Implement weighted contradiction severity**

Use the final trait score as the dominant side and only count opposing evidence weighted by `signalStrength * abs(weight)`.

```kotlin
private fun contradictionLevel(trait: ProfileTrait): ContradictionLevel {
    if (trait.evidence.isEmpty() || trait.contradictoryEvidenceCount == 0) {
        return ContradictionLevel.NONE
    }

    val dominantHigh = trait.score >= 50
    val totalStrength = trait.evidence.sumOf {
        (it.signalStrength * kotlin.math.abs(it.weight)).coerceAtLeast(0.0)
    }
    if (totalStrength <= 0.0) return ContradictionLevel.NONE

    val opposingStrength = trait.evidence
        .filter { (it.contribution >= 50) != dominantHigh }
        .sumOf { (it.signalStrength * kotlin.math.abs(it.weight)).coerceAtLeast(0.0) }

    val ratio = opposingStrength / totalStrength
    return when {
        ratio >= 0.40 -> ContradictionLevel.HIGH
        ratio >= 0.25 -> ContradictionLevel.MODERATE
        ratio > 0.0 -> ContradictionLevel.LOW
        else -> ContradictionLevel.NONE
    }
}
```

- [ ] **Step 4: Run focused tests and verify GREEN**

Expected: PASS.

- [ ] **Step 5: Run certainty/stability regressions**

Run all `PersonalModel*Test` JVM tests.

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt app/src/test/java/com/whoareyou/app/PersonalModelContradictionTest.kt
git commit -m "feat: weight personal trait contradictions"
```

---

### Task 5: Add deterministic aggregates and knowledge gaps

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt`
- Create: `app/src/test/java/com/whoareyou/app/PersonalModelAggregationTest.kt`

**Interfaces:**
- Consumes: full `ProfileCoverage` and `ProfileKnowledgeMap`.
- Produces deterministic aggregate lists in `PersonalModel`.

- [ ] **Step 1: Write failing aggregation tests**

```kotlin
package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalModelAggregationTest {
    @Test
    fun `unknown coverage traits appear in knowledge gaps`() {
        val coverage = ProfileCoverage(
            knownTraitCount = 1,
            totalTraitCount = 2,
            coveragePercent = 50,
            averageConfidence = 70,
            strongTraitCount = 1,
            uncertainTraitCount = 1,
            traits = listOf(
                TraitCoverage("curiosity", 3, 70, 0, CoverageStatus.STRONG),
                TraitCoverage("boundaries", 0, 0, 0, CoverageStatus.UNKNOWN)
            )
        )

        val model = PersonalModelEngine.build(
            graph = graphFor("curiosity", 80, 70, 3),
            coverage = coverage
        )

        assertEquals(listOf("boundaries"), model.unknownTraitIds)
    }

    @Test
    fun `input ordering does not change semantic output`() {
        val a = PersonalModelEngine.build(
            graph = graphWithTraits(order = listOf("curiosity", "planning")),
            coverage = coverageWithTraits(order = listOf("curiosity", "planning"))
        )
        val b = PersonalModelEngine.build(
            graph = graphWithTraits(order = listOf("planning", "curiosity")),
            coverage = coverageWithTraits(order = listOf("planning", "curiosity"))
        )

        assertEquals(a, b)
    }

    @Test
    fun `source quiz ids are unique and sorted`() {
        val trait = ProfileTrait(
            id = "curiosity",
            score = 80,
            confidence = 70,
            evidence = listOf(
                evidence("z", 80, 0.8),
                evidence("a", 80, 0.8),
                evidence("z", 80, 0.8)
            ),
            contradictoryEvidenceCount = 0
        )

        val model = PersonalModelEngine.build(
            TraitGraph(listOf(trait), 3),
            coverageFor("curiosity", 3, 70)
        )

        assertEquals(listOf("a", "z"), model.traits.single().sourceQuizIds)
    }
}
```

- [ ] **Step 2: Run focused tests and verify RED**

Expected: ordering comparison fails if aggregate ordering still depends on input order.

- [ ] **Step 3: Make all outputs deterministic**

Ensure:
- trait maps keyed by ID are used for joins,
- source IDs use `distinct().sorted()`,
- aggregate trait lists sort by the shared stable comparator,
- unknown IDs sort alphabetically,
- domain lists sort by coverage then `domain.name`.

Define one private comparator:

```kotlin
private val personalTraitComparator =
    compareByDescending<PersonalTrait> { it.certainty.ordinal }
        .thenByDescending { it.confidence }
        .thenByDescending { kotlin.math.abs(it.score - 50) }
        .thenBy { it.traitId }
```

Apply this comparator to all trait aggregate lists after filtering.

- [ ] **Step 4: Run focused tests and verify GREEN**

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt app/src/test/java/com/whoareyou/app/PersonalModelAggregationTest.kt
git commit -m "feat: aggregate deterministic personal knowledge"
```

---

### Task 6: Integrate Personal Model into GlobalProfileEngine

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/GlobalProfile.kt`
- Create: `app/src/test/java/com/whoareyou/app/GlobalProfilePersonalModelTest.kt`

**Interfaces:**
- Consumes existing outputs already built inside `GlobalProfileEngine.build`.
- Produces `GlobalProfileSummary.personalModel: PersonalModel`.

- [ ] **Step 1: Write failing integration tests**

```kotlin
package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GlobalProfilePersonalModelTest {
    @Test
    fun `global profile exposes personal model`() {
        val quiz = testQuiz(
            id = "curiosity-quiz",
            traits = listOf(QuizTraitWeight("curiosity", 1.0))
        )

        val summary = GlobalProfileEngine.build(
            catalog = listOf(quiz),
            latestScores = mapOf("curiosity-quiz" to 82),
            scoreHistory = mapOf("curiosity-quiz" to listOf(82)),
            timedScoreHistory = emptyMap()
        )

        assertNotNull(summary.personalModel)
        assertEquals(PersonalCertainty.EXPLORING, summary.personalModel.traits.single().certainty)
        assertEquals(PersonalStability.UNKNOWN, summary.personalModel.traits.single().stability)
    }

    @Test
    fun `historic sparse profile builds without timed history`() {
        val quiz = testQuiz(
            id = "planning-quiz",
            traits = listOf(QuizTraitWeight("planning", 1.0))
        )

        val summary = GlobalProfileEngine.build(
            catalog = listOf(quiz),
            latestScores = mapOf("planning-quiz" to 68)
        )

        assertEquals(1, summary.personalModel.traits.size)
        assertEquals(PersonalStability.UNKNOWN, summary.personalModel.traits.single().stability)
    }
}
```

- [ ] **Step 2: Run focused tests and verify RED**

Expected: compilation failure because `GlobalProfileSummary.personalModel` does not exist.

- [ ] **Step 3: Add Personal Model to GlobalProfileSummary**

Add:

```kotlin
val personalModel: PersonalModel = PersonalModel.EMPTY
```

to `GlobalProfileSummary`.

Inside `GlobalProfileEngine.build`, after `coverage` and `traitTimelines` exist:

```kotlin
val knowledgeMap = ProfileKnowledgeMapEngine.build(coverage)
val personalModel = PersonalModelEngine.build(
    graph = traitGraph,
    coverage = coverage,
    timelines = traitTimelines,
    knowledgeMap = knowledgeMap
)
```

Pass `personalModel = personalModel` to the returned summary.

- [ ] **Step 4: Run focused tests and verify GREEN**

Expected: PASS.

- [ ] **Step 5: Run existing GlobalProfile/TraitGraph/ProfileCoverage tests**

Run:

```bash
gradle :app:testDebugUnitTest --stacktrace
```

Expected: all JVM tests PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/whoareyou/app/GlobalProfile.kt app/src/test/java/com/whoareyou/app/GlobalProfilePersonalModelTest.kt
git commit -m "feat: expose personal model in global profile"
```

---

### Task 7: Protect zero-weight evidence and M771 compatibility

**Files:**
- Modify if required: `app/src/main/java/com/whoareyou/app/TraitGraph.kt`
- Modify if required: `app/src/main/java/com/whoareyou/app/PersonalModelEngine.kt`
- Create: `app/src/test/java/com/whoareyou/app/PersonalModelCompatibilityTest.kt`
- Existing regression target: `app/src/test/java/com/whoareyou/app/ResultProfileConnectionTest.kt`

**Interfaces:**
- Consumes M771 trait graph/profile connection semantics.
- Produces compatibility guarantees; no new public API.

- [ ] **Step 1: Write failing/protective tests**

```kotlin
package com.whoareyou.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PersonalModelCompatibilityTest {
    @Test
    fun `zero weight mapping never becomes personal trait evidence`() {
        val quiz = testQuiz(
            id = "neutral-map",
            traits = listOf(QuizTraitWeight("curiosity", 0.0))
        )

        val summary = GlobalProfileEngine.build(
            catalog = listOf(quiz),
            latestScores = mapOf("neutral-map" to 100)
        )

        assertTrue(summary.personalModel.traits.none { it.traitId == "curiosity" })
    }

    @Test
    fun `personal model does not mutate m771 result connection inputs`() {
        val graph = TraitGraph(
            traits = listOf(profileTrait("focus", 82, 80, 2)),
            evidenceCount = 2
        )
        PersonalModelEngine.build(
            graph = graph,
            coverage = coverageFor("focus", 2, 80)
        )

        val quiz = testQuiz(
            id = "focus",
            traits = listOf(QuizTraitWeight("focus", 1.0))
        )
        val connections = ResultProfileConnectionEngine.derive(quiz, 84, graph)

        assertFalse(connections.isEmpty())
    }
}
```

- [ ] **Step 2: Run focused tests**

If zero-weight mapping currently produces a trait, expected RED. If it already passes due M771 changes, preserve the test as a regression test and do not invent production changes.

- [ ] **Step 3: If RED, fix the source only**

In `TraitGraphEngine.build`, skip zero-weight mappings before constructing evidence:

```kotlin
dimension.traitWeights
    .filter { it.weight != 0.0 }
    .forEach { mapping ->
        // existing evidence construction
    }
```

Do not add a duplicate filter elsewhere if the source graph can guarantee the invariant.

- [ ] **Step 4: Run compatibility tests and M771 result-connection tests**

```bash
gradle :app:testDebugUnitTest   --tests com.whoareyou.app.PersonalModelCompatibilityTest   --tests com.whoareyou.app.ResultProfileConnectionTest   --stacktrace
```

Expected: PASS.

- [ ] **Step 5: Commit only if code or regression tests were added**

```bash
git add app/src/test/java/com/whoareyou/app/PersonalModelCompatibilityTest.kt app/src/main/java/com/whoareyou/app/TraitGraph.kt
git commit -m "test: protect personal model compatibility"
```

---

### Task 8: Full quality and device verification

**Files:**
- No product files unless a real defect is found.
- Update PR description only after all evidence is available.

**Interfaces:**
- Consumes completed M772 branch.
- Produces release-quality evidence; does not merge.

- [ ] **Step 1: Run repository verification and JVM suite through Android CI**

Required checks:
- repository/catalog validation,
- all JVM unit tests,
- instrumentation-test compilation,
- benchmark compilation,
- Android lint,
- debug build/checksum.

Expected: Android CI SUCCESS for the exact M772 HEAD.

- [ ] **Step 2: Run/observe M59 Android Device Validation for the exact M772 HEAD**

Required jobs:
- connected instrumentation/runtime stress,
- rendering-capable visual validation.

Expected: both jobs SUCCESS.

- [ ] **Step 3: Inspect CI logs rather than trusting workflow summary**

Confirm:
- no JVM test failures,
- no lint errors,
- no instrumentation compile errors,
- no crash/ANR failures in device validation.

- [ ] **Step 4: Review the complete branch against the spec**

Check every definition-of-done line:
- deterministic PersonalTrait output,
- certainty available,
- stability available,
- contradiction available,
- trend available,
- knowledge gaps explicit,
- sparse-user degradation,
- no scoring changes,
- no new sensitive data source,
- no clinical inference,
- existing M771 behavior intact.

- [ ] **Step 5: Update/create the M772 draft PR**

PR body must state:
- dependency on M771,
- architecture,
- tests added,
- exact validated HEAD SHA,
- Android CI run number,
- M59 device run number,
- known limitations: no “Who Am I?” UI yet, no behavioral data, no values/motivations.

- [ ] **Step 6: Do not merge**

Leave PR draft/open until explicit user authorization.

---

## Final branch review checklist

- [ ] No source file duplicates logic already owned by TraitGraph/Coverage/Evolution/Timeline.
- [ ] No `TODO`, `TBD`, or placeholder production behavior.
- [ ] No raw quiz answer storage or telemetry was added.
- [ ] No user-facing diagnostic or immutable-personality claims were added.
- [ ] All aggregate ordering has stable tiebreakers.
- [ ] Historic profiles build safely.
- [ ] M771 result intelligence remains compatible.
- [ ] Android CI is green on exact HEAD.
- [ ] M59 device validation is green on exact HEAD.
- [ ] PR remains unmerged pending explicit authorization.
