# Security Intelligence Supply Chain

An open-source security intelligence and software supply-chain platform that correlates repositories, security posture, dependencies, vulnerabilities, findings, and events.

The project is currently entering Phase 1: repository foundation.

## Repository Layout

```text
backend/                    Java 21 and Spring Boot modular monolith
frontend/                   React and TypeScript application
deployments/docker-compose/ Local PostgreSQL and Redis infrastructure
docs/phase-0/               Research and architecture baseline
```

## Quick Start

Prerequisites:

- JDK 21
- Node.js 20 or later
- Docker with Docker Compose

```bash
cp .env.example .env
docker compose --env-file .env -f deployments/docker-compose/compose.yml up -d

cd backend
./mvnw spring-boot:run
```

In another terminal:

```bash
cd frontend
npm install
npm run dev
```

See [DEVELOPMENT.md](DEVELOPMENT.md) for validation commands and environment details.

## Project Documents

- [Project plan](PLAN.md)
- [Architecture](ARCHITECTURE.md)
- [Phase 0 tracker](docs/phase-0/README.md)
- [Contribution rules](CONTRIBUTING.md)
- [Development change log](CHANGELOG.md)

No open-source license has been selected yet. See the [license evaluation](docs/phase-0/LICENSE_EVALUATION.md) before using or distributing the project.