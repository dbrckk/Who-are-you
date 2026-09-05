package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileInsightsTest {
    private fun dimension(id: String, score: Int) = ProfileDimension(
        quizId = id,
        title = id,
        score = score,
        resultTitle = "result",
        metricLabel = "metric"
    )

    @Test
    fun requiresEnoughCompletedDimensions() {
        val result = ProfileInsights.derive(listOf(
            dimension("a", 10), dimension("b", 90), dimension("c", 50), dimension("d", 50)
        ))
        assertTrue(result.isEmpty())
    }

    @Test
    fun derivesStrongBalancedAndContrastPatterns() {
        val result = ProfileInsights.derive(listOf(
            dimension("a", 10),
            dimension("b", 90),
            dimension("c", 45),
            dimension("d", 50),
            dimension("e", 55),
            dimension("f", 20)
        ))

        assertEquals(
            listOf(
                ProfileInsightKey.STRONG_SIGNATURE,
                ProfileInsightKey.BALANCED_CORE,
                ProfileInsightKey.BOLD_CONTRAST
            ),
            result
        )
    }

    @Test
    fun respectsLimit() {
        val result = ProfileInsights.derive(listOf(
            dimension("a", 10), dimension("b", 90), dimension("c", 45),
            dimension("d", 50), dimension("e", 55), dimension("f", 20)
        ), limit = 1)

        assertEquals(listOf(ProfileInsightKey.STRONG_SIGNATURE), result)
    }
}
