# Threat model: what 2FA protects against

| Threat | Does an authenticator app help? | What else you should do |
| --- | --- | --- |
| **Password guessing and credential stuffing by bots** (automated login attempts using leaked passwords) | Yes, strongly. A guessed or leaked password alone no longer works. | Use a unique password per site, ideally from a password manager. |
| **Fake or malicious accounts** (spam, impersonation, scam messages) | Partly. It protects *your* account from takeover, but doesn't stop strangers from messaging you. | Don't accept requests from unknown accounts, report and block them, check profile age and history. |
| **Phishing links** (a fake page asks for your password and your 6-digit code) | Only partly. A phisher can relay the code in real time. | Never log in from links in messages. Type the address or use a bookmark. Prefer passkeys or hardware keys, which refuse fake sites. |
| **SIM swapping** | Yes, if you use an app instead of SMS codes. | Add a PIN or port-out lock with your mobile carrier. |
| **Malware on your device** | Limited. Malware can steal sessions and codes. | Keep the OS and apps updated, install only from official stores. |
| **Lost or stolen phone** | Only if the phone is locked and you have backup codes. | Use a screen lock, remote wipe, and keep backup codes offline. |

## Spotting phishing links

- The domain is slightly off (extra words, swapped letters, odd endings) or hidden behind a link shortener.
- The message creates urgency: "act in 10 minutes or lose access".
- It asks for a password, a 6-digit code, a recovery phrase, or a "verification" payment.
- It arrives from an unexpected sender, even one that looks like a friend. Friends' accounts get hacked too.

**Rule:** if a message makes you want to click quickly, stop and open the site yourself instead.

## Recognising bot-like behaviour

- Many near-identical messages or accounts created at the same time.
- Instant replies, scripted wording, links to the same destination.
- Accounts that push you to move to another app or to a payment.

## What you can do as a developer

- Rate-limit logins and add lockouts or progressive delays.
- Offer passkeys or hardware keys alongside TOTP.
- Don't accept a code after it has been used once (prevent replay).
- Show users the real domain in sensitive emails, and don't send login links that ask for a code.
- Never log or store users' TOTP secrets in plain text.
