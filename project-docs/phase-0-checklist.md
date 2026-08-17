# Phase 0 Checklist

Tracks every individual file modification from [phase-0-foundation-plan.md](phase-0-foundation-plan.md).
Check items off as they land.

## B1 — Prune `build.gradle`

Plugins:
- [x] Remove `id 'org.graalvm.buildtools.native' version '1.1.1'`
- [x] Remove `id 'org.asciidoctor.jvm.convert' version '4.0.5'`

Remove `implementation` lines:
- [x] `spring-boot-starter-data-jdbc`
- [x] `spring-boot-starter-jdbc`
- [x] `spring-boot-starter-data-mongodb`
- [x] `spring-boot-starter-data-mongodb-reactive`
- [x] `spring-boot-starter-data-r2dbc`
- [x] `spring-boot-starter-r2dbc`
- [x] `spring-boot-starter-opentelemetry`
- [x] `spring-boot-starter-rsocket`
- [x] `spring-boot-starter-webclient`
- [x] `spring-boot-starter-websocket`
- [x] `spring-boot-starter-zipkin`
- [x] `spring-security-messaging`
- [x] `spring-security-rsocket`

Remove `runtimeOnly` lines:
- [x] `io.micrometer:micrometer-registry-prometheus`
- [x] `io.r2dbc:r2dbc-h2`
- [x] `org.xerial:sqlite-jdbc`

Remove `testImplementation` lines:
- [x] `spring-boot-starter-data-jdbc-test`
- [x] `spring-boot-starter-data-mongodb-reactive-test`
- [x] `spring-boot-starter-data-mongodb-test`
- [x] `spring-boot-starter-data-r2dbc-test`
- [x] `spring-boot-starter-opentelemetry-test`
- [x] `spring-boot-starter-r2dbc-test`
- [x] `spring-boot-starter-restdocs`
- [x] `spring-boot-starter-rsocket-test`
- [x] `spring-boot-starter-webclient-test`
- [x] `spring-boot-starter-websocket-test`
- [x] `spring-boot-starter-zipkin-test`
- [x] `org.springframework.restdocs:spring-restdocs-mockmvc`

Remove build-script leftovers:
- [x] Remove `ext { set('snippetsDir', file("build/generated-snippets")) }` block
- [x] Remove `outputs.dir snippetsDir` from `tasks.named('test') { ... }` (keep `useJUnitPlatform()`)
- [x] Remove the whole `tasks.named('asciidoctor') { ... }` block

Add dependencies:
- [x] `testImplementation 'org.springframework.boot:spring-boot-starter-test'`
- [x] `implementation 'io.jsonwebtoken:jjwt-api:0.12.6'`
- [x] `runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'`
- [x] `runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'`
- [x] `testImplementation 'org.wiremock:wiremock-standalone:3.9.2'` (confirm current stable version)

Confirm untouched:
- [x] `h2console`, `actuator(+test)`, `data-jpa(+test)`, `flyway(+test)`, `restclient(+test)`,
      `security(+test)`, `validation(+test)`, `webmvc(+test)`, Lombok (4 lines), `devtools`, `h2` runtime,
      `junit-platform-launcher`, `spring-boot-configuration-processor`, `org.hibernate.orm` plugin,
      `hibernate { enhancement {} }` block — all still present after the edit

Verify:
- [x] `./gradlew dependencies` resolves cleanly, no version-conflict errors
- [x] `grep` confirms zero remaining references to mongodb/r2dbc/rsocket/websocket/sqlite/zipkin/
      opentelemetry/graalvm/asciidoctor/restdocs in `build.gradle`
- [x] `./gradlew build` compiles with the pruned + added dependency set

## B2 — Datasource + Flyway config

- [x] Edit `application.yaml`: add `spring.profiles.active: dev`
- [x] Edit `application.yaml`: add `spring.datasource` block (url, driver-class-name, username, password)
- [x] Edit `application.yaml`: add `spring.flyway` block (enabled, locations)
- [x] Edit `application.yaml`: add `server.port: 8080`
- [x] Create `application-dev.yaml` with `spring.h2.console.enabled: true` + `path: /h2-console`

Verify:
- [x] `./gradlew bootRun` connects to `jdbc:h2:file:./data/pokedb` with no datasource errors
- [x] Log confirms `dev` profile active
- [x] `data/pokedb.mv.db` file created on disk after first boot
- [x] `http://localhost:8080/h2-console` reachable with `dev` profile active (401 pre-Phase-3 — default
      Spring Security denies all paths until `SecurityConfig` lands; confirmed via the
      `H2ConsoleAutoConfiguration` log line instead of HTTP status)
