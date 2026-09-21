# Initial REST API Design

**Status:** Phase 0 draft  
**Base path:** `/api/v1`  
**Date:** 2026-09-21

## Contract Principles

- Use JSON over HTTPS and publish an OpenAPI contract generated or verified in CI.
- Expose normalized domain resources; provider payloads remain internal evidence.
- Scope organization resources under `/organizations/{organizationId}` and authorize against the authenticated principal, never the path alone.
- Use plural nouns, stable opaque UUIDs, and ISO 8601 UTC timestamps.
- Use asynchronous jobs for synchronization, scans, SBOM processing, and other expensive work.
- Preserve backward compatibility within `v1`; additive fields are allowed and clients must ignore unknown fields.

## Authentication and Authorization

The authentication mechanism is an open Phase 0 decision. Regardless of mechanism:

- every endpoint except documented health/authentication routes requires authentication;
- organization membership and permission are checked for every organization-scoped request;
- unauthorized and inaccessible cross-organization resources do not disclose resource existence;
- browser session designs must include CSRF protection; bearer-token designs must define storage, expiry, rotation, and revocation;
- administrative changes create security events.

## Common Conventions

### Pagination

Collection endpoints use cursor pagination:

```http
GET /api/v1/organizations/{organizationId}/findings?limit=50&cursor=opaque
```

`limit` has a documented maximum. Responses provide `items` and `page.nextCursor`; cursors are opaque and bound to the filter/sort context.

### Filtering and Sorting

Each endpoint allowlists filter and sort fields. Unknown fields return `400`; arbitrary query fragments are never accepted.

### Idempotency and Concurrency

- Commands that can be retried accept `Idempotency-Key` with bounded retention.
- Duplicate keys with different request bodies return `409`.
- Mutable resources expose an `ETag` or version and require `If-Match` where lost updates matter.
- GitHub webhook idempotency uses the provider delivery identifier, not a client header.

### Error Format

Use `application/problem+json` with stable problem types:

```json
{
  "type": "https://docs.example.invalid/problems/validation-error",
  "title": "Request validation failed",
  "status": 400,
  "detail": "One or more fields are invalid.",
  "instance": "/api/v1/organizations/00000000-0000-0000-0000-000000000000/scans",
  "correlationId": "01J...",
  "errors": [
    { "field": "scanType", "code": "unsupported_value" }
  ]
}
```

Errors never include tokens, payload bodies, SQL details, stack traces, or inaccessible resource identifiers.

### Status Codes

- `200` successful read or command with immediate result
- `201` resource created
- `202` asynchronous job accepted
- `204` successful command without a response body
- `400` malformed or invalid request
- `401` authentication required or invalid
- `403` authenticated but operation not permitted
- `404` resource absent or intentionally concealed
- `409` state, uniqueness, or idempotency conflict
- `412` stale `If-Match` precondition
- `413` request exceeds documented size limit
- `422` syntactically valid content cannot be processed
- `429` rate limit exceeded, with `Retry-After`

## Resource Summary

### Session and Current User

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/me` | Current principal and organization memberships |

Authentication initiation/callback endpoints will be added after the authentication decision.

### Organizations and Memberships

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/organizations` | List organizations visible to current principal |
| `GET` | `/organizations/{organizationId}` | Get organization summary |
| `GET` | `/organizations/{organizationId}/members` | List memberships |
| `POST` | `/organizations/{organizationId}/members` | Invite or add a member |
| `PATCH` | `/organizations/{organizationId}/members/{membershipId}` | Change a role using optimistic concurrency |
| `DELETE` | `/organizations/{organizationId}/members/{membershipId}` | Remove a membership with last-owner protection |
| `GET` | `/organizations/{organizationId}/teams` | List teams |

Organization creation policy is deferred until the onboarding and authentication design is approved.

### GitHub Integrations

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/organizations/{organizationId}/integrations/github` | Get installation state and granted capabilities, never credentials |
| `POST` | `/organizations/{organizationId}/integrations/github/installations` | Complete an authorized GitHub App installation handshake |
| `DELETE` | `/organizations/{organizationId}/integrations/github/installations/{installationId}` | Disconnect an installation and revoke local access |
| `POST` | `/organizations/{organizationId}/integrations/github/syncs` | Start repository synchronization; returns a job |
| `POST` | `/webhooks/github` | Receive GitHub deliveries; signature verification occurs before parsing |

Webhook responses disclose no organization data. Accepted deliveries should be acknowledged quickly and processed asynchronously.

### Repositories and Posture

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/organizations/{organizationId}/repositories` | List and filter repository inventory |
| `GET` | `/organizations/{organizationId}/repositories/{repositoryId}` | Get repository details and current posture summary |
| `GET` | `/organizations/{organizationId}/repositories/{repositoryId}/posture-evaluations` | List historical control evaluations |
| `POST` | `/organizations/{organizationId}/repositories/{repositoryId}/posture-evaluations` | Queue an authorized reevaluation; returns a job |

