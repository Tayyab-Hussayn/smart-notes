# Project Constitution

**Project:** Smart Sticky Wallpaper  
**Document:** `PROJECT_CONSTITUTION.md`  
**Version:** 2.0  
**Status:** Draft — Final Review Pending  
**Purpose:** Define the non-negotiable governance, architecture, quality, security, documentation, and AI-agent rules for the project.  
**Dependencies:** None. This document governs all other project specifications.  
**Last Updated Phase:** Post Phase 10 Planning  
**Authority:** Highest project-level specification unless explicitly amended by the project owner.

---

## 1. Constitutional Role

This document defines the permanent rules under which Smart Sticky Wallpaper is designed, implemented, tested, released, and maintained.

All specifications, ADRs, implementation decisions, AI-agent actions, code changes, migrations, and release procedures must remain consistent with this constitution.

If another document conflicts with this constitution, this constitution wins unless the project owner explicitly approves an amendment.

---

## 2. Source of Truth

The `docs/` directory is the authoritative project specification.

Implementation agents must:

1. read the relevant specifications before coding,
2. follow approved architecture and product boundaries,
3. report contradictions or missing decisions,
4. avoid silently inventing requirements,
5. update affected documentation when an approved decision changes.

Code is not allowed to become the undocumented source of truth.

---

## 3. Spec-Driven Development

The project uses **Spec-Driven Development**.

The required lifecycle is:

```text
Discover
→ Specify
→ Review
→ Correct
→ Resolve ADRs
→ Consolidated Freeze
→ Implement
→ Test
→ Validate
→ Release
```

Implementation begins only after the specification set is sufficiently resolved and formally frozen by the project owner.

---

## 4. Current Freeze Policy

Individual phases may remain **Draft** while planning continues.

After all planned phases are complete:

```text
Phase 0–10 Complete
→ Cross-Phase Review
→ Resolve Conflicts / Gaps / ADRs
→ Apply Corrections
→ Final Consolidated Freeze
→ Generate MASTER_KIMI_INSTRUCTIONS.md
→ Begin Implementation
```

A frozen decision may change only through an explicit approved change or ADR.

---

## 5. Documentation Rules

Each document should:

- have one clear primary purpose,
- be concise and non-redundant,
- avoid duplicating rules already owned elsewhere,
- use precise language,
- distinguish requirements from examples,
- expose assumptions and unresolved decisions,
- remain understandable to both humans and AI agents.

### Length Guideline

Target approximately **250 lines or fewer** where practical.

This is not a hard limit.

A document may exceed the target when necessary for correctness or completeness.

Do not split a coherent specification solely to satisfy line count.

---

## 6. Required Document Metadata

Each formal specification should identify:

- purpose,
- version,
- status,
- dependencies,
- relevant phase or last-updated phase.

Status should clearly distinguish at least:

- Draft
- Review Required
- Frozen
- Superseded

---

## 7. Architecture Principles

The system must be:

- modular,
- maintainable,
- testable,
- secure,
- observable,
- API-first,
- local-first where practical,
- platform-aware,
- incrementally extensible.

Avoid premature microservices and unnecessary architectural complexity.

---

## 8. Locked Technology Direction

Baseline technology direction:

- Kotlin Multiplatform for shared client logic,
- Compose Multiplatform where practical,
- native platform APIs where OS integration requires them,
- Rust only when performance or native integration materially benefits,
- FastAPI backend,
- PostgreSQL persistence,
- Docker-based VPS deployment,
- provider-independent AI integration,
- subscription and advertising abstractions.

Technology may change only through an explicit architectural decision.

---

## 9. Platform Strategy

Target platforms:

- Android
- iOS
- Windows
- macOS
- Linux

The project must use **capability-based platform design**.

Do not assume identical behavior across platforms.

For restricted or unavailable capabilities:

- detect support,
- use native alternatives,
- provide documented fallbacks,
- never falsely advertise unsupported behavior.

---

## 10. Core Domain Boundaries

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

Business logic should remain inside the correct domain.

Cross-cutting providers must not leak implementation details into domain logic.

---

## 11. Layering Rule

Preferred logical layering:

```text
Presentation
→ Application
→ Domain
→ Infrastructure
```

Higher-level business policy should not depend directly on provider SDKs, UI frameworks, billing SDKs, AI SDKs, or storage-specific types.

---

## 12. Local-First Rule

Core note operations must remain useful without network access.

At minimum:

- create,
- read,
- edit,
- delete/archive,
- local persistence

must not require AI, billing, ads, or live backend availability.

Network failure must never destroy user-authored state.

---

## 13. Sync Rule

Sync must be explicit, deterministic, and data-safe.

The design must support:

- revisions,
- idempotency,
- offline outbox,
- retries,
- conflict detection,
- tombstones/deletion state,
- multi-device convergence,
- account isolation.

A blanket last-write-wins policy must not be adopted without explicit approval.

Exact sync protocol and conflict strategy remain ADR-level decisions until finalized.

---

## 14. AI Governance

AI is assistive, not authoritative.

Rules:

- deterministic logic wins when user intent is explicit,
- AI must remain provider-independent,
- AI output is untrusted until validated,
- AI failure must not break core note functionality,
- consequential inferred actions require confirmation,
- AI must not silently delete or irreversibly modify important user data,
- authored and AI-derived data must remain distinguishable,
- cloud AI use must respect privacy and cost controls.

Future on-device AI should fit behind the same abstraction boundary.

---

## 15. Security Rule

Security is a design constraint, not a final-stage audit.

Required principles:

- server-side authorization,
- least privilege,
- validated external input,
- secrets never embedded in clients,
- secure credential storage,
- encrypted transport,
- protected billing/webhook verification,
- rate limiting,
- auditability for privileged actions,
- security testing for critical boundaries.

