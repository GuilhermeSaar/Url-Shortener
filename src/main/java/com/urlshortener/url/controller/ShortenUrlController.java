package com.urlshortener.url.controller;

import com.urlshortener.url.dto.UrlRequest;
import com.urlshortener.url.dto.UrlResponse;
import com.urlshortener.url.dto.UrlStatsResponse;
import com.urlshortener.url.service.UrlService;
import com.urlshortener.user.model.UserModel;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class ShortenUrlController {

    private final UrlService service;

    @PostMapping(value = "/shorten")
    public ResponseEntity<UrlResponse> shortenUrl(@Valid @RequestBody UrlRequest request, Authentication authentication) {

        var userAuth = authentication != null &&
                authentication.isAuthenticated()
                ? authentication.getName() : null;

        String shortCode = service.createShortenUrl(request.url(), userAuth);
        String longUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/{code}")
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

    @GetMapping(value = "/url/{code}")
    public ResponseEntity<UrlStatsResponse> getUrlStats(@AuthenticationPrincipal UserModel user, @PathVariable String code) {

        var response = service.getUrlStats(user, code);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/urls")
    public ResponseEntity<List<UrlStatsResponse>> getUrlStats(@AuthenticationPrincipal UserModel user) {

        var response = service.findAllUrls(user.getEmail());
        return ResponseEntity.ok(response);
    }
}
