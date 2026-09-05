package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SignatureProfileTest {
    private fun withFillers(vararg pairs: Pair<String, Int>): Map<String, Int> = buildMap {
        putAll(pairs)
        put("boundaries", 50)
        put("optimism", 50)
        put("trust_style", get("trust_style") ?: 50)
        put("communication_style", get("communication_style") ?: 50)
        put("adaptability", get("adaptability") ?: 50)
        put("patience", get("patience") ?: 50)
        put("self_discipline", get("self_discipline") ?: 50)
        put("assertiveness", get("assertiveness") ?: 50)
        put("stress_response", get("stress_response") ?: 50)
    }

    @Test
    fun insufficientProfileDataFallsBackCleanly() {
        val scores = mapOf(
            "learning_drive" to 90,
            "novelty_seeker" to 90,
            "independence" to 90
        )

        assertNull(SignatureProfiles.primary(scores))
        assertTrue(SignatureProfiles.matches(scores).isEmpty())
    }

    @Test
    fun independentExplorerMatchesAtBoundary() {
        val scores = withFillers(
            "learning_drive" to 65,
            "novelty_seeker" to 65,
            "independence" to 65
        )

        assertEquals(SignatureProfileKey.INDEPENDENT_EXPLORER, SignatureProfiles.primary(scores)?.key)
    }

    @Test
    fun contradictoryEvidencePreventsMatch() {
        val scores = withFillers(
            "learning_drive" to 90,
            "novelty_seeker" to 20,
            "independence" to 90
        )

        assertTrue(SignatureProfiles.matches(scores).none { it.key == SignatureProfileKey.INDEPENDENT_EXPLORER })
    }

    @Test
    fun calmStrategistMatchesLowStressWithPatienceAndDiscipline() {
        val scores = withFillers(
            "patience" to 82,
            "self_discipline" to 88,
            "stress_response" to 25
        )

        assertEquals(SignatureProfileKey.CALM_STRATEGIST, SignatureProfiles.primary(scores)?.key)
    }

    @Test
    fun rankingPrefersStrongerEligibleSignature() {
        val scores = withFillers(
            "learning_drive" to 100,
            "novelty_seeker" to 100,
            "independence" to 100,
            "planning_style" to 65,
            "self_discipline" to 65,
            "communication_style" to 55
        )

        val matches = SignatureProfiles.matches(scores)
        assertTrue(matches.any { it.key == SignatureProfileKey.INDEPENDENT_EXPLORER })
        assertTrue(matches.any { it.key == SignatureProfileKey.STRUCTURED_BUILDER })
        assertEquals(SignatureProfileKey.INDEPENDENT_EXPLORER, SignatureProfiles.primary(scores)?.key)
    }

    @Test
    fun sixDistinctSignatureFamiliesCanMatch() {
        val cases = listOf(
            withFillers("learning_drive" to 80, "novelty_seeker" to 80, "independence" to 80) to SignatureProfileKey.INDEPENDENT_EXPLORER,
            withFillers("planning_style" to 80, "self_discipline" to 80, "communication_style" to 70) to SignatureProfileKey.STRUCTURED_BUILDER,
            withFillers("emotional_openness" to 80, "trust_style" to 75, "communication_style" to 70) to SignatureProfileKey.OPEN_CONNECTOR,
            withFillers("adaptability" to 80, "patience" to 80, "assertiveness" to 55) to SignatureProfileKey.ADAPTIVE_DIPLOMAT,
            withFillers("assertiveness" to 80, "competitiveness" to 80, "stress_response" to 70) to SignatureProfileKey.DRIVEN_CHALLENGER,
            withFillers("patience" to 80, "self_discipline" to 80, "stress_response" to 30) to SignatureProfileKey.CALM_STRATEGIST
        )

        cases.forEach { (scores, expected) ->
            assertTrue("Expected $expected", SignatureProfiles.matches(scores).any { it.key == expected })
        }
    }
}
