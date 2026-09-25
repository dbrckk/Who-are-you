package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BehaviorIntegrationPolicyTest {
    @Test
    fun `enable and authorize request permission for activity`() {
        assertEquals(
            BehaviorIntegrationCommand.REQUEST_ACTIVITY_PERMISSION,
            BehaviorIntegrationPolicy.command(BehaviorSource.ACTIVITY, BehaviorSourceAction.ENABLE)
        )
        assertEquals(
            BehaviorIntegrationCommand.REQUEST_ACTIVITY_PERMISSION,
            BehaviorIntegrationPolicy.command(BehaviorSource.ACTIVITY, BehaviorSourceAction.AUTHORIZE)
        )
    }

    @Test
    fun `enable and authorize open usage access for app usage`() {
        assertEquals(
            BehaviorIntegrationCommand.OPEN_USAGE_ACCESS,
            BehaviorIntegrationPolicy.command(BehaviorSource.APP_USAGE, BehaviorSourceAction.ENABLE)
        )
        assertEquals(
            BehaviorIntegrationCommand.OPEN_USAGE_ACCESS,
            BehaviorIntegrationPolicy.command(BehaviorSource.APP_USAGE, BehaviorSourceAction.AUTHORIZE)
        )
    }

    @Test
    fun `disable clears only selected source`() {
        assertEquals(
            BehaviorIntegrationCommand.DISABLE_SOURCE,
            BehaviorIntegrationPolicy.command(BehaviorSource.ACTIVITY, BehaviorSourceAction.DISABLE)
        )
        assertEquals(
            BehaviorIntegrationCommand.DISABLE_SOURCE,
            BehaviorIntegrationPolicy.command(BehaviorSource.APP_USAGE, BehaviorSourceAction.DISABLE)
        )
    }

    @Test
    fun `none performs no integration action`() {
        assertEquals(
            BehaviorIntegrationCommand.NONE,
            BehaviorIntegrationPolicy.command(BehaviorSource.ACTIVITY, BehaviorSourceAction.NONE)
        )
    }

    @Test
    fun `activity permission result refreshes only when granted`() {
        assertTrue(BehaviorIntegrationPolicy.shouldRefreshAfterActivityPermission(granted = true))
        assertFalse(BehaviorIntegrationPolicy.shouldRefreshAfterActivityPermission(granted = false))
    }

    @Test
    fun `return from usage access always rechecks source state`() {
        assertTrue(BehaviorIntegrationPolicy.shouldRefreshAfterUsageAccessReturn())
    }
}
