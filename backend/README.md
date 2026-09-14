# Backend foundation

Implemented: validated environment configuration and `GET /health/live`.
Liveness reports process responsiveness only; no database readiness is implied.
Notes, authentication, sync, billing, migrations, and database adapters remain unimplemented.
There are deliberately no placeholder product endpoints.

## Local run

Use Python 3.12+ in a virtual environment, then:

```sh
pip install -e '.[test]'
uvicorn smart_sticky_backend.main:create_app --factory --no-access-log
python -m unittest discover -s tests -v
```

Configuration-only tests can run without installing dependencies:

```sh
PYTHONPATH=src python -m unittest discover -s tests -v
```

HTTP tests are skipped when FastAPI or HTTPX is absent; skips are not validation.

## Local containers

Set `SMART_STICKY_DEV_DB_PASSWORD` to a local development password in your shell,
then run `docker compose up --build` from this directory.
PostgreSQL persists in a named volume and has no host port. The API currently
does not connect to it. Changing the password after first initialization does
not rotate an existing volume's PostgreSQL credentials.

## Configuration

| Variable | Behavior |
| --- | --- |
| `SMART_STICKY_ENV` | `development` (default), `test`, or `production` |
| `SMART_STICKY_ALLOWED_HOSTS` | Comma-separated explicit hostnames; required in production |
| `SMART_STICKY_DATABASE_URL` | Optional PostgreSQL URL; production requires credentials and `sslmode=verify-full` |

Production operation is not ready: deployment must add TLS termination, reviewed
dependency locking/security scanning, database adapters/migrations, readiness,
observability, and the remaining security controls. Do not expose the local
Compose stack publicly. Top-level dependency pins are a baseline, not a complete
transitive lock or a security review. Configuration rejects unsafe defaults but
does not establish database connectivity or validate certificate installation.
