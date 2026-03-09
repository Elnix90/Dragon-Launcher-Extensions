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
> Installation of extensions from untrusted sources is strongly discouraged. Dragon Launcher does not rely on Android's native signature-level protection for extensions; instead, it performs a **manual signature verification**. In production, only extensions signed with the official Dragon certificate are authorized. If the signature check fails, the extension will be blocked unless the user has explicitly enabled **Debug Mode** and **Disable Signature Check**.

### 🛠️ Manual Build
Extensions are regular Android modules. To build them all at once:
1. Clone the repo: `git clone https://github.com/Elnix90/Dragon-Launcher-Extensions`
2. Run Gradle: `./gradlew assembleRelease`
3. APKs will be found in `ext-*/build/outputs/apk/release/`

### 🚀 Fast Deploy (Script)
You can build and install any extension directly to a connected device:
```bash
./deploy-extension.sh ext-weather-widget
```
If you don't provide a name, the script will list available extensions and ask for input.

> [!TIP]
> **Developer Setup**: To test your extension in the launcher, you must enable **Debug Mode** and **Disable Signature Check** in the Dragon Launcher settings. 
> See the [**Setup Screenshot**](DOCS/disable_signature_check.jpg) in the `DOCS` folder for reference.

---

## 🐲 Philosophy: Privacy First
Dragon Launcher is designed to be **offline-first** and **minimalist**. 
Extensions allow users to add functionality (like Internet access, Auto-updates, or Privileged installs) without compromising the security or simplicity of the main launcher.

## 📦 Available Extensions

| Extension | Build Status | Purpose | Link |
|-----------|:---------:|---------|--------|
| **Internet Proxy** | ![Status](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/badges/ext-internet-proxy.json) | Bridges the launcher with the web while keeping the main app offline. | [`ext-internet-proxy/`](ext-internet-proxy/) |
| **Auto-Update** | ![Status](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/badges/ext-auto-update.json) | Checks GitHub Releases for the latest versions of the ecosystem. | [`ext-auto-update/`](ext-auto-update/) |
| **Shizuku Installer**| ![Status](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/badges/ext-shizuku-installer.json) | Silent APK installation via privileged ADB shell (Shizuku). | [`ext-shizuku-installer/`](ext-shizuku-installer/) |
| **Additional Fonts**| ![Status](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/badges/ext-additional-fonts.json) | Downloads custom fonts from Google Fonts API. | [`ext-additional-fonts/`](ext-additional-fonts/) |
| **Weather Widget** | ![Status](https://img.shields.io/endpoint?url=https://raw.githubusercontent.com/Elnix90/Dragon-Launcher-Extensions/badges/ext-weather-widget.json) | Full weather forecasting (Current, Hourly, Daily) with rain alerts and configuration. | [`ext-weather-widget/`](ext-weather-widget/) |

> Check the [**Extensions Registry**](extensions-registry.json) for full multilingual descriptions (EN, FR, DE, ES, HI, JA, KO, PT) and required permissions.

## 🤝 How to Contribute

1.  **Naming**: New extensions must be in a root folder prefixed with `ext-`.
2.  **Registry**: Don't forget to update the [`extensions-registry.json`](extensions-registry.json) with your metadata.
3.  **CI**: Automated checks will verify that your registry info matches the [`AndroidManifest.xml`](ext-internet-proxy/src/main/AndroidManifest.xml).

## 🌳 Repository Structure

```text
.
├── DOCS
│   └── disable_signature_check.jpg
├── LICENSE
├── README.md
├── build.gradle.kts
├── deploy-extension.sh
├── ext-additional-fonts
│   ├── build.gradle.kts
│   ├── google-fonts-cache.json
│   └── src
│       └── main
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
├── ext-weather-widget
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

19 directories, 21 files
```

## 📄 License
This project is licensed under the **GPL 3 License**, the same as [Dragon Launcher](https://github.com/Elnix90/Dragon-Launcher). See the [LICENSE](LICENSE) file for more details.
