package com.whoareyou.app

object BillingReconnectPolicy {
    private val delaysMillis = longArrayOf(
        1_000L,
        2_000L,
        5_000L,
        10_000L,
        30_000L
    )

    fun delayMillis(attempt: Int): Long =
        delaysMillis[attempt.coerceIn(0, delaysMillis.lastIndex)]

    fun nextAttempt(attempt: Int): Int =
        (attempt + 1).coerceAtMost(delaysMillis.lastIndex)
}
