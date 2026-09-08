# Phase 05 — AI & Intelligence

**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending

Phase 5 defines how Smart Sticky Wallpaper uses deterministic intelligence and optional AI without making AI authoritative or required for core product behavior.

## Artifacts

- `AI_ARCHITECTURE.md` — provider-neutral AI gateway, request lifecycle, structured outputs, privacy/security, cost, observability, evaluation, model evolution.
- `INTELLIGENCE_RULES.md` — eligibility, hard rules, scoring, repetition control, reminders, explanations, feedback, personalization, offline behavior.

## Core Invariants

1. Explicit user intent outranks AI.
2. Hard rules are evaluated before AI.
3. AI output is untrusted until validated.
4. Core notes work without AI.
5. Authored and AI-derived data remain distinct.
6. Consequential inferred actions require confirmation.
7. Hidden/deleted/snoozed state cannot be overridden by AI.
8. AI does not own authorization, billing, deletion, or sync truth.
9. External AI requests are minimized and privacy-controlled.
10. Provider/model replacement does not redefine domain behavior.

## Relationship to Other Phases

```text
Phase 2 Requirements
→ Phase 3 AI/Provider Boundary
→ Phase 4 AI UX
→ Phase 5 Rules & AI Architecture
→ Phase 6 Quotas/Entitlements
→ Phase 7 Security/Privacy
→ Phase 8 Evaluation/Testing
→ Phase 10 Rollout/Operations
```

## Freeze Policy

Phase 5 is not frozen independently.

Before final freeze:
- reconcile with Phase 2–4 V2,
- verify monetization/privacy/testing/operations alignment,
- resolve remaining provider/model implementation decisions,
- then include Phase 5 in consolidated freeze.
