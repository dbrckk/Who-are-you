package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorUiModelTest {
    @Test
    fun `disabled sources produce opt in actions without fabricated metrics`() {
        val model = BehaviorUiModelFactory.build(
            BehaviorSnapshot.EMPTY.copy(
                sourceStates = mapOf(
                    BehaviorSource.ACTIVITY to BehaviorSourceState.DISABLED,
                    BehaviorSource.APP_USAGE to BehaviorSourceState.DISABLED
                )
            )
        )

        assertEquals(2, model.sources.size)
        assertTrue(model.sources.all { it.action == BehaviorSourceAction.ENABLE })
        assertTrue(model.today.metrics.isEmpty())
    }

    @Test
    fun `sources expose privacy preserving permission explanations`() {
        val model = BehaviorUiModelFactory.build(BehaviorSnapshot.EMPTY)

        assertEquals(
            BehaviorCopyKey.SOURCE_ACTIVITY_DETAIL,
            model.sources.first { it.source == BehaviorSource.ACTIVITY }.detail
        )
        assertEquals(
            BehaviorCopyKey.SOURCE_APP_USAGE_DETAIL,
            model.sources.first { it.source == BehaviorSource.APP_USAGE }.detail
        )
    }

    @Test
    fun `permission required produces authorization action`() {
        val model = BehaviorUiModelFactory.build(
            BehaviorSnapshot.EMPTY.copy(
                sourceStates = mapOf(
                    BehaviorSource.ACTIVITY to BehaviorSourceState.PERMISSION_REQUIRED,
                    BehaviorSource.APP_USAGE to BehaviorSourceState.UNSUPPORTED
                )
            )
        )

        assertEquals(
            BehaviorSourceAction.AUTHORIZE,
            model.sources.first { it.source == BehaviorSource.ACTIVITY }.action
        )
        assertEquals(
            BehaviorSourceAction.NONE,
            model.sources.first { it.source == BehaviorSource.APP_USAGE }.action
        )
    }

    @Test
    fun `partial today data only exposes measured values`() {
        val today = DailyBehaviorAggregate(
            epochDay = 1L,
            steps = 4200L,
            totalForegroundMillis = null,
            topApps = emptyList(),
            launchesOrSessions = null,
            daypartUsage = DaypartUsage.EMPTY
        )
        val model = BehaviorUiModelFactory.build(
            BehaviorSnapshot(
                today = today,
                last7Days = listOf(today),
                last30Days = listOf(today),
                sourceStates = mapOf(
                    BehaviorSource.ACTIVITY to BehaviorSourceState.AVAILABLE,
                    BehaviorSource.APP_USAGE to BehaviorSourceState.PERMISSION_REQUIRED
                ),
                insights = emptyList()
            )
        )

        assertEquals(listOf(BehaviorMetricKind.STEPS), model.today.metrics.map { it.kind })
        assertFalse(model.today.metrics.any { it.kind == BehaviorMetricKind.SCREEN_TIME })
    }

    @Test
    fun `sufficient history exposes measured summaries and deterministic insight rows`() {
        val days = (1L..7L).map { epoch ->
            DailyBehaviorAggregate(
                epochDay = epoch,
                steps = 5000L + epoch,
                totalForegroundMillis = 60_000L * epoch,
                topApps = listOf(AppUsageAggregate("example.app", 30_000L * epoch, 1)),
                launchesOrSessions = epoch.toInt(),
                daypartUsage = DaypartUsage(0L, 0L, 0L, 60_000L * epoch)
            )
        }
        val snapshot = BehaviorSnapshot(
            today = days.last(),
            last7Days = days,
            last30Days = days,
            sourceStates = BehaviorSource.entries.associateWith { BehaviorSourceState.AVAILABLE },
            insights = listOf(
                BehaviorInsight(
                    category = BehaviorInsightCategory.LATE_USAGE_PATTERN,
                    evidenceTier = BehaviorEvidenceTier.DEVELOPING,
                    supportingValues = listOf(42L),
                    copyTokens = listOf("late_usage_pattern")
                )
            )
        )

        val model = BehaviorUiModelFactory.build(snapshot)

        assertTrue(model.last7Days.metrics.any { it.kind == BehaviorMetricKind.AVERAGE_STEPS })
        assertTrue(model.last7Days.metrics.any { it.kind == BehaviorMetricKind.AVERAGE_SCREEN_TIME })
        assertEquals(BehaviorInsightCategory.LATE_USAGE_PATTERN, model.patterns.single().category)
    }

    @Test
    fun `presentation vocabulary never uses clinical personality or moral labels`() {
        val forbidden = listOf(
            "diagnosis", "diagnostic", "addiction", "addicted", "personality",
            "depression", "anxiety", "lazy", "good", "bad", "healthy", "unhealthy"
        )

        val vocabulary = BehaviorCopyKey.entries.joinToString(" ") { it.name.lowercase() }

        forbidden.forEach { word ->
            assertFalse("forbidden word: $word", vocabulary.contains(word))
        }
    }

    @Test
    fun `late usage pattern exposes a separate neutral suggestion`() {
        val model = BehaviorUiModelFactory.build(
            BehaviorSnapshot.EMPTY.copy(
                insights = listOf(
                    BehaviorInsight(
                        category = BehaviorInsightCategory.LATE_USAGE_PATTERN,
                        evidenceTier = BehaviorEvidenceTier.DEVELOPING
                    )
                )
            )
        )

        assertEquals(
            listOf(BehaviorCopyKey.SUGGESTION_REDUCE_EVENING_USE),
            model.suggestions.map { it.copy }
        )
    }

    @Test
    fun `insufficient history does not fabricate a suggestion`() {
        val model = BehaviorUiModelFactory.build(
            BehaviorSnapshot.EMPTY.copy(
                insights = listOf(
                    BehaviorInsight(
                        category = BehaviorInsightCategory.INSUFFICIENT_HISTORY,
                        evidenceTier = BehaviorEvidenceTier.EARLY
                    )
                )
            )
        )

        assertTrue(model.suggestions.isEmpty())
    }

    @Test
    fun `local only disclosure is always present`() {
        val model = BehaviorUiModelFactory.build(BehaviorSnapshot.EMPTY)
        assertEquals(BehaviorCopyKey.LOCAL_ONLY_BODY, model.privacyCopy)
    }
}
