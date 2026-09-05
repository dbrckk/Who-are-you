package com.whoareyou.app

object ProfileEvolutionShare {
    fun deltaLabel(change: ScoreChange, french: Boolean): String = when (change.direction) {
        ScoreChangeDirection.HIGHER -> if (french) "+${change.absoluteDelta} pts vs précédent" else "+${change.absoluteDelta} pts vs previous"
        ScoreChangeDirection.LOWER -> if (french) "−${change.absoluteDelta} pts vs précédent" else "−${change.absoluteDelta} pts vs previous"
        ScoreChangeDirection.SAME -> if (french) "= précédent" else "same as previous"
    }
}
