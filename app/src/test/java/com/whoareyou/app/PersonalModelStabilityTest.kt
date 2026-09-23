package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalModelStabilityTest {
    @Test
    fun `missing timeline keeps stability unknown`() {
        val model = PersonalModelEngine.build(
            graph = graphFor("planning", 78, 72, 3),
            coverage = coverageFor("planning", 3, 72),
            timelines = emptyList()
        )

        assertEquals(PersonalStability.UNKNOWN, model.traits.single().stability)
    }

    @Test
    fun `stable repeated timeline yields stable stability`() {
        val timeline = timelineFor(
            traitId = "planning",
            kind = LongitudinalTrendKind.STABLE,
            pointCount = 4
        )
        val model = PersonalModelEngine.build(
            graph = graphFor("planning", 78, 72, 3),
            coverage = coverageFor("planning", 3, 72),
            timelines = listOf(timeline)
        )

        assertEquals(PersonalStability.STABLE, model.traits.single().stability)
        assertEquals(PersonalTrend.STABLE, model.traits.single().trend)
    }

    @Test
    fun `volatile history prevents established certainty`() {
        val timeline = timelineFor(
            traitId = "planning",
            kind = LongitudinalTrendKind.VOLATILE,
            pointCount = 4
        )
        val model = PersonalModelEngine.build(
            graph = graphFor("planning", 78, 80, 4),
            coverage = coverageFor("planning", 4, 80),
            timelines = listOf(timeline)
        )

        assertEquals(PersonalStability.VARIABLE, model.traits.single().stability)
        assertEquals(PersonalCertainty.LIKELY, model.traits.single().certainty)
    }
}
