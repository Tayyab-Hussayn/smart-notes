# Test Case Catalog

**Project:** Smart Sticky Wallpaper  
**Phase:** 08 — Testing  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Provide a stable baseline catalog of high-value tests and traceability IDs for implementation agents and CI.

---

## 1. ID Convention

```text
DOMAIN-TYPE-NNN
```

Types may include `UNIT`, `UI`, `INT`, `CONTRACT`, `SEC`, `PRIV`, `PERF`, `REL`, `E2E`.

---

## 2. Notes

- `NOTE-UNIT-001` Create note → valid local entity.
- `NOTE-UNIT-002` Oversized note → rejected without mutation.
- `NOTE-UNIT-003` Edit → authored content preserved.
- `NOTE-UNIT-004` Archive/restore → lifecycle correct.
- `NOTE-UNIT-005` Delete → active queries exclude note.
- `NOTE-UNIT-006` Duplicate → independent ID/lifecycle.
- `NOTE-REL-007` Crash after local save → authored content recoverable.

---

## 3. Canvas

- `CANVAS-UI-001` Drag → logical placement persists.
- `CANVAS-UI-002` Resize → constraints respected.
- `CANVAS-UI-003` Restart → placement restored.
- `CANVAS-UI-004` Accessible move alternative works.
- `CANVAS-INT-005` Screen/window change → logical layout remains valid.
- `CANVAS-UI-006` Unsupported external surface → fallback shown.

---

## 4. Reminders

- `REM-UNIT-001` Exact time → correct schedule.
- `REM-UNIT-002` Relative date → correct date.
- `REM-UNIT-003` Semantic daypart → expected window.
- `REM-UNIT-004` Recurrence completion preserves recurrence.
- `REM-UNIT-005` Snooze suppresses until expiry.
- `REM-UNIT-006` One-time completion prevents resurfacing.
- `REM-INT-007` Duplicate scheduling retry → one occurrence.
- `REM-INT-008` Restart → state/schedule recoverable.
- `REM-UNIT-009` Timezone/clock change → defined behavior.

---

## 5. Intelligence

- `INTEL-UNIT-001` Hidden/deleted note → ineligible.
- `INTEL-UNIT-002` Snoozed note → suppressed.
- `INTEL-UNIT-003` Pinned note → explicit priority respected.
- `INTEL-UNIT-004` Repeated dismissal → penalty/cooldown.
- `INTEL-UNIT-005` Intelligence disabled → no contextual surfacing.
- `INTEL-UNIT-006` Explicit state overrides AI.
- `INTEL-UNIT-007` Candidate limit enforced.
- `INTEL-UNIT-008` Notification not created from AI relevance alone.

---

## 6. AI

- `AI-CONTRACT-001` Valid structured response → accepted.
- `AI-CONTRACT-002` Malformed response → rejected.
- `AI-CONTRACT-003` Schema version mismatch → safe rejection.
- `AI-INT-004` Timeout/outage → core flow continues.
- `AI-SEC-005` Prompt injection → no policy bypass.
- `AI-PRIV-006` Opt-out → zero provider call.
- `AI-QUOTA-007` Quota exhausted → graceful rejection.
- `AI-INT-008` Retry → no duplicate domain action.
- `AI-EVAL-009` Model/provider change → curated eval gate passes.

---

## 7. Authentication / Authorization

- `AUTH-INT-001` Valid login → session.
- `AUTH-INT-002` Invalid credentials → no session.
- `AUTH-SEC-003` Expired access credential → rejected.
- `AUTH-SEC-004` Revoked refresh credential → cannot refresh.
- `AUTH-SEC-005` Brute-force → rate limit.
- `AUTHZ-SEC-001` Another user's note → denied.
- `AUTHZ-SEC-002` Another user's reminder → denied.
- `AUTHZ-SEC-003` Another user's export → denied.
- `AUTHZ-SEC-004` Normal user admin route → denied.
- `AUTHZ-SEC-005` Client changes `user_id` → ignored/rejected.

---

## 8. Account Isolation

- `ACCT-SEC-001` A→B switch → no A note visible.
- `ACCT-SEC-002` A outbox → never syncs under B.
- `ACCT-SEC-003` A entitlement cache → cannot authorize B.
- `ACCT-SEC-004` A rewarded state → cannot apply to B.
- `ACCT-SEC-005` Account transition → no stale-data flash.

---

## 9. Sync

- `SYNC-INT-001` Offline create → sync once after reconnect.
- `SYNC-INT-002` Duplicate mutation → idempotent.
- `SYNC-INT-003` Two-device edit → deterministic reconciliation.
- `SYNC-INT-004` Delete vs stale edit → no resurrection.
- `SYNC-INT-005` Long-offline reconnect → queue drains safely.
- `SYNC-SEC-006` Cross-account mutation → denied.
- `SYNC-INT-007` Network drop mid-sync → safe retry.
- `SYNC-REL-008` Process death mid-sync → safe resume.
- `SYNC-INT-009` Partial batch failure → successful ops not duplicated.
- `SYNC-INT-010` Local mutation + outbox → atomic invariant.

---

## 10. Billing

