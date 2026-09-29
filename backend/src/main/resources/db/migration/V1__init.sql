-- Application-owned data only. Movie/TV/person metadata stays in TMDB and the cache.

CREATE TABLE users (
    id            UUID PRIMARY KEY,
    email         VARCHAR(320) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE refresh_tokens (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL,
    family_id    UUID NOT NULL,
    token_hash   VARCHAR(64) NOT NULL,
    expires_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at   TIMESTAMP WITH TIME ZONE,
    replaced_by  UUID,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_tokens_user ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_tokens_expires ON refresh_tokens (expires_at);

CREATE TABLE watchlist_items (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    media_type  VARCHAR(10) NOT NULL,
    tmdb_id     BIGINT NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT fk_watchlist_items_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_watchlist_user_media UNIQUE (user_id, media_type, tmdb_id),
    CONSTRAINT ck_watchlist_media_type CHECK (media_type IN ('movie', 'tv')),
    CONSTRAINT ck_watchlist_tmdb_id CHECK (tmdb_id > 0)
);

CREATE INDEX idx_watchlist_items_user_created ON watchlist_items (user_id, created_at DESC);
