# Development Change Log

This file is the team's shared record of repository changes. Every developer must add an entry for each commit they create.

## Rules

1. Add your entry as part of the same commit as the change it describes.
2. Use one entry per commit. Do not combine unrelated work into one commit.
3. Write the exact commit message you intend to use before committing, then keep it synchronized if the message changes.
4. Keep commits small, focused, and independently reviewable.
5. Describe what changed and why in two to four concise bullets.
6. List important tests, checks, or manual verification performed.
7. Add new entries at the top of the **Entries** section.
8. Use the date format `YYYY-MM-DD` and do not include secrets, tokens, or sensitive data.

## Entry Template

```markdown
### YYYY-MM-DD - Developer Name

**Commit message:** `type(scope): short description`

**Changes:**

- Briefly describe what changed.
- Briefly explain why it changed.

**Verification:**

- Command or manual check performed.
```

Recommended commit prefixes are `docs`, `feat`, `fix`, `test`, `refactor`, `build`, and `ci`.

## Entries

### 2026-10-07 - Shivansh Garg

**Commit message:** `feat(frontend): build complete security workspace`

**Changes:**

- Connected the Phase 1 organization and team dashboard to the versioned backend API.
- Added the complete frontend navigation shell for onboarding, repositories, integrations, jobs, findings, vulnerabilities, dependencies, SBOMs, posture, events, relationships, administration, and session states.
- Added Tailwind CSS and Motion integrations, explicit unavailable states for later-phase APIs, and configurable Vite API proxying.

**Verification:**

- `npm run lint` passed in `frontend`.
- `npm run build` passed in `frontend`.
- Browser-verified onboarding and repository detail routes.

### 2026-10-07 - Manav Gupta

**Commit message:** `build(docker): add one-command local full-stack startup`

**Changes:**

- Added backend and frontend Docker builds and Compose services with healthy database and Redis dependencies.
- Added project-root Compose selection and configurable app ports to the environment template; preserved local environment credentials.
- Made the frontend API proxy configurable for Docker networking and documented full-stack and host-side startup workflows.
- Ensured the non-root frontend container user can write Vite cache and temporary files.

**Verification:**

- `docker compose config --quiet` passed; resolved service names and backend/frontend build contexts were checked.
- `npm ci`, `npm run lint`, and `npm run build` passed in `frontend`.
- Docker container builds passed and all four services started; PostgreSQL and Redis health checks passed.
- Backend `/actuator/health` reported `UP`; frontend `/` and proxied `/api/v1/organizations` returned HTTP `200`.

### 2026-10-06 - Manav Gupta

**Commit message:** `feat(frontend): replace Vite starter with dashboard`

**Changes:**

- Replaced the Vite starter page with an unstyled layout skeleton: header, organization selector placeholder, navigation for the MVP dashboard views, and a placeholder content area.
- Removed unused starter assets and styles, set the page title, and proxied `/api` to the local backend for development.

**Verification:**

- `npm ci`, `npm run lint`, and `npm run build` passed.

### 2026-10-06 - Manav Gupta

**Commit message:** `feat(backend): add problem errors, organization read API, and GitHub webhook intake`

**Changes:**

- Added RFC 9457 problem responses with correlation IDs, springdoc OpenAPI, hardened error and actuator settings, and cursor pagination.
- Added Flyway migrations for identity, organization, team, integration installation, and webhook delivery tables with organization-consistent constraints.
- Added read-only organization and team endpoints, and a GitHub webhook receiver that verifies HMAC signatures over bounded raw bodies before parsing and records deliveries idempotently.
- Added modularity, signature, controller, pagination, and Testcontainers integration tests, and documented backend configuration and endpoints.

**Verification:**

- `./mvnw test` for the 24 Docker-independent tests passed locally, compiled with `-Djava.version=17` because JDK 21 is not installed; integration tests require Docker and run in CI.

### 2026-10-06 - Manav Gupta

**Commit message:** `docs: adopt MIT license and record Phase 1 decisions`

**Changes:**

- Added the MIT license, security policy, code of conduct, issue forms, and pull request template required for an open-source repository.
- Added decision records for the license, technology baseline, API error format and documentation, and GitHub webhook intake.
- Updated the README, contribution rules, Phase 0 tracker, license evaluation, and architecture document to reference the decisions and backend owner.

**Verification:**

- Reviewed internal links and decision references manually.

### 2026-09-21 - Manav Gupta

**Commit message:** `build: initialize Phase 1 project foundation`

**Changes:**

- Added the React and TypeScript frontend and Java 21 Spring Boot modular-monolith backend scaffolds.
- Added pinned PostgreSQL and Redis infrastructure, CI, environment templates, ignore rules, and local development documentation.

**Verification:**

- Passed frontend lint/build, Compose validation, and backend packaging in a Java 21 JDK container.

### 2026-09-21 - Manav Gupta

**Commit message:** `docs: establish Phase 0 foundation`

**Changes:**

- Added the Phase 0 research, requirements, architecture, threat model, initial data model, and REST API design.
- Added contribution rules, license evaluation, deliverable tracking, and the shared developer change-log format.

**Verification:**

- Validated required documents, internal links, requirement and threat identifiers, and Phase 0 coverage.