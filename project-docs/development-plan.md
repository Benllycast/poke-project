# Development Plan: Pokemon Interview Exercise

## Context

This repo (`C:\git\poke-project`) is a graded technical-interview exercise: a Spring Boot API that
integrates with the external PokeAPI, replicates Pokemon data into a local relational store, allows
local edits, and exposes it to a React frontend. It's evaluated on Clean Architecture, test coverage
(TDD preferred), code quality, working functionality, and a live presentation + code review.

Both `backend/pokeapi-back` and `frontend/pokeapi-front` are currently **unmodified framework
scaffolds** — no entities, controllers, services, or app-specific frontend code exist yet. The backend's
`build.gradle` also has a self-contradictory "kitchen sink" dependency set (simultaneous JPA + JDBC +
R2DBC + MongoDB + RSocket + native-image tooling) that needs deliberate pruning before real work starts.

Goal of this plan: turn the four required user stories (Enumeration, Detail View, Data Sync, Local
Edit) plus the auxiliary auth API into a buildable, defensible, Docker-runnable full-stack app, sequenced
so the candidate always has something demoable.

This document is written to `project-docs/development-plan.md` for reference throughout the build.

## Guiding principles

- Simplest option that satisfies each stated requirement and is defensible in a code review — no
  microservices, no reactive stack, no Kubernetes, no premature optimization.
- Clean Architecture via package structure: `domain` (framework-free) → `application` (business logic,
  depends only on `domain`) → `api` + `infrastructure` (depend inward, never on each other).
- TDD concentrated where it's cheap and high value: `domain`/`application` layers (pure, mockable).
  Controllers/repositories get integration tests alongside the slice.
- Every backend/frontend phase maps visibly back to a user story (US-01..04) — that mapping is exactly
  what a reviewer checks.

## 1. Backend architecture

Package layout under `com.bcastillo.pokeapiback`:

```
domain/model/          Pokemon, PokemonStat, EvolutionStage, User — plain Java, zero Spring imports
domain/exception/       PokemonNotFoundException, InvalidPokemonDataException, UserAlreadyExistsException
domain/port/             PokemonRepositoryPort, UserRepositoryPort, PokeApiClientPort (interfaces)
application/pokemon/     PokemonSyncService (US-03), PokemonQueryService (US-01/02), PokemonEditService (US-04)
application/auth/        AuthService, PasswordHasher port
api/pokemon/              PokemonController, DTOs, mapper
api/auth/                  AuthController, DTOs
api/common/                GlobalExceptionHandler, ApiErrorResponse
infrastructure/persistence/  JPA entities, JpaRepositories, RepositoryAdapters, entity<->domain mappers
infrastructure/pokeapi/       PokeApiClientAdapter (RestClient), raw response DTOs, mapper to domain model
infrastructure/security/       SecurityConfig, JwtTokenProvider, JwtAuthenticationFilter
```

Rule: `domain` never imports `api.*` or `infrastructure.*`; controllers never touch JPA entities or raw
PokeAPI DTOs directly — always through ports/services and their own DTOs. Optional polish: an ArchUnit
test asserting the import rule, turning the architecture claim into an enforced fact.

### Dependency pruning (`build.gradle`)

**Remove:** data-mongodb(+reactive), data-r2dbc, r2dbc-h2, data-jdbc, sqlite-jdbc, rsocket(+security-rsocket),
websocket, spring-security-messaging, opentelemetry/zipkin/prometheus, graalvm native-image plugin,
webclient (reactive client — use blocking `RestClient` instead), spring-restdocs + asciidoctor (a
hand-written endpoint table in the README covers the same value more cheaply here).

**Keep/add:** webmvc, data-jpa + H2 (runtime), flyway, validation, security, restclient, actuator (free
`/actuator/health` for Docker healthcheck), lombok, devtools, h2console (gate to a dev profile — good demo
value), `spring-boot-starter-test` (currently missing — most split test starters assume it). **Add:**
`io.jsonwebtoken:jjwt-api/impl/jackson` for JWT; test-scope WireMock (or MockWebServer) for stubbing
PokeAPI calls. Optional if time allows: `spring-boot-starter-cache` + Caffeine for the caching nice-to-have.

