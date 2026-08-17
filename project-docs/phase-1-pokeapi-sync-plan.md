# Phase 1 Implementation Plan: PokeAPI Integration + Sync (US-03)

## Context

Phase 0 gave the backend a pruned dependency set, a working H2+Flyway datasource, and empty `pokemon`/
`app_user` tables. Phase 1 is the first real business logic: fetch a Pokemon from PokeAPI, map it into
the local schema, persist it, and make re-sync never clobber the proprietary fields a user has edited
locally. This is the foundation US-01/US-02 (Phase 2) read from and US-04 edits against.

Per [development-plan.md](development-plan.md) §1/§3/§9 (Phase 1, steps 4–7), this phase delivers:
`PokeApiClientPort`/`Adapter`, the `domain.model.Pokemon` + mapper, `PokemonRepositoryPort`/entity/
adapter, and `PokemonSyncService` wired behind `POST /api/pokemon/sync/{idOrName}`.

**Definition of done**: `POST /api/pokemon/sync/25` against a running app fetches pikachu from PokeAPI,
persists it into the `pokemon` table, and calling it again after manually editing `localized_name`
in H2 console leaves that edit untouched.

## Design decisions

**Port return type.** `domain.port.PokeApiClientPort` must not leak PokeAPI's raw JSON shape outside
`infrastructure` — that would break the "business logic independent of both layers" claim. So the port
returns a domain-owned aggregate, not the raw DTOs:

```java
// domain/port/PokeApiClientPort.java
public interface PokeApiClientPort {
    Pokemon fetchReplicaData(String idOrName); // throws PokeApiResourceNotFoundException
}
```

`fetchReplicaData` returns a `domain.model.Pokemon` with every *replicated* field populated
(id/name/sprite/category/weight/height/abilities/moves/stats/types/description/evolutionChain) and every
*proprietary* field (`localizedName`/`region`/`tags`) left `null` — the sync service's job, not the
client's, is deciding what happens to those. All three raw PokeAPI HTTP calls (`/pokemon/{id}`,
`/pokemon-species/{id}`, `/evolution-chain/{id}`) and their JSON DTOs stay inside
`infrastructure.pokeapi`; only `PokeApiClientAdapter` sees them.

**Domain model holds typed data, not JSON strings.** `domain.model.Pokemon` fields like `abilities`,
`moves`, `stats`, `types`, `evolutionChain` are typed Java lists/records (e.g. `List<String> abilities`,
`List<StatValue> stats`, `List<EvolutionStage> evolutionChain`), never raw JSON. Serializing those to the
`_json` TEXT columns from V1 is `infrastructure.persistence`'s job (`PokemonEntityMapper`, via Jackson),
matching the "domain has zero Spring/Jackson dependency" rule from the main plan.

**Proprietary-field preservation lives in the service, not the repository.** `PokemonSyncService`:
1. calls `pokeApiClientPort.fetchReplicaData(idOrName)` → replica with null proprietary fields
2. calls `pokemonRepositoryPort.findById(replica.id())` → existing record, if any
3. if present, copies `localizedName`/`region`/`tags` from the existing record onto the replica
4. saves the merged result

This is a plain, unit-testable method — no framework needed to verify the preservation rule.

**Minimal error handling now, extended in Phase 2.** The sync endpoint needs to handle "PokeAPI returned
404 for this id/name" cleanly (it's a foreseeable, not exceptional, path — mandatory "proper error
handling" applies from the first endpoint, not just Phase 2's CRUD work). Bring forward a small
`GlobalExceptionHandler` now (`PokeApiResourceNotFoundException` → 404, fallback → 500); Phase 2 adds the
400/`PokemonNotFoundException` cases for local CRUD on top of it.

**Security stays default-deny for now.** No `SecurityConfig` exists until Phase 3, so the sync endpoint
is unreachable without credentials today — that's correct, not a gap (Phase 3 explicitly marks all
mutating Pokemon endpoints protected). Controller tests disable the security filter chain
(`@AutoConfigureMockMvc(addFilters = false)`) so they test routing/serialization, not auth — auth gets
its own tests in Phase 3.

**Response DTO is shared with Phase 2.** Rather than a throwaway "sync response" shape, `PokemonResponse`
(full detail: every replicated + proprietary field) is built now and reused as-is for `GET
/api/pokemon/{id}` in Phase 2 — one mapper, one shape, less churn.

## Package layout (new files this phase)

