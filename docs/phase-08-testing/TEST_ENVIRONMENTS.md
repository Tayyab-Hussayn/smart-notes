# Test Environments

**Project:** Smart Sticky Wallpaper  
**Phase:** 08 — Testing  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define reproducible test environments, provider substitutes, synthetic data, platform/device coverage, failure injection, security isolation, and release-validation conditions.  
**Governed By:** `PROJECT_CONSTITUTION.md`

---

## 1. Environment Levels

```text
Local Development
CI
Integration
Staging
Production-Like Validation
Production
```

Production is not a routine test environment.

---

## 2. Environment Invariants

All non-production environments must:

- avoid production secrets,
- avoid real user note content,
- use synthetic/test accounts,
- identify environment clearly,
- isolate databases/provider credentials,
- support reproducible setup,
- prevent accidental production writes.

---

## 3. Local Development

Should support:

- client development,
- local backend,
- local/test PostgreSQL,
- selected local DB,
- mock AI,
- mock billing,
- mock ads,
- deterministic clock,
- synthetic users/data.

Core tests must not require paid live providers.

---

## 4. CI

CI uses:

- clean checkout,
- locked dependencies,
- ephemeral DB/services,
- deterministic config,
- provider mocks,
- no production secrets,
- isolated test data,
- test artifacts/logs,
- secret/dependency scanning.

Parallel runs require separate namespaces/resources.

---

## 5. Integration Environment

Used for:

- real PostgreSQL behavior,
- API integration,
- local/server migrations,
- sync,
- background jobs,
- adapter contracts,
- multi-client tests.

Prefer scripted containers/orchestration.

---

## 6. Staging

Should resemble production architecture and support:

- full backend deployment,
- provider sandboxes,
- staging client builds,
- release-candidate E2E,
- migration rehearsal,
- observability testing,
- kill-switch testing,
- backup/restore drills where safe.

No casual production-data copying.

---

## 7. Production-Like Validation

Validate:

- reverse proxy/TLS,
- production-equivalent config shape,
- DB migration,
- worker behavior,
- health/readiness,
- secrets injection,
- backup/restore procedure,
- monitoring/alerts,
- rollback path.

Use non-production credentials/data.

---

## 8. Test Accounts

Maintain synthetic profiles for:

- free,
- premium active,
- pending purchase,
- grace,
- expired,
- refunded/revoked,
- AI disabled,
- ads/consent disabled,
- multi-device,
- pending deletion,
- admin,
- account-switch A/B pair.

---

## 9. Synthetic Data

Include:

- short/long notes,
- Unicode/emoji,
- multilingual content,
- hidden/archived/deleted notes,
- recurring reminders,
- stale tombstones,
- sync conflicts,
- large outbox,
- AI-derived metadata,
- entitlement snapshots,
- expired exports.

No real secrets/content.

---

## 10. Deterministic Time

Test infrastructure should control time for:

- reminders,
- recurrence,
- snooze,
- subscription expiry,
- grace,
- token expiry,
- AI cooldown,
- ranking repetition,
- export expiry,
- webhook freshness.

---

## 11. Mock AI Provider

Modes:

- valid structured output,
- malformed output,
- low confidence,
- timeout,
- rate limit,
- outage,
- schema mismatch,
- injection-shaped output.

Provider/model evaluation runs are separately tagged.

---

## 12. Billing Test Environment

Use:

- mock billing in CI,
- provider sandboxes where applicable,
- synthetic webhook events.

Cover:

- pending,
- active,
- renewal,
- cancellation,
- grace,
- expiry,
- refund/revocation,
- duplicate event,
- out-of-order event,
- provider outage.

---

## 13. Advertising Test Environment

Mock:

- ad available,
- no-fill,
- load failure,
- consent denied,
- frequency cap,
- rewarded success/cancel,
- duplicate reward,
- kill switch,
- unsupported platform.

CI must not depend on live inventory.

---

## 14. Notification Testing

Validate:

