# Product Discovery

**Project:** Smart Sticky Wallpaper  
**Phase:** 01 — Product Discovery  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Record the product problem, discovery evidence/hypotheses, key decisions, platform constraints, risks, assumptions, validation priorities, and decisions delegated to later phases.  
**Governed By:** `00-project-governance/PROJECT_CONSTITUTION.md`

---

## 1. Discovery Objective

Determine whether a persistent visual note surface with contextual prioritization is valuable enough to justify a cross-platform product, and identify the constraints that must shape the design before implementation.

Discovery is a decision record, not an implementation specification.

---

## 2. Problem Hypothesis

The central hypothesis is:

> Users often fail to act on captured information because conventional notes are stored out of sight and require deliberate retrieval.

The product aims to reduce this retrieval burden by bringing useful information into a persistent visual context.

---

## 3. User Friction

Likely friction patterns include:

- notes become buried,
- users forget to reopen note apps,
- reminders become noisy when everything becomes a notification,
- large collections are difficult to prioritize,
- widgets require manual maintenance,
- routines are repeated manually,
- users want persistent visibility without losing control of their screen.

These remain product hypotheses until supported by user or behavioral validation.

---

## 4. Desired Outcomes

Users should be able to:

- capture information quickly,
- keep useful information visually accessible,
- surface time-relevant notes automatically,
- organize notes spatially,
- remain productive offline,
- synchronize across supported devices,
- override automation,
- trust that inference will not silently damage authored information.

---

## 5. Representative Scenarios

### Scenario A — Explicit Reminder
User creates: “Walk tomorrow at 7 AM.”

Expected product direction:
- parse explicit timing deterministically where possible,
- create/schedule according to reminder policy,
- surface the note near the relevant time.

### Scenario B — Ambiguous Context
User creates: “Morning walk tomorrow.”

Expected product direction:
- interpret the semantic morning window,
- suggest a reminder if needed,
- require confirmation when the interpretation is consequential or ambiguous.

### Scenario C — Routine
User repeatedly uses an exercise note in the evening.

Expected direction:
- the system may identify a routine hypothesis,
- suggest recurrence or prioritization,
- avoid silently creating permanent automation from weak evidence.

### Scenario D — Visual Surface
User arranges notes spatially.

Expected direction:
- placement persists,
- layout remains familiar,
- unsupported OS surfaces use documented fallbacks.

### Scenario E — Temporary Focus
User hides, snoozes, dismisses, or lowers visibility without deleting information.

---

## 6. Validated Project Decisions

The current project direction is:

| Area | Decision |
|---|---|
| Primary interaction | Visual sticky-note information surface |
| Core data behavior | Local-first |
| Intelligence | Deterministic rules + optional AI |
| Shared client logic | Kotlin Multiplatform |
| Shared UI | Compose Multiplatform where practical |
| Native integration | Platform APIs |
| Backend | FastAPI |
| Database | PostgreSQL |
| Deployment | Docker on VPS |
| Architecture | Modular, API-first, capability-based |
| Monetization | Useful free tier + subscription + controlled ads |
| Development | Spec-driven + AI-agent-assisted |

Detailed implementation belongs to later phases and ADRs.

---

## 7. Product Differentiation

The strongest differentiation is not “AI notes.”

It is:

> **A personal visual information surface that continuously brings the most relevant user-controlled information into view.**

Competitive categories already solve pieces of the problem:

- note capture,
- reminders,
- widgets,
- sticky notes,
- wallpaper customization,
- AI productivity.

The opportunity is the integrated experience of persistent visibility + contextual relevance + visual control.

---

## 8. Platform Reality

Android, iOS, Windows, macOS, and Linux differ materially in:

- wallpaper APIs,
- lock-screen access,
- widgets,
- notifications,
- overlays,
- background scheduling,
- power management,
- desktop window behavior,
- billing,
- advertising.

Therefore the product must be capability-based.

A capability may be:

- native,
- alternative,
- fallback,
- unsupported with clear UX.

---

## 9. Critical Platform Risk

The original concept can easily become overdependent on unrestricted wallpaper/lock-screen behavior.

That is unacceptable.

Discovery decision:

> The product value must survive even when a platform cannot provide the ideal persistent surface.

Fallbacks may include:

- widgets,
- notifications,
- desktop surfaces,
- wallpaper updates,
- app-resident canvas,
- platform-approved live surfaces.

Exact capability behavior belongs to architecture and UX specifications.

---

## 10. Intelligence Discovery

Deterministic signals should handle explicit intent:

- reminder time,
- date,
- recurrence,
- pinning,
- priority,
- visibility,
- snooze/completion.

AI is more appropriate for:

- ambiguous language,
- semantic categories,
- routine suggestions,
- context estimation,
- summarization,
- relevance assistance.

Discovery conclusion:

> AI should increase usefulness without becoming a dependency for basic correctness.

---

## 11. Monetization Discovery

