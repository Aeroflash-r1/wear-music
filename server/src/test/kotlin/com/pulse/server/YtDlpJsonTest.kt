package com.pulse.server

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class YtDlpJsonTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `parses flat playlist json`() {
        val raw = """
            {
              "_type": "playlist",
              "id": "PL123",
              "title": "My Playlist",
              "uploader": "Someone",
              "entries": [
                {"_type": "url", "id": "abc123", "title": "Song One", "url": "https://www.youtube.com/watch?v=abc123", "duration": 210, "uploader": "Artist A"},
                {"_type": "url", "id": "def456", "title": "Song Two", "url": "https://www.youtube.com/watch?v=def456", "duration": 180}
              ]
            }
        """.trimIndent()
        val playlist = json.decodeFromString<YtDlpPlaylist>(raw)
        assertEquals("PL123", playlist.id)
        assertEquals("My Playlist", playlist.title)
        assertEquals(2, playlist.entries?.size)
        assertEquals("abc123", playlist.entries?.first()?.id)
        assertEquals(210L, playlist.entries?.first()?.duration)
        assertNull(playlist.entries?.get(1)?.uploader)
    }

    @Test
    fun `parses single video json with audio stream fields`() {
        val raw = """
            {
              "id": "BSTsnWoslP4",
              "title": "Bohemian Rhapsody",
              "uploader": "Queen Official",
              "duration": 355,
              "thumbnail": "https://i.ytimg.com/vi/BSTsnWoslP4/maxresdefault.jpg",
              "url": "https://rr.example/videoplayback?expire=1",
              "ext": "m4a",
              "abr": 129.0,
              "asr": 44100
            }
        """.trimIndent()
        val video = json.decodeFromString<YtDlpVideo>(raw)
        assertEquals("BSTsnWoslP4", video.id)
        assertEquals(355L, video.duration)
        assertEquals("m4a", video.ext)
        assertEquals(129.0, video.abr)
        assertEquals(44100L, video.asr)
        assertNotNull(video.url)
    }

    @Test
    fun `parses search line-delimited videos`() {
        val raw = """
            {"id": "one", "title": "A", "uploader": "U1", "duration": 60, "thumbnail": "t1.jpg"}
            {"id": "two", "title": "B", "uploader": "U2", "duration": 90}
            {"id": "three", "title": "C", "duration": 30}
        """.trimIndent()
        val videos = raw.lineSequence()
            .filter { it.isNotBlank() }
            .map { json.decodeFromString<YtDlpVideo>(it) }
            .toList()
        assertEquals(3, videos.size)
        assertEquals("one", videos[0].id)
        assertNull(videos[2].uploader)
    }

    @Test
    fun `tolerates missing fields and extra keys`() {
        val raw = """{"id": "x", "unknown_key": [1,2,3], "extra": {"nested": true}}"""
        val video = json.decodeFromString<YtDlpVideo>(raw)
        assertEquals("x", video.id)
        assertNull(video.title)
    }
}
