# Agent Swarm Protocol

**Project:** Smart Sticky Wallpaper  
**Phase:** 09 — Development Protocol  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define how Kimi Agent Swarm or another multi-agent implementation system safely decomposes, parallelizes, reviews, integrates, and validates work.  
**Governed By:** `PROJECT_CONSTITUTION.md` and `DEVELOPMENT_PROTOCOL.md`

---

## 1. Core Rule

> Parallelism is allowed only when ownership and contracts are clear.

Shared architecture, APIs, schemas, migrations, capability contracts, and security boundaries remain centrally controlled.

---

## 2. Orchestrator Responsibilities

The orchestrator owns:

- reading the frozen specification set,
- loading approved ADRs,
- dependency DAG,
- task decomposition,
- assignment,
- writable ownership,
- shared-contract sequencing,
- conflict resolution,
- integration order,
- quality gates,
- unresolved-issue escalation,
- final evidence collection.

Sub-agents do not independently redefine architecture.

---

## 3. Orchestrator Preflight

Before spawning implementation work:

- confirm docs are frozen/current,
- identify unresolved ADRs,
- map dependencies,
- identify high-conflict files/contracts,
- select integration sequence,
- define test requirements,
- define task evidence format.

Do not maximize agent count before this step.

---

## 4. Task Packet

Every sub-agent should receive a compact task packet containing:

```text
Task ID
Goal
Requirement IDs
Relevant Specs / ADRs
Owning Module
Writable Files / Areas
Read-Only Dependencies
Forbidden Areas
Acceptance Criteria
Required Tests
Required Quality Gate
Known Dependencies
Expected Handoff Format
```

---

## 5. Task Design

Good tasks are:

- narrow,
- independently testable,
- low-overlap,
- tied to one owning domain,
- contract-aware,
- integration-ready.

Avoid:

- “build the backend,”
- “fix anything necessary,”
- “improve architecture,”
- “make tests pass however needed.”

---

## 6. Suggested Domain Ownership

```text
Identity / Sessions
Notes
Canvas
Reminders
Sync
Intelligence / AI
Monetization
Notifications
Settings
Platform Capabilities
Test Infrastructure
Release / Operations
```

Assignments follow dependency order, not arbitrary parallelism.

---

## 7. Writable Ownership

Every task explicitly defines:

- writable modules/files,
- shared files requiring orchestrator coordination,
- read-only dependencies,
- prohibited areas.

High-conflict files should have a single active owner.

Examples:

- root build config,
- dependency catalogs,
- central API schemas,
- DB migration registry,
- navigation root,
- capability registry,
- shared DI/config,
- entitlement key registry.

---

## 8. Contract-First Parallelism

Before producer/consumer work runs in parallel, stabilize the contract.

Examples:

- API schema,
- repository interface,
- sync operation model,
- capability interface,
- AI task contract,
- entitlement snapshot,
- error contract.

Only then parallelize implementations.

---

## 9. Dependency DAG

Maintain explicit ordering such as:

```text
ADR / Contract
→ Domain
→ Persistence / Backend
→ Client Integration
→ Provider Adapter
→ Integration Test
→ E2E
```

Agents should not code against speculative downstream contracts.

---

## 10. Change Isolation

An agent assigned one domain must not opportunistically rewrite adjacent domains.

Cross-domain changes require:

- explicit scope expansion,
- orchestrator approval,
- updated ownership.

---

## 11. Handoff Contract

Every agent returns:

- task ID,
- summary,
- changed files,
- requirement/spec references,
- assumptions,
- tests added,
- tests executed/results,
- quality gate status,
- API/schema/migration impact,
- security/privacy impact,
- known limitations,
- unresolved issues,
- integration notes.

No test evidence → no completed handoff where tests apply.

---

## 12. Handoff Validation

Orchestrator verifies:

- task stayed in scope,
- required files changed,
- tests exist,
- tests actually ran,
- shared contracts match,
- no unapproved architecture change,
- no security/privacy weakening,
- docs/ADR impact handled.

---

## 13. Conflict Protocol

When agents conflict:

1. stop overlapping edits,
2. identify the contested contract,
3. compare with frozen docs/ADR,
4. determine whether conflict is implementation or specification-level,
5. orchestrator chooses implementation direction or escalates spec decision,
6. update ADR/spec if approved,
7. reassign ownership,
8. resume.

Do not merge competing architectures.

---

## 14. Architecture Change Proposal

Proposal must include:

