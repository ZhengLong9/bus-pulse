# BusPulse command guide

This is a maintained explanation of non-trivial commands used in BusPulse, starting
with PR #1's bootstrap and Docker troubleshooting. It is a reference, not a script
to run from top to bottom. Failed attempts are included for learning.

Commands are exact individual invocations; repeated invocations share an entry.
Each category identifies the tool. Unless specified otherwise, commands run from
the repository root, `C:\Users\nicho\OneDrive\Documents\ChatGPT\Bus Pulse\bus-pulse`.
Frontend commands run from `apps/web`; Maven and Java commands from `apps/api`.
The safety column covers flags, side effects, and reversibility. Variations are
examples, not additional commands that have necessarily been executed.

Approval usually allowed network access, cache writes outside the workspace, or
local service control beyond the execution sandbox. It did not necessarily grant
Windows administrator privileges. Before an approval request, explain the action
and its effects in plain English. Never put credentials or tokens in this guide.

## Git: repository and review workflow

Tool: Git. These commands affect the repository or its GitHub remote.

| Exact command | What it does and why BusPulse needed it | Affected files/services | Safety, flags, and side effects | Common variation |
| --- | --- | --- | --- | --- |
| `git clone https://github.com/ZhengLong9/bus-pulse.git bus-pulse` | Download the requested repository. Ran from the parent `Bus Pulse` directory. | Creates `bus-pulse/` and its `.git`; reads GitHub. | Additive; requires network. The original remote was empty. | Omit the destination to use the default repository name. |
| `git ls-remote origin` | Check remote branches before establishing the first PR base. | Reads GitHub refs. | Read-only; network required. | `git remote -v` shows configured URLs without contacting GitHub. |
| `git status --short --branch` | Check branch and pending changes before editing/committing. | Reads working tree and Git index. | Read-only; `--short` makes output compact. | `git status` gives more explanation. |
| `git add docs/MVP.md docs/Agents.md` | Stage the existing documentation for the initial baseline. | Git index only. | Does not commit; staged files can be unstaged. Historical path predates the move to root. | Stage individual files rather than `git add .`. |
| `git commit -m "docs: record MVP scope and engineering workflow"` | Create the documentation-only base because the repository had no commits. | Local history on initial `main`. | This was the initial baseline, not application implementation. Published history should be corrected with later commits. | Subsequent changes use feature branches and PRs. |
| `git switch -c chore/bootstrap-project` | Create and enter PR #1's feature branch before implementation. | Local branch and checkout. | `-c` creates a branch; does not push it. | `git switch chore/bootstrap-project` selects an existing branch. |
| `git mv docs/Agents.md AGENTS.md` | Move engineering instructions to the repository root for discovery. | Moves the file and stages the rename. | Content preserved; reversible with another move. | A filesystem move followed by staging both paths. |
| `git add .editorconfig .env.example .gitattributes .gitignore .nvmrc AGENTS.md README.md compose.yaml .github apps docs/architecture.md docs/local-development.md` | Stage the bootstrap implementation and docs. | Git index for the listed paths. | Directory arguments include all eligible changes beneath them; ignored secrets/build files stay excluded. Review the staged diff. | Stage a smaller list for a smaller change. |
| `git update-index --chmod=+x apps/api/mvnw` | Make the Unix Maven Wrapper executable after checkout. | File mode recorded in the Git index. | `+x` adds executable permission; does not run the script. Reversible with `--chmod=-x`. | Unix users can use `chmod +x` then stage the file. |
| `git diff --check` | Check unstaged changes for whitespace errors. | Reads working-tree differences. | Read-only. It does not inspect untracked files. | Use the staged or branch comparison below when appropriate. |
| `git diff --cached --check` | Check staged content before committing. | Reads Git index against HEAD. | Read-only; nonzero exit means whitespace problems. | `git diff --cached` displays the staged patch. |
| `git diff --cached --stat`; `git diff --cached -- AGENTS.md README.md .github/pull_request_template.md` | Review the documentation change size and persistent workflow rules before committing; individual invocations. | Reads the staged changes. | Read-only; `--` separates file paths from options. | `git diff --cached` shows all staged content. |
| `git diff main...HEAD --check` | Check all PR changes since the common ancestor with `main`. | Reads local branch history. | Read-only; `...` selects the merge-base comparison. | `git diff main...HEAD --stat` summarizes the PR. |
| `git check-ignore .env apps/api/.env.local apps/web/.env.local .bootstrap/pr-body.md` | Confirm local configuration and helper artifacts are excluded. | Reads ignore rules for these paths. | Read-only; does not prove every possible secret is excluded. | Add `-v` to see the matching ignore rule. |
| `git commit -m "chore: bootstrap frontend backend and local database"` | Record PR #1's reviewed bootstrap files. | Local feature-branch history. | Commits only staged content; does not publish. | Use a similarly descriptive message for each focused change. |
| `git push -u origin main` | Publish the initial documentation-only base so GitHub could compare a feature PR. | Creates remote `main`; sets local upstream. | Writes to GitHub. Initial-empty-repository exception; future features must not be pushed directly to `main`. No force flag. | Future work pushes its feature branch. |
| `git push -u origin chore/bootstrap-project` | Publish PR #1's branch and establish tracking. | GitHub feature branch; local upstream metadata. | Network write; `-u` sets tracking. Does not merge. | Once tracking exists, `git push`. |
| `git add .gitattributes .github/pull_request_template.md` | Stage line-ending and PR-template corrections. | Git index. | Reversible staging; no remote effects. | Stage any explicit list of reviewed files. |
| `git add AGENTS.md README.md .github/pull_request_template.md docs/COMMANDS.md` | Stage the command guide and persistent logging rules in PR #1. | Git index for four documentation files. | No application changes; review the staged diff. | Stage one file at a time. |
| `git commit -m "docs: document commands and approval workflow"` | Record this guide, agent rules, README link, and PR-template section. | Local feature-branch history. | Can be corrected with a follow-up commit; publication is separate. | Choose a message describing the next documentation change. |
| `git push` | Publish the documentation follow-up to the already tracked bootstrap branch. | GitHub feature branch and existing PR diff. | Network write; no force or merge. Verify the current branch first. | Explicitly name `origin chore/bootstrap-project`. |

