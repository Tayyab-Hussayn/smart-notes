# Software Requirements Specification

**Project:** Smart Sticky Wallpaper  
**Phase:** 02 — Requirements  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define observable, testable product and system requirements without prescribing unnecessary implementation details.  
**Governed By:** `00-project-governance/PROJECT_CONSTITUTION.md`  
**Primary Inputs:** Phase 01 Product Vision and Product Discovery

---

## 1. Requirements Principles

1. Requirements describe externally observable behavior or enforceable system constraints.
2. Every critical requirement must have a validation path.
3. Explicit user intent overrides inferred intelligence.
4. Core note operations are local-first.
5. AI is optional for core correctness.
6. Platform differences are explicit and capability-based.
7. Existing user-owned data must remain accessible regardless of subscription expiry.
8. Security, privacy, accessibility, reliability, and recoverability are first-class requirements.
9. Unsupported platform behavior must fail honestly and provide a documented fallback where possible.
10. Implementation details belong to architecture/ADR documents unless needed to define a mandatory constraint.

---

## 2. Requirement Priority

Requirements use:

- **MUST** — required for MVP or safety/integrity.
- **SHOULD** — expected unless a documented platform/product constraint prevents it.
- **MAY** — optional or capability-dependent.

Requirement IDs remain stable after freeze whenever practical.

---

## 3. Actors

### User
Creates and manages local notes, canvas state, reminders, settings, and privacy controls.

### Authenticated User
Uses account-backed synchronization, entitlement retrieval, export/deletion, and cloud-dependent features.

### Free User
Uses the free entitlement set and may be eligible for advertising.

### Premium User
Receives features granted by verified premium entitlements.

### System
Performs local persistence, scheduling, synchronization, deterministic prioritization, notifications, and fallback behavior.

### AI Service
Optionally assists with semantic interpretation, classification, extraction, ranking, summaries, and suggestions.

### Administrator
Uses separately protected operational capabilities with least privilege and auditability.

---

# Functional Requirements

## 4. Account & Session Requirements

**REQ-AUTH-01 — MUST:** Users shall be able to create an account and authenticate where account-backed features are used.

**REQ-AUTH-02 — MUST:** Core local note functionality shall not require a live authentication request after the user has a valid local application state.

**REQ-AUTH-03 — MUST:** Authentication shall support secure session lifecycle management, including expiration and revocation behavior.

**REQ-AUTH-04 — MUST:** Users shall be able to sign out from the current account/device.

**REQ-AUTH-05 — SHOULD:** Users shall be able to invalidate other active account sessions where supported by the final identity design.

**REQ-AUTH-06 — MUST:** Switching accounts shall not expose the previous account's local user data, sync state, or entitlements.

**REQ-AUTH-07 — MUST:** Users shall be able to request account deletion.

**REQ-AUTH-08 — MUST:** Account deletion shall define and execute treatment of active user data, derived data, sessions, synchronized records, and retained backups according to the privacy lifecycle.

**REQ-AUTH-09 — MUST:** The client shall never contain server-only credentials, private provider keys, or authoritative entitlement secrets.

---

## 5. Note Requirements

**REQ-NOTE-01 — MUST:** Users shall be able to create a sticky note.

**REQ-NOTE-02 — MUST:** Users shall be able to read and edit note content.

**REQ-NOTE-03 — MUST:** Users shall be able to archive and restore a note.

**REQ-NOTE-04 — MUST:** Users shall be able to delete a note through a data-safe deletion flow.

**REQ-NOTE-05 — MUST:** Users shall be able to duplicate a note into an independently editable note.

**REQ-NOTE-06 — MUST:** Users shall be able to pin and unpin important notes.

**REQ-NOTE-07 — MUST:** Notes shall support product-approved visual properties.

**REQ-NOTE-08 — MUST:** Notes shall retain required creation/modification metadata.

**REQ-NOTE-09 — MUST:** Core create/read/edit/archive/delete operations shall function offline.

