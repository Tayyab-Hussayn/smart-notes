# Product Vision

**Project:** Smart Sticky Wallpaper  
**Phase:** 01 — Product Discovery  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define the product's enduring vision, target users, value proposition, experience invariants, MVP boundary, and success definition.  
**Governed By:** `00-project-governance/PROJECT_CONSTITUTION.md`

---

## 1. Product Vision

Smart Sticky Wallpaper is a cross-platform personal visual information surface that keeps the user's most useful notes, reminders, routines, and contextual guidance visible at the moment they matter.

The product moves important information out of passive storage and into an intentionally calm, persistent visual surface.

It should feel like a personal canvas that is:

- immediately useful,
- visually understandable,
- local-first,
- private by design,
- controllable by the user,
- context-aware without becoming intrusive,
- progressively intelligent rather than AI-dependent.

---

## 2. Core Problem

Traditional note and productivity tools primarily optimize for **capture and retrieval**.

The user must still remember:

- that the information exists,
- where it was stored,
- when to reopen the application,
- which note matters now.

This creates a visibility gap:

> Important information can be successfully captured and still fail to influence the user's behavior because it is not visible at the right moment.

Smart Sticky Wallpaper exists to reduce that gap.

---

## 3. Product Promise

> **Write it once. See the right thing at the right time.**

The system should help users keep relevant information in sight while avoiding notification overload and unnecessary manual organization.

The product must never require AI to fulfill this promise.

---

## 4. Primary Value Proposition

The product combines four capabilities that are normally separated:

1. **Fast capture** — create a note with minimal friction.
2. **Persistent visual presence** — useful information remains naturally visible.
3. **Contextual prioritization** — the system can elevate what matters now.
4. **User control** — the user can always override, hide, pin, edit, dismiss, or disable automation.

The differentiation is the combination, not any one capability alone.

---

## 5. Target Users

### Primary Users

People who frequently manage small pieces of actionable or useful information, including:

- students,
- professionals,
- routine-driven users,
- visual organizers,
- people who create many short reminders or notes.

### Strong Behavioral Fit

The product is especially suited to users who:

- frequently forget notes after creating them,
- dislike repeatedly opening productivity apps,
- prefer visual spatial organization,
- want passive visibility without constant notifications,
- manage recurring routines or time-sensitive reminders.

### Secondary Users

- creators,
- planners,
- habit-focused users,
- desktop productivity users,
- users who want a customizable personal information surface.

---

## 6. Core Jobs to Be Done

### JTBD-01 — Capture
When I think of something important, I want to capture it quickly so I do not lose it.

### JTBD-02 — Remember
When something becomes relevant, I want it to become visible so I do not have to remember to search for it.

### JTBD-03 — Prioritize
When I have many notes, I want the system to help identify what matters now without taking control away from me.

### JTBD-04 — Organize Visually
When I arrange information spatially, I want the layout to persist so the surface remains familiar.

### JTBD-05 — Trust Automation
When the system interprets my notes, I want consequential actions to remain understandable, reversible, and under my control.

---

## 7. Core Experience

The intended experience is:

```text
Capture
→ Persist Locally
→ Place / Organize
→ Optional Reminder or Intelligence
→ Surface Relevant Information
→ User Acts / Dismisses / Snoozes / Pins
→ System Learns Only Within Approved Boundaries
```

The core experience must remain useful even when:

- offline,
- AI is disabled,
- an AI provider is unavailable,
- ads fail,
- billing verification is temporarily unavailable.

---

## 8. Experience Invariants

The following should remain true across the product:

1. User-authored information remains distinguishable from machine-derived information.
2. Explicit user intent outranks inferred intent.
3. Important content is never silently deleted by intelligence.
4. Existing user data is not held hostage by monetization.
5. Unsupported OS capabilities degrade honestly.
6. Core note operations remain local-first.
7. Contextual surfacing should reduce friction, not create distraction.
8. Automation remains explainable enough for the user to understand why something surfaced.

---

## 9. MVP Product Boundary

### Included

The MVP should provide:

