# Phase 0 Checklist

Tracks every individual file modification from [phase-0-foundation-plan.md](phase-0-foundation-plan.md).
Check items off as they land.

## B1 — Prune `build.gradle`

Plugins:
- [ ] Remove `id 'org.graalvm.buildtools.native' version '1.1.1'`
- [ ] Remove `id 'org.asciidoctor.jvm.convert' version '4.0.5'`

Remove `implementation` lines:
- [ ] `spring-boot-starter-data-jdbc`
- [ ] `spring-boot-starter-jdbc`
- [ ] `spring-boot-starter-data-mongodb`
- [ ] `spring-boot-starter-data-mongodb-reactive`
- [ ] `spring-boot-starter-data-r2dbc`
- [ ] `spring-boot-starter-r2dbc`
- [ ] `spring-boot-starter-opentelemetry`
- [ ] `spring-boot-starter-rsocket`
- [ ] `spring-boot-starter-webclient`
- [ ] `spring-boot-starter-websocket`
- [ ] `spring-boot-starter-zipkin`
- [ ] `spring-security-messaging`
- [ ] `spring-security-rsocket`

Remove `runtimeOnly` lines:
- [ ] `io.micrometer:micrometer-registry-prometheus`
- [ ] `io.r2dbc:r2dbc-h2`
- [ ] `org.xerial:sqlite-jdbc`

Remove `testImplementation` lines:
- [ ] `spring-boot-starter-data-jdbc-test`
- [ ] `spring-boot-starter-data-mongodb-reactive-test`
- [ ] `spring-boot-starter-data-mongodb-test`
- [ ] `spring-boot-starter-data-r2dbc-test`
- [ ] `spring-boot-starter-opentelemetry-test`
- [ ] `spring-boot-starter-r2dbc-test`
- [ ] `spring-boot-starter-restdocs`
- [ ] `spring-boot-starter-rsocket-test`
- [ ] `spring-boot-starter-webclient-test`
- [ ] `spring-boot-starter-websocket-test`
- [ ] `spring-boot-starter-zipkin-test`
- [ ] `org.springframework.restdocs:spring-restdocs-mockmvc`

Remove build-script leftovers:
- [ ] Remove `ext { set('snippetsDir', file("build/generated-snippets")) }` block
- [ ] Remove `outputs.dir snippetsDir` from `tasks.named('test') { ... }` (keep `useJUnitPlatform()`)
- [ ] Remove the whole `tasks.named('asciidoctor') { ... }` block

Add dependencies:
- [ ] `testImplementation 'org.springframework.boot:spring-boot-starter-test'`
- [ ] `implementation 'io.jsonwebtoken:jjwt-api:0.12.6'`
- [ ] `runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.12.6'`
- [ ] `runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.12.6'`
- [ ] `testImplementation 'org.wiremock:wiremock-standalone:3.9.2'` (confirm current stable version)

Confirm untouched:
- [ ] `h2console`, `actuator(+test)`, `data-jpa(+test)`, `flyway(+test)`, `restclient(+test)`,
      `security(+test)`, `validation(+test)`, `webmvc(+test)`, Lombok (4 lines), `devtools`, `h2` runtime,
      `junit-platform-launcher`, `spring-boot-configuration-processor`, `org.hibernate.orm` plugin,
      `hibernate { enhancement {} }` block — all still present after the edit

Verify:
- [ ] `./gradlew dependencies` resolves cleanly, no version-conflict errors
- [ ] `grep` confirms zero remaining references to mongodb/r2dbc/rsocket/websocket/sqlite/zipkin/
      opentelemetry/graalvm/asciidoctor/restdocs in `build.gradle`
- [ ] `./gradlew build` compiles with the pruned + added dependency set

## B2 — Datasource + Flyway config

- [ ] Edit `application.yaml`: add `spring.profiles.active: dev`
- [ ] Edit `application.yaml`: add `spring.datasource` block (url, driver-class-name, username, password)
- [ ] Edit `application.yaml`: add `spring.flyway` block (enabled, locations)
- [ ] Edit `application.yaml`: add `server.port: 8080`
- [ ] Create `application-dev.yaml` with `spring.h2.console.enabled: true` + `path: /h2-console`

Verify:
- [ ] `./gradlew bootRun` connects to `jdbc:h2:file:./data/pokedb` with no datasource errors
- [ ] Log confirms `dev` profile active
- [ ] `data/pokedb.mv.db` file created on disk after first boot
- [ ] `http://localhost:8080/h2-console` reachable with `dev` profile active
- [ ] H2 console unreachable (404/disabled) when `dev` profile is not active

## B3 — Flyway migrations

