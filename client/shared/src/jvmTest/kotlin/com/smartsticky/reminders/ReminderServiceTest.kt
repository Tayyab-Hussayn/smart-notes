package com.smartsticky.reminders

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.*
import kotlin.test.*

class ReminderServiceTest {
    private fun check(block: (NotesDatabase, ReminderService, SqlReminderRepository) -> Unit) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            NotesDatabase.Schema.create(driver)
            val db = NotesDatabase(driver)
            SqlNoteRepository(db).save(Note("owner", "note", "text", 1, 1), null, "note-create")
            var id = 0
            block(db, ReminderService(db, { "id-${++id}" }, { 1000L }), SqlReminderRepository(db))
        } finally { driver.close() }
    }

    @Test fun oneShotSnoozeCompletionAndStaleEdits() = check { _, service, repo ->
        val schedule = service.createOneShot("owner", "note", 2000)
        val occurrence = repo.occurrences("owner", schedule.id).single()
        service.snoozeTenMinutes("owner", occurrence)
        val snoozed = repo.find(occurrence.occurrence.key)!!
        assertEquals(602000L, snoozed.occurrence.snoozedUntil)
        assertTrue(repo.due("owner", 601999).isEmpty())
        assertEquals(1, repo.due("owner", 602000).size)
        assertFailsWith<ReminderRevisionConflict> { service.complete("owner", occurrence) }
        service.complete("owner", snoozed)
        assertEquals(OccurrenceState.COMPLETED, repo.find(occurrence.occurrence.key)!!.occurrence.state)
        assertTrue(repo.due("owner", 700000).isEmpty())
        assertTrue(repo.find("owner", schedule.id)!!.enabled)
    }

    @Test fun accountAndLifecycleChecksDoNotMutate() = check { db, service, repo ->
        val schedule = service.createOneShot("owner", "note", 2000)
        val occurrence = repo.occurrences("owner", schedule.id).single()
        assertFailsWith<IllegalArgumentException> { service.createOneShot("other", "note", 2000) }
        assertFailsWith<IllegalArgumentException> { service.complete("other", occurrence) }
        assertFailsWith<IllegalArgumentException> { service.disable("other", schedule) }
        assertEquals(occurrence, repo.find(occurrence.occurrence.key))
        val notes = SqlNoteRepository(db)
        val note = notes.find("owner", "note")!!
        notes.save(note.copy(lifecycle = Lifecycle.ARCHIVED, revision = 2), 1, "archive")
        assertFailsWith<IllegalArgumentException> { service.snoozeTenMinutes("owner", occurrence) }
        assertFailsWith<IllegalArgumentException> { service.createOneShot("owner", "note", 3000) }
        assertTrue(repo.due("owner", 9000).isEmpty())
    }

    @Test fun disableAndInvalidTimeAreSafe() = check { _, service, repo ->
        assertFailsWith<IllegalArgumentException> { service.createOneShot("owner", "note", 1000) }
        assertTrue(repo.schedules("owner").isEmpty())
        val schedule = service.createOneShot("owner", "note", 2000)
        service.disable("owner", schedule)
        assertTrue(repo.due("owner", 9000).isEmpty())
        assertFailsWith<IllegalArgumentException> { service.snoozeTenMinutes("owner", repo.occurrences("owner", schedule.id).single()) }
    }

    @Test fun occurrenceFailureRollsBackScheduleAndOutbox() = check { db, _, repo ->
        val ids = listOf("schedule", "duplicate-operation", "duplicate-operation").iterator()
        val service = ReminderService(db, { ids.next() }, { 1000L })
        assertFails { service.createOneShot("owner", "note", 2000) }
        assertTrue(repo.schedules("owner").isEmpty())
        assertTrue(db.reminderQueries.pendingReminders("owner").executeAsList().isEmpty())
    }

    @Test fun retryAfterTimePassesDoesNotDuplicateOrReenable() = check { db, service, repo ->
        val schedule = service.createOneShot("owner", "note", 2000, "stable-request")
        service.disable("owner", schedule)
        val replay = ReminderService(db, { error("Retry must not create operations") }, { 4000L })
            .createOneShot("owner", "note", 2000, "stable-request")
        assertFalse(replay.enabled)
        assertEquals(1, repo.schedules("owner").size)
        assertEquals(1, repo.occurrences("owner", schedule.id).size)
        assertEquals(3, db.reminderQueries.pendingReminders("owner").executeAsList().size)
        assertFailsWith<IllegalArgumentException> { service.createOneShot("owner", "note", 3000, "stable-request") }
    }
}
