# 0002. Phase 1 Technology Baseline

- Status: Accepted
- Date: 2026-10-06
- Owner: Manav Gupta
- Reviewers: Team review pending
- Resolves: `D-003`, architecture pending decisions 1, 2, and 4

## Context

The Phase 1 foundation commit selected tool versions before a decision record existed. This record documents those choices so later changes are deliberate.

## Decision

| Concern | Choice |
| --- | --- |
| Java | Java 21 (LTS), Maven Wrapper |
| Backend framework | Spring Boot 4.1 with Spring Modulith for module boundary verification |
| Persistence | PostgreSQL 17, Spring Data JPA, `JdbcClient` where SQL-level control is needed (for example `ON CONFLICT`) |
| Schema migrations | Flyway, versioned SQL under `backend/src/main/resources/db/migration` |
| Cache and coordination | Redis 8 |
| Testing | JUnit 5, Spring test slices, Testcontainers with images pinned to the Compose versions |
| Frontend | React 19, TypeScript, Vite; no component library selected yet |
| Python | Reserved for later security-analysis tooling; not part of the Phase 1 runtime |

## Persistence Conventions

- Tables use singular `snake_case` names matching the [data model](../phase-0/DATA_MODEL.md).
- Primary keys are application-generated UUIDs.
- Enumerated values are stored as `text` with `CHECK` constraints, not database enum types, so values can be added in a migration without type rewrites.
- Organization-owned child tables use composite foreign keys that include `organization_id` where cross-organization references must be impossible.
- Mutable aggregates carry a `version` column for optimistic locking.
- Migration descriptions name the owning module. Applied migrations are never edited.
- Hibernate schema generation is disabled; Flyway is the only schema authority.

## Consequences

- Contributors need JDK 21 and Docker to run the full backend test suite.
- Spring Modulith's module verification test fails the build if a module reaches into another module's internals.
- Row-level security and schema-per-module remain open (data model open decisions 1 and 2).
