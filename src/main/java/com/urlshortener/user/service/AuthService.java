package com.urlshortener.user.service;

import com.urlshortener.shared.security.cookie.CookieUtil;
import com.urlshortener.shared.security.jwt.JwtService;
import com.urlshortener.user.dto.LoginRequest;
import com.urlshortener.user.exception.FailedRetrieveAuthenticatedException;
import com.urlshortener.user.model.UserModel;
import com.urlshortener.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authentication;
    private final JwtService jwt;
    private final CookieUtil cookieUtil;
    private final UserRepository userRepository;

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

    @Transactional
    public void logout(UserModel user, HttpServletResponse response) {
        if (user != null) {
            userRepository.incrementTokenVersion(user.getId(), user.getTokenVersion());
        }
        response.addHeader(HttpHeaders.SET_COOKIE, cookieUtil.clearJwtCookie().toString());
    }
}
