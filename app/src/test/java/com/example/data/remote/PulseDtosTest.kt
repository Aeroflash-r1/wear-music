package com.example.data.remote

import com.example.data.remote.dto.PulseSearchResponse
import com.example.data.remote.dto.PulseStream
import com.example.data.remote.dto.PulseTrack
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PulseDtosTest {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Test
    fun `parses search response with items`() {
        val raw = """
            {
              "items": [
                {"id": "abc123", "title": "Song One", "artist": "Artist A", "duration": "3:30", "thumbnailUrl": "http://t/1.jpg", "type": "song"},
                {"id": "def456", "title": "Song Two", "artist": "Artist B", "duration": "2:45", "type": "album"}
              ]
            }
        """.trimIndent()

        val response = moshi.adapter(PulseSearchResponse::class.java).fromJson(raw)!!

        assertEquals(2, response.items!!.size)
        assertEquals("abc123", response.items[0].id)
        assertEquals("Song One", response.items[0].title)
        assertEquals("Artist A", response.items[0].artist)
        assertEquals("3:30", response.items[0].duration)
        assertEquals("http://t/1.jpg", response.items[0].thumbnailUrl)
        assertEquals("song", response.items[0].type)
        assertEquals("album", response.items[1].type)
    }

    @Test
    fun `parses stream response`() {
        val raw = """{"audioUrl": "https://rr.example/videoplayback?x=1", "bitrate": 128000, "mimeType": "audio/mp4", "sampleRate": 44100}"""
        val stream = moshi.adapter(PulseStream::class.java).fromJson(raw)!!
        assertEquals("https://rr.example/videoplayback?x=1", stream.audioUrl)
        assertEquals(128000, stream.bitrate)
        assertEquals("audio/mp4", stream.mimeType)
        assertEquals(44100, stream.sampleRate)
    }

    @Test
    fun `parses track with optional fields omitted`() {
        val raw = """{"id": "abc", "title": "T", "artist": "A", "duration": "1:00"}"""
        val track = moshi.adapter(PulseTrack::class.java).fromJson(raw)!!
        assertEquals("abc", track.id)
        assertNull(track.album)
        assertNull(track.streamUrl)
        assertNull(track.relatedTracks)
        assertTrue(track.thumbnailUrl == null)
    }
}
