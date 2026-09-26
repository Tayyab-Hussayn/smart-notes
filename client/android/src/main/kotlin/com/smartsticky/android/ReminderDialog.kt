package com.smartsticky.android

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartsticky.notes.Note
import com.smartsticky.reminders.OccurrenceState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

@Composable
fun ReminderDialog(note: Note, state: NotesState, model: NotesViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val zone = remember { ZoneId.systemDefault() }
    var date by rememberSaveable { mutableStateOf(LocalDate.now(zone).plusDays(1).toString()) }
    var hour by rememberSaveable { mutableStateOf(9) }
    var minute by rememberSaveable { mutableStateOf(0) }
    val requestId = rememberSaveable(note.id, date, hour, minute) { UUID.randomUUID().toString() }
    val time = "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    val schedules = state.schedules.filter { it.noteId == note.id }
    AlertDialog(onDismissRequest = { if (!state.busy) onDismiss() }, title = { Text("Reminders") }, text = {
        Column(Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("One-time reminders • $zone")
            Text("Reminders are saved on this device. Background notifications and reminder sync are not connected yet.")
            TextButton({
                val current = LocalDate.parse(date)
                DatePickerDialog(context, { _, year, month, day -> date = LocalDate.of(year, month + 1, day).toString() },
                    current.year, current.monthValue - 1, current.dayOfMonth).show()
            }, enabled = !state.busy) { Text("Date: $date") }
            TextButton({ TimePickerDialog(context, { _, h, m -> hour = h; minute = m }, hour, minute, true).show() }, enabled = !state.busy) { Text("Time: $time") }
            Text("Choose a future time. Times skipped or repeated by daylight saving are rejected.", style = MaterialTheme.typography.bodySmall)
            Button({ model.addReminder(note, "${date}T$time", zone.id, requestId) }, enabled = !state.busy) { Text("Add reminder") }
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            TextButton(model::refresh, enabled = !state.busy) { Text("Refresh status") }
            if (schedules.isEmpty()) Text("No reminders for this note.")
            schedules.forEach { schedule ->
                HorizontalDivider()
                Text("${Instant.ofEpochMilli(schedule.firstAt).atZone(zone)}${if (!schedule.enabled) " • disabled" else ""}")
                if (schedule.enabled) TextButton({ model.disableReminder(schedule) }, enabled = !state.busy) { Text("Disable schedule") }
                state.occurrences.filter { it.occurrence.key.scheduleId == schedule.id }.forEach { stored ->
                    val value = stored.occurrence
                    val due = schedule.enabled && value.isDue(state.account, System.currentTimeMillis())
                    Text(if (due) "Due now" else value.state.name.lowercase())
                    value.snoozedUntil?.let { Text("Snoozed until ${Instant.ofEpochMilli(it).atZone(zone)}") }
                    if (schedule.enabled && value.state == OccurrenceState.PENDING) {
                        TextButton({ model.completeReminder(stored) }, enabled = !state.busy) { Text("Complete") }
                        TextButton({ model.snoozeReminder(stored) }, enabled = !state.busy) { Text("Snooze 10 minutes") }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onDismiss, enabled = !state.busy) { Text("Done") } })
}
