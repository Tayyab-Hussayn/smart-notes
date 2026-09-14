"""Opt-in real PostgreSQL checks; use a dedicated migrated test database."""
import importlib.util
import os
import unittest
from uuid import uuid4
from smart_sticky_backend.database import Database
from smart_sticky_backend.sync import apply_operation, SyncConflict

URL = os.environ.get("SMART_STICKY_TEST_DATABASE_URL")


@unittest.skipUnless(URL and importlib.util.find_spec("psycopg"), "Dedicated migrated PostgreSQL test URL and psycopg required")
class PostgresSyncTests(unittest.TestCase):
    def setUp(self):
        self.database = Database(URL)
        self.user, self.other = uuid4(), uuid4()
        with self.database.transaction() as connection:
            for user in (self.user, self.other):
                connection.execute("INSERT INTO users(id,email,password_hash) VALUES(%s,%s,%s)", (user, f"{user}@test.invalid", "unused"))
        self.addCleanup(self.cleanup_users)

    def cleanup_users(self):
        with self.database.transaction() as connection:
            connection.execute("DELETE FROM users WHERE id IN (%s,%s)", (self.user, self.other))

    def apply(self, operation, user=None):
        with self.database.transaction() as connection:
            return apply_operation(connection, user or self.user, operation)

    def test_replay_conflict_isolation_and_tombstone(self):
        op = {"operation_id": str(uuid4()), "note_id": str(uuid4()), "base_revision": 0,
              "kind": "upsert", "note": {"title": "title", "body": "first"}}
        result = self.apply(op)
        self.assertEqual(self.apply(op), result)
        with self.assertRaises(SyncConflict):
            self.apply({**op, "note": {"body": "changed"}})
        # Same IDs in another account neither disclose nor mutate original state.
        self.assertEqual(self.apply(op, self.other)["revision"], 1)
        stale = {**op, "operation_id": str(uuid4())}
        with self.assertRaises(SyncConflict):
            self.apply(stale)
        delete = {**op, "operation_id": str(uuid4()), "base_revision": 1, "kind": "delete", "note": None}
        self.assertTrue(self.apply(delete)["deleted"])
        with self.assertRaises(SyncConflict):
            self.apply({**op, "operation_id": str(uuid4()), "base_revision": 2})
        with self.database.transaction() as connection:
            row = connection.execute("SELECT payload,revision FROM notes WHERE user_id=%s AND id=%s", (self.user, op["note_id"])).fetchone()
            self.assertEqual(row["payload"], {})
            self.assertEqual(row["revision"], 2)
