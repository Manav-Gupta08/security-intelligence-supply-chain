# Architecture

**Status:** Phase 0 draft  
**Date:** 2026-09-21

## Architectural Goals

- Correlate software and security data while preserving source evidence.
- Enforce organization isolation and least privilege throughout the system.
- Keep security evaluations deterministic and reproducible.
- Support a four-person team and external contributors without distributed-system overhead.
- Allow module extraction later only when measured operational needs justify it.

## System Context

```text
Users
  |
  v
React web application
  |
  v
Spring Boot REST API and background jobs
  |              |                 |
  v              v                 v
PostgreSQL     Redis           GitHub / OSV
```

GitHub and vulnerability sources are untrusted external systems. The browser is also outside the server trust boundary. The backend authenticates requests, authorizes organization access, validates external data, and owns all security decisions.

## Architecture Style

The backend is a modular monolith. It is built and deployed as one Spring Boot application while modules expose narrow application interfaces and own their domain data. Modules must not reach into another module's repositories or persistence implementation.

The frontend is a separate React and TypeScript application that communicates only through the versioned REST API.

## Backend Modules

| Module | Responsibility |
| --- | --- |
| `identity` | Authentication principals and platform-level identity concerns |
| `organization` | Organizations, memberships, teams, roles, and tenant authorization |
| `integration` | GitHub App installations, encrypted credential references, sync state, and provider clients |
| `repository` | Repository inventory, ownership, branches, and security-setting snapshots |
| `posture` | Versioned control definitions and deterministic evaluations |
| `dependency` | Packages, versions, manifests, dependency observations, and paths |
| `sbom` | CycloneDX/SPDX ingestion, validation, and component mapping |
| `vulnerability` | Advisory ingestion and version-range matching |
| `finding` | Normalized findings, lifecycle, evidence, and remediation guidance |
| `event` | Immutable provider and platform security events |
| `graph` | Typed relationships and contextual traversal queries |
| `job` | Durable job definitions, attempts, scheduling, and status |

Notifications and active remediation are deferred until their roadmap phases.

## Dependency Rules

1. HTTP controllers call their owning module's application services.
2. Modules collaborate through public application interfaces or domain events, never another module's database repository.
3. Provider-specific models remain inside `integration`; normalized models cross into domain modules.
4. `finding` references affected assets through stable typed identifiers rather than provider payloads.
5. `graph` projects relationships owned by source modules; it does not become the source of truth for those entities.
6. Cross-module operations define transaction boundaries explicitly and are idempotent where retries are possible.

## Primary Data Flows

### GitHub Synchronization

```text
Scheduled/manual request
  -> integration authorizes installation
  -> GitHub client fetches bounded pages
  -> provider payload is validated and normalized
  -> repository stores inventory snapshots
  -> posture evaluates controls
  -> finding reconciles results
  -> graph projects relationships
```

### Webhook Ingestion

```text
Raw request
  -> enforce body-size limit
  -> verify signature against raw bytes
  -> identify installation and organization
  -> reserve delivery ID for idempotency
  -> persist minimal delivery evidence
  -> acknowledge accepted delivery
  -> process normalized event asynchronously
```

Invalid signatures are rejected before JSON deserialization or side effects. Retry processing must not duplicate events or findings.

### Dependency and Vulnerability Analysis

```text
Repository metadata or uploaded SBOM
  -> static parser with resource limits
  -> normalized package coordinates
  -> dependency relationships
  -> advisory lookup and version matching
  -> evidence-backed finding reconciliation
```

Repository code, installers, build scripts, and package lifecycle hooks are never executed in the MVP.

## Persistence

PostgreSQL is the system of record. Each module owns its tables and migrations even though all modules initially share one database. Organization-owned rows include an organization identifier; authorization tests verify isolation.

Relationships are stored relationally using typed entity identifiers and indexed edge tables. A dedicated graph database is deferred until representative queries and measured performance show PostgreSQL is insufficient.

Redis may provide short-lived caching, distributed locks, rate-limit counters, and job coordination. Redis is not the authoritative store for security findings, audit evidence, integration configuration, or job outcomes.

## Processing Model

- User-facing reads and small commands use synchronous REST requests.
- Provider synchronization, SBOM processing, and analysis run as durable background jobs.
- PostgreSQL records job state and outcomes; Redis may coordinate workers.
- Jobs carry an organization ID, idempotency key, attempt count, and sanitized failure details.
- Retries use bounded exponential backoff and classify permanent failures.

## Security Boundaries

- Authenticate every protected API request and authorize every organization-scoped operation.
- Store integration secrets through encrypted secret handling; persist references or ciphertext, never plaintext logs.
- Validate webhook signatures from raw request bytes using constant-time comparison.
- Apply schema, size, count, recursion-depth, and timeout limits to untrusted documents.
- Use parameterized persistence APIs and encode output at the presentation boundary.
- Record administrative and security-relevant actions without storing secrets.
- Keep outbound provider clients allowlisted and protected against unsafe redirects and server-side request forgery.

## Deployment Baseline

The Phase 1 topology will contain:

- one frontend container
- one Spring Boot application container, scalable to additional identical instances
- PostgreSQL
- Redis
- persistent PostgreSQL storage
- externally supplied secrets and environment-specific configuration

Docker Compose is the supported initial deployment. Kubernetes and separate worker services are not MVP requirements.

## Architecture Decisions Pending Approval

1. Java LTS and Spring Boot versions.
2. Frontend build tool and component system.
3. Authentication and session/token mechanism.
4. Database migration tool.
5. Durable job implementation.
6. Secret encryption and key-management approach for self-hosting.
7. API documentation generator and error format.
8. Deployment scale and availability targets.

Each accepted choice should be recorded as a short architecture decision record under `docs/decisions/` before implementation depends on it.