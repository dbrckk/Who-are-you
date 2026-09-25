package com.whoareyou.app

enum class BehaviorIntegrationCommand {
    REQUEST_ACTIVITY_PERMISSION,
    OPEN_USAGE_ACCESS,
    DISABLE_SOURCE,
    NONE
}

object BehaviorIntegrationPolicy {
    fun command(source: BehaviorSource, action: BehaviorSourceAction): BehaviorIntegrationCommand = when (action) {
        BehaviorSourceAction.ENABLE,
        BehaviorSourceAction.AUTHORIZE -> when (source) {
            BehaviorSource.ACTIVITY -> BehaviorIntegrationCommand.REQUEST_ACTIVITY_PERMISSION
            BehaviorSource.APP_USAGE -> BehaviorIntegrationCommand.OPEN_USAGE_ACCESS
        }
        BehaviorSourceAction.DISABLE -> BehaviorIntegrationCommand.DISABLE_SOURCE
        BehaviorSourceAction.NONE -> BehaviorIntegrationCommand.NONE
    }
}
