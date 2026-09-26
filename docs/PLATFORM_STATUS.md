# Platform support evidence

This is implementation status, not an advertised support matrix.

| Target | App shell | Persistent surface | Native reminders | Verified |
|---|---|---|---|---|
| Linux desktop | Compose editor and manual account sync | In-app canvas | Not connected | Desktop compilation, 4 startup acceptance tests and 29 shared tests pass; UI not exercised |
| Windows desktop | Shared desktop source | Not connected | Not connected | No native build |
| macOS desktop | Shared desktop source | Not connected | Not connected | No native build |
| Android | Compose editor, export, manual account sync and in-app one-time reminders | Opt-in device-only live wallpaper | Not connected | Reminder-flow APK assembly and 29 shared tests passed locally; no device/end-to-end testing |
| iOS | Not implemented | Not implemented | Not implemented | No Xcode/device build |

Capabilities default to unavailable until positively detected by native adapters.
Android wallpaper is user-selected and displays only explicitly exposed notes.
Device behavior, lock-screen handling and lifecycle behavior need native testing.

Provider integrations (AI, billing, advertising) are not configured or verified.
Store purchase/reward verification, signing and native packaging remain release gates.

- September 26 reminder increment: 29 shared Kotlin tests passed with zero failures
  or skips, and Android debug APK assembly passed. New tests cover account and
  lifecycle isolation, stale revisions, disable/snooze/completion, transactional
  rollback and idempotent creation retries. No additional Actions run.