- account/authentication foundation,
- note create/read/edit/delete,
- archive and duplicate,
- visual sticky-note canvas,
- drag-and-drop placement,
- note size/appearance controls,
- opacity/visibility controls,
- time/date reminders,
- local-first storage,
- offline operation,
- cross-device synchronization foundation,
- deterministic intelligent prioritization,
- limited AI assistance behind provider-neutral boundaries,
- persistent home/desktop surfaces where supported,
- platform-specific fallbacks where required,
- notification support,
- settings and privacy controls,
- subscription entitlement foundation,
- controlled advertising for eligible free users,
- diagnostics and production-safe error handling.

---

## 10. Explicit MVP Non-Goals

The initial MVP does not require:

- fully autonomous personal agents,
- social/community features,
- enterprise administration,
- advanced collaborative editing,
- unrestricted background execution,
- identical lock-screen behavior across platforms,
- mandatory cloud AI,
- complex long-term behavioral profiling,
- calendar/location/voice integrations,
- a web companion,
- a marketplace.

These require later product decisions.

---

## 11. Intelligence Position

Intelligence is a supporting capability, not the product identity.

Priority order:

```text
Explicit User Intent
>
Hard Product Rules
>
Deterministic Context
>
AI Assistance
```

AI may help with:

- classification,
- semantic date/time interpretation,
- routine suggestions,
- relevance estimation,
- summarization,
- organization suggestions.

Consequential inferred actions must follow the confirmation policy defined in later specifications.

---

## 12. Cross-Platform Vision

Target platforms:

- Android
- iOS
- Windows
- macOS
- Linux

The product must preserve the **experience goal**, not force identical OS behavior.

Each capability may be:

- natively supported,
- supported through an alternative,
- degraded through a documented fallback,
- explicitly unavailable.

Platform restrictions must be treated as product reality rather than implementation failure.

---

## 13. Technology Direction

The approved high-level direction is:

- Kotlin Multiplatform for shared client logic,
- Compose Multiplatform where practical,
- native platform APIs for OS-specific capabilities,
- Rust only where materially justified,
- FastAPI backend,
- PostgreSQL,
- Docker/VPS deployment,
- provider-neutral AI,
- modular billing and advertising boundaries.

Detailed architecture belongs to Phase 3 and approved ADRs.

---

## 14. Monetization Vision

The product may use:

- useful free tier,
- controlled advertising for eligible free users,
- paid subscription,
- optional rewarded ads,
- future advanced AI or premium capabilities.

Product invariant:

> Subscription state may limit premium functionality, but must not remove access to existing user-owned notes.

Exact pricing and limits remain configurable business decisions.

---

## 15. Privacy & Trust Vision

Trust is part of the product value.

The product should:

- minimize unnecessary data collection,
- keep note content out of general telemetry,
- make external AI processing controllable,
- support export and deletion,
- keep secrets out of clients,
- avoid hidden behavioral surveillance,
- clearly separate user-authored and AI-derived state.

---

## 16. Success Definition

The product succeeds when users reliably capture information and later encounter it at useful moments with less effort than traditional note retrieval.

Key product measures may include:

- first-note activation,
- first successful surface placement,
- reminder usefulness,
- contextual-surface usefulness,
- retention,
- dismissal/snooze patterns,
- sync reliability,
- crash-free usage,
- premium conversion,
- ad impact on retention.

Metrics must remain privacy-conscious.

---

## 17. Product Principles

1. Useful before clever.
2. Visibility before complexity.
3. User control over automation.
4. Deterministic behavior before AI.
5. Local-first core experience.
6. Platform-native behavior over fake uniformity.
7. Private content is not telemetry.
8. Monetization must not damage core trust.
9. Every critical behavior must be testable.
10. Extensibility must not make the architecture fragile.

---

## 18. Future Direction

Possible future capabilities:

- advanced routine intelligence,
- natural-language creation,
- semantic search,
- calendar/task integrations,
- voice capture,
- on-device AI,
- advanced personalization,
- dynamic wallpapers,
- web companion,
- optional collaboration,
- third-party integrations.

These are directions, not commitments.

---

## 19. Phase 1 Review Criteria

Before final consolidated freeze, verify that:

- the product problem is still valid,
- target users remain appropriate,
- the differentiation remains clear,
- MVP scope matches later requirements,
- platform expectations match Phase 3/4 decisions,
- intelligence positioning matches Phase 5,
- monetization promise matches Phase 6,
- privacy promise matches Phase 7,
- no later phase contradicts this vision.

Phase 1 remains **Draft** until the project-wide consolidated freeze.
