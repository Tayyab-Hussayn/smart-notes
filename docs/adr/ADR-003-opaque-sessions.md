# ADR-003: Revocable opaque session foundation

Status: Implemented foundation under owner delegation; security review pending.

Use random 256-bit opaque bearer tokens with only SHA-256 token digests persisted.
Tokens expire after 30 minutes, are checked in PostgreSQL on every protected
request, and are revoked on logout. Renewal atomically consumes the old token
and returns a fresh token with a new expiry. Expired sessions require login.
This deliberately has no long-lived refresh credential yet.

Passwords use standard-library scrypt (N=32768,r=8,p=1), random 128-bit salts,
and constant-time comparison. Passwords accept 12–128 characters. Login performs
a dummy scrypt verification for unknown identities. Registration currently
discloses existing email through a generic 409; verified-email enrollment and
recovery are not implemented and are release gaps, not claimed features.

Transport uses bearer headers rather than cookies. Clients must use OS-secure
storage and HTTPS. Credentials and authored content are excluded from errors
and access logging. The API limits decoded request bytes and supplies an IP
rate-limit backstop; a trusted TLS edge must enforce distributed limits before
multi-worker deployment. Do not trust forwarded addresses without an explicitly
configured proxy boundary. Periodic expired-session cleanup remains operational
work. Account deletion, credential recovery and audit pipeline remain pending.
