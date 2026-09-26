-- Intentionally no foreign key: marker survives removal of the account.
-- No email, credentials, IP address, or authored content is retained here.
CREATE TABLE account_deletions (
 user_id uuid PRIMARY KEY, deleted_at timestamptz NOT NULL DEFAULT now()
);
INSERT INTO schema_version(version) VALUES (2);
