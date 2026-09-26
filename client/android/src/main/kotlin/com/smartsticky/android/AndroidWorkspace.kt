package com.smartsticky.android

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.SqlNoteRepository
import com.smartsticky.canvas.SqlCanvasRepository

class AndroidWorkspace(context: Context) : AutoCloseable {
    private val driver = AndroidSqliteDriver(NotesDatabase.Schema, context.applicationContext, "notes.db",
        callback = object : AndroidSqliteDriver.Callback(NotesDatabase.Schema) {
            override fun onConfigure(db: SupportSQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
        })
    val database = NotesDatabase(driver)
    val notes = SqlNoteRepository(database)
    val canvas = SqlCanvasRepository(database)
    override fun close() = driver.close()
    companion object { const val ACCOUNT = "local"; const val SURFACE = "android-wallpaper" }
}
