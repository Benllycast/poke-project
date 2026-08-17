# Phase 5 Checklist

Tracks every individual file/task from
[phase-5-seed-docker-docs-plan.md](phase-5-seed-docker-docs-plan.md). Check items off as they land.

## T18 — Demo data seeder

- [x] Edit `application/pokemon/PokemonSyncService.java`: add `syncByIds(List<Integer> ids)`
- [x] Create `infrastructure/config/DemoDataSeeder.java` (`CommandLineRunner`): seed demo user via
      `AuthService.register` (swallow `UserAlreadyExistsException`); seed `app.seed.pokemon-count`
      Pokemon via `syncByIds(1..N)` only when the Pokemon table is empty
- [x] Edit `application.yaml`: `app.seed.enabled: true`, `app.seed.pokemon-count: 20`,
      `app.seed.demo-email: demo@pokeapp.dev`, `app.seed.demo-password: Demo1234!`
- [x] Edit `application-test.yaml`: `app.seed.enabled: false`

Verify:
- [x] `PokemonSyncServiceTest`: `syncByIds` calls sync for every id, returns the synced list
- [x] `DemoDataSeederTest`: seeds user + Pokemon when both are empty/missing
- [x] `DemoDataSeederTest`: skips Pokemon seeding when the repo already has data
- [x] `DemoDataSeederTest`: register conflict (`UserAlreadyExistsException`) is swallowed, not thrown
- [x] `DemoDataSeederTest`: does nothing when `app.seed.enabled` is false
- [x] `./gradlew test --tests "*PokemonSyncServiceTest" --tests "*DemoDataSeederTest"` passes (3/3, 4/4)
- [x] `./gradlew test --tests "*AuthenticationFlowTest"` still passes (1/1 — confirms
      `app.seed.enabled: false` in `application-test.yaml` actually stops the seeder from running against
      the isolated test DB)

## T19 — Backend Dockerfile

- [x] Edit `build.gradle`: `tasks.named('jar') { enabled = false }`
- [x] Create `backend/pokeapi-back/Dockerfile` (multi-stage: `eclipse-temurin:25-jdk-alpine` build via
      `./gradlew bootJar`, `eclipse-temurin:25-jre-alpine` runtime, non-root user, `EXPOSE 8080`)
- [x] Create `backend/pokeapi-back/.dockerignore` (`build/`, `.gradle/`, `data/`, `bin/`)

Verify:
- [x] `./gradlew bootJar -x test` still produces exactly one jar in `build/libs/`
      (`pokeapi-back-0.0.1-SNAPSHOT.jar`, no more `-plain.jar`)
- [x] `docker build -t pokeapi-back backend/pokeapi-back` succeeds — run via WSL2 Ubuntu-22.04 (Docker
      wasn't available in the primary Windows environment; the user pointed at their existing WSL Docker
      install). Build stage resolved deps and ran `bootJar` cleanly.
- [x] Found by actually running the container: it exits immediately with
      `AccessDeniedException: /app/data/pokedb.lock.db`. The image adds a non-root `spring` user (good
      practice) but the named volume mounts at `/app/data` owned by root, and the JRE stage never chowned
      that path — H2 couldn't write its lock file. Fixed by adding
      `RUN mkdir -p /app/data && chown -R spring:spring /app` before `USER spring`. Confirmed fixed via a
      full `docker compose up --build` (see T21) — backend now boots, migrates, and seeds successfully.

## T20 — Frontend Dockerfile + apiClient fix

- [x] Edit `src/api/apiClient.js`: `||` → `??` for the `VITE_API_BASE_URL` fallback
- [x] Create `frontend/pokeapi-front/Dockerfile` (multi-stage: `node:22-alpine` build with
      `ARG VITE_API_BASE_URL=""`, `nginx:alpine` runtime serving `dist/`)
- [x] Create `frontend/pokeapi-front/nginx.conf` (SPA fallback + `/api/` reverse proxy to `backend:8080`)
- [x] Create `frontend/pokeapi-front/.dockerignore` (`node_modules/`, `dist/`, `.env*`)

