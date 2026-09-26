"""Validate the real v1 SQL migration preserves existing data using SQLite.

This does not verify SQLDelight generation or the platform migration caller.
"""
from pathlib import Path
import re
import sqlite3
import tempfile
import unittest

SOURCE = Path(__file__).resolve().parents[1] / 'client/shared/src/commonMain/sqldelight/com/smartsticky/database'


def schema(name):
    return re.split(r'^[A-Za-z][A-Za-z0-9_]*:\s*$', (SOURCE / name).read_text(), flags=re.M)[0]


class CanvasMigration(unittest.TestCase):
    def test_existing_notes_and_outbox_survive_upgrade_and_restart(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'existing.db'
            db = sqlite3.connect(path)
            try:
                db.executescript(schema('Notes.sq'))
                db.execute('PRAGMA user_version = 1')
                notes = [('a', 'n', '  original\n', 1, 7, 3, 'ACTIVE', 1),
                         ('b', 'n', 'deleted note', 2, 8, 4, 'DELETED', 0)]
                operations = [('op-a', 'a', 'n', 'earlier content', 2, 'ACTIVE', 0, 6),
                              ('op-b', 'b', 'n', 'deleted note', 4, 'DELETED', 0, 8)]
                db.executemany('INSERT INTO note VALUES (?, ?, ?, ?, ?, ?, ?, ?)', notes)
                db.executemany('INSERT INTO outbox VALUES (?, ?, ?, ?, ?, ?, ?, ?)', operations)
                db.commit()
                db.executescript('BEGIN;\n' + (SOURCE / '1.sqm').read_text() + '\nPRAGMA user_version = 2;\nCOMMIT;')
                self.assertEqual(notes, db.execute('SELECT * FROM note ORDER BY account_id').fetchall())
                self.assertEqual(operations, db.execute('SELECT * FROM outbox ORDER BY operation_id').fetchall())
                db.execute('PRAGMA foreign_keys = ON')
                placement = ('a', 'p', 'n', 'main', .1, .1, .3, .25, 0, 1., 1, 'yellow', 1, 9)
                db.execute('INSERT INTO canvas_placement VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)', placement)
                db.commit()
                db.close()
                db = sqlite3.connect(path)
                self.assertEqual(2, db.execute('PRAGMA user_version').fetchone()[0])
                self.assertEqual(notes, db.execute('SELECT * FROM note ORDER BY account_id').fetchall())
                self.assertEqual(operations, db.execute('SELECT * FROM outbox ORDER BY operation_id').fetchall())
                self.assertEqual(placement, db.execute('SELECT * FROM canvas_placement').fetchone())
                self.assertEqual([], db.execute('PRAGMA foreign_key_check').fetchall())
            finally:
                db.close()

    def test_migration_matches_fresh_schema(self):
        upgraded = sqlite3.connect(':memory:')
        fresh = sqlite3.connect(':memory:')
        try:
            upgraded.executescript(schema('Notes.sq') + (SOURCE / '1.sqm').read_text())
            fresh.executescript(schema('Notes.sq') + schema('Canvas.sq'))
            query = "SELECT type, name, tbl_name, sql FROM sqlite_master ORDER BY type, name"
            self.assertEqual(fresh.execute(query).fetchall(), upgraded.execute(query).fetchall())
        finally:
            upgraded.close()
            fresh.close()


if __name__ == '__main__':
    unittest.main()
