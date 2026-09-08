# System Architecture

**Project:** Smart Sticky Wallpaper  
**Phase:** 03 — Architecture  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define system-wide boundaries, responsibility allocation, trust boundaries, runtime topology, cross-domain contracts, and architecture constraints.  
**Governed By:** `00-project-governance/PROJECT_CONSTITUTION.md`  
**Primary Input:** Phase 02 SRS

---

## 1. Architecture Drivers

The architecture must optimize for:

1. local-first core note interaction,
2. cross-platform capability variance,
3. deterministic behavior before AI,
4. safe multi-device synchronization,
5. replaceable external providers,
6. strong user-data isolation,
7. production observability,
8. incremental delivery without premature distributed systems.

---

## 2. Architectural Style

Initial system style:

- **client:** modular Kotlin Multiplatform application with native capability adapters,
- **backend:** FastAPI modular monolith,
- **server persistence:** PostgreSQL,
- **deployment:** Docker on VPS,
- **integration style:** versioned HTTP API + durable background work where justified,
- **AI:** provider-neutral gateway,
- **monetization:** provider-neutral billing/ads boundaries.

Microservices are explicitly deferred until evidence justifies them.

---

## 3. System Context

```text
User
  ↓
Platform Client
  ├── Shared KMP Core
  ├── Local Database
  ├── Platform Capability Adapters
  └── Secure Session Storage
        │
        │ HTTPS / Versioned API
        ▼
FastAPI Modular Monolith
  ├── Identity
  ├── Notes
  ├── Canvas
  ├── Reminders
  ├── Sync
  ├── Intelligence / AI Gateway
  ├── Entitlements / Billing
  ├── Ad Eligibility / Rewards
  ├── Settings
  ├── Admin
  └── Observability
        │
        ├── PostgreSQL
        ├── AI Providers
        ├── Billing Providers
        ├── Notification Integrations where needed
        └── Operational Infrastructure
```

---

## 4. Layering

Preferred logical dependency direction:

```text
Presentation
    ↓
Application
    ↓
Domain
    ↑
Infrastructure Adapters
```

### Presentation
Owns screens, canvas interaction, navigation, accessibility semantics, and presentation state.

### Application
Coordinates use cases, transactions, policies, and domain collaboration.

### Domain
Owns product rules independent of UI, network, persistence technology, and provider SDKs.

### Infrastructure
Implements storage, HTTP, platform capabilities, scheduling, AI, billing, ads, telemetry, and provider integrations.

Domain logic must not depend directly on infrastructure implementations.

---

## 5. Domain Ownership

| Domain | Owns | Must Not Own |
|---|---|---|
| Notes | authored note content/lifecycle | canvas placement, billing |
| Canvas | placement/visual-surface state | authored content semantics |
| Reminders | explicit schedules/recurrence | AI inference authority |
| Intelligence | relevance/suggestions | source note persistence |
| Sync | replication protocol/state | product business rules |
| Identity | user/session identity | entitlements |
| Monetization | entitlements/billing/ad eligibility | note ownership |
| Notifications | delivery of approved alerts | reminder truth |
| Settings | typed user preferences | arbitrary domain storage |

Cross-domain access should occur through explicit application interfaces/contracts.

---

## 6. Data Classification

Architecture must keep these categories conceptually distinct:

- **authored data** — user-entered content,
- **deterministic-derived data** — parsed/scheduled/calculated state,
- **AI-derived data** — model inferences/suggestions,
- **provider metadata** — billing/AI/ad provider references,
- **sync metadata** — revisions, operation IDs, tombstones,
- **operational telemetry** — logs/metrics/audit metadata.

AI/provider/telemetry data must not silently become authored truth.

---

## 7. Client–Server Responsibility

### Client is authoritative for immediate local interaction

Client owns:

- local note CRUD,
- local canvas state,
- offline queueing,
- deterministic local relevance inputs,
- platform capability execution,
- local reminder representation,
- presentation of verified server state.

### Server is authoritative for protected shared state

Server owns:

- authentication/session validation,
- authorization,
- synchronized cloud state,
- cross-device reconciliation authority,
- server-verified entitlement state,
- provider credentials,
- AI provider orchestration where remote AI is used,
- protected configuration,
- administrative operations.

The server must not be required for every local note interaction.

---

## 8. Local-First Write Path

```text
User Action
  ↓
Domain Validation
  ↓
Local Transaction
  ├── Entity Mutation
  └── Outbox Operation
  ↓
UI Observes Local State
  ↓
Async Sync
  ↓
Server Validation / Reconciliation
  ↓
Client Applies Acknowledgment or Conflict Result
```

Entity mutation and outbox creation should be atomic where the selected local DB supports it.

---

## 9. Sync Architecture Baseline

Required architecture shape:

- durable local outbox,
- operation identifier/idempotency key,
- entity/server revision,
- authenticated account/device context,
- incremental sync cursor or equivalent,
- tombstone/deletion propagation,
- deterministic domain-aware conflict handling,
- retry with bounded backoff,
- convergence after reconnect.

