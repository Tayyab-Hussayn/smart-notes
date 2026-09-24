package com.smartsticky.android

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
    var draft by rememberSaveable { mutableStateOf("") }
    var editing by rememberSaveable(stateSaver = listSaver<Note?, Any>(
        save = { note -> note?.let { listOf(it.accountId, it.id, it.content, it.createdAt, it.updatedAt, it.revision, it.lifecycle.name, it.pinned) } ?: emptyList() },
        restore = { values -> if (values.isEmpty()) null else Note(values[0] as String, values[1] as String, values[2] as String, values[3] as Long, values[4] as Long, values[5] as Long, Lifecycle.valueOf(values[6] as String), values[7] as Boolean) }
    )) { mutableStateOf<Note?>(null) }
    var filter by rememberSaveable { mutableStateOf("ACTIVE") }
    var confirm by remember { mutableStateOf<Note?>(null) }
    var wallpaperError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    Surface(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.safeDrawingPadding().imePadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Text("Smart Notes", style = MaterialTheme.typography.headlineLarge)
                Text("Private on this device • no account required")
                Text("Only notes you explicitly show appear on wallpaper. Wallpaper may also appear on your lock screen; avoid sensitive content.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(draft, { draft = it }, Modifier.fillMaxWidth(), label = { Text(if (editing == null) "New note" else "Edit note") }, minLines = 3)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ model.save(editing, draft) { editing = it } }, enabled = !state.busy && draft.isNotBlank()) { Text("Save") }
                    TextButton({ editing = null; draft = "" }, enabled = !state.busy) { Text("New draft") }
                }
                Text("Draft stays here after saving. Tap New draft for another note.", style = MaterialTheme.typography.bodySmall)
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
                        Row {
                            TextButton({ editing = note; draft = note.content }, enabled = !state.busy) { Text("Edit") }
                            TextButton({ model.change(note, "pin") }, enabled = !state.busy) { Text(if (note.pinned) "Unpin" else "Pin") }
                            TextButton({ model.change(note, if (note.lifecycle == Lifecycle.ACTIVE) "archive" else "restore") }, enabled = !state.busy) { Text(if (note.lifecycle == Lifecycle.ACTIVE) "Archive" else "Restore") }
                        }
                        if (note.lifecycle == Lifecycle.ACTIVE) {
                            Row { Checkbox(note.id in state.exposed, { model.expose(note, it) }, enabled = !state.busy); Text("Show on wallpaper", Modifier.padding(top = 12.dp)) }
                        }
                        if (note.lifecycle != Lifecycle.DELETED) TextButton({ confirm = note }, enabled = !state.busy) { Text("Move to trash") }
                    }
                }
            }
        }
    }
    confirm?.let { note -> AlertDialog(onDismissRequest = { confirm = null }, title = { Text("Move note to trash?") }, text = { Text("You can restore it from the deleted tab.") }, confirmButton = { TextButton({ model.change(note, "delete"); confirm = null }) { Text("Move to trash") } }, dismissButton = { TextButton({ confirm = null }) { Text("Cancel") } }) }
}
