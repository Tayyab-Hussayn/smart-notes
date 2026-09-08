# Data Privacy Lifecycle

**Project:** Smart Sticky Wallpaper
**Phase:** 07 — Security & Privacy
**Version:** 2.0
**Status:** Draft — Consolidated Review Pending
**Purpose:** Define how user data is created, stored, synchronized, processed, shared, retained, exported, deleted, backed up, and governed across the product lifecycle.

## 1. Privacy Principles
1. Collect only what is needed.
2. Use data only for defined purposes.
3. Keep private content out of general telemetry.
4. Separate authored and derived data.
5. External processing must be visible and controllable.
6. Preserve user ownership.
7. Support export and deletion.
8. Retain data only as long as justified.
9. Default to privacy-preserving behavior.
10. New integrations require new review.
11. Account switching cannot expose prior-account data.
12. Monetization does not change ownership.

## 2. Data Classification

### Account Data
- user ID
- email/identity metadata
- account status
- security/session metadata

### User-Authored Content
- note title/body
- reminder text
- manual category
- labels
- canvas placement

### Deterministic-Derived Data
- parsed reminder time
- recurrence state
- deterministic ranking values
- sync revisions

### AI-Derived Data
- inferred category
- reminder suggestion
- routine hypothesis
- relevance
- summary
- confidence/provenance

### Device / Sync Metadata
- device ID
- platform
- operation ID
- revision/cursor
- last sync

### Billing / Entitlement Data
- provider
- transaction reference
- product/plan
- entitlement
- lifecycle timestamps

### Advertising Data
- provider
- consent
- eligibility
- placement/reward metadata

### Operational Telemetry
- error code
- latency
- success/failure
- app/platform version
- coarse product state

Raw note content is excluded by default from telemetry.

## 3. Data Ownership
User-authored content belongs to the user's account context.

Subscription expiry, ad eligibility, or AI quota does not change ownership.

Existing data remains accessible according to Phase 6 guarantees.

## 4. Data Creation Flow
```text
User Input
→ Local Validation
→ Local Persistence
→ Optional Deterministic Derivation
→ Optional AI Eligibility
→ Outbox / Sync
→ Server Storage if Sync Enabled
```

AI is not required for basic persistence.

## 5. Local Storage
Local storage may contain:
- notes
- reminders
- placements
- settings
- sync state
- selected derived metadata
- bounded entitlement cache

Session secrets belong in secure platform storage.

## 6. Account-Scoped Local State
On account switch:
- previous account data stops rendering
- prior session cache stops authorizing
- sync context changes
- new account data loads independently

## 7. Server Storage
Server may store only data justified by product/operational needs, including:
- account record
- synced notes/reminders/placements/settings
- sync metadata
- entitlements
- billing references
- approved AI-derived data
- security/audit records

Do not retain unnecessary raw third-party payloads indefinitely.

## 8. Synchronization Privacy
Sync transmits only required user state and metadata.

Requirements:
- authenticated transport
- ownership
- revision/tombstone metadata
- idempotency
- account isolation
- no cross-user blending

## 9. AI Processing Lifecycle
```text
Eligible Data
→ Privacy / Feature Check
→ Data Minimization
→ Provider Request
→ Structured Response
→ Validation
→ Derived Result
→ Optional Persistence
```

Only send task-required context.

## 10. AI Opt-Out
When external AI is disabled:
- cloud AI requests stop
- deterministic intelligence may continue if separately enabled
- core notes continue
- authored data remains unchanged
- persisted AI-derived metadata follows deletion/retention policy

## 11. AI Provider Registry
Before provider adoption record:
- provider
- purpose
- data categories
- retention
- training/use policy
- processing region where relevant
- deletion controls
- contract/privacy assumptions
- kill-switch/removal path

Review periodically.

## 12. Billing Data Lifecycle
Store only what is required for:
- verification
- entitlement state
- reconciliation
- fraud prevention
- support
- legal/accounting obligations

Do not store raw card number/CVV.

## 13. Advertising Data Lifecycle
Never send ad providers:
- note/reminder content
- AI prompts
- private labels
- canvas content
- sync payloads
- auth credentials

Respect consent/tracking requirements.

## 14. Notification Privacy
Minimize notification content.

Support where feasible:
- full preview
- limited/generic preview
- hidden preview
- OS privacy behavior

## 15. Analytics
Prefer:
```text
event_name
feature
platform
success/failure
duration
coarse entitlement state
```

Exclude:
```text
note_body
reminder_text
AI_prompt
tokens
provider secrets
```

## 16. Crash Reporting
May include:
- stack trace
- app version
- platform
- device class
- error code

Must avoid:
- note text
- auth tokens
- provider keys
- billing secrets

## 17. Data Inventory
Before production maintain:
```text
data_category
purpose
source
storage_location
security_classification
external_recipient
retention
deletion_path
exportability
account_scope
```

Update when features/providers change.

## 18. Retention Policy
Every category needs explicit retention rationale.

