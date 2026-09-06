package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class BillingReconnectPolicyTest {
    @Test
    fun `reconnect delays back off and cap`() {
        assertEquals(1_000L, BillingReconnectPolicy.delayMillis(0))
        assertEquals(2_000L, BillingReconnectPolicy.delayMillis(1))
        assertEquals(5_000L, BillingReconnectPolicy.delayMillis(2))
        assertEquals(10_000L, BillingReconnectPolicy.delayMillis(3))
        assertEquals(30_000L, BillingReconnectPolicy.delayMillis(4))
        assertEquals(30_000L, BillingReconnectPolicy.delayMillis(99))
    }

    @Test
    fun `next reconnect attempt saturates at maximum backoff`() {
        assertEquals(1, BillingReconnectPolicy.nextAttempt(0))
        assertEquals(2, BillingReconnectPolicy.nextAttempt(1))
        assertEquals(4, BillingReconnectPolicy.nextAttempt(4))
        assertEquals(4, BillingReconnectPolicy.nextAttempt(99))
    }

    @Test
    fun `negative attempts use first reconnect delay`() {
        assertEquals(1_000L, BillingReconnectPolicy.delayMillis(-5))
    }
}