**REQ-NOTE-10 — MUST:** User-authored content shall remain distinguishable from deterministic-derived and AI-derived metadata.

**REQ-NOTE-11 — MUST:** Note mutations shall not silently discard user-authored content during recoverable failures.

**REQ-NOTE-12 — SHOULD:** Destructive actions shall provide confirmation, undo, recovery, or another proportionate safeguard.

---

## 6. Canvas Requirements

**REQ-CANVAS-01 — MUST:** Users shall be able to view notes as objects on a visual canvas.

**REQ-CANVAS-02 — MUST:** Users shall be able to reposition notes through direct manipulation where the platform supports it.

**REQ-CANVAS-03 — MUST:** Users shall have an accessible non-drag alternative for essential positioning/interaction where required by accessibility needs.

**REQ-CANVAS-04 — SHOULD:** Users shall be able to resize notes where the target platform and design support resizing.

**REQ-CANVAS-05 — MUST:** Users shall be able to control supported opacity/visibility properties.

**REQ-CANVAS-06 — MUST:** Users shall be able to hide and restore notes without deleting them.

**REQ-CANVAS-07 — MUST:** Canvas state shall persist across application sessions.

**REQ-CANVAS-08 — MUST:** Canvas placement shall remain logically stable across supported screen sizes, density changes, and orientation/window changes.

**REQ-CANVAS-09 — MUST:** A large or growing note collection shall not make basic canvas navigation unusable.

---

## 7. Persistent Surface Requirements

**REQ-SURFACE-01 — MUST:** The product shall provide the strongest approved persistent visual surface available on each supported platform.

**REQ-SURFACE-02 — MUST:** Persistent-surface behavior shall use platform-approved capabilities.

**REQ-SURFACE-03 — MUST:** Restricted capabilities shall have an explicit fallback or clearly communicated unsupported state.

**REQ-SURFACE-04 — MUST:** The product shall not depend on unrestricted lock-screen, overlay, or background behavior that an operating system does not permit.

**REQ-SURFACE-05 — MUST:** Platform capability status shall be represented explicitly rather than inferred from another platform's behavior.

**REQ-SURFACE-06 — MUST:** Failure or absence of a persistent surface shall not prevent users from accessing notes inside the application.

**REQ-SURFACE-07 — MUST:** Platform-specific rendering/surface behavior shall not corrupt shared note or sync data.

---

## 8. Interaction & UX Requirements

**REQ-UX-01 — MUST:** Users shall be able to select and open a note.

**REQ-UX-02 — MUST:** Edit, hide, archive, restore, delete, pin, and reminder actions shall use discoverable controls.

**REQ-UX-03 — MUST:** Consequential or destructive actions shall use confirmation or recovery behavior appropriate to their impact.

**REQ-UX-04 — MUST:** Loading, empty, offline, denied-permission, and recoverable-error states shall be defined.

**REQ-UX-05 — MUST:** The interface shall remain usable as note count increases.

**REQ-UX-06 — MUST:** Permission requests shall be contextual and explain the user benefit.

**REQ-UX-07 — MUST:** A denied optional permission shall not unnecessarily block unrelated core note functionality.

**REQ-UX-08 — SHOULD:** The product shall minimize interruption during note capture and editing.

---

## 9. Reminder Requirements

**REQ-REM-01 — MUST:** Users shall be able to assign a date/time or supported semantic time window to a reminder.

**REQ-REM-02 — MUST:** Users shall be able to create recurring reminders.

**REQ-REM-03 — MUST:** Users shall be able to edit and remove reminder schedules.

**REQ-REM-04 — MUST:** Users shall be able to complete one-time reminder occurrences.

**REQ-REM-05 — MUST:** Users shall be able to snooze supported reminders.

**REQ-REM-06 — MUST:** Recurring reminder completion shall not unintentionally destroy the recurrence definition.

**REQ-REM-07 — MUST:** The system shall surface due reminders using platform-supported mechanisms.

