# Who Are You — Product benchmark and Play Store improvement specification

Status: implementation roadmap for the first public Play Store releases.
Date: 2026-09-14

## 1. Product objective

Build a personality/self-discovery application that is fast enough to feel native, deep enough to reward repeated use, understandable without psychological jargon, useful without requiring an account, and social without becoming a dating network.

The product should combine:
- the immediate clarity and polished result storytelling of 16Personalities;
- the breadth, trait graph and repeat-use depth of Dimensional;
- the compatibility/share loop demonstrated by Boo and Dimensional;
- stronger privacy, offline-first operation, French localization and lightweight Android performance as differentiators.

The app must not claim clinical diagnosis, IQ measurement, psychiatric assessment or scientific validation unless supported by an appropriate validated instrument and evidence.

## 2. Competitive benchmark

### 16Personalities
Observed strengths:
- extremely simple promise: a short test followed by a recognizable identity;
- strong result narrative and type descriptions;
- relationship, career and personal-growth extensions;
- team assessments and visual reports;
- very large brand awareness.

Opportunity for Who Are You:
- do not copy the 16-type model;
- offer multiple independent dimensions instead of forcing one permanent label;
- make French a first-class app language;
- expose evolution over time and cross-test synthesis.

### Dimensional
Observed strengths:
- 200+ traits across many personality/life dimensions;
- persistent personality fingerprint;
- friend comparison and compatibility;
- content that remains useful after the initial test;
- social/peer feedback and broad trait library.

Opportunity for Who Are You:
- this is the closest product benchmark;
- deepen the global profile without making onboarding heavy;
- add a progressive trait graph, insight feed and meaningful retakes;
- keep core self-discovery available offline and with less data collection.

### Boo
Observed strengths:
- personality is connected to an immediate social purpose;
- compatibility is actionable rather than a static score;
- strengths, weaknesses, conflicts, love language and relationship-oriented explanations create shareable/useful outputs.

Opportunity for Who Are You:
- keep compatibility without turning the app into dating;
- use share links / friend comparison as a viral loop;
- explain WHY two profiles are similar or complementary rather than showing only a percentage.

## 3. Target positioning

Primary promise:
"Discover the different dimensions of who you are — and see how they evolve."

Core differentiation:
1. Multi-dimensional identity rather than one immutable personality label.
2. Progressive profile built from short focused tests.
3. Evolution/history after retakes.
4. Private/offline-first core.
5. Friend compatibility without a public social graph.
6. Strong visual identity and share cards.
7. Native French + English from launch-quality content.

## 4. P0 — required before / immediately around public Play launch

### 4.1 First-session activation
- User reaches first meaningful result in <= 3 minutes.
- Onboarding <= 3 screens or one concise screen.
- Explain: what the app does, approximate duration, privacy/offline behavior.
- Never require account creation for the core flow.
- Resume an interrupted quiz exactly where the user stopped.
- Result must contain: score, two poles, plain-language interpretation, nuance, one actionable insight, next recommended test.
- Add "Why this result?" explaining which answer patterns influenced the dimension without exposing misleading pseudo-precision.

Acceptance:
- >= 90% of local automated onboarding-to-result journeys complete.
- no blocking network dependency.
- TalkBack traversal works end-to-end.
- 200% font scaling does not hide primary actions.

### 4.2 Discover information architecture
Sections, in order:
1. Continue / recommended next action.
2. Identity snapshot.
3. Recommended test.
4. Explore dimensions.
5. Collections.
6. Progress/evolution.
7. Library.
8. Optional premium/social actions.

Requirements:
- no perpetual animation while Discover is idle;
- stable lazy keys/content types;
- avoid duplicate calls-to-action;
- completed tests visually distinct;
- search/filter only if catalog size makes it useful.

### 4.3 Profile 2.0
Create a real personality fingerprint:
- overall Identity Aura;
- strongest dimensions;
- trait/dimension map;
- completed vs undiscovered;
- confidence/context disclaimer;
- evolution timeline;
- retake history;
- profile synthesis generated deterministically from known dimensions;
- no unsupported clinical conclusions.

Add radar/spider visualization only if it remains accessible with a text/table equivalent.

### 4.4 Result 2.0
Result hierarchy:
1. identity/result name;
2. score + spectrum;
3. concise explanation;
4. strengths / watch-outs;
5. "in everyday life" examples;
6. evolution since previous attempt;
7. related dimensions;
8. recommended next test;
9. compare/share;
10. disclaimer.

The first viewport must provide the core result without scrolling through promotional content.

### 4.5 Quality and Play readiness
Release gates:
- zero known P0/P1 crash;
- Android Vitals internal target: user-perceived crash < 0.75%, ANR < 0.30%;
- AAB <= 30 MiB target, hard CI failure > 50 MiB;
- Macrobenchmark startup + Discover scroll + quiz-to-result + Result scroll;
- physical-device performance review;
- accessibility review;
- privacy/data-safety review;
- Play pre-launch report reviewed before Production.

## 5. P1 — first major post-launch releases

### 5.1 Trait graph
Introduce a normalized internal trait model:
Quiz answer -> dimension score -> trait evidence -> profile trait.

