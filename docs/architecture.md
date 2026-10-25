---
layout: default
title: App architecture
---

# App architecture

How the Android authenticator app is meant to fit together, and the rules it follows to protect the person using it.

## Parts

| Part | Job | Status |
| --- | --- | --- |
| **OTP core** (`otp/`) | Reads setup keys (Base32) and `otpauth://` QR links, and makes 6-digit TOTP codes (RFC 6238) | Written, with unit tests against the RFC test vectors |
| **DNS check** (`dnscheck/`) | Compares the device's DNS answer with encrypted DNS answers for a domain | Written, network path not yet tested |
| **Safe code gate** (`gate/`) | Decides whether to show a code for a site, or to send the user to the [warning page](dns-mismatch-trap.md) | Written, with unit tests |
| **Secure storage** | Keeps account secrets encrypted on the device | Planned |
| **User interface** | Add account (scan or type), list of codes with a countdown, link-check screen | Planned |
| **TLS check** | Confirms a site's certificate is valid for its domain | Planned |

## How a login check flows

1. The user opens a site's login through the app, or shares a link to it.
2. The **safe code gate** runs the **DNS check** for that domain.
3. Answers agree: the app shows the current code with a countdown.
4. Answers differ and the certificate can't be confirmed, or the address is private: the app shows the warning page and **no code**.
5. The check can't run (offline): the app shows the code with a notice to check the address yourself.

## Rules the app follows

| Rule | Why |
| --- | --- |
| Secrets are encrypted at rest with a key in the Android Keystore | A stolen backup or file dump is not enough to make codes |
| Screens that show a QR code, a setup key or a code use `FLAG_SECURE` | Blocks screenshots, screen recording and the recent-apps preview |
| Automatic cloud backup of app data is off | Secrets don't leave the device by accident |
| Secrets and codes are never written to logs, crash reports or analytics | Logs are easy to leak |
| Error messages never repeat a secret or a full setup link | Same reason |
| Codes copied to the clipboard are cleared after a short time | Other apps can read the clipboard |
| Biometric or device lock is required to open the app | A lost phone is not a lost account |
| The app never asks the user to type a code into the app on behalf of a website | Codes go to the real site only |
| The warning page has no forms and collects nothing | It can't become a phishing page itself |
| Only domains the user chooses to check are looked up | Privacy |

## Clock drift

TOTP depends on the phone's clock. If codes are rejected, the likely cause is a wrong time. The app should compare the phone's time with a trusted source and warn if they differ by more than a few seconds.

## Testing

- Code generation is checked against the official RFC 6238 test vectors for SHA-1, SHA-256 and SHA-512.
- Parsing, error messages (no secret leaks) and the gate decisions have unit tests.
- A GitHub Actions workflow runs them on every change under `android/`.
