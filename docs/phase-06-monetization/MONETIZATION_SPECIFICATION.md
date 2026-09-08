# Monetization Specification

**Project:** Smart Sticky Wallpaper
**Phase:** 06 — Monetization
**Version:** 2.0
**Status:** Draft — Consolidated Review Pending
**Purpose:** Define free/premium product rules, entitlement-based access, AI monetization, upgrade/downgrade behavior, paywalls, pricing flexibility, and data-access guarantees.

## 1. Monetization Objective

Monetization should fund the product without damaging the core promise:

> Users retain reliable access to their own notes while premium unlocks convenience, scale, intelligence, customization, and an ad-free experience.

## 2. Core Principles

1. Free remains useful.
2. Premium adds meaningful value.
3. Existing user-owned data remains accessible after subscription loss.
4. Billing-provider truth and entitlement truth are separate.
5. Clients cannot self-grant premium.
6. Feature access uses named entitlements.
7. Pricing and plan composition remain configurable.
8. Ads never become a dependency for core functionality.
9. AI monetization is transparent and server-enforced.
10. Paywalls do not block owned data.
11. Platform/store rules are respected.
12. Monetization state is recoverable across supported device/account changes.

## 3. Initial Model

Supports:
- useful free tier
- premium subscription
- controlled ads for eligible free users
- optional rewarded ads
- monthly/yearly plans

Possible future models require separate approval:
- trials
- introductory offers
- regional/student offers
- AI packs
- family/team plans
- lifetime license
- premium-plus tier

Exact prices are not frozen here.

## 4. Free Tier Contract

Free should include meaningful access to:
- note CRUD
- local-first storage
- basic canvas
- basic reminders
- basic deterministic intelligence
- basic customization
- standard notifications
- account access
- supported sync foundation
- limited AI where enabled
- existing-data access/export/delete

Free restrictions may apply to:
- advanced AI
- advanced intelligence
- premium themes/styles
- extended sync/device limits
- advanced reminders
- premium surfaces/integrations
- ad-free status

Limits must be configuration/entitlement driven.

## 5. Premium Value Contract

Premium may include:
- `feature.ads.remove`
- higher AI allowance
- advanced AI capabilities
- advanced contextual intelligence
- premium note themes/styles
- advanced reminders
- extended sync/device allowance
- premium surfaces where supported
- advanced personalization
- future integrations

Premium does not redefine ownership of user data.

## 6. Existing Data Guarantee

When premium becomes unavailable:
- notes remain readable
- notes remain editable
- notes remain searchable
- export remains available
- deletion remains available
- account deletion remains available
- no user-owned data is deleted
- premium-only creation/actions may be restricted
- premium-only automation may pause
- ads may resume if eligible

This is a product invariant.

## 7. Over-Limit Downgrade

If a downgraded account exceeds a free limit:
- preserve existing resources
- optionally block additional restricted creation
- allow deletion/reduction
- allow export
- explain limit clearly
- restore full behavior after valid upgrade

Never delete data to enforce a free-tier limit.

## 8. Entitlement Model

Baseline keys:

```text
feature.ai.basic
feature.ai.advanced
feature.ads.remove
feature.theme.premium
feature.sync.extended
feature.reminder.advanced
feature.surface.premium
```

Additional entitlements require centralized definition.

Do not scatter `isPremium` checks through product code.

## 9. Plan vs Entitlement

```text
Commercial Plan
→ Entitlement Bundle
→ Feature Access
```

Feature modules inspect entitlement keys, not store product IDs.

## 10. Subscription Lifecycle

Product-level states support:

```text
Inactive
→ Purchase Initiated
→ Pending Verification
→ Active
→ Renewed / Grace / Cancelled Pending Expiry
→ Expired / Revoked / Refunded
→ Restored or Inactive
```

Technical normalization belongs in `ENTITLEMENT_BILLING_SPEC.md`.

## 11. Upgrade Behavior

On successful upgrade:
1. approved billing flow completes
2. backend verifies purchase
3. normalized state updates
4. entitlements resolve
5. client receives fresh snapshot
6. premium features unlock
7. ads stop where ad-removal applies

Avoid requiring restart where possible.

## 12. Pending Verification

If client purchase UI succeeds but server verification is pending:
- do not permanently grant premium
- show pending state
- retry/reconcile safely
- preserve previous entitlement until verified
- provide restore/retry path

## 13. Cancellation

Cancellation normally disables renewal.

Until verified paid period end:
- premium stays active
- user data is unchanged
- entitlement does not end early unless provider truth says so

## 14. Expiration

After verified expiration:
- premium entitlements deactivate
- free-tier behavior resumes
- existing data remains available
- ads may resume if eligible
- AI quota may return to free allowance
- premium automation may pause
- downgrade explanation should be clear

