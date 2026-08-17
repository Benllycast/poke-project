# Phase 4 Implementation Plan: Frontend Features

## Context

Phases 0–3 delivered a fully working, tested backend: PokeAPI sync (US-03), paginated list + detail reads
(US-01/US-02), full local CRUD (US-04), and JWT auth gating every mutating endpoint. The frontend
(`frontend/pokeapi-front`) is still exactly what Phase 0 left it: `App.jsx` renders a bare `<h1>Pokedex</h1>`
inside the `BrowserRouter`/`QueryClientProvider` shell wired in `main.jsx`, with `react-router-dom` and
`@tanstack/react-query` installed but unused, and no `src/api/`, `src/context/`, `src/routes/`, or
`src/components/` directories yet.

Per [development-plan.md](development-plan.md) §6 and §9 (Phase 4, steps 13–17), this phase builds the
first full-stack demo: a routed React app that lists Pokemon from the real backend, shows a detail view
with stats/description/evolution chain, lets a logged-in user sync new Pokemon from PokeAPI and edit/delete
local records, and gates the mutating actions behind login. This is the first phase where the frontend
becomes real product code instead of scaffold.

**Definition of done**: `npm run build`, `npm run lint`, and `npm run test` all pass; with the backend
running (`./gradlew bootRun`) and the frontend dev server running (`npm run dev`), a user can browse the
paginated Pokemon list with no token, open a detail page with stats/evolution chain, register/log in,
sync a new Pokemon by id/name, edit a Pokemon's proprietary fields (localized name/region/tags) and see
the change persist, delete a Pokemon, and get redirected to `/login` when hitting a protected route
unauthenticated — all with zero browser console errors/warnings.

## Design decisions

**`apiClient.js` reads the JWT from `localStorage` directly, not from React context.** The API layer
(`src/api/*.js`) has to be usable from anywhere — including outside the React tree in tests — so it stays
framework-agnostic: a plain `fetch` wrapper that reads `localStorage.getItem('pokeapp_token')` for the
`Authorization` header, parses JSON, and throws a typed `ApiError { status, message }` built from the
backend's `ApiErrorResponse { status, error, message, path, timestamp }` shape on non-2xx responses.
`AuthContext` is the only thing that *writes* to that `localStorage` key (on login/register/logout); the
two stay loosely coupled through the storage key, not a prop or import cycle.

**No manual "create a blank local-only Pokemon" form.** The backend exposes `POST /api/pokemon` for that,
but the realistic creation path per US-03 is sync-from-PokeAPI, not hand-typing a Pokemon's full stat
block. Phase 4 ships a small protected "sync by id or name" control (calls
`POST /api/pokemon/sync/{idOrName}`) as the only creation path; `PokemonForm` is edit-only (US-04: "allow
updates to any Pokemon currently stored"). This is a deliberate scope cut, not an oversight — it also
means the list starts empty and demoable without waiting on Phase 5's seed data.

**`PokemonForm` only exposes the proprietary fields (`localizedName`, `region`, `tags`) plus the handful
of replicated fields worth hand-editing (`name`, `weight`, `height`).** Editing `abilities`/`moves`/
`stats`/`evolutionChain` through a form would mean building list-of-objects editors for data that's
supposed to be replicated *from* PokeAPI, not hand-maintained — out of scope for what US-04 asks for and
not a good use of Phase 4's time. The `PUT` request still round-trips the full `PokemonResponse` payload
(spread the loaded record, override just the edited fields) so unedited replicated fields survive the
update untouched.

**`ProtectedRoute` wraps route elements, not individual buttons.** `/pokemon/:id/edit` is the only
protected route; the sync control and delete button are protected by simply not rendering when
`!isAuthenticated` (checked via `useAuth()`), consistent with how the backend treats `GET` as always
public and mutations as always protected — no route ever needs a *partial* auth state.

**Router stays declarative (`<Routes>`/`<Route>`), not the v7 data router.** `main.jsx` already wraps
`<App />` in `BrowserRouter` from Phase 0; switching to `createBrowserRouter` would mean restructuring
that entry point for loader/action patterns this app doesn't need. `<Routes>` inside `App.jsx` is the
smaller diff and matches the plan's original `src/routes/` structure (page components, not route-module
objects).

**Base API URL is a Vite env var (`VITE_API_BASE_URL`), defaulting to `http://localhost:8080`.** Lets
Phase 5's Docker Compose point the built frontend at a different origin (or a reverse-proxied `/api`
path) without a code change — `.env.example` documents the variable for the README.

## Package layout (new files this phase)

```
src/api/apiClient.js                 (new) — fetch wrapper, ApiError, Authorization header
src/api/pokemonApi.js                (new) — list, getById, sync, create, update, remove
src/api/authApi.js                   (new) — register, login

src/context/AuthContext.jsx          (new) — token/email state, localStorage persistence, login/register/logout

src/components/ProtectedRoute.jsx    (new)
src/components/NavBar.jsx            (new) — brand link, login state, logout button
src/components/PokemonCard.jsx       (new) — US-01: sprite, category, mass, skills summary
src/components/Pagination.jsx        (new)
src/components/SyncForm.jsx          (new) — protected idOrName input, calls sync
src/components/StatBar.jsx           (new) — US-02: one StatValue as a labeled bar
src/components/EvolutionChain.jsx    (new) — US-02: ordered EvolutionStage list
src/components/PokemonForm.jsx       (new) — US-04: edit proprietary + core fields

src/routes/PokemonListPage.jsx       (new)
src/routes/PokemonDetailPage.jsx     (new)
src/routes/PokemonEditPage.jsx       (new)
src/routes/LoginPage.jsx             (new)
src/routes/RegisterPage.jsx          (new)
src/routes/NotFoundPage.jsx          (new)

src/App.jsx                          (edit) — NavBar + <Routes> table, replaces placeholder h1
src/main.jsx                         (edit) — wrap <App/> in <AuthProvider>
.env.example                         (new) — VITE_API_BASE_URL=http://localhost:8080
```

