# First development milestone — offline notes and canvas

Scope follows `KIMI_START_HERE.md` section 5 and `IMPLEMENTATION_PLAN.md`.
This is the first coding milestone, not documentation Phase 01 or a full release.

## Implemented scope

- Desktop app shell with local database initialization and safe error reporting.
- Offline note creation, editing, archive, trash, restore and persistent drafts while editing.
- Account-scoped notes, optimistic revisions and atomic durable mutation snapshots.
- Basic in-app canvas with persisted placement, movement and visibility.
- No cloud, AI, billing or advertising dependency for these operations.

## Acceptance evidence — September 26

| Criterion | Evidence |
|---|---|
| Fresh local database opens | Production `LocalWorkspace.open` integration test |
| Create/edit/archive/delete/restore | Lifecycle exercised through real repositories |
| Notes and canvas survive restart | File-backed database closed and reopened; text, lifecycle, coordinates and pending operations checked |
| Supported upgrade preserves data | v1 database opened through production migrations twice; archived note and pending operation retained |
| Unsupported newer schema | Open rejected; existing note retained; subsequent valid open succeeds |
| Failed initialization | Partial schema changes roll back and pre-existing data remains intact |
| Account isolation and stale changes | Desktop acceptance test plus shared persistence regressions |
| Basic canvas UI | Compose implementation compiles; interactive rendering remains unverified |

The startup tests exposed a real fresh-install failure: manual SQL transactions
conflicted with SQLDelight schema transactions, producing `cannot commit - no
transaction is active`. Bootstrap now uses a SQLDelight-owned transaction for
schema creation/migration and the version update, including rollback on failure.

## Verification commands

```sh
./gradlew :client:desktop:jvmTest :client:shared:jvmTest
python -m unittest discover -s scripts -p 'test_*.py'
```

The existing optional CI workflow now includes desktop startup tests. Its trigger
policy is unchanged; no additional Actions run was started.

## Remaining acceptance boundary

Implementation and automated persistence verification are available. A graphical
desktop session is not available in this execution environment, so interactive
app boot, canvas rendering, keyboard navigation and visual accessibility have
not been verified. Run `./gradlew :client:desktop:run` on a desktop to exercise
create/edit, Show on canvas, movement, archive/trash/restore and restart.
Do not treat this milestone as fully platform-accepted or production-ready yet.

Android device verification and subsequent modules remain tracked separately in
`IMPLEMENTATION_STATUS.md`; they do not redefine this offline desktop milestone.
