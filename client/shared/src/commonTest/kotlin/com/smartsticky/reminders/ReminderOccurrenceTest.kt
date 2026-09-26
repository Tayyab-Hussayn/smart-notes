package com.smartsticky.reminders

import kotlin.test.*

class ReminderOccurrenceTest {
    private val occurrence = ReminderOccurrence(OccurrenceKey("a", "schedule", 100), "note")

    @Test fun snoozeIsAuthoritativeAndAccountScoped() {
        val snoozed = occurrence.snooze(200, 90)
        assertFalse(snoozed.isDue("a", 199))
        assertTrue(snoozed.isDue("a", 200))
        assertFalse(snoozed.isDue("b", 200))
        assertFalse(snoozed.complete().isDue("a", 300))
        assertFailsWith<IllegalArgumentException> { occurrence.snooze(99, 50) }
    }

    @Test fun completionAffectsOnlyOneOccurrenceAndReconciliationUsesStableKey() {
        val requests = mutableMapOf<OccurrenceKey, ReminderRequest>()
        val platform = object : ReminderPlatform {
            override fun capability() = ReminderCapability.Available
            override fun replace(request: ReminderRequest) { requests[request.key] = request }
            override fun cancel(key: OccurrenceKey) { requests.remove(key) }
        }
        val coordinator = ReminderCoordinator(platform)
        coordinator.reconcile("a", occurrence)
        coordinator.reconcile("a", occurrence.snooze(200, 100))
        assertEquals(1, requests.size)
        assertEquals(200L, requests.values.single().deliverAt)
        val next = occurrence.copy(key = occurrence.key.copy(scheduledAt = 500))
        coordinator.reconcile("a", next)
        coordinator.reconcile("a", occurrence.complete())
        assertEquals(setOf(next.key), requests.keys)
        assertFailsWith<IllegalArgumentException> { coordinator.reconcile("b", next) }
    }
}
