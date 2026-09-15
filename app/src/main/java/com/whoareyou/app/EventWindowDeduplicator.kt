package com.whoareyou.app

class EventWindowDeduplicator(
    private val windowMillis: Long
) {
    init {
        require(windowMillis >= 0L) { "windowMillis must be non-negative" }
    }

    private var lastKey: String? = null
    private var lastAtMillis: Long = Long.MIN_VALUE

    @Synchronized
    fun shouldEmit(key: String, nowMillis: Long): Boolean {
        val elapsed = if (lastAtMillis == Long.MIN_VALUE) Long.MAX_VALUE else nowMillis - lastAtMillis
        val duplicate = lastKey == key && elapsed >= 0L && elapsed < windowMillis
        if (duplicate) return false

        lastKey = key
        lastAtMillis = nowMillis
        return true
    }
}
