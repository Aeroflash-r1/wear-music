package com.example.data.remote

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MemoryCacheTest {

    @Test
    fun `put then get returns stored value`() {
        val cache = MemoryCache()
        cache.put("key", "value", ttlMs = 60_000)
        assertEquals("value", cache.get<String>("key"))
    }

    @Test
    fun `get returns null for missing key`() {
        assertNull(MemoryCache().get<String>("missing"))
    }

    @Test
    fun `get returns null for expired entry and removes it`() {
        val cache = MemoryCache()
        cache.put("key", "value", ttlMs = 10)
        Thread.sleep(50)
        assertNull(cache.get<String>("key"))
        // Second call still returns null (entry already removed).
        assertNull(cache.get<String>("key"))
    }

    @Test
    fun `remove deletes entry`() {
        val cache = MemoryCache()
        cache.put("key", "value")
        cache.remove("key")
        assertNull(cache.get<String>("key"))
    }

    @Test
    fun `clear empties all entries`() {
        val cache = MemoryCache()
        cache.put("a", 1)
        cache.put("b", 2)
        cache.clear()
        assertNull(cache.get<Int>("a"))
        assertNull(cache.get<Int>("b"))
    }
}
