# Dragon Launcher Extension Architecture & Security

This document explains how Dragon Launcher communicates with its extensions and how the security model is implemented.

## Security Model: Signature-Level Permissions

To prevent malicious apps from interacting with your extensions or the launcher, we use **signature-level permissions**.

1.  **The Launcher defines the permission**:
    ```xml
    <permission android:name="org.elnix.dragonlauncher.APP_EXTENSION"
        android:protectionLevel="signature" />
    ```
2.  **The Extension requests the permission**:
    ```xml
    <uses-permission android:name="org.elnix.dragonlauncher.APP_EXTENSION" />
    ```
3.  **The Extension protects its services**:
    By adding `android:permission="org.elnix.dragonlauncher.APP_EXTENSION"` to its `<service>` tag, the Android system will **only** allow apps signed with the **same certificate** as the extension to bind or start that service.

## Communication Channels

### 1. Simple Command (Intents)
Used for one-way triggers or simple data exchange where real-time performance isn't critical.
- **Launcher to Extension**: `startService(Intent("org.elnix.dragonlauncher.ACTION_RUN"))`
- **Extension to Launcher**: `startService(Intent("org.elnix.dragonlauncher.ACTION_RESULT"))`

### 2. High Performance (AIDL)
Recommended for frequent data exchange or when the extension provides a complex API.
- See `ext-docs-template/extension-template/src/main/aidl/` for an example.

## Best Practices
- **Do not request INTERNET in the Launcher**: The launcher must remain offline-first. Use a proxy extension if needed.
- **Process management**: Be aware that your Service can be killed at any time by the system. Save state if necessary.
- **Background work**: Always use background threads or Coroutines for heavy tasks to avoid blocking the UI of the launcher.
