package com.urlshortener.integration;

import com.jayway.jsonpath.JsonPath;
import com.urlshortener.url.repository.UrlRepository;
import jakarta.servlet.http.Cookie;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "security.jwt.secret=dGVzdC1qd3Qta2V5LXRvLWtlZXAtc3ByaW5nLWJvb3QtdGVzdHMtc2VsZi1jb250YWluZWQtYW5kLWRldGVybWluaXN0aWM=")
@AutoConfigureMockMvc
@Transactional
class UrlStatsFlowIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UrlRepository repository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void shouldKeepHistoryAndStatsPrivateAndRedirectPublicly() throws Exception {
        var owner = registerAndLogin("owner-flow@example.com");
        var other = registerAndLogin("other-flow@example.com");
        var ownerUrl = "https://example.com/owner";
        var otherUrl = "https://example.com/other";
        var anonymousUrl = "https://example.com/anonymous";

        var ownerPath = createLink(ownerUrl, owner);
        var otherPath = createLink(otherUrl, other);
        var anonymousPath = createLink(anonymousUrl, null);
        var ownerCode = codeFrom(ownerPath);
        var otherCode = codeFrom(otherPath);
        var anonymousCode = codeFrom(anonymousPath);

        assertThat(repository.findByCode(ownerCode).orElseThrow().getUser().getEmail())
                .isEqualTo("owner-flow@example.com");
        assertThat(repository.findByCode(anonymousCode).orElseThrow().getUser()).isNull();

        mockMvc.perform(get("/api/urls")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/urls").cookie(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value(ownerCode))
                .andExpect(jsonPath("$[0].urlOriginal").value(ownerUrl));
        mockMvc.perform(get("/api/urls").cookie(other))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].code").value(otherCode))
                .andExpect(jsonPath("$[0].urlOriginal").value(otherUrl));

        mockMvc.perform(get("/api/url/{code}", ownerCode)).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/url/{code}", ownerCode).cookie(owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.urlOriginal").value(ownerUrl));
        mockMvc.perform(get("/api/url/{code}", otherCode).cookie(owner)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/url/{code}", anonymousCode).cookie(owner)).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/url/{code}", ownerCode).cookie(other)).andExpect(status().isNotFound());

        mockMvc.perform(get(ownerPath)).andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, ownerUrl));
        mockMvc.perform(get(anonymousPath)).andExpect(status().isFound())
                .andExpect(header().string(HttpHeaders.LOCATION, anonymousUrl));
        entityManager.flush();
        entityManager.clear();
        mockMvc.perform(get("/api/url/{code}", ownerCode).cookie(owner))
                .andExpect(status().isOk()).andExpect(jsonPath("$.clickCount").value(1));
    }

    private Cookie registerAndLogin(String email) throws Exception {
        var body = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        var response = mockMvc.perform(post("/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse();
        var cookie = response.getCookie("accessToken");
        if (cookie != null) return cookie;
        var setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        return new Cookie("accessToken", setCookie.substring("accessToken=".length(), setCookie.indexOf(';')));
    }

    private String createLink(String originalUrl, Cookie cookie) throws Exception {
        var request = post("/api/shorten").contentType(MediaType.APPLICATION_JSON)
                .content("{\"url\":\"" + originalUrl + "\"}");
        if (cookie != null) request.cookie(cookie);
        var response = mockMvc.perform(request).andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return URI.create((String) JsonPath.read(response, "$.response")).getPath();
    }

    private String codeFrom(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}