Clients must be treated as potentially compromised.

---

## 16. Privacy Rule

User note content is **private application data**, not general telemetry.

The system must:

- minimize collected data,
- avoid raw note content in logs by default,
- avoid unnecessary third-party sharing,
- expose relevant AI/privacy controls,
- support account deletion,
- support data export,
- document retention and external data flows.

New external data uses require explicit review.

---

## 17. Monetization Rule

Monetization must remain isolated from core note ownership.

Rules:

- billing providers are payment source of truth,
- backend is entitlement source of truth,
- clients cannot self-grant premium access,
- ads remain behind provider abstractions,
- premium users are ad-free where specified,
- subscription expiry must not delete or lock users out of existing personal data,
- monetization must not interrupt critical note creation/editing flows.

---

## 18. Testing Rule

Compilation is not proof of correctness.

A feature is not complete until appropriate tests verify its specified behavior.

Critical areas require strong coverage:

- authentication,
- authorization,
- notes,
- reminders,
- sync,
- deletion,
- AI boundaries,
- billing,
- privacy,
- migrations,
- cross-platform fallbacks.

Confirmed bugs should receive regression tests where practical.

---

## 19. Quality Gates

Implementation must respect the Phase 8 quality gates.

No production release may proceed with:

- unresolved P0 defect,
- known cross-user data access,
- known data-loss path,
- authentication bypass,
- unresolved critical security flaw,
- invalid entitlement trust,
- unverified destructive migration.

Exceptions require explicit documented approval.

---

## 20. Definition of Ready

A task should not begin until:

- requirement is clear,
- owning module is known,
- relevant specs are identified,
- dependencies are known,
- acceptance criteria exist,
- no unresolved critical architectural conflict blocks it.

Agents must report ambiguity rather than guessing.

---

## 21. Definition of Done

A task is not done merely because code exists.

Done requires, as applicable:

- implementation complete,
- acceptance criteria satisfied,
- tests passing,
- failure paths handled,
- security/privacy reviewed,
- platform behavior verified,
- documentation updated,
- quality gates satisfied,
- limitations reported.

---

## 22. Kimi / AI-Agent Governance

One primary orchestrator coordinates implementation.

The orchestrator owns:

- task decomposition,
- agent assignment,
- dependency sequencing,
- shared-contract ownership,
- conflict resolution,
- integration,
- quality-gate enforcement.

Sub-agents should receive narrow, non-overlapping ownership.

---

## 23. Swarm Parallelism Rule

Parallel work is permitted only when boundaries are clear.

Before parallel producer/consumer work:

- stabilize the shared contract,
- identify writable ownership,
- identify read-only dependencies,
- identify integration tests.

Reduce parallelism when shared files, migrations, or architecture decisions are unstable.

---

## 24. No Silent Redefinition

An agent may propose changes, but may not silently:

- change the technology stack,
- redefine product behavior,
- replace architecture,
- weaken security/privacy,
- change monetization policy,
- alter shared API contracts,
- remove tests to make CI green,
- invent unresolved requirements.

Proposals must be surfaced for approval.

---

## 25. Change Management

Important decisions should be recorded through ADRs when they affect:

- local database technology,
- auth/session strategy,
- sync protocol/conflict model,
- background job mechanism,
- major provider selection,
- encryption architecture,
- major platform integration,
- other cross-cutting architecture.

Implementation should not permanently encode unresolved ADRs by accident.

---

## 26. Database & Migration Rule

All schema changes must use version-controlled migrations.

Destructive changes require:

- explicit review,
- data-safety plan,
- tested migration path,
- backup/recovery thinking.

Manual production schema edits are not the normal workflow.

---

## 27. Dependency Rule

New major dependencies require justification based on:

- maintenance,
- security,
- licensing,
- platform compatibility,
- runtime/package cost,
- lock-in,
- replacement difficulty,
- testability.

Avoid redundant dependencies.

---

## 28. Observability Rule

Production systems should expose enough observability to diagnose failures without violating privacy.

Use:

- structured logs,
- stable error codes,
- health checks,
- request/correlation IDs where useful,
- metrics for important operations.

Sensitive user content must not be logged by default.

---

## 29. Production Readiness Rule

Production release requires:

- tested migrations,
- backups,
- verified restore procedure,
- monitoring,
- rollback/recovery plan,
- platform smoke tests,
- billing verification,
- sync validation,
- documented known limitations.

A backup that has never been tested for restore is not considered verified.

---

## 30. Precedence Order

Unless a later approved document explicitly supersedes another, use:

```text
Project Constitution
→ Approved ADRs
→ Frozen Phase Specifications
→ Accepted API / Schema Contracts
→ Implementation
→ Tests / Operational Evidence
```

Tests validate intended behavior; they do not silently redefine the specification.

---

## 31. Unresolved Decisions

The constitution intentionally does not finalize unresolved ADR-level choices such as:

- exact local database technology,
- exact auth/session token implementation,
- exact sync protocol,
- final conflict-resolution algorithm,
- final background worker mechanism,
- final AI provider/model,
- exact provider-specific platform integrations.

These must be resolved during consolidated review or implementation ADR work before affected code is finalized.

---

## 32. Constitutional Amendment

Changing this constitution requires:

1. explicit project-owner approval,
2. documented rationale,
3. impact review across affected phases,
4. updates to conflicting specifications,
5. version increment.

Silent constitutional drift is prohibited.

---

## 33. Final Principle

> Build a system that can evolve without becoming fragile.

Every architectural, product, security, AI, monetization, testing, and implementation decision should preserve that principle.
