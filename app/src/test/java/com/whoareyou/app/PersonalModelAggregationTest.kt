package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalModelAggregationTest {
    @Test
    fun `unknown coverage traits are exposed deterministically`() {
        val coverage = ProfileCoverage(
            knownTraitCount = 1,
            totalTraitCount = 3,
            coveragePercent = 33,
            averageConfidence = 70,
            strongTraitCount = 1,
            uncertainTraitCount = 2,
            traits = listOf(
                TraitCoverage("curiosity", 3, 70, 0, CoverageStatus.STRONG),
                TraitCoverage("trust_openness", 0, 0, 0, CoverageStatus.UNKNOWN),
                TraitCoverage("boundaries", 0, 0, 0, CoverageStatus.UNKNOWN)
            )
        )

        val model = PersonalModelEngine.build(
            graph = graphFor("curiosity", 80, 70, 3),
            coverage = coverage
        )

        assertEquals(listOf("boundaries", "trust_openness"), model.unknownTraitIds)
    }

    @Test
    fun `input ordering does not change semantic personal model`() {
        val curiosity = profileTrait("curiosity", 78, 72, 3)
        val planning = profileTrait("planning", 68, 66, 3)
        val curiosityCoverage = TraitCoverage("curiosity", 3, 72, 0, CoverageStatus.STRONG)
        val planningCoverage = TraitCoverage("planning", 3, 66, 0, CoverageStatus.STRONG)

        fun model(
            traits: List<ProfileTrait>,
            coverageTraits: List<TraitCoverage>
        ): PersonalModel = PersonalModelEngine.build(
            graph = TraitGraph(traits, traits.sumOf { it.evidenceCount }),
            coverage = ProfileCoverage(
                knownTraitCount = 2,
                totalTraitCount = 2,
                coveragePercent = 100,
                averageConfidence = 69,
                strongTraitCount = 2,
                uncertainTraitCount = 0,
                traits = coverageTraits
            )
        )

        val first = model(
            traits = listOf(curiosity, planning),
            coverageTraits = listOf(curiosityCoverage, planningCoverage)
        )
        val reversed = model(
            traits = listOf(planning, curiosity),
            coverageTraits = listOf(planningCoverage, curiosityCoverage)
        )

        assertEquals(first, reversed)
    }

    @Test
    fun `source quiz ids are unique and sorted`() {
        val trait = ProfileTrait(
            id = "curiosity",
            score = 80,
            confidence = 70,
            evidence = listOf(
                evidence("z", 80, 0.8),
                evidence("a", 80, 0.8),
                evidence("z", 80, 0.8)
            ),
            contradictoryEvidenceCount = 0
        )

        val model = PersonalModelEngine.build(
            TraitGraph(listOf(trait), 3),
            coverageFor("curiosity", 3, 70)
        )

        assertEquals(listOf("a", "z"), model.traits.single().sourceQuizIds)
    }

    @Test
    fun `knowledge domains use deterministic coverage ordering`() {
        val knowledgeMap = ProfileKnowledgeMap(
            strong = emptyList(),
            developing = emptyList(),
            unknown = emptyList(),
            domains = listOf(
                TraitDomainCoverage(TraitDomain.SOCIAL, 1, 4, 0, 25),
                TraitDomainCoverage(TraitDomain.THINKING, 4, 4, 3, 100),
                TraitDomainCoverage(TraitDomain.GROWTH, 2, 4, 1, 50),
                TraitDomainCoverage(TraitDomain.EMOTIONAL, 3, 4, 2, 75)
            )
        )

        val model = PersonalModelEngine.build(
            graph = graphFor("curiosity", 80, 70, 3),
            coverage = coverageFor("curiosity", 3, 70),
            knowledgeMap = knowledgeMap
        )

        assertEquals(
            listOf(TraitDomain.THINKING, TraitDomain.EMOTIONAL, TraitDomain.GROWTH),
            model.strongestKnowledgeDomains.map { it.domain }
        )
        assertEquals(
            listOf(TraitDomain.SOCIAL, TraitDomain.GROWTH, TraitDomain.EMOTIONAL),
            model.knowledgeGaps.map { it.domain }
        )
    }
}
