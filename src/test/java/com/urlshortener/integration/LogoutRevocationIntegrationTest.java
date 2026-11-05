package com.urlshortener.integration;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "security.jwt.secret=dGVzdC1qd3Qta2V5LXRvLWtlZXAtc3ByaW5nLWJvb3QtdGVzdHMtc2VsZi1jb250YWluZWQtYW5kLWRldGVybWluaXN0aWM=")
@AutoConfigureMockMvc
@Transactional
class LogoutRevocationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void logoutRevokesAllUserSessionsButNotOtherAccounts() throws Exception {
        register("revocation-owner@example.com");
        register("revocation-other@example.com");
        var firstSession = login("revocation-owner@example.com");
        var secondSession = login("revocation-owner@example.com");
        var otherUser = login("revocation-other@example.com");

        mockMvc.perform(get("/api/auth/session").cookie(firstSession)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/session").cookie(secondSession)).andExpect(status().isNoContent());
        mockMvc.perform(post("/logout").cookie(firstSession))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        mockMvc.perform(get("/api/auth/session").cookie(firstSession)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/urls").cookie(secondSession)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/auth/session").cookie(otherUser)).andExpect(status().isNoContent());

        var newSession = login("revocation-owner@example.com");
        mockMvc.perform(get("/api/auth/session").cookie(newSession)).andExpect(status().isNoContent());
        // A previously revoked token cannot log out a later session.
        mockMvc.perform(post("/logout").cookie(firstSession)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/auth/session").cookie(newSession)).andExpect(status().isNoContent());
        mockMvc.perform(post("/logout")).andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
        mockMvc.perform(get("/api/auth/session").cookie(newSession)).andExpect(status().isNoContent());
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(credentials(email)))
                .andExpect(status().isCreated());
    }

    private Cookie login(String email) throws Exception {
        var response = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(credentials(email)))
                .andExpect(status().isOk()).andReturn().getResponse();
        var header = response.getHeader(HttpHeaders.SET_COOKIE);
        return new Cookie("accessToken", header.substring("accessToken=".length(), header.indexOf(';')));
    }

    private String credentials(String email) {
        return "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
    }
}
