package com.movieai.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.movieai.dto.MediaType;

@Entity
@Table(name = "watchlist_items",
        uniqueConstraints = @UniqueConstraint(name = "uq_watchlist_user_media",
                columnNames = {"user_id", "media_type", "tmdb_id"}))
public class WatchlistItem extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Convert(converter = MediaTypeConverter.class)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    @Column(name = "tmdb_id", nullable = false)
    private long tmdbId;

    protected WatchlistItem() {
    }

    public WatchlistItem(UUID userId, MediaType mediaType, long tmdbId) {
        this.userId = userId;
        this.mediaType = mediaType;
        this.tmdbId = tmdbId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public long getTmdbId() {
        return tmdbId;
    }
}
