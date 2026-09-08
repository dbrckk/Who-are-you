package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileIdentityMapTest {
    private fun dimension(id: String, score: Int) = ProfileDimension(
        quizId = id,
        title = id,
        score = score,
        resultTitle = id,
        metricLabel = id
    )

    @Test
    fun `strongest deviations are selected first and capped to six`() {
        val dimensions = listOf(
            dimension("a", 50),
            dimension("b", 95),
            dimension("c", 5),
            dimension("d", 80),
            dimension("e", 20),
            dimension("f", 70),
            dimension("g", 30),
            dimension("h", 55)
        )

        val result = ProfileIdentityMapEngine.build(dimensions)

        assertEquals(6, result.axes.size)
        assertEquals(listOf("b", "c", "d", "e", "f", "g"), result.axes.map { it.quizId })
    }

    @Test
    fun `summary exposes dominant signal contrast and balanced axes`() {
        val result = ProfileIdentityMapEngine.build(
            listOf(
                dimension("high", 90),
                dimension("low", 20),
                dimension("balanced", 55),
                dimension("balanced2", 45)
            )
        )

        assertEquals(80, result.dominantSignalPercent)
        assertEquals(70, result.contrast)
        assertEquals(2, result.balancedAxes)
    }

    @Test
    fun `empty profile returns neutral summary`() {
        val result = ProfileIdentityMapEngine.build(emptyList())

        assertEquals(emptyList<ProfileDimension>(), result.axes)
        assertEquals(0, result.dominantSignalPercent)
        assertEquals(0, result.contrast)
        assertEquals(0, result.balancedAxes)
    }
}
