package com.example.authapp.otp

import java.net.URI
import java.net.URISyntaxException
import java.net.URLDecoder

/**
 * The account details found in a setup QR code (an otpauth:// link).
 * [secret] is sensitive, so [toString] never prints it.
 */
class OtpAuthConfig(
    val label: String,
    val issuer: String?,
    val secret: ByteArray,
    val algorithm: OtpAlgorithm,
    val digits: Int,
    val periodSeconds: Int,
) {
    override fun toString(): String =
        "OtpAuthConfig(label=$label, issuer=$issuer, algorithm=$algorithm, digits=$digits, periodSeconds=$periodSeconds, secret=<hidden>)"
}

/** Reads links like otpauth://totp/Example:alice@example.com?secret=...&issuer=Example */
object OtpAuthUri {

    fun parse(uri: String): OtpAuthConfig {
        // Error messages never include [uri], because it contains the secret.
        val parsed = try {
            URI(uri.trim())
        } catch (e: URISyntaxException) {
            throw IllegalArgumentException("Invalid otpauth link")
        }
        require(parsed.scheme.equals("otpauth", ignoreCase = true)) { "Not an otpauth link" }
        require(parsed.host.equals("totp", ignoreCase = true) || parsed.authority.equals("totp", ignoreCase = true)) {
            "Only time-based (totp) accounts are supported"
        }

        val rawLabel = (parsed.rawPath ?: "").removePrefix("/")
        val label = decode(rawLabel)
        require(label.isNotBlank()) { "Missing account label" }

        val params = parseQuery(parsed.rawQuery)
        val secretText = params["secret"] ?: throw IllegalArgumentException("Missing secret")
        val secret = Base32.decode(secretText)

        val algorithm = when ((params["algorithm"] ?: "SHA1").uppercase()) {
            "SHA1" -> OtpAlgorithm.SHA1
            "SHA256" -> OtpAlgorithm.SHA256
            "SHA512" -> OtpAlgorithm.SHA512
            else -> throw IllegalArgumentException("Unsupported algorithm")
        }
        val digits = (params["digits"] ?: "6").toIntOrNull() ?: throw IllegalArgumentException("Invalid digits")
        require(digits in 6..8) { "Digits must be 6, 7 or 8" }
        val period = (params["period"] ?: "30").toIntOrNull() ?: throw IllegalArgumentException("Invalid period")
        require(period in 1..300) { "Period must be between 1 and 300 seconds" }

        // The issuer can be a parameter, or a prefix of the label ("Issuer:account").
        val issuer = params["issuer"] ?: label.substringBefore(':', missingDelimiterValue = "").ifBlank { null }

        return OtpAuthConfig(label, issuer, secret, algorithm, digits, period)
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrEmpty()) return emptyMap()
        val result = mutableMapOf<String, String>()
        for (pair in rawQuery.split('&')) {
            if (pair.isEmpty()) continue
            val key = decode(pair.substringBefore('=')).lowercase()
            val value = decode(pair.substringAfter('=', ""))
            result.putIfAbsent(key, value)
        }
        return result
    }

    private fun decode(text: String): String = try {
        URLDecoder.decode(text, "UTF-8")
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid encoding in otpauth link")
    }
}
