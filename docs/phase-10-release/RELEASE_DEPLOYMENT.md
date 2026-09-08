# Release & Deployment

**Project:** Smart Sticky Wallpaper  
**Phase:** 10 — Production Readiness & Release  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define build promotion, CI/CD, environment separation, backend/database/client deployment, staged rollout, rollback, provider release, and post-deploy validation.  
**Governed By:** `PROJECT_CONSTITUTION.md`, Phase 08 V2, and Phase 09 V2

---

## 1. Environment Flow

```text
Development
→ CI / Integration
→ Staging
→ Production-Like Validation
→ Production
```

Each environment uses separate:

- databases,
- secrets,
- provider credentials,
- billing products/config,
- ad config,
- telemetry configuration.

---

## 2. Baseline Backend Topology

```text
Internet
  ↓
Reverse Proxy / TLS
  ↓
FastAPI Container(s)
  ├── Worker Container(s) if required
  ↓
PostgreSQL
```

Docker on VPS is the initial production baseline.

---

## 3. Container Rules

Production containers should:

- use explicit/immutable versions,
- use minimal images,
- run non-root where practical,
- expose only required ports,
- contain health checks,
- support graceful shutdown,
- exclude embedded secrets,
- be traceable to source commit/build.

---

## 4. Reverse Proxy / TLS

Provide:

- HTTPS,
- certificate management,
- request/body limits,
- forwarding headers,
- safe timeouts,
- optional edge rate limiting,
- relevant security headers.

Plain HTTP should redirect/reject as appropriate.

---

## 5. Release Artifact Build

```text
Source Commit
→ Dependency Lock Verification
→ Static / Secret / Supply-Chain Checks
→ Tests
→ Build
→ Sign / Package
→ Generate Artifact Identity
→ Store Immutable Artifact
```

Do not rebuild different bytes under the same release identity.

---

## 6. Artifact Identity

Every artifact should identify:

- app/service version,
- source commit,
- build number,
- image digest/checksum where applicable,
- build pipeline/run.

Released backend images are immutable.

---

## 7. CI Pipeline

Typical stages:

```text
Static / Secret Scan
→ Unit / Component
→ Integration / DB
→ Contract
→ Security / Abuse
→ Build
→ E2E / Smoke
→ Publish Candidate Artifact
```

CI must not expose production secrets to untrusted jobs.

---

## 8. CD Promotion

Promote the same approved candidate artifact through environments where practical.

Avoid rebuilding a “production version” manually after validation.

---

## 9. Production Approval

Production deployment should require explicit approval based on:

- G3 status,
- release manifest,
- migration plan,
- backup/restore status,
- security/privacy blockers,
- rollback plan,
- rollout scope.

---

## 10. Database Deployment Strategy

Migration ordering follows compatibility.

Preferred risky change sequence:

```text
Expand Schema
→ Deploy Backward-Compatible Backend
→ Backfill
→ Verify
→ Update Clients/Reads
→ Contract Later
```

Do not couple app-store client availability to an irreversible backend migration.

---

## 11. Migration Execution

Before running:

- verify target DB/environment,
- verify current schema version,
- confirm backup/recovery decision,
- estimate lock/runtime risk,
- confirm compatible backend versions.

After running:

- validate schema,
- validate important data,
- verify queries,
- record migration version/result.

---

## 12. Backfills

Backfills should be:

- observable,
- restartable where practical,
- idempotent where possible,
- rate/batch controlled,
- safe with concurrent application traffic,
- independently verifiable.

---

## 13. Backend Compatibility

Backend must preserve supported client versions during rollout.

Use:

- additive contracts,
- versioning,
- tolerant readers,
- deprecation windows,
- version-aware feature exposure.

---

## 14. Client Compatibility Contract

Track per client:

- app version/build,
- API contract compatibility,
- minimum supported backend,
- minimum supported client if ever enforced,
- feature capability version where needed.

Forced upgrade should be exceptional and require explicit policy.

---

## 15. Client Channels

Where supported:

- internal/developer,
- beta/test,
- staged production,
- general production.

Critical OS integrations should receive beta/physical-device validation.

---

## 16. Android Release

Validate:

- signed release artifact,
- application/version IDs,
- production API config,
- permission declarations,
- billing products,
- ad placement IDs,
- notification/background behavior,
- surface capabilities,
- store metadata/policy.

---

## 17. iOS Release

Validate:

- signing/provisioning,
- bundle/build version,
- production API config,
- StoreKit products,
- widget/notification behavior,
- background limitations,
- privacy declarations,
- App Store metadata.

---

## 18. Windows Release

Verify:

- packaging/install,
- code signing where used,
- update path,
- uninstall,
- desktop surface behavior,
- local-data location,
- native dependencies.

---

## 19. macOS Release

Verify:

- package,
- signing/notarization where applicable,
- permissions,
- update/uninstall,
- desktop surface,
- local-data location,
- native dependencies.

---

## 20. Linux Release

Verify:

- chosen package formats,
- installation/uninstallation,
- desktop environment/window manager matrix,
- startup behavior,
- surface fallback,
- native dependencies.

