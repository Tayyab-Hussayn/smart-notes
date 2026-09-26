package com.smartsticky.reminders

/** A recurrence expander creates occurrences; completing one never edits its schedule. */
data class OccurrenceKey(val accountId: String, val scheduleId: String, val scheduledAt: Long) {
    init { require(accountId.isNotBlank() && scheduleId.isNotBlank()) }
}

enum class OccurrenceState { PENDING, COMPLETED, CANCELLED }

data class ReminderOccurrence(
    val key: OccurrenceKey,
    val noteId: String,
    val state: OccurrenceState = OccurrenceState.PENDING,
    val snoozedUntil: Long? = null,
) {
    init { require(noteId.isNotBlank()) }

    fun snooze(until: Long, now: Long): ReminderOccurrence {
        require(state == OccurrenceState.PENDING) { "Only pending occurrences can be snoozed" }
        require(until > now && until >= key.scheduledAt) { "Snooze must defer the occurrence" }
        return copy(snoozedUntil = until)
    }

    fun complete(): ReminderOccurrence {
        require(state != OccurrenceState.CANCELLED) { "Cancelled occurrences cannot be completed" }
        return copy(state = OccurrenceState.COMPLETED, snoozedUntil = null)
    }

    fun cancel() = copy(state = OccurrenceState.CANCELLED, snoozedUntil = null)

    fun isDue(activeAccount: String, now: Long): Boolean =
        activeAccount == key.accountId && state == OccurrenceState.PENDING &&
            now >= maxOf(key.scheduledAt, snoozedUntil ?: key.scheduledAt)
}

/** A stable key is passed to native adapters for replacement, not duplicate scheduling. */
data class ReminderRequest(val key: OccurrenceKey, val noteId: String, val deliverAt: Long)

sealed class ReminderCapability {
    data object Available : ReminderCapability()
    data object PermissionRequired : ReminderCapability()
    data class Unavailable(val reason: String) : ReminderCapability()
}

interface ReminderPlatform {
    fun capability(): ReminderCapability
    fun replace(request: ReminderRequest)
    fun cancel(key: OccurrenceKey)
}

/** Caller supplies persisted occurrences; OS state is never the reminder source of truth. */
class ReminderCoordinator(private val platform: ReminderPlatform) {
    fun reconcile(activeAccount: String, occurrence: ReminderOccurrence): ReminderCapability {
        require(activeAccount == occurrence.key.accountId) { "Occurrence belongs to another account" }
        val capability = platform.capability()
        if (occurrence.state != OccurrenceState.PENDING) {
            platform.cancel(occurrence.key)
        } else if (capability == ReminderCapability.Available) {
            platform.replace(ReminderRequest(occurrence.key, occurrence.noteId,
                maxOf(occurrence.key.scheduledAt, occurrence.snoozedUntil ?: occurrence.key.scheduledAt)))
        }
        return capability
    }
}
