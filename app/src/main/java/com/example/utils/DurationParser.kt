package com.example.utils

/**
 * Parses Pulse duration strings. All backend durations (from the self-hosted
 * pulse-server) are in the "m:ss" format, so [toSeconds] only accepts that shape.
 */
object DurationParser {

    /** Total seconds for a "m:ss" string; 0 when the string is not in that shape. */
    fun toSeconds(duration: String): Long {
        val parts = duration.split(":")
        if (parts.size != 2) return 0L
        val minutes = parts[0].toLongOrNull() ?: return 0L
        val seconds = parts[1].toLongOrNull() ?: return 0L
        return minutes * 60 + seconds
    }

    /**
     * Milliseconds for a "m:ss" string. Preserves a parseable-but-zero duration
     * ("0:00" → 0ms) and falls back to 4 minutes (240000 ms) only when unparseable.
     */
    fun toMillis(duration: String): Long {
        val parts = duration.split(":")
        if (parts.size != 2) return 240_000L
        val minutes = parts[0].toLongOrNull() ?: return 240_000L
        val seconds = parts[1].toLongOrNull() ?: return 240_000L
        return (minutes * 60 + seconds) * 1000L
    }
}
