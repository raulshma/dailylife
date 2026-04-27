package com.raulshma.dailylife.domain

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ContentInferenceTest {

    @Test
    fun inferTypeFromText_mixedMediaLinks_returnsMixed() {
        val inferred = inferTypeFromText("https://example.com/photo.png and https://example.com/voice.ogg")

        assertEquals(LifeItemType.Mixed, inferred)
    }

    @Test
    fun inferImagePreviewUrl_detectsMimeHintedContentUri() {
        val item = testItem(
            body = "content://com.android.providers.media.documents/document/12345?mimeType=image%2Fjpeg",
            type = LifeItemType.Note,
        )

        assertEquals(
            "content://com.android.providers.media.documents/document/12345?mimeType=image%2Fjpeg",
            item.inferImagePreviewUrl(),
        )
    }

    @Test
    fun inferAudioUrl_detectsEmbeddedAudioLinkWithPunctuation() {
        val item = testItem(
            body = "Save this memo: (https://example.com/voice-note.m4a), thanks.",
            type = LifeItemType.Note,
        )

        assertEquals("https://example.com/voice-note.m4a", item.inferAudioUrl())
    }

    @Test
    fun inferVideoPlaybackUrl_fallsBackWhenExplicitVideoType() {
        val raw = "content://media/external/file/734"
        val item = testItem(
            body = raw,
            type = LifeItemType.Video,
        )

        assertEquals(raw, item.inferVideoPlaybackUrl())
    }

    @Test
    fun inferAudioUrl_fallbackWithTranscript_returnsEmbeddedUriOnly() {
        val raw = "content://media/external/audio/media/42"
        val item = testItem(
            body = "Need to buy eggs and basil.\n$raw",
            type = LifeItemType.Audio,
        )

        assertEquals(raw, item.inferAudioUrl())
    }

    @Test
    fun firstInferredUri_returnsActualToken_notWholeBody() {
        val raw = "file:///storage/emulated/0/DCIM/Camera/video_clip"
        val uri = firstInferredUri("Walk recap: $raw")

        assertEquals(raw, uri)
    }

    @Test
    fun inferTypeFromText_unknownContentUri_returnsNull() {
        val inferred = inferTypeFromText("content://com.android.providers.media.documents/document/12345")

        assertNull(inferred)
    }

    private fun testItem(body: String, type: LifeItemType): LifeItem =
        LifeItem(
            id = 99L,
            type = type,
            title = "",
            body = body,
            createdAt = LocalDateTime.of(2026, 1, 1, 10, 0),
        )
}
