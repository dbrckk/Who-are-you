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
            Achievement("first_test", "First Test", "Complete your first personality test.", completed >= 1),
            Achievement("profile_builder", "Profile Builder", "Discover five profile dimensions.", dimensions >= 5),
            Achievement("ten_tests", "10 Tests", "Complete ten different tests.", completed >= 10),
            Achievement("twenty_five_tests", "25 Tests", "Complete twenty-five different tests.", completed >= 25),
            Achievement("profile_complete", "Know Yourself", "Discover every available profile dimension.", completed >= completeTarget),
            Achievement("strong_match", "Strong Match", "Reach at least 90% compatibility with a friend.", (profile.bestMatchPercent ?: -1) >= 90),
            Achievement("opposites", "Opposites", "Find a friend match at 45% compatibility or lower.", (profile.lowestMatchPercent ?: 101) <= 45),
            Achievement("social_butterfly", "Social Butterfly", "Complete five friend comparisons.", profile.matchCount >= 5),
            Achievement("streak_3", "Coming Back", "Keep a 3 day Daily Question streak.", streak >= 3),
            Achievement("streak_7", "7 Day Streak", "Keep a 7 day Daily Question streak.", streak >= 7),
            Achievement("streak_30", "30 Day Streak", "Keep a 30 day Daily Question streak.", streak >= 30)
        )
    }

    fun unlocked(profile: StoredProfile, totalQuizCount: Int): List<Achievement> = build(profile, totalQuizCount).filter { it.unlocked }
}
