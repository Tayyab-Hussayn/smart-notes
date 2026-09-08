# Testing Strategy

**Project:** Smart Sticky Wallpaper  
**Phase:** 08 — Testing  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define the verification strategy across unit, integration, contract, end-to-end, security, privacy, performance, reliability, migration, and cross-platform behavior.  
**Governed By:** `PROJECT_CONSTITUTION.md`  
**Primary Inputs:** Phase 02–07 V2 specifications

---

## 1. Testing Principles

1. Compilation is not proof of correctness.
2. Every critical requirement needs a verification path.
3. P0/P1 behavior should be automated unless there is a documented reason not to.
4. Deterministic logic must be tested deterministically.
5. External providers must be mockable.
6. Core note CRUD must be testable offline.
7. Security/privacy/data-integrity failures are release blockers.
8. Cross-platform behavior is validated by capability, not assumed parity.
9. Confirmed bugs should receive regression tests where practical.
10. Flaky tests are defects.
11. Tests must preserve account isolation.
12. Test evidence must be traceable to requirements/threats.

---

## 2. Test Layers

### Unit
Fast tests for domain rules, parsers, recurrence, ranking, entitlement mapping, validation, conflict policy, and pure state transitions.

### Component / UI
View-model/state reducers, component behavior, accessibility semantics, editor/canvas interaction, error/offline states.

### Integration
Repositories, PostgreSQL behavior, local DB, sync engine, API boundary, background jobs, provider adapters, entitlement resolution.

### Contract
Versioned API schemas and owned provider-neutral interfaces.

### End-to-End
Critical user journeys across client, backend, persistence, sync, and controlled provider substitutes.

### Security / Abuse
Authorization, replay, injection, rate limiting, provider callbacks, secrets, account isolation.

### Performance / Reliability
Representative datasets, failure injection, restart/recovery, provider outages, migration load.

---

## 3. Traceability

Required chain for critical behavior:

```text
Requirement ID
→ Architecture / Module
→ Test Case ID
→ Automated or Manual Procedure
→ Result / Evidence
```

Critical/High threats should additionally map:

```text
Threat ID
→ Mitigation
→ Test Case ID
→ Detection / Monitoring
```

---

## 4. Priority Model

- **P0** — data loss, cross-user access, auth bypass, destructive migration, entitlement integrity, deletion safety, critical secret exposure.
- **P1** — core note/reminder/sync reliability, major platform capability failure.
- **P2** — AI, ads, customization, recoverable provider issues.
- **P3** — polish and low-impact edge cases.

---

## 5. Client Testing

Cover:

- note CRUD and lifecycle,
- local persistence,
- account switching,
- canvas placement/resize,
- reminders,
- intelligence rules,
- AI suggestion state,
- sync outbox state machine,
- entitlement snapshot behavior,
- settings/privacy controls,
- offline transitions,
- capability fallbacks,
- notification preview behavior,
- accessibility alternatives.

Shared KMP behavior should be tested in shared modules where practical.

---

## 6. Backend Testing

Cover:

- authentication/session lifecycle,
- authorization/ownership,
- request validation,
- note/canvas/reminder application services,
- sync revisions/idempotency/conflicts,
- entitlement resolution,
- billing verification/webhooks,
- AI eligibility/quota/gateway validation,
- export/deletion,
- background jobs,
- DB constraints/migrations,
- stable error contracts,
- rate limits.

Use real PostgreSQL for persistence semantics.

---

## 7. Sync Testing

Sync is P0/P1.

Required scenarios:

- first sync,
- repeated sync,
- offline create/edit/delete,
- atomic local mutation + outbox,
- reconnect,
- duplicate operation,
- stale revision,
- two-device concurrent edits,
- edit-vs-delete,
- tombstone propagation,
- deleted-note non-resurrection,
- partial batch failure,
- retry/backoff,
- process death mid-sync,
- account switch,
- long-offline reconnect,
- multi-device convergence.

Conflict outcomes must be deterministic.

---

## 8. Account Isolation Testing

Mandatory cases:

- account A → sign out → account B,
- no A notes rendered under B,
- no A outbox replayed under B,
- no A entitlement cache used under B,
- no A AI/reward cache used under B,
- server rejects cross-user IDs,
- export jobs remain account-scoped.

Any failure is P0.

---

## 9. Reminder & Time Testing

Use controllable clocks.

Test:

- exact date/time,
- relative dates,
- semantic dayparts,
- recurrence,
- snooze,
- one-time completion,
- recurring occurrence completion,
- cancellation,
- timezone change,
- DST where applicable,
- midnight/month/year boundary,
- clock rollback/forward,
- process/device restart,
- duplicate scheduling prevention.

---

## 10. Intelligence Testing

Test hard-rule precedence:

```text
Explicit User State
>
Hard Domain Rules
>
Deterministic Context
>
AI Signal
```

Cover:

- hidden/deleted/archive suppression,
- snooze,
- completion,
- pin,
- repetition penalty,
- candidate limit,
- refresh triggers,
- intelligence disabled,
- notification boundary,
- offline ranking,
- explanation reason codes.

---

## 11. AI Testing

Core CI uses deterministic fake providers.

Cover:

- valid schema,
- malformed schema,
- unsupported action,
- low confidence,
- timeout,
- provider outage,
- rate limit,
- quota exhausted,
- privacy opt-out,
- prompt injection,
- provider/model routing,
- schema/prompt version mismatch,
- deterministic fallback,
- no duplicate domain action after retry.

Live-provider tests are supplementary.

---

