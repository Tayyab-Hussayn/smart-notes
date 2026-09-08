# Threat Model

**Project:** Smart Sticky Wallpaper
**Phase:** 07 — Security & Privacy
**Version:** 2.0
**Status:** Draft — Consolidated Review Pending
**Purpose:** Identify high-value assets, attackers, trust boundaries, abuse cases, threat scenarios, mitigations, residual risks, and test obligations.

## 1. Security Objectives
Protect:
- user note/reminder confidentiality
- authored-data integrity
- account ownership
- sync correctness
- entitlement integrity
- AI privacy boundary
- admin control
- deletion/export correctness
- service availability
- audit integrity

## 2. Primary Assets
- note/reminder content
- account identity
- session/refresh credentials
- sync metadata
- entitlement state
- billing references
- AI provider credentials
- DB credentials
- signing keys
- webhook secrets
- admin credentials
- backups
- audit/security logs
- export artifacts

## 3. Threat Actors
Consider:
- unauthenticated internet attacker
- malicious authenticated user
- compromised device
- patched client
- credential-stuffing bot
- abusive automation
- compromised provider
- malicious/over-privileged admin
- leaked-secret attacker
- stolen-backup attacker
- supply-chain attacker
- compromised CI/CD runner

## 4. Trust Boundaries
```text
User Input
→ Client Process / OS
→ Network / API
→ Authentication
→ Authorization / Domain
→ Database / Jobs
→ External Providers
→ Admin / Operations
```

## 5. Severity Model
- **Critical** — broad account/data compromise, privileged compromise, major irreversible data loss.
- **High** — meaningful data exposure, authz bypass, entitlement/security bypass.
- **Medium** — limited privacy leak, abuse, integrity issue, meaningful degradation.
- **Low** — narrow low-impact edge case.

## 6. Threat Register

### T-01 Broken Object Authorization — Critical/High
Scenario: attacker changes resource ID to access another user's note/export/device.
Mitigations:
- server ownership checks
- user-scoped queries
- never trust client user IDs
- IDOR tests

### T-02 Cross-Account Local Leakage — Critical
Scenario: account switch exposes prior account notes or entitlement cache.
Mitigations:
- account-scoped local stores/namespaces
- stop old sync
- invalidate account caches
- transition tests

### T-03 Stolen Session Credential — High
Mitigations:
- secure storage
- short-lived access credentials
- refresh rotation
- revocation
- no token logging
- reauthentication for sensitive actions

### T-04 Credential Stuffing — High
Mitigations:
- rate limits
- adaptive password hashing
- suspicious-login monitoring
- careful recovery
- optional MFA later

### T-05 Account Enumeration — Medium
Mitigations:
- generic responses
- rate limits
- minimal account-state leakage

### T-06 Weak Recovery Flow — Critical
Mitigations:
- random one-time token
- short expiry
- replay prevention
- rate limiting
- session invalidation where needed

### T-07 SQL Injection — Critical
Mitigations:
- parameterized DB access
- schema validation
- least privilege
- security tests

### T-08 Command/Path/Template Injection — Critical/High
Mitigations:
- avoid shelling out
- strict path handling
- safe serializers/templates
- allowlisted operations

### T-09 API Flooding / DoS — High
Mitigations:
- rate limits
- proxy limits
- payload caps
- timeouts
- quotas
- monitoring

### T-10 Oversized Payload Exhaustion — Medium
Mitigations:
- note/request size caps
- early rejection
- bounded parsing

### T-11 Sync Stale Overwrite — High
Mitigations:
- revisions
- conflict detection
- deterministic reconciliation
- safe user recovery

### T-12 Deleted-Data Resurrection — High
Mitigations:
- tombstones
- deletion revision
- stale-client policy
- retention window
- regression tests

### T-13 Sync Replay — Medium
Mitigations:
- TLS
- idempotency keys
- revision checks
- authenticated sessions
- deduplication

### T-14 Cross-User Sync Mutation — Critical
Mitigations:
- authenticated account scope
- ownership checks
- ignore client ownership claims

### T-15 AI Cost Exhaustion — High
Mitigations:
- quotas
- model routing
- request caps
- per-user budgets
- rate limiting
- abuse monitoring

### T-16 Prompt Injection — High
Mitigations:
- note text treated as data
- structured outputs
- no unrestricted tools
- domain validation
- user confirmation

### T-17 AI Data Leakage — High
Mitigations:
- data minimization
- opt-out
- task-scoped prompts
- provider review
- no raw prompt logging

### T-18 Hallucinated Consequential Action — High
Mitigations:
- deterministic precedence
- confidence/policy checks
- confirmation
- reversible actions

### T-19 Patched Client Premium Grant — Medium/High
Mitigations:
- server entitlement truth
- authenticated API
- no trusted local premium boolean

### T-20 Replayed Purchase Transaction — High
Mitigations:
- provider verification
- uniqueness
- idempotency
- ownership mapping
- audit

### T-21 Forged Billing Webhook — High
Mitigations:
- signature/certificate validation
- replay protection
- timestamp freshness
- event allowlist
- deduplication

