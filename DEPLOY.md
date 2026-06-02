# SearchWars Backend — Deployment Guide

Spring Boot 3 + PostgreSQL 15 | Deploy on Railway

---

## Deploy to Railway (5 minutes)

### Step 1 — Push this folder to a new GitHub repo

```bash
cd higher-lower-game-backend
git init
git add .
git commit -m "feat: initial backend"
git remote add origin https://github.com/YOUR_USERNAME/searchwars-backend.git
git push -u origin main
```

### Step 2 — Create Railway project

1. Go to [railway.app](https://railway.app) and sign in with GitHub
2. Click **New Project → Deploy from GitHub repo**
3. Select your `searchwars-backend` repo
4. Railway auto-detects the Dockerfile ✅

### Step 3 — Add PostgreSQL database

1. In your Railway project, click **+ New** → **Database** → **PostgreSQL**
2. Railway auto-sets `DATABASE_URL` as an environment variable — no manual config needed ✅

### Step 4 — Set environment variables

In Railway → your backend service → **Variables**, add:

| Variable | Value |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |

That's it. `DATABASE_URL` and `PORT` are injected automatically by Railway.

### Step 5 — Get your backend URL

After deploy succeeds (2-3 min), Railway gives you a URL like:
```
https://searchwars-backend-production.up.railway.app
```

---

## Connect Frontend (Vercel)

1. Go to your Vercel project → **Settings → Environment Variables**
2. Add:

| Variable | Value |
|---|---|
| `VITE_API_BASE_URL` | `https://YOUR-RAILWAY-URL.up.railway.app/api` |

3. Redeploy the frontend on Vercel (Deployments → Redeploy)

---

## Verify it's working

Hit these URLs in your browser after deploy:

```
GET  https://YOUR-RAILWAY-URL.up.railway.app/actuator/health
GET  https://YOUR-RAILWAY-URL.up.railway.app/api/leaderboard
```

Both should return JSON. Then open your Vercel frontend — game should load! 🎮

---

## Local development

```bash
# Requires: Java 21, Maven, PostgreSQL running locally

cp src/main/resources/application.properties src/main/resources/application-local.properties
# Edit application-local.properties with your local DB credentials

mvn spring-boot:run -Dspring-boot.run.profiles=local
```

API available at `http://localhost:8080`
