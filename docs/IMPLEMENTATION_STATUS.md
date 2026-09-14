# Implementation checkpoint — 2026-09-13

## Written, not compiled

- Gradle/KMP/Compose/SQLDelight module configuration.
- Account-scoped note repository and optimistic local revision checks.
- Create/edit/archive/restore/delete/pin/duplicate domain operations.
- Soft-deletion tombstones and atomic immutable outbox snapshots.
- JVM tests for restart persistence, account-scoped reads, stale-edit rejection,
  and transaction rollback if outbox insertion fails.
- Initial desktop list/editor with archive/deleted views and delete confirmation.
- Canvas persistence, normalized drag/button movement, visibility, and versioned migration.
- Deterministic surface selection with hard eligibility filters and explanation strings.
- Backend liveness/configuration/container foundation; no product APIs yet.

## Verification

- `git diff --check` passed.
- Gradle 8.14.3 downloaded successfully.
- `:client:shared:jvmTest` failed before compilation: the configured build proxy
  reported `java.net.SocketException: Network is unreachable` for Google,
  Maven Central and Gradle Plugin Portal.
- No Kotlin tests executed; no UI launch or native-device verification occurred.
- Three local SQLite integrity tests and two canvas migration tests passed.
- Four backend configuration tests passed; two HTTP tests skipped due to absent dependencies.
- No remote push, deployment, or GitHub Actions run was initiated.
- Manual-only GitHub Actions workflow prepared under `.github/workflows/verify.yml`.
  Owner authorized Actions, with one or two batched checkpoint runs. None dispatched.

## Remaining

Restore authorized Gradle dependency access, compile and repair any compiler errors,
run tests, generate wrapper, and verify UI behavior. Desktop database driver lifecycle,
draft handling across other actions, and accessible responsive actions need review.
Add migration fixtures and multi-connection concurrency tests before considering
the persistence milestone complete.

Canvas visual verification, reminders, Android/iOS targets, persistent
surfaces, backend product APIs, identity, multi-device sync, intelligence integration,
billing/ads, privacy export/deletion, CI and deployment remain unimplemented.
The current outbox is local bookkeeping, not an approved sync wire protocol.
