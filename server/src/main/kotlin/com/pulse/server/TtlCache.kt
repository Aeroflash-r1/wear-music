package com.pulse.server

import java.util.concurrent.ConcurrentHashMap

/**
 * Tiny thread-safe TTL cache. yt-dlp calls are expensive (1-5s each) and YouTube
 * rate-limits aggressive scraping, so every endpoint result is cached.
 */
class TtlCache<K : Any, V : Any>(private val ttlMillis: Long) {

    private data class Entry<V>(val value: V, val expiresAt: Long)

    private val map = ConcurrentHashMap<K, Entry<V>>()

    fun get(key: K): V? {
        val entry = map[key] ?: return null
        if (System.currentTimeMillis() > entry.expiresAt) {
            map.remove(key, entry)
            return null
        }
        return entry.value
    }

    fun put(key: K, value: V) {
        map[key] = Entry(value, System.currentTimeMillis() + ttlMillis)
    }

    fun remove(key: K) {
        map.remove(key)
    }

    fun clear() = map.clear()

    fun size(): Int = map.size

    /** Evicts expired entries; call periodically or before reporting stats. */
    fun evictExpired() {
        val now = System.currentTimeMillis()
        map.entries.removeIf { now > it.value.expiresAt }
    }
}
