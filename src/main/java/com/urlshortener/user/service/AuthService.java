package com.urlshortener.user.service;

import com.urlshortener.shared.security.JwtService;
import com.urlshortener.user.dto.LoginRequest;
import com.urlshortener.user.dto.TokenResponse;
import com.urlshortener.user.exception.FailedRetrieveAuthenticatedException;
import com.urlshortener.user.model.UserModel;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AuthService {

    private final AuthenticationManager authentication;
    private final JwtService jwt;

    public TokenResponse login(LoginRequest request) {

        var auth = authentication.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        if  (auth.getPrincipal() instanceof UserModel user) {
            var token = jwt.generateToken(user);
            return new TokenResponse(token);
        }

        throw  new FailedRetrieveAuthenticatedException("Falha ao recuperar o usuário autenticado.");
    }
}
