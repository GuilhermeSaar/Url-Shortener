package com.urlshortener.shared.security.jwt;

import com.urlshortener.shared.security.cookie.CookieUtil;
import com.urlshortener.user.service.UserDetailService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final UserDetailService service;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var token = cookieUtil.recoverTokenFromCookie(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            var userEmail = jwt.extractSubject(token);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var user = this.service.loadUserByUsername(userEmail);

                if (jwt.isTokenValid(token, user)) {
                    var auth= new  UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
            SecurityContextHolder.clearContext();
            response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.clearJwtCookie().toString());
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
