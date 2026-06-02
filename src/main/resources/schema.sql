-- ============================================================
--  Higher Lower Game — PostgreSQL Schema
--  Run this once against your database before starting the app.
--  Spring Boot with ddl-auto=update will also create tables,
--  but this file gives you full control (indexes, constraints).
-- ============================================================

-- ── Database & User Setup (run as superuser) ─────────────────
-- CREATE DATABASE higher_lower_db;
-- CREATE USER higher_lower_user WITH ENCRYPTED PASSWORD 'your_password';
-- GRANT ALL PRIVILEGES ON DATABASE higher_lower_db TO higher_lower_user;

-- ── Items ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS items (
    id          VARCHAR(36)  NOT NULL,
    title       VARCHAR(200) NOT NULL,
    image_url   TEXT         NOT NULL,
    search_volume BIGINT     NOT NULL CHECK (search_volume >= 0),
    category    VARCHAR(100),
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_items      PRIMARY KEY (id),
    CONSTRAINT uq_items_title UNIQUE (title)
);

CREATE INDEX IF NOT EXISTS idx_item_category ON items (category);
CREATE INDEX IF NOT EXISTS idx_item_active   ON items (active);

-- ── Game Sessions ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS game_sessions (
    id              VARCHAR(36)  NOT NULL,
    player_name     VARCHAR(100),
    current_item_id VARCHAR(36),
    seen_item_ids   TEXT         DEFAULT '',
    score           INTEGER      NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_game_sessions PRIMARY KEY (id),
    CONSTRAINT chk_session_status CHECK (status IN ('ACTIVE','GAME_OVER','ABANDONED')),
    CONSTRAINT chk_session_score  CHECK (score >= 0)
);

CREATE INDEX IF NOT EXISTS idx_session_status     ON game_sessions (status);
CREATE INDEX IF NOT EXISTS idx_session_player     ON game_sessions (player_name);
CREATE INDEX IF NOT EXISTS idx_session_created_at ON game_sessions (created_at DESC);

-- ── Leaderboard ───────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS leaderboard (
    id          VARCHAR(36)  NOT NULL,
    session_id  VARCHAR(36)  NOT NULL,
    player_name VARCHAR(100) NOT NULL,
    score       INTEGER      NOT NULL CHECK (score >= 0),
    achieved_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_leaderboard       PRIMARY KEY (id),
    CONSTRAINT uq_leaderboard_session UNIQUE (session_id)   -- one entry per session
);

CREATE INDEX IF NOT EXISTS idx_leaderboard_score       ON leaderboard (score DESC);
CREATE INDEX IF NOT EXISTS idx_leaderboard_player_name ON leaderboard (player_name);
CREATE INDEX IF NOT EXISTS idx_leaderboard_achieved_at ON leaderboard (achieved_at DESC);

-- ── Auto-update updated_at trigger ────────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_items_updated_at ON items;
CREATE TRIGGER trg_items_updated_at
    BEFORE UPDATE ON items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS trg_sessions_updated_at ON game_sessions;
CREATE TRIGGER trg_sessions_updated_at
    BEFORE UPDATE ON game_sessions
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
