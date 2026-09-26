package com.smartsticky.app

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.smartsticky.canvas.SqlCanvasRepository
import com.smartsticky.database.NotesDatabase
import com.smartsticky.notes.SqlNoteRepository
import java.nio.file.Files
import java.nio.file.Path

class LocalWorkspace private constructor(private val driver: JdbcSqliteDriver) : AutoCloseable {
    val database = NotesDatabase(driver)
    val notes = SqlNoteRepository(database)
    val canvas = SqlCanvasRepository(database)
    override fun close() = driver.close()

    companion object {
        fun open(directory: Path): LocalWorkspace {
            Files.createDirectories(directory)
            val driver = JdbcSqliteDriver("jdbc:sqlite:${directory.resolve("notes.db")}")
            try {
                driver.execute(null, "PRAGMA foreign_keys = ON", 0)
                driver.execute(null, "PRAGMA busy_timeout = 5000", 0)
                // Use the same transaction owner as generated schema creation/migration.
                NotesDatabase(driver).transaction {
                    val version = driver.executeQuery(null, "PRAGMA user_version", { cursor ->
                        check(cursor.next().value)
                        QueryResult.Value(requireNotNull(cursor.getLong(0)))
                    }, 0).value
                    val current = NotesDatabase.Schema.version
                    check(version in 0..current) { "Unsupported database version" }
                    if (version == 0L) NotesDatabase.Schema.create(driver)
                    else if (version < current) NotesDatabase.Schema.migrate(driver, version, current)
                    driver.execute(null, "PRAGMA user_version = $current", 0)
                }
                return LocalWorkspace(driver)
            } catch (failure: Exception) {
                driver.close()
                throw failure
            }
        }
    }
}
