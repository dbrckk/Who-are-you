package com.whoareyou.app

object AchievementUnlockQueue {
    fun enqueue(pendingIds: List<String>, newlyUnlockedIds: List<String>): List<String> {
        val result = pendingIds.distinct().toMutableList()
        newlyUnlockedIds.forEach { id ->
            if (id.isNotBlank() && id !in result) result += id
        }
        return result
    }

    fun consume(pendingIds: List<String>, achievementId: String): List<String> =
        pendingIds.filterNot { it == achievementId }.distinct()
}
