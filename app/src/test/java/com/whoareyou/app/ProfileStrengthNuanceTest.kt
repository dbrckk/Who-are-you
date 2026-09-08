package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProfileStrengthNuanceTest {
    private fun dimension(id: String, score: Int) = ProfileDimension(
        quizId = id,
        title = id,
        score = score,
        resultTitle = id,
        metricLabel = id
    )

    @Test
    fun `strongest dimensions are ranked by distance from neutral`() {
        val result = ProfileStrengthNuanceEngine.build(
            listOf(
                dimension("balanced", 52),
                dimension("high", 91),
                dimension("low", 14),
                dimension("mid", 68)
            )
        )

        assertEquals(listOf("high", "low"), result.strongest.map { it.quizId })
    }

    @Test
    fun `most nuanced dimension is closest to neutral`() {
        val result = ProfileStrengthNuanceEngine.build(
            listOf(
                dimension("a", 20),
                dimension("b", 49),
                dimension("c", 83)
            )
        )

        assertEquals("b", result.mostNuanced?.quizId)
    }

    @Test
    fun `contrast pair exposes lowest and highest scores`() {
        val result = ProfileStrengthNuanceEngine.build(
            listOf(
                dimension("low", 12),
                dimension("middle", 55),
                dimension("high", 92)
            )
        )

        assertNotNull(result.contrastPair)
        assertEquals("low", result.contrastPair?.first?.quizId)
        assertEquals("high", result.contrastPair?.second?.quizId)
    }
}
