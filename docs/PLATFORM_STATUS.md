# Platform support evidence

This is implementation status, not an advertised support matrix.

| Target | App shell | Persistent surface | Native reminders | Verified |
|---|---|---|---|---|
| Linux desktop | Compose editor and manual account sync | In-app canvas | Not connected | Current desktop compilation and 23 Kotlin tests passed locally; UI not exercised |
| Windows desktop | Shared desktop source | Not connected | Not connected | No native build |
| macOS desktop | Shared desktop source | Not connected | Not connected | No native build |
| Android | Local Compose editor and export | Opt-in live wallpaper | Not connected | SDK 35 debug APK built and signature verified; no device testing |
| iOS | Not implemented | Not implemented | Not implemented | No Xcode/device build |

Capabilities default to unavailable until positively detected by native adapters.
Android wallpaper is user-selected and displays only explicitly exposed notes.
Device behavior, lock-screen handling and lifecycle behavior need native testing.

Provider integrations (AI, billing, advertising) are not configured or verified.
Store purchase/reward verification, signing and native packaging remain release gates.
