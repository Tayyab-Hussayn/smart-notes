# Advertising Specification

**Project:** Smart Sticky Wallpaper
**Phase:** 06 — Monetization
**Version:** 2.0
**Status:** Draft — Consolidated Review Pending
**Purpose:** Define ad eligibility, formats, placement restrictions, provider abstraction, frequency controls, rewarded-ad integrity, privacy/consent, accessibility, failure behavior, and cross-platform fallback.

## 1. Advertising Objective

Advertising may subsidize the free tier without degrading core trust or note-taking.

Priority:

```text
User Task
>
Data Safety
>
Reminder Reliability
>
Accessibility
>
Privacy
>
Monetization
```

Ads always yield to higher priorities.

## 2. Core Principles

1. Ad-removal entitlement means no product ads.
2. Ads never masquerade as notes/system UI.
3. Ads never block access to user-owned notes.
4. Core workflows do not depend on ad availability.
5. Ad SDKs remain isolated.
6. Private note/reminder/AI content is never ad payload.
7. Rewarded ads are explicit opt-in.
8. Frequency is bounded.
9. Consent/platform rules are honored.
10. Unsupported ad platforms degrade cleanly to no-ad behavior.

## 3. Eligibility Decision

An ad may be requested only when all are true:

```text
Eligible Commercial State
AND feature.ads.remove == false
AND platform supports ads
AND region/config allows ads
AND consent/privacy policy permits
AND placement policy permits
AND frequency policy permits
AND provider available
```

If any condition fails, no ad is shown.

## 4. Eligibility Ownership

Backend/config may define commercial/config eligibility.

Client still enforces:
- placement context
- UX prohibition zones
- capability state
- current ad-removal entitlement

Client cannot override verified ad-removal.

## 5. Provider Abstraction

Conceptual interface:

```text
AdService
- initialize()
- availability()
- requestBanner()
- requestNative()
- showInterstitial()
- showRewarded()
- dispose()
```

Provider callbacks normalize into application-level events.

SDK types stay out of core domains.

## 6. Adapter Responsibilities

Adapter may own:
- SDK initialization
- consent integration
- placement config
- loading/caching
- impression/click callbacks
- rewarded completion
- lifecycle cleanup
- error normalization

Adapter does not decide entitlement.

## 7. Allowed Formats

Potential:
- banner
- clearly labeled native ad
- rewarded ad
- limited interstitial

Initial policy should favor least disruptive formats.

SDK support alone does not imply product approval.

## 8. Global Prohibited Contexts

No ads:
- while typing/editing
- over note content
- during drag/resize
- during reminder confirmation
- during auth credential entry
- during purchase confirmation
- during export/delete confirmation
- over critical recovery controls
- over accessibility controls
- as fake warning/system UI
- styled like a user-created sticky note

## 9. Banner Policy

Banner placement must:
- stay outside note content
- have clear visual separation
- avoid accidental taps
- not overlay keyboard/editor
- collapse cleanly on no-fill/failure
- disappear promptly after ad-removal activates

Avoid layout jumps where practical.

## 10. Native Ad Policy

If used:
- label as sponsored/ad
- do not imitate sticky-note visual language
- separate from authored note collections/surfaces
- avoid reminder/action sequences
- maintain accessible labeling

## 11. Interstitial Policy

Interstitials are optional and discouraged unless data supports acceptable UX.

Never show:
- at app launch by default
- immediately after note creation
- during save
- during reminder action
- after destructive confirmation
- repeatedly during navigation
- when user urgently opens existing notes

If used, require a natural transition plus frequency/cooldown eligibility.

## 12. Rewarded Ads

Rewarded ads are user-initiated.

Possible temporary rewards:
- additional AI requests
- temporary premium theme access
- short feature trial
- bonus quota

Rewards must not:
- unlock existing user data
- create permanent paid entitlement
- bypass billing verification
- unlock security-sensitive/admin features

## 13. Reward Flow

```text
User requests reward
→ Explain reward
→ Check eligibility
→ Show rewarded ad
→ Provider completion
→ Validate event
→ Deduplicate
→ Grant temporary reward
→ Persist source/expiry
→ Update UI
```

No completion means no false reward.

## 14. Reward Idempotency

A completion event is grantable at most once.

Use:
- provider reward/event ID
- server-side grant record
- internal idempotency key

Repeated callbacks produce same final result.

## 15. Reward State

Reward record identifies:
- user/account
- reward type
- source provider/event
- grant time
- expiry/consumption
- status

