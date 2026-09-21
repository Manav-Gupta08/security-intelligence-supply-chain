# Development

## Toolchain

- Java 21 JDK
- Maven Wrapper included in `backend/`
- Node.js 20 or later with npm
- Docker and Docker Compose

A Java runtime alone is insufficient; `javac -version` must report Java 21.

## Infrastructure

Create local environment configuration and start PostgreSQL and Redis:

```bash
cp .env.example .env
docker compose --env-file .env -f deployments/docker-compose/compose.yml up -d
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
```

## Frontend

```bash
cd frontend
npm install
npm run lint
npm run build
npm run dev
```

The generated Vite screen is temporary scaffolding, not the product dashboard design.

## Commit Workflow

Before committing, add one entry to [CHANGELOG.md](CHANGELOG.md) with your name, date, exact commit message, changes, and verification. Keep implementation commits focused and independently reviewable.