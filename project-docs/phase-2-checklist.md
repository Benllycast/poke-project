# Phase 2 Checklist

Tracks every individual file/task from
[phase-2-crud-api-plan.md](phase-2-crud-api-plan.md). Check items off as they land.

## T5 — Domain/port additions

- [ ] Create `domain/model/PageResult.java` (`items`, `page`, `size`, `totalElements`, `totalPages`)
- [ ] Create `domain/exception/PokemonNotFoundException.java`
- [ ] Create `domain/exception/PokemonAlreadyExistsException.java`
- [ ] Edit `domain/port/PokemonRepositoryPort.java`: add `PageResult<Pokemon> findAll(int page, int size)`
- [ ] Edit `domain/port/PokemonRepositoryPort.java`: add `void deleteById(Long id)`

Verify:
- [ ] `./gradlew compileJava` succeeds (no test of its own — exercised by T6/T8)

## T6 — Persistence: pagination + delete

- [ ] Edit `infrastructure/persistence/PokemonRepositoryAdapter.java`: implement `findAll` via
      `PokemonJpaRepository.findAll(PageRequest.of(page, size))`, map `Page<PokemonEntity>` →
      `PageResult<Pokemon>`
- [ ] Edit `infrastructure/persistence/PokemonRepositoryAdapter.java`: implement `deleteById` via
      `PokemonJpaRepository.deleteById`

Verify:
- [ ] `PokemonRepositoryAdapterTest`: seed 3 records, `findAll(0, 2)` returns 2 items,
      `totalElements == 3`, `totalPages == 2`
- [ ] `PokemonRepositoryAdapterTest`: `deleteById` then `findById` on the same id returns empty
- [ ] `./gradlew test --tests "*PokemonRepositoryAdapterTest"` passes

## T7 — Query service + GET endpoints

- [ ] Create `application/pokemon/PokemonQueryService.java` (`getById` throws
      `PokemonNotFoundException`; `list(page, size)` clamps `page>=0`, `size` in `[1,100]`)
- [ ] Edit `api/pokemon/PokemonController.java`: add `GET /api/pokemon` (`page`/`size` request params,
      defaults `0`/`20`)
- [ ] Edit `api/pokemon/PokemonController.java`: add `GET /api/pokemon/{id}`
- [ ] Create `api/pokemon/dto/PokemonPageResponse.java`
- [ ] Edit `api/pokemon/mapper/PokemonDtoMapper.java`: add `toPageResponse`

Verify:
- [ ] `PokemonQueryServiceTest`: `getById` found case
- [ ] `PokemonQueryServiceTest`: `getById` not-found → `PokemonNotFoundException`
- [ ] `PokemonQueryServiceTest`: `list` clamps negative page to 0
- [ ] `PokemonQueryServiceTest`: `list` clamps oversized size to 100
- [ ] `PokemonControllerTest`: `GET /api/pokemon` → 200, correct `PokemonPageResponse` shape
- [ ] `PokemonControllerTest`: `GET /api/pokemon/{id}` → 200
- [ ] `PokemonControllerTest`: `GET /api/pokemon/{id}` (missing) → 404
- [ ] `./gradlew test --tests "*PokemonQueryServiceTest" --tests "*PokemonControllerTest"` passes

## T8 — Edit service + write endpoints + validation

- [ ] Create `api/pokemon/dto/PokemonCreateRequest.java` (`id` `@NotNull @Positive`, `name`
      `@NotBlank @Size(max=100)`, other fields matching V1 column length constraints)
- [ ] Create `api/pokemon/dto/PokemonUpdateRequest.java` (same shape minus `id`)
- [ ] Create `application/pokemon/PokemonEditService.java` (`create`: 409 if id exists; `update`: 404 if
      missing, full replace; `delete`: 404 if missing)
- [ ] Edit `api/pokemon/mapper/PokemonDtoMapper.java`: add `fromCreateRequest`, `applyUpdate`
- [ ] Edit `api/pokemon/PokemonController.java`: add `POST /api/pokemon` (201)
- [ ] Edit `api/pokemon/PokemonController.java`: add `PUT /api/pokemon/{id}` (200)
- [ ] Edit `api/pokemon/PokemonController.java`: add `DELETE /api/pokemon/{id}` (204)
- [ ] Edit `api/common/GlobalExceptionHandler.java`: add `PokemonNotFoundException` → 404
- [ ] Edit `api/common/GlobalExceptionHandler.java`: add `PokemonAlreadyExistsException` → 409
- [ ] Edit `api/common/GlobalExceptionHandler.java`: add `MethodArgumentNotValidException` → 400 with
      field-level messages
- [ ] Edit `api/common/GlobalExceptionHandler.java`: add `HttpRequestMethodNotSupportedException` → 405
      (fixes the catch-all masking this as 500, found in Phase 1)
- [ ] Edit `api/common/GlobalExceptionHandler.java`: add `NoResourceFoundException` → 404

Verify:
- [ ] `PokemonEditServiceTest`: `create` success
- [ ] `PokemonEditServiceTest`: `create` conflict → `PokemonAlreadyExistsException`
- [ ] `PokemonEditServiceTest`: `update` success (full replace)
- [ ] `PokemonEditServiceTest`: `update` not-found → `PokemonNotFoundException`
- [ ] `PokemonEditServiceTest`: `delete` success
- [ ] `PokemonEditServiceTest`: `delete` not-found → `PokemonNotFoundException`
- [ ] `PokemonControllerTest`: `POST /api/pokemon` → 201, correct response shape
- [ ] `PokemonControllerTest`: `POST /api/pokemon` malformed payload (missing `name`) → 400
- [ ] `PokemonControllerTest`: `POST /api/pokemon` duplicate id → 409
- [ ] `PokemonControllerTest`: `PUT /api/pokemon/{id}` → 200
- [ ] `PokemonControllerTest`: `PUT /api/pokemon/{id}` malformed payload → 400
- [ ] `PokemonControllerTest`: `PUT /api/pokemon/{id}` missing id → 404
- [ ] `PokemonControllerTest`: `DELETE /api/pokemon/{id}` → 204
- [ ] `PokemonControllerTest`: `DELETE /api/pokemon/{id}` missing id → 404
- [ ] `./gradlew test --tests "*PokemonEditServiceTest" --tests "*PokemonControllerTest"` passes

## Overall Phase 2 Verification

- [ ] `./gradlew build` passes (full suite, all new + existing tests green)
- [ ] `./gradlew bootRun`, then `curl -u user:<generated-password> "http://localhost:8080/api/pokemon?page=0&size=5"` → 200 with real data
- [ ] `curl -u user:<generated-password> http://localhost:8080/api/pokemon/25` → 200 with real data
      (assumes Phase 1's sync of id 25 already ran against this datastore)
- [ ] Throwaway `@SpringBootTest` (not committed) exercising `PokemonEditService` directly: create → update
      → delete cycle against the real H2 datastore, each step confirmed via `findById`
- [ ] `npm run lint` / frontend untouched this phase — confirm via `git status` on `frontend/`
