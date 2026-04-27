package com.raulshma.dailylife.ui

import com.raulshma.dailylife.domain.LifeItemType
import org.junit.Assert.assertEquals
import org.junit.Test

class InferTypeFromBodyTest {

    @Test
    fun unknownContentUri_isNotForcedToMixed() {
        val inferred = inferTypeFromBody("content://com.android.providers.media.documents/document/12345")

        assertEquals(null, inferred)
    }

    @Test
    fun imageContentUri_isDetectedAsPhoto() {
        val inferred = inferTypeFromBody("content://media/external/images/media/photo_1.jpg")

        assertEquals(LifeItemType.Photo, inferred)
    }

    @Test
    fun videoContentUri_isDetectedAsVideo() {
        val inferred = inferTypeFromBody("file:///storage/emulated/0/DCIM/Camera/clip_1.mp4")

        assertEquals(LifeItemType.Video, inferred)
    }

    @Test
    fun embeddedImageUrlWithPunctuation_isDetectedAsPhoto() {
        val inferred = inferTypeFromBody("Check this out (https://cdn.example.com/pic_01.JPG?size=large), looks great!")

        assertEquals(LifeItemType.Photo, inferred)
    }

    @Test
    fun httpAudioUrl_isDetectedAsAudio() {
        val inferred = inferTypeFromBody("https://example.com/voice-note.m4a")

        assertEquals(LifeItemType.Audio, inferred)
    }

    @Test
    fun mimeHintContentUriWithoutExtension_isDetectedAsPhoto() {
        val inferred = inferTypeFromBody("content://com.android.providers.media.documents/document/12345?mimeType=image%2Fjpeg")

        assertEquals(LifeItemType.Photo, inferred)
    }

    @Test
    fun mapUrl_isDetectedAsLocation() {
        val inferred = inferTypeFromBody("https://www.openstreetmap.org/?mlat=40.73061&mlon=-73.935242#map=14/40.73061/-73.935242")

        assertEquals(LifeItemType.Location, inferred)
    }

    @Test
    fun mixedMediaLinks_areDetectedAsMixed() {
        val inferred = inferTypeFromBody("https://example.com/photo.png and https://example.com/voice.ogg")

        assertEquals(LifeItemType.Mixed, inferred)
    }
}