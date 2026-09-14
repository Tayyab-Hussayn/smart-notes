package com.smartsticky.canvas

import com.smartsticky.database.NotesDatabase

/** Fractions of the available surface, preserved when the viewport changes. */
data class CanvasPlacement(
    val accountId: String,
    val id: String,
    val noteId: String,
    val surfaceId: String = "main",
    val x: Double = 0.05,
    val y: Double = 0.05,
    val width: Double = 0.3,
    val height: Double = 0.25,
    val zOrder: Long = 0,
    val opacity: Double = 1.0,
    val visible: Boolean = true,
    val appearance: String = "yellow",
    val revision: Long = 1,
    val updatedAt: Long,
) {
    init {
        require(listOf(accountId, id, noteId, surfaceId, appearance).all { it.isNotBlank() })
        require(x.isFinite() && y.isFinite() && width.isFinite() && height.isFinite())
        require(width > 0.0 && width <= 1.0 && height > 0.0 && height <= 1.0)
        require(x >= 0.0 && y >= 0.0 && x <= 1.0 - width && y <= 1.0 - height)
        require(opacity.isFinite() && opacity in 0.0..1.0)
        require(revision > 0 && updatedAt >= 0)
    }
}

class CanvasRevisionConflict : IllegalStateException("Placement changed; reload before saving")

interface CanvasRepository {
    fun list(accountId: String, surfaceId: String): List<CanvasPlacement>
    fun find(accountId: String, id: String): CanvasPlacement?
    fun save(placement: CanvasPlacement, expectedRevision: Long?, operationId: String)
}

class CanvasService(
    private val repository: CanvasRepository,
    private val newId: () -> String,
    private val now: () -> Long,
) {
    fun place(accountId: String, noteId: String, surfaceId: String = "main"): CanvasPlacement =
        CanvasPlacement(accountId, newId(), noteId, surfaceId, updatedAt = now()).also {
            repository.save(it, null, newId())
        }

    /** Also used by keyboard/button controls so dragging is never required. */
    fun move(placement: CanvasPlacement, x: Double, y: Double): CanvasPlacement {
        require(x.isFinite() && y.isFinite())
        return mutate(placement) { copy(x = x.coerceIn(0.0, 1.0 - width), y = y.coerceIn(0.0, 1.0 - height)) }
    }

    fun resize(placement: CanvasPlacement, width: Double, height: Double): CanvasPlacement {
        require(width.isFinite() && height.isFinite() && width > 0.0 && height > 0.0)
        val w = width.coerceAtMost(1.0)
        val h = height.coerceAtMost(1.0)
        return mutate(placement) { copy(width = w, height = h, x = x.coerceAtMost(1.0 - w), y = y.coerceAtMost(1.0 - h)) }
    }

    fun setVisible(placement: CanvasPlacement, visible: Boolean) = mutate(placement) { copy(visible = visible) }
    fun setOpacity(placement: CanvasPlacement, opacity: Double) = mutate(placement) { copy(opacity = opacity) }
    fun setAppearance(placement: CanvasPlacement, appearance: String) = mutate(placement) { copy(appearance = appearance) }
    fun setZOrder(placement: CanvasPlacement, zOrder: Long) = mutate(placement) { copy(zOrder = zOrder) }

    private fun mutate(placement: CanvasPlacement, transform: CanvasPlacement.() -> CanvasPlacement): CanvasPlacement {
        require(placement.revision < Long.MAX_VALUE)
        return placement.transform().copy(revision = placement.revision + 1, updatedAt = maxOf(placement.updatedAt, now())).also {
            repository.save(it, placement.revision, newId())
        }
    }
}

class SqlCanvasRepository(private val database: NotesDatabase) : CanvasRepository {
    private val queries get() = database.canvasQueries
    override fun list(accountId: String, surfaceId: String) = queries.listPlacements(accountId, surfaceId, ::map).executeAsList()
    override fun find(accountId: String, id: String) = queries.findPlacement(accountId, id, ::map).executeAsOneOrNull()

    override fun save(placement: CanvasPlacement, expectedRevision: Long?, operationId: String) {
        require(operationId.isNotBlank())
        database.transaction {
            val existing = find(placement.accountId, placement.id)
            if (existing?.revision != expectedRevision) throw CanvasRevisionConflict()
            require((expectedRevision ?: 0) < Long.MAX_VALUE)
            require(placement.revision == (expectedRevision ?: 0) + 1)
            require(database.notesQueries.findNote(placement.accountId, placement.noteId).executeAsOneOrNull() != null) {
                "Placement requires an existing note in the same account"
            }
            if (existing != null) {
                require(existing.noteId == placement.noteId && existing.surfaceId == placement.surfaceId)
                require(placement.updatedAt >= existing.updatedAt)
            }
            with(placement) {
                if (existing == null) {
                    queries.putPlacement(accountId, id, noteId, surfaceId, x, y, width, height, zOrder,
                        opacity, if (visible) 1L else 0L, appearance, revision, updatedAt)
                } else {
                    queries.updatePlacement(x, y, width, height, zOrder, opacity, if (visible) 1L else 0L,
                        appearance, revision, updatedAt, accountId, id)
                }
                queries.enqueuePlacement(operationId, accountId, id, noteId, surfaceId, x, y, width, height,
                    zOrder, opacity, if (visible) 1L else 0L, appearance, revision, updatedAt)
            }
        }
    }

    private fun map(accountId: String, id: String, noteId: String, surfaceId: String, x: Double, y: Double,
                    width: Double, height: Double, zOrder: Long, opacity: Double, visible: Long,
                    appearance: String, revision: Long, updatedAt: Long) =
        CanvasPlacement(accountId, id, noteId, surfaceId, x, y, width, height, zOrder, opacity,
            visible != 0L, appearance, revision, updatedAt)
}
