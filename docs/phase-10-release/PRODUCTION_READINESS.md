# Production Readiness

**Project:** Smart Sticky Wallpaper  
**Phase:** 10 — Production Readiness & Release  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define evidence-backed conditions that must be satisfied before a release may be approved for production.  
**Governed By:** `PROJECT_CONSTITUTION.md`  
**Primary Inputs:** Phase 02–09 V2 specifications

---

## 1. Core Rule

> Production readiness is demonstrated by evidence, not by implementation claims.

A feature being coded, merged, or passing a small smoke test does not make the system production-ready.

---

## 2. Readiness Dimensions

A production candidate must be ready across:

- product functionality,
- data integrity,
- security,
- privacy,
- authentication/account isolation,
- synchronization,
- database/migrations,
- backend/runtime,
- clients/platform capabilities,
- AI,
- billing/entitlements,
- advertising/rewards,
- accessibility,
- performance/capacity,
- observability,
- backups/recovery,
- deployment/rollback,
- supply chain,
- policy/store requirements,
- support/operations.

---

## 3. Quality Gate Prerequisite

Before production approval:

- Phase 8 G3 is passed for the release candidate,
- no unresolved P0 exists,
- no unaccepted Critical security/privacy issue exists,
- P1 exceptions are explicitly documented/approved,
- required regression suites pass,
- release evidence is attached to the release record.

G4 is completed only after production deployment and validation.

---

## 4. Functional Readiness

Verify:

- note CRUD/lifecycle,
- canvas persistence,
- reminders/recurrence/snooze/completion,
- local-first operation,
- search/retrieval,
- sync,
- surface capability/fallback,
- notifications,
- AI optional behavior,
- entitlement flows,
- advertising eligibility,
- export,
- account deletion.

Core notes must remain usable during optional provider outages.

---

## 5. Account Isolation Readiness

P0 evidence required for:

- account A → B client switch,
- no stale A note rendering,
- no A outbox under B,
- no A entitlement/reward cache under B,
- server cross-user resource rejection,
- export isolation,
- AI-derived state isolation.

Any confirmed cross-account leak blocks production.

---

## 6. Sync & Data Integrity Readiness

Verify:

- offline create/edit/delete,
- atomic local mutation + outbox invariant,
- reconnect,
- idempotent replay,
- stale revisions,
- concurrent multi-device updates,
- edit-vs-delete,
- tombstone propagation,
- no stale-client resurrection,
- partial sync failure,
- process restart mid-sync,
- long-offline reconnect,
- eventual convergence.

Known authored-data loss/corruption blocks release.

---

## 7. Database Readiness

PostgreSQL production setup requires:

- restricted network exposure,
- least-privileged application role,
- version-controlled schema,
- reviewed indexes/constraints,
- monitored storage/connections,
- backup automation,
- tested restore,
- documented maintenance approach.

No casual production schema edits.

---

## 8. Migration Readiness

Every production migration must have:

- migration version,
- staging rehearsal,
- compatible deployment order,
- data preservation validation,
- expected runtime/lock impact,
- recovery strategy,
- backup decision,
- post-migration validation.

For risky changes prefer:

```text
Expand
→ Deploy Compatible Code
→ Backfill
→ Verify
→ Contract Later
```

Irreversible/destructive migrations require explicit approval.

---

## 9. Backend Runtime Readiness

FastAPI production runtime should include:

- reverse proxy/TLS,
- health/readiness endpoints,
- graceful shutdown,
- bounded timeouts,
- structured logs,
- request/correlation IDs,
- rate limits,
- safe error responses,
- background worker health if used,
- restart policy,
- environment-specific configuration.

---

## 10. Client Readiness

For every supported platform:

- production build succeeds,
- signing/package setup verified,
- clean install works,
- upgrade path works where applicable,
- app starts without stale dev config,
- local persistence works,
- sync/account isolation works,
- notifications/surface fallback work,
- capability matrix matches actual OS behavior,
- production endpoints/provider IDs are correct,
- accessibility baseline passes.

---

## 11. Android Readiness

Validate as applicable:

- signing,
- application ID,
- target/minimum OS support,
- runtime permissions,
- notifications/background scheduling,
- persistent surface behavior,
- billing products,
- ads configuration,
- privacy/store declarations,
- install/update/uninstall.

