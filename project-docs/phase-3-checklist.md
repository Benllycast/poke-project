# Phase 3 Checklist

Tracks every individual file/task from [phase-3-auth-plan.md](phase-3-auth-plan.md). Check items off as
they land.

## T9 — User domain + persistence

- [ ] Create `domain/model/User.java` (`id`, `email`, `passwordHash`, `role`)
- [ ] Create `domain/model/UserRole.java` (`USER`, `ADMIN`)
- [ ] Create `domain/port/UserRepositoryPort.java` (`findByEmail`, `save`)
- [ ] Create `infrastructure/persistence/entity/UserEntity.java` (mirrors V2 columns, `role` via
      `@Enumerated(EnumType.STRING)`)
- [ ] Create `infrastructure/persistence/repository/UserJpaRepository.java` (+ derived `findByEmail`)
- [ ] Create `infrastructure/persistence/mapper/UserEntityMapper.java`
- [ ] Create `infrastructure/persistence/UserRepositoryAdapter.java`

Verify:
- [ ] `UserRepositoryAdapterTest` (`@DataJpaTest`): save then reload round-trips every field
- [ ] `UserRepositoryAdapterTest`: `findByEmail` returns empty when missing
- [ ] `UserRepositoryAdapterTest`: duplicate email violates the `V2` unique constraint (insert throws)
- [ ] `./gradlew test --tests "*UserRepositoryAdapterTest"` passes

## T10 — AuthService + password hashing

- [ ] Create `domain/port/PasswordHasher.java` (`hash`, `matches`)
- [ ] Create `infrastructure/security/BCryptPasswordHasher.java` (wraps `BCryptPasswordEncoder`)
- [ ] Create `domain/exception/UserAlreadyExistsException.java`
- [ ] Create `domain/exception/InvalidCredentialsException.java`
- [ ] Create `application/auth/AuthService.java` (`register`: 409 if email exists; `login`: 401 on
      missing user or wrong password)

Verify:
- [ ] `AuthServiceTest`: `register` success
- [ ] `AuthServiceTest`: `register` conflict → `UserAlreadyExistsException`
- [ ] `AuthServiceTest`: `login` success → token from mocked `TokenProvider`
- [ ] `AuthServiceTest`: `login` wrong password → `InvalidCredentialsException`
- [ ] `AuthServiceTest`: `login` unknown email → `InvalidCredentialsException`
- [ ] `./gradlew test --tests "*AuthServiceTest"` passes

## T11 — JWT token provider

- [ ] Add `jwt.secret` / `jwt.expiration-ms` to `application.yaml` (32+ byte dev-only default,
      `${JWT_SECRET:...}` override)
- [ ] Create `domain/port/TokenProvider.java` (`generateToken(User)`)
- [ ] Create `infrastructure/security/JwtTokenProvider.java` (HS256; also exposes a parse method used
      directly by `JwtAuthenticationFilter` in T12 — not part of the domain port)

Verify:
- [ ] `JwtTokenProviderTest`: generate → parse round-trip recovers correct email/role claims
- [ ] `JwtTokenProviderTest`: an expired token fails parsing
- [ ] `./gradlew test --tests "*JwtTokenProviderTest"` passes

## T12 — SecurityConfig + AuthController + real integration test

- [ ] Create `infrastructure/security/JwtAuthenticationFilter.java` (reads `Authorization: Bearer`, sets
      `SecurityContext` on valid token, otherwise leaves it empty)
- [ ] Create `infrastructure/security/SecurityConfig.java`: CSRF disabled, CORS allows
      `http://localhost:5173`, session policy `STATELESS`
- [ ] Edit `SecurityConfig.java`: `POST /api/auth/**` → `permitAll`
- [ ] Edit `SecurityConfig.java`: `GET /api/pokemon/**` → `permitAll`
- [ ] Edit `SecurityConfig.java`: `/h2-console/**` → `permitAll`, frame options disabled
- [ ] Edit `SecurityConfig.java`: everything else → `authenticated`
- [ ] Edit `SecurityConfig.java`: register `JwtAuthenticationFilter` before
      `UsernamePasswordAuthenticationFilter`
- [ ] Create `api/auth/dto/RegisterRequest.java` (`@Email @NotBlank` email, `@NotBlank @Size(min=8)`
      password)
- [ ] Create `api/auth/dto/LoginRequest.java`
- [ ] Create `api/auth/dto/AuthResponse.java` (`token`, `email`, `role`)
- [ ] Create `api/auth/mapper/AuthDtoMapper.java`
- [ ] Create `api/auth/AuthController.java` (`POST /api/auth/register` → 201, `POST /api/auth/login` →
      200)
- [ ] Edit `api/common/GlobalExceptionHandler.java`: add `UserAlreadyExistsException` → 409
- [ ] Edit `api/common/GlobalExceptionHandler.java`: add `InvalidCredentialsException` → 401
- [ ] Create `src/test/resources/application-test.yaml` (isolated `jdbc:h2:mem:...` datasource)

Verify:
- [ ] `AuthenticationFlowTest` (`@SpringBootTest` + `@AutoConfigureMockMvc`, filters **not** disabled,
      `@ActiveProfiles("test")`): register → 201 + token
- [ ] `AuthenticationFlowTest`: register same email again → 409
- [ ] `AuthenticationFlowTest`: login with the registered credentials → 200 + token
- [ ] `AuthenticationFlowTest`: login with a wrong password → 401
- [ ] `AuthenticationFlowTest`: `GET /api/pokemon` with no token → 200
- [ ] `AuthenticationFlowTest`: `POST /api/pokemon` (local-only create) with no token → 401
- [ ] `AuthenticationFlowTest`: `POST /api/pokemon` with the login token → 201
- [ ] `./gradlew test --tests "*AuthenticationFlowTest"` passes

## Overall Phase 3 Verification

- [ ] `./gradlew build` passes (full suite, all new + existing tests green)
- [ ] `./gradlew bootRun`, then `curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"email":"demo@pokeapp.dev","password":"Demo1234!"}'` → 201 with a token
- [ ] `curl http://localhost:8080/api/pokemon` (no auth header) → 200
- [ ] `curl -X POST http://localhost:8080/api/pokemon/sync/25` (no auth header) → 401
- [ ] `curl -X POST http://localhost:8080/api/pokemon/sync/25 -H "Authorization: Bearer <token>"` → 200,
      real PokeAPI data persisted (first genuine HTTP-driven sync since Phase 1 — no more scratch-test
      workaround needed)
- [ ] H2 console (`http://localhost:8080/h2-console`, `dev` profile) loads and renders correctly (frame
      options fix)
- [ ] `npm run lint` / frontend untouched this phase — confirm via `git status` on `frontend/`