- `BILL-INT-001` Valid purchase → verified entitlement.
- `BILL-INT-002` Pending verification → no premature grant.
- `BILL-SEC-003` Fake local premium → denied.
- `BILL-SEC-004` Replayed transaction → no duplicate grant.
- `BILL-INT-005` Renewal → period/version updates.
- `BILL-INT-006` Cancellation → active until verified end.
- `BILL-INT-007` Grace → bounded entitlement.
- `BILL-INT-008` Expiry → premium off, owned data remains.
- `BILL-INT-009` Restore → idempotent rebuild.
- `BILL-SEC-010` Invalid webhook signature → rejected.
- `BILL-SEC-011` Duplicate webhook → no duplicate effect.
- `BILL-SEC-012` Out-of-order webhook → newer state preserved.
- `BILL-REL-013` Partial DB failure → rollback/reconcile.

---

## 11. Advertising

- `ADS-INT-001` Eligible free user → approved placement may load.
- `ADS-INT-002` Premium → no ad.
- `ADS-INT-003` No-fill/provider failure → core UI unaffected.
- `ADS-INT-004` Prohibited context → no ad request/show.
- `ADS-INT-005` Frequency cap → suppresses excess display.
- `ADS-INT-006` Entitlement activates in-session → ads disappear.
- `ADS-SEC-007` Reward completion → one grant.
- `ADS-SEC-008` Duplicate/cross-account reward → rejected.
- `ADS-PRIV-009` Provider payload → no private note semantics.
- `ADS-INT-010` Kill switch → provider/placement disabled safely.

---

## 12. Privacy / Lifecycle

- `PRIV-001` Analytics → no raw note body.
- `PRIV-002` Crash report → no token/note body.
- `PRIV-003` Notification privacy → configured preview enforced.
- `PRIV-004` AI minimization → task-only context.
- `PRIV-005` AI disabled → no external AI request.
- `DELETE-E2E-001` Account delete → sessions revoked and lifecycle begins.
- `DELETE-INT-002` Stale deleted-note sync → no resurrection.
- `DELETE-REL-003` Backup restore → deleted state not reactivated.
- `EXPORT-E2E-001` Own export → only supported own data.
- `EXPORT-SEC-002` Tampered export ID → denied.
- `EXPORT-SEC-003` Expired export → unavailable.

---

## 13. Security / Supply Chain

- `SEC-INJ-001` SQL injection payload → harmless/rejected.
- `SEC-INJ-002` Oversized request → bounded rejection.
- `SEC-LOG-003` Sensitive fields → redacted.
- `SEC-DEEP-004` Malicious deep link → no unauthorized action.
- `SEC-CICD-005` Untrusted CI path → no production secret access.
- `SEC-SUPPLY-006` Critical vulnerable dependency → gate blocks release.
- `SEC-SSRF-007` Private network URL → blocked if URL-fetch feature exists.

---

## 14. Platform Capability

- `CAP-ANDROID-001` Declared Android capability/fallback matches behavior.
- `CAP-IOS-001` Declared iOS capability/fallback matches behavior.
- `CAP-WIN-001` Declared Windows capability/fallback matches behavior.
- `CAP-MAC-001` Declared macOS capability/fallback matches behavior.
- `CAP-LINUX-001` Declared Linux capability/fallback matches behavior.

---

## 15. Accessibility

- `A11Y-001` Screen reader labels usable.
- `A11Y-002` Keyboard core desktop flow works.
- `A11Y-003` Font scaling remains usable.
- `A11Y-004` Non-drag essential path exists.
- `A11Y-005` Reduced motion respected.
- `A11Y-006` Ads do not trap focus.
- `A11Y-007` Status meaning not color-only.

---

## 16. Performance / Reliability

- `PERF-001` 10-note baseline.
- `PERF-002` 100-note normal profile.
- `PERF-003` 1,000-note stress/limit profile.
- `PERF-004` Large outbox drains without loss.
- `REL-001` Backend down → local CRUD.
- `REL-002` DB down → safe backend failure.
- `REL-003` AI down → core usable.
- `REL-004` Billing down → bounded verified policy.
- `REL-005` Ads down → no blocking.
- `REL-006` Worker/app restart → durable work recovers.

---

## 17. Migration / Compatibility

- `MIG-001` Clean install initializes latest schema.
- `MIG-002` Existing local DB upgrade preserves data.
- `MIG-003` Backend migration preserves invariants.
- `MIG-004` Failed migration → safe recovery.
- `MIG-005` Expand/backfill/contract sequence remains compatible.
- `API-COMPAT-001` Older supported client works with current backend contract.

---

## 18. Critical E2E Journeys

- `E2E-FLOW-001` First run → create/place/remind/reopen.
- `E2E-FLOW-002` Offline create/edit → reconnect → converge.
- `E2E-FLOW-003` Ambiguous AI suggestion → confirmation required.
- `E2E-FLOW-004` Purchase → verify → unlock → ads stop.
- `E2E-FLOW-005` Expiry → premium restricts, owned data remains.
- `E2E-FLOW-006` Account A→B → strict data isolation.
- `E2E-FLOW-007` Export → secure artifact → expiry.
- `E2E-FLOW-008` Account deletion → revoke/sync stop/delete lifecycle.

---

## 19. Test Metadata

Track where practical:

```text
id
requirement_reference
threat_reference?
priority
test_level
platform
automated/manual
owner
status
last_result
evidence
```

---

## 20. Consolidated Review Criteria

Before final freeze:

- every Phase 2 MUST maps to a case/procedure,
- every Phase 7 Critical/High threat maps to a security case,
- P0/P1 cases have ownership,
- platform capability cases match the final support matrix,
- implementation may add cases but may not silently weaken this baseline.

Phase 8 remains **Draft** until final project freeze.
