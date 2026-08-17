# Phase 5 Implementation Plan: Seed Data, Docker, Docs

## Context

Phases 0–4 delivered a fully working, tested full-stack app: Clean Architecture backend (PokeAPI sync,
CRUD, JWT auth), and a React frontend covering all four user stories against the real API. What's still
missing is everything in [development-plan.md](development-plan.md) §7–§8 and the delivery requirements
in `project-docs/requeriments.md`/`domain-requirements.md`: the app starts with an empty database and no
users, there's no Dockerfile for either side, and the root `README.md` is a three-line folder-structure
stub. This is the last phase — its whole point is turning "works on my machine, from source" into "works
for a grader, from a clean clone."

**Definition of done** (from development-plan.md §9): `docker compose up` from a fresh clone produces a
working, pre-seeded, login-able app — browse the list with real Pokemon already there, log in with
documented demo credentials, no manual setup steps beyond having Docker and network access to PokeAPI on
first boot.

## Design decisions

**Demo data is seeded by a `CommandLineRunner` calling the real `AuthService`/`PokemonSyncService`, not a
`V3__seed_demo_user.sql` migration.** The main plan's §7 suggested a SQL migration with a pre-hashed
BCrypt password. That means hand-computing (and then maintaining) a BCrypt hash string embedded in raw
SQL — fragile if the hashing scheme or cost factor ever changes, and impossible to verify by reading the
file. Calling `AuthService.register(email, password)` directly seeds through the exact same code path a
real signup uses (same validation, same hashing), and `PokemonSyncService` already has the real sync path
this whole app is built around. This is the same "dogfood the real path instead of hand-rolling a
shortcut" reasoning the main plan already applies to the Pokemon side — Phase 5 just applies it
consistently to the User side too, so there's no `V3` migration and no separate seeding mechanism to keep
in sync with the real one. Schema stays at `V2`.

**Seeding is idempotent and config-gated.** `DemoDataSeeder implements CommandLineRunner`
(`infrastructure/config/`): seeds the demo user by calling `register` and silently ignoring
`UserAlreadyExistsException` (safe to run on every boot); seeds Pokemon only when
`pokemonRepositoryPort.findAll(0, 1).totalElements() == 0` (so a restart with an existing H2 file doesn't
re-sync 20 Pokemon from PokeAPI every time). Both behaviors, plus the pokemon count and demo credentials,
are `@Value`-injected from `app.seed.*` properties — same externalization pattern already used for
`jwt.secret`. `app.seed.enabled: false` is set in `application-test.yaml` so the existing
`@SpringBootTest` (`AuthenticationFlowTest`) doesn't try to hit the real network or seed data into its
isolated in-memory database on every test run.

**`PokemonSyncService` gets a `syncByIds(List<Integer>)` method**, matching what the main plan's §3
already named as "a bulk variant [that] powers the startup seeder" — a thin loop over the existing
`syncPokemon(String)`, kept in the application layer (testable with mocked ports) rather than inlined as
a loop inside the infrastructure-layer `CommandLineRunner`.

**A real bug surfaced while designing the Docker path: `apiClient.js`'s `BASE_URL` fallback breaks for a
same-origin deployment.** `import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'` treats an empty
string as falsy, so building the frontend with `VITE_API_BASE_URL=""` (the whole point of the
nginx-reverse-proxy setup below — same-origin relative `/api/...` calls, no CORS) silently falls back to
`http://localhost:8080` instead, which doesn't exist inside the frontend container. Fixed by switching to
`??` (nullish coalescing), which only falls back when the variable is genuinely unset. This is a Phase 5
fix, not scope creep — Phase 4 never exercised a non-default base URL, so the bug was latent until now.

**nginx reverse-proxies `/api/` to the backend container; the frontend image is built with
`VITE_API_BASE_URL=""`.** This is what makes `docker-compose.yml` avoid CORS entirely, per the main
plan's §8 — from the browser's perspective every request is same-origin
(`http://localhost:5173/api/...`), and nginx forwards `/api/` server-side to `http://backend:8080/api/`.
Local dev (`npm run dev`) is unaffected: `.env`/`.env.example` still default to
`http://localhost:8080` for that workflow, only the Docker build arg is empty.

