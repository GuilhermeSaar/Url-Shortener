package com.urlshortener.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;

import java.net.URI;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = "security.jwt.secret=dGVzdC1qd3Qta2V5LXRvLWtlZXAtc3ByaW5nLWJvb3QtdGVzdHMtc2VsZi1jb250YWluZWQtYW5kLWRldGVybWluaXN0aWM=")
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
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"stats@example.com","password":"password123"}
                                """))
                .andExpect(status().isCreated());

        var loginResponse = mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"stats@example.com","password":"password123"}
                                """))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("accessToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=3600")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Strict")))
                .andReturn()
                .getResponse();

        var setCookie = loginResponse.getHeader(HttpHeaders.SET_COOKIE);
        var token = setCookie.substring("accessToken=".length(), setCookie.indexOf(';'));
        var jwtCookie = new Cookie("accessToken", token);

        mockMvc.perform(get("/api/url/{code}", shortCode)
                        .cookie(jwtCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date").isNotEmpty())
                .andExpect(jsonPath("$.clickCount").value(0))
                .andExpect(jsonPath("$.urlOriginal").value(originalUrl));

        mockMvc.perform(get("/api/auth/session").cookie(jwtCookie))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/logout").cookie(jwtCookie))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }
}
