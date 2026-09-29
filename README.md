# MovieAI

Movie and TV discovery app backed by real TMDB data.

```
film/
├── Create app/          React 19 + TypeScript + Vite + Tailwind v4 frontend
├── backend/             Java 21 + Spring Boot 3 REST API (TMDB integration, auth, watchlist)
└── docker-compose.yml   PostgreSQL (and optional Redis / containerised backend)
```

The browser only ever talks to the MovieAI backend under `/api/v1`. The backend is the only component that
holds the TMDB token and calls TMDB.

## Prerequisites

- Node.js 22.12+ and pnpm 9+
- JDK 21+ (the Maven wrapper downloads Maven itself)
- Docker (for PostgreSQL), or any PostgreSQL 14+ instance
- A TMDB API **Read Access Token** — <https://www.themoviedb.org/settings/api>

## Quick start

```bash
# 1. Database
cp .env.example .env                 # optional: change the Postgres password
docker compose up -d

# 2. Backend (http://localhost:8080)
cd backend
cp .env.example .env                 # set TMDB_ACCESS_TOKEN, JWT_SECRET, JWT_REFRESH_SECRET, DATABASE_PASSWORD
./mvnw spring-boot:run               # Windows: .\mvnw.cmd spring-boot:run

# 3. Frontend (http://localhost:8443)
cd "../Create app"
pnpm install
pnpm dev
```

The Vite dev server proxies `/api` to `http://localhost:8080`, so the frontend needs no configuration in
development and the refresh-token cookie stays same-origin.

Flyway creates the schema on first start (`backend/src/main/resources/db/migration`).

## Configuration

### Backend (`backend/.env` or real environment variables)

| Variable | Purpose |
| --- | --- |
| `TMDB_ACCESS_TOKEN` | TMDB v4 read access token (server-side only) |
| `TMDB_API_BASE_URL` | Defaults to `https://api.themoviedb.org/3` |
| `DATABASE_URL` | JDBC URL or `postgres://user:pass@host:port/db` |
| `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Database credentials (not needed if embedded in `DATABASE_URL`) |
| `JWT_SECRET` / `JWT_REFRESH_SECRET` | Two different secrets, 32+ bytes each |
| `CORS_ALLOWED_ORIGINS` | Comma-separated frontend origins |
| `REFRESH_COOKIE_SECURE` | `true` in production (HTTPS) |
| `REFRESH_COOKIE_SAME_SITE` | `Lax` (same site) or `None` (frontend and API on different sites) |
| `FORWARD_HEADERS_STRATEGY` | `native` behind a trusted reverse proxy, so rate limiting sees real client IPs |
| `JWT_EXPIRATION` / `JWT_REFRESH_EXPIRATION` | Token lifetimes, default `15m` / `30d` |
| `SPRING_PROFILES_ACTIVE` | `prod` in production (see below) |
| `API_DOCS_ENABLED` | Swagger UI / OpenAPI; on locally, off under `prod` |

Locally, if the JWT secrets are missing the backend starts with random ephemeral keys and logs a warning
(sessions won't survive a restart). Under the `prod` profile missing secrets fail startup. Secrets shorter
than 32 bytes always fail startup.

### Frontend (`Create app/.env.local`)

| Variable | Purpose |
| --- | --- |
| `VITE_API_URL` | Backend origin in production (e.g. `https://api.example.com`). Leave empty for same-origin. |

Never put secrets in `VITE_` variables — they are bundled into the browser build.

## Production deployment (Render + Vercel)

```
Browser ──► Vercel (Create app/, static SPA) ──► Render web service (backend/, Docker, Java 21) ──► Render PostgreSQL
                                                   └──► TMDB (token stays on Render)
```

### 1. Backend + database on Render

1. Render Dashboard → **New → Blueprint** → select this repository. Render reads `render.yaml` and creates
   the `movieai-backend` web service (Docker) and the `movieai-db` PostgreSQL database in Frankfurt.
