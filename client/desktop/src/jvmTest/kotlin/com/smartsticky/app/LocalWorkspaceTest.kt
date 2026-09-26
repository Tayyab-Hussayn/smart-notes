package com.smartsticky.app

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.smartsticky.canvas.CanvasService
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.*
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.*

class LocalWorkspaceTest {
    @Test fun offlineLifecycleAndCanvasSurviveWorkspaceRestart() = temporaryDirectory { directory ->
        var sequence = 0
        val ids = { "intent-${++sequence}" }
        val clock = { 100L }
        val noteId: String
        val placementId: String
        LocalWorkspace.open(directory).use { workspace ->
            val notes = NoteService(workspace.notes, ids, clock)
            val created = notes.create("local", "First note")
            noteId = created.id
            val edited = notes.edit(created, "Saved offline 🌱\nSecond line")
            assertFailsWith<RevisionConflict> { notes.edit(created, "stale draft") }
            val archived = notes.archive(edited)
            assertEquals(Lifecycle.ARCHIVED, workspace.notes.find("local", noteId)!!.lifecycle)
            val restored = notes.restore(archived)
            val deleted = notes.delete(restored)
            assertEquals(Lifecycle.DELETED, workspace.notes.find("local", noteId)!!.lifecycle)
            notes.restore(deleted)
            val canvas = CanvasService(workspace.canvas, ids, clock)
            val placement = canvas.place("local", noteId)
            placementId = placement.id
            canvas.move(placement, 0.4, 0.3)
            assertTrue(workspace.notes.list("another-account").isEmpty())
            assertTrue(workspace.canvas.list("another-account", "main").isEmpty())
        }
        LocalWorkspace.open(directory).use { reopened ->
            val saved = reopened.notes.find("local", noteId)!!
            assertEquals("Saved offline 🌱\nSecond line", saved.content)
            assertEquals(Lifecycle.ACTIVE, saved.lifecycle)
            assertEquals(6L, saved.revision)
            val placement = reopened.canvas.find("local", placementId)!!
            assertEquals(noteId, placement.noteId)
            assertEquals(0.4, placement.x)
            assertEquals(0.3, placement.y)
            assertEquals(6L, reopened.database.notesQueries.pendingCount("local").executeAsOne())
            assertEquals(2L, reopened.database.canvasQueries.pendingCanvasCount("local").executeAsOne())
        }
    }

    @Test fun versionOneWorkspaceMigratesThroughProductionBootstrap() = temporaryDirectory { directory ->
        JdbcSqliteDriver("jdbc:sqlite:${directory.resolve("notes.db")}").use { driver ->
            // Frozen v1 tables: exercise Schema.migrate through the actual app opener.
            driver.execute(null, "CREATE TABLE note (account_id TEXT NOT NULL, id TEXT NOT NULL, content TEXT NOT NULL, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, revision INTEGER NOT NULL, lifecycle TEXT NOT NULL, pinned INTEGER NOT NULL, PRIMARY KEY(account_id,id))", 0)
            driver.execute(null, "CREATE TABLE outbox (operation_id TEXT NOT NULL PRIMARY KEY, account_id TEXT NOT NULL, note_id TEXT NOT NULL, content TEXT NOT NULL, revision INTEGER NOT NULL, lifecycle TEXT NOT NULL, pinned INTEGER NOT NULL, created_at INTEGER NOT NULL)", 0)
            driver.execute(null, "INSERT INTO note VALUES ('local','legacy','Original content',1,2,2,'ARCHIVED',1)", 0)
            driver.execute(null, "INSERT INTO outbox VALUES ('pending','local','legacy','Original content',2,'ARCHIVED',1,2)", 0)
            driver.execute(null, "PRAGMA user_version = 1", 0)
        }
        repeat(2) {
            LocalWorkspace.open(directory).use { workspace ->
                val note = workspace.notes.find("local", "legacy")!!
                assertEquals("Original content", note.content)
                assertEquals(Lifecycle.ARCHIVED, note.lifecycle)
                assertTrue(note.pinned)
                assertEquals(2L, note.revision)
                assertEquals(1L, workspace.database.notesQueries.pendingCount("local").executeAsOne())
                assertTrue(workspace.canvas.list("local", "main").isEmpty())
            }
        }
    }

    @Test fun failedInitializationRollsBackPartialSchema() = temporaryDirectory { directory ->
        val url = "jdbc:sqlite:${directory.resolve("notes.db")}"
        JdbcSqliteDriver(url).use { driver ->
            driver.execute(null, "CREATE TABLE outbox (sentinel TEXT)", 0)
            driver.execute(null, "INSERT INTO outbox VALUES ('preserve')", 0)
        }
        assertFails { LocalWorkspace.open(directory) }
        JdbcSqliteDriver(url).use { driver ->
            fun scalar(sql: String) = driver.executeQuery(null, sql, { cursor ->
                check(cursor.next().value)
                QueryResult.Value(cursor.getLong(0))
            }, 0).value
            assertEquals(0L, scalar("PRAGMA user_version"))
            assertEquals(0L, scalar("SELECT count(*) FROM sqlite_master WHERE name='note'"))
            assertEquals(1L, scalar("SELECT count(*) FROM outbox WHERE sentinel='preserve'"))
        }
    }

    @Test fun newerSchemaIsRejectedWithoutChangingSavedData() = temporaryDirectory { directory ->
        LocalWorkspace.open(directory).use { workspace ->
            workspace.notes.save(Note("local", "keep", "Do not modify", 1, 1), null, "keep-op")
        }
        val url = "jdbc:sqlite:${directory.resolve("notes.db")}"
        JdbcSqliteDriver(url).use { driver ->
            driver.execute(null, "PRAGMA user_version = ${NotesDatabase.Schema.version + 1}", 0)
        }
        assertFailsWith<IllegalStateException> { LocalWorkspace.open(directory) }
        JdbcSqliteDriver(url).use { driver ->
            assertEquals("Do not modify", SqlNoteRepository(NotesDatabase(driver)).find("local", "keep")!!.content)
        }
        // A failed open must release its connection and leave the file usable.
        JdbcSqliteDriver(url).use { driver ->
            driver.execute(null, "PRAGMA user_version = ${NotesDatabase.Schema.version}", 0)
        }
        LocalWorkspace.open(directory).use { assertEquals(1, it.notes.list("local").size) }
    }

    private fun temporaryDirectory(block: (Path) -> Unit) {
        val directory = Files.createTempDirectory("workspace-acceptance")
        try { block(directory) } finally {
            Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach(Files::delete) }
        }
    }
}
