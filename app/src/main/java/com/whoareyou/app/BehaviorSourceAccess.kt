package com.whoareyou.app

import android.content.Context

object BehaviorSourceAccess {
    suspend fun activityState(context: Context): BehaviorSourceState =
        runCatching { HealthConnectActivityDataSource(context).state() }
            .getOrDefault(BehaviorSourceState.ERROR)

    fun appUsageState(context: Context): BehaviorSourceState =
        runCatching { UsageAccess.state(context) }
            .getOrDefault(BehaviorSourceState.ERROR)
}
