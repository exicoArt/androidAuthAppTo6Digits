package com.example.authapp.gate

import com.example.authapp.dnscheck.DnsCheckResult
import com.example.authapp.dnscheck.DnsMismatchCheck
import com.example.authapp.dnscheck.DnsVerdict
import java.net.URLEncoder

sealed interface CodeGateDecision {
    /** Nothing suspicious. Show the code. */
    data object Allow : CodeGateDecision

    /** Show the code, with a visible notice. */
    data class AllowWithNotice(val message: String) : CodeGateDecision

    /** Do not show or accept a code for this site. Send the user to [trapUrl] instead. */
    data class Block(val trapUrl: String, val result: DnsCheckResult) : CodeGateDecision
}

/**
 * Decides whether the app should show a 6-digit code for a site, based on the DNS check
 * (see docs/dns-mismatch-trap.md).
 *
 * @param trapPageBase address of the warning page, for example https://example.github.io/repo/trap/
 * @param check how to compare DNS answers. Replaceable so it can be tested without a network.
 * @param tlsCheck returns true if the site's TLS certificate is valid for the domain at the given
 *   addresses. The default says "not verified", so answers that differ are blocked until a real
 *   check is plugged in.
 */
class SafeCodeGate(
    private val trapPageBase: String,
    private val check: (String) -> DnsCheckResult = DnsMismatchCheck::check,
    private val tlsCheck: (domain: String, addresses: Set<String>) -> Boolean = { _, _ -> false },
) {

    fun decide(domain: String): CodeGateDecision {
        val result = check(domain)
        return when (result.verdict) {
            DnsVerdict.OK -> CodeGateDecision.Allow
            DnsVerdict.UNKNOWN -> CodeGateDecision.AllowWithNotice(
                "We could not double-check this site's address. Make sure you typed or opened it yourself."
            )
            DnsVerdict.SUSPICIOUS ->
                if (tlsCheck(result.domain, result.systemIps)) {
                    CodeGateDecision.AllowWithNotice(
                        "This network gave a different address than usual, but the site's certificate checks out."
                    )
                } else {
                    CodeGateDecision.Block(trapUrl(result), result)
                }
            DnsVerdict.HIGH_RISK -> CodeGateDecision.Block(trapUrl(result), result)
        }
    }

    internal fun trapUrl(result: DnsCheckResult): String {
        val expected = result.dohIps.values.flatten().sorted().firstOrNull()
        val seen = result.systemIps.sorted().firstOrNull()
        val params = buildList {
            add("domain=" + enc(result.domain))
            if (expected != null) add("expected=" + enc(expected))
            if (seen != null) add("seen=" + enc(seen))
        }
        val separator = if (trapPageBase.contains('?')) "&" else "?"
        return trapPageBase + separator + params.joinToString("&")
    }

    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")
}
