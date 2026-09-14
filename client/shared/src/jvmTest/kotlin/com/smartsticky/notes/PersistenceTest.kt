package com.smartsticky.notes

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.smartsticky.database.NotesDatabase
import java.nio.file.Files
import kotlin.test.*

class PersistenceTest {
    @Test fun restartAndLifecycle() {
        val path = Files.createTempFile("notes-test", ".db")
        val url = "jdbc:sqlite:$path"
        val driver = JdbcSqliteDriver(url)
        try {
            NotesDatabase.Schema.create(driver)
            val db = NotesDatabase(driver)
            val repo = SqlNoteRepository(db)
            var counter = 0
            val service = NoteService(repo, { "op-${++counter}" }, { 100L })
            val note = service.create("a", "  preserve whitespace\n")
            val edited = service.edit(note, "new")
            assertFailsWith<RevisionConflict> { service.edit(note, "stale") }
            assertNull(repo.find("b", note.id))
            val deleted = service.delete(edited)
            assertEquals(Lifecycle.DELETED, repo.find("a", note.id)?.lifecycle)
            val restored = service.restore(deleted)
            assertEquals("new", restored.content)
            assertEquals(4L, db.notesQueries.pendingCount("a").executeAsOne())
            driver.close()
            val reopened = JdbcSqliteDriver(url)
            try {
                assertEquals(restored, SqlNoteRepository(NotesDatabase(reopened)).find("a", note.id))
            } finally { reopened.close() }
        } finally {
            driver.close()
            Files.deleteIfExists(path)
        }
    }

    @Test fun failedOutboxInsertRollsBackNote() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            NotesDatabase.Schema.create(driver)
            val repo = SqlNoteRepository(NotesDatabase(driver))
            val note = Note("a", "n", "original", 1, 1)
            repo.save(note, null, "duplicate")
            assertFails { repo.save(note.copy(content = "lost", revision = 2), 1, "duplicate") }
            assertEquals(note, repo.find("a", "n"))
        } finally { driver.close() }
    }
}