## npm: frontend dependencies and checks

Tool: npm, Node.js, and package-provided tools. Working directory: `apps/web`.

| Exact command | What it does and why BusPulse needed it | Affected files/services | Safety, flags, and side effects | Common variation |
| --- | --- | --- | --- | --- |
| `node --version`; `npm.cmd --version` | Identify available runtimes before choosing compatible dependencies. These are two individual commands. | Reads installed tooling. | Read-only. | `node -v`; `npm.cmd -v`. |
| `npm.cmd view next version`; `npm.cmd view react version`; `npm.cmd view typescript version`; `npm.cmd view eslint version` | Read released package versions for bootstrap selection; each is an individual invocation. | npm registry and local npm cache/logs. | Network reads; no project dependency changes. | Inspect a specific version's metadata with `npm view package@version`. |
| `npm.cmd view eslint@9 version`; `npm.cmd view braces version`; `npm.cmd view eslint-config-next@16.4.0 peerDependencies --json` | Investigate ESLint compatibility and whether audit findings have a compatible fix. | npm registry and cache. | No application changes; `--json` changes output format. | `npm.cmd view eslint version` checks the newest major instead. |
| `npm.cmd install` | Install dependencies and generate/update the frontend lockfile after manifest changes. | `node_modules/`, `package-lock.json`, npm cache; registry. | Writes files and can run dependency lifecycle scripts. Lockfile changes require review. Dependencies/build output are regenerable. | `npm.cmd ci` for an existing approved lockfile. |
| `npm.cmd ci` | Verify a clean install reproduces the committed lockfile. | Replaces `node_modules/`; npm cache and registry. | Removes the existing dependency tree, can run lifecycle scripts, and fails if manifest and lockfile disagree. Preserves source and lockfile. | `npm.cmd install` intentionally updates resolution. |
| `npm.cmd audit --json` | Obtain detailed vulnerability findings for investigation. | Queries npm audit service; reads dependency tree. | Does not fix packages; may exit nonzero for findings. Metadata is sent to the registry service. | `npm.cmd audit` gives a readable summary. |
| `npm.cmd audit --omit=dev` | Check vulnerabilities affecting production dependencies separately. | npm audit service and dependency metadata. | Read-only; excludes development dependencies, so it is not a complete audit. | Omit `--omit=dev` for the full tree. |
| `npm.cmd ls eslint`; `npm.cmd ls eslint --depth=0` | Diagnose invalid ESLint peers, then confirm the selected direct version. | Reads installed dependency tree. | Read-only; invalid peers can produce nonzero exit. `--depth=0` hides transitive details. | `npm.cmd ls` inspects the whole tree. |
| `npm.cmd run lint` | Run ESLint checks on frontend files. | Reads source/config; executes the package script. | Current script has no `--fix`, so it does not rewrite source. | Adding `-- --fix` would modify files and requires reviewing changes. |
| `npm.cmd run build` | Check that Next.js produces a deployable frontend build. | Writes `.next/`, generated route types, and `next-env.d.ts`. | Generated output is reversible/regenerable; package scripts and build-time code execute. | `npm.cmd run dev` runs the development server. |
| `npm.cmd run typecheck` | Run TypeScript without emitting application JavaScript. | Reads source and generated Next.js types; may write incremental cache. | `--noEmit` prevents JS output; cache is disposable. Run build first on a fresh checkout. | The Next.js build also performs a type check. |
| `npm.cmd start` | Smoke-test the built frontend and `/api` rewrite. | Starts a loopback HTTP server on port 3000. | Requires an existing build; stop with Ctrl+C. Uses a local port and process resources. | `npm.cmd run dev` for editing with reload. |

