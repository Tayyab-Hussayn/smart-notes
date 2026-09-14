CREATE TABLE IF NOT EXISTS schema_version (version integer PRIMARY KEY);
CREATE TABLE users (
 id uuid PRIMARY KEY, email text NOT NULL UNIQUE, password_hash text NOT NULL,
 revision bigint NOT NULL DEFAULT 0, created_at timestamptz NOT NULL DEFAULT now()
);
CREATE TABLE sessions (
 token_hash text PRIMARY KEY, user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
 expires_at timestamptz NOT NULL, created_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX sessions_user ON sessions(user_id);
CREATE TABLE notes (
 user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE, id uuid NOT NULL,
 revision bigint NOT NULL CHECK (revision > 0), deleted boolean NOT NULL DEFAULT false,
 payload jsonb NOT NULL, PRIMARY KEY(user_id,id)
);
CREATE INDEX notes_changes ON notes(user_id,revision);
CREATE TABLE operations (
 user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE, id uuid NOT NULL,
 request_hash text NOT NULL, response jsonb NOT NULL, PRIMARY KEY(user_id,id)
);
INSERT INTO schema_version(version) VALUES (1);
