# User Flows

**Project:** Smart Sticky Wallpaper  
**Phase:** 04 — UX/UI Specification  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define implementation-ready user journeys, decision branches, recovery paths, accessibility alternatives, and platform fallbacks.  
**Governed By:** `UX_SPECIFICATION.md`

---

## 1. Flow Notation

```text
Action → Action
[Decision?]
  ├─ Yes → Path
  └─ No  → Path
```

Every critical failure branch must preserve user work where possible.

---

## 2. First Run

```text
Install
→ Launch
→ Product Promise
→ Example / Interactive Note
→ Explain Surface
→ Main Surface
```

Account and permissions should be requested only when useful.

---

## 3. First Persistent Surface Setup

```text
User chooses persistent surface
→ Check capability
  ├─ Supported → Explain permission/setup → Configure → Confirm
  ├─ Limited → Explain limitation → Configure supported mode
  ├─ Fallback → Offer fallback → Configure
  └─ Unavailable → Explain → Continue with in-app surface
```

Failure must not block note access.

---

## 4. Fast Note Creation

```text
Surface
→ Add
→ Type
→ Save / Auto-commit
→ Local DB
→ Note appears immediately
→ Outbox queued if sync enabled
```

Network and AI are not required.

---

## 5. AI-Assisted Creation

```text
Add
→ Enter natural language
→ Deterministic extraction
→ Optional AI enrichment
→ Validate result
  ├─ No consequential inference → Show optional suggestion
  └─ Consequential/ambiguous → Show confirm/edit UI
→ User accepts/edits/rejects
→ Save authored/domain state
```

AI rejection/failure returns to ordinary creation.

---

## 6. Edit Note

```text
Open note
→ Edit
→ Local save
→ UI updates
→ Sync queued
```

Remote failure:

```text
Local save remains
→ Sync pending/retry
→ User work preserved
```

---

## 7. Move Note

```text
Select
→ Drag
→ Preview
→ Drop
→ Persist logical placement
→ Sync
```

Accessible alternative:

```text
Select note
→ Move/Reorder action
→ Choose semantic position/order
→ Save
```

---

## 8. Resize Note

```text
Select
→ Resize handle / size action
→ Preview
→ Persist
```

If unsupported:

```text
No resize affordance
→ Offer supported size presets or omit capability
```

---

## 9. Hide / Restore

```text
Note → Hide → Removed from active surface → Remains in Notes collection
```

Restore:

```text
Notes / Hidden filter → Restore → Return to active surface
```

---

## 10. Archive / Restore

```text
Note → Archive → Removed from active set → Archived collection
```

Restore returns the note without implying it was newly created.

---

## 11. Delete

```text
Note
→ Delete
→ Proportionate confirm/undo
→ Local tombstone/deletion state
→ Remove from active UI
→ Sync deletion
```

Stale remote/client data must not visually resurrect the note unexpectedly.

---

## 12. Pin / Unpin

```text
Note → Pin → Explicit priority state → Surface reflects priority
```

Unpin removes explicit priority but does not hide/delete.

---

## 13. Create Reminder

```text
Note
→ Add reminder
→ Choose date/time/window
→ Optional recurrence
→ Review
→ Save local reminder
→ Schedule using platform adapter
→ Confirmation/status
```

If permission/scheduler unavailable:

```text
Explain limitation
→ Offer supported fallback
→ Preserve reminder definition where useful
```

---

## 14. AI-Inferred Reminder

```text
Note text
→ AI/deterministic interpretation
→ Proposed reminder
→ Show interpreted time
→ Confirm / Edit / Reject
→ Only confirmed action becomes authoritative
```

---

## 15. Reminder Lifecycle

```text
Upcoming
→ Due
→ Notification / Surface
  ├─ Open
  ├─ Snooze → Upcoming at snooze time
  ├─ Complete → Completed occurrence
  └─ Dismiss notification → Reminder state follows product rule
```

For recurrence, completion affects current occurrence unless recurrence is edited.

---

## 16. Intelligent Surfacing

```text
Eligible notes
→ Deterministic filter/ranking
→ Optional validated AI signal
→ Surface policy
→ Note highlighted/surfaced
→ Show concise reason
→ User action
```

Actions:

- Open/Edit
- Pin
- Snooze
- Dismiss
- Hide similar
- Reduce intelligence
- Disable intelligence

---

## 17. Dismiss Suggestion

```text
Contextual surfaced note
→ Dismiss
→ Remove current suggestion emphasis
→ Preserve underlying note
→ Apply repetition-control feedback
```

Dismiss does not equal delete.

---

## 18. Search

```text
Notes / Surface Search
→ Query
→ Local results
→ Optional filters
→ Open note
→ Action
```

Offline local search remains available.

---

## 19. Offline Editing

```text
Connectivity lost
→ User edits/creates
→ Local transaction
→ Outbox pending
→ UI continues normally
```

No blocking offline modal.

---

## 20. Reconnect & Sync

