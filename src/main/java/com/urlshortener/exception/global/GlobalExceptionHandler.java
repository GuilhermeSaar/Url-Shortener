package com.urlshortener.exception.global;

import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.exception.dto.FieldErrors;
import com.urlshortener.exception.dto.ResponseError;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({ResourceNotFoundException.class})
    public ResponseEntity<ResponseError> urlNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.NOT_FOUND;

        var response = new ResponseError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURI(),
                List.of(new FieldErrors("shortcode", ex.getMessage()))
        );

        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ResponseError> validationException(MethodArgumentNotValidException ex, HttpServletRequest request) {

        HttpStatus status = HttpStatus.BAD_REQUEST;

        List<FieldErrors> errors = new ArrayList<>();

        for (FieldError err : ex.getBindingResult().getFieldErrors()) {

            String field = err.getField();
            String error =  err.getDefaultMessage();

            errors.add(new FieldErrors(field, error));
        }

        var response = new ResponseError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