Blanket last-write-wins is not the baseline.

**ADR required before implementation:** exact wire protocol and conflict algorithm.

---

## 10. Persistent Surface Architecture

Persistent surfaces are capabilities, not one universal feature.

```text
Surface Request
  ↓
Capability Registry
  ↓
Platform Adapter
  ├── Native Surface
  ├── Approved Alternative
  ├── Fallback
  └── Unsupported State
```

The application-resident canvas remains available even when an OS cannot provide the ideal external surface.

---

## 11. Intelligence Architecture

Decision precedence:

```text
Explicit User Intent
>
Hard Deterministic Rules
>
Contextual Deterministic Scoring
>
Validated AI Assistance
```

AI cannot grant authorization, entitlement, deletion authority, or bypass confirmation policy.

---

## 12. External Provider Boundary

All replaceable external systems sit behind owned interfaces.

Examples:

- `AiGateway`
- `BillingVerifier`
- `AdProvider`
- `NotificationAdapter`
- `TelemetrySink`

Provider DTOs/SDK types must not leak into core domain contracts.

---

## 13. API Boundary

Remote operations use explicit versioned contracts with:

- authentication,
- server-side authorization,
- schema validation,
- predictable errors,
- idempotency where durable duplicate effects matter,
- backward-compatible evolution where practical,
- request/correlation identifiers.

Clients must tolerate additive response fields and documented compatibility evolution.

---

## 14. Background Work

Background execution is justified for work such as:

- AI jobs too expensive for request latency,
- billing/webhook reconciliation,
- cleanup/retention tasks,
- notification coordination where server-side processing is required,
- operational maintenance.

Do not introduce a queue merely because asynchronous technology is fashionable.

**ADR required:** initial worker/queue mechanism.

---

## 15. Security Trust Boundaries

Primary trust boundaries:

1. untrusted local/user input,
2. client process/device boundary,
3. authenticated network boundary,
4. authorization boundary,
5. database boundary,
6. admin boundary,
7. external AI boundary,
8. billing/webhook boundary,
9. advertising/telemetry boundary.

Clients are treated as potentially compromised.

---

## 16. Failure Isolation

Required behavior:

- AI outage → core notes continue,
- sync outage → local authored work remains safe,
- billing outage → no false grants; bounded verified-state grace may apply,
- ad outage → no core-product failure,
- notification failure → reminder state remains correct,
- persistent-surface failure → in-app access remains available,
- telemetry outage → product does not fail.

---

## 17. Observability

System-level observability should distinguish:

- client/app errors,
- API errors,
- authorization failures,
- sync conflict/failure rates,
- background job failures,
- AI provider latency/errors/cost,
- billing verification/webhook failures,
- DB health,
- release/version correlation.

Raw note content and secrets are excluded by default.

---

## 18. Deployment Topology

Initial production shape:

```text
Internet
  ↓
Reverse Proxy / TLS
  ↓
FastAPI Container(s)
  ├── Background Worker(s) if justified
  ↓
PostgreSQL
```

Operational additions such as cache/queue/object storage require evidence and an ADR when architecturally significant.

---

## 19. Scalability Strategy

Scale in this order where evidence supports it:

1. query/index optimization,
2. vertical resource adjustment,
3. stateless API replication,
4. worker separation,
5. targeted caching,
6. high-load module extraction.

Service decomposition must preserve domain contracts.

---

## 20. Architecture Invariants

Implementation must not:

- place core business rules in UI components,
- make cloud access mandatory for core note editing,
- trust client entitlement assertions,
- couple domain logic to provider SDKs,
- assume OS capability parity,
- let AI override explicit user intent,
- mix account-local data across users,
- bypass documented migrations,
- introduce distributed complexity without measurable need.

---

## 21. Required ADRs Before Affected Implementation

At minimum:

- ADR-001 — local database technology,
- ADR-002 — sync protocol + conflict model,
- ADR-003 — authentication/session strategy,
- ADR-004 — background work/queue mechanism,
- ADR-005 — API versioning policy,
- ADR-006 — platform capability registry semantics,
- ADR-007 — AI execution/provider boundary details,
- ADR-008 — entitlement verification/cache semantics,
- ADR-009 — telemetry/observability provider strategy.

Not every ADR must select a vendor; each must remove ambiguity required for implementation.

---

## 22. Consolidated Review Criteria

Before final freeze:

- Phase 2 MUST requirements are architecturally satisfiable,
- domain ownership has no overlaps/holes,
- sync/auth/local-DB ADRs are resolved,
- platform capability model is explicit,
- AI/monetization/security boundaries align with Phases 5–7,
- quality/operations requirements align with Phases 8–10,
- no architecture assumption contradicts the Project Constitution.

Phase 3 remains **Draft** until the final consolidated freeze.
