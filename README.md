# Pokedex

A full-stack Pokemon technical-interview exercise: a Spring Boot API that integrates with the external
[PokeAPI](https://pokeapi.co/docs/v2), replicates Pokemon data into a local relational store, allows
local edits to that data, and exposes it to a React frontend.

## User stories

1. **Pokemon enumeration** — browse a paginated list, each entry showing sprite, category, mass, and
   abilities/moves.
2. **Detailed view** — comprehensive data for a chosen Pokemon: image, stats, description, and
   evolutionary lineage.
3. **Data synchronization** — pull a Pokemon from PokeAPI and persist it locally, extended with
   proprietary fields (localized name, region, tags) PokeAPI doesn't have.
4. **Local data modification** — edit or delete any locally stored Pokemon.

Auth (registration/login, public vs. protected routes) is an auxiliary requirement supporting stories 3
and 4.

## Architecture

**Backend** (`backend/pokeapi-back`, Java 25 + Spring Boot 4.1, Gradle): Clean Architecture in four
layers —

```
domain/            framework-free: model/ (plain records), exception/, port/ (interfaces)
application/         use-case services, depend only on domain
infrastructure/       implements domain ports: persistence/, pokeapi/, security/, config/
api/                   controllers, request/response DTOs, per-feature *DtoMapper
```

`domain` never imports `api.*` or `infrastructure.*`; controllers never touch JPA entities or raw PokeAPI
DTOs directly, always through a port/service and its own DTOs. Persistence is Spring Data JPA + H2
(file-based for local dev, in-memory for tests), schema managed by Flyway migrations. Auth is stateless
JWT (HS256) via a `JwtAuthenticationFilter` ahead of Spring Security's chain.

**Frontend** (`frontend/pokeapi-front`, React 19 + Vite): `react-router-dom` for routing, TanStack Query
for server state (caching, loading/error states, mutation + cache invalidation), a small `AuthContext` for
the one piece of client-only state (JWT + email), and a thin `apiClient.js` wrapping `fetch`.

See [project-docs/development-plan.md](project-docs/development-plan.md) for the full phase-by-phase
build plan, and `project-docs/phase-{0..5}-*.md` for the plan + as-built checklist of each phase.

## Local setup

### Backend

From `backend/pokeapi-back/` (Windows: use `gradlew.bat` in place of `./gradlew`):

```bash
./gradlew bootRun
```

Runs on `http://localhost:8080` with the `dev` profile (file-based H2 at `backend/pokeapi-back/data/`,
H2 console at `/h2-console`). On first boot with an empty database it seeds a demo user and 20 Pokemon
from PokeAPI — see [Demo credentials](#demo-credentials) below. First boot needs network access to
PokeAPI.

```bash
./gradlew test                                                # full suite
./gradlew test --tests "*PokemonSyncServiceTest"                # single test class
```

### Frontend

From `frontend/pokeapi-front/`:

```bash
npm install
npm run dev            # Vite dev server on http://localhost:5173, proxies API calls to :8080
npm run lint
npm run test            # Vitest
npm run build
```

> **Note:** `frontend/package.json` (one directory above `pokeapi-front/`) is an unused decoy left by the
> original project scaffold — it has no real scripts. Always run frontend commands from
> `frontend/pokeapi-front/`.

## Docker setup

From the repo root:

```bash
docker compose up --build
```

Builds and starts both services: backend on `http://localhost:8080`, frontend on
`http://localhost:5173`. The frontend's nginx container reverse-proxies `/api/` to the backend container,
so the browser only ever talks to one origin — no CORS configuration needed in this deployment mode. The
backend's H2 data directory is a named Docker volume (`pokedb-data`), so seeded/edited data survives
container restarts; the seeder only runs against an empty Pokemon table, so it won't re-sync or duplicate
data on a restart. To start over from a clean slate:

```bash
docker compose down -v
docker compose up --build
```

## Demo credentials

Seeded automatically on first boot (both local `bootRun` and Docker, whenever the Pokemon table is
empty):

```
email:    demo@pokeapp.dev
password: Demo1234!
```

## API endpoints

| Method | Path | Purpose | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | create account | public |
| POST | `/api/auth/login` | issue JWT | public |
| GET | `/api/pokemon?page=&size=` | paginated list (US-01) | public |
| GET | `/api/pokemon/{id}` | detail view (US-02) | public |
| POST | `/api/pokemon/sync/{idOrName}` | replicate from PokeAPI (US-03) | protected |
| POST | `/api/pokemon` | create local-only record | protected |
| PUT | `/api/pokemon/{id}` | update local record (US-04) | protected |
| DELETE | `/api/pokemon/{id}` | delete local record | protected |
| GET | `/actuator/health` | health check (used by the Docker healthcheck) | public |

Protected routes require `Authorization: Bearer <token>`. Every response — success and error — uses
consistent DTOs; errors follow `{ status, error, message, path, timestamp }` (404 for missing records, 400
for malformed payloads, 409 for conflicts, 401 for auth failures).

## Repository layout

- `backend/pokeapi-back/` — Java 25 + Spring Boot 4.1 project (Gradle). All backend code goes here.
- `frontend/pokeapi-front/` — React 19 + Vite project (npm). All frontend code goes here.
- `project-docs/` — the exercise brief, PokeAPI spec reference, and the phase-by-phase development
  plan/checklist pairs written before and checked off after each phase of the build.
