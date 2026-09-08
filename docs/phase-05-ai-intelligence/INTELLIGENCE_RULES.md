# Intelligence Rules

**Project:** Smart Sticky Wallpaper  
**Phase:** 05 — AI & Intelligence  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define deterministic and AI-assisted rules for note eligibility, prioritization, resurfacing, repetition control, feedback, reminders, notification boundaries, and contextual personalization.  
**Governed By:** `PROJECT_CONSTITUTION.md` and `AI_ARCHITECTURE.md`

---

## 1. Core Question

> What information is most useful to the user right now, without becoming intrusive or overriding explicit intent?

The answer must remain:

- explainable,
- reversible,
- deterministic where possible,
- privacy-aware,
- bounded by user controls.

---

## 2. Decision Pipeline

```text
Collect Allowed Signals
→ Determine Eligibility
→ Apply Hard Rules
→ Compute Deterministic Score
→ Shortlist Candidates
→ Optional AI Enrichment
→ Reapply Hard/Safety Rules
→ Apply Repetition Policy
→ Select Surface Candidates
→ Render Explanation
→ Capture User Feedback
```

AI never runs before hard eligibility rules.

---

## 3. Eligibility

A note is eligible only when:

- it exists,
- it is owned by the active user,
- it is not deleted,
- it is not archived unless explicitly allowed,
- it is not hidden,
- its intelligence/surfacing setting permits it,
- it is not under authoritative snooze suppression,
- completion state does not make it inactive,
- target surface/capability can represent it.

---

## 4. Hard Rules

Hard rules override scoring and AI.

- Deleted notes never surface.
- Hidden notes never surface.
- Archived notes do not surface unless explicitly configured.
- Snoozed items remain suppressed until snooze expires.
- Completed one-time reminders do not normally resurface.
- Explicit user schedules outrank inferred schedules.
- Pinned/explicit priority is never silently downgraded by AI.
- Intelligence-disabled notes are excluded from contextual surfacing.
- Prohibited/private context is not used.
- Cross-account notes can never enter the candidate set.

---

## 5. Signal Classes

### Explicit Signals
- pin,
- manual priority,
- reminder time,
- recurrence,
- snooze,
- completion,
- visibility,
- user-defined category,
- manual intelligence preference.

### Deterministic Context
- current time,
- day/date,
- reminder proximity,
- recurrence match,
- configured routine window,
- recent user interaction,
- recent dismissal/snooze.

### Optional AI Signals
- semantic relevance,
- inferred category,
- routine hypothesis,
- context interpretation,
- similarity/grouping.

---

## 6. Precedence

```text
Explicit User State
>
Hard Domain Rules
>
Deterministic Context
>
AI Signal
```

This precedence is invariant.

---

## 7. Time Relevance

Time relevance may include:

- exact reminder due time,
- lead window,
- overdue state,
- semantic daypart,
- day-of-week,
- recurrence occurrence,
- date proximity.

Suggested default dayparts:

```text
Morning   05:00–11:59
Afternoon 12:00–16:59
Evening   17:00–21:59
Night     22:00–04:59
```

These are configurable defaults, not product truth.

---

## 8. Reminder Urgency

Reminder urgency should increase as a valid reminder approaches.

Conceptual bands may include:

- future,
- upcoming,
- near due,
- due,
- overdue.

Urgency must not bypass hidden/snoozed/completed rules.

---

## 9. Deterministic Score

Conceptual model:

```text
score =
    explicit_priority
  + reminder_urgency
  + time_relevance
  + recurrence_relevance
  + configured_context_relevance
  + recent_usefulness
  + optional_ai_relevance
  - repetition_penalty
  - dismissal_penalty
```

Weights are configurable and must be tested.

---

## 10. Shortlisting

Do not send every note to AI.

Preferred order:

1. hard eligibility,
2. deterministic scoring,
3. shortlist,
4. optional AI enrichment,
5. final hard-rule validation,
6. surface selection.

This protects privacy, latency, and cost.

---

## 11. Repetition Control

The system should support:

