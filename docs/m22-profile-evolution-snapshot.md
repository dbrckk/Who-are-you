# M22 — Profile evolution snapshot

The full profile now turns persistent retake history into a compact evolution snapshot.

## Behavior

- The snapshot is hidden until at least one dimension has a real previous score.
- Coverage reports how many currently discovered dimensions have retake history.
- The most-changed dimension is selected by largest absolute recent delta.
- Equal deltas preserve catalog/profile dimension order.
- An unchanged retake still counts as tracked history.
- Higher and lower values are described neutrally; no direction is treated as better.
- Signature and archetype calculations remain based on current scores only.

## Validation

`ProfileEvolutionSummaryTest` covers no-history, coverage, largest-change selection, stable ties, and unchanged retakes.
