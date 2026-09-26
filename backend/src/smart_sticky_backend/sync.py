"""Durable idempotency and optimistic revisions, serialized per account."""
import hashlib
import json


def fingerprint(operation: dict) -> str:
    return hashlib.sha256(json.dumps(operation, sort_keys=True, separators=(",", ":")).encode()).hexdigest()


class SyncConflict(Exception):
    def __init__(self, code: str, current: dict | None = None):
        self.code, self.current = code, current


def apply_operation(connection, user_id, operation: dict) -> dict:
    from psycopg.types.json import Jsonb
    # A per-user row lock orders revisions by committed transaction, unlike sequences.
    user = connection.execute("SELECT revision FROM users WHERE id=%s FOR UPDATE", (user_id,)).fetchone()
    prior = connection.execute("SELECT request_hash,response FROM operations WHERE user_id=%s AND id=%s",
                               (user_id, operation["operation_id"])).fetchone()
    digest = fingerprint(operation)
    if prior:
        if prior["request_hash"] != digest:
            raise SyncConflict("operation_id_reused")
        return prior["response"]
    current = connection.execute("SELECT revision,deleted,payload FROM notes WHERE user_id=%s AND id=%s",
                                 (user_id, operation["note_id"])).fetchone()
    if operation["base_revision"] != (current["revision"] if current else 0):
        raise SyncConflict("revision_conflict", current)
    if current and current["deleted"]:
        raise SyncConflict("note_deleted", current)
    revision = user["revision"] + 1
    deleted = operation["kind"] == "delete"
    if deleted and not current:
        raise SyncConflict("note_not_found")
    payload = {} if deleted else operation["note"]
    connection.execute("UPDATE users SET revision=%s WHERE id=%s", (revision, user_id))
    connection.execute("""INSERT INTO notes(user_id,id,revision,deleted,payload) VALUES(%s,%s,%s,%s,%s)
      ON CONFLICT(user_id,id) DO UPDATE SET revision=EXCLUDED.revision,deleted=EXCLUDED.deleted,payload=EXCLUDED.payload""",
      (user_id, operation["note_id"], revision, deleted, Jsonb(payload)))
    result = {"operation_id": operation["operation_id"], "note_id": operation["note_id"], "revision": revision, "deleted": deleted}
    connection.execute("INSERT INTO operations(user_id,id,request_hash,response) VALUES(%s,%s,%s,%s)",
                       (user_id, operation["operation_id"], digest, Jsonb(result)))
    return result
