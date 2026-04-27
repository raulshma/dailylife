package com.raulshma.dailylife.domain

import android.net.Uri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

private val MediaUriTokenPattern = Regex("""(?i)(?:https?|content|file)://[^\s<>()]+""")
private val DataUriPattern = Regex("""(?i)data:(?:image|video|audio)/[^\s<>()]+""")

private val GeoTypePattern =
    Regex("""geo:\s*[-+]?\d{1,2}(?:\.\d+)?,\s*[-+]?\d{1,3}(?:\.\d+)?""", RegexOption.IGNORE_CASE)
private val OsmMlatMlonPattern =
    Regex("""[?&]mlat=([-+]?\d{1,2}(?:\.\d+)?).*?[?&]mlon=([-+]?\d{1,3}(?:\.\d+)?)""", RegexOption.IGNORE_CASE)

private val ImageExtensionPattern = Regex("""\.(?:png|jpe?g|webp|gif|bmp|avif)(?:\.enc)?$""", RegexOption.IGNORE_CASE)
private val VideoExtensionPattern = Regex("""\.(?:mp4|m4v|webm|mkv|mov|m3u8)(?:\.enc)?$""", RegexOption.IGNORE_CASE)
private val AudioExtensionPattern = Regex("""\.(?:mp3|aac|wav|ogg|m4a|flac)(?:\.enc)?$""", RegexOption.IGNORE_CASE)

internal fun inferTypeFromText(text: String): LifeItemType? {
    val source = text.trim()
    if (source.isBlank()) return null

    if (GeoTypePattern.containsMatchIn(source) || OsmMlatMlonPattern.containsMatchIn(source)) {
        return LifeItemType.Location
    }

    val detectedTypes = linkedSetOf<LifeItemType>()
    extractUriCandidates(source).forEach { candidate ->
        inferTypeFromUriToken(candidate)?.let { detectedTypes += it }
    }

    return when (detectedTypes.size) {
        0 -> null
        1 -> detectedTypes.first()
        else -> LifeItemType.Mixed
    }
}

internal fun firstInferredUri(source: String): String? =
    extractUriCandidates(source).firstOrNull()

internal fun firstInferredUriByType(source: String, target: LifeItemType): String? =
    extractUriCandidates(source).firstOrNull { candidate ->
        inferTypeFromUriToken(candidate) == target
    }

private fun extractUriCandidates(source: String): List<String> = buildList {
    MediaUriTokenPattern.findAll(source).forEach { add(it.value.trimUriTokenPunctuation()) }
    DataUriPattern.findAll(source).forEach { add(it.value.trimUriTokenPunctuation()) }
}
    .filter { it.isNotBlank() }
    .distinct()

private fun inferTypeFromUriToken(rawToken: String): LifeItemType? {
    val token = rawToken.trimUriTokenPunctuation()
    if (token.isBlank()) return null

    val normalized = token.lowercase()

    if (normalized.startsWith("geo:") && GeoTypePattern.containsMatchIn(token)) {
        return LifeItemType.Location
    }

    if (normalized.contains("maps.google") ||
        normalized.contains("google.com/maps") ||
        normalized.contains("openstreetmap.org") ||
        normalized.contains("apple.com/maps") ||
        OsmMlatMlonPattern.containsMatchIn(token)
    ) {
        return LifeItemType.Location
    }

    if (normalized.startsWith("data:image/")) return LifeItemType.Photo
    if (normalized.startsWith("data:video/")) return LifeItemType.Video
    if (normalized.startsWith("data:audio/")) return LifeItemType.Audio

    val uri = runCatching { Uri.parse(token) }.getOrNull()
    val path = uri?.path?.lowercase().orEmpty()
    val lastSegment = uri?.lastPathSegment?.lowercase().orEmpty()
    val plainToken = normalized.substringBefore('?').substringBefore('#')
    val rawQuery = uri?.encodedQuery
        ?.takeIf { it.isNotBlank() }
        ?: token.substringAfter('?', "")
            .substringBefore('#')
            .takeIf { it.isNotBlank() }
    val decodedQuery = rawQuery
        ?.let { runCatching { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }.getOrDefault(it) }
        ?.lowercase()
        .orEmpty()
    val analysisInput = listOf(normalized, path, lastSegment, plainToken, decodedQuery)
        .joinToString(" ")

    val hasImageExtension = ImageExtensionPattern.containsMatchIn(path) ||
        ImageExtensionPattern.containsMatchIn(lastSegment) ||
        ImageExtensionPattern.containsMatchIn(plainToken)
    val hasVideoExtension = VideoExtensionPattern.containsMatchIn(path) ||
        VideoExtensionPattern.containsMatchIn(lastSegment) ||
        VideoExtensionPattern.containsMatchIn(plainToken)
    val hasAudioExtension = AudioExtensionPattern.containsMatchIn(path) ||
        AudioExtensionPattern.containsMatchIn(lastSegment) ||
        AudioExtensionPattern.containsMatchIn(plainToken)

    if (hasImageExtension) return LifeItemType.Photo
    if (hasVideoExtension) return LifeItemType.Video
    if (hasAudioExtension) return LifeItemType.Audio

    if (analysisInput.contains("youtube.com") || analysisInput.contains("youtu.be") || analysisInput.contains("vimeo.com")) {
        return LifeItemType.Video
    }

    val isLocalUri = normalized.startsWith("content://") || normalized.startsWith("file://")
    if (isLocalUri || normalized.startsWith("http://") || normalized.startsWith("https://")) {
        if (analysisInput.contains("image/")) return LifeItemType.Photo
        if (analysisInput.contains("video/")) return LifeItemType.Video
        if (analysisInput.contains("audio/")) return LifeItemType.Audio

        if (analysisInput.contains("mimetype=image") || analysisInput.contains("type=image")) return LifeItemType.Photo
        if (analysisInput.contains("mimetype=video") || analysisInput.contains("type=video")) return LifeItemType.Video
        if (analysisInput.contains("mimetype=audio") || analysisInput.contains("type=audio")) return LifeItemType.Audio

        if (analysisInput.contains("/images/") || analysisInput.contains("/image/")) return LifeItemType.Photo
        if (analysisInput.contains("/videos/") || analysisInput.contains("/video/")) return LifeItemType.Video
        if (analysisInput.contains("/audio/") || analysisInput.contains("/audios/")) return LifeItemType.Audio
    }

    return null
}

private fun String.trimUriTokenPunctuation(): String =
    trim()
        .trim('"', '\'', '`', '(', '[', '{', '<')
        .trimEnd('"', '\'', '`', '.', ',', ';', ':', '!', '?', ')', ']', '}', '>')
