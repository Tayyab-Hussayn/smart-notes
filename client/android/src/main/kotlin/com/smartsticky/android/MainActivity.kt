package com.smartsticky.android

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.smartsticky.notes.Lifecycle
import com.smartsticky.notes.Note

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { NotesScreen() } }
    }
}

@Composable
private fun NotesScreen(model: NotesViewModel = viewModel()) {
    val state by model.state.collectAsStateWithLifecycle()
    // Saved editor state is scoped to the authenticated workspace, including process recreation.
    key(state.account) { WorkspaceScreen(model, state) }
}

@Composable
private fun WorkspaceScreen(model: NotesViewModel, state: NotesState) {
    var draft by rememberSaveable { mutableStateOf("") }
    var editing by rememberSaveable(stateSaver = listSaver<Note?, Any>(
        save = { note -> note?.let { listOf(it.accountId, it.id, it.content, it.createdAt, it.updatedAt, it.revision, it.lifecycle.name, it.pinned) } ?: emptyList() },
        restore = { values -> if (values.isEmpty()) null else Note(values[0] as String, values[1] as String, values[2] as String, values[3] as Long, values[4] as Long, values[5] as Long, Lifecycle.valueOf(values[6] as String), values[7] as Boolean) }
    )) { mutableStateOf<Note?>(null) }
    var filter by rememberSaveable { mutableStateOf("ACTIVE") }
    var confirm by remember { mutableStateOf<Note?>(null) }
    var wallpaperError by remember { mutableStateOf<String?>(null) }
    var exportMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var discardAction by remember { mutableStateOf<(() -> Unit)?>(null) }
    var accountDialog by remember { mutableStateOf(false) }
    var conflict by remember { mutableStateOf<Note?>(null) }
    var reminderNoteId by rememberSaveable { mutableStateOf<String?>(null) }
    var exportAccount by rememberSaveable { mutableStateOf<String?>(null) }
    val dirty = draft != (editing?.content ?: "")
    val context = LocalContext.current
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        val expectedAccount = exportAccount
        exportAccount = null
        if (uri != null && expectedAccount != null) model.exportNotes(uri, expectedAccount) { exportMessage = "Saved notes exported, including archived and deleted notes." }
    }
    fun replaceDraft(note: Note?) {
        val replace = { editing = note; draft = note?.content ?: "" }
        if (dirty) discardAction = replace else replace()
    }
    BackHandler(enabled = dirty || state.busy) {
        if (!state.busy) discardAction = { (context as? android.app.Activity)?.finish(); Unit }
    }
    Surface(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.safeDrawingPadding().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Smart Notes", style = MaterialTheme.typography.headlineLarge)
                Text(if (state.server == null) "Private on this device • no account required" else "${state.email} • ${state.server}")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (state.server == null || state.needsAuthentication) TextButton({ accountDialog = true }, enabled = !state.busy && !dirty) { Text("Sign in") }
                    if (state.server != null) {
                        TextButton({ replaceDraft(null); model.synchronize() }, enabled = !state.busy && !dirty && !state.needsAuthentication) { Text("Sync notes") }
                        TextButton({ model.signOut() }, enabled = !state.busy && !dirty) { Text("Sign out") }
                    }
                }
                if (dirty) Text("Save or discard your draft before changing account or syncing.", style = MaterialTheme.typography.bodySmall)
                state.message?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                if (state.server != null) Text("Wallpaper uses your device-only notes. Sign out to manage those notes and their wallpaper visibility.", style = MaterialTheme.typography.bodySmall)
                Text("Only notes you explicitly show appear on wallpaper. Wallpaper may also appear on your lock screen; avoid sensitive content.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(draft, { draft = it }, Modifier.fillMaxWidth(), enabled = !state.busy, label = { Text(if (editing == null) "New note" else "Edit note") }, minLines = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ model.save(editing, draft) { editing = it } }, enabled = !state.busy && draft.isNotBlank()) { Text("Save") }
                    if (editing != null && state.error != null) TextButton({ model.save(null, draft) { editing = it } }, enabled = !state.busy && draft.isNotBlank()) { Text("Save as copy") }
                    TextButton({ replaceDraft(null) }, enabled = !state.busy) { Text("New draft") }
                }
                Text("Draft stays here after saving. Tap New draft for another note.", style = MaterialTheme.typography.bodySmall)
                TextButton({ exportMessage = null; exportAccount = state.account; export.launch("smart-notes.json") }, enabled = !state.busy && !dirty) { Text("Export saved notes") }
                exportMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                Button({
                    runCatching { context.startActivity(Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, ComponentName(context, NotesWallpaperService::class.java))) }
                        .onFailure { wallpaperError = "This device cannot open the live wallpaper picker." }
                }) { Text("Choose notes wallpaper") }
                wallpaperError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
                Row {
                    Lifecycle.entries.forEach { lifecycle ->
                        TextButton({ filter = lifecycle.name }) { Text(if (lifecycle.name == filter) "• ${lifecycle.name.lowercase()}" else lifecycle.name.lowercase()) }
                    }
                }
            }
            val visible = state.notes.filter { it.lifecycle.name == filter }
            if (visible.isEmpty()) item { Text("No ${filter.lowercase()} notes yet.") }
            items(visible, key = { it.id }) { note ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(note.content)
                        if (note.lifecycle == Lifecycle.ACTIVE) TextButton({ reminderNoteId = note.id; model.refresh() }, enabled = !state.busy && !dirty) { Text("Reminders") }
                        Row {
                            TextButton({ replaceDraft(note) }, enabled = !state.busy) { Text("Edit") }
                            TextButton({ model.change(note, "pin") }, enabled = !state.busy && editing?.id != note.id) { Text(if (note.pinned) "Unpin" else "Pin") }
                            TextButton({ model.change(note, if (note.lifecycle == Lifecycle.ACTIVE) "archive" else "restore") }, enabled = !state.busy && editing?.id != note.id) { Text(if (note.lifecycle == Lifecycle.ACTIVE) "Archive" else "Restore") }
                        }
                        if (note.id in state.conflicts) TextButton({ conflict = note }, enabled = !state.busy && !dirty) { Text("Review sync conflict") }
                        if (note.lifecycle == Lifecycle.ACTIVE && state.server == null) {
                            Row { Checkbox(note.id in state.exposed, { model.expose(note, it) }, enabled = !state.busy); Text("Show on wallpaper", Modifier.padding(top = 12.dp)) }
                        }
                        if (note.lifecycle != Lifecycle.DELETED) TextButton({ confirm = note }, enabled = !state.busy && editing?.id != note.id) { Text("Move to trash") }
                    }
                }
            }
        }
    }
    if (accountDialog) AccountDialog(state.server, { accountDialog = false }, model::authenticate)
    state.notes.firstOrNull { it.id == reminderNoteId && it.lifecycle == Lifecycle.ACTIVE }?.let {
        ReminderDialog(it, state, model) { reminderNoteId = null }
    }
    conflict?.let { note ->
        AlertDialog(onDismissRequest = { conflict = null }, title = { Text("Choose how to resolve this note") },
            text = { Column(Modifier.heightIn(max = 320.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Keeping your text creates a new note before applying the server version. Layouts and reminders stay with the original.")
                Text("Your saved text", style = MaterialTheme.typography.titleSmall)
                Text(note.content.take(2_000))
                Text("Server version", style = MaterialTheme.typography.titleSmall)
                val remote = state.conflicts[note.id]
                Text(if (remote?.deleted == true) "Deleted on the server" else remote?.payload?.body?.take(2_000) ?: "Unavailable")
                if (note.content.length > 2_000 || (remote?.payload?.body?.length ?: 0) > 2_000) Text("Long previews show only the first 2,000 characters. Keeping a copy preserves the full local text.")
            } },
            confirmButton = { TextButton({ replaceDraft(null); model.resolveConflict(note.id, true); conflict = null }) { Text("Keep my text as a copy") } },
            dismissButton = { Column {
                TextButton({ replaceDraft(null); model.resolveConflict(note.id, false); conflict = null }) { Text("Use server version") }
                TextButton({ conflict = null }) { Text("Cancel") }
            } })
    }
    discardAction?.let { action ->
        AlertDialog(onDismissRequest = { discardAction = null }, title = { Text("Discard unsaved changes?") },
            text = { Text("Save your note first to keep these changes.") },
            confirmButton = { TextButton({ discardAction = null; action() }) { Text("Discard") } },
            dismissButton = { TextButton({ discardAction = null }) { Text("Keep editing") } })
    }
    confirm?.let { note -> AlertDialog(onDismissRequest = { confirm = null }, title = { Text("Move note to trash?") }, text = { Text("You can restore it from the deleted tab.") }, confirmButton = { TextButton({ model.change(note, "delete"); confirm = null }) { Text("Move to trash") } }, dismissButton = { TextButton({ confirm = null }) { Text("Cancel") } }) }
}
