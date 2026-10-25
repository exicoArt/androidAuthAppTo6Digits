---
layout: default
title: "Setting up an authenticator app"
---

# Setting up an authenticator app

## How it works

When you turn on 2FA, the service shows a QR code. It contains a shared secret. Your authenticator app stores that secret and combines it with the current time to produce a 6-digit code that changes every 30 seconds (this is called TOTP). The service does the same calculation and checks that the codes match.

## Steps

1. Install a reputable authenticator app from your official app store.
2. In the service's security settings, choose "authenticator app".
3. Scan the QR code with the app. If you can't scan, use the "enter the code manually" option to type the setup key.
4. Enter the current 6-digit code to confirm the setup works.
5. Save the **backup or recovery codes** the service gives you. Store them offline (for example printed, in a safe place) and not in the same place as your phone.
6. Test a login in a private window before you close your current session.

## Handle the QR code like a password

- The QR code and the manual setup key are the secret. Whoever has them can produce valid codes.
- **Do not** screenshot and share them, paste them into chats, email them, or commit them to Git.
- If the QR code was ever shown to someone else or posted anywhere, cancel the setup and start again to get a fresh one. If 2FA is already active, remove the authenticator in the service's settings and re-add it.
- Add `*.png`, `*.jpg` and any export files of secrets to `.gitignore` in repositories where you work on security topics, and check what you are about to commit.

## If you lose your phone

- Use your saved backup codes to log in, then register a new authenticator and generate new backup codes.
- Consider an app that supports encrypted backups, or register two devices at setup time.

## Limits

An authenticator app does **not** stop you from typing a valid code into a fake website. That is phishing, covered in the [threat model](threat-model.md). Hardware security keys and passkeys resist this better because they check the website's real address.