**REQ-REM-08 — MUST:** Reminder behavior shall account for permissions, timezone, clock changes, and background-execution limitations.

**REQ-REM-09 — MUST:** Explicit user-defined schedules shall take precedence over inferred schedules.

**REQ-REM-10 — MUST:** Ambiguous AI-inferred reminder creation shall follow the configured confirmation policy and shall not silently become authoritative.

**REQ-REM-11 — MUST:** Duplicate scheduling/retry behavior shall not produce duplicate reminder occurrences or duplicate notifications.

---

## 10. Intelligent Prioritization Requirements

**REQ-INT-01 — MUST:** Deterministic rules shall be evaluated before optional AI relevance signals.

**REQ-INT-02 — MUST:** Explicit signals may include reminder state, due time, priority, pin state, recurrence, visibility, snooze, completion, and configured context.

**REQ-INT-03 — MUST:** Hidden, deleted, completed one-time, or actively snoozed notes shall obey their authoritative suppression rules.

**REQ-INT-04 — MUST:** Explicit user intent and hard product rules shall outrank AI ranking.

**REQ-INT-05 — MUST:** Users shall be able to disable contextual/intelligent prioritization.

**REQ-INT-06 — MUST:** Users shall be able to override or dismiss intelligent surfacing.

**REQ-INT-07 — MUST:** The system shall control repeated resurfacing so the same suggestion does not become unnecessarily intrusive.

**REQ-INT-08 — SHOULD:** Surfaced notes shall expose a user-understandable reason where practical.

**REQ-INT-09 — MAY:** Intelligent prioritization may use configurable thresholds such as note count or relevance score.

**REQ-INT-10 — MUST:** Configurable thresholds shall not require unrelated client rewrites.

---

## 11. AI Requirements

**REQ-AI-01 — MUST:** AI functionality shall remain behind a provider-independent application boundary.

**REQ-AI-02 — MAY:** AI may assist with semantic classification, date/time interpretation, routine detection, relevance, grouping, summarization, and suggestions.

**REQ-AI-03 — MUST:** AI shall not be required for basic note CRUD, local persistence, or deterministic reminder correctness.

**REQ-AI-04 — MUST:** AI output shall be validated before it can affect application/domain state.

**REQ-AI-05 — MUST:** Consequential inferred actions shall require confirmation unless the user's explicit instruction already authorizes the action.

**REQ-AI-06 — MUST:** AI failure, timeout, malformed output, quota exhaustion, or provider outage shall not break core note functionality.

**REQ-AI-07 — MUST:** AI processing shall honor user privacy/opt-out controls.

**REQ-AI-08 — MUST:** The system shall minimize data sent to external AI providers to what is required for the task.

**REQ-AI-09 — MUST:** Persisted AI-derived metadata shall be distinguishable from user-authored data and support provenance where defined by Phase 5.

**REQ-AI-10 — MUST:** AI usage shall support enforceable quota/rate/cost controls.

**REQ-AI-11 — MUST:** Replacing an AI provider shall not require redesigning core note/domain behavior.

**REQ-AI-12 — MUST:** Note text or AI output shall not bypass authentication, authorization, or other application security policy.

---

## 12. Notification Requirements

**REQ-NOTIF-01 — MUST:** Users shall receive approved reminder notifications through supported platform mechanisms.

**REQ-NOTIF-02 — MUST:** Users shall be able to control relevant notification settings and permissions.

**REQ-NOTIF-03 — MUST:** The system shall avoid unnecessary duplicate notifications.

**REQ-NOTIF-04 — MUST:** Notification content shall support privacy-conscious preview behavior.

**REQ-NOTIF-05 — MUST:** Contextual AI relevance alone shall not create disruptive notifications unless product policy explicitly permits it.

**REQ-NOTIF-06 — MUST:** Notification failure shall not corrupt reminder state.

---

## 13. Local Storage & Sync Requirements

**REQ-SYNC-01 — MUST:** Core user note data shall have a local representation.

