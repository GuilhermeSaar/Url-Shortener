package com.urlshortener.url.controller;

import com.urlshortener.url.dto.UrlRequest;
import com.urlshortener.url.dto.UrlResponse;
import com.urlshortener.url.service.UrlService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@AllArgsConstructor
public class ShortenUrlController {

    private final UrlService service;

    @PostMapping(value = "/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@Valid @RequestBody UrlRequest request, Authentication authentication) {

        var userAuth = (authentication != null && authentication.isAuthenticated()
            && !authentication.getPrincipal().equals("anonymousUser"))
                ? authentication.getName() : null;

        String shortCode = service.createShortenUrl(request.url(), userAuth);
        String longUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/{code}")
                .buildAndExpand(shortCode)
                .toUriString();

        return ResponseEntity.status(HttpStatus.CREATED).body(new UrlResponse(longUrl));
    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String shortCode) {

        HttpHeaders headers = new HttpHeaders();

        var originalUrl = service.originalUrl(shortCode);

        headers.setLocation(URI.create(originalUrl));

        return ResponseEntity.status(HttpStatus.FOUND).headers(headers).build();
    }
}
