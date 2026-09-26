# M773 — “Qui suis-je ?” Living Portrait — Design

Date: 2026-09-23  
Branch: `m773-who-am-i`  
Depends on: M772 Personal Model & Confidence Engine

## Objective

Turn the existing Profile experience into the app’s central “Who am I?” portrait: a concise, explainable view of what the app currently knows about the person, how certain that knowledge is, what appears stable or variable, what remains unknown, and how the portrait evolves.

The portrait is not a diagnosis or a fixed identity label. Sparse evidence must produce uncertainty, not stronger prose.

## Product principles

1. **Evidence before conclusion.** The UI reflects `PersonalModel`; it does not invent a second inference system.
2. **Uncertainty is visible.** Exploring, likely and established knowledge must remain distinguishable.
3. **A living portrait, not a verdict.** Wording describes tendencies and current evidence.
4. **Progressive disclosure.** The first viewport gives a useful synthesis; detailed evidence and existing profile tools remain available below.
5. **Preserve useful existing capabilities.** Sharing, friend comparison, next-quiz recommendation, coverage, knowledge map, trends, narrative and local-data reset remain functional.
6. **Offline and private.** No remote model, raw-answer telemetry or new personal-data source.
7. **Bilingual and accessible.** EN/FR parity, TalkBack semantics, large-font/compact layouts and reduced-motion behavior are required.

## Architecture

M773 does not add a competing profile engine.

```
Quiz/history
    ↓
TraitGraph + Coverage + Timelines
    ↓
PersonalModel (M772)
    ↓
WhoAmI presentation mapper
    ↓
ProfileScreen / modular portrait cards
```

The presentation layer may derive ordering, labels and display groups from `PersonalModel`, but must not recalculate confidence, certainty, stability or contradiction severity.

### Presentation model

Introduce a small pure presentation contract such as:

```kotlin
data class WhoAmIPortrait(
    val headlineTraits: List<PersonalTrait>,
    val stableTraits: List<PersonalTrait>,
    val nuancedTraits: List<PersonalTrait>,
    val discoveryGaps: List<TraitDomainCoverage>,
    val evolvingTraits: List<PersonalTrait>
)
```

A pure `WhoAmIPortraitEngine` maps `PersonalModel` to these display groups deterministically. This keeps Compose simple and makes the screen behavior testable without UI instrumentation.

No user-facing strings belong in the domain/presentation engine.

## Information hierarchy

The existing Profile route remains the entry point. M773 reorganizes it rather than creating a parallel navigation destination.

### 1. “Who am I?” / “Qui suis-je ?”

The top of the screen becomes a portrait summary rather than leading with social actions.

It includes:
- current portrait heading;
- up to three strongest meaningful traits;
- a compact statement of how well the portrait is documented;
- visible uncertainty when evidence is sparse.

Headline traits must be `LIKELY` or `ESTABLISHED` and distinctive. If none qualify, the screen explicitly says the portrait is still being discovered.

### 2. “What seems stable” / “Ce qui semble stable”

Shows traits whose stability is `STABLE` and certainty is at least `LIKELY`.

Each item communicates:
- trait label;
- direction/intensity;
- certainty label;
- enough supporting context to avoid presenting a bare score.

No stable traits => omit the card or show a restrained discovery state when that state improves comprehension.

### 3. “Your nuances” / “Tes nuances”

This is where the portrait avoids becoming a simplistic label.

It can include:
- contradictory traits from `contradictoryTraits`;
- variable traits from `variableTraits`;
- well-documented near-balanced traits where useful.

Wording uses “varies”, “depends on context”, “mixed signals” or equivalent. It must not claim a psychological paradox, disorder or hidden cause.

Cross-trait paradox detection is explicitly out of scope for M773.

### 4. “Still discovering” / “Ce que nous découvrons encore”

Uses `knowledgeGaps`, `unknownTraitIds` and the existing next-quiz recommendation.

The section explains that missing information lowers confidence and offers the most useful next quiz when available.

Unknowns are not weaknesses.

### 5. “Your evolution” / “Ton évolution”

Uses the existing trait evolution, longitudinal trends and narrative infrastructure.

The section prioritizes meaningful movement and variability. Historical profiles without timed history degrade gracefully.

The existing detailed trend/narrative cards can remain below the new summary rather than being duplicated.

## Existing Profile content

