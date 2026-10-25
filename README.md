# androidAuthAppTo6Digits

**Secure Auth Guide** — a practical, plain-language guide to setting up an authenticator app (two-factor authentication) and using it to protect against bots, malicious accounts, phishing links and money scams.

> **Status:** early draft. Everything here is educational guidance, not a security audit or financial advice.

## Why this project exists

Passwords alone are easy to lose: they get reused, guessed, leaked and phished. An authenticator app adds a second step that changes every 30 seconds, so a stolen password is not enough to take over an account. This guide explains how to set one up correctly and what it does and does not protect you from.

## What's inside

| Document | What it covers |
| --- | --- |
| [docs/setup-authenticator.md](docs/setup-authenticator.md) | Setting up an authenticator app, and handling the QR code safely |
| [docs/threat-model.md](docs/threat-model.md) | Bots, malicious accounts, phishing links: what 2FA stops and what it doesn't |
| [docs/safe-money-checklist.md](docs/safe-money-checklist.md) | Habits and checks for keeping your money safe online |
| [docs/dns-mismatch-trap.md](docs/dns-mismatch-trap.md) | Detecting DNS tampering (app vs browser answers) and sending the user to a safe warning page |
| [trap/index.html](trap/index.html) | The warning landing page, featuring the Tulou lamp 3D model |
| [android/DnsMismatchCheck.kt](android/DnsMismatchCheck.kt) | Untested Kotlin sketch of the DNS comparison step |
| [assets/](assets/) | The Tulou lamp 3D model (`Tulou-lamp.glb`) |
| [SECURITY.md](SECURITY.md) | How to report a problem with this guide |

## Key ideas in one minute

1. **Use an authenticator app or a hardware key, not SMS, where you can.** Phone numbers can be hijacked through SIM swapping.
2. **Treat the setup QR code and its text key like a password.** Anyone who has it can generate your codes. Never post it, screenshot-share it or commit it to a repository.
3. **Save your backup codes offline** in a safe place, separate from your phone.
4. **Never type a 6-digit code into a page you reached from a link or message.** Go to the site yourself. Real support staff will never ask you for a code.
5. **For money, slow down.** Urgency is the scammer's main tool. Verify through a channel you started yourself.

## Contributing

Corrections and clearer wording are welcome. Please do not include real secrets, QR codes, account details or personal data in issues or pull requests.

## License

MIT. See [LICENSE](LICENSE).
