# UX Specification

**Project:** Smart Sticky Wallpaper  
**Phase:** 04 — UX/UI Specification  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define the product interaction model, information architecture, user states, accessibility behavior, platform-aware UX, and decision/recovery rules.  
**Governed By:** `PROJECT_CONSTITUTION.md`  
**Primary Inputs:** Phase 01 Product Vision, Phase 02 SRS V2, Phase 03 Architecture V2

---

## 1. UX Objective

The product should make useful information visible with less effort than a traditional notes application while remaining calm, understandable, and controllable.

The UX must optimize for:

- fast capture,
- persistent visibility,
- low interruption,
- local-first confidence,
- explainable intelligence,
- platform honesty,
- strong accessibility,
- recoverability from errors.

---

## 2. UX Principles

1. **Visible over buried.** Important information should be easy to encounter.
2. **Capture before configuration.** Creating a note should not require a form.
3. **User intent wins.** Explicit actions outrank inference.
4. **Automation must explain itself.** Users should understand why something surfaced.
5. **AI is optional.** Core use remains useful without AI.
6. **Offline is normal.** Offline state is not treated as a product failure.
7. **Platform reality is visible.** Do not pretend unsupported capabilities exist.
8. **Private by default.** Sensitive content is not exposed unnecessarily.
9. **Accessible alternatives exist.** Essential tasks cannot depend only on drag, color, or pointer input.
10. **Monetization does not interrupt trust-critical flows.**

---

## 3. Primary Product Mental Model

The user should understand the product as:

> “A personal surface where my notes live, and the system can help bring the right ones forward.”

The product should not feel like:

- a chatbot with notes attached,
- a notification generator,
- a wallpaper editor with productivity features,
- a complicated task-management database.

---

## 4. Information Architecture

Primary destinations:

- **Surface** — primary visual canvas and contextual experience.
- **Notes** — complete searchable note collection.
- **Reminders** — upcoming, due, snoozed, recurring, completed states.
- **Intelligence** — explanation/preferences/suggestions where useful.
- **Settings** — account, appearance, surface, sync, privacy, notifications, monetization, diagnostics.

Navigation implementation may adapt by form factor:

- bottom navigation on compact mobile,
- rail/sidebar on tablet/desktop,
- native menu patterns where appropriate.

The information architecture remains conceptually consistent across platforms.

---

## 5. Primary Journey

```text
Launch
→ Surface
→ Add Note
→ Type
→ Save Locally
→ Note Appears Immediately
→ Optional Placement / Style / Reminder
→ Optional Sync
→ Optional Contextual Surfacing
→ User Acts / Snoozes / Pins / Dismisses / Edits
```

No network or AI request should block the fast note-creation path.

---

## 6. Onboarding

Onboarding should prove value quickly.

Recommended sequence:

1. concise product promise,
2. interactive/example sticky note,
3. show how the surface works,
4. explain contextual surfacing briefly,
5. enter main experience,
6. request permissions only when the user invokes the related feature,
7. offer account/sync at a contextually useful moment if local-first guest use is supported.

Avoid long feature tours.

---

## 7. Permission UX

Permission flow:

```text
User Invokes Capability
→ Explain Benefit
→ Explain Limitation if Denied
→ Request OS Permission
→ Granted / Denied
→ Continue with Best Available Experience
```

Never request every permission at startup.

Denied optional permissions must not block unrelated note operations.

---

## 8. Surface / Home

The Surface is the signature experience.

It should prioritize:

- notes,
- readability,
- spatial familiarity,
- contextual relevance,
- minimal chrome.

Primary controls:

- Add,
- Search,
- Filter/View options,
- Surface settings,
- accessible navigation to Notes/Reminders/Settings.

Controls may collapse/adapt on small displays.

---

## 9. Sticky Note Interaction

Primary interactions:

- tap/click → open/select,
- drag → reposition where supported,
- resize → supported platforms/layouts,
- long press/right click → contextual menu,
- keyboard/action menu → accessible equivalent,
- pin → preserve explicit importance,
- hide → remove from active surface without deletion,
- archive → remove from active collection while preserving,
- delete → deletion-safe flow.

