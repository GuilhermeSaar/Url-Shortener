package com.urlshortener.shared.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtService {

    private final SecretKey signingKey;
    private final long expirationMillis;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.expiration-ms}") long expirationMillis
    ) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expirationMillis = expirationMillis;
    }

    public String generateToken(UserDetails user) {
        var now = new Date();

        return Jwts.builder()
                .subject(user.getUsername())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMillis))
                .signWith(signingKey)
                .compact();
    }

    public String extractSubject(String token) {
        return claims(token).getSubject();
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token, UserDetails user) {

        try {
            var claims = claims(token);
            var userNameToken = claims.getSubject();
            boolean isNotExpired = claims.getExpiration().after(new Date());
            boolean isValid = userNameToken.equals(user.getUsername());

            return isNotExpired && isValid;

        } catch (ExpiredJwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
