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
import com.smartsticky.notes.*
import com.smartsticky.canvas.*
import com.smartsticky.sync.*
import java.nio.file.Path
import java.util.UUID
import java.awt.FileDialog
import java.awt.Frame
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun main() = application {
    var closeRequested by remember { mutableStateOf(false) }
    Window(onCloseRequest = { closeRequested = true }, title = "Smart Notes") {
        MaterialTheme { App(closeRequested, { closeRequested = false }, ::exitApplication) }
    }
}

@Composable
private fun App(closeRequested: Boolean, cancelClose: () -> Unit, exit: () -> Unit) {
    var workspace by remember { mutableStateOf<LocalWorkspace?>(null) }
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
    var reminderNote by remember { mutableStateOf<Note?>(null) }
    var signIn by remember { mutableStateOf(false) }
    var client by remember { mutableStateOf<BackendClient?>(null) }
    var session by remember { mutableStateOf<Session?>(null) }
    var conflicts by remember { mutableStateOf(emptyList<RemoteNote>()) }
    val dirty = draft != (editing?.content ?: "")
    val account = session?.let { client?.localAccountId(it) } ?: "local-default"
    DisposableEffect(workspace) {
        val opened = workspace
        onDispose { opened?.close() }
    }
    LaunchedEffect(Unit) {
        try {
            workspace = withContext(Dispatchers.IO) {
                LocalWorkspace.open(Path.of(System.getProperty("user.home"), ".smart-notes"))
            }
            repository = workspace!!.notes
            canvasRepository = workspace!!.canvas
            notes = withContext(Dispatchers.IO) { repository!!.list(account) }
            placements = withContext(Dispatchers.IO) { canvasRepository!!.list(account, "main") }
        } catch (_: Exception) { error = "Cannot open notes safely. Your data has not been reset." }
    }
    LaunchedEffect(closeRequested, busy, dirty, workspace, error) {
        if (closeRequested && !busy && !dirty && reminderNote == null && !signIn && (workspace != null || error != null)) exit()
    }
    fun loadAccount(newSession: Session?) {
        notes = emptyList(); placements = emptyList(); conflicts = emptyList()
        editing = null; draft = ""; session = newSession
        busy = true
        scope.launch {
            try {
                val owner = newSession?.let { client!!.localAccountId(it) } ?: "local-default"
                val loaded = withContext(Dispatchers.IO) { repository!!.list(owner) to canvasRepository!!.list(owner, "main") }
                notes = loaded.first; placements = loaded.second; error = null
            } catch (_: Exception) { error = "Cannot load this workspace. Other accounts' content is hidden." }
            finally { busy = false }
        }
    }
    fun sync(resolve: RemoteNote? = null, keepLocal: Boolean = false) {
        val authenticated = session ?: return
        val backend = client ?: return
        busy = true
        scope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    val engine = SqlSyncEngine(workspace!!.database, backend, System::currentTimeMillis)
                    val owner = backend.localAccountId(authenticated)
                    if (resolve != null) engine.resolve(owner, resolve.id, keepLocal) { UUID.randomUUID().toString() }
                    engine.synchronize(authenticated)
                    Triple(repository!!.list(owner), canvasRepository!!.list(owner, "main"), engine.conflicts(owner))
                }
                notes = result.first; placements = result.second; conflicts = result.third
                editing = null; error = null
            } catch (_: Exception) { error = "Sync paused. Local changes are preserved. Check connection or sign in again." }
            finally { busy = false }
        }
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
        Text(if (session == null) "Local workspace · works offline" else "Account workspace · local edits work offline")
        Row {
            TextButton(enabled = !busy && !dirty && workspace != null, onClick = { signIn = true }) { Text("Sign in / switch account") }
            if (session != null) {
                TextButton(enabled = !busy && !dirty, onClick = { sync() }) { Text("Sync notes") }
                TextButton(enabled = !busy && !dirty, onClick = {
                    val old = session!!
                    val backend = client
                    client = null
                    loadAccount(null)
                    scope.launch { withContext(Dispatchers.IO) { runCatching { backend?.logout(old.token) } } }
                }) { Text("Sign out") }
            }
        }
        if (dirty) Text("Save or cancel your draft before switching accounts or syncing.")
        TextButton(enabled = !busy && repository != null, onClick = {
            val dialog = FileDialog(null as Frame?, "Export notes as JSON", FileDialog.SAVE)
            dialog.file = "smart-notes.json"
            dialog.isVisible = true
            val file = dialog.file
            val directory = dialog.directory
            dialog.dispose()
            if (file != null && directory != null) {
                val target = Path.of(directory, file)
                busy = true
                scope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            val content = NoteExport.json(account, repository!!.list(account))
                            // Never overwrite a user's existing file implicitly.
                            Files.writeString(target, content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)
                        }
                        error = null
                    } catch (_: Exception) { error = "Export failed. Choose a new filename and try again." }
                    finally { busy = false }
                }
            }
        }) { Text("Export authored notes") }
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
                            TextButton(enabled = !busy, onClick = { perform { it.duplicate(note) } }) { Text("Duplicate") }
                            TextButton(enabled = !busy, onClick = { reminderNote = note }) { Text("Reminders") }
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
    if (closeRequested && dirty && !busy) {
        AlertDialog(onDismissRequest = cancelClose,
            title = { Text("Close without saving?") },
            text = { Text("Your current draft has not been saved.") },
            confirmButton = { TextButton(onClick = exit) { Text("Discard and close") } },
            dismissButton = { Button(onClick = cancelClose) { Text("Keep editing") } })
    }
    reminderNote?.let { selected ->
        workspace?.let { ReminderEditor(it.database, selected) { reminderNote = null; cancelClose() } }
    }
    if (signIn) AccountDialog(onDismiss = { signIn = false; cancelClose() }, onSignedIn = { backend, authenticated ->
        signIn = false; client = backend; loadAccount(authenticated); cancelClose()
    })
    conflicts.firstOrNull()?.let { conflict ->
        AlertDialog(onDismissRequest = { conflicts = emptyList() }, title = { Text("A note changed on another device") },
            text = { Text("Keep your local text as a new note, or replace it with the server version. Canvas placement and reminders stay with the original note.") },
            confirmButton = { Button(enabled = !busy, onClick = { sync(conflict, true) }) { Text("Keep my text as a new note") } },
            dismissButton = { TextButton(enabled = !busy, onClick = { sync(conflict, false) }) { Text("Use server version") } })
    }
}
