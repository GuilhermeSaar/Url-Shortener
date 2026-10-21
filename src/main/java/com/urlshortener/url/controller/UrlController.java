package com.urlshortener.url.controller;

import com.urlshortener.url.dto.UrlStatsResponse;
import com.urlshortener.url.service.UrlService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/")
@AllArgsConstructor
public class UrlController {

    private final UrlService service;

    @GetMapping(value = "url/{code}")
    public ResponseEntity<UrlStatsResponse> getUrlStats(@PathVariable String code) {

        var response = service.getUrlStats(code);
        return ResponseEntity.ok(response);
    }
}