Current direction:

### Free
- meaningful core experience,
- controlled ads,
- bounded AI or premium limits where economically necessary.

### Premium
- ad-free,
- higher limits,
- advanced intelligence/customization,
- advanced capabilities as later defined.

### Optional Future
- advanced AI tier,
- paid integrations,
- expanded cloud features.

Exact prices and limits are intentionally not fixed in discovery.

---

## 12. Trust Discovery

Trust failures could destroy the product even if core functionality works.

High-risk trust failures include:

- AI silently changing important data,
- exposing private notes to third parties,
- aggressive ads,
- losing data during sync,
- locking existing notes behind subscription,
- claiming unsupported platform capabilities.

These are therefore product constraints, not merely technical concerns.

---

## 13. Key Risks

| Risk | Impact | Required Direction |
|---|---|---|
| Platform restrictions | Core surface unavailable | Capability matrix + fallbacks |
| Background limits | Delayed reminders | Native scheduling + honest guarantees |
| Sync conflicts | Data loss/inconsistency | Explicit conflict protocol |
| AI incorrectness | Wrong actions/priorities | Deterministic-first + confirmation |
| AI cost/outage | Feature degradation | Quotas + fallback |
| Privacy leakage | Loss of trust | Data minimization + provider boundaries |
| Aggressive monetization | Retention damage | UX restrictions |
| Agent swarm complexity | Architectural drift | Central orchestration + contracts |
| Cross-platform scope | Delivery risk | Shared core + staged capability validation |

---

## 14. Assumptions Requiring Validation

Important product assumptions:

1. Persistent visibility is materially more useful than ordinary note retrieval.
2. Contextual surfacing reduces forgotten tasks.
3. Users prefer passive visual surfacing to excessive notifications.
4. Users accept AI suggestions when control remains explicit.
5. Visual spatial arrangement provides meaningful organizational value.
6. Cross-device sync improves perceived product value.
7. Platform fallbacks still preserve enough of the product promise.
8. A useful ad-supported free tier can coexist with a trustworthy experience.
9. Premium intelligence/customization creates willingness to pay.

These should not be treated as proven facts merely because they appear in a specification.

---

## 15. Validation Priorities

Before or during early implementation, prioritize evidence for:

### P0 — Feasibility
- Android persistent-surface prototype,
- iOS fallback prototype,
- desktop surface prototype,
- reminder/background reliability,
- local-first persistence and sync.

### P1 — Product Value
- first-note capture friction,
- visual canvas usefulness,
- whether users revisit/surface notes,
- usefulness of contextual prioritization,
- notification-vs-passive-surface preference.

### P2 — Business
- acceptable ad placements,
- premium value drivers,
- AI usage economics.

---

## 16. Product Experiments

Useful early experiments:

- static visual canvas prototype,
- time-based note resurfacing,
- 10+ note prioritization test,
- offline create/edit/reconnect,
- two-device conflict prototype,
- reminder inference confirmation flow,
- platform capability proof-of-concepts.

Experiments should answer a decision, not merely demonstrate code.

---

## 17. Decisions Delegated to Later Phases

Phase 1 intentionally delegates details such as:

- exact requirements → Phase 2,
- architecture/ADRs → Phase 3,
- interaction design → Phase 4,
- AI behavior → Phase 5,
- entitlements/ads → Phase 6,
- security/privacy → Phase 7,
- test strategy → Phase 8,
- development workflow → Phase 9,
- release/operations → Phase 10.

Later phases may refine these areas without rewriting the core product vision.

---

## 18. Resolved vs Unresolved Questions

Many original discovery questions have since been resolved at specification level:

- AI consequential inference requires confirmation,
- monetization must preserve existing data,
- ads must avoid critical workflows,
- cross-platform behavior is capability-based,
- AI remains provider-neutral,
- testing/security are explicit project gates.

Still requiring ADR, prototype, benchmark, or business validation:

- exact local database technology,
- exact sync conflict algorithm,
- auth/session implementation,
- background worker implementation,
- platform-specific capability details,
- exact pricing/limits,
- final AI provider/model,
- measurable performance targets.

---

## 19. Discovery Anti-Goals

Do not:

- use AI as a marketing excuse without user value,
- promise impossible lock-screen behavior,
- treat every note as a notification,
- force identical UI behavior across operating systems,
- optimize monetization before core usefulness,
- let AI-generated implementation complexity dictate product scope,
- confuse a technical prototype with product validation.

---

## 20. Phase 1 Consolidated Review Criteria

Before final project freeze, verify:

- problem statement still matches product direction,
- target-user assumptions are explicit,
- differentiation remains defensible,
- scenarios match later reminder/AI rules,
- platform risks match architecture,
- risks map to later mitigation specs,
- old open questions are either resolved or intentionally deferred,
- no Phase 1 statement conflicts with Phases 2–10.

Phase 1 remains **Draft** until the final consolidated freeze.
