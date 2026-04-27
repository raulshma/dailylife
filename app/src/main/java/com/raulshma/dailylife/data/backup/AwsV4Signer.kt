package com.raulshma.dailylife.data.backup

import okhttp3.Request
import okio.Buffer
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * AWS Signature Version 4 signer for S3-compatible requests.
 * This is a minimal implementation suitable for PUT/GET/DELETE operations.
 */
class AwsV4Signer(
    private val accessKeyId: String,
    private val secretAccessKey: String,
    private val region: String,
    private val service: String = "s3",
) {

    fun sign(request: Request, payload: ByteArray = ByteArray(0)): Request {
        return signWithPayloadHash(request, sha256Hex(payload))
    }

    fun sign(request: Request, payloadHash: String): Request {
        return signWithPayloadHash(request, payloadHash)
    }

    private fun signWithPayloadHash(request: Request, hash: String): Request {
        val now = Instant.now().atZone(ZoneOffset.UTC)
        val dateStamp = DateFormatter.format(now)
        val amzDate = AmzDateFormatter.format(now)

        val builder = request.newBuilder()
            .header("x-amz-date", amzDate)
            .header("x-amz-content-sha256", hash)

        if (request.header("Host") == null) {
            builder.header("Host", request.url.host)
        }

        val signedRequest = builder.build()

        val canonicalRequest = buildCanonicalRequest(signedRequest, hash)
        val credentialScope = "$dateStamp/$region/$service/aws4_request"
        val stringToSign = buildStringToSign(amzDate, credentialScope, canonicalRequest)
        val signingKey = deriveSigningKey(secretAccessKey, dateStamp, region, service)
        val signature = hmacSha256Hex(signingKey, stringToSign)

        val authorizationHeader =
            "AWS4-HMAC-SHA256 Credential=$accessKeyId/$credentialScope, " +
                "SignedHeaders=${signedHeaders(signedRequest)}, " +
                "Signature=$signature"

        return signedRequest.newBuilder()
            .header("Authorization", authorizationHeader)
            .build()
    }

    private fun buildCanonicalRequest(request: Request, payloadHash: String): String {
        val method = request.method
        val canonicalUri = request.url.encodedPath.ifEmpty { "/" }
        val canonicalQueryString = canonicalQueryString(request)
        val canonicalHeaders = canonicalHeaders(request)
        val signedHeaders = signedHeaders(request)

        return "$method\n$canonicalUri\n$canonicalQueryString\n$canonicalHeaders\n$signedHeaders\n$payloadHash"
    }

    private fun canonicalQueryString(request: Request): String {
        val queryParams = request.url.queryParameterNames.sorted()
        return queryParams.joinToString("&") { name ->
            val values = request.url.queryParameterValues(name).filterNotNull().sorted()
            values.joinToString("&") { value ->
                "${urlEncode(name)}=${urlEncode(value)}"
            }
        }
    }

    private fun canonicalHeaders(request: Request): String {
        val headers = request.headers
        val sortedNames = headers.names().sorted()
        return sortedNames.joinToString("") { name ->
            val values = headers.values(name).joinToString(",") { it.trim() }
            "${name.lowercase(Locale.US)}:$values\n"
        }
    }

    private fun signedHeaders(request: Request): String {
        return request.headers.names().sorted()
            .joinToString(";") { it.lowercase(Locale.US) }
    }

    private fun buildStringToSign(amzDate: String, credentialScope: String, canonicalRequest: String): String {
        val hashedCanonicalRequest = sha256Hex(canonicalRequest.toByteArray(StandardCharsets.UTF_8))
        return "AWS4-HMAC-SHA256\n$amzDate\n$credentialScope\n$hashedCanonicalRequest"
    }

    private fun deriveSigningKey(secretKey: String, dateStamp: String, region: String, service: String): ByteArray {
        val kSecret = ("AWS4$secretKey").toByteArray(StandardCharsets.UTF_8)
        val kDate = hmacSha256(kSecret, dateStamp)
        val kRegion = hmacSha256(kDate, region)
        val kService = hmacSha256(kRegion, service)
        return hmacSha256(kService, "aws4_request")
    }

    private fun hmacSha256(key: ByteArray, data: String): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data.toByteArray(StandardCharsets.UTF_8))
    }

    private fun hmacSha256Hex(key: ByteArray, data: String): String {
        return hmacSha256(key, data).toHex()
    }

    private fun sha256Hex(data: ByteArray): String {
        return MessageDigest.getInstance("SHA-256").digest(data).toHex()
    }

    private fun urlEncode(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.name())
            .replace("+", "%20")
    }

    private fun ByteArray.toHex(): String {
        return joinToString("") { "%02x".format(it) }
    }

    companion object {
        private val DateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC)
        private val AmzDateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
    }
}
