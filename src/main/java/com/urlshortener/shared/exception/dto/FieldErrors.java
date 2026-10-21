package com.urlshortener.shared.exception.dto;

public record FieldErrors(
        String field,
        String message)
{}
