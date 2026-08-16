# Phase 0 Implementation Plan: Foundation

## Context

[project-docs/development-plan.md](../../../git/poke-project/project-docs/development-plan.md) lays out
the full build in 6 phases. Phase 0 ("Foundation") is the prerequisite for everything else: the backend's
`build.gradle` currently has a self-contradictory "everything" dependency set (blocking JPA/JDBC *and*
reactive R2DBC/Mongo/RSocket *and* three DB drivers, with no chosen persistence stack), there's no
datasource or Flyway config, and no schema exists. The frontend is still the untouched `create-vite`
scaffold with no router, no data-fetching library, and no test runner.

This plan breaks Phase 0 into concrete, checkable tasks so it can be executed and reviewed as a single
unit before Phase 1 (PokeAPI sync) starts. Read against the actual current file contents (`build.gradle`,
`application.yaml`, the existing test class, `App.jsx`, `package.json`) so every task is a precise diff,
not a restatement of the high-level plan.

**Definition of done for Phase 0**: `./gradlew build` passes with the pruned dependency set, the app boots
and Flyway applies both migrations against a file-based H2 database, and `npm run dev`/`lint`/`test` all
pass on the frontend with the create-vite boilerplate replaced by a minimal routed/query-provider shell.

## Backend tasks

### B1 — Prune `build.gradle`

File: `backend/pokeapi-back/build.gradle`

Remove plugins:
- `org.graalvm.buildtools.native` (no native-image delivery target — Docker is the delivery mechanism)
- `org.asciidoctor.jvm.convert` (dropping restdocs, see below)

Remove dependencies (conflicting/unused stacks):
- `spring-boot-starter-data-jdbc`, `spring-boot-starter-jdbc` (data-jpa pulls JDBC transitively; explicit
  JDBC starter is redundant once JPA is the chosen stack)
- `spring-boot-starter-data-mongodb`, `spring-boot-starter-data-mongodb-reactive` (+ their `-test` starters)
- `spring-boot-starter-data-r2dbc`, `spring-boot-starter-r2dbc`, `io.r2dbc:r2dbc-h2` (+ `-test` starter)
- `org.xerial:sqlite-jdbc` (H2 is the chosen driver)
- `spring-boot-starter-rsocket`, `spring-security-rsocket` (+ `-test` starter)
- `spring-boot-starter-websocket` (+ `-test` starter)
- `spring-security-messaging` (only relevant with STOMP/websocket)
- `spring-boot-starter-opentelemetry`, `spring-boot-starter-zipkin`, `io.micrometer:micrometer-registry-prometheus`
  (+ `-test` starters — observability polish out of scope for this exercise)
- `spring-boot-starter-webclient` (+ `-test` starter — reactive client; blocking `RestClient` is enough)
- `spring-boot-starter-restdocs`, `org.springframework.restdocs:spring-restdocs-mockmvc` (a hand-written
  endpoint table in the README covers this cheaper than wiring generated snippets)

Remove build-script leftovers tied to the removed restdocs/asciidoctor setup:
- the `ext { set('snippetsDir', ...) }` block
- `outputs.dir snippetsDir` inside `tasks.named('test') { ... }` (keep `useJUnitPlatform()`)
- the whole `tasks.named('asciidoctor') { ... }` block

Keep as-is: `h2console`, `actuator` (+ `-test`), `data-jpa` (+ `-test`), `flyway` (+ `-test`),
`restclient` (+ `-test`), `security` (+ `-test`), `validation` (+ `-test`), `webmvc` (+ `-test`), Lombok
(all 4 lines), `devtools`, `h2` runtime, `junit-platform-launcher`, `spring-boot-configuration-processor`,
the `org.hibernate.orm` plugin, and the `hibernate { enhancement {} }` block.

Add:
- `testImplementation 'org.springframework.boot:spring-boot-starter-test'` — the base test starter is
  currently missing entirely; several of the `-test` starters being kept assume it's present
- `implementation 'io.jsonwebtoken:jjwt-api:0.12.6'`, `runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'`,
  `runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'` — JWT for Phase 3 auth (added now so the dependency
  pruning pass is one clean commit; confirm current stable version at implementation time)
- `testImplementation 'org.wiremock:wiremock-standalone:3.9.2'` — stubs PokeAPI HTTP calls in Phase 1
  tests (confirm current stable version at implementation time)

Net result: ~40 dependency lines with 3 competing persistence stacks down to ~17, every line traceable to
a requirement.

### B2 — Configure datasource + Flyway

File: `backend/pokeapi-back/src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: pokeapi-back
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:file:./data/pokedb;AUTO_SERVER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  flyway:
    enabled: true
    locations: classpath:db/migration
server:
  port: 8080
```

New file: `backend/pokeapi-back/src/main/resources/application-dev.yaml` — gates the H2 console to the
`dev` profile rather than enabling it unconditionally:

```yaml
spring:
  h2:
    console:
      enabled: true
      path: /h2-console
```

File-based H2 (`jdbc:h2:file:...`, not in-memory) so data survives app restarts between local runs.

