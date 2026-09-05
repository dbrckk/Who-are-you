package com.whoareyou.app

import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileEvolutionShareTest {
    @Test
    fun formatsHigherDeltaInEnglishAndFrench() {
        val change = ScoreChangeEngine.compare(40, 52)!!
        assertEquals("+12 pts vs previous", ProfileEvolutionShare.deltaLabel(change, french = false))
        assertEquals("+12 pts vs précédent", ProfileEvolutionShare.deltaLabel(change, french = true))
    }

    @Test
    fun formatsLowerDeltaWithoutCallingItWorse() {
        val change = ScoreChangeEngine.compare(70, 61)!!
        assertEquals("−9 pts vs previous", ProfileEvolutionShare.deltaLabel(change, french = false))
        assertEquals("−9 pts vs précédent", ProfileEvolutionShare.deltaLabel(change, french = true))
    }

    @Test
    fun formatsStableScoreCompactly() {
        val change = ScoreChangeEngine.compare(55, 55)!!
        assertEquals("same as previous", ProfileEvolutionShare.deltaLabel(change, french = false))
        assertEquals("= précédent", ProfileEvolutionShare.deltaLabel(change, french = true))
    }
}
