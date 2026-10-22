package com.urlshortener.user.service;

import com.urlshortener.user.exception.EmailAlreadyExistException;
import com.urlshortener.user.dto.RegisterRequest;
import com.urlshortener.user.domain.RoleUser;
import com.urlshortener.user.model.UserModel;
import com.urlshortener.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
