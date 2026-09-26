package com.whoareyou.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.launch

object BehaviorGoalFactory {
    fun create(
        request: BehaviorGoalCreateRequest,
        id: String,
        startEpochDay: Long
    ): BehaviorGoal = BehaviorGoal(
        id = id,
        metric = request.metric,
        targetValue = request.targetValue,
        startEpochDay = startEpochDay,
        packageName = request.packageName
    )

}

data class BehaviorGoalsHostState(
    val goals: List<BehaviorGoalUiModel>,
    val onCreate: (BehaviorGoalCreateRequest) -> Unit,
    val onSetPaused: (String, Boolean) -> Unit,
    val onDelete: (String) -> Unit
)

@Composable
fun rememberBehaviorGoalsHostState(
    context: Context,
    snapshot: BehaviorSnapshot
): BehaviorGoalsHostState {
    val scope = rememberCoroutineScope()
    val goals by remember(context) {
        BehaviorGoalRepository.observe(context.applicationContext)
    }.collectAsState(initial = emptyList())

    val currentEpochDay = LocalDate.now().toEpochDay()
    val models = remember(goals, snapshot, currentEpochDay) {
        BehaviorGoalPresentation.build(
            goals = goals,
            snapshot = snapshot,
            currentEpochDay = currentEpochDay
        )
    }

    return remember(context, scope, goals, models, currentEpochDay) {
        BehaviorGoalsHostState(
            goals = models,
            onCreate = { request ->
                val goal = BehaviorGoalFactory.create(
                    request = request,
                    id = UUID.randomUUID().toString(),
                    startEpochDay = currentEpochDay
                )
                scope.launch {
                    BehaviorGoalRepository.upsert(context.applicationContext, goal)
                }
            },
            onSetPaused = { id, paused ->
                scope.launch {
                    BehaviorGoalRepository.setPaused(context.applicationContext, id, paused)
                }
            },
            onDelete = { id ->
                scope.launch {
                    BehaviorGoalRepository.remove(context.applicationContext, id)
                }
            }
        )
    }
}