**REQ-SYNC-02 — MUST:** The client shall queue sync-eligible mutations while offline.

**REQ-SYNC-03 — MUST:** Queued changes shall synchronize when connectivity returns and the user/account state permits it.

**REQ-SYNC-04 — MUST:** Retriable mutations shall support duplicate-safe/idempotent behavior.

**REQ-SYNC-05 — MUST:** Synchronization shall use revision/version information sufficient to detect stale state and conflicts.

**REQ-SYNC-06 — MUST:** Conflict resolution shall be deterministic, documented, and domain-aware.

**REQ-SYNC-07 — MUST:** Deletion state shall propagate in a way that prevents unintended stale-client resurrection.

**REQ-SYNC-08 — MUST:** Sync failures shall be recoverable without silent loss of local authored work.

**REQ-SYNC-09 — MUST:** Multi-device synchronization shall eventually converge for supported conflict cases.

**REQ-SYNC-10 — MUST:** Sync state and cached user data shall be isolated by authenticated account.

**REQ-SYNC-11 — MUST:** A client shall not be able to read or mutate another user's resources by changing identifiers or payload ownership fields.

**REQ-SYNC-12 — MUST:** App/process interruption during synchronization shall leave durable state recoverable.

---

## 14. Cross-Platform Requirements

**REQ-PLAT-01 — MUST:** Shared business/domain behavior shall be reusable across supported platforms where practical.

**REQ-PLAT-02 — MUST:** Native APIs shall be used where platform-specific capabilities require them.

**REQ-PLAT-03 — MUST:** Android, iOS, Windows, macOS, and Linux shall have an explicit capability support matrix before production release.

**REQ-PLAT-04 — MUST:** Unsupported capabilities shall have a documented fallback or explicit unsupported state.

**REQ-PLAT-05 — MUST:** Platform-specific behavior shall not corrupt shared user data.

**REQ-PLAT-06 — MUST:** The product shall not claim functional parity where OS restrictions make parity impossible.

**REQ-PLAT-07 — SHOULD:** Shared UI shall preserve common product semantics while allowing native interaction differences.

---

## 15. Subscription & Entitlement Requirements

**REQ-SUB-01 — MUST:** The product shall support paid feature entitlements.

**REQ-SUB-02 — MUST:** Billing-provider purchase state and application entitlement state shall be treated as separate concepts.

**REQ-SUB-03 — MUST:** Purchase/subscription claims shall be verified server-side through approved provider mechanisms.

**REQ-SUB-04 — MUST:** Client UI/local flags shall not be authoritative for premium access.

**REQ-SUB-05 — MUST:** Renewal, cancellation, grace, expiration, restoration, refund, and revocation states shall be handled according to provider truth and product policy.

**REQ-SUB-06 — MUST:** Premium features shall be protected by named entitlement checks.

**REQ-SUB-07 — MUST:** Subscription expiration shall not delete or block access to existing user-owned notes.

**REQ-SUB-08 — MUST:** Downgrade behavior shall preserve existing data even when new premium-only actions become unavailable.

**REQ-SUB-09 — MUST:** Entitlement state shall support bounded offline caching and eventual revalidation.

**REQ-SUB-10 — MUST:** Replayed purchase events shall not create duplicate entitlements.

---

## 16. Advertising Requirements

**REQ-ADS-01 — MUST:** Advertising shall remain behind a provider abstraction.

**REQ-ADS-02 — MAY:** Eligible free users may receive ads when platform, region, consent, and entitlement rules permit.

**REQ-ADS-03 — MUST:** Users with the ad-removal entitlement shall not be served product ads.

**REQ-ADS-04 — MUST:** Ads shall not block access to existing user-owned notes.

**REQ-ADS-05 — MUST:** Ads shall not interrupt critical note creation/editing, reminder confirmation, authentication, or purchase confirmation workflows.

**REQ-ADS-06 — MUST:** Ads shall not visually masquerade as user-authored notes or system warnings.

