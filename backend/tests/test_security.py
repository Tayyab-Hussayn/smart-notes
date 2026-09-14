import unittest
from smart_sticky_backend.security import hash_password, verify_password, new_token, token_digest
from smart_sticky_backend.sync import fingerprint


class SecurityTests(unittest.TestCase):
    def test_password_verification_and_unique_salts(self):
        first = hash_password("a-long-example-password")
        self.assertNotEqual(first, hash_password("a-long-example-password"))
        self.assertTrue(verify_password("a-long-example-password", first))
        self.assertFalse(verify_password("wrong-password", first))
        self.assertFalse(verify_password("anything", "malformed"))

    def test_tokens_are_unique_and_only_digest_is_stored(self):
        tokens = {new_token() for _ in range(100)}
        self.assertEqual(len(tokens), 100)
        token = next(iter(tokens))
        self.assertEqual(len(token_digest(token)), 64)
        self.assertNotEqual(token, token_digest(token))

    def test_idempotency_hash_canonical_and_content_sensitive(self):
        self.assertEqual(fingerprint({"id": 1, "body": "private"}), fingerprint({"body": "private", "id": 1}))
        self.assertNotEqual(fingerprint({"body": "first"}), fingerprint({"body": "second"}))
