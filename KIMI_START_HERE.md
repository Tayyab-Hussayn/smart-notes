# KIMI START HERE

**Project:** Smart Sticky Wallpaper  
**Status:** Implementation Start Package  
**Purpose:** Tell the Kimi orchestrator how to begin implementation from the prepared specifications.

## 1. First Action

Read the documentation before writing production code.

Read in this order:

1. `docs/00-project-governance/PROJECT_CONSTITUTION.md`
2. `docs/phase-01-product-discovery/`
3. `docs/phase-02-requirements/`
4. `docs/phase-03-architecture/`
5. `docs/phase-04-ux-ui/`
6. `docs/phase-05-ai-intelligence/`
7. `docs/phase-06-monetization/`
8. `docs/phase-07-security-privacy/`
9. `docs/phase-08-testing/`
10. `docs/phase-09-development/`
11. `docs/phase-10-release/`

## 2. Source of Truth

- These docs are the source of truth.
- Do not silently redefine product behavior or architecture.
- If two documents conflict, follow the Project Constitution first.
- If an implementation-blocking decision is still unresolved, create an ADR proposal before coding that part.
- Do not invent a new stack unless explicitly approved.

## 3. Implementation Mode

Use spec-driven development and orchestrator-controlled agent parallelism.

Before coding, produce only a short implementation plan containing:
- repo/module structure,
- implementation sequence,
- unresolved blocking ADRs,
- first batch of agent tasks,
- test/quality-gate plan.

Do **not** spend time rewriting the specifications.

## 4. Recommended Implementation Sequence

```text
Foundation / Repository
→ Local Database + Identity Foundation
→ Notes Domain + CRUD
→ Canvas
→ Reminders
→ Sync
→ Intelligence / AI
→ Monetization
→ Platform Persistent Surfaces
→ Security / Reliability Hardening
→ Release Preparation
```

## 5. First Coding Milestone

Start with a small vertical slice:

```text
App boots
→ local database initialized
→ create note
→ edit note
→ delete/archive note
→ note persists across restart
→ basic canvas renders note
→ tests pass
```

This milestone must not depend on AI, billing, ads, or cloud availability.

## 6. Agent Swarm Rules

Follow:
- `docs/phase-09-development/AGENT_SWARM_PROTOCOL.md`
- `docs/phase-09-development/DEVELOPMENT_PROTOCOL.md`
- `docs/phase-09-development/CODE_CHANGE_PROTOCOL.md`
- `docs/phase-09-development/DEFINITION_OF_DONE.md`

The orchestrator controls:
- task decomposition,
- shared contracts,
- migrations,
- integration order,
- quality gates.

## 7. Testing

Follow Phase 08 from the first commit.

At minimum for the first milestone:
- unit tests,
- local persistence tests,
- note lifecycle tests,
- migration/init test,
- restart persistence test,
- no cross-account leakage once identity/account switching exists.

## 8. Important Constraints

- Kotlin Multiplatform shared logic.
- Compose Multiplatform where practical.
- Native platform APIs for OS-specific capabilities.
- FastAPI backend.
- PostgreSQL server database.
- Docker/VPS deployment baseline.
- Local-first note CRUD.
- Provider-neutral AI.
- Server-authoritative entitlements.
- No premature microservices.
- No production secrets in coding agents or repository.
- No blanket last-write-wins sync without approved ADR.

## 9. Immediate Orchestrator Output

Before spawning many agents, return:

1. proposed repository tree,
2. unresolved implementation-blocking ADRs,
3. first 5–10 implementation tasks,
4. dependency order,
5. which tasks can run in parallel,
6. first milestone acceptance criteria.

Then begin implementation.

## 10. Do Not Wait for Another Planning Phase

The specification package is sufficient to begin coding.

Only stop for:
- genuine specification contradiction,
- implementation-blocking ADR,
- security/data-loss risk,
- impossible platform capability.

Otherwise proceed.
