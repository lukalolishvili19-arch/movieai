package com.movieai.exception;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(ErrorCode code) {
        super(code);
    }
}
