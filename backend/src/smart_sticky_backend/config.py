"""Explicit runtime configuration without loading secrets from source files."""

from dataclasses import dataclass, field
from os import environ
from typing import Mapping
from urllib.parse import parse_qs, urlsplit


@dataclass(frozen=True)
class Settings:
    environment: str
    allowed_hosts: tuple[str, ...]
    database_url: str | None = field(default=None, repr=False)

    @classmethod
    def from_env(cls, values: Mapping[str, str] | None = None) -> "Settings":
        values = environ if values is None else values
        environment = values.get("SMART_STICKY_ENV", "development")
        if environment not in {"development", "test", "production"}:
            raise ValueError("SMART_STICKY_ENV must be development, test, or production")
        default_hosts = "localhost,127.0.0.1,testserver" if environment != "production" else ""
        hosts = tuple(h.strip() for h in values.get("SMART_STICKY_ALLOWED_HOSTS", default_hosts).split(",") if h.strip())
        if not hosts or any(not h or any(c in h for c in "*/:@ ") for h in hosts):
            raise ValueError("SMART_STICKY_ALLOWED_HOSTS requires explicit hostnames without ports or wildcards")
        database_url = values.get("SMART_STICKY_DATABASE_URL") or None
        if environment == "production" and not database_url:
            raise ValueError("SMART_STICKY_DATABASE_URL is required in production")
        if database_url:
            try:
                parsed = urlsplit(database_url)
                valid = parsed.scheme in {"postgresql", "postgresql+psycopg"} and parsed.hostname and parsed.username and parsed.password and parsed.path not in {"", "/"}
                port = parsed.port
            except ValueError:
                raise ValueError("SMART_STICKY_DATABASE_URL must be a valid PostgreSQL connection URL") from None
            if not valid or (port is not None and port == 0) or parsed.fragment:
                raise ValueError("SMART_STICKY_DATABASE_URL must include PostgreSQL host, credentials, and database")
            if environment == "production" and parse_qs(parsed.query).get("sslmode") != ["verify-full"]:
                raise ValueError("Production PostgreSQL requires sslmode=verify-full")
        return cls(environment, hosts, database_url)
