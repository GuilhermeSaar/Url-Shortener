package com.urlshortener.user.exception;

public class FailedRetrieveAuthenticatedException extends RuntimeException {
    public FailedRetrieveAuthenticatedException(String message) {
        super(message);
    }
}
