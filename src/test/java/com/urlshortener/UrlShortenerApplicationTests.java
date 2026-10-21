package com.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "security.jwt.secret=dGVzdC1qd3Qta2V5LXRvLWtlZXAtc3ByaW5nLWJvb3QtdGVzdHMtc2VsZi1jb250YWluZWQtYW5kLWRldGVybWluaXN0aWM=")
class UrlShortenerApplicationTests {

    @Test
    void contextLoads() {
    }

}
