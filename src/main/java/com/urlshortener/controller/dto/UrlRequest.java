package com.urlshortener.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.URL;

public record UrlRequest(

        @NotBlank(message = "A url nao pode estar vazia")
        @URL(message = "A url fornecida é inválida")
        @Pattern(
                regexp = "^(http|https)://.*",
                flags = Pattern.Flag.CASE_INSENSITIVE,
                message = "A url deve utilizar o protocolo http ou https"
        )
        String url
) {
}
