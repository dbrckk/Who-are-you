package com.whoareyou.app

object BehaviorGoalPresentation {
    fun build(
        goals: List<BehaviorGoal>,
        snapshot: BehaviorSnapshot,
        currentEpochDay: Long
    ): List<BehaviorGoalUiModel> = goals
        .sortedBy { it.id }
        .map { goal ->
            val progress = BehaviorGoalEngine.evaluate(
                goal = goal,
                days = snapshot.last30Days,
                currentEpochDay = currentEpochDay
            )
            BehaviorGoalUiModelFactory.build(progress, currentEpochDay)
        }
}