```
domain/model/Pokemon.java, StatValue.java, EvolutionStage.java
domain/exception/PokeApiResourceNotFoundException.java
domain/port/PokeApiClientPort.java, PokemonRepositoryPort.java

application/pokemon/PokemonSyncService.java

api/pokemon/PokemonController.java
api/pokemon/dto/PokemonResponse.java
api/pokemon/mapper/PokemonDtoMapper.java
api/common/GlobalExceptionHandler.java, ApiErrorResponse.java

infrastructure/pokeapi/PokeApiClientAdapter.java
infrastructure/pokeapi/dto/PokeApiPokemonResponse.java, PokeApiSpeciesResponse.java,
  PokeApiEvolutionChainResponse.java
infrastructure/pokeapi/mapper/PokeApiResponseMapper.java
infrastructure/config/RestClientConfig.java

infrastructure/persistence/entity/PokemonEntity.java
infrastructure/persistence/repository/PokemonJpaRepository.java
infrastructure/persistence/PokemonRepositoryAdapter.java
infrastructure/persistence/mapper/PokemonEntityMapper.java
```

`PokemonRepositoryPort` is scoped to what this phase needs (`findById`, `save`) — Phase 2 extends it with
pagination/delete rather than speculatively building those now.

## Test fixtures

`project-docs/json-examples/pokemon-response.json` (a real `/pokemon/35` response) already exists —
copy it into `backend/pokeapi-back/src/test/resources/wiremock/pokemon-response.json` so backend tests
are self-contained (no reach-outside-module path dependency). Two fixtures don't exist yet and need
creating the same way (small, hand-trimmed real PokeAPI responses, not full 500-line dumps):
- `pokemon-species-response.json` — needs `genera` (English genus), `flavor_text_entries` (English),
  `evolution_chain.url`
- `evolution-chain-response.json` — a short 2–3 stage chain

## Tasks

**T1 — PokeAPI DTOs + client adapter + WireMock test**
`PokeApiPokemonResponse`/`PokeApiSpeciesResponse`/`PokeApiEvolutionChainResponse` (Jackson-mapped raw
shapes, only the fields actually used), `RestClientConfig` (RestClient bean, base URL from a new
`pokeapi.base-url` property so tests can point it at WireMock), `PokeApiClientAdapter` implementing
`PokeApiClientPort` — three HTTP calls, 404 → `PokeApiResourceNotFoundException`, then delegates mapping.
Test: WireMock stubs all three endpoints from the fixtures, asserts the adapter returns a correctly
populated `Pokemon` domain object.

**T2 — Domain model + response mapper unit tests**
`Pokemon`/`StatValue`/`EvolutionStage` domain records. `PokeApiResponseMapper` (pure function, no HTTP) —
tested directly against the fixtures for the happy path plus edge cases: missing English flavor text
(fall back to any available / empty string, document the choice), species with no pre-evolution (chain
root), genus missing English translation.

**T3 — Persistence: entity, repository, adapter**
`PokemonEntity` (mirrors V1 columns exactly), `PokemonJpaRepository`, `PokemonEntityMapper` (domain ↔
entity, including JSON (de)serialization of the list/record fields into the `_json` TEXT columns),
`PokemonRepositoryAdapter` implementing `PokemonRepositoryPort`. Test: `@DataJpaTest` against real
H2 + Flyway — save then reload, assert every field (including JSON round-trip) survives.

**T4 — Sync service + endpoint**
`PokemonSyncService.syncPokemon(idOrName)` per the merge algorithm above. Unit test (Mockito, both ports
mocked): new-pokemon case, and the proprietary-field-preservation case (existing record has
`localizedName`/`region`/`tags` set, replica has them null, result keeps the existing values and updates
everything else). `GlobalExceptionHandler`/`ApiErrorResponse`, `PokemonController` (`POST
/api/pokemon/sync/{idOrName}`), `PokemonDtoMapper`/`PokemonResponse`. Controller test: MockMvc with
security filters disabled, service mocked, asserts 200 + response shape on success and 404 on
`PokeApiResourceNotFoundException`.

## Verification

Unit + integration suite: `./gradlew test` — all new tests green, existing `MainTests` still passes.

End-to-end, from `backend/pokeapi-back/`: `./gradlew bootRun`, then:
```bash
curl -X POST http://localhost:8080/api/pokemon/sync/25
```
(expect 401 until Phase 3 adds auth — verify via a MockMvc test with filters disabled instead, per the
design decision above; the manual curl check is for confirming routing/wiring only, not final behavior).
Confirm via H2 console (or the `@DataJpaTest` suite) that a `pokemon` row for id 25 exists with real
PokeAPI data. Manually set `localized_name` on that row, re-run the sync, confirm it's unchanged while
`weight`/`stats_json`/etc. reflect a fresh fetch.
