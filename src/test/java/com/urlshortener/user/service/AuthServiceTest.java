package com.urlshortener.user.service;

import com.urlshortener.shared.security.JwtService;
import com.urlshortener.user.dto.LoginRequest;
import com.urlshortener.user.model.UserModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService service;

    @Test
    void shouldReturnJwtForValidCredentials() {
        var user = new UserModel();
        user.setEmail("user@example.com");
        user.setPassword("hashed-password");
        var request = new LoginRequest("user@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        var response = service.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }
}
