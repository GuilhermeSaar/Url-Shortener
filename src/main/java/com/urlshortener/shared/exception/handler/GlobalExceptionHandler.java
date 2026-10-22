package com.urlshortener.shared.exception.handler;

import com.urlshortener.user.exception.FailedRetrieveAuthenticatedException;
import com.urlshortener.user.exception.EmailAlreadyExistException;
import com.urlshortener.url.exception.UrlNotFoundException;
import com.urlshortener.shared.exception.dto.FieldErrors;
import com.urlshortener.shared.exception.dto.ResponseError;
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

    @ExceptionHandler({UrlNotFoundException.class})
    public ResponseEntity<ResponseError> urlNotFoundException(UrlNotFoundException ex, HttpServletRequest request) {

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
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({EmailAlreadyExistException.class})
    public ResponseEntity<ResponseError> emailAlreadyExist(EmailAlreadyExistException ex, HttpServletRequest request) {

        var status = HttpStatus.CONFLICT;

        var response = new ResponseError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURI(),
                List.of(new FieldErrors("email", ex.getMessage()))
        );
        return ResponseEntity.status(status).body(response);
    }

    @ExceptionHandler({FailedRetrieveAuthenticatedException.class})
    public ResponseEntity<ResponseError> failedRetrieveAuthenticated(FailedRetrieveAuthenticatedException ex, HttpServletRequest request) {

        var status = HttpStatus.UNAUTHORIZED;

        var response = new ResponseError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                request.getRequestURI(),
                List.of(new FieldErrors(null, ex.getMessage()))
        );
        return ResponseEntity.status(status).body(response);
    }
}

