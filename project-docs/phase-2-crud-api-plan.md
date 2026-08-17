# Phase 2 Implementation Plan: Read + CRUD API (US-01, US-02, US-04)

## Context

Phase 1 delivered replication (`POST /api/pokemon/sync/{idOrName}`) — PokeAPI data now lands in the local
`pokemon` table with proprietary fields preserved across re-sync. Phase 2 is the read/write surface a
frontend actually consumes: paginated browsing (US-01), a detail view (US-02), and full local CRUD with
the required 404/400 behavior (US-04 + the "standard REST verbs" mandate). This is also the last backend
phase before Phase 3 (auth) — everything built here stays behind Spring's current default-deny security
(confirmed in Phase 1: even GET requests need the devtools-generated credentials), so verification leans
on the automated test suite plus GET-only curl checks rather than a live end-to-end demo of the mutating
endpoints.

Per [development-plan.md](development-plan.md) §9 (Phase 2, steps 8–9), this phase delivers
`PokemonQueryService`/`PokemonEditService` behind `GET /api/pokemon`, `GET /api/pokemon/{id}`,
`POST /api/pokemon`, `PUT /api/pokemon/{id}`, `DELETE /api/pokemon/{id}`.

**Definition of done**: all 5 endpoints exist, return correct status codes (200/201/204/400/404/409),
and a client can page through the local Pokemon table, fetch one, create a local-only record, edit any
field on any record, and delete it — all backed by real DB pagination, not an in-memory list slice.

## Design decisions

**Pagination stays framework-free at the domain boundary.** `PokemonRepositoryPort.findAll(page, size)`
returns a new `domain.model.PageResult<Pokemon>` record (`items`, `page`, `size`, `totalElements`,
`totalPages`) — not Spring Data's `Page<T>`. `PokemonRepositoryAdapter` is the only place that touches
`org.springframework.data.domain.Pageable`/`Page`, converting to/from `PageResult` at the boundary. Same
rule as Phase 1's DTOs: framework types never cross into `domain`/`application`.

