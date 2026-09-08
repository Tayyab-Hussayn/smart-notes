# Backend Architecture

**Project:** Smart Sticky Wallpaper  
**Phase:** 03 — Architecture  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define FastAPI backend modules, boundaries, API behavior, synchronization responsibilities, provider integration, background work, security, and operational architecture.  
**Governed By:** `PROJECT_CONSTITUTION.md` and `SYSTEM_ARCHITECTURE.md`

---

## 1. Backend Style

Use a **modular monolith** initially.

```text
FastAPI Application
├── identity
├── users
├── notes
├── canvas
├── reminders
├── sync
├── intelligence
├── ai_gateway
├── entitlements
├── billing
├── advertising
├── settings
├── admin
├── jobs
└── health_observability
```

These are logical modules with explicit ownership, not independent microservices.

---

## 2. Module Internal Shape

Preferred flow:

```text
HTTP Router
  ↓
Request Schema / Auth Context
  ↓
Application Service
  ↓
Domain Policy
  ↓
Repository / Provider Port
  ↓
PostgreSQL / External Adapter
```

Routers remain thin. Provider SDK logic remains in infrastructure adapters.

---

## 3. Dependency Rule

Modules may depend on explicit public contracts but should not reach into another module's persistence internals.

Cross-domain workflows belong in application services rather than hidden database coupling.

---

## 4. Identity Architecture

Backend identity responsibilities:

- registration/login where supported,
- secure credential verification,
- access/session lifecycle,
- refresh/revocation behavior,
- account deletion initiation,
- protected endpoint authentication,
- device/session association where used.

Exact token/session mechanism requires ADR-003.

---

## 5. Authorization

Every protected operation establishes:

1. authenticated identity,
2. resource ownership/access scope,
3. role where applicable,
4. entitlement where applicable,
5. resource-state constraints.

Repository queries should be user-scoped where practical to reduce accidental IDOR exposure.

---

## 6. Notes / Canvas / Reminder APIs

These modules expose product operations, not raw table CRUD.

Examples:

- create/update/archive/delete/duplicate note,
- pin/unpin,
- update canvas placement,
- create/edit/snooze/complete reminder.

Network mutations that may be retried should carry idempotency semantics where duplicate effects matter.

---

## 7. Sync Service Boundary

Sync service owns replication protocol concerns:

- accept operation batches or equivalent mutation form,
- authenticate account/device context,
- validate operation IDs,
- compare revisions,
- apply authorized domain mutations,
- generate acknowledgments/conflicts,
- expose incremental changes/cursor,
- propagate tombstones,
- support replay-safe retry.

Sync does **not** bypass domain validation.

---

## 8. Server Revision Model

Each sync-relevant aggregate/entity should carry server revision metadata sufficient to:

- identify stale writes,
- order accepted changes where required,
- construct incremental synchronization,
- prevent accidental resurrection.

Exact revision/cursor design is an ADR.

---

## 9. Idempotency

Persist or otherwise reliably track idempotency for operations such as:

- sync mutations,
- purchase verification requests,
- provider webhook events,
- rewarded-ad reward grants,
- important asynchronous commands.

Duplicate input must not create duplicate durable effects.

---

## 10. AI Gateway

```text
Intelligence Application Service
  ↓
AI Gateway
  ↓
Provider Adapter
```

Gateway responsibilities:

- task-specific request schema,
- privacy/data-minimization checks,
- entitlement/quota checks,
- provider selection,
- timeout/retry policy,
- structured-output validation,
- normalized errors,
- provenance/usage metadata,
- cost/latency observability.

Provider output is untrusted data.

---

## 11. AI Persistence Boundary

Persist only AI-derived state required by product behavior.

Keep separate from authored data and store provenance/confidence/version metadata where useful.

Raw prompts/responses must not become unrestricted logs.

---

## 12. Entitlement Architecture

```text
Billing Provider Truth
  ↓
Verification Adapter
  ↓
Normalized Subscription State
  ↓
Entitlement Resolver
  ↓
Versioned Entitlement Snapshot
  ↓
Client
```

