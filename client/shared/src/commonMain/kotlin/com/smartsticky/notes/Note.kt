package com.smartsticky.notes

enum class Lifecycle { ACTIVE, ARCHIVED, DELETED }

data class Note(
    val accountId: String,
    val id: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
    val revision: Long = 1,
    val lifecycle: Lifecycle = Lifecycle.ACTIVE,
    val pinned: Boolean = false,
) {
    init {
        require(accountId.isNotBlank() && id.isNotBlank())
        require(revision > 0)
    }
}

/** Optimistic local concurrency; stale editors must retain their draft. */
class RevisionConflict : IllegalStateException("Note changed; reload before saving")

interface NoteRepository {
    fun list(accountId: String): List<Note>
    fun find(accountId: String, id: String): Note?
    fun save(note: Note, expectedRevision: Long?, operationId: String)
}

class NoteService(
    private val repository: NoteRepository,
    private val newId: () -> String,
    private val now: () -> Long,
) {
    fun create(account: String, content: String): Note {
        val time = now()
        return Note(account, newId(), content, time, time).also {
            repository.save(it, null, newId())
        }
    }

    fun edit(note: Note, content: String): Note = mutate(note) { copy(content = content) }
    fun pin(note: Note, pinned: Boolean): Note = mutate(note) { copy(pinned = pinned) }
    fun archive(note: Note): Note = mutate(note) { copy(lifecycle = Lifecycle.ARCHIVED) }
    fun restore(note: Note): Note = mutate(note) { copy(lifecycle = Lifecycle.ACTIVE) }
    fun delete(note: Note): Note = mutate(note) { copy(lifecycle = Lifecycle.DELETED) }
    fun duplicate(note: Note): Note = create(note.accountId, note.content)

    private fun mutate(note: Note, transform: Note.() -> Note): Note {
        return note.transform().copy(
            revision = note.revision + 1,
            updatedAt = maxOf(note.updatedAt, now()),
        ).also { repository.save(it, note.revision, newId()) }
    }
}
