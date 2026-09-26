package com.smartsticky.reminders

import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.Lifecycle
import com.smartsticky.notes.SqlNoteRepository

/** Account checks, occurrence creation, and outbox writes are one local transaction. */
class ReminderService(private val database: NotesDatabase, private val newId: () -> String, private val now: () -> Long) {
    private val repository = SqlReminderRepository(database)

    fun createOneShot(account: String, noteId: String, at: Long, scheduleId: String = newId()): ReminderSchedule = database.transactionWithResult {
        activeNote(account, noteId)
        val existing = repository.find(account, scheduleId)
        if (existing != null) {
            require(existing.noteId == noteId && existing.firstAt == at && existing.intervalMs == null) { "Reminder request changed" }
            return@transactionWithResult existing
        }
        val time = now()
        require(at > time) { "Choose a future date and time" }
        val schedule = ReminderSchedule(account, scheduleId, noteId, at, updatedAt = time)
        repository.save(schedule, null, newId())
        repository.materialize(account, schedule.id, at, at, time, newId)
        schedule
    }

    fun complete(account: String, value: StoredOccurrence) = update(account, value) { it.complete() }

    fun snoozeTenMinutes(account: String, value: StoredOccurrence) = update(account, value) {
        val time = now()
        val base = maxOf(time, it.key.scheduledAt, it.snoozedUntil ?: 0)
        require(base <= Long.MAX_VALUE - 600_000) { "Snooze time is out of range" }
        it.snooze(base + 600_000, time)
    }

    fun disable(account: String, schedule: ReminderSchedule) = database.transaction {
        require(schedule.accountId == account) { "Reminder belongs to another account" }
        require(schedule.enabled)
        repository.save(schedule.copy(enabled = false, revision = next(schedule.revision),
            updatedAt = maxOf(now(), schedule.updatedAt)), schedule.revision, newId())
    }

    private fun update(account: String, value: StoredOccurrence, transform: (ReminderOccurrence) -> ReminderOccurrence) = database.transaction {
        require(value.occurrence.key.accountId == account) { "Reminder belongs to another account" }
        activeNote(account, value.occurrence.noteId)
        require(repository.find(account, value.occurrence.key.scheduleId)?.enabled == true) { "Schedule is disabled" }
        require(value.occurrence.state == OccurrenceState.PENDING) { "Occurrence is no longer pending" }
        repository.save(value.copy(occurrence = transform(value.occurrence), revision = next(value.revision),
            updatedAt = maxOf(now(), value.updatedAt)), value.revision, newId())
    }

    private fun activeNote(account: String, noteId: String) {
        require(SqlNoteRepository(database).find(account, noteId)?.lifecycle == Lifecycle.ACTIVE) { "Choose an active note" }
    }

    private fun next(revision: Long): Long {
        require(revision < Long.MAX_VALUE) { "Revision limit reached" }
        return revision + 1
    }
}
