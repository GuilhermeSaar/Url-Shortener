package com.urlshortener.url.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqids.Sqids;

@Configuration
public class SqidsConfig {

    @Bean
    public Sqids sqids() {
        String BASE_62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        return Sqids.builder()
                .minLength(6)
                .alphabet(BASE_62)
                .build();
    }
}