Verify:
- [x] `npm run test` still passes (20/20 — the `??` fix doesn't change default-URL behavior)
- [x] `npm run lint` clean
- [x] `npm run build` still passes
- [x] `docker build -t pokeapi-front frontend/pokeapi-front` succeeds (via WSL2 Ubuntu-22.04 Docker) —
      `npm ci` + `vite build` ran cleanly inside the container, nginx stage copied `dist/` + `nginx.conf`

## T21 — docker-compose.yml

- [x] Create root `docker-compose.yml`: `backend` (build context, port `8080:8080`, named volume for
      `/app/data`, healthcheck on `/actuator/health`), `frontend` (build context, build arg
      `VITE_API_BASE_URL: ""`, port `5173:80`, `depends_on: backend: condition: service_healthy`)
- [x] Found while wiring the healthcheck: `/actuator/health` fell under `SecurityConfig`'s
      `anyRequest().authenticated()` catch-all, so the unauthenticated `wget` healthcheck would have 401'd
      forever and the `frontend` service (gated on `service_healthy`) would never have started. Added
      `.requestMatchers("/actuator/health").permitAll()` and a matching assertion in
      `AuthenticationFlowTest`

Verify (all run for real via WSL2 Ubuntu-22.04 Docker, backend temporarily remapped to host port 8090
during this run only — port 8080 was held by an unrelated pre-existing container on the user's machine;
`docker-compose.yml` itself was reverted back to `8080:8080` before committing):
- [x] `docker compose down -v && docker compose up --build` from a clean state — backend logs show both
      Flyway migrations, `Seeded demo user demo@pokeapp.dev / Demo1234!`, `Seeded 20 Pokemon`; both
      containers reach `Healthy`/`Started`
- [x] Frontend root shows a pre-populated list with no login — confirmed via `get_page_text` in the
      browser: real Bulbasaur..Raticate data, sprites, categories, mass, abilities/moves all render
- [x] `GET /api/pokemon` via `http://localhost:5173/api/...` (through nginx) returns the same data as
      `http://localhost:<backend-port>/api/...` directly — same-origin proxy confirmed working, and
      confirms the `apiClient.js` `??` fix actually does what it was fixed for
- [x] Log in with `demo@pokeapp.dev` / `Demo1234!` succeeds — nav updates to show the email + "Log out",
      `SyncForm` (protected UI) appears
- [x] Edit a Pokemon's `localizedName`/`region`/`tags` via the real API, `GET` again — values persisted
- [x] Browser console clean throughout (checked after every step via `read_console_messages`)
- [x] Restart (`docker compose restart backend`, same volume): logs show
      `Schema "PUBLIC" is up to date. No migration necessary` and no new "Seeded..." lines;
      `totalElements` still 20 (no duplicates); the earlier edit is still there; demo login still returns
      200
- [x] Found + fixed along the way: the non-root-user/volume-ownership bug in T19 (blocked every boot until
      fixed)

## T22 — README

- [x] Rewrite root `README.md`: overview + 4 user stories, architecture summary, local setup
      (backend + frontend), Docker setup, demo credentials, endpoint table, test commands, note on the
      unused decoy `frontend/package.json`

Verify:
- [x] Backend/frontend commands (`bootRun`, `test`, `npm run dev/lint/test/build`) all already verified
      live earlier in Phases 0–5; Docker commands documented but not independently run (see T19–T21
      caveat)
- [x] Endpoint table matches the real `PokemonController`/`AuthController` routes — checked against
      `@GetMapping`/`@PostMapping`/`@PutMapping`/`@DeleteMapping` annotations directly

## T23 — Final polish pass

- [x] `./gradlew build` passes (backend, full suite — 53/53 tests)
- [x] `npm run lint && npm run test && npm run build` passes (frontend, full suite — 20/20 tests)
- [x] Grep check: no `import ...api.` / `import ...infrastructure.` inside `domain/` or `application/` —
      zero matches in both
- [x] Browser console check on the Dockerized app — done via WSL2 Docker (see T21); clean throughout
- [x] `git log` review for the branch — one implementation commit + one checklist-checkoff commit per
      task, consistent with every prior phase; reads cleanly

## Overall Phase 5 Verification

- [x] `./gradlew build` passes (53/53)
- [x] `npm run lint && npm run test && npm run build` passes (20/20)
- [x] `docker compose down -v && docker compose up --build` from a clean state produces a working,
      pre-seeded, login-able app (the project's stated Definition of Done) — **verified for real** via
      WSL2 Ubuntu-22.04 Docker (no Docker CLI in the primary Windows environment; the user pointed at
      their existing WSL install). Along the way this surfaced and fixed a real bug that would have
      blocked every container boot: the non-root `spring` user had no write access to the `/app/data`
      volume mount (see T19).
- [x] Demo credentials documented in README work end-to-end — logged in through the actual browser
      against the Dockerized stack, nav updated correctly
- [x] `git status` clean on both `backend/` and `frontend/` outside this phase's intended changes
