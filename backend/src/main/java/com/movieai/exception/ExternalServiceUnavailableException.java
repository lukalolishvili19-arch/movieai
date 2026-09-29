package com.movieai.exception;

/** The external data provider could not be reached, timed out, or rejected the request. */
public class ExternalServiceUnavailableException extends ApiException {

    private final String internalReason;

    public ExternalServiceUnavailableException(String internalReason, Throwable cause) {
        super(ErrorCode.TMDB_UNAVAILABLE, ErrorCode.TMDB_UNAVAILABLE.defaultMessage(), cause);
        this.internalReason = internalReason;
    }

    /** For logs only; never returned to clients. */
    public String internalReason() {
        return internalReason;
    }
}
