package com.smartsticky.android

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.smartsticky.notes.*
import com.smartsticky.canvas.CanvasService
import com.smartsticky.reminders.*
import java.time.LocalDateTime
import java.time.ZoneId
import com.smartsticky.sync.BackendClient
import com.smartsticky.sync.BackendException
import com.smartsticky.sync.Session
import com.smartsticky.sync.RemoteNote
import com.smartsticky.sync.SqlSyncEngine
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

data class NotesState(
    val notes: List<Note> = emptyList(), val exposed: Set<String> = emptySet(),
    val busy: Boolean = true, val error: String? = null,
    val account: String = AndroidWorkspace.ACCOUNT, val server: String? = null,
    val email: String? = null,
    val conflicts: Map<String, RemoteNote> = emptyMap(), val message: String? = null,
    val needsAuthentication: Boolean = false,
    val schedules: List<ReminderSchedule> = emptyList(),
    val occurrences: List<StoredOccurrence> = emptyList(),
)

class NotesViewModel(application: Application) : AndroidViewModel(application) {
    private val lock = Mutex()
    private var workspace: AndroidWorkspace? = null
    // Credentials never enter saved instance state, preferences, or the database.
    private var client: BackendClient? = null
    private var session: Session? = null
    private var server: String? = null
    private val account: String get() = session?.let { client!!.localAccountId(it) } ?: AndroidWorkspace.ACCOUNT
    private val mutable = MutableStateFlow(NotesState(busy = false))
    val state = mutable.asStateFlow()
    private fun id() = UUID.randomUUID().toString()
    init { refresh() }
    private fun run(errorMessage: String? = null, action: suspend (AndroidWorkspace) -> Unit) {
        // Reserve the operation before dispatch so rapid taps cannot enqueue duplicate creates.
        val before = mutable.value
        if (before.busy || !mutable.compareAndSet(before, before.copy(busy = true, error = null))) return
        viewModelScope.launch(Dispatchers.IO) {
            lock.withLock {
                mutable.value = mutable.value.copy(busy = true, error = null)
                try {
                    val db = workspace ?: AndroidWorkspace(getApplication()).also { workspace = it }
                    action(db)
                    val reminders = SqlReminderRepository(db.database)
                    val schedules = reminders.schedules(account)
                    mutable.value = mutable.value.copy(
                        notes = db.notes.list(account),
                        exposed = if (session == null) db.canvas.list(account, AndroidWorkspace.SURFACE).filter { it.visible }.map { it.noteId }.toSet() else emptySet(),
                        busy = false, account = account, server = server,
                        schedules = schedules, occurrences = schedules.flatMap { reminders.occurrences(account, it.id) },
                        conflicts = client?.let { SqlSyncEngine(db.database, it, System::currentTimeMillis).conflicts(account).associateBy { note -> note.id } } ?: emptyMap(),
                    )
                } catch (e: CancellationException) {
                    throw e
                } catch (e: BackendException) {
                    mutable.value = mutable.value.copy(busy = false,
                        needsAuthentication = mutable.value.needsAuthentication || (e.status == 401 && session != null),
                        error = if (e.status == 401) "Sign in again to sync. Your saved notes and unsent changes remain on this device."
                            else "The server request failed. Check your connection and account details; saved changes are retained.")
                } catch (e: Exception) {
                    mutable.value = mutable.value.copy(busy = false, error = errorMessage ?: when (e) {
                        is RevisionConflict -> "This note changed. Keep your draft and reopen the saved note before editing."
                        is ReminderRevisionConflict -> "This reminder changed. Refresh reminders before trying again."
                        is IllegalArgumentException -> "Check the server address, account, and note length (1–20,000 characters)."
                        else -> "Unable to complete the action. Your draft and previously saved notes are retained."
                    })
                }
            }
        }
    }
    fun refresh() = run { }
    fun addReminder(note: Note, date: String, zoneName: String, requestId: String) = run("Choose an active note and a future, unambiguous local time.") { db ->
        require(note.accountId == account)
        val local = LocalDateTime.parse(date)
        val offsets = ZoneId.of(zoneName).rules.getValidOffsets(local)
        require(offsets.size == 1) { "Ambiguous or nonexistent local time" }
        ReminderService(db.database, ::id, System::currentTimeMillis)
            .createOneShot(account, note.id, local.toInstant(offsets.single()).toEpochMilli(), requestId)
        mutable.value = mutable.value.copy(message = "Reminder saved on this device. Background notifications are not connected yet.")
    }
    fun completeReminder(value: StoredOccurrence) = run("Could not complete the reminder. Refresh and try again.") { db ->
        ReminderService(db.database, ::id, System::currentTimeMillis).complete(account, value)
    }
    fun snoozeReminder(value: StoredOccurrence) = run("Could not snooze the reminder. Refresh and check that the schedule is enabled.") { db ->
        ReminderService(db.database, ::id, System::currentTimeMillis).snoozeTenMinutes(account, value)
    }
    fun disableReminder(value: ReminderSchedule) = run("Could not disable the schedule. Refresh and try again.") { db ->
        ReminderService(db.database, ::id, System::currentTimeMillis).disable(account, value)
    }
    fun authenticate(endpoint: String, email: String, password: String, register: Boolean) = run { _ ->
        val candidate = BackendClient(endpoint.trim())
        val authenticated = if (register) candidate.register(email.trim(), password) else candidate.login(email.trim(), password)
        if (session != null && candidate.localAccountId(authenticated) != account) {
            runCatching { candidate.logout(authenticated.token) }
            throw IllegalArgumentException("Sign out before changing accounts")
        }
        client = candidate; session = authenticated; server = endpoint.trim().trimEnd('/')
        mutable.value = NotesState(busy = true, account = account, server = server, email = email.trim(),
            message = "Signed in. Tap Sync notes to send saved changes and retrieve your notes.")
    }
    fun signOut() {
        if (mutable.value.busy) return
        val previousClient = client; val previousSession = session
        client = null; session = null; server = null
        // Clear credentials/rendered account data even when reopening the local DB fails.
        mutable.value = NotesState(busy = false, message = "Signed out. Account notes remain saved separately on this device.")
        run { _ ->
            if (previousClient != null && previousSession != null) {
                try { previousClient.logout(previousSession.token) }
                catch (_: BackendException) {
                    mutable.value = mutable.value.copy(message = "Signed out on this device. Server logout could not be confirmed; the short-lived session will expire.")
                }
            }
        }
    }
    fun synchronize() = run { db ->
        val active = requireNotNull(session)
        val result = SqlSyncEngine(db.database, requireNotNull(client), System::currentTimeMillis).synchronize(active)
        mutable.value = mutable.value.copy(message = "Sync processed ${result.uploaded} outgoing and ${result.downloaded} incoming changes. ${result.conflicts} conflicts need review.")
    }
    fun resolveConflict(noteId: String, keepLocalCopy: Boolean) = run { db ->
        SqlSyncEngine(db.database, requireNotNull(client), System::currentTimeMillis).resolve(account, noteId, keepLocalCopy, ::id)
        mutable.value = mutable.value.copy(message = if (keepLocalCopy) "Local text preserved as a new note. Sync again to upload it." else "Server version kept.")
    }
    fun exportNotes(destination: Uri, expectedAccount: String, onExported: () -> Unit) = run { db ->
        require(expectedAccount == account)
        val snapshot = NoteExport.json(account, db.notes.list(account))
        val resolver = getApplication<Application>().contentResolver
        val stream = resolver.openOutputStream(destination, "wt")
            ?: error("Unable to open the export destination.")
        stream.bufferedWriter(Charsets.UTF_8).use { it.write(snapshot) }
        withContext(Dispatchers.Main) { onExported() }
    }
    fun save(note: Note?, content: String, onSaved: (Note) -> Unit) = run { db ->
        require(content.isNotBlank()) { "Write something before saving." }
        require(content.length <= 20_000) { "Please keep notes under 20,000 characters." }
        val service = NoteService(db.notes, ::id, System::currentTimeMillis)
        require(note == null || note.accountId == account)
        val saved = if (note == null) service.create(account, content) else service.edit(note, content)
        withContext(Dispatchers.Main) { onSaved(saved) }
    }
    fun change(note: Note, action: String) = run { db ->
        require(note.accountId == account)
        val service = NoteService(db.notes, ::id, System::currentTimeMillis)
        when (action) { "pin" -> service.pin(note, !note.pinned); "archive" -> service.archive(note); "restore" -> service.restore(note); "delete" -> service.delete(note) }
    }
    fun expose(note: Note, visible: Boolean) = run { db ->
        require(session == null && note.accountId == account)
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
