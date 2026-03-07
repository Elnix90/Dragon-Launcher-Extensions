# Dragon Launcher Extensions

This repository contains official and community extensions for Dragon Launcher. Each folder at the root (except configuration files) represents a standalone extension.

[![GitHub license](https://img.shields.io/github/license/Elnix90/Dragon-Launcher-Extensions?style=for-the-badge)](LICENSE)
[![Dragon Launcher](https://img.shields.io/badge/Dragon-Launcher-blue?style=for-the-badge)](https://github.com/Elnix90/Dragon-Launcher)

## Available Extensions

- [**Internet Proxy Extension**](internet-proxy-extension/) : Allows the launcher to delegate its network requests to this extension (keeping the main launcher without `INTERNET` permission).
- [**Auto-Update Extension**](auto-update-extension/) : Checks for updates from GitHub for the Dragon Launcher ecosystem.

## Project Structure

Each directory at the root represents a Kotlin extension:
- `internet-proxy-extension/`
- `auto-update-extension/`

## How to Contribute

1. Create a new folder at the root for your extension.
2. Follow Android best practices (signature-level permissions, secure Intents).
3. Ensure the extension respects the "Privacy First" philosophy.
4. Submit a Pull Request.

## License

This project is licensed under the **GPL 3 License**, the same as [Dragon Launcher](https://github.com/Elnix90/Dragon-Launcher). See the [LICENSE](LICENSE) file for more details.
