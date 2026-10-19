package com.urlshortener.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UrlStatsFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateUrlAndReturnItsStatsThroughApi() throws Exception {
        var originalUrl = "https://www.google.com/search?q=url-shortener";

        var shortenResponse = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"url":"https://www.google.com/search?q=url-shortener"}
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        var shortUrl = URI.create((String) JsonPath.read(shortenResponse, "$.response"));
        var shortCode = shortUrl.getPath().substring(shortUrl.getPath().lastIndexOf('/') + 1);

        mockMvc.perform(get("/api/url/{code}", shortCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").isNotEmpty())
                .andExpect(jsonPath("$.clickCount").value(0))
                .andExpect(jsonPath("$.urlOriginal").value(originalUrl));
    }
}