Requirements:
- traits can receive evidence from multiple tests;
- provenance is retained;
- contradictory evidence is represented rather than silently averaged;
- user can inspect which completed tests contributed;
- deterministic versioned scoring.

### 5.2 Daily/weekly insight loop
Do not use meaningless streak pressure.

Provide:
- one contextual insight after enough evidence exists;
- weekly profile recap;
- suggested retake only after a sensible interval;
- "what changed?" after retake;
- optional local notifications, off by default until explicit consent.

### 5.3 Friend comparison v2
Current challenge links evolve into:
- compare two compatible quiz dimensions;
- similarity and complementarity separated;
- strongest alignment;
- biggest difference;
- potential communication friction;
- concrete discussion prompt;
- shareable private link;
- no public profile required.

### 5.4 Content depth
For important dimensions, author:
- overview;
- strengths;
- blind spots;
- communication;
- stress tendencies;
- relationships;
- work/study;
- growth suggestions;
- common misconceptions.

Every statement should be framed as a tendency, not destiny.

### 5.5 Localization
- French and English content parity.
- No untranslated fallback in production UI.
- locale QA screenshots.
- language selector in Settings independent of system language if feasible.
- copy review for natural French, not literal translation.

## 6. P2 — differentiation after product-market signal

### 6.1 Private circles
Small opt-in circles for friends/couples/family:
- invite by link/code;
- no discoverable public directory;
- aggregate compatibility;
- compare selected dimensions;
- revoke access.

### 6.2 Peer perception / 360 feedback
Inspired by the useful part of peer assessment:
- user chooses a trait;
- trusted friend can privately rate how strongly it appears;
- clearly distinguish self-perception from peer perception;
- never silently merge peer feedback into the user's score.

### 6.3 Guided growth
Turn results into optional experiments:
- communication challenge;
- reflection prompt;
- habit suggestion;
- follow-up after several days;
- user marks useful/not useful.

No medical or therapeutic treatment claims.

### 6.4 Export
- polished profile image;
- PDF profile report;
- privacy-safe share card;
- user-controlled local data export.

## 7. Monetization

Principle: result comprehension must not be held hostage after a user completes a test.

Free:
- core tests;
- core results;
- profile;
- basic evolution;
- basic friend comparison;
- local history.

Premium candidate:
- advanced synthesis;
- deeper reports;
- advanced compatibility;
- richer history/evolution;
- premium collections;
- export customization.

Avoid:
- interstitial immediately after every result;
- fake scarcity;
- blocking the first meaningful result;
- subscription dark patterns.

Current interstitial strategy should remain frequency-capped and demand-loaded.

## 8. Measurement plan

North-star candidate:
Meaningful profile discoveries per retained user per month.

Activation:
- onboarding_started -> first_quiz_started;
- first_quiz_started -> first_result;
- time-to-first-result.

Engagement:
- tests completed D1/D7/D30;
- profile opens;
- next-test conversion from Result;
- retake rate after meaningful interval;
- share/compare initiation and completion.

Quality:
- crash/ANR;
- cold/warm startup;
- frameOverrunMs P95/P99;
- AAB size;
- battery regressions;
- accessibility failures.

Monetization:
- premium impression -> purchase;
- restore success;
- ad impressions per active user;
- retention segmented by monetization exposure.

Privacy rule:
analytics events should not contain raw answers or sensitive free text.

## 9. Experiment backlog

A/B only after enough traffic:
- onboarding single-screen vs progressive;
- Result CTA: "Continue discovering" vs named recommended quiz;
- profile snapshot placement;
- compatibility CTA wording;
- premium timing after second/third meaningful discovery.

Never A/B:
- privacy consent clarity;
- accessibility;
- misleading psychological claims;
- hidden prices or purchase mechanics.

## 10. Technical architecture requirements

- offline-first quiz catalog;
- versioned quiz/scoring schema;
- process cache for immutable catalog;
- Compose stable keys/content types;
- no infinite decorative animation on feed/list surfaces;
- expensive drawing through drawWithCache where appropriate;
- CPU image rendering on Dispatchers.Default;
- file/network I/O on Dispatchers.IO;
- strict release CI;
- Baseline Profile + Macrobenchmark;
- migration tests for persisted results;
- deterministic profile synthesis;
- feature flags only when fail-safe defaults exist.

## 11. Delivery order

Phase A — launch hardening:
performance, accessibility, resume flow, Result 2.0 hierarchy, Profile 2.0 foundation, localization QA, Play gates.

Phase B — retention:
trait graph, evolution, weekly insight, better recommendations, content depth.

Phase C — organic growth:
friend comparison v2, private share links, richer share cards.

Phase D — premium:
advanced synthesis/reports/history/compatibility, after free value is proven.

Phase E — network features:
private circles and peer perception only after privacy/security design review.

## 12. Definition of done for a public-quality feature

A feature is complete only when:
- product behavior and empty/error/offline states are specified;
- accessibility semantics exist;
- French and English strings exist;
- analytics are privacy-safe;
- unit/contract/UI tests cover critical behavior;
- no new uncontrolled background work is introduced;
- performance impact is measured for hot paths;
- release notes/user-facing copy are ready where applicable.
