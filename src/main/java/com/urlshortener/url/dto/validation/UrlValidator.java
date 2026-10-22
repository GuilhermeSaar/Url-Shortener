package com.urlshortener.url.dto.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlValidator implements ConstraintValidator<ValidUrl, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isBlank()) return true;

        try {
            var uri = new URI(value);
            String host = uri.getHost();


            return uri.getScheme() != null
                    && (uri.getScheme().equalsIgnoreCase("http")
                    || uri.getScheme().equalsIgnoreCase("https"))
                    && host != null
                    && !host.isBlank();

        } catch (URISyntaxException e) { return false; }
    }
}
