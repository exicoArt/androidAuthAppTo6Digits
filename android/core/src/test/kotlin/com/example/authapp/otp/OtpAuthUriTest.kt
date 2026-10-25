package com.example.authapp.otp

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OtpAuthUriTest {

    // A made-up example key, not a real account secret.
    private val exampleSecret = "JBSWY3DPEHPK3PXP"

    @Test
    fun parsesAFullLink() {
        val config = OtpAuthUri.parse(
            "otpauth://totp/Example:alice%40example.com?secret=$exampleSecret&issuer=Example&algorithm=SHA256&digits=8&period=60"
        )
        assertEquals("Example:alice@example.com", config.label)
        assertEquals("Example", config.issuer)
        assertEquals(OtpAlgorithm.SHA256, config.algorithm)
        assertEquals(8, config.digits)
        assertEquals(60, config.periodSeconds)
        assertContentEquals(Base32.decode(exampleSecret), config.secret)
    }

    @Test
    fun usesDefaultsAndTakesIssuerFromTheLabel() {
        val config = OtpAuthUri.parse("otpauth://totp/Acme:bob?secret=$exampleSecret")
        assertEquals(OtpAlgorithm.SHA1, config.algorithm)
        assertEquals(6, config.digits)
        assertEquals(30, config.periodSeconds)
        assertEquals("Acme", config.issuer)
    }

    @Test
    fun issuerIsNullWhenThereIsNone() {
        assertNull(OtpAuthUri.parse("otpauth://totp/bob?secret=$exampleSecret").issuer)
    }

    @Test
    fun rejectsUnsupportedOrBrokenLinks() {
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("https://example.com/?secret=$exampleSecret") }
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("otpauth://hotp/bob?secret=$exampleSecret&counter=1") }
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("otpauth://totp/bob") }
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("otpauth://totp/bob?secret=$exampleSecret&digits=4") }
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("otpauth://totp/bob?secret=$exampleSecret&algorithm=MD5") }
        assertFailsWith<IllegalArgumentException> { OtpAuthUri.parse("not a uri at all") }
    }

    @Test
    fun errorsAndToStringNeverContainTheSecret() {
        val error = assertFailsWith<IllegalArgumentException> {
            OtpAuthUri.parse("otpauth://totp/bob?secret=$exampleSecret&digits=4")
        }
        assertFalse(error.message.orEmpty().contains(exampleSecret))

        val text = OtpAuthUri.parse("otpauth://totp/bob?secret=$exampleSecret").toString()
        assertFalse(text.contains(exampleSecret))
        assertTrue(text.contains("<hidden>"))
    }
}
