# Code Change Protocol

**Project:** Smart Sticky Wallpaper  
**Phase:** 09 — Development Protocol  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define how code, API, schema, migration, dependency, platform, security, privacy, test, and documentation changes are safely proposed, reviewed, integrated, and recovered.  
**Governed By:** `PROJECT_CONSTITUTION.md` and `DEVELOPMENT_PROTOCOL.md`

---

## 1. Change Principle

Each change should have one clear primary purpose and an explicit owner.

---

## 2. Pre-Change Checklist

Before editing identify:

- requirement/spec,
- owning module,
- acceptance criteria,
- required tests,
- API/schema impact,
- migration impact,
- security/privacy impact,
- account-isolation impact,
- platform impact,
- provider impact,
- documentation/ADR impact.

---

## 3. Change Classes

- feature,
- bug fix,
- refactor,
- API/contract,
- DB migration,
- dependency/provider,
- platform capability,
- security/privacy,
- test infrastructure,
- configuration/feature flag,
- documentation,
- emergency fix.

---

## 4. Branch / Work Unit

Prefer short-lived, focused work units.

Avoid long-lived architecture divergence.

Example:

```text
feature/reminder-recurrence
fix/sync-resurrection
security/billing-event-ordering
```

---

## 5. Commit Discipline

Commits should:

- represent coherent work,
- use meaningful messages,
- avoid unrelated formatting noise,
- include tests with behavior changes where practical,
- avoid mixing generated churn with substantive logic.

---

## 6. API Change Protocol

1. define contract,
2. identify version/compatibility effect,
3. define auth/ownership,
4. update schemas,
5. implement backend,
6. implement client,
7. add contract/integration tests,
8. document deprecation/breaking impact.

Breaking changes require explicit approval.

---

## 7. API Compatibility

Before merge answer:

- will old supported clients still work?
- will new client tolerate old supported server behavior?
- are additive fields tolerated?
- is deprecation documented?
- is feature rollout version-aware where required?

---

## 8. Database Migration Protocol

Every schema change requires a version-controlled migration.

Migrations must be:

- ordered,
- deterministic,
- testable,
- data-preserving where required,
- compatible with deployment sequence,
- reviewed for rollback/recovery.

---

## 9. Destructive Migration

Requires:

- explicit approval,
- backup/recovery plan,
- transformation plan,
- validation query/procedure,
- compatibility window where useful,
- staging rehearsal,
- release/rollback notes.

Prefer expand/backfill/contract.

---

## 10. Backfills

Large backfills should be:

- observable,
- restartable/resumable where practical,
- idempotent where possible,
- tested on representative data,
- bounded to avoid long blocking transactions.

---

## 11. Sync Contract Changes

Changes to sync require review of:

- operation schema,
- revisions/cursors,
- idempotency,
- conflict rules,
- tombstones,
- older client behavior,
- account isolation,
- migration/data conversion,
- convergence tests.

---

## 12. Entitlement Changes

Changing entitlement keys or semantics requires:

- centralized registry update,
- plan mapping review,
- backend/client update,
- downgrade behavior review,
- billing/reward implications,
- tests,
- documentation.

Never reuse an entitlement key for unrelated meaning.

---

## 13. AI Contract Changes

Changes to AI task/schema require:

- task contract versioning,
- schema validation update,
- provider adapter update,
- evaluation suite,
- prompt/version tracking where applicable,
- fallback test,
- privacy review if data scope changes.

---

## 14. Platform Capability Changes

When capability behavior changes:

- update registry,
- update native adapter,
- preserve fallback,
- verify unsupported state,
- update capability matrix,
- add platform test evidence.

---

## 15. Dependency Changes

For major dependency/provider changes assess:

- need,
- maintenance,
- security,
- license,
- compatibility,
- platform support,
- size/runtime impact,
- supply-chain/provenance,
- lock-in,
- replacement path.

---

## 16. Provider SDKs

AI, billing, ads, and notification SDKs remain behind owned adapters.

Provider-specific DTOs must not leak into domain contracts.

Provider replacement requires failure/kill-switch review.

---

## 17. Security-Sensitive Changes

Explicit review required for:

- auth/session,
- authorization,
- account switching,
- sync,
- billing/webhooks,
- rewards,
- exports/deletion,
- admin,
- secrets,
- AI action/tool boundaries,
- CI/CD credentials,
- external URL/file handling.

---

## 18. Privacy-Sensitive Changes

New data collection/sharing must document:

- purpose,
- data category,
- recipient,
- storage,
- retention,
- consent/opt-out,
- deletion/export impact,
- privacy-policy impact.

---

## 19. Configuration / Feature Flags

Flag changes require:

- owner,
- default state,
- allowed environments,
- rollout plan,
- rollback/kill-switch behavior,
- test coverage.

Flags must not weaken auth/authorization/data-integrity controls.

---

## 20. Refactors

A pure refactor requires:

- clear reason,
- bounded scope,
- passing tests before/after,
- no intended behavior change,
- no hidden contract change.

If behavior changes, treat it as a feature/change.

---

## 21. Test Changes

Do not remove/weaken tests merely to make CI green.

Changed assertions require rationale.

Flaky tests follow Phase 8 quarantine policy.

---

## 22. Security Test Changes

Removing/reducing P0, Critical, High, authz, account-isolation, billing, deletion, or sync tests requires explicit security/quality approval.

---

## 23. Documentation Changes

Update docs when accepted behavior changes:

- API,
- schema,
- architecture,
- capability,
- AI,
- entitlement,
- privacy/security,
- operations.

Do not update frozen specs casually; follow approved change/ADR process.

---

## 24. ADR Triggers

ADR required for material decisions such as:

- local DB technology,
- auth/session strategy,
- sync conflict model,
- worker/queue,
- API versioning,
- major provider selection,
- encryption approach,
- capability semantics,
- cross-cutting caching/telemetry architecture.

---

## 25. Review Checklist

Review:

- spec compliance,
- scope,
- code readability,
- architecture boundaries,
- duplicate logic,
- tests,
- negative paths,
- account isolation,
- security/privacy,
- API compatibility,
- migration impact,
- platform impact,
- observability,
- documentation.

---

## 26. Merge Rule

Applicable Phase 8 quality gates must pass before merge unless a documented exception is explicitly approved.

No agent may self-approve a blocking exception.

---

## 27. Revert / Recovery Strategy

Risky changes must have a practical recovery path.

Examples:

- code revert,
- feature disable,
- migration repair,
- provider kill switch,
- backward-compatible fallback.

Database rollback is not always safe; prefer forward-compatible recovery when appropriate.

---

## 28. Emergency Fixes

Emergency fixes may shorten normal process but must:

- protect security/data,
- have clear owner,
- receive review,
- add follow-up regression tests,
- reconcile docs/ADR,
- rerun applicable gates.

---

## 29. Post-Merge Verification

After integration:

- run G2,
- verify migration compatibility,
- verify contract consumers,
- review unexpected telemetry/errors,
- reopen change if integration behavior differs from task-level tests.

---

## 30. Ownership

Each change owner remains responsible for:

- implementation,
- tests,
- review responses,
- migration/API implications,
- documentation,
- follow-up defects.

Phase 9 remains **Draft** until final project freeze.
