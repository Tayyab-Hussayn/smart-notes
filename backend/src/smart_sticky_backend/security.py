"""Opaque credentials; no passwords or bearer tokens are persisted in plaintext."""
import hashlib
import hmac
import secrets


def hash_password(password: str, salt: bytes | None = None) -> str:
    salt = salt or secrets.token_bytes(16)
    digest = hashlib.scrypt(password.encode(), salt=salt, n=32768, r=8, p=1, maxmem=67108864)
    return f"scrypt-v1${salt.hex()}${digest.hex()}"


def verify_password(password: str, encoded: str) -> bool:
    try:
        version, salt, _ = encoded.split("$")
        return version == "scrypt-v1" and hmac.compare_digest(hash_password(password, bytes.fromhex(salt)), encoded)
    except (ValueError, TypeError):
        return False


def token_digest(token: str) -> str:
    return hashlib.sha256(token.encode()).hexdigest()


def new_token() -> str:
    return secrets.token_urlsafe(32)
