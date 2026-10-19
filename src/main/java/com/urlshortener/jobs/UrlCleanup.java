package com.urlshortener.jobs;

import com.urlshortener.repository.UrlRepository;
import lombok.AllArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@AllArgsConstructor
public class UrlCleanup {

    private final UrlRepository urlRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deleteExpiredUrls() {

        urlRepository.deleteByExpiresAtBefore(Instant.now());
    }
}