**REQ-ADS-07 — MAY:** Rewarded advertising may unlock approved temporary benefits.

**REQ-ADS-08 — MUST:** Reward completion shall be duplicate-safe/idempotent.

**REQ-ADS-09 — MUST:** Ad-provider failure or no-fill shall not block core application behavior.

**REQ-ADS-10 — MUST:** Private note/reminder content shall not be sent to ad providers.

---

## 17. Settings Requirements

**REQ-SET-01 — MUST:** Users shall be able to configure supported visual behavior.

**REQ-SET-02 — MUST:** Users shall be able to configure intelligent/contextual behavior.

**REQ-SET-03 — MUST:** Users shall be able to configure relevant notification behavior.

**REQ-SET-04 — MUST:** Users shall be able to manage account and privacy settings.

**REQ-SET-05 — MUST:** Users shall be able to review current subscription/entitlement state.

**REQ-SET-06 — MUST:** Users shall have controls for external AI processing where required by product/privacy policy.

**REQ-SET-07 — SHOULD:** Users shall be able to control sensitive notification preview behavior.

**REQ-SET-08 — MUST:** Settings shall persist appropriately across sessions and sync only where the specification explicitly permits it.

---

## 18. Privacy & Data Requirements

**REQ-PRIV-01 — MUST:** Data collection shall be limited to documented product or operational purposes.

**REQ-PRIV-02 — MUST:** Private note/reminder content shall not be treated as general analytics data.

**REQ-PRIV-03 — MUST:** External data flows shall be documented by category and purpose.

**REQ-PRIV-04 — MUST:** External AI processing shall be identifiable and controllable.

**REQ-PRIV-05 — MUST:** Users shall have a supported mechanism to export user-owned data.

**REQ-PRIV-06 — MUST:** Users shall have a supported mechanism to request account/data deletion.

**REQ-PRIV-07 — MUST:** Sensitive content, credentials, and tokens shall be excluded from normal application logs and analytics.

**REQ-PRIV-08 — MUST:** Account switching shall not expose another account's local content or cached entitlement state.

**REQ-PRIV-09 — MUST:** Retention and backup behavior for deleted data shall be documented before production.

**REQ-PRIV-10 — MUST:** New context sources such as location, calendar, activity, or voice shall require explicit privacy review before use.

---

## 19. Backend/API Requirements

**REQ-BE-01 — MUST:** Protected backend APIs shall authenticate requests.

**REQ-BE-02 — MUST:** Protected resource operations shall authorize the authenticated identity against resource ownership and required role/entitlement.

**REQ-BE-03 — MUST:** APIs shall validate external input and reject malformed/oversized payloads according to configured limits.

**REQ-BE-04 — MUST:** APIs shall expose predictable, non-sensitive error contracts.

**REQ-BE-05 — MUST:** Retriable durable operations shall support idempotency where duplicate effects would be harmful.

**REQ-BE-06 — MUST:** Backend services shall provide health/operational visibility.

**REQ-BE-07 — MUST:** Security-relevant administrative, billing, deletion, and entitlement actions shall be auditable without exposing unnecessary note content.

**REQ-BE-08 — MUST:** API evolution shall follow the supported version/compatibility policy.

**REQ-BE-09 — MUST:** Provider outages shall be isolated so unrelated core domains continue operating where feasible.

---

# Non-Functional Requirements

## 20. Performance & Responsiveness

**REQ-PERF-01 — MUST:** Core local note interactions shall avoid avoidable UI-blocking work.

**REQ-PERF-02 — MUST:** The product shall remain usable at representative note counts defined by the test/performance plan.

**REQ-PERF-03 — MUST:** Synchronization shall not unnecessarily block local note interaction.

**REQ-PERF-04 — MUST:** External AI latency shall be bounded by timeout/fallback behavior.

**REQ-PERF-05 — MUST:** Critical database/API operations shall have measurable production observability.