---

## 12. iOS Readiness

Validate:

- signing/provisioning,
- bundle ID/version,
- approved surface/widget behavior,
- notifications/background constraints,
- StoreKit configuration,
- privacy declarations,
- App Store metadata,
- install/update behavior.

Do not claim unsupported lock-screen behavior.

---

## 13. Desktop Readiness

For Windows/macOS/Linux validate:

- packaging,
- signing/notarization where applicable,
- install/update/uninstall,
- local data location,
- permissions,
- startup/background behavior,
- desktop surface/fallback,
- native dependencies,
- supported OS/environment matrix.

Linux support should document desktop-environment limitations.

---

## 14. AI Readiness

Before AI is enabled in production:

- provider/task adapter configured,
- structured-output validation active,
- deterministic fallback tested,
- privacy opt-out tested,
- quotas/rate/cost controls active,
- prompt-injection boundary tested,
- model/provider/task schema versions recorded,
- relevant evaluation suite passed,
- timeout/outage path tested,
- kill switch/provider-switch path tested,
- no provider secret in client.

---

## 15. AI Change Readiness

A new model/provider/prompt affecting semantics requires:

- evaluation comparison,
- regression review,
- schema compatibility check,
- privacy/provider review,
- cost/latency review,
- staged rollout,
- rollback/disable plan.

---

## 16. Billing & Entitlement Readiness

Verify:

- production product mappings,
- server-side purchase verification,
- pending verification behavior,
- renewal,
- cancellation,
- grace,
- expiry,
- refund/revocation,
- restore,
- bounded offline cache,
- account isolation,
- client tampering does not grant premium.

---

## 17. Billing Event Readiness

Webhook/provider-event processing must demonstrate:

- authenticity verification,
- replay protection,
- deduplication,
- out-of-order event handling,
- idempotent state transitions,
- reconciliation after missed/failed events,
- auditability,
- provider outage behavior.

---

## 18. Advertising Readiness

If ads are enabled:

- eligibility rules verified,
- premium users ad-free,
- prohibited contexts enforced,
- consent/privacy policy verified,
- no private note semantics sent,
- frequency/cooldown controls active,
- no-fill/failure safe,
- rewarded grants duplicate-safe,
- account isolation verified,
- provider/placement kill switch tested.

Unsupported platforms may ship with no ads.

---

## 19. Security Readiness

Required evidence:

- authorization/IDOR tests,
- session expiry/revocation,
- cross-account isolation,
- secrets scan,
- TLS validation,
- rate limits,
- webhook verification,
- admin access controls,
- sync replay tests,
- prompt-injection boundary,
- export authorization,
- log redaction,
- dependency vulnerability review.

Known Phase 7 release blockers remain blockers.

---

## 20. Supply-Chain / Build Readiness

Release build must have:

- locked/pinned dependencies,
- secret scan,
- dependency/security scan,
- identifiable source commit,
- reproducible-enough documented build process,
- artifact checksums/signatures where supported,
- protected signing credentials,
- SBOM retained where practical,
- no production secrets embedded in artifacts.

---

## 21. Privacy Readiness

Verify:

- note content excluded from general telemetry,
- AI external data flows match policy,
- ad providers receive no private note content,
- notification privacy setting works,
- export is account-scoped,
- account deletion lifecycle works,
- retention policy documented,
- processor/provider registry current,
- privacy disclosures match actual behavior.

---

## 22. Accessibility Readiness

At minimum:

- semantic labels,
- keyboard/focus where applicable,
- text scaling,
- non-color-only meaning,
- accessible alternatives to drag,
- reduced motion where applicable,
- ads do not trap focus,
- key release flows remain operable.

---

## 23. Performance Readiness

Before broad production rollout establish measured baselines/gates for:

- startup,
- note/editor interaction,
- dense canvas,
- local DB operations,
- sync throughput,
- API latency,
- memory,
- background work,
- AI timeout/fallback.

Metrics must identify test environment and dataset.

---

## 24. Capacity Readiness

Document initial expected:

