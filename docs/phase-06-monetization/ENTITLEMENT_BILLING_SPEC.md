# Entitlement & Billing Specification

**Project:** Smart Sticky Wallpaper
**Phase:** 06 — Monetization
**Version:** 2.0
**Status:** Draft — Consolidated Review Pending
**Purpose:** Define billing verification, normalized commercial state, entitlement resolution, webhooks, idempotency, offline trust, restoration, auditability, account isolation, and fraud resistance.

## 1. Source-of-Truth Model

### Payment Truth
Approved billing provider/store.

### Normalized Commercial Truth
Backend billing module after provider verification.

### Application Feature Truth
Backend entitlement resolver.

### Client
Authenticated bounded cache/consumer only.

```text
Billing Provider
→ Backend Verification
→ Normalized Subscription
→ Entitlement Resolution
→ Versioned Snapshot
→ Client
```

## 2. Core Principles

1. Purchase claims are verified server-side.
2. Clients cannot self-authorize premium.
3. Provider events are authenticated where supported.
4. Durable billing events are idempotent.
5. Entitlements are feature-based.
6. Account boundaries are strict.
7. Existing user data survives entitlement loss.
8. Offline trust is bounded.
9. Billing state is auditable.
10. Provider-specific states are normalized.
11. Restore/reconciliation is safe to repeat.
12. Manual overrides are exceptional and controlled.

## 3. Core Entities

### Subscription
```text
subscription_id
user_id
provider
provider_account_reference?
provider_subscription_id
provider_product_id
internal_plan_id
normalized_status
current_period_start
current_period_end
cancel_at_period_end
grace_until?
environment
last_verified_at
created_at
updated_at
```

### Entitlement
```text
entitlement_id
user_id
entitlement_key
status
source
starts_at
ends_at?
source_reference
version
last_verified_at
```

### BillingEvent
```text
billing_event_id
provider
provider_event_id
event_type
received_at
processed_at?
status
payload_hash
failure_code?
```

### PurchaseVerification
```text
verification_id
user_id
provider
transaction_reference
product_id
status
verified_at?
failure_code?
idempotency_key?
```

## 4. Normalized Subscription States

```text
inactive
pending
active
grace_period
cancelled_pending_expiry
expired
revoked
refunded
verification_failed
```

Unknown provider state must not silently map to active.

## 5. Entitlement States

```text
active
grace
inactive
revoked
```

Derived from verified commercial state and plan policy.

## 6. Purchase Flow

```text
User selects plan
→ Native/approved purchase
→ Provider returns transaction reference
→ Client submits reference + authenticated context
→ Backend validates
→ Backend verifies provider
→ Normalize subscription
→ Resolve entitlements
→ Persist required state
→ Increment version
→ Return snapshot
→ Client updates UI
```

## 7. Verification Requirements

Verify as provider allows:
- provider identity
- transaction/product ID
- environment
- ownership/account linkage
- purchase time
- expiry
- renewal state
- refund/revocation
- duplicate/replay status
- offer/trial data where relevant

Unverifiable claims are rejected.

## 8. Verification Idempotency

Repeated verification must not:
- duplicate subscription rows
- duplicate entitlements
- double-count revenue events
- corrupt lifecycle state

Use provider transaction IDs plus internal deduplication.

## 9. Atomicity

Commercial-state and entitlement updates should be transactional where practical.

Avoid partial states such as:
- active subscription with missing entitlements
- refund persisted while premium remains indefinitely

Reconciliation must repair partial failures.

## 10. Entitlement Resolution

Resolver consumes:
- normalized commercial state
- internal plan definition
- time/grace policy
- approved promotions/overrides

It emits feature keys.

Feature modules never inspect provider product IDs directly.

## 11. Entitlement Snapshot

Conceptual response:

```json
{
  "version": 42,
  "verified_at": "2026-09-08T12:00:00Z",
  "commercial_state": "active",
  "valid_until": "2026-10-08T12:00:00Z",
  "offline_revalidate_after": "2026-09-15T12:00:00Z",
  "features": {
    "feature.ads.remove": true,
    "feature.ai.advanced": true
  }
}
```

Exact schema is finalized with API design.

## 12. Snapshot Versioning

Use monotonic revision/version or equivalent.

Client should:
- refresh when a newer version is known
- avoid replacing newer state with obviously older snapshots
- scope snapshots to the authenticated account

## 13. Account Isolation

On sign-out/account switch:
- previous cache stops authorizing premium
- billing UI reloads for active account
- restore binds to active account according to policy
- snapshots never cross users

## 14. Renewal

Renewal may arrive through:
- provider server notification
- backend reconciliation
- explicit client refresh

On verified renewal:
- period updates
- normalized state active
- entitlements resolve
- version increments
- audit transition recorded

## 15. Cancellation

Cancellation normally maps to:

`cancelled_pending_expiry`

Until verified end:
- entitlement remains active
- current period end is authoritative
- UI may show cancellation date

## 16. Grace Period

If provider supports grace:
- state becomes `grace_period`
- selected entitlements may remain active
- grace end is stored
- backend revalidates
- grace remains bounded/configurable

Grace cannot become indefinite provider bypass.

## 17. Expiration