**REQ-PERF-06 — SHOULD:** Numeric performance targets shall be established through benchmarking before production freeze.

---

## 21. Reliability & Recoverability

**REQ-REL-01 — MUST:** Common network/API failures shall not cause silent loss of local authored work.

**REQ-REL-02 — MUST:** App restart or process termination shall not corrupt durable note/sync state.

**REQ-REL-03 — MUST:** AI, billing, and ad-provider failures shall degrade according to their documented fallback policies.

**REQ-REL-04 — MUST:** Database/schema migrations shall preserve supported user data and be tested before production.

**REQ-REL-05 — MUST:** Production backups and a tested restore procedure shall exist before launch.

**REQ-REL-06 — MUST:** Known unrecoverable/data-loss failure paths block production release.

---

## 22. Accessibility

**REQ-ACC-01 — MUST:** Interactive controls shall expose meaningful accessibility names/labels.

**REQ-ACC-02 — MUST:** Text and controls shall remain usable with supported platform text/accessibility scaling.

**REQ-ACC-03 — MUST:** Essential actions shall not rely solely on color.

**REQ-ACC-04 — MUST:** Core desktop flows shall support keyboard/focus interaction where applicable.

**REQ-ACC-05 — MUST:** Essential actions shall not require drag-only interaction when an accessible alternative is necessary.

**REQ-ACC-06 — SHOULD:** Motion shall respect supported reduced-motion preferences.

**REQ-ACC-07 — MUST:** Core accessibility failures identified by Phase 8 release gates shall block affected releases.

---

## 23. Security Requirements

**REQ-SEC-01 — MUST:** Production client/server communication shall use secure transport.

**REQ-SEC-02 — MUST:** Authorization shall be enforced server-side for protected resources.

**REQ-SEC-03 — MUST:** Clients shall be treated as potentially compromised.

**REQ-SEC-04 — MUST:** Secrets shall remain outside source code and client packages.

**REQ-SEC-05 — MUST:** Sensitive client credentials shall use appropriate secure storage where available.

**REQ-SEC-06 — MUST:** Authentication, AI, sync, billing, export, and administrative endpoints shall have applicable abuse/rate controls.

**REQ-SEC-07 — MUST:** Billing/provider webhooks shall authenticate provider events and resist replay/duplication.

**REQ-SEC-08 — MUST:** User resource ownership shall be tested against cross-user identifier manipulation.

**REQ-SEC-09 — MUST:** AI-generated or note-embedded instructions shall not bypass normal authorization.

**REQ-SEC-10 — MUST:** Production errors shall not expose secrets, stack internals, or private user content unnecessarily.

**REQ-SEC-11 — MUST:** Privileged administrative access shall be separated, least-privileged, and auditable.

---

## 24. Observability Requirements

**REQ-OBS-01 — MUST:** Production backend shall expose health status without revealing secrets.

**REQ-OBS-02 — MUST:** Important failures shall be observable through structured operational signals.

**REQ-OBS-03 — MUST:** Logs shall support redaction and bounded retention.

**REQ-OBS-04 — MUST:** Production diagnostics shall avoid raw note content by default.

**REQ-OBS-05 — SHOULD:** Correlation/request identifiers shall be available for distributed or multi-step operational troubleshooting where useful.

---

## 25. Configuration & Feature Control

**REQ-CONFIG-01 — MUST:** Business values expected to change shall not be unnecessarily hardcoded.

Examples:
- intelligence thresholds,
- AI quotas,
- entitlement mappings,
- ad eligibility,
- reminder defaults,
- feature availability.

**REQ-CONFIG-02 — MUST:** Configuration changes shall be validated and safely rolled out.

**REQ-CONFIG-03 — MUST:** Ordinary remote feature flags shall not disable authentication, authorization, or critical data-integrity controls.

**REQ-CONFIG-04 — MUST:** Environment-specific configuration and secrets shall remain separated across development, staging, and production.

---

## 26. Administrative Requirements

