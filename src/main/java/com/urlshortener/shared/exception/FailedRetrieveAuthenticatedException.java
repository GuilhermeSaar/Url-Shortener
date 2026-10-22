package com.urlshortener.shared.exception;

public class FailedRetrieveAuthenticatedException extends RuntimeException {
    public FailedRetrieveAuthenticatedException(String message) {
        super(message);
    }
}