## 15. Refund / Revocation

After verified refund/revocation:
- backend updates commercial state
- applicable premium entitlements revoke
- existing user data remains
- audit record is retained
- client refreshes state

## 16. Restore Purchase

Restore must:
- use applicable provider
- verify server-side
- rebuild entitlements
- work across reinstall/device/account where policy permits
- be idempotent
- avoid duplicate subscription records

## 17. Cross-Device Entitlement

Where policy permits, premium attaches to the authenticated user account.

New device:

```text
Authenticate
→ Fetch Entitlement Snapshot
→ Reconcile Provider State if Needed
→ Apply Entitlements
```

Entitlement cache never crosses accounts.

## 18. Offline Entitlement Policy

Client may cache:
- entitlement version
- verified timestamp
- expiry/grace metadata
- feature keys

Rules:
- temporary offline premium may be allowed
- stale cache cannot grant indefinite access
- reconnect triggers revalidation
- client cache cannot modify backend truth

Exact grace window remains configurable.

## 19. AI Monetization

Free may receive:
- limited requests
- limited task set
- lower-cost model routing
- optional rewarded top-ups

Premium may receive:
- higher quota
- advanced tasks
- higher-cost models where justified
- advanced contextual intelligence

AI quotas are enforced server-side.

## 20. AI Usage Accounting

Do not unfairly multiply one user action because of:
- provider retry
- malformed provider output
- duplicate replay
- infrastructure retry

Usage accounting should be logical-action based and auditable.

## 21. AI Quota UX

Where useful, users should understand:
- whether a feature consumes AI quota
- remaining allowance/exhaustion
- reset timing
- premium impact on limits

Avoid hidden depletion.

## 22. Pricing

Displayed prices come from billing provider/store where applicable.

Support:
- currencies
- platform-specific prices
- taxes handled by provider/store where applicable
- regional pricing
- promotions
- monthly/yearly differences

Do not hardcode final prices into product logic.

## 23. Paywall Entry Rules

Good trigger:
```text
User requests premium feature
→ Explain value
→ Show plan
```

Bad triggers:
- during typing
- opening existing notes
- reminder confirmation
- account deletion
- urgent access
- immediate repeated prompt after dismissal

## 24. Paywall Content

Should show:
- feature value
- current plan/status
- provider price
- billing period
- trial/intro terms if applicable
- restore
- manage/cancel path
- clear CTA
- legal/subscription disclosures

Avoid misleading “unlimited” claims.

## 25. Upgrade Prompt Frequency

Upgrade prompts should have cooldown/frequency controls.

Repeated dismissals reduce immediate re-prompting.

No fake urgency or countdowns.

## 26. Monetization UX Invariants

Never:
- block existing notes behind payment
- delete content after downgrade
- disguise ads as notes
- obscure price conditions
- make restore intentionally difficult
- claim premium before verification
- hide required subscription-management routes

## 27. Advertising Relationship

Advertising details are defined in `ADVERTISING_SPEC.md`.

At product level:
- eligible free users may see ads
- ad-removal entitlement suppresses ads
- ad failure never blocks notes
- ad providers never receive private note content
- rewarded ads remain optional

## 28. Analytics

Privacy-safe events may include:
- paywall viewed
- plan selected
- purchase initiated
- verification success/failure
- entitlement activated
- restore started/completed
- renewal
- cancellation state
- expiry
- refund/revocation
- rewarded ad completion

Do not attach private note content.

## 29. Abuse Resistance

Resist:
- patched clients
- forged entitlement flags
- replayed transactions
- duplicate webhooks
- fake reward completion
- modified local entitlement cache

Sensitive decisions remain server-side.

## 30. Platform Differences

Billing differs across Android, iOS, Windows, macOS, and Linux.

Shared product sees normalized plan/entitlement concepts.

Desktop direct billing is not assumed and requires explicit provider/policy approval.

## 31. Configuration Safety

Remote configuration may alter business values but cannot:
- bypass server verification
- grant unauthorized entitlement
- weaken account ownership
- make client state authoritative

## 32. MVP Boundary

MVP requires:
- useful free tier
- premium subscription foundation
- named entitlements
- server verification
- restore
- downgrade safety
- bounded offline entitlement cache
- ad-removal entitlement
- controlled ads where supported
- optional rewarded ads if implemented
- privacy-safe analytics

## 33. Consolidated Review Criteria

Before final freeze verify:
- feature entitlements match Phase 2 V2
- trust boundaries match Phase 3 V2
- paywall/downgrade UX matches Phase 4 V2
- AI quotas match Phase 5 V2
- billing/ad privacy matches Phase 7
- lifecycle tests map to Phase 8
- provider setup maps to Phase 10

Phase 6 remains **Draft** until final project freeze.
