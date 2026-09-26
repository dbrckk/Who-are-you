# M771 Result Intelligence 3.0 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn each quiz result into a deterministic, offline, explainable self-reflection report while preserving existing scoring, persistence, sharing, monetization, and accessibility behavior.

**Architecture:** Add pure Kotlin result-intelligence models/engines beside the existing `ResultInterpretationEngine`, then feed one immutable aggregate into focused Compose cards. Authored insight content is localized with the quiz catalog; answer evidence is optional so historic results degrade gracefully. Existing 0–100 scoring remains authoritative.

**Tech Stack:** Kotlin, Jetpack Compose Material 3, JUnit 4, AndroidX Compose UI tests, existing JSON quiz catalog/DataStore/profile infrastructure.

**Spec:** `docs/superpowers/specs/2026-09-22-m771-result-intelligence-3-design.md`

## Global Constraints

- Keep the existing quiz scoring contract and quiz IDs unchanged.
- Offline-first: no new runtime network or remote-generation dependency.
- Do not send raw answers or free-text psychological data through telemetry.
- No medical, psychiatric, IQ, diagnostic, deterministic-personality, or clinical claims.
- French and English result-intelligence content must have parity.
- Existing persisted results must remain readable; missing historical evidence omits evidence UI rather than fabricating it.
- Preserve Done, Retry, Share, Compare, next-quiz recommendation, profile persistence, ads, billing and reduced-motion behavior.
- Keep current minSdk 26, targetSdk 36, compileSdk 37 and Java 17 unless the repository changes independently before execution.
- Do not weaken current lint, minification, R8, accessibility, device-validation or release gates.

## Review Focus

- Score exactly 50: balanced copy must not imply a high-direction preference merely because the legacy enum maps 50 to HIGH.
- Equal answer contributions: ordering must be deterministic and stable.
- Historic result with no answer evidence: evidence card is absent and all other result actions remain usable.
- Sparse profile evidence: no speculative profile connection is emitted.
- Long FR/EN copy at 130% font/landscape: cards scroll without clipping primary content.

---

### Task 1: Result intelligence domain contract and intensity-aware insights

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/ResultIntelligence.kt`
- Create: `app/src/test/java/com/whoareyou/app/ResultIntelligenceTest.kt`
- Modify: `app/src/main/java/com/whoareyou/app/QuizCatalog.kt`
- Modify: localized quiz catalog JSON assets that define the representative test content
- Test: `app/src/test/java/com/whoareyou/app/ResultIntelligenceTest.kt`

**Interfaces:**
- Consumes: `Quiz`, `ResultInterpretationEngine.derive(score)`, localized authored result-intelligence content.
- Produces: `ResultInsightEngine.derive(quiz: Quiz, score: Int): ResultInsightSummary` and immutable content models used by later tasks.

- [ ] **Step 1: Write failing tests for strong-high, strong-low and balanced selection**

Create tests that construct a quiz with three intensity/direction content variants and assert:
```kotlin
val high = ResultInsightEngine.derive(quiz, 88)
assertEquals(ResultSignalStrength.STRONG, high.strength)
assertEquals(listOf("high-strength-1", "high-strength-2", "high-strength-3"), high.strengths)
assertEquals("high-reflection", high.reflection)

val low = ResultInsightEngine.derive(quiz, 12)
assertEquals(ResultDirection.LOW, low.direction)
assertEquals("low-reflection", low.reflection)

val balanced = ResultInsightEngine.derive(quiz, 50)
assertEquals(ResultSignalStrength.BALANCED, balanced.strength)
assertEquals("balanced-reflection", balanced.reflection)
```
Also assert no list exceeds three strengths or three watch-outs.

- [ ] **Step 2: Run the focused JVM test and verify RED**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.ResultIntelligenceTest
```
Expected: compilation/test failure because `ResultInsightEngine` and its models do not exist.

- [ ] **Step 3: Implement the minimal pure domain models and selector**

