# ADR-002: PostgreSQL account revision and optimistic sync

Status: Implemented foundation under owner delegation; release review pending.

Each accepted operation locks its owning user row and advances that account's
revision in the same transaction as note mutation and idempotency receipt.
This prevents commit order gaps inherent in using a sequence as a sync cursor.
Requests authenticate ownership; client-supplied account IDs are never accepted.

An operation UUID is unique within an account. Canonical request hashing rejects
reuse with different input. Successful retries return the original receipt.
Conflicting requests return 409 and current scoped state without overwriting it.
Rejected operations do not create receipts; they may be retried after resolution.
Base revision zero creates; updates require an exact current revision.

Deletion retains an empty-content tombstone permanently in this foundation;
updates cannot resurrect it. Restoring content requires a new note ID. Tombstone
or receipt expiration requires a future cursor-expiration/full-resync protocol.
Incremental pull returns the latest state of each changed note ordered by revision.
Pagination never advances beyond returned records. Intermediate edits are not an
audit log. Notes are the only implemented replicated aggregate in this milestone.

Deployment gate: test concurrency, replay, isolation and rollback against actual
PostgreSQL before release. SQLite is not a substitute for these locking tests.
