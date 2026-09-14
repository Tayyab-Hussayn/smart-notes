package com.smartsticky.canvas

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.Note
import com.smartsticky.notes.SqlNoteRepository
import kotlin.test.*

class CanvasPersistenceTest {
    @Test fun movementIsolationAndImmutableSnapshots() = withDatabase { db ->
        val repo = SqlCanvasRepository(db)
        var sequence = 0
        val service = CanvasService(repo, { "canvas-${++sequence}" }, { 10L })
        val original = service.place("a", "note")
        val moved = service.move(original, 20.0, -1.0)
        assertEquals(0.7, moved.x)
        assertEquals(0.0, moved.y)
        assertFailsWith<CanvasRevisionConflict> { service.setVisible(original, false) }
        assertNull(repo.find("b", original.id))
        assertTrue(repo.list("b", "main").isEmpty())
        val hidden = service.setVisible(moved, false)
        assertFalse(repo.find("a", original.id)!!.visible)
        val restored = service.setVisible(hidden, true)
        assertTrue(restored.visible)
        val snapshots = db.canvasQueries.pendingCanvas("a").executeAsList()
        assertEquals(4, snapshots.size)
        assertEquals(original.x, snapshots.first().x)
        assertEquals(1L, snapshots.first().revision)
        assertEquals(1L, db.notesQueries.pendingCount("a").executeAsOne())
        assertEquals("unchanged", SqlNoteRepository(db).find("a", "note")!!.content)
    }

    @Test fun duplicateOperationRollsBackAndCrossAccountReferenceFails() = withDatabase { db ->
        val repo = SqlCanvasRepository(db)
        val placement = CanvasPlacement("a", "p", "note", updatedAt = 1)
        repo.save(placement, null, "operation")
        assertFails { repo.save(placement.copy(x = 0.2, revision = 2), 1, "operation") }
        assertEquals(placement, repo.find("a", "p"))
        assertEquals(1L, db.canvasQueries.pendingCanvasCount("a").executeAsOne())
        assertFails { repo.save(placement.copy(accountId = "b"), null, "other") }
        assertNull(repo.find("b", "p"))
        assertFails { repo.save(placement.copy(id = "second"), null, "second-op") }
        assertEquals(placement, repo.find("a", "p"))
    }

    @Test fun invalidCoordinatesAndResizeBounds() = withDatabase { db ->
        assertFailsWith<IllegalArgumentException> { CanvasPlacement("a", "p", "note", x = Double.NaN, updatedAt = 1) }
        assertFailsWith<IllegalArgumentException> { CanvasPlacement("a", "p", "note", opacity = 1.1, updatedAt = 1) }
        val repo = SqlCanvasRepository(db)
        var sequence = 0
        val service = CanvasService(repo, { "id-${++sequence}" }, { 1L })
        val placement = service.place("a", "note")
        val resized = service.resize(placement, 1.0, 1.0)
        assertEquals(0.0, resized.x)
        assertEquals(0.0, resized.y)
        assertEquals(1.0, resized.width)
    }

    private fun withDatabase(block: (NotesDatabase) -> Unit) {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            NotesDatabase.Schema.create(driver)
            val db = NotesDatabase(driver)
            SqlNoteRepository(db).save(Note("a", "note", "unchanged", 1, 1), null, "note-op")
            block(db)
        } finally { driver.close() }
    }
}
