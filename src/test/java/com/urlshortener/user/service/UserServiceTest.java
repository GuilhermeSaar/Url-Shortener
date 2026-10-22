package com.urlshortener.user.service;

import com.urlshortener.user.dto.RegisterRequest;
import com.urlshortener.user.domain.RoleUser;
import com.urlshortener.user.model.UserModel;
import com.urlshortener.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    @Test
    void shouldHashPasswordBeforeRegisteringUser() {
        var request = new RegisterRequest("USER@EXAMPLE.COM", "password123");
        when(repository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");

        service.register(request);

        var userCaptor = ArgumentCaptor.forClass(UserModel.class);
        verify(repository).save(userCaptor.capture());
        var user = userCaptor.getValue();
        assertThat(user.getEmail()).isEqualTo("user@example.com");
        assertThat(user.getPassword()).isEqualTo("hashed-password");
        assertThat(user.getRole()).isEqualTo(RoleUser.ROLE_USER);
    }

}
