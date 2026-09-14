import importlib.util
import unittest

HTTP_DEPENDENCIES = all(importlib.util.find_spec(name) is not None for name in ("fastapi", "httpx"))


@unittest.skipUnless(HTTP_DEPENDENCIES, "Install backend .[test] dependencies to run HTTP tests")
class HealthTests(unittest.TestCase):
    def setUp(self):
        from fastapi.testclient import TestClient
        from smart_sticky_backend.config import Settings
        from smart_sticky_backend.main import create_app

        self.client = TestClient(create_app(Settings.from_env({"SMART_STICKY_ENV": "test"})))
        self.addCleanup(self.client.close)

    def test_liveness_and_host_rejection(self):
        response = self.client.get("/health/live")
        self.assertEqual(response.status_code, 200)
        self.assertEqual(response.json(), {"status": "alive"})
        self.assertEqual(self.client.get("/health/live", headers={"host": "untrusted.example"}).status_code, 400)

    def test_unimplemented_product_and_readiness_routes_are_absent(self):
        for path in ("/notes", "/auth/login", "/health/ready", "/docs", "/openapi.json"):
            with self.subTest(path=path):
                self.assertEqual(self.client.get(path).status_code, 404)

    def test_auth_required_and_validation_redacts_password(self):
        self.assertEqual(self.client.get("/v1/sync/changes").status_code, 401)
        response = self.client.post("/v1/auth/login", json={"email": "invalid", "password": "secret"})
        self.assertEqual(response.status_code, 422)
        self.assertNotIn("secret", response.text)
        self.assertEqual(response.json(), {"code": "invalid_request"})

    def test_large_payload_rejected_before_json_parsing(self):
        response = self.client.post("/v1/auth/login", content=b"x" * 131073)
        self.assertEqual(response.status_code, 413)

    def test_auth_rate_limit_backstop(self):
        for _ in range(10):
            self.client.post("/v1/auth/login", json={})
        response = self.client.post("/v1/auth/login", json={})
        self.assertEqual(response.status_code, 429)
        self.assertEqual(response.headers["Retry-After"], "60")
