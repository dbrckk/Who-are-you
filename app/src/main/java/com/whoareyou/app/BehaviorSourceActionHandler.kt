package com.whoareyou.app

import android.content.Context

enum class BehaviorSourceEffect {
    NONE,
    REFRESH,
    REQUEST_ACTIVITY_PERMISSION,
    OPEN_USAGE_ACCESS
}

object BehaviorSourceActionHandler {
    suspend fun handle(
        context: Context,
        source: BehaviorSource,
        action: BehaviorSourceAction
    ): BehaviorSourceEffect = when (BehaviorIntegrationPolicy.command(source, action)) {
        BehaviorIntegrationCommand.DISABLE_SOURCE -> {
            BehaviorRepository.clearSource(context, source)
            BehaviorSourceEffect.NONE
        }
        BehaviorIntegrationCommand.REQUEST_ACTIVITY_PERMISSION -> {
            BehaviorRepository.setSourceEnabled(context, source, true)
            val state = BehaviorSourceAccess.activityState(context)
            BehaviorRepository.setSourceState(context, source, state)
            when (state) {
                BehaviorSourceState.AVAILABLE -> BehaviorSourceEffect.REFRESH
                BehaviorSourceState.PERMISSION_REQUIRED -> BehaviorSourceEffect.REQUEST_ACTIVITY_PERMISSION
                else -> BehaviorSourceEffect.NONE
            }
        }
        BehaviorIntegrationCommand.OPEN_USAGE_ACCESS -> {
            BehaviorRepository.setSourceEnabled(context, source, true)
            val state = BehaviorSourceAccess.appUsageState(context)
            BehaviorRepository.setSourceState(context, source, state)
            when (state) {
                BehaviorSourceState.AVAILABLE -> BehaviorSourceEffect.REFRESH
                BehaviorSourceState.PERMISSION_REQUIRED -> BehaviorSourceEffect.OPEN_USAGE_ACCESS
                else -> BehaviorSourceEffect.NONE
            }
        }
        BehaviorIntegrationCommand.NONE -> BehaviorSourceEffect.NONE
    }
}
