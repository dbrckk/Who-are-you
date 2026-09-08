package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ResultInterpretationTest {
    @Test
    fun `neutral result stays balanced`() {
        val summary = ResultInterpretationEngine.derive(50)

        assertEquals(ResultSignalStrength.BALANCED, summary.strength)
        assertEquals(ResultDirection.HIGH, summary.direction)
        assertEquals(0, summary.distanceFromNeutral)
        assertEquals(100, summary.nuancePercent)
    }

    @Test
    fun `clear low result is classified correctly`() {
        val summary = ResultInterpretationEngine.derive(30)

        assertEquals(ResultSignalStrength.CLEAR, summary.strength)
        assertEquals(ResultDirection.LOW, summary.direction)
        assertEquals(20, summary.distanceFromNeutral)
        assertEquals(60, summary.nuancePercent)
    }

    @Test
    fun `strong high result is classified correctly`() {
        val summary = ResultInterpretationEngine.derive(88)

        assertEquals(ResultSignalStrength.STRONG, summary.strength)
        assertEquals(ResultDirection.HIGH, summary.direction)
        assertEquals(38, summary.distanceFromNeutral)
        assertEquals(24, summary.nuancePercent)
    }

    @Test
    fun `scores are clamped to quiz range`() {
        assertEquals(0, ResultInterpretationEngine.derive(-20).score)
        assertEquals(100, ResultInterpretationEngine.derive(140).score)
    }
}
