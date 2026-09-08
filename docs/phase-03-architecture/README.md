# Phase 03 — Architecture

**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending

Phase 3 converts product requirements into enforceable technical boundaries without prematurely choosing every implementation technology.

## Documents

- `SYSTEM_ARCHITECTURE.md` — system context, domain ownership, responsibility allocation, trust/failure boundaries, runtime topology, ADR register.
- `CLIENT_ARCHITECTURE.md` — KMP/local-first client, state, sync, platform capabilities, identity, entitlement, native integration.
- `BACKEND_ARCHITECTURE.md` — FastAPI modular monolith, APIs, sync, AI, billing, jobs, security, observability, deployment.
- `DATA_ARCHITECTURE.md` — data categories, entities, local/server storage, outbox, revisions, conflicts, deletion, integrity, migrations.

## Architectural Invariants

1. Local note CRUD does not depend on cloud availability.
2. Clients are not trusted for authorization or entitlement truth.
3. Platform parity is capability-based, not assumed.
4. AI is assistive and provider-neutral.
5. Provider SDKs do not leak into core domains.
6. Sync is revisioned, idempotent, and deletion-aware.
7. Account-local data cannot cross user boundaries.
8. Backend begins as a modular monolith.
9. Migrations are explicit and version-controlled.
10. Observability excludes private note content by default.

## ADRs Required Before Affected Implementation

| ADR | Decision |
|---|---|
| ADR-001 | Local database technology |
| ADR-002 | Sync protocol, revision/cursor, conflict model |
| ADR-003 | Authentication/session strategy |
| ADR-004 | Background worker/queue mechanism |
| ADR-005 | API versioning/compatibility policy |
| ADR-006 | Platform capability registry semantics |
| ADR-007 | AI execution/provider routing boundary |
| ADR-008 | Entitlement verification/cache semantics |
| ADR-009 | Telemetry/observability provider strategy |

These decisions are intentionally explicit rather than hidden in implementation assumptions.

## Freeze Policy

Phase 3 is not frozen independently.

Before final project freeze:

- reconcile against Phase 2 SRS V2,
- resolve implementation-blocking ADRs,
- verify Phase 4–10 compatibility,
- remove contradictions,
- then include Phase 3 in consolidated freeze.

## Implementation Rule

> Architecture may evolve through an approved ADR; implementation may not silently redefine architecture.
