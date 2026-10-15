package com.urlshortener.controller.dto;

import com.urlshortener.controller.dto.validation.ValidUrl;
import jakarta.validation.constraints.NotBlank;

public record UrlRequest(

        @NotBlank(message = "A url nao pode estar vazia")
        @ValidUrl
        String url
) {}