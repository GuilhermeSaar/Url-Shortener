package com.urlshortener.shared.security.cookie;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Arrays;

@Component
public class CookieUtil {

    private static final String COOKIE_NAME = "accessToken";

    private final Duration expiration;
    private final boolean secure;
    private final String sameSite;

    public CookieUtil(
            @Value("${security.jwt.expiration-ms}") long expirationMillis,
            @Value("${security.cookie.secure}") boolean secure,
            @Value("${security.cookie.same-site}") String sameSite
    ) {
        this.expiration = Duration.ofMillis(expirationMillis);
        this.secure = secure;
        this.sameSite = sameSite;
    }

    public ResponseCookie generateJwtCookie(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(expiration)
                .sameSite(sameSite)
                .build();
    }

    public ResponseCookie clearJwtCookie() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite(sameSite)
                .build();
    }

    public String recoverTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

}
