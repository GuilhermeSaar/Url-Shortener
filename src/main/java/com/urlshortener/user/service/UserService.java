package com.urlshortener.user.service;

import com.urlshortener.shared.exception.EmailAlreadyExistException;
import com.urlshortener.shared.exception.InvalidCredentialsException;
import com.urlshortener.shared.security.JwtService;
import com.urlshortener.user.controller.dto.LoginRequest;
import com.urlshortener.user.controller.dto.RegisterRequest;
import com.urlshortener.user.controller.dto.TokenResponse;
import com.urlshortener.user.domain.RoleUser;
import com.urlshortener.user.model.UserModel;
import com.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistException("E-mail já cadastrado");
        }

        UserModel user = new UserModel();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(RoleUser.ROLE_USER);
        repository.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
