# Data Architecture

**Project:** Smart Sticky Wallpaper  
**Phase:** 03 — Architecture  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define conceptual data ownership, local/server persistence, synchronization metadata, deletion semantics, AI-derived data separation, integrity, and schema-evolution rules.  
**Governed By:** `PROJECT_CONSTITUTION.md` and `SYSTEM_ARCHITECTURE.md`

---

## 1. Data Principles

1. Local data is working state, not merely cache.
2. Server state is authoritative for protected/shared server-owned facts.
3. Every durable field has an owning domain.
4. Authored and derived data remain distinguishable.
5. Sync metadata is explicit.
6. Deletion is modeled, not inferred from absence.
7. Migrations are version-controlled.
8. Data collection is minimized.
9. Account isolation is mandatory.

---

## 2. Data Categories

Architecture distinguishes:

- authored user data,
- deterministic-derived data,
- AI-derived data,
- sync metadata,
- identity/session metadata,
- entitlement/billing metadata,
- device metadata,
- operational/audit metadata.

These categories may have different ownership, retention, and sync behavior.

---

## 3. Core Conceptual Entities

```text
User
├── Note
│   ├── CanvasPlacement
│   └── Reminder
├── Settings
├── Device
├── Entitlement
├── SyncOperation / SyncCursor
├── AIInsight
└── Audit / Provider Events (as justified)
```

Logical relationships do not require identical physical schemas locally and server-side.

---

## 4. User

Conceptual fields:

- `user_id`
- authentication identifier(s)
- account status
- created/updated timestamps
- deletion lifecycle state

Password/session secrets are handled by the identity architecture, not ordinary profile fields.

---

## 5. Note

Owned by Notes domain.

Conceptual fields:

- `note_id`
- `user_id`
- authored content
- lifecycle status
- priority
- pinned state
- authored/modified timestamps
- archive state
- sync revision metadata

AI-derived attributes should not be merged indistinguishably into authored fields.

---

## 6. CanvasPlacement

Owned by Canvas domain.

Conceptual fields:

- placement ID,
- note ID,
- surface/context ID,
- logical x/y,
- logical width/height,
- z-order,
- opacity,
- visibility,
- appearance configuration,
- revision/update metadata.

Placement must not permanently encode one device's raw pixel coordinate system.

---

## 7. Reminder

Owned by Reminders domain.

Conceptual fields:

- reminder ID,
- note ID/user ID,
- schedule/time window,
- timezone semantics,
- recurrence definition,
- enabled state,
- snooze state where applicable,
- completion/occurrence state,
- revision/update metadata.

Explicit user schedule remains authoritative over AI suggestion metadata.

---

## 8. Settings

Use typed settings or versioned structured configuration for:

- intelligence controls,
- AI privacy controls,
- notification preferences,
- surface preferences,
- appearance defaults,
- privacy preferences.

Do not create an uncontrolled global key-value dump.

---

## 9. Device

A device record may support:

- sync/device identity,
- platform,
- app version,
- last seen,
- session association,
- coarse capability snapshot where useful,
- revocation state.

Do not collect unnecessary hardware fingerprinting data.

---

## 10. Entitlement

Server-owned normalized entitlement state may include:

- user ID,
- entitlement key,
- status,
- source/provider,
- validity period,
- provider reference,
- version/update timestamp.

Client stores only a bounded snapshot/cache.

---

## 11. AI-Derived Data

AI-derived records may include:

- task/type,
- source note ID(s),
- normalized result,
- confidence,
- provenance,
- provider/model identifier where useful,
- schema/prompt version where useful,
- created timestamp,
- user confirmation state.

AI-derived state must be removable/recomputable without corrupting authored data.

---

## 12. Local Database

Local persistence must support:

- offline notes,
- canvas state,
- reminders,
- settings,
- durable outbox,
- tombstones/revisions,
- account namespace/isolation,
- bounded entitlement snapshot,
- selected derived metadata.

Exact KMP-compatible relational database technology is ADR-001.

---

## 13. Server Database

PostgreSQL stores:

- account identity/profile state,
- synchronized user data,
- sync revisions/cursors,
- entitlements,
- provider verification references,
- AI-derived records where required,
- security/admin audit records,
- operational records justified by requirements.

Provider raw payloads are not retained indefinitely without purpose.

---

## 14. Atomic Local Outbox

A local user mutation and its sync operation should be committed atomically where possible:

