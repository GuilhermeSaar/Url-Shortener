package com.urlshortener.url.dto;

import com.urlshortener.url.model.UrlModel;

import java.time.LocalDateTime;

public record UrlStatsResponse(LocalDateTime date, Long clickCount, String urlOriginal) {

    public UrlStatsResponse(UrlModel data) {

        this (
                data.getCreatedAt(),
                data.getClickCount(),
                data.getUrlOriginal()
        );
    }
}
