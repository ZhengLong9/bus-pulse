# Local development

Commands below use PowerShell on Windows. Begin in the repository root. Use JDK
21, Node.js 22.14.0 with npm 10.9.2, and Docker Desktop in Linux-container mode.
Node's selected version is recorded in `.nvmrc`; Windows users may select it with
their Node version manager. Maven does not need to be installed separately.

## 1. Configure

```powershell
Copy-Item .env.example .env
Copy-Item apps/api/.env.example apps/api/.env.local
Copy-Item apps/web/.env.example apps/web/.env.local
```

Run these copies once; do not overwrite existing configuration on subsequent
starts. All real environment files are ignored by Git. The examples contain only
local development credentials, which must not be reused for deployment.

The root `.env` configures Compose. Keep its database name, username, password,
and published port aligned with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` in
`apps/api/.env.local`. Spring loads that file explicitly via
`application-local.yml`; it is Java properties syntax, so do not use shell `export`
statements or surrounding quotes. OS environment variables override file values.
Start Maven from `apps/api` so the relative file import resolves correctly.

Next.js reads `apps/web/.env.local`; `API_BASE_URL` is server-only. Restart Next.js
after changing it, and rebuild before production-mode testing. No LTA key is
needed for this bootstrap.

## 2. Start PostgreSQL

Start Docker Desktop and wait for its Linux engine to be ready, then:

```powershell
docker compose config --quiet
docker compose up -d --wait db
docker compose ps
```

PostgreSQL is available only on `127.0.0.1:5433` by default (container port 5432).
This avoids conflicting with a host PostgreSQL installation. Its named volume mounts
at `/var/lib/postgresql`, the PostgreSQL 18 image's supported parent data path.

## 3. Start the backend

In a separate terminal:

```powershell
Set-Location apps/api
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"
```

The wrapper downloads pinned Maven on its first invocation. On macOS/Linux use
`./mvnw` instead of `.\mvnw.cmd`. The backend listens on `http://127.0.0.1:8080`.
Startup requires PostgreSQL and the database configuration. Flyway is enabled;
the empty migration directory is intentional until the domain schema is approved.

## 4. Start the frontend

In another terminal:

```powershell
Set-Location apps/web
npm.cmd ci
npm.cmd run dev
```

Open `http://127.0.0.1:3000`. The placeholder is the entire current UI. Use `npm`
instead of `npm.cmd` on macOS/Linux.

## Verify

With both servers running, from any PowerShell terminal:

```powershell
Invoke-RestMethod http://127.0.0.1:8080/actuator/health
Invoke-RestMethod http://127.0.0.1:3000/api/actuator/health
```

Both should report `status: UP`. The second also verifies Next.js's backend rewrite.
The frontend serves the placeholder independently; the health request verifies
the database-connected path.

From `apps/api`, with PostgreSQL running and `.env.local` configured:

```powershell
.\mvnw.cmd verify
```

The wiring test loads the local profile, checks a real PostgreSQL connection, and
validates Flyway with no pending migrations. It needs the local database; it does
not silently skip when Docker is unavailable. Future backend logic should add
fixture-based tests that do not require LTA credentials.

From `apps/web`:

```powershell
npm.cmd run lint
npm.cmd run build
npm.cmd run typecheck
```

Build generates Next.js route types; run it before the standalone type check on
a fresh checkout. For a production-mode local smoke check, stop `next dev` and
run `npm.cmd start` after building.

## Stop and troubleshoot

Stop the application terminals with Ctrl+C. From the repository root:

```powershell
docker compose down
```

This preserves the named database volume. Volume deletion is a separate,
destructive action and is not part of the normal stop workflow.

- **Docker pipe/engine error:** start Docker Desktop and enable Linux containers.
- **Port 5433 occupied:** change `POSTGRES_PORT` and the JDBC URL port together.
- **Port 8080 occupied:** change backend `SERVER_PORT` and frontend `API_BASE_URL`.
- **Authentication failure after editing `.env`:** initialization credentials only
  apply when the data volume is first created. Update the existing database role
  deliberately or use a separate development volume; restarting is insufficient.
- **Missing `DB_URL` or password:** check the working directory, local profile,
  and `.env.local` copies. Spring does not auto-load arbitrary `.env` files.
- **Health returns 503:** inspect backend logs and `docker compose logs db` for
  database connectivity errors. A running HTTP server alone does not mean readiness.
- **First run cannot download dependencies:** npm, Maven Central, and the container
  registry require network access. Check proxy/network configuration.
