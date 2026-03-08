# Dragon Launcher Extensions

Official and community extensions for **Dragon Launcher**. Each folder prefixed with `ext-` represents a standalone Kotlin module/extension for Android.

[![GitHub license](https://img.shields.io/github/license/Elnix90/Dragon-Launcher-Extensions?style=for-the-badge)](LICENSE)
[![Dragon Launcher](https://img.shields.io/badge/Dragon-Launcher-blue?style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher)
[![Discord](https://img.shields.io/discord/1327996079786168441?color=blue&label=Discord&logo=discord&style=for-the-badge)](https://discord.gg/6UyuP8EBWS)

---

> [!IMPORTANT]
> This repository is for **Extension** related issues/features only.
> If you have an issue with the **Main Launcher**, please report it here: [**Dragon Launcher Repository**](https://github.com/Elnix90/Dragon-Launcher/issues).

---

## 🏗️ How to Build & Install

### 📥 Download
You can download the latest official APKs from the [**Releases Page**](https://github.com/Elnix90/Dragon-Launcher-Extensions/releases).

> [!WARNING]
> **Use caution with third-party extensions.**
> Installation of extensions from untrusted sources is strongly discouraged. Because extensions use a **signature-level security model**, unauthorized extensions will be blocked by the launcher, but malicious APKs can still pose a risk to your device. Only install extensions from this official repository.

### 🛠️ Manual Build
Extensions are regular Android modules. To build them all at once:
1. Clone the repo: `git clone https://github.com/Elnix90/Dragon-Launcher-Extensions`
2. Run Gradle: `./gradlew assembleRelease`
3. APKs will be found in `ext-*/build/outputs/apk/release/`

---

## 🐲 Philosophy: Privacy First
Dragon Launcher is designed to be **offline-first** and **minimalist**. 
Extensions allow users to add functionality (like Internet access, Auto-updates, or Privileged installs) without compromising the security or simplicity of the main launcher.

## 📦 Available Extensions

| Extension | Purpose | Link |
|-----------|---------|--------|
| **Internet Proxy** | Bridges the launcher with the web while keeping the main app offline. | [`ext-internet-proxy/`](ext-internet-proxy/) |
| **Auto-Update** | Checks GitHub Releases for the latest versions of the ecosystem. | [`ext-auto-update/`](ext-auto-update/) |
| **Shizuku Installer**| Silent APK installation via privileged ADB shell (Shizuku). | [`ext-shizuku-installer/`](ext-shizuku-installer/) |

> Check the [**Extensions Registry**](extensions-registry.json) for full multilingual descriptions (EN, FR, DE, ES, HI, JA, KO, PT) and required permissions.

## 🛠️ Documentation

- [**Architecture & Security**](DOCS/ARCHITECTURE.md): Deep dive into the signature-level security model and communication channels (Intents vs AIDL).
- [**Extension Template**](DOCS/extension-template/): A reference template showing the expected structure for new extensions.

## 🤝 How to Contribute

1.  **Explore**: Read the [**Architecture Guide**](DOCS/ARCHITECTURE.md) to understand the security model.
2.  **Boilerplate**: Use the [`DOCS/extension-template/`](DOCS/extension-template/) folder as a starting point.
3.  **Naming**: New extensions must be in a root folder prefixed with `ext-`.
4.  **Registry**: Don't forget to update the [`extensions-registry.json`](extensions-registry.json) with your metadata.
5.  **CI**: Automated checks will verify that your registry info matches the [`AndroidManifest.xml`](ext-internet-proxy/src/main/AndroidManifest.xml).

## 🌳 Repository Structure

```text
.
├── DOCS
│   ├── ARCHITECTURE.md
│   └── extension-template
│       └── src
├── LICENSE
├── README.md
├── build.gradle.kts
├── ext-auto-update
│   ├── README.md
│   ├── build.gradle.kts
│   └── src
│       └── main
├── ext-internet-proxy
│   ├── README.md
│   ├── build.gradle.kts
│   └── src
│       └── main
├── ext-shizuku-installer
│   ├── README.md
│   ├── build.gradle.kts
│   └── src
│       └── main
├── extensions-registry.json
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle.kts

15 directories, 17 files
```

## 📄 License
This project is licensed under the **GPL 3 License**, the same as [Dragon Launcher](https://github.com/Elnix90/Dragon-Launcher). See the [LICENSE](LICENSE) file for more details.
