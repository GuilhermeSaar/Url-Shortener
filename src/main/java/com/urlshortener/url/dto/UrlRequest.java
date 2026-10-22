package com.urlshortener.url.dto;

import com.urlshortener.url.dto.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;

public record UrlRequest(

        @NotBlank(message = "A url nao pode estar vazia")
        @ValidUrl
        String url
) {}