# Phase 1 Checklist

Tracks every individual file/task from
[phase-1-pokeapi-sync-plan.md](phase-1-pokeapi-sync-plan.md). Check items off as they land.

## T1 — PokeAPI DTOs + client adapter + WireMock test

- [ ] Copy `project-docs/json-examples/pokemon-response.json` → `src/test/resources/wiremock/pokemon-response.json`
- [ ] Create `src/test/resources/wiremock/pokemon-species-response.json` (genera, English flavor text,
      evolution_chain.url)
- [ ] Create `src/test/resources/wiremock/evolution-chain-response.json` (2–3 stage chain)
- [ ] Add `pokeapi.base-url` property to `application.yaml` (default `https://pokeapi.co/api/v2`)
- [ ] Create `infrastructure/config/RestClientConfig.java` (`RestClient` bean, base URL from property)
- [ ] Create `infrastructure/pokeapi/dto/PokeApiPokemonResponse.java`
- [ ] Create `infrastructure/pokeapi/dto/PokeApiSpeciesResponse.java`
- [ ] Create `infrastructure/pokeapi/dto/PokeApiEvolutionChainResponse.java`
- [ ] Create `domain/exception/PokeApiResourceNotFoundException.java`
- [ ] Create `domain/port/PokeApiClientPort.java` (`Pokemon fetchReplicaData(String idOrName)`)
- [ ] Create `infrastructure/pokeapi/PokeApiClientAdapter.java` (implements the port; 3 HTTP calls; maps
      upstream 404 → `PokeApiResourceNotFoundException`)

Verify:
- [ ] `PokeApiClientAdapterTest` (WireMock, all 3 endpoints stubbed from fixtures) — adapter returns a
      `Pokemon` with all replicated fields populated, proprietary fields null
- [ ] Test covers the upstream-404 path → `PokeApiResourceNotFoundException` thrown
- [ ] `./gradlew test --tests "*PokeApiClientAdapterTest"` passes

## T2 — Domain model + response mapper unit tests

- [ ] Create `domain/model/Pokemon.java`
- [ ] Create `domain/model/StatValue.java`
- [ ] Create `domain/model/EvolutionStage.java`
- [ ] Create `infrastructure/pokeapi/mapper/PokeApiResponseMapper.java` (pure function: 3 raw DTOs → `Pokemon`)

Verify:
- [ ] `PokeApiResponseMapperTest`: happy path against the 3 fixtures produces the expected `Pokemon`
- [ ] Edge case test: missing English flavor text entry handled without throwing
- [ ] Edge case test: species with no pre-evolution (chain root) handled without throwing
- [ ] Edge case test: genus missing English translation handled without throwing
- [ ] `./gradlew test --tests "*PokeApiResponseMapperTest"` passes

## T3 — Persistence: entity, repository, adapter

- [ ] Create `infrastructure/persistence/entity/PokemonEntity.java` (mirrors V1 columns exactly)
- [ ] Create `infrastructure/persistence/repository/PokemonJpaRepository.java`
- [ ] Create `infrastructure/persistence/mapper/PokemonEntityMapper.java` (domain ↔ entity, JSON
      (de)serialization for `abilities`/`moves`/`stats`/`types`/`evolutionChain`)
- [ ] Create `domain/port/PokemonRepositoryPort.java` (`findById`, `save` only — pagination/delete deferred
      to Phase 2)
- [ ] Create `infrastructure/persistence/PokemonRepositoryAdapter.java` (implements the port)

Verify:
- [ ] `PokemonJpaRepositoryTest` (`@DataJpaTest`, real H2 + Flyway): save then reload, every column
      survives
- [ ] JSON round-trip test: `abilities`/`moves`/`stats`/`types`/`evolutionChain` reload as equal Java
      objects, not just non-null strings
- [ ] `PokemonRepositoryAdapterTest`: domain↔entity mapping correct both directions
- [ ] `./gradlew test --tests "*Pokemon*Repository*"` passes

## T4 — Sync service + endpoint

- [ ] Create `application/pokemon/PokemonSyncService.java` (`syncPokemon(idOrName)`: fetch replica → look
      up existing → merge proprietary fields → save)
- [ ] Create `api/common/ApiErrorResponse.java`
- [ ] Create `api/common/GlobalExceptionHandler.java` (`PokeApiResourceNotFoundException` → 404, fallback
      → 500)
- [ ] Create `api/pokemon/dto/PokemonResponse.java` (full detail shape, reused by Phase 2's GET endpoint)
- [ ] Create `api/pokemon/mapper/PokemonDtoMapper.java`
- [ ] Create `api/pokemon/PokemonController.java` (`POST /api/pokemon/sync/{idOrName}`)

Verify:
- [ ] `PokemonSyncServiceTest` (Mockito, both ports mocked) — new-pokemon case: saves with null
      proprietary fields
- [ ] `PokemonSyncServiceTest` — re-sync case: existing `localizedName`/`region`/`tags` preserved,
      replicated fields updated to the new fetch
- [ ] `PokemonControllerTest` (`@AutoConfigureMockMvc(addFilters = false)`, service mocked): 200 + correct
      `PokemonResponse` shape on success
- [ ] `PokemonControllerTest`: `PokeApiResourceNotFoundException` from the service → 404 via
      `GlobalExceptionHandler`
- [ ] `./gradlew test --tests "*PokemonSyncServiceTest" --tests "*PokemonControllerTest"` passes

## Overall Phase 1 Verification

- [ ] `./gradlew build` passes (full suite, all new + existing tests green)
- [ ] `./gradlew bootRun`, then `curl -X POST http://localhost:8080/api/pokemon/sync/25` — confirms
      routing/wiring reaches the controller (401 expected pre-Phase-3; final behavior verified via the
      MockMvc test instead)
- [ ] H2 console: `pokemon` row for id 25 exists with real PokeAPI data after a sync
- [ ] Manually edit `localized_name` on that row in H2 console, re-sync id 25 again, confirm
      `localized_name` unchanged while `weight`/`stats_json`/etc. reflect the fresh fetch
- [ ] `npm run lint` / frontend untouched this phase — confirm no accidental frontend changes crept in
      (`git status` on `frontend/`)
