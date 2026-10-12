# BusPulse Engineering Instructions

BusPulse is a learning-focused portfolio project.

## Architecture
- Frontend: Next.js + React + TypeScript
- Backend: Spring Boot + Java
- Database: PostgreSQL
- Prefer a simple modular monolith for the MVP.
- Do not introduce infrastructure such as Kafka, Redis, microservices, or Kubernetes without explicit approval.

## Working style
- Before large changes, explain the intended implementation approach.
- Keep PRs small and reviewable.
- Do not silently expand product scope.
- Add or update automated tests for meaningful backend logic.
- Prefer readable code over clever abstractions.
- Preserve raw upstream data before transforming it.
- Do not fabricate precision in historical bus-arrival calculations.

## Project documentation
Use `docs/MVP.md` for product scope and architecture decisions.

## Git workflow

- Never implement a feature directly on `main`.
- Each issue should be implemented on its own feature branch.
- Use descriptive branch names, for example:
  - `chore/bootstrap-project`
  - `feature/live-arrivals`
  - `feature/historical-collector`

- Keep branches focused on one issue or clearly defined change.
- After implementation:
  1. run relevant tests and validation,
  2. summarize what changed,
  3. commit the work with meaningful commit messages,
  4. push the branch,
  5. create a pull request into `main`.

- Do not merge pull requests automatically.
- The project owner must review and approve each PR before merge.

## Pull request requirements

Every meaningful PR should include:

- What changed
- Why it changed
- Key files/functions
- How the change was tested
- Known limitations or follow-up work

For very small changes, keep the PR explanation proportionate to the change.
