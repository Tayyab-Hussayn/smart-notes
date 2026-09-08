# Operations Runbook

**Project:** Smart Sticky Wallpaper  
**Phase:** 10 — Production Readiness & Release  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define operational priorities, alert handling, incidents, recovery, provider failures, backups, restores, security response, and routine production maintenance.  
**Governed By:** `PROJECT_CONSTITUTION.md` and `PRODUCTION_READINESS.md`

---

## 1. Operational Priority

```text
User Data Safety
→ Security / Account Isolation
→ Authentication
→ Sync Integrity
→ Core Availability
→ Monetization
→ AI / Ads
```

Optional features may be disabled to protect higher priorities.

---

## 2. Incident Roles

For significant incidents identify:

- incident commander/owner,
- technical responder(s),
- communications owner where needed,
- decision log owner.

One person should own coordination even if many agents/engineers investigate.

---

## 3. Severity

### SEV-0
Security compromise, cross-user exposure, widespread destructive data loss/corruption.

### SEV-1
Major outage, widespread auth/sync failure, severe billing entitlement corruption.

### SEV-2
Important degraded functionality with workaround or limited scope.

### SEV-3
Minor operational defect.

---

## 4. Incident Workflow

```text
Detect
→ Classify
→ Assign Owner
→ Contain
→ Preserve Evidence
→ Stabilize
→ Recover
→ Validate
→ Communicate
→ Postmortem
```

---

## 5. First Response Checklist

- identify release/version,
- confirm scope/user impact,
- inspect dashboards/logs,
- inspect recent deploy/config/provider changes,
- preserve useful evidence,
- stop destructive/retry loops,
- decide containment/rollback/forward fix,
- record decisions/timestamps.

---

## 6. Health Signals

Monitor:

- API readiness/liveness,
- DB,
- worker/jobs,
- storage/disk,
- CPU/memory,
- queue/backlog,
- auth failures,
- sync failures,
- provider health,
- backups.

Health endpoints must not expose secrets.

---

## 7. Alert Standard

Each alert should specify:

- severity,
- signal/threshold,
- responder,
- first diagnostic action,
- suppression/escalation policy,
- runbook reference.

---

## 8. Security Incident

If compromise suspected:

1. contain affected access,
2. preserve evidence,
3. revoke sessions/credentials where required,
4. rotate affected secrets/keys,
5. identify exposed data/accounts,
6. patch root cause,
7. validate recovery,
8. follow notification/legal requirements,
9. update threat model/tests.

---

## 9. Cross-Account Data Exposure

Treat as SEV-0.

Actions:

- halt affected rollout,
- disable risky sync/account-switch path if safely possible,
- prevent further exposure,
- preserve logs/audit evidence,
- identify affected accounts/data,
- fix authorization/isolation root cause,
- regression test,
- assess disclosure obligations.

---

## 10. Signing / Secret Compromise

If signing/provider/deploy credential leaks:

- revoke/rotate immediately where supported,
- identify artifacts/actions created with compromised credential,
- restrict deployment,
- verify repository/CI integrity,
- replace trusted material,
- monitor abuse.

---

## 11. Database Outage

- verify DB process/network/storage,
- inspect connections/locks,
- stop retry storm,
- preserve client local-first operation,
- restore DB,
- validate integrity,
- monitor sync backlog.

---

## 12. Data Corruption

- stop destructive writes if required,
- snapshot current state,
- scope affected rows/accounts,
- inspect release/migration/sync cause,
- compare backup/audit evidence,
- choose repair vs restore,
- validate in isolation,
- reopen carefully.

---

## 13. Backup Failure

- alert,
- identify failure,
- verify last successful backup,
- run manual backup if safe,
- validate artifact,
- repair automation/storage,
- record uncovered recovery window.

---

## 14. Restore Procedure

1. choose restore point,
2. provision isolated target,
3. restore backup,
4. verify schema,
5. run required migrations,
6. verify data integrity,
7. reconcile deleted accounts/tombstones,
8. ensure stale data will not reactivate,
9. validate application,
10. reconnect traffic only after approval,
11. monitor sync convergence.

---

## 15. Restore Drill

Periodically record:

- restore source/date,
- restore duration,
- recovered schema version,
- integrity checks,
- deletion reconciliation result,
- issues,
- actual RPO/RTO evidence.

---

## 16. Migration Failure

- stop rollout,
- capture current schema/migration state,
- stop conflicting deploys/jobs,
- use tested recovery plan,
- avoid improvised destructive SQL,
- restore only if approved,
- verify before resume.

---

## 17. Sync Failure Spike

- inspect error/conflict metrics,
- identify app/server versions,
- inspect revision/idempotency/tombstone patterns,
- stop risky rollout,
- prevent blind mass retry,
- verify account isolation,
- fix/reconcile,
- confirm convergence.

---

## 18. Sync Corruption / Resurrection

If deleted data reappears or authored state corrupts:

- treat as high severity,
- preserve affected state,
- disable dangerous reconciliation path,
- protect tombstones,
- identify stale-client pattern,
- repair data,
- add regression test,
- validate multi-device convergence.

---

## 19. Authentication Outage

