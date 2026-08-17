# Phase 1 Checklist

Tracks every individual file/task from
[phase-1-pokeapi-sync-plan.md](phase-1-pokeapi-sync-plan.md). Check items off as they land.

## T1 — PokeAPI DTOs + client adapter + WireMock test

- [x] Copy `project-docs/json-examples/pokemon-response.json` → `src/test/resources/wiremock/pokemon-response.json`
- [x] Create `src/test/resources/wiremock/pokemon-species-response.json` (genera, English flavor text,
      evolution_chain.url)
- [x] Create `src/test/resources/wiremock/evolution-chain-response.json` (2–3 stage chain)
- [x] Add `pokeapi.base-url` property to `application.yaml` (default `https://pokeapi.co/api/v2`)
- [x] Create `infrastructure/config/RestClientConfig.java` (`RestClient` bean, base URL from property)
- [x] Create `infrastructure/pokeapi/dto/PokeApiPokemonResponse.java`
- [x] Create `infrastructure/pokeapi/dto/PokeApiSpeciesResponse.java`
- [x] Create `infrastructure/pokeapi/dto/PokeApiEvolutionChainResponse.java`
- [x] Create `domain/exception/PokeApiResourceNotFoundException.java`
- [x] Create `domain/port/PokeApiClientPort.java` (`Pokemon fetchReplicaData(String idOrName)`)
- [x] Create `infrastructure/pokeapi/PokeApiClientAdapter.java` (implements the port; 3 HTTP calls; maps
      upstream 404 → `PokeApiResourceNotFoundException`)

Verify:
- [x] `PokeApiClientAdapterTest` (WireMock, all 3 endpoints stubbed from fixtures) — adapter returns a
      `Pokemon` with all replicated fields populated, proprietary fields null
- [x] Test covers the upstream-404 path → `PokeApiResourceNotFoundException` thrown
- [x] `./gradlew test --tests "*PokeApiClientAdapterTest"` passes (2/2)

## T2 — Domain model + response mapper unit tests

- [x] Create `domain/model/Pokemon.java`
- [x] Create `domain/model/StatValue.java`
- [x] Create `domain/model/EvolutionStage.java`
- [x] Create `infrastructure/pokeapi/mapper/PokeApiResponseMapper.java` (pure function: 3 raw DTOs → `Pokemon`)

Verify:
- [x] `PokeApiResponseMapperTest`: happy path against the 3 fixtures produces the expected `Pokemon`
- [x] Edge case test: missing English flavor text entry handled without throwing
- [x] Edge case test: species with no pre-evolution (chain root) handled without throwing
- [x] Edge case test: genus missing English translation handled without throwing
- [x] `./gradlew test --tests "*PokeApiResponseMapperTest"` passes (4/4). Built before T1 in practice —
      the adapter needs `Pokemon` + the mapper to compile — commits just landed in that order instead of
      the checklist's numbering.

## T3 — Persistence: entity, repository, adapter

- [x] Create `infrastructure/persistence/entity/PokemonEntity.java` (mirrors V1 columns exactly)
- [x] Create `infrastructure/persistence/repository/PokemonJpaRepository.java`
- [x] Create `infrastructure/persistence/mapper/PokemonEntityMapper.java` (domain ↔ entity, JSON
      (de)serialization for `abilities`/`moves`/`stats`/`types`/`evolutionChain`)
- [x] Create `domain/port/PokemonRepositoryPort.java` (`findById`, `save` only — pagination/delete deferred
      to Phase 2)
- [x] Create `infrastructure/persistence/PokemonRepositoryAdapter.java` (implements the port)

Verify:
- [x] `PokemonJpaRepositoryTest` (`@DataJpaTest`, real H2 + Flyway): save then reload, every column
      survives
- [x] JSON round-trip test: `abilities`/`moves`/`stats`/`types`/`evolutionChain` reload as equal Java
      objects, not just non-null strings (covered via `PokemonRepositoryAdapterTest`, which round-trips
      through the typed domain model rather than asserting on raw JSON text)
- [x] `PokemonRepositoryAdapterTest`: domain↔entity mapping correct both directions
- [x] `./gradlew test --tests "*Pokemon*Repository*"` passes (3/3). Found along the way: plain
      `repository.save()` doesn't flush, so `@CreationTimestamp`/`@UpdateTimestamp` aren't populated on
      the in-memory entity until `saveAndFlush()` runs — fixed in the test.

## T4 — Sync service + endpoint

- [x] Create `application/pokemon/PokemonSyncService.java` (`syncPokemon(idOrName)`: fetch replica → look
      up existing → merge proprietary fields → save)
- [x] Create `api/common/ApiErrorResponse.java`
- [x] Create `api/common/GlobalExceptionHandler.java` (`PokeApiResourceNotFoundException` → 404, fallback
      → 500)
- [x] Create `api/pokemon/dto/PokemonResponse.java` (full detail shape, reused by Phase 2's GET endpoint)
- [x] Create `api/pokemon/mapper/PokemonDtoMapper.java`
- [x] Create `api/pokemon/PokemonController.java` (`POST /api/pokemon/sync/{idOrName}`)

Verify:
- [x] `PokemonSyncServiceTest` (Mockito, both ports mocked) — new-pokemon case: saves with null
      proprietary fields
- [x] `PokemonSyncServiceTest` — re-sync case: existing `localizedName`/`region`/`tags` preserved,
      replicated fields updated to the new fetch
- [x] `PokemonControllerTest` (`@AutoConfigureMockMvc(addFilters = false)`, service mocked): 200 + correct
      `PokemonResponse` shape on success (also needed `@Import(PokemonDtoMapper.class)` — `@WebMvcTest`
      slices don't pick up plain `@Component` beans)
- [x] `PokemonControllerTest`: `PokeApiResourceNotFoundException` from the service → 404 via
      `GlobalExceptionHandler`
- [x] `./gradlew test --tests "*PokemonSyncServiceTest" --tests "*PokemonControllerTest"` passes (4/4)

## Overall Phase 1 Verification

- [x] `./gradlew build` passes (full suite, all new + existing tests green — 14/14 across the whole module)
- [x] `./gradlew bootRun`, then `curl -X POST http://localhost:8080/api/pokemon/sync/25` — confirmed 401.
      Also tried authenticating with the devtools-generated default user/password (which *does* work for
      `GET /actuator/health`) expecting to get further: still 401, because Spring Security's default
      filter chain runs CSRF checks before `BasicAuthenticationFilter` for POST requests, so an
      unauthenticated request gets rejected — and re-challenged with `WWW-Authenticate: Basic` — before
      credentials are ever evaluated. Confirms the endpoint is fully inert over HTTP pre-Phase-3, exactly
      as designed.
- [x] Real PokeAPI + real persistence, verified end-to-end via a throwaway `@SpringBootTest` (not
      committed — hits live network, doesn't belong in the permanent suite) that autowired
      `PokemonSyncService` directly, bypassing the HTTP/security layer: synced id 25 against the live
      PokeAPI (`name=pikachu weight=60`), confirmed a `pokemon` row exists with real data
- [x] Manually set proprietary fields (`localizedName="Pikachu-Local"`, `region="Kanto"`,
      `tags="starter,electric"`) via `Pokemon.withProprietaryFields(...)` + `repositoryPort.save(...)`,
      re-ran `syncPokemon("25")`: all three preserved on the second sync while `weight` (and the rest of
      the replicated fields) came fresh from PokeAPI again
- [x] `git status` on `frontend/` — clean; every Phase 1 commit touched `backend/` only
