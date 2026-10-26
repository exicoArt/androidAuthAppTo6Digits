---
layout: default
title: Home
---

# Secure Auth Guide

A practical, plain-language guide to setting up an authenticator app (two-factor authentication) and using it to protect against bots, malicious accounts, phishing links and money scams.

> Early draft. Everything here is educational guidance, not a security audit or financial advice.

## Read the guide

| Page | What it covers |
| --- | --- |
| [Setting up an authenticator app]({{ '/docs/setup-authenticator.html' | relative_url }}) | Setup steps, and handling the QR code safely |
| [Threat model]({{ '/docs/threat-model.html' | relative_url }}) | Bots, fake accounts and phishing links: what 2FA stops and what it doesn't |
| [Safe money checklist]({{ '/docs/safe-money-checklist.html' | relative_url }}) | Habits and checks for keeping your money safe online |
| [DNS mismatch warning]({{ '/docs/dns-mismatch-trap.html' | relative_url }}) | How the app spots DNS tampering and sends you to a safe page |
| [App architecture]({{ '/docs/architecture.html' | relative_url }}) | How the Android app is put together, and what it never does |

## See the warning page

[Open the lamp page]({{ '/trap/' | relative_url }}). This is the safe landing page the app shows when a site's address does not match what a trusted, encrypted lookup returned. Opened on its own it reports nothing, because it only warns when the app sends it values that do not match. You can [see an example warning]({{ '/trap/' | relative_url }}?demo=1) (made-up values).

## Key ideas in one minute

1. Use an authenticator app or a hardware key, not SMS, where you can.
2. Treat the setup QR code and its text key like a password. Never post, share or commit it.
3. Save your backup codes offline, away from your phone.
4. Never type a 6-digit code into a page you reached from a link or message.
5. For money, slow down. Verify through a channel you started yourself.

[Source code and issues on GitHub](https://github.com/exicoArt/androidAuthAppTo6Digits)
