# Phase 4 Checklist

Tracks every individual file/task from [phase-4-frontend-plan.md](phase-4-frontend-plan.md). Check items
off as they land.

## T13 — API client + PokemonListPage (US-01)

- [x] Create `.env.example` (`VITE_API_BASE_URL=http://localhost:8080`)
- [x] Create `src/api/apiClient.js` (fetch wrapper, `Authorization` header from
      `localStorage.getItem('pokeapp_token')`, JSON parsing, `ApiError { status, message }` on non-2xx)
- [x] Create `src/api/pokemonApi.js` (`list(page, size)`, `getById(id)`)
- [x] Create `src/components/PokemonCard.jsx` (sprite, category, mass, abilities+moves)
- [x] Create `src/components/Pagination.jsx`
- [x] Create `src/routes/PokemonListPage.jsx` (`useQuery`, loading/error/empty states)
- [x] Edit `src/App.jsx`: wire `/` route

Verify:
- [x] `PokemonCard.test.jsx`: renders sprite, category, mass, and skills from a stub Pokemon
- [x] `PokemonListPage.test.jsx` (`pokemonApi` mocked): renders one card per item in a stubbed page
      response
- [x] `PokemonListPage.test.jsx`: clicking next page calls `list` with the incremented page number
- [x] `npm run test -- --run PokemonCard PokemonListPage` passes (4/4)

## T14 — PokemonDetailPage (US-02)

- [ ] Create `src/components/StatBar.jsx` (stat name + bar sized by `baseStat`)
- [ ] Create `src/components/EvolutionChain.jsx` (ordered stage list with `minLevel`)
- [ ] Create `src/routes/PokemonDetailPage.jsx` (`useQuery`, loading + 404-aware error state)
- [ ] Edit `src/App.jsx`: wire `/pokemon/:id` route

Verify:
- [ ] `PokemonDetailPage.test.jsx`: renders image, stats, description, evolution chain from a stubbed
      detail response
- [ ] `PokemonDetailPage.test.jsx`: a mocked `ApiError{status:404}` renders a not-found state, no crash
- [ ] `npm run test -- --run PokemonDetailPage` passes

## T15 — Auth: AuthContext, LoginPage, RegisterPage, ProtectedRoute

- [ ] Create `src/api/authApi.js` (`register`, `login`)
- [ ] Create `src/context/AuthContext.jsx` (`{ token, email }` state seeded from `localStorage`,
      `login`/`register`/`logout`, `isAuthenticated`)
- [ ] Create `src/components/ProtectedRoute.jsx` (redirect to `/login`, preserve attempted location)
- [ ] Create `src/components/NavBar.jsx` (logged-out vs logged-in nav state)
- [ ] Create `src/routes/LoginPage.jsx`
- [ ] Create `src/routes/RegisterPage.jsx`
- [ ] Edit `src/main.jsx`: wrap `<App/>` in `<AuthProvider>`
- [ ] Edit `src/App.jsx`: wire `/login`, `/register` routes + render `NavBar`

Verify:
- [ ] `AuthContext.test.jsx`: successful login updates context state and `localStorage`
- [ ] `AuthContext.test.jsx`: failed login (mocked 401) leaves state unauthenticated
- [ ] `ProtectedRoute.test.jsx`: redirects to `/login` when logged out
- [ ] `ProtectedRoute.test.jsx`: renders children when logged in
- [ ] `npm run test -- --run AuthContext ProtectedRoute` passes

## T16 — Sync control + PokemonForm (edit) + delete (US-03/US-04)

- [ ] Edit `src/api/pokemonApi.js`: add `sync(idOrName)`, `update(id, payload)`, `remove(id)`
- [ ] Create `src/components/SyncForm.jsx` (protected, idOrName input, mutate + invalidate list +
      navigate to new detail)
- [ ] Create `src/components/PokemonForm.jsx` (controlled fields: name/weight/height/localizedName/
      region/tags, pre-filled)
- [ ] Create `src/routes/PokemonEditPage.jsx` (load record, render form, mutate `update`, surface
      400/404)
- [ ] Edit `src/routes/PokemonDetailPage.jsx`: add protected delete button (confirm guard, mutate
      `remove`, invalidate list, navigate to `/`)
- [ ] Edit `src/routes/PokemonListPage.jsx`: render `SyncForm`
- [ ] Edit `src/App.jsx`: wire `/pokemon/:id/edit` behind `ProtectedRoute`

Verify:
- [ ] `PokemonForm.test.jsx`: pre-fills from an existing record
- [ ] `PokemonForm.test.jsx`: submit calls `onSubmit` with the merged payload
- [ ] `PokemonEditPage.test.jsx` (mutation mocked): success path invalidates + navigates
- [ ] `PokemonEditPage.test.jsx`: a mocked 400 renders the validation message inline
- [ ] `npm run test -- --run PokemonForm PokemonEditPage` passes

## T17 — Cleanup, NotFoundPage, and a full manual pass

- [ ] Create `src/routes/NotFoundPage.jsx`
- [ ] Edit `src/App.jsx`: wire `*` → `NotFoundPage`
- [ ] Replace `src/App.test.jsx` (stale placeholder-`<h1>` assertion) with a router smoke test
      (`MemoryRouter`+`QueryClientProvider`+`AuthProvider`, asserts list page + nav render)
- [ ] `npm run lint` clean

Verify:
- [ ] `App.test.jsx` passes
- [ ] Manual click-through (both servers running): list → detail → register → login → sync → edit →
      delete → logout → protected-route redirect to `/login`
- [ ] Browser console clear of warnings/errors through the full click-through above

## Overall Phase 4 Verification

- [ ] `npm run lint` passes
- [ ] `npm run test` passes (full suite)
- [ ] `npm run build` passes
- [ ] Manual: `./gradlew bootRun` (backend) + `npm run dev` (frontend) both running, list loads with no
      token
- [ ] Manual: register → log in → sync a real Pokemon by id → appears in list
- [ ] Manual: detail page shows stats/description/evolution chain for the synced Pokemon
- [ ] Manual: edit `localizedName`/`region`/`tags`, reload, confirm the change persisted
- [ ] Manual: delete the Pokemon, confirm it's gone from the list
- [ ] Manual: log out, hit `/pokemon/<id>/edit` directly, confirm redirect to `/login`
- [ ] `git status` on `backend/` — clean; every Phase 4 commit touches `frontend/` only
