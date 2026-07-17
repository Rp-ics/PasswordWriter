# PasswordWriter

> **Open-source, offline-first password manager with a custom keyboard.**  
> AES-256-GCM encrypted. No cloud. No tracking. No permissions required.

[![License: MIT](https://img.shields.io/badge/License-MIT-purple.svg)](LICENSE)
[![Platform: Android](https://img.shields.io/badge/Platform-Android-3DDC84)](https://www.android.com)
[![Min SDK](https://img.shields.io/badge/minSdk-26-orange)](https://developer.android.com/studio/releases/platforms)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-blue)](https://kotlinlang.org)

---

## ✨ Features

| Feature | Description |
|---|---|
| **Custom Keyboard IME** | Type passwords directly — no overlays, no accessibility hacks |
| **AES-256-GCM Encryption** | Every password encrypted before storage. 100% offline. |
| **PBKDF2 Key Derivation** | Master password stretched with 100,000 iterations |
| **Biometric Unlock** | Fingerprint / face unlock support |
| **Password Generator** | Create strong passwords with custom length, symbols, numbers |
| **Search** | Instant search across all passwords |
| **Categories** | Organize passwords into colored folders with custom icons |
| **Export / Import** | Full backup to `.pwb` file, protected by master password |
| **3 Themes** | Dark, Light, Vintage (90s PC style) |
| **Multi-language** | English, Italiano, Русский |
| **No Internet** | Zero network permissions. Your data never leaves your device. |
| **Open Source** | MIT license — audit it, fork it, trust it. |

---

## 🚀 How it works

```
┌─────────────────────────────────────────┐
│  1. Set a master password (PBKDF2)      │
│  2. Save your logins (AES-256-GCM)      │
│  3. Enable PasswordWriter keyboard      │
│  4. Tap a text field → switch keyboard  │
│  5. Pick a password → it's typed 🔐     │
└─────────────────────────────────────────┘
```

No overlays. No accessibility service. No clipboard.  
Just a keyboard that knows your passwords.

---

## 📦 Installation

1. Download the latest APK from [Releases](https://github.com/Rp-ics/PasswordWriter/releases)
2. Open **Settings → System → Languages & input → On-screen keyboard → Manage keyboards**
3. Enable **PasswordWriter**
4. Open the app and create your master password
5. Start saving logins

---

## 🛡️ Security

| Layer | Method |
|---|---|
| Storage encryption | AES-256-GCM with random salt + IV per entry |
| Key derivation | PBKDF2WithHmacSHA256, 100k iterations |
| Master password | Hashed with unique salt, stored in EncryptedSharedPreferences |
| Backup file | `.pwb` — AES-256-GCM encrypted JSON |
| Network | **None.** No INTERNET permission. |
| Clipboard | Password is typed directly via IME — never copied (unless you tap "copy") |

---

## 🌐 Translations

- **English** — `values/strings.xml`
- **Italiano** — `values-it/strings.xml`
- **Русский** — `values-ru/strings.xml`

Want to add your language?  
Open a PR with a new `values-{lang}/strings.xml` file.

---

## 🧱 Tech Stack

- **Language:** Kotlin 100%
- **Architecture:** Room (SQLite), Coroutines, Flow
- **Crypto:** javax.crypto (AES/GCM/NoPadding), PBKDF2WithHmacSHA256
- **Encrypted prefs:** AndroidX Security Crypto
- **Keyboard:** InputMethodService (IME)
- **Min SDK:** 26 | **Target SDK:** 34

---

## 🖌️ Themes

| Theme | Preview |
|---|---|
| **Dark** | Modern dark theme (default) |
| **Light** | Clean white theme |
| **Vintage** | 90s PC nostalgia — Windows 95 inspired palette |

Choose in **Settings → Theme**.

---

## 🔐 Backup (.pwb)

Export your entire vault as a single encrypted file:
- **Settings → Export** → choose save location
- **Settings → Import** → select `.pwb` → enter master password

The backup uses the same AES-256-GCM encryption as the vault.

---

## 📄 License

MIT License — see [LICENSE](LICENSE).
---

## 🤝 Contributing

Contributions are welcome!  
- Fork the repo  
- Create a feature branch  
- Open a pull request  

For bugs and feature requests, use [GitHub Issues](https://github.com/Rp-ics/PasswordWriter/issues).

---

<p align="center">
  <b>PasswordWriter</b> — Your passwords. Your keyboard. Your control.<br>
  <i>100% offline. 100% open source. 0% cloud.</i>
</p>
