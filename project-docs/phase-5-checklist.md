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
- [ ] `docker build -t pokeapi-back backend/pokeapi-back` succeeds — **NOT independently verified**: no
      Docker CLI is available in this execution environment (checked both bash and PowerShell). Dockerfile
      was hand-reviewed for correctness instead; needs verification wherever Docker is actually available.
- [ ] `docker run --rm -p 8080:8080 pokeapi-back` boots, `curl http://localhost:8080/api/pokemon` → 200 —
      same caveat as above, not run

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
- [ ] `docker build -t pokeapi-front frontend/pokeapi-front` succeeds — **NOT independently verified**,
      same Docker-unavailable caveat as T19

## T21 — docker-compose.yml

- [ ] Create root `docker-compose.yml`: `backend` (build context, port `8080:8080`, named volume for
      `/app/data`, healthcheck on `/actuator/health`), `frontend` (build context, build arg
      `VITE_API_BASE_URL: ""`, port `5173:80`, `depends_on: backend: condition: service_healthy`)

Verify:
- [ ] `docker compose up --build` from a clean state (`docker compose down -v` first) — backend logs show
      2 Flyway migrations + seeded Pokemon + demo user
- [ ] `http://localhost:5173` shows a pre-populated list with no login
- [ ] Log in with `demo@pokeapp.dev` / `Demo1234!` succeeds
- [ ] Open a detail page, edit proprietary fields, confirm persistence
- [ ] Browser console clean throughout
- [ ] Restart (`docker compose up`, same volume): seeder does not re-sync/duplicate, demo login still
      works

## T22 — README

- [ ] Rewrite root `README.md`: overview + 4 user stories, architecture summary, local setup
      (backend + frontend), Docker setup, demo credentials, endpoint table, test commands, note on the
      unused decoy `frontend/package.json`

Verify:
- [ ] All documented commands actually run as written (spot-check each one)
- [ ] Endpoint table matches the real `PokemonController`/`AuthController` routes

## T23 — Final polish pass

- [ ] `./gradlew build` passes (backend, full suite)
- [ ] `npm run lint && npm run test && npm run build` passes (frontend, full suite)
- [ ] Grep check: no `import ...api.` / `import ...infrastructure.` inside `domain/`
- [ ] Browser console check on the Dockerized app (not just `npm run dev`)
- [ ] `git log` review for the branch — commit history reads cleanly

## Overall Phase 5 Verification

- [ ] `./gradlew build` passes
- [ ] `npm run lint && npm run test && npm run build` passes
- [ ] `docker compose down -v && docker compose up --build` from a clean state produces a working,
      pre-seeded, login-able app (the project's stated Definition of Done)
- [ ] Demo credentials documented in README work end-to-end
- [ ] `git status` clean on both `backend/` and `frontend/` outside this phase's intended changes
