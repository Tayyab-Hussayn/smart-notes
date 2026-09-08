# AI Architecture

**Project:** Smart Sticky Wallpaper  
**Phase:** 05 — AI & Intelligence  
**Version:** 2.0  
**Status:** Draft — Consolidated Review Pending  
**Purpose:** Define the provider-neutral AI architecture, request lifecycle, trust boundaries, data handling, cost controls, observability, testing, and future model evolution.  
**Governed By:** `PROJECT_CONSTITUTION.md`  
**Primary Inputs:** Phase 02 SRS V2, Phase 03 Architecture V2, Phase 04 UX/UI V2

---

## 1. Architectural Position

AI is an optional enrichment subsystem.

It is not:

- the source of truth for notes,
- the source of truth for reminders,
- an authorization engine,
- an entitlement engine,
- a sync conflict resolver,
- a deletion authority,
- a mandatory dependency for core product behavior.

Core note CRUD and deterministic reminder behavior must remain functional without AI.

---

## 2. Decision Precedence

```text
Explicit User Intent
>
Hard Product / Domain Rules
>
Deterministic Context
>
Validated AI Assistance
```

AI may influence suggestions and ranking, but cannot override higher-precedence rules.

---

## 3. AI Responsibilities

AI may assist with:

- semantic classification,
- date/time interpretation,
- reminder suggestions,
- routine hypotheses,
- contextual relevance,
- note grouping,
- summarization,
- natural-language interpretation,
- optional semantic search,
- personalization suggestions.

AI should be used primarily where deterministic logic is insufficient or semantic interpretation adds meaningful value.

---

## 4. Non-Responsibilities

AI must not directly own:

- authentication,
- authorization,
- billing truth,
- entitlement truth,
- final reminder scheduling authority,
- user-authored source data,
- sync conflict resolution,
- permanent deletion,
- security policy,
- account switching,
- provider secrets.

---

## 5. Provider-Neutral Gateway

All remote/local AI implementations sit behind an owned interface.

Conceptually:

```text
Application Service
  ↓
AiGateway
  ↓
Policy / Eligibility / Quota Layer
  ↓
Provider Router
  ↓
Provider Adapter
  ├── CloudProviderA
  ├── CloudProviderB
  ├── OnDeviceProvider
  └── MockProvider
```

Domain/application code must not depend on provider SDK types.

---

## 6. Task-Oriented Contracts

Prefer narrow task contracts over one generic “chat” interface.

Examples:

```text
classifyNote()
extractDateTime()
inferContext()
detectRoutine()
rankCandidates()
summarizeNotes()
generateSuggestion()
semanticSearch()
```

Each task should define:

- input schema,
- output schema,
- required context,
- confidence semantics,
- privacy class,
- quota class,
- timeout budget,
- fallback behavior.

---

## 7. Request Lifecycle

Every AI request follows:

1. validate application request,
2. authenticate user/session if server-backed,
3. authorize AI capability,
4. check feature enablement,
5. check user AI/privacy preferences,
6. check entitlement/quota/rate limit,
7. determine whether deterministic logic is sufficient,
8. minimize data,
9. select provider/model,
10. execute with bounded timeout,
11. validate structured output,
12. apply policy/confidence checks,
13. persist derived result only if justified,
14. return normalized result,
15. record privacy-safe operational metrics.

No provider call may bypass this lifecycle.

---

## 8. AI Eligibility

Skip AI when:

- AI is disabled,
- external AI consent is absent,
- deterministic logic already resolves the case,
- feature is unavailable,
- entitlement/quota does not allow it,
- no network/provider is available for a cloud-only task,
- required data would violate privacy policy,
- expected usefulness does not justify cost/latency.

---

## 9. Structured Outputs

AI output should be schema-constrained wherever possible.

Conceptual result:

