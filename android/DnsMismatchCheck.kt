package com.example.authapp.dnscheck

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.Inet4Address
import java.net.InetAddress
import java.net.URL
import java.net.URLEncoder

/**
 * REFERENCE SKETCH, not production code and not compiled or tested yet.
 *
 * Compares what the device's normal resolver returns for a domain with what
 * encrypted DNS-over-HTTPS (DoH) resolvers return. Run it off the main thread.
 *
 * A mismatch is a SIGNAL, not proof (CDNs, geo-DNS and captive portals also
 * cause it). See docs/dns-mismatch-trap.md for how to score the result and
 * add a TLS certificate check before showing the warning page.
 */
enum class DnsVerdict { OK, SUSPICIOUS, HIGH_RISK, UNKNOWN }

data class DnsCheckResult(
    val verdict: DnsVerdict,
    val domain: String,
    val systemIps: Set<String>,
    val dohIps: Map<String, Set<String>>,
    val reason: String,
)

object DnsMismatchCheck {

    // Name -> JSON endpoint. Add more independent resolvers if you can.
    private val resolvers = mapOf(
        "cloudflare" to "https://cloudflare-dns.com/dns-query",
        "google" to "https://dns.google/resolve",
    )

    private val hostnameRegex =
        Regex("^(?=.{1,253}$)([A-Za-z0-9]([A-Za-z0-9-]{0,61}[A-Za-z0-9])?\\.)+[A-Za-z]{2,63}$")

    fun check(domain: String): DnsCheckResult {
        val d = domain.trim().lowercase()
        require(hostnameRegex.matches(d)) { "Not a valid hostname" }

        val systemIps = try {
            InetAddress.getAllByName(d).filterIsInstance<Inet4Address>().map { it.hostAddress!! }.toSet()
        } catch (e: Exception) {
            emptySet()
        }

        val dohIps = resolvers.mapNotNull { (name, base) ->
            runCatching { name to queryDoh(base, d) }.getOrNull()
        }.toMap().filterValues { it.isNotEmpty() }

        if (dohIps.isEmpty()) {
            return DnsCheckResult(DnsVerdict.UNKNOWN, d, systemIps, dohIps, "Encrypted resolvers unreachable")
        }

        val dohUnion = dohIps.values.flatten().toSet()

        // Public name that only the system resolver sends to a private or loopback address.
        if (systemIps.any(::isPrivateOrLoopback) && dohUnion.none(::isPrivateOrLoopback)) {
            return DnsCheckResult(DnsVerdict.HIGH_RISK, d, systemIps, dohIps, "System DNS returned a private address")
        }

        if (systemIps.intersect(dohUnion).isNotEmpty()) {
            return DnsCheckResult(DnsVerdict.OK, d, systemIps, dohIps, "Answers overlap")
        }

        // Answers differ. Next step in the app: verify the TLS certificate for [d]
        // on the system-resolved IP before deciding to show the warning page.
        return DnsCheckResult(DnsVerdict.SUSPICIOUS, d, systemIps, dohIps, "System DNS differs from encrypted DNS")
    }

    private fun queryDoh(base: String, domain: String): Set<String> {
        val url = URL("$base?name=${URLEncoder.encode(domain, "UTF-8")}&type=A")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            connectTimeout = 4000
            readTimeout = 4000
            setRequestProperty("Accept", "application/dns-json")
        }
        try {
            if (conn.responseCode != 200) return emptySet()
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val answers = JSONObject(body).optJSONArray("Answer") ?: return emptySet()
            val ips = mutableSetOf<String>()
            for (i in 0 until answers.length()) {
                val a = answers.getJSONObject(i)
                if (a.optInt("type") == 1) ips.add(a.getString("data")) // type 1 = A record
            }
            return ips
        } finally {
            conn.disconnect()
        }
    }

    private fun isPrivateOrLoopback(ip: String): Boolean {
        val a = InetAddress.getByName(ip)
        return a.isSiteLocalAddress || a.isLoopbackAddress || a.isAnyLocalAddress || a.isLinkLocalAddress
    }
}
