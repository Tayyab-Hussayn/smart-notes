"""Verify schema upgrade without discarding an unsent authored note."""
from pathlib import Path
import re
import sqlite3
import unittest

ROOT = Path(__file__).resolve().parents[1] / 'client/shared/src/commonMain/sqldelight/com/smartsticky/database'


def schema(name):
    return re.split(r'^\w+:\s*$', (ROOT / name).read_text(), flags=re.M)[0]


class SyncMigrationTest(unittest.TestCase):
    def test_upgrade_keeps_outbox_and_matches_fresh_schema(self):
        old = sqlite3.connect(':memory:')
        fresh = sqlite3.connect(':memory:')
        try:
            old.executescript(schema('Notes.sq'))
            old.execute("INSERT INTO note VALUES ('owner','note','unsent',1,1,1,'ACTIVE',0)")
            old.execute("INSERT INTO outbox VALUES ('op','owner','note','unsent',1,'ACTIVE',0,1)")
            old.commit()
            for migration in ('1.sqm', '2.sqm', '3.sqm'):
                old.executescript((ROOT / migration).read_text())
            for table in ('Notes.sq', 'Canvas.sq', 'Reminder.sq', 'Sync.sq'):
                fresh.executescript(schema(table))
            self.assertEqual('unsent', old.execute('SELECT content FROM note').fetchone()[0])
            self.assertEqual('op', old.execute('SELECT operation_id FROM outbox').fetchone()[0])
            def columns(connection):
                tables = connection.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name").fetchall()
                return {name: connection.execute(f'PRAGMA table_info({name})').fetchall() for (name,) in tables}
            self.assertEqual(columns(old), columns(fresh))
        finally:
            old.close()
            fresh.close()


if __name__ == '__main__':
    unittest.main()
