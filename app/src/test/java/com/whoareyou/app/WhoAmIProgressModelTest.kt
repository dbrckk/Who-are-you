package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WhoAmIProgressModelTest {
    @Test
    fun `knowledge gaps map to safe domain labels deterministically`() {
        val gap = TraitDomainCoverage(
            domain = TraitDomain.THINKING,
            knownCount = 1,
            totalCount = 4,
            strongCount = 0,
            coveragePercent = 25
        )

        assertEquals(R.string.who_am_i_domain_thinking, WhoAmIProgressModel.domainLabel(gap.domain))
    }

    @Test
    fun `evolution language is neutral and absent when trend is unknown`() {
        assertEquals(R.string.who_am_i_trend_rising, WhoAmIProgressModel.trendLabel(PersonalTrend.RISING))
        assertEquals(R.string.who_am_i_trend_falling, WhoAmIProgressModel.trendLabel(PersonalTrend.FALLING))
        assertEquals(R.string.who_am_i_trend_variable, WhoAmIProgressModel.trendLabel(PersonalTrend.VARIABLE))
        assertNull(WhoAmIProgressModel.trendLabel(PersonalTrend.UNKNOWN))
        assertNull(WhoAmIProgressModel.trendLabel(PersonalTrend.STABLE))
    }
}
