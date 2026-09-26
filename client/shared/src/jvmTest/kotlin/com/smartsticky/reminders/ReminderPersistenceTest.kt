package com.smartsticky.reminders

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.Note
import com.smartsticky.notes.SqlNoteRepository
import kotlin.test.*

class ReminderPersistenceTest {
    @Test fun durableIdempotentOccurrencesAndAccountIsolation() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            NotesDatabase.Schema.create(driver)
            val db = NotesDatabase(driver)
            SqlNoteRepository(db).save(Note("a", "n", "text", 1, 1), null, "note")
            val repo = SqlReminderRepository(db)
            repo.save(ReminderSchedule("a", "s", "n", 100, 100, updatedAt = 1), null, "schedule")
            var sequence = 0
            val first = repo.materialize("a", "s", 0, 200, 1) { "op-${++sequence}" }.first()
            val done = first.copy(occurrence = first.occurrence.complete(), revision = 2, updatedAt = 2)
            repo.save(done, 1, "complete")
            repo.materialize("a", "s", 0, 200, 2) { "op-${++sequence}" }
            assertEquals(2, sequence)
            assertEquals(done, SqlReminderRepository(db).find(first.occurrence.key))
            assertEquals(1, repo.due("a", 300).size)
            assertTrue(repo.due("b", 300).isEmpty())
            assertNull(repo.find("b", "s"))
            assertFailsWith<ReminderRevisionConflict> { repo.save(first, 1, "stale") }
            assertEquals("PENDING", db.reminderQueries.pendingReminders("a").executeAsList()
                .first { it.operation_id == "op-1" }.state)
            val pending = repo.occurrences("a", "s").last()
            assertFails { repo.save(pending.copy(occurrence = pending.occurrence.complete(), revision = 2), 1, "complete") }
            assertEquals(pending, repo.find(pending.occurrence.key))
            repo.save(repo.find("a", "s")!!.copy(enabled = false, revision = 2), 1, "disable")
            assertTrue(repo.due("a", 300).isEmpty())
        } finally { driver.close() }
    }

    @Test fun fixedIntervalBoundariesAndOverflow() {
        val schedule = ReminderSchedule("a", "s", "n", 100, 10, updatedAt = 1)
        assertEquals(listOf(120L, 130L), schedule.expand(111, 130).map { it.key.scheduledAt })
        assertEquals(2, schedule.expand(0, 1000, 2).size)
        assertTrue(schedule.copy(enabled = false).expand(0, 1000).isEmpty())
        assertEquals(1, schedule.copy(firstAt = Long.MAX_VALUE - 1).expand(0, Long.MAX_VALUE).size)
        assertTrue(schedule.copy(intervalMs = null).expand(101, 200).isEmpty())
    }
}
