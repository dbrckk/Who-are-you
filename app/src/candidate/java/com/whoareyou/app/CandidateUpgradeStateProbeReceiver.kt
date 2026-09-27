package com.whoareyou.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class CandidateUpgradeStateProbeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION) return

        val result = runCatching {
            runBlocking {
                val profile = ProfileStore.observe(context).first()
                val behavior = BehaviorRepository.observe(context).first()
                val goals = BehaviorGoalRepository.observe(context).first()
                val today = behavior.today
                val goal = goals.firstOrNull { it.id == "upgrade-probe-goal" }

                listOf(
                    "onboarding=" + profile.onboardingComplete,
                    "ads_removed=" + profile.adsRemoved,
                    "activity_state=" + behavior.sourceStates[BehaviorSource.ACTIVITY].orEmptyName(),
                    "steps=" + (today?.steps ?: -1L),
                    "goal_id=" + (goal?.id ?: ""),
                    "goal_metric=" + (goal?.metric?.name ?: ""),
                    "goal_target=" + (goal?.targetValue ?: -1L)
                ).joinToString(";")
            }
        }.getOrElse { error ->
            "probe_error=" + error.javaClass.simpleName
        }

        setResultData(result)
    }

    private fun BehaviorSourceState?.orEmptyName(): String = this?.name ?: ""

    companion object {
        const val ACTION = "com.whoareyou.app.action.CANDIDATE_UPGRADE_STATE"
    }
}