The full bootstrap audit reported five high-severity development-only findings
through lint tooling's `braces` dependency. Production audit was clean. ESLint 10
had incompatible transitive peers, so ESLint 9.39.5 is pinned despite its support
warning. We did not run `npm audit fix --force`; that variation can introduce
breaking dependency changes.

## Maven and Java: backend build and execution

Tool: Maven Wrapper and Java. Working directory: `apps/api`.

| Exact command | What it does and why BusPulse needed it | Affected files/services | Safety, flags, and side effects | Common variation |
| --- | --- | --- | --- | --- |
| `java -version` | Confirm Java 21 is available. | Reads installed runtime. | Read-only. | `java --version`. |
| `.\mvnw.cmd --version` | Initialize the committed wrapper and confirm Maven/Java versions. | May download Maven to the user's `.m2/wrapper` cache. | No application edits; network and cache writes may need approval. | `./mvnw --version` on Unix. |
| `.\mvnw.cmd verify` | Compile, test, package, and run configured verification phases. | `target/`, `.m2` dependency cache; configured PostgreSQL. | Executes build plugins/tests. The wiring test connects to PostgreSQL; Flyway can apply pending migrations. Use a development database. | `.\mvnw.cmd test` stops before packaging. |
| `.\mvnw.cmd -q verify` | Run the same verification with less Maven output. | Same build output/cache/database as above. | `-q` reduces logging; it does not skip checks. Passed against PostgreSQL 18.6 after Docker recovered. | Omit `-q` for troubleshooting. |
| `.\mvnw.cmd -q -DskipTests package` | Check backend compilation/packaging while Docker was unavailable. | `target/` and Maven cache. | **`-DskipTests` skips test execution.** This did not establish database correctness; full verification followed. | `.\mvnw.cmd verify` runs tests too. |
| `java -jar target/api-0.0.1-SNAPSHOT.jar --spring.profiles.active=local` | Smoke-test the packaged backend with explicit local configuration. | Starts port 8080; connects to PostgreSQL; Flyway runs on startup. | Stop with Ctrl+C. Local profile loads `.env.local`; future migrations can change the configured database. | Documented development variation: `.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local"`. |

## Docker: local database and troubleshooting

Tool: Docker CLI, Compose, and Docker Desktop. Commands target the current Docker
context; PR #1 used `desktop-linux`. Read-only inspection needs a reachable engine
for server/container details. These commands never cleared the owner's old resources.

