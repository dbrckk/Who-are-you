package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfilePersistenceCodecTest {
    @Test
    fun decodeScoresNormalizesIdsClampsValuesAndDropsInvalidEntries() {
        val decoded = ProfilePersistenceCodec.decodeScores(
            " quiz-a : 120 ;invalid; :50;quiz-b:-5;quiz-c:not-a-number; quiz-a : 80 "
        )

        assertEquals(mapOf("quiz-a" to 80, "quiz-b" to 0), decoded)
    }

    @Test
    fun encodeScoresNormalizesIdsAndClampsValues() {
        val encoded = ProfilePersistenceCodec.encodeScores(
            linkedMapOf(" quiz-b " to -10, "quiz-a" to 150, "   " to 42)
        )

        assertEquals("quiz-a:100;quiz-b:0", encoded)
    }

    @Test
    fun stringMapCodecNormalizesLegacyAttemptIdentifiers() {
        val decoded = ProfilePersistenceCodec.decodeStringMap(
            " quiz-a : attempt-a ;bad;quiz-b:  attempt-b  ; :missing"
        )

        assertEquals(mapOf("quiz-a" to "attempt-a", "quiz-b" to "attempt-b"), decoded)
        assertEquals("quiz-a:attempt-a;quiz-b:attempt-b", ProfilePersistenceCodec.encodeStringMap(decoded))
    }

    @Test
    fun listCodecDropsBlankAndDuplicateIdentifiers() {
        assertEquals(listOf("a", "b"), ProfilePersistenceCodec.decodeList(" a, ,b,a "))
        assertEquals(setOf("a", "b"), ProfilePersistenceCodec.decodeSet(" a, ,b,a "))
        assertTrue(ProfilePersistenceCodec.decodeList(null).isEmpty())
    }
}
