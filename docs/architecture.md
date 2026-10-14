# Initial architecture

## Current bootstrap

```text
Browser -> Next.js :3000 -> /api/* rewrite -> Spring Boot :8080 -> PostgreSQL :5433
```

Next.js serves a static placeholder page. It forwards `/api/<path>` to the backend
at `/<path>`, so `/api/actuator/health` reaches `/actuator/health`. This keeps browser
requests on the frontend origin. The backend currently exposes only the built-in
health endpoint; its database indicator checks PostgreSQL connectivity. Health
details are hidden. No LTA requests or scheduled collection run in this increment.

The applications run on the host; Docker Compose runs only PostgreSQL. Local HTTP
servers and the database port bind to loopback. These are development defaults;
deployment configuration is a later decision.

Spring's local profile imports `apps/api/.env.local` as a Java properties file
when started from `apps/api`. Compose reads the root `.env`, and Next.js reads
`apps/web/.env.local`. Each process owns its configuration. Backend secrets must
never use a `NEXT_PUBLIC_` variable or be sent to the browser.

Flyway owns schema evolution. There are no domain migrations yet. Flyway may create
its schema history table on startup; it does not create bus observation tables.

## Planned boundaries

The Spring Boot application will contain both the API and collector. Organize
future code by feature (`stops`, `arrivals`, `collection`, `reliability`), with the
upstream adapter under `integration/lta`. Create those packages only when needed.
Do not introduce separate services or extra infrastructure without a demonstrated
requirement and approval.

Live requests and historical collection are separate use cases. A live search
must work even if the requested stop/service has no historical observations.
Collection will be opt-in for selected pairs, at 60-second intervals, and retain
all three upcoming buses. Preserve raw responses before normalizing observations.

Before persistence implementation, review `docs/data-model.md` (to be created
with that issue). Every normalized field needs a stated use. Preserve the raw
response reference and measurement uncertainty; do not represent an inferred
arrival between two polls as an exact measurement. Store instants in UTC and use
`Asia/Singapore` for commuter-facing time windows and displays.

## Next increments

1. Live-arrival backend and initial API contract.
2. Search-first frontend and arrival display.
3. Approved data model, targeted collector, and raw/normalized persistence.
4. First understandable historical waiting-time insight with sample coverage.

Future API and data-model documents belong in `docs` alongside the MVP. Do not
duplicate the product scope or treat this bootstrap as approval for later features.
