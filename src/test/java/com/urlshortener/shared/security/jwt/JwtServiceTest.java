package com.urlshortener.shared.security.jwt;

import com.urlshortener.user.model.UserModel;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private static final String SECRET = "dGVzdC1qd3Qta2V5LXRvLWtlZXAtc3ByaW5nLWJvb3QtdGVzdHMtc2VsZi1jb250YWluZWQtYW5kLWRldGVybWluaXN0aWM=";
    private final JwtService jwt = new JwtService(SECRET, 3600000);

    @Test
    void acceptsCurrentVersionAndRejectsItAfterVersionChanges() {
        var user = new UserModel();
        user.setEmail("user@example.com");
        var token = jwt.generateToken(user);

        assertThat(jwt.isTokenValid(token, user)).isTrue();
        user.setTokenVersion(1L);
        assertThat(jwt.isTokenValid(token, user)).isFalse();
        assertThat(jwt.isTokenValid(jwt.generateToken(user), user)).isTrue();
    }

    @Test
    void rejectsLegacyTokenWithoutVersion() {
        var user = new UserModel();
        user.setEmail("user@example.com");
        var token = Jwts.builder()
                .subject(user.getUsername())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertThat(jwt.isTokenValid(token, user)).isFalse();
    }
}
