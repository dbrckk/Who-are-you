package com.whoareyou.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PurchaseGrantPolicyTest {
    @Test
    fun `live purchase emits purchase success`() {
        assertTrue(PurchaseGrantPolicy.shouldLogPurchaseSuccess(PurchaseGrantSource.LIVE_PURCHASE))
    }

    @Test
    fun `restored purchase does not emit purchase success`() {
        assertFalse(PurchaseGrantPolicy.shouldLogPurchaseSuccess(PurchaseGrantSource.RESTORE))
    }
}
