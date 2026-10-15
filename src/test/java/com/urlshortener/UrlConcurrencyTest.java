package com.urlshortener;

import com.urlshortener.repository.UrlRepository;
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

@SpringBootTest
@AutoConfigureMockMvc
public class UrlConcurrencyTest {

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

        String[] parts = responseContent.split("/");
        var shortCode = parts[parts.length - 1];

        var numberOfThreads = 5000;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        var latch  = new CountDownLatch(numberOfThreads);

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