2. When prompted, fill in:
   - `TMDB_ACCESS_TOKEN` — TMDB v4 read access token
   - `CORS_ALLOWED_ORIGINS` — your Vercel URL, e.g. `https://movieai.vercel.app` (no trailing slash).
     If you don't know it yet, enter a placeholder and update it after step 2.
3. `DATABASE_URL`, `JWT_SECRET` and `JWT_REFRESH_SECRET` are wired/generated automatically. Flyway creates the
   schema on first start. Health check: `https://<service>.onrender.com/actuator/health` → `{"status":"UP"}`.

`SPRING_PROFILES_ACTIVE=prod` refuses to start if a required variable is missing, forces `Secure` cookies,
sets `SameSite=None` for the refresh cookie (frontend and API are on different sites), trusts Render's
`X-Forwarded-*` headers and disables Swagger UI.

### 2. Frontend on Vercel

1. Vercel → **Add New → Project** → import this repository.
2. **Root Directory:** `Create app` (framework preset: Vite; build `pnpm run build`, output `dist`).
3. Environment variable: `VITE_API_URL=https://<service>.onrender.com` (Production and Preview).
4. Deploy, then put the resulting URL into Render's `CORS_ALLOWED_ORIGINS`. Preview deployments can be allowed
   with a pattern, e.g. `https://movieai.vercel.app,https://movieai-*-<team>.vercel.app`.

`vercel.json` rewrites unknown paths to `index.html`, so deep links such as `/movie/438631` survive a refresh.

### Custom domains (recommended)

Browsers that block third-party cookies (Safari, some privacy modes) drop the refresh cookie when the
frontend and API are on different sites, which signs users out on reload. Serving both from one domain fixes
this: e.g. `movieai.example.com` on Vercel and `api.movieai.example.com` on Render (CNAME records as shown
in each dashboard), then set `VITE_API_URL=https://api.movieai.example.com`,
`CORS_ALLOWED_ORIGINS=https://movieai.example.com` and `REFRESH_COOKIE_SAME_SITE=Lax`.

## API

- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- Swagger UI: <http://localhost:8080/swagger-ui.html>

All errors use one envelope:

```json
{ "success": false, "error": { "code": "TMDB_UNAVAILABLE", "message": "Movie data is temporarily unavailable." } }
```

Main groups: `/movies`, `/tv`, `/people`, `/search`, `/recommendations/movies`, `/auth`, `/users/me`,
`/watchlist`.

## How it works

- **TMDB layer** — `TmdbClient` (timeouts, one retry on 429/5xx, concurrency cap) feeds `TmdbMovieService`,
  `TmdbTvService`, `TmdbPersonService` and `TmdbImageService`, which map TMDB payloads to internal DTOs.
  Each detail page is a single TMDB call using `append_to_response`.
- **Caching** — Caffeine caches per data type (trending 30 min, lists 1 h, details 6 h, genres/config
  24 h). Every successful response is also kept in a 7-day stale cache that is served if TMDB is down.
  Concurrent identical requests share one TMDB call. The cache abstraction is Spring's, so Redis can be
  swapped in later (`docker compose --profile redis up -d`).
- **Auth** — BCrypt password hashes; 15-minute JWT access tokens kept in memory by the frontend; 30-day
  rotating refresh tokens in an HttpOnly cookie scoped to `/api/v1/auth`, stored hashed in the database,
  with reuse detection that revokes the whole token family.
- **Watchlist** — stored per user as `(media_type, tmdb_id)` with a unique constraint; responses are
  `Cache-Control: no-store, private`, and the frontend scopes every watchlist query by user id and drops it
  on sign-out.
- **Rate limiting** — per-IP (or per-user for the watchlist) token buckets, stricter for login/register,
  search and recommendations. Exceeding a limit returns `429 RATE_LIMITED` with `Retry-After`.

## Tests and builds

```bash
cd backend && ./mvnw test            # Spring Boot tests against H2 + a fake TMDB server
cd "Create app" && pnpm test         # Vitest + Testing Library
cd "Create app" && pnpm build        # type-check + production build
```

## Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB. Streaming availability data is
provided by JustWatch.
