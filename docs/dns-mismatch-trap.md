---
layout: default
title: "DNS mismatch warning ('trap page')"
---

# DNS mismatch warning ("trap page")

If the authenticator app and the user's browser see **different answers for the same domain**, something may be tampering with DNS (a hijacked router, malicious Wi-Fi, a poisoned resolver, or malware that changed the hosts file). In that case the app does not let the user carry on to the real-looking site. It sends them to a safe landing page, the [lamp warning page](../trap/index.html), that explains what was detected and has no login form.

> This is a **defensive warning for the user's own protection**. The landing page never collects credentials or personal data, never impersonates another site, and is not used to "hack back" or target anyone.

## The idea

1. The user opens a link or a login domain through the app (or the app is asked to verify a domain before a 6-digit code is shown or used).
2. The app resolves the domain in **two independent ways**:
   - the device's normal resolver (what the browser will use), and
   - one or more encrypted resolvers (DNS over HTTPS) that the app trusts.
3. It compares the answers. If they do not agree, it treats the connection as suspect.
4. It shows the warning/landing page instead of continuing, and never displays or accepts a code for that domain.

## Why a mismatch is only a signal, not proof

Different answers happen for harmless reasons too. Don't block on a single difference; score it.

| Harmless cause | Why it differs |
| --- | --- |
| CDNs and geo-DNS | Large sites return different IPs by region and time |
| Split-horizon / company DNS | Internal names resolve differently inside a network |
| Captive portals | Hotel or airport Wi-Fi redirects until you sign in |
| Short TTLs and load balancing | Answers rotate between lookups |

## Detection ladder

From cheapest to strongest. Use as many as you can.

1. **Compare answer sets.** If the system resolver and the encrypted resolvers share at least one IP, treat it as OK.
2. **Compare more than one encrypted resolver.** If two independent DoH resolvers agree with each other but not with the system resolver, that points to tampering on the local path.
3. **Check the network owner.** Same ASN or provider on both sides is likely a CDN. A different country or an unrelated provider is more suspicious.
4. **Check private ranges.** A public domain that resolves to a private or loopback address (`10.x`, `192.168.x`, `127.x`) from the system resolver only is a strong red flag.
5. **Verify TLS.** Connect to the IP the system resolver gave and check that the certificate is valid **for the domain** and issued by a trusted CA. A DNS trick that sends you to a lookalike server usually fails this check.
6. **DNSSEC.** Where the domain is signed, a validating resolver rejects forged answers.
7. **Known-bad lists.** Optionally check a reputation or safe-browsing service. Send only the domain, never the full URL, and tell the user you do.

## Suggested response levels

| Result | Action |
| --- | --- |
| Answers agree | Continue normally |
| Differ, but TLS is valid for the domain and ASN matches | Continue, note it in a local log |
| Differ and TLS fails or the IP is private/loopback | Show the landing page, do not show or accept codes for this domain |
| Differ and a second encrypted resolver disagrees too | Show the landing page and suggest switching networks |

## What the landing page does with the link

The page cannot check anyone's connection itself. It only displays the values in the link the app sends, so it behaves differently depending on those values:

| Link | What the page shows |
| --- | --- |
| No values (opened on its own) | "Nothing to report", with an explanation and a link to an example |
| Site and two addresses that match | "No mismatch found" |
| Addresses differ | The warning, what was seen, and what to do |
| The device's address is private or loopback | The warning, plus a note that a public site should never resolve to one |
| Missing or invalid values | "This link could not be read" |
| `?demo=1` | An example warning, clearly labelled as made up |

Values are shown as plain text only after they pass a hostname or IP check, so a crafted link cannot inject markup. Because anyone can write such a link, a warning only means something when the app sent you.

## What the landing page should tell the user

- What was seen, shown as plain text: the domain, the address the app expected, and the address the device got.
- Why that matters in one sentence.
- Safe next steps: leave the page, switch from public Wi-Fi to mobile data, restart the router, turn on Private DNS, and contact the site through its official app or number.
- No forms, no "enter your code here", no "continue anyway" button for the highest-risk result.

## Android notes

An app can't redirect the browser by itself, so choose one route:

- **Link handling:** register the app for `https` links, or let the user "share" a link to it, and run the check before opening the browser.
- **Local DNS filter:** a `VpnService` that checks DNS answers on the device. This is powerful, so it needs a clear permission screen and a strict privacy policy.
- **Guidance only:** tell users to set Android's **Private DNS** (DNS over TLS) to a resolver they trust.

See [`DnsMismatchCheck.kt`](https://github.com/exicoArt/androidAuthAppTo6Digits/blob/main/android/core/src/main/kotlin/com/example/authapp/dnscheck/DnsMismatchCheck.kt) for the comparison step and [`SafeCodeGate.kt`](https://github.com/exicoArt/androidAuthAppTo6Digits/blob/main/android/core/src/main/kotlin/com/example/authapp/gate/SafeCodeGate.kt) for the decision to show the warning page.

## Privacy

- Only domains the user chooses to check are looked up.
- Don't keep a browsing history. Keep results on the device, and delete them after a short period.
- Be open about which resolvers the app contacts.

## Limits

- Malware on the device can change what the app sees.
- An attacker who controls the real site's DNS account changes the answers everywhere. DNS comparison won't catch that, so combine it with TLS checks and, ideally, passkeys or hardware keys.
- Phishing links that use a *different but legitimate-looking domain* have no mismatch at all. See the [threat model](threat-model.md).