- [x] H2 console disabled when `dev` profile is not active (found devtools was auto-enabling it
      regardless of profile; fixed by explicitly setting `spring.h2.console.enabled: false` in the base
      `application.yaml`)

## B3 — Flyway migrations

- [x] Create `db/migration/V1__create_pokemon_table.sql` (16-column `pokemon` table, `id` not
      auto-generated)
- [x] Create `db/migration/V2__create_user_table.sql` (`app_user` table — not `user`, reserved word)

Verify:
- [x] Boot log shows Flyway applying `V1__create_pokemon_table.sql` (success, no checksum errors)
- [x] Boot log shows Flyway applying `V2__create_user_table.sql` (success, no checksum errors)
- [x] H2 console: `POKEMON` table has all 16 columns from the spec, correct types
- [x] H2 console: `APP_USER` table has `id/email/password_hash/role/created_at`, `email` UNIQUE constraint
      enforced (insert duplicate email → error)
- [x] Re-running `bootRun` a second time does not re-apply or error on already-applied migrations

## B4 — Fix stale smoke test

- [x] Rename `PokeapiBackApplicationTests.java` → `MainTests.java`
- [x] Rename class `PokeapiBackApplicationTests` → `MainTests`
- [x] Confirm `contextLoads()` test body unchanged

Verify:
- [x] `./gradlew test --tests "com.bcastillo.pokeapiback.MainTests"` passes
- [x] `grep -r PokeapiBackApplicationTests` returns no matches anywhere in the repo

## F1 — Strip create-vite boilerplate

- [x] Rewrite `App.jsx`: remove counter/hero/logo/doc-links markup, replace with placeholder
- [x] Delete `src/assets/hero.png`
- [x] Delete `src/assets/react.svg`
- [x] Delete `src/assets/vite.svg`
- [x] Delete `public/icons.svg`
- [x] Clean dead marketing rules out of `App.css`
- [x] Clean dead marketing rules out of `index.css`

Verify:
- [x] `grep -r "hero.png\|react.svg\|vite.svg\|icons.svg"` in `src/` returns no matches
- [x] `npm run build` succeeds with no missing-asset import errors
- [x] `npm run dev` shows placeholder page, no 404s in network tab for deleted assets

## F2 — Add dependencies

- [x] `npm install react-router-dom`
- [x] `npm install @tanstack/react-query`
- [x] `npm install -D vitest`
- [x] `npm install -D @testing-library/react`
- [x] `npm install -D @testing-library/jest-dom`
- [x] `npm install -D @testing-library/user-event`
- [x] `npm install -D jsdom`
- [x] Add `"test": "vitest run"` script to `package.json`
- [x] Add `"test:watch": "vitest"` script to `package.json`

Verify:
- [x] `npm install` completes with no unresolved-peer-dependency errors
- [x] `package-lock.json` reflects all newly added packages
- [x] `npm run test` script resolves and invokes `vitest run` (even before F4's test file exists)

## F3 — Wire test runner + app shell

- [x] Edit `vite.config.js`: add `test` block (`environment: 'jsdom'`, `setupFiles`, `globals: true`)
- [x] Create `src/setupTests.js` (`import '@testing-library/jest-dom'`)
- [x] Edit `main.jsx`: wrap `<App />` in `BrowserRouter`
- [x] Edit `main.jsx`: wrap `<App />` in `QueryClientProvider` (`new QueryClient()`)
- [x] Confirm no `src/api/`, `src/context/`, `src/routes/`, `src/components/` folders created yet
      (deferred to Phase 1/4)

Verify:
- [x] `npm run dev` renders with `BrowserRouter`/`QueryClientProvider` wired, no runtime console errors
- [x] `npm run test` picks up `jsdom` environment with no config errors
- [x] `git status` shows no empty `src/api|context|routes|components` directories added

## F4 — Smoke test

- [x] Create `src/App.test.jsx`: render `<App />` wrapped in the same providers as `main.jsx`
- [x] Assert placeholder text is present

Verify:
- [x] `npm run test` passes `App.test.jsx`
- [x] Sanity check: temporarily remove the placeholder text, confirm the test fails (not vacuous), then
      restore it

## Overall Phase 0 Verification

- [x] `./gradlew build` passes (backend)
- [x] `./gradlew bootRun` logs show `V1` and `V2` applied by Flyway
- [x] App starts on port 8080 with no bean-wiring errors
- [x] `dev` profile: `http://localhost:8080/h2-console` reachable
- [x] H2 console: `POKEMON` table exists with expected columns
- [x] H2 console: `APP_USER` table exists with expected columns
- [x] `npm install` succeeds (frontend)
- [x] `npm run lint` clean
- [x] `npm run test` passes (smoke test)
- [x] `npm run dev` shows placeholder page, no console errors/warnings
