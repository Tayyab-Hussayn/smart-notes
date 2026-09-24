# Platform support evidence

This is implementation status, not an advertised support matrix.

| Target | App shell | Persistent surface | Native reminders | Verified |
|---|---|---|---|---|
| Linux desktop | Compose code | In-app canvas only | Not connected | Pending CI/build and UI checks |
| Windows desktop | Shared desktop source | Not connected | Not connected | No device build |
| macOS desktop | Shared desktop source | Not connected | Not connected | No device build |
| Android | Not implemented | Not implemented | Not implemented | No SDK/device build |
| iOS | Not implemented | Not implemented | Not implemented | No Xcode/device build |

Capabilities default to unavailable until positively detected by native adapters.
Do not claim persistent wallpaper, background notifications, or secure credential
storage merely because a platform normally offers an API for them.

Provider integrations (AI, billing, advertising) are not configured or verified.
Real store purchase/reward verification and native packaging remain release gates.
