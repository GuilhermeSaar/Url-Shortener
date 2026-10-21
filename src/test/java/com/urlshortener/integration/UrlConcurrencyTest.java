package com.urlshortener.integration;

import com.jayway.jsonpath.JsonPath;
import com.urlshortener.url.repository.UrlRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "security.jwt.secret=dGVzdC1qd3Qta2V5LXRvLWtlZXAtc3ByaW5nLWJvb3QtdGVzdHMtc2VsZi1jb250YWluZWQtYW5kLWRldGVybWluaXN0aWM=")
@AutoConfigureMockMvc
class UrlConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlRepository repository;

    @Test
    void shouldIncrementCounterWithThreadConcurrency() throws Exception {
        var responseContent = mockMvc.perform(post("/shorten")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\": \"https://github.com\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        var shortUrl = java.net.URI.create((String) JsonPath.read(responseContent, "$.response"));
        var path = shortUrl.getPath();
        var shortCode = path.substring(path.lastIndexOf('/') + 1);

        var numberOfThreads = 5000;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        var latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            service.submit(() -> {
                try {
                    mockMvc.perform(get("/" + shortCode))
                            .andExpect(status().isFound());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean finishedInTime = latch.await(10, TimeUnit.SECONDS);
        service.shutdown();

        assertThat(finishedInTime).isTrue();
        var urlModel = repository.findByCode(shortCode).orElseThrow();

        assertThat(urlModel.getClickCount()).isEqualTo(numberOfThreads);
    }
}