## 12. AI Evaluation

Maintain curated task-specific evaluation sets for:

- datetime extraction,
- ambiguous reminder interpretation,
- classification,
- routine suggestion,
- ranking,
- summarization,
- multilingual text,
- injection resistance.

Provider/model changes must rerun relevant evaluations before broad rollout.

---

## 13. Billing Testing

Required:

- valid purchase,
- invalid/unverifiable purchase,
- pending verification,
- replayed transaction,
- renewal,
- cancellation,
- grace,
- expiry,
- refund/revocation,
- restore,
- offline entitlement cache,
- stale snapshot,
- account switch,
- cross-device refresh,
- patched local premium state,
- partial DB failure/reconciliation.

---

## 14. Billing Webhook Testing

Cover:

- valid signature,
- invalid signature,
- duplicate event,
- replay/stale timestamp,
- out-of-order event,
- unknown event type,
- malformed payload,
- provider retry,
- reconciliation repairing missed event.

---

## 15. Advertising Testing

Cover:

- free eligibility,
- premium no-ad,
- prohibited placement,
- no-fill,
- SDK/provider failure,
- consent denied,
- unsupported platform,
- entitlement change in-session,
- frequency cap/cooldown,
- rewarded completion,
- duplicate reward callback,
- cross-account reward replay,
- provider kill switch,
- accessibility/focus behavior.

Ad failure never blocks core use.

---

## 16. Security Testing

Map Critical/High Phase 7 threats to tests.

Minimum:

- IDOR/resource ownership,
- malformed/expired/revoked credentials,
- credential stuffing/rate limits,
- SQL/injection payloads,
- oversized payloads,
- sync replay,
- cross-user sync,
- prompt injection,
- forged webhook,
- rewarded replay,
- admin privilege escalation,
- export tampering,
- account-switch leakage,
- secret/log redaction,
- deep-link abuse where implemented,
- SSRF/file-upload tests if those features are introduced.

---

## 17. Privacy Testing

Verify:

- raw note text absent from analytics,
- secrets absent from crash reports,
- AI opt-out sends no external AI request,
- AI payload is minimized,
- ad provider receives no private note semantics,
- notification privacy setting works,
- export contains only current user's supported data,
- deletion propagates,
- backup restore does not reactivate deleted state,
- provider kill switches stop external processing.

---

## 18. Cross-Platform Testing

For Android, iOS, Windows, macOS, and Linux validate:

- build/install,
- core note flow,
- local persistence,
- sync,
- reminder behavior,
- notification behavior,
- capability registry output,
- native surface/fallback,
- account/session state,
- accessibility basics.

Do not test nonexistent capability parity.

---

## 19. Accessibility Testing

Verify:

- screen-reader labels,
- logical focus order,
- keyboard navigation where applicable,
- text scaling,
- contrast/non-color-only meaning,
- drag alternatives,
- reduced motion,
- ad focus behavior,
- error announcements.

Automated checks supplement manual accessibility review.

---

## 20. Performance Testing

Benchmark representative cases:

- 10 notes,
- 100 notes,
- 1,000 notes or documented upper profile,
- dense canvas,
- large outbox,
- large sync delta,
- API latency,
- DB query latency,
- memory use,
- startup,
- reminder scheduling,
- AI timeout/fallback.

Numeric gates should be frozen after benchmarking.

---

## 21. Reliability / Failure Injection

Test:

- backend unavailable,
- DB unavailable,
- network loss,
- app/process killed,
- AI provider down,
- billing provider down,
- ad provider down,
- notification scheduling failure,
- worker restart,
- stale configuration,
- disk/storage failure where feasible.

Expected behavior must preserve authored data and safe recovery.

---

## 22. Migration & Compatibility

Cover:

- clean install,
- local DB upgrade,
- backend DB forward migration,
- migration failure,
- expand/backfill/contract sequence,
- older supported client vs newer API,
- newer client vs supported backend contract,
- rollback/recovery rehearsal where applicable.

---

## 23. CI Pipeline

Recommended stages:

```text
Static / Secret Scan
→ Unit
→ Component
→ Integration / DB
→ Contract
→ Security / Abuse
→ Build
→ E2E / Smoke
→ Artifact / Evidence
```

Expensive platform/device/provider suites may run on scheduled, merge, or release pipelines.

---

## 24. Flaky Test Policy

A flaky test must:

- be assigned,
- be investigated,
- not rely on unlimited reruns,
- be quarantined only temporarily with reason/owner/expiry,
- be restored before it silently erodes a release gate.

---

## 25. Manual / Exploratory Testing

Manual verification is appropriate for:

- real OS permissions,
- native surfaces,
- visual quality,
- store billing sandboxes,
- physical notification behavior,
- accessibility screen-reader experience,
- packaging/install/update,
- destructive recovery drills.

Manual cases still require recorded result/evidence.

---

## 26. Definition of Tested

A feature is tested only when:

- acceptance criteria are covered,
- success and failure paths are covered,
- offline/provider behavior is covered where relevant,
- security/privacy implications are covered,
- platform scope is clear,
- regressions are added for confirmed bugs,
- evidence is available.

---

## 27. Consolidated Review Criteria

Before final freeze:

- all Phase 2 MUST requirements have coverage,
- all Phase 7 Critical/High threats map to tests,
- AI/billing/ad V2 behaviors are represented,
- platform capability matrix is testable,
- numeric performance gates are identified for benchmarking,
- Phase 10 release procedures consume Phase 8 evidence.

Phase 8 remains **Draft** until final project freeze.