| Exact command | What it does and why BusPulse needed it | Affected files/services | Safety, flags, and side effects | Common variation |
| --- | --- | --- | --- | --- |
| `docker version`; `docker compose version`; `docker desktop status` | Check CLI, engine, Compose availability, and Desktop status; individual commands. | Reads Docker installation/backend state. | Read-only. A Client section alone does not establish engine readiness. | `docker info` gives more engine details. |
| `docker desktop start --detach` | Request Desktop startup without waiting in the foreground. | Docker Desktop and its engine/services. | `--detach` means command completion is not proof of readiness. May start other configured Docker workloads. | Open Docker Desktop from Start. |
| `docker desktop stop` | Try stopping failed Desktop startup before investigating the runtime socket. | Docker Desktop and all workloads using its engine. | Interrupts running containers; reversible by starting Desktop. At the time, Desktop reported stopped. | Quit Docker Desktop from its tray menu. |
| `docker desktop disable model-runner` | Try disabling the optional AI-model feature that appeared in the startup error. | Docker Desktop settings and Model Runner. | Would persist a feature change, reversible by enabling it. **Failed** because the settings backend was unavailable; no successful disable was established. | Disable Model Runner in Settings → AI if accessible. |
| `docker compose config --quiet` | Validate BusPulse Compose syntax and environment substitution. | Reads `compose.yaml` and root `.env`. | Read-only; `--quiet` suppresses rendered configuration, including credentials. Does not start containers or prove connectivity. | `docker compose config` prints resolved values and can expose secrets. |
| `docker compose up -d --wait db` | Pull/start the project's PostgreSQL service and wait for readiness. | PostgreSQL image; `buspulse-db-1`; project network; `buspulse_postgres_data` volume; host port 5433. | Writes persistent Docker data. `-d` runs in background; `--wait` checks startup health. Can recreate a changed service; does not prune unrelated resources. | `docker compose up -d db` returns without waiting for health. |
| `docker compose ps` | Confirm the BusPulse database container's state and published port. | Reads this Compose project's containers. | Read-only. | `docker ps` lists all running containers in the context. |
| `docker compose exec -T db psql -U buspulse -d buspulse -Atc 'SHOW server_version;'` | Confirm the container actually runs PostgreSQL 18.6. | Executes a read-only SQL statement inside `db`. | `-T` disables interactive terminal allocation; `-Atc` means unaligned, tuples-only output for one SQL command. It does not change data. | `docker compose exec db psql -U buspulse -d buspulse` opens an interactive SQL session. |

Docker's original `dockerInference` startup error later cleared; the cause was
not established. The intended PostgreSQL 18.6 container then started and passed
the backend test and both health checks. Failed socket-removal attempts below
must not be treated as a proven fix. No prune, volume deletion, or factory reset ran.

## PowerShell: setup, downloads, HTTP checks, and PR helpers

Tool: PowerShell and Windows file/process APIs. `.bootstrap/` is ignored and
contains one-off local helpers/validation data, not files available in a fresh clone.

