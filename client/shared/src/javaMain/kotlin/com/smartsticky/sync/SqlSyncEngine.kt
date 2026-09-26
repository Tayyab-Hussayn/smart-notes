package com.smartsticky.sync

import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.Lifecycle
import com.smartsticky.notes.NoteService
import com.smartsticky.notes.SqlNoteRepository
import kotlinx.serialization.json.*

/** Runs on a background worker, once per account at a time. No credentials persisted. */
class SqlSyncEngine(
    private val database: NotesDatabase,
    private val backend: BackendClient,
    private val now: () -> Long,
) {
    private val q get() = database.syncQueries
    private val notes get() = database.notesQueries

    data class Result(val uploaded: Int, val downloaded: Int, val conflicts: Int)

    fun synchronize(session: Session): Result {
        val account = backend.localAccountId(session)
        require(account.isNotBlank() && account != "local-default")
        var uploaded = 0
        var downloaded = 0
        val blocked = q.conflicts(account).executeAsList().map { it.note_id }.toMutableSet()
        // Order within each note by authored revision, never by the device clock.
        val pending = q.pending(account).executeAsList().sortedWith(compareBy({ it.note_id }, { it.revision }))
        for (operation in pending) {
            if (operation.note_id in blocked) continue
            val attempt = database.transactionWithResult {
                q.attempt(account, operation.operation_id).executeAsOneOrNull() ?: run {
                    val metadata = q.metadata(account, operation.note_id).executeAsOneOrNull()
                    q.putAttempt(account, operation.operation_id, metadata?.server_revision ?: 0,
                        metadata?.remote_title ?: "", metadata?.remote_priority ?: "normal")
                    q.attempt(account, operation.operation_id).executeAsOne()
                }
            }
            val deleted = operation.lifecycle == Lifecycle.DELETED.name
            val payload = if (deleted) null else NotePayload(attempt.remote_title, operation.content,
                attempt.remote_priority, operation.pinned != 0L, operation.lifecycle == Lifecycle.ARCHIVED.name)
            try {
                val ack = backend.push(session.token, Mutation(operation.operation_id, operation.note_id,
                    attempt.base_revision, if (deleted) "delete" else "upsert", payload))
                check(ack.operationId == operation.operation_id && ack.noteId == operation.note_id && ack.revision > attempt.base_revision)
                database.transaction {
                    q.putMetadata(account, operation.note_id, ack.revision, attempt.remote_title, attempt.remote_priority)
                    q.deleteOperation(account, operation.operation_id)
                    q.deleteAttempt(account, operation.operation_id)
                }
                uploaded++
            } catch (failure: BackendException) {
                if (failure.status != 409 || failure.current == null) throw failure
                q.putConflict(account, operation.note_id, encode(failure.current))
                blocked += operation.note_id
            }
        }
        var cursor = q.cursor(account).executeAsOneOrNull() ?: 0
        var pages = 0
        do {
            val page = backend.pull(session.token, cursor)
            check(page.cursor >= cursor && (!page.hasMore || page.cursor > cursor))
            database.transaction {
                for (remote in page.changes) {
                    require(remote.revision > cursor && remote.revision <= page.cursor)
                    val metadata = q.metadata(account, remote.id).executeAsOneOrNull()
                    if (metadata != null && metadata.server_revision >= remote.revision) continue
                    if (q.pendingForNote(account, remote.id).executeAsOne() > 0 || remote.id in blocked) {
                        q.putConflict(account, remote.id, encode(remote))
                        blocked += remote.id
                    } else {
                        applyRemote(account, remote)
                        downloaded++
                    }
                }
                q.putCursor(account, page.cursor)
            }
            cursor = page.cursor
            pages++
            // Bound one foreground sync; resume from the durable cursor next time.
        } while (page.hasMore && pages < 20)
        return Result(uploaded, downloaded, q.conflicts(account).executeAsList().size)
    }

    fun conflicts(account: String): List<RemoteNote> = q.conflicts(account).executeAsList().map { decode(it.remote_json) }

    /** Explicit resolution: optionally preserve a local copy under a new identity first. */
    fun resolve(account: String, noteId: String, keepLocalCopy: Boolean, newId: () -> String) {
        database.transaction {
            val record = q.conflicts(account).executeAsList().first { it.note_id == noteId }
            val remote = decode(record.remote_json)
            val local = SqlNoteRepository(database).find(account, noteId)
            if (keepLocalCopy && local != null) {
                NoteService(SqlNoteRepository(database), newId, now).create(account, local.content)
            }
            for (operation in q.pending(account).executeAsList().filter { it.note_id == noteId }) {
                q.deleteAttempt(account, operation.operation_id)
            }
            q.deleteNoteOperations(account, noteId)
            applyRemote(account, remote)
            q.deleteConflict(account, noteId)
        }
    }

    private fun applyRemote(account: String, remote: RemoteNote) {
        val existing = notes.findNote(account, remote.id).executeAsOneOrNull()
        val payload = remote.payload
        val content = payload?.body ?: ""
        val lifecycle = if (remote.deleted) "DELETED" else if (payload?.archived == true) "ARCHIVED" else "ACTIVE"
        val at = maxOf(now(), existing?.updated_at ?: 0)
        val revision = (existing?.revision ?: 0) + 1
        require(revision > 0)
        if (existing == null) notes.insertNote(account, remote.id, content, at, at, revision, lifecycle, if (payload?.pinned == true) 1 else 0)
        else notes.updateNote(content, at, revision, lifecycle, if (payload?.pinned == true) 1 else 0, account, remote.id)
        q.putMetadata(account, remote.id, remote.revision, payload?.title ?: "", payload?.priority ?: "normal")
    }

    private fun encode(remote: RemoteNote) = buildJsonObject {
        put("id", remote.id); put("revision", remote.revision); put("deleted", remote.deleted)
        remote.payload?.let { p -> put("payload", buildJsonObject {
            put("title", p.title); put("body", p.body); put("priority", p.priority)
            put("pinned", p.pinned); put("archived", p.archived)
        }) }
    }.toString()

    private fun decode(value: String): RemoteNote {
        val obj = Json.parseToJsonElement(value).jsonObject
        val p = obj["payload"]?.jsonObject
        return RemoteNote(obj.getValue("id").jsonPrimitive.content, obj.getValue("revision").jsonPrimitive.long,
            obj.getValue("deleted").jsonPrimitive.boolean, p?.let {
                NotePayload(it.getValue("title").jsonPrimitive.content, it.getValue("body").jsonPrimitive.content,
                    it.getValue("priority").jsonPrimitive.content, it.getValue("pinned").jsonPrimitive.boolean,
                    it.getValue("archived").jsonPrimitive.boolean)
            })
    }
}
