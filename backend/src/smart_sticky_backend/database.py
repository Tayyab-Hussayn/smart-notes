"""PostgreSQL transactions. Migrations run explicitly, never at API startup."""
from contextlib import contextmanager
from pathlib import Path
from .config import Settings


class Database:
    def __init__(self, url: str):
        self.url = url.replace("postgresql+psycopg://", "postgresql://", 1)

    @contextmanager
    def transaction(self):
        import psycopg
        from psycopg.rows import dict_row
        with psycopg.connect(self.url, row_factory=dict_row, connect_timeout=5,
                             options="-c statement_timeout=10000 -c lock_timeout=5000") as connection:
            yield connection

    def migrate(self):
        with self.transaction() as connection:
            connection.execute("SELECT pg_advisory_xact_lock(84729261)")
            exists = connection.execute("SELECT to_regclass('schema_version') AS table_name").fetchone()
            if exists["table_name"]:
                version = connection.execute("SELECT max(version) AS version FROM schema_version").fetchone()["version"]
                if version != 1:
                    raise RuntimeError("Unsupported schema version")
                return
            connection.execute(Path(__file__).with_name("migrations").joinpath("001_initial.sql").read_text())


if __name__ == "__main__":
    settings = Settings.from_env()
    if not settings.database_url:
        raise SystemExit("SMART_STICKY_DATABASE_URL is required")
    Database(settings.database_url).migrate()
