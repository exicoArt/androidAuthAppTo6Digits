package com.example.authapp.otp

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class TotpTest {

    // RFC 6238, appendix B. Each algorithm has its own test seed.
    private val sha1Seed = "12345678901234567890".toByteArray()
    private val sha256Seed = "12345678901234567890123456789012".toByteArray()
    private val sha512Seed = "1234567890123456789012345678901234567890123456789012345678901234".toByteArray()

    // time in seconds -> expected 8-digit codes for SHA1, SHA256, SHA512
    private val vectors = listOf(
        Triple(59L, "94287082", Pair("46119246", "90693936")),
        Triple(1111111109L, "07081804", Pair("68084774", "25091201")),
        Triple(1111111111L, "14050471", Pair("67062674", "99943326")),
        Triple(1234567890L, "89005924", Pair("91819424", "93441116")),
        Triple(2000000000L, "69279037", Pair("90698825", "38618901")),
        Triple(20000000000L, "65353130", Pair("77737706", "47863826")),
    )

    @Test
    fun matchesRfc6238VectorsForAllAlgorithms() {
        for ((seconds, sha1, others) in vectors) {
            val ms = seconds * 1000
            assertEquals(sha1, Totp.generate(sha1Seed, ms, OtpAlgorithm.SHA1, digits = 8), "SHA1 at $seconds")
            assertEquals(others.first, Totp.generate(sha256Seed, ms, OtpAlgorithm.SHA256, digits = 8), "SHA256 at $seconds")
            assertEquals(others.second, Totp.generate(sha512Seed, ms, OtpAlgorithm.SHA512, digits = 8), "SHA512 at $seconds")
        }
    }

    @Test
    fun sixDigitCodesAreTheLastSixOfTheEightDigitCodes() {
        for ((seconds, sha1, _) in vectors) {
            assertEquals(sha1.takeLast(6), Totp.generate(sha1Seed, seconds * 1000))
        }
        assertEquals("287082", Totp.generate(sha1Seed, 59_000))
    }

    @Test
    fun keepsLeadingZeros() {
        assertEquals("081804", Totp.generate(sha1Seed, 1111111109L * 1000))
    }

    @Test
    fun codeIsStableWithinAStepAndChangesAtTheBoundary() {
        assertEquals(Totp.generate(sha1Seed, 30_000), Totp.generate(sha1Seed, 59_999))
        assertEquals(Totp.generate(sha1Seed, 59_000), Totp.generate(sha1Seed, 30_000))
        assert(Totp.generate(sha1Seed, 59_999) != Totp.generate(sha1Seed, 60_000))
    }

    @Test
    fun secondsRemainingCountsDownFromThePeriod() {
        assertEquals(30, Totp.secondsRemaining(0))
        assertEquals(1, Totp.secondsRemaining(29_000))
        assertEquals(30, Totp.secondsRemaining(30_000))
        assertEquals(15, Totp.secondsRemaining(45_000))
    }

    @Test
    fun verifyAcceptsCurrentAndNeighbouringStepsOnly() {
        val now = 1111111109L * 1000
        val counter = Totp.counterAt(now)
        val current = Totp.generate(sha1Seed, now)
        val previous = Totp.generate(sha1Seed, now - 30_000)
        val tooOld = Totp.generate(sha1Seed, now - 90_000)

        assertEquals(counter, Totp.verify(sha1Seed, current, now))
        assertEquals(counter - 1, Totp.verify(sha1Seed, previous, now))
        assertNull(Totp.verify(sha1Seed, tooOld, now))
        assertNull(Totp.verify(sha1Seed, "000000", now))
    }

    @Test
    fun verifyRejectsReplayOfAnAcceptedStep() {
        val now = 1111111109L * 1000
        val code = Totp.generate(sha1Seed, now)
        val first = Totp.verify(sha1Seed, code, now)!!
        assertNull(Totp.verify(sha1Seed, code, now, lastAcceptedCounter = first))
    }

    @Test
    fun rejectsBadParameters() {
        assertFailsWith<IllegalArgumentException> { Totp.generate(ByteArray(0), 0) }
        assertFailsWith<IllegalArgumentException> { Totp.generate(sha1Seed, 0, digits = 5) }
        assertFailsWith<IllegalArgumentException> { Totp.generate(sha1Seed, 0, digits = 9) }
        assertFailsWith<IllegalArgumentException> { Totp.generate(sha1Seed, -1) }
        assertFailsWith<IllegalArgumentException> { Totp.generate(sha1Seed, 0, periodSeconds = 0) }
    }
}