Net effect: ~40 lines of contradictory deps down to ~15, each traceable to a requirement.

### Persistence: Spring Data JPA + H2

Chosen over plain JDBC/Spring Data JDBC — declarative repositories maximize delivery speed for a
2-entity schema, `@DataJpaTest` + H2 keeps tests fast with no Testcontainers needed, and H2→Postgres is a
one-line swap later (good "how would you productionize this" talking point since Flyway migrations
already exist). Use **file-based H2** (`jdbc:h2:file:./data/pokedb`) so data survives restarts.

Keep `domain.model.Pokemon` (plain object) separate from `infrastructure.persistence.entity.PokemonEntity`
(`@Entity`), converted by an infra-layer mapper. This is what makes "business logic independent of
persistence" literally true, not just claimed.

### Flyway migrations (`src/main/resources/db/migration/`)

- `V1__create_pokemon_table.sql`, `V2__create_user_table.sql` — schema (see §2)
- `V3__seed_demo_user.sql` — one demo user, password pre-hashed with BCrypt

### Auth

- `POST /api/auth/register`, `POST /api/auth/login` — BCrypt hashing (via `spring-boot-starter-security`),
  JWT issued on login (stateless HS256, ~2h expiry) — simplest defensible choice for a separate-origin SPA,
  no session store or cookie/CORS complications.
- `SecurityConfig`: CSRF disabled (stateless API), CORS allows the Vite dev origin, session policy
  `STATELESS`. **Public:** register, login, all `GET /api/pokemon/**` (browsing works logged-out).
  **Protected:** all mutating Pokemon endpoints, via a `JwtAuthenticationFilter` before
  `UsernamePasswordAuthenticationFilter`.
- Demo credentials seeded via `V3__seed_demo_user.sql`, documented in the README.

### Global error handling

`@RestControllerAdvice` mapping `PokemonNotFoundException`→404, bean-validation failures→400,
`UserAlreadyExistsException`→409, `BadCredentialsException`→401, fallback→500 — all through one
`ApiErrorResponse { status, error, message, path, timestamp }` shape. Build this early; every controller
depends on it, and it's what makes the required 404/400 behavior actually consistent.

## 2. Domain model

**Pokemon** (primary entity) — id (reuse PokeAPI id), name, sprite_url, category (species genus),
weight, height, abilities_json/moves_json/stats_json/types_json (small display-only lists — JSON/TEXT
columns, not normalized join tables — not worth the migration churn here), description (species flavor
text), evolution_chain_json (flattened `{speciesId, name, minLevel}` list from `/evolution-chain/{id}`),
plus **proprietary** columns not in PokeAPI: `localized_name`, `region`, `tags` (nullable, only touched by
the edit flow). created_at/updated_at via JPA auditing.

**Critical design rule:** the sync use case must never overwrite `localized_name`/`region`/`tags` (or any
other local edit) on re-sync — cover this with a unit test. This is the concrete answer to "replication
layer must support proprietary fields."