## Routes delivered this phase

| Path | Page | Auth |
|---|---|---|
| `/` | `PokemonListPage` (US-01) | public |
| `/pokemon/:id` | `PokemonDetailPage` (US-02) | public |
| `/pokemon/:id/edit` | `PokemonEditPage` (US-04) | protected |
| `/login` | `LoginPage` | public |
| `/register` | `RegisterPage` | public |
| `*` | `NotFoundPage` | public |

Sync (US-03) is a control embedded in `PokemonListPage`, not a standalone route — it always returns to
the same list.

## Tasks

**T13 — API client + PokemonListPage (US-01)**
`apiClient.js` (fetch wrapper + `ApiError`), `pokemonApi.js` (`list(page, size)`, `getById(id)`), env var
wiring (`.env.example`, `vite.config.js` already exposes `import.meta.env` for free). `PokemonCard`
(sprite, category, mass, abilities+moves as a skills list), `Pagination` (prev/next + page indicator),
`PokemonListPage` (`useQuery(['pokemon', 'list', page, size], ...)`, loading/error/empty states). Wire
`/` in `App.jsx`. Component tests: `PokemonCard` renders all required fields; `PokemonListPage` (api
mocked via `vi.mock`) renders cards from a stubbed page response and calls `list` with the right
page/size on pagination click.

**T14 — PokemonDetailPage (US-02)**
`pokemonApi.getById` (already added in T13, used here). `StatBar` (one stat name + bar sized by
`baseStat`), `EvolutionChain` (ordered stage list with `minLevel`), `PokemonDetailPage`
(`useQuery(['pokemon', 'detail', id], ...)`, loading/404-aware error state using the thrown `ApiError`'s
status). Wire `/pokemon/:id`. Component tests: `PokemonDetailPage` renders image/stats/description/evolution
chain from a stubbed detail response; a mocked 404 `ApiError` renders a "not found" state instead of
crashing.

**T15 — Auth: AuthContext, LoginPage, RegisterPage, ProtectedRoute**
`authApi.js` (`login`, `register`, both hit `AuthResponse { token, email }`). `AuthContext.jsx`
(`useState` for `{ token, email }` seeded from `localStorage`, `login`/`register`/`logout` that call the
API then write/clear `localStorage` + state, `isAuthenticated` derived from `token != null`).
`ProtectedRoute` (redirects to `/login` with the attempted location in router state when unauthenticated).
`LoginPage`/`RegisterPage` (controlled form, submit → context method → navigate to `/` on success, render
the backend's validation/409/401 message on failure). `NavBar` (shows "Log in"/"Register" or the logged-in
email + "Log out"). Wrap `<App />` with `<AuthProvider>` in `main.jsx`; add `/login`, `/register` routes.
Component tests: `AuthContext` — login success updates state + localStorage, login failure leaves state
unauthenticated; `ProtectedRoute` — redirects when logged out, renders children when logged in.

**T16 — Sync control + PokemonForm (edit) + delete (US-03/US-04)**
`pokemonApi.sync(idOrName)`, `.update(id, payload)`, `.remove(id)`. `SyncForm` (protected — renders only
when `isAuthenticated`; idOrName text input, submit → `useMutation` → invalidate the list query →
navigate to the new detail page). `PokemonForm` (controlled inputs for `name`/`weight`/`height`/
`localizedName`/`region`/`tags`, pre-filled from the loaded record, submits the full merged payload).
`PokemonEditPage` (loads the record, renders `PokemonForm`, `useMutation` for `update` with
cache invalidation on success, surfaces 400 field errors and 404 inline). Delete button on
`PokemonDetailPage` (protected, `window.confirm` guard, `useMutation` for `remove` → invalidate list →
navigate to `/`). Wire `/pokemon/:id/edit` behind `ProtectedRoute`. Component tests: `PokemonForm` —
renders existing values, calls `onSubmit` with the merged payload; `PokemonEditPage`/delete flow with the
mutation mocked.

**T17 — Cleanup, NotFoundPage, and a full manual pass**
`NotFoundPage` for unmatched routes. Replace the now-stale `App.test.jsx` (asserted the placeholder `<h1>`
that T13 removes) with a router smoke test — renders `<App/>` wrapped in `MemoryRouter`+
`QueryClientProvider`+`AuthProvider` at `/`, asserts the list page and nav render with no console errors.
Run `npm run lint` and fix anything the new components trip. Manual pass with both servers running: click
through list → detail → login → sync → edit → delete → logout → protected-route redirect, watching the
browser console the whole time for warnings (React key warnings, unhandled promise rejections, etc.) —
the "no browser console warnings" line item from `frontend-requirements.md`.

## Verification

`npm run lint && npm run test && npm run build` from `frontend/pokeapi-front/` — all green.

Manual, with `./gradlew bootRun` (backend, port 8080) and `npm run dev` (frontend, port 5173) both
running: open `http://localhost:5173`, confirm the list loads with no token; register a new account; log
in; sync a Pokemon by id (e.g. `25` for pikachu) and confirm it appears in the list; open its detail page
and confirm stats/description/evolution chain render; edit its `localizedName`/`region`/`tags` and confirm
the change persists on reload; delete it and confirm it's gone from the list; log out and confirm
`/pokemon/<id>/edit` redirects to `/login`; check the browser console for warnings/errors throughout.
