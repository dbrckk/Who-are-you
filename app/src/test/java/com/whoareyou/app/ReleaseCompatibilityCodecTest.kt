package com.whoareyou.app

import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

class ReleaseCompatibilityCodecTest {
    @Test
    fun `M774 v1 golden payload remains readable`() {
        val payload = """
            v1
            100|7321|3600000|2|3600000|0|0|0|ZXhhbXBsZS5hcHA:3600000:2
        """.trimIndent()

        val decoded = BehaviorStoreCodec.decode(payload)

        assertEquals(
            listOf(
                DailyBehaviorAggregate(
                    epochDay = 100L,
                    steps = 7_321L,
                    totalForegroundMillis = 3_600_000L,
                    topApps = listOf(AppUsageAggregate("example.app", 3_600_000L, 2)),
                    launchesOrSessions = 2,
                    daypartUsage = DaypartUsage(
                        morningMillis = 3_600_000L,
                        afternoonMillis = 0L,
                        eveningMillis = 0L,
                        nightMillis = 0L
                    )
                )
            ),
            decoded
        )
    }

    @Test
    fun `M775 v1 golden payload remains readable`() {
        val payload = """
            v1
            Z29hbC1sZWdhY3k|APP_USAGE_AT_MOST|1200000|200|7|1|Y29tLmV4YW1wbGUudmlkZW8
        """.trimIndent()

        val decoded = BehaviorGoalStoreCodec.decode(payload)

        assertEquals(
            listOf(
                BehaviorGoal.appUsageAtMost(
                    id = "goal-legacy",
                    packageName = "com.example.video",
                    targetMillis = 1_200_000L,
                    startEpochDay = 200L
                ).copy(paused = true)
            ),
            decoded
        )
    }

    @Test
    fun `behavior and goal encoding are locale independent`() {
        val original = Locale.getDefault()
        try {
            val behavior = listOf(
                DailyBehaviorAggregate(
                    epochDay = 300L,
                    steps = 9_876L,
                    totalForegroundMillis = 5_432_100L,
                    topApps = listOf(AppUsageAggregate("example.app", 5_432_100L, 4)),
                    launchesOrSessions = 4,
                    daypartUsage = DaypartUsage(1L, 2L, 3L, 4L)
                )
            )
            val goals = listOf(
                BehaviorGoal.screenTimeAtMost("screen", 5_400_000L, 300L)
            )

            Locale.setDefault(Locale.FRANCE)
            val behaviorFr = BehaviorStoreCodec.encode(behavior)
            val goalsFr = BehaviorGoalStoreCodec.encode(goals)

            Locale.setDefault(Locale.US)
            val behaviorUs = BehaviorStoreCodec.encode(behavior)
            val goalsUs = BehaviorGoalStoreCodec.encode(goals)

            assertEquals(behaviorFr, behaviorUs)
            assertEquals(goalsFr, goalsUs)
        } finally {
            Locale.setDefault(original)
        }
    }

    @Test
    fun `unknown future codec versions fail safely to empty state`() {
        assertEquals(emptyList<DailyBehaviorAggregate>(), BehaviorStoreCodec.decode("v2\nfuture"))
        assertEquals(emptyList<BehaviorGoal>(), BehaviorGoalStoreCodec.decode("v2\nfuture"))
    }
}
