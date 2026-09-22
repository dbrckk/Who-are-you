# M771 Result Intelligence 3.0 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make every quiz result more useful and explainable without changing the existing quiz score, by preserving answer-choice evidence and deriving deterministic strengths, watch-outs, everyday context and one reflection/action prompt.

**Architecture:** Keep scoring untouched. Record only the selected answer index for each question in the in-memory/saveable quiz attempt, then feed the completed answer trace to a new pure `ResultIntelligenceEngine`. The engine produces structured, non-clinical result insight data; a dedicated Compose panel renders it in ResultScreen using localized EN/FR copy.

**Tech Stack:** Kotlin, Jetpack Compose, JUnit4, Android string resources, existing GitHub Actions Android CI.

**Spec:** User-approved M771 scope in the 2026-09-22 conversation: Result Intelligence 3.0 with real “why this result?” evidence, strengths, watch-outs, everyday-life context and a concrete next action, while preserving deterministic scoring and non-clinical framing.

## Global Constraints

- Do not modify `Scoring.quizPercent` or answer score values.
- No network dependency and no generative/LLM runtime dependency.
- Do not persist raw answer text outside the current quiz/result flow.
- Preserve process-resumable quiz state through `rememberSaveable`.
- English and French UI copy must remain in parity.
- Existing result sharing, friend challenge, ads, profile progress, retake and next-exploration behavior must remain intact.
- Result claims are tendencies for self-reflection, never diagnoses.
- Reduced-motion/accessibility/release safeguards remain unchanged.

## Review Focus

1. Interrupted quiz/process recreation: selected answer indexes must survive with the existing quiz state and not desynchronize from question index.
2. Final answer: the last selected choice must be included before ResultScreen renders.
3. Invalid/missing answer indexes: the insight engine must ignore unusable entries rather than crash.
4. Neutral scores: copy must avoid falsely presenting a strong pole.
5. Localization: all new user-facing copy must exist in both EN and FR resources.

---

### Task 1: Pure result intelligence engine

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/ResultIntelligence.kt`
- Create: `app/src/test/java/com/whoareyou/app/ResultIntelligenceTest.kt`

**Interfaces:**
- Consumes: `Quiz`, `Question`, `Answer`, `ResultInterpretationEngine.derive(score)`
- Produces: `ResultIntelligenceEngine.derive(quiz, score, selectedAnswerIndexes): ResultIntelligenceSummary`

- [ ] **Step 1: Write failing tests**
  - verifies strongest answer evidence comes from selected extreme answers;
  - verifies invalid/missing indexes are ignored;
  - verifies neutral result produces balanced framing;
  - verifies low/high result produces deterministic strength/watch-out/action keys.

- [ ] **Step 2: Run test to verify RED**
  Run: `./gradlew :app:testDebugUnitTest --tests com.whoareyou.app.ResultIntelligenceTest`
  Expected: FAIL because `ResultIntelligenceEngine` does not exist.

- [ ] **Step 3: Implement minimal pure engine**
  The engine returns structured enum/key data and evidence records, not localized strings.

- [ ] **Step 4: Run focused test GREEN**
  Run the same command.
  Expected: PASS.

### Task 2: Preserve answer evidence through quiz navigation

**Files:**
- Modify: `app/src/main/java/com/whoareyou/app/QuizScreenUi.kt`
- Modify: `app/src/main/java/com/whoareyou/app/MainActivity.kt`
- Test: existing JVM/UI compilation and Android CI.

**Interfaces:**
- Consumes: answer index from each `V2PressableSurface` click.
- Produces: ordered saveable `quizAnswerIndexes` supplied to ResultScreen.

- [ ] **Step 1: Add compile-contract test/usage first**
  Update ResultScreen call contract only after the new argument is required so compilation catches missing plumbing.

- [ ] **Step 2: Verify RED**
  Run Android/JVM compile gate; expected missing callback/argument compile failure.

- [ ] **Step 3: Wire selected answer index without changing scoring**
  Extend quiz callbacks with selected answer index, append one index per answered question, clear on new/retry attempts, and include the final answer before result commit/navigation.

- [ ] **Step 4: Verify GREEN**
  Run unit suite + Kotlin/Android compile gate.

### Task 3: Result Intelligence UI and localization

**Files:**
- Create: `app/src/main/java/com/whoareyou/app/ResultIntelligenceUi.kt`
- Modify: `app/src/main/java/com/whoareyou/app/ResultScreenUi.kt`
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values-fr/strings.xml`

**Interfaces:**
- Consumes: `ResultIntelligenceSummary`, `Quiz`.
- Produces: accessible “Why this result?”, strengths, watch-outs, everyday-life and reflection/action panels inside ResultScreen.

- [ ] **Step 1: Add UI contract expecting answer indexes in ResultScreen**
- [ ] **Step 2: Verify RED compile**
- [ ] **Step 3: Implement panel + EN/FR strings**
- [ ] **Step 4: Run full JVM tests, lint/build CI and device validation as available**
- [ ] **Step 5: Open PR only after branch verification is green**

## Completion contract

M771 is complete only when:
- Result scoring output is unchanged.
- Result Intelligence tests were observed RED then GREEN.
- Completed answer evidence includes the final answer.
- Invalid evidence cannot crash ResultScreen.
- EN/FR strings are present.
- Existing Android CI passes on the M771 PR.
- No existing release/device gate is weakened.
