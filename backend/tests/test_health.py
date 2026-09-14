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
