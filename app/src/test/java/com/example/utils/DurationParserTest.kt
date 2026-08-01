package com.example.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DurationParserTest {

    @Test
    fun `toSeconds parses m ss format`() {
        assertEquals(225L, DurationParser.toSeconds("3:45"))
        assertEquals(5L, DurationParser.toSeconds("0:05"))
        assertEquals(0L, DurationParser.toSeconds("0:00"))
    }

    @Test
    fun `toSeconds returns 0 for invalid input`() {
        assertEquals(0L, DurationParser.toSeconds("garbage"))
        assertEquals(0L, DurationParser.toSeconds("3:45:30"))
        assertEquals(0L, DurationParser.toSeconds(""))
        assertEquals(0L, DurationParser.toSeconds("3"))
        assertEquals(0L, DurationParser.toSeconds("3:xx"))
    }

    @Test
    fun `toMillis parses valid durations`() {
        assertEquals(225_000L, DurationParser.toMillis("3:45"))
        assertEquals(5_000L, DurationParser.toMillis("0:05"))
        // A parseable-but-zero duration must stay zero, not hit the fallback.
        assertEquals(0L, DurationParser.toMillis("0:00"))
    }

    @Test
    fun `toMillis falls back to 4 minutes for invalid input`() {
        assertEquals(240_000L, DurationParser.toMillis("invalid"))
        assertEquals(240_000L, DurationParser.toMillis(""))
        assertEquals(240_000L, DurationParser.toMillis("1:02:03"))
    }
}
