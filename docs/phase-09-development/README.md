# Phase 09 — Development Protocol

**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending

Phase 9 defines how Smart Sticky Wallpaper is implemented by Kimi Agent Swarm, another AI coding system, or human developers without allowing implementation speed to undermine architecture, security, data integrity, or specification fidelity.

## Artifacts

- `DEVELOPMENT_PROTOCOL.md` — overall engineering workflow and implementation rules.
- `AGENT_SWARM_PROTOCOL.md` — orchestrator, task packets, ownership, parallelism, integration, and agent evidence.
- `CODE_CHANGE_PROTOCOL.md` — API, schema, migration, dependency, security/privacy, platform, and recovery change process.
- `DEFINITION_OF_DONE.md` — minimum completion criteria from task through production.

## Core Invariants

1. Specs and approved ADRs drive implementation.
2. No agent silently redefines product/architecture.
3. Shared contracts are centrally controlled.
4. Parallel work requires explicit ownership.
5. Account isolation is checked in every user-state domain.
6. Tests ship with behavior changes.
7. Production secrets are not exposed to ordinary coding agents.
8. Migrations are versioned and rehearsed.
9. Provider SDKs remain behind adapters.
10. Phase 8 gates determine merge/release readiness.

## Swarm Model

```text
Frozen Specs / ADRs
→ Orchestrator Preflight
→ Contract Stabilization
→ Dependency DAG
→ Narrow Task Packets
→ Parallel Implementation
→ Agent Handoff Evidence
→ Central Integration
→ Phase 8 Quality Gates
→ Phase 10 Release Workflow
```

## Freeze Policy

Phase 9 remains Draft until project-wide consolidated review.

Before final freeze:
- reconcile against Phase 2–8 V2,
- resolve implementation-blocking ADRs,
- finalize task packet/handoff format,
- align CI and branch rules with actual repository setup,
- align G3/G4 with Phase 10,
- then include Phase 9 in consolidated freeze.