Verified expiration:
- state → `expired`
- paid entitlements → inactive
- existing data remains
- snapshot version increments
- client refreshes

## 18. Refund / Revocation

Verified refund/revocation:
- authenticate provider event or re-query provider
- persist event
- update normalized state
- revoke applicable entitlements
- preserve user-owned notes
- audit transition

## 19. Restore

```text
Client requests restore
→ Provider returns owned transactions/subscriptions
→ Submit references
→ Backend verifies
→ Deduplicate
→ Normalize
→ Rebuild entitlements
→ Return latest snapshot
```

Restore is idempotent.

## 20. Webhook Processing

```text
Receive
→ Enforce payload limit
→ Authenticate provider
→ Validate timestamp/replay constraints
→ Validate schema/event type
→ Deduplicate provider_event_id
→ Persist receipt
→ Process state transition
→ Resolve entitlements
→ Mark processed
```

Transient processing failure remains retriable.

## 21. Webhook Security

Controls:
- signature verification
- trusted certificate/key validation where relevant
- timestamp freshness
- replay protection
- event allowlisting
- payload-size limit
- schema validation
- no unauthenticated entitlement mutation
- abuse/rate protection where appropriate

## 22. Event Ordering

Provider events may arrive late or out of order.

Processing should compare provider commercial timestamps/version/state rather than blindly using receive order.

Older events must not incorrectly overwrite newer verified state.

## 23. Duplicate Events

Duplicate events resolve to the same final state without duplicate effects.

Store provider event IDs or equivalent deduplication evidence.

## 24. Periodic Reconciliation

Webhooks alone are not permanent truth.

Support reconciliation via:
- scheduled stale-record checks
- client entitlement refresh
- restore
- support/admin troubleshooting

Reconciliation re-queries provider truth and repairs normalized state.

## 25. Offline Entitlement Cache

Client cache:
- is account-scoped
- has bounded age
- uses secure storage where practical
- contains no provider secrets
- cannot update backend
- revalidates after reconnect
- eventually loses authority when stale

## 26. Offline Grace

Policy should define:
- eligibility
- max age since verification
- paid period end
- provider grace state
- clock-tampering considerations
- revalidation trigger

Exact duration remains configurable.

## 27. Clock Trust

Client wall-clock alone cannot permanently extend premium.

Use server-verified timestamps and bounded local logic.

Suspicious clock changes may trigger online revalidation.

## 28. Product Mapping

```text
Provider Product ID
→ Internal Plan ID
→ Entitlement Bundle
```

Server controls the mapping.

## 29. AI Quota Entitlement

AI quota service consumes normalized entitlement state, not raw purchase IDs.

Provider retries follow Phase 5 usage-accounting rules.

## 30. Rewarded Ad Integration

Rewarded benefits:
- are duplicate-safe
- are temporary
- are source-tagged
- expire independently
- cannot impersonate paid subscription truth

## 31. Manual Overrides

Avoid in MVP.

If later introduced:
- privileged role
- explicit reason
- bounded scope
- expiry where appropriate
- audit trail
- separate source from provider history

## 32. Failure Handling

### Provider unavailable
Preserve recently verified access only within bounded grace.

### Invalid purchase
Do not grant.

### DB partial failure
Rollback or reconcile.

### Webhook auth failure
Reject and audit safely.

### Duplicate
Return idempotent result.

### Unknown provider response
Fail closed for new grants.

## 33. Audit Events

Record:
- verification attempt/result
- entitlement grant/revoke
- restore
- renewal
- cancellation
- expiry
- refund/revocation
- webhook result
- reconciliation
- manual override

Avoid unnecessary payment-sensitive payload storage.

## 34. Data Retention

Retain billing/audit data only for:
- entitlement correctness
- fraud/security
- provider reconciliation
- support
- applicable legal/accounting obligations

Raw provider/webhook payload retention is bounded.

## 35. API Concepts

```text
POST /v1/billing/verify
POST /v1/billing/restore
GET  /v1/entitlements
POST /v1/billing/webhooks/{provider}
```

Exact API design follows Phase 3 versioning rules.

## 36. Testing

Required cases:
- valid purchase
- pending purchase
- invalid token
- wrong product
- replayed transaction
- duplicate webhook
- out-of-order webhook
- renewal
- cancellation
- grace
- expiration
- refund
- revocation
- restore
- stale offline cache
- cross-device
- account switch
- patched-client attempt
- DB partial failure/reconciliation

## 37. Operational Metrics

Monitor:
- verification success/failure
- provider latency/outage
- webhook authentication failures
- replay/duplicates
- entitlement transitions
- reconciliation backlog
- restore failures
- suspicious grant patterns

No note content belongs in billing telemetry.

## 38. Non-Goals

Does not define:
- exact prices
- card storage
- custom gateway
- tax accounting system
- enterprise invoicing
- crypto billing
- final desktop payment provider

## 39. Consolidated Review Criteria

Before final freeze verify:
- state model matches Phase 2 V2
- trust boundary matches Phase 3 V2
- purchase/pending/expiry UX matches Phase 4 V2
- AI quota coupling matches Phase 5 V2
- privacy/audit retention matches Phase 7
- lifecycle tests map to Phase 8
- production webhook/reconciliation maps to Phase 10

Phase 6 remains **Draft** until final project freeze.
