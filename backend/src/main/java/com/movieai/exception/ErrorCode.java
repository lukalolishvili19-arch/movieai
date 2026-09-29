package com.movieai.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    TMDB_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "Movie data is temporarily unavailable."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "The requested resource was not found."),
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "This movie could not be found."),
    TV_NOT_FOUND(HttpStatus.NOT_FOUND, "This TV show could not be found."),
    PERSON_NOT_FOUND(HttpStatus.NOT_FOUND, "This person could not be found."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "The request contains invalid parameters."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Authentication is required."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Your session has expired. Please sign in again."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "Incorrect email or password."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "Your session is no longer valid. Please sign in again."),
    EMAIL_ALREADY_REGISTERED(HttpStatus.CONFLICT, "An account with this email already exists."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "You are not allowed to perform this action."),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "Too many requests. Please slow down and try again shortly."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "This HTTP method is not supported for this endpoint."),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported content type."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus status() {
        return status;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