```text
Connectivity returns
→ Authenticate if valid
→ Submit queued operations
→ Server validates/reconciles
  ├─ Ack → mark synced
  ├─ Conflict → domain reconciliation
  └─ Retryable failure → retry/backoff
→ UI status updates
```

---

## 21. Sync Conflict — Auto Resolvable

```text
Conflict detected
→ Domain policy can merge safely
→ Merge
→ Persist resolved state
→ Continue sync
→ No user interruption
```

---

## 22. Sync Conflict — User Decision Needed

```text
Conflict detected
→ Meaningful authored content may be lost
→ Show human-readable difference
→ Keep Local / Keep Remote / Merge / Preserve Copy
→ Confirm resolution
→ Sync result
```

Avoid technical revision terminology.

---

## 23. Session Expired

```text
Protected action/sync
→ Session invalid
→ Preserve local work
→ Explain sign-in required
→ Re-authenticate
→ Resume sync/action where safe
```

---

## 24. Account Switch

```text
Settings → Switch/Sign out
→ Stop account sync
→ Securely transition local account state
→ Authenticate/select new account
→ Load only new account state
→ Resume sync
```

Previous account data must never flash/show during transition.

---

## 25. Subscription Purchase

```text
Premium action
→ Explain value
→ User chooses plan
→ Platform billing
→ Server verification
  ├─ Verified → entitlement update → unlock
  ├─ Pending → show pending state
  └─ Failed → preserve previous entitlement → retry/support path
```

Client purchase success alone does not permanently unlock protected access.

---

## 26. Restore Purchase

```text
Settings / Paywall
→ Restore
→ Platform provider
→ Server verification
→ Rebuild entitlement
→ Update UI
```

Repeated restore is safe/idempotent.

---

## 27. Subscription Expiry

```text
Entitlement expires
→ Premium-only new actions restricted
→ Ads may resume if eligible
→ Existing user-owned notes remain accessible/editable/exportable
→ Explain downgrade without alarmist messaging
```

---

## 28. Ad Display

```text
Eligible free user
→ Placement allowed by UX policy?
  ├─ No → no request/show
  └─ Yes → request ad
      ├─ Loaded → render in dedicated ad container
      └─ Failure/no-fill → remove cleanly
```

---

## 29. Rewarded Ad

```text
User explicitly requests reward
→ Explain reward
→ Show labeled rewarded ad
→ Provider confirms completion
→ Validate duplicate-safe reward
→ Grant temporary benefit
```

Cancellation/failure grants no false permanent entitlement.

---

## 30. AI Disabled

```text
User disables AI
→ Stop external AI requests
→ Keep deterministic intelligence active if separately enabled
→ Existing authored notes unaffected
```

---

## 31. AI Provider Failure

```text
AI request
→ Timeout/error/malformed output
→ Discard invalid derived result
→ Explain optional feature unavailable if needed
→ Continue core note flow
```

---

## 32. Permission Denied

```text
Feature requires permission
→ Explain
→ OS prompt
→ Denied
→ Explain reduced capability
→ Offer settings path / fallback
→ Continue unrelated features
```

---

## 33. Notification Privacy

```text
Settings → Notification Preview
→ Full / Limited / Hidden according to supported policy
→ Persist preference
→ Notification renderer applies preference
```

---

## 34. Data Export

```text
Settings → Privacy & Data → Export
→ Authenticate/re-authenticate if required
→ Request export
→ Processing
→ Ready
→ Secure download/access
→ Expiry/cleanup
```

Export is scoped only to the current authenticated user.

---

## 35. Account Deletion

```text
Settings → Delete Account
→ Explain impact
→ Strong confirmation / re-authentication where required
→ Start deletion lifecycle
→ Revoke sessions
→ Stop sync
→ Confirm request/status
```

Do not use manipulative retention copy in the deletion flow.

---

## 36. Error Recovery Pattern

Default recovery:

```text
Failure
→ Preserve work
→ Explain impact
→ Retry / Alternative / Continue Offline
→ Return user to task
```

Do not use generic fatal dialogs for provider failures that do not block core work.

---

## 37. Accessibility Flow

For every essential canvas action, verify:

```text
Pointer/Touch Path
AND
Keyboard / Assistive Alternative where applicable
```

The Notes collection/detail experience provides a non-spatial route to essential operations.

---

## 38. Platform Variant Rule

Conceptual flows are shared.

The following steps may vary by platform:

- surface setup,
- permission prompts,
- notification scheduling,
- lock-screen/widget behavior,
- billing,
- ads,
- window management.

A platform variant may shorten or replace a step, but must preserve data/control semantics.

---

## 39. Consolidated Review Criteria

Before final freeze:

- every critical Phase 2 user requirement maps to a flow,
- every flow has failure/recovery behavior,
- offline/sync/account-switch paths preserve data isolation,
- AI confirmation matches Phase 5,
- subscription/ad flows match Phase 6,
- export/delete/privacy match Phase 7,
- accessibility flows are testable in Phase 8.

Phase 4 remains **Draft** until final project freeze.