- inspect identity/session endpoints,
- verify signing keys/time/config,
- inspect DB,
- inspect recent key/config deployment,
- avoid weakening authentication,
- preserve local authored work,
- restore and verify refresh/revocation.

---

## 20. Credential Stuffing / Abuse

- inspect source/rate patterns,
- tighten existing abuse controls carefully,
- block malicious sources where useful,
- protect recovery endpoints,
- do not globally lock legitimate users unnecessarily.

---

## 21. AI Provider Outage

Expected:

- notes work,
- deterministic intelligence continues,
- AI requests remain bounded.

Actions:

- activate provider kill switch,
- route to approved alternate provider if configured,
- reduce/disable optional AI tasks,
- monitor cost/retry queues.

---

## 22. AI Quality Regression

If new model/prompt produces bad outcomes:

- stop staged rollout,
- revert provider/model/task version,
- disable affected task,
- preserve authored data,
- inspect evaluation gap,
- update eval/test set before re-enable.

---

## 23. AI Cost Anomaly

- inspect task/provider distribution,
- identify retry/abuse loop,
- enforce quota/rate caps,
- disable expensive path if required,
- preserve core functionality.

---

## 24. Billing Provider Outage

Expected:

- no false premium grants,
- bounded verified grace applies,
- purchase/restore may delay.

Actions:

- monitor provider,
- pause risky retries,
- reconcile when provider returns,
- communicate pending verification if needed.

---

## 25. Billing Webhook Failure

- verify endpoint/certificate/signature config,
- inspect provider delivery logs,
- do not manually grant based on unverified event,
- restore processing,
- replay/reconcile through approved provider mechanism,
- verify entitlement versions.

---

## 26. Out-of-Order / Incorrect Entitlement State

- stop affected billing rollout,
- identify latest verified provider state,
- run reconciliation,
- correct normalized state,
- verify account scope,
- audit impacted grants/revocations,
- add regression coverage.

---

## 27. Ad Provider Outage

Expected:

- ad containers disappear/cleanly fail,
- core app continues,
- premium unaffected.

Use provider/placement kill switch if needed.

---

## 28. Reward Abuse / Duplicate Grants

- disable rewarded placement if widespread,
- inspect event IDs/idempotency,
- prevent new duplicate grants,
- repair temporary reward state,
- preserve paid entitlement truth.

---

## 29. Notification Failure

- inspect OS/provider scheduling,
- preserve reminder truth,
- retry only within platform rules,
- do not create duplicate notifications,
- expose diagnostic state.

---

## 30. Export Privacy Incident

If an export becomes accessible to wrong account/publicly:

- disable export delivery,
- revoke links/artifacts,
- identify accesses,
- repair ownership/auth,
- delete exposed temporary artifacts where appropriate,
- assess privacy obligations,
- regression test.

---

## 31. Account Deletion Failure

- stop state that may resurrect deleted data,
- verify session revocation,
- verify sync/tombstones,
- inspect provider cleanup,
- complete active data deletion,
- document backup aging separately.

---

## 32. Disk Capacity

- identify DB/log/container growth,
- protect DB first,
- rotate/prune logs according to policy,
- expand storage if needed,
- never manually delete unknown database files.

---

## 33. CPU / Memory / Worker Backlog

Inspect:

- recent deployment,
- request storm,
- sync storm,
- provider retry loop,
- expensive DB query,
- worker backlog.

Scale, throttle, disable optional work, or rollback based on cause.

---

## 34. Rate-Limit Incident

If legitimate users blocked:

- distinguish abuse vs bad threshold,
- inspect per-user/global/provider limits,
- adjust through reviewed config,
- preserve security controls.

---

## 35. Bad Backend Release

- halt rollout,
- determine schema compatibility,
- disable optional failing feature,
- rollback artifact or fix forward,
- validate auth/notes/sync,
- monitor.

---

## 36. Bad Client Release

- halt staged rollout,
- use server kill switch for affected optional capability,
- preserve backend compatibility,
- issue patched client,
- avoid weakening server trust boundaries.

---

## 37. Provider Breach

For AI/billing/ad/notification provider incident:

- disable integration if necessary,
- rotate credentials,
- assess data shared,
- identify affected users/events,
- use alternate provider only if approved,
- update security/privacy registry.

---

## 38. Log Access

Production logs:

- access-controlled,
- redacted,
- retention-bounded,
- used for operational need only.

Never paste private logs into public issue trackers or broad AI prompts.

---

## 39. Routine Maintenance

Schedule:

- OS/runtime patching,
- dependency/security review,
- secret rotation,
- backup verification,
- restore drill,
- disk/capacity review,
- DB performance review,
- provider/cost review,
- alert review,
- stale feature-flag review,
- admin/session review.

---

## 40. Postmortem

For SEV-0/1 and meaningful SEV-2 incidents record:

- timeline,
- impact,
- root cause,
- contributing factors,
- detection gap,
- response/recovery,
- user communication,
- tests/gates that should have caught it,
- preventive actions,
- owner/due date.

---

## 41. Operational Learning

After incidents or real production experience update:

- this runbook,
- alerts,
- Phase 7 threat model where relevant,
- Phase 8 tests/gates,
- deployment procedure,
- known limitations.

Phase 10 remains **Draft** until final consolidated freeze.
