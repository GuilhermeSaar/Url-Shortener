package com.urlshortener.url.dto;

import com.urlshortener.url.model.UrlModel;
import java.time.LocalDateTime;

public record UrlHistoryResponse(String code, LocalDateTime date, Long clickCount, String urlOriginal) {
    public UrlHistoryResponse(UrlModel url) {
        this(url.getCode(), url.getCreatedAt(), url.getClickCount(), url.getUrlOriginal());
    }
}
