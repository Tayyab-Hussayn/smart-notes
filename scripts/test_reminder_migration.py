"""Offline SQLite migration/invariant checks; does not substitute for Kotlin compilation."""
import pathlib
import sqlite3
import unittest

ROOT = pathlib.Path(__file__).resolve().parents[1]
SQL = ROOT / 'client/shared/src/commonMain/sqldelight/com/smartsticky/database'


class ReminderMigrationTest(unittest.TestCase):
    def test_migration_and_constraints(self):
        with sqlite3.connect(':memory:') as db:
            db.execute('PRAGMA foreign_keys=ON')
            db.executescript((SQL / 'Notes.sq').read_text().split('listNotes:')[0])
            db.executescript((SQL / '1.sqm').read_text())
            db.execute("INSERT INTO note VALUES ('a','n','original',1,1,1,'ACTIVE',0)")
            db.executescript((SQL / '2.sqm').read_text())
            db.execute("INSERT INTO reminder_schedule VALUES ('a','s','n',100,100,1,1,1)")
            db.execute("INSERT INTO reminder_occurrence VALUES ('a','s',100,'n','PENDING',NULL,1,1)")
            with self.assertRaises(sqlite3.IntegrityError):
                db.execute("INSERT INTO reminder_occurrence VALUES ('b','s',100,'n','PENDING',NULL,1,1)")
            with self.assertRaises(sqlite3.IntegrityError):
                db.execute("INSERT INTO reminder_occurrence VALUES ('a','s',100,'n','PENDING',NULL,1,1)")
            with self.assertRaises(sqlite3.IntegrityError):
                db.execute("UPDATE reminder_occurrence SET state='COMPLETED',snoozed_until=200")
            self.assertEqual('original', db.execute('SELECT content FROM note').fetchone()[0])
            self.assertEqual((SQL / 'Reminder.sq').read_text().split('findSchedule:')[0].strip(),
                             (SQL / '2.sqm').read_text().strip())


if __name__ == '__main__':
    unittest.main()
