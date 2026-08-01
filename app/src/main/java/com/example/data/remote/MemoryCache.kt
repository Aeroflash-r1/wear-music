package com.example.data.remote

import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryCache @Inject constructor() {

    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val ttlMs: Long
    ) {
        fun isExpired(): Boolean = System.currentTimeMillis() - timestamp > ttlMs
    }

    private val cache = ConcurrentHashMap<String, CacheEntry<Any>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(key: String): T? {
        val entry = cache[key] ?: return null
        if (entry.isExpired()) {
            cache.remove(key)
            return null
        }
        return entry.data as? T
    }

    fun <T : Any> put(key: String, data: T, ttlMs: Long = 300_000L) { // Default 5 mins TTL
        cache[key] = CacheEntry(data as Any, System.currentTimeMillis(), ttlMs)
    }

    fun remove(key: String) {
        cache.remove(key)
    }

    fun clear() {
        cache.clear()
    }
}
