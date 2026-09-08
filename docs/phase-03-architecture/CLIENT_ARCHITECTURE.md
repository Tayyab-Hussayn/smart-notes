# Client Architecture

**Project:** Smart Sticky Wallpaper  
**Phase:** 03 — Architecture  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define the KMP client structure, local-first state model, synchronization boundary, platform-capability system, security model, and native integration strategy.  
**Governed By:** `PROJECT_CONSTITUTION.md` and `SYSTEM_ARCHITECTURE.md`

---

## 1. Technology Baseline

- Kotlin Multiplatform for shared domain/application/data coordination.
- Compose Multiplatform where shared UI is practical.
- Native platform APIs for OS-specific capabilities.
- Rust only for narrow, measured performance/system-integration needs.
- Relational local persistence compatible with KMP; exact technology is an ADR.

---

## 2. Recommended Client Modules

```text
client/
├── shared/
│   ├── core-domain/
│   ├── notes/
│   ├── canvas/
│   ├── reminders/
│   ├── intelligence/
│   ├── sync/
│   ├── identity/
│   ├── monetization/
│   ├── notifications/
│   ├── settings/
│   └── infrastructure-contracts/
├── shared-ui/
└── platform/
    ├── android/
    ├── ios/
    ├── windows/
    ├── macos/
    └── linux/
```

Exact Gradle/package boundaries may evolve, but domain ownership may not blur silently.

---

## 3. State Model

Use unidirectional data flow:

```text
User Event
  ↓
Intent / Action
  ↓
Application Use Case
  ↓
Domain + Repository
  ↓
Durable State
  ↓
Observable UI State
```

UI state is derived from application/domain state rather than becoming an independent business source of truth.

---

## 4. Local Database Role

The local database is the working source for local-first user data, not a disposable cache.

It stores, as appropriate:

- notes,
- canvas placements,
- reminders,
- typed settings,
- sync revisions,
- outbox operations,
- tombstones,
- AI-derived metadata selected for persistence,
- bounded entitlement snapshot/cache.

Account data must be namespaced or isolated so account switching cannot expose previous-user content.

---

## 5. Atomic Local Mutation

Preferred write transaction:

```text
Validate Domain Mutation
  ↓
Begin Local Transaction
  ├── Update Entity
  └── Append Outbox Operation
  ↓
Commit
  ↓
Emit New UI State
```

This prevents a successful local edit from being forgotten by synchronization bookkeeping.

---

## 6. Repository Boundaries

Conceptual interfaces may include:

- `NoteRepository`
- `CanvasRepository`
- `ReminderRepository`
- `SettingsRepository`
- `SyncRepository`
- `SessionRepository`
- `EntitlementRepository`
- `AiInsightRepository`

Application code should not know whether an implementation uses SQL, HTTP, secure storage, or provider SDKs.

---

## 7. Canvas Representation

Canvas placement uses logical coordinates, not permanent physical pixels.

Placement state should support:

- logical position,
- logical size,
- z-order where needed,
- visibility,
- opacity,
- appearance token/configuration,
- surface context,
- revision/update metadata.

Rendering adapts this state to screen/window dimensions.

---

## 8. Capability Registry

The client exposes explicit capability status rather than platform guesses.

Suggested status model:

```text
Supported
SupportedWithLimitations(reason)
FallbackAvailable(type)
Unavailable(reason)
PermissionRequired(permission)
TemporarilyUnavailable(reason)
```

Capability families:

- persistent wallpaper/surface,
- lock-screen/widget,
- notifications,
- background scheduling,
- overlay/window behavior,
- startup/background persistence,
- billing,
- advertising,
- secure storage.

---

## 9. Persistent Surface Contract

A shared abstraction should describe product intent rather than vendor APIs.

Conceptually:

```text
PersistentSurfaceCapability
- status()
- supportedModes()
- publish(surfaceModel)
- refresh()
- remove()
```

The `surfaceModel` should contain presentation-safe data, not unrestricted domain/provider objects.

---

## 10. Reminder Client Architecture

```text
Reminder Domain State
  ↓
Scheduler Coordinator
  ↓
Platform Reminder Adapter
  ↓
OS Scheduler / Notification Mechanism
```

The domain owns reminder meaning; the platform adapter owns OS scheduling mechanics.

Clock/time calculations must be testable using an injected clock/time abstraction.

---

## 11. Intelligence Client Architecture

Client-side deterministic engine may use:

- explicit reminders,
- current time/window,
- pin/priority,
- recurrence,
- visibility,
- snooze/completion,
- configured context,
- repetition controls.

