package com.urlshortener.controller;

import com.urlshortener.controller.dto.UrlRequest;
import com.urlshortener.service.UrlService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/urls")
@AllArgsConstructor
public class ShortenUrlController {

    private final UrlService service;

    @PostMapping(value = "/shorten")
    public ResponseEntity<String> shortenUrl(@Valid @RequestBody UrlRequest request) {

        String shortCode = service.createShortenUrl(request.url());
        return ResponseEntity.status(HttpStatus.OK).body(shortCode);
    }
}
