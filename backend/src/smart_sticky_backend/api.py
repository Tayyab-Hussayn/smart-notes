from datetime import datetime, timedelta, timezone
from typing import Literal
from uuid import UUID, uuid4

from fastapi import APIRouter, Depends, Header, HTTPException, Query, Response
from pydantic import BaseModel, ConfigDict, Field, field_validator, model_validator

from .security import hash_password, verify_password, new_token, token_digest
from .sync import apply_operation, SyncConflict


class Credentials(BaseModel):
    model_config = ConfigDict(extra="forbid")
    email: str = Field(min_length=3, max_length=254)
    password: str = Field(min_length=12, max_length=128)

    @field_validator("email")
    @classmethod
    def normalize_email(cls, value):
        value = value.strip().lower()
        if value.count("@") != 1 or any(c.isspace() for c in value) or not all(value.split("@")):
            raise ValueError("Invalid email")
        return value


class Note(BaseModel):
    model_config = ConfigDict(extra="forbid")
    title: str = Field(default="", max_length=200)
    body: str = Field(max_length=20000)
    priority: Literal["low", "normal", "high"] = "normal"
    pinned: bool = False
    archived: bool = False


class DeleteAccount(BaseModel):
    model_config = ConfigDict(extra="forbid")
    password: str = Field(min_length=12, max_length=128)
    confirmation: Literal["DELETE MY ACCOUNT"]


class Operation(BaseModel):
    model_config = ConfigDict(extra="forbid")
    operation_id: UUID
    note_id: UUID
    base_revision: int = Field(ge=0, le=9223372036854775806)
    kind: Literal["upsert", "delete"]
    note: Note | None = None

    @model_validator(mode="after")
    def validate_payload(self):
        if (self.kind == "upsert") != (self.note is not None):
            raise ValueError("Upsert requires note; delete forbids note")
        return self


def router(database):
    api = APIRouter(prefix="/v1")
    dummy_hash = hash_password("dummy-password-for-timing")

    def db():
        if database is None:
            raise HTTPException(503, "database_not_configured")
        return database

    def identity(authorization: str | None = Header(default=None)):
        if not authorization or not authorization.startswith("Bearer ") or len(authorization) > 128:
            raise HTTPException(401, "authentication_required")
        digest = token_digest(authorization[7:])
        with db().transaction() as connection:
            session = connection.execute("SELECT user_id FROM sessions WHERE token_hash=%s AND expires_at>now()", (digest,)).fetchone()
        if not session:
            raise HTTPException(401, "invalid_session")
        return session["user_id"], digest

    def issue(connection, user_id):
        token = new_token()
        connection.execute("INSERT INTO sessions(token_hash,user_id,expires_at) VALUES(%s,%s,%s)",
                           (token_digest(token), user_id, datetime.now(timezone.utc) + timedelta(minutes=30)))
        return {"access_token": token, "token_type": "bearer", "expires_in": 1800, "user_id": str(user_id)}

    def locked_account(connection, account):
        # Serialize deletion with reads/writes and recheck credentials after waiting.
        user = connection.execute("SELECT id,password_hash,revision FROM users WHERE id=%s FOR UPDATE", (account[0],)).fetchone()
        session = connection.execute("SELECT 1 FROM sessions WHERE token_hash=%s AND user_id=%s AND expires_at>now()", (account[1], account[0])).fetchone()
        if not user or not session:
            raise HTTPException(401, "invalid_session")
        return user

    @api.post("/auth/register", status_code=201)
    def register(body: Credentials, response: Response):
        response.headers["Cache-Control"] = "no-store"
        encoded = hash_password(body.password)
        with db().transaction() as connection:
            user_id = uuid4()
            row = connection.execute("INSERT INTO users(id,email,password_hash) VALUES(%s,%s,%s) ON CONFLICT(email) DO NOTHING RETURNING id",
                                     (user_id, body.email, encoded)).fetchone()
            if not row:
                raise HTTPException(409, "registration_unavailable")
            return issue(connection, user_id)

    @api.post("/auth/login")
    def login(body: Credentials, response: Response):
        response.headers["Cache-Control"] = "no-store"
        with db().transaction() as connection:
            user = connection.execute("SELECT id,password_hash FROM users WHERE email=%s FOR UPDATE", (body.email,)).fetchone()
            valid = verify_password(body.password, user["password_hash"] if user else dummy_hash)
            if not valid or not user:
                raise HTTPException(401, "invalid_credentials")
            return issue(connection, user["id"])

    @api.post("/auth/logout", status_code=204)
    def logout(account=Depends(identity)):
        with db().transaction() as connection:
            locked_account(connection, account)
            connection.execute("DELETE FROM sessions WHERE token_hash=%s AND user_id=%s", (account[1], account[0]))

    @api.post("/auth/renew")
    def renew(response: Response, account=Depends(identity)):
        response.headers["Cache-Control"] = "no-store"
        with db().transaction() as connection:
            locked_account(connection, account)
            row = connection.execute("DELETE FROM sessions WHERE token_hash=%s AND user_id=%s AND expires_at>now() RETURNING user_id", (account[1], account[0])).fetchone()
            if not row:
                raise HTTPException(401, "invalid_session")
            return issue(connection, account[0])

    @api.post("/sync/operations")
    def push(body: Operation, account=Depends(identity)):
        try:
            with db().transaction() as connection:
                locked_account(connection, account)
                return apply_operation(connection, account[0], body.model_dump(mode="json"))
        except SyncConflict as conflict:
            raise HTTPException(409, {"code": conflict.code, "current": conflict.current}) from None

    @api.get("/sync/changes")
    def pull(after: int = Query(default=0, ge=0, le=9223372036854775807), limit: int = Query(default=100, ge=1, le=500), account=Depends(identity)):
        with db().transaction() as connection:
            locked_account(connection, account)
            rows = connection.execute("SELECT id,revision,deleted,payload FROM notes WHERE user_id=%s AND revision>%s ORDER BY revision LIMIT %s", (account[0], after, limit + 1)).fetchall()
        more = len(rows) > limit
        rows = rows[:limit]
        return {"changes": rows, "cursor": rows[-1]["revision"] if rows else after, "has_more": more}

    @api.get("/account/export")
    def export(after: UUID | None = None, revision: int | None = Query(default=None, ge=0),
               limit: int = Query(default=100, ge=1, le=500), account=Depends(identity)):
        with db().transaction() as connection:
            user = locked_account(connection, account)
            # The client restarts if data changes during paginated export.
            if (after is not None and revision is None) or (revision is not None and revision != user["revision"]):
                raise HTTPException(409, "export_changed_restart")
            rows = connection.execute("SELECT id,payload FROM notes WHERE user_id=%s AND NOT deleted AND (%s::uuid IS NULL OR id>%s::uuid) ORDER BY id LIMIT %s",
                                      (account[0], after, after, limit + 1)).fetchall()
        more = len(rows) > limit
        rows = rows[:limit]
        return {"format": "smart-sticky-notes-v1", "revision": user["revision"], "notes": rows,
                "next_after": str(rows[-1]["id"]) if more else None, "has_more": more}

    @api.post("/account/delete", status_code=204)
    def delete_account(body: DeleteAccount, account=Depends(identity)):
        with db().transaction() as connection:
            user = locked_account(connection, account)
            if not verify_password(body.password, user["password_hash"]):
                raise HTTPException(401, "invalid_credentials")
            connection.execute("INSERT INTO account_deletions(user_id) VALUES(%s)", (account[0],))
            # FK cascades remove sessions, notes, and replay receipts atomically.
            connection.execute("DELETE FROM users WHERE id=%s", (account[0],))

    return api
