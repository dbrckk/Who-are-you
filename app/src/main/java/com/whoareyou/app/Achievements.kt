package com.whoareyou.app

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val unlocked: Boolean
)

object AchievementEngine {
    fun build(profile: StoredProfile, totalQuizCount: Int): List<Achievement> {
        val completed = profile.completedQuizIds.size
        val dimensions = profile.latestScores.size
        val streak = profile.daily.longestStreak
        val completeTarget = totalQuizCount.coerceAtLeast(1)

        return listOf(
            Achievement(
                id = "first_test",
                title = "First Test",
                description = "Complete your first personality test.",
                unlocked = completed >= 1
            ),
            Achievement(
                id = "ten_tests",
                title = "10 Tests",
                description = "Complete ten different tests.",
                unlocked = completed >= 10
            ),
            Achievement(
                id = "profile_builder",
                title = "Profile Builder",
                description = "Discover five profile dimensions.",
                unlocked = dimensions >= 5
            ),
            Achievement(
                id = "profile_complete",
                title = "Know Yourself",
                description = "Discover every available profile dimension.",
                unlocked = completed >= completeTarget
            ),
            Achievement(
                id = "streak_3",
                title = "Coming Back",
                description = "Keep a 3 day Daily Question streak.",
                unlocked = streak >= 3
            ),
            Achievement(
                id = "streak_7",
                title = "7 Day Streak",
                description = "Keep a 7 day Daily Question streak.",
                unlocked = streak >= 7
            ),
            Achievement(
                id = "streak_30",
                title = "30 Day Streak",
                description = "Keep a 30 day Daily Question streak.",
                unlocked = streak >= 30
            )
        )
    }

    fun unlocked(profile: StoredProfile, totalQuizCount: Int): List<Achievement> =
        build(profile, totalQuizCount).filter { it.unlocked }
}
