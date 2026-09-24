# CI usage policy

Owner direction: conserve Actions usage, batch verification near the integration
checkpoint, target one initial run and at most one repair run.

- Manual dispatch plus one initial PR-open checkpoint on the implementation branch.
- Pushes, PR synchronize events, and schedules never trigger this workflow.
- The connected app does not expose manual workflow dispatch; PR creation provides
  the authorized first verification run without enabling runs on every push.
- One standard Linux job, maximum 15 minutes per run.
- Gradle dependency cache enabled by setup-gradle.
- No OS matrix, paid larger runners, automatic retries, or scheduled jobs.
- First checkpoint: desktop compilation plus shared critical persistence tests.
- Two dispatched runs imply at most 30 configured job minutes, not a billing quote.
- Track actual dispatches below before starting another run.

## Run ledger

1. Initial PR-open run: `34814749042`, commit `02166aa`, draft PR #1.
   One Linux job; success. Desktop compilation, shared Kotlin tests, SQLite
   migrations and 13 backend tests including PostgreSQL passed.
2. Consolidated Android/client-sync/account-lifecycle verification via controlled
   reopen of draft PR #1. No runs on pushes or ordinary PR updates.

## Publication

Workflow is prepared with the implementation. GitHub requires it on the default
branch for manual dispatch. Publish/register it at the integration checkpoint;
do not merge unverified app code merely to register the workflow.

## Scope of evidence

These checks do not prove complete software readiness. Later backend, security,
sync, native-device, and provider validation must be included in appropriately
batched checks before claiming those features complete. Budget limits never
convert untested functionality into verified functionality.

Keep Gradle: replacing the Kotlin/Compose build system does not resolve the
workspace network issue. A developer machine running Gradle is another option
if Actions access is unavailable; no separate hosted runner is required.
