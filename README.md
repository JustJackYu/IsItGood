# IsItGood?

An AI-powered game discovery app — get honest, personalized review summaries, find the best price deals, and ask an AI anything about a game.

> **Status:** live on **Google Play (internal testing)**. Full-stack Android + backend, built and deployed solo.

## Features

- **AI review summaries** — Google Gemini combined with live web search (Tavily) produces an honest verdict for any game, personalized to your preferences (length, tone, what you care about).
- **Context-aware AI chat** — ask follow-up questions about a game; answers use the summary plus fresh web results.
- **Price deals** — current prices per store and a **"Popular Games on Sale"** feed (≥50% off) via IsThereAnyDeal, ranked by popularity (RAWG).
- **Saved games** with optional **sale-alert thresholds** that highlight a game when a deal beats your target discount/price.
- **Preferences** — summary length, tone, focus areas, mature-content toggle, font size (live-scaled), and notifications.
- **Accounts** — email/password auth with JWT + bcrypt, 30-day persisted login, change password, and self-service account deletion.

## Tech stack

| Layer | Technologies |
|-------|--------------|
| **Android** | Kotlin, Jetpack Compose, MVVM, Navigation Compose, Retrofit/OkHttp, Coil, DataStore, Coroutines + StateFlow |
| **Backend** | Node.js, TypeScript, Express, Prisma |
| **Database** | PostgreSQL |
| **AI / external APIs** | Google Gemini, Tavily (web search), RAWG (game data), IsThereAnyDeal (pricing) |
| **Infra / tooling** | Railway (API + managed Postgres), Google Play, Jest |

## Architecture

```
Android app (Compose / MVVM)
        │  HTTPS (JWT)
        ▼
Express REST API  ──►  PostgreSQL (Prisma)
        │
        └──►  Gemini · Tavily · RAWG · IsThereAnyDeal
```

All external API keys live **server-side**; the app only knows the backend URL. Summaries are cached per user for an hour to cut latency and API cost.

## Project structure

```
backend/   Express + TypeScript API
  src/routes       auth, games, chat, preferences
  src/services     gemini, tavily, rawg, itad, deals, preferences, summary
  prisma/          schema + migrations
  tests/           Jest unit tests
frontend/  Android app (com.juhyeonyu.isitgood)
  ui/screens, ui/viewmodel, data/{remote,model,repository,local}
```

## Running locally

### Backend
```bash
cd backend
cp .env.example .env      # fill in DATABASE_URL, JWT_SECRET, and the API keys
npm install
npm run dev               # http://localhost:3000
npm test                  # unit tests
```

### Android
Open `frontend/` in Android Studio and run the **debug** build on an emulator — it points at `http://10.0.2.2:3000` (your local backend). Release builds point at the deployed backend.

## Notes

- Secrets are provided via environment variables and are never committed.
- Database schema changes are managed with Prisma migrations.
