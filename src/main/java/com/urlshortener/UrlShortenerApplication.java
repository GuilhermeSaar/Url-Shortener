package com.urlshortener;

import com.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class UrlShortenerApplication implements CommandLineRunner {

    @Autowired
    private UrlService service;

    public static void main(String[] args) {
        SpringApplication.run(UrlShortenerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

//        String url = "https://www.google.com";
//
//        for (int i = 0; i <= 1000000; i++) {
//            service.createShortenUrl(url);
//        }
    }
}
