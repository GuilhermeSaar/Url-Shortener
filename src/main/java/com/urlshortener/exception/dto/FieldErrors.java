package com.urlshortener.exception.dto;

public record FieldErrors(
        String field,
        String message)
{}
