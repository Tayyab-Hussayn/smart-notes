# Smart Sticky Wallpaper

A smart, context-aware wallpaper application.

## Overview

This project is being built following a phased product-and-engineering approach. All documentation
lives under the [`docs/`](docs/) directory, organized by project phase.

## Documentation

- **00 — Project Governance**: [Project Constitution](docs/00-project-governance/PROJECT_CONSTITUTION.md)
- **Phase 01 — Product Discovery**: [Product Vision](docs/phase-01-product-discovery/PRODUCT_VISION.md), [Product Discovery](docs/phase-01-product-discovery/PRODUCT_DISCOVERY.md)
- **Phase 02 — Requirements**: [Software Requirements Specification](docs/phase-02-requirements/SOFTWARE_REQUIREMENTS_SPECIFICATION.md)
- **Phase 03 — Architecture**: [System](docs/phase-03-architecture/SYSTEM_ARCHITECTURE.md), [Client](docs/phase-03-architecture/CLIENT_ARCHITECTURE.md), [Backend](docs/phase-03-architecture/BACKEND_ARCHITECTURE.md), [Data](docs/phase-03-architecture/DATA_ARCHITECTURE.md)
- **Phase 04 — UX/UI**: [UX Specification](docs/phase-04-ux-ui/UX_SPECIFICATION.md), [UI Design System](docs/phase-04-ux-ui/UI_DESIGN_SYSTEM.md)
- **Phase 05 — AI Intelligence**: [AI Architecture](docs/phase-05-ai-intelligence/AI_ARCHITECTURE.md), [Intelligence Rules](docs/phase-05-ai-intelligence/INTELLIGENCE_RULES.md)
- **Phase 06 — Monetization**: [Monetization Specification](docs/phase-06-monetization/MONETIZATION_SPECIFICATION.md)
- **Phase 07 — Security & Privacy**: [Security & Privacy Spec](docs/phase-07-security-privacy/SECURITY_PRIVACY_SPEC.md)
- **Phase 08 — Testing**: [Testing Strategy](docs/phase-08-testing/TESTING_STRATEGY.md)
- **Phase 09 — Development**: [Development Protocol](docs/phase-09-development/DEVELOPMENT_PROTOCOL.md)
- **Phase 10 — Release**: [Production Readiness](docs/phase-10-release/PRODUCTION_READINESS.md), [Release & Deployment](docs/phase-10-release/RELEASE_DEPLOYMENT.md)

## Development Phases

The project is built in clearly defined, sequential phases. Every phase is authored, reviewed,
corrected, and then **frozen** before the next phase begins. Each phase produces documentation that
becomes the source of truth for the implementation stage.

- **Phase 1 → Product Discovery** — Jo hum abhi complete kar rahe hain.
- **Phase 2 → Requirements** — Exactly define hoga app ko kya karna hai.
- **Phase 3 → Architecture** — Exactly define hoga app kaise banegi.
- **Phase 4 → UX/UI** — User experience aur visual/system design.
- **Phase 5 → AI** — AI + deterministic intelligence ka complete behavior.
- **Phase 6 → Monetization** — Subscriptions, ads, entitlements, limits etc.
- **Phase 7 → Security & Privacy** — Security aur privacy ka complete treatment.
- **Phase 8 → Testing & QA** — Test strategy, QA process aur quality gates.
- **Phase 9 → AI-Agent Development Protocol** — Yahan specifically Kimi Swarm ke liye rules honge:
  agents kaise kaam karenge, ownership, Git workflow, reviews, tests, conflict resolution, etc.
- **Phase 10 → Production Release** — Deployment, monitoring, release checklist aur
  production-readiness.

## Freeze Workflow (Sabse Important Rule)

Hum har phase **complete → review → corrections → freeze** karenge. Koi bhi agla phase tabhi start
hota hai jab pichla phase freeze ho chuka ho.

```
Phase 1
   ↓
Draft
   ↓
Our Review
   ↓
Corrections
   ↓
VISION FREEZE                ← Phase 1 frozen
   ↓
Phase 2
   ↓
Requirements Freeze
   ↓
Phase 3
   ↓
Architecture Freeze
   ↓
...
   ↓
ALL DOCUMENTATION COMPLETE
   ↓
KIMI SWARM
   ↓
IMPLEMENTATION
```

## Kimi Swarm Handoff

Kimi ko ek giant prompt nahi dena. Final stage par:

1. Usse poora [`docs/`](docs/) directory denge.
2. Ek **`MASTER_KIMI_INSTRUCTIONS.md`** bhi banayenge jo batayegi ke Kimi ko documents **kis order
   mein read karne hain** aur unke against implementation kaise karni hai.

## Implementation status

Development includes shared local persistence, desktop notes/canvas/reminders and
manual account sync, Android local notes/live wallpaper, and backend account/sync APIs.
The initial desktop/backend CI verification passed. The second CI run failed during
Android plugin setup. Local repairs now pass 23 Kotlin tests, desktop compilation
and Android debug APK assembly; 12 local backend tests pass, with two PostgreSQL
tests skipped. Device and release verification remain incomplete.
No complete app or release is available. See [implementation status](docs/IMPLEMENTATION_STATUS.md).

With JDK 17 installed, run
`./gradlew :client:shared:jvmTest :client:desktop:compileKotlinJvm`.
Launch with `./gradlew :client:desktop:run` (Windows: `gradlew.bat`).
The wrapper pins Gradle 8.14.3 and verifies its distribution checksum.

With Android SDK 35 installed, build the development APK using
`./gradlew -PenableAndroid=true :client:android:assembleDebug`.
Both planned Actions runs have been used; ordinary pushes do not trigger CI.