Add immutable models equivalent to:
```kotlin
data class ResultInsightContent(
    val strengths: List<String>,
    val watchOuts: List<String>,
    val everydayLife: List<String>,
    val reflection: String
)

data class ResultInsightSummary(
    val direction: ResultDirection,
    val strength: ResultSignalStrength,
    val strengths: List<String>,
    val watchOuts: List<String>,
    val everydayLife: List<String>,
    val reflection: String
)
```
Extend `Quiz` with a backward-compatible/defaulted result-intelligence content field. Select content from score direction + signal strength, treating BALANCED as its own authored branch rather than as semantically HIGH.

- [ ] **Step 4: Add one representative EN/FR authored content set and parser support**

Extend catalog parsing with optional result-intelligence JSON. Missing content must parse successfully and yield no rich insight rather than crash. Add equivalent keys/content structure in English and French for the same representative quiz.

- [ ] **Step 5: Run focused and existing interpretation tests**

Run:
```bash
./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.ResultIntelligenceTest --tests com.whoareyou.app.ResultInterpretationTest
```
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main app/src/test
git commit -m "feat: add deterministic result insight model"
```

### Task 2: Explainable answer evidence

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/ResultEvidence.kt`
- Create: `app/src/test/java/com/whoareyou/app/ResultEvidenceTest.kt`
- Modify: quiz completion/result state files identified from the existing scoring flow during execution
- Test: `app/src/test/java/com/whoareyou/app/ResultEvidenceTest.kt`

**Interfaces:**
- Consumes: ordered answer score contributions for the just-completed quiz; question index/text and localized metric labels.
- Produces: `ResultEvidenceEngine.derive(contributions: List<ResultAnswerContribution>, metricLow: String, metricHigh: String): ResultEvidenceSummary?`.

- [ ] **Step 1: Write failing evidence-ranking tests**

Test strong high/low contributions, ties, empty input and clamped malformed contribution values. The stable tie rule is question order:
```kotlin
val summary = ResultEvidenceEngine.derive(
    contributions = listOf(
        ResultAnswerContribution(questionIndex = 0, question = "Q1", answerScore = 3),
        ResultAnswerContribution(questionIndex = 1, question = "Q2", answerScore = 3),
        ResultAnswerContribution(questionIndex = 2, question = "Q3", answerScore = 1)
    ),
    metricLow = "Reserved",
    metricHigh = "Expressive"
)
assertEquals(listOf(0, 1, 2), summary!!.items.map { it.questionIndex })
```
Assert empty input returns null.

- [ ] **Step 2: Run focused test and verify RED**

```bash
./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.ResultEvidenceTest
```
Expected: FAIL because evidence types/engine are missing.

- [ ] **Step 3: Implement minimal deterministic evidence engine**

Normalize answer score 0..3 around the neutral midpoint, rank by absolute contribution descending then `questionIndex` ascending, and return at most three items. Output direction/relative influence data, not pseudo-precise percentages.

- [ ] **Step 4: Wire ephemeral answer contributions into the result flow**

At quiz completion, derive contributions from the answers already held in quiz state and pass them to the result screen/state. Do not add answer-level analytics. Do not require migration of old persisted results.

- [ ] **Step 5: Run focused test and existing quiz/result JVM suite**

```bash
./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.ResultEvidenceTest
./gradlew :app:testDebugUnitTest
```
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main app/src/test
git commit -m "feat: explain result answer evidence"
```

### Task 3: Deterministic profile connections

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/ResultProfileConnection.kt`
- Create: `app/src/test/java/com/whoareyou/app/ResultProfileConnectionTest.kt`
- Modify: result call site to provide existing `GlobalProfileSummary`/dimensions where available
- Test: `app/src/test/java/com/whoareyou/app/ResultProfileConnectionTest.kt`

