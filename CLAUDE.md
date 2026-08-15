# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Technical interview exercise: a RESTful API (Java/Spring Boot) that integrates with the external
[PokeAPI](https://pokeapi.co/docs/v2), replicates Pokémon data into a local relational store, allows
local edits to that data, and exposes it to a React frontend. Full requirements (functional, technical,
delivery, and evaluation criteria) live in `project-docs/requeriments.md` — read it before implementing
features, since it defines the required user stories, architecture constraints, and grading criteria.

**Current state:** both `backend/pokeapi-back` and `frontend/pokeapi-front` are unmodified framework
scaffolds (Spring Initializr output / `create-vite` React template). No API endpoints, entities, or
business logic exist yet.

## Repository layout

- `backend/pokeapi-back/` — Java 25 + Spring Boot 4.1 project (Gradle). All backend code goes here.
- `frontend/pokeapi-front/` — React 19 + Vite project (npm). All frontend code goes here.
- `frontend/package.json` — a separate, minimal placeholder package.json at the `frontend/` root; it is
  not the app's package.json and has no real scripts. Always run frontend commands from
  `frontend/pokeapi-front/`.
- `project-docs/requeriments.md` — the exercise brief; source of truth for required functionality and
  evaluation criteria.

## Backend (`backend/pokeapi-back`)

Build tool: Gradle wrapper. Run all commands from `backend/pokeapi-back/`.

```
./gradlew build          # compile + run tests + assemble
./gradlew bootRun         # run the app locally
./gradlew test            # run all tests (JUnit 5 / JUnit Platform)
./gradlew test --tests "com.bcastillo.pokeapiback.PokeapiBackApplicationTests"   # run a single test class
```

On Windows use `gradlew.bat` in place of `./gradlew`.

Notes on `build.gradle`:
- Base package is `com.bcastillo.pokeapiback`; entry point is `Main.java`.
- Java toolchain is pinned to version 25.
- The dependency set is the full, untrimmed Spring Initializr "everything" selection — it currently
  pulls in JPA, JDBC, R2DBC, MongoDB (reactive + blocking), RSocket, WebSocket, GraalVM native-image,
  OpenTelemetry/Zipkin, Security, and h2/sqlite drivers simultaneously. The requirements only call for a
  single relational datastore (H2 is wired up via `spring-boot-h2console` and the H2 runtime driver) —
  expect to prune unused starters as the real architecture takes shape rather than treating the current
  list as intentional.
- Flyway and `src/main/resources/db/migration` are present for schema migrations but currently empty —
  add migrations there rather than relying on Hibernate DDL auto-generation.
- Lombok is available (`compileOnly`/`annotationProcessor`).
- `spring-boot-starter-restdocs` + the `asciidoctor` task are wired up, so documented endpoints should
  produce generated snippets under `build/generated-snippets` when tests run.

## Frontend (`frontend/pokeapi-front`)

Package manager: npm. Run all commands from `frontend/pokeapi-front/`.

```
npm install
npm run dev        # Vite dev server with HMR
npm run build       # production build
npm run lint        # ESLint (flat config, eslint.config.js)
npm run preview     # preview a production build
```

Notes:
- ESLint flat config applies `js.configs.recommended`, `eslint-plugin-react-hooks`, and
  `eslint-plugin-react-refresh` (Vite variant) to `**/*.{js,jsx}`; `dist/` is ignored.
- No test runner is configured yet — the requirements call for adequate coverage, so a test setup
  (e.g. Vitest) will need to be added.
- No routing, state management, or API client is wired up yet — `App.jsx` is still the default Vite
  template.

## Target architecture (per requirements — not yet implemented)

These constraints come from `project-docs/requeriments.md` and should guide how the backend is built:

- **Clean Architecture** with a clear separation between API/controllers, a business logic layer, and a
  data access layer; the business logic layer must stay independent of both the API and persistence
  layers.
- **TDD** is the preferred workflow — write tests alongside/before implementation.
- Two persisted entities are required: Pokémon (primary entity, replicated from PokeAPI with additional
  proprietary fields such as localized name, region, or internal tags) and Users (for auth), each with a
  primary key and at least two descriptive attributes.
- Full CRUD on the local Pokémon data, with 404 for missing records and 400 for malformed payloads.
- Auxiliary auth API: user registration/login plus a public vs. protected route distinction.
- Caching of PokeAPI responses is a "nice to have."
- Delivery expects a Dockerfile for containerized execution and the app pre-seeded with demo
  data/credentials.
