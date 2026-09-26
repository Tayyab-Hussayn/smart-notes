package com.smartsticky.app

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.smartsticky.canvas.CanvasPlacement
import com.smartsticky.notes.Lifecycle
import com.smartsticky.notes.Note
import kotlin.math.roundToInt

/** Placements are fractions of the viewport; pixel offsets never enter storage. */
@Composable
fun NotesCanvas(
    notes: List<Note>, placements: List<CanvasPlacement>, busy: Boolean,
    modifier: Modifier = Modifier,
    onMove: (CanvasPlacement, Double, Double) -> Unit,
) {
    BoxWithConstraints(modifier.fillMaxWidth().background(Color(0xFFF2EFE8))) {
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val visible = placements.filter { p -> p.visible && notes.any { it.id == p.noteId && it.accountId == p.accountId && it.lifecycle == Lifecycle.ACTIVE } }
        if (visible.isEmpty()) Text("Choose Show on canvas from a note to place it here.", Modifier.padding(20.dp))
        visible.sortedWith(compareBy<CanvasPlacement> { it.zOrder }.thenBy { it.id }).forEach { placement ->
            key(placement.id) {
                var x by remember(placement) { mutableStateOf(placement.x) }
                var y by remember(placement) { mutableStateOf(placement.y) }
                val note = notes.first { it.id == placement.noteId && it.accountId == placement.accountId }
                Card(Modifier.offset { IntOffset((x * widthPx).roundToInt(), (y * heightPx).roundToInt()) }
                    .width(maxWidth * placement.width.toFloat())
                    .height(maxHeight * placement.height.toFloat())
                    .alpha(placement.opacity.toFloat())) {
                    Column(Modifier.fillMaxSize().background(Color(0xFFFFE89C)).padding(8.dp)) {
                        Text("Move", Modifier.fillMaxWidth().pointerInput(placement, busy, widthPx, heightPx) {
                            if (!busy && widthPx > 0 && heightPx > 0) detectDragGestures(
                                onDragEnd = {
                                    onMove(placement, x, y)
                                    x = placement.x; y = placement.y
                                },
                                onDragCancel = { x = placement.x; y = placement.y },
                            ) { change, delta ->
                                change.consume()
                                x = (x + delta.x / widthPx).coerceIn(0.0, 1.0 - placement.width)
                                y = (y + delta.y / heightPx).coerceIn(0.0, 1.0 - placement.height)
                            }
                        })
                        Text(note.content, Modifier.weight(1f))
                        // Named buttons provide a keyboard/screen-reader alternative to dragging.
                        Row {
                            TextButton(enabled = !busy, onClick = { onMove(placement, x - .05, y) }) { Text("Left") }
                            TextButton(enabled = !busy, onClick = { onMove(placement, x + .05, y) }) { Text("Right") }
                        }
                        Row {
                            TextButton(enabled = !busy, onClick = { onMove(placement, x, y - .05) }) { Text("Up") }
                            TextButton(enabled = !busy, onClick = { onMove(placement, x, y + .05) }) { Text("Down") }
                        }
                    }
                }
            }
        }
    }
}