```json
{
  "schema_version": 1,
  "task": "reminder_suggestion",
  "result": {
    "date": "2026-09-09",
    "time_window": "morning"
  },
  "confidence": 0.91,
  "reason_code": "explicit_relative_date_semantic_time",
  "requires_confirmation": true
}
```

Validate:

- schema version,
- required fields,
- enum values,
- lengths,
- date/time format,
- confidence bounds,
- unsupported action requests.

Malformed output is discarded.

---

## 10. Explanation Boundary

The system may expose concise product explanations such as:

- “Due this morning”
- “Suggested from your note text”
- “Often useful around this time”

It must not expose hidden model reasoning or chain-of-thought.

Explanations should be based on structured reason codes or approved summaries.

---

## 11. Confidence Model

Confidence is advisory and task-specific.

Recommended semantic levels:

- **High** — strong evidence; may prefill/suggest.
- **Medium** — plausible; confirmation normally required for consequential actions.
- **Low** — insufficient; avoid consequential action.

Confidence alone must never bypass domain rules.

Thresholds remain configurable and testable.

---

## 12. Consequential Action Policy

AI-inferred actions that materially change user state require confirmation unless the user explicitly and unambiguously requested the action.

Examples:

- create ambiguous reminder,
- change recurrence,
- rewrite authored content,
- alter priority based only on inference,
- send content to another external service,
- destructive action.

AI proposes; application/domain code executes after normal validation.

---

## 13. Authored vs Derived State

Persisted information must distinguish:

### User Authored
Directly entered/edited by the user.

### Deterministic Derived
Produced by parsers/rules.

### AI Derived
Produced by a model.

### Provider Metadata
Provider/model/request usage metadata.

AI-derived state must never become indistinguishable from authored state.

---

## 14. AI-Derived Record

Where persistence is justified, conceptual metadata may include:

```text
insight_id
user_id
source_entity_id
task_type
normalized_result
confidence
reason_code
provider
model
schema_version
prompt/template_version
created_at
review_status
expires_at? / recomputable flag
```

Persist only what is useful to product behavior or operations.

---

## 15. Data Minimization

External AI requests must send only required data.

Do not send by default:

- full note history,
- unrelated notes,
- auth tokens,
- billing data,
- exact identity fields,
- private settings unrelated to the task,
- hidden/deleted content unless explicitly required and permitted.

Task contracts should define the minimum permissible context.

---

## 16. Context Data Classes

Context sources should be classified:

### Baseline Allowed
- current time,
- reminder state,
- note text needed for the task,
- explicit category,
- explicit user settings.

### Restricted / Future Review Required
- location,
- calendar,
- activity,
- contacts,
- voice,
- external app history.

Restricted context requires separate privacy/product approval.

---

## 17. Privacy Controls

Users must be able to:

- disable external AI where required,
- understand that external processing occurs,
- remove applicable persisted AI-derived state,
- continue using core notes without AI.

AI provider requests must follow Phase 7 privacy lifecycle requirements.

---

## 18. Security Boundary

AI input and output are untrusted.

Required controls:

- provider credentials remain server-side,
- auth/authz enforced before protected requests,
- prompt injection cannot change authorization,
- model output cannot execute unrestricted actions,
- tool/action allowlists where tools ever exist,
- generated identifiers are validated against ownership,
- no AI path may grant entitlement,
- no AI path may bypass deletion/privacy policy.

---

## 19. Prompt Injection Handling

User note text may contain instructions intended to manipulate the model.

Therefore:

- note text is treated as data,
- system/task instructions are isolated from user content,
- provider output is schema-validated,
- domain policy validates every proposed action,
- no provider response is trusted as authority.

---

## 20. Provider Routing

Provider/model selection may consider:

- task type,
- latency,
- cost,
- capability,
- privacy class,
- region/provider availability,
- entitlement,
- model health.

Routing policy must be centrally controlled and observable.

Provider switching must not change domain contracts.

---

## 21. Timeout and Retry

Each task defines:

- timeout budget,
- retry eligibility,
- max attempts,
- backoff,
- fallback.

