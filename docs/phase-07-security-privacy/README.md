# Phase 07 — Security & Privacy

**Version:** 2.0
**Status:** Draft — Consolidated Review Pending

Phase 7 defines security and privacy constraints that every implementation, provider integration, test plan, deployment, and operational process must respect.

## Artifacts

- `SECURITY_PRIVACY_SPEC.md` — mandatory security controls, trust boundaries, authorization, secrets, sync, AI, billing, infrastructure, and release blockers.
- `THREAT_MODEL.md` — explicit threat register, mitigations, residual risks, and abuse-test obligations.
- `DATA_PRIVACY_LIFECYCLE.md` — data classification, storage, provider flows, retention, export, deletion, backups, and processor registry.

## Security Invariants

1. Clients are untrusted for identity, authorization, and entitlement truth.
2. Cross-account data leakage is a release blocker.
3. Private note content is not general telemetry.
4. Provider secrets remain server-side.
5. AI cannot bypass authorization/domain policy.
6. Sync is replay-safe, account-scoped, and deletion-aware.
7. Billing grants require verified provider truth.
8. Export/delete are authenticated and account-scoped.
9. Production secrets/environments remain separated.
10. Known critical privilege/data-loss vulnerabilities block release.

## Cross-Phase Relationship

```text
Phase 2 Security/Privacy Requirements
→ Phase 3 Trust Boundaries
→ Phase 4 Privacy UX
→ Phase 5 AI Security
→ Phase 6 Billing/Ad Security
→ Phase 7 Security & Privacy Controls
→ Phase 8 Abuse/Regression Tests
→ Phase 10 Operational Security
```

## Freeze Policy

Phase 7 remains Draft until project-wide consolidated review.

Before freeze:
- resolve auth/session ADR
- verify account-local storage strategy
- finalize retention durations
- confirm provider registry
- map Critical/High threats to tests
- verify Phase 10 incident/backup procedures
