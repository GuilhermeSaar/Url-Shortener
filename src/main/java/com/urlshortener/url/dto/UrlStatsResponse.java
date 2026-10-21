package com.urlshortener.url.dto;

import java.time.LocalDateTime;

public record UrlStatsResponse(LocalDateTime date, Long clickCount, String urlOriginal) {
}
