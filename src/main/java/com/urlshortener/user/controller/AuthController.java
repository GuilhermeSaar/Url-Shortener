package com.urlshortener.user.controller;

import com.urlshortener.user.dto.LoginRequest;
import com.urlshortener.user.dto.RegisterRequest;
import com.urlshortener.user.service.AuthService;
import com.urlshortener.user.service.UserService;
import com.urlshortener.user.model.UserModel;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        authService.login(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserModel user, HttpServletResponse response) {
        authService.logout(user, response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/auth/session")
    public ResponseEntity<Void> session() {
        return ResponseEntity.noContent().build();
    }
}
