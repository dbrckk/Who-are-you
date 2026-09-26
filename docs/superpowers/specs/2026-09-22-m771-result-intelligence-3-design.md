# M771 — Result Intelligence 3.0 Design

**Date:** 2026-09-22  
**Status:** design approved in chat; awaiting written-spec review  
**Repository:** `dbrckk/Who-are-you`

## Goal

Make every completed quiz feel substantially more useful by turning the existing score/result into a richer, explainable, deterministic self-reflection report, while preserving the app's offline-first architecture and explicitly non-clinical positioning.

## Product intent

The first result is the app's highest-value moment. M771 should increase perceived depth without increasing quiz length, requiring an account, adding a network dependency, or pretending that short entertainment/self-reflection quizzes provide psychological diagnosis.

Success means a user can understand:
1. what the result says;
2. why the result leaned that way;
3. what strengths and watch-outs may accompany that tendency;
4. how it may show up in ordinary life;
5. one useful reflection/action;
6. how it relates to evidence already present in their evolving profile.

## Scope

### 1. Explainable result evidence

Add a deterministic “Why this result?” model derived from the completed quiz's answer contributions.

The explanation must:
- identify a small number of the strongest contributing answer patterns;
- describe direction and relative contribution in plain language;
- avoid pseudo-precision beyond the existing score;
- never expose internal implementation jargon;
- remain useful for balanced/near-neutral scores;
- work offline.

Raw answer text or answer-level sensitive data must not be sent through telemetry.

### 2. Strengths and watch-outs

For each supported result dimension, produce structured content for:
- up to three likely strengths;
- two or three watch-outs/nuances;
- intensity-aware wording so balanced, clear and strong signals do not read identically.

Copy must use tendency language such as “you may”, “you often”, or equivalent natural French/English phrasing. It must not present destiny, diagnosis, pathology, intelligence, psychiatric assessment or clinical conclusions.

### 3. Everyday-life interpretation

Provide concise contextual examples relevant to the quiz dimension. Where content supports it, examples may cover:
- decisions;
- relationships/communication;
- work/study;
- social situations;
- stress or change.

The UI should not display empty categories merely to satisfy a fixed template.

### 4. Reflection/action prompt

Each result receives one concrete, lightweight reflection or experiment. It must be non-medical and non-therapeutic. The action should help the user inspect the tendency in real life rather than instruct them to change personality.

### 5. Profile connections

When sufficient prior profile evidence exists, derive a small number of deterministic connections between the current result and already measured dimensions.

Connections may describe:
- reinforcing signals;
- meaningful contrasts;
- near-neutral/context-sensitive dimensions.

No connection is shown when evidence is insufficient. Contradictory evidence is represented as nuance rather than silently averaged away.

### 6. Result hierarchy

Keep the existing fast first viewport:
1. result identity/title;
2. score and spectrum;
3. concise core explanation.

The richer intelligence follows below it:
4. why this result;
5. strengths/watch-outs;
6. everyday-life examples;
7. profile connections when available;
8. reflection/action;
9. evolution/retake information;
10. next exploration;
11. share/compare and disclaimer.

Existing result completion, retry, recommendation, share, friend challenge, ads and profile persistence behavior must remain intact.

## Architecture

### Pure domain layer

Introduce focused deterministic domain models rather than expanding Compose logic.

Proposed responsibilities:
- `ResultEvidenceEngine`: converts scored answer contributions into a compact explanation model.
- `ResultInsightEngine`: selects intensity-aware strengths, watch-outs, everyday-life insights and reflection prompts from authored quiz/result content.
- `ResultProfileConnectionEngine`: compares the current result with known profile evidence and returns only meaningful connections.
- `ResultIntelligence`: immutable aggregate consumed by the UI.

Engines remain pure Kotlin and receive all required inputs explicitly. They must not depend on Android UI classes, networking, analytics, billing or ads.

### Content

Result-intelligence content should be authored/versioned with the existing quiz/content model or a tightly scoped companion content model. French and English must have parity.

