# Security Intelligence Supply Chain

An open-source security intelligence and software supply-chain platform that correlates repositories, security posture, dependencies, vulnerabilities, findings, and events.

The project is currently entering Phase 1: repository foundation.

## Repository Layout

```text
backend/                    Java 21 and Spring Boot modular monolith
frontend/                   React and TypeScript application
deployments/docker-compose/ Local full-stack Docker Compose setup
docs/phase-0/               Research and architecture baseline
```

## Quick Start

Prerequisites:

- Docker with Docker Compose (on Windows, start Docker Desktop with Linux containers)

From the repository root, create `.env` once if it does not already exist:

```powershell
Copy-Item .env.example .env
```

Then start PostgreSQL, Redis, the backend, and the frontend:

```powershell
docker compose up --build -d
```

Open the frontend at <http://localhost:5173> and API documentation at <http://localhost:8080/swagger-ui.html>.
Java and Node.js run inside containers; they do not need to be installed locally for this workflow.
The frontend remains a layout skeleton, and this Compose setup is for local development, not production.

See [DEVELOPMENT.md](DEVELOPMENT.md) for validation commands and environment details.

## Project Documents

- [Project plan](PLAN.md)
- [Architecture](ARCHITECTURE.md)
- [Phase 0 tracker](docs/phase-0/README.md)
- [Contribution rules](CONTRIBUTING.md)
- [Architecture decision records](docs/decisions/README.md)
- [Security policy](SECURITY.md)
- [Code of conduct](CODE_OF_CONDUCT.md)
- [Development change log](CHANGELOG.md)

## License

Released under the [MIT License](LICENSE).