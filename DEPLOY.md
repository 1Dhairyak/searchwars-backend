# SearchWars Backend — Deployment Guide

Spring Boot 3 + PostgreSQL | Render (free web service) + Neon (free Postgres)

---

## 1. Database — Neon

1. Create a project at [neon.tech](https://neon.tech) (free plan).
2. Region **AWS Asia Pacific (Singapore)**, same as Render. Dashboard → **Connect**, connection pooling OFF → copy host, database, user, password.
3. Build the JDBC URL:
   ```
   jdbc:postgresql://<host>/<database>?sslmode=require
   ```
Tables are created automatically on first boot and `DataSeeder` loads the game items.

## 2. Backend — Render

**Quick way:** [render.com](https://render.com) → **New → Blueprint** → pick this repo.
`render.yaml` sets everything (Docker, Free, Singapore, health check, generated `JWT_SECRET`);
Render only asks for `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

**Manual way:**

1. [render.com](https://render.com) → **New → Web Service** → connect this GitHub repo.
2. Runtime: **Docker** (uses `Dockerfile`), plan: **Free**.
3. Advanced → Health Check Path: `/actuator/health`.
4. Environment variables (all required — the app refuses to start without them):

| Variable | Value |
|---|---|
| `DB_URL` | JDBC URL from step 1 |
| `DB_USERNAME` | Neon user |
| `DB_PASSWORD` | Neon password |
| `JWT_SECRET` | random string, 32+ chars |
| `GOOGLE_CLIENT_ID` | Google OAuth Web client ID |

Every push to `main` redeploys automatically.

## 3. Frontend — Vercel

Vercel project → Settings → Environment Variables:

| Variable | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://<your-service>.onrender.com/api` |

Then redeploy. CORS already allows `search-wars.vercel.app`, its preview URLs and `localhost:5173` (`WebConfig.java`).

## 4. Keep it awake

Render free services sleep after 15 min idle (~30-60s cold start).
Add a free [UptimeRobot](https://uptimerobot.com) HTTP monitor on
`https://<your-service>.onrender.com/actuator/health`, every 10 minutes.

## Verify

```
GET https://<your-service>.onrender.com/actuator/health   -> {"status":"UP"}
GET https://<your-service>.onrender.com/api/leaderboard
```

---

## Local development

Requires Java 21+, Maven, PostgreSQL on `localhost:5432` with database `higher_lower_db` (user/password `postgres`/`postgres`, see `application.properties`).

```bash
mvn spring-boot:run
```

API at `http://localhost:8080`.
