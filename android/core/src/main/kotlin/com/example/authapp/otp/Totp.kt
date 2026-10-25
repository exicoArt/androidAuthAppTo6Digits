package com.example.authapp.otp

import java.nio.ByteBuffer
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

enum class OtpAlgorithm(val macName: String) {
    SHA1("HmacSHA1"),
    SHA256("HmacSHA256"),
    SHA512("HmacSHA512"),
}

/** Time-based one-time passwords (RFC 6238) built on HOTP (RFC 4226). Defaults give the usual 6 digits every 30 seconds. */
object Totp {

    /** The code that is valid at [timeMillis] (milliseconds since the Unix epoch). */
    fun generate(
        secret: ByteArray,
        timeMillis: Long,
        algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
        digits: Int = 6,
        periodSeconds: Int = 30,
    ): String {
        require(timeMillis >= 0) { "Time must not be negative" }
        require(periodSeconds > 0) { "Period must be positive" }
        return hotp(secret, counterAt(timeMillis, periodSeconds), algorithm, digits)
    }

    /** Seconds until the current code stops being valid, from 1 to [periodSeconds]. */
    fun secondsRemaining(timeMillis: Long, periodSeconds: Int = 30): Int {
        require(periodSeconds > 0) { "Period must be positive" }
        return (periodSeconds - ((timeMillis / 1000) % periodSeconds)).toInt()
    }

    fun counterAt(timeMillis: Long, periodSeconds: Int = 30): Long = timeMillis / 1000 / periodSeconds

    /**
     * Checks a code the way a server would. Accepts the current step plus or minus [window] steps
     * to allow for clock drift. Returns the matched step so the caller can store it, or null if
     * there is no match. Pass the last accepted step as [lastAcceptedCounter] so a code can
     * never be used twice.
     */
    fun verify(
        secret: ByteArray,
        code: String,
        timeMillis: Long,
        algorithm: OtpAlgorithm = OtpAlgorithm.SHA1,
        digits: Int = 6,
        periodSeconds: Int = 30,
        window: Int = 1,
        lastAcceptedCounter: Long = -1,
    ): Long? {
        require(window >= 0) { "Window must not be negative" }
        val now = counterAt(timeMillis, periodSeconds)
        val candidate = code.toByteArray(Charsets.UTF_8)
        var matched: Long? = null
        for (step in (now - window)..(now + window)) {
            if (step < 0 || step <= lastAcceptedCounter) continue
            val expected = hotp(secret, step, algorithm, digits).toByteArray(Charsets.UTF_8)
            // Constant-time comparison. Keep checking every step so timing does not reveal which one matched.
            if (MessageDigest.isEqual(expected, candidate) && matched == null) {
                matched = step
            }
        }
        return matched
    }

    fun hotp(secret: ByteArray, counter: Long, algorithm: OtpAlgorithm = OtpAlgorithm.SHA1, digits: Int = 6): String {
        require(secret.isNotEmpty()) { "Secret must not be empty" }
        require(digits in 6..8) { "Digits must be 6, 7 or 8" }
        require(counter >= 0) { "Counter must not be negative" }

        val message = ByteBuffer.allocate(8).putLong(counter).array()
        val mac = Mac.getInstance(algorithm.macName)
        mac.init(SecretKeySpec(secret, algorithm.macName))
        val hash = mac.doFinal(message)

        // Dynamic truncation (RFC 4226, section 5.3).
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
            ((hash[offset + 1].toInt() and 0xFF) shl 16) or
            ((hash[offset + 2].toInt() and 0xFF) shl 8) or
            (hash[offset + 3].toInt() and 0xFF)

        var modulus = 1
        repeat(digits) { modulus *= 10 }
        return (binary % modulus).toString().padStart(digits, '0')
    }
}
