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

### Command guide and approvals
- Maintain `docs/COMMANDS.md` whenever running a non-trivial command, especially one requiring approval.
- Record the exact command, tool, what it does, why BusPulse needs it, affected files/services, safety/reversibility, dangerous flags or side effects, and a simpler equivalent or common variation when relevant.
- Keep entries concise and grouped by tool/category. Reuse an existing entry for an identical command; add new variants or changed effects as needed.
- Include the working directory when it matters, and distinguish failed attempts from successful commands. Do not present troubleshooting attempts as established fixes.
- Skip trivial navigation, directory listings, and basic file reads unless they explain an important workflow.
- Document commands before execution when practical, and update outcomes afterward. Never record credentials or tokens; reference variables instead and explicitly mark any necessary redaction.
- Before requesting approval, briefly explain in plain English what the command will do and its material side effects. The approval prompt must also explain the action.
- End every PR description with a short section titled `Commands introduced in this PR`, linking relevant guide entries and noting material risks.

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