| Data | Principle |
|---|---|
| Active notes | Until user deletion/account lifecycle |
| Tombstones | Sync-safe bounded period |
| AI-derived metadata | Until removal/recompute/policy expiry |
| Security logs | Bounded security/operational need |
| Billing records | Provider/legal/audit need |
| Export artifacts | Short-lived |
| Backups | Rotating bounded retention |

Exact durations are finalized before production.

## 19. Soft Delete
Soft deletion may support:
- undo
- sync propagation
- stale-client protection
- accidental recovery

Soft-deleted records cannot remain indefinitely without policy.

## 20. Hard Delete
Hard deletion removes active application copies after policy requirements are satisfied.

Deletion scope includes as relevant:
- primary DB
- derived tables
- search indexes
- AI-derived metadata
- active caches
- export artifacts

Tombstone timing follows sync safety.

## 21. Account Deletion
```text
Authenticated Request
→ Explain Impact
→ Confirm / Reauthenticate if Required
→ Revoke Sessions
→ Disable Sync
→ Mark Deletion State
→ Delete User-Owned Active Data
→ Delete Derived Data
→ Provider Cleanup Where Applicable
→ Backup Aging
→ Record Completion State
```

Subscription cancellation at provider/store may require separate user action.

## 22. Backup Deletion Semantics
Immediate removal from immutable backups may not be technically practical.

Policy must ensure:
- access restriction
- bounded retention
- deleted data ages out
- restored backups do not reactivate deleted users/data without reconciliation

Communicate this accurately.

## 23. Data Export
Export may include:
- notes
- reminders
- user-created categories
- canvas metadata
- settings where useful

Do not include:
- provider credentials
- auth secrets
- other users' data
- internal security metadata

## 24. Export Security
Export should:
- require authentication
- be account-scoped
- use protected temporary artifacts
- expire
- be deleted after expiry
- be non-indexable
- require reauth for high-risk cases if policy requires

## 25. Export Format
Prefer portable formats:
- JSON
- Markdown/plain text for notes
- CSV for suitable tabular subsets

Exact package structure is implementation-level.

## 26. User Correction
Users should be able to correct:
- authored content
- reminders
- categories
- settings
- accepted AI-derived assumptions where exposed

Derived data should be recomputable/removable.

## 27. Privacy Settings
Potential settings:
- cloud AI enabled
- contextual intelligence enabled
- notification preview
- analytics/diagnostics consent where applicable
- ad/tracking consent
- connected integrations
- export
- account deletion

Controls must explain actual behavior.

## 28. Third-Party Processor Registry
Maintain an internal registry for:
- AI
- billing
- ads
- notifications
- analytics/crash
- future integrations

For each record:
- purpose
- data categories
- credentials/access model
- retention assumptions
- region/platform scope
- removal/disable path

## 29. Future Calendar Integration
Requires separate review.

Principles:
- narrow scopes
- minimum fields
- no unnecessary full-calendar import
- separate imported vs authored data
- disconnect/delete support

## 30. Future Location Context
Requires separate privacy review.

If approved:
- explicit permission
- clear benefit
- minimum precision
- minimum retention
- disable option
- no ad use without independent policy/consent

## 31. Future Voice
Must separately define:
- on-device vs cloud processing
- provider
- retention
- transcription lifecycle
- consent
- deletion
- derived text ownership

## 32. Age-Sensitive Use
If the product later intentionally targets minors or uses age-sensitive data, perform dedicated product/legal/privacy review.

This specification does not claim universal youth compliance.

## 33. Legal/Policy Documentation
Before production provide as applicable:
- Privacy Policy
- Terms of Service
- subscription terms
- data deletion instructions
- AI processing disclosure
- third-party disclosures

Legal review depends on launch regions and business model.

## 34. Data Breach Lifecycle
```text
Contain
→ Identify Systems/Data
→ Preserve Evidence
→ Rotate/Revoke
→ Assess Scope
→ Recover
→ Notify as Required
→ Remediate
→ Update Threat Model
```

## 35. Privacy Testing
Test:
- AI-disabled path
- account switching isolation
- analytics without note text
- deletion propagation
- no deleted-note resurrection
- export scope
- notification previews
- AI request minimization
- ad data isolation
- backup restore after deletion
- provider kill-switch behavior

## 36. Privacy Change Review
Any feature adding a new data category, external recipient, sensitive context source, retention purpose, or tracking behavior requires privacy review before implementation freeze.

## 37. Non-Goals
This document does not claim zero retention, zero-knowledge architecture, immediate deletion from every immutable backup, compliance with every jurisdiction, or anonymous account-free usage.

## 38. Consolidated Review Criteria
Before final freeze verify the data inventory matches architecture, Phase 5 AI data flow is represented, Phase 6 billing/ad flow is represented, retention/deletion/export map to requirements, Phase 4 privacy UX matches behavior, Phase 8 tests cover boundaries, and Phase 10 backup/incident operations preserve privacy semantics.

Phase 7 remains **Draft** until final project freeze.
