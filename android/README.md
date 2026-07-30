# SearchWars Android (Kotlin + Jetpack Compose)

Native Android client for the SearchWars word-guessing game, talking to the
existing Spring Boot backend in this repo.

## Structure
- `app/src/main/java/com/searchwars/app/MainActivity.kt` — Compose entry point, guest-login demo screen
- `app/src/main/java/com/searchwars/app/network/` — Retrofit `ApiClient` + `SearchWarsService` (login, guest, current round)
- `app/src/main/java/com/searchwars/app/ui/theme/` — Material3 theme

## Setup
1. Open the `android/` folder in Android Studio (Koala+).
2. Update `ApiClient.BASE_URL` to the current backend URL (Elastic Beanstalk / Railway).
3. Sync Gradle and run on an emulator or device.

## Status
Skeleton stage: guest-login flow wired up end-to-end as a starting point.
Next: full auth screens, word-round gameplay UI, score/leaderboard screens.
