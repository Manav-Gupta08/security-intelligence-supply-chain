# 0004. GitHub Webhook Intake

- Status: Accepted
- Date: 2026-10-06
- Owner: Manav Gupta
- Reviewers: Team review pending
- Requirements: `FR-GH-004`, `FR-GH-005`, `NFR-REL-001`
- Threat model references: `TM-001`, `TM-010`, `TM-011`, `TM-013`

## Context

GitHub delivers App events to a public endpoint. The endpoint is reachable by anyone, so authenticity must be established before any parsing or state change, and GitHub may redeliver the same event.

## Decision

`POST /api/v1/webhooks/github` processes a request in this order:

1. Fail closed with `503` if no webhook secret is configured.
2. Reject a declared or actual body larger than the configured limit (default 25 MB, GitHub's maximum) with `413`. The body is read as bounded raw bytes, never through a buffered `@RequestBody`.
3. Verify `X-Hub-Signature-256` as HMAC-SHA256 over the raw bytes using constant-time comparison. Reject with `401` before any parsing.
4. Validate `X-GitHub-Event` and `X-GitHub-Delivery` header formats.
5. Parse only `action` and `installation.id` from the payload.
6. Resolve the organization from an `ACTIVE` `integration_installation` row. Unknown installations are recorded with status `UNBOUND` and no organization.
7. Insert a `webhook_delivery` row with `ON CONFLICT DO NOTHING` on `(provider, provider_delivery_id)`. New deliveries return `202`; redeliveries return `200` with no side effects.

Persisted evidence is minimal: delivery ID, event type, action, installation ID, SHA-256 payload digest, and timestamps. Raw payloads are not stored, per the data model. Response bodies contain no organization data.

## Deferred

- Normalizing deliveries into security events. Because raw payloads are not retained, a later decision must choose between normalizing synchronously before acknowledging, or approving bounded transient payload retention.
- Webhook secret rotation (accepting two secrets during rollover).
- GitHub App permissions and the subscribed event list (`D-006`).
- The installation handshake that creates `integration_installation` rows.

## Consequences

- Forged, altered, and oversized requests cause no database writes.
- Redelivery is idempotent at the database level, including concurrent duplicates.
- Deliveries arriving before an installation is bound are kept as evidence but not attributed to any organization.