```text
Transaction
├── mutate entity
└── append outbox operation
Commit
```

This is a key local-first integrity invariant.

---

## 15. Sync Metadata

Sync-relevant records need sufficient metadata such as:

- entity ID,
- account/user ID,
- local mutation ID,
- operation ID/idempotency key,
- server revision,
- origin device where useful,
- tombstone/deletion revision,
- timestamps for diagnostics/business rules, not as sole conflict truth,
- sync status.

---

## 16. Sync Flow

```text
Local Mutation + Outbox
  ↓
Authenticated Sync Submission
  ↓
Server Ownership Validation
  ↓
Revision / Idempotency Check
  ↓
Domain Mutation
  ↓
Server Revision Advance
  ↓
Ack / Conflict / Rejection
  ↓
Client Reconciliation
```

---

## 17. Conflict Categories

Conflict policy must explicitly cover:

- note-content edit vs edit,
- edit vs delete,
- canvas movement vs movement,
- reminder schedule vs schedule,
- appearance/state changes,
- stale client after long offline period.

Prefer domain-aware reconciliation over whole-object blind overwrite.

Exact conflict algorithm is ADR-002.

---

## 18. Deletion Model

Distinguish:

- archive,
- active soft deletion/tombstone,
- hard deletion after policy/retention.

A tombstone should carry enough identity/revision information to prevent stale-device resurrection during its synchronization-safe lifetime.

Account deletion uses the lifecycle defined by Phase 7.

---

## 19. Authored vs Derived Precedence

When values conflict:

```text
Explicit Authored/User State
>
Approved Deterministic State
>
AI Suggestion
```

Derived data cannot silently overwrite authored state.

---

## 20. Account Isolation

Every user-owned server record must be scoped to an owning user/account.

Local storage must use either:

- separate per-account stores, or
- strict account namespace keys with safe clearing/switching semantics.

Account switch tests are mandatory.

---

## 21. Referential Integrity

Use, where appropriate:

- foreign keys,
- uniqueness constraints,
- check constraints,
- transactions,
- ownership-scoped queries,
- invariant validation in domain/application layer.

DB constraints complement, not replace, authorization.

---

## 22. Idempotency Records

Durable/replay-sensitive flows may require deduplication records for:

- sync operations,
- billing verification,
- billing webhooks,
- rewarded-ad rewards,
- exports/jobs.

Retention should be long enough to cover realistic replay windows.

---

## 23. Indexing

Expected access patterns include:

- notes by user/lifecycle,
- sync changes by user/revision,
- outbox by state/order locally,
- due reminders,
- entitlement lookup,
- provider event IDs,
- audit lookup by actor/time.

Indexes should follow actual query patterns and measured behavior.

---

## 24. Schema Evolution

All schema changes use migrations.

For risky changes prefer:

```text
Expand
→ Deploy Compatible Code
→ Backfill
→ Verify
→ Contract Later
```

Client and server migrations evolve independently but must preserve protocol compatibility.

---

## 25. Export Architecture

Export is generated from authorized user-owned data only.

Exports must not include:

- credentials,
- server secrets,
- other users' data,
- internal provider secrets,
- unnecessary security metadata.

Temporary export artifacts follow bounded retention.

---

## 26. Privacy & Retention

Data architecture must support:

- data minimization,
- user deletion,
- export,
- AI opt-out/removal behavior,
- bounded operational logs,
- backup aging,
- provider-specific retention rules.

Inactive user data must not be deleted merely because it is old unless an approved retention policy says so.

---

## 27. Backups

Backups must preserve recoverability without becoming uncontrolled copies.

Requirements:

- access restricted,
- retention bounded,
- restore tested,
- deleted data not reintroduced into active state contrary to lifecycle policy.

---

## 28. Data ADR Dependencies

Before affected implementation:

- local DB technology,
- sync revision/cursor model,
- conflict algorithm,
- tombstone retention semantics,
- session/device identity representation,
- final entitlement snapshot schema.

---

## 29. Consolidated Review Criteria

Verify:

- every Phase 2 data requirement has an ownership/storage path,
- sync/deletion model prevents resurrection and duplicates,
- Phase 5 AI provenance fits cleanly,
- Phase 6 entitlement states fit the model,
- Phase 7 deletion/retention/export lifecycle is supported,
- Phase 8 migration/sync tests can be implemented.

Phase 3 remains **Draft** until final project freeze.