| Exact command | What it does and why BusPulse needed it | Affected files/services | Safety, flags, and side effects | Common variation |
| --- | --- | --- | --- | --- |
| `Copy-Item -LiteralPath .env.example -Destination .env` | Create Compose's local development configuration. | Root `.env`. | **Overwrites an existing destination**; copy once or inspect first. Example credentials are local-only. | Check `Test-Path .env` before copying. |
| `Copy-Item -LiteralPath apps/api/.env.example -Destination apps/api/.env.local` | Create backend database settings. | Backend `.env.local`. | Can overwrite local settings; ignored by Git. The local Spring profile imports it as properties. | Edit an existing file instead of copying again. |
| `Copy-Item -LiteralPath apps/web/.env.example -Destination apps/web/.env.local` | Create Next.js's backend URL setting. | Frontend `.env.local`. | Can overwrite local settings; restart/rebuild Next.js after URL changes. | Use environment variables supplied by the process launcher. |
| `New-Item -ItemType Directory -Force .bootstrap \| Out-Null` | Create a place for temporary generation and validation artifacts; suppress normal command output. | Ignored `.bootstrap/` directory. | `-Force` permits an already existing directory; it is not a recursive deletion flag. | Create the folder in File Explorer. |
| `Start-Process -FilePath 'C:\Program Files\Docker\Docker\Docker Desktop.exe' -WindowStyle Hidden` | Try launching installed Docker Desktop for validation. | Docker Desktop process/services. | Starts an application; may initialize Docker services. `Hidden` requests no foreground window. Startup still needed verification. | `docker desktop start --detach`. |
| `fsutil reparsepoint query 'C:\Users\nicho\AppData\Local\Docker\run\dockerInference'` | Investigate the inaccessible special runtime endpoint. | Reads filesystem metadata for that exact endpoint. | Read-only; **failed with Windows error 1920**. | Gather Docker diagnostics through its Troubleshoot UI. |
| `Remove-Item -LiteralPath 'C:\Users\nicho\AppData\Local\Docker\run\dockerInference' -Force -ErrorAction Stop` | Attempt targeted cleanup after Desktop reported stopped. | Would delete only that runtime endpoint. | **Destructive, approval-required troubleshooting attempt; failed.** `-Force` requests removal despite ordinary restrictions, not bypassing Windows access controls; `Stop` makes errors terminating. No recursive flag. | Prefer supported restart/diagnostic workflows before manual cleanup. |
| `[System.IO.File]::Delete('C:\Users\nicho\AppData\Local\Docker\run\dockerInference')` | Try the Windows/.NET deletion API after PowerShell removal failed. | Same single endpoint. | **Also destructive and failed.** No old containers, images, or volumes were deleted. | Same supported restart/diagnostic workflow. |
| `Invoke-RestMethod http://127.0.0.1:8080/actuator/health` | Check backend and database health directly. | Local backend GET request. | Read-only. `UP` is a point-in-time health result. | Open the endpoint in a browser. |
| `Invoke-RestMethod http://127.0.0.1:3000/api/actuator/health` | Check Next.js forwarding to the healthy backend. | Frontend, backend, and health-check database connection. | Read-only; requires both servers running. | Direct backend health check above skips proxy validation. |
| `Invoke-WebRequest http://127.0.0.1:3000` | Check homepage HTTP status and BusPulse content. | Local frontend GET request. | Read-only; does not validate all UI behavior. | Open the page in a browser. |
| `& .\.bootstrap\create-pr.ps1` | Run the local helper that created draft PR #1, avoiding duplicate creation. | Git credential helper; GitHub API and PR metadata. | External write; used credentials in memory without printing/storing tokens. Helper is ignored, not part of the checkout. Did not merge. | With authenticated GitHub CLI: `gh pr create --base main --head chore/bootstrap-project --draft`. |
| `& .\.bootstrap\update-validation.ps1` | Update PR #1's validation notes and mark it ready after PostgreSQL 18 checks passed. | GitHub PR body/review-ready state; credential helper. | External write; preserves unrelated description sections. Ready for review is not approval or merge. | Edit the PR description and use Ready for review on GitHub. |
| `& .\.bootstrap\update-command-section.ps1` | Add/update the final command-summary section in PR #1 using the current remote description. | GitHub PR body; credential helper. | External write; preserves earlier sections and review state. No merge. Local helper is ignored. | Edit the PR description on GitHub. |

The Spring Initializr skeleton was downloaded with this exact command. Tool:
PowerShell `Invoke-WebRequest`; purpose: obtain official Maven Wrapper and baseline
Java sources. It writes only the ignored ZIP and makes a network request. There
are no credentials in the URL. A common alternative is downloading the same
project through Spring Initializr's browser UI.

```powershell
Invoke-WebRequest -Uri 'https://start.spring.io/starter.zip?type=maven-project&language=java&bootVersion=4.1.1.RELEASE&baseDir=api&groupId=sg.buspulse&artifactId=api&name=BusPulse&description=BusPulse%20API&packageName=sg.buspulse.api&packaging=jar&javaVersion=21&dependencies=web,jdbc,flyway,postgresql,actuator' -OutFile .bootstrap/api.zip
Expand-Archive -LiteralPath .bootstrap/api.zip -DestinationPath .bootstrap/generated -Force
```

