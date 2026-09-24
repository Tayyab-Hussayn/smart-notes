package com.smartsticky.sync

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.*
import java.util.UUID
import kotlinx.serialization.json.*
import kotlin.test.*

class SqlSyncEngineTest {
    private val session = Session(UUID.randomUUID().toString(), "a".repeat(32), 1800)

    @Test fun lostAcknowledgementRetriesIdenticalOperationAndPreservesNewEdit() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            NotesDatabase.Schema.create(driver)
            val db = NotesDatabase(driver)
            val requests = mutableListOf<String>()
            val receipts = mutableMapOf<String, BackendResponse>()
            var serverRevision = 0L
            var loseFirstAck = true
            val client = BackendClient("https://notes.example", transport = BackendTransport { request ->
                if (request.method == "GET") BackendResponse(200, """{"changes":[],"cursor":0,"has_more":false}""")
                else {
                    val body = request.body!!
                    requests += body
                    val json = Json.parseToJsonElement(body).jsonObject
                    val id = json.getValue("operation_id").jsonPrimitive.content
                    val receipt = receipts.getOrPut(id) {
                        assertEquals(serverRevision, json.getValue("base_revision").jsonPrimitive.long)
                        serverRevision++
                        BackendResponse(200, """{"operation_id":"$id","note_id":${json["note_id"]},"revision":$serverRevision,"deleted":false}""")
                    }
                    if (loseFirstAck) { loseFirstAck = false; throw java.io.IOException("lost response") }
                    receipt
                }
            })
            val account = client.localAccountId(session)
            val repository = SqlNoteRepository(db)
            val service = NoteService(repository, { UUID.randomUUID().toString() }, { 100 })
            val note = service.create(account, "first")
            assertFailsWith<BackendException> { SqlSyncEngine(db, client) { 100 }.synchronize(session) }
            service.edit(note, "second")
            val result = SqlSyncEngine(db, client) { 101 }.synchronize(session)
            assertEquals(requests[0], requests[1])
            assertEquals(2, result.uploaded)
            assertEquals("second", repository.find(account, note.id)?.content)
            assertEquals(0L, db.notesQueries.pendingCount(account).executeAsOne())
        } finally { driver.close() }
    }

    @Test fun conflictPreservesLocalTextUntilExplicitResolution() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        try {
            NotesDatabase.Schema.create(driver)
            val db = NotesDatabase(driver)
            val noteId = UUID.randomUUID().toString()
            val remote = """{"id":"$noteId","revision":5,"deleted":false,"payload":{"title":"","body":"server","priority":"normal","pinned":false,"archived":false}}"""
            val client = BackendClient("https://notes.example", transport = BackendTransport { request ->
                if (request.method == "GET") BackendResponse(200, """{"changes":[$remote],"cursor":5,"has_more":false}""")
                else BackendResponse(409, """{"detail":{"code":"revision_conflict","current":$remote}}""")
            })
            val account = client.localAccountId(session)
            val repo = SqlNoteRepository(db)
            repo.save(Note(account, noteId, "mine", 1, 1), null, UUID.randomUUID().toString())
            val engine = SqlSyncEngine(db, client) { 2 }
            assertEquals(1, engine.synchronize(session).conflicts)
            assertEquals("mine", repo.find(account, noteId)?.content)
            engine.resolve(account, noteId, true) { UUID.randomUUID().toString() }
            assertEquals("server", repo.find(account, noteId)?.content)
            assertEquals("mine", repo.list(account).first { it.id != noteId }.content)
            assertTrue(engine.conflicts(account).isEmpty())
        } finally { driver.close() }
    }

    @Test fun sameUserIdOnDifferentServersIsIsolated() {
        assertNotEquals(BackendClient("https://one.example").localAccountId(session),
            BackendClient("https://two.example").localAccountId(session))
    }
}
