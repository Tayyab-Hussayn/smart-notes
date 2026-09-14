package com.smartsticky.intelligence

import com.smartsticky.notes.Lifecycle
import com.smartsticky.notes.Note

/** Only explicitly permitted signals enter selection; this never emits notifications. */
data class SurfaceCandidate(
    val note: Note,
    val hidden: Boolean = false,
    val enabled: Boolean = true,
    val supported: Boolean = true,
    val completed: Boolean = false,
    val snoozedUntil: Long? = null,
    val cooldownUntil: Long? = null,
    val priority: Int = 0,
    val routineMatches: Boolean = false,
    val dueAt: Long? = null,
    val lastShownAt: Long? = null,
)

data class SelectionPolicy(val capacity: Int, val upcomingWindowMs: Long = 900_000) {
    init { require(capacity >= 0 && upcomingWindowMs >= 0) }
}

data class SelectedNote(val note: Note, val reason: String)

object SurfaceSelector {
    fun select(account: String, now: Long, candidates: List<SurfaceCandidate>, policy: SelectionPolicy): List<SelectedNote> {
        require(account.isNotBlank())
        return candidates.filter {
            it.note.accountId == account && it.note.lifecycle == Lifecycle.ACTIVE &&
                !it.hidden && it.enabled && it.supported && !it.completed &&
                (it.snoozedUntil == null || it.snoozedUntil <= now) &&
                (it.cooldownUntil == null || it.cooldownUntil <= now)
        }.sortedWith(
            compareByDescending<SurfaceCandidate> { it.note.pinned }
                .thenByDescending { it.priority }
                .thenByDescending { urgency(it, now, policy) }
                .thenByDescending { it.routineMatches }
                .thenBy { it.lastShownAt ?: Long.MIN_VALUE }
                .thenBy { it.note.id }
        ).distinctBy { it.note.id }.take(policy.capacity).map {
            SelectedNote(it.note, when {
                it.note.pinned -> "Pinned by you"
                it.priority > 0 -> "Your selected priority"
                urgency(it, now, policy) == 2 -> "Reminder due"
                urgency(it, now, policy) == 1 -> "Reminder coming up"
                it.routineMatches -> "Matches your routine"
                else -> "Not shown recently"
            })
        }
    }

    private fun urgency(candidate: SurfaceCandidate, now: Long, policy: SelectionPolicy): Int {
        val due = candidate.dueAt ?: return 0
        if (due <= now) return 2
        // Compare with saturating addition to avoid overflowing the clock range.
        val limit = if (now > Long.MAX_VALUE - policy.upcomingWindowMs) Long.MAX_VALUE
                    else now + policy.upcomingWindowMs
        return if (due <= limit) 1 else 0
    }
}