- active users,
- notes/user,
- sync requests,
- DB storage/growth,
- worker backlog,
- AI volume/cost,
- billing/webhook traffic,
- backup growth,
- log/metric retention.

Initial values may be modest but must be explicit.

---

## 25. Observability Readiness

Production monitoring should cover:

- API availability/latency/error rate,
- DB availability/connections/storage,
- worker/job backlog,
- sync failure/conflict rate,
- auth failure anomalies,
- AI provider/cost/latency,
- billing verification/webhook/reconciliation,
- backup jobs,
- crash/client health where enabled,
- disk/CPU/memory.

---

## 26. Alert Readiness

Each critical alert should define:

```text
signal
threshold/condition
severity
owner/route
first action
runbook link
```

Alerts without an actionable response path create noise.

---

## 27. Logging Readiness

Logs must be:

- structured,
- redacted,
- access-controlled,
- retention-bounded,
- correlated to release/request/job where useful.

Do not log raw note bodies, tokens, provider secrets, or export contents by default.

---

## 28. Backup Readiness

Before launch:

- automated backups enabled,
- retention defined,
- access restricted,
- off-host/isolated storage considered,
- backup completion monitored,
- restore test completed,
- restore evidence recorded.

A backup never restore-tested is not verified.

---

## 29. Restore Readiness

Restore rehearsal must verify:

- backup is readable,
- schema/data integrity,
- migrations after restore if required,
- deleted-account/tombstone reconciliation,
- no unintended stale data activation,
- application reconnect,
- sync convergence.

---

## 30. Recovery Objectives

Before public production operation define target or provisional:

- **RPO** — acceptable data recovery window,
- **RTO** — acceptable service recovery time.

If final targets depend on benchmarking, assign owner/date to finalize them before release approval.

---

## 31. Rollback Readiness

Release must identify:

- previous backend artifact,
- compatible configuration,
- migration compatibility,
- provider/feature kill switches,
- client rollback constraints,
- forward-fix path,
- responsible operator.

Client-store rollback limitations require backend compatibility and remote-safe disable paths.

---

## 32. Configuration Readiness

Production config must be:

- environment-specific,
- validated,
- auditable/versioned where practical,
- secret-safe,
- separated from staging/dev,
- covered by startup validation.

Ordinary feature flags cannot disable auth/authorization/data-integrity controls.

---

## 33. Release Manifest

Each candidate should record:

```text
release_version
source_commit
backend_image_digest/version
client_versions
migration_version
config_version/reference
enabled feature flags
AI model/provider/task versions
billing/ad provider configuration references
known limitations
test/gate evidence
owner
```

---

## 34. Store / Policy Readiness

Where applicable:

- package IDs,
- signing keys,
- permission declarations,
- privacy disclosures,
- subscription products,
- ad disclosures,
- screenshots/metadata,
- platform policy review.

---

## 35. Legal / User Documentation

Before public launch, provide as applicable:

- Privacy Policy,
- Terms of Service,
- subscription terms,
- deletion instructions,
- AI processing disclosure,
- third-party disclosures.

Launch-region legal review remains separate where needed.

---

## 36. Support Readiness

Before public launch define:

- user support/contact route,
- billing/restore troubleshooting path,
- account recovery support boundaries,
- incident escalation path,
- privacy/deletion request handling,
- known-issue communication channel.

---

## 37. Known Limitations

Document honestly:

- platform surface differences,
- unsupported lock-screen behavior,
- background restrictions,
- AI/provider limitations,
- desktop billing/ad constraints,
- supported OS versions,
- any feature under staged rollout.

---

## 38. Production Approval Package

Production approver should be able to inspect:

- release manifest,
- G3 evidence,
- migration result,
- backup/restore evidence,
- security/privacy status,
- provider readiness,
- platform smoke results,
- rollout/rollback plan,
- known limitations.

---

## 39. Release Blockers

Do not release with known:

- P0 defect,
- cross-user/account leak,
- data-loss/corruption path,
- auth bypass,
- Critical security issue,
- unverified entitlement grant,
- unsafe destructive migration,
- untested backup restore,
- broken deletion/export authorization,
- privacy leak of authored content.

---

## 40. Review

Phase 10 remains **Draft** until the project-wide consolidated review and final freeze.
