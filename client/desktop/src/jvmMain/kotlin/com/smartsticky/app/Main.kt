package com.smartsticky.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.*
import com.smartsticky.canvas.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "Smart Notes") {
        MaterialTheme { App() }
    }
}

@Composable
private fun App() {
    var repository by remember { mutableStateOf<NoteRepository?>(null) }
    var canvasRepository by remember { mutableStateOf<CanvasRepository?>(null) }
    var placements by remember { mutableStateOf(emptyList<CanvasPlacement>()) }
    var canvasMode by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    var notes by remember { mutableStateOf(emptyList<Note>()) }
    var draft by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Note?>(null) }
    var busy by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf(Lifecycle.ACTIVE) }
    var deleteCandidate by remember { mutableStateOf<Note?>(null) }
    var pendingEditor by remember { mutableStateOf<Note?>(null) }
    var confirmDiscard by remember { mutableStateOf(false) }
    val dirty = draft != (editing?.content ?: "")
    val account = "local-default"
    LaunchedEffect(Unit) {
        try {
            repository = withContext(Dispatchers.IO) {
                val directory = Path.of(System.getProperty("user.home"), ".smart-notes")
                Files.createDirectories(directory)
                val path = directory.resolve("notes.db")
                val driver = JdbcSqliteDriver("jdbc:sqlite:$path")
                // Schema initialization is atomic; never swallow a DB failure.
                driver.execute(null, "BEGIN IMMEDIATE", 0)
                try {
                    val version = driver.executeQuery(null, "PRAGMA user_version", { cursor ->
                        cursor.next()
                        app.cash.sqldelight.db.QueryResult.Value(cursor.getLong(0)!!)
                    }, 0).value
                    val currentVersion = NotesDatabase.Schema.version
                    check(version <= currentVersion) { "Unsupported database version" }
                    if (version == 0L) {
                        NotesDatabase.Schema.create(driver)
                    } else if (version < currentVersion) {
                        NotesDatabase.Schema.migrate(driver, version, currentVersion)
                    }
                    driver.execute(null, "PRAGMA user_version = $currentVersion", 0)
                    driver.execute(null, "COMMIT", 0)
                } catch (failure: Exception) {
                    driver.execute(null, "ROLLBACK", 0)
                    driver.close()
                    throw failure
                }
                val database = NotesDatabase(driver)
                canvasRepository = SqlCanvasRepository(database)
                SqlNoteRepository(database)
            }
            notes = withContext(Dispatchers.IO) { repository!!.list(account) }
            placements = withContext(Dispatchers.IO) { canvasRepository!!.list(account, "main") }
        } catch (_: Exception) { error = "Cannot open notes safely. Your data has not been reset." }
    }
    fun changePlacement(action: (CanvasService) -> Unit) {
        val repo = canvasRepository ?: return
        if (busy) return
        busy = true
        scope.launch {
            try {
                placements = withContext(Dispatchers.IO) {
                    action(CanvasService(repo, { UUID.randomUUID().toString() }, System::currentTimeMillis))
                    repo.list(account, "main")
                }
                error = null
            } catch (_: Exception) {
                error = "Could not save canvas change. Reload and try again."
            } finally { busy = false }
        }
    }
    fun perform(clearDraft: Boolean = false, action: (NoteService) -> Unit) {
        val repo = repository ?: return
        if (busy) return
        busy = true
        scope.launch {
            try {
                notes = withContext(Dispatchers.IO) {
                    action(NoteService(repo, { UUID.randomUUID().toString() }, System::currentTimeMillis))
                    repo.list(account)
                }
                if (clearDraft) { draft = ""; editing = null }
                // Preserve authored draft while refreshing non-content mutations.
                if (!clearDraft) editing = editing?.let { selected ->
                    notes.find { it.id == selected.id && it.content == selected.content } ?: selected
                }
                error = null
            } catch (_: RevisionConflict) {
                error = "This note changed. Your draft is preserved; reload before saving."
            } catch (_: Exception) {
                error = "Save failed. Your draft is preserved. Try again."
            } finally { busy = false }
        }
    }
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Smart Notes", style = MaterialTheme.typography.headlineLarge)
        Text("Local workspace · works offline")
        Row {
            FilterChip(selected = !canvasMode, onClick = { canvasMode = false }, label = { Text("Notes") })
            Spacer(Modifier.width(8.dp))
            FilterChip(selected = canvasMode, onClick = { canvasMode = true }, label = { Text("Canvas") })
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (repository == null && error == null) CircularProgressIndicator()
        OutlinedTextField(draft, { draft = it }, label = { Text("Note content") },
            modifier = Modifier.fillMaxWidth(), minLines = 3, enabled = !busy)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(enabled = !busy && repository != null && draft.isNotBlank(), onClick = {
                val selected = editing
                val content = draft
                perform(clearDraft = true) { if (selected == null) it.create(account, content) else it.edit(selected, content) }
            }) { Text(if (editing == null) "Create note" else "Save changes") }
            TextButton(onClick = {
                if (dirty) { pendingEditor = null; confirmDiscard = true }
                else { editing = null; draft = "" }
            }, enabled = !busy) { Text("Cancel edit") }
        }
        Row {
            Lifecycle.entries.forEach { state ->
                FilterChip(selected = filter == state, onClick = { filter = state }, label = { Text(state.name) })
                Spacer(Modifier.width(8.dp))
            }
        }
        if (canvasMode) {
            NotesCanvas(notes, placements, busy, Modifier.weight(1f), onMove = { placement, x, y ->
                changePlacement { it.move(placement, x, y) }
            })
        } else LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(notes.filter { it.lifecycle == filter }, key = { it.id }) { note ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(note.content)
                        if (note.pinned) Text("Pinned")
                        val placement = placements.find { it.noteId == note.id }
                        TextButton(enabled = !busy, onClick = {
                            changePlacement {
                                if (placement == null) it.place(account, note.id)
                                else it.setVisible(placement, !placement.visible)
                            }
                        }) { Text(if (placement?.visible == true) "Hide from canvas" else "Show on canvas") }
                        Row {
                            TextButton(enabled = !busy, onClick = {
                                if (dirty) { pendingEditor = note; confirmDiscard = true }
                                else { editing = note; draft = note.content }
                            }) { Text("Edit") }
                            TextButton(enabled = !busy, onClick = { perform { it.pin(note, !note.pinned) } }) { Text("Pin / unpin") }
                            TextButton(enabled = !busy, onClick = { perform {
                                if (note.lifecycle == Lifecycle.ACTIVE) it.archive(note) else it.restore(note)
                            } }) { Text(if (note.lifecycle == Lifecycle.ACTIVE) "Archive" else "Restore") }
                            TextButton(enabled = !busy, onClick = { deleteCandidate = note }) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }
    deleteCandidate?.let { note ->
        AlertDialog(onDismissRequest = { deleteCandidate = null }, title = { Text("Move to deleted notes?") },
            text = { Text("You can restore this note from Deleted.") },
            confirmButton = { Button(onClick = { deleteCandidate = null; perform { it.delete(note) } }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deleteCandidate = null }) { Text("Cancel") } })
    }
    if (confirmDiscard) {
        AlertDialog(onDismissRequest = { confirmDiscard = false },
            title = { Text("Discard unsaved changes?") },
            text = { Text("Your current draft has not been saved.") },
            confirmButton = { TextButton(onClick = {
                editing = pendingEditor; draft = pendingEditor?.content ?: ""
                confirmDiscard = false; pendingEditor = null
            }) { Text("Discard changes") } },
            dismissButton = { Button(onClick = { confirmDiscard = false }) { Text("Keep editing") } })
    }
}