Do not retry:

- invalid requests,
- privacy-denied requests,
- malformed application input,
- permanent provider rejection.

Retries must remain bounded and must not create duplicate domain actions.

---

## 22. Failure Model

Failure categories:

- offline,
- timeout,
- provider unavailable,
- rate limited,
- quota exhausted,
- invalid response,
- schema mismatch,
- safety refusal,
- policy denial,
- internal gateway failure.

Expected fallback:

```text
AI failure
→ preserve authored data
→ deterministic behavior if possible
→ non-blocking status when relevant
→ no consequential side effect
```

---

## 23. Cost Controls

Support:

- per-user quotas,
- per-feature quotas,
- model-by-task selection,
- max input/output size,
- request deduplication,
- safe caching,
- batching where useful,
- entitlement-aware limits,
- rate limits,
- provider budget alerts.

Do not use expensive models for trivial deterministic/classification cases without justification.

---

## 24. Caching

Cache only when:

- semantics are stable enough,
- privacy permits,
- keying is account-safe,
- stale results are harmless or versioned,
- invalidation is defined.

AI-derived cache must not cross accounts.

---

## 25. Observability

Track privacy-safe metrics such as:

- task count,
- success/failure,
- latency,
- provider/model,
- quota rejection,
- timeout,
- schema failures,
- estimated cost,
- fallback frequency,
- confirmation acceptance/rejection,
- model version changes.

Do not log raw note content by default.

---

## 26. Model / Prompt Versioning

Where AI behavior affects product semantics, track:

- task schema version,
- prompt/template version,
- model/provider version where available.

This supports:

- regression testing,
- rollback,
- provenance,
- evaluation comparison.

---

## 27. Evaluation Strategy

Maintain curated evaluation sets per task.

Examples:

- explicit datetime extraction,
- ambiguous semantic time,
- routine suggestion,
- relevance ranking,
- summarization fidelity,
- injection attempts,
- multilingual note text.

Provider/model changes should rerun the relevant evaluation suite before broad rollout.

---

## 28. Deterministic CI

Core CI must use mock/fake AI providers.

Tests should cover:

- valid output,
- malformed output,
- low confidence,
- timeout,
- quota exhaustion,
- provider outage,
- privacy opt-out,
- prompt injection,
- unsupported action,
- schema version mismatch.

Live-provider tests are supplementary.

---

## 29. Human Confirmation UX Contract

AI architecture returns enough structured state for UX to render:

- suggestion,
- concise reason,
- confidence/ambiguity state,
- confirm,
- edit,
- reject.

The AI layer must not assume UI confirmation merely because a suggestion was generated.

---

## 30. On-Device AI

Future on-device AI must implement the same task-level contracts.

Potential benefits:

- privacy,
- offline operation,
- lower latency,
- lower provider cost.

Architecture must allow cloud and local providers to coexist without changing domain semantics.

---

## 31. Rollout & Kill Switches

AI features should support:

- per-feature enablement,
- model/provider routing change,
- staged rollout,
- safe provider disable,
- fallback to deterministic behavior.

Security/authorization controls cannot be disabled by AI feature flags.

---

## 32. AI Non-Goals for MVP

MVP does not authorize:

- autonomous agent loops,
- unrestricted tool execution,
- silent content rewriting,
- silent destructive actions,
- invisible behavioral surveillance,
- mandatory cloud AI,
- permanent provider lock-in,
- AI-owned billing/auth/sync truth.

---

## 33. Consolidated Review Criteria

Before final freeze verify:

- AI task contracts map to Phase 2 requirements,
- provider gateway matches Phase 3 V2,
- confirmation/explanation state matches Phase 4 V2,
- quotas/entitlements align with Phase 6,
- privacy/security align with Phase 7,
- evaluation/testing align with Phase 8,
- operational metrics/kill switches align with Phase 10.

Phase 5 remains **Draft** until final project freeze.
