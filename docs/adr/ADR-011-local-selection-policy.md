# ADR-011 — Deterministic local selection baseline

Status: Implementation decision under owner delegation; 2026-09-13.
Requirements: Phase 05 Intelligence Rules sections 3–6, 11, 15, 17.

Filter account ownership, lifecycle, hidden, snooze, completion, capability,
and opt-out before ordering. Rank eligible candidates by pin, explicit priority,
reminder urgency, configured routine match, least recent display, then stable ID.
Capacity and upcoming window are injected configuration. Pin does not bypass
hard suppression. No note body interpretation, cloud call, or notification is
performed by selection. Reasons are derived from the applied rule.

This is the initial local policy, not a completed AI or reminders subsystem.
Integration must supply durable repetition history and authorized context.
