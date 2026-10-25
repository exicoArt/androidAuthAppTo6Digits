package com.example.authapp.otp

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class Base32Test {

    @Test
    fun encodesKnownValues() {
        assertEquals("MZXW6YTBOI", Base32.encode("foobar".toByteArray()))
        assertEquals("MZXW6YTB", Base32.encode("fooba".toByteArray()))
        assertEquals("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ", Base32.encode("12345678901234567890".toByteArray()))
    }

    @Test
    fun decodesKnownValues() {
        assertContentEquals("foobar".toByteArray(), Base32.decode("MZXW6YTBOI======"))
        assertContentEquals("12345678901234567890".toByteArray(), Base32.decode("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"))
    }

    @Test
    fun ignoresCaseSpacesHyphensAndPadding() {
        assertContentEquals("foobar".toByteArray(), Base32.decode("mzxw 6ytb-oi=="))
    }

    @Test
    fun roundTripsRandomLengths() {
        for (length in 1..40) {
            val data = ByteArray(length) { (it * 31 + length).toByte() }
            assertContentEquals(data, Base32.decode(Base32.encode(data)))
        }
    }

    @Test
    fun rejectsInvalidInputWithoutEchoingIt() {
        val error = assertFailsWith<IllegalArgumentException> { Base32.decode("MZXW1YTB") } // "1" is not Base32
        assertEquals("Invalid Base32 character", error.message)
        assertFailsWith<IllegalArgumentException> { Base32.decode("   ") }
    }
}