Do not generate interpretation text remotely at runtime. M771 must remain deterministic and offline-first.

### UI

Create focused Compose components rather than making `ResultScreenUi.kt` substantially larger:
- `ResultEvidenceCard`
- `ResultStrengthsWatchoutsCard`
- `ResultEverydayLifeCard`
- `ResultProfileConnectionsCard`
- `ResultReflectionCard`

`ResultScreen` composes these components from a prepared `ResultIntelligence` model.

## Data flow

1. User answers the existing quiz.
2. Existing scoring remains authoritative for the 0–100 result.
3. Answer contribution data is passed to `ResultEvidenceEngine`.
4. Score/intensity + authored content are passed to `ResultInsightEngine`.
5. Current result + existing local profile evidence are passed to `ResultProfileConnectionEngine`.
6. Outputs are assembled into `ResultIntelligence`.
7. Compose renders only non-empty sections.
8. Existing persistence/profile update and navigation behavior continues unchanged.

## Privacy and safety

- Offline-first; no new server is required.
- No raw quiz answers in telemetry.
- No free-text psychological data collection.
- No medical, psychiatric, IQ or diagnostic claims.
- Profile connections are framed as observed quiz evidence, not facts about the person.
- Existing disclaimer remains and richer copy follows the same non-clinical rule.

## Accessibility and responsive behavior

All new sections must:
- preserve logical TalkBack traversal;
- use headings/semantics where useful;
- not encode meaning only through color;
- support at least the repository's existing large-font validation, including 130% scenarios, without clipped primary content;
- remain usable in compact width and landscape;
- respect reduced-motion mode;
- avoid perpetual animation.

## Performance

M771 must not add a runtime network dependency. Engines should operate on small in-memory models and avoid expensive recomputation during Compose recomposition. Derived intelligence should be calculated outside or memoized at stable boundaries.

No new uncontrolled background work is allowed.

## Testing

Follow test-first development.

### JVM/domain tests

Cover at minimum:
- strong high-direction evidence;
- strong low-direction evidence;
- balanced result;
- contribution ranking/ties;
- missing/partial contribution data;
- intensity-aware content selection;
- profile reinforcement;
- profile contrast;
- insufficient profile evidence;
- deterministic output for identical inputs;
- FR/EN content parity/integrity.

### UI/device validation

Extend existing result validation to verify:
- intelligence sections appear for a representative result;
- optional profile connections disappear when evidence is insufficient;
- scrolling reaches all content;
- large font;
- compact display;
- landscape;
- reduced motion;
- no crash/ANR;
- existing Done/Retry/Share/Compare/Next Quiz actions remain reachable.

### Release gates

M771 is complete only when current repository quality gates are green:
- catalog/repository integrity;
- JVM tests;
- instrumentation compile/tests used by the project;
- candidate/release lint;
- optimized candidate APK;
- device visual/runtime validation;
- fresh release-candidate artifact.

## Out of scope

M771 does not include:
- weekly/daily retention synthesis (M772);
- notification scheduling (M772);
- multi-profile friend compatibility redesign (M773);
- accounts/cloud sync;
- remote LLM-generated interpretations;
- new clinical/validated psychometric claims;
- a general visual redesign of the app.

## Migration and compatibility

Existing persisted quiz results must remain readable. New intelligence should be derivable from current result/profile data where possible. If historic sessions lack answer-contribution detail, the UI gracefully omits “Why this result?” rather than invalidating or fabricating evidence.

No existing quiz ID or scoring contract should change merely to support M771.

## Definition of done

M771 is done when:
- richer intelligence is available for supported EN/FR results;
- explanations are deterministic and evidence-aware;
- result sections are modular and accessible;
- historic/partial data degrades gracefully;
- existing result actions and monetization behavior are preserved;
- tests cover the pure engines and critical UI behavior;
- all current quality gates are green;
- a fresh installable RC APK is produced.
