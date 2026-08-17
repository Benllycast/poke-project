# Phase 3 Checklist

Tracks every individual file/task from [phase-3-auth-plan.md](phase-3-auth-plan.md). Check items off as
they land.

## T9 — User domain + persistence

- [x] Create `domain/model/User.java` (`id`, `email`, `passwordHash`, `role`)
- [x] Create `domain/model/UserRole.java` (`USER`, `ADMIN`)
- [x] Create `domain/port/UserRepositoryPort.java` (`findByEmail`, `save`)
- [x] Create `infrastructure/persistence/entity/UserEntity.java` (mirrors V2 columns, `role` via
      `@Enumerated(EnumType.STRING)`)
- [x] Create `infrastructure/persistence/repository/UserJpaRepository.java` (+ derived `findByEmail`)
- [x] Create `infrastructure/persistence/mapper/UserEntityMapper.java`
- [x] Create `infrastructure/persistence/UserRepositoryAdapter.java`

Verify:
- [x] `UserRepositoryAdapterTest` (`@DataJpaTest`): save then reload round-trips every field
- [x] `UserRepositoryAdapterTest`: `findByEmail` returns empty when missing
- [x] `UserRepositoryAdapterTest`: duplicate email violates the `V2` unique constraint (insert throws)
- [x] `./gradlew test --tests "*UserRepositoryAdapterTest"` passes (3/3)

## T10 — AuthService + password hashing

- [x] Create `domain/port/PasswordHasher.java` (`hash`, `matches`)
- [x] Create `infrastructure/security/BCryptPasswordHasher.java` (wraps `BCryptPasswordEncoder`)
- [x] Create `domain/exception/UserAlreadyExistsException.java`
- [x] Create `domain/exception/InvalidCredentialsException.java`
- [x] Create `application/auth/AuthService.java` (`register`: 409 if email exists; `login`: 401 on
      missing user or wrong password)
- [x] Create `domain/port/TokenProvider.java` (interface only) — pulled forward from T11 since
      `AuthService.login` needs it to compile; T11 only adds the concrete `JwtTokenProvider`

Verify:
- [x] `AuthServiceTest`: `register` success
- [x] `AuthServiceTest`: `register` conflict → `UserAlreadyExistsException`
- [x] `AuthServiceTest`: `login` success → token from mocked `TokenProvider`
- [x] `AuthServiceTest`: `login` wrong password → `InvalidCredentialsException`
- [x] `AuthServiceTest`: `login` unknown email → `InvalidCredentialsException`
- [x] `./gradlew test --tests "*AuthServiceTest"` passes (5/5)

## T11 — JWT token provider

- [x] Add `jwt.secret` / `jwt.expiration-ms` to `application.yaml` (32+ byte dev-only default,
      `${JWT_SECRET:...}` override)
- [x] Create `infrastructure/security/JwtTokenProvider.java` (HS256; also exposes a parse method used
      directly by `JwtAuthenticationFilter` in T12 — not part of the domain port)

Verify:
- [x] `JwtTokenProviderTest`: generate → parse round-trip recovers correct email/role claims
- [x] `JwtTokenProviderTest`: an expired token fails parsing
- [x] `./gradlew test --tests "*JwtTokenProviderTest"` passes (2/2) — jjwt 0.12.6's fluent
      builder/parser API worked as designed on the first try

## T12 — SecurityConfig + AuthController + real integration test

- [x] Create `infrastructure/security/JwtAuthenticationFilter.java` (reads `Authorization: Bearer`, sets
      `SecurityContext` on valid token, otherwise leaves it empty)
- [x] Create `infrastructure/security/SecurityConfig.java`: CSRF disabled, CORS allows
      `http://localhost:5173`, session policy `STATELESS`
- [x] Edit `SecurityConfig.java`: `POST /api/auth/**` → `permitAll`
- [x] Edit `SecurityConfig.java`: `GET /api/pokemon/**` → `permitAll`
- [x] Edit `SecurityConfig.java`: `/h2-console/**` → `permitAll`, frame options disabled
- [x] Edit `SecurityConfig.java`: everything else → `authenticated`
- [x] Edit `SecurityConfig.java`: register `JwtAuthenticationFilter` before
      `UsernamePasswordAuthenticationFilter`