### Dependencies and SBOMs

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/organizations/{organizationId}/repositories/{repositoryId}/dependencies` | List dependency observations and direct/transitive state |
| `GET` | `/organizations/{organizationId}/repositories/{repositoryId}/dependency-paths` | Get bounded paths to a selected package or vulnerability |
| `GET` | `/organizations/{organizationId}/repositories/{repositoryId}/sboms` | List SBOM documents and metadata |
| `POST` | `/organizations/{organizationId}/repositories/{repositoryId}/sboms` | Upload a bounded CycloneDX/SPDX document; returns a job |
| `GET` | `/organizations/{organizationId}/sboms/{sbomId}` | Get SBOM metadata and processing state |
| `GET` | `/organizations/{organizationId}/sboms/{sbomId}/components` | List normalized components |

Upload endpoints require explicit media types, byte limits, component limits, parser timeouts, and safe rejection behavior.

### Vulnerabilities and Findings

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/organizations/{organizationId}/vulnerabilities` | List vulnerabilities affecting observed organization assets |
| `GET` | `/organizations/{organizationId}/vulnerabilities/{vulnerabilityId}` | Get advisory, aliases, affected assets, and source provenance |
| `GET` | `/organizations/{organizationId}/findings` | List findings by status, severity, type, repository, or package |
| `GET` | `/organizations/{organizationId}/findings/{findingId}` | Get finding, evidence, relationships, and remediation |
| `PATCH` | `/organizations/{organizationId}/findings/{findingId}/status` | Transition finding status with reason and `If-Match` |

Finding status transitions are explicit commands, not unrestricted resource replacement.

### Events and Graph

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/organizations/{organizationId}/events` | List security events by time, actor, action, or resource |
| `GET` | `/organizations/{organizationId}/events/{eventId}` | Get event provenance and available state changes |
| `GET` | `/organizations/{organizationId}/graph/entities/{entityType}/{entityId}/relationships` | Get allowlisted inbound/outbound relationships with bounded depth |

The graph API does not accept a general-purpose query language in the MVP.

### Scans and Jobs

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/organizations/{organizationId}/scans` | Start an allowlisted scan type and return `202` with a job |
| `GET` | `/organizations/{organizationId}/jobs/{jobId}` | Get job state, progress, and sanitized failure information |
| `POST` | `/organizations/{organizationId}/jobs/{jobId}/cancellations` | Request cancellation where supported |

## Representative Schemas

### Finding Summary

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "type": "VULNERABLE_DEPENDENCY",
  "severity": "HIGH",
  "status": "OPEN",
  "title": "example-lib 2.1.0 is affected by CVE-20XX-XXXX",
  "affectedAsset": {
    "type": "REPOSITORY",
    "id": "00000000-0000-0000-0000-000000000000",
    "displayName": "payments-api"
  },
  "source": "OSV",
  "firstDetectedAt": "2026-09-21T10:42:00Z",
  "lastDetectedAt": "2026-09-21T10:42:00Z",
  "version": 1
}
```

### Accepted Job

```json
{
  "id": "00000000-0000-0000-0000-000000000000",
  "type": "REPOSITORY_SYNC",
  "status": "QUEUED",
  "submittedAt": "2026-09-21T10:42:00Z",
  "links": {
    "self": "/api/v1/organizations/00000000-0000-0000-0000-000000000000/jobs/00000000-0000-0000-0000-000000000000"
  }
}
```

## API Security Test Baseline

- Authenticate all protected endpoints and test every role against an approved permission matrix.
- Substitute organization and resource IDs to test cross-tenant reads and writes.
- Test malformed, duplicate, stale, oversized, and unsupported requests.
- Test webhook signatures against altered raw bodies and replayed delivery IDs.
- Test sort/filter allowlists and encoded stored data.
- Verify logs and problem responses with secret canaries.
- Verify pagination stability and enforced graph/path depth limits.

## Open Decisions

1. Authentication endpoints and session/token lifecycle.
2. Exact role permission matrix and organization creation rules.
3. OpenAPI implementation approach and canonical problem-type URI.
4. Maximum page, upload, graph-depth, and filter limits.
5. Bulk export and long-running report design.
6. Whether API clients need organization-scoped service accounts in the MVP.
7. Retention period and semantics for idempotency keys.