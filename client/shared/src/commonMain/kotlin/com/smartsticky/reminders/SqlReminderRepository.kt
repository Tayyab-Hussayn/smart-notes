package com.smartsticky.reminders

import com.smartsticky.database.NotesDatabase

/** All ownership checks and immutable outbox writes share the mutation transaction. */
class SqlReminderRepository(private val database: NotesDatabase) {
    private val q get() = database.reminderQueries
    fun find(account: String, id: String): ReminderSchedule? =
        q.findSchedule(account, id, ::schedule).executeAsOneOrNull()
    fun schedules(account: String): List<ReminderSchedule> = q.listSchedules(account, ::schedule).executeAsList()
    fun find(key: OccurrenceKey): StoredOccurrence? =
        q.findOccurrence(key.accountId, key.scheduleId, key.scheduledAt, ::occurrence).executeAsOneOrNull()
    fun occurrences(account: String, scheduleId: String): List<StoredOccurrence> =
        q.listOccurrences(account, scheduleId, ::occurrence).executeAsList()
    fun due(account: String, now: Long): List<StoredOccurrence> =
        q.dueOccurrences(account, now, now, ::occurrence).executeAsList()

    fun save(value: ReminderSchedule, expectedRevision: Long?, operationId: String) {
        require(operationId.isNotBlank())
        database.transaction {
            val old = find(value.accountId, value.id)
            if (old?.revision != expectedRevision) throw ReminderRevisionConflict()
            require(value.revision == (expectedRevision ?: 0) + 1)
            require(database.notesQueries.findNote(value.accountId, value.noteId).executeAsOneOrNull() != null)
            if (old == null) q.insertSchedule(value.accountId, value.id, value.noteId, value.firstAt,
                value.intervalMs, if (value.enabled) 1 else 0, value.revision, value.updatedAt)
            else {
                require(old.noteId == value.noteId)
                // Schedule edits require explicit handling of existing pending occurrences by the caller.
                require(old.firstAt == value.firstAt && old.intervalMs == value.intervalMs) {
                    "Create a new schedule to change recurrence; disable the old schedule"
                }
                q.updateSchedule(value.firstAt, value.intervalMs, if (value.enabled) 1 else 0,
                    value.revision, value.updatedAt, value.accountId, value.id)
            }
            q.enqueueReminder(operationId, value.accountId, value.id, null, value.noteId,
                value.firstAt, value.intervalMs, if (value.enabled) 1 else 0, null, null,
                value.revision, value.updatedAt)
        }
    }

    /** Materialization is idempotent by occurrence key, including completed occurrences. */
    fun materialize(account: String, id: String, from: Long, through: Long, now: Long,
                    operationId: () -> String): List<StoredOccurrence> = database.transactionWithResult {
        val schedule = requireNotNull(find(account, id))
        schedule.expand(from, through).map { value ->
            find(value.key) ?: StoredOccurrence(value, updatedAt = now).also { save(it, null, operationId()) }
        }
    }

    fun save(value: StoredOccurrence, expectedRevision: Long?, operationId: String) {
        require(operationId.isNotBlank())
        val o = value.occurrence
        val k = o.key
        database.transaction {
            val old = find(k)
            if (old?.revision != expectedRevision) throw ReminderRevisionConflict()
            require(value.revision == (expectedRevision ?: 0) + 1)
            val schedule = requireNotNull(find(k.accountId, k.scheduleId))
            require(schedule.noteId == o.noteId)
            require(k.scheduledAt >= schedule.firstAt)
            require(if (schedule.intervalMs == null) k.scheduledAt == schedule.firstAt
                else (k.scheduledAt - schedule.firstAt) % schedule.intervalMs == 0L)
            require(o.snoozedUntil == null || o.snoozedUntil >= k.scheduledAt)
            require(o.state == OccurrenceState.PENDING || o.snoozedUntil == null)
            require(old == null || old.occurrence.state == OccurrenceState.PENDING || old.occurrence == o) {
                "Terminal occurrences cannot be reopened"
            }
            if (old == null) q.insertOccurrence(k.accountId, k.scheduleId, k.scheduledAt, o.noteId,
                o.state.name, o.snoozedUntil, value.revision, value.updatedAt)
            else q.updateOccurrence(o.state.name, o.snoozedUntil, value.revision, value.updatedAt,
                k.accountId, k.scheduleId, k.scheduledAt)
            q.enqueueReminder(operationId, k.accountId, k.scheduleId, k.scheduledAt, o.noteId,
                null, null, null, o.state.name, o.snoozedUntil, value.revision, value.updatedAt)
        }
    }

    private fun schedule(a: String, id: String, n: String, first: Long, interval: Long?, enabled: Long,
                         revision: Long, updated: Long) =
        ReminderSchedule(a, id, n, first, interval, enabled != 0L, revision, updated)
    private fun occurrence(a: String, id: String, at: Long, n: String, state: String, snooze: Long?,
                           revision: Long, updated: Long) =
        StoredOccurrence(ReminderOccurrence(OccurrenceKey(a, id, at), n, OccurrenceState.valueOf(state), snooze),
            revision, updated)
}