`Expand-Archive` extracts the downloaded files into the ignored generation folder;
`-Force` can overwrite existing extracted files. These files are regenerable. The
generator's `4.1.1.RELEASE` identifier did not resolve as a Maven parent version;
the committed POM was corrected to the resolvable `4.1.1` release.

## PostgreSQL utilities: temporary fallback validation

Tool: PostgreSQL 16's installed Windows utilities, invoked through PowerShell.
These were a one-off fallback while Docker was unavailable, not the standard
development setup. They used `.bootstrap/pg-check/data` on port 5433 and did not
modify the pre-existing server on 5432. The temporary cluster was stopped before
Docker validation. Prefer the Compose setup now that it works.

| Exact command | What it does and why BusPulse needed it | Affected files/services | Safety, flags, and side effects | Common variation |
| --- | --- | --- | --- | --- |
| `New-Item -ItemType Directory -Force .bootstrap/pg-check \| Out-Null` | Create the isolated validation directory and suppress normal output. | Ignored workspace folder. | Additive; no data deletion. | Normal Compose needs no native cluster folder. |
| `'buspulse_local' \| Set-Content -LiteralPath .bootstrap/pg-check/password.txt` | Supply a disposable local-only password to `initdb`. | Ignored temporary password file. | Overwrites the file; never use or commit a real credential here. The Markdown backslash only escapes the table's pipe. | Use a securely provided password file for non-disposable credentials. |
| `& 'C:\Program Files\PostgreSQL\16\bin\initdb.exe' -D .bootstrap/pg-check/data -U buspulse --auth=scram-sha-256 --pwfile=.bootstrap/pg-check/password.txt --encoding=UTF8 --locale=C` | Initialize a separate PostgreSQL cluster with password authentication. | New database files under the ignored directory. | Writes a new cluster; `-D` is the target directory. Do not point it at existing database data. | Use `docker compose up -d --wait db`. |
| `& 'C:\Program Files\PostgreSQL\16\bin\pg_ctl.exe' -D .bootstrap/pg-check/data -l .bootstrap/pg-check/server.log -o '-p 5433 -h 127.0.0.1' -w start` | Start only the validation cluster and wait for startup. | Local PostgreSQL process, port 5433, data/log files. | Reversible by stopping it; `-o` passes port/address settings to the server. Loopback binding limits access. | Compose manages its own PostgreSQL process. |
| `$env:PGPASSWORD = 'buspulse_local'` | Set the disposable password for the following database utility. | Current process environment and child commands. | Local test credential only; environment variables are not a secret vault. Cleared afterward. | Interactive `psql` can prompt for a password. |
| `& 'C:\Program Files\PostgreSQL\16\bin\createdb.exe' -h 127.0.0.1 -p 5433 -U buspulse buspulse` | Create the validation database in the new cluster. | Adds `buspulse` database to that cluster. | Database write; existing user databases were not targeted. | Compose's `POSTGRES_DB` initializes its fresh volume. |
| `Remove-Item Env:PGPASSWORD` | Clear the disposable credential from the shell environment. | Current process environment only. | Does not remove files or change database passwords. | Close the temporary process/session. |
| `& 'C:\Program Files\PostgreSQL\16\bin\pg_ctl.exe' -D .bootstrap/pg-check/data -m fast -w stop` | Stop the isolated fallback cluster and free port 5433. | That PostgreSQL process; preserves its data files. | `-m fast` disconnects clients and rolls back active transactions before shutdown; `-w` waits. It does not drop databases. | Stop the corresponding service/container through its manager. |

## Commands introduced in PR #1

- Git branch/commit/push and PR helpers establish an owner-reviewed workflow.
- npm clean installation, lint, build, type checks, and production start validate the frontend.
- Maven Wrapper verification and packaged execution validate the backend/Flyway wiring.
- Compose creates persistent PostgreSQL storage and binds port 5433; SQL/HTTP checks confirm readiness.
- PowerShell setup/download commands create local configuration and bootstrap artifacts.
- Docker repair and native PostgreSQL fallback commands are historical diagnostics;
  socket deletion failed and is not part of routine setup. No old Docker data was cleared.

For future PRs, append or update relevant entries and end the PR description with
`Commands introduced in this PR`, linking the appropriate categories above.