AI assistance enters through a separate interface and produces validated derived results, never direct UI/database authority.

---

## 12. Sync Client

Sync owns transport replication concerns:

- durable outbox,
- operation IDs,
- retry/backoff,
- last server cursor/revision,
- acknowledgments,
- conflicts,
- reconciliation,
- tombstone handling,
- account/device context.

Sync must call domain/application reconciliation policies rather than inventing business conflict rules itself.

---

## 13. Sync State Machine

Conceptual operation states:

```text
Pending
→ InFlight
→ Acknowledged
   or
→ Conflict
   or
→ RetryableFailure
   or
→ PermanentFailure
```

Process death/restart must recover durable states safely.

---

## 14. Identity & Session Storage

Client stores only required session material.

Rules:

- use platform-secure credential storage where available,
- separate account identity from note-domain state,
- clear/invalidate account-bound caches on account switch as required,
- never store provider/server secrets,
- handle expired/revoked sessions explicitly.

Exact token strategy is defined by ADR-003.

---

## 15. Entitlement Client

Conceptual interface:

```text
EntitlementProvider
- snapshot()
- refresh()
- restorePurchases()
- observeChanges()
```

Rules:

- server-verified entitlement state is authoritative,
- local cache is bounded temporary trust,
- stale cache cannot permanently grant access,
- downgrade never deletes existing user data.

---

## 16. Advertising Client

Advertising remains platform/provider-specific behind an owned abstraction.

Core UI must not depend on ad availability.

The ad layer must not receive:

- note body,
- reminder text,
- AI prompts,
- auth secrets,
- private canvas data.

Reward completion must map into duplicate-safe application state.

---

## 17. Network/API Client

The API client should provide centrally:

- authentication headers/session handling,
- versioning,
- serialization,
- timeouts,
- error normalization,
- request IDs/correlation,
- safe retry policy,
- idempotency-key support.

Do not retry non-idempotent operations blindly.

---

## 18. Error Model

Normalize errors into categories such as:

- validation,
- authentication,
- authorization,
- network/offline,
- timeout,
- sync conflict,
- capability/permission,
- entitlement,
- provider,
- internal/unexpected.

UI receives actionable product errors, not provider stack traces.

---

## 19. Background Execution

The client must assume OS background execution can be delayed or denied.

Architecture therefore favors:

- durable state,
- native schedulers,
- resumable sync,
- foreground recovery,
- capability-aware UX.

Do not depend on unrestricted background loops.

---

## 20. Security & Privacy

Client architecture must:

- minimize stored secrets,
- separate accounts,
- redact logs,
- avoid note content in analytics,
- treat deep links/external intents as untrusted,
- validate server responses before applying protected state,
- avoid provider credentials in binaries.

---

## 21. Accessibility Architecture

Shared and native UI must support:

- semantic labels,
- keyboard/focus navigation where applicable,
- scalable text,
- reduced-motion support where available,
- non-color-only status,
- alternatives to essential drag-only actions.

Accessibility state belongs in component behavior, not a late overlay.

---

## 22. Performance Principles

- no DB/network/AI work on UI thread,
- avoid unnecessary recomposition,
- paginate/lazily render large collections where appropriate,
- benchmark canvas with representative note counts,
- bound background retries,
- avoid loading all historical sync/provider data into memory.

---

## 23. Testability

Client architecture must allow deterministic tests through injectable/fake boundaries for:

- clock/time,
- network,
- local persistence,
- platform capabilities,
- AI,
- billing,
- ads,
- notification scheduler.

Live providers are not required for core CI correctness.

---

## 24. Platform Direction

### Android
Prioritize approved wallpaper/widget/notification/scheduling capabilities and supported overlay behavior.

### iOS
Prioritize widgets, notifications, and approved system surfaces; never assume unrestricted lock-screen overlays.

### Windows / macOS
Prioritize desktop surface/window behavior, notifications, startup/background integration, and native packaging.

### Linux
Capability detection must account for desktop-environment/window-manager variance.

---

## 25. Client ADR Dependencies

Before affected implementation finalize:

- local DB technology,
- shared/native UI boundaries,
- sync protocol details,
- auth/session storage/token strategy,
- capability registry semantics,
- background scheduling abstraction.

---

## 26. Consolidated Review Criteria

Verify:

- Phase 2 local-first requirements are fully satisfiable,
- account switching cannot leak state,
- sync survives restart/offline/retry,
- platform fallbacks are representable,
- AI/provider SDKs do not leak into domain code,
- Phase 4 UX and Phase 8 testing fit the architecture.

Phase 3 remains **Draft** until final project freeze.
