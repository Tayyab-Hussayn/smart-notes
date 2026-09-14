# Implementation Plan

## Repository shape

- `client/shared`: KMP domain, use cases, repository contracts, SQLDelight persistence.
- `client/desktop`: Compose Desktop app and first runnable platform shell.
- Future Android/iOS/native-surface modules remain separate platform adapters.
- Future `backend`: FastAPI modular monolith after the local-first vertical slice.

## First milestone

1. Establish the Gradle/KMP/Compose foundation.
2. Implement note lifecycle rules and account-scoped repository contracts.
3. Persist notes and durable outbox intent atomically with SQLDelight.
4. Render active notes on a responsive canvas.
5. Support create, edit, archive, restore, and delete.
6. Test lifecycle, account isolation, and restart persistence.

ADR-001 is resolved. Later ADRs do not block this offline milestone.
