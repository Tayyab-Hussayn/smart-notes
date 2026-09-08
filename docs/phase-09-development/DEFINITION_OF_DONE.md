# Definition of Done

**Project:** Smart Sticky Wallpaper  
**Phase:** 09 — Development Protocol  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define the minimum evidence and quality conditions required before a task, feature, module, release candidate, or production release may be considered complete.  
**Governed By:** `PROJECT_CONSTITUTION.md`, Phase 08 V2, and `DEVELOPMENT_PROTOCOL.md`

---

## 1. Core Rule

> Implemented is not the same as done.

---

## 2. Task-Level Done

A task is done when:

- requested scope is implemented,
- requirement/acceptance criteria are met,
- relevant tests pass,
- failure paths are handled,
- architecture boundaries are respected,
- security/privacy impacts are handled,
- no known blocking regression exists,
- handoff evidence is complete.

---

## 3. Feature-Level Done

A feature additionally requires:

- unit/component tests,
- integration tests where applicable,
- critical E2E where applicable,
- negative/failure tests,
- security/privacy tests where applicable,
- platform verification where applicable,
- accessibility behavior,
- documentation updates,
- operational/error behavior where relevant.

---

## 4. Spec Compliance

Done means:

- relevant frozen specs followed,
- approved ADRs followed,
- no silent spec change,
- deviations explicitly approved,
- changed contracts documented.

---

## 5. Code Quality

Code should be:

- readable,
- maintainable,
- modular,
- appropriately simple,
- testable,
- free of unnecessary duplication.

Known debt must be explicit, owned, and non-blocking.

---

## 6. Negative Paths

Handle as relevant:

- invalid input,
- unauthorized access,
- cross-account access,
- network failure,
- provider outage,
- timeout,
- stale state,
- offline state,
- duplicate/replay,
- restart/process death,
- unsupported capability.

---

## 7. Security Done

Not done if:

- authorization missing,
- ownership unclear,
- cross-account leakage exists,
- secrets exposed,
- untrusted AI executes directly,
- required webhook verification absent,
- replay/idempotency protection absent,
- required rate limit absent,
- admin boundary weak,
- Critical/High security blocker remains.

---

## 8. Privacy Done

Not done if:

- note content leaks to general telemetry,
- external sharing is undocumented,
- AI opt-out is bypassed,
- ad provider receives private note semantics,
- export scope is unsafe,
- deletion lifecycle is broken,
- notification privacy policy is ignored.

---

## 9. Data Integrity Done

Not done if:

- migration can silently lose supported data,
- local mutation/outbox invariant is broken,
- sync conflict behavior is undefined,
- deletion can resurrect contrary to policy,
- retries create duplicate durable effects,
- process restart loses pending work,
- account switching mixes state.

---

## 10. API Done

Requires:

- schema,
- auth/authz,
- validation,
- stable error behavior,
- idempotency where required,
- compatibility handling,
- contract/integration tests,
- documentation.

---

## 11. Database Done

Requires:

- migration,
- migration test,
- constraints/indexes where appropriate,
- data preservation validation,
- deployment compatibility,
- recovery strategy,
- documentation.

---

## 12. Sync Done

Requires:

- revisions,
- durable outbox,
- idempotency,
- retries,
- tombstones,
- conflict tests,
- process-restart recovery,
- account isolation,
- convergence evidence.

---

## 13. AI Feature Done

Requires:

- provider-neutral task contract,
- structured validation,
- deterministic fallback,
- privacy eligibility,
- prompt-injection boundary,
- confidence/confirmation policy,
- quota/cost controls,
- deterministic tests,
- relevant model/provider evaluation,
- kill-switch/fallback behavior.

---

## 14. Billing / Entitlement Done

Requires:

- server verification,
- normalized entitlement,
- replay protection,
- duplicate/out-of-order webhook handling,
- reconciliation,
- bounded offline cache,
- non-destructive downgrade,
- account isolation,
- lifecycle tests.

---

## 15. Advertising / Reward Done

Requires:

- eligibility rule,
- premium suppression,
- prohibited placement enforcement,
- privacy boundary,
- frequency/cooldown,
- provider failure fallback,
- duplicate-safe reward grant,
- kill switch,
- accessibility behavior.

---

## 16. Platform Feature Done

Requires:

- capability implemented,
- fallback/unsupported behavior defined,
- native restrictions respected,
- capability matrix updated,
- platform verification evidence.

---

## 17. UI/UX Done

Requires:

- required states,
- loading/error/empty/offline behavior,
- accessibility semantics,
- keyboard/focus where relevant,
- responsive behavior,
- design-system consistency,
- AI/ad/authored-content visual separation where relevant.

---

## 18. Accessibility Done

Relevant UI is not done until:

- labels/semantics work,
- focus order is usable,
- text scaling supported,
- non-color-only meaning exists,
- essential drag-only interaction has alternative,
- reduced-motion behavior is respected where applicable.

---

## 19. Documentation Done

Update as applicable:

- API/schema,
- ADR,
- capability matrix,
- architecture,
- AI contract,
- monetization,
- privacy/security,
- operations,
- release notes.

---

## 20. Observability Done

As appropriate:

- stable errors,
- structured logs,
- request/job IDs,
- health signals,
- metrics,
- provider failure visibility.

No private-content leakage.

---

## 21. Test Evidence Done

Evidence should include:

- test IDs/suites,
- results,
- environment,
- build/version,
- known skipped/quarantined tests,
- manual validation evidence where required.

---

## 22. Agent Handoff Done

Agent reports:

- task ID,
- changed files,
- implemented behavior,
- requirement/ADR references,
- tests added/run,
- gate status,
- limitations,
- unresolved issues,
- integration notes.

---

## 23. Module Done

A module milestone additionally requires:

- stable public contracts,
- integration tests,
- documented error/failure behavior,
- no unresolved P0/P1 in owned scope,
- predictable integration for dependents,
- no hidden architecture debt blocking next modules.

---

## 24. Release Candidate Done

Requires Phase 8 **G3** plus:

- critical E2E pass,
- security/privacy gate,
- platform smoke validation,
- migration rehearsal,
- AI/billing/ad provider validation as applicable,
- rollback/recovery review.

---

## 25. Production Done

Requires Phase 8 **G4** and Phase 10 production readiness:

- monitoring,
- backup,
- tested restore,
- migration plan,
- rollback/recovery,
- secrets/config review,
- known limitations,
- post-deploy validation.

---

## 26. Not-Done Examples

Not done when:

- build succeeds but tests fail,
- only happy path works,
- cross-account leakage exists,
- provider outage breaks core notes,
- premium is a local boolean,
- AI silently changes consequential state,
- webhook ordering/replay is unsafe,
- migration is untested,
- platform fallback is missing,
- backup restore is unverified,
- docs contradict implementation.

---

## 27. Final Checklist

- [ ] Specs/ADRs followed
- [ ] Scope complete
- [ ] Build passes
- [ ] Tests pass
- [ ] Negative paths handled
- [ ] Account isolation verified
- [ ] Security reviewed
- [ ] Privacy reviewed
- [ ] Data integrity preserved
- [ ] Platform behavior verified
- [ ] Accessibility verified
- [ ] Docs updated
- [ ] Applicable quality gate passed
- [ ] Evidence recorded
- [ ] Limitations reported

Phase 9 remains **Draft** until final project freeze.
