package com.smartsticky.reminders

/** UTC epoch milliseconds. Fixed elapsed intervals deliberately do not model local calendar/DST recurrence. */
data class ReminderSchedule(
    val accountId: String,
    val id: String,
    val noteId: String,
    val firstAt: Long,
    val intervalMs: Long? = null,
    val enabled: Boolean = true,
    val revision: Long = 1,
    val updatedAt: Long,
) {
    init {
        require(accountId.isNotBlank() && id.isNotBlank() && noteId.isNotBlank())
        require(firstAt >= 0 && revision > 0)
        require(intervalMs == null || intervalMs > 0)
    }

    /** Inclusive window, bounded to avoid unbounded catch-up work. Stable identity survives retries. */
    fun expand(from: Long, through: Long, limit: Int = 128): List<ReminderOccurrence> {
        require(from >= 0 && through >= from && limit in 1..1000)
        if (!enabled || firstAt > through) return emptyList()
        val interval = intervalMs
        var at = firstAt
        if (at < from) {
            if (interval == null) return emptyList()
            val difference = from - at
            val steps = difference / interval + if (difference % interval == 0L) 0 else 1
            if (steps > (Long.MAX_VALUE - at) / interval) return emptyList()
            at += steps * interval
        }
        val result = mutableListOf<ReminderOccurrence>()
        while (at <= through && result.size < limit) {
            result += ReminderOccurrence(OccurrenceKey(accountId, id, at), noteId)
            if (interval == null || at > Long.MAX_VALUE - interval) break
            at += interval
        }
        return result
    }
}

data class StoredOccurrence(val occurrence: ReminderOccurrence, val revision: Long = 1, val updatedAt: Long) {
    init { require(revision > 0) }
}

class ReminderRevisionConflict : IllegalStateException("Reminder changed; reload before editing")
