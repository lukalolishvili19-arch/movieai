package com.movieai.exception;

public class InvalidParameterException extends ApiException {

    private final String field;

    public InvalidParameterException(String field, String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.field = field;
    }

    public String field() {
        return field;
    }
}