Text selection/editing must not accidentally trigger movement.

---

## 10. Note Creation

Fast path:

```text
Add
→ Focus Content Field
→ Type
→ Save / Auto-commit
→ Local Note Appears
```

Secondary options remain progressive:

- title,
- color/appearance,
- reminder,
- pin,
- visibility,
- category,
- contextual/intelligence options.

Do not force users to configure metadata before saving.

---

## 11. Note Editing

Editing should preserve focus on content.

Recommended hierarchy:

1. content/title,
2. reminder,
3. pin/visibility,
4. appearance,
5. intelligence/context metadata,
6. archive/delete/advanced actions.

Save state should be understandable but unobtrusive.

---

## 12. Dense Canvas Behavior

When many notes exist, avoid turning the canvas into visual noise.

UX may use:

- search/filter,
- contextual prioritization,
- temporary focus mode,
- hide/archive,
- zoom/pan on suitable devices,
- list/collection view,
- automatic but reversible de-emphasis.

The user must always be able to find all non-deleted notes outside the active visual surface.

---

## 13. Intelligent Surfacing

Contextually surfaced information should answer:

> “Why am I seeing this now?”

Possible concise explanations:

- Upcoming reminder
- Relevant this morning
- Pinned by you
- Often useful around this time
- Suggested from your routine

Actions should include as relevant:

- open,
- pin,
- snooze,
- dismiss,
- hide similar suggestions,
- reduce intelligence,
- disable intelligence.

A dismissed contextual suggestion does not delete the underlying note.

---

## 14. AI Suggestion UX

AI-derived suggestions must be visually distinct from user-authored state.

States:

- processing,
- suggestion available,
- confidence/ambiguity requiring review,
- accepted,
- edited then accepted,
- rejected,
- failed/unavailable.

Consequential inference flow:

```text
AI Suggestion
→ Show Proposed Meaning / Action
→ Confirm or Edit
→ Apply Domain Action
```

AI failure should collapse gracefully back to normal note behavior.

---

## 15. Reminder UX

Reminder editor supports as applicable:

- exact date/time,
- semantic time window,
- recurrence,
- snooze behavior,
- notification preview/privacy,
- timezone-aware explanation when relevant.

Reminder states:

- upcoming,
- due,
- snoozed,
- completed,
- missed,
- disabled.

Recurring reminder completion should clearly apply to the current occurrence unless the user edits the recurrence itself.

---

## 16. Notification UX

Notifications should be:

- actionable,
- concise,
- non-duplicative,
- privacy-conscious,
- tied to explicit reminder/product policy.

Contextual AI relevance alone should not unexpectedly become a disruptive notification.

Where supported, actions may include:

- Open,
- Snooze,
- Complete.

---

## 17. Search & Collection UX

Notes view provides reliable retrieval even when the surface hides/de-emphasizes content.

Support filters as appropriate:

- text,
- pinned,
- reminder state,
- archived,
- hidden,
- contextual status,
- category/tag if included.

Local data remains searchable offline.

---

## 18. Offline UX

Offline state should feel normal.

Behavior:

- local reads/writes continue,
- subtle offline/sync status appears when relevant,
- queued operations do not create alarming errors,
- provider-dependent features explain temporary unavailability,
- user work remains visible.

Avoid modal “you are offline” blocks for ordinary note editing.

---

## 19. Sync UX

Sync status should be progressive, not noisy.

Possible states:

- synced,
- pending,
- offline,
- retrying,
- conflict needs attention,
- account/session issue.

Do not show technical revision IDs or provider errors to ordinary users.

---

## 20. Conflict UX

Prefer automatic domain-safe reconciliation.

Ask the user only when meaningful authored information may be lost.

Conflict UI should:

- explain that versions differ,
- show meaningful differences,
- preserve both values where practical,
- provide clear resolution actions,
- avoid technical sync terminology.

---

## 21. Account UX

Account area should support:

- sign in/out,
- sync state,
- active subscription,
- restore purchase,
- export,
- delete account,
- privacy controls.

Account switching must visibly complete before showing another account's data.

---

## 22. Subscription UX

Paywalls should explain value without blocking access to user-owned notes.

