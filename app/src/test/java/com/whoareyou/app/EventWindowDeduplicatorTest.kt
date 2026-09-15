package com.whoareyou.app

import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class EventWindowDeduplicatorTest {
    @Test
    fun `first event is emitted`() {
        val deduplicator = EventWindowDeduplicator(2_000L)

        assertTrue(deduplicator.shouldEmit("values:generic", 10_000L))
    }

    @Test
    fun `same key inside window is suppressed`() {
        val deduplicator = EventWindowDeduplicator(2_000L)
        deduplicator.shouldEmit("values:generic", 10_000L)

        assertFalse(deduplicator.shouldEmit("values:generic", 11_999L))
    }

    @Test
    fun `same key at window boundary is emitted`() {
        val deduplicator = EventWindowDeduplicator(2_000L)
        deduplicator.shouldEmit("values:generic", 10_000L)

        assertTrue(deduplicator.shouldEmit("values:generic", 12_000L))
    }

    @Test
    fun `different key is emitted immediately`() {
        val deduplicator = EventWindowDeduplicator(2_000L)
        deduplicator.shouldEmit("values:generic", 10_000L)

        assertTrue(deduplicator.shouldEmit("social:generic", 10_001L))
    }

    @Test
    fun `clock rollback does not suppress event`() {
        val deduplicator = EventWindowDeduplicator(2_000L)
        deduplicator.shouldEmit("values:generic", 10_000L)

        assertTrue(deduplicator.shouldEmit("values:generic", 9_000L))
    }

    @Test
    fun `negative window is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            EventWindowDeduplicator(-1L)
        }
    }
}
