# Development

## Full Stack With Docker

Start Docker Desktop with Linux containers on Windows. From the repository root,
copy `.env.example` to `.env` once if `.env` does not already exist:

```powershell
Copy-Item .env.example .env
```

Then start the complete local stack with one command:

```powershell
docker compose up --build -d
```

The `COMPOSE_FILE` value in `.env` selects the configuration under
`deployments/docker-compose/`. Compose builds the Java 21 backend and Node.js
frontend and waits for PostgreSQL and Redis health checks before starting the
backend. No local Java, Maven, or Node.js installation is required for this path.
The frontend runs the Vite development server; this is not a production deployment.

- Frontend: <http://localhost:5173>
- Backend health: <http://localhost:8080/actuator/health>
- API documentation: <http://localhost:8080/swagger-ui.html>

The first build downloads images and dependencies. Wait for backend startup before
using the API. Inspect startup and stop containers without deleting database data:

```powershell
docker compose ps
docker compose logs -f backend frontend
docker compose down
```

`BACKEND_PORT` and `FRONTEND_PORT` in `.env` control host ports. The containerized
backend derives database credentials from `POSTGRES_*` and uses Docker service
names for connections; host-side `SPRING_*` values are not used by Compose.
Changing `POSTGRES_PASSWORD` does not change the password in an already initialized
PostgreSQL volume. Update the database role password separately or retain its
existing credentials; do not delete a volume containing data you need.

## Toolchain

For running the backend and frontend directly on your machine:

- Java 21 JDK
- Maven Wrapper included in `backend/`
- Node.js 22.12 or later with npm
- Docker and Docker Compose

A Java runtime alone is insufficient; `javac -version` must report Java 21.

## Infrastructure

For host-side backend and frontend processes, create local environment
configuration (if absent) and start only PostgreSQL and Redis:

```bash
cp .env.example .env
docker compose --env-file .env -f deployments/docker-compose/compose.yml up -d postgres redis
docker compose --env-file .env -f deployments/docker-compose/compose.yml ps
```

The values in `.env.example` are development-only defaults. Do not reuse them in a shared or production environment.

Stop services without deleting data:

```bash
docker compose --env-file .env -f deployments/docker-compose/compose.yml down
```

## Backend

The backend uses Spring Boot 4.1, Java 21, PostgreSQL, Redis, Flyway, Spring Modulith, and Testcontainers.

```bash
cd backend
./mvnw test
./mvnw spring-boot:run
```

Backend tests require Docker because integration context tests start pinned PostgreSQL and Redis containers.

For a local application process, configure these environment variables after starting Compose:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/secintel
export SPRING_DATASOURCE_USERNAME=secintel
export SPRING_DATASOURCE_PASSWORD=change-me-for-local-development
export SPRING_DATA_REDIS_HOST=localhost
export SPRING_DATA_REDIS_PORT=6379
# Optional: enables POST /api/v1/webhooks/github. Use a long random value, also set on the GitHub App.
export GITHUB_WEBHOOK_SECRET=<random-value>
```

Run a subset of tests that does not need Docker:

```bash
./mvnw test -Dtest='CursorPageTests,GitHubWebhook*Tests,OrganizationControllerTests,ModularityTests' -Dsurefire.failIfNoSpecifiedTests=false
```

Classes ending in `IntegrationTests`, and `BackendApplicationTests`, require Docker.

### Backend Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `GITHUB_WEBHOOK_SECRET` | unset | GitHub App webhook secret. When unset, the webhook endpoint returns `503`. |
| `SECINTEL_API_DOCS_ENABLED` | `true` | Serves `/v3/api-docs` and `/swagger-ui.html`. Set `false` in shared environments until authentication exists. |

### Backend Endpoints

| Path | Purpose |
| --- | --- |
| `GET /actuator/health` | Liveness and readiness (`/actuator/health/liveness`, `/actuator/health/readiness`) |
| `GET /api/v1/organizations` | Cursor-paginated organizations |
| `GET /api/v1/organizations/{organizationId}` | Organization summary |
| `GET /api/v1/organizations/{organizationId}/teams` | Cursor-paginated teams |
| `POST /api/v1/webhooks/github` | Signed GitHub App deliveries ([ADR 0004](docs/decisions/0004-github-webhook-intake.md)) |
| `/swagger-ui.html` | Interactive API documentation |

Organization endpoints are unauthenticated until the authentication decision (`D-004`) is implemented. They expose no personal data; membership endpoints are intentionally not exposed yet.

Errors use `application/problem+json`. Every response has an `X-Correlation-Id` header that also appears in logs.

## Frontend

```bash
cd frontend
npm install
npm run lint
npm run build
npm run dev
```

The frontend is a layout skeleton only; it does not call the API yet. The dev server proxies `/api` to `http://localhost:8080`.

## Windows

Git on Windows may report `backend/mvnw` as changed from mode `100755` to `100644`. Do not commit that change, because CI needs the executable bit. Run this once per clone:

```bash
git config core.fileMode false
```

## Commit Workflow

Before committing, add one entry to [CHANGELOG.md](CHANGELOG.md) with your name, date, exact commit message, changes, and verification. Keep implementation commits focused and independently reviewable.