---

## 21. Staged Rollout

Use staged rollout where practical for:

- client versions,
- sync protocol changes,
- AI model/provider changes,
- billing changes,
- ad changes,
- platform integrations.

Increase rollout only after observation criteria pass.

---

## 22. Rollout Stages

Example:

```text
Internal
→ 1–5%
→ 10–25%
→ 50%
→ 100%
```

Exact percentages depend on platform/user volume.

Each stage defines:

- observation window,
- success metrics,
- rollback triggers,
- owner.

---

## 23. Feature Flags

Useful for noncritical features:

- AI provider/model,
- advanced intelligence,
- new surfaces,
- ad formats,
- experimental integrations.

Each production flag should have:

- owner,
- default,
- purpose,
- expiry/review date where temporary,
- rollback behavior.

---

## 24. Kill Switches

Verify safe disable for:

- AI provider/task,
- ad provider/placement,
- optional external integration,
- risky experimental capability.

Kill switches cannot disable core auth/authz/data-integrity controls.

---

## 25. Provider Rollout

New external provider flow:

```text
Mock
→ Sandbox
→ Staging
→ Limited Production
→ Full Production
```

Before full rollout verify:

- credentials,
- privacy/security review,
- failure behavior,
- monitoring,
- cost,
- kill switch.

---

## 26. Secrets Deployment

Production secrets:

- injected at runtime/secure environment layer,
- scoped minimally,
- unavailable to ordinary source builds,
- rotated on compromise,
- never logged.

---

## 27. Configuration Validation

Startup should fail safely on critical invalid config such as:

- wrong environment,
- missing DB credentials,
- missing signing/auth keys,
- invalid provider mapping,
- impossible required production setting.

Optional provider config may disable only the affected optional feature.

---

## 28. Pre-Deploy Procedure

Before production:

- candidate artifact identified,
- G3 verified,
- release manifest complete,
- migration reviewed,
- backup/restore status known,
- config/secrets validated,
- rollout stage selected,
- rollback triggers agreed,
- monitoring dashboard/alerts ready,
- operator identified.

---

## 29. Deployment Procedure

Typical backend:

```text
Confirm Pre-Deploy
→ Capture Current State/Version
→ Run Compatible Migration Stage if Required
→ Deploy Backend/Worker
→ Health Validation
→ Run Remaining Migration/Backfill Step if Designed
→ Smoke Tests
→ Observe Metrics
→ Continue/Rollback
```

Exact order follows migration compatibility design.

---

## 30. Post-Deploy Smoke

Verify:

- API health,
- DB connectivity,
- auth/session,
- note create/read,
- sync,
- entitlement fetch,
- background jobs,
- provider health,
- error rate.

Use synthetic/controlled accounts.

---

## 31. Post-Deploy Observation

Do not mark release complete immediately after one successful smoke test.

Observe defined window for:

- error spikes,
- auth failures,
- sync failures,
- DB load,
- worker backlog,
- crash/client errors,
- billing anomalies,
- AI/provider errors/cost.

---

## 32. Rollback Triggers

Examples:

- data corruption,
- cross-account leak,
- auth failure spike,
- sync corruption/failure spike,
- entitlement misgrant/misrevoke,
- severe crash regression,
- migration failure,
- large API error increase,
- security incident.

P0 triggers should stop rollout immediately.

---

## 33. Backend Rollback

Procedure:

1. stop rollout,
2. assess schema compatibility,
3. pause risky jobs,
4. disable optional feature/provider if that contains impact,
5. deploy previous compatible artifact or fix forward,
6. validate health/auth/notes/sync,
7. monitor.

---

## 34. Database Rollback

Database rollback is not assumed safe.

Preferred options:

- forward-compatible repair,
- restore in isolated environment for analysis,
- restore production only through approved recovery procedure.

Never improvise destructive reverse SQL during incident response.

---

## 35. Client Rollback

App stores may prevent immediate rollback.

Mitigate through:

- staged rollout,
- backward-compatible backend,
- kill switches,
- server-side capability disable,
- emergency client patch,
- minimum-version enforcement only when necessary.

---

## 36. Bad Client Release

If a released client is harmful:

- halt staged rollout,
- disable affected optional backend capability,
- maintain safe old-client compatibility,
- issue patched release,
- communicate known impact if needed.

Never solve a client bug by weakening server authorization.

---

## 37. Release Notes

Record:

- features/fixes,
- behavior changes,
- API/schema changes,
- migration notes,
- platform-specific changes,
- known limitations,
- operational actions.

---

## 38. Deployment Record

Record:

```text
release version
source commit
artifact digest/version
deployment time
operator
migration version
config/flag state
rollout stage
validation result
incidents/rollback
```

---

## 39. Evidence Retention

Retain links/references to:

- CI/test reports,
- G3/G4 approval,
- migration evidence,
- platform validation,
- restore test,
- security review,
- deployment record.

---

## 40. Review

Phase 10 remains **Draft** until final consolidated freeze.
