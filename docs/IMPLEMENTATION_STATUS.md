# Implementation status — 2026-09-25

## Implemented on the development branch

- Shared Kotlin note lifecycle, account-scoped SQLDelight storage, optimistic
  revisions, tombstones, immutable outboxes and versioned migrations.
- Canvas placement/visibility and deterministic note selection.
- Stored reminder schedules, occurrences, snooze/completion and bounded
  fixed-interval expansion; native notification delivery is not connected.
- Compose desktop editor, archive/trash, canvas, JSON note export and reminder UI.
- Desktop sign-in/register, manual note sync and explicit conflict resolution.
  Sessions remain in memory; local account namespaces include the server origin.
- HTTP sync with durable retry attempts, server revisions and pull cursors;
  conflicts retain local edits and can preserve them as a separate note.
- Android local note editor and opt-in live wallpaper with visible-only rotation.
  Draft-discard confirmation, synchronous save reservation and system-picker
  JSON note export added on September 25.
- FastAPI/PostgreSQL registration, login, session renewal/revocation, revisioned
  note sync, account export and authenticated account deletion.

## Verification evidence

- Actions run `34814749042` on commit `02166aa`: passed desktop compilation,
  shared Kotlin tests, SQLite checks and 13 backend tests including PostgreSQL.
- Actions run `36070669269` on commit `e35f803`: failed during Android plugin
  setup with missing `com/android/build/gradle/api/BaseVariant`. Compilation
  and subsequent test steps did not execute; no APK was produced.
- Root Android/Kotlin plugin classpath alignment has been repaired in source.
  Android plugin setup and AndroidX configuration repaired. September 25 local
  `:client:android:assembleDebug` succeeded with SDK 35; APK signature verified.
  Device verification remains pending.
- Both planned Actions runs have been used. Pushes do not launch further runs.
- September 25 local checks: seven SQLite tests passed; twelve backend tests
  passed after installing dependencies, with two PostgreSQL tests skipped.
  Backend package metadata repaired and editable installation verified.
- Gradle wrapper generated for 8.14.3 with the published SHA-256 checksum.
- September 25 local Gradle run passed: 23 Kotlin tests, SQLDelight generation
  and desktop compilation, including the new sync client. Local build access
  was restored using the configured proxy, system CA store and a full JDK 17.
- No native-device, emulator, iOS/Xcode, store or production verification exists.

Development APK: package `com.smartsticky.android`, version `0.1.0`, min SDK 26,
target SDK 35. SHA-256:
`f5ec3d76a1df3820c5790d26bd43b4a5df2ba654683860db76dea5f55a5e42cb`.
This is a debug-signed local-notes/wallpaper build, not a release candidate.

## Remaining implementation and release work

- Android account/sync and reminder interfaces; native notification delivery.
- iOS application and supported native surfaces.
- Windows/macOS packaging and native adapters; Linux desktop UI verification.
- Calendar/time-zone recurrence, background work and canvas/reminder replication.
- Full aggregate export, client account-deletion UI, recovery/verification email
  and secure persistent credential storage.
- Consent-controlled cloud AI, verified subscriptions and rewarded ads.
- External deletion journal, backup-restore reconciliation and production ops.
- End-to-end, accessibility, security, device lifecycle and release acceptance.

Draft PR #1 is development work, not a finished application or production release.
