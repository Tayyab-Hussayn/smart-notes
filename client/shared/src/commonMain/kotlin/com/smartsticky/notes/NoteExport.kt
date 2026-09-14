package com.smartsticky.notes

/** Portable authored-note export. Sync operations and credentials are excluded. */
object NoteExport {
    fun json(accountId: String, notes: List<Note>): String {
        require(accountId.isNotBlank())
        require(notes.all { it.accountId == accountId }) { "Export contains another account" }
        return "{\"format\":\"smart-notes-authored-v1\",\"notes\":[" +
            notes.sortedBy { it.id }.joinToString(",") {
                "{\"id\":${quote(it.id)},\"content\":${quote(it.content)}," +
                    "\"createdAt\":${it.createdAt},\"updatedAt\":${it.updatedAt}," +
                    "\"lifecycle\":${quote(it.lifecycle.name)},\"pinned\":${it.pinned}}"
            } + "]}"
    }

    private fun quote(value: String): String = buildString {
        append('"')
        value.forEach { character ->
            when (character) {
                '"' -> append("\\\"")
                '\\' -> append("\\\\")
                else -> if (character.code < 32 || character.isSurrogate()) {
                    append("\\u").append(character.code.toString(16).padStart(4, '0'))
                } else append(character)
            }
        }
        append('"')
    }
}
