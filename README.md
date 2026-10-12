# BusPulse

BusPulse is a learning-focused Singapore bus reliability project. The planned MVP
combines live arrivals with historical observations for selected stop/service pairs.
Product scope is defined in [docs/MVP.md](docs/MVP.md).

This first increment provides a runnable development foundation: a Next.js placeholder,
a Spring Boot application, PostgreSQL, and Flyway wiring. Bus search, LTA access,
collection, and reliability metrics are not implemented yet.

## Repository

| Path | Purpose |
| --- | --- |
| `apps/web` | Next.js App Router, React, TypeScript; independent npm project |
| `apps/api` | Spring Boot modular monolith; independent Maven project |
| `apps/api/src/main/resources/db/migration` | Backend-owned Flyway migrations |
| `compose.yaml` | Local PostgreSQL with a health check and persistent volume |
| `docs` | Scope, architecture, and local development instructions |
| `AGENTS.md` | Engineering and Git workflow instructions |

## Development

Prerequisites: Node.js 22.14.0, npm 10.9.2, JDK 21, and Docker Desktop with the
Compose plugin and Linux containers. Maven is downloaded by the committed wrapper.
Start with [local-development.md](docs/local-development.md) for configuration,
startup, verification, and troubleshooting instructions.

Pinned application dependencies include Next.js 16.4.0, React 19.3.0, and Spring
Boot 4.1.1. The Maven Wrapper selects Maven 3.10.0; Compose selects
PostgreSQL 18.6 Alpine. Exact frontend dependency resolution is committed in
`apps/web/package-lock.json`.

Read [architecture.md](docs/architecture.md) for the data flow and boundaries.
Implement each issue on a focused branch, run relevant checks, and submit a PR
into `main`. The project owner reviews and approves PRs before merge.

## Tooling follow-up

Next.js's lint plugins currently require ESLint 9, so the project pins its latest
compatible patch despite its upstream end-of-support warning. The full npm audit
also reports five high-severity development dependency findings through the
lint tooling's `braces` dependency (GHSA-vfj7-8cjw-p6xm); the production-only audit
is clean. Review upstream compatible fixes before adding CI enforcement. Do not
apply npm's suggested major downgrade automatically.
