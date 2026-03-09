# Auto-Update Extension

Handles update checks for the Dragon Launcher ecosystem by querying GitHub Releases.

## 🚀 Features
- Checks `https://api.github.com/repos/{repo}/releases/latest`.
- Provides download URLs and tag versions back to the launcher.

## 🛡️ Security
- **Permission**: `org.elnix.dragonlauncher.APP_EXTENSION` (Signature Level)
- **Isolation**: Keeps the main launcher "air-gapped" while still allowing for updates.
