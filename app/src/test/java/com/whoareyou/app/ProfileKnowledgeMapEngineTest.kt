package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfileKnowledgeMapEngineTest {
    @Test
    fun `knowledge map separates strong developing and unknown traits`() {
        val coverage = ProfileCoverage(
            knownTraitCount = 2,
            totalTraitCount = 3,
            coveragePercent = 67,
            averageConfidence = 60,
            strongTraitCount = 1,
            uncertainTraitCount = 2,
            traits = listOf(
                TraitCoverage("assertiveness", 2, 82, 0, CoverageStatus.STRONG),
                TraitCoverage("adaptability", 1, 38, 1, CoverageStatus.LOW),
                TraitCoverage("curiosity", 0, 0, 0, CoverageStatus.UNKNOWN)
            )
        )

        val map = ProfileKnowledgeMapEngine.build(coverage)

        assertEquals(listOf("assertiveness"), map.strong.map { it.traitId })
        assertEquals(listOf("adaptability"), map.developing.map { it.traitId })
        assertEquals(listOf("curiosity"), map.unknown.map { it.traitId })
    }

    @Test
    fun `domain coverage counts known traits independently`() {
        val coverage = ProfileCoverage(
            knownTraitCount = 2,
            totalTraitCount = 3,
            coveragePercent = 67,
            averageConfidence = 50,
            strongTraitCount = 1,
            uncertainTraitCount = 2,
            traits = listOf(
                TraitCoverage("social_energy", 2, 70, 0, CoverageStatus.STRONG),
                TraitCoverage("assertiveness", 1, 40, 0, CoverageStatus.LOW),
                TraitCoverage("trust_openness", 0, 0, 0, CoverageStatus.UNKNOWN)
            )
        )

        val social = ProfileKnowledgeMapEngine.build(coverage)
            .domains.single { it.domain == TraitDomain.SOCIAL }

        assertEquals(2, social.knownCount)
        assertEquals(3, social.totalCount)
        assertEquals(1, social.strongCount)
        assertTrue(social.coveragePercent in 66..67)
    }
}