Backend entitlement state is authoritative for protected application access.

---

## 13. Billing Webhooks

Webhook processing must support:

- provider authentication/signature verification,
- timestamp/replay validation where supported,
- provider event ID deduplication,
- idempotent state transitions,
- auditability,
- retry/reconciliation.

Invalid webhook input must not modify entitlement state.

---

## 14. Advertising Boundary

Backend does not become dependent on ad delivery SDKs.

It may own:

- ad eligibility/configuration,
- reward validation/grant records,
- feature flags,
- entitlement-based suppression.

Private note content is never an ad-selection input.

---

## 15. Configuration

Configuration categories:

- feature availability,
- AI quotas/timeouts,
- entitlement mappings,
- ad eligibility/frequency limits,
- reminder defaults,
- provider routing.

Secrets are injected separately from ordinary configuration.

Critical authorization/data-integrity controls cannot be disabled by ordinary feature flags.

---

## 16. API Contract

API responses/errors should be predictable.

Conceptual error:

```text
code
message
request_id
details?   # safe structured details only
```

Do not expose stack traces/provider secrets/internal SQL details.

---

## 17. API Versioning

Architecture must support client releases that cannot update instantly.

Use an explicit compatibility/versioning policy with:

- additive evolution where practical,
- deprecation window,
- tolerant clients,
- breaking-change review.

Exact policy is ADR-005.

---

## 18. Background Jobs

Jobs may handle:

- AI tasks,
- billing reconciliation,
- cleanup/retention,
- exports,
- notification coordination,
- operational maintenance.

Requirements:

- durable state for important work,
- retry policy,
- idempotency,
- failure observability,
- bounded execution.

Exact worker/queue technology is ADR-004.

---

## 19. Database Access

Repository adapters isolate PostgreSQL.

Use transactions for invariants spanning multiple writes.

Important access patterns should be indexed based on measured or known requirements.

The database is not publicly exposed.

---

## 20. Administrative Architecture

Admin capabilities are separate from user APIs and follow least privilege.

Admin tools should expose operational metadata without casual access to raw note content.

High-impact actions require audit records.

---

## 21. Security Controls

Backend baseline:

- TLS termination,
- authentication,
- ownership authorization,
- request schema/payload limits,
- rate limits,
- secrets management,
- parameterized DB access,
- secure webhook validation,
- audit logging,
- safe errors,
- dependency/container hardening.

---

## 22. Rate-Limit Classes

At minimum define independent policy classes for:

- authentication/recovery,
- AI,
- sync writes,
- export,
- billing verification,
- webhooks,
- admin operations.

Limits remain configurable.

---

## 23. Observability

Backend should provide:

- liveness/readiness health,
- structured logs,
- request IDs,
- latency/error metrics,
- DB health,
- sync metrics,
- worker/job metrics,
- provider failures,
- billing/webhook failures,
- AI usage/cost signals.

Logs exclude raw note content and secrets by default.

---

## 24. Deployment

Initial topology:

```text
Reverse Proxy / TLS
  ↓
FastAPI Container(s)
  ├── Worker Container(s) if required
  ↓
PostgreSQL
```

Use Docker health/restart controls and environment-specific secrets/config.

---

## 25. Evolution Strategy

Only extract a module into a separate service when evidence shows meaningful need for:

- independent scaling,
- fault isolation,
- security isolation,
- deployment cadence,
- resource specialization.

Preserve domain/API contracts during extraction.

---

## 26. Required Backend ADRs

- auth/session strategy,
- sync protocol/revision/cursor model,
- background worker/queue,
- API versioning,
- local/server reconciliation semantics,
- AI provider routing strategy,
- observability provider/stack.

---

## 27. Consolidated Review Criteria

Verify:

- every Phase 2 backend/security MUST has a boundary,
- entitlement lifecycle matches Phase 6,
- AI gateway matches Phase 5/7,
- threat-model controls are implementable,
- Phase 8 integration/security tests can target these boundaries,
- Phase 10 deployment model matches this architecture.

Phase 3 remains **Draft** until final project freeze.