- [x] Edit `SecurityConfig.java`: explicit `HttpStatusEntryPoint(401)` — with no `httpBasic()`/
      `formLogin()` registered, Spring Security has no default `AuthenticationEntryPoint` and falls back
      to 403 for unauthenticated requests instead of 401; found via the integration test
- [x] Create `api/auth/dto/RegisterRequest.java` (`@Email @NotBlank` email, `@NotBlank @Size(min=8)`
      password)
- [x] Create `api/auth/dto/LoginRequest.java`
- [x] Create `api/auth/dto/AuthResponse.java` (`token`, `email` — dropped `role` from the plan's shape;
      `AuthService` returns just the token string, and adding a role field would have meant either
      threading a richer result type back through an already-committed T10 method or having the
      controller re-fetch the user, neither worth it for a field the JWT itself already carries as a
      claim if a client needs it)
- [x] Create `api/auth/mapper/AuthDtoMapper.java`
- [x] Create `api/auth/AuthController.java` (`POST /api/auth/register` → 201, `POST /api/auth/login` →
      200)
- [x] Edit `api/common/GlobalExceptionHandler.java`: add `UserAlreadyExistsException` → 409
- [x] Edit `api/common/GlobalExceptionHandler.java`: add `InvalidCredentialsException` → 401
- [x] Create `src/test/resources/application-test.yaml` (isolated `jdbc:h2:mem:...` datasource)
- [x] Edit `application.yaml`: `spring.profiles.active: dev` → `spring.profiles.default: dev` — `active`
      hardcodes `dev` regardless of a test's `@ActiveProfiles`, which would have made selecting the
      isolated test datasource impossible; `default` keeps the same `bootRun` behavior when nothing else
      is set
- [x] Edit `PokemonControllerTest.java`: exclude `JwtAuthenticationFilter` from the `@WebMvcTest` slice —
      `@WebMvcTest` always includes servlet `Filter` beans regardless of component-scan exclusions
      (undocumented gotcha), so `JwtAuthenticationFilter` (implements `Filter`) kept getting pulled into
      the context and failing to construct (needs `JwtTokenProvider`, which the slice correctly excludes
      as a plain `@Component`)

Verify:
- [x] `AuthenticationFlowTest` (`@SpringBootTest` + `@AutoConfigureMockMvc`, filters **not** disabled,
      `@ActiveProfiles("test")`): register → 201 + token
- [x] `AuthenticationFlowTest`: register same email again → 409
- [x] `AuthenticationFlowTest`: login with the registered credentials → 200 + token
- [x] `AuthenticationFlowTest`: login with a wrong password → 401
- [x] `AuthenticationFlowTest`: `GET /api/pokemon` with no token → 200
- [x] `AuthenticationFlowTest`: `POST /api/pokemon` (local-only create) with no token → 401
- [x] `AuthenticationFlowTest`: `POST /api/pokemon` with the login token → 201
- [x] `./gradlew test --tests "*AuthenticationFlowTest"` passes (1 test method, all 7 assertions above)
- [x] Existing `PokemonControllerTest`/other `@WebMvcTest` suites still pass with the real `SecurityConfig`
      now on the classpath (verified via the full suite run, not just this class in isolation)

## Overall Phase 3 Verification

- [x] `./gradlew build` passes — full suite green, 48/48 tests across the whole module
- [x] `./gradlew bootRun`, then `curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"email":"demo@pokeapp.dev","password":"Demo1234!"}'` → 201 with a real JWT
- [x] `curl http://localhost:8080/api/pokemon` (no auth header) → 200
- [x] `curl -X POST http://localhost:8080/api/pokemon/sync/25` (no auth header) → 401
- [x] `curl -X POST http://localhost:8080/api/pokemon/sync/25 -H "Authorization: Bearer <token>"` → 200,
      real pikachu data persisted — first genuine HTTP-driven sync since Phase 1, no more scratch-test
      workaround needed
- [x] H2 console (`http://localhost:8080/h2-console/`) → 200, no `X-Frame-Options` header (frame-options
      fix confirmed via response headers)
- [x] `git status` on `frontend/` — clean; every Phase 3 commit touched `backend/` only
