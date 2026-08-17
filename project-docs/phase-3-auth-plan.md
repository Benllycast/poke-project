# Phase 3 Implementation Plan: Auth (Registration, Login, Route Protection)

## Context

Phases 1–2 built replication and full local CRUD, but every endpoint has sat behind Spring Boot's
zero-config default-deny security the whole time — Phase 1/2 verification only worked because of the
devtools-generated dev password, and even `GET` requests needed it. Phase 3 replaces that accidental
lockdown with the real, intended split: public registration/login plus public Pokemon reads, JWT-protected
Pokemon writes. This is also the last backend phase — Phase 4 is the frontend, Phase 5 is seed
data/Docker/README.

Per [development-plan.md](development-plan.md) §9 (Phase 3, steps 10–12), this phase delivers `User`
domain/persistence, `AuthService` (BCrypt), a `SecurityConfig` filter chain with JWT, `AuthController`
(`POST /api/auth/register`, `POST /api/auth/login`), and — as a consequence of the same `SecurityConfig`,
not a separate code change — the Phase 2 mutating Pokemon endpoints become genuinely protected instead of
just "protected by accident."

**Definition of done**: `GET /api/pokemon/**` works with no credentials; every mutating Pokemon endpoint
returns 401 with no token and 200/201/204 with a valid one obtained from `/api/auth/login`; a duplicate
registration returns 409; a wrong password returns 401.

## Design decisions

**AuthService authenticates manually, no `UserDetailsService`/`AuthenticationManager`.** Login only needs
to verify a password hash and issue a token — it doesn't need Spring Security's authentication objects.
`AuthService.login` calls `PasswordHasher.matches(raw, user.passwordHash())` directly and, on success,
`TokenProvider.generateToken(user)`. This avoids wiring a `UserDetailsService` + `DaoAuthenticationProvider`
for a code path that would just immediately discard the resulting `Authentication` object anyway.

**Both ports (`PasswordHasher`, `TokenProvider`) exist so `AuthService` stays framework-free and
mockable**, same rule as `PokeApiClientPort`/`PokemonRepositoryPort` in Phases 1–2. `TokenProvider` only
declares `generateToken(User)` — token *parsing* is needed exclusively by `JwtAuthenticationFilter`, which
is itself infrastructure code, so it calls the concrete `JwtTokenProvider`'s parse method directly rather
than routing validation through a domain port that only one infra class would ever use.

**`User.id` is auto-generated, unlike `Pokemon.id`.** `V2__create_user_table.sql` (Phase 0) already
declares `id BIGINT AUTO_INCREMENT PRIMARY KEY` — so `UserRepositoryAdapter.save` is a plain
`JpaRepository.save()` with no merge-as-upsert trick needed; a transient `User` with `id = null` inserts
normally and Hibernate assigns the id.

**JWT secret is externalized with a dev-only fallback.** `jwt.secret: ${JWT_SECRET:<32+ byte dev default>}`
in `application.yaml` — overridable via env var for Docker (Phase 5) without code changes. JJWT's HS256
requires a key ≥256 bits, so the fallback string is deliberately long. `jwt.expiration-ms` defaults to
7,200,000 (2h), matching the main plan.

**`jjwt-jackson` pulls Jackson *2.x* (`com.fasterxml.jackson.databind`), coexisting with Spring Boot
4.1's Jackson *3.x* (`tools.jackson.databind`, per Phase 1's finding) without conflict** — confirmed by
inspecting the resolved jar: different Maven coordinates, different Java packages, no shared classes. No
special handling needed.

**Register also returns a token.** Rather than forcing a client to call `/login` immediately after
`/register`, both endpoints return the same `AuthResponse { token, email, role }` shape — one fewer round
trip for the frontend's signup flow.

**`SecurityConfig` finally makes the public/protected split real, plus fixes the H2 console.**
`SecurityFilterChain`: CSRF disabled (stateless JWT API — no session/cookie to forge against), CORS
allows the Vite dev origin (`http://localhost:5173`), session policy `STATELESS`. Rules: `POST
/api/auth/**` and `GET /api/pokemon/**` → `permitAll`; `/h2-console/**` → `permitAll` with frame options
disabled (the console couldn't render in an iframe under the previous default-deny setup even when the
`dev` profile registered it — this is a real, if minor, regression fix); everything else →
`authenticated`. `JwtAuthenticationFilter` runs before `UsernamePasswordAuthenticationFilter`, reads
`Authorization: Bearer <token>`, and on a valid token sets a `UsernamePasswordAuthenticationToken`
(principal = email, authority = `ROLE_<role>` from the JWT claim) directly — no DB round trip per
request, keeping the filter genuinely stateless. An invalid/expired/missing token just leaves the
`SecurityContext` empty; Spring Security's own entry point produces the 401 for protected routes.

**No `V3__seed_demo_user.sql` yet.** Demo credentials are Phase 5's job (seed data), not this phase's —
Phase 3 is scoped to the auth *mechanism*, proven via a real register→login→authenticated-request flow in
tests, not pre-seeded data.

## Package layout (new files this phase)

