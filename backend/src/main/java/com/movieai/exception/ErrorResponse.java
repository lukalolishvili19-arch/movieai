package com.movieai.exception;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErrorResponse", description = "Uniform error envelope returned by every endpoint on failure.")
public record ErrorResponse(
        @Schema(example = "false") boolean success,
        ErrorBody error) {

    public static ErrorResponse of(ErrorCode code, String message) {
        return new ErrorResponse(false, new ErrorBody(code.name(), message, null));
    }

    public static ErrorResponse of(ErrorCode code, String message, List<FieldViolation> details) {
        return new ErrorResponse(false, new ErrorBody(code.name(), message, details));
    }

    @Schema(name = "ErrorBody")
    public record ErrorBody(
            @Schema(example = "TMDB_UNAVAILABLE") String code,
            @Schema(example = "Movie data is temporarily unavailable.") String message,
            @JsonInclude(JsonInclude.Include.NON_EMPTY) List<FieldViolation> details) {
    }

    @Schema(name = "FieldViolation")
    public record FieldViolation(
            @Schema(example = "minRating") String field,
            @Schema(example = "must be less than or equal to 10") String message) {
    }
}
