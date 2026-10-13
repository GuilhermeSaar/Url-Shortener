package com.urlshortener.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqids.Sqids;

@Configuration
public class SqidsConfig {

    @Bean
    public Sqids sqids() {
        String BASE_62 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        return Sqids.builder()
                .minLength(6)
                .alphabet(BASE_62)
                .build();
    }
}
