# Security & Privacy Specification

**Project:** Smart Sticky Wallpaper  
**Phase:** 07 — Security & Privacy  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define mandatory security controls, privacy boundaries, trust assumptions, authorization rules, data-protection requirements, abuse controls, operational safeguards, and security acceptance criteria.  
**Governed By:** `PROJECT_CONSTITUTION.md`

## 1. Security Objectives
Protect confidentiality of notes/reminders, authored-data integrity, account/session ownership, synchronization integrity, entitlement integrity, AI privacy boundaries, administrative control, deletion/export correctness, operational availability, and auditability.

## 2. Core Principles
1. Treat every client as potentially compromised.
2. Enforce authorization server-side.
3. Apply least privilege.
4. Never expose server/provider secrets to clients.
5. Validate all external input.
6. Treat AI input/output as untrusted.
7. Keep private note content out of general telemetry.
8. Encrypt data in transit.
9. Minimize sensitive collection and retention.
10. Fail closed for new privilege grants.
11. Fail safely without destroying user-authored data.
12. Security-sensitive actions must be auditable.
13. Account isolation is non-negotiable.
14. Security controls must be testable.
15. Do not invent custom cryptography.

## 3. Trust Boundaries
```text
User
↓
Client / Device
↓
Authenticated API Boundary
↓
Application / Authorization Boundary
↓
Persistence / Job Infrastructure
↓
External Providers
  ├── AI
  ├── Billing
  ├── Ads
  └── Notifications
```
Each crossing requires explicit validation.

## 4. Client Trust Model
The client is not trusted to assert identity, resource ownership, premium status, subscription validity, AI quota, server timestamps, final sync truth, admin role, or deletion authority.

## 5. Authentication Requirements
Authentication must use established mechanisms, never store plaintext passwords, use modern adaptive password hashing if passwords exist, support logout/revocation, protect recovery flows, resist enumeration where practical, rate-limit abuse, and support secure session renewal.

## 6. Session Strategy
Recommended baseline:
- short-lived access credential
- longer-lived refresh/session credential
- revocation support
- rotation where appropriate
- device/session metadata where useful
- secure expiration handling

Exact token technology remains ADR-controlled.

## 7. Access Credential Rules
Access credentials should be short-lived, minimal, content-free, validated on protected requests, support key rotation, reject malformed/expired tokens, and not rely only on client time.

## 8. Refresh / Session Credential Rules
Refresh/session credentials must use secure storage, never be logged, be revocable, support rotation where applicable, be session/device-scoped where useful, and be invalidated appropriately on logout/security events.

## 9. Secure Client Storage
Use platform-secure storage where available for refresh/session credentials, device secrets, cryptographic keys, and tamper-sensitive entitlement cache metadata.

## 10. Local Content Protection
Local user data should use OS storage protections. Additional database/file encryption may be adopted when the threat model or product requirements justify it.

## 11. Authorization Model
Every protected action validates:
1. authenticated user
2. resource ownership/access grant
3. action permission
4. role/entitlement if applicable
5. resource state constraints

Resource ownership must never be inferred from a client-provided `user_id`.

## 12. Resource Ownership
User-owned resources include notes, canvas placements, reminders, settings, sync state, AI-derived metadata, device records, export jobs, and reward records.

## 13. Account Switching
On account switch/sign-out:
- stop previous account sync
- invalidate account-bound entitlement cache
- isolate/clear session-bound state as required
- prevent old data from rendering during transition
- load only active account data

Cross-account leakage is a release blocker.

## 14. Administrative Authorization
Admin capabilities require separate roles, least privilege, strong authentication, route/control separation, audit logs, and additional controls for high-impact actions where justified.

## 15. Break-Glass Access
If emergency access to sensitive content is ever required, it must be exceptional, purpose-documented, strongly authenticated, time-bounded where practical, audited, and reviewed afterward.

## 16. Secrets Management
Secrets include DB credentials, AI keys, billing credentials, webhook secrets, signing keys, encryption keys, and notification credentials.

Rules:
- server-side only
- runtime injection
- environment separation
- no source control
- rotate after compromise
- least-privileged access
- do not expose to agents unless required

## 17. Key Rotation
Operational design must support rotation of token-signing keys, provider secrets, DB credentials, and webhook verification keys where applicable.

## 18. Transport Security
Production client/server traffic uses HTTPS/TLS with valid certificates, modern TLS, hostname validation, and protected provider callbacks.

## 19. Database Security
PostgreSQL requirements:
- non-public exposure
- least-privileged application role
- restricted network access
- parameterized queries
- controlled migrations
- protected backups
- restricted direct production access

## 20. Input Validation
Validate JSON/schema, IDs, strings/lengths, timestamps, enums, pagination, sort/filter values, URLs, provider callbacks, AI structured output, and export parameters. Reject oversized payloads early.

## 21. Injection Protection
Protect against SQL injection, command injection, path traversal, template injection, unsafe deserialization, log injection, unsafe HTML rendering if introduced, and prompt injection crossing into privileged actions.

## 22. API Abuse Controls
Rate-limit classes should include login/registration/recovery, AI, sync writes, exports, billing verification, webhooks, admin operations, and expensive search endpoints.

## 23. Sync Security
Sync must enforce authenticated account context, ownership, revision validation, idempotency, replay resistance, payload limits, account isolation, deterministic conflict policy, and deletion/tombstone handling.

## 24. Device Identity
Device IDs may support session association, sync diagnostics, revocation, and coarse platform capability metadata, but never replace user authentication.