```
domain/model/User.java, UserRole.java                      (new)
domain/exception/UserAlreadyExistsException.java             (new)
domain/exception/InvalidCredentialsException.java            (new)
domain/port/UserRepositoryPort.java                          (new)
domain/port/PasswordHasher.java                              (new)
domain/port/TokenProvider.java                                (new)

application/auth/AuthService.java                             (new)

infrastructure/security/BCryptPasswordHasher.java              (new)
infrastructure/security/JwtTokenProvider.java                  (new)
infrastructure/security/JwtAuthenticationFilter.java           (new)
infrastructure/security/SecurityConfig.java                    (new)
infrastructure/persistence/entity/UserEntity.java               (new)
infrastructure/persistence/repository/UserJpaRepository.java    (new)
infrastructure/persistence/mapper/UserEntityMapper.java          (new)
infrastructure/persistence/UserRepositoryAdapter.java            (new)

api/auth/dto/RegisterRequest.java, LoginRequest.java, AuthResponse.java   (new)
api/auth/mapper/AuthDtoMapper.java                              (new)
api/auth/AuthController.java                                     (new)
api/common/GlobalExceptionHandler.java                           (edit: + 2 handlers)

src/main/resources/application.yaml                              (edit: + jwt.secret, jwt.expiration-ms)
```

## Endpoints delivered this phase

| Method | Path | Success | Failure | Auth |
|---|---|---|---|---|
| POST | `/api/auth/register` | 201 `AuthResponse` | 400 validation, 409 `UserAlreadyExistsException` | public |
| POST | `/api/auth/login` | 200 `AuthResponse` | 400 validation, 401 `InvalidCredentialsException` | public |

Plus the side effect on every Phase 2 Pokemon endpoint: `GET /api/pokemon/**` stays public;
`POST`/`PUT`/`DELETE /api/pokemon/**` (including `POST /api/pokemon/sync/{idOrName}`) now require a valid
`Authorization: Bearer <token>` header — enforced by `SecurityConfig`, zero controller changes required.

## Tasks

**T9 — User domain + persistence**
`User`/`UserRole` records; `UserRepositoryPort` (`findByEmail`, `save`); `UserEntity` (mirrors V2 columns,
`role` via `@Enumerated(EnumType.STRING)`); `UserJpaRepository` (+ derived `findByEmail`);
`UserEntityMapper`; `UserRepositoryAdapter`. Test: `@DataJpaTest` — save/reload round-trip, `findByEmail`
empty when missing, duplicate email violates the `V2` unique constraint (mirrors Phase 0's manual H2
verification, now automated).

**T10 — AuthService + password hashing**
`PasswordHasher` port + `BCryptPasswordHasher` (wraps `org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder`,
already on the classpath via `spring-boot-starter-security`); `UserAlreadyExistsException`/
`InvalidCredentialsException`; `AuthService.register`/`.login` per the design above. Unit tests (Mockito,
all 3 ports mocked): register success, register conflict, login success (token from mocked
`TokenProvider`), login wrong password, login unknown email.

**T11 — JWT token provider**
`TokenProvider` port; `JwtTokenProvider` (HS256, `jwt.secret`/`jwt.expiration-ms` externalized). Unit
test: generate → parse round-trip recovers the correct email/role claims; an expired token (construct one
with a near-zero expiration) fails parsing.

**T12 — SecurityConfig + AuthController + real integration test**
`JwtAuthenticationFilter`, `SecurityConfig` (rules above), `RegisterRequest`/`LoginRequest` (Bean
Validation: `@Email @NotBlank` / `@NotBlank @Size(min=8)` on password), `AuthResponse`, `AuthDtoMapper`,
`AuthController`. `GlobalExceptionHandler`: `UserAlreadyExistsException` → 409,
`InvalidCredentialsException` → 401. New test `AuthenticationFlowTest` — a **real** `@SpringBootTest` +
`@AutoConfigureMockMvc` (filters *not* disabled, unlike every `@WebMvcTest` controller test so far — the
whole point is exercising the actual filter chain), against an isolated in-memory H2 (`@ActiveProfiles`
+ a `application-test.yaml` pointing at `jdbc:h2:mem:...` so it never touches the dev file datastore):
register → 201 + token; register same email again → 409; login with the registered credentials → 200 +
token; login with a wrong password → 401; `GET /api/pokemon` with no token → 200 (public); `POST
/api/pokemon` (local-only create, no PokeAPI dependency) with no token → 401; the same request with the
token from login → 201.

## Verification

`./gradlew build` — full suite green (existing 37 + this phase's new tests, including the real security
integration test — no mocking of the filter chain).

Manual: `./gradlew bootRun`, then:
```bash
curl -s -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" -d '{"email":"demo@pokeapp.dev","password":"Demo1234!"}'
# extract the token from the response, then:
curl -s http://localhost:8080/api/pokemon                              # 200, no token needed
curl -s -X POST http://localhost:8080/api/pokemon/sync/25               # 401, no token
curl -s -X POST http://localhost:8080/api/pokemon/sync/25 \
  -H "Authorization: Bearer <token>"                                    # 200, real PokeAPI sync
```
This finally replaces the devtools-generated-password workaround from Phase 1/2's manual checks with the
real mechanism the app ships with.
