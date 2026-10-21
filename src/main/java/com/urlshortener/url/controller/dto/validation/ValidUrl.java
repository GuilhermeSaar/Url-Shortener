package com.urlshortener.url.controller.dto.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Constraint(validatedBy = UrlValidator.class)
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUrl {

    String message() default "A url deve possuir um host válido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
