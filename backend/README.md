# Backend foundation

Implemented: validated environment configuration, `GET /health/live`, PostgreSQL
migration, password login/registration, revocable 30-minute opaque sessions,
rotation, logout, and authenticated note sync with idempotency and tombstones.
Liveness reports process responsiveness only; no database readiness is implied.
Billing, recovery, verified email, reminder/canvas replication,
client HTTP integration, and production security review remain unimplemented.

## Local run

Use Python 3.12+ in a virtual environment, then:

```sh
pip install -e '.[test]'
python -m smart_sticky_backend.database
uvicorn smart_sticky_backend.main:create_app --factory --no-access-log
python -m unittest discover -s tests -v
```

Configuration and security-unit tests can run without installing dependencies:

```sh
PYTHONPATH=src python -m unittest discover -s tests -v
```

HTTP tests are skipped when FastAPI or HTTPX is absent; skips are not validation.
Real database tests require `SMART_STICKY_TEST_DATABASE_URL` pointing to a dedicated
migrated PostgreSQL test database. Tests insert uniquely named accounts and remove
only those accounts on completion. They do not run migrations automatically.

## API contract

All product routes use `/v1`. Registration/login accept `email` and `password`.
Protected routes require `Authorization: Bearer <access_token>`.
`POST /auth/renew` atomically rotates a still-valid session; `/auth/logout` revokes it.
`POST /sync/operations` accepts one operation: UUID `operation_id`, UUID `note_id`,
integer `base_revision`, `kind` (`upsert` or `delete`), and `note` for upserts.
Notes contain `title`, `body`, `priority` (`low`, `normal`, `high`), `pinned`,
and `archived`. Deletion forbids a note payload. Conflict status is 409.
`GET /sync/changes?after=0&limit=100` returns `changes`, `cursor`, and `has_more`.
Changes contain `id`, `revision`, `deleted`, and `payload`. Clients preserve
conflicting local content and must explicitly resolve conflicts; there is no
silent last-write-wins behavior. These are foundations, not a full client adapter.

### Account lifecycle

`GET /v1/account/export` exports authenticated authored notes, including archived
notes but excluding tombstones and all credentials/session metadata. Response:
`format`, `revision`, `notes` (`id`, `payload`), `next_after`, and `has_more`.
Use `limit` (1–500, default 100); subsequent pages supply UUID `after` from
`next_after` and the initial `revision`. A concurrent mutation returns 409
`export_changed_restart`; discard collected pages and restart to avoid mixing
snapshots. No export files are saved server-side. This exports synchronized note
data only, not device-only content or future reminder/canvas aggregates.

`POST /v1/account/delete` requires a valid bearer token and JSON `password` plus
`confirmation: "DELETE MY ACCOUNT"`. A successful 204 means the PostgreSQL account,
all sessions, synchronized notes and operation receipts were deleted atomically.
An audit marker retains only account UUID and deletion timestamp. Incorrect
passwords leave all state intact. Old sessions cannot sync or recreate that
account; a later registration receives a new UUID. Clients must stop syncing and
clear credentials/local account data only after acknowledging successful deletion.
An authenticated operation already completed before deletion cannot be recalled.

### Mandatory backup restoration guard

The deletion marker is inside PostgreSQL and is **not sufficient on its own**
when restoring an older backup. Before production launch, operators must maintain
a durable deletion journal outside the database restore boundary (without notes,
passwords, or email), reconcile committed `account_deletions` into it, and define
failure/reconciliation handling. No external journal integration is implemented.
Keep a restored database isolated from API/sync traffic; reapply all subsequent
account UUID deletions and session revocations from the authoritative journal,
verify the account/data/session rows are absent, and only then restore service.
Journal retention must cover the full backup retention/restore horizon. Backups
must age out under a documented policy. Production deletion/restore guarantees
remain blocked until this guard, operational tests, and any future provider data
cleanup are implemented and verified. Do not claim immediate erasure from backups.

## Local containers

Set `SMART_STICKY_DEV_DB_PASSWORD` to a local development password in your shell,
and `SMART_STICKY_DEV_DATABASE_URL` (host `database`, database/user
`smart_sticky_dev`, URL-encoded password) in your shell. From this directory run:

```sh
docker compose up -d database
docker compose run --build --rm api python -m smart_sticky_backend.database
docker compose up -d api
```
PostgreSQL persists in a named volume and has no host port. The API currently
connects through the supplied URL. Changing the password after first initialization does
not rotate an existing volume's PostgreSQL credentials.

## Configuration

| Variable | Behavior |
| --- | --- |
| `SMART_STICKY_ENV` | `development` (default), `test`, or `production` |
| `SMART_STICKY_ALLOWED_HOSTS` | Comma-separated explicit hostnames; required in production |
| `SMART_STICKY_DATABASE_URL` | Optional PostgreSQL URL; production requires credentials and `sslmode=verify-full` |

Production operation is not ready: deployment must add TLS termination, reviewed
dependency locking/security scanning, tested migrations, readiness,
observability, and the remaining security controls. Do not expose the local
Compose stack publicly. Top-level dependency pins are a baseline, not a complete
transitive lock or a security review. Configuration rejects unsafe defaults but
does not establish database connectivity or validate certificate installation.
The in-process rate limit is a bounded backstop, not a distributed abuse service.
Configure edge timeouts/body limits and rate limits before multi-worker exposure.
