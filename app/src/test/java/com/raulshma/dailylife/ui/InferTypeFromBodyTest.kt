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
}