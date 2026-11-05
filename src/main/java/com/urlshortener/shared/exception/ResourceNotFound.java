package com.urlshortener.shared.exception;

public class ResourceNotFound extends RuntimeException {
    public ResourceNotFound(String message) {
        super(message);
    }
}
