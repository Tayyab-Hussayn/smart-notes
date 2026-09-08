# Quality Gates

**Project:** Smart Sticky Wallpaper  
**Phase:** 08 — Testing  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define objective gates for local work, pull requests, main integration, release candidates, and production release.  
**Governed By:** `PROJECT_CONSTITUTION.md`

---

## 1. Gate Levels

```text
G0 — Local Development
G1 — Pull Request / Change
G2 — Main Branch Integration
G3 — Release Candidate
G4 — Production Release
```

A later gate includes all earlier applicable gates.

---

## 2. G0 — Local Development

Before submission:

- code builds,
- formatter/linter passes,
- relevant unit tests pass,
- new behavior has tests,
- no secret is knowingly committed,
- spec/ADR impact checked.

Failure blocks submission.

---

## 3. G1 — Pull Request / Change

Required:

- build/compile,
- unit/component tests,
- changed-module integration tests,
- static analysis,
- secret scan,
- relevant contract tests,
- migration validation where applicable,
- regression test for bug fixes where practical,
- docs/ADR update when contract changes,
- no unresolved P0 introduced.

---

## 4. Security Blockers

Block merge/release for known:

- auth bypass,
- cross-user access,
- critical secret exposure,
- exploitable SQL/command injection,
- unverified premium grant path,
- privileged webhook mutation without verification,
- unrestricted AI privileged-action path,
- critical dependency/supply-chain issue without accepted mitigation,
- cross-account cache/data leakage.

---

## 5. Data Integrity Blockers

Block for:

- reproducible note loss,
- sync corruption,
- deleted-note resurrection violating policy,
- destructive migration without recovery plan,
- duplicate entitlement/reward from replay,
- export of another user's data,
- account-switch contamination,
- backup restore reactivating deleted data.

---

## 6. G2 — Main Integration

Required:

- all G1 checks,
- broader integration suite,
- real DB tests,
- sync convergence suite,
- account-isolation suite,
- provider adapter contract tests,
- selected E2E,
- supported-platform smoke builds,
- migration forward-path test,
- no unowned flaky critical test.

Main should remain deployable.

---

## 7. Flaky-Test Gate

A flaky critical test must be fixed or temporarily quarantined with:

```text
test
reason
owner
issue
expiry
risk
```

Unlimited reruns are forbidden as a fix.

---

## 8. Coverage Expectations

Coverage percentage is not the primary gate.

Mandatory high-value coverage:

- auth/session,
- authorization,
- account switching,
- sync,
- deletion,
- reminders,
- entitlement/billing,
- AI validation,
- privacy boundaries,
- provider failure fallback,
- migrations.

Coverage targets may supplement this.

---

## 9. G3 — Release Candidate

Required:

- all critical automated suites pass,
- no unresolved P0,
- no unaccepted Critical/High security defect,
- critical E2E journeys pass,
- supported-platform install/smoke pass,
- accessibility baseline pass,
- performance sanity thresholds pass,
- migrations rehearsed,
- billing sandbox/provider validation,
- ad eligibility/prohibited placement validation,
- AI evaluation complete for changed model/provider,
- privacy tests pass,
- rollback/recovery plan reviewed,
- release notes/known limitations ready.

---

## 10. Platform Gate

For each supported release target verify:

- package/install/update,
- core note flow,
- local persistence,
- sync,
- reminder/notification,
- capability registry,
- native surface/fallback,
- account/session behavior,
- accessibility baseline.

Unsupported capabilities must not be falsely advertised.

---

## 11. AI Gate

Release AI changes only when:

- schema validation passes,
- prompt-injection boundary passes,
- opt-out passes,
- quota passes,
- timeout/provider failure fallback passes,
- no duplicate action on retry,
- relevant evaluation set passes,
- model/provider/version recorded,
- kill switch verified.

---

## 12. Billing Gate

Verify:

- valid/invalid/pending purchase,
- replay protection,
- duplicate/out-of-order webhook,
- renewal/cancellation/grace/expiry,
- refund/revocation,
- restore,
- offline cache,
- account isolation,
- reconciliation.

---

## 13. Advertising Gate

Verify:

- premium no-ad,
- eligibility,
- prohibited contexts,
- consent,
- frequency caps,
- provider failure/no-fill,
- rewarded idempotency,
- account isolation,
- ad-data privacy,
- kill switch.

---

## 14. Privacy Gate

Block release if:

- raw note content leaks to analytics unexpectedly,
- tokens/secrets leak to logs/crash reports,
- AI opt-out still sends provider requests,
- ad provider receives private note semantics,
- export is not user-scoped,
- deletion lifecycle fails,
- account switching leaks data,
- backup restore violates deletion semantics.

---

## 15. Performance Gate

Before production freeze, define measured thresholds for:

- startup,
- note/canvas interaction,
- memory,
- local DB operations,
- sync duration/throughput,
- API latency,
- background impact,
- AI timeout/fallback.

Thresholds require environment metadata.

---

## 16. Reliability Gate

Verify:

- offline CRUD,
- backend outage,
- DB outage,
- network interruption,
- app/process restart,
- AI outage,
- billing outage,
- ad outage,
- worker restart,
- stale config,
- interrupted sync recovery.

---

## 17. G4 — Production Release

Required:

- G3 approved,
- production config reviewed,
- production secrets scoped,
- signing/deploy credentials protected,
- migration plan approved,
- backup completed,
- restore procedure verified,
- monitoring/alerts active,
- health checks active,
- rollback/recovery available,
- incident owner/on-call route identified,
- known limitations documented,
- release owner identified.

---

## 18. Defect Severity

### P0 / Critical
Data loss, cross-user access, auth bypass, severe security/entitlement failure, release unusable.

### P1 / High
Core workflow broken, sync/reminders unreliable, major supported-platform failure.

### P2 / Medium
Recoverable noncritical feature/provider issue.

### P3 / Low
Polish/low-impact edge case.

---

## 19. Release Block Rules

Production blocked by:

- unresolved P0,
- unaccepted Critical security issue,
- known data-loss path,
- broken account isolation,
- broken billing verification,
- failed supported migration,
- failed backup/restore safety,
- known privacy leak of authored content.

P1 exceptions require explicit documented approval.

---

## 20. Quality Exception Process

Any waiver records:

```text
gate
issue
severity
reason
user impact
mitigation
owner
expiry/review date
approver
```

No silent bypass.

---

## 21. Agent Rules

Agents must not:

- mark failed work complete,
- delete tests to make CI green,
- weaken assertions without approval,
- bypass security/privacy gates,
- hide flaky tests,
- claim provider/platform validation without evidence.

---

## 22. Evidence

Gate completion should reference:

- CI run,
- test report,
- benchmark,
- device/platform validation,
- migration result,
- security review,
- restore drill,
- provider sandbox verification.

---

## 23. Consolidated Review Criteria

Before final freeze:

- align gates with final CI/CD,
- finalize performance thresholds after benchmarks,
- verify Phase 7 blockers,
- verify Phase 10 readiness consumes G3/G4,
- ensure no V2 requirement is outside a gate or test path.

Phase 8 remains **Draft** until final project freeze.