- per-note cooldown,
- consecutive-display limit,
- daily resurfacing limit,
- dismissal penalty,
- “show less often” feedback,
- snooze suppression,
- optional reset after meaningful state change.

Pinned notes may intentionally bypass some repetition reduction, but explicit user choice should be clear.

---

## 12. Dismissal Semantics

Dismissal means:

- remove current contextual emphasis,
- preserve underlying note,
- record feedback,
- reduce immediate resurfacing.

Dismissal does not mean:

- delete,
- archive,
- complete,
- permanently hide.

---

## 13. Snooze Semantics

Snooze is authoritative temporary suppression.

Until snooze expiry:

- contextual surface suppressed,
- reminder behavior follows reminder policy,
- AI ranking cannot override it.

---

## 14. Completion Semantics

### One-Time Reminder
Completion ends normal reminder resurfacing.

### Recurring Reminder
Completion applies to the current occurrence; recurrence remains.

### Plain Note
Completion is not inferred unless product state explicitly supports task completion.

---

## 15. Pinning

Pin is explicit user priority.

Pinned notes:

- receive deterministic priority,
- remain user-controlled,
- are never silently unpinned,
- may still obey visibility/deletion rules.

---

## 16. Hide vs Archive vs Dismiss

These states are distinct:

| Action | Meaning |
|---|---|
| Hide | Remove from active surface |
| Archive | Remove from active collection workflow |
| Dismiss | Remove current contextual emphasis |
| Snooze | Suppress until time |
| Delete | Remove through deletion lifecycle |

Implementation and UX must not conflate them.

---

## 17. Surface vs Notification

### Surface
Passive visual presentation.

### Notification
Interruptive OS-level alert.

Default policy:

- contextual intelligence may influence surface selection,
- notifications should primarily follow explicit reminder or user-approved policy,
- AI relevance alone does not normally create interruptive notifications.

---

## 18. Explanation Rules

Each contextual surface decision should have a concise reason code where practical.

Examples:

- `due_soon`
- `pinned`
- `morning_match`
- `recurring_today`
- `routine_suggestion`
- `semantic_relevance`

UX maps reason codes to human-readable text.

Do not expose raw model reasoning.

---

## 19. Feedback

Supported feedback may include:

- helpful,
- not helpful,
- show more often,
- show less often,
- dismiss,
- snooze,
- pin,
- disable intelligence for this note,
- disable AI,
- disable contextual intelligence.

Feedback affects ranking, not authored content.

---

## 20. Personalization

MVP personalization should primarily use explicit behavior:

- pins,
- preferred reminder windows,
- categories,
- snooze behavior,
- dismissals,
- intelligence settings,
- explicit routine configuration.

Advanced learned behavioral profiles are deferred.

---

## 21. Routine Detection

A routine hypothesis may be generated from repeated patterns.

It must:

- remain a suggestion,
- expose an understandable reason,
- require confirmation before creating durable recurrence when not explicitly requested,
- be removable/rejectable,
- not use prohibited context.

---

## 22. Routine Confidence

Routine confidence should consider:

- number of consistent observations,
- temporal consistency,
- explicit user interactions,
- contradiction rate,
- recency.

Weak evidence should never silently create permanent automation.

---

## 23. Context Sources

Allowed baseline context:

- time,
- date/day,
- reminder state,
- explicit category,
- explicit routine config,
- current app state,
- recent interaction metadata,
- platform capability.

Restricted future context:

- location,
- calendar,
- activity,
- contacts,
- voice,
- external services.

Restricted sources require separate approval and privacy review.

---

## 24. Offline Behavior

Offline mode continues:

- hard rules,
- deterministic scoring,
- local reminder behavior where platform permits,
- cached approved derived metadata,
- note CRUD,
- outbox queueing.

Cloud AI is skipped.

---

## 25. AI Enrichment

AI may add a bounded relevance signal to shortlisted candidates.

AI cannot:

- make hidden/deleted notes eligible,
- bypass snooze,
- override explicit reminder schedule,
- grant notification permission,
- create permanent recurrence without policy confirmation,
- alter entitlement,
- access another account's notes.

---

## 26. Confidence Handling

Suggested product mapping:

- **High** → show/prefill suggestion if policy allows,
- **Medium** → ask for confirmation where consequential,
- **Low** → avoid consequential action; possibly omit suggestion.

Confidence is not the sole safety mechanism.

---

## 27. Example — Morning Walk

Input:

> “Morning walk tomorrow”

Flow:

1. deterministic parser detects relative date,
2. semantic “morning” may be interpreted by rules or AI,
3. proposed reminder shown,
4. user confirms/edits if needed,
5. reminder becomes authoritative,
6. note surfaces during relevant window,
7. user may snooze/complete/dismiss/pin,
8. one-time completion ends normal resurfacing.

---

## 28. Example — Exercise Routine

Repeated evening interaction may produce:

> “You often use this around 7 PM. Make it a recurring reminder?”

Until accepted:

- no permanent recurrence is created,
- the suggestion may be dismissed,
- future suggestion frequency follows repetition policy.

---

## 29. Example — Pinned Note

A pinned note may remain highly ranked even without current contextual relevance.

However:

- hidden still wins,
- deleted still wins,
- explicit user unpin changes priority,
- notification policy remains separate.

---

## 30. Example — Repeated Dismissal

If a contextual note is dismissed repeatedly:

- apply increasing repetition penalty,
- reduce surfacing frequency,
- optionally offer “show less often”/disable controls,
- never modify note content.

---

## 31. Candidate Limits

Surface selection should be bounded.

Configuration may define:

- max active contextual suggestions,
- max suggestions per refresh,
- minimum score threshold,
- cooldown windows.

The system should prefer fewer useful notes over many weakly relevant notes.

---

## 32. Refresh Triggers

Re-evaluation may occur on meaningful events such as:

- time-window transition,
- reminder state change,
- note edit,
- pin/hide/snooze/completion,
- app foreground,
- sync reconciliation,
- settings change.

Avoid wasteful continuous reevaluation.

---

## 33. Privacy Rules

Intelligence must:

- use only approved context,
- respect AI opt-out,
- avoid hidden profiling,
- avoid sending all notes externally,
- isolate accounts,
- support deletion of persisted AI-derived metadata where required.

---

## 34. Security Rules

Intelligence must not:

- surface another user's data,
- trust note text as system instructions,
- bypass authorization,
- bypass visibility/deletion policy,
- change billing/subscription truth,
- execute unrestricted tools/actions.

---

## 35. Configuration

Potential configuration:

- intelligence enabled,
- AI enabled,
- daypart windows,
- relevance threshold,
- max candidates,
- cooldown,
- repetition penalty,
- confidence thresholds,
- routine detection,
- notification policy,
- ranking weights,
- feature rollout.

Configuration cannot weaken hard security/data-integrity rules.

---

## 36. Evaluation Metrics

Useful product metrics may include:

- suggestion acceptance,
- dismissal rate,
- snooze rate,
- repeat-dismissal rate,
- surface open rate,
- false/incorrect reminder inference reports,
- AI fallback frequency.

Metrics must remain privacy-safe.

---

## 37. Testing

Tests should cover:

- hidden suppression,
- deleted suppression,
- snooze,
- completion,
- recurrence,
- pinning,
- daypart matching,
- overdue reminders,
- repeated dismissal,
- max suggestion limit,
- offline mode,
- AI failure,
- low confidence,
- privacy opt-out,
- notification boundary,
- cross-account isolation.

---

## 38. Non-Goals

Phase 5 does not authorize:

- unrestricted behavioral surveillance,
- silent location inference,
- autonomous personal agents,
- intrusive notification generation,
- opaque ranking with no controls,
- permanent habit modeling without consent,
- AI-only ranking for basic cases.

---

## 39. Consolidated Review Criteria

Before final freeze verify:

- rules map to Phase 2 SRS V2,
- architecture matches Phase 3 V2,
- UX states match Phase 4 V2,
- entitlement/quota rules align with Phase 6,
- privacy/security rules align with Phase 7,
- tests map to Phase 8,
- rollout/observability align with Phase 10.

Phase 5 remains **Draft** until final project freeze.
