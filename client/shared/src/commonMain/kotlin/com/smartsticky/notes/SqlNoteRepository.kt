package com.smartsticky.notes

import com.smartsticky.database.NotesDatabase

class SqlNoteRepository(private val database: NotesDatabase) : NoteRepository {
    private val queries get() = database.notesQueries
    override fun list(accountId: String): List<Note> =
        queries.listNotes(accountId, ::map).executeAsList()
    override fun find(accountId: String, id: String): Note? =
        queries.findNote(accountId, id, ::map).executeAsOneOrNull()

    override fun save(note: Note, expectedRevision: Long?, operationId: String) {
        require(operationId.isNotBlank())
        database.transaction {
            val existing = find(note.accountId, note.id)
            if (existing?.revision != expectedRevision) throw RevisionConflict()
            require(note.revision == (expectedRevision ?: 0) + 1)
            if (existing == null) {
                queries.insertNote(note.accountId, note.id, note.content, note.createdAt,
                    note.updatedAt, note.revision, note.lifecycle.name, if (note.pinned) 1L else 0L)
            } else {
                require(note.createdAt == existing.createdAt)
                queries.updateNote(note.content, note.updatedAt, note.revision, note.lifecycle.name,
                    if (note.pinned) 1L else 0L, note.accountId, note.id)
            }
            queries.enqueue(operationId, note.accountId, note.id, note.content, note.revision,
                note.lifecycle.name, if (note.pinned) 1L else 0L, note.updatedAt)
        }
    }

    private fun map(account: String, id: String, content: String, created: Long,
                    updated: Long, revision: Long, lifecycle: String, pinned: Long) =
        Note(account, id, content, created, updated, revision, Lifecycle.valueOf(lifecycle), pinned != 0L)
}