- scheduling,
- duplicate prevention,
- cancellation,
- permission denial,
- foreground/background,
- privacy preview,
- restart,
- unsupported fallback.

Use platform-approved test mechanisms.

---

## 15. Platform Matrix

| Platform | Required Validation |
|---|---|
| Android | Core + surface/background + notifications + billing/ads |
| iOS | Core + widget/notification/fallback + billing |
| Windows | Core + desktop surface/fallback |
| macOS | Core + desktop surface/fallback |
| Linux | Core + desktop-environment capability/fallback |

Final supported OS versions must be recorded before release.

---

## 16. Device / Form-Factor Coverage

Representative coverage:

- lower-resource device,
- typical current device,
- large-screen/tablet,
- compact phone,
- desktop common resolution,
- high-DPI desktop,
- multiple Linux desktop environments where feasible.

Use physical devices for critical OS integration.

---

## 17. Network Profiles

Test:

- offline,
- high latency,
- intermittent connectivity,
- packet loss,
- reconnect,
- slow transfer,
- provider timeout.

---

## 18. Database Profiles

Test:

- empty DB,
- normal dataset,
- large dataset,
- previous-schema migration,
- temporary outage,
- connection pressure where useful,
- interrupted migration/recovery where safe.

---

## 19. Multi-Device Sync Setup

At least two logical clients against one account for:

- concurrent edit,
- edit-vs-delete,
- outbox replay,
- long offline period,
- convergence,
- stale app version where supported.

Also maintain separate-account clients for isolation tests.

---

## 20. Security Environment

Use isolated systems for:

- brute-force simulation,
- injection payloads,
- oversized requests,
- replay,
- forged webhook,
- privilege escalation,
- malicious deep links,
- SSRF/upload tests if features exist.

Never run destructive abuse testing against production.

---

## 21. Performance Environment

Record:

```text
device/machine
OS
build type
dataset size
network profile
backend resources
DB size/config
test version
```

Numbers without environment metadata are not comparable.

---

## 22. Build Types

Recommended:

- dev/debug,
- CI test,
- staging,
- release candidate,
- production.

Release candidate should use production-like optimization/signing behavior where safely possible.

---

## 23. Feature Flags / Kill Switches

Test:

- enabled,
- disabled,
- stale config,
- unsupported platform,
- entitlement-dependent state,
- AI provider kill switch,
- ad provider/placement kill switch.

Critical auth/data-integrity controls cannot be disabled by ordinary flags.

---

## 24. Test Secrets

Test secrets must be:

- separate from production,
- least-privileged,
- not committed,
- rotated if exposed,
- short-lived where practical.

Untrusted CI jobs must not receive production credentials.

---

## 25. Cleanup

Automated environments should:

- remove ephemeral users/data,
- reset DB state,
- clear provider sandbox state where possible,
- delete temporary exports,
- isolate parallel runs,
- preserve only required diagnostic artifacts.

---

## 26. Observability

Collect:

- structured logs,
- request IDs,
- traces where useful,
- metrics,
- provider state,
- build/version,
- test-case correlation.

Do not enable unsafe sensitive logging just for debugging.

---

## 27. Backup / Restore Test Setup

Non-production environment must support:

- create backup,
- restore backup,
- verify account/data integrity,
- verify deletion/tombstone reconciliation,
- document restore time/result.

---

## 28. Environment Drift

Material differences between staging and production must be documented and reviewed before release.

---

## 29. Reproducibility

Environment setup should be scripted/versioned.

Failed CI/integration tests should be reproducible locally or in a documented equivalent environment where practical.

---

## 30. Consolidated Review Criteria

Before final freeze:

- final OS/device matrix exists,
- provider mocks/sandboxes are confirmed,
- staging topology matches Phase 10,
- account A/B isolation setup exists,
- performance benchmark environment is fixed,
- no production-data dependency exists,
- backup/restore validation is reproducible.

Phase 8 remains **Draft** until final project freeze.
