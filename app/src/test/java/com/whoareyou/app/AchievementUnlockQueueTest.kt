package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AchievementUnlockQueueTest {
    @Test
    fun enqueuePreservesExistingOrderAndAppendsNewUnlocks() {
        assertEquals(
            listOf("first_test", "profile_builder", "ten_tests"),
            AchievementUnlockQueue.enqueue(
                pendingIds = listOf("first_test"),
                newlyUnlockedIds = listOf("profile_builder", "ten_tests")
            )
        )
    }

    @Test
    fun enqueueSuppressesDuplicatesAndBlankIds() {
        assertEquals(
            listOf("first_test", "profile_builder"),
            AchievementUnlockQueue.enqueue(
                pendingIds = listOf("first_test", "first_test"),
                newlyUnlockedIds = listOf("", "first_test", "profile_builder", "profile_builder")
            )
        )
    }

    @Test
    fun consumeRemovesOnlyRequestedAchievement() {
        assertEquals(
            listOf("first_test", "ten_tests"),
            AchievementUnlockQueue.consume(
                pendingIds = listOf("first_test", "profile_builder", "ten_tests"),
                achievementId = "profile_builder"
            )
        )
    }
}
