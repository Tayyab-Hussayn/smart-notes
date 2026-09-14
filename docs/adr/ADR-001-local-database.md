# ADR-001: SQLDelight for local persistence

- Status: Accepted
- Date: 2026-09-13
- Requirements: REQ-NOTE-09, REQ-REL-02, REQ-REL-04, REQ-PRIV-08

## Decision

Use SQLDelight as the KMP relational persistence layer. Store user-authored mutations and durable outbox operations in one database transaction. Every user-owned row is account-scoped.

## Rationale

SQLDelight provides explicit SQL schemas, generated type-safe APIs, migrations, transactions, and drivers across KMP targets. Persistence stays behind repository contracts and database types do not leak into domain logic.

## Consequences

- Schema migrations are versioned and tested.
- Platform modules provide their database driver.
- Database encryption remains a separate platform/security decision.
