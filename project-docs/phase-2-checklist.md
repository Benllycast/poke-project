# Phase 2 Checklist

Tracks every individual file/task from
[phase-2-crud-api-plan.md](phase-2-crud-api-plan.md). Check items off as they land.

## T5 — Domain/port additions

- [x] Create `domain/model/PageResult.java` (`items`, `page`, `size`, `totalElements`, `totalPages`)
- [x] Create `domain/exception/PokemonNotFoundException.java`
- [x] Create `domain/exception/PokemonAlreadyExistsException.java`
- [x] Edit `domain/port/PokemonRepositoryPort.java`: add `PageResult<Pokemon> findAll(int page, int size)`
- [x] Edit `domain/port/PokemonRepositoryPort.java`: add `void deleteById(Long id)`

Verify:
- [x] `./gradlew compileJava` succeeds. Committed together with T6 — an interface method and its
      implementation can't land in separate compiling commits, same as Phase 1's T1/T2.

## T6 — Persistence: pagination + delete

- [x] Edit `infrastructure/persistence/PokemonRepositoryAdapter.java`: implement `findAll` via
      `PokemonJpaRepository.findAll(PageRequest.of(page, size))`, map `Page<PokemonEntity>` →
      `PageResult<Pokemon>`
- [x] Edit `infrastructure/persistence/PokemonRepositoryAdapter.java`: implement `deleteById` via
      `PokemonJpaRepository.deleteById`

Verify:
- [x] `PokemonRepositoryAdapterTest`: seed 3 records, `findAll(0, 2)` returns 2 items,
      `totalElements == 3`, `totalPages == 2`
- [x] `PokemonRepositoryAdapterTest`: `deleteById` then `findById` on the same id returns empty
- [x] `./gradlew test --tests "*PokemonRepositoryAdapterTest"` passes (4/4)

## T7 — Query service + GET endpoints

- [x] Create `application/pokemon/PokemonQueryService.java` (`getById` throws
      `PokemonNotFoundException`; `list(page, size)` clamps `page>=0`, `size` in `[1,100]`)
- [x] Edit `api/pokemon/PokemonController.java`: add `GET /api/pokemon` (`page`/`size` request params,
      defaults `0`/`20`)
- [x] Edit `api/pokemon/PokemonController.java`: add `GET /api/pokemon/{id}`
- [x] Create `api/pokemon/dto/PokemonPageResponse.java`
- [x] Edit `api/pokemon/mapper/PokemonDtoMapper.java`: add `toPageResponse`
- [x] Edit `api/common/GlobalExceptionHandler.java`: add `PokemonNotFoundException` → 404 — pulled forward
      from T8's list since this task's own 404 test needed it to actually pass, not just compile

Verify:
- [x] `PokemonQueryServiceTest`: `getById` found case
- [x] `PokemonQueryServiceTest`: `getById` not-found → `PokemonNotFoundException`
- [x] `PokemonQueryServiceTest`: `list` clamps negative page to 0
- [x] `PokemonQueryServiceTest`: `list` clamps oversized size to 100
- [x] `PokemonControllerTest`: `GET /api/pokemon` → 200, correct `PokemonPageResponse` shape
- [x] `PokemonControllerTest`: `GET /api/pokemon/{id}` → 200
- [x] `PokemonControllerTest`: `GET /api/pokemon/{id}` (missing) → 404
- [x] `./gradlew test --tests "*PokemonQueryServiceTest" --tests "*PokemonControllerTest"` passes (4 + 5)

## T8 — Edit service + write endpoints + validation

- [x] Create `api/pokemon/dto/PokemonCreateRequest.java` (`id` `@NotNull @Positive`, `name`
      `@NotBlank @Size(max=100)`, other fields matching V1 column length constraints)
- [x] Create `api/pokemon/dto/PokemonUpdateRequest.java` (same shape minus `id`)
- [x] Create `application/pokemon/PokemonEditService.java` (`create`: 409 if id exists; `update`: 404 if
      missing, full replace; `delete`: 404 if missing)
- [x] Edit `api/pokemon/mapper/PokemonDtoMapper.java`: add `fromCreateRequest`, `applyUpdate`
- [x] Edit `api/pokemon/PokemonController.java`: add `POST /api/pokemon` (201)
- [x] Edit `api/pokemon/PokemonController.java`: add `PUT /api/pokemon/{id}` (200)
- [x] Edit `api/pokemon/PokemonController.java`: add `DELETE /api/pokemon/{id}` (204)
- [x] Edit `api/common/GlobalExceptionHandler.java`: add `PokemonAlreadyExistsException` → 409
- [x] Edit `api/common/GlobalExceptionHandler.java`: add `MethodArgumentNotValidException` → 400 with
      field-level messages
- [x] Edit `api/common/GlobalExceptionHandler.java`: add `HttpRequestMethodNotSupportedException` → 405
      (fixes the catch-all masking this as 500, found in Phase 1)
- [x] Edit `api/common/GlobalExceptionHandler.java`: add `NoResourceFoundException` → 404

Verify:
- [x] `PokemonEditServiceTest`: `create` success
- [x] `PokemonEditServiceTest`: `create` conflict → `PokemonAlreadyExistsException`
- [x] `PokemonEditServiceTest`: `update` success (full replace)
- [x] `PokemonEditServiceTest`: `update` not-found → `PokemonNotFoundException`
- [x] `PokemonEditServiceTest`: `delete` success
- [x] `PokemonEditServiceTest`: `delete` not-found → `PokemonNotFoundException`
- [x] `PokemonControllerTest`: `POST /api/pokemon` → 201, correct response shape
- [x] `PokemonControllerTest`: `POST /api/pokemon` malformed payload (missing `name`) → 400
- [x] `PokemonControllerTest`: `POST /api/pokemon` duplicate id → 409
- [x] `PokemonControllerTest`: `PUT /api/pokemon/{id}` → 200
- [x] `PokemonControllerTest`: `PUT /api/pokemon/{id}` malformed payload → 400
- [x] `PokemonControllerTest`: `PUT /api/pokemon/{id}` missing id → 404
- [x] `PokemonControllerTest`: `DELETE /api/pokemon/{id}` → 204
- [x] `PokemonControllerTest`: `DELETE /api/pokemon/{id}` missing id → 404
- [x] `./gradlew test --tests "*PokemonEditServiceTest" --tests "*PokemonControllerTest"` passes
      (6/6 + 13/13)

## Overall Phase 2 Verification

- [x] `./gradlew build` passes — full suite green, 37/37 tests across the whole module
- [x] `./gradlew bootRun`, then `curl -u user:<generated-password> "http://localhost:8080/api/pokemon?page=0&size=5"`
      → 200 with real data (`totalElements: 1`, pikachu — synced fresh via the same scratch-test approach
      as Phase 1, since this run started from an empty datastore)
- [x] `curl -u user:<generated-password> http://localhost:8080/api/pokemon/25` → 200 with real pikachu
      data; `curl .../api/pokemon/9999` → 404 with the correct `ApiErrorResponse` body
- [x] Throwaway `@SpringBootTest` (not committed) exercising `PokemonSyncService` + `PokemonEditService`
      directly: synced id 25 (`weight=60`), created local id 100000 (`custom-mon`), updated its
      proprietary fields, deleted it, then confirmed a second delete correctly throws
      `PokemonNotFoundException` — full create→update→delete cycle against the real H2 datastore
- [x] `git status` on `frontend/` — clean; every Phase 2 commit touched `backend/` only