### T-22 Out-of-Order Billing Event — High
Scenario: stale billing event overwrites newer state.
Mitigations:
- provider timestamps/version
- state transition validation
- reconciliation

### T-23 Rewarded-Ad Replay — Medium
Mitigations:
- provider completion verification
- event deduplication
- server-side grant for valuable rewards
- account-scoped reward IDs

### T-24 Ad Provider Privacy Leak — High
Mitigations:
- no note/reminder/AI content
- provider review
- consent handling
- analytics separation

### T-25 Sensitive Log Leakage — High
Mitigations:
- structured logs
- redaction
- no content/token logging
- access control
- retention limits

### T-26 Crash Report Secret Leakage — High
Mitigations:
- scrub breadcrumbs/custom fields
- no auth/provider secrets
- redaction tests

### T-27 Backup Exposure — High
Mitigations:
- protected/encrypted storage
- restricted access
- bounded retention
- access audit where possible

### T-28 Restore Reintroduces Deleted Data — High
Mitigations:
- deletion-aware restore
- post-restore reconciliation
- lifecycle policy
- restore tests

### T-29 Admin Abuse — High
Mitigations:
- least privilege
- separate roles
- no raw content by default
- audit
- break-glass controls

### T-30 Secret Leakage — Critical
Mitigations:
- runtime injection
- secret scanning
- server-only storage
- rotation
- environment separation

### T-31 Malicious Dependency — Critical
Mitigations:
- dependency minimization
- lockfiles
- scanning
- provenance
- SBOM where practical
- controlled upgrades

### T-32 CI/CD Credential Theft — Critical
Mitigations:
- separate build/deploy credentials
- protected environments
- untrusted PR isolation
- artifact provenance

### T-33 Container/VPS Compromise — Critical
Mitigations:
- patched host
- non-root container
- firewall
- restricted daemon
- secret isolation
- monitoring

### T-34 Environment Mixing — High
Mitigations:
- separate credentials/networks/config
- CI safeguards
- no production-data test shortcuts

### T-35 Malicious Export Request — High
Mitigations:
- account-scoped jobs
- authenticated downloads
- expiring links
- ownership checks
- audit

### T-36 Export Artifact Exposure — High
Mitigations:
- private storage
- unguessable references
- expiration
- no public indexing
- cleanup

### T-37 Notification Lock-Screen Leak — Medium
Mitigations:
- configurable preview
- minimal content
- generic mode
- OS privacy controls

### T-38 Deep-Link Abuse — Medium/High
Mitigations:
- validate scheme/host/params
- ownership checks
- confirm consequential actions

### T-39 XSS / Unsafe HTML — High
Applies if web/embedded HTML is introduced.
Mitigations:
- escaping
- CSP
- sanitize rich content
- no raw note HTML rendering

### T-40 CSRF — High
Applies if cookie-authenticated browser flows exist.
Mitigations:
- SameSite
- CSRF token
- origin validation

### T-41 SSRF — High
Applies if backend fetches user-supplied URLs.
Mitigations:
- destination validation
- private-network blocking
- allowlists where practical
- timeout/size limits

### T-42 Malicious Attachment — High
Applies if attachments are introduced.
Mitigations:
- type/size validation
- isolated private storage
- malware/content policy
- authorized downloads

### T-43 Privacy-Invasive New Context Source — High
Mitigations:
- explicit review
- user permission
- data minimization
- revocable access
- no silent use

### T-44 Provider Breach — High/Critical
Mitigations:
- minimize shared data
- provider isolation
- secret rotation
- kill switch
- incident process

### T-45 Unauthorized Provider Callback — High
Mitigations:
- callback authentication
- allowlisted event types
- no direct privileged mutation without validation

## 7. Required Abuse Tests
At minimum:
- another user's note ID
- another user's export ID
- stale account cache after switch
- replay sync operation
- replay/out-of-order billing event
- fake premium flag
- oversized request
- malformed AI output
- prompt-injection note
- stale deleted-note resurrection
- brute-force login
- normal user invoking admin route
- duplicate rewarded-ad completion
- tampered entitlement cache
- invalid webhook signature

## 8. Residual Risks
Not fully eliminable:
- compromised user device
- OS-level notification exposure
- zero-day dependency compromise
- third-party provider breach
- lawful infrastructure access
- user voluntarily sharing data

## 9. Threat Review Triggers
Review before production, after major architecture/auth/sync changes, after new providers or sensitive context integrations, after serious incidents, and periodically during active development.

## 10. Threat-to-Test Traceability
Each Critical/High threat should map to:
- architecture/control
- implementation owner
- Phase 8 test case(s)
- monitoring/detection where applicable
- residual risk statement

## 11. Consolidated Review Criteria
Before final freeze ensure Critical/High threats have concrete mitigation ownership, no privileged boundary is missing, Phase 3 V2 can implement controls, Phase 5/6 provider threats are represented, Phase 8 can test abuse cases, and Phase 10 can detect/respond operationally.

Phase 7 remains **Draft** until final project freeze.
