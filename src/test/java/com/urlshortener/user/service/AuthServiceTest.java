package com.urlshortener.user.service;

import com.urlshortener.shared.security.cookie.CookieUtil;
import com.urlshortener.shared.security.jwt.JwtService;
import com.urlshortener.user.dto.LoginRequest;
import com.urlshortener.user.model.UserModel;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

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
    private CookieUtil cookieUtil;

    @Mock
    private HttpServletResponse httpResponse;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService service;

    @Test
    void shouldSetJwtCookieForValidCredentials() {
        var user = new UserModel();
        user.setEmail("user@example.com");
        user.setPassword("hashed-password");
        var request = new LoginRequest("user@example.com", "password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        var cookie = ResponseCookie.from("accessToken", "jwt-token").httpOnly(true).build();
        when(cookieUtil.generateJwtCookie("jwt-token")).thenReturn(cookie);

        service.login(request, httpResponse);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
        verify(httpResponse).addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