**REQ-ADMIN-01 — MUST:** Administrative capabilities shall be separated from ordinary user permissions.

**REQ-ADMIN-02 — MUST:** Administrators shall be able to inspect operational health without requiring unrestricted access to private note bodies.

**REQ-ADMIN-03 — MUST:** Subscription/entitlement troubleshooting shall use auditable, least-privileged capabilities.

**REQ-ADMIN-04 — MUST:** High-impact administrative actions shall be authenticated and auditable.

**REQ-ADMIN-05 — SHOULD:** Manual entitlement overrides, if introduced, shall require explicit reason and bounded authority.

---

## 27. Data Portability & Lifecycle

**REQ-DATA-01 — MUST:** User-owned data shall remain accessible after premium expiration.

**REQ-DATA-02 — MUST:** Export shall be scoped to the authenticated requesting user.

**REQ-DATA-03 — MUST:** Deletion shall propagate across active synchronized state according to the deletion lifecycle.

**REQ-DATA-04 — MUST:** Deleted data shall not be restored into active state merely because an older client reconnects.

**REQ-DATA-05 — MUST:** Backup retention semantics shall be documented accurately.

**REQ-DATA-06 — MUST:** AI-derived data associated with a user shall follow the applicable deletion/privacy policy.

---

## 28. MVP Acceptance Boundary

The MVP is acceptable when, on supported platforms/capabilities, a user can:

1. install and launch the application,
2. create/read/edit/archive/delete notes,
3. arrange notes on the visual canvas,
4. persist local state across sessions,
5. use core note functionality offline,
6. create/manage reminders,
7. receive platform-supported reminder surfacing/notifications,
8. use deterministic prioritization,
9. optionally use AI assistance without making core behavior AI-dependent,
10. authenticate and synchronize supported data,
11. recover from common sync/network interruptions without losing local work,
12. understand platform capability/fallback limitations,
13. understand free vs premium access,
14. complete supported subscription/restore flows,
15. receive ads only when eligible,
16. retain access to existing notes after subscription expiry,
17. control privacy-relevant AI/notification behavior,
18. export supported user data,
19. request account deletion,
20. use essential flows with required accessibility support.

---

## 29. Explicit MVP Non-Requirements

The MVP does not require:

- unrestricted autonomous agents,
- identical persistent-surface implementation across all OSs,
- mandatory cloud AI,
- enterprise collaboration/admin suites,
- calendar/location/voice integrations,
- zero-knowledge/end-to-end encryption,
- a web client,
- a custom payment gateway,
- advanced behavioral profiling.

Later additions require their own approved requirements.

---

## 30. Requirements Traceability

Every critical implementation task should map to one or more requirement IDs.

Every **MUST** requirement shall eventually map to:

- implementation ownership,
- acceptance criteria,
- automated test or explicit manual verification,
- relevant platform scope.

P0/P1 test cases in Phase 8 should reference these requirements where practical.

---

## 31. Deferred Decisions

Requirements intentionally do not choose:

- exact local database technology,
- exact auth/token technology,
- exact sync wire protocol,
- exact conflict algorithm implementation,
- exact background worker technology,
- exact AI provider/model,
- exact performance thresholds before benchmarking,
- exact subscription prices,
- exact platform provider SDKs.

These belong to ADRs, later specifications, prototypes, or business decisions.

---

## 32. Phase 2 Consolidated Review Criteria

Before final project freeze, verify:

- all Phase 1 MVP commitments map to requirements,
- Phase 3 architecture can satisfy every MUST requirement,
- Phase 4 UX covers required user states,
- Phase 5 AI rules match AI requirements,
- Phase 6 monetization matches subscription/ad requirements,
- Phase 7 security/privacy rules satisfy the security/privacy requirements,
- Phase 8 test catalog covers critical requirements,
- no later specification silently contradicts a requirement,
- every unresolved decision is explicitly deferred rather than ambiguous.

Phase 2 remains **Draft** until the final consolidated freeze.