### B3 — Flyway migrations

New files under `backend/pokeapi-back/src/main/resources/db/migration/` (currently empty):

`V1__create_pokemon_table.sql`:
```sql
CREATE TABLE pokemon (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    sprite_url VARCHAR(500),
    category VARCHAR(100),
    weight INT,
    height INT,
    abilities_json CLOB,
    moves_json CLOB,
    stats_json CLOB,
    types_json CLOB,
    description CLOB,
    evolution_chain_json CLOB,
    localized_name VARCHAR(100),
    region VARCHAR(100),
    tags VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
`id` is not auto-generated — it reuses the PokeAPI id, per the domain model in the main plan.

`V2__create_user_table.sql`:
```sql
CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```
Table named `app_user`, not `user` — `USER` is a reserved word in H2 (and most SQL dialects), and this
sidesteps quoting headaches in every future query.

### B4 — Fix the stale smoke test

Rename `backend/pokeapi-back/src/test/java/com/bcastillo/pokeapiback/PokeapiBackApplicationTests.java` →
`MainTests.java`, class `PokeapiBackApplicationTests` → `MainTests`, keep the single `contextLoads()`
test as-is. It stays useful post-rename: with B2/B3 in place, `contextLoads()` now also verifies the full
Spring context boots with the real datasource and both Flyway migrations applying cleanly.

## Frontend tasks

### F1 — Strip create-vite boilerplate

File: `frontend/pokeapi-front/src/App.jsx` — replace the counter/hero/logo/doc-links markup entirely
with a minimal placeholder (e.g. a heading), wired to the providers added in F3.

Delete: `src/assets/hero.png`, `src/assets/react.svg`, `src/assets/vite.svg`, `public/icons.svg` (only
consumer is the boilerplate being deleted). Strip the now-dead marketing rules out of `App.css` and
`index.css`, keeping only any base reset/layout rules actually still referenced.

### F2 — Add dependencies

File: `frontend/pokeapi-front/package.json`

Runtime: `react-router-dom`, `@tanstack/react-query`.
Dev: `vitest`, `@testing-library/react`, `@testing-library/jest-dom`, `@testing-library/user-event`, `jsdom`.

Add scripts:
```json
"test": "vitest run",
"test:watch": "vitest"
```

### F3 — Wire test runner + minimal app shell

File: `frontend/pokeapi-front/vite.config.js` — add a `test` block (`environment: 'jsdom'`,
`setupFiles: './src/setupTests.js'`, `globals: true`).

New file: `frontend/pokeapi-front/src/setupTests.js` — `import '@testing-library/jest-dom'`.

File: `frontend/pokeapi-front/src/main.jsx` — wrap `<App />` in `BrowserRouter` (react-router-dom) and
`QueryClientProvider` (TanStack Query, `new QueryClient()`).

Deliberately **not** scaffolding empty `src/api/`, `src/context/`, `src/routes/`, `src/components/`
folders yet — those get created in Phase 1/4 when the files that belong in them actually exist, avoiding
dead placeholder directories in the diff.

### F4 — Smoke test

New file: `frontend/pokeapi-front/src/App.test.jsx` — renders `<App />` (wrapped in the same providers as
`main.jsx`) and asserts the placeholder text is present. Gives the new Vitest setup something real to run
and proves the router/query-provider wiring doesn't throw.

## Verification

**Backend**, from `backend/pokeapi-back/`:
```bash
./gradlew build
```
Then `./gradlew bootRun` and confirm in the logs: Flyway reports both `V1` and `V2` applied, the app
starts on port 8080 with no dependency/bean-wiring errors. With `dev` profile active, open
`http://localhost:8080/h2-console`, connect to `jdbc:h2:file:./data/pokedb`, and confirm `POKEMON` and
`APP_USER` tables exist with the expected columns.

**Frontend**, from `frontend/pokeapi-front/`:
```bash
npm install
npm run lint
npm run test
npm run dev
```
Confirm lint is clean, the smoke test passes, and the dev server shows the placeholder page with no
console errors/warnings.

## Files this phase touches

- `backend/pokeapi-back/build.gradle`
- `backend/pokeapi-back/src/main/resources/application.yaml` (edit), `application-dev.yaml` (new)
- `backend/pokeapi-back/src/main/resources/db/migration/V1__create_pokemon_table.sql` (new)
- `backend/pokeapi-back/src/main/resources/db/migration/V2__create_user_table.sql` (new)
- `backend/pokeapi-back/src/test/java/com/bcastillo/pokeapiback/MainTests.java` (renamed from
  `PokeapiBackApplicationTests.java`)
- `frontend/pokeapi-front/src/App.jsx`, `App.css`, `index.css`, `main.jsx`
- `frontend/pokeapi-front/src/assets/*`, `frontend/pokeapi-front/public/icons.svg` (deleted)
- `frontend/pokeapi-front/package.json`, `vite.config.js`
- `frontend/pokeapi-front/src/setupTests.js` (new), `src/App.test.jsx` (new)
