package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignatureProfilesTest {
    private val neutralEvidence = mapOf(
        "optimism" to 50,
        "boundaries" to 50,
        "risk_taker" to 50,
        "social_battery" to 50,
        "decision_style" to 50,
        "love_style" to 50,
        "friendship_type" to 50,
        "money_personality" to 50
    )

    private fun withEvidence(required: Map<String, Int>): Map<String, Int> = neutralEvidence + required

    @Test
    fun requiresEnoughCompletedDimensions() {
        val result = SignatureProfiles.primary(
            mapOf(
                "learning_drive" to 90,
                "novelty_seeker" to 90,
                "independence" to 90
            )
        )
        assertNull(result)
    }

    @Test
    fun matchesIndependentExplorerAtBoundary() {
        val result = SignatureProfiles.primary(
            withEvidence(
                mapOf(
                    "learning_drive" to 65,
                    "novelty_seeker" to 65,
                    "independence" to 65
                )
            )
        )
        assertEquals(SignatureProfileKey.INDEPENDENT_EXPLORER, result?.key)
        assertEquals(listOf("learning_drive", "novelty_seeker", "independence"), result?.supportingQuizIds)
    }

    @Test
    fun doesNotMatchIndependentExplorerBelowRequirement() {
        val result = SignatureProfiles.primary(
            withEvidence(
                mapOf(
                    "learning_drive" to 80,
                    "novelty_seeker" to 64,
                    "independence" to 80
                )
            )
        )
        assertNull(result)
    }

    @Test
    fun contradictoryEvidenceBlocksAdaptiveDiplomat() {
        val result = SignatureProfiles.primary(
            withEvidence(
                mapOf(
                    "adaptability" to 80,
                    "patience" to 80,
                    "assertiveness" to 92
                )
            )
        )
        assertNull(result)
    }

    @Test
    fun matchesAllSignatureFamilies() {
        val cases = listOf(
            SignatureProfileKey.INDEPENDENT_EXPLORER to mapOf("learning_drive" to 80, "novelty_seeker" to 85, "independence" to 90),
            SignatureProfileKey.STRUCTURED_BUILDER to mapOf("planning_style" to 85, "self_discipline" to 90, "communication_style" to 70),
            SignatureProfileKey.OPEN_CONNECTOR to mapOf("emotional_openness" to 80, "trust_style" to 75, "communication_style" to 70),
            SignatureProfileKey.ADAPTIVE_DIPLOMAT to mapOf("adaptability" to 80, "patience" to 85, "assertiveness" to 60),
            SignatureProfileKey.DRIVEN_CHALLENGER to mapOf("assertiveness" to 85, "competitiveness" to 90, "stress_response" to 75),
            SignatureProfileKey.CALM_STRATEGIST to mapOf("patience" to 90, "self_discipline" to 85, "stress_response" to 30)
        )

        cases.forEach { (expected, required) ->
            assertEquals(expected, SignatureProfiles.primary(withEvidence(required))?.key)
        }
    }

    @Test
    fun strongerMatchingSignatureWinsRanking() {
        val scores = withEvidence(
            mapOf(
                "planning_style" to 68,
                "self_discipline" to 70,
                "communication_style" to 82,
                "emotional_openness" to 95,
                "trust_style" to 95
            )
        )

        val matches = SignatureProfiles.matches(scores)
        assertTrue(matches.any { it.key == SignatureProfileKey.STRUCTURED_BUILDER })
        assertTrue(matches.any { it.key == SignatureProfileKey.OPEN_CONNECTOR })
        assertEquals(SignatureProfileKey.OPEN_CONNECTOR, SignatureProfiles.primary(scores)?.key)
    }

    @Test
    fun outOfRangeScoresAreClampedForMatching() {
        val result = SignatureProfiles.primary(
            withEvidence(
                mapOf(
                    "assertiveness" to 130,
                    "competitiveness" to 120,
                    "stress_response" to 110
                )
            )
        )
        assertEquals(SignatureProfileKey.DRIVEN_CHALLENGER, result?.key)
        assertTrue((result?.confidence ?: 0) in 65..100)
    }
}