The current screen contains valuable secondary tools. M773 changes their order:

1. Who Am I portrait header
2. stable traits
3. nuances
4. discovery gaps + next quiz
5. evolution summary
6. existing detailed profile/knowledge/trend content
7. optional social actions (share / compare)
8. reset/local-data controls

Social actions should no longer dominate the top of the identity experience.

## Components

Prefer focused files/components instead of further expanding `ProfileScreenUi.kt`:

- `WhoAmIPortrait.kt` — pure presentation mapping
- `WhoAmIPortraitUi.kt` — portrait/header
- `WhoAmIStableTraitsUi.kt`
- `WhoAmINuancesUi.kt`
- `WhoAmIDiscoveryUi.kt`
- `WhoAmIEvolutionUi.kt`

Existing reusable profile components remain unchanged where possible.

## Trait labels and copy

Trait IDs must never be shown raw to users.

Reuse existing localized trait/domain naming infrastructure where available. Add EN/FR resources only for new headings, certainty labels, empty states and explanatory copy.

Certainty language:
- `EXPLORING`: “À explorer” / “Exploring”
- `LIKELY`: “Probable” / “Likely”
- `ESTABLISHED`: “Bien établi” / “Well established”

`UNKNOWN` should normally appear as missing knowledge rather than a badge attached to a conclusion.

Scores remain implementation detail unless an existing visualization genuinely benefits from them. Prefer understandable language over “78/100”.

## Empty and legacy states

- No completed quizzes: discovery-oriented portrait with next action; no fake identity summary.
- One source: never presented as established.
- No timed history: evolution section does not invent stability.
- No distinctive traits: balanced/insufficient portrait state.
- Missing M772 data in legacy paths: `PersonalModel.EMPTY` remains safe.
- Missing catalog label: use a safe localized generic fallback, not the raw internal ID.

## Accessibility and responsive behavior

- headings use semantic heading roles;
- trait rows expose a concise merged TalkBack description;
- interactive rows meet touch-target requirements;
- no meaning conveyed only by color;
- font scale >=130% remains readable without clipped badges;
- compact/landscape layouts stack rather than compress horizontally;
- reduced motion continues to use existing behavior.

## Privacy and safety

M773 adds no new collection or persistence.

Do not:
- diagnose psychiatric/medical conditions;
- estimate IQ;
- claim addiction, pathology or hidden causes;
- imply certainty beyond `PersonalModel`;
- claim causality from trends;
- send profile data to remote inference services;
- use profile conclusions for advertising.

## Testing

### Pure presentation tests

Required cases:
1. established distinctive traits become portrait headlines;
2. exploring traits do not become headline conclusions;
3. stable section requires both sufficient certainty and stable history;
4. variable/contradictory evidence goes to nuances;
5. knowledge gaps are deterministic;
6. no data produces a discovery state;
7. input ordering does not change semantic output;
8. headline list is capped;
9. raw trait IDs are not required as user-facing labels;
10. legacy `PersonalModel.EMPTY` is safe.

### Compose/UI tests

Verify:
- five-section order when data supports all sections;
- sparse profile does not show false stable/evolution claims;
- next quiz remains actionable;
- share/compare remain available but secondary;
- EN/FR resources exist;
- accessibility semantics exist for headings and trait summaries;
- compact/large-font layout renders without missing core actions.

### Regression gates

Retain existing repository gates:
- catalog/repository integrity;
- JVM unit tests;
- instrumentation compile;
- lint;
- candidate/debug build as applicable;
- M59 device validation at exact HEAD;
- inspect failing logs rather than weakening gates.

## Out of scope

M773 does not add:
- device behavioral data (M774);
- values/motivation inference;
- new questionnaires solely for context;
- cross-trait paradox engine;
- remote generative summaries;
- clinical labels;
- a second profile navigation destination;
- a rewrite of scoring or M772 confidence rules.

## Definition of done

M773 is complete when:
- the existing Profile entry presents the five-part “Who am I?” hierarchy;
- all conclusions are traceable to M772 outputs;
- sparse/legacy states remain honest and useful;
- existing profile, recommendation, share/compare and reset capabilities remain reachable;
- EN/FR and accessibility requirements are covered;
- pure mapper and UI/regression tests are green;
- Android CI and device validation are green at the exact reviewed HEAD;
- the work remains on a draft PR until explicit merge authorization.
