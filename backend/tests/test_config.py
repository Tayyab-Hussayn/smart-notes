import unittest

from smart_sticky_backend.config import Settings


class ConfigurationTests(unittest.TestCase):
    def test_development_needs_no_secrets(self):
        settings = Settings.from_env({})
        self.assertEqual(settings.environment, "development")
        self.assertIsNone(settings.database_url)

    def test_production_fails_closed(self):
        for values in (
            {"SMART_STICKY_ENV": "production"},
            {"SMART_STICKY_ENV": "production", "SMART_STICKY_ALLOWED_HOSTS": "api.example.com"},
            {"SMART_STICKY_ENV": "staging"},
            {"SMART_STICKY_ALLOWED_HOSTS": "*"},
        ):
            with self.subTest(values=values), self.assertRaises(ValueError):
                Settings.from_env(values)

    def test_production_requires_verified_tls(self):
        values = {
            "SMART_STICKY_ENV": "production",
            "SMART_STICKY_ALLOWED_HOSTS": "api.example.com",
            "SMART_STICKY_DATABASE_URL": "postgresql://app:secret@database/notes",
        }
        with self.assertRaises(ValueError):
            Settings.from_env(values)
        values["SMART_STICKY_DATABASE_URL"] += "?sslmode=verify-full"
        self.assertEqual(Settings.from_env(values).allowed_hosts, ("api.example.com",))
        self.assertNotIn("secret", repr(Settings.from_env(values)))

    def test_invalid_database_does_not_leak_credentials(self):
        for url in ("mysql://app:secret@db/notes", "postgresql://app:secret@db:bad/notes", "postgresql://app:secret@db/"):
            with self.subTest(url=url):
                with self.assertRaises(ValueError) as caught:
                    Settings.from_env({"SMART_STICKY_DATABASE_URL": url})
                self.assertNotIn("secret", str(caught.exception))
