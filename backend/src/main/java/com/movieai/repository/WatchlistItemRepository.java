package com.movieai.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.movieai.dto.MediaType;
import com.movieai.entity.WatchlistItem;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WatchlistItemRepository extends JpaRepository<WatchlistItem, UUID> {

    Page<WatchlistItem> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    Page<WatchlistItem> findByUserIdAndMediaTypeOrderByCreatedAtDesc(UUID userId, MediaType mediaType, Pageable pageable);

    Optional<WatchlistItem> findByUserIdAndMediaTypeAndTmdbId(UUID userId, MediaType mediaType, long tmdbId);

    boolean existsByUserIdAndMediaTypeAndTmdbId(UUID userId, MediaType mediaType, long tmdbId);

    List<WatchlistItem> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @Modifying
    @Query("delete from WatchlistItem w where w.userId = :userId and w.mediaType = :mediaType and w.tmdbId = :tmdbId")
    int deleteItem(@Param("userId") UUID userId, @Param("mediaType") MediaType mediaType, @Param("tmdbId") long tmdbId);
}
