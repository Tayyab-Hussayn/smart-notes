package com.smartsticky.notes

import kotlin.test.*

class NoteExportTest {
    @Test fun authoredContentEscapedWithoutMutation() {
        val note = Note("a", "id", "line\n\"quoted\"\\", 1, 1)
        val exported = NoteExport.json("a", listOf(note))
        assertTrue(exported.contains("line\\u000a\\\"quoted\\\"\\\\"))
        assertEquals("line\n\"quoted\"\\", note.content)
    }
    @Test fun refusesMixedAccountExport() {
        assertFailsWith<IllegalArgumentException> {
            NoteExport.json("a", listOf(Note("b", "id", "secret", 1, 1)))
        }
    }
}
