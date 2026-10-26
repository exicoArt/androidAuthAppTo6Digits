package com.example.authapp.gate

import com.example.authapp.dnscheck.DnsCheckResult
import com.example.authapp.dnscheck.DnsVerdict
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SafeCodeGateTest {

    private val trapBase = "https://example.github.io/repo/trap/"

    private fun result(verdict: DnsVerdict, system: Set<String>, doh: Map<String, Set<String>>) =
        DnsCheckResult(verdict, "bank.example.com", system, doh, "test")

    private fun gate(verdict: DnsVerdict, tls: Boolean = false) = SafeCodeGate(
        trapPageBase = trapBase,
        check = { result(verdict, setOf("10.0.0.5"), mapOf("cloudflare" to setOf("203.0.113.7"))) },
        tlsCheck = { _, _ -> tls },
    )

    @Test
    fun allowsWhenAnswersAgree() {
        assertEquals(CodeGateDecision.Allow, gate(DnsVerdict.OK).decide("bank.example.com"))
    }

    @Test
    fun allowsWithNoticeWhenTheCheckCouldNotRun() {
        assertIs<CodeGateDecision.AllowWithNotice>(gate(DnsVerdict.UNKNOWN).decide("bank.example.com"))
    }

    @Test
    fun blocksHighRiskEvenIfTlsLooksFine() {
        assertIs<CodeGateDecision.Block>(gate(DnsVerdict.HIGH_RISK, tls = true).decide("bank.example.com"))
    }

    @Test
    fun blocksSuspiciousUntilTlsIsVerified() {
        assertIs<CodeGateDecision.Block>(gate(DnsVerdict.SUSPICIOUS, tls = false).decide("bank.example.com"))
        assertIs<CodeGateDecision.AllowWithNotice>(gate(DnsVerdict.SUSPICIOUS, tls = true).decide("bank.example.com"))
    }

    @Test
    fun trapUrlCarriesTheDomainAndBothAddresses() {
        val decision = gate(DnsVerdict.HIGH_RISK).decide("bank.example.com") as CodeGateDecision.Block
        assertEquals(
            "https://example.github.io/repo/trap/?domain=bank.example.com&expected=203.0.113.7&seen=10.0.0.5",
            decision.trapUrl,
        )
    }

    @Test
    fun trapUrlShowsTheAddressThatIsNotInTheTrustedAnswers() {
        // The device also got the real address, but sorted order would pick the private one first
        // only by luck. Here the odd one out sorts last, and must still be the one shown.
        val g = SafeCodeGate(
            trapPageBase = trapBase,
            check = {
                result(
                    DnsVerdict.HIGH_RISK,
                    setOf("203.0.113.7", "9.9.9.9"),
                    mapOf("cloudflare" to setOf("203.0.113.7")),
                )
            },
        )
        val block = g.decide("bank.example.com") as CodeGateDecision.Block
        assertTrue(block.trapUrl.contains("seen=9.9.9.9"), block.trapUrl)
        assertTrue(block.trapUrl.contains("expected=203.0.113.7"), block.trapUrl)
    }

    @Test
    fun trapUrlOmitsMissingAddressesAndKeepsExistingQueryStrings() {
        val g = SafeCodeGate(
            trapPageBase = "https://example.com/trap?lang=en",
            check = { result(DnsVerdict.HIGH_RISK, emptySet(), emptyMap()) },
        )
        val block = g.decide("bank.example.com") as CodeGateDecision.Block
        assertEquals("https://example.com/trap?lang=en&domain=bank.example.com", block.trapUrl)
        assertTrue(!block.trapUrl.contains("expected="))
    }
}