**Interfaces:**
- Consumes: current `Quiz`, current score, `GlobalProfileSummary` or its dimensions.
- Produces: `ResultProfileConnectionEngine.derive(currentQuiz: Quiz, score: Int, dimensions: List<ProfileDimension>): List<ResultProfileConnection>`.

- [ ] **Step 1: Write failing reinforcement, contrast and insufficient-evidence tests**

Use trait-weight overlap as the evidence boundary. Assert a connection requires another completed dimension with meaningful shared trait evidence; current quiz is excluded; output count is bounded.

- [ ] **Step 2: Run focused test and verify RED**

```bash
./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.ResultProfileConnectionTest
```
Expected: FAIL because the engine does not exist.

- [ ] **Step 3: Implement minimal connection engine**

Compare signed normalized score direction with shared signed trait weights. Classify only clear evidence as `REINFORCING` or `CONTRASTING`; represent near-neutral evidence as contextual nuance only when it passes the minimum evidence threshold. Sort deterministically by evidence strength then quiz ID and cap the list.

- [ ] **Step 4: Add deterministic-output and sparse-profile edge tests**

Run the same inputs repeatedly and assert equality. Assert zero/one insufficient dimension returns an empty list.

- [ ] **Step 5: Run profile and result JVM tests**

```bash
./gradlew :app:testDebugUnitTest --tests 'com.whoareyou.app.*Profile*Test' --tests 'com.whoareyou.app.Result*Test'
```
Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main app/src/test
git commit -m "feat: connect results to profile evidence"
```

### Task 4: Aggregate intelligence and modular Result UI

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/ResultIntelligence.kt`
- Create: `app/src/main/java/com/whoareyou/app/ResultIntelligenceUi.kt`
- Modify: `app/src/main/java/com/whoareyou/app/ResultScreenUi.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fr/strings.xml`
- Modify: `app/src/androidTest/java/com/whoareyou/app/ResultScreenUiTest.kt`

**Interfaces:**
- Consumes: `ResultInsightSummary?`, `ResultEvidenceSummary?`, `List<ResultProfileConnection>`.
- Produces: immutable `ResultIntelligence` and Compose cards `ResultEvidenceCard`, `ResultStrengthsWatchoutsCard`, `ResultEverydayLifeCard`, `ResultProfileConnectionsCard`, `ResultReflectionCard`.

- [ ] **Step 1: Add failing Compose tests for section presence and graceful omission**

Update the test fixture with representative rich content and evidence. Assert tags:
```kotlin
composeRule.onNodeWithTag("result_evidence").assertExists()
composeRule.onNodeWithTag("result_strengths_watchouts").assertExists()
composeRule.onNodeWithTag("result_everyday_life").assertExists()
composeRule.onNodeWithTag("result_reflection").assertExists()
```
Add a second case with no evidence/profile connection and assert those optional tags do not exist while `result_done`, `result_retry`, `result_share` and `result_compare` remain reachable.

- [ ] **Step 2: Run instrumentation compile/test target and verify RED**

Run the repository's existing connected/device test command when an emulator is available; at minimum:
```bash
./gradlew :app:compileDebugAndroidTestKotlin
```
Expected before implementation: compile/test failure for missing UI/model APIs.

- [ ] **Step 3: Implement the aggregate and focused cards**

Keep the score/title/description hero unchanged. Insert rich intelligence immediately after the existing concise interpretation. Render only non-empty sections. Use existing V2 surfaces/type/spacing and heading semantics; do not add perpetual animation.

- [ ] **Step 4: Add EN/FR UI labels with parity**

Add localized labels for why-result, strengths, watch-outs, everyday-life, profile-connections and reflection. Keep authored psychological interpretation in catalog content, not hard-coded Compose branches.

- [ ] **Step 5: Verify ResultScreen action regression**

Run:
```bash
./gradlew :app:compileDebugAndroidTestKotlin
./gradlew :app:testDebugUnitTest
```
Then run the existing device/UI test workflow locally if available. Expected: all existing result actions remain reachable and tests pass.

