# Release Checklist

**Project:** Smart Sticky Wallpaper  
**Phase:** 10 — Production Readiness & Release  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Provide the practical go/no-go checklist for a release candidate, deployment, staged rollout, and production completion.

---

## 1. Release Identity

- [ ] Release version assigned
- [ ] Source commit identified
- [ ] Backend artifact/image digest recorded
- [ ] Client build/version numbers recorded
- [ ] Migration version recorded
- [ ] Release owner identified
- [ ] Included changes documented
- [ ] Known limitations documented

## 2. Build / Supply Chain

- [ ] Dependency lock state verified
- [ ] Secret scan passed
- [ ] Dependency/security scan reviewed
- [ ] Production secrets absent from artifacts
- [ ] Signing/deploy credentials protected
- [ ] Artifacts immutable/identifiable
- [ ] SBOM/checksum retained where applicable

## 3. Phase 8 Quality

- [ ] G3 passed
- [ ] No unresolved P0
- [ ] No unaccepted Critical security issue
- [ ] P1 exceptions documented/approved
- [ ] Critical E2E passes
- [ ] Regression suite passes
- [ ] Flaky critical tests resolved/approved

## 4. Account / Security

- [ ] Account A→B isolation test passes
- [ ] Cross-user API authorization tests pass
- [ ] Session expiry/revocation works
- [ ] Rate limits active
- [ ] TLS valid
- [ ] Admin access reviewed
- [ ] Logs/secrets redacted
- [ ] Webhook authenticity/replay controls active

## 5. Sync / Data Integrity

- [ ] Offline CRUD passes
- [ ] Atomic local mutation + outbox verified
- [ ] Reconnect/retry passes
- [ ] Duplicate sync mutation safe
- [ ] Concurrent conflict cases pass
- [ ] Tombstones propagate
- [ ] Deleted state does not resurrect
- [ ] Process restart mid-sync recovers
- [ ] Multi-device convergence passes

## 6. Database / Migration

- [ ] Migration reviewed
- [ ] Staging rehearsal passed
- [ ] Deployment compatibility checked
- [ ] Data preservation validation exists
- [ ] Lock/runtime risk reviewed
- [ ] Backup/recovery decision recorded
- [ ] Post-migration validation defined

## 7. Backup / Recovery

- [ ] Recent backup exists
- [ ] Backup job monitored/healthy
- [ ] Retention configured
- [ ] Backup access restricted
- [ ] Restore drill completed
- [ ] Restore includes deletion/tombstone reconciliation
- [ ] RPO/RTO target/provisional values documented

## 8. Backend / Infrastructure

- [ ] Production config validated
- [ ] Reverse proxy/TLS configured
- [ ] Health/readiness works
- [ ] DB connectivity verified
- [ ] Worker health verified if used
- [ ] Monitoring/alerts active
- [ ] Disk/capacity acceptable
- [ ] Rollback artifact available

## 9. AI

- [ ] Production provider/task config correct
- [ ] Structured output validation active
- [ ] Deterministic fallback passes
- [ ] Privacy opt-out passes
- [ ] Prompt-injection boundary passes
- [ ] Quotas/rate/cost controls active
- [ ] Model/provider/task versions recorded
- [ ] Relevant evaluation suite passes
- [ ] Provider/task kill switch tested

## 10. Billing / Entitlements

- [ ] Production product mapping verified
- [ ] Purchase verification works
- [ ] Pending verification safe
- [ ] Renewal/cancellation/grace/expiry verified
- [ ] Refund/revocation verified
- [ ] Restore verified
- [ ] Duplicate/replayed event safe
- [ ] Out-of-order event safe
- [ ] Reconciliation tested
- [ ] Offline cache policy verified
- [ ] Client tampering cannot grant premium

## 11. Advertising / Rewards

- [ ] Premium users ad-free
- [ ] Free eligibility correct
- [ ] Prohibited contexts enforced
- [ ] Consent/privacy config correct
- [ ] Provider receives no private note content
- [ ] No-fill/failure harmless
- [ ] Frequency/cooldown active
- [ ] Reward completion idempotent
- [ ] Cross-account reward replay rejected
- [ ] Ad provider/placement kill switch tested

## 12. Privacy / Lifecycle

- [ ] Note content absent from general telemetry
- [ ] AI external-processing behavior matches disclosure
- [ ] Notification privacy verified
- [ ] Export account scope verified
- [ ] Export temporary artifact expiry works
- [ ] Account deletion lifecycle verified
- [ ] Third-party processor registry current
- [ ] Retention/backup semantics documented

## 13. Accessibility

- [ ] Screen-reader/semantic baseline passes
- [ ] Keyboard/focus flows pass where applicable
- [ ] Text scaling usable
- [ ] Essential drag alternative exists
- [ ] Reduced motion respected where applicable
- [ ] Ads do not trap/obscure focus

## 14. Platform Release

- [ ] Android release validation complete
- [ ] iOS release validation complete
- [ ] Windows release validation complete
- [ ] macOS release validation complete
- [ ] Linux release validation complete
- [ ] Capability matrix matches actual release behavior
- [ ] Unsupported capabilities documented honestly

## 15. Store / Legal

- [ ] Package/bundle IDs correct
- [ ] Permission declarations justified
- [ ] Subscription products configured
- [ ] Privacy declarations accurate
- [ ] Store metadata ready
- [ ] Privacy Policy available
- [ ] Terms/subscription terms available
- [ ] Data deletion instructions available
- [ ] AI/third-party disclosures available as needed

## 16. Deployment Plan

- [ ] Rollout stage/percentage selected
- [ ] Observation window defined
- [ ] Success metrics defined
- [ ] Rollback triggers defined
- [ ] Operator identified
- [ ] Config/feature flags reviewed
- [ ] Release manifest complete

## 17. Rollback / Recovery

- [ ] Previous compatible backend artifact available
- [ ] Schema compatibility checked
- [ ] Migration recovery path understood
- [ ] Optional provider kill switches available
- [ ] Bad-client mitigation ready
- [ ] Responsible operator knows procedure

## 18. Pre-Production Approval

- [ ] Production-readiness evidence reviewed
- [ ] Deployment approved
- [ ] Support/escalation path ready
- [ ] Communication plan ready if needed

## 19. Post-Deploy Smoke

- [ ] API health
- [ ] DB health
- [ ] Login/session
- [ ] Note create/read
- [ ] Sync
- [ ] Entitlement fetch
- [ ] Worker/jobs
- [ ] Provider health
- [ ] Migration result
- [ ] Error rate

## 20. Observation Window

- [ ] No P0 rollback trigger
- [ ] Auth metrics stable
- [ ] Sync metrics stable
- [ ] DB/storage stable
- [ ] Crash/error metrics stable
- [ ] Billing/webhook state stable
- [ ] AI/provider/cost stable

## 21. Production Completion

- [ ] Phase 8 G4 passed
- [ ] Deployment record saved
- [ ] Release evidence linked
- [ ] Known issues updated
- [ ] Rollout status recorded
- [ ] Release marked complete

This checklist remains **Draft** until final consolidated freeze.
