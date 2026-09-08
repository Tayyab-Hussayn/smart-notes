# Phase 06 — Monetization

**Version:** 2.0
**Status:** Draft — Consolidated Review Pending

Phase 6 defines how Smart Sticky Wallpaper monetizes sustainably while preserving user data, privacy, accessibility, and the core note-taking experience.

## Artifacts

- `MONETIZATION_SPECIFICATION.md` — free/premium rules, named entitlements, downgrade safety, AI quota principles, pricing/paywall behavior.
- `ENTITLEMENT_BILLING_SPEC.md` — provider verification, normalized subscription states, entitlement resolution, webhooks, reconciliation, offline cache, fraud resistance.
- `ADVERTISING_SPEC.md` — ad eligibility, provider boundary, placement rules, frequency, rewarded integrity, privacy, accessibility, kill switches.

## Core Invariants

1. Existing user-owned notes are never held hostage by payment state.
2. Client state cannot permanently grant premium.
3. Backend entitlements derive from verified commercial truth.
4. Ads do not interrupt critical note/reminder/account workflows.
5. Private note content is not advertising data.
6. Rewarded ads are optional and never create permanent paid subscription truth.
7. AI quotas are server-enforced.
8. Provider failure does not break core notes.
9. Account switching cannot leak entitlement/reward state.
10. Pricing and plan composition can evolve without rewriting feature domains.

## Cross-Phase Relationship

```text
Phase 2 Requirements
→ Phase 3 Architecture / Trust Boundaries
→ Phase 4 Paywall & Ad UX
→ Phase 5 AI Quotas
→ Phase 6 Commercial Rules
→ Phase 7 Security / Privacy
→ Phase 8 Billing / Ad Tests
→ Phase 10 Production Provider Setup
```

## Freeze Policy

Phase 6 remains Draft until project-wide consolidated review.

Before final freeze:
- validate final entitlement keys
- verify provider-specific state mappings
- finalize offline grace policy
- validate ad format/desktop strategy
- reconcile with Phase 7–10
- include Phase 6 in consolidated freeze