- problem,
- current architecture,
- proposed change,
- alternatives considered,
- impacted modules,
- migration/API impact,
- test impact,
- security/privacy impact,
- rollout/rollback impact,
- documentation impact.

Sub-agent may propose, not silently apply.

---

## 15. Shared Schema / API Sequencing

Only one coordinated stream should mutate a shared API/schema at a time.

Dependent agents should consume a versioned contract or stub.

Do not let parallel agents invent incompatible DTOs.

---

## 16. Database Migration Sequencing

Migration ownership is centralized.

Before multiple agents add migrations:

- reserve/order migration IDs,
- check shared tables,
- avoid conflicting backfills,
- define integration order,
- run combined migration tests.

---

## 17. Security Reviewer Role

Security-focused agent may review:

- auth/session,
- IDOR/account isolation,
- sync replay/deletion,
- AI boundaries,
- billing/webhooks,
- exports/deletion,
- admin access,
- secrets/logging,
- CI/CD/supply chain.

Security reviewer does not replace feature ownership.

---

## 18. Test Reviewer Role

Test-focused agent may:

- audit requirement coverage,
- audit threat-to-test mapping,
- create fixtures/harnesses,
- identify flaky tests,
- review E2E gaps.

Feature agents still own feature tests.

---

## 19. Documentation Reviewer Role

A docs/reconciliation agent may:

- verify spec references,
- detect drift,
- identify duplicated/obsolete rules,
- prepare approved updates.

It must not change product behavior independently.

---

## 20. Parallelism Limits

Reduce parallelism when:

- contracts are unstable,
- migrations overlap,
- shared-file conflicts increase,
- architecture is unresolved,
- CI integration failures rise,
- multiple agents touch same domain invariant,
- rollback/review becomes unclear.

More agents do not automatically mean more throughput.

---

## 21. Context Discipline

Give each agent:

- task-specific specs,
- relevant architecture/security rules,
- approved ADRs,
- required contracts,
- acceptance criteria.

Do not dump the entire conversation history into every agent.

---

## 22. No Spec Invention

When behavior is ambiguous:

```text
Detect Ambiguity
→ Report
→ Propose Options
→ Orchestrator / Owner Decision
→ Update Contract if Needed
→ Continue
```

Do not guess.

---

## 23. No Silent Rewrites

Agents may not silently:

- switch stack,
- replace architecture,
- redefine domain boundaries,
- weaken security/privacy,
- change monetization policy,
- change entitlement meaning,
- remove tests,
- lower quality gates,
- change API compatibility policy,
- rename major modules/contracts,
- introduce new external data collection.

---

## 24. Secrets and Agents

Coding agents should not receive production secrets.

Where provider integration is required, use:

- mock credentials,
- sandbox credentials,
- scoped temporary credentials.

Production secret injection belongs to deployment/operations.

---

## 25. External Provider Work

Provider agent must preserve owned interfaces.

Before integration:

- confirm adapter contract,
- use sandbox/mock,
- document provider assumptions,
- avoid provider DTO leakage,
- add failure-path tests,
- identify kill switch/fallback.

---

## 26. Integration Sequence

```text
Validate Handoff
→ Run Agent-Owned Tests
→ Merge Contract-Compatible Work
→ Run Shared Integration Tests
→ Run Account / Security Tests
→ Resolve Conflicts
→ Run Applicable Quality Gate
→ Record Evidence
```

---

## 27. Failed Agent Output

Reject or return for correction when:

- scope exceeded,
- tests missing,
- tests failing,
- architecture changed silently,
- API/schema incompatible,
- migration unsafe,
- security boundary weakened,
- platform fallback omitted,
- unsupported assumption hidden.

---

## 28. Recovery from Bad Merge

If an integrated change destabilizes main:

- stop additional dependent merges,
- identify offending change,
- revert or fix forward based on safety,
- rerun G2,
- update dependent tasks if contract changed.

---

## 29. Emergency Work

Emergency changes may reduce parallelism and process steps, but must preserve:

- security/data safety,
- ownership,
- review,
- post-fix tests,
- documentation reconciliation.

---

## 30. Completion Evidence

The orchestrator should maintain per-task completion records sufficient to answer:

- what changed,
- why,
- which requirement,
- which tests,
- which quality gate,
- which risks remain.

---

## 31. Final Swarm Rule

> Sub-agents optimize their assigned scope. The orchestrator protects whole-system coherence.

Phase 9 remains **Draft** until final project freeze.