- [ ] **Step 6: Commit**

```bash
git add app/src/main app/src/androidTest
git commit -m "feat: render result intelligence report"
```

### Task 5: Expand localized content safely across the catalog

**Files:**
- Modify: `app/src/main/assets/quizzes*.json`
- Modify: `app/src/main/assets/quizzes*-fr.json`
- Create or modify: repository catalog-integrity test/tool appropriate to the current validation pattern
- Test: catalog/string integrity validation

**Interfaces:**
- Consumes: optional result-intelligence catalog schema from Task 1.
- Produces: EN/FR authored content for all supported quiz result dimensions.

- [ ] **Step 1: Add failing catalog integrity checks**

Require that every production quiz has all three authored branches needed by M771, each branch respects list limits, required reflection text is non-blank, and EN/FR quiz IDs/content structure match.

- [ ] **Step 2: Run integrity test and verify RED**

Run the repository's catalog integrity command plus the new focused check. Expected: FAIL listing quizzes not yet enriched.

- [ ] **Step 3: Author content in small catalog batches**

For every quiz, write concise non-clinical content for balanced/low/high intensity branches according to the schema. Keep claims framed as tendencies and avoid diagnostic language. Maintain EN/FR structural parity.

- [ ] **Step 4: Run integrity checks after each batch**

Expected: the missing-coverage list monotonically decreases to zero with no schema/parity regression.

- [ ] **Step 5: Run complete catalog and JVM validation**

```bash
./gradlew :app:testDebugUnitTest
```
plus the repository's existing catalog/integrity scripts. Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/assets tools app/src/test
git commit -m "content: enrich all quiz results"
```

### Task 6: Device, accessibility and release verification

**Files:**
- Modify only validation tests/scripts if a genuine M771 coverage gap is found test-first.
- No production behavior change is permitted in this task without a new failing regression test.

**Interfaces:**
- Consumes: completed M771 implementation.
- Produces: fresh verification evidence and RC artifact.

- [ ] **Step 1: Run full JVM suite**

```bash
./gradlew :app:testDebugUnitTest
```
Expected: PASS with no ignored M771 failures.

- [ ] **Step 2: Run repository integrity and Android candidate/release lint gates**

Use the exact commands/workflows documented by the current repository at execution time. Expected: PASS.

- [ ] **Step 3: Build optimized candidate APK/AAB**

Use the current candidate/release build path. Confirm minification remains enabled and the existing WorkManager/R8 fix is preserved.

- [ ] **Step 4: Run device visual/runtime scenarios**

Exercise representative rich result and historic/no-evidence result under:
- normal portrait;
- landscape;
- compact display;
- 130% font;
- reduced motion.

Expected: no clipping of primary content, all cards scroll into view, actions reachable, no crash/ANR.

- [ ] **Step 5: Run fresh release-candidate workflow**

Expected: successful RC artifact containing the M771 branch head. Record workflow/run IDs, artifact name/digest, APK/AAB hashes and package/version metadata.

- [ ] **Step 6: Final commit only if verification tooling required a tested correction**

Do not create a cosmetic “verification” commit when no file changed.

## Self-review

- Spec coverage: evidence, strengths/watch-outs, everyday examples, reflection, profile connections, graceful historic behavior, localization, accessibility, offline/privacy, performance and release gates are mapped to tasks.
- Placeholder scan: implementation decisions required for each task are explicit; repository-specific scoring call-site filenames are intentionally discovered at execution because the plan forbids guessing a stale path, while the exact interface/behavior is specified.
- Type consistency: Task 1 produces `ResultInsightSummary`; Task 2 `ResultEvidenceSummary`; Task 3 `ResultProfileConnection`; Task 4 aggregates them in `ResultIntelligence`.
- Review Focus cases are explicitly assigned to Tasks 1–4 and Task 6.
- Scope remains M771 only; M772 retention and M773 compatibility are excluded.