## 25. AI Security
AI requires server-side provider secrets, minimum required data, normal auth/authz, schema-validated output, no implicit tool authority, normal domain validation after AI, and no model-driven entitlement/security decisions.

## 26. Prompt Injection Boundary
```text
Note Text
→ Treated as Data
→ AI Task Contract
→ Structured Output
→ Validation
→ Domain Policy
→ Optional User Confirmation
```
Model output cannot bypass authorization.

## 27. AI Provider Security Review
Before adopting/changing a provider, document data sent, credentials model, retention, training/use policy, region constraints, deletion controls, security posture, and fallback/disable path.

## 28. Billing Security
Required:
- server-side verification
- authenticated provider events
- replay/duplicate protection
- out-of-order event handling
- no client-authoritative premium
- auditable grants/revocations
- fail closed for unverifiable new grants

## 29. Webhook Security
Provider webhooks should support signature/certificate verification, timestamp freshness, replay protection, event ID deduplication, payload-size limits, schema/event allowlisting, safe retry, and security failure logging.

## 30. Advertising Privacy & Security
Ad systems must not receive note text, reminder text, AI prompts/responses, private labels/categories, canvas content, auth credentials, or sync payloads.

## 31. Notification Privacy
Notification content should minimize sensitive information and support reduced-detail or generic previews where possible.

## 32. Logging Rules
Never log by default:
- passwords
- access/refresh tokens
- provider secrets
- raw payment credentials
- raw note bodies
- full AI prompts
- private export contents

Use structured redaction.

## 33. Security Event Logging
Useful events include auth failure, session revocation, authorization failure, rate-limit abuse, billing verification failure, webhook auth failure, admin action, account deletion, export generation, and suspicious sync/replay attempts.

## 34. Telemetry
Telemetry must be purpose-limited, privacy-minimal, documented, separated from note content, and disableable/reducible where policy requires.

## 35. Error Handling
Production errors must not expose stack traces, DB schema, internal paths, secrets, provider credentials, or authorization internals.

## 36. Account Recovery
Recovery must verify control of the recovery channel, use expiring one-time credentials, resist replay, rate-limit, minimize enumeration, invalidate sessions when appropriate, and log relevant events.

## 37. Data Export Security
Export requires authentication, account scope, protected temporary artifacts, expiration, no public indexing, audit metadata, and no internal/provider secrets.

## 38. Account Deletion Security
Deletion requires authenticated confirmation, session revocation, sync disablement, ownership-scoped deletion, stale-client resurrection prevention, provider cleanup where required, and clear lifecycle status.

## 39. Backup Security
Backups should be protected/encrypted where supported, access-restricted, retention-bounded, not copied casually, tested for restore, and deletion-aware during restoration.

## 40. Environment Separation
Separate development, staging/integration, and production. Never reuse production secrets in dev or send production user data to test providers.

## 41. Dependency / Supply-Chain Security
Minimize dependencies, pin/lock versions, scan vulnerabilities, review high-risk SDKs, verify provenance where supported, patch critical issues promptly, and retain an SBOM where practical.

## 42. CI/CD Security
Build/release pipeline should protect signing/deploy credentials, restrict production permissions, isolate untrusted PR execution from production secrets, preserve artifact provenance, and scan for committed secrets.

## 43. Container & VPS Security
Production baseline:
- minimal images
- non-root execution where practical
- patched host/runtime
- firewall
- restricted ports
- protected Docker daemon
- runtime secret injection
- monitoring
- backup
- limited SSH/admin access

## 44. SSRF / Outbound Requests
If backend later fetches user/provider-supplied URLs, validate destinations, block private network targets, use allowlists where practical, enforce timeouts/size limits, and prevent metadata-service access.

## 45. Attachments
If attachments are introduced later, require a separate threat/privacy review covering type/size validation, private storage, malware/content handling, and authorized downloads.

## 46. Deep Links / External Intents
Validate accepted schemes/domains, parameters, resource ownership, and action scope. Consequential actions require confirmation.

## 47. Browser/Web Surface
If browser or embedded web UI is introduced, add XSS/CSP/safe-rendering controls and CSRF protections if cookie-authenticated flows exist.

## 48. Incident Response
```text
Detect
→ Classify
→ Contain
→ Preserve Evidence
→ Rotate/Revoke if Needed
→ Assess Impact
→ Recover
→ Communicate as Required
→ Postmortem
→ Prevent Recurrence
```

## 49. Security Review Triggers
Formal review before production launch, new identity/AI/billing/ad provider, new admin capability, sensitive context source, attachments, major auth/sync changes, or major incident remediation.

## 50. Security Testing
Required categories:
- authentication
- authorization/IDOR
- session expiry/revocation
- account switching
- injection
- rate limiting
- sync replay/conflict/deletion
- AI prompt injection
- billing fraud/webhooks
- export authorization
- deletion lifecycle
- admin privilege escalation
- provider failure boundaries

## 51. Release Blockers
Production must not ship with known:
- auth bypass
- cross-user access
- critical secret exposure
- exploitable SQL/command injection
- unverified entitlement grant path
- destructive sync/data-loss path
- unauthenticated privileged webhook mutation
- unrestricted AI privileged-action path

## 52. Non-Goals
This phase does not mandate zero-knowledge architecture, end-to-end encryption for all note content, custom cryptography, enterprise certifications, or hardware-backed encryption on every platform.

## 53. Consolidated Review Criteria
Before final freeze verify Phase 2 requirements, Phase 3 trust boundaries, Phase 4 privacy UX, Phase 5 AI controls, Phase 6 billing/ad controls, Phase 8 abuse tests, and Phase 10 operational procedures all align.

Phase 7 remains **Draft** until final project freeze.
