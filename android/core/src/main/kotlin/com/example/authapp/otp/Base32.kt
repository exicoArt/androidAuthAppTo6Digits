package com.example.authapp.otp

import java.io.ByteArrayOutputStream

/** RFC 4648 Base32, which is how authenticator setup keys are written. */
object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    /**
     * Decodes a setup key. Case, spaces, hyphens and "=" padding are ignored.
     * Error messages never include the input, because it is a secret.
     */
    fun decode(input: String): ByteArray {
        val clean = input.uppercase().filter { it != ' ' && it != '-' && it != '=' }
        require(clean.isNotEmpty()) { "Empty Base32 string" }

        val out = ByteArrayOutputStream()
        var buffer = 0
        var bits = 0
        for (ch in clean) {
            val value = ALPHABET.indexOf(ch)
            require(value >= 0) { "Invalid Base32 character" }
            buffer = (buffer shl 5) or value
            bits += 5
            if (bits >= 8) {
                bits -= 8
                out.write((buffer shr bits) and 0xFF)
                buffer = buffer and ((1 shl bits) - 1)
            }
        }
        return out.toByteArray()
    }

    /** Encodes bytes without padding. */
    fun encode(data: ByteArray): String {
        val sb = StringBuilder()
        var buffer = 0
        var bits = 0
        for (b in data) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bits += 8
            while (bits >= 5) {
                bits -= 5
                sb.append(ALPHABET[(buffer shr bits) and 0x1F])
            }
            buffer = buffer and ((1 shl bits) - 1)
        }
        if (bits > 0) {
            sb.append(ALPHABET[(buffer shl (5 - bits)) and 0x1F])
        }
        return sb.toString()
    }
}