Subscription UX should support:

- feature value explanation,
- plan status,
- purchase,
- restore,
- manage subscription,
- expiry/grace state,
- downgrade explanation.

Avoid dark patterns, fake urgency, hidden cancellation language, or misleading entitlement claims.

---

## 23. Advertising UX

Ads must not:

- cover notes,
- interrupt typing/editing,
- interrupt reminder confirmation,
- appear as fake system UI,
- look like user-created notes,
- interfere with authentication/purchase,
- degrade accessibility.

Rewarded ads must be explicitly user-initiated and clearly explain the temporary reward.

Ad load failure should leave clean empty space or remove the placement.

---

## 24. Settings IA

Recommended groups:

### Account
Sign-in, sync, sessions where supported.

### Surface
Persistent surface type, fallback, visibility behavior.

### Appearance
Theme, note defaults, visual preferences.

### Intelligence
Contextual surfacing, AI processing, explanations, reset/reduce behavior.

### Notifications
Reminder notifications and sensitive previews.

### Subscription & Ads
Plan, restore, manage, ad-related controls where applicable.

### Privacy & Data
AI data controls, telemetry controls, export, deletion.

### Diagnostics
Sync status, app version, safe troubleshooting information.

---

## 25. Empty States

Empty states should teach value with one primary action.

Examples:

- no notes → “Create your first sticky note”
- no reminders → “Add a reminder to a note”
- no search results → clear filters / create note
- no intelligence suggestions → no pressure to enable AI

Avoid empty states that feel like errors.

---

## 26. Error UX

Error hierarchy:

- inline validation for local correction,
- transient banner/snackbar for recoverable events,
- persistent status for sync/account issues,
- blocking dialog only for genuinely blocking/high-impact decisions.

Every error should answer:

1. what happened,
2. whether work is safe,
3. what the user can do next.

---

## 27. Accessibility

Essential behavior must support:

- screen readers,
- semantic labels,
- logical focus order,
- keyboard navigation on keyboard platforms,
- scalable text,
- adequate touch targets,
- contrast,
- non-color-only status,
- reduced motion,
- accessible alternatives to drag/reorder.

The Notes/list view should function as an accessible alternative to spatial-only canvas interaction.

---

## 28. Responsive UX

### Compact Phone
- canvas-first,
- compact controls,
- bottom sheets/context menus,
- one primary panel at a time.

### Tablet / Foldable
- more surface visible,
- optional side panel,
- multi-pane editing where useful.

### Desktop
- keyboard/mouse-first affordances,
- context menus,
- resizable panels/windows,
- optional sidebars,
- persistent app controls without covering canvas.

---

## 29. Platform-Aware UX

Shared concepts remain consistent, but native behavior may differ for:

- widgets/wallpaper/surfaces,
- lock screen,
- notifications,
- background scheduling,
- permissions,
- billing,
- ads,
- window management.

The capability registry controls what UI is offered.

Never show a control for a capability that is impossible unless the UI explicitly explains the unsupported state/fallback.

---

## 30. Privacy UX

Privacy-sensitive actions require clear language.

Especially:

- external AI processing,
- notification previews,
- telemetry choices,
- export,
- account deletion.

Do not use vague language such as “improve your experience” when specific data use can be explained.

---

## 31. MVP UX Acceptance

The MVP UX is acceptable when:

- a new user understands the concept quickly,
- first note creation is fast,
- notes remain retrievable regardless of surface prioritization,
- offline operation is understandable,
- sync state is recoverable,
- reminders are predictable,
- AI suggestions are distinguishable/controllable,
- monetization does not block owned data,
- platform limitations are honest,
- essential accessibility paths exist.

---

## 32. Consolidated Review Criteria

Before final freeze verify:

- every Phase 2 user-facing MUST has a UX state/flow,
- Phase 3 capability states map to actual UI behavior,
- Phase 5 AI confirmation/explanation behavior is represented,
- Phase 6 paywall/ad rules are represented,
- Phase 7 privacy/deletion/export language is represented,
- Phase 8 accessibility/error scenarios are testable,
- no flow silently assumes ideal platform capabilities.

Phase 4 remains **Draft** until final project freeze.
