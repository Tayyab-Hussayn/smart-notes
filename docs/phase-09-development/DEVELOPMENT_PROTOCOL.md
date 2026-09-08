# Development Protocol

**Project:** Smart Sticky Wallpaper  
**Phase:** 09 — Development Protocol  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define the implementation workflow, engineering rules, quality expectations, security/privacy obligations, evidence requirements, and integration discipline for human or AI-assisted development.  
**Governed By:** `PROJECT_CONSTITUTION.md`  
**Primary Inputs:** Phase 02–08 V2 specifications

---

## 1. Core Rule

> An agent or developer may implement the specification, but may not silently redefine it.

Implementation is subordinate to the frozen specification set and approved ADRs.

---

## 2. Required Workflow

```text
Read Specs
→ Confirm Scope
→ Resolve Blocking ADRs
→ Identify Dependencies
→ Define Acceptance Criteria
→ Define Test Plan
→ Implement
→ Run Local Validation
→ Produce Handoff Evidence
→ Review
→ Integrate
→ Run Quality Gates
→ Update Docs if Required
```

---

## 3. Definition of Ready

Work may begin only when:

- requirement is clear,
- owning module is known,
- relevant specs are identified,
- dependencies are known,
- required ADRs are resolved,
- acceptance criteria exist,
- test approach is known,
- platform scope is known,
- security/privacy implications are understood,
- providers are available or mockable.

If any critical item is missing, report the gap instead of guessing.

---

## 4. Source-of-Truth Order

```text
Project Constitution
→ Approved ADRs
→ Frozen Phase Specifications
→ Accepted API / Schema Contracts
→ Implementation
→ Test Evidence
```

Tests validate intended behavior; they do not silently redefine product behavior.

---

## 5. Architecture Boundaries

Primary domains:

- Notes
- Canvas
- Reminders
- Intelligence
- Sync
- Identity
- Monetization
- Notifications
- Settings

Preferred layering:

```text
Presentation
→ Application
→ Domain
← Infrastructure Adapters
```

Provider SDKs, transport details, UI frameworks, and storage implementations must not leak into core domain contracts.

---

## 6. Client Rules

Client implementation must:

- use KMP for shared logic where practical,
- use Compose Multiplatform where practical,
- use native APIs for actual OS capabilities,
- preserve local-first note operations,
- use capability-based fallbacks,
- preserve account isolation,
- treat secure storage separately from ordinary local DB state,
- avoid unsupported platform hacks.

---

## 7. Backend Rules

Backend implementation must:

- remain a FastAPI modular monolith initially,
- use PostgreSQL,
- enforce server-side authorization,
- validate all external inputs,
- use versioned/compatible APIs,
- isolate provider adapters,
- expose health/observability,
- support idempotent durable operations where required,
- avoid premature microservices.

---

## 8. Rust Rule

Use Rust only when:

- native/system integration materially benefits,
- measurable performance need exists,
- KMP/native implementation is insufficient,
- the added operational/build complexity is justified.

Rust must not become a parallel business-logic stack.

---

## 9. Local-First Rule

Core note CRUD must not depend on:

- network,
- AI,
- billing,
- ads.

Local mutation and durable sync intent should remain consistent.

Where the selected local DB permits it:

```text
Local Entity Mutation
+
Outbox Operation
=
One Transaction
```

---

## 10. Sync Development Rules

Implementation must support:

- revisions,
- idempotency,
- durable outbox,
- retries/backoff,
- tombstones,
- deletion safety,
- multi-device convergence,
- process restart recovery,
- account isolation,
- partial failure handling.

Do not implement blanket last-write-wins unless explicitly approved by ADR.

---

## 11. Account Isolation

Every feature touching local or remote user state must answer:

- which account owns the data?
- how is it scoped locally?
- how is it authorized remotely?
- what happens on account switch?
- what cache/state must be invalidated?

Cross-account leakage is a P0 defect.

---

## 12. API Development Rules

API changes require:

1. contract/schema definition,
2. ownership/auth rules,
3. compatibility review,
4. stable error behavior,
5. implementation,
6. client impact handling,
7. contract/integration tests,
8. documentation.

Breaking changes require explicit approval.

---

## 13. Database Rules

All schema changes use version-controlled migrations.

For risky changes prefer:

```text
Expand
→ Deploy Compatible Code
→ Backfill
→ Verify
→ Contract Later
```

Destructive changes require explicit review, backup/recovery thinking, and representative migration testing.

---

## 14. Background Jobs

Important background work must be:

- durable where loss would matter,
- idempotent,
- retry-bounded,
- observable,
- safe after worker restart.

Jobs must not become an alternate hidden source of business truth.

---

## 15. Dependency Policy

Before adding a major dependency evaluate:

