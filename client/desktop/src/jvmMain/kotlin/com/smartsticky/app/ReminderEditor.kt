package com.smartsticky.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.Note
import com.smartsticky.reminders.*
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ReminderEditor(database: NotesDatabase, note: Note, onDismiss: () -> Unit) {
    val repo = remember(database) { SqlReminderRepository(database) }
    val scope = rememberCoroutineScope()
    var date by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf("") }
    var schedules by remember { mutableStateOf(emptyList<ReminderSchedule>()) }
    var occurrences by remember { mutableStateOf(emptyList<StoredOccurrence>()) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val zone = remember { ZoneId.systemDefault() }
    fun refresh(action: () -> Unit = {}) {
        if (busy) return
        busy = true
        scope.launch {
            try {
                val loaded = withContext(Dispatchers.IO) {
                    action()
                    val own = repo.schedules(note.accountId).filter { it.noteId == note.id }
                    own to own.flatMap { repo.occurrences(note.accountId, it.id) }
                }
                schedules = loaded.first; occurrences = loaded.second; error = null
            } catch (_: Exception) { error = "Could not save reminder. Check the date and try again." }
            finally { busy = false }
        }
    }
    LaunchedEffect(note.id) { refresh() }
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("Reminders") },
        text = {
            Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Local time zone: $zone. Native notifications are not connected yet.")
                OutlinedTextField(date, { date = it }, label = { Text("Date and time: 2026-09-20T09:00") }, enabled = !busy)
                OutlinedTextField(interval, { interval = it }, label = { Text("Repeat every N hours (optional)") }, enabled = !busy)
                Text("Repeats use elapsed hours, not local calendar days.")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Button(enabled = !busy && date.isNotBlank(), onClick = {
                    refresh {
                        val local = LocalDateTime.parse(date.trim())
                        val offsets = zone.rules.getValidOffsets(local)
                        require(offsets.size == 1) // Reject ambiguous/nonexistent DST local times.
                        val at = local.toInstant(offsets.single()).toEpochMilli()
                        val now = System.currentTimeMillis()
                        require(at > now)
                        val every = interval.trim().takeIf { it.isNotEmpty() }?.toLong()?.let {
                            require(it in 1..8760); it * 3_600_000
                        }
                        val schedule = ReminderSchedule(note.accountId, UUID.randomUUID().toString(), note.id,
                            at, every, updatedAt = now)
                        database.transaction {
                            repo.save(schedule, null, UUID.randomUUID().toString())
                            repo.materialize(note.accountId, schedule.id, at, at, now) { UUID.randomUUID().toString() }
                        }
                    }
                }) { Text("Add reminder") }
                schedules.forEach { schedule ->
                    Text("${Instant.ofEpochMilli(schedule.firstAt).atZone(zone)}${if (!schedule.enabled) " · disabled" else ""}")
                    TextButton(enabled = !busy && schedule.enabled, onClick = { refresh {
                        repo.save(schedule.copy(enabled = false, revision = schedule.revision + 1,
                            updatedAt = System.currentTimeMillis()), schedule.revision, UUID.randomUUID().toString())
                    } }) { Text("Disable schedule") }
                }
                occurrences.forEach { stored ->
                    val occurrence = stored.occurrence
                    Text("${Instant.ofEpochMilli(occurrence.key.scheduledAt).atZone(zone)} · ${occurrence.state}")
                    if (occurrence.state == OccurrenceState.PENDING) {
                        Row {
                            TextButton(enabled = !busy, onClick = { refresh {
                                repo.save(stored.copy(occurrence = occurrence.complete(), revision = stored.revision + 1,
                                    updatedAt = System.currentTimeMillis()), stored.revision, UUID.randomUUID().toString())
                            } }) { Text("Complete") }
                            TextButton(enabled = !busy, onClick = { refresh {
                                val now = System.currentTimeMillis()
                                val until = maxOf(now, occurrence.key.scheduledAt, occurrence.snoozedUntil ?: 0) + 600_000
                                repo.save(stored.copy(occurrence = occurrence.snooze(until, now), revision = stored.revision + 1,
                                    updatedAt = now), stored.revision, UUID.randomUUID().toString())
                            } }) { Text("Snooze 10 min") }
                        }
                    }
                }
            }
        }, confirmButton = { TextButton(enabled = !busy, onClick = onDismiss) { Text("Done") } })
}
