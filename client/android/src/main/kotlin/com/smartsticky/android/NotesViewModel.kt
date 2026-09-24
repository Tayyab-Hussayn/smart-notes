package com.smartsticky.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsticky.notes.*
import com.smartsticky.canvas.CanvasService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class NotesState(val notes: List<Note> = emptyList(), val exposed: Set<String> = emptySet(), val busy: Boolean = true, val error: String? = null)

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val lock = Mutex()
    private var workspace: AndroidWorkspace? = null
    private val mutable = MutableStateFlow(NotesState())
    val state = mutable.asStateFlow()
    private fun id() = UUID.randomUUID().toString()
    init { refresh() }
    private fun run(action: (AndroidWorkspace) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            lock.withLock {
                mutable.value = mutable.value.copy(busy = true, error = null)
                try {
                    val db = workspace ?: AndroidWorkspace(getApplication()).also { workspace = it }
                    action(db)
                    mutable.value = NotesState(db.notes.list(AndroidWorkspace.ACCOUNT), db.canvas.list(AndroidWorkspace.ACCOUNT, AndroidWorkspace.SURFACE).filter { it.visible }.map { it.noteId }.toSet(), false)
                } catch (e: Exception) {
                    mutable.value = mutable.value.copy(busy = false, error = e.message ?: "Unable to save. Your draft is retained.")
                }
            }
        }
    }
    fun refresh() = run { }
    fun save(note: Note?, content: String, onSaved: (Note) -> Unit) = run { db ->
        require(content.isNotBlank()) { "Write something before saving." }
        require(content.length <= 20_000) { "Please keep notes under 20,000 characters." }
        val service = NoteService(db.notes, ::id, System::currentTimeMillis)
        val saved = if (note == null) service.create(AndroidWorkspace.ACCOUNT, content) else service.edit(note, content)
        viewModelScope.launch(Dispatchers.Main) { onSaved(saved) }
    }
    fun change(note: Note, action: String) = run { db ->
        val service = NoteService(db.notes, ::id, System::currentTimeMillis)
        when (action) { "pin" -> service.pin(note, !note.pinned); "archive" -> service.archive(note); "restore" -> service.restore(note); "delete" -> service.delete(note) }
    }
    fun expose(note: Note, visible: Boolean) = run { db ->
        val service = CanvasService(db.canvas, ::id, System::currentTimeMillis)
        val placement = db.canvas.list(AndroidWorkspace.ACCOUNT, AndroidWorkspace.SURFACE).firstOrNull { it.noteId == note.id }
        if (placement == null && visible) service.place(AndroidWorkspace.ACCOUNT, note.id, AndroidWorkspace.SURFACE)
        else if (placement != null) service.setVisible(placement, visible)
    }
    override fun onCleared() {
        // Drivers are opened and used on IO; queued cleanup waits for any in-flight operation.
        java.util.concurrent.Executors.newSingleThreadExecutor().let { executor ->
            executor.execute { kotlinx.coroutines.runBlocking { lock.withLock { workspace?.close(); workspace = null } } }
            executor.shutdown()
        }
    }
}
