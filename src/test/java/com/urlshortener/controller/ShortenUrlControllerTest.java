package com.urlshortener.controller;

import com.urlshortener.url.controller.ShortenUrlController;
import com.urlshortener.url.service.UrlService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ShortenUrlControllerTest {

    @Mock
    private UrlService service;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ShortenUrlController(service)).build();
    }

    @Test
    void shouldCreateShortUrlAndReturnCreated() throws Exception {
        var shortCode = "k1sd5o";
        var originalUrl = "https://www.google.com";
        when(service.createShortenUrl(originalUrl)).thenReturn(shortCode);

        mockMvc.perform(post("/shorten")
                        .contentType("application/json")
                        .content("""
                                {"url":"https://www.google.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.response").value("http://localhost/" + shortCode));

        verify(service).createShortenUrl(originalUrl);
    }

    @Test
    void shouldRedirectToOriginalUrl() throws Exception {
        var shortCode = "k1sd5o";
        var originalUrl = "https://www.google.com";
        when(service.originalUrl(shortCode)).thenReturn(originalUrl);

        mockMvc.perform(get("/{shortCode}", shortCode))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));

        verify(service).originalUrl(shortCode);
    }
}