- [ ] Create `db/migration/V1__create_pokemon_table.sql` (16-column `pokemon` table, `id` not
      auto-generated)
- [ ] Create `db/migration/V2__create_user_table.sql` (`app_user` table — not `user`, reserved word)

Verify:
- [ ] Boot log shows Flyway applying `V1__create_pokemon_table.sql` (success, no checksum errors)
- [ ] Boot log shows Flyway applying `V2__create_user_table.sql` (success, no checksum errors)
- [ ] H2 console: `POKEMON` table has all 16 columns from the spec, correct types
- [ ] H2 console: `APP_USER` table has `id/email/password_hash/role/created_at`, `email` UNIQUE constraint
      enforced (insert duplicate email → error)
- [ ] Re-running `bootRun` a second time does not re-apply or error on already-applied migrations

## B4 — Fix stale smoke test

- [ ] Rename `PokeapiBackApplicationTests.java` → `MainTests.java`
- [ ] Rename class `PokeapiBackApplicationTests` → `MainTests`
- [ ] Confirm `contextLoads()` test body unchanged

Verify:
- [ ] `./gradlew test --tests "com.bcastillo.pokeapiback.MainTests"` passes
- [ ] `grep -r PokeapiBackApplicationTests` returns no matches anywhere in the repo

## F1 — Strip create-vite boilerplate

- [ ] Rewrite `App.jsx`: remove counter/hero/logo/doc-links markup, replace with placeholder
- [ ] Delete `src/assets/hero.png`
- [ ] Delete `src/assets/react.svg`
- [ ] Delete `src/assets/vite.svg`
- [ ] Delete `public/icons.svg`
- [ ] Clean dead marketing rules out of `App.css`
- [ ] Clean dead marketing rules out of `index.css`

Verify:
- [ ] `grep -r "hero.png\|react.svg\|vite.svg\|icons.svg"` in `src/` returns no matches
- [ ] `npm run build` succeeds with no missing-asset import errors
- [ ] `npm run dev` shows placeholder page, no 404s in network tab for deleted assets

## F2 — Add dependencies

- [ ] `npm install react-router-dom`
- [ ] `npm install @tanstack/react-query`
- [ ] `npm install -D vitest`
- [ ] `npm install -D @testing-library/react`
- [ ] `npm install -D @testing-library/jest-dom`
- [ ] `npm install -D @testing-library/user-event`
- [ ] `npm install -D jsdom`
- [ ] Add `"test": "vitest run"` script to `package.json`
- [ ] Add `"test:watch": "vitest"` script to `package.json`

Verify:
- [ ] `npm install` completes with no unresolved-peer-dependency errors
- [ ] `package-lock.json` reflects all newly added packages
- [ ] `npm run test` script resolves and invokes `vitest run` (even before F4's test file exists)

## F3 — Wire test runner + app shell

- [ ] Edit `vite.config.js`: add `test` block (`environment: 'jsdom'`, `setupFiles`, `globals: true`)
- [ ] Create `src/setupTests.js` (`import '@testing-library/jest-dom'`)
- [ ] Edit `main.jsx`: wrap `<App />` in `BrowserRouter`
- [ ] Edit `main.jsx`: wrap `<App />` in `QueryClientProvider` (`new QueryClient()`)
- [ ] Confirm no `src/api/`, `src/context/`, `src/routes/`, `src/components/` folders created yet
      (deferred to Phase 1/4)

Verify:
- [ ] `npm run dev` renders with `BrowserRouter`/`QueryClientProvider` wired, no runtime console errors
- [ ] `npm run test` picks up `jsdom` environment with no config errors
- [ ] `git status` shows no empty `src/api|context|routes|components` directories added

## F4 — Smoke test

- [ ] Create `src/App.test.jsx`: render `<App />` wrapped in the same providers as `main.jsx`
- [ ] Assert placeholder text is present

Verify:
- [ ] `npm run test` passes `App.test.jsx`
- [ ] Sanity check: temporarily remove the placeholder text, confirm the test fails (not vacuous), then
      restore it

## Overall Phase 0 Verification

- [ ] `./gradlew build` passes (backend)
- [ ] `./gradlew bootRun` logs show `V1` and `V2` applied by Flyway
- [ ] App starts on port 8080 with no bean-wiring errors
- [ ] `dev` profile: `http://localhost:8080/h2-console` reachable
- [ ] H2 console: `POKEMON` table exists with expected columns
- [ ] H2 console: `APP_USER` table exists with expected columns
- [ ] `npm install` succeeds (frontend)
- [ ] `npm run lint` clean
- [ ] `npm run test` passes (smoke test)
- [ ] `npm run dev` shows placeholder page, no console errors/warnings