- necessity,
- maintenance,
- security,
- license,
- platform compatibility,
- package/runtime cost,
- supply-chain risk,
- lock-in,
- replacement path,
- testability.

Avoid redundant libraries.

---

## 16. Supply-Chain Rules

Implementation workflow should:

- use lockfiles/version pinning,
- scan dependencies,
- review high-risk native/provider SDKs,
- preserve artifact provenance where supported,
- generate/retain SBOM where practical for release artifacts,
- avoid installing unreviewed tools into privileged CI paths.

---

## 17. Secrets

Never commit:

- DB credentials,
- AI keys,
- billing credentials,
- webhook secrets,
- signing keys,
- deploy credentials,
- notification provider credentials.

Production secrets must not be available to ordinary coding agents or untrusted CI jobs.

---

## 18. AI Development

AI features must use:

- provider-neutral task contracts,
- structured output validation,
- deterministic fallback,
- privacy eligibility,
- quota/cost controls,
- prompt-injection boundaries,
- versioned task/schema behavior where relevant,
- confirmation for consequential inferred actions.

Model output is untrusted data.

---

## 19. Monetization Development

Implement:

- server-authoritative entitlement,
- server-side purchase verification,
- named entitlement keys,
- idempotent billing events,
- out-of-order provider event protection,
- reconciliation,
- bounded offline entitlement cache,
- non-destructive downgrade,
- isolated ad SDKs,
- duplicate-safe reward grants.

---

## 20. Security & Privacy Review

Every feature must consider:

- authentication,
- authorization,
- account ownership,
- input validation,
- abuse/rate limits,
- secrets,
- logs,
- external data flows,
- retention,
- deletion/export impact.

Raw note content must not enter general telemetry by default.

---

## 21. Platform Capability Rule

Platform behavior changes require:

- capability declaration,
- native implementation or documented fallback,
- unsupported-state handling,
- platform verification,
- capability matrix update.

Do not implement fake parity.

---

## 22. Accessibility Rule

UI features must include accessibility during implementation, not after completion.

Check:

- semantic labels,
- keyboard/focus behavior,
- scalable text,
- reduced motion,
- non-color-only meaning,
- alternatives to essential drag-only actions.

---

## 23. Testing During Development

Tests are implemented with the feature.

As applicable:

- unit,
- component/UI,
- integration,
- contract,
- regression,
- security/abuse,
- privacy,
- failure-path,
- performance.

Build success alone is not completion.

---

## 24. Phase 8 Integration

Every change must satisfy the applicable Phase 8 gate:

- G0 during local development,
- G1 before merge,
- G2 after integration,
- G3 release candidate,
- G4 production release.

Agents may not weaken gates to make work pass.

---

## 25. Error Handling

Errors must:

- preserve recoverable user state,
- use stable categories/codes,
- avoid secrets/private content,
- support meaningful UX,
- be observable,
- distinguish retryable from permanent failures.

---

## 26. Observability

New production-impacting behavior should define:

- useful logs,
- relevant metrics,
- request/job IDs where needed,
- provider error signals,
- operational failure detection.

Observability must remain privacy-safe.

---

## 27. Feature Flags / Kill Switches

Feature flags may control noncritical behavior such as:

- AI rollout,
- provider routing,
- ad placements,
- experimental capabilities.

Flags may not disable:

- authentication,
- authorization,
- ownership checks,
- critical data-integrity controls.

---

## 28. Documentation Discipline

Update docs/ADR when accepted behavior changes:

- API,
- data schema,
- architecture,
- platform capability,
- entitlement,
- AI contract,
- security/privacy,
- operations.

Do not allow implementation to become undocumented truth.

---

## 29. Change Size

Prefer focused, reviewable changes.

Avoid combining:

- unrelated feature work,
- architecture refactor,
- migration,
- dependency upgrade,
- security change,

unless the coupling is genuinely required.

---

## 30. Implementation Evidence

Every meaningful task handoff should report:

```text
Task / Requirement IDs
Files Changed
Behavior Implemented
Tests Added
Tests Run + Results
Quality Gate Status
Security / Privacy Impact
Migration / API Impact
Known Limitations
Unresolved Issues
ADR / Spec References
```

Prose-only “done” claims are insufficient.

---

## 31. Stop Conditions

Stop implementation and escalate when:

- requirement conflicts with frozen docs,
- ADR is missing,
- architecture boundary is unclear,
- migration threatens data,
- test evidence contradicts intended behavior,
- security/privacy requirement cannot be met,
- platform capability is impossible,
- provider assumptions are unverified.

---

## 32. Completion

A feature is complete only when:

- `DEFINITION_OF_DONE.md` is satisfied,
- applicable Phase 8 quality gates pass,
- no known blocking specification conflict remains.

Phase 9 remains **Draft** until final project freeze.
