package com.movieai.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A batch of movie picks. Send the same seed with page+1 to get the next, different batch.")
public record RecommendationResponse(
        List<MovieSummary> results,
        @Schema(example = "1") int page,
        @Schema(description = "Shuffle seed; reuse it to continue the same randomized sequence") long seed,
        @Schema(description = "Whether a further, not-yet-seen batch exists") boolean hasMore,
        @Schema(description = "Number of movies matching the filters") int totalMatches) {
}
