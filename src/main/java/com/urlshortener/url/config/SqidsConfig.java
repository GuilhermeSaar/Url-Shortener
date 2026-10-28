package com.urlshortener.url.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqids.Sqids;

@Configuration
public class SqidsConfig {

    @Bean
    public Sqids sqids(@Value("${SQIDS_ALPHABET}") String alphabet) {
        return Sqids.builder()
                .minLength(6)
                .alphabet(alphabet)
                .build();
    }
}
