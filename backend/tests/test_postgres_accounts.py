"""Full HTTP lifecycle against a dedicated migrated PostgreSQL database."""
import importlib.util
import os
import unittest
from uuid import uuid4

URL = os.environ.get("SMART_STICKY_TEST_DATABASE_URL")
READY = URL and all(importlib.util.find_spec(m) for m in ("psycopg", "fastapi", "httpx"))


@unittest.skipUnless(READY, "HTTP dependencies and migrated PostgreSQL test database required")
class AccountTests(unittest.TestCase):
    def setUp(self):
        from fastapi.testclient import TestClient
        from smart_sticky_backend.config import Settings
        from smart_sticky_backend.database import Database
        from smart_sticky_backend.main import create_app
        self.database = Database(URL)
        self.users = []
        self.client = TestClient(create_app(Settings("test", ("testserver",), URL)))
        self.addCleanup(self.client.close)
        self.addCleanup(self.cleanup)

    def cleanup(self):
        with self.database.transaction() as connection:
            for user in self.users:
                connection.execute("DELETE FROM users WHERE id=%s", (user,))
                connection.execute("DELETE FROM account_deletions WHERE user_id=%s", (user,))

    def register(self):
        credentials = {"email": f"{uuid4()}@test.invalid", "password": "a-long-secret-password"}
        response = self.client.post("/v1/auth/register", json=credentials)
        self.assertEqual(response.status_code, 201, response.text)
        session = response.json()
        self.users.append(session["user_id"])
        return credentials, session

    @staticmethod
    def bearer(session):
        return {"Authorization": "Bearer " + session["access_token"]}

    def test_sessions_isolation_export_and_deletion(self):
        credentials, owner = self.register()
        _, other = self.register()
        login = self.client.post("/v1/auth/login", json=credentials)
        self.assertEqual(login.status_code, 200)
        second_session = login.json()
        renewed = self.client.post("/v1/auth/renew", headers=self.bearer(owner))
        self.assertEqual(renewed.status_code, 200)
        self.assertEqual(self.client.get("/v1/account/export", headers=self.bearer(owner)).status_code, 401)
        owner = renewed.json()
        for body in ("owner private one", "owner private two"):
            response = self.client.post("/v1/sync/operations", headers=self.bearer(owner), json={
                "operation_id": str(uuid4()), "note_id": str(uuid4()), "base_revision": 0,
                "kind": "upsert", "note": {"body": body}})
            self.assertEqual(response.status_code, 200, response.text)
        export = self.client.get("/v1/account/export?limit=1", headers=self.bearer(owner))
        self.assertEqual(export.status_code, 200)
        self.assertNotIn("password", export.text)
        self.assertNotIn("token", export.text)
        self.assertEqual(export.headers["Cache-Control"], "no-store")
        page = export.json()
        self.assertTrue(page["has_more"])
        last = self.client.get("/v1/account/export", headers=self.bearer(owner), params={"after": page["next_after"], "revision": page["revision"]})
        self.assertEqual(len(last.json()["notes"]), 1)
        self.assertFalse(last.json()["has_more"])
        self.assertEqual(self.client.get("/v1/account/export", headers=self.bearer(other)).json()["notes"], [])
        wrong = self.client.post("/v1/account/delete", headers=self.bearer(owner), json={"password": "wrong-long-password", "confirmation": "DELETE MY ACCOUNT"})
        self.assertEqual(wrong.status_code, 401)
        self.assertEqual(self.client.get("/v1/account/export", headers=self.bearer(owner)).status_code, 200)
        deleted = self.client.post("/v1/account/delete", headers=self.bearer(owner), json={"password": credentials["password"], "confirmation": "DELETE MY ACCOUNT"})
        self.assertEqual(deleted.status_code, 204, deleted.text)
        for session in (owner, second_session):
            self.assertEqual(self.client.get("/v1/account/export", headers=self.bearer(session)).status_code, 401)
        with self.database.transaction() as connection:
            for table in ("notes", "sessions", "operations"):
                self.assertEqual(connection.execute(f"SELECT count(*) AS n FROM {table} WHERE user_id=%s", (owner["user_id"],)).fetchone()["n"], 0)
            self.assertIsNotNone(connection.execute("SELECT deleted_at FROM account_deletions WHERE user_id=%s", (owner["user_id"],)).fetchone())
        self.assertEqual(self.client.post("/v1/auth/logout", headers=self.bearer(other)).status_code, 204)
        self.assertEqual(self.client.get("/v1/account/export", headers=self.bearer(other)).status_code, 401)
