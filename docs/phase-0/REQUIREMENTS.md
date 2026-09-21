# MVP Requirements

**Status:** Draft for team review  
**Version:** 0.1  
**Date:** 2026-09-21

## Product Outcome

An organization can connect a GitHub organization and obtain evidence-backed, explainable security findings that connect repositories to posture controls, dependencies, vulnerabilities, and relevant security events.

## Actors

| Actor | Primary needs |
| --- | --- |
| Organization owner | Install integrations, manage membership, and configure organization policy |
| Security analyst | Investigate findings, evidence, dependency paths, and events |
| Developer | Understand affected repositories and remediation guidance |
| Viewer | Read organization security information without changing it |
| Platform operator | Deploy, configure, monitor, back up, and upgrade the service |

## Functional Requirements

### GitHub Integration

- `FR-GH-001`: The system shall connect a GitHub organization through a GitHub App.
- `FR-GH-002`: The system shall request only permissions required by enabled capabilities.
- `FR-GH-003`: The system shall discover accessible repositories and synchronize relevant metadata.
- `FR-GH-004`: The system shall verify webhook signatures before parsing or processing payloads.
- `FR-GH-005`: The system shall process webhook deliveries idempotently and retain delivery provenance.

### Repository Inventory and Posture

- `FR-RP-001`: The system shall inventory repository identity, ownership, visibility, default branch, and relevant security settings.
- `FR-RP-002`: The system shall evaluate the initial controls listed in the project plan.
- `FR-RP-003`: Each posture result shall include status, evidence, evaluation time, and the deterministic rule version.
- `FR-RP-004`: Failed posture controls shall produce normalized findings where policy requires them.

### Dependencies, SBOMs, and Vulnerabilities

- `FR-SC-001`: The system shall statically extract dependency metadata for the initially supported Python, Java, and JavaScript/TypeScript ecosystems.
- `FR-SC-002`: The system shall distinguish direct and transitive dependencies and preserve known dependency paths.
- `FR-SC-003`: The system shall ingest standards-compliant CycloneDX and SPDX SBOMs; generation support will be finalized by research.
- `FR-SC-004`: The system shall correlate package ecosystem and version data with external vulnerability intelligence.
- `FR-SC-005`: A match shall report `AFFECTED`, `NOT_AFFECTED`, `FIXED`, or `UNKNOWN` with source evidence.
- `FR-SC-006`: Scanning shall not execute repository code, installers, lifecycle scripts, or builds.

### Findings and Events

- `FR-FE-001`: The system shall normalize posture, vulnerability, and supported imported results into a common finding model.
- `FR-FE-002`: Findings shall include type, severity, status, affected asset, source, timestamps, evidence, and remediation guidance.
- `FR-FE-003`: Finding status shall support `OPEN`, `ACKNOWLEDGED`, `IN_PROGRESS`, `RESOLVED`, `FALSE_POSITIVE`, and `ACCEPTED_RISK`.
- `FR-FE-004`: The system shall retain immutable security-event facts with actor, time, source, action, resource, and available state changes.
- `FR-FE-005`: The system shall expose relationships among findings, events, repositories, packages, and vulnerabilities.

### API and Dashboard

- `FR-UI-001`: The backend shall expose a versioned REST API under `/api/v1`.
- `FR-UI-002`: The dashboard shall provide organization overview, repository inventory, findings, vulnerability details, posture, and event timeline views.
- `FR-UI-003`: List endpoints shall support pagination, stable sorting, and organization-scoped filtering.
- `FR-UI-004`: API errors shall use a consistent machine-readable format without exposing secrets or internal stack traces.

### Identity and Isolation

- `FR-ID-001`: Every protected operation shall require an authenticated principal.
- `FR-ID-002`: Authorization shall enforce organization isolation at service and persistence boundaries.
- `FR-ID-003`: The initial role model shall include `OWNER`, `ADMIN`, `SECURITY_ANALYST`, `DEVELOPER`, and `VIEWER`; exact permissions remain a Phase 0 decision.
- `FR-ID-004`: Security-relevant administrative actions shall produce audit events.

## Non-Functional Requirements

- `NFR-SEC-001`: Secrets shall be supplied through external configuration and never committed or written to logs.
- `NFR-SEC-002`: All external payloads and repository content shall be treated as untrusted input with explicit size and format limits.
- `NFR-SEC-003`: Security decisions shall be deterministic, reproducible, and linked to evidence and rule/source versions.
- `NFR-OPS-001`: The supported MVP deployment shall run through Docker Compose with PostgreSQL and Redis.
- `NFR-OPS-002`: The service shall provide structured logs, health endpoints, and operational metrics without sensitive data.
- `NFR-DATA-001`: PostgreSQL schema changes shall use versioned migrations.
- `NFR-DATA-002`: Organization-owned records shall carry an organization boundary that can be enforced and tested.
- `NFR-REL-001`: Webhook and synchronization processing shall be retryable and idempotent.
- `NFR-TEST-001`: Authorization, organization isolation, webhook forgery, policy evaluation, version matching, and secret leakage shall have automated tests.
- `NFR-OSS-001`: A new contributor shall be able to configure, run, test, and submit a change using repository documentation.

## MVP Exclusions

- Non-GitHub source-control integrations
- Kubernetes as a required runtime
- A dedicated graph database
- Automatic changes to production systems or credentials
- Arbitrary repository code execution
- Proprietary vulnerability-database creation
- AI-dependent security decisions
- Full SIEM, EDR, cloud-security, IAM, or compliance-platform capabilities

## MVP Acceptance Scenario

The MVP is usable when a fresh deployment can connect a test GitHub organization, discover repositories, evaluate supported controls, ingest or derive dependencies and an SBOM, correlate at least one known test vulnerability, create an evidence-backed finding, and display its repository, dependency path, events, and remediation through the API and dashboard.

## Open Decisions

1. Authentication mechanism for the self-hosted MVP.
2. Exact role-to-permission matrix.
3. GitHub App permissions and supported webhook events.
4. Supported CycloneDX and SPDX versions and generation strategy.
5. Vulnerability source precedence and update cadence.
6. MVP scale targets for organizations, repositories, components, and events.
7. Retention and deletion policy for webhook payloads, evidence, and audit events.
8. License selection.

## Approval

Requirements become implementation commitments only after maintainers resolve or explicitly defer the open decisions and record approval in this document.