**User** (secondary entity) — id, username/email (unique), password_hash (BCrypt), role (USER/ADMIN —
also a legitimate hook for `@PreAuthorize` if there's time), created_at.

## 3. PokeAPI integration

Sync strategy: **on-demand fetch-and-persist**, not eager full replication (PokeAPI has 1000+ entries).
`PokemonSyncService.syncPokemon(idOrName)` calls `GET /pokemon/{id}`, `GET /pokemon-species/{id}` (genus,
English flavor text, evolution chain URL), `GET /evolution-chain/{id}`, maps combined response to
`domain.model.Pokemon`, upserts preserving proprietary fields. A `syncByIds(List<Integer>)` bulk variant
powers the startup seeder (§7).

After first sync, **list/detail reads never call PokeAPI** — they read the local table. This satisfies
"replicates into local store" and makes the demo independent of PokeAPI uptime.

Caching (nice-to-have, do last if time remains): `@Cacheable` + Caffeine on `PokeApiClientAdapter`
methods, to avoid redundant upstream calls on repeated sync triggers — framed honestly as optional.

## 4. API surface

| Method | Path | Purpose | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | create account | public |
| POST | `/api/auth/login` | issue JWT | public |
| GET | `/api/pokemon?page=&size=` | paginated list (US-01) | public |
| GET | `/api/pokemon/{id}` | detail view (US-02) | public |
| POST | `/api/pokemon/sync/{idOrName}` | replicate from PokeAPI (US-03) | protected |
| POST | `/api/pokemon` | create local-only record (completes CRUD) | protected |
| PUT | `/api/pokemon/{id}` | update local record (US-04) | protected |
| DELETE | `/api/pokemon/{id}` | delete local record | protected |

Every response — success and error — uses consistent DTOs (`PokemonSummaryResponse`,
`PokemonDetailResponse`, `ApiErrorResponse`).

## 5. Testing strategy

- **Unit** (fast, no Spring context): `application.pokemon.*Service` and `application.auth.AuthService`
  with ports mocked via Mockito — this is where coverage should concentrate (pagination logic, the
  proprietary-field-preservation rule, validation paths).
- **Repository**: `@DataJpaTest` against real H2 + Flyway migrations (also validates the migrations
  themselves).
- **Controller**: `@WebMvcTest`/`MockMvc` asserting status codes (200/201/400/404/401) and the
  public/protected split (Spring Security Test).
- **PokeAPI client**: stub HTTP with WireMock, fed by the existing fixture
  `project-docs/json-examples/pokemon-response.json` — reuse it rather than hand-rolling new fixtures.
- **Frontend**: Vitest + React Testing Library for list/detail/form components; `msw` or `vi.mock` for
  API mocking.

## 6. Frontend architecture

Additions to `frontend/pokeapi-front` (currently pure create-vite scaffold — no router, state lib, HTTP
client, or test runner):

- **Router**: `react-router` — `/` (list), `/pokemon/:id` (detail), `/pokemon/:id/edit`, `/login`,
  `/register`.
- **HTTP client**: thin `apiClient.js` wrapping `fetch` — centralizes base URL, JSON parsing,
  `Authorization` header, error shape matching backend's `ApiErrorResponse`.
- **State/data**: **TanStack Query** for server state (caching, loading/error states, mutation +
  invalidation — e.g. edit → auto-refetch) — much less hand-rolled boilerplate than context+reducer for a
  CRUD-heavy app. A lightweight `AuthContext` for the one piece of client-only state (current user/token).
- **Test runner**: Vitest + Testing Library + jsdom; add missing `"test"` script to `package.json`.
- **Structure**:
  ```
  src/api/            apiClient.js, pokemonApi.js, authApi.js
  src/context/         AuthContext.jsx
  src/routes/           PokemonListPage, PokemonDetailPage, PokemonEditPage, LoginPage, RegisterPage
  src/components/        PokemonCard, PokemonList, Pagination, StatBar, EvolutionChain, SkillList,
                          PokemonForm, ProtectedRoute
  ```
- **First step, before any feature work**: strip create-vite boilerplate (counter, logos, marketing
  links) from `App.jsx`/`App.css`/`index.css`/`src/assets/*` — five minutes, directly serves the "clean,
  no console warnings" evaluation criterion.
- **Leave `frontend/package.json`** (the decoy stub one level above `pokeapi-front/`) untouched; note in
  the README that it's unused, to preempt reviewer confusion.

## 7. Seed data & demo credentials

Flyway seeds the demo user (`V3__seed_demo_user.sql`). Pokemon demo data comes from a
`CommandLineRunner` that calls `syncByIds(1..N)` on startup, guarded by "only if the pokemon table is
empty" — this dogfoods the real sync path on every fresh boot rather than hand-maintaining a large SQL
seed file. Pick N modestly (20–30) for fast first boot. Document in README: first boot needs network
access to PokeAPI; demo login is `demo@pokeapp.dev` / `Demo1234!` (or similar, exact values don't matter).

## 8. Docker

- **Backend** `Dockerfile`: multi-stage — `gradle:8-jdk25` builds `bootJar` with layer caching, then
  `eclipse-temurin:25-jre-alpine` runs the jar, exposes 8080.
- **Frontend** `Dockerfile`: multi-stage — `node:22-alpine` runs `npm ci && npm run build`, then
  `nginx:alpine` serves `dist/` with SPA fallback routing.
- **`docker-compose.yml`** at repo root ties both together; nginx reverse-proxies `/api` to the backend
  container (avoids CORS entirely in the containerized deployment). One `docker compose up` is the target
  grader experience.

## 9. Build order / milestones

**Phase 0 — Foundation**
1. Prune `build.gradle`; configure `application.yaml` (H2 file datasource + Flyway); verify clean boot.
2. Write `V1`/`V2` migrations; fix the stale `PokeapiBackApplicationTests` class name.
3. Frontend cleanup (strip boilerplate); add router, TanStack Query, Vitest+RTL, base folders.

**Phase 1 — PokeAPI integration + sync (US-03)**
4. `PokeApiClientPort`/`Adapter` + DTOs, tested against WireMock using the existing JSON fixture.
5. `domain.model.Pokemon` + mapper from raw PokeAPI DTOs; unit tests for mapping edge cases.
6. `PokemonRepositoryPort`/entity/adapter; `@DataJpaTest` coverage.
7. `PokemonSyncService` (unit-tested, mocked ports) incl. proprietary-field preservation rule; wire
   `POST /api/pokemon/sync/{id}` with MockMvc tests.

**Phase 2 — Read + CRUD (US-01, US-02, US-04, full CRUD)**
8. `PokemonQueryService` → `GET /api/pokemon` (paginated), `GET /api/pokemon/{id}`; 200/404 tests.
9. `PokemonEditService` → `POST`/`PUT`/`DELETE /api/pokemon`; validation + `GlobalExceptionHandler`;
   explicit tests for malformed-payload (400) and missing-id (404) cases.

**Phase 3 — Auth**
10. `User` domain/entity/repository; `AuthService` unit-tested; BCrypt hashing.
11. `SecurityConfig` filter chain + JWT provider/filter; `AuthController`; tests for 401 (no token) vs
    200 (valid token).
12. Wire security onto the Phase 2 mutating endpoints.

**Phase 4 — Frontend features**
13. `PokemonListPage` + pagination + `PokemonCard` (US-01) against the real backend.
14. `PokemonDetailPage` with stats/description/evolution chain (US-02).
15. Auth: `LoginPage`/`RegisterPage`, `AuthContext`, `ProtectedRoute`.
16. `PokemonEditPage`/`PokemonForm` wired to the protected update endpoint, surfacing 400/404 (US-04).
17. Frontend component tests for list/detail/form behavior.

**Phase 5 — Seed data, Docker, docs, polish**
18. `V3__seed_demo_user.sql`; `CommandLineRunner` startup seeder.
19. Backend + frontend Dockerfiles, `docker-compose.yml`; verify `docker compose up` works from a clean clone.
20. README: setup (local + Docker), demo credentials, endpoint table, architecture summary, note on the
    unused decoy `frontend/package.json`.
21. Final pass: console-warning check, full test suite run, architecture-boundary review, tidy commit
    history for presentation.

Phases 0–3 are backend-only and independently demoable. Phase 4 is the first full-stack demo. Phase 5 is
what makes it gradeable on a clean machine — **Definition of Done: `docker compose up` from a fresh clone
produces a working, pre-seeded, login-able app.**

## Files this plan touches most

- `backend/pokeapi-back/build.gradle` — dependency pruning
- `backend/pokeapi-back/src/main/resources/application.yaml` — datasource/Flyway config
- `backend/pokeapi-back/src/main/resources/db/migration/` — new V1/V2/V3 migrations
- `project-docs/json-examples/pokemon-response.json` — reused as the test fixture for PokeAPI mocking
- `frontend/pokeapi-front/src/App.jsx`, `package.json` — cleanup + new deps
- `README.md` (repo root) — setup docs, demo credentials, endpoint table
- New: `docker-compose.yml`, backend/frontend `Dockerfile`s

## Verification

After each phase, run `./gradlew test` (backend) and `npm run lint && npm run test` (frontend) from
their respective directories. End-to-end verification once Phase 5 lands: fresh clone →
`docker compose up` → open frontend, browse paginated list (US-01), open a detail page with evolution
chain (US-02), log in with demo credentials, edit a Pokemon's proprietary fields and confirm the change
persists (US-03/US-04), confirm a malformed edit returns 400 and an edit to a non-existent id returns 404,
check browser console for warnings.
