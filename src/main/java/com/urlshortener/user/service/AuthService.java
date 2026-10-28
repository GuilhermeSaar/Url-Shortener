package com.urlshortener.user.service;

import com.urlshortener.shared.security.cookie.CookieUtil;
import com.urlshortener.shared.security.jwt.JwtService;
import com.urlshortener.user.dto.LoginRequest;
import com.urlshortener.user.exception.FailedRetrieveAuthenticatedException;
import com.urlshortener.user.model.UserModel;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authentication;
    private final JwtService jwt;
    private final CookieUtil cookieUtil;

    public void login(LoginRequest request, HttpServletResponse response) {

        var auth = authentication.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        if  (auth.getPrincipal() instanceof UserModel user) {
            var token = jwt.generateToken(user);

            ResponseCookie cookie = cookieUtil.generateJwtCookie(token);
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
            return;
        }

        throw  new FailedRetrieveAuthenticatedException("Falha ao recuperar o usuário autenticado.");
    }

    public void logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.clearJwtCookie().toString());
    }
}
