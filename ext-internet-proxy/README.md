# Internet Proxy Extension

This extension allows **Dragon Launcher** to perform network requests without requiring the `INTERNET` permission in the main application.

## 🛡️ Security
- **Permission**: `com.dragon.launcher.APP_EXTENSION` (Signature Level)
- **Data Flow**: Launcher -> `Intent(ACTION_PROXY_FETCH)` -> Proxy Ext -> (Internet) -> Proxy Ext -> `Intent(ACTION_PROXY_RESPONSE)` -> Launcher.

## ⚙️ Requirements
- `android.permission.INTERNET`
- Must be signed with the same key as Dragon Launcher.
