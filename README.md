# Higher or Lower Game — Backend

Enterprise-grade Spring Boot backend for the Higher or Lower web game.  
Players compare two items and guess which has more monthly Google search volume.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Runtime      | Java 21                             |
| Framework    | Spring Boot 3.3                     |
| Database     | PostgreSQL 15+                      |
| ORM          | Spring Data JPA / Hibernate         |
| Mapping      | Hand-rolled mapper (ItemMapper)     |
| Caching      | Spring Cache (simple / Redis-ready) |
| Validation   | Jakarta Bean Validation             |
| Build        | Maven                               |
| Testing      | JUnit 5 + Mockito + MockMvc         |

---

## Project Structure

```
src/main/java/com/higherlower/game/
├── HigherLowerGameApplication.java   # Boot entry point
│
├── config/
│   ├── WebConfig.java                # CORS configuration
│   └── DataSeeder.java               # Dev seed data (non-prod only)
│
├── controller/
│   ├── GameController.java           # GET /api/game/start, POST /api/game/guess
│   └── LeaderboardController.java    # GET /api/leaderboard
│
├── service/
│   ├── GameService.java              # Interface
│   ├── LeaderboardService.java       # Interface
│   └── impl/
│       ├── GameServiceImpl.java      # Core game loop logic
│       └── LeaderboardServiceImpl.java
│
├── repository/
│   ├── ItemRepository.java
│   ├── GameSessionRepository.java
│   └── LeaderboardRepository.java
│
├── entity/
│   ├── Item.java                     # Game item (title, searchVolume, …)
│   ├── GameSession.java              # Per-player session state
│   ├── LeaderboardEntry.java         # Immutable score record
│   └── enums/
│       ├── GameStatus.java           # ACTIVE | GAME_OVER | ABANDONED
│       └── GuessType.java            # HIGHER | LOWER
│
├── dto/
│   ├── request/
│   │   ├── StartGameRequest.java
│   │   └── GuessRequest.java
│   └── response/
│       ├── ApiResponse.java          # Generic envelope { success, message, data }
│       ├── ItemDto.java
│       ├── GameRoundDto.java
│       ├── GuessResultDto.java
│       └── LeaderboardDto.java
│
├── exception/
│   ├── GameSessionNotFoundException.java
│   ├── InsufficientItemsException.java
│   ├── InvalidGuessException.java
│   └── GlobalExceptionHandler.java   # @RestControllerAdvice
│
└── util/
    └── ItemMapper.java               # Entity ↔ DTO conversion
```

---

## Quick Start

## Game Logic

### Duplicate Prevention
Each `GameSession` maintains a `seenItemIds` list (comma-delimited IDs).  
The repository queries for random items **excluding all seen IDs**, so an item can never appear twice in the same session.

### Tie-Breaking
When both items have identical search volumes, a `HIGHER` guess is treated as **correct** (player-friendly design).

### Session Lifecycle
```
[New Request] → ACTIVE → (correct guesses) → ACTIVE
                       → (wrong guess)     → GAME_OVER → [Leaderboard Entry Written]
```

---