**The backend Gradle build disables the plain (non-executable) jar.** Spring Boot's Gradle plugin
produces both `pokeapi-back-<version>.jar` (the bootJar) and `pokeapi-back-<version>-plain.jar` by
default; a Dockerfile `COPY --from=build /app/build/libs/*.jar app.jar` would match both and fail ("must
be a directory"). `tasks.named('jar') { enabled = false }` is the standard fix — this project never
publishes the plain jar as a library, so there's nothing lost.

**The backend Dockerfile builds via the Gradle *wrapper*, not a `gradle:X` base image.** The project pins
Gradle 9.5.1 (`gradle-wrapper.properties`); using a `gradle:9...` image risks a silent version mismatch if
the wrapper version ever moves independently of the base image tag. Building `FROM eclipse-temurin:25-jdk-alpine`
and invoking `./gradlew bootJar` lets the wrapper fetch the exact pinned Gradle version itself, same as
every other build in this project (local, CI would do the same).

## Package layout (new/changed files this phase)

```
backend/pokeapi-back/src/main/java/.../application/pokemon/PokemonSyncService.java   (edit: + syncByIds)
backend/pokeapi-back/src/main/java/.../infrastructure/config/DemoDataSeeder.java     (new)
backend/pokeapi-back/src/main/resources/application.yaml                            (edit: + app.seed.*)
backend/pokeapi-back/src/test/resources/application-test.yaml                        (edit: + app.seed.enabled: false)
backend/pokeapi-back/build.gradle                                                    (edit: disable plain jar)
backend/pokeapi-back/Dockerfile                                                      (new)
backend/pokeapi-back/.dockerignore                                                    (new)

frontend/pokeapi-front/src/api/apiClient.js                                          (edit: || -> ??)
frontend/pokeapi-front/Dockerfile                                                     (new)
frontend/pokeapi-front/nginx.conf                                                     (new)
frontend/pokeapi-front/.dockerignore                                                  (new)

docker-compose.yml                                                                    (new, repo root)
README.md                                                                             (rewrite, repo root)
```

## Tasks

**T18 — Demo data seeder**
`PokemonSyncService.syncByIds(List<Integer> ids)` (loops `syncPokemon`); unit test (Mockito) confirms it
calls sync for every id and returns the synced list. `DemoDataSeeder` (`CommandLineRunner`): seeds the
demo user via `AuthService.register`, swallowing `UserAlreadyExistsException`; seeds
`app.seed.pokemon-count` Pokemon (ids `1..N`) via `syncByIds` only when the Pokemon table is empty; logs
the demo credentials on successful seed. `application.yaml`: `app.seed.enabled: true`,
`app.seed.pokemon-count: 20`, `app.seed.demo-email: demo@pokeapp.dev`,
`app.seed.demo-password: Demo1234!`. `application-test.yaml`: `app.seed.enabled: false`. Unit test
(Mockito, all ports/services mocked): seeds both when empty; skips Pokemon seeding when the repo already
has data; register conflict is swallowed without throwing; does nothing at all when `app.seed.enabled` is
false (verified via a `@SpringBootTest`-free constructor-arg test, not a context reload).

**T19 — Backend Dockerfile**
`build.gradle`: `tasks.named('jar') { enabled = false }`. Multi-stage `Dockerfile`: build stage
(`eclipse-temurin:25-jdk-alpine`) copies the wrapper + build files first for layer caching, then `src/`,
runs `./gradlew bootJar --no-daemon -x test`; runtime stage (`eclipse-temurin:25-jre-alpine`) runs as a
non-root user, copies the single jar, `EXPOSE 8080`. `.dockerignore` excludes `build/`, `.gradle/`,
`data/`, `bin/`.

**T20 — Frontend Dockerfile + apiClient fix**
Fix `apiClient.js`'s `||` → `??` (see design decisions). Multi-stage `Dockerfile`: build stage
(`node:22-alpine`) `npm ci` + `npm run build` with `ARG VITE_API_BASE_URL=""`; runtime stage
(`nginx:alpine`) serves `dist/`. `nginx.conf`: SPA fallback (`try_files $uri /index.html`) plus
`location /api/ { proxy_pass http://backend:8080/api/; }`. `.dockerignore` excludes `node_modules/`,
`dist/`. Existing frontend Vitest suite re-run unchanged (the `??` fix doesn't change default-URL
behavior, only the previously-broken empty-string case) to confirm nothing regressed.

**T21 — docker-compose.yml**
Root `docker-compose.yml`: `backend` service (build context `backend/pokeapi-back`, port `8080:8080`,
named volume for `/app/data` so the H2 file survives container restarts, healthcheck against
`/actuator/health`); `frontend` service (build context `frontend/pokeapi-front`, build arg
`VITE_API_BASE_URL: ""`, port `5173:80`, `depends_on: backend: condition: service_healthy`). Manual
verification: `docker compose up --build` from a clean state (fresh volume) — confirm the backend logs
show Flyway migrating, the seeder syncing 20 Pokemon and seeding the demo user, then open
`http://localhost:5173` and confirm the list is pre-populated with no manual sync needed, then log in with
the seeded demo credentials.

**T22 — README**
Rewrite root `README.md`: project overview + the four user stories, architecture summary (Clean
Architecture layers, linking to `project-docs/development-plan.md` and the phase docs for detail), local
setup for both backend (`gradlew bootRun`) and frontend (`npm run dev`), Docker setup
(`docker compose up --build`), demo credentials, full API endpoint table (from
`project-docs/development-plan.md` §4), test commands, and an explicit note that
`frontend/package.json` (one level above `pokeapi-front/`) is an unused decoy left by the original
scaffold — to preempt exactly the reviewer confusion the main plan flagged.

**T23 — Final polish pass**
`./gradlew build` (backend, full suite) and `npm run lint && npm run test && npm run build` (frontend)
both green. Grep-based architecture-boundary spot check: no `import ...api.` or `import
...infrastructure.` inside `domain/`. Browser console check on the Dockerized app (not just `npm run dev`)
for warnings. Review `git log` for the full branch — commit history should already read cleanly given the
one-task-per-commit pattern followed since Phase 0, no rewriting needed unless something's out of place.

## Verification

Backend: `./gradlew build` from `backend/pokeapi-back/`.
Frontend: `npm run lint && npm run test && npm run build` from `frontend/pokeapi-front/`.

Full definition-of-done check: from a clean state (`docker compose down -v` to drop the named volume,
simulating a fresh clone), `docker compose up --build` — confirm backend logs show 2 Flyway migrations,
20 Pokemon synced, and the demo user seeded; open `http://localhost:5173`, confirm the list shows real
Pokemon with no login; log in with `demo@pokeapp.dev` / `Demo1234!`; open a detail page; edit a Pokemon's
proprietary fields and confirm persistence; confirm the browser console is clean throughout. Restart the
stack (`docker compose up` again, same volume) and confirm the seeder does *not* re-sync (Pokemon count
unchanged, no duplicate-key errors) and the demo user login still works (register conflict swallowed).
