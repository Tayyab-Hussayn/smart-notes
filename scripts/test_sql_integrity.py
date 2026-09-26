"""Exercise actual SQLDelight SQL with stdlib SQLite, without downloading tools.

Complements JVM repository tests; does not validate generated Kotlin or UI code.
"""
from pathlib import Path
import re
import sqlite3
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'client/shared/src/commonMain/sqldelight/com/smartsticky/database/Notes.sq'
PARTS = re.split(r'^([A-Za-z][A-Za-z0-9_]*):\s*$', SOURCE.read_text(), flags=re.M)
SCHEMA = PARTS[0]
QUERIES = dict(zip(PARTS[1::2], PARTS[2::2]))


class NotesIntegrity(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.path = Path(self.directory.name) / 'notes.db'
        self.db = sqlite3.connect(self.path)
        self.db.executescript(SCHEMA)

    def tearDown(self):
        self.db.close()
        self.directory.cleanup()

    def seed(self):
        self.db.execute(QUERIES['insertNote'], ('a', 'id', '  original\n', 1, 1, 1, 'ACTIVE', 0))
        self.db.execute(QUERIES['enqueue'], ('op', 'a', 'id', '  original\n', 1, 'ACTIVE', 0, 1))
        self.db.commit()

    def test_account_isolation_and_restart(self):
        self.seed()
        self.db.close()
        self.db = sqlite3.connect(self.path)
        self.assertEqual([], self.db.execute(QUERIES['listNotes'], ('b',)).fetchall())
        self.assertIsNone(self.db.execute(QUERIES['findNote'], ('b', 'id')).fetchone())
        self.assertEqual('  original\n', self.db.execute(QUERIES['findNote'], ('a', 'id')).fetchone()[2])

    def test_failed_outbox_rolls_back_mutation(self):
        self.seed()
        with self.assertRaises(sqlite3.IntegrityError):
            with self.db:
                self.db.execute(QUERIES['updateNote'], ('changed', 2, 2, 'DELETED', 0, 'a', 'id'))
                self.db.execute(QUERIES['enqueue'], ('op', 'a', 'id', 'changed', 2, 'DELETED', 0, 2))
        row = self.db.execute(QUERIES['findNote'], ('a', 'id')).fetchone()
        self.assertEqual(('  original\n', 'ACTIVE'), (row[2], row[6]))
        self.assertEqual(1, self.db.execute(QUERIES['pendingCount'], ('a',)).fetchone()[0])

    def test_tombstone_and_account_scoped_update(self):
        self.seed()
        with self.db:
            self.db.execute(QUERIES['updateNote'], ('wrong', 2, 2, 'DELETED', 0, 'b', 'id'))
        self.assertEqual('ACTIVE', self.db.execute(QUERIES['findNote'], ('a', 'id')).fetchone()[6])
        with self.db:
            self.db.execute(QUERIES['updateNote'], ('  original\n', 2, 2, 'DELETED', 0, 'a', 'id'))
        self.assertEqual('DELETED', self.db.execute(QUERIES['findNote'], ('a', 'id')).fetchone()[6])


if __name__ == '__main__':
    unittest.main()
