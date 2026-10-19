package com.urlshortener.controller;

import com.urlshortener.dto.UrlStatsResponse;
import com.urlshortener.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UrlControllerTest {

    @Mock
    private UrlService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new UrlController(service)).build();
    }

    @Test
    void shouldReturnUrlStatsForCode() throws Exception {
        var code = "k1sd5o";
        var createdAt = LocalDateTime.of(2026, 10, 19, 12, 0);
        var clickCount = 7L;
        var originalUrl = "https://www.google.com";
        when(service.getUrlStats(code)).thenReturn(
                new UrlStatsResponse(createdAt, clickCount, originalUrl));

        mockMvc.perform(get("/api/url/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-10-19T12:00:00"))
                .andExpect(jsonPath("$.clickCount").value(clickCount))
                .andExpect(jsonPath("$.urlOriginal").value(originalUrl));

        verify(service).getUrlStats(code);
    }
}
