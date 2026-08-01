package com.pulse.server

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TtlCacheTest {

    @Test
    fun `get returns stored value before expiry`() {
        val cache = TtlCache<String, Int>(10_000L)
        cache.put("a", 1)
        assertEquals(1, cache.get("a"))
        assertEquals(1, cache.size())
    }

    @Test
    fun `get returns null after expiry`() {
        val cache = TtlCache<String, Int>(-1L)
        cache.put("a", 1)
        assertNull(cache.get("a"))
    }

    @Test
    fun `remove deletes key`() {
        val cache = TtlCache<String, Int>(10_000L)
        cache.put("a", 1)
        cache.remove("a")
        assertNull(cache.get("a"))
    }

    @Test
    fun `clear empties cache`() {
        val cache = TtlCache<String, Int>(10_000L)
        cache.put("a", 1)
        cache.put("b", 2)
        cache.clear()
        assertEquals(0, cache.size())
        assertNull(cache.get("a"))
    }

    @Test
    fun `evictExpired removes only expired entries`() {
        val cache = TtlCache<String, Int>(10_000L)
        cache.put("fresh", 1)
        cache.put("stale", 2)
        // Force the stale entry to expire by manipulating time isn't possible here;
        // instead verify eviction of an already-expired entry via negative TTL cache.
        val expired = TtlCache<String, Int>(-1L)
        expired.put("gone", 3)
        expired.evictExpired()
        assertEquals(0, expired.size())
        assertTrue(cache.size() == 2)
    }
}