**Local-only creation needs an explicit id, not auto-generation.** The `pokemon` table's `id` column is
the PokeAPI id (see Phase 0's `V1__create_pokemon_table.sql`) — not `AUTO_INCREMENT`. A purely local
record (never synced from PokeAPI) has no natural id, so `POST /api/pokemon` requires the caller to
supply one in the request body (validated `@NotNull @Positive`). If that id already exists, the response
is `409 Conflict` (`PokemonAlreadyExistsException`) rather than silently overwriting — silent overwrite
would make POST behave like sync's upsert, which defeats the point of having separate create/update
verbs. Documented in the README's endpoint table: callers creating local-only entries should pick an id
outside the real PokeAPI dex range (e.g. 100000+) to avoid an eventual collision with a synced entry.

**PUT is a full replace, not a partial merge.** `PokemonUpdateRequest` mirrors every editable field
(replicated *and* proprietary). A `PUT` that omits `region` sets it to `null` — standard REST PUT
semantics, and simpler to reason about/test than partial-patch merge logic. If partial updates turn out
to be needed for the frontend's edit form, that's a `PATCH` addition later, not a Phase 2 requirement.

**Two request DTOs, not one with validation groups.** `PokemonCreateRequest` (has `id`) and
`PokemonUpdateRequest` (no `id` — comes from the path) are separate small records. Bean Validation groups
would avoid the duplication but add ceremony disproportionate to ~10 fields; two plain DTOs are easier to
read and test.

**List and detail share `PokemonResponse`.** No separate "summary" shape for the list endpoint (the kind
of thing US-01 literally asks for — sprite/category/mass/skills — is a subset of `PokemonResponse`'s
fields). Reusing it avoids a second mapper and a second set of tests for marginal payload-size savings
that don't matter at this scale.

**`GlobalExceptionHandler` gets two more mappings, plus a bug fix found while testing Phase 1's 500
fallback.** New: `PokemonNotFoundException` → 404, `PokemonAlreadyExistsException` → 409,
`MethodArgumentNotValidException` (bean validation failures) → 400 with field-level messages. Fix:
the catch-all `@ExceptionHandler(Exception.class)` currently swallows framework exceptions like
`HttpRequestMethodNotSupportedException` (wrong HTTP verb) into a generic 500 instead of the correct 405 —
discovered by accident while curl-testing Phase 1's sync endpoint with the wrong verb. Add an explicit
handler for it (and `NoResourceFoundException` → 404) before the catch-all so real framework error codes
aren't masked.

**Pagination params are clamped, not bean-validated.** `page`/`size` are plain `@RequestParam` ints,
defaulting to `0`/`20`. `PokemonQueryService` clamps (`page = max(page, 0)`, `size` capped to `[1, 100]`)
rather than rejecting out-of-range values with a 400 — the spec's 400/404 requirements are about the
Pokemon payload, not pagination trivia, and clamping is friendlier than erroring on `?size=99999`.

## Package layout (new/changed files this phase)

```
domain/model/PageResult.java                              (new)
domain/exception/PokemonNotFoundException.java             (new)
domain/exception/PokemonAlreadyExistsException.java        (new)
domain/port/PokemonRepositoryPort.java                     (edit: + findAll(page,size), + deleteById)

application/pokemon/PokemonQueryService.java                (new)
application/pokemon/PokemonEditService.java                 (new)

infrastructure/persistence/PokemonRepositoryAdapter.java    (edit: implement the 2 new port methods)

api/pokemon/dto/PokemonPageResponse.java                    (new)
api/pokemon/dto/PokemonCreateRequest.java                   (new)
api/pokemon/dto/PokemonUpdateRequest.java                   (new)
api/pokemon/mapper/PokemonDtoMapper.java                    (edit: + toPageResponse, + fromCreateRequest,
                                                               + applyUpdate)
api/pokemon/PokemonController.java                          (edit: + 4 endpoints)
api/common/GlobalExceptionHandler.java                      (edit: + 3 handlers, fix the 500-masking bug)
```

## Endpoints delivered this phase

| Method | Path | Success | Failure |
|---|---|---|---|
| GET | `/api/pokemon?page=&size=` | 200 `PokemonPageResponse` | — |
| GET | `/api/pokemon/{id}` | 200 `PokemonResponse` | 404 `PokemonNotFoundException` |
| POST | `/api/pokemon` | 201 `PokemonResponse` | 400 validation, 409 `PokemonAlreadyExistsException` |
| PUT | `/api/pokemon/{id}` | 200 `PokemonResponse` | 400 validation, 404 `PokemonNotFoundException` |
| DELETE | `/api/pokemon/{id}` | 204 | 404 `PokemonNotFoundException` |

## Tasks

**T5 — Domain/port additions**
`PageResult<T>` record; `PokemonNotFoundException`/`PokemonAlreadyExistsException`; extend
`PokemonRepositoryPort` with `findAll(int page, int size)` and `deleteById(Long id)`. No tests of their
own (plain data/interface) — exercised by T6/T8's tests.

**T6 — Persistence: pagination + delete**
`PokemonRepositoryAdapter.findAll` uses `PokemonJpaRepository.findAll(PageRequest.of(page, size))` and
maps `Page<PokemonEntity>` → `PageResult<Pokemon>`. `deleteById` delegates straight to
`PokemonJpaRepository.deleteById`. Test (extends Phase 1's `PokemonRepositoryAdapterTest`): seed 3
records, page through with `size=2` (assert 2 pages, correct `totalElements`), delete one, confirm
`findById` afterward is empty.

**T7 — Query service + GET endpoints**
`PokemonQueryService.getById` (404 via `PokemonNotFoundException`), `.list(page, size)` (clamped, then
delegates). Unit tests (Mockito): found/not-found, clamping behavior. Controller: `GET /api/pokemon`,
`GET /api/pokemon/{id}`. `PokemonControllerTest` additions: 200 list shape, 200 detail, 404 detail.

**T8 — Edit service + write endpoints + validation**
`PokemonEditService.create` (409 if id exists), `.update` (404 if missing, full replace), `.delete` (404
if missing). `PokemonCreateRequest`/`PokemonUpdateRequest` with Bean Validation annotations matching the
V1 column constraints (`@NotBlank @Size(max=100)` on `name`, `@Size(max=500)` on `spriteUrl`/`tags`,
etc.). `GlobalExceptionHandler` additions + the 405/404-masking fix. Unit tests (Mockito): create
success/conflict, update success/not-found, delete success/not-found. `PokemonControllerTest` additions:
201 create, 400 on a malformed create payload (missing `name`), 409 on duplicate id, 200 update, 400 on
malformed update, 404 update-missing, 204 delete, 404 delete-missing — explicit named tests for the
malformed-payload and missing-id cases per the mandatory requirement, not just happy-path coverage.

## Verification

`./gradlew build` — full suite green (existing 14 + this phase's new tests).

Manual: `./gradlew bootRun`, then (reusing the Phase 1 finding that GET requests work with the
devtools-generated basic-auth credentials, since CSRF only blocks state-changing methods):
```bash
curl -u user:<generated-password> "http://localhost:8080/api/pokemon?page=0&size=5"
curl -u user:<generated-password> http://localhost:8080/api/pokemon/25
```
against data synced in Phase 1's verification. For the write endpoints, reuse Phase 1's approach — a
throwaway (uncommitted) `@SpringBootTest` that autowires `PokemonEditService` directly, bypassing
HTTP/security, to prove a real create → update → delete cycle against the real H2 datastore.
