package com.urlshortener.controller;

import com.urlshortener.url.controller.ShortenUrlController;
import com.urlshortener.url.dto.UrlStatsResponse;
import com.urlshortener.url.service.UrlService;
import com.urlshortener.user.model.UserModel;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

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
    private UserModel user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ShortenUrlController(service))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        user = new UserModel();
        user.setEmail("owner@example.com");
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnUrlStatsForAuthenticatedOwner() throws Exception {
        var code = "k1sd5o";
        var createdAt = LocalDateTime.of(2026, 10, 19, 12, 0);
        var originalUrl = "https://www.google.com";
        when(service.getUrlStats(user, code)).thenReturn(new UrlStatsResponse(createdAt, 7L, originalUrl));

        mockMvc.perform(get("/api/url/{code}", code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").value("2026-10-19T12:00:00"))
                .andExpect(jsonPath("$.clickCount").value(7))
                .andExpect(jsonPath("$.urlOriginal").value(originalUrl));
        verify(service).getUrlStats(user, code);
    }

    @Test
    void shouldReturnAuthenticatedUserHistory() throws Exception {
        when(service.findAllUrls(user.getEmail())).thenReturn(List.of());
        mockMvc.perform(get("/api/urls"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
        verify(service).findAllUrls(user.getEmail());
    }
}