Reward cannot impersonate subscription state.

## 16. Premium Transition

When ad-removal activates:
- stop future requests
- discard loaded ads where practical
- collapse ad containers
- cancel pending interstitial flow
- preserve layout
- avoid restart where possible

## 17. Premium Expiry

Ads may resume only after verified entitlement update and normal eligibility checks.

Do not show an interstitial merely because premium expired.

## 18. Frequency Policy

Configurable:
- max impressions/session
- max interstitials/session/day
- cooldown
- minimum session age
- navigation cooldown
- repeated-dismissal suppression
- rewarded availability

Thresholds remain configurable and testable.

## 19. User Interaction Suppression

After intensive activity such as:
- rapid editing
- drag/rearrange
- reminder interaction
- error recovery

ad display should remain suppressed for a suitable cooldown.

## 20. Offline / No-Fill / Failure

```text
Ad unavailable
→ remove/hide placement
→ preserve layout
→ continue core product
```

No blocking empty screen.

Reward verification may remain pending only if product policy permits safe later verification.

## 21. Consent

Ad initialization/request respects applicable:
- platform tracking permission
- regional consent
- provider consent state
- user privacy preferences
- future age/eligibility policy

If required consent is absent, use lawful non-personalized/no-ad fallback as applicable.

## 22. Data Isolation

Never send ad providers:
- note body
- reminder body
- AI prompt/response
- private category/label
- canvas content
- auth token
- subscription secrets
- sync payload

Ad targeting must never inspect private note semantics.

## 23. Analytics Separation

Ad analytics may record:
- request
- load
- failure
- impression
- click
- rewarded start/completion
- reward grant

No private note content.

Ad analytics and note/product analytics remain logically separated.

## 24. Accessibility

Ad containers must:
- not trap keyboard focus
- expose sponsorship labeling where provider permits
- preserve logical focus order
- not cover controls
- respect reduced motion where practical
- avoid unexpected auto-focus

If a provider cannot meet critical accessibility needs, disable that format/provider on the affected platform.

## 25. Platform Capability Model

Registry should represent:
- ads supported
- supported formats
- rewarded supported
- consent required
- provider initialized
- temporary failure
- unsupported state

Unsupported platforms behave as no-ad.

## 26. Desktop Strategy

Desktop ads are not assumed.

Options:
- no ads
- limited banner/native
- premium upsell without third-party ad SDK
- future approved provider

Exact strategy requires provider/policy research.

## 27. Ad Configuration

Remote/server config may define:
- enabled platforms/regions
- allowed formats
- placement IDs
- frequency caps
- rewarded mapping
- kill switch
- minimum app/session version

Provider secrets do not belong in public config.

## 28. Kill Switch

Operations must be able to disable:
- provider
- format
- placement
- rewarded ads

without disabling core notes.

Kill switch must not grant unauthorized premium.

## 29. Provider Switching Review

Changing provider requires review of:
- SDK permissions
- data collection
- consent behavior
- platform support
- accessibility
- security
- privacy disclosures

Abstraction does not eliminate privacy review.

## 30. Fraud & Abuse

Reward flow should resist:
- fake completion callbacks
- replayed callbacks
- modified local reward state
- duplicate grants
- cross-account replay

High-value rewards should use server validation.

## 31. Testing

Required:
- eligible free user
- premium/no-ad user
- entitlement change in-session
- no-fill
- offline
- SDK init failure
- consent denied
- prohibited placement
- frequency cap
- interstitial cooldown
- rewarded completion
- duplicate reward
- reward cancellation
- account switch
- unsupported platform
- accessibility/focus behavior

## 32. Operational Metrics

Monitor:
- load success
- no-fill
- provider errors
- impressions
- reward verification failures
- duplicate reward attempts
- provider initialization health

Metrics remain privacy-safe.

## 33. Non-Goals

Not approved:
- aggressive frequency
- forced rewarded ads
- private-note targeting
- ads disguised as notes
- blocking full-screen ads in critical flows
- permanent premium through ads
- ad dependency for startup
- unsupported SDK hacks

## 34. Consolidated Review Criteria

Before final freeze verify:
- eligibility matches Phase 2/6 entitlements
- provider boundary matches Phase 3 V2
- placements match Phase 4 V2
- rewarded AI benefits match Phase 5 V2
- privacy/consent matches Phase 7
- tests map to Phase 8
- provider/kill-switch setup maps to Phase 10

Phase 6 remains **Draft** until final project freeze.
