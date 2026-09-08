# Phase 04 — UX/UI Specification

**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending

Phase 4 defines how users experience Smart Sticky Wallpaper across mobile and desktop platforms while preserving local-first behavior, user control, accessibility, privacy, and platform honesty.

## Artifacts

- `UX_SPECIFICATION.md` — interaction model, IA, state/recovery behavior, accessibility, platform-aware UX, AI/monetization/privacy UX.
- `UI_DESIGN_SYSTEM.md` — semantic tokens, note/canvas components, visual states, responsive behavior, accessibility foundations, QA rules.
- `USER_FLOWS.md` — implementation-ready journeys including offline, sync conflicts, AI, subscriptions, ads, export, deletion, permissions, and fallbacks.

## Core UX Invariants

1. Fast note creation is local-first.
2. AI does not block or impersonate authored content.
3. Explicit user intent wins over inference.
4. Hidden/contextually dismissed notes remain retrievable.
5. Platform limitations are shown honestly.
6. Ads do not interrupt trust-critical workflows.
7. Subscription expiry never hides user-owned notes.
8. Every essential spatial action has an accessibility strategy.
9. Provider/network failure preserves user work.
10. Privacy-sensitive behavior is explained specifically.

## Relationship to Architecture

Phase 4 consumes the Phase 3 capability registry and local-first architecture rather than inventing platform capabilities.

```text
UX Intent
→ Capability Check
→ Native / Limited / Fallback / Unsupported State
→ User-Understandable Experience
```

## Freeze Policy

Phase 4 remains Draft until the final consolidated cross-phase review.

Before freeze, reconcile against:

- Phase 2 SRS V2,
- Phase 3 Architecture V2,
- Phase 5 AI rules,
- Phase 6 Monetization,
- Phase 7 Security/Privacy,
- Phase 8 test coverage.
