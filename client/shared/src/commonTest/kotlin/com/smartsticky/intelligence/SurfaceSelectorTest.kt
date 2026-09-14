package com.smartsticky.intelligence

import com.smartsticky.notes.*
import kotlin.test.*

class SurfaceSelectorTest {
    private fun candidate(id: String) = SurfaceCandidate(Note("a", id, "private", 0, 0))

    @Test fun hardRulesBeatPinningAndDueTime() {
        val pinned = candidate("pinned").let { it.copy(note = it.note.copy(pinned = true), dueAt = 0) }
        val candidates = listOf(
            pinned.copy(hidden = true), pinned.copy(snoozedUntil = 200),
            pinned.copy(note = pinned.note.copy(accountId = "b")),
            pinned.copy(note = pinned.note.copy(lifecycle = Lifecycle.DELETED)),
            pinned.copy(completed = true), pinned.copy(enabled = false),
            candidate("visible"),
        )
        assertEquals(listOf("visible"), SurfaceSelector.select("a", 100, candidates, SelectionPolicy(10)).map { it.note.id })
    }

    @Test fun rotationAndTiesAreDeterministic() {
        val candidates = listOf(candidate("z"), candidate("a"), candidate("recent").copy(lastShownAt = 90))
        val policy = SelectionPolicy(2)
        val result = SurfaceSelector.select("a", 100, candidates, policy)
        assertEquals(listOf("a", "z"), result.map { it.note.id })
        assertEquals(result, SurfaceSelector.select("a", 100, candidates.reversed(), policy))
    }
}
