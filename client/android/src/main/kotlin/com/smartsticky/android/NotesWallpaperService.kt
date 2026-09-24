package com.smartsticky.android

import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.HandlerThread
import android.service.wallpaper.WallpaperService
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.SurfaceHolder
import com.smartsticky.intelligence.*

/** Reads a fresh, bounded snapshot only while the wallpaper is visible. No network access. */
class NotesWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine = NotesEngine()
    inner class NotesEngine : Engine() {
        private val thread = HandlerThread("notes-wallpaper").apply { start() }
        private val worker = Handler(thread.looper)
        private var workspace: AndroidWorkspace? = null
        @Volatile private var active = false
        @Volatile private var surfaceReady = false
        @Volatile private var destroyed = false
        private val shown = mutableMapOf<String, Long>()
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(37, 38, 35) }
        private val draw = object : Runnable {
            override fun run() {
                if (!active || !surfaceReady || destroyed) return
                val texts = runCatching {
                    val db = workspace ?: AndroidWorkspace(applicationContext).also { workspace = it }
                    val allowed = db.canvas.list(AndroidWorkspace.ACCOUNT, AndroidWorkspace.SURFACE).filter { it.visible }.map { it.noteId }.toSet()
                    val now = System.currentTimeMillis()
                    SurfaceSelector.select(AndroidWorkspace.ACCOUNT, now, db.notes.list(AndroidWorkspace.ACCOUNT).map {
                        SurfaceCandidate(it, hidden = it.id !in allowed, lastShownAt = shown[it.id])
                    }, SelectionPolicy(4)).map { shown[it.note.id] = now; it.note.content.take(1200) }
                }.getOrDefault(emptyList())
                if (active && surfaceReady && !destroyed) render(texts)
                if (active && surfaceReady && !destroyed) worker.postDelayed(this, 30_000)
            }
        }
        private fun render(notes: List<String>) {
            val canvas = runCatching { surfaceHolder.lockCanvas() }.getOrNull() ?: return
            try {
                canvas.drawColor(Color.rgb(22, 29, 35))
                val pad = canvas.width * 0.07f
                val top = canvas.height * 0.16f
                val cardHeight = canvas.height * 0.15f
                textPaint.textSize = 17 * resources.displayMetrics.scaledDensity
                notes.forEachIndexed { index, content ->
                    val y = top + index * (cardHeight + pad / 2)
                    paint.color = Color.rgb(255, 235, 162)
                    canvas.drawRoundRect(pad, y, canvas.width - pad, y + cardHeight, 18f, 18f, paint)
                    val inset = 16 * resources.displayMetrics.density
                    val width = (canvas.width - 2 * pad - 2 * inset).toInt().coerceAtLeast(1)
                    val text = StaticLayout.Builder.obtain(content, 0, content.length, textPaint, width)
                        .setAlignment(Layout.Alignment.ALIGN_NORMAL).setMaxLines(4).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
                    canvas.save()
                    canvas.clipRect(pad, y, canvas.width - pad, y + cardHeight)
                    canvas.translate(pad + inset, y + inset)
                    text.draw(canvas)
                    canvas.restore()
                }
            } finally { runCatching { surfaceHolder.unlockCanvasAndPost(canvas) } }
        }
        private fun refresh() { worker.removeCallbacks(draw); if (active && surfaceReady && !destroyed) worker.post(draw) }
        override fun onVisibilityChanged(visible: Boolean) { active = visible; refresh() }
        override fun onSurfaceCreated(holder: SurfaceHolder) { super.onSurfaceCreated(holder); surfaceReady = true; refresh() }
        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) { super.onSurfaceChanged(holder, format, width, height); refresh() }
        override fun onSurfaceDestroyed(holder: SurfaceHolder) { surfaceReady = false; worker.removeCallbacks(draw); super.onSurfaceDestroyed(holder) }
        override fun onDestroy() {
            destroyed = true; active = false; worker.removeCallbacksAndMessages(null)
            worker.post { workspace?.close(); workspace = null; thread.quitSafely() }
            super.onDestroy()
        }
    }
}
